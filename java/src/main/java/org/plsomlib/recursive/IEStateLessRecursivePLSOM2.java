package org.plsomlib.recursive;

import org.plsomlib.metrics.WeightedEuclideanMetric;
import org.plsomlib.util.IterativeArray;

/**
 * @author Erik Berglund
 * 
 */
public class IEStateLessRecursivePLSOM2 extends StateLessRecursivePLSOM2
{
	private static final long serialVersionUID = 8309747554287736672L;
	private IterativeArray<double[]> importanceScaleDirect;
	private double[] minDirect;
	private double[] maxDirect;
	private double[] range;
	private IterativeArray<double[]> importanceScaleRecursive;
	private double[] minRecursive;
	private double[] maxRecursive;
	private double importanceScaling = 0.00005;
	private double[] recursiveRange;

	private static final double DEFAULT_IMPORTANCE = 1;

	/**
	 * @param alpha
	 * @param inputSize
	 * @param outputDimensions
	 */
	public IEStateLessRecursivePLSOM2(double alpha, int inputSize,
			int... outputDimensions)
	{
		super(alpha, inputSize, outputDimensions);
		this.setInputMetric(new WeightedEuclideanMetric());
		// create new importance scalings
		importanceScaleDirect = new IterativeArray<double[]>(outputDimensions);
		importanceScaleRecursive = new IterativeArray<double[]>(
				outputDimensions);

		// populate scalings
		for (int x = 0; x < importanceScaleDirect.getCount(); x++)
		{
			// create a new double array, fill it with ones
			double[] importance = new double[inputSize];
			for (int t = 0; t < inputSize; t++)
			{
				importance[t] = DEFAULT_IMPORTANCE;
			}
			importanceScaleDirect.setValueAtOffset(importance, x);
		}
		for (int x = 0; x < importanceScaleRecursive.getCount(); x++)
		{
			// create a new double array, fill it with ones
			double[] importance = new double[this.getWeights().getCount()];
			for (int t = 0; t < importance.length; t++)
			{
				importance[t] = DEFAULT_IMPORTANCE;
			}
			importanceScaleRecursive.setValueAtOffset(importance, x);
		}
		minDirect = new double[inputSize];
		maxDirect = new double[minDirect.length];
		range = new double[minDirect.length];
		for (int x = 0; x < minDirect.length; x++)
		{
			minDirect[x] = Double.POSITIVE_INFINITY;
			maxDirect[x] = Double.NEGATIVE_INFINITY;
		}
		minRecursive = new double[getWeights().getCount()];
		maxRecursive = new double[minRecursive.length];
		recursiveRange = new double[minRecursive.length];
		for (int x = 0; x < minRecursive.length; x++)
		{
			minRecursive[x] = Double.POSITIVE_INFINITY;
			maxRecursive[x] = Double.NEGATIVE_INFINITY;
		}
	}

	@Override
	public int[] classify()
	{
		calculateRecursiveMaxMin();
		return super.classify();
	}

	/**
	 * Iterate over the recursive excitations and find the max/min value for
	 * each.
	 * 
	 */
	protected void calculateRecursiveMaxMin()
	{
		if (getExcitations() != null)
		{
			// calculate the range of the input
			for (int x = 0; x < minRecursive.length; x++)
			{
				maxRecursive[x] = getExcitations()[x] > maxRecursive[x] ? getExcitations()[x]
						: maxRecursive[x];
				minRecursive[x] = getExcitations()[x] < minRecursive[x] ? getExcitations()[x]
						: minRecursive[x];
				recursiveRange[x]=maxRecursive[x]-minRecursive[x];
			}
		}
	}

	@Override
	protected double calculateRecursiveDistance(int x)
	{
		double recDistance = 0;
		if (getExcitations() != null)
		{
			// copy the importance weights to the input metric
			((WeightedEuclideanMetric) getInputMetric())
					.setWeights(importanceScaleRecursive.getValueFromOffset(x));
			recDistance = (1 - alpha)
					* this.getInputMetric().getDistance(getExcitations(),
							(double[]) recursiveWeights.toArray()[x]);
		}
		return recDistance;
	}

	@Override
	protected double calculateInputDistance(int x)
	{
		double inputDistance = 0;
		// if we have input (non-predict mode) calculate the input
		// distance
		if (!this.isPredict())
		{
			Object[] weights = getWeights().toArray();
			// copy the importance weights to the input metric
			double[] importance = importanceScaleDirect.getValueFromOffset(x)
					.clone();
			// scale the importance according to the range of the input
			for (int t = 0; t < importance.length; t++)
			{
				if (range[t] > 0)
				{
					importance[t] /= range[t];
				}
			}
			((WeightedEuclideanMetric) getInputMetric()).setWeights(importance);
			inputDistance = alpha
					* this.getInputMetric().getDistance(getInput(),
							(double[]) weights[x]);
		}
		return inputDistance;
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
			maxDirect[x] = input[x] > maxDirect[x] ? input[x] : maxDirect[x];
			minDirect[x] = input[x] < minDirect[x] ? input[x] : minDirect[x];
			range[x] = maxDirect[x] - minDirect[x];
		}
		super.setInput(input);
	}

	/**
	 * Approximate the fuzzy truth value of two fuzzy values, x and y. Does no
	 * bounds or range checking at all.
	 * 
	 * @param x
	 *            must be in the range [0,1].
	 * @param y
	 *            must be in the range [0,1].
	 * @return
	 */
	private double fuzzyXor(final double x, final double y)
	{
		double a = (1 - x) < y ? (1 - x) : y;
		double b = x < (1 - y) ? x : (1 - y);
		return a > b ? a : b;
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
			double nhScale = getNeighbourhoodScaling(getWeights()
					.getPosition(x), getWinner(), getNeighbourhoodSize());

			// update the non-recursive weights, importance
			if (!isPredict())
			{
				// get the weight vector
				double[] weight = getWeights().getValueFromOffset(x);
				// get the scaling vectors
				double[] scaling = this.importanceScaleDirect
						.getValueFromOffset(x);
				// update the weights
				updateWeightsAndScaling(weight, scaling, range,
						getInput(), nhScale);
			}

			// update the recursive weights, importance
			if (getExcitations() != null)
			{
				// get the weight vector
				double[] weight = recursiveWeights.getValueFromOffset(x);
				// get the scaling vectors
				double[] scaling = this.importanceScaleRecursive
						.getValueFromOffset(x);
				// update the weight
				updateWeightsAndScaling(weight, scaling, recursiveRange, getExcitations(), nhScale);
			}
		}
	}

	/**
	 * Helper function for updateWeights(). Calculates the new weights based on
	 * the old weights and the data. The scaling is calculated based on the old
	 * weights, the data and the range of the data.
	 * 
	 * @param weight
	 *            the old weights, will contain the new weights upon completion.
	 * @param scaling
	 *            the scaling array to update.
	 * @param range the data range.
	 * 
	 * @param data
	 *            the data to use for training.
	 * @param nhScale
	 *            the scale of the neighbourhood at the node we are updating.
	 */
	protected void updateWeightsAndScaling(double[] weight, double[] scaling,
			final double[] range, final double[] data,
			final double nhScale)
	{
		final double scaledNh = nhScale * importanceScaling;
		final double anhc = getEpsilon() * nhScale * getLearningScale();
		// update the weights
		for (int wIndex = 0; wIndex < weight.length; wIndex++)
		{
			// get the difference between the input and the weight
			double diff = data[wIndex] - weight[wIndex];
			// update the weight
			weight[wIndex] += anhc * diff * scaling[wIndex];

			// update the importance scaling:
			// normalise the difference to [0,1] interval
			diff = Math.abs(diff)/range[wIndex];

			if (diff >= 0 && diff <= 1)
			{
				// time-integrate
				scaling[wIndex] *= 1 - scaledNh;
				// find out how well diff and nhScale correlates
				scaling[wIndex] += scaledNh * fuzzyXor(diff, nhScale);
			}
		}
	}

	/**
	 * Set the importance scaling factor. This indicates how sensitive the
	 * importance scaling will be to differences between the weight vector and
	 * the input.
	 * 
	 * Higher value means that the importance scaling will react quicker.
	 * 
	 * 1 (the maximum) indicates instant reaction, while 0 (the minimum)
	 * indicates no reaction at all.
	 * 
	 * @param importanceScaling
	 */
	public void setImportanceScalingFactor(double importanceScaling)
	{
		if (importanceScaling > 1 || importanceScaling < 0)
		{
			throw new IllegalArgumentException(
					"Importance scaling factor must be between 0 and 1, inclusive.");
		}
		this.importanceScaling = importanceScaling;
	}

	/**
	 * @see org.plsomlib.recursive.StateLessRecursivePLSOM2#train()
	 */
	@Override
	public void train()
	{
		calculateRecursiveMaxMin();
		super.train();
	}

	/**
	 * @return the importanceScaleDirect
	 */
	public IterativeArray<double[]> getImportanceScaleDirect()
	{
		return importanceScaleDirect;
	}

	/**
	 * @return the importanceScaleRecursive
	 */
	public IterativeArray<double[]> getImportanceScaleRecursive()
	{
		return importanceScaleRecursive;
	}
}
