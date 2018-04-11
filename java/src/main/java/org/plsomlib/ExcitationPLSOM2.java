package org.plsomlib;

import org.plsomlib.metrics.Metric;
import org.plsomlib.neighbourhood.NeighbourhoodFunction;

public class ExcitationPLSOM2 extends PLSOM2
{
	private static final long serialVersionUID = -2116842233930948175L;
	private double[] excitations;
	
	/**
	 * @param inputMetric
	 * @param outputMetric
	 * @param nhFunction
	 * @param inputSize
	 * @param outputDimensions
	 */
	public ExcitationPLSOM2(Metric inputMetric, Metric outputMetric, NeighbourhoodFunction nhFunction, int inputSize, int... outputDimensions)
	{
		super(inputMetric, outputMetric, nhFunction, inputSize, outputDimensions);
	}

	/**
	 * @param inputSize
	 * @param outputDimensions
	 */
	public ExcitationPLSOM2(int inputSize, int... outputDimensions)
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
}
