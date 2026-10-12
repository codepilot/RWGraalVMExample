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

public class ExperimentalAPI_Device {
  private static final ThreadLocal<Long> device_id_counter = new ThreadLocal<Long>() { @Override protected Long initialValue() { return 1L; } };
  private static final ThreadLocal<Long> tile_id_counter = new ThreadLocal<Long>() { @Override protected Long initialValue() { return 1L; } };
  private static final ThreadLocal<Map<Long, Device>> device_map = new ThreadLocal<Map<Long, Device>>() { @Override protected Map<Long, Device> initialValue() { return new HashMap<Long, Device>(); } };
  private static final ThreadLocal<Map<Long, Tile>> tile_map = new ThreadLocal<Map<Long, Tile>>() { @Override protected Map<Long, Tile> initialValue() { return new HashMap<Long, Tile>(); } };

  @CEntryPoint(name = "Device_getDevice")
  public static long Device_getDevice(IsolateThread thread, final @CConst CCharPointer deviceName) {
    final String devName = CTypeConversion.toJavaString(deviceName);
    final Device device = Device.getDevice(devName);
    final Long boxed_id = device_id_counter.get();
    final long id = boxed_id.longValue();
    device_id_counter.set(boxed_id + 1L);
    device_map.get().put(id, device);
    return id;
  }

  @CEntryPoint(name = "Device_freeId")
  public static void Device_freeId(IsolateThread thread, final long id) {
    device_map.get().remove(id);
  }

  @CEntryPoint(name = "Device_getTile")
  public static long Device_getTile(IsolateThread thread, final long deviceId, final int row, final int column) {
    final Device device = device_map.get().get(deviceId);
    final Tile tile = device.getTile(row, column);
    final Long boxed_id = tile_id_counter.get();
    final long id = boxed_id.longValue();
    tile_id_counter.set(boxed_id + 1L);
    tile_map.get().put(id, tile);
    return id;
  }

  @CEntryPoint(name = "Tile_freeId")
  public static void Tile_freeId(IsolateThread thread, final long id) {
    tile_map.get().remove(id);
  }

  @CEntryPoint(name = "Tile_getName")
  public static CCharPointer Tile_getName(IsolateThread thread, final long tileId) {
    final Tile tile = tile_map.get().get(tileId);
    final String name = tile.getName();
    final byte[] bytes = name.getBytes();
    final UnsignedWord size = WordFactory.unsigned(bytes.length + 1);
    final CCharPointer buffer = UnmanagedMemory.malloc(size);
    CTypeConversion.toCString(name, buffer, size);

    // return CTypeConversion.toCString(name).get();
    return buffer;
  }

  @CEntryPoint(name = "freeString")
  public static void freeString(IsolateThread thread, CCharPointer pointer) {
    if (pointer.isNonNull()) {
      UnmanagedMemory.free(pointer);
    }
  }
}
