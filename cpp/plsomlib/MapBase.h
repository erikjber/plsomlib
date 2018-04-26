/**
 * Created by Erik Berglund on 2018-04-18.
 */

#ifndef CPP_MAPBASE_H
#define CPP_MAPBASE_H

#include <vector>


using namespace std;

/**
* The base interface for mapping algorithms like SOM and PLSOM.
*
*
* @author Erik Berglund
*/
class MapBase
{

  /**
   * Method for retreiving the weight vector associated with a given node.
   * @param location a vector indicating the position of the desired node along each of the
   * SOM's dimensions.
   * @return the weight vector of the node at the given location, any changes will to the returned
   * object will be reflected in the node.
   */
  virtual vector<double>& getWeights (vector<int> location) = 0;

  /**
   * Method for changing the weight vector associated with a given node.
   * @param location a vector indicating the position of the desired node along each of the
   * map's dimensions.
   * @param newWeights the new weights.
   */
  virtual void setWeights (vector<double>& newWeights, vector<int> location) = 0;

  /**
   * Classify the most recently applied input according to this SOM.
   * @see #setInput(vector<double> input)
   */
  virtual vector<int> classify () = 0;


  /**
   * Classify the supplied input according to this SOM.
   * This is a convenience method, used where one wants to be
   * sure that no other thread modifies the input value before it is
   * classified.
   */
  virtual vector<int> classify (vector<double>& input) = 0;

  /**
   * Set the input of this map. The input can be classified
   * or used for training.
   * @param input the input vector, also called data vector.
   * @see #classify()
   * @see #train()
   */
  virtual void setInput (vector<double>& input) = 0;


  /**
   * Train this map using the most recently applied input.
   */
  virtual void train () = 0;


  /**
   * Train the map using the supplied input.
   * @param input the input to use for training.
   */
  virtual void train (vector<double>& input) = 0;


  /**
   * Get the number and size of the output dimensions, as an integer array.
   * Example:
   * For a 2-dimensional map, 20 nodes wide and 15 nodes high, this function will return
   * new int[] {20,15};
   *
   * @return the output dimensions.
   */
  virtual vector<int> getOutputDimensions () = 0;

  /**
   * Get the input dimensions of the map.
   * This can be used to ensure that the presented input is of the correct dimensionality.
   *
   * @return the number of input dimensions.
   */
  virtual int getInputDimension () = 0;

};


#endif //CPP_MAPBASE_H
