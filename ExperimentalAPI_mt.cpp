#include <print>
#include <optional>
#include <vector>
#include <ranges>
#include <thread>

#include "ExperimentalAPI_mt.hpp"
#include "util_org_graalvm.hpp"

int main(int argc, char **argv) {
  using Device = com::xilinx::rapidwright::device::Device;
  using Tile = com::xilinx::rapidwright::device::Tile;
  using Design = com::xilinx::rapidwright::design::Design;
  using ModulePlacement = com::xilinx::rapidwright::design::ModulePlacement;
  using RWRouteConfig = com::xilinx::rapidwright::rwroute::RWRouteConfig;
  using TileColumnPattern = com::xilinx::rapidwright::device::helper::TileColumnPattern;

  std::println("Device::FRAMEWORK_NAME: {}", StringHolder(Device::get_static_field_FRAMEWORK_NAME()).cstr);
  std::println("Device::RAPIDWRIGHT_VERSION: {}", StringHolder(Device::get_static_field_RAPIDWRIGHT_VERSION()).cstr);
  std::println("Device::RAPIDWRIGHT_YEAR_VERSION: {}", Device::get_static_field_RAPIDWRIGHT_YEAR_VERSION());
  std::println("Device::DEVICE_FILE_VERSION: {}", StringHolder(Device::get_static_field_DEVICE_FILE_VERSION()).cstr);
  std::println("Device::RAPIDWRIGHT_QUARTER_VERSION: {}", Device::get_static_field_RAPIDWRIGHT_QUARTER_VERSION());
  std::println("Device::RAPIDWRIGHT_MINOR_VERSION: {}", Device::get_static_field_RAPIDWRIGHT_MINOR_VERSION());
  std::println("Device::QUIET_MESSAGE: {}", Device::get_static_field_QUIET_MESSAGE());
  std::println("TileColumnPattern::MAX_PATTERN_LENGTH: {}", TileColumnPattern::get_static_field_MAX_PATTERN_LENGTH());
  //TileColumnPattern::MAX_PATTERN_LENGTH = TileColumnPattern::MAX_PATTERN_LENGTH + 1;
  //std::println("TileColumnPattern::MAX_PATTERN_LENGTH: {}", TileColumnPattern::MAX_PATTERN_LENGTH);

  std::vector<std::string> args;
  args.reserve(argc);
  std::ranges::copy(std::span<char*>(argv, static_cast<std::size_t>(argc)).subspan(1), std::back_inserter(args));

  for (int i{}; i < 10; i++) {
    std::jthread extra{ [&]() {
      attach_thread();
      auto device{ Device::getDevice(utf8ToJavaString(args.at(0)))};
      int maxRow = 105;
      int maxCol = 105;
      for (int row = 100; row < maxRow; row++){
        for (int col = 100; col < maxCol; col++){
          auto tile{ device.getTile(row, col) };
          std::println("Tile({})[{}, {}]: {}", (intptr_t)tile._objectHandle, col, row, StringHolder(tile.getName()).cstr);
        }
      }
    }};
  }

  return 0;
}
