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

set(JAR_EXE "${GRAAL_PATH}/jar.exe")

set(JAVA_HOME "${graalvm_SOURCE_DIR}")

set(PATH "${GRAAL_PATH};${PATH}")

set(rw_gradlew_bat "${rapidwright_SOURCE_DIR}/gradlew.bat")

set(RAPIDWRIGHT_PATH "${rapidwright_SOURCE_DIR}")

set(RAPIDWRIGHT_EXAMPLES_PATH "${RAPIDWRIGHT_PATH}/src/com/xilinx/rapidwright/examples")

file(REMOVE_RECURSE ${CMAKE_CURRENT_BINARY_DIR}/TransformAgent)

file(COPY ${CMAKE_CURRENT_SOURCE_DIR}/TransformAgent DESTINATION ${CMAKE_CURRENT_BINARY_DIR})

message("COMPILING TransformAgent.java...")
execute_process(
  COMMAND ${JAVAC_EXE}
  -d ${CMAKE_CURRENT_BINARY_DIR}/TransformAgent
  ${CMAKE_CURRENT_SOURCE_DIR}/TransformAgent/TransformAgent.java
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/TransformAgent
)

message("JARing TransformAgent.jar...")
execute_process(
  COMMAND ${JAR_EXE}
  cvfm
  TransformAgent.jar
  MANIFEST.MF
  *.class
  WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/TransformAgent
)

set(RapidWrightAPI_JAVA_PATH "${RAPIDWRIGHT_EXAMPLES_PATH}/RapidWrightAPI.java")
file(REMOVE "${RapidWrightAPI_JAVA_PATH}")
file(REMOVE "${RAPIDWRIGHT_EXAMPLES_PATH}/FileToolsUpdateAllDataFiles.java")


# file(REMOVE_RECURSE "${rapidwright_SOURCE_DIR}/build/experimental")
# file(REMOVE_RECURSE "${rapidwright_SOURCE_DIR}/src/com/xilinx/rapidwright/experimental")
# file(REMOVE_RECURSE "${rapidwright_SOURCE_DIR}/bin/com/xilinx/rapidwright/experimental")

# file(
#   COPY_FILE
#   "${CMAKE_CURRENT_SOURCE_DIR}/BuildInterface.java"
#   "${RAPIDWRIGHT_EXAMPLES_PATH}/BuildInterface.java"
# )

execute_process(COMMAND ${rw_gradlew_bat} compileJava WORKING_DIRECTORY ${rapidwright_SOURCE_DIR})
execute_process(COMMAND ${rw_gradlew_bat} jar WORKING_DIRECTORY ${rapidwright_SOURCE_DIR})

set(CLASS_PATH "${RAPIDWRIGHT_PATH}/build/libs/main.jar")

set(REFLECTION_CONFIG_DIR "${CMAKE_CURRENT_BINARY_DIR}/META-INF/native-image/com.xilinx.rapidwright.examples/RapidWrightAPI")

file(MAKE_DIRECTORY "${REFLECTION_CONFIG_DIR}")

file(COPY_FILE
  "${CMAKE_CURRENT_SOURCE_DIR}/reachability-metadata.json"
  "${REFLECTION_CONFIG_DIR}/reachability-metadata.json"
)

file(
  COPY_FILE
  "${CMAKE_CURRENT_SOURCE_DIR}/FileToolsUpdateAllDataFiles.java"
  "${RAPIDWRIGHT_EXAMPLES_PATH}/FileToolsUpdateAllDataFiles.java"
)

message("BUILDING FileToolsUpdateAllDataFiles.java...")
execute_process(COMMAND ${JAVAC_EXE}
  -cp "${CLASS_PATH}"
  "${RAPIDWRIGHT_EXAMPLES_PATH}/FileToolsUpdateAllDataFiles.java"
  -d "${RAPIDWRIGHT_PATH}/bin"
  WORKING_DIRECTORY ${RAPIDWRIGHT_PATH}/bin
)

message("EXECUTING FileToolsUpdateAllDataFiles.java...")
execute_process(COMMAND ${JAVA_EXE}
  "-agentlib:native-image-agent=config-merge-dir=${REFLECTION_CONFIG_DIR}"
  -cp "${CLASS_PATH}"
  --enable-native-access=ALL-UNNAMED
  "${RAPIDWRIGHT_EXAMPLES_PATH}/FileToolsUpdateAllDataFiles.java"
  -d "${RAPIDWRIGHT_PATH}"
  WORKING_DIRECTORY ${RAPIDWRIGHT_PATH}
)

# file(MAKE_DIRECTORY "${rapidwright_SOURCE_DIR}/build/experimental")
# file(MAKE_DIRECTORY "${rapidwright_SOURCE_DIR}/src/com/xilinx/rapidwright/experimental")

# message("EXECUTING BuildInterface.java...")
# execute_process(COMMAND ${JAVA_EXE}
#   "-agentlib:native-image-agent=config-merge-dir=${REFLECTION_CONFIG_DIR}"
#   -cp "${CLASS_PATH}"
#   "${RAPIDWRIGHT_EXAMPLES_PATH}/BuildInterface.java"
#   -d "${RAPIDWRIGHT_PATH}"
#   WORKING_DIRECTORY ${RAPIDWRIGHT_PATH}
# )

file(COPY_FILE
  "${graalvmexample_SOURCE_DIR}/RapidWrightAPI.java"
  "${RapidWrightAPI_JAVA_PATH}"
)

message("COMPILING RapidWrightAPI.java...")
execute_process(
  COMMAND ${JAVAC_EXE}
  -cp "${CLASS_PATH}"
  -d "${RAPIDWRIGHT_PATH}/bin"
  "${RapidWrightAPI_JAVA_PATH}"
  WORKING_DIRECTORY ${RAPIDWRIGHT_PATH}/bin
)

# message("COMPILING @java_file_list.txt...")
# execute_process(COMMAND ${JAVAC_EXE} -cp "${CLASS_PATH}" -d "${RAPIDWRIGHT_PATH}/bin" "@${rapidwright_SOURCE_DIR}/build/java_file_list.txt" WORKING_DIRECTORY ${RAPIDWRIGHT_PATH}/bin)

set(native_image_cmd "${GRAAL_PATH}/native-image.cmd")

file(REMOVE_RECURSE ${CMAKE_CURRENT_BINARY_DIR}/experimental)
file(MAKE_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/experimental")

message("EXECUTING NATIVE-IMAGE...")
execute_process(
  COMMAND "${native_image_cmd}"
  --exact-reachability-metadata
  --enable-native-access=ALL-UNNAMED
  -cp "${CLASS_PATH};${CMAKE_CURRENT_BINARY_DIR}"
  --initialize-at-build-time
  --initialize-at-run-time=com.esotericsoftware.kryo.unsafe.UnsafeUtil
  --initialize-at-run-time=com.esotericsoftware.kryo.util.CuckooObjectMap
  --initialize-at-run-time=com.github.luben.zstd
  --initialize-at-run-time=com.google.ortools.sat.mainJNI
  --initialize-at-run-time=org.python.modules.posix.PosixModule
  --initialize-at-run-time=org.python.modules.Setup
  --initialize-at-run-time=com.xilinx.rapidwright.util.ParallelismTools
  --initialize-at-run-time=com.xilinx.rapidwright.gui.FileFilters
  --initialize-at-run-time=com.xilinx.rapidwright.gui.GUIModuleInst
  --initialize-at-run-time=com.xilinx.rapidwright.gui.GUIShapeState
  --initialize-at-run-time=com.xilinx.rapidwright.gui.HierCellInstTreeWidgetItem
  --initialize-at-run-time=com.xilinx.rapidwright.gui.HMTile
  --initialize-at-run-time=com.xilinx.rapidwright.gui.ModuleInstanceScene
  --initialize-at-run-time=com.xilinx.rapidwright.gui.NetlistBrowser
  --initialize-at-run-time=com.xilinx.rapidwright.gui.NetlistTreeWidget
  --initialize-at-run-time=com.xilinx.rapidwright.gui.NumberedHighlightedTile
  --initialize-at-run-time=com.xilinx.rapidwright.gui.PBlockScene
  --initialize-at-run-time=com.xilinx.rapidwright.gui.SchematicScene
  --initialize-at-run-time=com.xilinx.rapidwright.gui.SchematicView
  --initialize-at-run-time=com.xilinx.rapidwright.gui.TileColors
  --initialize-at-run-time=com.xilinx.rapidwright.gui.TileScene
  --initialize-at-run-time=com.xilinx.rapidwright.gui.TileView
  --initialize-at-run-time=com.xilinx.rapidwright.gui.UiPBlock
  --initialize-at-run-time=com.xilinx.rapidwright.gui.UiTools
  --initialize-at-run-time=com.xilinx.rapidwright.gui.WidgetMaker
  --initialize-at-run-time=org.fusesource.jansi.internal.Kernel32
  --initialize-at-run-time=org.fusesource.jansi.internal.CLibrary
  -J-javaagent:TransformAgent/TransformAgent.jar
  --enable-native-access=ALL-UNNAMED
  --shared
  --emit build-report
  -g
  -O3
  --verbose
  --future-defaults=all
  --gc=G1
  -march=x86-64-v3
  -H:+UnlockExperimentalVMOptions
  -H:+SourceLevelDebug
  -H:-DeleteLocalSymbols
  -H:+AddAllCharsets
  -H:Name=librapidwright
  --native-compiler-options=/bigobj
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

add_executable(ExperimentalAPI_mt
  "${CMAKE_CURRENT_SOURCE_DIR}/util_org_graalvm.hpp"
  "${CMAKE_CURRENT_SOURCE_DIR}/ExperimentalAPI_mt.cpp"
  "${CMAKE_CURRENT_SOURCE_DIR}/ExperimentalAPI_mt.hpp"
  "${CMAKE_CURRENT_SOURCE_DIR}/ExperimentalAPI_mt_Isolate.hpp"
)

add_executable(ExperimentalAPI_st
  "${CMAKE_CURRENT_SOURCE_DIR}/util_org_graalvm.hpp"
  "${CMAKE_CURRENT_SOURCE_DIR}/ExperimentalAPI_st.cpp"
  "${CMAKE_CURRENT_SOURCE_DIR}/ExperimentalAPI_st.hpp"
  "${CMAKE_CURRENT_SOURCE_DIR}/ExperimentalAPI_mt_Isolate.hpp"
)

target_compile_options(ExperimentalAPI_mt PUBLIC /arch:AVX512 /Zc:__cplusplus /MP /GR- /constexpr:steps1073741824 /bigobj)
target_compile_options(ExperimentalAPI_st PUBLIC /arch:AVX512 /Zc:__cplusplus /MP /GR- /constexpr:steps1073741824 /bigobj)

target_compile_features(ExperimentalAPI_mt PUBLIC cxx_std_23)
target_compile_features(ExperimentalAPI_st PUBLIC cxx_std_23)

target_include_directories(ExperimentalAPI_mt PUBLIC ${CMAKE_CURRENT_BINARY_DIR})
target_include_directories(ExperimentalAPI_st PUBLIC ${CMAKE_CURRENT_BINARY_DIR})

target_include_directories(ExperimentalAPI_mt PUBLIC "${CMAKE_CURRENT_BINARY_DIR}/experimental")
target_include_directories(ExperimentalAPI_st PUBLIC "${CMAKE_CURRENT_BINARY_DIR}/experimental")

target_link_directories(ExperimentalAPI_mt PUBLIC ${CMAKE_CURRENT_BINARY_DIR})
target_link_directories(ExperimentalAPI_st PUBLIC ${CMAKE_CURRENT_BINARY_DIR})

target_link_libraries(ExperimentalAPI_mt PUBLIC librapidwright.lib)
target_link_libraries(ExperimentalAPI_st PUBLIC librapidwright.lib)

set_target_properties(ExperimentalAPI_mt PROPERTIES VS_DEBUGGER_ENVIRONMENT "RAPIDWRIGHT_PATH=${RAPIDWRIGHT_PATH}\nPATH=${CMAKE_CURRENT_BINARY_DIR};${PATH}")
set_target_properties(ExperimentalAPI_st PROPERTIES VS_DEBUGGER_ENVIRONMENT "RAPIDWRIGHT_PATH=${RAPIDWRIGHT_PATH}\nPATH=${CMAKE_CURRENT_BINARY_DIR};${PATH}")

set_target_properties(ExperimentalAPI_mt PROPERTIES VS_DEBUGGER_COMMAND_ARGUMENTS "xcvu9p")
set_target_properties(ExperimentalAPI_st PROPERTIES VS_DEBUGGER_COMMAND_ARGUMENTS "xcvu9p")

set_target_properties(ExperimentalAPI_mt PROPERTIES VS_DEBUGGER_WORKING_DIRECTORY "${RAPIDWRIGHT_PATH}")
set_target_properties(ExperimentalAPI_st PROPERTIES VS_DEBUGGER_WORKING_DIRECTORY "${RAPIDWRIGHT_PATH}")

set_property(DIRECTORY PROPERTY VS_STARTUP_PROJECT ExperimentalAPI_st)
