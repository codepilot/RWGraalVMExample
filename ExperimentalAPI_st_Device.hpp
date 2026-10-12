#pragma once

#include <optional>
#include <utility>
#include <cstdlib>

// This is the header file created by native-image (Graal)
#include <librapidwright.h>

#define Device_freeId st_Device_freeId
#define Device_getTile st_Device_getTile
#define Device_getDevice st_Device_getDevice

namespace GraalVM_Native_Image {
  struct Device {
    int64_t id;

    Device(const Device& other) = delete;

    Device& operator=(const Device& other) = delete;

    inline Device(Device&& other) noexcept :
      id{ std::exchange(other.id, 0) } {}

    inline Device& operator=(Device&& other) noexcept {
      std::swap(*this, other);
      return *this;
    }

    inline explicit Device(int64_t id) noexcept :
      id{ id } {}

    inline ~Device() noexcept {
      if (id) {
        Device_freeId(globalIsolate._thread, id);
      }
    }
    inline Tile getTile(const int32_t row, const int32_t col) const noexcept {
      const auto tileID{ Device_getTile(globalIsolate._thread, id, row, col) };
      return Tile{ tileID };
    }
    inline static Device getDevice(const char* devName) noexcept {
      const auto deviceID{ Device_getDevice(globalIsolate._thread, devName) };
      return Device{ deviceID };
    }
  };
};
