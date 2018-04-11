package org.plsomlib.util;

import java.io.Serializable;
import java.util.ArrayList;

import org.plsomlib.metrics.EuclideanMetric;
import org.plsomlib.metrics.Metric;

/**
 * Estimates the diameter of a buffer of n-dimensional arrays. Mathematically,
 * this object describes a set of points in n-space, and calculates the diameter
 * of this set.
 * 
 * 
 * @author Erik Berglund
 * 
 */
public class DiameterBuffer implements Serializable
{
	private static final long serialVersionUID = 1890525815177426503L;
	private ArrayList<double[]> buffer = new ArrayList<double[]>();
	private double maxDiameter = -1;
	private Metric bufferMetric = new EuclideanMetric();

	public double getMaxDiameter()
	{
		return this.maxDiameter;
	}

	/**
	 * Evaluate data for insertion into buffer. If the distance from data to any
	 * member of buffer is larger than maxBufferDistance, add data to buffer. If
	 * data is added to buffer and buffer.size() > maxSize, remove the element
	 * of buffer that is closest to data.
	 * 
	 * @param data
	 *            the new data to check to the buffer.
	 */
	public void updateBuffer(double[] data)
	{
		// calculate the distance from the new input to all inputs in the buffer
		// find the largest distance between the input and any entry in the
		// buffer
		// as well as the entry in the buffer that is closest to the input
		int minDistIndex = 0;
		int index = 0;
		double minDist = Double.MAX_VALUE;
		double maxNewDist = 0;
		for (double[] n : buffer)
		{
			double tmp = bufferMetric.getDistance(data, n);
			if (tmp < minDist)
			{
				// closest buffer entry so far
				minDist = tmp;
				minDistIndex = index;
			}
			if (tmp > maxNewDist)
			{
				// largest distance so far
				maxNewDist = tmp;
			}
			index++;
		}
		// check if we've received a 'distant' input
		if (maxNewDist > maxDiameter)
		{
			// add the new input to the buffer
			maxDiameter = maxNewDist;
			buffer.add(data.clone());
			// keep the buffer to the set size...
			if (buffer.size() > (data.length + 1))
			{
				// ...by removing the buffer entry that is closest to the new
				// input
				buffer.remove(minDistIndex);
			}
		}
	}

	/**
	 * Set the metric used to measure the distance between inputs. MaxDiameter
	 * will be calculated in this metric. If the metric is changed after the
	 * buffer has been in use the behaviour is undefined.
	 * 
	 * @param metric
	 */
	public void setMetric(Metric metric)
	{
		this.bufferMetric = metric;
	}

	/**
	 * Get the metric used for measuring distances between inputs.
	 * 
	 * @return
	 */
	public Metric getMetric()
	{
		return this.bufferMetric;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()
	{
		DiameterBuffer res = new DiameterBuffer();
		res.maxDiameter = maxDiameter;
		for (double[] d : buffer)
		{
			res.buffer.add(d.clone());
		}
		return res;
	}

	/**
	 * Returns true if the number of samples received by this buffer is greater
	 * than the size of the sample vectors.
	 * 
	 * @return true if this buffer has received n+1 or more samples, where n is the data dimension.
	 */
	public boolean isFilled()
	{
		if(buffer.isEmpty())
		{
			return false;
		}
		else
		{
			return (buffer.size() > (buffer.get(0).length + 1));
		}
	}

}
