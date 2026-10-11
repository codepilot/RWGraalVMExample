#include <print>
#include <optional>
#include <vector>
#include <ranges>

#include "ExperimentalAPI.hpp"

int main(int argc, char **argv) {

  std::vector<std::string> args;
  args.reserve(argc);
  std::ranges::copy(std::span<char*>(argv, static_cast<std::size_t>(argc)).subspan(1), std::back_inserter(args));

  const auto isolate{ GraalVM_Native_Image::Isolate::create() };

  const auto device{isolate->getDevice(args.at(0).c_str())};

  int maxRow = 105;
  int maxCol = 105;
  for (int row = 100; row < maxRow; row++){
    for (int col = 100; col < maxCol; col++){
      const auto tile{ device.getTile(row, col) };
      std::println("Tile({})[{}, {}] = \"{}\"", tile.id, col, row, tile.getName().str);
    }
  }
  
  return 0;
}
