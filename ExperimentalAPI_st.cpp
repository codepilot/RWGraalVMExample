#include <print>
#include <optional>
#include <vector>
#include <ranges>
#include <thread>

#include "ExperimentalAPI_st.hpp"


int main(int argc, char **argv) {
  using Device = com::xilinx::rapidwright::device::Device;
  using TileColumnPattern = com::xilinx::rapidwright::device::helper::TileColumnPattern;

  std::println("Device::RAPIDWRIGHT_YEAR_VERSION: {}", Device::RAPIDWRIGHT_YEAR_VERSION);
  std::println("Device::RAPIDWRIGHT_QUARTER_VERSION: {}", Device::RAPIDWRIGHT_QUARTER_VERSION);
  std::println("Device::RAPIDWRIGHT_MINOR_VERSION: {}", Device::RAPIDWRIGHT_MINOR_VERSION);
  std::println("Device::QUIET_MESSAGE: {}", static_cast<int>(Device::QUIET_MESSAGE));
  std::println("TileColumnPattern::MAX_PATTERN_LENGTH: {}", static_cast<int>(TileColumnPattern::MAX_PATTERN_LENGTH));
  TileColumnPattern::MAX_PATTERN_LENGTH = TileColumnPattern::MAX_PATTERN_LENGTH + 1;
  std::println("TileColumnPattern::MAX_PATTERN_LENGTH: {}", static_cast<int>(TileColumnPattern::MAX_PATTERN_LENGTH));

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
  return 0;
}
