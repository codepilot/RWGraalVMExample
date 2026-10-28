package ProxyClass;

// import com.xilinx.rapidwright.MainEntrypoint;
import com.xilinx.rapidwright.device.Device;
import com.xilinx.rapidwright.util.FileTools;
import org.graalvm.nativeimage.c.CHeader;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.struct.SizeOf;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CConst;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.nativeimage.c.type.CTypedef;
import org.graalvm.nativeimage.c.type.WordPointer;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.ObjectHandle;
import org.graalvm.nativeimage.ObjectHandles;
import org.graalvm.nativeimage.UnmanagedMemory;
import org.graalvm.word.PointerBase;
import org.graalvm.word.SignedWord;
import org.graalvm.word.UnsignedWord;
import org.graalvm.word.WordFactory;

@SuppressWarnings({"preview","deprecation","removal"})
public class FileToolsUpdateAllDataFiles {

  public final static class com_xilinx_rapidwright implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright"; }}
  public final static class com_xilinx_rapidwright_bitstream implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_bitstream"; }}
  public final static class com_xilinx_rapidwright_debug implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_debug"; }}
  public final static class com_xilinx_rapidwright_design implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_design"; }}
  public final static class com_xilinx_rapidwright_design_blocks implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_design_blocks"; }}
  public final static class com_xilinx_rapidwright_design_compare implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_design_compare"; }}
  public final static class com_xilinx_rapidwright_design_drc implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_design_drc"; }}
  public final static class com_xilinx_rapidwright_design_merge implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_design_merge"; }}
  public final static class com_xilinx_rapidwright_design_noc implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_design_noc"; }}
  public final static class com_xilinx_rapidwright_design_tools implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_design_tools"; }}
  public final static class com_xilinx_rapidwright_design_xdc implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_design_xdc"; }}
  public final static class com_xilinx_rapidwright_design_xdc_parser implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_design_xdc_parser"; }}
  public final static class com_xilinx_rapidwright_device implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_device"; }}
  public final static class com_xilinx_rapidwright_device_browser implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_device_browser"; }}
  public final static class com_xilinx_rapidwright_device_helper implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_device_helper"; }}
  public final static class com_xilinx_rapidwright_eco implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_eco"; }}
  public final static class com_xilinx_rapidwright_edif implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_edif"; }}
  public final static class com_xilinx_rapidwright_edif_compare implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_edif_compare"; }}
  public final static class com_xilinx_rapidwright_examples implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_examples"; }}
  public final static class com_xilinx_rapidwright_examples_tilebrowser implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_examples_tilebrowser"; }}
  public final static class com_xilinx_rapidwright_gui implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_gui"; }}
  public final static class com_xilinx_rapidwright_interchange implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_interchange"; }}
  public final static class com_xilinx_rapidwright_ipi implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_ipi"; }}
  public final static class com_xilinx_rapidwright_placer implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_placer"; }}
  public final static class com_xilinx_rapidwright_placer_blockplacer implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_placer_blockplacer"; }}
  public final static class com_xilinx_rapidwright_placer_handplacer implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_placer_handplacer"; }}
  public final static class com_xilinx_rapidwright_router implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_router"; }}
  public final static class com_xilinx_rapidwright_rwroute implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_rwroute"; }}
  public final static class com_xilinx_rapidwright_tests implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_tests"; }}
  public final static class com_xilinx_rapidwright_timing implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_timing"; }}
  public final static class com_xilinx_rapidwright_timing_delayestimator implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_timing_delayestimator"; }}
  public final static class com_xilinx_rapidwright_util implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_util"; }}
  public final static class com_xilinx_rapidwright_util_function implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_util_function"; }}
  public final static class com_xilinx_rapidwright_util_performance_evaluation implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_util_performance_evaluation"; }}
  public final static class com_xilinx_rapidwright_util_rwroute implements CHeader.Header { @Override public String name() { return "com_xilinx_rapidwright_util_rwroute"; }}

  public final static class org_graalvm_nativeimage_UnmanagedMemory implements CHeader.Header { @Override public String name() { return "org_graalvm_nativeimage_UnmanagedMemory"; }}
  public final static class org_graalvm_nativeimage_ObjectHandles implements CHeader.Header { @Override public String name() { return "org_graalvm_nativeimage_ObjectHandles"; }}
  public final static class org_graalvm_nativeimage_c_type_CTypeConversion implements CHeader.Header { @Override public String name() { return "org_graalvm_nativeimage_c_type_CTypeConversion"; }}

  @CHeader(org_graalvm_nativeimage_ObjectHandles.class)
  @CEntryPoint(name = "org_graalvm_nativeimage_ObjectHandles_getGlobal_destroy", include = CEntryPoint.AlwaysIncluded.class)
  public static void org_graalvm_nativeimage_ObjectHandles_getGlobal_destroy(final IsolateThread thread, final ObjectHandle objectHandle) {
    ObjectHandles.getGlobal().destroy(objectHandle);
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "java_lang_Object_toString", include = CEntryPoint.AlwaysIncluded.class)
  @CTypedef(name = "OBJECT_HANDLE_java_lang_String")
  public static ObjectHandle java_lang_Object_toString(final IsolateThread thread, @CTypedef(name = "OBJECT_HANDLE_java_lang_Object") final ObjectHandle ohThis) {
    final ObjectHandles globalHandles = ObjectHandles.getGlobal();
    final Object oThis = globalHandles.get(ohThis);
    return globalHandles.create(oThis.toString());
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "java_lang_Object_equals", include = CEntryPoint.AlwaysIncluded.class)
  public static boolean java_lang_Object_equals(final IsolateThread thread, @CTypedef(name = "OBJECT_HANDLE_java_lang_Object") final ObjectHandle ohThis, @CTypedef(name = "OBJECT_HANDLE_java_lang_Object") final ObjectHandle ohObj) {
    final ObjectHandles globalHandles = ObjectHandles.getGlobal();
    final Object oThis = globalHandles.get(ohThis);
    final Object oObj = globalHandles.get(ohObj);
    return oThis.equals(oObj);
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "org_graalvm_nativeimage_c_type_CTypeConversion_utf8ToJavaString", include = CEntryPoint.AlwaysIncluded.class)
  @CTypedef(name = "OBJECT_HANDLE_java_lang_String")
  public static ObjectHandle org_graalvm_nativeimage_c_type_CTypeConversion_utf8ToJavaString(final IsolateThread thread, @CConst final CCharPointer utf8String) {
    return ObjectHandles.getGlobal().create(CTypeConversion.utf8ToJavaString(utf8String));
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "new_rank_1_java_lang_Object", include = CEntryPoint.AlwaysIncluded.class)
  @CTypedef(name = "OBJECT_HANDLE_RANK_1_java_lang_Object")
  public static ObjectHandle new_rank_1_java_lang_Object(final IsolateThread thread, int dim1) {
    return ObjectHandles.getGlobal().create(new java.lang.Object[dim1]);
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "new_rank_1_java_lang_String", include = CEntryPoint.AlwaysIncluded.class)
  @CTypedef(name = "OBJECT_HANDLE_RANK_1_java_lang_String")
  public static ObjectHandle new_rank_1_java_lang_String(final IsolateThread thread, int dim1) {
    return ObjectHandles.getGlobal().create(new java.lang.String[dim1]);
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "new_rank_2_java_lang_String", include = CEntryPoint.AlwaysIncluded.class)
  @CTypedef(name = "OBJECT_HANDLE_RANK_2_java_lang_String")
  public static ObjectHandle new_rank_2_java_lang_String(final IsolateThread thread, int dim1, int dim2) {
    return ObjectHandles.getGlobal().create(new java.lang.String[dim1][dim2]);
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "length_rank_1_java_lang_Object", include = CEntryPoint.AlwaysIncluded.class)
  public static int length_rank_1_java_lang_Object(final IsolateThread thread, @CTypedef(name = "OBJECT_HANDLE_RANK_1_java_lang_Object") final ObjectHandle ohArr) {
    final java.lang.Object[] arr = ObjectHandles.getGlobal().get(ohArr);
    return arr.length;
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "length_rank_1_java_lang_String", include = CEntryPoint.AlwaysIncluded.class)
  public static int length_rank_1_java_lang_String(final IsolateThread thread, @CTypedef(name = "OBJECT_HANDLE_RANK_1_java_lang_String") final ObjectHandle ohArr) {
    final java.lang.String[] arr = ObjectHandles.getGlobal().get(ohArr);
    return arr.length;
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "length_rank_2_java_lang_String", include = CEntryPoint.AlwaysIncluded.class)
  public static int length_rank_2_java_lang_String(final IsolateThread thread, @CTypedef(name = "OBJECT_HANDLE_RANK_2_java_lang_String") final ObjectHandle ohArr) {
    final java.lang.String[][] arr = ObjectHandles.getGlobal().get(ohArr);
    return arr.length;
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "java_util_Collection_JAVA_ARRAY", include = CEntryPoint.AlwaysIncluded.class)
  @CTypedef(name = "OBJECT_HANDLE_java_lang_Object *")
  public static WordPointer java_util_Collection_JAVA_ARRAY(
    final IsolateThread thread,
    @CTypedef(name = "OBJECT_HANDLE_java_util_Collection") final ObjectHandle ohCollection,
    @CTypedef(name = "size_t *") final WordPointer sizePtr
  ) {

    final ObjectHandles globalHandles = ObjectHandles.getGlobal();
    final java.util.Collection col = globalHandles.get(ohCollection);
    final UnsignedWord elementCount = WordFactory.unsigned(col.size());
    sizePtr.write(elementCount);

    final UnsignedWord elementByteCount = SizeOf.unsigned(WordPointer.class); 
    final UnsignedWord arrayByteCount = elementCount.multiply(elementByteCount);
    final WordPointer buffer = UnmanagedMemory.malloc(arrayByteCount);

    SignedWord index = WordFactory.signed(0);
    for(final Object item: col) {
      final SignedWord current_index = index; index = index.add(1);
      final ObjectHandle itemObjectHandle = globalHandles.create(item);
      buffer.write(current_index, itemObjectHandle);
    }

    return buffer;
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "free_JAVA_ARRAY", include = CEntryPoint.AlwaysIncluded.class)
  public static void free_JAVA_ARRAY(
    final IsolateThread thread,
    @CTypedef(name = "OBJECT_HANDLE_java_lang_Object *") final WordPointer javaArray,
    final UnsignedWord elementCount
  ) {

    final ObjectHandles globalHandles = ObjectHandles.getGlobal();
    final long elementCountLong = elementCount.rawValue();
    for(long index = 0; index < elementCountLong; index++) {
      final ObjectHandle itemObjectHandle = javaArray.read(WordFactory.signed(index));
      globalHandles.destroy(itemObjectHandle);
    }

    UnmanagedMemory.free(javaArray);
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "java_util_Collection_size", include = CEntryPoint.AlwaysIncluded.class)
  public static int java_util_Collection_size(final IsolateThread thread, @CTypedef(name = "OBJECT_HANDLE_java_util_Collection") final ObjectHandle ohCollection) {
    final ObjectHandles globalHandles = ObjectHandles.getGlobal();
    final java.util.Collection col = globalHandles.get(ohCollection);
    return col.size();
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "get_rank_1_java_lang_Object", include = CEntryPoint.AlwaysIncluded.class)
  @CTypedef(name = "OBJECT_HANDLE_java_lang_Object")
  public static ObjectHandle get_rank_1_java_lang_Object(final IsolateThread thread, @CTypedef(name = "OBJECT_HANDLE_RANK_1_java_lang_Object") final ObjectHandle ohArr, final int index) {
    final java.lang.Object[] arr = ObjectHandles.getGlobal().get(ohArr);
    return ObjectHandles.getGlobal().create(arr[index]);
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "get_rank_1_java_lang_String", include = CEntryPoint.AlwaysIncluded.class)
  @CTypedef(name = "OBJECT_HANDLE_java_lang_String")
  public static ObjectHandle get_rank_1_java_lang_String(final IsolateThread thread, @CTypedef(name = "OBJECT_HANDLE_RANK_1_java_lang_String") final ObjectHandle ohArr, final int index) {
    final java.lang.String[] arr = ObjectHandles.getGlobal().get(ohArr);
    return ObjectHandles.getGlobal().create(arr[index]);
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "set_rank_1_java_lang_Object", include = CEntryPoint.AlwaysIncluded.class)
  public static void set_rank_1_java_lang_Object(final IsolateThread thread, @CTypedef(name = "OBJECT_HANDLE_RANK_1_java_lang_Object") final ObjectHandle ohArr, final int index, @CTypedef(name = "OBJECT_HANDLE_java_lang_Object") final ObjectHandle ohVal) {
    final ObjectHandles globalHandles = ObjectHandles.getGlobal();
    final java.lang.Object[] arr = globalHandles.get(ohArr);
    final java.lang.Object val = globalHandles.get(ohVal);
    arr[index] = val;
  }

  @CHeader(org_graalvm_nativeimage_c_type_CTypeConversion.class)
  @CEntryPoint(name = "set_rank_1_java_lang_String", include = CEntryPoint.AlwaysIncluded.class)
  public static void set_rank_1_java_lang_String(final IsolateThread thread, @CTypedef(name = "OBJECT_HANDLE_RANK_1_java_lang_String") final ObjectHandle ohArr, final int index, @CTypedef(name = "OBJECT_HANDLE_java_lang_String") final ObjectHandle ohVal) {
    final ObjectHandles globalHandles = ObjectHandles.getGlobal();
    final java.lang.String[] arr = globalHandles.get(ohArr);
    final java.lang.String val = globalHandles.get(ohVal);
    arr[index] = val;
  }

  @CHeader(org_graalvm_nativeimage_UnmanagedMemory.class)
  @CEntryPoint(name = "org_graalvm_nativeimage_UnmanagedMemory_malloc_String", include = CEntryPoint.AlwaysIncluded.class)
  public static CCharPointer org_graalvm_nativeimage_UnmanagedMemory_malloc_String(final IsolateThread thread, @CTypedef(name = "OBJECT_HANDLE_java_lang_String") final ObjectHandle ohString) {
    final String str = ObjectHandles.getGlobal().get(ohString);
    final byte[] bytes = str.getBytes();
    final UnsignedWord size = WordFactory.unsigned(bytes.length + 1);
    final CCharPointer buffer = UnmanagedMemory.malloc(size);
    CTypeConversion.toCString(str, buffer, size);
    return buffer;
  }

  @CHeader(org_graalvm_nativeimage_UnmanagedMemory.class)
  @CEntryPoint(name = "org_graalvm_nativeimage_UnmanagedMemory_malloc", include = CEntryPoint.AlwaysIncluded.class)
  public static PointerBase org_graalvm_nativeimage_UnmanagedMemory_malloc(final IsolateThread thread, final UnsignedWord size) {
    return UnmanagedMemory.malloc(size);
  }

  @CHeader(org_graalvm_nativeimage_UnmanagedMemory.class)
  @CEntryPoint(name = "org_graalvm_nativeimage_UnmanagedMemory_calloc", include = CEntryPoint.AlwaysIncluded.class)
  public static PointerBase org_graalvm_nativeimage_UnmanagedMemory_calloc(final IsolateThread thread, final UnsignedWord size) {
    return UnmanagedMemory.calloc(size);
  }

  @CHeader(org_graalvm_nativeimage_UnmanagedMemory.class)
  @CEntryPoint(name = "org_graalvm_nativeimage_UnmanagedMemory_realloc", include = CEntryPoint.AlwaysIncluded.class)
  public static PointerBase org_graalvm_nativeimage_UnmanagedMemory_realloc(final IsolateThread thread, final PointerBase ptr, final UnsignedWord size) {
    return UnmanagedMemory.realloc(ptr, size);
  }

  @CHeader(org_graalvm_nativeimage_UnmanagedMemory.class)
  @CEntryPoint(name = "org_graalvm_nativeimage_UnmanagedMemory_free", include = CEntryPoint.AlwaysIncluded.class)
  public static void org_graalvm_nativeimage_UnmanagedMemory_free(final IsolateThread thread, final PointerBase ptr) {
    UnmanagedMemory.free(ptr);
  }

  public static void main(String[] args) throws Throwable {
    FileTools.updateAllDataFiles();
    final String devName = "xcvu9p";
    final int row = 1;
    final int column = 1;
    final Device device = Device.getDevice(devName);
    final String tileName = device.getTile(row, column).getName();
    System.out.println("tileName: " + tileName);

    // System.out.println("trying MainEntrypoint.main --list-apps");
    // MainEntrypoint.main(new String[]{"--list-apps"});

    // System.out.println("trying MainEntrypoint.main AddSubGenerator");
    // MainEntrypoint.main(new String[]{"AddSubGenerator"});

    // System.out.println("FileToolsUpdateAllDataFiles completed");
  }
}
