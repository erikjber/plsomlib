package org.plsomlib;


/**
 * Implements the controlled magnification factor training algorithm by H.-U
 * Bauer, R. Der and M. Herrmann. The learning rate is determined by a function
 * of the time since the winning neuron was last a winning neuron, the distance
 * from the input to the winning weight vector and three parameters: epsilon0,
 * d, and m.
 * 
 * @author Erik Berglund
 * 
 */
public class BdhSOM extends SOM
{
	private static final long serialVersionUID = -3305886560751272668L;
	
	/**
	 * The maximum allowed value of epsilon
	 */
	private static final double EPSILON_MAX = 0.9;
	
	/**
	 * Epsilon scaling constant
	 */
	private double epsilon0;
	
	/**
	 * dimension scaling constant, usually less or equal to the input size
	 */
	private double d;
	
	/**
	 * The magnification factor control parameter.
	 */
	private double m;
	
	/**
	 * Keep track of how many training updates since each node was the winner.
	 */
	private int [] timeCounters;

	
	/**
	 * @param epsilon0 scaling constant for the weight update
	 * @param d dimension scaling, usually less than or equal to the input size
	 * @param m magnification factor control parameter
	 * @param inputSize the dimension of the input space
	 * @param outputDimensions the size of each output dimension.
	 */
	public BdhSOM(double epsilon0, double d, double m, int inputSize, int... outputDimensions)
	{
		super(inputSize, outputDimensions);
		this.epsilon0 = epsilon0;
		this.d = d;
		this.m = m;
		timeCounters = new int[this.getWeights().getCount()];
	}

    /**
     * Update all weights given the winner.
     * 
     * @param w
     *            the Vector containing a subtree of the weights tree.
     * @param index
     *            the level in the weights tree.
     */
    protected void updateWeights()
    {
    	//get the winning node offset
    	int winnerOffset = getWeights().getOffset(getWinner());	
    	
    	//increment time counters on all nodes
    	incrementTimeCounters();
    	//get the time scaling of the winner
    	double timeScale = 1.0/timeCounters[winnerOffset];    	
    	//reset the time counter of the winning node
    	this.timeCounters[winnerOffset]=0;
    	
    	//get the distance from the input to the winning node
    	double diff = getInputMetric().getDistance(getInput(), getWeights().getValueFromOffset(winnerOffset));
    	diff = 1.0/Math.pow(diff, d);
    	
    	double epsilon = this.epsilon0*Math.pow(timeScale*diff,m);
    	
    	//Make sure epsilon is not larger than EPSILON_MAX.
    	epsilon = Math.min(epsilon, EPSILON_MAX);
    	
        Object[] data = getWeights().toArray();
        for (int x = 0; x < data.length; x++)
        {
            double anhc = epsilon * getNeighbourhoodScaling(getWeights().getPosition(x), getWinner(), getNeighbourhoodSize());
            
            // get the weight vector
            double[] weight = (double[]) data[x];
            
            // update the weights
            for (int wIndex = 0; wIndex < weight.length; wIndex++)
            {
                weight[wIndex] += anhc * (getInput()[wIndex] - weight[wIndex]);
            }
        }
    }

	private void incrementTimeCounters()
	{
		for(int x =0;x<this.timeCounters.length;x++)
		{
			timeCounters[x] ++;
		}
	}
}
