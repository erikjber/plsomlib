package org.plsomlib;

import org.plsomlib.metrics.Metric;
import org.plsomlib.neighbourhood.NeighbourhoodFunction;
import org.w3c.dom.*;

/**
 * Implementation of the Parameter-Less Self-Organizing Map algorithm. Uses a
 * gaussian neighbourhood function and euclidean distances by default, but others can be selected.
 * The setNeighbourhoodRange method must be called before training can commence.
 * 
 * @author Erik Berglund
 */
public class PLSOM extends MapBaseImpl
{
	private static final long serialVersionUID = -482188102386857278L;

	/**
     * The allowed range of the neighbourhood function.
     */
    private double nhRange;

    private transient double neighbourhoodSize;

    private double rho;

    
    private double epsilon;

    /**
     * The distance between the weight vector of the winning node and the input.
     */
    private double lastError;

	private double learningScale=1;

    
    /**
     * Class constructor.
     * 
     * @param inputSize
     *            the number of inputs to the PSLOM.
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
    public PLSOM(Metric inputMetric, Metric outputMetric, NeighbourhoodFunction nhFunction, int inputSize, int... outputDimensions)
    {
        super(inputMetric, outputMetric, nhFunction, inputSize, outputDimensions);
    }

    /**
     * Class constructor.
     * 
     * @param inputSize
     *            the number of inputs to the SOM.
     * @param outputDimensions
     *            the number of and size of output dimensions.
     */
    public PLSOM(int inputSize, int... outputDimensions)
    {
        super(inputSize, outputDimensions);
    }

    /**
     * Set the allowed range for the neighbourhood size.
     * 
     * @param nhRange
     *            the new neighbourhood size range.
     */
    public void setNeighbourhoodRange(double nhRange)
    {
        this.nhRange = nhRange;
    }

    /**
     * Get the allowed ragne for the neighbourhood size.
     * 
     * @returne the new neighbourhood size range.
     */
    public double getNeighbourhoodRange()
    {
        return nhRange;
    }

    /**
     * Classify the most recently applied input according to this SOM.
     * 
     * @see #setInput(double [] input)
     */
    public int[] classify()
    {
        int[] res = new int[getOutputDimensions().length];
        setLastError(findMinDist(res));
        setWinner(res);
        return res;
    }

    /**
     * Train this SOM using the most recently applied input.
     */
    public void train()
    {
        classify();
        // calculate epsilon
        setEpsilon(getLastError() / getRho());
        if (getEpsilon() > 1)
        {
            setRho(getLastError());
            setEpsilon(1);
        }
        // calculate the neighbourhood size
        setNeighbourhoodSize(this.getNeighbourhoodRange() * Math.log(1 + getEpsilon() * (Math.E - 1)));
        

        // calculate the new weights
        updateWeights();        
    }

    public double getNeighbourhoodSize()
    {
        return this.neighbourhoodSize;
    }

    public void setNeighbourhoodSize(double nhSize)
    {
        this.neighbourhoodSize = nhSize;
    }

    /**
     * Recursively update all weights given the current winner.
     * 
     */
    protected void updateWeights()
    {
        Object[] data = this.getWeights().toArray();
        for (int x = 0; x < data.length; x++)
        {
            // calculate the neighbourhood scaling, multiply by epsilon
            double anhc = getLearningScale()*getEpsilon() * getNeighbourhoodScaling(getWeights().getPosition(x), getWinner(), getNeighbourhoodSize());
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
        Element nhRange = doc.createElement("neighbourhood");
        nhRange.setAttribute("range", "" + this.nhRange);
        e.appendChild(nhRange);
        Element rho = doc.createElement("rho");
        rho.setAttribute("value", "" + this.rho);
        e.appendChild(rho);
        Element epsilon = doc.createElement("epsilon");
        epsilon.setAttribute("value", "" + this.epsilon);
        e.appendChild(epsilon);
        Element lastError = doc.createElement("lasterror");
        lastError.setAttribute("value", "" + getLastError());
        e.appendChild(lastError);
    }

    /**
     * Helper function for read(...) Perform implementation-specific
     * initialization of the new object.
     */
    public void readHelper(Element e)
    {
        // set the nhrange,rho, epsilon and lastError values
        String nhRange = e.getElementsByTagName("neighbourhood").item(0).getAttributes().getNamedItem("range").getTextContent();
        this.setNeighbourhoodRange(Double.parseDouble(nhRange));
        String rho = e.getElementsByTagName("rho").item(0).getAttributes().getNamedItem("value").getTextContent();
        this.rho = Double.parseDouble(rho);
        String epsilon = e.getElementsByTagName("epsilon").item(0).getAttributes().getNamedItem("value").getTextContent();
        this.epsilon = Double.parseDouble(epsilon);
        String lastError = e.getElementsByTagName("lasterror").item(0).getAttributes().getNamedItem("value").getTextContent();
        this.lastError = Double.parseDouble(lastError);
    }

    /**
     * @return the lastError
     */
    protected double getLastError()
    {
        return lastError;
    }

    /**
     * @param lastError
     *            the lastError to set
     */
    protected void setLastError(double lastError)
    {
        this.lastError = lastError;
    }

    /**
     * @return the rho
     */
    protected double getRho()
    {
        return rho;
    }

    /**
     * @param rho
     *            the rho to set
     */
    protected void setRho(double rho)
    {
        this.rho = rho;
    }

    public double getEpsilon()
    {
        return this.epsilon;
    }
    
    /**
     * @param epsilon
     *            the epsilon to set
     */
    protected void setEpsilon(double epsilon)
    {
        this.epsilon = epsilon;
    }

	/**
	 * Creates a new state vector and returns it.
	 * The state vector is the superclass state vector concatenated with rho
	 * @see org.plsomlib.MapBaseImpl#getStateVector()
	 */
	@Override
	public double[] getStateVector()
	{
		double [] tmp = super.getStateVector();
		double [] res = new double[tmp.length+1];
		System.arraycopy(tmp, 0, res, 0, tmp.length);
		res[tmp.length]=rho;
		return res;
	}

	public void setLearningScale(double learningScale)
	{
		this.learningScale = learningScale;
	}

	/**
	 * @return the learningScale
	 */
	public double getLearningScale()
	{
		return learningScale;
	}
	

}
