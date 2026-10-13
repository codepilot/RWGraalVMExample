package com.xilinx.rapidwright.examples;

import org.graalvm.nativeimage.ObjectHandle;
import org.graalvm.nativeimage.ObjectHandles;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.UnmanagedMemory;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.nativeimage.c.type.CConst;
import org.graalvm.word.UnsignedWord;
import org.graalvm.word.WordFactory;

import com.xilinx.rapidwright.device.Device;
import com.xilinx.rapidwright.device.Tile;
import java.util.HashMap;
import java.util.Map;

public class ExperimentalAPI_Device {

  @CEntryPoint(name = "Device_getDevice")
  public static ObjectHandle Device_getDevice(IsolateThread thread, final @CConst CCharPointer deviceName) {
    final String devName = CTypeConversion.toJavaString(deviceName);
    final Device device = Device.getDevice(devName);
    final ObjectHandle handle = ObjectHandles.getGlobal().create(device);
    return handle;
  }

  @CEntryPoint(name = "Device_freeId")
  public static void Device_freeId(IsolateThread thread, final ObjectHandle handle_this) {
    ObjectHandles.getGlobal().destroy(handle_this);
  }

  @CEntryPoint(name = "Device_getTile")
  public static ObjectHandle Device_getTile(IsolateThread thread, final ObjectHandle handle_this, final int row, final int column) {
    final Device device = ObjectHandles.getGlobal().get(handle_this);
    final Tile tile = device.getTile(row, column);
    final ObjectHandle handle = ObjectHandles.getGlobal().create(tile);
    return handle;
  }

  @CEntryPoint(name = "Tile_freeId")
  public static void Tile_freeId(IsolateThread thread, final ObjectHandle handle_this) {
    ObjectHandles.getGlobal().destroy(handle_this);
  }

  @CEntryPoint(name = "Tile_getName")
  public static CCharPointer Tile_getName(IsolateThread thread, final ObjectHandle handle_this) {
    final Tile tile = ObjectHandles.getGlobal().get(handle_this);
    final String name = tile.getName();
    final byte[] bytes = name.getBytes();
    final UnsignedWord size = WordFactory.unsigned(bytes.length + 1);
    final CCharPointer buffer = UnmanagedMemory.malloc(size);
    CTypeConversion.toCString(name, buffer, size);
    return buffer;
  }

  @CEntryPoint(name = "freeString")
  public static void freeString(IsolateThread thread, CCharPointer pointer) {
    if (pointer.isNonNull()) {
      UnmanagedMemory.free(pointer);
    }
  }
}
