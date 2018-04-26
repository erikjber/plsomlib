cmake_minimum_required(VERSION 3.10)
project(plsomlib)

set(CMAKE_CXX_STANDARD 11)

add_library(plsom
        metrics/EuclideanMetric.cc
        neighbourhood/GaussianNeighbourhoodFunction.cc
        util/DiameterBuffer.cc
        util/IterativeArray.cc
        MapBase.cc
        MapBaseImpl.cc
        PLSOM2.cc )
