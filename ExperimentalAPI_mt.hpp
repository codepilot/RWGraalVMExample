#pragma once

#include <optional>
#include <utility>
#include <cstdlib>

#include <frwd_ExperimentalAPI.hpp>
#include <decl_ExperimentalAPI.hpp>
// This is the header file created by native-image (Graal)
#include <librapidwright.h>
#ifdef NDEBUG
inline static constexpr bool is_debug{ false };
#else
inline static constexpr bool is_debug{ true };
#endif

#include "ExperimentalAPI_mt_Isolate.hpp"
#include <impl_ExperimentalAPI.hpp>
#include <frmt_ExperimentalAPI.hpp>
