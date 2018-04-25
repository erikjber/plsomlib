include_directories("../lib/googletest/googletest/include" "../lib/googletest/googletest")

add_executable(runBasicNetworkTests
        iterative_array_check.cc
        plsom2_check.cc)

target_link_libraries(runBasicNetworkTests gtest gtest_main)
target_link_libraries(runBasicNetworkTests cpp)
