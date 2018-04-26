/**
 * Created by Erik Berglund on 2018-04-18.
 */

#ifndef CPP_DIAMETERBUFFER_H
#define CPP_DIAMETERBUFFER_H

#include <vector>
#include "../metrics/EuclideanMetric.h"

using namespace std;

class DiameterBuffer
{
private:
  vector<vector<double>> buffer;
  EuclideanMetric bufferMetric;
  double maxDiameter = -1;

public:
  double getMaxDiameter()
  {
    return maxDiameter;
  }
  void setMaxDiameter(double newDiameter)
  {
    maxDiameter = newDiameter;
  }

  void updateBuffer(vector<double>& data);
};


#endif //CPP_DIAMETERBUFFER_H
