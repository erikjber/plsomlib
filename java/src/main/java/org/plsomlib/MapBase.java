package org.plsomlib;

import org.plsomlib.metrics.*;
import org.plsomlib.neighbourhood.*;
import org.w3c.dom.Element;

/**
 * The base interface for mapping algorithms like SOM and PLSOM.
 * 
 * 
 * @author Erik Berglund
 */
public interface MapBase
{    
    /**
     * Method for retreiving the weight vector associated with a given node.
     * @param location an array indicating the position of the desired node along each of the 
     * SOM's dimensions.
     * @return the weight vector of the node at the given location, any changes will to the returned
     * object will be reflected in the node.
     */
    public double [] getWeights(int... location);

    /**
     * Method for changing the weight vector associated with a given node.
     * @param location an array indicating the position of the desired node along each of the 
     * map's dimensions.
     * @param newWeights the new weights.
     */
    public void setWeights(double [] newWeights, int... location);
    
    /**
     * Classify the most recently applied input according to this SOM.
     * @see #setInput(double [] input)
     */
    public int [] classify();

    
    /**
     * Classify the supplied input according to this SOM.
     * This is a convenience method, used where one wants to be 
     * sure that no other thread modifies the input value before it is
     * classified.
     */
    public int [] classify(double [] input);    

    /**
     * Set the input of this map. The input can be classified
     * or used for training.
     * @param input the input vector, also called data vector.
     * @see #classify()
     * @see #train()
     */
    public void setInput(double [] input);
    

    /**
     * Train this map using the most recently applied input.
     */
    public void train();
    

    /**
     * Train the map using the supplied input.
     * @param input the input to use for training.
     */
    public void train(double [] input);
    
    /**
     * Get a string representing the version of this obect.
     */
    public String getVersion();
    
    /**
     * Set the distance metric used in this MapBase.
     * 
     * @param inputMetric the distance metric to use in the input space.
     */
    public void setInputMetric(Metric inputMetric);
    
    /**
     * Get the distance metric used in the input space of this MapBase.
     * @return the distance metric.
     */
    public Metric getInputMetric();

    /**
     * Set the distance metric used in this MapBase.
     * 
     * @param outputMetric the distance metric to use in the output space.
     */
    public void setOutputMetric(Metric outputMetric);

    /**
     * Get the distance metric used in the output space of this MapBase.
     * @return the distance metric.
     */
    public Metric getOutputMetric();
    
    /**
     * Set the neighbourhood function.
     * 
     * @param nhFunction the function used for calculating neighbourhood scalings in this map.
     */
    public void setNeighbourhoodFunction(NeighbourhoodFunction nhFunction);

    /**
     * Get the neighbourhood function.
     * 
     * @return the function used for calculating neighbourhood scalings in this map.
     */
    public NeighbourhoodFunction getNeighbourhoodFunction();
    

    /**
     * Helper function for read(...)
     * Perform implementation-specific initialization of the new object.
     */
    public void readHelper(Element e);
    
    /**
     * Get the number and size of the output dimensions, as an integer array.
     * Example:
     * For a 2-dimensional map, 20 nodes wide and 15 nodes high, this function will return
     * new int[] {20,15};
     * 
     * @return the output dimensions.
     */
    public int [] getOutputDimensions();
    
    /**
     * Get the input dimensions of the map.
     * This can be used to ensure that the presented input is of the correct dimensionality.
     * 
     * @return the number of input dimensions.
     */
    public int getInputDimension();
    
    /**
     * Create a double array that represents the state vector of this map.
     * The information in this vector, along with the parameters passed to the constructor, is by definition 
     * sufficient to recreate the object completely.
     * 
     * Any two objects implementing this interface and having identical state vectors will, by definition, produce the same output
     * when presented with the same input.
     * 
     * @return the state vector.
     */
    public double [] getStateVector();
}
