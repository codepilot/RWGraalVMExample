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
import java.lang.classfile.CodeElement;
import java.lang.classfile.MethodModel;
import java.lang.classfile.FieldModel;
import java.lang.classfile.AccessFlags;
import java.lang.classfile.Attribute;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.attribute.ExceptionsAttribute;
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
import java.util.Iterator;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.lang.Package;

import java.nio.file.Paths;
import java.nio.file.Path;

public class BuildInterface {
  private final static SortedSet<ClassModel> classModels = new TreeSet<ClassModel>((ClassModel s1, ClassModel s2) -> s1.thisClass().asInternalName().compareTo(s2.thisClass().asInternalName()));

  private static void jarPath(final String jp) throws IOException {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

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
      final String classNameJava = entry.getName()
        .substring(0, entry.getName().length() - 6)
        .replace('/', '.');

      if(!classNameJava.startsWith("com.xilinx.rapidwright.")) continue;
      if(classNameJava.startsWith("com.xilinx.rapidwright.gui.")) continue;
      if(classNameJava.startsWith("com.xilinx.rapidwright.examples.")) continue;
      if(classNameJava.startsWith("com.xilinx.rapidwright.placer.handplacer.")) continue;
      if(classNameJava.startsWith("com.xilinx.rapidwright.design.blocks.")) continue;
      if(classNameJava.startsWith("com.xilinx.rapidwright.device.browser.")) continue;
      if(classNameJava.startsWith("com.xilinx.rapidwright.debug")) continue;
      if(classNameJava.equals("com.xilinx.rapidwright.util.a")) continue;

      if(classNameJava.contains("$")) continue;

      try {
        final InputStream is = jarFile.getInputStream(entry);
        final byte[] classBytes = is.readAllBytes();
        
        final ClassFile cf = ClassFile.of();
        final ClassModel classModel = cf.parse(classBytes);
        if(!classModel.flags().has(AccessFlag.PUBLIC)) { continue; }
        if(classModel.flags().has(AccessFlag.PRIVATE)) { continue; }
        if(classModel.flags().has(AccessFlag.PROTECTED)) { continue; }
        if(classModel.flags().has(AccessFlag.ABSTRACT)) { continue; }
        classModels.add(classModel);
      } catch(IOException e) {
      }
    }
  }

  public static void method_throws(final FileWriter fw_java, final String class_name, final MethodModel methodModel) throws IOException {
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

  public static void write_field_static_primitive(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final FieldModel fieldModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_');
    final String field_name = fieldModel.fieldName().stringValue();
    final ClassDesc field_type_class_desc = fieldModel.fieldTypeSymbol();
    final AccessFlags accessFlags = fieldModel.flags();

    fw_java.write("\n");
    fw_java.write("  @CEntryPoint(name = \"" + class_name + "_get_static_field_" + field_name + "\")\n");
    fw_java.write("  public static " + field_type_class_desc.displayName() + " " + class_name + "_get_static_field_" + field_name + "(IsolateThread thread) {\n");
    fw_java.write("    return " + classNameJava + "." + field_name +";\n");
    fw_java.write("  }\n");

    if(accessFlags.has(AccessFlag.FINAL)) {
      fw_hpp.write("    inline static const auto " + field_name + "{" + class_name + "_get_static_field_" + field_name + "(localThread.get())};\n");
    } else {
      fw_hpp.write("\n");
      fw_hpp.write("    __forceinline static auto " + field_name + "() noexcept {\n");
      fw_hpp.write("      return " + class_name + "_get_static_field_" + field_name + "(localThread.get());\n");
      fw_hpp.write("    }\n");
    }
  }

  public static void write_field_static_object(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final FieldModel fieldModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_');
    final String field_name = fieldModel.fieldName().stringValue();
    final ClassDesc field_type_class_desc = fieldModel.fieldTypeSymbol();
    final AccessFlags accessFlags = fieldModel.flags();

    fw_java.write("\n");
    fw_java.write("  @CEntryPoint(name = \"" + class_name + "_get_static_field_" + field_name + "\")\n");
    fw_java.write("  public static ObjectHandle " + class_name + "_get_static_field_" + field_name + "(IsolateThread thread) {\n");
    fw_java.write("    final ObjectHandle handle = ObjectHandles.getGlobal().create(" + classNameJava + "." + field_name +");\n");
    fw_java.write("    return handle;\n");
    fw_java.write("  }\n");

    final String java_class_name = field_type_class_desc.packageName() + "." + field_type_class_desc.displayName();
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
      fw_hpp.write("      return unique_" + cpp_class_name + "(std::bit_cast<" + cpp_class_name + " *>(" + class_name + "_get_static_field_" + field_name + "(localThread.get())));\n");
      fw_hpp.write("    }\n");
      fw_hpp.write("    inline static const unique_" + cpp_class_name + " " + field_name + "{STATIC_FINAL_" + field_name + "()};\n");
    } else {
      fw_hpp.write("\n");
      fw_hpp.write("    __forceinline static unique_" + cpp_class_name + " " + field_name + "() noexcept {\n");
      fw_hpp.write("      if constexpr (is_debug) {\n");
      fw_hpp.write("        std::println(\"unique_" + cpp_class_name + "\");\n");
      fw_hpp.write("      }\n");
      fw_hpp.write("      return unique_" + cpp_class_name + "(std::bit_cast<" + cpp_class_name + " *>(" + class_name + "_get_static_field_" + field_name + "(localThread.get())));\n");
      fw_hpp.write("    }\n");
    }
  }

  public static void write_field_member_primitive(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final FieldModel fieldModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_');
    final String field_name = fieldModel.fieldName().stringValue();
    final ClassDesc field_type_class_desc = fieldModel.fieldTypeSymbol();
    final AccessFlags accessFlags = fieldModel.flags();

    fw_java.write("\n");
    fw_java.write("  @CEntryPoint(name = \"" + class_name + "_get_field_" + field_name + "\")\n");
    fw_java.write("  public static " + field_type_class_desc.displayName() + " " + class_name + "_get_field_" + field_name + "(IsolateThread thread, final ObjectHandle instance_handle) {\n");
    fw_java.write("    final " + classNameJava + " instance = ObjectHandles.getGlobal().get(instance_handle);\n");
    fw_java.write("    return instance." + field_name +";\n");
    fw_java.write("  }\n");

    fw_hpp.write("    __forceinline auto " + field_name + "() noexcept {\n");
    fw_hpp.write("      return " + class_name + "_get_field_" + field_name + "(localThread.get(), this);\n");
    fw_hpp.write("    }\n");
  }

  public static void write_field_member_object(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final FieldModel fieldModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_');
    final String field_name = fieldModel.fieldName().stringValue();
    final ClassDesc field_type_class_desc = fieldModel.fieldTypeSymbol();
    final AccessFlags accessFlags = fieldModel.flags();

    fw_java.write("\n");
    fw_java.write("  @CEntryPoint(name = \"" + class_name + "_get_field_" + field_name + "\")\n");
    fw_java.write("  public static ObjectHandle " + class_name + "_get_field_" + field_name + "(IsolateThread thread, final ObjectHandle instance_handle) {\n");
    fw_java.write("    final " + classNameJava + " instance = ObjectHandles.getGlobal().get(instance_handle);\n");
    fw_java.write("    final ObjectHandle handle = ObjectHandles.getGlobal().create(instance." + field_name +");\n");
    fw_java.write("    return handle;\n");

    fw_java.write("  }\n");

    final String java_class_name = field_type_class_desc.packageName() + "." + field_type_class_desc.displayName();
    final String cpp_class_name = java_class_name.replace(".", "_");
    if(cpp_class_name.startsWith("java_")) return;
    if(cpp_class_name.contains("$")) return;

    fw_hpp.write("    __forceinline unique_" + cpp_class_name + " " + field_name + "() noexcept {\n");
      fw_hpp.write("      return unique_" + cpp_class_name + "(std::bit_cast<" + cpp_class_name + " *>(" + class_name + "_get_field_" + field_name + "(localThread.get(), this)));\n");
    fw_hpp.write("    }\n");
  }

  public static void write_field(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final FieldModel fieldModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_');

    final AccessFlags accessFlags = fieldModel.flags();
    if(!accessFlags.has(AccessFlag.PUBLIC)) return;
    if(accessFlags.has(AccessFlag.PRIVATE)) return;

    final String field_name = fieldModel.fieldName().stringValue();

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

  public static void write_method(final ClassModel classModel, final FileWriter fw_java, final FileWriter fw_hpp, final MethodModel methodModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_');

    final String methodModel_name = methodModel.methodName().stringValue();
    if(methodModel_name.length() <= 1) return;

    final MethodTypeDesc methodTypeDesc = methodModel.methodTypeSymbol();
    final ClassDesc returnTypeDesc = methodTypeDesc.returnType();
    final AccessFlags accessFlags = methodModel.flags();
    if(!accessFlags.has(AccessFlag.PUBLIC)) return;
    if(accessFlags.has(AccessFlag.PRIVATE)) return;
    if(!returnTypeDesc.isPrimitive()) return;
    if(methodTypeDesc.parameterCount() != 0) {
      return;
    }

    if(accessFlags.has(AccessFlag.STATIC)) {
        fw_java.write("  @CEntryPoint(name = \"" + class_name + "_" + methodModel_name + "\") /* 0x" + Integer.toHexString(accessFlags.flagsMask()) + " */\n");
        fw_java.write("  public static " + returnTypeDesc.displayName() + " " + methodModel_name + "(IsolateThread thread)");
        method_throws(fw_java, class_name, methodModel);
        fw_java.write(" {\n");
        if(returnTypeDesc.displayName().equals("void")) {
          fw_java.write("    " + classNameJava + "." + methodModel_name + "();\n");
        } else {
          fw_java.write("    return " + classNameJava + "." + methodModel_name + "();\n");
        }
        fw_java.write("  }\n");
    } else {
      if(methodModel_name.equals("<init>")) {
        fw_java.write("\n");
        fw_java.write("  @CEntryPoint(name = \"" + class_name + "_init\") /* 0x" + Integer.toHexString(accessFlags.flagsMask()) + " */\n");
        fw_java.write("  public static ObjectHandle " + class_name + "_init(IsolateThread thread)");
        method_throws(fw_java, class_name, methodModel);
        fw_java.write(" {\n");
        fw_java.write("    final " + classNameJava + " instance = new " + classNameJava + "();\n");
        fw_java.write("    final ObjectHandle handle = ObjectHandles.getGlobal().create(instance);\n");
        fw_java.write("    return handle;\n");
        fw_java.write("  }\n");

        fw_hpp.write("    __forceinline static unique_" + class_name + " init() noexcept {\n");
        fw_hpp.write("      return unique_" + class_name + "(std::bit_cast<" + class_name + " *>(" + class_name + "_init(localThread.get())) );\n");
        fw_hpp.write("    }\n");
        fw_hpp.write("\n");

      } else {
        fw_java.write("  @CEntryPoint(name = \"" + class_name + "_" + methodModel_name + "\") /* 0x" + Integer.toHexString(accessFlags.flagsMask()) + " */\n");
        fw_java.write("  public static " + returnTypeDesc.displayName() + " " + methodModel_name + "(IsolateThread thread, final ObjectHandle handle)");
        method_throws(fw_java, class_name, methodModel);
        fw_java.write(" {\n");
        fw_java.write("    final " + classNameJava + " instance = ObjectHandles.getGlobal().get(handle);\n");
        if(returnTypeDesc.displayName().equals("void")) {
          fw_java.write("    instance." + methodModel_name + "();\n");
        } else {
          fw_java.write("    return instance." + methodModel_name + "();\n");
        }
        fw_java.write("  }\n");
      }

      // // System.out.println("      " + class_name + "." + methodModel_name + " " + methodTypeDesc.displayDescriptor());
      // for(ClassDesc param: methodTypeDesc.parameterArray()) {
      //   if(param.isPrimitive()) {
      //     // System.out.println("        " + param.displayName());
      //   }
      //   if(param.isClassOrInterface()) {
      //     // System.out.println("        " + param.packageName() + "." + param.displayName());
      //   }
      //   if(param.isArray()) {
      //     // System.out.println("        " + param.displayName());
      //   }
      // }
    }
  }

  public static void make_class_files(final FileWriter java_file_list, final FileWriter hpp_file_list, final ClassModel classModel) throws IOException {
    final String classNameJava = classModel.thisClass().asInternalName().replace('/', '.');
    final String class_name = classModel.thisClass().asInternalName().replace('/', '_');

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

    fw_java.write("public class " + class_name + " {\n");
    fw_java.write("\n");

    fw_java.write("  @CEntryPoint(name = \"" + class_name + "_free_instance\")\n");
    fw_java.write("  public static void " + class_name + "_free_instance(IsolateThread thread, final ObjectHandle handle) {\n");
    fw_java.write("    ObjectHandles.getGlobal().destroy(handle);\n");
    fw_java.write("  }\n");

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

  public static void main(final String[] args) throws IOException {
    jarPath("./jars/rapidwright-api-lib-2026.1.1.jar");
    jarPath("./build/libs/rapidwright.jar");

    final FileWriter java_file_list = new FileWriter("./build/java_file_list.txt");
    final FileWriter hpp_file_list = new FileWriter("./build/experimental/ExperimentalAPI.hpp");
    hpp_file_list.write("#pragma once\n\n");

    hpp_file_list.write("namespace GraalVM_Native_Image {\n");

    for(final ClassModel classModel: classModels) {
      final String class_name = classModel.thisClass().asInternalName().replace('/', '_');
      hpp_file_list.write("  struct " + class_name + ";\n");
    }

    for(final ClassModel classModel: classModels) {
      final String class_name = classModel.thisClass().asInternalName().replace('/', '_');
      hpp_file_list.write("  struct unique_" + class_name + "_destructor {\n");
      hpp_file_list.write("    inline void operator()(" + class_name + "* handle) const noexcept {\n");
      hpp_file_list.write("      if (handle) {\n");
      hpp_file_list.write("        if constexpr (is_debug) {\n");
      hpp_file_list.write("          std::println(\"unique_" + class_name + "_destructor\");\n");
      hpp_file_list.write("        }\n");
      hpp_file_list.write("        " + class_name + "_free_instance(localThread.get(), handle);\n");
      hpp_file_list.write("      }\n");
      hpp_file_list.write("    }\n");
      hpp_file_list.write("  };\n");
      hpp_file_list.write("\n");
    }

    for(final ClassModel classModel: classModels) {
      final String class_name = classModel.thisClass().asInternalName().replace('/', '_');
      hpp_file_list.write("  using unique_" + class_name + " = std::unique_ptr<" + class_name + ", unique_" + class_name + "_destructor>;\n");
    }

    hpp_file_list.write("};\n\n");

    for(final ClassModel classModel: classModels) {
      make_class_files(java_file_list, hpp_file_list, classModel);
    }

    java_file_list.close();
    hpp_file_list.close();
  }
}
