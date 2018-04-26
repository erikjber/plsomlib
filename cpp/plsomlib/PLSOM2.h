/**
 * Created by Erik Berglund on 2018-04-18.
 */

#ifndef CPP_PLSOM2_H
#define CPP_PLSOM2_H

#include <cmath>
#include "util/DiameterBuffer.h"
#include "MapBaseImpl.h"

class PLSOM2 : MapBaseImpl
{
private:
  DiameterBuffer diameterBuffer;

  /**
   * The allowed range of the neighbourhood function.
   */
  double nhRange;

  double neighbourhoodSize;

  double rho;


  double epsilon;

  /**
   * The distance between the weight vector of the winning node and the input.
   */
  double lastError;
  const double EulerConstant = std::exp(1.0);

public:

  /**
   * @param inputSize the dimensionality of the input.
   * @param outputDimensions the number and size of output dimensions.
   */
  PLSOM2(int inputSize, std::initializer_list<int> outputDimensions) : MapBaseImpl(inputSize, outputDimensions)
  {
  }



  void setInput(vector<double>& input)
  {
    this->diameterBuffer.updateBuffer(input);
    MapBaseImpl::setInput(input);
  }

  void train()
  {
    auto winner = classify();
    setWinner(winner);
    // calculate epsilon
    if (getLastError() == 0)
    {
      setEpsilon(0);
    }
    else
    {
      setEpsilon(getLastError() / diameterBuffer.getMaxDiameter());
      if (getEpsilon() > 1)
      {
        setEpsilon(1);
      }
    }
    // calculate the neighbourhood size
    setNeighbourhoodSize(getNeighbourhoodRange() * log(1 + getEpsilon() * (EulerConstant - 1)));

    // calculate the new weights
    updateWeights();
  }

  void train(vector<double>& data)
  {
    MapBaseImpl::train(data);
  }



  /**
   * Set the allowed range for the neighbourhood size.
   *
   * @param nhRange
   *            the new neighbourhood size range.
   */
  void setNeighbourhoodRange(double nhRange)
  {
    this->nhRange = nhRange;
  }

  /**
   * Get the allowed ragne for the neighbourhood size.
   *
   * @return the new neighbourhood size range.
   */
  double getNeighbourhoodRange()
  {
    return nhRange;
  }

  /**
   * Classify the most recently applied input according to this SOM.
   *
   * @see #setInput(double [] input)
   */

  vector<int> classify()
  {
    vector<int> res;
    res.resize(getOutputDimensions().size());
    setLastError(findMinDist(res));
    setWinner(res);
    return res;
  }

  vector<int> classify(vector<double>& input)
  {
    return MapBaseImpl::classify(input);
  }



  double getNeighbourhoodSize()
  {
    return neighbourhoodSize;
  }

  double getEpsilon()
  {
    return epsilon;
  }

private:
  void setNeighbourhoodSize(double nhSize)
  {
    neighbourhoodSize = nhSize;
  }


protected:
  /**
   * @param epsilon
   *            the epsilon to set
   */
  void setEpsilon(double epsilon)
  {
    this->epsilon = epsilon;
  }

  /**
   * Recursively update all weights given the current winner.
   *
   */
  void updateWeights()
  {
    for (int x = 0; x < getWeights().getCount(); x++)
    {
      vector<double>& weight = getWeights().getPointerFromOffset(x);
      // calculate the neighbourhood scaling, multiply by epsilon
      double anhc = getEpsilon() * getNeighbourhoodScaling(getWeights().getPosition(x), getWinner(), getNeighbourhoodSize());
      // update the weights
      for (int wIndex = 0; wIndex < weight.size(); wIndex++)
      {
        (weight)[wIndex] += anhc * (getInput()[wIndex] - (weight)[wIndex]);
      }
    }
  }

  /**
   * @return the lastError
   */
  double getLastError()
  {
    return lastError;
  }

  /**
   * @param lastError
   *            the lastError to set
   */
  void setLastError(double lastError)
  {
    this->lastError = lastError;
  }

  /**
   * @return the rho
   */
  double getRho()
  {
    return rho;
  }

  /**
   * @param rho
   *            the rho to set
   */
  void setRho(double rho)
  {
    this->rho = rho;
  }

};


#endif //CPP_PLSOM2_H
