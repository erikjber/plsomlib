package org.plsomlib.util.labelling;

import java.util.ArrayList;

import org.plsomlib.MapBase;
import org.plsomlib.metrics.EuclideanMetric;
import org.plsomlib.metrics.Metric;
import org.plsomlib.util.IterativeArray;

/**
 * Produces the centroid of a site of double[] associated with a given node.
 * 
 * @author Erik Berglund
 * 
 */
public class CentroidLabeller extends SomLabeller<double[]>
{
	private static final long serialVersionUID = 5942664554649178918L;
	private IterativeArray<ArrayList<double[]>> arrayLabels;
	private Metric metric = new EuclideanMetric();

	public CentroidLabeller(MapBase map)
	{
		super(map);
		arrayLabels = new IterativeArray<ArrayList<double[]>>(
				map.getOutputDimensions());
	}

	/**
	 * Add a label used in calculating the centroids.
	 * 
	 * @param value
	 * @param location
	 */
	public void addLabel(double[] value, int... location)
	{
		ArrayList<double[]> existing = arrayLabels.getValue(location);
		if (existing == null)
		{
			existing = new ArrayList<double[]>();
			arrayLabels.setValue(existing, location);
		}
		existing.add(value);
		// delete existing centroid
		setLabel(null, location);
	}

	/**
	 * Get the centroid for a given entry.
	 * 
	 * @see org.plsomlib.util.labelling.SomLabeller#getLabel(int[])
	 */
	@Override
	public double[] getLabel(int... location)
	{
		double[] res = super.getLabel(location);
		if (res == null)
		{
			// generate the centroid
			ArrayList<double[]> existing = arrayLabels.getValue(location);
			if (existing != null && existing.size() > 0)
			{
				res = new double[existing.get(0).length];
				for (double[] d : existing)
				{
					for (int x = 0; x < res.length; x++)
					{
						res[x] += d[x];
					}
				}
				for (int x = 0; x < res.length; x++)
				{
					res[x] /= existing.size();
				}
				// store it
				setLabel(res, location);
			}
		}
		return res;
	}

	/**
	 * Search the labels for a matching input.
	 * 
	 * @param input
	 *            the input to match.
	 * @return the location of the label most similar to the input.
	 */
	public int [] getLocationOfClosestLabel(double[] input)
	{
		int res = 0;
		double minDist = Double.POSITIVE_INFINITY;
		for (int x = 0; x < labels.getCount(); x++)
		{
			double[] label = getLabel(labels.getPosition(x));
			if (label != null)
			{
				double dist = metric.getDistance(label, input);
				if (dist < minDist)
				{
					minDist = dist;
					res = x;
				}
			}
		}
		return labels.getPosition(res);
	}

}
