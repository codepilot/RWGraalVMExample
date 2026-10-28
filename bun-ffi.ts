import { CString, dlopen, FFIType, ptr, suffix, toArrayBuffer } from "bun:ffi";
import type {Pointer} from "bun:ffi";

const path = `./build/native-image/librapidwright.${suffix}`;

const {
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

type FFI_PTR = bigint | Pointer | null;

class StringHolder {
  #thread: bigint;
  #char_ptr: FFI_PTR;
  constructor(thread: bigint, objectHandle: FFI_PTR) {
    this.#thread = thread;
    this.#char_ptr = org_graalvm_nativeimage_UnmanagedMemory_malloc_String(this.#thread, objectHandle);
  }
  toString(): string {
    return CString(this.#char_ptr);
  }
  [Symbol.dispose](): void {
    org_graalvm_nativeimage_UnmanagedMemory_free(this.#thread, this.#char_ptr);
  }
};

class java_lang_Object {
  thread: bigint;
  objectHandle: FFI_PTR;
  isOwning: boolean;
  constructor(thread: bigint, objectHandle: FFI_PTR, isOwning: boolean) {
    this.thread = thread;
    this.objectHandle = objectHandle;
    this.isOwning = isOwning;
  }
  [Symbol.dispose](): void {
    if(this.isOwning) {
      org_graalvm_nativeimage_ObjectHandles_getGlobal_destroy(this.thread, this.objectHandle);
    }
  }
  toJavaString(): java_lang_String {
    return new java_lang_String(this.thread, java_lang_Object_toString(this.thread, this.objectHandle), true);
  }
  toString(): string {
    using javaString = this.toJavaString();
    return javaString.toString();
  }
  equals(obj: java_lang_Object): boolean {
    return java_lang_Object_equals(this.thread, this.objectHandle, obj.objectHandle);
  }
};

class java_lang_String extends java_lang_Object {
  createStringHolder(): StringHolder {
    return new StringHolder(this.thread, this.objectHandle);
  }
  static create(thread: bigint, str: string): java_lang_String {
    return new java_lang_String(thread, org_graalvm_nativeimage_c_type_CTypeConversion_utf8ToJavaString(thread, str), true);
  }
  override toString(): string {
    using stringHolder = this.createStringHolder();
    return stringHolder.toString();
  }
}

class JAVA_ARRAY<ElementConstructor extends typeof java_lang_Object> {
  thread: bigint;
  _element_ptr: FFI_PTR;
  _element_count: bigint;
  _element_ab: ArrayBuffer;
  _elements: BigUint64Array;
  _element_constructor: ElementConstructor;
  constructor(thread: bigint, _element_constructor: ElementConstructor, _element_ptr: FFI_PTR, _element_count: bigint) {
    this.thread = thread;
    this._element_ptr = _element_ptr;
    this._element_count = _element_count;
    if(_element_ptr === null) throw new TypeError(`_element_ptr === null`);
    this._element_ab = toArrayBuffer(_element_ptr, 0, Number(_element_count << 3n));
    this._elements = new BigUint64Array(this._element_ab);
    this._element_constructor = _element_constructor;
    console.log(this._elements);
  }
  [Symbol.dispose](): void {
    free_JAVA_ARRAY(this.thread, this._element_ptr, this._element_count);
  }
  *[Symbol.iterator](): Generator<InstanceType<ElementConstructor>, void, unknown> {
  for (const el of this._elements) {
    const inst = new this._element_constructor(this.thread, el, false);
    yield inst as InstanceType<ElementConstructor>;
  }
  }
}

class java_util_Collection<ElementConstructor extends typeof java_lang_Object> extends java_lang_Object {
  _element_constructor: ElementConstructor;
  constructor(thread: bigint, objectHandle: FFI_PTR, isOwning: boolean, _element_constructor: ElementConstructor) {
    super(thread, objectHandle, isOwning);
    this.thread = thread;
    this.objectHandle = objectHandle;
    this.isOwning = isOwning;
    this._element_constructor = _element_constructor;
  }
  createJAVA_ARRAY(): JAVA_ARRAY<ElementConstructor> {
    const _element_count_buf = new BigUint64Array(1);
    const _element_ptr = java_util_Collection_JAVA_ARRAY(this.thread, this.objectHandle, ptr(_element_count_buf));
    const _element_count = _element_count_buf[0] as bigint;
    return new JAVA_ARRAY<ElementConstructor>(this.thread, this._element_constructor, _element_ptr, _element_count);
  }
};

class com_xilinx_rapidwright_design_Design extends java_lang_Object {
  static readCheckpoint(thread: bigint, dcpPath: java_lang_String, edfPath: java_lang_String) {
    return new com_xilinx_rapidwright_design_Design(
      thread,
      sha256_a71dae503a8a49ba8ee573728aa423ae59e03e2d0ed613d59758ad1e2f090d29_com_xilinx_rapidwright_design_Design_id_15_readCheckpoint(
        thread,
        dcpPath.objectHandle,
        edfPath.objectHandle),
      true);
  }
  getCells(): java_util_Collection<typeof com_xilinx_rapidwright_design_Cell> {
    return new java_util_Collection(this.thread,
      sha256_a71dae503a8a49ba8ee573728aa423ae59e03e2d0ed613d59758ad1e2f090d29_com_xilinx_rapidwright_design_Design_id_110_getCells(this.thread, this.objectHandle),
      true,
      com_xilinx_rapidwright_design_Cell
    );
  }
  getNets(): java_util_Collection<typeof com_xilinx_rapidwright_design_Net> {
    return new java_util_Collection(this.thread,
      sha256_a71dae503a8a49ba8ee573728aa423ae59e03e2d0ed613d59758ad1e2f090d29_com_xilinx_rapidwright_design_Design_id_69_getNets(this.thread, this.objectHandle),
      true,
      com_xilinx_rapidwright_design_Net
    );
  }
};

class com_xilinx_rapidwright_design_Cell extends java_lang_Object {
  
};

class com_xilinx_rapidwright_design_Net extends java_lang_Object {
  
};

class GraalIsolate {
  static GRAAL_STATUS_SUCCESS = 0;
  #isolate: bigint;
  thread: bigint;
  constructor() {
    const isolate_thread = new BigInt64Array(2);
    if(GraalIsolate.GRAAL_STATUS_SUCCESS !== graal_create_isolate(null, ptr(isolate_thread), ptr(isolate_thread, 8))) {
      throw new Error("GRAAL_STATUS_SUCCESS !== graal_create_isolate");
    }
    
    const [isolate, thread] = isolate_thread;
    if(isolate === undefined) throw new TypeError(`isolate === undefined`);
    if(thread === undefined) throw new TypeError(`thread === undefined`);
    this.#isolate = isolate;
    this.thread = thread;
    
  }
  [Symbol.dispose](): void {
    if(GraalIsolate.GRAAL_STATUS_SUCCESS !== graal_tear_down_isolate(this.thread)) {
      throw new Error("GRAAL_STATUS_SUCCESS !== graal_tear_down_isolate");
    }
  }
  loadDevice(devName: string): void {
    loadDevice(this.thread, devName);
  }
  getTileName(devName: string, row: number, col: number): string | null {
    return getTileName(this.thread, devName, row, col);
  }
  utf8ToJavaString(str: string): java_lang_String {
    return java_lang_String.create(this.thread, str);
  }
};

using graalIsolate = new GraalIsolate();
switch(Bun.argv[2]) {
  case 'test_getDevice': {
    const devName =  String(Bun.argv[3]);
    graalIsolate.loadDevice(devName);
    console.log(graalIsolate.getTileName(devName, 100, 100));
    break;
  }
  case 'test_readCheckpoint': {
    using dcpPath = graalIsolate.utf8ToJavaString(String(Bun.argv[3]));
    using edfPath = graalIsolate.utf8ToJavaString(String(Bun.argv[4]));
    using design = com_xilinx_rapidwright_design_Design.readCheckpoint(graalIsolate.thread, dcpPath, edfPath);
    using cellCollection = design.getCells();
    using netCollection = design.getNets();
    using cells = cellCollection.createJAVA_ARRAY();
    using nets = netCollection.createJAVA_ARRAY();
    for(const cell of cells) {
      console.log(cell.toString());
    }
    for(const net of nets) {
      console.log(net.toString());
    }
    break;
  }
}
