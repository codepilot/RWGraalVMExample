import { dlopen, FFIType, ptr, suffix } from "bun:ffi";

const path = `./build/native-image/librapidwright.${suffix}`;

const {
  symbols: {
    graal_create_isolate,
    graal_tear_down_isolate,
    loadDevice,
    getTileName,
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
  },
);

class GraalIsolate {
  static GRAAL_STATUS_SUCCESS = 0;
  #isolate: bigint;
  #thread: bigint;
  constructor() {
    const isolate_thread = new BigInt64Array(2);
    if(GraalIsolate.GRAAL_STATUS_SUCCESS !== graal_create_isolate(null, ptr(isolate_thread), ptr(isolate_thread, 8))) {
      throw new Error("GRAAL_STATUS_SUCCESS !== graal_create_isolate");
    }
    
    const [isolate, thread] = isolate_thread;
    if(isolate === undefined) throw new TypeError(`isolate === undefined`);
    if(thread === undefined) throw new TypeError(`thread === undefined`);
    this.#isolate = isolate;
    this.#thread = thread;
    
  }
  [Symbol.dispose](): void {
    if(GraalIsolate.GRAAL_STATUS_SUCCESS !== graal_tear_down_isolate(this.#thread)) {
      throw new Error("GRAAL_STATUS_SUCCESS !== graal_tear_down_isolate");
    }
  }
  loadDevice(devName: string): void {
    loadDevice(this.#thread, devName);
  }
  getTileName(devName: string, row: number, col: number): string | null {
    return getTileName(this.#thread, devName, row, col);
  }
};

using graalIsolate = new GraalIsolate();
const devName =  Bun.argv[2] ?? 'xcvu9p';
graalIsolate.loadDevice(devName);
console.log(graalIsolate.getTileName(devName, 100, 100));
