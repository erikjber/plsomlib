using System;
using org.plsomlib.util;
using org.plsomlib.metrics;
using org.plsomlib.neighbourhood;

namespace org.plsomlib
{
	/**
 * Default implementation of the MapBase interface. Classes can extend this
 * class instead of implementing MapBase for ease of use.
 * 
 * @author Erik Berglund
 */
	public abstract class MapBaseImpl : MapBase
	{

		/**
     * A temporary local reference to the last input vector.
     */
		private double[] input;

		/**
     * The input size this SOM expects.
     */
		private int inputDimension;

		private Random random;

		private IterativeArray<double[]> weights;

		/**
     * Temporary winner location pointer.
     */
		private int[] winner;

		/**
     * The distance metric used in the input space.
     */
		private EuclideanMetric inputMetric;

		/**
     * The distance metric used in the output space.
     */
		private EuclideanMetric outputMetric;

		/**
     * A neighbourhood function.
     */
		private GaussianNeighbourhoodFunction nhFunction;

		/**
     * Class constructor.
     * 
     * @param inputSize
     *            the number of inputs to the map.
     * @param outputDimensions
     *            the number of and size of output dimensions.
     */
		public MapBaseImpl(int inputSize, params int[] outputDimensions)
		{
			inputMetric = new EuclideanMetric ();
			outputMetric = new EuclideanMetric ();
			nhFunction = new GaussianNeighbourhoodFunction ();
			this.inputDimension = inputSize;
			this.weights = new IterativeArray<double[]>(outputDimensions);
			this.random = new Random();
			this.initWeights();
		}


		/**
     * @return the inputDimension
     */
		public int getInputDimension()
		{
			return inputDimension;
		}

		/**
     * @param inputDimension
     *            the inputDimension to set
     */
		public void setInputDimension(int inputDimension)
		{
			this.inputDimension = inputDimension;
		}

		/**
     * Access the underlying weight storage implementation.
     * @return the weights
     */
		public IterativeArray<double[]> getWeights()
		{
			return weights;
		}

		/**
     * Create the weights. All weights are initially set to random values
     * between -1 and 1.
     */
		public void initWeights()
		{
			for (int x = 0; x < this.weights.toArray().Length; x++)
			{
				double[] tmpArray = new double[this.inputDimension];
				// initialize weight to random values
				if (random != null)
				{
					for (int t = 0; t < this.inputDimension; t++)
					{
						tmpArray[t] = 0.1*(random.NextDouble() * 2 - 1);
					}
				}
				this.weights.toArray()[x]=tmpArray;
			}
		}

		/**
     * Method for retreiving the weight vector associated with a given node.
     * 
     * @param location
     *            an array indicating the position of the desired node along
     *            each of the SOM's dimensions.
     * @return the weight vector of the node at the given location, any changes
     *         will to the returned object will be reflected in the node.
     */
		public double[] getWeights(params int[] location)
		{
			return weights.getValue(location);
		}

		/**
     * Method for changing the weight vector associated with a given node.
     * 
     * @param location
     *            an array indicating the position of the desired node along
     *            each of the SOM's dimensions.
     * @param newWeights
     *            the new weights.
     */
		public void setWeights(double[] newWeights,params int[] location)
		{
			weights.setValue(newWeights, location);
		}

		/**
     * Set the input of this SOM. The input can be classified or used for
     * training.
     * 
     * @param input
     *            the input vector, also called data vector.
     * @see #classify()
     * @see #train()
     */
		public virtual void setInput(double[] input)
		{
			this.input = input;
		}

		/**
     * Classify the most recently applied input according to this SOM.
     * 
     * @see #setInput(double [] input)
     */
		public virtual int[] classify()
		{
			int[] res = new int[this.weights.getDimensions().Length];
			findMinDist(res);
			return res;
		}

		/**
     * Classify the supplied input according to this SOM. This is a convenience
     * method, used where one wants to be sure that no other thread modifies the
     * input value before it is classified.
     */
		public virtual int[] classify(double[] input)
		{
			setInput(input);
			return classify();
		}

		/**
     * Helper function for classify. Recursively compares all weight vectors to
     * the input.
     */
		protected double findMinDist( int[] res)
		{
			double minDist = Double.PositiveInfinity;
			Object [] data = getWeights().toArray();
			int winner = 0;
			for(int x = 0;x<data.Length;x++)
			{
				if (inputMetric.getDistance((double[])data[x], getInput()) < minDist)
				{
					minDist = inputMetric.getDistance((double[])data[x], getInput());
					winner = x;
				}
			}
			Array.Copy (getWeights ().getPosition (winner), res, res.Length);
			return minDist;
		}

		/**
     * Calculate the value of the neighbourhood function for the node located at
     * loc, given the location of the winner and the neighbourhood size.
     * 
     * @param loc
     *            the location of the node to calculate the neighbourhood for.
     * @param winner
     *            the location of the winner.
     * @param nhSize
     *            the neighbourhood size.
     */
		protected double getNeighbourhoodScaling(int[] loc, int[] winner, double nhSize)
		{
			double dist = this.outputMetric.getDistance(loc, winner);
			return this.nhFunction.getScaling(dist, nhSize);
		}



		/**
     * Train this map using the most recently applied input. Must be overridden
     * by subclasses.
     * Updates the Winner property.
     */
		public abstract void train();

		/**
     * Train the map using the supplied input.
     * 
     * @param input
     *            the input to use for training.
     */
		public virtual void train(double[] input)
		{
			setInput(input);
			train();
		}



		/**
     * @see org.plsomlib.MapBase#getOutputDimensions()
     */
		public int[] getOutputDimensions()
		{
			return weights.getDimensions();
		}

		public double[] getInput()
		{
			return input;
		}

		public int[] getWinner()
		{
			return winner;
		}

		public void setWinner(int[] winner)
		{
			this.winner = winner;
		}

		/**
	 * @param weights the weights to set
	 */
		protected void setWeights(IterativeArray<double[]> weights)
		{
			this.weights = weights;
		}

		/**
	 * @return the random
	 */
		protected Random getRandom()
		{
			return random;
		}

		/**
	 * Returns the weights of all the nodes as one vector.
	 * @see org.plsomlib.MapBase#getStateVector()
	 */
		public virtual double[] getStateVector()
		{
			//find out how large the result vector will be
			int nodes = this.weights.toArray().Length;
			if(nodes > 0)
			{
				nodes*=((double[])this.weights.toArray()[0]).Length;
				//create the result array
				double [] res = new double[nodes];
				//copy the node weights into the result array
				int offset =0;
				foreach (Object o in weights.toArray())
				{
					double [] w = (double[])o;
					Array.Copy (w, res, w.Length);
					offset+=w.Length;
				}
				return res;
			}
			else
			{
				//no nodes in map, return empty array
				return new double[0];
			}
		}



	}
}

