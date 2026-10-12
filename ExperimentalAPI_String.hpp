#pragma once

#include <optional>
#include <utility>
#include <cstdlib>

// This is the header file created by native-image (Graal)
#include <librapidwright.h>

namespace GraalVM_Native_Image {
  struct String {
    char* str;

    String(const String& other) = delete;

    String& operator=(const String& other) = delete;

    inline String(String&& other) noexcept :
      str{ std::exchange(other.str, nullptr) } {}

    inline String& operator=(String&& other) noexcept {
      std::swap(*this, other);
      return *this;
    }

    inline explicit String(char* str) noexcept :
      str{ str } {}

    inline ~String() noexcept {
      if (str) {
        freeString(threadIsolate.get_thread(), str);
      }
    }

    inline operator const char* () const noexcept {
      return str;
    }
  };
};
