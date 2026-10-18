package com.xilinx.rapidwright.examples;

import com.xilinx.rapidwright.util.FileTools;
import com.xilinx.rapidwright.device.Device;

@SuppressWarnings({"preview","deprecation","removal"})
public class FileToolsUpdateAllDataFiles {
  public static void main(String[] args) {
    FileTools.updateAllDataFiles();
    final String devName = "xcvu9p";
    final int row = 1;
    final int column = 1;
    final Device device = Device.getDevice(devName);
    final String tileName = device.getTile(row, column).getName();
    System.out.println("tileName: " + tileName);
  }
}
