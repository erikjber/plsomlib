/**
 * 
 */
package org.plsomlib.metrics;

/**
 * Provides a distance measure between two double or int vectors of eaqual
 * length. The output of this metric is the same as squaring the output of
 * EuclidenMetric, but this method is faster since it does not rely on the
 * Math.sqrt(...) function.
 * 
 * @author Erik Berglund
 * 
 */
public class SquaredEuclideanMetric extends MetricImpl
{
	private static final long serialVersionUID = 6198512752402861550L;

	/**
     * Calculate the squared n-dimensional Euclidean distance between a and b,
     * where the length of a and b must be equal to n.
     * 
     * @param a
     *            the first int array to calculate the distance from.
     * @param b
     *            the second int array to calculate the distance to.
     * 
     */
    public double getDistance(final int[] a, final int[] b)
    {
        double res = 0;
        for (int x = 0; x < a.length; x++)
        {
            res += (a[x] - b[x]) * (a[x] - b[x]);
        }
        return res;
    }

    /**
     * Calculate the squared n-dimensional Euclidean distance between a and b,
     * where the length of a and b must be equal to n.
     * 
     * @param a
     *            the first double array to calculate the distance from.
     * @param b
     *            the second double array to calculate the distance to.
     * 
     * @see org.plsomlib.metrics.Metric#getDistance(double[], double[])
     */
    public double getDistance(final double[] a, final double[] b)
    {
        double res = 0;
        for (int x = 0; x < a.length; x++)
        {
            res += (a[x] - b[x]) * (a[x] - b[x]);
        }
        return res;
    }
    

}
