package org.plsomlib.neighbourhood;

/**
 * This NeighbourhoodFunction only returns two possible values: 1 or 0.
 * 0 if the distance is greater than the neighbourhoodSize value, 1 otherwise.
 * @author Erik Berglund
 *
 */
public class BinaryNeighbourhoodFunction extends NeighbourhoodFunctionImpl
{
	private static final long serialVersionUID = -7464004638589123031L;

	/**
	 * @see org.plsomlib.neighbourhood.NeighbourhoodFunctionImpl#getScaling(double, double)
	 */
	@Override
	public double getScaling(double distance, double neighbourhoodSize)
	{
		if(distance <= neighbourhoodSize)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

}
