#pragma once

template<typename T>
struct JAVA_ARRAY {
  size_t _element_count;
  T* _element_ptr;

  JAVA_ARRAY() = delete;

  __forceinline explicit JAVA_ARRAY(const java::util::Collection &col) noexcept :
    _element_count{},
    _element_ptr{ std::bit_cast<T*>( java_util_Collection_JAVA_ARRAY(localThread.get(), col._objectHandle, &_element_count)) } {
  }

  __forceinline ~JAVA_ARRAY() noexcept {
    if (_element_ptr) {
      free_JAVA_ARRAY(localThread.get(), std::bit_cast<OBJECT_HANDLE_java_lang_Object *>(_element_ptr), _element_count);
    }
  }

  __forceinline constexpr T& operator[](size_t index) const noexcept {
    return _element_ptr[index];
  }

  __forceinline constexpr T& at(size_t index) const noexcept {
    if (index >= _element_count) {
      std::abort();
    }
    return _element_ptr[index];
  }

  __forceinline constexpr size_t size() const noexcept {
    return _element_count;
  }

  __forceinline constexpr T* begin() const noexcept { return _element_ptr; }
  __forceinline constexpr T* end() const noexcept { return _element_ptr + _element_count; }

  __forceinline constexpr std::span<T> get_span() const noexcept {
    return std::span(begin(), end());
  }
};

