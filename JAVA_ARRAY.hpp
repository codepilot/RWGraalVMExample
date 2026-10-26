#pragma once

template<typename T>
struct JAVA_ARRAY {
  java::lang::RANK_1_Object objArray;
  int32_t length;

  __forceinline explicit JAVA_ARRAY(OBJECT_HANDLE_RANK_1_java_lang_Object _objectHandle) noexcept :
    objArray{ _objectHandle },
    length{ length_rank_1_java_lang_Object(localThread.get(), _objectHandle) } {}

  __forceinline explicit JAVA_ARRAY(OBJECT_HANDLE_java_util_Collection _objectHandle) noexcept :
    JAVA_ARRAY(java_util_Collection_toArray(localThread.get(), _objectHandle)) {}

  __forceinline T operator[](int32_t index) const noexcept {
    return T{ std::bit_cast<decltype(std::declval<T>()._objectHandle)>(get_rank_1_java_lang_Object(localThread.get(), objArray._objectHandle, index)) };
  }

  struct Iterator {
    using iterator_concept = std::input_iterator_tag;
    using iterator_category = std::input_iterator_tag;
    using value_type = T;
    using difference_type = std::ptrdiff_t;
    using pointer = void;
    using reference = T;

    __forceinline constexpr Iterator() noexcept : _objectHandle{}, offset{} {
      std::println("Iterator()");
    }
    __forceinline constexpr Iterator(OBJECT_HANDLE_RANK_1_java_lang_Object _objectHandle, int32_t offset) noexcept : _objectHandle(_objectHandle), offset{ offset } {
      std::println("Iterator({}, {})", std::bit_cast<std::ptrdiff_t>(_objectHandle), offset);
    }

    __forceinline reference operator*() const noexcept {
      std::println("_objectHandle: {}, offset: {}", std::bit_cast<std::ptrdiff_t>(_objectHandle), offset);

      return T(
        std::bit_cast<decltype(std::declval<T>()._objectHandle)>(
          get_rank_1_java_lang_Object(localThread.get(), _objectHandle, offset)
        )
      );
    }

    //struct ArrowProxy {
    //  T val;
    //  __forceinline constexpr const T* operator->() const noexcept { return &val; }
    //};

    //__forceinline constexpr ArrowProxy operator->() const noexcept {
    //  return ArrowProxy{ **this };
    //}

    //__forceinline reference operator[](difference_type off) const noexcept {
    //  return T{
    //    std::bit_cast<decltype(std::declval<T>()._objectHandle)>(
    //      get_rank_1_java_lang_Object(localThread.get(), _objectHandle, offset + off)
    //    )
    //  };
    //}

    __forceinline constexpr Iterator& operator++() noexcept {
      offset++;
      return *this;
    }

    //__forceinline constexpr Iterator& operator+=(difference_type value) noexcept {
    //  offset += value;
    //  return *this;
    //}

    //__forceinline constexpr Iterator& operator-=(difference_type value) noexcept {
    //  offset -= value;
    //  return *this;
    //}

    __forceinline constexpr Iterator operator++(int) noexcept {
      Iterator tmp = *this;
      ++(*this);
      return tmp;
    }

    //__forceinline constexpr Iterator& operator--() noexcept {
    //  offset--;
    //  return *this;
    //}

    //__forceinline constexpr Iterator operator--(int) noexcept {
    //  Iterator tmp = *this;
    //  --(*this);
    //  return tmp;
    //}

    //__forceinline constexpr difference_type operator-(const Iterator& other) const noexcept {
    //  return offset - other.offset;
    //}

    //__forceinline constexpr friend bool operator<=>(const Iterator& a, const Iterator& b) noexcept {
    //  return a.offset <=> b.offset;
    //}

    __forceinline constexpr friend bool operator==(const Iterator& a, const Iterator& b) noexcept {
      return a.offset == b.offset;
    }

    __forceinline constexpr friend bool operator!=(const Iterator& a, const Iterator& b) noexcept {
      return a.offset != b.offset;
    }

    //__forceinline constexpr friend Iterator operator+(Iterator it, std::ptrdiff_t n) {
    //  return Iterator(it._objectHandle, it.offset + n);
    //}

    //__forceinline constexpr friend Iterator operator+(std::ptrdiff_t n, Iterator it) {
    //  return Iterator(it._objectHandle, it.offset + n);

    //}

    //__forceinline constexpr friend Iterator operator-(Iterator it, std::ptrdiff_t n) {
    //  return Iterator(it._objectHandle, it.offset - n);
    //}

    OBJECT_HANDLE_RANK_1_java_lang_Object _objectHandle;
    int32_t offset;
  };

  __forceinline constexpr Iterator begin() const noexcept { return Iterator(objArray._objectHandle, 0); }
  __forceinline constexpr Iterator end() const noexcept { return Iterator(objArray._objectHandle, length); }
  static_assert(std::input_iterator<JAVA_ARRAY<T>::Iterator>);
  static_assert(std::indirectly_readable<JAVA_ARRAY<T>::Iterator>);
  static_assert(std::input_or_output_iterator<JAVA_ARRAY<T>::Iterator>);
  //static_assert(std::forward_iterator<JAVA_ARRAY<T>::Iterator>);
  //static_assert(std::bidirectional_iterator<JAVA_ARRAY<T>::Iterator>);
  //static_assert(std::random_access_iterator<JAVA_ARRAY<T>::Iterator>);
  //static_assert(std::contiguous_iterator<JAVA_ARRAY<T>::Iterator>);
};

