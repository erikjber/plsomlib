using System;

namespace org.plsomlib.metrics
{
	public class EuclideanMetric
	{
		public double getDistance(int [] a, int [] b)
		{
			double res = 0;
			for ( int x = 0;x < a.Length;x++ )
			{
				res += (a[x]-b[x])*(a[x]-b[x]);
			}
			return Math.Sqrt(res);
		}
		public double getDistance(double [] a, double [] b)
		{
			double res = 0;
			for ( int x = 0;x < a.Length;x++ )
			{
				res += (a[x]-b[x])*(a[x]-b[x]);
			}
			return Math.Sqrt(res);
		}
	}
}

