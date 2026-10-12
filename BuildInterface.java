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
import java.lang.reflect.AccessFlag;
import java.lang.constant.ClassDesc;

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
  private static SortedSet<ClassModel> classModels = new TreeSet<ClassModel>((ClassModel s1, ClassModel s2) -> s1.thisClass().asInternalName().compareTo(s2.thisClass().asInternalName()));

  private static void jarPath(String jp) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    Path currentPath = Paths.get(jp).toAbsolutePath();
    String abs_jp = "jar:file:" + currentPath.normalize().toString() + "!/";

    URL url = new URL(abs_jp);
    JarURLConnection jarConnection = (JarURLConnection)url.openConnection();
    JarFile jarFile = jarConnection.getJarFile();
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();

      if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
        continue;
      }
      String className = entry.getName()
        .substring(0, entry.getName().length() - 6)
        .replace('/', '.');

      if(!className.startsWith("com.xilinx.rapidwright")) continue;
      if(className.startsWith("com.xilinx.rapidwright.gui")) continue;
      if(className.startsWith("com.xilinx.rapidwright.examples")) continue;
      if(className.startsWith("com.xilinx.rapidwright.placer.handplacer")) continue;
      if(className.startsWith("com.xilinx.rapidwright.design.blocks")) continue;
      if(className.startsWith("com.xilinx.rapidwright.device.browser")) continue;

      if(className.contains("$")) continue;

      try {
        InputStream is = jarFile.getInputStream(entry);
        byte[] classBytes = is.readAllBytes();
        
        ClassFile cf = ClassFile.of();
        ClassModel classModel = cf.parse(classBytes);
        if(!classModel.flags().has(AccessFlag.PUBLIC)) { continue; }
        if(classModel.flags().has(AccessFlag.PRIVATE)) { continue; }
        if(classModel.flags().has(AccessFlag.PROTECTED)) { continue; }
        classModels.add(classModel);
      } catch(IOException e) {
      }
    }
  }

  public static void main(String[] args) throws IOException {
    jarPath("./jars/rapidwright-api-lib-2026.1.1.jar");
    jarPath("./build/libs/rapidwright.jar");

    FileWriter java_file_list = new FileWriter("./build/java_file_list.txt");

    for(ClassModel classModel: classModels) {
      String className = classModel.thisClass().asInternalName().replace('/', '.');

      String str_st_java = "./src/com/xilinx/rapidwright/experimental/st_" + className.replace('.', '_') + ".java";
      FileWriter st_java = new FileWriter(str_st_java);

      Path str_st_java_path = Paths.get(str_st_java).toAbsolutePath();

      java_file_list.write(str_st_java_path.normalize().toString() + "\n");

      FileWriter st_hpp = new FileWriter("./build/experimental/st_" + className.replace('.', '_') + ".hpp");
      st_hpp.write("struct experimental_st_" + className.replace('.', '_') + ";\n");

      st_java.write("package com.xilinx.rapidwright.experimental;\n");

      st_java.write("import org.graalvm.nativeimage.IsolateThread;\n");
      st_java.write("import org.graalvm.nativeimage.UnmanagedMemory;\n");
      st_java.write("import org.graalvm.nativeimage.c.function.CEntryPoint;\n");
      st_java.write("import org.graalvm.nativeimage.c.type.CCharPointer;\n");
      st_java.write("import org.graalvm.nativeimage.c.type.CTypeConversion;\n");
      st_java.write("import org.graalvm.nativeimage.c.type.CConst;\n");
      st_java.write("import org.graalvm.word.UnsignedWord;\n");
      st_java.write("import org.graalvm.word.WordFactory;\n");
      st_java.write("import com.xilinx.rapidwright.device.Device;\n");
      st_java.write("import com.xilinx.rapidwright.device.Tile;\n");
      st_java.write("import java.util.HashMap;\n");
      st_java.write("import java.util.Map;\n");

      st_java.write("\n");

      st_java.write("public class st_" + className.replace('.', '_') + " {\n");
      st_java.write("  private static long instance_counter = 1L;\n");
      st_java.write("  private static final Map<Long, " + className + "> instance_map = new HashMap<>();\n");

      st_java.write("\n");
      st_java.write("  @CEntryPoint(name = \"st_" + className.replace('.', '_') + "_free_instance\")\n");
      st_java.write("  public static void st_" + className.replace('.', '_') + "_free_instance(IsolateThread thread, final long id) {\n");
      st_java.write("    instance_map.remove(id);\n");
      st_java.write("  }\n");

      for(FieldModel fieldModel: classModel.fields()) {
        final AccessFlags accessFlags = fieldModel.flags();
        if(!accessFlags.has(AccessFlag.PUBLIC)) continue;

        String field_name = fieldModel.fieldName().stringValue();

        if(accessFlags.has(AccessFlag.STATIC)) {
          String field_type_name = fieldModel.fieldType().stringValue();
          ClassDesc field_type_class_desc = fieldModel.fieldTypeSymbol();
          if(field_type_class_desc.isPrimitive()) {
            st_java.write("\n");
            st_java.write("  @CEntryPoint(name = \"st_" + className.replace('.', '_') + "_get_field_" + field_name + "\")\n");
            st_java.write("  public static " + field_type_class_desc.displayName() + " st_" + className.replace('.', '_') + "_get_field_" + field_name + "(IsolateThread thread) {\n");
            st_java.write("    return " + className + "." + field_name +";\n");
            st_java.write("  }\n");
          }
        }
      }

      st_java.write("}\n");

      // System.out.println("    methods:");
      // for(Method method: cls.getMethods()) {
      //   System.out.println("      " + method);
      // }

      // System.out.println("    constructors:");
      // for(Constructor<?> constructor: cls.getConstructors()) {
      //   System.out.println("      " + constructor);
      // }
      // if(cls.isEnum()) {
      //   System.out.println("    enums:");
      //   for (Object enum_obj : cls.getEnumConstants()) {
      //     System.out.println("      " + enum_obj);
      //   }
      // }
      st_java.close();
      st_hpp.close();
    }

    java_file_list.close();
  }
}
