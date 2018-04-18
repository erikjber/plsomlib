/**
 * Created by Erik Berglund on 2018-04-18.
 */

#ifndef CPP_MAPBASEIMPL_H
#define CPP_MAPBASEIMPL_H


#include <random>
#include <cstring>
#include "MapBase.h"
#include "util/IterativeArray.h"
#include "metrics/EuclideanMetric.h"
#include "neighbourhood/GaussianNeighbourhoodFunction.h"

/**
 * Default implementation of the MapBase interface. Classes can extend this
 * class instead of implementing MapBase for ease of use.
 *
 * @author Erik Berglund
 */
class MapBaseImpl : MapBase
{
private:
  // A temporary local copy of the last input vector.
  vector<double> input;

  /**
   * The input size this SOM expects.
   */
  int inputDimension;


  IterativeArray<vector<double>> weights;

  /**
   * Temporary winner location pointer.
   */
  vector<int> winner;

  /**
   * The distance metric used in the input space.
   */
  EuclideanMetric inputMetric;

  /**
   * The distance metric used in the output space.
   */
  EuclideanMetric outputMetric;

  /**
   * A neighbourhood function.
   */
  GaussianNeighbourhoodFunction nhFunction;

  std::default_random_engine rand;
  std::normal_distribution<double> norm;

public:

  /**
   * Class constructor.
   *
   * @param inputSize
   *            the number of inputs to the map.
   * @param outputDimensions
   *            the number of and size of output dimensions.
   */
  MapBaseImpl(int inputSize, vector<int> outputDimensions) : inputDimension(inputSize),
                                                             weights{IterativeArray<vector<double>>(outputDimensions)},
                                                             norm(0, 0.1)
  {
    input = vector<double>();
    initWeights();
  }


  /**
   * Create the weights. All weights are initially set to random values
   * between -1 and 1.
   */
  void initWeights()
  {
    for (int x = 0; x < weights.getCount(); x++)
    {
      vector<double> tmpData;
      // initialize weight to random values
      for (int t = 0; t < inputDimension; t++)
      {
        tmpData.push_back(norm(rand));
      }
      weights.setValueAtOffset(tmpData, x);
    }
  }

  /**
   * @return the inputDimension
   */
  int getInputDimension()
  {
    return inputDimension;
  }

  /**
   * @param inputDimension
   *            the inputDimension to set
   */
  void setInputDimension(int inputDimension)
  {
    this->inputDimension = inputDimension;
  }

  /**
   * Access the underlying weight storage implementation.
   * @return the weights
   */
  IterativeArray<vector<double>> getWeights()
  {
    return weights;
  }


  /**
   * Method for retreiving the weight vector associated with a given node.
   *
   * @param location
   *            a vector indicating the position of the desired node along
   *            each of the SOM's dimensions.
   * @return the weight vector of the node at the given location, any changes
   *         will to the returned object will be reflected in the node.
   */
  vector<double> getWeights(vector<int> location)
  {
    return weights.getValue(location);
  }

  /**
   * Method for changing the weight vector associated with a given node.
   *
   * @param location
   *            a vector indicating the position of the desired node along
   *            each of the SOM's dimensions.
   * @param newWeights
   *            the new weights.
   */
  void setWeights(vector<double> newWeights, vector<int> location)
  {
    weights.setValue(newWeights, location);
  }


  /**
   * Set the input of this SOM. The input can be classified or used for
   * training.
   *
   * @param input
   *            the input vector, also called data vector.
   * @see #classify()
   * @see #train()
   */
  virtual void setInput(vector<double>& input)
  {
    this->input = input;
  }

  /**
   * Classify the most recently applied input according to this SOM.
   *
   * @see #setInput(double [] input)
   */
  virtual vector<int> classify()
  {
    vector<int> res;
    res.resize(weights.getDimensions().size());
    findMinDist(res);
    return res;
  }


  /**
   * Classify the supplied input according to this SOM. This is a convenience
   * method, used where one wants to be sure that no other thread modifies the
   * input value before it is classified.
   */
  virtual vector<int> classify(vector<double> input)
  {
    setInput(input);
    return classify();
  }


  /**
   * Train this map using the most recently applied input. Must be overridden
   * by subclasses.
   * Updates the Winner property.
   */
  virtual void train() = 0;

  /**
   * Train the map using the supplied input.
   *
   * @param input
   *            the input to use for training.
   */
  virtual void train(vector<double> input)
  {
    setInput(input);
    train();
  }


  /**
   * @see org.plsomlib.MapBase#getOutputDimensions()
   */
  vector<int> getOutputDimensions()
  {
    return weights.getDimensions();
  }

  vector<double>& getInput()
  {
    return input;
  }

  vector<int> getWinner()
  {
    return winner;
  }

  void setWinner(vector<int>& winner)
  {
    this->winner = winner;
  }


protected:

  /**
   * @param weights the weights to set
   */
  void setWeights(IterativeArray<vector<double>> weights)
  {
    this->weights = weights;
  }

  /**
   * Calculate the value of the neighbourhood function for the node located at
   * loc, given the location of the winner and the neighbourhood size.
   *
   * @param loc
   *            the location of the node to calculate the neighbourhood for.
   * @param winner
   *            the location of the winner.
   * @param nhSize
   *            the neighbourhood size.
   */
  double getNeighbourhoodScaling(vector<int> loc, vector<int> winner, double nhSize)
  {
    double dist = outputMetric.getDistance(loc, winner);
    return nhFunction.getScaling(dist, nhSize);
  }

  /**
   * Helper function for classify. Recursively compares all weight vectors to
   * the input.
   */
  double findMinDist(vector<int>& res)
  {
    double minDist = numeric_limits<double>::max();
    int winner = 0;
    for (int x = 0; x < getWeights().getCount(); x++)
    {
      vector<double> weight = getWeights().getValueFromOffset(x);
      double tmp = inputMetric.getDistance(weight, getInput());
      if (tmp < minDist)
      {
        minDist = tmp;
        winner = x;
      }
    }
    vector<int> winnerPos = getWeights().getPosition(winner);
    for(int x = 0;x<winnerPos.size();x++)
    {
      res[x]=winnerPos[x];
    }
    return minDist;
  }

};


#endif //CPP_MAPBASEIMPL_H
