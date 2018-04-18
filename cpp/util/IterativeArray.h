/**
 * Created by Erik Berglund on 2018-04-18.
 */

#ifndef CPP_ITERATIVEARRAY_H
#define CPP_ITERATIVEARRAY_H


#include <iterator>
#include <vector>

using namespace std;

template<typename V>

/**
 * An array with an arbitrary number of dimensions.
 * In mathematical term, this is an implementation of a tensor with arbitrary rank.
 * Any type of object can be stored and retrieved.
 * Each stored object is associated with an n-dimensional position vector,
 * where n is the rank of the storage tensor.
 *
 * @author Erik Berglund
 *
 * @param <V> the type of data to store in the array
 */
class IterativeArray
{
private:
  vector<int> dimensions;
  vector<int> factors;
  vector<V> data;
  vector<vector<int>> positions;
  int offset;
  int count;

  /**
   * Copying constructor.
   */
  IterativeArray(vector<int> dimensions,
                 vector<int> factors,
                 vector<vector<int>> positions,
                 vector<V> data) : dimensions{dimensions}, factors{factors},
                                   positions{positions}, data{data}
  {
  }


public:
  using iterator = V*;

  iterator begin() const
  {
    return &data[0];
  };

  iterator end() const
  {
    return &data[data.size()];
  }

  IterativeArray(vector<int> dimens);

  int getOffset(vector<int> position) const;

  vector<int> getPosition(int offset);

  vector<int> getDimensions() const
  {
    return this->dimensions;
  }


  /**
   * Return the value of at a given offset.
   * The result is the same as returned by getValue(getPosition(offset)),
   * but completes faster since it is only an array index lookup.
   *
   * No bounds checking is performed.
   *
   */
  V getValueFromOffset(int offset)
  {
    return data[offset];
  }

  /**
   * Set the the value at a particular offset.
   * The change in the object is the same as for calling setValue(value,getPosition(offset)),
   * but completes faster since it is only an array index lookup.
   */
  void setValueAtOffset(V value, int offset)
  {
    data[offset] = value;
  }

  V getValue(vector<int> position)
  {
    return data[getOffset(position)];
  }

  void setValue(V value, vector<int> position)
  {
    data[getOffset(position)] = value;
  }

  /**
   * Return the number of entities in this IterativeArray.
   */
  int getCount()
  {
    return count;
  }

};


#endif //CPP_ITERATIVEARRAY_H
