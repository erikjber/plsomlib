/**
 * Created by Erik Berglund on 2018-04-18.
 */

#include "IterativeArray.h"

template<typename V>
IterativeArray<V>::IterativeArray(vector<int> dimens): dimensions{dimens}
{
  count = 1;
  for (int x = 0; x < dimensions.size(); x++)
  {
    factors.push_back(count);
    count *= dimensions[x];
  }
  data.resize(count);
  positions.resize(count);
}

template<typename V>
int IterativeArray<V>::getOffset(vector<int> position) const
{
  if (position.size() != dimensions.size())
  {
    string error = "Array of " + to_string(dimensions.size()) +
                   " dimensions cannot be accessed by an address of length "
                   + to_string(position.size()) + ".";
    throw error.c_str();
  }
  int res = 0;
  for (int x = 0; x < position.size(); x++) {
    if (position[x] >= dimensions[x]) {
      string error = "Index " + to_string(position[x]) + " out of bounds (" +
                     to_string(dimensions[x]) + ") at dimension " +
                     to_string(x);
    }
    res += position[x] * factors[x];
  }
  return res;
}

template<typename V>
vector<int> IterativeArray<V>::getPosition(int offset)
{
  vector<int> res = this->positions[offset];
  if(res.empty())
  {
    res.resize(factors.size());
    this->positions[offset] = res;
    for(int x = factors.size()-1;x>0;x--)
    {
      res[x] = offset/factors[x];
      offset -= res[x]*factors[x];
    }
    res[0] = offset/factors[0];
  }
  return res;
}
