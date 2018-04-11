package org.plsomlib.util;

import org.plsomlib.metrics.SquaredEuclideanMetric;

/**
 * Measures the distance between two IterativeArray<double[]> instances.
 * 
 * @author Erik Berglund
 */
public class ArrayDistance
{
	private static SquaredEuclideanMetric metric = new SquaredEuclideanMetric();
	
	public static double distance(IterativeArray<double[]> first, IterativeArray<double[]> second)
	{
		double dist = 0;
		Object [] a = first.toArray();
		Object [] b = second.toArray();
		if(a.length!=b.length)
		{
			throw new IllegalArgumentException("Arrays of different size.");
		}
		for(int x = 0;x< a.length;x++)
		{
			dist += metric.getDistance((double[])a[x], (double[])b[x]);
		}		
		return Math.sqrt(dist);		
	}
}
