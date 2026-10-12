#pragma once

#include <optional>
#include <utility>
#include <cstdlib>

// This is the header file created by native-image (Graal)
#include <librapidwright.h>

namespace GraalVM_Native_Image {
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

    inline explicit Isolate(graal_isolate_t* isolate, graal_isolatethread_t* thread) noexcept :
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

  };
  inline static Isolate globalIsolate{ Isolate::create().value() };
};