cmake_minimum_required(VERSION 4.4.2 FATAL_ERROR)
include(FetchContent)

project(RWGraalVMExample LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

FetchContent_Declare(graalvmexample URL https://www.rapidwright.io/docs/_downloads/920a717501e0cb0d129a581fd9ad4e03/GraalVMExample.tar.gz)
FetchContent_Declare(graalvm URL https://gds.oracle.com/download/graal/25i4/latest/graalvm-jdk-25i4-25_windows-x64_bin.zip)
FetchContent_Declare(rapidwright GIT_REPOSITORY https://github.com/Xilinx/RapidWright.git)

FetchContent_MakeAvailable(graalvmexample graalvm rapidwright)

set(GRAAL_PATH "${graalvm_SOURCE_DIR}/bin")

set(JAVA_EXE "${GRAAL_PATH}/java.exe")

set(JAVAC_EXE "${GRAAL_PATH}/javac.exe")

set(JAVA_HOME "${graalvm_SOURCE_DIR}")

set(PATH "${GRAAL_PATH};${PATH}")

set(rw_gradlew_bat "${rapidwright_SOURCE_DIR}/gradlew.bat")

set(RAPIDWRIGHT_PATH "${rapidwright_SOURCE_DIR}")


# file(REMOVE ${RAPIDWRIGHT_PATH}/bin/com/xilinx/rapidwright/util/RapidWright.class)
# file(REMOVE ${RAPIDWRIGHT_PATH}/src/com/xilinx/rapidwright/util/RapidWright.java)
file(REMOVE "${RAPIDWRIGHT_PATH}/src/com/xilinx/rapidwright/examples/RapidWrightAPI.java")

file(COPY_FILE "${CMAKE_CURRENT_SOURCE_DIR}/FileToolsUpdateAllDataFiles.java" "${RAPIDWRIGHT_PATH}/src/com/xilinx/rapidwright/examples/FileToolsUpdateAllDataFiles.java")

execute_process(COMMAND ${rw_gradlew_bat} compileJava WORKING_DIRECTORY ${rapidwright_SOURCE_DIR})
set(CLASS_PATH "${RAPIDWRIGHT_PATH}/build/libs/main.jar;${RAPIDWRIGHT_PATH}/build/libs/rapidwright.jar")

file(MAKE_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/META-INF/native-image/com.xilinx.rapidwright.examples/RapidWrightAPI")
file(COPY_FILE "${CMAKE_CURRENT_SOURCE_DIR}/reachability-metadata.json" "${CMAKE_CURRENT_BINARY_DIR}/META-INF/native-image/com.xilinx.rapidwright.examples/RapidWrightAPI/reachability-metadata.json")

execute_process(COMMAND ${JAVA_EXE} "-agentlib:native-image-agent=config-merge-dir=${CMAKE_CURRENT_BINARY_DIR}/META-INF/native-image/com.xilinx.rapidwright.examples/RapidWrightAPI" -cp "${CLASS_PATH}" "${RAPIDWRIGHT_PATH}/src/com/xilinx/rapidwright/examples/FileToolsUpdateAllDataFiles.java" -d "${RAPIDWRIGHT_PATH}" WORKING_DIRECTORY ${RAPIDWRIGHT_PATH})

file(COPY_FILE "${graalvmexample_SOURCE_DIR}/RapidWrightAPI.java" "${RAPIDWRIGHT_PATH}/src/com/xilinx/rapidwright/examples/RapidWrightAPI.java")

execute_process(COMMAND ${JAVAC_EXE} -cp "${CLASS_PATH}" "${RAPIDWRIGHT_PATH}/src/com/xilinx/rapidwright/examples/RapidWrightAPI.java" -d "${RAPIDWRIGHT_PATH}/bin" WORKING_DIRECTORY ${RAPIDWRIGHT_PATH}/bin)

file(ARCHIVE_EXTRACT
  INPUT "${rapidwright_SOURCE_DIR}/jars/zstd-jni-1.5.5-1.jar" 
  DESTINATION "${CMAKE_CURRENT_BINARY_DIR}/zstd-jni-1.5.5-1"
)

set(native_image_cmd "${GRAAL_PATH}/native-image.cmd")

execute_process(
  COMMAND "${native_image_cmd}"
  --exact-reachability-metadata
  --enable-native-access=ALL-UNNAMED
  -cp "${CLASS_PATH};${CMAKE_CURRENT_BINARY_DIR}"
  --initialize-at-build-time
  --initialize-at-run-time=com.esotericsoftware.kryo.unsafe.UnsafeUtil
  --initialize-at-run-time=com.github.luben.zstd
  --shared
  --verbose
  --emit build-report
  -H:+UnlockExperimentalVMOptions
  -H:+EnableFallbackCompilation
  -H:Name=librapidwright
  WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}
)
  
add_executable(RapidWrightExample "${graalvmexample_SOURCE_DIR}/RapidWrightExample.cpp")

target_compile_features(RapidWrightExample PUBLIC cxx_std_23)
target_include_directories(RapidWrightExample PUBLIC ${CMAKE_CURRENT_BINARY_DIR})
target_link_directories(RapidWrightExample PUBLIC ${CMAKE_CURRENT_BINARY_DIR})
target_link_libraries(RapidWrightExample PUBLIC librapidwright.lib)
set_target_properties(RapidWrightExample PROPERTIES VS_DEBUGGER_ENVIRONMENT "RAPIDWRIGHT_PATH=${RAPIDWRIGHT_PATH}\nPATH=${CMAKE_CURRENT_BINARY_DIR};${PATH}")
set_target_properties(RapidWrightExample PROPERTIES VS_DEBUGGER_COMMAND_ARGUMENTS "xcvu9p")
set_target_properties(RapidWrightExample PROPERTIES VS_DEBUGGER_WORKING_DIRECTORY "${RAPIDWRIGHT_PATH}")
set_property(DIRECTORY PROPERTY VS_STARTUP_PROJECT RapidWrightExample)
