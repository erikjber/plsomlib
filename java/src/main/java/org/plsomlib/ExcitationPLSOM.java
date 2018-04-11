package org.plsomlib;

import org.plsomlib.metrics.Metric;
import org.plsomlib.neighbourhood.NeighbourhoodFunction;

/**
 * A PLSOM that keeps track of the individual excitations of the nodes in the Map.
 * The excitations are non-normalized, with the highest excitation belonging to the winning node.
 * Excitations are in the range [-Inf,1].
 * Apart from this, the ExcitationPLSOM is identical to the PLSOM in every way and will produce identical output.
 * 
 * @author Erik Berglund
 *
 */
public class ExcitationPLSOM extends PLSOM
{
	private static final long serialVersionUID = 3048919531656666763L;
	private double[] excitations;
	
	/**
	 * @param inputMetric
	 * @param outputMetric
	 * @param nhFunction
	 * @param inputSize
	 * @param outputDimensions
	 */
	public ExcitationPLSOM(Metric inputMetric, Metric outputMetric, NeighbourhoodFunction nhFunction, int inputSize, int... outputDimensions)
	{
		super(inputMetric, outputMetric, nhFunction, inputSize, outputDimensions);
	}

	/**
	 * @param inputSize
	 * @param outputDimensions
	 */
	public ExcitationPLSOM(int inputSize, int... outputDimensions)
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
        for(int x = 0;x<weights.length;x++)
        {
        	double dist  = getInputMetric().getDistance((double[])weights[x], getInput());
            if (dist < minDist)
            {
                minDist = dist;
                winner = x;
            }
            getExcitations()[x]=1-dist;
        }
        System.arraycopy(getWeights().getPosition(winner),0,res,0,res.length);
        return minDist;
    }	

	/**
	 * @see org.plsomlib.MapBaseImpl#initWeights()
	 */
	@Override
	public void initWeights()
	{
		super.initWeights();
		int nodeCount = this.getWeights().toArray().length;
		this.setExcitations(new double[nodeCount]);
	}

	/**
	 * Return an array of all node excitations values from the last
	 * classification.
	 * 
	 * @return the excitations
	 */
	public double[] getExcitations()
	{
		return excitations;
	}

	/**
	 * @param excitations the excitations to set
	 */
	public void setExcitations(double[] excitations)
	{
		this.excitations = excitations;
	}

	/**
	 * Creates a new state vector. The returned state vector is the superclass state vector 
	 * concatenated with the excitation values.
	 * 
	 * @see org.plsomlib.PLSOM#getStateVector()
	 */
	@Override
	public double[] getStateVector()
	{
		double[] tmp = super.getStateVector();
		double [] res = new double[tmp.length+this.excitations.length];
		//copy the superclass values into the result array
		System.arraycopy(tmp, 0, res, 0, tmp.length);
		//copy the excitation values into the result array
		System.arraycopy(excitations, 0, res, tmp.length, excitations.length);
		return res;		
	}
}
