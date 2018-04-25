/**
 * Created by Erik Berglund on 2018-04-11.
 */

#ifndef CPP_EUCLIDEANMETRIC_H
#define CPP_EUCLIDEANMETRIC_H

#include <vector>
#include <cmath>

using namespace std;

class EuclideanMetric
{
public:
  /**
   * Get the eucliedean distance between two points in n-space.
   * @param a a vector of length n.
   * @param b a vector of length n
   */
  double getDistance(vector<int> a, vector<int> b);
  /**
   * Get the eucliedean distance between two points in n-space.
   * @param a a vector of length n.
   * @param b a vector of length n
   */
  double getDistance(vector<double> a, vector<double> b);
};


#endif //CPP_EUCLIDEANMETRIC_H
