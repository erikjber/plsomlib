/**
 * Created by Erik Berglund on 2018-04-25.
 */



#include <include/gtest/gtest.h>
#include <PLSOM2.h>
#include "plsom2_check.h"

TEST(plsom2, simpletest) {
  //create test data
  std::default_random_engine rand;
  std::uniform_real_distribution<double> norm;
  vector<vector<double>> data;
  for(int x = 0; x < 10000;x++)
  {
    vector<double>tmp;
    tmp.push_back(norm(rand));
    tmp.push_back(norm(rand));
    data.push_back(tmp);
  }
  vector<double>input;
  input.push_back(0.5);
  input.push_back(0.5);

  PLSOM2 map(2,{20,20});
  map.setNeighbourhoodRange(30);
  for(int x = 0;x<data.size();x++)
  {
    map.train(data[x]);
  }

  vector<int> classification = map.classify(input);
  // An input in the middle of the input space should activate a node in the middle of the output space
  ASSERT_EQ(9,classification[0]);
  ASSERT_EQ(9,classification[1]);
}
