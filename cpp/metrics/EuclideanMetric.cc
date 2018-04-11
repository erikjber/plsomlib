/**
 * Created by Erik Berglund on 2018-04-11.
 */

#include "EuclideanMetric.h"

double EuclideanMetric::getDistance(vector<int> a, vector<int> b)
{
  double res = 0;
  for( int x = 0;x < a.size(); x++)
  {
    res += (a[x]-b[x])*(a[x]-b[x]);
  }
  return sqrt(res);
}

double EuclideanMetric::getDistance(vector<double> a, vector<double> b)
{
  double res = 0;
  for( int x = 0;x < a.size(); x++)
  {
    res += (a[x]-b[x])*(a[x]-b[x]);
  }
  return sqrt(res);
}
