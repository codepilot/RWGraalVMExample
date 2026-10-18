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
import java.lang.classfile.attribute.ConstantValueAttribute;
import java.lang.classfile.attribute.ExceptionsAttribute;
import java.lang.classfile.attribute.InnerClassInfo;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.classfile.attribute.SourceFileAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantValueEntry;
import java.lang.classfile.constantpool.DoubleEntry;
import java.lang.classfile.constantpool.FloatEntry;
import java.lang.classfile.constantpool.IntegerEntry;
import java.lang.classfile.constantpool.LongEntry;
import java.lang.classfile.constantpool.StringEntry;
import java.lang.reflect.AccessFlag;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
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
import java.util.Arrays;
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
  private static Set<String> java_primitives = new TreeSet<>(Arrays.asList(
    "boolean",
    "int",
    "byte",
    "char",
    "short",
    "float",
    "long",
    "double"
  ));

  private static Set<String> cpp_keywords = new TreeSet<>(Arrays.asList(
    "alignas",
    "alignof",
    "and",
    "and_eq",
    "asm",
    "atomic_cancel",
    "atomic_commit",
    "atomic_noexcept",
    "auto",
    "bitand",
    "bitor",
    "bool",
    "break",
    "case",
    "catch",
    "char",
    "char8_t",
    "char16_t",
    "char32_t",
    "class",
    "compl",
    "concept",
    "const",
    "consteval",
    "constexpr",
    "constinit",
    "const_cast",
    "continue",
    "contract_assert",
    "co_await",
    "co_return",
    "co_yield",
    "decltype",
    "default",
    "delete",
    "do",
    "double",
    "dynamic_cast",
    "else",
    "enum",
    "explicit",
    "export",
    "extern",
    "false",
    "float",
    "for",
    "friend",
    "goto",
    "if",
    "inline",
    "int",
    "long",
    "mutable",
    "namespace",
    "new",
    "noexcept",
    "not",
    "not_eq",
    "nullptr",
    "operator",
    "or",
    "or_eq",
    "private",
    "protected",
    "public",
    "reflexpr",
    "register",
    "reinterpret_cast",
    "requires",
    "return",
    "short",
    "signed",
    "sizeof",
    "static",
    "static_assert",
    "static_cast",
    "struct",
    "switch",
    "synchronized",
    "template",
    "this",
    "thread_local",
    "throw",
    "true",
    "try",
    "typedef",
    "typeid",
    "typename",
    "union",
    "unsigned",
    "using",
    "virtual",
    "void",
    "volatile",
    "wchar_t",
    "while",
    "xor",
    "xor_eq"
  ));

  private static int CEntryPointCounter = 1;
  private final static SortedMap<ClassDesc, Set<AccessFlag>> classAccessFlagsMap = new TreeMap<>((ClassDesc s1, ClassDesc s2) -> s1.descriptorString().compareTo(s2.descriptorString()));
  private final static SortedMap<ClassDesc, ClassModel> classMap = new TreeMap<>((ClassDesc s1, ClassDesc s2) -> s1.descriptorString().compareTo(s2.descriptorString()));

  static int getRank(ClassDesc desc) {
    int rank = 0;
    while (desc.isArray()) {
      rank++;
      desc = desc.componentType();
    }
    return rank;
  }

  static ClassDesc getBaseComponentType(ClassDesc desc) {
    int rank = 0;
    while (desc.isArray()) {
      rank++;
      desc = desc.componentType();
    }
    return desc;
  }

  static void addClassModel(final ClassModel classModel, final boolean filterRWJar) {
    final ClassDesc classDesc = classModel.thisClass().asSymbol();
    final String classDisplayName = classDesc.displayName();
    final AccessFlags accessFlags = classModel.flags();

    // final String classNameJava = entry.getName()
    //   .substring(0, entry.getName().length() - 6)
    //   .replace('/', '.');

    if(classDesc.packageName().length() == 0) return;

    final String classNameJava = classDesc.packageName() + "." + classDesc.displayName();
    final Set<AccessFlag> accessFlagSet = accessFlags.flags();

    classAccessFlagsMap.compute(classDesc, (final ClassDesc k, final Set<AccessFlag> oldValue)-> {
      final Set<AccessFlag> newValue = accessFlagSet;
      if(oldValue == null) return newValue;

      final Set<AccessFlag> mergedValue = new HashSet<AccessFlag>(newValue);

      if(!oldValue.contains(AccessFlag.PUBLIC)) mergedValue.remove(AccessFlag.PUBLIC);
      if(oldValue.contains(AccessFlag.PROTECTED)) mergedValue.add(AccessFlag.PROTECTED);
      if(oldValue.contains(AccessFlag.SUPER)) mergedValue.add(AccessFlag.SUPER);
      if(oldValue.contains(AccessFlag.STATIC)) mergedValue.add(AccessFlag.STATIC);

      System.out.println(
        "classAccessFlagsMap.compute: " + k.packageName() + "." + k.displayName() +
        ", oldValue: " + oldValue + 
        ", newValue: " + newValue +
        ", mergedValue: " + mergedValue
      );
      return mergedValue;
    });

    for(final InnerClassesAttribute innerClasses: classModel.findAttributes(Attributes.innerClasses())) {
      for(final InnerClassInfo innerClassInfo: innerClasses.classes()) {
        final ClassEntry outerClassEntry = innerClassInfo.outerClass().orElse(null);
        final ClassEntry innerClassEntry = innerClassInfo.innerClass();
        final ClassDesc innerClassDesc = innerClassEntry.asSymbol();
        final String innerClassJavaName = innerClassEntry.asInternalName().replace('/', '.').replace('$', '.');

        final Set<AccessFlag> innerClassAccessFlags = innerClassInfo.flags();

        classAccessFlagsMap.compute(innerClassDesc, (final ClassDesc k, final Set<AccessFlag> oldValue)-> {
          final Set<AccessFlag> newValue = innerClassAccessFlags;
          if(oldValue == null) return newValue;

          final Set<AccessFlag> mergedValue = new HashSet<AccessFlag>(newValue);

          if(!oldValue.contains(AccessFlag.PUBLIC)) mergedValue.remove(AccessFlag.PUBLIC);
          if(oldValue.contains(AccessFlag.PROTECTED)) mergedValue.add(AccessFlag.PROTECTED);
          if(oldValue.contains(AccessFlag.SUPER)) mergedValue.add(AccessFlag.SUPER);
          if(oldValue.contains(AccessFlag.STATIC)) mergedValue.add(AccessFlag.STATIC);
          
          System.out.println(
            "classAccessFlagsMap.compute: " + k.packageName() + "." + k.displayName() +
            ", oldValue: " + oldValue + 
            ", newValue: " + newValue +
            ", mergedValue: " + mergedValue
          );
          return mergedValue;
        });

      }
    }      

    if(filterRWJar) {
      if(!classNameJava.startsWith("com.xilinx.rapidwright.")) {
        // System.out.println("!startsWith 'com.xilinx.rapidwright.' "+ classNameJava + ", classDesc.packageName(): '" + classDesc.packageName() + "', classDesc.displayName(): '" + classDesc.displayName() + "'");
        return;
      }

      if(classNameJava.equals("com.xilinx.rapidwright.examples.RapidWrightAPI")) {
        System.out.println("equals 'com.xilinx.rapidwright.examples.RapidWrightAPI'");
        return;
      }
    }

    if(accessFlags.has(AccessFlag.PRIVATE)) {
      // System.out.println("PRIVATE " + classNameJava + " 0x" + Integer.toHexString(accessFlags.flagsMask()));
      return;
    }

    if(!accessFlags.has(AccessFlag.PUBLIC)) {
      // System.out.println("!PUBLIC " + classNameJava + " 0x" + Integer.toHexString(accessFlags.flagsMask()));
      return;
    }

    if(!inner_class_is_public(classModel)) {
      // System.out.println("!inner_class_is_public " + classNameJava + " 0x" + Integer.toHexString(accessFlags.flagsMask()));
      return;
    }

    if(classMap.put(classDesc, classModel) != null) {
      System.out.println("classModel duplicated " + classNameJava);
    }
  }

  static void jarPath(final String jp) throws IOException {
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
      if(classDesc.packageName().length() == 0) continue;

      addClassModel(classModel, true);
    }
  }

  static void method_throws(final FileWriter fw_java, final MethodModel methodModel) throws IOException {
    final String methodModel_name = methodModel.methodName().stringValue();
    for(final Attribute<?> attribute: methodModel.attributes()) {
      if(attribute instanceof ExceptionsAttribute) {
        ExceptionsAttribute exceptionsAttribute = (ExceptionsAttribute)attribute;
        fw_java.write(" throws");
        int entryCounter = 0;
        for(ClassEntry classEntryException: exceptionsAttribute.exceptions()) {
          ClassDesc classDescException = classEntryException.asSymbol();
          if(entryCounter > 0) fw_java.write(" , ");
          fw_java.write(" " + classDescException.packageName() + "." + classDescException.displayName() + "");
          entryCounter++;
        }
      }
    }
  }

  static void write_field(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_decl_hpp, final FileWriter fw_impl_hpp, final FileWriter fw_frmt_hpp, final String root_namespace, final FieldModel fieldModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_');
    final ClassDesc classDesc = classModel.thisClass().asSymbol();

    final AccessFlags accessFlags = fieldModel.flags();
    if(!accessFlags.has(AccessFlag.PUBLIC)) return;
    if(accessFlags.has(AccessFlag.PRIVATE)) return;

    final String field_name = fieldModel.fieldName().stringValue();
    if(field_name.length() <= 1) return;

    fw_decl_hpp.write("#undef " + field_name + "\n");
    for(final Attribute<?> attribute: fieldModel.attributes()) {
      fw_decl_hpp.write("/* " + attribute + " */\n");
      if(attribute instanceof ConstantValueAttribute) {
        final ConstantValueAttribute constantValueAttribute = (ConstantValueAttribute)attribute;
        final ConstantValueEntry constantValueEntry = constantValueAttribute.constant();
        final ConstantDesc constantDesc = constantValueEntry.constantValue();

        if(constantValueEntry instanceof DoubleEntry) {
          final DoubleEntry doubleEntry = (DoubleEntry)constantValueEntry;
          final double doubleValue = doubleEntry.doubleValue();
          fw_decl_hpp.write("/* " + accessFlags + " ConstantValueAttribute DoubleEntry " + constantValueAttribute + ": " + doubleValue + " */\n");
          if(doubleValue == Double.POSITIVE_INFINITY) {
            fw_decl_hpp.write("    inline static constexpr double " + field_name + "{std::numeric_limits<double>::infinity()};\n\n");
          } else if(doubleValue == Double.NEGATIVE_INFINITY) {
            fw_decl_hpp.write("    inline static constexpr double " + field_name + "{-std::numeric_limits<double>::infinity()};\n\n");
          } else if(Double.isNaN(doubleValue)) {
            fw_decl_hpp.write("    inline static constexpr double " + field_name + "{std::numeric_limits<double>::quiet_NaN()};\n\n");
          } else {
            fw_decl_hpp.write("    inline static constexpr double " + field_name + "{" + doubleValue + "};\n\n");
          }
          return;
        } else if(constantValueEntry instanceof FloatEntry) {
          final FloatEntry floatEntry = (FloatEntry)constantValueEntry;
          final float floatValue = floatEntry.floatValue();
          fw_decl_hpp.write("/* " + accessFlags + " ConstantValueAttribute FloatEntry " + constantValueAttribute + ": " + floatValue + " */\n");
          if(floatValue == Float.POSITIVE_INFINITY) {
            fw_decl_hpp.write("    inline static constexpr float " + field_name + "{std::numeric_limits<float>::infinity()};\n\n");
          } else if(floatValue == Float.NEGATIVE_INFINITY) {
            fw_decl_hpp.write("    inline static constexpr float " + field_name + "{-std::numeric_limits<float>::infinity()};\n\n");
          } else if(Float.isNaN(floatValue)) {
            fw_decl_hpp.write("    inline static constexpr float " + field_name + "{std::numeric_limits<float>::quiet_NaN()};\n\n");
          } else {
            fw_decl_hpp.write("    inline static constexpr float " + field_name + "{" + floatValue + "f};\n\n");
          }
          return;
        } else if(constantValueEntry instanceof IntegerEntry) {
          final IntegerEntry integerEntry = (IntegerEntry)constantValueEntry;
          final int intValue = integerEntry.intValue();
          fw_decl_hpp.write("/* " + accessFlags + " ConstantValueAttribute IntegerEntry " + constantValueAttribute + ": " + intValue + " */\n");
          fw_decl_hpp.write("    inline static constexpr int32_t " + field_name + "{" + intValue + "i32};\n\n");
          return;
        } else if(constantValueEntry instanceof LongEntry) {
          final LongEntry longEntry = (LongEntry)constantValueEntry;
          final long longValue = longEntry.longValue();
          fw_decl_hpp.write("/* " + accessFlags + " ConstantValueAttribute LongEntry " + constantValueAttribute + ": " + longValue + " */\n");
          fw_decl_hpp.write("    inline static constexpr int64_t " + field_name + "{" + longValue + "i64};\n\n");
          return;
        } else if(constantValueEntry instanceof StringEntry) {
          final StringEntry stringEntry = (StringEntry)constantValueEntry;
          final String stringValue = stringEntry.stringValue();
          fw_decl_hpp.write("/* " + accessFlags + " ConstantValueAttribute StringEntry " + constantValueAttribute + ": \"" + stringValue + "\" */\n");
          fw_decl_hpp.write("    inline static constexpr std::string_view " + field_name + "{\"" + stringValue.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"};\n\n");
          return;
        } else {
          fw_decl_hpp.write("/* " + accessFlags + " ConstantValueAttribute " + constantValueEntry.typeKind() + " " + constantValueAttribute + ": " + constantDesc + " */\n");
        }
      }
    }

    final String field_type_name = fieldModel.fieldType().stringValue();
    final ClassDesc field_type_class_desc = fieldModel.fieldTypeSymbol();
    final int CEntryPointID_getter = CEntryPointCounter++;
    final int CEntryPointID_setter = CEntryPointCounter++;
    final String cpp_static_keyword = accessFlags.has(AccessFlag.STATIC)?"inline static ":"";
    final String static_descriptor = accessFlags.has(AccessFlag.STATIC)?"static_":"";
    final String java_class_name = ((field_type_class_desc.packageName().length()>0)?(field_type_class_desc.packageName() + "."):"") + field_type_class_desc.displayName();
    final String cpp_class_name = java_class_name.replace(".", "_");
    final String java_instance_arg = accessFlags.has(AccessFlag.STATIC)?"":", @CTypedef(name = \"OBJECT_HANDLE_" + class_name + "\") final ObjectHandle instance_handle";
    final String java_unbox_instance = accessFlags.has(AccessFlag.STATIC)?"":"    final " + classNameJava + " instance = ObjectHandles.getGlobal().get(instance_handle);\n";
    final String java_unbox_setter_value = field_type_class_desc.isPrimitive()?"setter_value":"ObjectHandles.getGlobal().get(setter_value)";
    final String cpp_unbox_setter_value = field_type_class_desc.isPrimitive()?"val":"val.ObjectHandle";
    final String java_box_return_prefix = field_type_class_desc.isPrimitive()?"":"ObjectHandles.getGlobal().create(";
    final String java_box_return_postfix = field_type_class_desc.isPrimitive()?"":")";
    String cpp_box_return_prefix = "";
    String cpp_box_return_postfix = "";
    final String java_field_base = accessFlags.has(AccessFlag.STATIC)?classNameJava:"instance";
    final String cpp_getter_specifier = accessFlags.has(AccessFlag.STATIC)?"noexcept":"const noexcept";
    final String cpp_func_static = accessFlags.has(AccessFlag.STATIC)?"static ":"";
    final String cpp_this_arg = accessFlags.has(AccessFlag.STATIC)?"":", std::bit_cast<" + classDesc.displayName() + "*>(this)->ObjectHandle";

    String cpp_type = cpp_class_name;
    if(cpp_type.equals("boolean")) cpp_type = "int32_t";
    if(cpp_type.equals("char")) cpp_type = "int16_t";
    if(cpp_type.equals("long")) cpp_type = "int64_t";
    if(!field_type_class_desc.isPrimitive()) cpp_type = "unique_" + cpp_type;

    final String cpp_putter = accessFlags.has(AccessFlag.FINAL)?"":", put = put_" + field_name;

    String java_get_ret = field_type_class_desc.displayName();
    String java_get_ret_CTypedef = "";
    if(field_type_class_desc.isClassOrInterface()) {
      java_get_ret = "ObjectHandle";
      java_get_ret_CTypedef = "  @CTypedef(name = \"OBJECT_HANDLE_" + cpp_class_name + "\")\n";
      cpp_type = "::" + field_type_class_desc.packageName().replace(".", "::") + "::" + field_type_class_desc.displayName();
      cpp_box_return_prefix = cpp_type + "(";
      cpp_box_return_postfix = ")";
    }

    if(field_type_class_desc.isArray()) {
      final int rank = getRank(field_type_class_desc);
      final ClassDesc base = getBaseComponentType(field_type_class_desc);

      java_get_ret = "ObjectHandle";
      if(base.packageName().length() > 0) {
        final String base_class_name = base.packageName().replace(".", "_") + "_" + base.displayName().replace(".", "_");
        java_get_ret_CTypedef = "  @CTypedef(name = \"OBJECT_HANDLE_RANK_" + rank + "_" + base_class_name + "\")\n";
        cpp_type = "::" + base.packageName().replace(".", "::") + "::RANK_" + rank + "_" + base.displayName();
      } else {
        final String base_class_name = base.displayName().replace(".", "_");
        java_get_ret_CTypedef = "  @CTypedef(name = \"OBJECT_HANDLE_RANK_" + rank + "_" +  base_class_name + "\")\n";
        cpp_type = "::RANK_" + rank + "_" + base.displayName();
      }
      cpp_box_return_prefix = cpp_type + "(";
      cpp_box_return_postfix = ")";
    }

    fw_java.write("\n");
    fw_java.write("  @CEntryPoint(name = \"id_" + CEntryPointID_getter + "_" + class_name + "_get_" + static_descriptor + "field_" + field_name + "\") /* 0x" + Integer.toHexString(accessFlags.flagsMask()) + " */\n");
    fw_java.write(java_get_ret_CTypedef);
    fw_java.write("  public static " + java_get_ret + " id_" + CEntryPointID_getter + "_" + class_name + "_get_" + static_descriptor + "field_" + field_name + "(IsolateThread thread" + java_instance_arg + ") {\n");
    fw_java.write(java_unbox_instance);
    fw_java.write("    final " + java_get_ret + " retval = " + java_box_return_prefix + java_field_base + "." + field_name + java_box_return_postfix +";\n");
    fw_java.write("    return retval;\n");
    fw_java.write("  }\n\n");

    if(!accessFlags.has(AccessFlag.FINAL)) {
      fw_java.write("\n");
      fw_java.write("  @CEntryPoint(name = \"id_" + CEntryPointID_setter + "_" + class_name + "_set_" + static_descriptor + "field_" + field_name + "\") /* 0x" + Integer.toHexString(accessFlags.flagsMask()) + " */\n");
      fw_java.write("  public static void id_" + CEntryPointID_setter + "_" + class_name + "_set_" + static_descriptor + "field_" + field_name + "(IsolateThread thread" + java_instance_arg + ", " + java_get_ret_CTypedef + " " + java_get_ret + " setter_value) {\n");
      fw_java.write(java_unbox_instance);
      fw_java.write("    " + java_field_base + "." + field_name + " = " + java_unbox_setter_value + ";\n");
      fw_java.write("  }\n\n");
    }

    if(accessFlags.has(AccessFlag.STATIC)) {
      fw_decl_hpp.write("\n");
      fw_decl_hpp.write("    struct Property_" + CEntryPointID_getter + "_" + field_name + " {\n");

      fw_decl_hpp.write("\n");
      fw_decl_hpp.write("      /* static getter " + cpp_type + " " + field_name + "*/\n");
      fw_decl_hpp.write("      __forceinline operator " + cpp_type + "() const noexcept;\n");

      fw_impl_hpp.write("  __forceinline " + classDesc.displayName() + "::Property_" + CEntryPointID_getter + "_" + field_name + "::operator " + cpp_type + "() const noexcept {\n");
      fw_impl_hpp.write("    return " + cpp_box_return_prefix + "id_" + CEntryPointID_getter + "_" + class_name + "_get_" + static_descriptor + "field_" + field_name + "(localThread.get()" + cpp_this_arg + ")" + cpp_box_return_postfix + ";\n");
      fw_impl_hpp.write("  }\n\n");

      if(!accessFlags.has(AccessFlag.FINAL)) {
        fw_decl_hpp.write("\n");
        fw_decl_hpp.write("      /* static setter " + cpp_type + " " + field_name + "*/\n");
        fw_decl_hpp.write("      __forceinline Property_" + CEntryPointID_getter + "_" + field_name + "& operator=(const " + cpp_type + "& val) noexcept;\n");
        fw_impl_hpp.write("  __forceinline " + classDesc.displayName() + "::Property_" + CEntryPointID_getter + "_" + field_name + "& " + classDesc.displayName() + "::Property_" + CEntryPointID_getter + "_" + field_name + "::operator=(const " + cpp_type + "& val) noexcept {\n");
        fw_impl_hpp.write("    id_" + CEntryPointID_setter + "_" + class_name + "_set_" + static_descriptor + "field_" + field_name + "(localThread.get()" + cpp_this_arg + ", " + cpp_unbox_setter_value + ");\n");
        fw_impl_hpp.write("    return *this;\n");
        fw_impl_hpp.write("  }\n\n");
      }

      fw_decl_hpp.write("    };\n\n");
      fw_decl_hpp.write("    static Property_" + CEntryPointID_getter + "_" + field_name + " " + field_name + ";\n\n");
      fw_impl_hpp.write("  " + classDesc.displayName() + "::Property_" + CEntryPointID_getter + "_" + field_name + " " + classDesc.displayName() + "::" + field_name + "{};\n\n");

      if(field_type_class_desc.isPrimitive()) {
        fw_frmt_hpp.write("template <typename CharT>\n");
        fw_frmt_hpp.write("struct ::std::formatter<" + root_namespace + classDesc.displayName() + "::Property_" + CEntryPointID_getter + "_" + field_name + ", CharT> : std::formatter<" + cpp_type + ", CharT> {\n");
        fw_frmt_hpp.write("  __forceinline constexpr auto format(const " + root_namespace + classDesc.displayName() + "::Property_" + CEntryPointID_getter + "_" + field_name + "& wrapper, auto& ctx) const {\n");
        fw_frmt_hpp.write("    return std::formatter<" + cpp_type + ", CharT>::format(wrapper.operator " + cpp_type + "(), ctx);\n");
        fw_frmt_hpp.write("  }\n");
        fw_frmt_hpp.write("};\n\n");
      }

    } else {
      // fw_decl_hpp.write("    [[no_unique_address]] Property_" + CEntryPointID + "_" + field_name + " " + field_name + ";\n\n");

    }

    // if(field_type_class_desc.isClassOrInterface()) {
    //   if(accessFlags.has(AccessFlag.STATIC)) {

    //     if(accessFlags.has(AccessFlag.FINAL)) {
    //       // fw_impl_hpp.write("    inline static const unique_" + cpp_class_name + " " + field_name + "{std::bit_cast<" + cpp_class_name + " *>(" + class_name + "_get_static_field_" + field_name + "(localThread.get()))};\n");
    //       fw_decl_hpp.write("    __forceinline static ::" + field_type_class_desc.packageName().replace(".", "::") + "::" + field_type_class_desc.displayName() + " get_" + field_name + "() noexcept;\n");
    //       fw_decl_hpp.write("    static ::" + field_type_class_desc.packageName().replace(".", "::") + "::" + field_type_class_desc.displayName() + " " + field_name + ";\n");

    //       fw_impl_hpp.write("\n");
    //       fw_impl_hpp.write("    __forceinline ::" + field_type_class_desc.packageName().replace(".", "::") + "::" + field_type_class_desc.displayName() + " " + classDesc.displayName() + "::get_" + field_name + "() noexcept {\n");
    //       fw_impl_hpp.write("      if constexpr (is_debug) {\n");
    //       fw_impl_hpp.write("        std::println(\"unique_" + cpp_class_name + "\");\n");
    //       fw_impl_hpp.write("      }\n");
    //       fw_impl_hpp.write("      return ::" + field_type_class_desc.packageName().replace(".", "::") + "::" + field_type_class_desc.displayName() + "(id_" + CEntryPointID + "_" + class_name + "_get_static_field_" + field_name + "(localThread.get()));\n");
    //       fw_impl_hpp.write("    }\n");
    //       fw_impl_hpp.write("    ::" + field_type_class_desc.packageName().replace(".", "::") + "::" + field_type_class_desc.displayName() + " " + classDesc.displayName() + "::" + field_name + "{get_" + field_name + "()};\n");
    //     } else {
    //       fw_impl_hpp.write("\n");
    //       fw_impl_hpp.write("    __forceinline static ::" + field_type_class_desc.packageName().replace(".", "::") + "::" + field_type_class_desc.displayName() + " get_" + field_name + "() noexcept {\n");
    //       fw_impl_hpp.write("      if constexpr (is_debug) {\n");
    //       fw_impl_hpp.write("        std::println(\"unique_" + cpp_class_name + "\");\n");
    //       fw_impl_hpp.write("      }\n");
    //       fw_impl_hpp.write("      return ::" + field_type_class_desc.packageName().replace(".", "::") + "::" + field_type_class_desc.displayName() + "(id_" + CEntryPointID + "_" + class_name + "_get_static_field_" + field_name + "(localThread.get()));\n");
    //       fw_impl_hpp.write("    }\n");
    //     }
    //   } else {

    //     fw_decl_hpp.write("    __forceinline ::" + field_type_class_desc.packageName().replace(".", "::") + "::" + field_type_class_desc.displayName() + " " + "get_" + field_name + "() const noexcept;\n");
    //     fw_impl_hpp.write("    __forceinline ::" + field_type_class_desc.packageName().replace(".", "::") + "::" + field_type_class_desc.displayName() + " " + classDesc.displayName() + "::get_" + field_name + "() const noexcept {\n");
    //     fw_impl_hpp.write("      return ::" + field_type_class_desc.packageName().replace(".", "::") + "::" + field_type_class_desc.displayName() + "(id_" + CEntryPointID + "_" + class_name + "_get_field_" + field_name + "(localThread.get(), ObjectHandle));\n");
    //     fw_impl_hpp.write("    }\n");
    //   }
    // }

    // if(!accessFlags.has(AccessFlag.STATIC)) {      
    //   fw_impl_hpp.write("__declspec(property(get = get_" + static_descriptor+ field_name + cpp_putter + ")) " + cpp_static_keyword + " " + cpp_type + " " + field_name + ";\n");
    // }

  }

  static void write_constructor(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_def_hpp, final FileWriter fw_impl_hpp, final MethodModel methodModel) throws IOException {
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

    final String cEntryPoint = "constructor_" + CEntryPointID + "_" + class_name;

    fw_java.write("\n");
    fw_java.write("  @CEntryPoint(name = \"" + cEntryPoint + "\") @CTypedef(name = \"" + class_name + " *\") /* method: 0x" + Integer.toHexString(methodAccessFlags.flagsMask()) + ", class: 0x" + Integer.toHexString(classAccessFlags.flagsMask()) + " */\n");
    fw_java.write("  public static ObjectHandle " + cEntryPoint + "(IsolateThread thread");
    write_java_arguments(fw_java, methodModel);
    fw_java.write(")");
    method_throws(fw_java, methodModel);
    fw_java.write(" {\n");
    write_java_arguments_unboxing(fw_java, methodModel);
    fw_java.write("    final " + classNameJava + " instance = new " + classNameJava + "(");
    write_java_method_params(fw_java, methodModel);
    fw_java.write(");\n");
    fw_java.write("    final ObjectHandle handle = ObjectHandles.getGlobal().create(instance);\n");
    fw_java.write("    return handle;\n");
    fw_java.write("  }\n");

    fw_impl_hpp.write("    __forceinline static unique_" + class_name + " constructor(");
    write_hpp_arguments(fw_impl_hpp, methodModel);
    fw_impl_hpp.write(") noexcept {\n");
    write_hpp_arguments_unboxing(fw_impl_hpp, methodModel);
    fw_impl_hpp.write("      return unique_" + class_name + "(" + cEntryPoint + "(localThread.get()");
    write_hpp_method_params(fw_impl_hpp, methodModel);
    fw_impl_hpp.write("));\n");
    fw_impl_hpp.write("    }\n");
    fw_impl_hpp.write("\n");
  }

  static void write_hpp_method_params(final FileWriter fw, final MethodModel methodModel) throws IOException {
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    int param_counter = 0;
    for(ClassDesc param: methodTypeDesc.parameterArray()) {
      final int param_id = param_counter++;
      fw.write(", param_" + param_id);
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
        final String class_name = ((param.packageName().length() > 0?(param.packageName() + "."):"") + param.displayName()).replace('.', '_');
        if(class_name.equals("java_lang_String")) {
          fw_java.write(", " + "@CConst final CCharPointer param_handle_" + param_id);
        } else {
          fw_java.write(", " + "@CTypedef(name = \"OBJECT_HANDLE_" + class_name + "\") final ObjectHandle param_handle_" + param_id);
        }
        // System.out.println("        " + param.packageName() + "." + param.displayName());
      }
      if(param.isArray()) {

        final int rank = getRank(param);
        final ClassDesc base = getBaseComponentType(param);
        if(base.packageName().length() > 0) {
          final String class_name = base.packageName().replace(".", "_") + "_" + base.displayName().replace(".", "_");
          fw_java.write(", " + "@CTypedef(name = \"OBJECT_HANDLE_RANK_" + rank + "_" + class_name + "\") final ObjectHandle param_handle_" + param_id);
        } else {
          final String class_name = base.displayName().replace(".", "_");
          fw_java.write(", " + "@CTypedef(name = \"OBJECT_HANDLE_RANK_" + rank + "_" +  class_name + "\") final ObjectHandle param_handle_" + param_id);
        }
        
        // System.out.println("        " + param.displayName());
      }
    }
  }

  static String replaceLast(String text, String target, String replacement) {
    final int index = text.lastIndexOf(target);
    if (index == -1) return text;
    return text.substring(0, index) + replacement + text.substring(index + target.length());
  }

  static void write_hpp_arguments(final FileWriter fw, final MethodModel methodModel) throws IOException {
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    int param_counter = 0;
    for(ClassDesc param: methodTypeDesc.parameterArray()) {
      final int param_id = param_counter++;

      if(param_id > 0) {
        fw.write(", ");
      }

      if(param.isPrimitive()) {
        if(param.displayName().equals("boolean")) {
          fw.write("const bool param_" + param_id);
        } else if(param.displayName().equals("byte")) {
          fw.write("const uint8_t param_" + param_id);
        } else {
          fw.write("const " + param.displayName() + " param_" + param_id);
        }
        // System.out.println("        " + param.displayName());
      }

      if(param.isClassOrInterface()) {
        final String paramJavaClass = (param.packageName() + "." + param.displayName());
        if(paramJavaClass.equals("java.lang.String")) {
          fw.write("const char* param_handle_" + param_id);
        } else {
          fw.write("::" + param.packageName().replace(".", "::") + "::" + param.displayName() + " &param_handle_" + param_id);
        }
        // fw_impl_hpp.write("void* param_" + param_id);
      }

      if(param.isArray()) {
        final int rank = getRank(param);
        final ClassDesc base = getBaseComponentType(param);
        if(base.packageName().length() > 0) {
          final String baseJavaName = "::" + ((base.packageName() + ".") + base.displayName()).replace(".", "::");
          fw.write(replaceLast(baseJavaName, "::", "::RANK_" + rank + "_") + " &param_handle_" + param_id);
            // "::" + base.packageName().replace(".", "::") + "::RANK_" + rank + "_" + base.displayName() + " &param_handle_" + param_id);
        } else {
          fw.write("RANK_" + rank + "_" + base.displayName() + " &param_handle_" + param_id);
        }
        // final String paramJavaClass = (base.packageName() + ((base.packageName().length() > 0)?".":"") + base.displayName()).replace('$', '.');
        // final String param_class = paramJavaClass.replace('.', '_');
        // fw.write("RANK_" + rank + "_" + param_class + " &param_handle_" + param_id);
        // System.out.println("        " + param.displayName());
      }
    }
  }

  static void write_java_arguments_unboxing(final FileWriter fw_java, final MethodModel methodModel) throws IOException {
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    int param_counter = 0;

    for(ClassDesc param: methodTypeDesc.parameterArray()) {
      final int param_id = param_counter++;

      if(param.isClassOrInterface()) {
        final String param_type_str = (param.packageName() + "." + param.displayName()).replace('$', '.');
        if(param_type_str.equals("java.lang.String")) {
          fw_java.write("    final String param_" + param_id + " = CTypeConversion.toJavaString(param_handle_" + param_id + ");\n");
        } else {
          fw_java.write("    final " + param_type_str + " param_" + param_id + " = ObjectHandles.getGlobal().get(param_handle_" + param_id + ");\n");
        }
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

  static void write_hpp_arguments_unboxing(final FileWriter fw, final MethodModel methodModel) throws IOException {
    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    int param_counter = 0;

    for(ClassDesc param: methodTypeDesc.parameterArray()) {
      final int param_id = param_counter++;

      if(param.isClassOrInterface()) {
        final String param_type_str = (param.packageName() + "." + param.displayName()).replace('$', '.');
        if(param_type_str.equals("java.lang.String")) {
          fw.write("      const char* /* " + param_type_str + " */ param_" + param_id + "{param_handle_" + param_id + "};\n");
        } else {
          fw.write("      auto /* " + param_type_str + " */ param_" + param_id + "{param_handle_" + param_id + ".ObjectHandle};\n");
        }
      }
      if(param.isArray()) {
        ClassDesc componentDesc = param.componentType();
        if(componentDesc.packageName().length() > 0) {
          final String param_type_str = (componentDesc.packageName() + "." + componentDesc.displayName()).replace('$', '.');
          fw.write("      auto /* " + param_type_str + "[] */ param_" + param_id + " = param_handle_" + param_id + ".ObjectHandle; // pkg:" + componentDesc.packageName() + " " + componentDesc + "\n");
        } else {
          final String param_type_str = (componentDesc.displayName()).replace('$', '.');
          fw.write("      auto /* " + param_type_str + "[] */ param_" + param_id + " = param_handle_" + param_id + ".ObjectHandle; // pkg:" + componentDesc.packageName() + " " + componentDesc + "\n");
        }
      }
    }
  }

  static boolean is_ClassDesc_public(final ClassDesc classDesc) {
    if(classDesc.isPrimitive()) return true;
    if(classDesc.isClassOrInterface()) {
      final String javaClass = (classDesc.packageName() + "." + classDesc.displayName()).replace('$', '.');
      final Set<AccessFlag> accessFlags = classAccessFlagsMap.get(classDesc);
      if(accessFlags == null) {
        // System.out.println("accessFlags == null for " + javaClass);
        return true;
      }
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

  static void write_method(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_decl_hpp, final FileWriter fw_impl_hpp, final MethodModel methodModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_');
    final ClassDesc classDesc = classModel.thisClass().asSymbol();
    final String classDisplayName = classDesc.displayName();
    final AccessFlags classAccessFlags = classModel.flags();

    final String java_methodModel_name = methodModel.methodName().stringValue();
    if(java_methodModel_name.length() <= 1) return;

    String cpp_methodModel_name = methodModel.methodName().stringValue();
    if(cpp_keywords.contains(cpp_methodModel_name)) {
      cpp_methodModel_name = cpp_methodModel_name + "_";
    }

    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    final ClassDesc returnTypeDesc = methodTypeDesc.returnType();
    final AccessFlags methodAccessFlags = methodModel.flags();
    if(!methodAccessFlags.has(AccessFlag.PUBLIC)) return;
    if(methodAccessFlags.has(AccessFlag.PRIVATE)) return;
    if(methodAccessFlags.has(AccessFlag.SYNTHETIC)) return;
    if(!are_method_types_fully_public(methodModel)) return;

    if(java_methodModel_name.equals("<init>")) {
      // write_constructor(classModel, fw_java, fw_def_hpp, fw_impl_hpp, methodModel);
      return;
    }

    final String return_keyword = returnTypeDesc.displayName().equals("void")?"":"return ";
    String java_return_type = returnTypeDesc.isPrimitive()?returnTypeDesc.displayName():"ObjectHandle";
    final String java_return_translation_start = returnTypeDesc.isPrimitive()?"":"ObjectHandles.getGlobal().create(";
    final String java_return_translation_end = returnTypeDesc.isPrimitive()?"":")";
    final String cpp_return_type = returnTypeDesc.displayName().equals("void")?"void":"auto";

    final int arrayRank = getRank(returnTypeDesc);
    final ClassDesc arrayBase = getBaseComponentType(returnTypeDesc);
    final String arrayBaseJavaClass = (arrayBase.packageName() + ((arrayBase.packageName().length() > 0)?".":"") + arrayBase.displayName());
    final String array_base_class = arrayBaseJavaClass.replace('.', '_');
    final String cpp_array_type = "RANK_" + arrayRank + "_" + array_base_class;

    final String cpp_class_name = returnTypeDesc.isArray()?cpp_array_type:(((returnTypeDesc.packageName().length() > 0)?(returnTypeDesc.packageName() + "."):"") + returnTypeDesc.displayName()).replace('.', '_');
    String cpp_return_translation_start = returnTypeDesc.isPrimitive()?"":("::" + returnTypeDesc.packageName().replace(".", "::") + "::" + returnTypeDesc.displayName() + "(");
    if(returnTypeDesc.isArray()) {
      if(arrayBase.isPrimitive()) {
        cpp_return_translation_start = "::RANK_" + arrayRank + "_" + arrayBase.displayName() + "(";
      } else {
        cpp_return_translation_start = "::" + arrayBase.packageName().replace(".", "::") + "::RANK_" + arrayRank + "_" +  arrayBase.displayName() + "(";
      }
    }
    final String cpp_return_translation_end = returnTypeDesc.isPrimitive()?"":")";
    final int CEntryPointID = CEntryPointCounter++;


    final String cEntryPoint = "id_" + CEntryPointID + "_" + class_name + "_" + java_methodModel_name;
    final String java_this_keyword = methodAccessFlags.has(AccessFlag.STATIC)?"":", @CTypedef(name = \"OBJECT_HANDLE_" + class_name + "\") final ObjectHandle handle_this";
    final String java_method_base = methodAccessFlags.has(AccessFlag.STATIC)?classNameJava:"instance";

    fw_java.write("  @CEntryPoint(name = \"" + cEntryPoint + "\") /* 0x" + Integer.toHexString(methodAccessFlags.flagsMask()) + " */\n");
    if(!returnTypeDesc.isPrimitive()) {
      if(cpp_class_name.equals("java_lang_String")) {
        fw_java.write("@CTypedef(name = \"char *\")\n");
        java_return_type = "CCharPointer";
      } else {
        fw_java.write("@CTypedef(name = \"OBJECT_HANDLE_" + cpp_class_name + "\")\n");
      }
    }
    fw_java.write("  public static " + java_return_type + " " + cEntryPoint + "(IsolateThread thread" + java_this_keyword);

    write_java_arguments(fw_java, methodModel);
    fw_java.write(")");
    method_throws(fw_java, methodModel);
    fw_java.write(" {\n");
    if(!methodAccessFlags.has(AccessFlag.STATIC)) {
      fw_java.write("    final " + classNameJava + " instance = ObjectHandles.getGlobal().get(handle_this);\n");
    }

    write_java_arguments_unboxing(fw_java, methodModel);

    if(cpp_class_name.equals("java_lang_String")) {
      fw_java.write("    final String retval = " + java_method_base + "." + java_methodModel_name + "(");
      write_java_method_params(fw_java, methodModel);
      fw_java.write(");\n");
      fw_java.write("    final byte[] bytes = retval.getBytes();\n");
      fw_java.write("    final UnsignedWord size = WordFactory.unsigned(bytes.length + 1);\n");
      fw_java.write("    final CCharPointer buffer = UnmanagedMemory.malloc(size);\n");
      fw_java.write("    CTypeConversion.toCString(retval, buffer, size);\n");
      fw_java.write("    return buffer;\n");
    } else {
      fw_java.write("    " + return_keyword + java_return_translation_start + java_method_base + "." + java_methodModel_name + "(");
      write_java_method_params(fw_java, methodModel);
      fw_java.write(")" + java_return_translation_end + " ;\n");
    }
    fw_java.write("  }\n");

    final String cpp_static_keyword = methodAccessFlags.has(AccessFlag.STATIC)?"static ":"";
    final String cpp_this_keyword = methodAccessFlags.has(AccessFlag.STATIC)?"":", ObjectHandle";

    if(cpp_class_name.equals("java_lang_String")) {
      fw_decl_hpp.write("    __forceinline " + cpp_static_keyword + "const char *" + " " + cpp_methodModel_name + "(");
      fw_impl_hpp.write("    __forceinline " + "const char *" + " " + classDisplayName + "::" + cpp_methodModel_name + "(");
    } else  {
      fw_decl_hpp.write("    __forceinline " + cpp_static_keyword + cpp_return_type + " " + cpp_methodModel_name + "(");
      fw_impl_hpp.write("    __forceinline " + cpp_return_type + " " + classDisplayName + "::" + cpp_methodModel_name + "(");
    }

    write_hpp_arguments(fw_decl_hpp, methodModel);
    write_hpp_arguments(fw_impl_hpp, methodModel);

    fw_decl_hpp.write(") noexcept;\n");

    for(final Attribute<?> attribute: methodModel.attributes()) {
      fw_decl_hpp.write("/* " + attribute + " */\n");
    }

    fw_impl_hpp.write(") noexcept {\n");
    write_hpp_arguments_unboxing(fw_impl_hpp, methodModel);
    if(cpp_class_name.equals("java_lang_String")) {
      fw_impl_hpp.write("      " + return_keyword + cEntryPoint + "(localThread.get()" + cpp_this_keyword);
      write_hpp_method_params(fw_impl_hpp, methodModel);
      fw_impl_hpp.write(")" + ";\n");
    } else {
      fw_impl_hpp.write("      " + return_keyword + cpp_return_translation_start + cEntryPoint + "(localThread.get()" + cpp_this_keyword);
      write_hpp_method_params(fw_impl_hpp, methodModel);
      fw_impl_hpp.write(")" + cpp_return_translation_end + ";\n");
    }
    fw_impl_hpp.write("    }\n");
    fw_impl_hpp.write("\n");
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

  // static void make_class_from_ClassDesc(final FileWriter java_file_list, final FileWriter hpp_frwd_list, final FileWriter hpp_decl_list, final FileWriter hpp_impl_list, final ClassDesc classDesc) throws IOException {
  //   final String classNameJava = (classDesc.packageName() + "." + classDesc.displayName()); //classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
  //   final String class_name = classNameJava.replace('.', '_');
  //   final String classDisplayName = classDesc.displayName();
  //   final String namespace = classDesc.packageName().replace(".", "::");

  //   final String str_fw_java = "./src/com/xilinx/rapidwright/experimental/" + class_name + ".java";
  //   final FileWriter fw_java = new FileWriter(str_fw_java);

  //   final Path str_fw_java_path = Paths.get(str_fw_java).toAbsolutePath();

  //   java_file_list.write(str_fw_java_path.normalize().toString() + "\n");

  //   final FileWriter fw_frwd_hpp = new FileWriter("./build/experimental/ClassDesc_frwd_" + class_name + ".hpp");
  //   final FileWriter fw_decl_hpp = new FileWriter("./build/experimental/ClassDesc_decl_" + class_name + ".hpp");
  //   final FileWriter fw_impl_hpp = new FileWriter("./build/experimental/ClassDesc_impl_" + class_name + ".hpp");
  //   hpp_frwd_list.write("#include <ClassDesc_frwd_" + class_name + ".hpp>\n");
  //   hpp_decl_list.write("#include <ClassDesc_decl_" + class_name + ".hpp>\n");
  //   hpp_impl_list.write("#include <ClassDesc_impl_" + class_name + ".hpp>\n");

  //   fw_decl_hpp.write("#pragma once\n\n");
  //   fw_decl_hpp.write("#include <optional>\n");
  //   fw_decl_hpp.write("#include <utility>\n");
  //   fw_decl_hpp.write("#include <cstdlib>\n");
  //   fw_decl_hpp.write("\n");

  //   fw_impl_hpp.write("#pragma once\n\n");
  //   fw_impl_hpp.write("#include <optional>\n");
  //   fw_impl_hpp.write("#include <utility>\n");
  //   fw_impl_hpp.write("#include <cstdlib>\n");
  //   fw_impl_hpp.write("\n");

  //   fw_frwd_hpp.write("struct OBJECT_" + class_name + ";\n");
  //   fw_frwd_hpp.write("struct OBJECT_RANK_1_" + class_name + ";\n");
  //   fw_frwd_hpp.write("struct OBJECT_RANK_2_" + class_name + ";\n");
  //   fw_frwd_hpp.write("using OBJECT_HANDLE_" + class_name + " = OBJECT_" + class_name + "*;\n");
  //   fw_frwd_hpp.write("using OBJECT_HANDLE_RANK_1_" + class_name + " = OBJECT_RANK_1_" + class_name + "*;\n");
  //   fw_frwd_hpp.write("using OBJECT_HANDLE_RANK_2_" + class_name + " = OBJECT_RANK_2_" + class_name + "*;\n");

  //   fw_frwd_hpp.write("namespace " + namespace + " {\n");
  //   fw_decl_hpp.write("namespace " + namespace + " {\n");
  //   fw_impl_hpp.write("namespace " + namespace + " {\n");

  //   fw_frwd_hpp.write("  struct " + classDisplayName + ";\n");
  //   fw_frwd_hpp.write("  struct RANK_1_" + classDisplayName + ";\n");
  //   fw_frwd_hpp.write("  struct RANK_2_" + classDisplayName + ";\n");
  //   fw_decl_hpp.write("  struct " + classDisplayName + " {\n");

  //   fw_decl_hpp.write("    /* deleted default constructor */\n");
  //   fw_decl_hpp.write("    " + classDisplayName + "() = delete;\n\n");

  //   fw_decl_hpp.write("    /* deleted copy constructor */\n");
  //   fw_decl_hpp.write("    " + classDisplayName + "(const " + classDisplayName + "& other) = delete;\n\n");
    
  //   fw_decl_hpp.write("    /* deleted move constructor */\n");
  //   fw_decl_hpp.write("    " + classDisplayName + "(" + classDisplayName + "&& other) = delete;\n\n");

  //   fw_decl_hpp.write("    /* deleted copy assignment */\n");
  //   fw_decl_hpp.write("    " + classDisplayName + "& operator=(const " + classDisplayName + "& other) = delete;\n\n");

  //   fw_decl_hpp.write("    /* deleted move assignment */\n");
  //   fw_decl_hpp.write("    " + classDisplayName + "& operator=(" + classDisplayName + "&& other) = delete;\n\n");

  //   fw_decl_hpp.write("    ~" + classDisplayName + "();\n\n");

  //   fw_java.write("package com.xilinx.rapidwright.experimental;\n");
  //   fw_java.write("\n");

  //   fw_java.write("import org.graalvm.nativeimage.ObjectHandle;\n");
  //   fw_java.write("import org.graalvm.nativeimage.ObjectHandles;\n");
  //   fw_java.write("import org.graalvm.nativeimage.IsolateThread;\n");
  //   fw_java.write("import org.graalvm.nativeimage.UnmanagedMemory;\n");
  //   fw_java.write("import org.graalvm.nativeimage.c.function.CEntryPoint;\n");
  //   fw_java.write("import org.graalvm.nativeimage.c.type.CCharPointer;\n");
  //   fw_java.write("import org.graalvm.nativeimage.c.type.CTypeConversion;\n");
  //   fw_java.write("import org.graalvm.nativeimage.c.type.CTypedef;\n");
  //   fw_java.write("import org.graalvm.nativeimage.c.type.CConst;\n");
  //   fw_java.write("import org.graalvm.word.UnsignedWord;\n");
  //   fw_java.write("import org.graalvm.word.WordFactory;\n");
  //   fw_java.write("\n");

  //   fw_java.write("public class " + class_name + " {\n");

  //   fw_java.write("\n");

  //   fw_java.write("}\n");
  //   fw_decl_hpp.write("  };\n");

  //   fw_frwd_hpp.write("};\n");
  //   fw_decl_hpp.write("};\n");
  //   fw_impl_hpp.write("};\n");

  //   fw_java.close();
  //   fw_frwd_hpp.close();
  //   fw_decl_hpp.close();
  //   fw_impl_hpp.close();
  // }

  static void make_class(final FileWriter java_file_list, final FileWriter hpp_frwd_list, final FileWriter hpp_decl_list, final FileWriter hpp_impl_list, final FileWriter hpp_frmt_list, final ClassDesc classDesc, final ClassModel classModel) throws IOException {
    // final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');
    final String classNameJava = (classDesc.packageName() + "." + classDesc.displayName()); //classModel.thisClass().asInternalName().replace('/', '.').replace('$', '.');

    // final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    final String class_name = classNameJava.replace('.', '_');

    final String classDisplayName = classDesc.displayName();
    final String namespace = classDesc.packageName().replace(".", "::");

    final String str_fw_java = "./src/com/xilinx/rapidwright/experimental/" + class_name + ".java";
    final FileWriter fw_java = new FileWriter(str_fw_java);

    final Path str_fw_java_path = Paths.get(str_fw_java).toAbsolutePath();

    java_file_list.write(str_fw_java_path.normalize().toString() + "\n");

    final FileWriter fw_frwd_hpp = new FileWriter("./build/experimental/frwd_" + class_name + ".hpp");
    final FileWriter fw_decl_hpp = new FileWriter("./build/experimental/decl_" + class_name + ".hpp");
    final FileWriter fw_impl_hpp = new FileWriter("./build/experimental/impl_" + class_name + ".hpp");
    final FileWriter fw_frmt_hpp = new FileWriter("./build/experimental/frmt_" + class_name + ".hpp");
    hpp_frwd_list.write("#include <frwd_" + class_name + ".hpp>\n");
    hpp_decl_list.write("#include <decl_" + class_name + ".hpp>\n");
    hpp_impl_list.write("#include <impl_" + class_name + ".hpp>\n");
    hpp_frmt_list.write("#include <frmt_" + class_name + ".hpp>\n");

    fw_decl_hpp.write("#pragma once\n\n");
    fw_decl_hpp.write("#include <optional>\n");
    fw_decl_hpp.write("#include <utility>\n");
    fw_decl_hpp.write("#include <cstdlib>\n");
    fw_decl_hpp.write("\n");

    fw_impl_hpp.write("#pragma once\n\n");
    fw_impl_hpp.write("#include <optional>\n");
    fw_impl_hpp.write("#include <utility>\n");
    fw_impl_hpp.write("#include <cstdlib>\n");
    fw_impl_hpp.write("#include <" + class_name + ".h>\n");
    fw_impl_hpp.write("\n");

    // fw_frwd_hpp.write("struct OBJECT_" + class_name + ";\n");
    // fw_frwd_hpp.write("struct OBJECT_RANK_1_" + class_name + ";\n");
    // fw_frwd_hpp.write("struct OBJECT_RANK_2_" + class_name + ";\n");
    fw_frwd_hpp.write("using OBJECT_HANDLE_" + class_name + " = intptr_t;\n");
    fw_frwd_hpp.write("using OBJECT_HANDLE_RANK_1_" + class_name + " = intptr_t;\n");
    fw_frwd_hpp.write("using OBJECT_HANDLE_RANK_2_" + class_name + " = intptr_t;\n");

    final String root_namespace = "::" + classDesc.packageName().replace(".", "::") + "::";

    fw_frwd_hpp.write("namespace " + classDesc.packageName().replace(".", "::") + " {\n");
    fw_decl_hpp.write("namespace " + classDesc.packageName().replace(".", "::") + " {\n");
    fw_impl_hpp.write("namespace " + classDesc.packageName().replace(".", "::") + " {\n");

    fw_frwd_hpp.write("  struct " + classDisplayName + ";\n");
    // fw_frwd_hpp.write("  struct RANK_1_" + classDisplayName + ";\n");
    // fw_frwd_hpp.write("  struct RANK_2_" + classDisplayName + ";\n");

    decl_array_rank(fw_frwd_hpp, fw_decl_hpp, fw_impl_hpp, classDisplayName, class_name, 1);
    decl_array_rank(fw_frwd_hpp, fw_decl_hpp, fw_impl_hpp, classDisplayName, class_name, 2);

    // fw_decl_hpp.write("  struct RANK_1_" + classDisplayName + " {\n");

    // fw_decl_hpp.write("    OBJECT_HANDLE_RANK_1_" + class_name + " ObjectHandle;\n\n");

    // fw_decl_hpp.write("    /* deleted default constructor */\n");
    // fw_decl_hpp.write("    RANK_1_" + classDisplayName + "() = delete;\n\n");

    // fw_decl_hpp.write("    /* deleted copy constructor */\n");
    // fw_decl_hpp.write("    RANK_1_" + classDisplayName + "(const RANK_1_" + classDisplayName + "& other) = delete;\n\n");
    
    // fw_decl_hpp.write("    /* deleted move constructor */\n");
    // fw_decl_hpp.write("    RANK_1_" + classDisplayName + "(RANK_1_" + classDisplayName + "&& other) = delete;\n\n");

    // fw_decl_hpp.write("    /* deleted copy assignment */\n");
    // fw_decl_hpp.write("    RANK_1_" + classDisplayName + "& operator=(const RANK_1_" + classDisplayName + "& other) = delete;\n\n");

    // fw_decl_hpp.write("    /* deleted move assignment */\n");
    // fw_decl_hpp.write("    RANK_1_" + classDisplayName + "& operator=(RANK_1_" + classDisplayName + "&& other) = delete;\n\n");

    // fw_decl_hpp.write("    /* explicit handle constructor */\n");
    // fw_decl_hpp.write("    explicit RANK_1_" + classDisplayName + "(OBJECT_HANDLE_RANK_1_" + class_name + " ObjectHandle);\n\n");

    // fw_decl_hpp.write("    /* destructor */\n");
    // fw_decl_hpp.write("    ~RANK_1_" + classDisplayName + "();\n");
    // fw_decl_hpp.write("  };\n\n");

    // fw_decl_hpp.write("  struct RANK_2_" + classDisplayName + " {\n");

    // fw_decl_hpp.write("    OBJECT_HANDLE_RANK_2_" + class_name + " ObjectHandle;\n\n");

    // fw_decl_hpp.write("    /* deleted default constructor */\n");
    // fw_decl_hpp.write("    RANK_2_" + classDisplayName + "() = delete;\n\n");

    // fw_decl_hpp.write("    /* deleted copy constructor */\n");
    // fw_decl_hpp.write("    RANK_2_" + classDisplayName + "(const RANK_2_" + classDisplayName + "& other) = delete;\n\n");
    
    // fw_decl_hpp.write("    /* deleted move constructor */\n");
    // fw_decl_hpp.write("    RANK_2_" + classDisplayName + "(RANK_2_" + classDisplayName + "&& other) = delete;\n\n");

    // fw_decl_hpp.write("    /* deleted copy assignment */\n");
    // fw_decl_hpp.write("    RANK_2_" + classDisplayName + "& operator=(const RANK_2_" + classDisplayName + "& other) = delete;\n\n");

    // fw_decl_hpp.write("    /* deleted move assignment */\n");
    // fw_decl_hpp.write("    RANK_2_" + classDisplayName + "& operator=(RANK_2_" + classDisplayName + "&& other) = delete;\n\n");

    // fw_decl_hpp.write("    /* explicit handle constructor */\n");
    // fw_decl_hpp.write("    explicit RANK_2_" + classDisplayName + "(OBJECT_HANDLE_RANK_2_" + class_name + " ObjectHandle);\n\n");

    // fw_decl_hpp.write("    /* destructor */\n");
    // fw_decl_hpp.write("    ~RANK_2_" + classDisplayName + "();\n");
    // fw_decl_hpp.write("  };\n\n");


    fw_decl_hpp.write("  struct " + classDisplayName + " {\n");

    fw_decl_hpp.write("    /* deleted default constructor */\n");
    fw_decl_hpp.write("    " + classDisplayName + "() = delete;\n\n");

    fw_decl_hpp.write("    /* deleted copy constructor */\n");
    fw_decl_hpp.write("    " + classDisplayName + "(const " + classDisplayName + "& other) = delete;\n\n");
    
    fw_decl_hpp.write("    /* deleted move constructor */\n");
    fw_decl_hpp.write("    " + classDisplayName + "(" + classDisplayName + "&& other) = delete;\n\n");

    fw_decl_hpp.write("    /* deleted copy assignment */\n");
    fw_decl_hpp.write("    " + classDisplayName + "& operator=(const " + classDisplayName + "& other) = delete;\n\n");

    fw_decl_hpp.write("    /* deleted move assignment */\n");
    fw_decl_hpp.write("    " + classDisplayName + "& operator=(" + classDisplayName + "&& other) = delete;\n\n");

    fw_decl_hpp.write("    /* explicit handle constructor */\n");
    fw_decl_hpp.write("    explicit " + classDisplayName + "(OBJECT_HANDLE_" + class_name + " ObjectHandle) noexcept;\n\n");

    fw_decl_hpp.write("    /* destructor */\n");
    fw_decl_hpp.write("    ~" + classDisplayName + "() noexcept;\n\n");

    fw_impl_hpp.write("  __forceinline " + classDisplayName + "::" + classDisplayName + "(OBJECT_HANDLE_" + class_name + " ObjectHandle) noexcept: ObjectHandle{ObjectHandle} {\n");
    fw_impl_hpp.write("  }\n");

    fw_impl_hpp.write("  __forceinline " + classDisplayName + "::~" + classDisplayName + "() noexcept {\n");
    fw_impl_hpp.write("    if (ObjectHandle) {\n");
    fw_impl_hpp.write("      " + class_name + "_free_instance(localThread.get(), ObjectHandle);\n");
    fw_impl_hpp.write("    }\n");
    fw_impl_hpp.write("  }\n");

    fw_java.write("package com.xilinx.rapidwright.experimental;\n");
    fw_java.write("\n");

    fw_java.write("import org.graalvm.nativeimage.ObjectHandle;\n");
    fw_java.write("import org.graalvm.nativeimage.ObjectHandles;\n");
    fw_java.write("import org.graalvm.nativeimage.IsolateThread;\n");
    fw_java.write("import org.graalvm.nativeimage.UnmanagedMemory;\n");
    fw_java.write("import org.graalvm.nativeimage.c.function.CEntryPoint;\n");
    fw_java.write("import org.graalvm.nativeimage.c.type.CCharPointer;\n");
    fw_java.write("import org.graalvm.nativeimage.c.type.CTypeConversion;\n");
    fw_java.write("import org.graalvm.nativeimage.c.type.CTypedef;\n");
    fw_java.write("import org.graalvm.nativeimage.c.type.CConst;\n");
    fw_java.write("import org.graalvm.nativeimage.c.CHeader;\n");
    fw_java.write("import org.graalvm.word.UnsignedWord;\n");
    fw_java.write("import org.graalvm.word.WordFactory;\n");
    fw_java.write("import java.io.PrintWriter;\n");
    fw_java.write("\n");

    fw_java.write("@CHeader(" + class_name + ".class)\n");
    fw_java.write("@SuppressWarnings({\"preview\",\"deprecation\",\"removal\"})\n");
    fw_java.write("public class " + class_name + " implements CHeader.Header {\n");
    fw_java.write("  @Override\n");
    fw_java.write("  public String name() {\n");
    fw_java.write("    return \"" + class_name + "\";\n");
    fw_java.write("  }\n");
    fw_java.write("\n");

    fw_java.write("  @Override\n");
    fw_java.write("  public void writePreamble(PrintWriter writer) {\n");
    fw_java.write("    writer.println(\"/* Automatically generated by BuildInterface.java */\");\n");
    fw_java.write("  }\n");
    fw_java.write("\n");

    fw_java.write("  @CEntryPoint(name = \"" + class_name + "_free_instance\")\n");
    fw_java.write("  public static void " + class_name + "_free_instance(IsolateThread thread, @CTypedef(name = \"OBJECT_HANDLE_" + class_name + "\") final ObjectHandle handle) {\n");
    fw_java.write("    ObjectHandles.getGlobal().destroy(handle);\n");
    fw_java.write("  }\n");
    fw_java.write("\n");

    if(classModel != null) {
      final SortedSet<String> usedNames = new TreeSet<>();
      for(final FieldModel fieldModel: classModel.fields()) {
        if(fieldModel.fieldName().stringValue().length() <= 1) continue;

        final AccessFlags accessFlags = fieldModel.flags();
        if(!accessFlags.has(AccessFlag.PUBLIC)) continue;
        if(accessFlags.has(AccessFlag.PRIVATE)) continue;

        final String field_key = classNameJava + (accessFlags.has(AccessFlag.STATIC)?"::":".") + fieldModel.fieldName().stringValue();
        usedNames.add(field_key);

        write_field(classModel, fw_java, fw_decl_hpp, fw_impl_hpp, fw_frmt_hpp, root_namespace, fieldModel);
      }
  
      for(final MethodModel methodModel: classModel.methods()) {
        if(methodModel.methodName().stringValue().length() <= 1) continue;

        final AccessFlags accessFlags = methodModel.flags();

        if(!accessFlags.has(AccessFlag.PUBLIC)) continue;
        if(accessFlags.has(AccessFlag.PRIVATE)) continue;
        if(accessFlags.has(AccessFlag.SYNTHETIC)) continue;

        final String method_key = classNameJava + (accessFlags.has(AccessFlag.STATIC)?"::":".") + methodModel.methodName().stringValue();
        if(usedNames.contains(method_key)) {
          System.out.println("skipping method " + method_key);
          continue;
        }
        write_method(classModel, fw_java, fw_decl_hpp, fw_impl_hpp, methodModel);
      }
    }

    fw_java.write("}\n");
    fw_decl_hpp.write("\n");
    fw_decl_hpp.write("    OBJECT_HANDLE_" + class_name + " ObjectHandle;\n");
    fw_decl_hpp.write("  };\n");

    fw_frwd_hpp.write("};\n");
    fw_decl_hpp.write("};\n");
    fw_impl_hpp.write("};\n");

    fw_java.close();
    fw_frwd_hpp.close();
    fw_decl_hpp.close();
    fw_impl_hpp.close();
    fw_frmt_hpp.close();
  }

  static void addMissingClassFromName(final String referenceJavaClassName, final String javaClassName, final ClassDesc classDesc) throws IOException {
    if(classMap.containsKey(classDesc)) return;

    final String resourceName = "/" + javaClassName.replace(".", "/") + ".class";

    final InputStream baseInputStream = Class.class.getResourceAsStream(resourceName);
    final InputStream theadInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);

    if(baseInputStream == null && theadInputStream == null) {
      // System.out.println("nonloading: " + referenceJavaClassName + " -> " + javaClassName);
      classMap.put(classDesc, null);
      return;
    }
    if(baseInputStream != null) {
      final byte[] bytes = baseInputStream.readAllBytes();
      final ClassModel classModel = ClassFile.of().parse(bytes);
      addClassModel(classModel, false);
      return;
    }
    if(theadInputStream != null) {
      final byte[] bytes = theadInputStream.readAllBytes();
      final ClassModel classModel = ClassFile.of().parse(bytes);
      addClassModel(classModel, false);
      return;
    }
  }

  static void addMissingClassFromClassDesc(final String referenceJavaClassName, final ClassDesc classDesc) throws IOException {
    if(classDesc.isArray()) {
      final int rank = getRank(classDesc);
      final ClassDesc base = getBaseComponentType(classDesc);
      final String javaClassName = (base.packageName() + ((base.packageName().length() > 0)?".":"") + base.displayName()).replace('$', '.');
      if(base.isPrimitive()) return;
      addMissingClassFromName(referenceJavaClassName, javaClassName, base);
      return;
    }
    if(classDesc.isPrimitive()) return;

    final String javaClassName = classDesc.packageName() + "." + classDesc.displayName();
    addMissingClassFromName(referenceJavaClassName, javaClassName, classDesc);
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
    // for(final ClassModel classModel: classModels) {
    //   addMissingClassesFromClassModel(classModel);
    // }

    for(;;) {
      SortedMap<ClassDesc, ClassModel> classMapCopy = new TreeMap<>(classMap);
      for(final ClassModel classModel: classMapCopy.values()) {
        if(classModel == null) continue;
        addMissingClassesFromClassModel(classModel);
        // System.out.println("  " + missingClass);
      }
      // System.out.println("classes added: " + (classMap.size() - classMapCopy.size()));
      if(classMapCopy.size() == classMap.size()) break;
    }
    // System.out.println();
    
  }

  static void general(final FileWriter java_file_list) throws IOException {
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
    fw_java.write("import org.graalvm.nativeimage.c.type.CTypedef;\n");
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
  }

  static void decl_array_rank(final FileWriter fw_frwd_hpp, final FileWriter fw_decl_hpp, final FileWriter fw_impl_hpp, final String classDisplayName, final String class_name, final int rank) throws IOException {
    fw_frwd_hpp.write("  struct RANK_" + rank + "_" + classDisplayName + ";\n");

    fw_decl_hpp.write("  struct RANK_" + rank + "_" + classDisplayName + " {\n");

    fw_decl_hpp.write("    /* deleted default constructor */\n");
    fw_decl_hpp.write("    RANK_" + rank + "_" + classDisplayName + "() = delete;\n\n");

    fw_decl_hpp.write("    /* deleted copy constructor */\n");
    fw_decl_hpp.write("    RANK_" + rank + "_" + classDisplayName + "(const RANK_" + rank + "_" + classDisplayName + "& other) = delete;\n\n");
    
    fw_decl_hpp.write("    /* deleted move constructor */\n");
    fw_decl_hpp.write("    RANK_" + rank + "_" + classDisplayName + "(RANK_" + rank + "_" + classDisplayName + "&& other) = delete;\n\n");

    fw_decl_hpp.write("    /* deleted copy assignment */\n");
    fw_decl_hpp.write("    RANK_" + rank + "_" + classDisplayName + "& operator=(const RANK_" + rank + "_" + classDisplayName + "& other) = delete;\n\n");

    fw_decl_hpp.write("    /* deleted move assignment */\n");
    fw_decl_hpp.write("    RANK_" + rank + "_" + classDisplayName + "& operator=(RANK_" + rank + "_" + classDisplayName + "&& other) = delete;\n\n");

    fw_decl_hpp.write("    /* explicit handle constructor */\n");
    fw_decl_hpp.write("    explicit RANK_" + rank + "_" + classDisplayName + "(OBJECT_HANDLE_RANK_" + rank + "_" + class_name + " ObjectHandle);\n\n");

    fw_decl_hpp.write("    /* destructor */\n");
    fw_decl_hpp.write("    ~RANK_" + rank + "_" + classDisplayName + "();\n");
    fw_decl_hpp.write("\n");
    fw_decl_hpp.write("    OBJECT_HANDLE_RANK_" + rank + "_" + class_name + " ObjectHandle;\n");
    fw_decl_hpp.write("  };\n\n");
  }

  public static void main(final String[] args) throws IOException {
    jarPath("./jars/rapidwright-api-lib-2026.1.1.jar");
    jarPath("./build/libs/rapidwright.jar");
    addMissingClasses();

    final FileWriter java_file_list = new FileWriter("./build/java_file_list.txt");

    final FileWriter hpp_frwd_list = new FileWriter("./build/experimental/ExperimentalAPI_frwd.hpp");
    final FileWriter hpp_decl_list = new FileWriter("./build/experimental/ExperimentalAPI_decl.hpp");
    final FileWriter hpp_impl_list = new FileWriter("./build/experimental/ExperimentalAPI_impl.hpp");
    final FileWriter hpp_frmt_list = new FileWriter("./build/experimental/ExperimentalAPI_frmt.hpp");

    hpp_frwd_list.write("#pragma once\n\n");
    hpp_decl_list.write("#pragma once\n\n");
    hpp_impl_list.write("#pragma once\n\n");
    hpp_frmt_list.write("#pragma once\n\n");

    for(final String javaPrim: java_primitives) {
      for(int rank = 1; rank <= 2; rank++) {
        // hpp_frwd_list.write("  struct OBJECT_RANK_" + rank + "_" + javaPrim + ";\n");
        hpp_frwd_list.write("  using OBJECT_HANDLE_RANK_" + rank + "_" + javaPrim + " = intptr_t;\n");
        decl_array_rank(hpp_frwd_list, hpp_decl_list, hpp_impl_list, javaPrim, javaPrim, rank);
      }
    }

    // hpp_using_list.write("template<typename T>\n");
    // hpp_using_list.write("struct unique_general_destructor {\n");
    // hpp_using_list.write("  inline void operator()(T* handle) const noexcept {\n");
    // hpp_using_list.write("    if (handle) {\n");
    // hpp_using_list.write("      if constexpr (is_debug) {\n");
    // hpp_using_list.write("        std::println(std::source_location::current().function_name());\n");
    // hpp_using_list.write("      }\n");
    // hpp_using_list.write("      general_free_instance(localThread.get(), handle);\n");
    // hpp_using_list.write("    }\n");
    // hpp_using_list.write("  }\n");
    // hpp_using_list.write("};\n");
    // hpp_using_list.write("\n");

    // general(java_file_list);

    // for(final ClassModel classModel: missingClassSet) {
    //   final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    //   forward_define_struct(hpp_frwd_list, class_name);
    // }
    // for(final ClassModel classModel: classModels) {
    //   final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    //   forward_define_struct(hpp_frwd_list, class_name);
    // }
    // for(final String nonloadingClass: nonloadingClassSet) {
    //   final String class_name = nonloadingClass.replace('.', '_').replace('$', '_');
    //   forward_define_struct(hpp_frwd_list, class_name);
    // }

    // for(final ClassModel classModel: missingClassSet) {
    //   final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    //   hpp_structs_list.write("struct unique_" + class_name + "_destructor;\n");
    // }
    // for(final ClassModel classModel: classModels) {
    //   final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    //   hpp_structs_list.write("struct unique_" + class_name + "_destructor;\n");
    // }
    // for(final String nonloadingClass: nonloadingClassSet) {
    //   final String class_name = nonloadingClass.replace('.', '_');
    //   hpp_structs_list.write("struct unique_" + class_name + "_destructor;\n");
    // }

    // for(final ClassModel classModel: missingClassSet) {
    //   final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    //   hpp_using_list.write("using unique_" + class_name + " = unique_ObjectHandle<" + class_name + ", unique_general_destructor<" + class_name + ">>;\n");
    // }

    // for(final ClassModel classModel: classModels) {
    //   final String class_name = classModel.thisClass().asInternalName().replace('/', '_').replace('$', '_');
    //   hpp_using_list.write("using unique_" + class_name + " = unique_ObjectHandle<" + class_name + ", unique_general_destructor<" + class_name + ">>;\n");
    // }

    // for(final String nonloadingClass: nonloadingClassSet) {
    //   final String class_name = nonloadingClass.replace('.', '_');
    //   hpp_using_list.write("using unique_" + class_name + " = unique_ObjectHandle<" + class_name + ", unique_general_destructor<" + class_name + ">>;\n");
    // }

    for(final Entry<ClassDesc, ClassModel> entry: classMap.entrySet()) {
      make_class(java_file_list, hpp_frwd_list, hpp_decl_list, hpp_impl_list, hpp_frmt_list, entry.getKey(), entry.getValue());
    }

    // System.out.println("nonloadingClassMap:");
    // for(final ClassDesc classDesc: nonloadingClassSet) {
    //   make_class_from_ClassDesc(java_file_list, hpp_frwd_list, hpp_decl_list, hpp_impl_list, classDesc);
    //   // System.out.println("  " + nonloadingClassEntry.getKey() + ": " + nonloadingClassEntry.getValue());
    // }
    // System.out.println();

    java_file_list.close();
    hpp_frwd_list.close();
    hpp_decl_list.close();
    hpp_impl_list.close();
    hpp_frmt_list.close();

    for(final Entry<ClassDesc, Set<AccessFlag>> entry: classAccessFlagsMap.entrySet()) {
      System.out.println(entry.getKey().packageName() + "." + entry.getKey().displayName() + " " + entry.getValue());
    }
  }
}
