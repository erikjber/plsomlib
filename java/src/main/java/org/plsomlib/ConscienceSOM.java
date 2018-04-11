package org.plsomlib;

import org.plsomlib.util.IterativeArray;

/**
 * 
 * Class that implements the "conscience" algorithm proposed by Duane DeSieno in
 * IEEE International Conference on Neural Networks, 1988, vol. 1 pp. 117-124.
 * 
 * 
 * @author Erik Berglund
 * 
 */
public class ConscienceSOM extends SOM
{
	private static final long serialVersionUID = -5733673008074722911L;
	
	//Extra parameters
	private double B = 0.0001;//as indicated by the original paper
	private double C = 10;//as indicated by the original paper

	private IterativeArray<Double> probToWin;

	/**
	 * Class constructor. Creates a map with euclidean input space, and
	 * rectangular euclidean output space.
	 * 
	 * @param inputSize
	 *            the number of inputs to the SOM.
	 * @param outputDimensions
	 *            the number of and size of output dimensions.
	 */
	public ConscienceSOM(int inputSize, int... outputDimensions)
	{
		super(inputSize, outputDimensions);
	}

	/**
	 * @see org.plsomlib.MapBaseImpl#initWeights()
	 */
	@Override
	public void initWeights()
	{
		super.initWeights();
		//create arrays for p, the fraction of time that a given processing element wins the competition
		probToWin = new IterativeArray<Double>(this.getOutputDimensions());
		//set all the probabilities to 1
		
        for (int x = 0; x < probToWin.getCount(); x++)
        {
        	probToWin.setValueAtOffset(1.0/probToWin.getCount(), x);
        }
	}

	/**
	 * Update the posteri winning probabilities, given the current winner.
	 * 
	 * @param winner
	 */
	protected void updateProbabilityToWin(int winner)
	{
		int numNodes = this.probToWin.getCount();
		for (int x = 0; x < numNodes; x++)
		{
			double y = 0;
			if (x == winner)
			{
				y = 1;
			}
			Double old = this.probToWin.getValueFromOffset(x);
			old += B * (y - old);
			this.probToWin.setValueAtOffset(old, x);
		}
	}

	/**
	 * @see org.plsomlib.SOM#updateWeights()
	 */
	@Override
	protected void updateWeights()
	{
		Object[] data = this.getWeights().toArray();
		for (int x = 0; x < data.length; x++)
		{
			// calculate the neighbourhood scaling, multiply by learning rate
			double anhc = getLearningRate() * getNeighbourhoodScaling(getWeights().getPosition(x), getWinner(), getNeighbourhoodSize());
			// get the weight vector
			double[] weight = (double[]) data[x];
			// update the weights
			for (int wIndex = 0; wIndex < weight.length; wIndex++)
			{
				weight[wIndex] += anhc * (getInput()[wIndex] - weight[wIndex]);
			}
		}
	}

	/**
	 * Calculate the bias conscience based on the probability that a node is a
	 * winner.
	 * 
	 * @param prob
	 * @return
	 */
	protected double getConscienceBias(double prob)
	{
		return C * ((1.0 / getWeights().getCount()) - prob);
	}

	/**
	 * @see org.plsomlib.MapBaseImpl#findMinDist(int[])
	 */
	@Override
	protected double findMinDist(int[] res)
	{
		double minDist = Double.POSITIVE_INFINITY;
		Object[] data = getWeights().toArray();
		int winner = 0;
		for (int x = 0; x < data.length; x++)
		{
			//modified from the ordinary SOM, the distance is biased by the conscience
			double distance = getInputMetric().getDistance((double[]) data[x], getInput());
			distance -= getConscienceBias(probToWin.getValueFromOffset(x));
			if (distance < minDist)
			{
				minDist = distance;
				winner = x;
			}
		}
		System.arraycopy(getWeights().getPosition(winner), 0, res, 0, res.length);
		updateProbabilityToWin(winner);
		return minDist;
	}

}
