cmake_minimum_required(VERSION 3.10)
project(cpp)

set(CMAKE_CXX_STANDARD 11)

include_directories(plsomlib)
include_directories(plsomlib_tests/lib/googletest/googletest/include plsomlib_tests/lib/googletest/googletest)

add_subdirectory(plsomlib)
add_subdirectory(plsomlib_tests)

