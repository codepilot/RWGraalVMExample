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


file(REMOVE_RECURSE "${CMAKE_CURRENT_BINARY_DIR}/native-image")
file(MAKE_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/native-image/experimental")
file(MAKE_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/native-image/ProxyClasses/ProxyClass")
file(COPY ${CMAKE_CURRENT_SOURCE_DIR}/TransformAgent DESTINATION ${CMAKE_CURRENT_BINARY_DIR}/native-image)

message("COMPILING TransformAgent.java...")
execute_process(
  COMMAND ${JAVAC_EXE}
  -d ${CMAKE_CURRENT_BINARY_DIR}/native-image/TransformAgent
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
  WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/native-image/TransformAgent
)

execute_process(COMMAND ${rw_gradlew_bat} jar WORKING_DIRECTORY ${rapidwright_SOURCE_DIR})

set(CLASS_PATH "${RAPIDWRIGHT_PATH}/build/libs/rapidwright.jar;${RAPIDWRIGHT_PATH}/jars/zstd-jni-1.5.5-1.jar;${RAPIDWRIGHT_PATH}/jars/kryo-5.2.1.jar;${RAPIDWRIGHT_PATH}/jars/jgrapht-core-1.3.0.jar;${RAPIDWRIGHT_PATH}/jars/runtime-0.1.13.jar;${RAPIDWRIGHT_PATH}/jars/jopt-simple-5.0.4.jar;${RAPIDWRIGHT_PATH}/jars/jython-standalone-2.7.2.jar;${RAPIDWRIGHT_PATH}/jars/ortools-java-9.14.6206.jar;${RAPIDWRIGHT_PATH}/jars/protobuf-java-4.31.1.jar;${RAPIDWRIGHT_PATH}/jars/annotations-20.1.0.jar;${RAPIDWRIGHT_PATH}/jars/jupyter-kernel-jsr223-1.0.1.jar;${RAPIDWRIGHT_PATH}/jars/jeromq-0.5.2.jar;${RAPIDWRIGHT_PATH}/jars/commons-cli-1.2.jar;${RAPIDWRIGHT_PATH}/jars/json-20160810.jar;${RAPIDWRIGHT_PATH}/jars/jzlib-1.1.3.jar;${RAPIDWRIGHT_PATH}/jars/commons-io-2.20.0.jar;${RAPIDWRIGHT_PATH}/jars/org.eclipse.elk.alg.layered-0.10.0.jar;${RAPIDWRIGHT_PATH}/jars/org.eclipse.elk.alg.common-0.10.0.jar;${RAPIDWRIGHT_PATH}/jars/org.eclipse.elk.core-0.10.0.jar;${RAPIDWRIGHT_PATH}/jars/org.eclipse.elk.graph-0.10.0.jar;${RAPIDWRIGHT_PATH}/jars/guava-33.5.0-jre.jar;${RAPIDWRIGHT_PATH}/jars/qtjambi-win64-msvc2005x64-4.5.2_01.jar;${RAPIDWRIGHT_PATH}/jars/jacl-1.4.1.jar;${RAPIDWRIGHT_PATH}/jars/rapidwright-api-lib-2026.1.1.jar;${RAPIDWRIGHT_PATH}/jars/rapidwright-api-lib-2026.1.1-javadoc.jar;${RAPIDWRIGHT_PATH}/jars/reflectasm-1.11.9.jar;${RAPIDWRIGHT_PATH}/jars/objenesis-3.2.jar;${RAPIDWRIGHT_PATH}/jars/minlog-1.3.1.jar;${RAPIDWRIGHT_PATH}/jars/jheaps-0.9.jar;${RAPIDWRIGHT_PATH}/jars/ortools-linux-x86-64-9.14.6206.jar;${RAPIDWRIGHT_PATH}/jars/ortools-darwin-x86-64-9.14.6206.jar;${RAPIDWRIGHT_PATH}/jars/ortools-win32-x86-64-9.14.6206.jar;${RAPIDWRIGHT_PATH}/jars/ortools-linux-aarch64-9.14.6206.jar;${RAPIDWRIGHT_PATH}/jars/ortools-darwin-aarch64-9.14.6206.jar;${RAPIDWRIGHT_PATH}/jars/jna-platform-5.14.0.jar;${RAPIDWRIGHT_PATH}/jars/jnacl-1.0.0.jar;${RAPIDWRIGHT_PATH}/jars/failureaccess-1.0.3.jar;${RAPIDWRIGHT_PATH}/jars/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;${RAPIDWRIGHT_PATH}/jars/jspecify-1.0.0.jar;${RAPIDWRIGHT_PATH}/jars/error_prone_annotations-2.41.0.jar;${RAPIDWRIGHT_PATH}/jars/j2objc-annotations-3.1.jar;${RAPIDWRIGHT_PATH}/jars/qtjambi-4.5.2_01.jar;${RAPIDWRIGHT_PATH}/jars/org.eclipse.emf.ecore.xmi-2.12.0.jar;${RAPIDWRIGHT_PATH}/jars/org.eclipse.emf.common-2.12.0.jar;${RAPIDWRIGHT_PATH}/jars/org.eclipse.emf.ecore-2.12.0.jar;${RAPIDWRIGHT_PATH}/jars/jna-5.14.0.jar")

set(REFLECTION_CONFIG_DIR "${CMAKE_CURRENT_BINARY_DIR}/native-image/ProxyClasses/META-INF/native-image/com.xilinx.rapidwright.examples/RapidWrightAPI")

file(MAKE_DIRECTORY "${REFLECTION_CONFIG_DIR}")

file(COPY_FILE
  "${CMAKE_CURRENT_SOURCE_DIR}/reachability-metadata.json"
  "${REFLECTION_CONFIG_DIR}/reachability-metadata.json"
)

file(
  COPY_FILE
  "${CMAKE_CURRENT_SOURCE_DIR}/FileToolsUpdateAllDataFiles.java"
  "${CMAKE_CURRENT_BINARY_DIR}/native-image/ProxyClasses/ProxyClass/FileToolsUpdateAllDataFiles.java"
)

message("BUILDING FileToolsUpdateAllDataFiles.java...")
execute_process(COMMAND ${JAVAC_EXE}
  -cp "${CLASS_PATH}"
  "${CMAKE_CURRENT_BINARY_DIR}/native-image/ProxyClasses/ProxyClass/FileToolsUpdateAllDataFiles.java"
  -d "${CMAKE_CURRENT_BINARY_DIR}/native-image/ProxyClasses"
  WORKING_DIRECTORY ${RAPIDWRIGHT_PATH}/bin
)

message("EXECUTING FileToolsUpdateAllDataFiles.java...")
execute_process(COMMAND ${JAVA_EXE}
  "-agentlib:native-image-agent=config-merge-dir=${REFLECTION_CONFIG_DIR}"
  -cp "${CLASS_PATH}"
  --enable-native-access=ALL-UNNAMED
  "${CMAKE_CURRENT_BINARY_DIR}/native-image/ProxyClasses/ProxyClass/FileToolsUpdateAllDataFiles.java"
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


file(MAKE_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/native-image/ProxyClasses/com/xilinx/rapidwright/examples")

set(RapidWrightAPI_JAVA_PATH "${CMAKE_CURRENT_BINARY_DIR}/native-image/ProxyClasses/com/xilinx/rapidwright/examples/RapidWrightAPI.java")

file(COPY_FILE
  "${graalvmexample_SOURCE_DIR}/RapidWrightAPI.java"
  "${RapidWrightAPI_JAVA_PATH}"
)

message("COMPILING RapidWrightAPI.java...")
execute_process(
  COMMAND ${JAVAC_EXE}
  -cp "${CLASS_PATH}"
  -d "${CMAKE_CURRENT_BINARY_DIR}/native-image/ProxyClasses"
  "${RapidWrightAPI_JAVA_PATH}"
  WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/native-image/ProxyClasses
)

# message("COMPILING @java_file_list.txt...")
# execute_process(COMMAND ${JAVAC_EXE} -cp "${CLASS_PATH}" -d "${RAPIDWRIGHT_PATH}/bin" "@${rapidwright_SOURCE_DIR}/build/java_file_list.txt" WORKING_DIRECTORY ${RAPIDWRIGHT_PATH}/bin)

set(native_image_cmd "${GRAAL_PATH}/native-image.cmd")

message("EXECUTING NATIVE-IMAGE...")
execute_process(
  COMMAND "${native_image_cmd}"
  --exact-reachability-metadata
  --enable-native-access=ALL-UNNAMED
  -cp "${CLASS_PATH};${CMAKE_CURRENT_BINARY_DIR}/native-image/ProxyClasses"
  --initialize-at-build-time
  --initialize-at-run-time=com.esotericsoftware.kryo.unsafe.UnsafeUtil
  --initialize-at-run-time=com.esotericsoftware.kryo.util.CuckooObjectMap
  --initialize-at-run-time=com.github.luben.zstd
  --initialize-at-run-time=com.github.luben.zstd.util.Native
  --initialize-at-run-time=com.google.ortools.sat.mainJNI
  --initialize-at-run-time=com.xilinx.rapidwright.device.EnumerateIOExpansions
  --initialize-at-run-time=com.xilinx.rapidwright.device.PartNameTools
  --initialize-at-run-time=com.xilinx.rapidwright.device.Site
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
  --initialize-at-run-time=com.xilinx.rapidwright.util.ParallelismTools
  --initialize-at-run-time=org.fusesource.jansi.internal.CLibrary
  --initialize-at-run-time=org.fusesource.jansi.internal.Kernel32
  --initialize-at-run-time=org.python.modules.posix.PosixModule
  --initialize-at-run-time=org.python.modules.Setup
  --trace-object-instantiation=jdk.internal.misc.InnocuousThread
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
  -ERAPIDWRIGHT_PATH
  WORKING_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/native-image"
)

add_executable(RapidWrightExample "${graalvmexample_SOURCE_DIR}/RapidWrightExample.cpp")
target_compile_features(RapidWrightExample PUBLIC cxx_std_23)
target_include_directories(RapidWrightExample PUBLIC ${CMAKE_CURRENT_BINARY_DIR}/native-image)
target_link_directories(RapidWrightExample PUBLIC ${CMAKE_CURRENT_BINARY_DIR}/native-image)
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

target_include_directories(ExperimentalAPI_mt PUBLIC ${CMAKE_CURRENT_BINARY_DIR}/native-image)
target_include_directories(ExperimentalAPI_st PUBLIC ${CMAKE_CURRENT_BINARY_DIR}/native-image)

target_include_directories(ExperimentalAPI_mt PUBLIC "${CMAKE_CURRENT_BINARY_DIR}/native-image/experimental")
target_include_directories(ExperimentalAPI_st PUBLIC "${CMAKE_CURRENT_BINARY_DIR}/native-image/experimental")

target_link_directories(ExperimentalAPI_mt PUBLIC ${CMAKE_CURRENT_BINARY_DIR}/native-image)
target_link_directories(ExperimentalAPI_st PUBLIC ${CMAKE_CURRENT_BINARY_DIR}/native-image)

target_link_libraries(ExperimentalAPI_mt PUBLIC librapidwright.lib)
target_link_libraries(ExperimentalAPI_st PUBLIC librapidwright.lib)

set_target_properties(ExperimentalAPI_mt PROPERTIES VS_DEBUGGER_ENVIRONMENT "RAPIDWRIGHT_PATH=${RAPIDWRIGHT_PATH}\nPATH=${CMAKE_CURRENT_BINARY_DIR}/native-image;${PATH}")
set_target_properties(ExperimentalAPI_st PROPERTIES VS_DEBUGGER_ENVIRONMENT "RAPIDWRIGHT_PATH=${RAPIDWRIGHT_PATH}\nPATH=${CMAKE_CURRENT_BINARY_DIR}/native-image;${PATH}")

set_target_properties(ExperimentalAPI_mt PROPERTIES VS_DEBUGGER_COMMAND_ARGUMENTS "xcvu9p")
set_target_properties(ExperimentalAPI_st PROPERTIES VS_DEBUGGER_COMMAND_ARGUMENTS "xcvu9p")

set_target_properties(ExperimentalAPI_mt PROPERTIES VS_DEBUGGER_WORKING_DIRECTORY "${RAPIDWRIGHT_PATH}")
set_target_properties(ExperimentalAPI_st PROPERTIES VS_DEBUGGER_WORKING_DIRECTORY "${RAPIDWRIGHT_PATH}")

set_property(DIRECTORY PROPERTY VS_STARTUP_PROJECT ExperimentalAPI_st)
