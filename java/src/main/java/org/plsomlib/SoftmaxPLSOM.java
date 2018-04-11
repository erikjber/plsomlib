package org.plsomlib;

import org.plsomlib.metrics.Metric;
import org.plsomlib.neighbourhood.NeighbourhoodFunction;

/**
 * A PLSOM that keeps track of the individual excitations of the nodes in the Map.
 * The excitations are computed using the softmax activation function, with the highest excitation belonging to the winning node.
 * Apart from this, the SoftmaxPLSOM is identical to the PLSOM in every way and will produce identical output.
 * 
 * @author Erik Berglund
 *
 */
public class SoftmaxPLSOM extends ExcitationPLSOM
{
	private static final long serialVersionUID = 44020219719246819L;

	/**
	 * @param inputMetric
	 * @param outputMetric
	 * @param nhFunction
	 * @param inputSize
	 * @param outputDimensions
	 */
	public SoftmaxPLSOM(Metric inputMetric, Metric outputMetric, NeighbourhoodFunction nhFunction, int inputSize, int... outputDimensions)
	{
		super(inputMetric, outputMetric, nhFunction, inputSize, outputDimensions);
	}

	/**
	 * @param inputSize
	 * @param outputDimensions
	 */
	public SoftmaxPLSOM(int inputSize, int... outputDimensions)
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
        Object [] weights = getWeights().toArray();
        int winner = 0;
        double expSum = 0;
        for(int x = 0;x<weights.length;x++)
        {
        	double dist  = getInputMetric().getDistance((double[])weights[x], getInput());
            if (dist < minDist)
            {
                minDist = dist;
                winner = x;
            }
            //calculate exponentials for use in softmax
            getExcitations()[x] = Math.exp(-Math.log(dist));
            expSum+=getExcitations()[x];
        }
        //renormalize excitations using softmax
        for(int x =0;x<weights.length;x++)
        {
        	getExcitations()[x]/=expSum;
        }
        System.arraycopy(getWeights().getPosition(winner),0,res,0,res.length);
        return minDist;
    }

}
