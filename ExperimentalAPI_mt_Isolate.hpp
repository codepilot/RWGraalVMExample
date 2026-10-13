#pragma once

#include <optional>
#include <utility>
#include <cstdlib>

// This is the header file created by native-image (Graal)
#include <librapidwright.h>
#include <shared_mutex>
#include <mutex>

namespace GraalVM_Native_Image {

  inline static const std::thread::id MAIN_THREAD_ID{ std::this_thread::get_id() };

  struct unique_graal_isolate_t_destructor {
    inline void operator()(graal_isolate_t* ptr) const noexcept {
      if (ptr) {
        if (std::this_thread::get_id() == MAIN_THREAD_ID) {
          if constexpr (is_debug) {
            std::println("unique_graal_isolate_t_destructor");
            std::println("graal_tear_down_isolate");
          }
          graal_isolatethread_t* thread{ graal_get_current_thread(ptr) };
          if (graal_tear_down_isolate(thread) != 0) {
            fprintf(stderr, "graal_tear_down_isolate error\n");
            std::abort();
          }
        } else {
          std::abort();
        }
      }
    }
  };

  struct unique_graal_isolatethread_t_destructor {
    inline void operator()(graal_isolatethread_t* ptr) const noexcept {
      if (ptr) {
        if (std::this_thread::get_id() == MAIN_THREAD_ID) {
          if constexpr (is_debug) {
            std::println("unique_graal_isolatethread_t_destructor, main thread, do not detatch");
          }
        } else {
          if constexpr (is_debug) {
            std::println("unique_graal_isolatethread_t_destructor, graal_detach_thread");
          }
          if (graal_detach_thread(ptr) != 0) {
            fprintf(stderr, "graal_detach_thread error\n");
            std::abort();
          }
        }
      }
    }
  };

  using unique_graal_isolate_t = std::unique_ptr<graal_isolate_t, unique_graal_isolate_t_destructor>;
  using unique_graal_isolatethread_t = std::unique_ptr<graal_isolatethread_t, unique_graal_isolatethread_t_destructor>;

  inline static constinit thread_local unique_graal_isolatethread_t localThread{};

  inline static const unique_graal_isolate_t globalIsolate{ []()-> graal_isolate_t* {
    graal_isolate_t* isolate{};
    graal_isolatethread_t* thread{};
    if constexpr (is_debug) {
      std::println("graal_create_isolate");
    }
    graal_create_isolate(nullptr, &isolate, &thread);
    localThread = unique_graal_isolatethread_t(thread);
    return isolate;
  }()};
};