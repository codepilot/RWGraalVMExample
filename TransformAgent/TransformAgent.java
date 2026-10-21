import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.MethodModel;
import java.lang.classfile.TypeAnnotation;
import java.lang.classfile.TypeAnnotation.TargetInfo;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.SourceFileAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@SuppressWarnings("unused")
public final class TransformAgent {
  static final ClassDesc classDesc_ObjectHandle = ClassDesc.of("org.graalvm.nativeimage", "ObjectHandle");
  static final ClassDesc classDesc_ObjectHandles = ClassDesc.of("org.graalvm.nativeimage", "ObjectHandles");
  static final ClassDesc classDesc_IsolateThread = ClassDesc.of("org.graalvm.nativeimage", "IsolateThread");
  static final ClassDesc classDesc_CTypedef = ClassDesc.of("org.graalvm.nativeimage.c.type", "CTypedef");
  static final ClassDesc classDesc_CEntryPoint = ClassDesc.of("org.graalvm.nativeimage.c.function", "CEntryPoint");
  static final List<TypeAnnotation.TypePathComponent> targetPath = List.of();

  static final int getRank(ClassDesc desc) {
    int rank = 0;
    while (desc.isArray()) {
      rank++;
      desc = desc.componentType();
    }
    return rank;
  }

  static final ClassDesc getBaseComponentType(ClassDesc desc) {
    while (desc.isArray()) {
      desc = desc.componentType();
    }
    return desc;
  }

  static final String OBJECT_HANDLE_STRING(ClassDesc desc) {
    if(desc.isClassOrInterface()) {
      return ("OBJECT_HANDLE_" + desc.packageName() + "_" + desc.displayName()).replace('.', '_').replace('$', '_');
    }
    if(desc.isArray()) {
      final int rank = getRank(desc);
      final ClassDesc base = getBaseComponentType(desc);
      return "RANK_" + rank + "_" + base.packageName() + "_" + base.displayName().replace('.', '_').replace('$', '_');
    }
    return null;
  }

  static final void cb_ObjectHandles_getGlobal(final CodeBuilder codeBuilder, final int storeSlot) {
    codeBuilder.invokestatic(classDesc_ObjectHandles, "getGlobal", MethodTypeDesc.of(classDesc_ObjectHandles));
    codeBuilder.astore(storeSlot);
  }

  static final void cb_ObjectHandles_get(final CodeBuilder codeBuilder, final int ohs_slot, final int oh_slot, final ClassDesc classDescCast, final int storeSlot) {
    codeBuilder.aload(ohs_slot);
    codeBuilder.aload(oh_slot);
    codeBuilder.invokeinterface(classDesc_ObjectHandles, "get", MethodTypeDesc.of(ConstantDescs.CD_Object, classDesc_ObjectHandle));
    codeBuilder.checkcast(classDescCast);
    codeBuilder.astore(storeSlot);
  }

  static final void cb_ObjectHandles_create(final CodeBuilder codeBuilder, final int ohs_slot, final int obj_slot, final int storeSlot) {
    codeBuilder.aload(ohs_slot);
    codeBuilder.aload(obj_slot);
    codeBuilder.invokeinterface(classDesc_ObjectHandles, "create", MethodTypeDesc.of(classDesc_ObjectHandle, ConstantDescs.CD_Object));
    codeBuilder.astore(storeSlot);
  }

  static final void cb_ObjectHandles_destroy(final CodeBuilder codeBuilder, final int ohs_slot, final int oh_slot) {
    codeBuilder.aload(ohs_slot);
    codeBuilder.aload(oh_slot);
    codeBuilder.invokeinterface(classDesc_ObjectHandles, "destroy", MethodTypeDesc.of(ConstantDescs.CD_void, classDesc_ObjectHandle));
  }

  static final void cb_return(final CodeBuilder codeBuilder, final int return_slot) {
    codeBuilder.aload(return_slot);
    codeBuilder.areturn();
  }

  public final static MethodTypeDesc transformMethodTypeDesc(final ClassModel classModel, final MethodModel methodModel, final List<TypeAnnotation> typeAnnotations) {
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    MethodTypeDesc addedMethodTypeDesc = methodTypeDesc;
    final boolean isConstructor = methodModel.methodName().stringValue().equals("<init>");
    final boolean isStatic = methodModel.flags().has(AccessFlag.STATIC);

    if(isConstructor) {
      typeAnnotations.add(TypeAnnotation.of(
        TargetInfo.ofMethodReturn(),
        targetPath,
        Annotation.of(classDesc_CTypedef, AnnotationElement.ofString("name", OBJECT_HANDLE_STRING(classModel.thisClass().asSymbol())))
      ));
      addedMethodTypeDesc = addedMethodTypeDesc.changeReturnType(classDesc_ObjectHandle);
    } else if(!addedMethodTypeDesc.returnType().isPrimitive()) {
      typeAnnotations.add(TypeAnnotation.of(
        TargetInfo.ofMethodReturn(),
        targetPath,
        Annotation.of(classDesc_CTypedef, AnnotationElement.ofString("name", OBJECT_HANDLE_STRING(addedMethodTypeDesc.returnType())))
      ));
      addedMethodTypeDesc = addedMethodTypeDesc.changeReturnType(classDesc_ObjectHandle);
    }

    final int paramOffset = (isStatic | isConstructor)?1:2;

    addedMethodTypeDesc = addedMethodTypeDesc.insertParameterTypes(0, classDesc_IsolateThread);
    if((!isStatic) && (!isConstructor)) {
      addedMethodTypeDesc = addedMethodTypeDesc.insertParameterTypes(1, classDesc_ObjectHandle);
      typeAnnotations.add(TypeAnnotation.of(
        TargetInfo.ofMethodFormalParameter(1),
        targetPath,
        Annotation.of(classDesc_CTypedef,
        AnnotationElement.ofString("name", OBJECT_HANDLE_STRING(classModel.thisClass().asSymbol())))
      ));
    }

    final int addedMethodParameterCount = addedMethodTypeDesc.parameterCount();
    for(int parameterIndex = paramOffset; parameterIndex < addedMethodParameterCount; parameterIndex++) {
      final ClassDesc parameterN = addedMethodTypeDesc.parameterType(parameterIndex);
      if(!parameterN.isPrimitive()) {
        addedMethodTypeDesc = addedMethodTypeDesc.changeParameterType(parameterIndex, classDesc_ObjectHandle);
        typeAnnotations.add(TypeAnnotation.of(
          TargetInfo.ofMethodFormalParameter(parameterIndex),
          targetPath,
          Annotation.of(classDesc_CTypedef,
          AnnotationElement.ofString("name", OBJECT_HANDLE_STRING(parameterN)))
        ));
      }
    }

    return addedMethodTypeDesc;
  }

  public final static class XFM implements ClassTransform {
    final ClassModel classModel;
    final ClassDesc classDesc;
    final String hash;
    int method_id = 0;

    // final boolean isInnerClass;

    public XFM(final ClassModel arg_classModel, final String hash) {
      classModel = arg_classModel;
      classDesc = classModel.thisClass().asSymbol();
      this.hash = hash;
      // System.out.println(classDesc.packageName() + "." + classDesc.displayName());
      // boolean maybeInnerClass = false;
      // for(final var attr: classModel.attributes()) {
      //   System.out.println(attr);
      //   if(attr instanceof InnerClassesAttribute) {
      //     final InnerClassesAttribute inner = (InnerClassesAttribute)attr;
      //     for(final var cls: inner.classes()) {
      //       if(cls.innerClass().asSymbol().equals(classDesc)) {
      //         // maybeInnerClass = true;
      //       }
      //       System.out.println("  " + cls);
      //     }
      //   }
      // }
      // isInnerClass = maybeInnerClass;
    }

    @Override
    final public void atStart(final ClassBuilder classBuilder) {
      Optional<SourceFileAttribute> optAttrSourceFile = classModel.findAttribute(Attributes.sourceFile());
      if(!optAttrSourceFile.isPresent()) {
        classBuilder.with(SourceFileAttribute.of((classDesc.packageName() + "." + classDesc.displayName()).replace('.', '/') + ".class"));
      }
      final int current_method_id = method_id++;
      final String methodName = "sha256_" + hash + "_" + (classDesc.packageName() + "." + classDesc.displayName()).replace('.', '_').replace('$', '_') + "_id_" + current_method_id + "_DECONSTRUCTOR";
      // System.out.println("  " + methodName);

      final List<TypeAnnotation> typeAnnotations = new ArrayList<>();
      typeAnnotations.add(TypeAnnotation.of(
        TargetInfo.ofMethodFormalParameter(1),
        targetPath,
        Annotation.of(classDesc_CTypedef,
        AnnotationElement.ofString("name", OBJECT_HANDLE_STRING(classDesc)))
      ));

      classBuilder.withMethod(
        methodName,
        MethodTypeDesc.of(ConstantDescs.CD_void, classDesc_IsolateThread, classDesc_ObjectHandle),
        ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
        (final MethodBuilder methodBuilder) -> {
          Annotation annotation = Annotation.of(
            classDesc_CEntryPoint,
            AnnotationElement.ofString("name", methodName),
            AnnotationElement.ofClass("include", ClassDesc.of("org.graalvm.nativeimage.c.function", "CEntryPoint$AlwaysIncluded"))
          );

          methodBuilder.with(RuntimeVisibleAnnotationsAttribute.of(annotation));
          methodBuilder.with(RuntimeVisibleTypeAnnotationsAttribute.of(typeAnnotations));

          methodBuilder.withCode((final CodeBuilder codeBuilder) -> {
            int nextAvailableSlot = 0;

            final int slot_arg_IsolateThread = nextAvailableSlot++;
            final int slot_arg_ObjectHandle = nextAvailableSlot++;
            if(false) {
              final int slot_globalObjectHandles = nextAvailableSlot++;
              cb_ObjectHandles_getGlobal(codeBuilder, slot_globalObjectHandles);
  
              final int slot_boxedObject = nextAvailableSlot++;
              cb_ObjectHandles_destroy(codeBuilder, slot_globalObjectHandles, slot_arg_ObjectHandle);
  
              codeBuilder.return_();
            } else {
              codeBuilder.invokestatic(classDesc_ObjectHandles, "getGlobal", MethodTypeDesc.of(classDesc_ObjectHandles));
              codeBuilder.aload(slot_arg_ObjectHandle);
              codeBuilder.invokeinterface(classDesc_ObjectHandles, "destroy", MethodTypeDesc.of(ConstantDescs.CD_void, classDesc_ObjectHandle));
              codeBuilder.return_();
            }
          });
        }
      );

    }

    @Override
    final public void accept(final ClassBuilder classBuilder, final ClassElement element) {
      classBuilder.with(element);
      if(!(element instanceof MethodModel)) return;
      final MethodModel methodModel = (MethodModel)element;
      if(methodModel.methodName().stringValue().equals("<clinit>")) return;

      final boolean isConstructor = methodModel.methodName().stringValue().equals("<init>");
      if(!isConstructor) return;
      // if(isInnerClass) return;

      final boolean isStatic = methodModel.flags().has(AccessFlag.STATIC);

      final int current_method_id = method_id++;
      final String methodName = "sha256_" + hash + "_" + (classDesc.packageName() + "." + classDesc.displayName()).replace('.', '_').replace('$', '_') + "_id_" + current_method_id + "_" + (isConstructor?"CONSTRUCTOR":methodModel.methodName().stringValue());
      // System.out.println("  " + methodName);

      final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
      final int methodParameterCount = methodTypeDesc.parameterCount();
      
      final List<TypeAnnotation> typeAnnotations = new ArrayList<>();
      MethodTypeDesc addedMethodTypeDesc = transformMethodTypeDesc(classModel, methodModel, typeAnnotations);

      classBuilder.withMethod(
        methodName,
        addedMethodTypeDesc,
        ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
        (final MethodBuilder methodBuilder) -> {
          Annotation annotation = Annotation.of(
            classDesc_CEntryPoint,
            AnnotationElement.ofString("name", methodName),
            AnnotationElement.ofClass("include", ClassDesc.of("org.graalvm.nativeimage.c.function", "CEntryPoint$AlwaysIncluded"))
          );

          methodBuilder.with(RuntimeVisibleAnnotationsAttribute.of(annotation));
          methodBuilder.with(RuntimeVisibleTypeAnnotationsAttribute.of(typeAnnotations));

          if(isConstructor) {
            methodBuilder.withCode((final CodeBuilder codeBuilder) -> {
              int nextAvailableSlot = 0;

              final int slot_arg_IsolateThread = nextAvailableSlot++;
              final int slot_args[] = new int[methodParameterCount];
              final int slot_unboxed_args[] = new int[methodParameterCount];

              for(int parameter_index = 0; parameter_index < methodParameterCount; parameter_index++) {
                slot_args[parameter_index] = nextAvailableSlot++;
                final ClassDesc paramType = methodTypeDesc.parameterType(parameter_index);
                if(paramType.equals(ConstantDescs.CD_double) || paramType.equals(ConstantDescs.CD_long)) nextAvailableSlot++;
              }

              final int slot_globalObjectHandles = nextAvailableSlot++;
              cb_ObjectHandles_getGlobal(codeBuilder, slot_globalObjectHandles);

              for(int parameter_index = 0; parameter_index < methodParameterCount; parameter_index++) {
                final ClassDesc paramType = methodTypeDesc.parameterType(parameter_index);
                if(paramType.isPrimitive()) {
                  slot_unboxed_args[parameter_index] = slot_args[parameter_index];
                } else {
                  final int slot_unboxed_argN = nextAvailableSlot++;
                  cb_ObjectHandles_get(codeBuilder, slot_globalObjectHandles, slot_args[parameter_index], paramType, slot_unboxed_argN);
                  slot_unboxed_args[parameter_index] = slot_unboxed_argN;
                }
              }

              final int slot_createdObject = nextAvailableSlot++;
              codeBuilder.new_(classDesc);
              codeBuilder.dup();
              for(int parameter_index = 0; parameter_index < methodParameterCount; parameter_index++) {
                final ClassDesc paramType = methodTypeDesc.parameterType(parameter_index);
                if(paramType.equals(ConstantDescs.CD_int) || paramType.equals(ConstantDescs.CD_short) || paramType.equals(ConstantDescs.CD_byte) || paramType.equals(ConstantDescs.CD_char) || paramType.equals(ConstantDescs.CD_boolean)) {
                  codeBuilder.iload(slot_unboxed_args[parameter_index]);
                } else if (paramType.equals(ConstantDescs.CD_float)) {
                  codeBuilder.fload(slot_unboxed_args[parameter_index]);
                } else if (paramType.equals(ConstantDescs.CD_double)) {
                  codeBuilder.dload(slot_unboxed_args[parameter_index]);
                } else if (paramType.equals(ConstantDescs.CD_long)) {
                  codeBuilder.lload(slot_unboxed_args[parameter_index]);
                } else {
                  codeBuilder.aload(slot_unboxed_args[parameter_index]);
                }
              }
              codeBuilder.invokespecial(classDesc, "<init>", methodTypeDesc);
              codeBuilder.astore(slot_createdObject);

              final int slot_boxedObject = nextAvailableSlot++;
              cb_ObjectHandles_create(codeBuilder, slot_globalObjectHandles, slot_createdObject, slot_boxedObject);

              cb_return(codeBuilder, slot_boxedObject);
            });
          } else {
            methodBuilder.withCode((final CodeBuilder codeBuilder) -> {
              codeBuilder.return_(); // Simple void return
            });
          }
        }
      );
    }

    @Override
    final public void atEnd(final ClassBuilder classBuilder) {
      
    }
  };

  public final static byte[] b2b_xfm(final byte[] classBytes) throws NoSuchAlgorithmException {
    final MessageDigest digest = MessageDigest.getInstance("SHA-256");
    final byte[] hashBytes = digest.digest(classBytes);
    final String hexString = HexFormat.of().formatHex(hashBytes);

    final ClassFile cf = ClassFile.of();
    final ClassModel classModel = cf.parse(classBytes);
    final ClassDesc classDesc = classModel.thisClass().asSymbol();
    if(classDesc.displayName().length() <= 1) return null;

    final byte[] newClassBytes = cf.transformClass(classModel, new XFM(classModel, hexString));
    return newClassBytes;
  }

  public final static void xJar(final String originalJar, final String modifiedJar) throws IOException, NoSuchAlgorithmException {
    final File inputJar = new File(originalJar);
    final File outputJar = new File(modifiedJar);
    final FileInputStream fisInputJar = new FileInputStream(inputJar);
    final FileOutputStream fosOutputJar = new FileOutputStream(outputJar);
    final BufferedInputStream bisInputJar = new BufferedInputStream(fisInputJar);
    final BufferedOutputStream bosOutputJar = new BufferedOutputStream(fosOutputJar);
    final JarInputStream jis = new JarInputStream(bisInputJar);
    final Manifest manifest = jis.getManifest();
    final JarOutputStream jos = new JarOutputStream(bosOutputJar, manifest);

    for(;;) {
      final JarEntry jeInput = jis.getNextJarEntry();
      if(jeInput == null) break;
      final String jeiName = jeInput.getName();
      final JarEntry jeOutput = new JarEntry(jeiName);
      jeOutput.setTime(jeInput.getTime());
      jos.putNextEntry(jeOutput);

      System.out.println("    " + jeiName);
      if (jeiName.endsWith(".class")) {
        byte[] originalBytes = jis.readAllBytes();
        byte[] transformedBytes = b2b_xfm(originalBytes);
        jos.write(transformedBytes);
      } else {
        jis.transferTo(jos);
      }
      jos.closeEntry();
      jis.closeEntry();
    }

    jos.close();
    jis.close();
    bosOutputJar.close();
    bisInputJar.close();
    fosOutputJar.close();
    fisInputJar.close();
  }

  public final static void main(final String[] args) throws IOException {
    // System.out.println("TransformAgent");
    // System.out.println("args.length: " + args.length);
    // for(final String originalJar: args) {
    //   final String modifiedJar = originalJar.replace(".jar", "-native-image.jar");
    //   System.out.println("  " + originalJar + " => " + modifiedJar);
    //   xJar(
    //     originalJar,
    //     modifiedJar
    //   );
    // }

    // Files.write(Path.of("./build/classes/java/main/com/xilinx/rapidwright/edif/EDIFCell.class"),
    //   b2b_xfm(Files.readAllBytes(Path.of("./build/classes/java/main/com/xilinx/rapidwright/edif/EDIFCell.class")))
    // );

    // Files.write(Path.of("./build/classes/java/main/com/xilinx/rapidwright/edif/EDIFParser.class"),
    //   b2b_xfm(Files.readAllBytes(Path.of("./build/classes/java/main/com/xilinx/rapidwright/edif/EDIFParser.class")))
    // );

  }

  public final static class XFM_ClassFileTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(
      final ClassLoader loader,
      final String className, 
      final Class<?> classBeingRedefined, 
      final ProtectionDomain protectionDomain, 
      final byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.contains("com/xilinx/rapidwright/")) {
          // System.out.println("XFM_ClassFileTransformer: '" + className + "', classfileBuffer: " + classfileBuffer.length);
          try {
            final byte[] result = b2b_xfm(classfileBuffer);
            return result;
          } catch(NoSuchAlgorithmException err) {
            return null;
          }
        }
        return null;
      }
  }

  public static void premain(String agentArgs, Instrumentation inst) {
    // System.out.println("Java Agent initialized. Registering TransformAgent...");
    inst.addTransformer(new XFM_ClassFileTransformer());
  }
}
