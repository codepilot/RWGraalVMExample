#include <print>
#include <optional>
#include <vector>
#include <ranges>
#include <thread>
#include <chrono>
#include <print>
#include <ranges>

#include "ExperimentalAPI_st.hpp"
#include "util_org_graalvm.hpp"
#include "org_graalvm_nativeimage_c_type_CTypeConversion.h"
#include "JAVA_ARRAY.hpp"

using Device = com::xilinx::rapidwright::device::Device;
using Tile = com::xilinx::rapidwright::device::Tile;
using Design = com::xilinx::rapidwright::design::Design;
using Cell = com::xilinx::rapidwright::design::Cell;
using Net = com::xilinx::rapidwright::design::Net;
using ModulePlacement = com::xilinx::rapidwright::design::ModulePlacement;
using RWRouteConfig = com::xilinx::rapidwright::rwroute::RWRouteConfig;
using TileColumnPattern = com::xilinx::rapidwright::device::helper::TileColumnPattern;
using MainEntrypoint = com::xilinx::rapidwright::MainEntrypoint;

void test_getDevice(const std::string &dev) noexcept {
  for (int i{}; i < 10; i++) {
    {
      auto device{ Device::getDevice(utf8ToJavaString(dev))};
      int maxRow = 105;
      int maxCol = 105;
      for (int row = 100; row < maxRow; row++){
        for (int col = 100; col < maxCol; col++){
          auto tile{ device.getTile(row, col) };
          std::println("Tile({})[{}, {}]: {}", (intptr_t)tile._objectHandle, col, row, tile.getName());
        }
      }
    }
  }
}

void test_readCheckpoint(const std::string& dcp, const std::string& edf) noexcept {
  auto design{ Design::readCheckpoint(utf8ToJavaString(dcp), utf8ToJavaString(edf)) };

  JAVA_ARRAY<Cell> cells{ design.getCells()._objectHandle };
  JAVA_ARRAY<Net> nets{ design.getNets()._objectHandle };

  std::println("cellsLength: {}", cells.length);
  std::println("netsLength: {}", nets.length);

  std::println("cell[0]: {}", cells[0].toString());

  for (auto &&cell : cells) {
    std::println("cell: {}", cell.toString());
  }

  for (auto p : cells | std::views::enumerate) {
    std::println("cell[{}]: {}", std::get<0>(p), std::get<1>(p).toString());
  }

  for (auto &&net: nets) {
    std::println("net: {}", net.toString());
  }
}

int main(int argc, char **argv) {
  std::println("Device::FRAMEWORK_NAME: {}", Device::get_static_field_FRAMEWORK_NAME());
  std::println("Device::RAPIDWRIGHT_VERSION: {}", Device::get_static_field_RAPIDWRIGHT_VERSION());
  std::println("Device::RAPIDWRIGHT_YEAR_VERSION: {}", Device::get_static_field_RAPIDWRIGHT_YEAR_VERSION());
  std::println("Device::DEVICE_FILE_VERSION: {}", Device::get_static_field_DEVICE_FILE_VERSION());
  std::println("Device::RAPIDWRIGHT_QUARTER_VERSION: {}", Device::get_static_field_RAPIDWRIGHT_QUARTER_VERSION());
  std::println("Device::RAPIDWRIGHT_MINOR_VERSION: {}", Device::get_static_field_RAPIDWRIGHT_MINOR_VERSION());
  std::println("Device::QUIET_MESSAGE: {}", Device::get_static_field_QUIET_MESSAGE());
  std::println("TileColumnPattern::MAX_PATTERN_LENGTH: {}", TileColumnPattern::get_static_field_MAX_PATTERN_LENGTH());
  //TileColumnPattern::MAX_PATTERN_LENGTH = TileColumnPattern::MAX_PATTERN_LENGTH + 1;
  //std::println("TileColumnPattern::MAX_PATTERN_LENGTH: {}", TileColumnPattern::MAX_PATTERN_LENGTH);


  std::vector<std::string> args;
  args.reserve(argc);
  std::ranges::copy(std::span<char*>(argv, static_cast<std::size_t>(argc)).subspan(1), std::back_inserter(args));
  if (!args.empty()) {
    if (args.at(0) == "test_getDevice") {
      test_getDevice(args.at(1));
    } else if (args.at(0) == "test_readCheckpoint") {
      test_readCheckpoint(args.at(1), args.at(2));
    } else {
      ::java::lang::RANK_1_String main_args{ new_rank_1_java_lang_String(localThread.get(), args.size()) };
      for (auto [index, str] : args | std::views::enumerate) {
        set_rank_1_java_lang_String(localThread.get(), main_args._objectHandle, index, utf8ToJavaString(str)._objectHandle);
      }
      MainEntrypoint::main(main_args);
    }
  }

  return 0;
}
