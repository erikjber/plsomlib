/**
 * 
 */
package org.plsomlib.metrics;

/**
 * An euclidean distance implementation that weights its distance based on a
 * weight array.
 * 
 * @author Erik Berglund
 * 
 */
public class WeightedEuclideanMetric extends EuclideanMetric
{
	private static final long serialVersionUID = -4670742056189027432L;
	private double[] weights;

	/**
	 * Set the weights to use when measuring the distance.
	 * 
	 * @param weights
	 */
	public void setWeights(final double[] weights)
	{
		this.weights = weights;
	}

	@Override
	public double getDistance(final double[] a, final double[] b)
	{
		double res = 0;
		for (int x = 0; x < a.length; x++)
		{
			res += (a[x] - b[x]) * (a[x] - b[x]) * weights[x];
		}
		return Math.sqrt(res);
	}

	@Override
	public double getDistance(final int[] a, final int[] b)
	{
		double res = 0;
		for (int x = 0; x < a.length; x++)
		{
			res += (a[x] - b[x]) * (a[x] - b[x]) * weights[x];
		}
		return Math.sqrt(res);
	}

}
