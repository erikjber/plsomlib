cmake_minimum_required(VERSION 3.10)
project(cpp)

set(CMAKE_CXX_STANDARD 11)

add_library(cpp metrics/EuclideanMetric.cc metrics/EuclideanMetric.h neighbourhood/GaussianNeighbourhoodFunction.cc neighbourhood/GaussianNeighbourhoodFunction.h util/DiameterBuffer.cc util/DiameterBuffer.h util/IterativeArray.cc util/IterativeArray.h MapBase.cc MapBase.h MapBaseImpl.cc MapBaseImpl.h PLSOM2.cc PLSOM2.h)
