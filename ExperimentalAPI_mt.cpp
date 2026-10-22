#include <print>
#include <optional>
#include <vector>
#include <ranges>
#include <thread>

#include "ExperimentalAPI_mt.hpp"


int main(int argc, char **argv) {
  using Device = com::xilinx::rapidwright::device::Device;
#if 0
  std::println("Device::RAPIDWRIGHT_YEAR_VERSION: {}", Device::RAPIDWRIGHT_YEAR_VERSION);
  std::println("Device::RAPIDWRIGHT_QUARTER_VERSION: {}", Device::RAPIDWRIGHT_QUARTER_VERSION);
  std::println("Device::RAPIDWRIGHT_MINOR_VERSION: {}", Device::RAPIDWRIGHT_MINOR_VERSION);
  std::println("Device::QUIET_MESSAGE: {}", Device::QUIET_MESSAGE);

  std::vector<std::string> args;
  args.reserve(argc);
  std::ranges::copy(std::span<char*>(argv, static_cast<std::size_t>(argc)).subspan(1), std::back_inserter(args));

  for (int i{}; i < 10; i++) {
    std::jthread extra{ [&]() {
      attach_thread();

      auto device{ Device::getDevice(args.at(0).c_str())};

      int maxRow = 105;
      int maxCol = 105;
      for (int row = 100; row < maxRow; row++){
        for (int col = 100; col < maxCol; col++){
          auto tile{ device.getTile(row, col) };
          std::println("Tile({})[{}, {}] = \"{}\"", tile.ObjectHandle, col, row, tile.getName());
        }
      }
    }};
  }
#endif  
  return 0;
}
