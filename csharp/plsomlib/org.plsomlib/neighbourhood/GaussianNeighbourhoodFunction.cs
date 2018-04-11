using System;

namespace org.plsomlib.neighbourhood
{
	/**
 * Implementation of the NeighbourhoodFunction interface that
 * calculates the neighbourhood scaling according to a Gaussian function.
 * 
 * The neighbourhood scaling is defined so that 
 * S = e^(-d^2/N^2)
 * where S is the scaling variable returned by getScaling(...),
 * e is the euler number, d is the distance and N is the neighbourhood size.  
 * 
 * @author Erik Berglund
 */
	public class GaussianNeighbourhoodFunction
	{

		/**
     * Calculate the scaling according to the Gaussian neighbourhood function.
     * The neighbourhoodSize argument is treated as a scaling variable of the
     * extent of the neighbourhood.
     * 
     * @param distance the distance between two nodes in output space.
     * @param neighbourhoodSize the size of the neighbourhood, 
     * determines how fast the scaling variable tends to zero with increasing distance.
     * @return a scaling variable ranging from 1 (where distance is 0) tending to 0 for increased distance.
     */
		public double getScaling (double distance, double neighbourhoodSize)
		{
			double nhSquared = neighbourhoodSize * neighbourhoodSize;
			if (nhSquared == 0.0) {
				//avoid NaN errors
				if (distance == 0.0) {
					return 1.0;
				} else {
					return 0.0;
				}
			}
			return Math.Pow(Math.E, -(distance * distance) / (nhSquared));
		}
	}
}

