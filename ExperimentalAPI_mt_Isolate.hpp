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

    inline static bool is_main_thread() noexcept {
      return std::this_thread::get_id() == MAIN_THREAD_ID;
    }

    mutable std::shared_mutex _mutex;
    graal_isolate_t* _isolate;
    graal_isolatethread_t *_thread;

    Isolate(const Isolate& other) = delete; // copy constructor
    Isolate(Isolate&& other) = delete; //move constructor
    Isolate& operator=(const Isolate& other) = delete; //copy assignment
    Isolate& operator=(Isolate&& other) = delete; //move assignment

    inline explicit Isolate() noexcept :
      _mutex{},
      _isolate{},
      _thread{} {
      std::unique_lock<std::shared_mutex> lock(_mutex);
      if (graal_create_isolate(nullptr, &_isolate, &_thread) != 0) {
        std::abort();
      }
    }

    inline ~Isolate() noexcept {
      if (_thread) {
        if (Isolate::is_main_thread()) {
          if (graal_tear_down_isolate(_thread) != 0) {
              fprintf(stderr, "graal_tear_down_isolate error\n");
              std::abort();
          }
        }
      }
    }

    inline graal_isolate_t* get_isolate() const noexcept {
      std::shared_lock<std::shared_mutex> lock(_mutex);
      return _isolate;
    }

  };

  inline static const Isolate globalIsolate{};

  struct IsolateThread {
    graal_isolatethread_t* _thread;

    IsolateThread(const IsolateThread& other) = delete; // copy constructor
    IsolateThread(IsolateThread&& other) = delete; //move constructor
    IsolateThread& operator=(const IsolateThread& other) = delete; //copy assignment
    IsolateThread& operator=(IsolateThread&& other) = delete; //move assignment

    inline explicit IsolateThread() noexcept :
      _thread{} {
    }

    inline void attach() noexcept {
      if (graal_attach_thread(globalIsolate.get_isolate(), &_thread) != 0) {
        std::abort();
      }
    }

    inline graal_isolatethread_t* get_thread() noexcept {
      if (!_thread) attach();
      return _thread;
    }

    inline ~IsolateThread() noexcept {
      if (_thread) {
        if (!Isolate::is_main_thread()) {
          if (graal_detach_thread(_thread) != 0) {
            fprintf(stderr, "graal_detach_thread error\n");
            std::abort();
          }
        }
      }
    }
  };

  inline thread_local IsolateThread threadIsolate{};
};