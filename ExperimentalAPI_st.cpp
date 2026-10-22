#include <print>
#include <optional>
#include <vector>
#include <ranges>
#include <thread>

#include "ExperimentalAPI_st.hpp"


int main(int argc, char **argv) {
  using Device = com::xilinx::rapidwright::device::Device;
  using Design = com::xilinx::rapidwright::design::Design;
  using RWRouteConfig = com::xilinx::rapidwright::rwroute::RWRouteConfig;
  using TileColumnPattern = com::xilinx::rapidwright::device::helper::TileColumnPattern;

  {
    Design design{};
  }
  std::println("Device::RAPIDWRIGHT_YEAR_VERSION: {}", Device::get_static_field_RAPIDWRIGHT_YEAR_VERSION());
  std::println("Device::RAPIDWRIGHT_QUARTER_VERSION: {}", Device::get_static_field_RAPIDWRIGHT_QUARTER_VERSION());
  std::println("Device::RAPIDWRIGHT_MINOR_VERSION: {}", Device::get_static_field_RAPIDWRIGHT_MINOR_VERSION());
  std::println("Device::QUIET_MESSAGE: {}", Device::get_static_field_QUIET_MESSAGE());

#if 0
  std::println("Device::FRAMEWORK_NAME: {}", Device::FRAMEWORK_NAME);
  std::println("Device::RAPIDWRIGHT_VERSION: {}", Device::RAPIDWRIGHT_VERSION);
  std::println("Device::RAPIDWRIGHT_YEAR_VERSION: {}", Device::RAPIDWRIGHT_YEAR_VERSION);
  std::println("Device::DEVICE_FILE_VERSION: {}", Device::DEVICE_FILE_VERSION);
  std::println("Device::RAPIDWRIGHT_QUARTER_VERSION: {}", Device::RAPIDWRIGHT_QUARTER_VERSION);
  std::println("Device::RAPIDWRIGHT_MINOR_VERSION: {}", Device::RAPIDWRIGHT_MINOR_VERSION);
  std::println("Device::QUIET_MESSAGE: {}", Device::QUIET_MESSAGE);
  std::println("TileColumnPattern::MAX_PATTERN_LENGTH: {}", TileColumnPattern::MAX_PATTERN_LENGTH);
  TileColumnPattern::MAX_PATTERN_LENGTH = TileColumnPattern::MAX_PATTERN_LENGTH + 1;
  std::println("TileColumnPattern::MAX_PATTERN_LENGTH: {}", TileColumnPattern::MAX_PATTERN_LENGTH);

  std::vector<std::string> args;
  args.reserve(argc);
  std::ranges::copy(std::span<char*>(argv, static_cast<std::size_t>(argc)).subspan(1), std::back_inserter(args));

  for (int i{}; i < 10; i++) {
    {
      auto device{ Device::getDevice(args.at(0).c_str())};
      int maxRow = 105;
      int maxCol = 105;
      for (int row = 100; row < maxRow; row++){
        for (int col = 100; col < maxCol; col++){
          auto tile{ device.getTile(row, col) };
          std::println("Tile({})[{}, {}]: {}", tile.ObjectHandle, col, row, tile.getName());
        }
      }
    }
  }

  using MainEntrypoint = com::xilinx::rapidwright::MainEntrypoint;
  auto main_args{ ::java::lang::RANK_1_String(0) };
  MainEntrypoint::main(main_args);
#endif
  return 0;
}
