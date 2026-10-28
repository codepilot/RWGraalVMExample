import { CString, dlopen, FFIType, ptr, suffix, toArrayBuffer } from "bun:ffi";
import type {Pointer} from "bun:ffi";

const path = `./build/native-image/librapidwright.${suffix}`;

export const {
  symbols: {
    free_JAVA_ARRAY,
    getTileName,
    graal_create_isolate,
    graal_tear_down_isolate,
    java_lang_Object_equals,
    java_lang_Object_toString,
    java_util_Collection_JAVA_ARRAY,
    loadDevice,
    org_graalvm_nativeimage_c_type_CTypeConversion_utf8ToJavaString,
    org_graalvm_nativeimage_ObjectHandles_getGlobal_destroy,
    org_graalvm_nativeimage_UnmanagedMemory_free,
    org_graalvm_nativeimage_UnmanagedMemory_malloc_String,
    sha256_a71dae503a8a49ba8ee573728aa423ae59e03e2d0ed613d59758ad1e2f090d29_com_xilinx_rapidwright_design_Design_id_110_getCells,
    sha256_a71dae503a8a49ba8ee573728aa423ae59e03e2d0ed613d59758ad1e2f090d29_com_xilinx_rapidwright_design_Design_id_15_readCheckpoint,
    sha256_a71dae503a8a49ba8ee573728aa423ae59e03e2d0ed613d59758ad1e2f090d29_com_xilinx_rapidwright_design_Design_id_69_getNets,
  },
} = dlopen(
  path,
  {
    graal_create_isolate: {
      returns: FFIType.i32, //int
      args: [
        FFIType.pointer,// graal_create_isolate_params_t* params
        FFIType.pointer,// graal_isolate_t** isolate
        FFIType.pointer,// graal_isolatethread_t** thread
      ],
    },
    graal_tear_down_isolate: {
      returns: FFIType.i32,
      args: [
        FFIType.pointer,//graal_isolatethread_t* thread
      ]
    },
    loadDevice: {
      returns: FFIType.void,
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.cstring, // char*
      ],
    },
    getTileName: {// char* getTileName();
      returns: FFIType.cstring,
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.cstring, // char*
        FFIType.i32,// int
        FFIType.i32,// int
      ],
    },
    org_graalvm_nativeimage_c_type_CTypeConversion_utf8ToJavaString: {
      returns: FFIType.pointer,
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.cstring,
      ]
    },
    org_graalvm_nativeimage_ObjectHandles_getGlobal_destroy: {
      returns: FFIType.void,
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.pointer, // object handle
      ]
    },
    org_graalvm_nativeimage_UnmanagedMemory_malloc_String: {
      returns: FFIType.pointer, // char*
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.pointer, // OBJECT_HANDLE_java_lang_String
      ]
    },
    org_graalvm_nativeimage_UnmanagedMemory_free: {
      returns: FFIType.void,
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.pointer, // void *
      ]
    },
    sha256_a71dae503a8a49ba8ee573728aa423ae59e03e2d0ed613d59758ad1e2f090d29_com_xilinx_rapidwright_design_Design_id_15_readCheckpoint: {
      returns: FFIType.pointer,
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.pointer, // OBJECT_HANDLE_java_lang_String
        FFIType.pointer, // OBJECT_HANDLE_java_lang_String
      ]
    },
    sha256_a71dae503a8a49ba8ee573728aa423ae59e03e2d0ed613d59758ad1e2f090d29_com_xilinx_rapidwright_design_Design_id_110_getCells: {
      returns: FFIType.pointer, //OBJECT_HANDLE_java_util_Collection
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.pointer, // OBJECT_HANDLE_com_xilinx_rapidwright_design_Design
      ],
    },
    sha256_a71dae503a8a49ba8ee573728aa423ae59e03e2d0ed613d59758ad1e2f090d29_com_xilinx_rapidwright_design_Design_id_69_getNets: {
      returns: FFIType.pointer, //OBJECT_HANDLE_java_util_Collection
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.pointer, // OBJECT_HANDLE_com_xilinx_rapidwright_design_Design
      ],
    },
    java_util_Collection_JAVA_ARRAY: {
      returns: FFIType.pointer, // OBJECT_HANDLE_java_lang_Object *
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.pointer, // OBJECT_HANDLE_java_util_Collection
        FFIType.pointer, // size_t *
      ]
    },
    free_JAVA_ARRAY: {
      returns: FFIType.void,
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.pointer, // OBJECT_HANDLE_java_lang_Object *
        FFIType.u64_fast, // size_t
      ]
    },
    java_lang_Object_toString: {
      returns: FFIType.pointer, // OBJECT_HANDLE_java_lang_String
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.pointer, // OBJECT_HANDLE_java_lang_Object
      ]
    },
    java_lang_Object_equals: {
      returns: FFIType.bool,
      args: [
        FFIType.pointer, // graal_isolatethread_t*
        FFIType.pointer, // OBJECT_HANDLE_java_lang_Object
        FFIType.pointer, // OBJECT_HANDLE_java_lang_Object
      ]
    }
  },
);
