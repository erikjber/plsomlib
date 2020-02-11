package org.plsomlib;

import org.plsomlib.metrics.Metric;
import org.plsomlib.neighbourhood.NeighbourhoodFunction;

/**
 * A PLSOM that keeps track of the individual excitations of the nodes in the Map.
 * The excitations are normalized in the [0,1] range, with the highest excitation belonging to the winning node.
 * Apart from this, the NormalizedExcitationPLSOM is identical to the PLSOM in every way and will produce identical output.
 * 
 * @author Erik Berglund
 *
 */
public class NormalizedExcitationPLSOM extends ExcitationPLSOM
{
	private static final long serialVersionUID = -8940349375370492079L;

	/**
	 * @param inputMetric
	 * @param outputMetric
	 * @param nhFunction
	 * @param inputSize
	 * @param outputDimensions
	 */
	public NormalizedExcitationPLSOM(Metric inputMetric, Metric outputMetric, NeighbourhoodFunction nhFunction, int inputSize, int... outputDimensions)
	{
		super(inputMetric, outputMetric, nhFunction, inputSize, outputDimensions);
	}

	/**
	 * @param inputSize
	 * @param outputDimensions
	 */
	public NormalizedExcitationPLSOM(int inputSize, int... outputDimensions)
	{
		super(inputSize, outputDimensions);
	}
	
    /**
     * Helper function for classify. Recursively compares all weight vectors to
     * the input.
     */
	@Override
    protected double findMinDist( int[] res)
    {
        double minDist = Double.POSITIVE_INFINITY;
        double maxDist = 0;
        Object [] weights = getWeights().toArray();
        int winner = 0;
        for(int x = 0;x<weights.length;x++)
        {
        	double dist  = getInputMetric().getDistance((double[])weights[x], getInput());
            if (dist < minDist)
            {
                minDist = dist;
                winner = x;
            }
            if(dist > maxDist)
            {
            	maxDist=dist;
            }
            getExcitations()[x]=dist;
        }
        double diff = maxDist-minDist;
        //renormalize excitations 
        for(int x =0;x<weights.length;x++)
        {
        	double tmp = getExcitations()[x]-minDist;
        	tmp/=diff;
        	getExcitations()[x]=1-tmp;
        }
        System.arraycopy(getWeights().getPosition(winner),0,res,0,res.length);
        return minDist;
    }

}
