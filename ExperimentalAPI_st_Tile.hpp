#pragma once

#include <optional>
#include <utility>
#include <cstdlib>

// This is the header file created by native-image (Graal)
#include <librapidwright.h>

#define Tile_freeId st_Tile_freeId
#define Tile_getName st_Tile_getName

namespace GraalVM_Native_Image {
  struct Tile {
    int64_t id;

    Tile(const Tile& other) = delete;

    Tile& operator=(const Tile& other) = delete;

    inline Tile(Tile&& other) noexcept :
      id{ std::exchange(other.id, 0) } {}

    inline Tile& operator=(Tile&& other) noexcept {
      std::swap(*this, other);
      return *this;
    }

    inline explicit Tile(int64_t id) noexcept :
      id{ id } {}

    inline ~Tile() noexcept {
      if (id) {
        Tile_freeId(globalIsolate._thread, id);
      }
    }
    inline String getName() const noexcept {
      auto str{ Tile_getName(globalIsolate._thread, id) };
      return String{ str };
    }
  };
};
