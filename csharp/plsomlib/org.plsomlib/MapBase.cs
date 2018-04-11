using System;

namespace org.plsomlib
{
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
		double [] getWeights (params int[] location);

		/**
     * Method for changing the weight vector associated with a given node.
     * @param location an array indicating the position of the desired node along each of the 
     * map's dimensions.
     * @param newWeights the new weights.
     */
		void setWeights (double[] newWeights, params int[] location);

		/**
     * Classify the most recently applied input according to this SOM.
     * @see #setInput(double [] input)
     */
		int [] classify ();


		/**
     * Classify the supplied input according to this SOM.
     * This is a convenience method, used where one wants to be 
     * sure that no other thread modifies the input value before it is
     * classified.
     */
		int [] classify (double[] input);

		/**
     * Set the input of this map. The input can be classified
     * or used for training.
     * @param input the input vector, also called data vector.
     * @see #classify()
     * @see #train()
     */
		void setInput (double[] input);


		/**
     * Train this map using the most recently applied input.
     */
		void train ();


		/**
     * Train the map using the supplied input.
     * @param input the input to use for training.
     */
		void train (double[] input);


		/**
     * Get the number and size of the output dimensions, as an integer array.
     * Example:
     * For a 2-dimensional map, 20 nodes wide and 15 nodes high, this function will return
     * new int[] {20,15};
     * 
     * @return the output dimensions.
     */
		int [] getOutputDimensions ();

		/**
     * Get the input dimensions of the map.
     * This can be used to ensure that the presented input is of the correct dimensionality.
     * 
     * @return the number of input dimensions.
     */
		int getInputDimension ();

	}
}

