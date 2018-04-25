/**
 * Created by Erik Berglund on 2018-04-11.
 */

#include <cmath>
#include "GaussianNeighbourhoodFunction.h"


double GaussianNeighbourhoodFunction::getScaling(double distance, double neighbourhoodSize)
{
  double nhSquared = neighbourhoodSize * neighbourhoodSize;
  if (nhSquared == 0.0) {
    //avoid NaN errors
    if (distance == 0.0) {
      return 1.0;
    } else {
      return 0.0;
    }
  }
  return exp(-(distance * distance) / (nhSquared));

}
