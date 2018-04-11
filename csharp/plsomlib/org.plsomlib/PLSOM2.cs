using System;
using org.plsomlib.util;

namespace org.plsomlib
{
	public class PLSOM2: MapBaseImpl
	{
		private DiameterBuffer diameterBuffer = new DiameterBuffer();


		/**
     * The allowed range of the neighbourhood function.
     */
		private double nhRange;

		private double neighbourhoodSize;

		private double rho;


		private double epsilon;

		/**
     * The distance between the weight vector of the winning node and the input.
     */
		private double lastError;


		/**
	 * @param inputSize
	 * @param outputDimensions
	 */
		public PLSOM2(int inputSize, params int[] outputDimensions) : base(inputSize,outputDimensions)
		{
		}

		public override void setInput(double [] input)
		{
			this.diameterBuffer.updateBuffer (input);
			base.setInput (input);
		}
		
		public override void train ()
		{
			setWinner(classify());
			// calculate epsilon
			if (getLastError() == 0)
			{
				setEpsilon(0);
			}
			else
			{
				setEpsilon(getLastError() / this.diameterBuffer.maxDiameter);
				if (getEpsilon() > 1)
				{
					setEpsilon(1);
				}
			}
			// calculate the neighbourhood size
			setNeighbourhoodSize(this.getNeighbourhoodRange() * Math.Log(1 + getEpsilon() * (Math.E - 1)));

			// calculate the new weights
			updateWeights();
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
		public new int[] classify()
		{
			int[] res = new int[getOutputDimensions().Length];
			setLastError(findMinDist(res));
			setWinner(res);
			return res;
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
			for (int x = 0; x < data.Length; x++)
			{
				// calculate the neighbourhood scaling, multiply by epsilon
				double anhc = getEpsilon() * getNeighbourhoodScaling(getWeights().getPosition(x), getWinner(), getNeighbourhoodSize());
				// get the weight vector
				double[] weight = (double[]) data[x];
				// update the weights
				for (int wIndex = 0; wIndex < weight.Length; wIndex++)
				{
					weight[wIndex] += anhc * (getInput()[wIndex] - weight[wIndex]);
				}
			}
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
		public new double[] getStateVector()
		{
			double [] tmp = base.getStateVector();
			double [] res = new double[tmp.Length+1];
			Array.Copy (tmp, res, tmp.Length);
			res[tmp.Length]=rho;
			return res;
		}

	}
}

