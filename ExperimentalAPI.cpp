#include <print>
#include <optional>
#include <vector>
#include <ranges>

// This is the header file created by native-image (Graal)
#include <librapidwright.h>

namespace GraalVM_Native_Image {
  struct String {
    graal_isolatethread_t* thread;
    char* str;

    String(const String& other) = delete;

    String& operator=(const String& other) = delete;

    inline String(String&& other) noexcept :
      thread{ std::exchange(other.thread, nullptr) },
      str{ std::exchange(other.str, nullptr) } {}

    inline String& operator=(String&& other) noexcept {
      std::swap(*this, other);
      return *this;
    }

    explicit inline String(graal_isolatethread_t* thread, char* str) noexcept :
      thread{ thread },
      str{ str } {}

    inline ~String() noexcept {
      if (thread) {
        if (str) {
          freeString(thread, str);
        }
      }
    }

    operator const char* () const noexcept {
      return str;
    }
  };

  struct Tile {
    graal_isolatethread_t* thread;
    int64_t id;

    Tile(const Tile& other) = delete;

    Tile& operator=(const Tile& other) = delete;

    inline Tile(Tile&& other) noexcept :
      thread{ std::exchange(other.thread, nullptr) },
      id{ std::exchange(other.id, 0) } {}

    inline Tile& operator=(Tile&& other) noexcept {
      std::swap(*this, other);
      return *this;
    }

    explicit inline Tile(graal_isolatethread_t* thread, int64_t id) noexcept :
      thread{ thread },
      id{ id } {}

    inline ~Tile() noexcept {
      if (thread) {
        if (id) {
          Tile_freeId(thread, id);
        }
      }
    }
    inline String getName() const noexcept {
      auto str{ Tile_getName(thread, id) };
      return String{ thread, str };
    }
  };

  struct Device {
    graal_isolatethread_t* thread;
    int64_t id;

    Device(const Device& other) = delete;

    Device& operator=(const Device& other) = delete;

    inline Device(Device&& other) noexcept :
      thread{ std::exchange(other.thread, nullptr) },
      id{ std::exchange(other.id, 0) } {}

    inline Device& operator=(Device&& other) noexcept {
      std::swap(*this, other);
      return *this;
    }

    explicit inline Device(graal_isolatethread_t* thread, int64_t id) noexcept :
      thread{ thread },
      id{ id } {}

    inline ~Device() noexcept {
      if (thread) {
        if (id) {
          Device_freeId(thread, id);
        }
      }
    }
    inline Tile getTile(const int32_t row, const int32_t col) const noexcept {
      const auto tileID{ Device_getTile(thread, id, row, col) };
      return Tile{ thread, tileID };
    }
  };

  struct Isolate {
    graal_isolate_t* isolate;
    graal_isolatethread_t *thread;

    Isolate(const Isolate& other) = delete;

    Isolate& operator=(const Isolate& other) = delete;

    inline Isolate(Isolate&& other) noexcept :
      isolate{ std::exchange(other.isolate, nullptr) },
      thread{ std::exchange(other.thread, nullptr) } {}

    inline Isolate& operator=(Isolate&& other) noexcept {
      std::swap(*this, other);
      return *this;
    }

    explicit inline Isolate(graal_isolate_t* isolate, graal_isolatethread_t* thread) noexcept :
      isolate{ isolate },
      thread{ thread } {}

    inline ~Isolate() noexcept {
      if (thread) {
        if (graal_detach_thread(thread) != 0) {
          fprintf(stderr, "graal_detach_thread error\n");
          std::abort();
        }
      }
    }

    inline static std::optional<Isolate> create() noexcept {
      graal_isolate_t* isolate{};
      graal_isolatethread_t* thread{};
      if (graal_create_isolate(nullptr, &isolate, &thread) != 0) {
        return std::nullopt;
      }
      return std::make_optional<Isolate>(isolate, thread);
    }

    inline Device getDevice(const char* devName) const noexcept {
      const auto deviceID{ Device_getDevice(thread, devName) };
      return Device{ thread, deviceID };
    }
  };
};



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
