using System;
using System.Collections;
using System.Collections.Generic;
using org.plsomlib.metrics;

namespace org.plsomlib.util
{
	public class DiameterBuffer
	{
		private List<double[]> buffer = new List<double[]>();
		public double maxDiameter { get; set;} = -1;
		private EuclideanMetric bufferMetric = new EuclideanMetric();


		public void updateBuffer(double[] data)
		{
			// calculate the distance from the new input to all inputs in the buffer
			// find the largest distance between the input and any entry in the
			// buffer
			// as well as the entry in the buffer that is closest to the input
			int minDistIndex = 0;
			int index = 0;
			double minDist = Double.MaxValue;
			double maxNewDist = 0;
			foreach (double[] n in buffer)
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
				buffer.Add((double[])data.Clone());
				// keep the buffer to the set size...
				if (buffer.Count > (data.GetLength(0) + 1))
				{
					// ...by removing the buffer entry that is closest to the new
					// input
					buffer.RemoveAt(minDistIndex);
				}
			}
		}

	}
}

