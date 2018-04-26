/**
 * Created by Erik Berglund on 2018-04-25.
 */

#include <include/gtest/gtest.h>
#include <util/IterativeArray.h>
#include <util/DiameterBuffer.h>
#include "iterative_array_check.h"

TEST(iterative_array_test, simpletest)
{
  IterativeArray<double> * ia = new IterativeArray<double>({2,5});
  ASSERT_EQ(10,ia->getCount());
  ASSERT_EQ(2,ia->getDimensions().size());
  ASSERT_EQ(2,ia->getDimensions()[0]);
  ASSERT_EQ(5,ia->getDimensions()[1]);
  double tmp = 4;
  ia->setValueAtOffset(tmp,0);
  tmp = 8;
  ia->setValueAtOffset(tmp,1);
  tmp = 11;
  ia->setValueAtOffset(tmp,9);
  ASSERT_FLOAT_EQ(4, ia->getPointerFromOffset(0));
  ASSERT_FLOAT_EQ(8, ia->getPointerFromOffset(1));
  ASSERT_FLOAT_EQ(11,ia->getPointerFromOffset(9));
  delete ia;
}
