import {
  CString,
  ptr,
  toArrayBuffer,
  type Pointer,
} from "bun:ffi";

import {
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
} from "./bun-ffi";

type FFI_PTR = bigint | Pointer | null;

export class StringHolder {
  static registry = new FinalizationRegistry((ptr: FFI_PTR)=> {
    console.log(`StringHolder:gc ${GraalIsolate.thread} ${ptr}`);
    org_graalvm_nativeimage_UnmanagedMemory_free(GraalIsolate.thread, ptr);
  });

  [Symbol.dispose](): void {
    StringHolder.registry.unregister(this);
    org_graalvm_nativeimage_UnmanagedMemory_free(GraalIsolate.thread, this.#char_ptr);
  }

  #char_ptr: FFI_PTR;
  constructor(objectHandle: FFI_PTR) {
    this.#char_ptr = org_graalvm_nativeimage_UnmanagedMemory_malloc_String(GraalIsolate.thread, objectHandle);
  }
  toString(): string {
    return CString(this.#char_ptr);
  }
};

export class java_lang_Object {
  static registry = new FinalizationRegistry((objectHandle: FFI_PTR) => {
    console.log(`java_lang_Object:gc ${GraalIsolate.thread} ${objectHandle}`);
    org_graalvm_nativeimage_ObjectHandles_getGlobal_destroy(GraalIsolate.thread, objectHandle);
  });

  [Symbol.dispose](): void {
    if(this.isOwning) {
      java_lang_Object.registry.unregister(this);
      org_graalvm_nativeimage_ObjectHandles_getGlobal_destroy(GraalIsolate.thread, this.objectHandle);
    }
  }

  objectHandle: FFI_PTR;
  isOwning: boolean;
  constructor(objectHandle: FFI_PTR, isOwning: boolean) {
    this.objectHandle = objectHandle;
    this.isOwning = isOwning;
    if(isOwning) {
      java_lang_Object.registry.register(this, this.objectHandle);
    }
  }
  toJavaString(): java_lang_String {
    return new java_lang_String(java_lang_Object_toString(GraalIsolate.thread, this.objectHandle), true);
  }
  toString(): string {
    return this.toJavaString().toString();
  }
  equals(obj: java_lang_Object): boolean {
    return java_lang_Object_equals(GraalIsolate.thread, this.objectHandle, obj.objectHandle);
  }
};

export class java_lang_String extends java_lang_Object {
  createStringHolder(): StringHolder {
    return new StringHolder(this.objectHandle);
  }
  static create(thread: bigint, str: string): java_lang_String {
    return new java_lang_String(org_graalvm_nativeimage_c_type_CTypeConversion_utf8ToJavaString(thread, str), true);
  }
  override toString(): string {
    return this.createStringHolder().toString();
  }
}

export class JAVA_ARRAY<ElementConstructor extends typeof java_lang_Object> {
  static registry = new FinalizationRegistry(({_element_ptr, _element_count}: {_element_ptr: FFI_PTR, _element_count: bigint}) => {
    console.log(`JAVA_ARRAY:gc ${GraalIsolate.thread} ${_element_ptr} ${_element_count}`);
    free_JAVA_ARRAY(GraalIsolate.thread, _element_ptr, _element_count);
  });

  [Symbol.dispose](): void {
    JAVA_ARRAY.registry.unregister(this);
    free_JAVA_ARRAY(GraalIsolate.thread, this._element_ptr, this._element_count);
  }

  _element_ptr: FFI_PTR;
  _element_count: bigint;
  _element_ab: ArrayBuffer;
  _elements: BigUint64Array;
  _element_constructor: ElementConstructor;
  constructor(_element_constructor: ElementConstructor, _element_ptr: FFI_PTR, _element_count: bigint) {
    this._element_ptr = _element_ptr;
    this._element_count = _element_count;
    if(_element_ptr === null) throw new TypeError(`_element_ptr === null`);
    this._element_ab = toArrayBuffer(_element_ptr, 0, Number(_element_count << 3n));
    this._elements = new BigUint64Array(this._element_ab);
    this._element_constructor = _element_constructor;
    JAVA_ARRAY.registry.register(this, {_element_ptr, _element_count});
  }
  *[Symbol.iterator](): Generator<InstanceType<ElementConstructor>, void, unknown> {
    for (const el of this._elements) {
      const inst = new this._element_constructor(el, false) as InstanceType<ElementConstructor>;
      yield inst;
    }
  }
  *entries(): Generator<[number, InstanceType<ElementConstructor>], void, unknown> {
    for(const [i, el] of this._elements.entries()) {
      const inst = new this._element_constructor(el, false) as InstanceType<ElementConstructor>;
      yield [i, inst];
    }
  }
}

export class java_util_Collection<ElementConstructor extends typeof java_lang_Object> extends java_lang_Object {
  _element_constructor: ElementConstructor;
  constructor(objectHandle: FFI_PTR, isOwning: boolean, _element_constructor: ElementConstructor) {
    super(objectHandle, isOwning);
    this._element_constructor = _element_constructor;
  }
  createJAVA_ARRAY(): JAVA_ARRAY<ElementConstructor> {
    const _element_count_buf = new BigUint64Array(1);
    const _element_ptr = java_util_Collection_JAVA_ARRAY(GraalIsolate.thread, this.objectHandle, ptr(_element_count_buf));
    const _element_count = _element_count_buf[0] as bigint;
    return new JAVA_ARRAY<ElementConstructor>(this._element_constructor, _element_ptr, _element_count);
  }
};

export class com_xilinx_rapidwright_design_Design extends java_lang_Object {
  static readCheckpoint(thread: bigint, dcpPath: java_lang_String, edfPath: java_lang_String) {
    return new com_xilinx_rapidwright_design_Design(
      sha256_a71dae503a8a49ba8ee573728aa423ae59e03e2d0ed613d59758ad1e2f090d29_com_xilinx_rapidwright_design_Design_id_15_readCheckpoint(
        thread,
        dcpPath.objectHandle,
        edfPath.objectHandle),
      true);
  }
  getCells(): java_util_Collection<typeof com_xilinx_rapidwright_design_Cell> {
    return new java_util_Collection(
      sha256_a71dae503a8a49ba8ee573728aa423ae59e03e2d0ed613d59758ad1e2f090d29_com_xilinx_rapidwright_design_Design_id_110_getCells(GraalIsolate.thread, this.objectHandle),
      true,
      com_xilinx_rapidwright_design_Cell
    );
  }
  getNets(): java_util_Collection<typeof com_xilinx_rapidwright_design_Net> {
    return new java_util_Collection(
      sha256_a71dae503a8a49ba8ee573728aa423ae59e03e2d0ed613d59758ad1e2f090d29_com_xilinx_rapidwright_design_Design_id_69_getNets(GraalIsolate.thread, this.objectHandle),
      true,
      com_xilinx_rapidwright_design_Net
    );
  }
};

export class com_xilinx_rapidwright_design_Cell extends java_lang_Object {
  
};

export class com_xilinx_rapidwright_design_Net extends java_lang_Object {
  
};

export class GraalIsolate {
  static thread: bigint = 0n;
  static registry = new FinalizationRegistry((thread: FFI_PTR)=> {
    console.log(`GraalIsolate:gc ${thread}`);
    if(GraalIsolate.GRAAL_STATUS_SUCCESS !== graal_tear_down_isolate(thread)) {
      throw new Error("GRAAL_STATUS_SUCCESS !== graal_tear_down_isolate");
    }
    if(GraalIsolate.thread === thread) {
      GraalIsolate.thread = 0n;
    }
  });

  [Symbol.dispose](): void {
    GraalIsolate.registry.unregister(this);
    if(GraalIsolate.GRAAL_STATUS_SUCCESS !== graal_tear_down_isolate(this.thread)) {
      throw new Error("GRAAL_STATUS_SUCCESS !== graal_tear_down_isolate");
    }
    if(GraalIsolate.thread === this.thread) {
      GraalIsolate.thread = 0n;
    }
  }

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
    GraalIsolate.thread = thread;
    GraalIsolate.registry.register(this, thread);
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
