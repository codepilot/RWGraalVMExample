package com.xilinx.rapidwright.examples;

import com.xilinx.rapidwright.util.FileTools;
import com.xilinx.rapidwright.device.Device;
// import com.xilinx.rapidwright.MainEntrypoint;
import org.graalvm.nativeimage.c.CHeader;

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
