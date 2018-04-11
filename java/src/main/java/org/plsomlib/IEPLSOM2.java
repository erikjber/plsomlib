/**
 * 
 */
package org.plsomlib;

import org.plsomlib.metrics.EuclideanMetric;
import org.plsomlib.metrics.Metric;
import org.plsomlib.metrics.WeightedEuclideanMetric;
import org.plsomlib.neighbourhood.GaussianNeighbourhoodFunction;
import org.plsomlib.util.IterativeArray;

/**
 * The ImportanceEstimating PLSOM2. Input dimensions that correspond poorly with
 * the win/loss situation of a given node gets its importance decreased.
 * 
 * 
 * @author Erik Berglund
 * 
 */
public class IEPLSOM2 extends PLSOM2
{
	private static final long serialVersionUID = -5600902825894975817L;
	private IterativeArray<double[]> importanceScale;
	private double[] min;
	private double[] max;

	private final static double TIME_INTEGRATION_CONSTANT = 0.05;

	public IEPLSOM2(int inputSize, int... outputDimensions)
	{
		super(new WeightedEuclideanMetric(), new EuclideanMetric(), new GaussianNeighbourhoodFunction(), inputSize, outputDimensions);
		// create new importance scalings
		importanceScale = new IterativeArray<double[]>(outputDimensions);

		// populate scalings
		for (int x = 0; x < importanceScale.getCount(); x++)
		{
			// create a new double array, fill it with ones
			double[] importance = new double[inputSize];
			for (int t = 0; t < inputSize; t++)
			{
				importance[t] = 1;
			}
			importanceScale.setValueAtOffset(importance, x);
		}
		min = new double[inputSize];
		max = new double[inputSize];
		for (int x = 0; x < inputSize; x++)
		{
			min[x] = Double.POSITIVE_INFINITY;
			max[x] = Double.NEGATIVE_INFINITY;
		}
	}

	/**
	 * @see org.plsomlib.PLSOM2#setInput(double[])
	 */
	@Override
	public void setInput(double[] input)
	{
		// calculate the range of the input
		for (int x = 0; x < input.length; x++)
		{
			max[x] = input[x] > max[x] ? input[x] : max[x];
			min[x] = input[x] < min[x] ? input[x] : min[x];

		}
		super.setInput(input);
	}

	/**
	 * @see org.plsomlib.MapBaseImpl#findMinDist(int[])
	 */
	@Override
	protected double findMinDist(int[] res)
	{
		double minDist = Double.POSITIVE_INFINITY;
		int winner = 0;
		for (int x = 0; x < getWeights().getCount(); x++)
		{
			// give the importance weights to the input metric
			((WeightedEuclideanMetric) getInputMetric()).setWeights(importanceScale.getValueFromOffset(x));
			// continue with ordinary weight difference calculation method
			if (getInputMetric().getDistance(getWeights().getValueFromOffset(x), getInput()) < minDist)
			{
				minDist = getInputMetric().getDistance(getWeights().getValueFromOffset(x), getInput());
				winner = x;
			}
		}
		System.arraycopy(getWeights().getPosition(winner), 0, res, 0, res.length);
		return minDist;
	}

	/**
	 * Standard weight update algorithm with fit-estimating addition. The
	 * relative change of each weight is estimated, and the relative importance
	 * is altered accordingly.
	 * 
	 * 
	 * @see org.plsomlib.PLSOM#updateWeights()
	 */
	@Override
	protected void updateWeights()
	{
		for (int x = 0; x < getWeights().getCount(); x++)
		{
			// calculate the neighbourhood scaling, multiply by epsilon
			double nhScale = getNeighbourhoodScaling(getWeights().getPosition(x), getWinner(), getNeighbourhoodSize());
			double scaledNh = TIME_INTEGRATION_CONSTANT * nhScale;
			double anhc = getEpsilon() * nhScale;
			// get the weight vector
			double[] weight = getWeights().getValueFromOffset(x);
			// get the scaling vectors
			double[] scaling = this.importanceScale.getValueFromOffset(x);
			// update the weights
			for (int wIndex = 0; wIndex < weight.length; wIndex++)
			{
				// get the difference between the input and the weight
				double diff = getInput()[wIndex] - weight[wIndex];
				// update the weight, taking the scaling into account
				weight[wIndex] += anhc * diff * scaling[wIndex];

				// update the importance:
				// Normalise the difference relative to the input range
				diff = Math.abs(diff);
				diff /= (max[wIndex] - min[wIndex]);
				if (diff >= 0 && diff <= 1)// don't update scaling if diff is
											// NaN, Inf or out of range
				{
					// find out how well diff and nhScale correlates
					double corr = fuzzyXor(diff, nhScale);
					// time-integrate
					scaling[wIndex] *= 1 - scaledNh;
					scaling[wIndex] += scaledNh * corr;
				}
			}
		}
	}

	/**
	 * Approximate the fuzzy truth value of two fuzzy values, x and y.
	 * Does no bounds or range checking at all.
	 * 
	 * @param x
	 *            must be in the range [0,1].
	 * @param y
	 *            must be in the range [0,1].
	 * @return
	 */
	public double fuzzyXor(final double x, final double y)
	{
		double a = (1-x)<y?(1-x):y;
		double b = x<(1-y)?x:(1-y);
		return a>b?a:b;
	}

	/**
	 * @see org.plsomlib.MapBaseImpl#setInputMetric(org.plsomlib.metrics.Metric)
	 */
	@Override
	public void setInputMetric(Metric inputMetric)
	{
		if (!(inputMetric instanceof WeightedEuclideanMetric))
		{
			throw new IllegalArgumentException("IEPLSOM2 only accepts WeightedEuclideanMetric input distances.");
		}
		super.setInputMetric(inputMetric);
	}

}
