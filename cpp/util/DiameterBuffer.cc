/**
 * Created by Erik Berglund on 2018-04-18.
 */

#include <limits>
#include "DiameterBuffer.h"


void DiameterBuffer::updateBuffer(vector<double> data)
{
  // calculate the distance from the new input to all inputs in the buffer
  // find the largest distance between the input and any entry in the buffer
  // as well as the entry in the buffer that is closest to the input
  int minDistIndex = 0;
  double minDist = numeric_limits<double>::max();
  double maxNewDist = 0;
  for(int index = 0;index<buffer.size();index++)
  {
    vector<double> &n = buffer.at(index);
    double tmp = bufferMetric.getDistance(data,n);
    if(tmp < minDist)
    {
      // Closest buffer entry so far.
      minDist = tmp;
      minDistIndex = index;
    }
    if(tmp > maxNewDist)
    {
      // Largest distance so far.
      maxNewDist = tmp;
    }
  }
  // Check if we've received a 'distant' input
  if(maxNewDist > maxDiameter)
  {
    // Add the new input to the buffer
    maxDiameter = maxNewDist;
    buffer.push_back(data);
    // Keep the buffer to the set size...
    if(buffer.size() > (data.size()+1))
    {
      // ... by removing the buffer entry that is closest to the new input
      buffer.erase(buffer.begin()+minDistIndex);
    }
  }
}
