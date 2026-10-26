#pragma once

__forceinline java::lang::String utf8ToJavaString(const std::string& str) noexcept {
  return { org_graalvm_nativeimage_c_type_CTypeConversion_utf8ToJavaString(localThread.get(), str.c_str()) };
}

__forceinline java::lang::String utf8ToJavaString(std::string&& str) noexcept {
  return { org_graalvm_nativeimage_c_type_CTypeConversion_utf8ToJavaString(localThread.get(), str.c_str()) };
}

__forceinline java::lang::String utf8ToJavaString(const char* str) noexcept {
  return { org_graalvm_nativeimage_c_type_CTypeConversion_utf8ToJavaString(localThread.get(), str) };
}

struct StringHolder {
  char* cstr;
  __forceinline StringHolder(const java::lang::String&& jstr) noexcept : cstr{ org_graalvm_nativeimage_UnmanagedMemory_malloc_String(localThread.get(), jstr._objectHandle) } {}
  __forceinline StringHolder(const java::lang::String& jstr) noexcept : cstr{ org_graalvm_nativeimage_UnmanagedMemory_malloc_String(localThread.get(), jstr._objectHandle) } { }
  __forceinline ~StringHolder() noexcept { org_graalvm_nativeimage_UnmanagedMemory_free(localThread.get(), cstr); }
  __forceinline operator const char* () const noexcept { return cstr; }
};

template <typename CharT>
struct ::std::formatter<java::lang::String, CharT> : public ::std::formatter<const char*, CharT> {
  __forceinline constexpr auto format(const java::lang::String&& wrapper, auto& ctx) const noexcept {
    return std::formatter<const char*, CharT>::format(StringHolder(wrapper).cstr, ctx);
  }
  __forceinline constexpr auto format(const java::lang::String& wrapper, auto& ctx) const noexcept {
    return std::formatter<const char*, CharT>::format(StringHolder(wrapper).cstr, ctx);
  }
};