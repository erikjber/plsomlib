/**
 * Created by Erik Berglund on 2018-04-25.
 */

#include <include/gtest/gtest.h>
#include <util/DiameterBuffer.h>
#include "diameterbuffer_check.h"

TEST(diameterbuffer_test, simpletest)
{
  DiameterBuffer* diameterBuffer = new DiameterBuffer();
  vector<double> data;
  data.push_back(1);
  data.push_back(2);
  diameterBuffer->updateBuffer(data);
  double maxdim = diameterBuffer->getMaxDiameter();
  ASSERT_FLOAT_EQ(0, maxdim);
  data.clear();
  data.push_back(0);
  data.push_back(0);
  diameterBuffer->updateBuffer(data);
  maxdim = diameterBuffer->getMaxDiameter();
  ASSERT_FLOAT_EQ(sqrt(5), maxdim);
  delete diameterBuffer;
}
