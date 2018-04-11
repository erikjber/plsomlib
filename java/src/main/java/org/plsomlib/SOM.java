package org.plsomlib;

import org.plsomlib.metrics.Metric;
import org.plsomlib.neighbourhood.NeighbourhoodFunction;
import org.w3c.dom.*;

/**
 * The basic Self Organizing Map class. Implements the algorithm described by
 * Prof. Teuvo Kohonen. This SOM implementation can have any number of inputs
 * and any number of output dimensions, uses a Gaussian neighbourhood function
 * and uses the Euclidean norm as a distance measure.
 * 
 * This class is thread-safe.
 * 
 * The learning rate and neighbourhood size is set from outside this class.
 * 
 * @see SOMTrainer
 * 
 * 
 * @author Erik Berglund
 */
public class SOM extends MapBaseImpl
{
	private static final long serialVersionUID = -3003146472842499776L;
	private double learningRate;
    private double neighbourhoodSize;
	private SOMTrainer trainer;

    /**
     * Class constructor.
     * 
     * @param inputSize
     *            the number of inputs to the LOM.
     * @param outputDimensions
     *            the number of and size of output dimensions.
     * @param inputMetric
     *            the input metric to use for this class.
     * @param outputMetric
     *            the output metric to use for this class.
     * @param nhFunction
     *            the neighbourhood function that calculates neighbourhood
     *            scalings in this map.
     */
    public SOM( Metric inputMetric, Metric outputMetric, NeighbourhoodFunction nhFunction,int inputSize, int... outputDimensions)
    {
        super(inputMetric, outputMetric, nhFunction,inputSize, outputDimensions);
        trainer = new SOMTrainer(this);
    }

    /**
     * Class constructor. Creates a map with euclidean input space, and
     * rectangualar euclidean output space.
     * 
     * @param inputSize
     *            the number of inputs to the SOM.
     * @param outputDimensions
     *            the number of and size of output dimensions.
     */
    public SOM(int inputSize, int... outputDimensions)
    {
        super(inputSize, outputDimensions);
        trainer = new SOMTrainer(this);
    }


    /**
     * Train this SOM using the most recently applied input.
     */
    public synchronized void train()
    {
        setWinner(classify());
        // create a temporary array to keep track of the location
        updateWeights();

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
        Object[] data = this.getWeights().toArray();
        for (int x = 0; x < data.length; x++)
        {
            // calculate the neighbourhood scaling, multiply by learning rate
            double anhc = this.learningRate * getNeighbourhoodScaling(getWeights().getPosition(x), getWinner(), this.neighbourhoodSize);
            // get the weight vector
            double[] weight = (double[]) data[x];
            // update the weights
            for (int wIndex = 0; wIndex < weight.length; wIndex++)
            {
                weight[wIndex] += anhc * (getInput()[wIndex] - weight[wIndex]);
            }
        }
    }

    /**
     * Train the SOM using the supplied input. This is a wrapper to ensure
     * synchronized operation.
     * 
     * @param input
     *            the input to use for training.
     * @param learningRate
     *            the new learning rate, must be in the 0-1 range.
     * @param nhSize
     *            the new neighbourhood size, must be non-negative.
     */
    public synchronized void train(double[] input, double learningRate, double nhSize)
    {
        setLearningRate(learningRate);
        setNeighbourhoodSize(nhSize);
        setInput(input);
        train();
    }
    
	/**
	 * @see org.plsomlib.MapBaseImpl#train(double[])
	 */
	@Override
	public void train(double[] input)
	{
		if(trainer!=null)
		{
			trainer.setInput(input);
		}
	}

    /**
     * Set the learning rate of this SOM.
     * 
     * @param learningRate
     *            the new learning rate, must be in the 0-1 range.
     */
    public synchronized void setLearningRate(double learningRate)
    {
        this.learningRate = learningRate;
    }
    
    public double getLearningRate()
    {
    	return this.learningRate;
    }

    /**
     * Set the neighbourhood size of this SOM.
     * 
     * @param nhSize
     *            the new neighbourhood size, must be non-negative.
     */
    public synchronized void setNeighbourhoodSize(double nhSize)
    {
        this.neighbourhoodSize = nhSize;
    }
    
	public double getNeighbourhoodSize()
	{
		return this.neighbourhoodSize;
	}

    /**
     * Helper function for createDocument(...). Use this to do subclass or
     * implementation-specific processing.
     * 
     * @param e
     *            the root element of the document.
     * @param doc
     *            the document.
     */
    protected void createDocumentHelper(Element e, Document doc)
    {
        // write the neighbourhood size, learning rate.
        Element nhSize = doc.createElement("neighbourhood");
        nhSize.setAttribute("size", "" + this.neighbourhoodSize);
        e.appendChild(nhSize);
        Element lr = doc.createElement("learning");
        lr.setAttribute("rate", "" + this.learningRate);
        e.appendChild(lr);
    }

    /**
     * Helper function for read(...) Perform implementation-specific
     * initialization of the new object.
     */
    public void readHelper(Element e)
    {
        // set the neighbourhood size and learning rate values
        String nhSize = e.getElementsByTagName("neighbourhood").item(0).getAttributes().getNamedItem("size").getTextContent();
        this.setNeighbourhoodSize(Double.parseDouble(nhSize));
        String lRate = e.getElementsByTagName("learning").item(0).getAttributes().getNamedItem("rate").getTextContent();
        this.setLearningRate(Double.parseDouble(lRate));
    }

	public void setTrainer(SOMTrainer trainer)
	{
		this.trainer = trainer;
	}
	
	public SOMTrainer getTrainer()
	{
		return trainer;
	}


}
