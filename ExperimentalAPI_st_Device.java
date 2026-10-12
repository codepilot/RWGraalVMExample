package com.xilinx.rapidwright.examples;

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

public class ExperimentalAPI_st_Device {
  private static long device_id_counter = 1L;
  private static long tile_id_counter = 1L;
  private static final Map<Long, Device> device_map = new HashMap<Long, Device>();
  private static final Map<Long, Tile> tile_map = new HashMap<Long, Tile>();

  @CEntryPoint(name = "st_Device_getDevice")
  public static long Device_getDevice(IsolateThread thread, final @CConst CCharPointer deviceName) {
    final String devName = CTypeConversion.toJavaString(deviceName);
    final Device device = Device.getDevice(devName);
    final long id = device_id_counter++;
    device_map.put(id, device);
    return id;
  }

  @CEntryPoint(name = "st_Device_freeId")
  public static void Device_freeId(IsolateThread thread, final long id) {
    device_map.remove(id);
  }

  @CEntryPoint(name = "st_Device_getTile")
  public static long Device_getTile(IsolateThread thread, final long deviceId, final int row, final int column) {
    final Device device = device_map.get(deviceId);
    final Tile tile = device.getTile(row, column);
    final long id = tile_id_counter++;
    tile_map.put(id, tile);
    return id;
  }

  @CEntryPoint(name = "st_Tile_freeId")
  public static void Tile_freeId(IsolateThread thread, final long id) {
    tile_map.remove(id);
  }

  @CEntryPoint(name = "st_Tile_getName")
  public static CCharPointer Tile_getName(IsolateThread thread, final long tileId) {
    final Tile tile = tile_map.get(tileId);
    final String name = tile.getName();
    final byte[] bytes = name.getBytes();
    final UnsignedWord size = WordFactory.unsigned(bytes.length + 1);
    final CCharPointer buffer = UnmanagedMemory.malloc(size);
    CTypeConversion.toCString(name, buffer, size);
    return buffer;
  }

  @CEntryPoint(name = "st_freeString")
  public static void freeString(IsolateThread thread, CCharPointer pointer) {
    if (pointer.isNonNull()) {
      UnmanagedMemory.free(pointer);
    }
  }
}
