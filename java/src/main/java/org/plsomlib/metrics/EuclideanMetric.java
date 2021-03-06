package org.plsomlib.metrics;


/**
 * Measures the distance in an euclidean space.
 * 
 * @author Erik Berglund
 */
public class EuclideanMetric extends MetricImpl
{    
	private static final long serialVersionUID = 1294776388419859261L;

	/**
     * Calculate the n-dimensional Euclidean distance between a and b, where the length of a and b must be
     * equal to n.
     * @param a the first int array to calculate the distance from.
     * @param b the second int array to calculate the distance to.
     * 
     */
    public double getDistance(final int[] a,final int[] b)
    {
        double res = 0;
        for ( int x = 0;x < a.length;x++ )
        {
            res += (a[x]-b[x])*(a[x]-b[x]);
        }
        return Math.sqrt(res);
    }
    
    /**
     * Calculate the n-dimensional Euclidean distance between a and b, where the length of a and b must be
     * equal to n.
     * @param a the first double array to calculate the distance from.
     * @param b the second double array to calculate the distance to.
     * 
     * @see org.plsomlib.metrics.Metric#getDistance(double[], double[])
     */
    public double getDistance(final double[] a,final double[] b)
    {
        double res = 0;
        for ( int x = 0;x < a.length;x++ )
        {
            res += (a[x]-b[x])*(a[x]-b[x]);
        }
        return Math.sqrt(res);
    }
}
