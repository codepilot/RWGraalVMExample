#pragma once

#include <optional>
#include <utility>
#include <cstdlib>

// This is the header file created by native-image (Graal)
#include <librapidwright.h>

namespace GraalVM_Native_Image {

  struct Isolate {
    graal_isolate_t* _isolate;
    graal_isolatethread_t *_thread;

    Isolate(const Isolate& other) = delete; // copy constructor
    Isolate(Isolate&& other) = delete; //move constructor
    Isolate& operator=(const Isolate& other) = delete; //copy assignment
    Isolate& operator=(Isolate&& other) = delete; //move assignment

    inline explicit Isolate() noexcept :
      _isolate{},
      _thread{} {
      if (graal_create_isolate(nullptr, &_isolate, &_thread) != 0) {
        std::abort();
      }
    }

    inline ~Isolate() noexcept {
      if (_thread) {
        if (graal_tear_down_isolate(_thread) != 0) {
            fprintf(stderr, "graal_tear_down_isolate error\n");
            std::abort();
        }
      }
    }

  };

  inline static const Isolate globalIsolate{};

};