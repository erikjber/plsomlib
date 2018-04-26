include_directories("../lib/googletest/googletest/include" "../lib/googletest/googletest")

add_executable(runBasicNetworkTests
        iterative_array_check.cc
        plsom2_check.cc diameterbuffer_check.cc diameterbuffer_check.h)

target_link_libraries(runBasicNetworkTests plsom gtest gtest_main )
