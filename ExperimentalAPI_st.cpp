#include <print>
#include <optional>
#include <vector>
#include <ranges>
#include <thread>

#include "ExperimentalAPI_st.hpp"

using Device = GraalVM_Native_Image::com_xilinx_rapidwright_device_Device;

int main(int argc, char **argv) {

  std::println("Device::RAPIDWRIGHT_YEAR_VERSION: {}", Device::RAPIDWRIGHT_YEAR_VERSION);
  std::println("Device::RAPIDWRIGHT_QUARTER_VERSION: {}", Device::RAPIDWRIGHT_QUARTER_VERSION);
  std::println("Device::RAPIDWRIGHT_MINOR_VERSION: {}", Device::RAPIDWRIGHT_MINOR_VERSION);
  std::println("Device::QUIET_MESSAGE: {}", Device::QUIET_MESSAGE());

  std::vector<std::string> args;
  args.reserve(argc);
  std::ranges::copy(std::span<char*>(argv, static_cast<std::size_t>(argc)).subspan(1), std::back_inserter(args));

#if 0
  for (int i{}; i < 10; i++) {
    const auto device{ Device::getDevice(args.at(0).c_str())};

    int maxRow = 105;
    int maxCol = 105;
    for (int row = 100; row < maxRow; row++){
      for (int col = 100; col < maxCol; col++){
        const auto tile{ device.getTile(row, col) };
        std::println("Tile({})[{}, {}] = \"{}\"", tile.id, col, row, tile.getName().str);
      }
    }
  }
#endif
  
  return 0;
}
