#pragma once

#include <optional>
#include <utility>
#include <cstdlib>

// This is the header file created by native-image (Graal)
#include <librapidwright.h>
#include <shared_mutex>
#include <mutex>

namespace GraalVM_Native_Image {

  struct Isolate {
    inline static const std::thread::id MAIN_THREAD_ID = std::this_thread::get_id();
    inline static constinit graal_isolate_t* _isolate{};
    graal_isolatethread_t* _thread;

    inline static bool is_main_thread() noexcept {
      return std::this_thread::get_id() == MAIN_THREAD_ID;
    }

    Isolate(const Isolate& other) = delete; // copy constructor
    Isolate(Isolate&& other) = delete; //move constructor
    Isolate& operator=(const Isolate& other) = delete; //copy assignment
    Isolate& operator=(Isolate&& other) = delete; //move assignment

    inline consteval explicit Isolate() noexcept : _thread{} {}

    inline bool attach() noexcept {
      if (is_main_thread()) {
        if (graal_create_isolate(nullptr, &_isolate, &_thread) != 0) {
          std::abort();
        }
      } else {
        if (graal_attach_thread(_isolate, &_thread) != 0) {
          std::abort();
        }
      }
      return true;
    }

    inline ~Isolate() noexcept {
      if (_thread) {
        if (Isolate::is_main_thread()) {
          if (graal_tear_down_isolate(_thread) != 0) {
              fprintf(stderr, "graal_tear_down_isolate error\n");
              std::abort();
          }
        } else {
          if (graal_detach_thread(_thread) != 0) {
            fprintf(stderr, "graal_detach_thread error\n");
            std::abort();
          }
        }
      }
    }
  };

  inline static thread_local constinit Isolate globalIsolate{};
  inline static const bool isMainAttached{ globalIsolate.attach() };
};