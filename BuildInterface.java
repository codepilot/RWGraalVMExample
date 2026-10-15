package com.xilinx.rapidwright.examples;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassSignature;
import java.lang.classfile.CodeElement;
import java.lang.classfile.MethodModel;
import java.lang.classfile.FieldModel;
import java.lang.classfile.AccessFlags;
import java.lang.classfile.Attribute;
import java.lang.classfile.Attributes;
import java.lang.classfile.attribute.BootstrapMethodsAttribute;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.attribute.ExceptionsAttribute;
import java.lang.classfile.attribute.InnerClassInfo;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.classfile.attribute.SourceFileAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.reflect.AccessFlag;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.Integer;

import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.HashSet;
import java.lang.Package;

import java.nio.file.Paths;
import java.nio.file.Path;

import java.lang.classfile.Signature.ClassTypeSig;
import java.lang.classfile.Signature.TypeParam;

@SuppressWarnings({"preview","deprecation"})
public class BuildInterface {
  private static int CEntryPointCounter = 1;
  private final static SortedSet<String> classSet = new TreeSet<>();
  private final static SortedMap<String, Set<AccessFlag>> classAccessFlagsMap = new TreeMap<>();
  private final static SortedSet<String> nonloadingClassSet = new TreeSet<>();
  private final static SortedSet<ClassModel> classModels = new TreeSet<ClassModel>((ClassModel s1, ClassModel s2) -> s1.thisClass().asInternalName().compareTo(s2.thisClass().asInternalName()));
  private final static SortedSet<ClassModel> missingClassSet = new TreeSet<ClassModel>((ClassModel s1, ClassModel s2) -> s1.thisClass().asInternalName().compareTo(s2.thisClass().asInternalName()));

  public static int getRank(ClassDesc desc) {
    int rank = 0;
    while (desc.isArray()) {
      rank++;
      desc = desc.componentType();
    }
    return rank;
  }

  public static ClassDesc getBaseComponentType(ClassDesc desc) {
    int rank = 0;
    while (desc.isArray()) {
      rank++;
      desc = desc.componentType();
    }
    return desc;
  }

  private static void jarPath(final String jp) throws IOException {
    final Path currentPath = Paths.get(jp).toAbsolutePath();
    final String abs_jp = "jar:file:" + currentPath.normalize().toString() + "!/";

    final URL url = new URL(abs_jp);
    final JarURLConnection jarConnection = (JarURLConnection)url.openConnection();
    final JarFile jarFile = jarConnection.getJarFile();
    final Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      final JarEntry entry = entries.nextElement();

      if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
        continue;
      }
      final InputStream is = jarFile.getInputStream(entry);
      final byte[] classBytes = is.readAllBytes();
      
      final ClassFile cf = ClassFile.of();
      final ClassModel classModel = cf.parse(classBytes);
      final ClassDesc classDesc = classModel.thisClass().asSymbol();
      final String classDisplayName = classDesc.displayName();
      final AccessFlags accessFlags = classModel.flags();

      final String classNameJava = entry.getName()
        .substring(0, entry.getName().length() - 6)
        .replace('/', '.');

      classAccessFlagsMap.put(classNameJava, accessFlags.flags());

      for(final InnerClassesAttribute innerClasses: classModel.findAttributes(Attributes.innerClasses())) {
        for(final InnerClassInfo innerClassInfo: innerClasses.classes()) {
          final ClassEntry outerClassEntry = innerClassInfo.outerClass().orElse(null);
          final ClassEntry innerClassEntry = innerClassInfo.innerClass();
          final ClassDesc innerClassDesc = innerClassEntry.asSymbol();
          final String innerClassJavaName = innerClassEntry.asInternalName().replace('/', '.').replace('$', '.');

          classAccessFlagsMap.putIfAbsent(innerClassJavaName, innerClassInfo.flags());
          // final boolean printBool = innerClassJavaName.equals("com.xilinx.rapidwright.rwroute.CUFRpartitionTree.PartitionBBox");
          final boolean printBool = innerClassInfo.has(AccessFlag.STATIC);
          if(printBool) {
            System.out.println(
              "innerClassInfoFlags: 0x" + Integer.toHexString(innerClassInfo.flagsMask()) +
              ", " + classNameJava + " 0x" + Integer.toHexString(accessFlags.flagsMask()) +
              " [-] innerName: " + innerClassInfo.innerName() +
              ", innerClass: " + innerClassEntry +
              ", innerClassJavaName: " + innerClassJavaName +
              ", outerClassEntry: " + outerClassEntry
            );
          }

          // if(classNameJava.equals(innerClassJavaName)) {
          //   return innerClassInfo.flags();
          // }
        }
      }      

      if(!classNameJava.startsWith("com.xilinx.rapidwright.")) {
        // System.out.println("!startsWith 'com.xilinx.rapidwright.' "+ classNameJava);
        continue;
      }

      if(classNameJava.startsWith("com.xilinx.rapidwright.rwroute.")) {
        System.out.println("startsWith 'com.xilinx.rapidwright.rwroute.' " + classNameJava);
        continue;
      }

      if(classNameJava.startsWith("com.xilinx.rapidwright.gui.")) {
        System.out.println("startsWith 'com.xilinx.rapidwright.gui.' " + classNameJava);
        continue;
      }

      if(classNameJava.startsWith("com.xilinx.rapidwright.examples.")) {
        System.out.println("startsWith 'com.xilinx.rapidwright.examples.'" + classNameJava);
        continue;
      }

      if(classNameJava.startsWith("com.xilinx.rapidwright.placer.handplacer.")) {
        System.out.println("startsWith 'com.xilinx.rapidwright.placer.handplacer.' " + classNameJava);
        continue;
      }

      if(classNameJava.equals("com.xilinx.rapidwright.util.ParallelismTools")) {
        System.out.println("equals 'com.xilinx.rapidwright.util.ParallelismTools' " + classNameJava);
        continue;
      }

      if(classNameJava.equals("com.xilinx.rapidwright.MainEntrypoint")) {
        System.out.println("equals 'com.xilinx.rapidwright.MainEntrypoint' " + classNameJava);
        continue;
      }

      if(classNameJava.equals("com.xilinx.rapidwright.StandaloneEntrypoint")) {
        System.out.println("equals 'com.xilinx.rapidwright.StandaloneEntrypoint' " + classNameJava);
        continue;
      }

      if(classNameJava.equals("com.xilinx.rapidwright.design.blocks.GUIPBlock")) {
        System.out.println("equals 'com.xilinx.rapidwright.design.blocks.GUIPBlock' " + classNameJava);
        continue;
      }

      if(classNameJava.equals("com.xilinx.rapidwright.design.blocks.PBlockGenerator")) {
        System.out.println("equals 'com.xilinx.rapidwright.design.blocks.PBlockGenerator' " + classNameJava);
        continue;
      }

      if(classNameJava.equals("com.xilinx.rapidwright.design.blocks.BlockView")) {
        System.out.println("equals 'com.xilinx.rapidwright.design.blocks.BlockView' " + classNameJava);
        continue;
      }

      if(classNameJava.startsWith("com.xilinx.rapidwright.device.browser.")) {
        System.out.println("startsWith 'com.xilinx.rapidwright.device.browser.' " + classNameJava);
        continue;
      }

      if(classNameJava.startsWith("com.xilinx.rapidwright.debug.")) {
        System.out.println("startsWith 'com.xilinx.rapidwright.debug.' " + classNameJava);
        continue;
      }

      if(classNameJava.startsWith("com.xilinx.rapidwright.interchange.")) {
        System.out.println("startsWith 'com.xilinx.rapidwright.interchange.' " + classNameJava);
        continue;
      }

      if(accessFlags.has(AccessFlag.PRIVATE)) {
        System.out.println("PRIVATE " + classNameJava + " 0x" + Integer.toHexString(accessFlags.flagsMask()));
        continue;
      }

      if(!accessFlags.has(AccessFlag.PUBLIC)) {
        System.out.println("!PUBLIC " + classNameJava + " 0x" + Integer.toHexString(accessFlags.flagsMask()));
        continue;
      }

      if(!inner_class_is_public(classModel)) {
        System.out.println("!inner_class_is_public " + classNameJava + " 0x" + Integer.toHexString(accessFlags.flagsMask()));
        continue;
      }

      if(!classModels.add(classModel)) {
        System.out.println("classModel duplicated " + classNameJava);
      }

      if(!classSet.add(classNameJava)) {
        System.out.println("classNameJava duplicated " + classNameJava);
      }

    }
  }

  static void method_throws(final FileWriter fw_java, final String class_name, final MethodModel methodModel) throws IOException {
    final String methodModel_name = methodModel.methodName().stringValue();
    for(final Attribute<?> attribute: methodModel.attributes()) {
      if(attribute instanceof ExceptionsAttribute) {
        ExceptionsAttribute exceptionsAttribute = (ExceptionsAttribute)attribute;
        // System.out.println("  // " + class_name + "." + methodModel_name + " " + attribute.attributeName().stringValue() + " ");
        fw_java.write(" throws");
        for(ClassEntry classEntryException: exceptionsAttribute.exceptions()) {
          ClassDesc classDescException = classEntryException.asSymbol();
          fw_java.write(" " + classDescException.packageName() + "." + classDescException.displayName() + "");
        }
      }
    }
  }

  static void write_field_static_primitive(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final FieldModel fieldModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    final String field_name = fieldModel.fieldName().stringValue();
    final ClassDesc field_type_class_desc = fieldModel.fieldTypeSymbol();
    final AccessFlags accessFlags = fieldModel.flags();
    final int CEntryPointID = CEntryPointCounter++;
  
    fw_java.write("\n");
    fw_java.write("  @CEntryPoint(name = \"id_" + CEntryPointID + "_" + class_name + "_get_static_field_" + field_name + "\") /* 0x" + Integer.toHexString(accessFlags.flagsMask()) + " */\n");
    fw_java.write("  public static " + field_type_class_desc.displayName() + " id_" + CEntryPointID + "_" + class_name + "_get_static_field_" + field_name + "(IsolateThread thread) {\n");
    fw_java.write("    return " + classNameJava + "." + field_name +";\n");
    fw_java.write("  }\n");

    if(accessFlags.has(AccessFlag.FINAL)) {
      fw_hpp.write("    inline static const auto " + field_name + "{id_" + CEntryPointID + "_" + class_name + "_get_static_field_" + field_name + "(localThread.get())};\n");
    } else {
      fw_hpp.write("\n");
      fw_hpp.write("    __forceinline static auto " + field_name + "() noexcept {\n");
      fw_hpp.write("      return id_" + CEntryPointID + "_" + class_name + "_get_static_field_" + field_name + "(localThread.get());\n");
      fw_hpp.write("    }\n");
    }
  }

  static void write_field_static_object(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final FieldModel fieldModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    final String field_name = fieldModel.fieldName().stringValue();
    final ClassDesc field_type_class_desc = fieldModel.fieldTypeSymbol();
    final AccessFlags accessFlags = fieldModel.flags();
    final int CEntryPointID = CEntryPointCounter++;

    fw_java.write("\n");
    fw_java.write("  @CEntryPoint(name = \"id_" + CEntryPointID + "_" + class_name + "_get_static_field_" + field_name + "\") /* 0x" + Integer.toHexString(accessFlags.flagsMask()) + " */\n");
    fw_java.write("  public static ObjectHandle id_" + CEntryPointID + "_" + class_name + "_get_static_field_" + field_name + "(IsolateThread thread) {\n");
    fw_java.write("    final ObjectHandle handle = ObjectHandles.getGlobal().create(" + classNameJava + "." + field_name +");\n");
    fw_java.write("    return handle;\n");
    fw_java.write("  }\n");

    final String java_class_name = field_type_class_desc.packageName() + "." + field_type_class_desc.displayName().replace('$', '.');
    final String cpp_class_name = java_class_name.replace(".", "_");
    if(cpp_class_name.startsWith("java_")) return;
    
    {
    }
    if(accessFlags.has(AccessFlag.FINAL)) {
      // fw_hpp.write("    inline static const unique_" + cpp_class_name + " " + field_name + "{std::bit_cast<" + cpp_class_name + " *>(" + class_name + "_get_static_field_" + field_name + "(localThread.get()))};\n");
      fw_hpp.write("\n");
      fw_hpp.write("    __forceinline static unique_" + cpp_class_name + " STATIC_FINAL_" + field_name + "() noexcept {\n");
      fw_hpp.write("      if constexpr (is_debug) {\n");
      fw_hpp.write("        std::println(\"unique_" + cpp_class_name + "\");\n");
      fw_hpp.write("      }\n");
      fw_hpp.write("      return unique_" + cpp_class_name + "(std::bit_cast<" + cpp_class_name + " *>(id_" + CEntryPointID + "_" + class_name + "_get_static_field_" + field_name + "(localThread.get())));\n");
      fw_hpp.write("    }\n");
      fw_hpp.write("    inline static const unique_" + cpp_class_name + " " + field_name + "{STATIC_FINAL_" + field_name + "()};\n");
    } else {
      fw_hpp.write("\n");
      fw_hpp.write("    __forceinline static unique_" + cpp_class_name + " " + field_name + "() noexcept {\n");
      fw_hpp.write("      if constexpr (is_debug) {\n");
      fw_hpp.write("        std::println(\"unique_" + cpp_class_name + "\");\n");
      fw_hpp.write("      }\n");
      fw_hpp.write("      return unique_" + cpp_class_name + "(std::bit_cast<" + cpp_class_name + " *>(id_" + CEntryPointID + "_" + class_name + "_get_static_field_" + field_name + "(localThread.get())));\n");
      fw_hpp.write("    }\n");
    }
  }

  static void write_field_member_primitive(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final FieldModel fieldModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    final String field_name = fieldModel.fieldName().stringValue();
    final ClassDesc field_type_class_desc = fieldModel.fieldTypeSymbol();
    final AccessFlags accessFlags = fieldModel.flags();
    final int CEntryPointID = CEntryPointCounter++;

    fw_java.write("\n");
    fw_java.write("  @CEntryPoint(name = \"id_" + CEntryPointID + "_" + class_name + "_get_field_" + field_name + "\") /* 0x" + Integer.toHexString(accessFlags.flagsMask()) + " */\n");
    fw_java.write("  public static " + field_type_class_desc.displayName() + " id_" + CEntryPointID + "_" + class_name + "_get_field_" + field_name + "(IsolateThread thread, final ObjectHandle instance_handle) {\n");
    fw_java.write("    final " + classNameJava + " instance = ObjectHandles.getGlobal().get(instance_handle);\n");
    fw_java.write("    return instance." + field_name +";\n");
    fw_java.write("  }\n");

    fw_hpp.write("    __forceinline auto " + field_name + "() noexcept {\n");
    fw_hpp.write("      return id_" + CEntryPointID + "_" + class_name + "_get_field_" + field_name + "(localThread.get(), this);\n");
    fw_hpp.write("    }\n");
  }

  static void write_field_member_object(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final FieldModel fieldModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    final String field_name = fieldModel.fieldName().stringValue();
    final ClassDesc field_type_class_desc = fieldModel.fieldTypeSymbol();
    final AccessFlags accessFlags = fieldModel.flags();
    final int CEntryPointID = CEntryPointCounter++;

    fw_java.write("\n");
    fw_java.write("  @CEntryPoint(name = \"id_" + CEntryPointID + "_" + class_name + "_get_field_" + field_name + "\") /* 0x" + Integer.toHexString(accessFlags.flagsMask()) + " */\n");
    fw_java.write("  public static ObjectHandle id_" + CEntryPointID + "_" + class_name + "_get_field_" + field_name + "(IsolateThread thread, final ObjectHandle instance_handle) {\n");
    fw_java.write("    final " + classNameJava + " instance = ObjectHandles.getGlobal().get(instance_handle);\n");
    fw_java.write("    final ObjectHandle handle = ObjectHandles.getGlobal().create(instance." + field_name +");\n");
    fw_java.write("    return handle;\n");

    fw_java.write("  }\n");

    final String java_class_name = field_type_class_desc.packageName() + "." + field_type_class_desc.displayName();
    final String cpp_class_name = java_class_name.replace(".", "_");
    if(cpp_class_name.startsWith("java_")) return;
    if(cpp_class_name.contains("$")) return;

    fw_hpp.write("    __forceinline unique_" + cpp_class_name + " " + field_name + "() noexcept {\n");
      fw_hpp.write("      return unique_" + cpp_class_name + "(std::bit_cast<" + cpp_class_name + " *>(id_" + CEntryPointID + "_" + class_name + "_get_field_" + field_name + "(localThread.get(), this)));\n");
    fw_hpp.write("    }\n");
  }

  static void write_field(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final FieldModel fieldModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_');

    final AccessFlags accessFlags = fieldModel.flags();
    if(!accessFlags.has(AccessFlag.PUBLIC)) return;
    if(accessFlags.has(AccessFlag.PRIVATE)) return;

    final String field_name = fieldModel.fieldName().stringValue();
    if(field_name.length() <= 1) return;

    fw_hpp.write("#undef " + field_name + "\n");

    final String field_type_name = fieldModel.fieldType().stringValue();
    final ClassDesc field_type_class_desc = fieldModel.fieldTypeSymbol();

    if(field_type_class_desc.isPrimitive()) {
      if(accessFlags.has(AccessFlag.STATIC)) {
        write_field_static_primitive(classModel, fw_java, fw_hpp, fieldModel);
      } else {
        write_field_member_primitive(classModel, fw_java, fw_hpp, fieldModel);
      }
    }
    if(field_type_class_desc.isClassOrInterface()) {
      if(accessFlags.has(AccessFlag.STATIC)) {
        write_field_static_object(classModel, fw_java, fw_hpp, fieldModel);
      } else {
        write_field_member_object(classModel, fw_java, fw_hpp, fieldModel);
      }
    }
  }

  static void write_static_method_primitive(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final MethodModel methodModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    final AccessFlags methodAccessFlags = methodModel.flags();
    final String methodModel_name = methodModel.methodName().stringValue();
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    final ClassDesc returnTypeDesc = methodTypeDesc.returnType();
    final AccessFlags classAccessFlags = classModel.flags();
    final int CEntryPointID = CEntryPointCounter++;

    fw_java.write("  @CEntryPoint(name = \"id_" + CEntryPointID + "_" + class_name + "_" + methodModel_name + "\") /* 0x" + Integer.toHexString(methodAccessFlags.flagsMask()) + " */\n");
    fw_java.write("  public static " + returnTypeDesc.displayName() + " id_" + CEntryPointID + "_" + methodModel_name + "(IsolateThread thread");
    write_java_arguments(fw_java, methodModel);
    fw_java.write(")");
    method_throws(fw_java, class_name, methodModel);
    fw_java.write(" {\n");
    write_java_arguments_unboxing(fw_java, methodModel);
    if(returnTypeDesc.displayName().equals("void")) {
      fw_java.write("    " + classNameJava + "." + methodModel_name + "(");
      write_java_method_params(fw_java, methodModel);
      fw_java.write(");\n");
    } else {
      fw_java.write("    return " + classNameJava + "." + methodModel_name + "(");
      write_java_method_params(fw_java, methodModel);
      fw_java.write(");\n");
    }
    fw_java.write("  }\n");

    if(returnTypeDesc.displayName().equals("void")) {
      fw_hpp.write("    __forceinline static void " + methodModel_name + "(");
    } else{
      fw_hpp.write("    __forceinline static auto " + methodModel_name + "(");
    }
    write_hpp_arguments(fw_hpp, methodModel);
    fw_hpp.write(") noexcept {\n");
    write_hpp_arguments_unboxing(fw_hpp, methodModel);
    if(returnTypeDesc.displayName().equals("void")) {
      fw_hpp.write("      id_" + CEntryPointID + "_" + class_name + "_" + methodModel_name + "(localThread.get()");
    } else {
      fw_hpp.write("      return id_" + CEntryPointID + "_" + class_name + "_" + methodModel_name + "(localThread.get()");
    }
    write_hpp_method_params(fw_hpp, methodModel);
    fw_hpp.write(");\n");
    fw_hpp.write("    }\n");
    fw_hpp.write("\n");

  }

  static void write_static_method_object(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final MethodModel methodModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    final AccessFlags methodAccessFlags = methodModel.flags();
    final String methodModel_name = methodModel.methodName().stringValue();
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    final ClassDesc returnTypeDesc = methodTypeDesc.returnType();
    final AccessFlags classAccessFlags = classModel.flags();
    final int CEntryPointID = CEntryPointCounter++;

    fw_java.write("  @CEntryPoint(name = \"id_" + CEntryPointID + "_" + class_name + "_" + methodModel_name + "\") /* 0x" + Integer.toHexString(methodAccessFlags.flagsMask()) + " */\n");
    fw_java.write("  public static ObjectHandle id_" + CEntryPointID + "_" + methodModel_name + "(IsolateThread thread");
    write_java_arguments(fw_java, methodModel);
    fw_java.write(")");
    method_throws(fw_java, class_name, methodModel);
    fw_java.write(" {\n");
    write_java_arguments_unboxing(fw_java, methodModel);
    fw_java.write("    return ObjectHandles.getGlobal().create(" + classNameJava + "." + methodModel_name + "(");
    write_java_method_params(fw_java, methodModel);
    fw_java.write("));\n");
    fw_java.write("  }\n");

    final String cpp_class_name = (((returnTypeDesc.packageName().length() > 0)?(returnTypeDesc.packageName() + "."):"")+
    returnTypeDesc.displayName()).replace('.', '_').replace('$', '_');
    fw_hpp.write("    __forceinline unique_" + cpp_class_name + " " + methodModel_name + "(");
    write_hpp_arguments(fw_hpp, methodModel);
    fw_hpp.write(") noexcept {\n");
    write_hpp_arguments_unboxing(fw_hpp, methodModel);
    fw_hpp.write("      return unique_" + cpp_class_name + "(std::bit_cast<" + cpp_class_name + " *>(id_" + CEntryPointID + "_" + class_name + "_" + methodModel_name + "(localThread.get()");
    write_hpp_method_params(fw_hpp, methodModel);
    fw_hpp.write(")));\n");
    fw_hpp.write("    }\n");
    fw_hpp.write("\n");    
  }

  static void write_constructor(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final MethodModel methodModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    final AccessFlags methodAccessFlags = methodModel.flags();
    final String methodModel_name = methodModel.methodName().stringValue();
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    final ClassDesc returnTypeDesc = methodTypeDesc.returnType();
    final AccessFlags classAccessFlags = classModel.flags();

    if(classAccessFlags.has(AccessFlag.ABSTRACT)) {
      return;
    }
    final int CEntryPointID = CEntryPointCounter++;

    final String cEntryPoint = "id_constructor_" + CEntryPointID + "_" + class_name;

    fw_java.write("\n");
    fw_java.write("  @CEntryPoint(name = \"" + cEntryPoint + "\") /* method: 0x" + Integer.toHexString(methodAccessFlags.flagsMask()) + ", class: 0x" + Integer.toHexString(classAccessFlags.flagsMask()) + " */\n");
    fw_java.write("  public static ObjectHandle id_constructor_" + CEntryPointID + "_" + class_name + "(IsolateThread thread");
    write_java_arguments(fw_java, methodModel);
    fw_java.write(")");
    method_throws(fw_java, class_name, methodModel);
    fw_java.write(" {\n");
    write_java_arguments_unboxing(fw_java, methodModel);
    fw_java.write("    final " + classNameJava + " instance = new " + classNameJava + "(");
    write_java_method_params(fw_java, methodModel);
    fw_java.write(");\n");
    fw_java.write("    final ObjectHandle handle = ObjectHandles.getGlobal().create(instance);\n");
    fw_java.write("    return handle;\n");
    fw_java.write("  }\n");

    fw_hpp.write("    __forceinline static unique_" + class_name + " constructor(");
    write_hpp_arguments(fw_hpp, methodModel);
    fw_hpp.write(") noexcept {\n");
    write_hpp_arguments_unboxing(fw_hpp, methodModel);
    fw_hpp.write("      return unique_" + class_name + "(std::bit_cast<" + class_name + " *>(" + cEntryPoint + "(localThread.get()");
    write_hpp_method_params(fw_hpp, methodModel);
    fw_hpp.write(")) );\n");
    fw_hpp.write("    }\n");
    fw_hpp.write("\n");
  }

  static void write_hpp_method_params(final FileWriter fw_hpp, final MethodModel methodModel) throws IOException {
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    int param_counter = 0;
    for(ClassDesc param: methodTypeDesc.parameterArray()) {
      final int param_id = param_counter++;
      fw_hpp.write(", param_" + param_id);
    }
  }

  static void write_java_method_params(final FileWriter fw_java, final MethodModel methodModel) throws IOException {
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    int param_counter = 0;
    for(ClassDesc param: methodTypeDesc.parameterArray()) {
      final int param_id = param_counter++;
      if(param_id > 0) {
        fw_java.write(", ");
      }
      fw_java.write("param_" + param_id);
    }
  }

  static void write_java_arguments(final FileWriter fw_java, final MethodModel methodModel) throws IOException {
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    int param_counter = 0;
    for(ClassDesc param: methodTypeDesc.parameterArray()) {
      final int param_id = param_counter++;

      if(param.isPrimitive()) {
        fw_java.write(", " + "final " + param.displayName() + " param_" + param_id);
        // System.out.println("        " + param.displayName());
      }
      if(param.isClassOrInterface()) {
        fw_java.write(", " + "final ObjectHandle param_handle_" + param_id);
        // System.out.println("        " + param.packageName() + "." + param.displayName());
      }
      if(param.isArray()) {
        fw_java.write(", " + "final ObjectHandle param_handle_" + param_id);
        // System.out.println("        " + param.displayName());
      }
    }
  }

  static void write_hpp_arguments(final FileWriter fw_hpp, final MethodModel methodModel) throws IOException {
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    int param_counter = 0;
    for(ClassDesc param: methodTypeDesc.parameterArray()) {
      final int param_id = param_counter++;

      if(param_id > 0) {
        fw_hpp.write(", ");
      }

      if(param.isPrimitive()) {
        if(param.displayName().equals("boolean")) {
          fw_hpp.write("const bool param_" + param_id);
        } else {
          fw_hpp.write("const " + param.displayName() + " param_" + param_id);
        }
        // System.out.println("        " + param.displayName());
      }
      if(param.isClassOrInterface()) {
        final String paramJavaClass = (param.packageName() + "." + param.displayName()).replace('$', '.');
        fw_hpp.write("unique_" + paramJavaClass.replace('.', '_') + " param_handle_" + param_id);
        // fw_hpp.write("void* param_" + param_id);
      }
      if(param.isArray()) {
        final int rank = getRank(param);
        final ClassDesc base = getBaseComponentType(param);
        final String paramJavaClass = (base.packageName() + ((base.packageName().length() > 0)?".":"") + base.displayName()).replace('$', '.');
        final String param_class = paramJavaClass.replace('.', '_');
        fw_hpp.write("unique_rank_" + rank + "_" + param_class + " param_handle_" + param_id);
        // System.out.println("        " + param.displayName());
      }
    }
  }

  static void write_java_arguments_unboxing(final FileWriter fw_java, final MethodModel methodModel) throws IOException {
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    int param_counter = 0;
    for(final Attribute<?> attribute: methodModel.attributes()) {
      fw_java.write("/* " + attribute + " */\n");
      if(attribute instanceof SignatureAttribute) {
        SignatureAttribute signatureAttribute = (SignatureAttribute)attribute;
        fw_java.write("/* SignatureAttribute " + signatureAttribute + " */");
      }
    }

    for(ClassDesc param: methodTypeDesc.parameterArray()) {
      final int param_id = param_counter++;

      if(param.isClassOrInterface()) {
        final String param_type_str = (param.packageName() + "." + param.displayName()).replace('$', '.');
        fw_java.write("    final " + param_type_str + " param_" + param_id + " = ObjectHandles.getGlobal().get(param_handle_" + param_id + ");\n");
      }
      if(param.isArray()) {
        final int rank = getRank(param);
        final ClassDesc baseComponentDesc = getBaseComponentType(param);
        fw_java.write("    // rank:" + rank + "\n");
        fw_java.write("    // baseComponentDesc:" + baseComponentDesc.descriptorString() + "\n");
        fw_java.write("    // baseComponentDesc.descriptorString():" + baseComponentDesc.descriptorString() + "\n");
        if(baseComponentDesc.packageName().length() > 0) {
          final String param_type_str = (baseComponentDesc.packageName() + "." + baseComponentDesc.displayName()).replace('$', '.');
          fw_java.write("    final " + param_type_str + "[]".repeat(rank) + " param_" + param_id + " = ObjectHandles.getGlobal().get(param_handle_" + param_id + "); // pkg:" + baseComponentDesc.packageName() + " " + baseComponentDesc + "\n");
        } else {
          final String param_type_str = (baseComponentDesc.displayName()).replace('$', '.');
          fw_java.write("    final " + param_type_str + "[]".repeat(rank) + "  param_" + param_id + " = ObjectHandles.getGlobal().get(param_handle_" + param_id + "); // pkg:" + baseComponentDesc.packageName() + " " + baseComponentDesc + "\n");
        }
      }
    }
  }

  static void write_hpp_arguments_unboxing(final FileWriter fw_hpp, final MethodModel methodModel) throws IOException {
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    int param_counter = 0;
    for(final Attribute<?> attribute: methodModel.attributes()) {
      fw_hpp.write("/* " + attribute + " */\n");
      if(attribute instanceof SignatureAttribute) {
        SignatureAttribute signatureAttribute = (SignatureAttribute)attribute;
        fw_hpp.write("/* SignatureAttribute " + signatureAttribute + " */");
      }
    }

    for(ClassDesc param: methodTypeDesc.parameterArray()) {
      final int param_id = param_counter++;

      if(param.isClassOrInterface()) {
        final String param_type_str = (param.packageName() + "." + param.displayName()).replace('$', '.');
        fw_hpp.write("      void* /* " + param_type_str + " */ param_" + param_id + "{param_handle_" + param_id + ".get()};\n");
      }
      if(param.isArray()) {
        ClassDesc componentDesc = param.componentType();
        if(componentDesc.packageName().length() > 0) {
          final String param_type_str = (componentDesc.packageName() + "." + componentDesc.displayName()).replace('$', '.');
          fw_hpp.write("      void* /* " + param_type_str + "[] */ param_" + param_id + " = param_handle_" + param_id + ".get(); // pkg:" + componentDesc.packageName() + " " + componentDesc + "\n");
        } else {
          final String param_type_str = (componentDesc.displayName()).replace('$', '.');
          fw_hpp.write("      void* /* " + param_type_str + "[] */ param_" + param_id + " = param_handle_" + param_id + ".get(); // pkg:" + componentDesc.packageName() + " " + componentDesc + "\n");
        }
      }
    }
  }

  static boolean is_ClassDesc_public(final ClassDesc classDesc) {
    if(classDesc.isPrimitive()) return true;
    if(classDesc.isClassOrInterface()) {
      final String javaClass = (classDesc.packageName() + "." + classDesc.displayName()).replace('$', '.');
      final Set<AccessFlag> accessFlags = classAccessFlagsMap.get(javaClass);
      if(accessFlags == null) return true;
      return accessFlags.contains(AccessFlag.PUBLIC);
    }
    if(classDesc.isArray()) {
      ClassDesc componentDesc = classDesc.componentType();
      while (componentDesc.isArray()) {
        componentDesc = componentDesc.componentType();
      }
      return is_ClassDesc_public(componentDesc);
    }
    return true;
  }

  static boolean are_method_types_fully_public(final MethodModel methodModel) {
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    if(!is_ClassDesc_public(methodTypeDesc.returnType())) return false;

    // int param_counter = 0;
    // for(final Attribute<?> attribute: methodModel.attributes()) {
    //   fw_java.write("/* " + attribute + " */\n");
    //   if(attribute instanceof SignatureAttribute) {
    //     SignatureAttribute signatureAttribute = (SignatureAttribute)attribute;
    //     fw_java.write("/* SignatureAttribute " + signatureAttribute + " */");
    //   }
    // }

    for(ClassDesc param: methodTypeDesc.parameterArray()) {
      if(!is_ClassDesc_public(param)) return false;
    //   final int param_id = param_counter++;

    //   if(param.isClassOrInterface()) {
    //     final String param_type_str = (param.packageName() + "." + param.displayName()).replace('$', '.');
    //     fw_java.write("    final " + param_type_str + " param_" + param_id + " = ObjectHandles.getGlobal().get(param_handle_" + param_id + ");\n");
    //   }
    //   if(param.isArray()) {
    //     ClassDesc componentDesc = param.componentType();
    //     if(componentDesc.packageName().length() > 0) {
    //       final String param_type_str = (componentDesc.packageName() + "." + componentDesc.displayName()).replace('$', '.');
    //       fw_java.write("    final " + param_type_str + "[] param_" + param_id + " = ObjectHandles.getGlobal().get(param_handle_" + param_id + "); // pkg:" + componentDesc.packageName() + " " + componentDesc + "\n");
    //     } else {
    //       final String param_type_str = (componentDesc.displayName()).replace('$', '.');
    //       fw_java.write("    final " + param_type_str + "[] param_" + param_id + " = ObjectHandles.getGlobal().get(param_handle_" + param_id + "); // pkg:" + componentDesc.packageName() + " " + componentDesc + "\n");
    //     }
    //   }
    }
    return true;
  }

  static void write_method_primitive(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final MethodModel methodModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    final AccessFlags methodAccessFlags = methodModel.flags();
    final String methodModel_name = methodModel.methodName().stringValue();
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    final ClassDesc returnTypeDesc = methodTypeDesc.returnType();
    final AccessFlags classAccessFlags = classModel.flags();

    if(methodAccessFlags.has(AccessFlag.STATIC)) {
      write_static_method_primitive(classModel, fw_java, fw_hpp, methodModel);
      return;
    }
    if(methodModel_name.equals("<init>")) {
      write_constructor(classModel, fw_java, fw_hpp, methodModel);
      return;
    }

    final int CEntryPointID = CEntryPointCounter++;
    {
      fw_java.write("  @CEntryPoint(name = \"id_" + CEntryPointID + "_" + class_name + "_" + methodModel_name + "\") /* 0x" + Integer.toHexString(methodAccessFlags.flagsMask()) + " */\n");
      fw_java.write("  public static " + returnTypeDesc.displayName() + " id_" + CEntryPointID + "_" + methodModel_name + "(IsolateThread thread, final ObjectHandle handle_this");
      write_java_arguments(fw_java, methodModel);
      fw_java.write(")");
      method_throws(fw_java, class_name, methodModel);
      fw_java.write(" {\n");
      fw_java.write("    final " + classNameJava + " instance = ObjectHandles.getGlobal().get(handle_this);\n");
      write_java_arguments_unboxing(fw_java, methodModel);
      if(returnTypeDesc.displayName().equals("void")) {
        fw_java.write("    instance." + methodModel_name + "(");
        write_java_method_params(fw_java, methodModel);
        fw_java.write(");\n");
      } else {
        fw_java.write("    return instance." + methodModel_name + "(");
        write_java_method_params(fw_java, methodModel);
        fw_java.write(");\n");
      }
      fw_java.write("  }\n");
    }

    {
      if(returnTypeDesc.displayName().equals("void")) {
        fw_hpp.write("    __forceinline void " + methodModel_name + "(");
      } else{
        fw_hpp.write("    __forceinline auto " + methodModel_name + "(");
      }
      write_hpp_arguments(fw_hpp, methodModel);
      fw_hpp.write(") noexcept {\n");
      write_hpp_arguments_unboxing(fw_hpp, methodModel);
      if(returnTypeDesc.displayName().equals("void")) {
        fw_hpp.write("      id_" + CEntryPointID + "_" + class_name + "_" + methodModel_name + "(localThread.get(), this");
      } else {
        fw_hpp.write("      return id_" + CEntryPointID + "_" + class_name + "_" + methodModel_name + "(localThread.get(), this");
      }
      write_hpp_method_params(fw_hpp, methodModel);
      fw_hpp.write(");\n");
      fw_hpp.write("    }\n");
      fw_hpp.write("\n");      
    }

  }

  static void write_method_object(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final MethodModel methodModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    final AccessFlags methodAccessFlags = methodModel.flags();
    final String methodModel_name = methodModel.methodName().stringValue();
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    final ClassDesc returnTypeDesc = methodTypeDesc.returnType();
    final AccessFlags classAccessFlags = classModel.flags();

    if(methodAccessFlags.has(AccessFlag.STATIC)) {
      write_static_method_object(classModel, fw_java, fw_hpp, methodModel);
      return;
    }

    final int CEntryPointID = CEntryPointCounter++;
    {
      fw_java.write("  @CEntryPoint(name = \"id_" + CEntryPointID + "_" + class_name + "_" + methodModel_name + "\") /* 0x" + Integer.toHexString(methodAccessFlags.flagsMask()) + " */\n");
      fw_java.write("  public static ObjectHandle id_" + CEntryPointID + "_" + methodModel_name + "(IsolateThread thread, final ObjectHandle handle_this");
      write_java_arguments(fw_java, methodModel);
      fw_java.write(")");
      method_throws(fw_java, class_name, methodModel);
      fw_java.write(" {\n");
      fw_java.write("    final " + classNameJava + " instance = ObjectHandles.getGlobal().get(handle_this);\n");
      write_java_arguments_unboxing(fw_java, methodModel);
      fw_java.write("    return ObjectHandles.getGlobal().create(instance." + methodModel_name + "(");
      write_java_method_params(fw_java, methodModel);
      fw_java.write("));\n");
      fw_java.write("  }\n");
    }

    {
      final String cpp_class_name = (((returnTypeDesc.packageName().length() > 0)?(returnTypeDesc.packageName() + "."):"")+
      returnTypeDesc.displayName()).replace('.', '_').replace('$', '_');
      fw_hpp.write("    __forceinline unique_" + cpp_class_name + " " + methodModel_name + "(");
      write_hpp_arguments(fw_hpp, methodModel);
      fw_hpp.write(") noexcept {\n");
      write_hpp_arguments_unboxing(fw_hpp, methodModel);
      fw_hpp.write("      return unique_" + cpp_class_name + "(std::bit_cast<" + cpp_class_name + " *>(id_" + CEntryPointID + "_" + class_name + "_" + methodModel_name + "(localThread.get(), this");
      write_hpp_method_params(fw_hpp, methodModel);
      fw_hpp.write(")));\n");
      fw_hpp.write("    }\n");
      fw_hpp.write("\n");         
    }
  }

  static void write_method(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final MethodModel methodModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    final ClassDesc classDesc = classModel.thisClass().asSymbol();
    final String classDisplayName = classDesc.displayName();
    final AccessFlags classAccessFlags = classModel.flags();

    final String methodModel_name = methodModel.methodName().stringValue();
    if(methodModel_name.length() <= 1) return;

    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    final ClassDesc returnTypeDesc = methodTypeDesc.returnType();
    final AccessFlags methodAccessFlags = methodModel.flags();
    if(!methodAccessFlags.has(AccessFlag.PUBLIC)) return;
    if(methodAccessFlags.has(AccessFlag.PRIVATE)) return;
    if(methodAccessFlags.has(AccessFlag.SYNTHETIC)) return;
    if(!are_method_types_fully_public(methodModel)) return;

    if(returnTypeDesc.isPrimitive()) {
      write_method_primitive(classModel, fw_java, fw_hpp, methodModel);
      return;
    }
    if(returnTypeDesc.isClassOrInterface()) {
      write_method_object(classModel, fw_java, fw_hpp, methodModel);
      return;
    }
  }

  static boolean inner_class_is_public(final ClassModel classModel) {
    final Set<AccessFlag> accessFlags = inner_class_AccessFlags(classModel);
    return accessFlags.contains(AccessFlag.PUBLIC);
  }

  static Set<AccessFlag> inner_class_AccessFlags(final ClassModel classModel) {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final AccessFlags accessFlags = classModel.flags();

    for(final Attribute<?> attribute: classModel.attributes()) {
      if(attribute instanceof SourceFileAttribute) {
        continue;
      }
      if(attribute instanceof InnerClassesAttribute) {
        InnerClassesAttribute innerClasses = (InnerClassesAttribute)attribute;
        for(InnerClassInfo innerClassInfo: innerClasses.classes()) {
          ClassEntry outerClassEntry = innerClassInfo.outerClass().orElse(null);
          ClassEntry innerClassEntry = innerClassInfo.innerClass();
          ClassDesc innerClassDesc = innerClassEntry.asSymbol();
          String innerClassJavaName = innerClassEntry.asInternalName().replace('/', '.').replace('$', '.');

          // System.out.println(classNameJava + " 0x" + Integer.toHexString(accessFlags.flagsMask()) +
          //   " <-> innerName: " + innerClassInfo.innerName() +
          //   ", innerClass: " + innerClassEntry +
          //   ", innerClassJavaName: " + innerClassJavaName +
          //   ", outerClassEntry: " + outerClassEntry +
          //   ", classMask: 0x" + Integer.toHexString(innerClassInfo.flagsMask()));

          if(classNameJava.equals(innerClassJavaName)) {
            return innerClassInfo.flags();
          }
        }
        continue;
      }
      if(attribute instanceof SignatureAttribute) {
        continue;
      }
      if(attribute instanceof BootstrapMethodsAttribute) {
        continue;
      }
      if(attribute instanceof RuntimeVisibleAnnotationsAttribute) {
        continue;
      }
      // System.out.println(class_name + " <-> " + attribute.attributeName().stringValue() + " ");
      // if(attribute instanceof ExceptionsAttribute) {
      //   ExceptionsAttribute exceptionsAttribute = (ExceptionsAttribute)attribute;
      //   fw_java.write(" throws");
      //   for(ClassEntry classEntryException: exceptionsAttribute.exceptions()) {
      //     ClassDesc classDescException = classEntryException.asSymbol();
      //     fw_java.write(" " + classDescException.packageName() + "." + classDescException.displayName() + "");
      //   }
      // }
    }
    return accessFlags.flags();
  }

  static void make_class_file(final FileWriter java_file_list, final FileWriter hpp_file_list, final ClassModel classModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    final ClassDesc classDesc = classModel.thisClass().asSymbol();
    final String classDisplayName = classDesc.displayName();
    final AccessFlags accessFlags = classModel.flags();

    final String str_fw_java = "./src/com/xilinx/rapidwright/experimental/" + class_name + ".java";
    final FileWriter fw_java = new FileWriter(str_fw_java);

    final Path str_fw_java_path = Paths.get(str_fw_java).toAbsolutePath();

    java_file_list.write(str_fw_java_path.normalize().toString() + "\n");

    final FileWriter fw_hpp = new FileWriter("./build/experimental/" + class_name + ".hpp");
    hpp_file_list.write("#include <" + class_name + ".hpp>\n");

    fw_hpp.write("#pragma once\n");
    fw_hpp.write("\n");

    fw_hpp.write("#include <optional>\n");
    fw_hpp.write("#include <utility>\n");
    fw_hpp.write("#include <cstdlib>\n");
    fw_hpp.write("\n");

    fw_hpp.write("// This is the header file created by native-image (Graal)\n");
    fw_hpp.write("#include <librapidwright.h>\n");
    fw_hpp.write("\n");

    fw_hpp.write("namespace GraalVM_Native_Image {\n");
      
    fw_hpp.write("  struct " + class_name + " {\n");
    fw_hpp.write("    " + class_name + "() = delete;\n");
    fw_hpp.write("\n");

    fw_hpp.write("    " + class_name + "(const " + class_name + "& other) = delete;\n");
    fw_hpp.write("\n");

    fw_hpp.write("    " + class_name + "& operator=(const " + class_name + "& other) = delete;\n");
    fw_hpp.write("\n");

    fw_hpp.write("    __forceinline " + class_name + "(" + class_name + "&& other) = delete;\n");

    fw_hpp.write("    __forceinline " + class_name + "& operator=(" + class_name + "&& other) = delete;\n");

    fw_hpp.write("\n");

    fw_java.write("package com.xilinx.rapidwright.experimental;\n");
    fw_java.write("\n");

    fw_java.write("import org.graalvm.nativeimage.ObjectHandle;\n");
    fw_java.write("import org.graalvm.nativeimage.ObjectHandles;\n");
    fw_java.write("import org.graalvm.nativeimage.IsolateThread;\n");
    fw_java.write("import org.graalvm.nativeimage.UnmanagedMemory;\n");
    fw_java.write("import org.graalvm.nativeimage.c.function.CEntryPoint;\n");
    fw_java.write("import org.graalvm.nativeimage.c.type.CCharPointer;\n");
    fw_java.write("import org.graalvm.nativeimage.c.type.CTypeConversion;\n");
    fw_java.write("import org.graalvm.nativeimage.c.type.CConst;\n");
    fw_java.write("import org.graalvm.word.UnsignedWord;\n");
    fw_java.write("import org.graalvm.word.WordFactory;\n");
    fw_java.write("\n");

    fw_java.write("public class " + class_name + " /* 0x" + Integer.toHexString(accessFlags.flagsMask()) + " */ {\n");

    for(final Attribute<?> attribute: classModel.attributes()) {
      if(attribute instanceof SignatureAttribute) {
        SignatureAttribute signatureAttribute = (SignatureAttribute)attribute;
        fw_java.write("  /* SignatureAttribute " + signatureAttribute.signature() + " */\n");
        ClassSignature classSignature = signatureAttribute.asClassSignature();
        fw_java.write("    /* signatureString " + classSignature.signatureString() + " */\n");
        fw_java.write("    /* superclassSignature " + classSignature.superclassSignature() + " */\n");
        fw_java.write("    /* superinterfaceSignatures " + classSignature.superinterfaceSignatures() + " */\n");
        for(ClassTypeSig classTypeSig: classSignature.superinterfaceSignatures()) {
          fw_java.write("      /* ClassTypeSig " + classTypeSig + " */\n");
        }
        fw_java.write("    /* typeParameters " + classSignature.typeParameters() + " */\n");
        for(TypeParam typeParameter: classSignature.typeParameters()) {
          fw_java.write("      /* TypeParam " + typeParameter + " */\n");
        }
      } else {
        fw_java.write("  /* " + attribute + " */\n");
      }
    }

    for(final ClassEntry classEntryInterface: classModel.interfaces()) {
      fw_java.write("  /* INTERFACE " + classEntryInterface + " */\n");
    }

    fw_java.write("\n");

    for(final FieldModel fieldModel: classModel.fields()) {
      write_field(classModel, fw_java, fw_hpp, fieldModel);
    }

    for(final MethodModel methodModel: classModel.methods()) {
      write_method(classModel, fw_java, fw_hpp, methodModel);
    }

    fw_java.write("}\n");
    fw_hpp.write("  };\n");
    fw_hpp.write("};\n");

    fw_java.close();
    fw_hpp.close();
  }

  static void addMissingClassFromName(final String referenceJavaClassName, final String javaClassName) throws IOException {
    if(classSet.contains(javaClassName)) return;

    final String resourceName = "/" + javaClassName.replace(".", "/") + ".class";

    final InputStream baseInputStream = Class.class.getResourceAsStream(resourceName);
    final InputStream theadInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);

    if(baseInputStream == null && theadInputStream == null) {
      System.out.println("nonloading: " + referenceJavaClassName + " -> " + javaClassName);
      nonloadingClassSet.add(javaClassName.replace('$', '.'));
      return;
    }
    if(baseInputStream != null) {
      final byte[] bytes = baseInputStream.readAllBytes();
      final ClassModel classModel = ClassFile.of().parse(bytes);
      if(!missingClassSet.add(classModel)) return;
    }
    if(theadInputStream != null) {
      final byte[] bytes = theadInputStream.readAllBytes();
      final ClassModel classModel = ClassFile.of().parse(bytes);
      if(!missingClassSet.add(classModel)) return;
    }
  }
  static void addMissingClassFromClassDesc(final String referenceJavaClassName, final ClassDesc classDesc) throws IOException {
    if(classDesc.isArray()) return;
    if(classDesc.isPrimitive()) return;

    final String javaClassName = classDesc.packageName() + "." + classDesc.displayName();
    addMissingClassFromName(referenceJavaClassName, javaClassName);
  }

  static void addMissingClassFromFieldModel(final ClassModel classModel, final FieldModel fieldModel) throws IOException {
    final String javaClassName = classModel.thisClass().asInternalName().replace('/', '.');

    final AccessFlags accessFlags = fieldModel.flags();
    if(!accessFlags.has(AccessFlag.PUBLIC)) {
      // System.out.println("!PUBLIC " + javaClassName + " field 0x" + Integer.toHexString(accessFlags.flagsMask()) + " " + fieldModel.fieldName().stringValue());
      return;
    }
    if(accessFlags.has(AccessFlag.PRIVATE)) return;
    if(accessFlags.has(AccessFlag.SYNTHETIC)) return;
    if(accessFlags.has(AccessFlag.PROTECTED)) return;

    final ClassDesc field_type_class_desc = fieldModel.fieldTypeSymbol();
    addMissingClassFromClassDesc(javaClassName + " field 0x" + Integer.toHexString(accessFlags.flagsMask()) + " " + fieldModel.fieldName().stringValue(), field_type_class_desc);
  }

  static void addMissingClassesFromClassModel(final ClassModel classModel) throws IOException {
    final String javaClassName = classModel.thisClass().asInternalName().replace('/', '.');

    for(final FieldModel fieldModel: classModel.fields()) {
      addMissingClassFromFieldModel(classModel, fieldModel);
    }

    for(final MethodModel methodModel: classModel.methods()) {
      final String methodName = methodModel.methodName().stringValue();
      if(methodName.length() <= 1) {
        continue;
      }
      final AccessFlags accessFlags = methodModel.flags();
      if(!accessFlags.has(AccessFlag.PUBLIC)) continue;
      if(accessFlags.has(AccessFlag.PRIVATE)) continue;
      if(accessFlags.has(AccessFlag.PROTECTED)) continue;

      final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
      final ClassDesc returnTypeDesc = methodTypeDesc.returnType();
      addMissingClassFromClassDesc(javaClassName + "->" + methodName + " return 0x" + Integer.toHexString(accessFlags.flagsMask()), returnTypeDesc);

      for(ClassDesc param: methodTypeDesc.parameterArray()) {
        addMissingClassFromClassDesc(javaClassName + "->" + methodName + " param 0x" + Integer.toHexString(accessFlags.flagsMask()), param);
      }
    }
  }

  static void addMissingClasses() throws IOException {
    for(final ClassModel classModel: classModels) {
      addMissingClassesFromClassModel(classModel);
    }

    System.out.println("nonloadingClassSet:");
    for(final String nonloadingClass: nonloadingClassSet) {
      System.out.println("  " + nonloadingClass);
    }
    System.out.println();

    System.out.println("missingClassSet:");
    for(final ClassModel missingClass: missingClassSet) {
      System.out.println("  " + missingClass);
    }
    System.out.println();
    
  }

  public static void general(final FileWriter java_file_list, final FileWriter hpp_file_list) throws IOException {
    final String class_name = "general";
    final String str_fw_java = "./src/com/xilinx/rapidwright/experimental/general.java";
    final FileWriter fw_java = new FileWriter(str_fw_java);
    final Path str_fw_java_path = Paths.get(str_fw_java).toAbsolutePath();
    java_file_list.write(str_fw_java_path.normalize().toString() + "\n");

    fw_java.write("package com.xilinx.rapidwright.experimental;\n");
    fw_java.write("\n");

    fw_java.write("import org.graalvm.nativeimage.ObjectHandle;\n");
    fw_java.write("import org.graalvm.nativeimage.ObjectHandles;\n");
    fw_java.write("import org.graalvm.nativeimage.IsolateThread;\n");
    fw_java.write("import org.graalvm.nativeimage.UnmanagedMemory;\n");
    fw_java.write("import org.graalvm.nativeimage.c.function.CEntryPoint;\n");
    fw_java.write("import org.graalvm.nativeimage.c.type.CCharPointer;\n");
    fw_java.write("import org.graalvm.nativeimage.c.type.CTypeConversion;\n");
    fw_java.write("import org.graalvm.nativeimage.c.type.CConst;\n");
    fw_java.write("import org.graalvm.word.UnsignedWord;\n");
    fw_java.write("import org.graalvm.word.WordFactory;\n");
    fw_java.write("\n");
    
    fw_java.write("public class " + class_name +" {\n");

    fw_java.write("  @CEntryPoint(name = \"" + class_name + "_free_instance\")\n");
    fw_java.write("  public static void " + class_name + "_free_instance(IsolateThread thread, final ObjectHandle handle) {\n");
    fw_java.write("    ObjectHandles.getGlobal().destroy(handle);\n");
    fw_java.write("  }\n");

    fw_java.write("}\n");
    fw_java.close();

    hpp_file_list.write("  template<typename T>\n");
    hpp_file_list.write("  struct unique_general_destructor {\n");
    hpp_file_list.write("    inline void operator()(T* handle) const noexcept {\n");
    hpp_file_list.write("      if (handle) {\n");
    hpp_file_list.write("        if constexpr (is_debug) {\n");
    hpp_file_list.write("          std::println(std::source_location::current().function_name());\n");
    hpp_file_list.write("        }\n");
    hpp_file_list.write("        general_free_instance(localThread.get(), handle);\n");
    hpp_file_list.write("      }\n");
    hpp_file_list.write("    }\n");
    hpp_file_list.write("  };\n");
    hpp_file_list.write("\n");

  }

  public static void main(final String[] args) throws IOException {
    jarPath("./jars/rapidwright-api-lib-2026.1.1.jar");
    jarPath("./build/libs/rapidwright.jar");
    addMissingClasses();
    nonloadingClassSet.add("rank_1_int");
    nonloadingClassSet.add("rank_1_java_lang_String");
    nonloadingClassSet.add("rank_1_tcl_lang_TclObject");
    nonloadingClassSet.add("rank_1_com_xilinx_rapidwright_edif_EDIFHierPortInst");
    nonloadingClassSet.add("rank_1_byte");
    nonloadingClassSet.add("rank_1_char");
    nonloadingClassSet.add("rank_1_java_nio_file_Path");
    nonloadingClassSet.add("rank_1_short");
    nonloadingClassSet.add("rank_1_com_xilinx_rapidwright_design_Cell");
    nonloadingClassSet.add("rank_1_com_xilinx_rapidwright_design_Design");
    nonloadingClassSet.add("rank_1_com_xilinx_rapidwright_device_Tile");
    nonloadingClassSet.add("rank_2_com_xilinx_rapidwright_device_Tile");
    nonloadingClassSet.add("rank_1_com_xilinx_rapidwright_edif_EDIFCellInst");
    nonloadingClassSet.add("rank_1_java_lang_Integer");
    nonloadingClassSet.add("rank_1_java_lang_Object");

    final FileWriter java_file_list = new FileWriter("./build/java_file_list.txt");

    final FileWriter hpp_file_list = new FileWriter("./build/experimental/ExperimentalAPI.hpp");
    hpp_file_list.write("#pragma once\n\n");
    hpp_file_list.write("#include <source_location>\n\n");

    hpp_file_list.write("namespace GraalVM_Native_Image {\n");

    general(java_file_list, hpp_file_list);

    for(final ClassModel classModel: missingClassSet) {
      final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
      hpp_file_list.write("  struct " + class_name + ";\n");
    }
    for(final ClassModel classModel: classModels) {
      final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
      hpp_file_list.write("  struct " + class_name + ";\n");
    }
    for(final String nonloadingClass: nonloadingClassSet) {
      final String class_name = nonloadingClass.replace('.', '_');
      hpp_file_list.write("  struct " + class_name + ";\n");
    }


    for(final ClassModel classModel: missingClassSet) {
      final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
      hpp_file_list.write("  struct unique_" + class_name + "_destructor;\n");
    }
    for(final ClassModel classModel: classModels) {
      final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
      hpp_file_list.write("  struct unique_" + class_name + "_destructor;\n");
    }
    for(final String nonloadingClass: nonloadingClassSet) {
      final String class_name = nonloadingClass.replace('.', '_');
      hpp_file_list.write("  struct unique_" + class_name + "_destructor;\n");
    }

    for(final ClassModel classModel: missingClassSet) {
      final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
      hpp_file_list.write("  using unique_" + class_name + " = std::unique_ptr<" + class_name + ", unique_general_destructor<" + class_name + ">>;\n");
    }

    for(final ClassModel classModel: classModels) {
      final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
      hpp_file_list.write("  using unique_" + class_name + " = std::unique_ptr<" + class_name + ", unique_general_destructor<" + class_name + ">>;\n");
    }

    for(final String nonloadingClass: nonloadingClassSet) {
      final String class_name = nonloadingClass.replace('.', '_');
      hpp_file_list.write("  using unique_" + class_name + " = std::unique_ptr<" + class_name + ", unique_general_destructor<" + class_name + ">>;\n");
    }

    hpp_file_list.write("};\n\n");

    for(final ClassModel classModel: classModels) {
      make_class_file(java_file_list, hpp_file_list, classModel);
    }

    java_file_list.close();
    hpp_file_list.close();

    for(final Entry<String, Set<AccessFlag>> entry: classAccessFlagsMap.entrySet()) {
      System.out.println(entry);
    }
  }
}
