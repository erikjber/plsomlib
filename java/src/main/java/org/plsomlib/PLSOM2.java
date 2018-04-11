package org.plsomlib;

import org.plsomlib.metrics.*;
import org.plsomlib.neighbourhood.*;
import org.plsomlib.util.DiameterBuffer;

/**
 * This PLSOM creates an estimate of the input space and its size.
 * 
 * @author Erik Berglund
 * 
 */
public class PLSOM2 extends PLSOM
{
	private static final long serialVersionUID = 7632067630416332906L;
	
	private DiameterBuffer diameterBuffer = new DiameterBuffer();
	/**
	 * @param inputMetric
	 * @param outputMetric
	 * @param nhFunction
	 * @param inputSize
	 * @param outputDimensions
	 */
	public PLSOM2(Metric inputMetric, Metric outputMetric, NeighbourhoodFunction nhFunction, int inputSize, int... outputDimensions)
	{
		super(inputMetric, outputMetric, nhFunction, inputSize, outputDimensions);
	}

	/**
	 * @param inputSize
	 * @param outputDimensions
	 */
	public PLSOM2(int inputSize, int... outputDimensions)
	{
		super(inputSize, outputDimensions);
	}

	/**
	 * @param inputSize
	 * @param width
	 * @param height
	 */
	public PLSOM2(int inputSize, int width, int height)
	{
		super(inputSize, width, height);
	}

	/**
	 * @see org.plsomlib.MapBaseImpl#setInput(double[])
	 */
	@Override
	public void setInput(double[] input)
	{
		//update diameter buffer
		this.diameterBuffer.updateBuffer(input);
		//proceed as usual
		super.setInput(input);
	}

	/**
	 * @see org.plsomlib.PLSOM#train()
	 */
	@Override
	public void train()
	{
		setWinner(classify());
		// calculate epsilon
		if (getLastError() == 0)
		{
			setEpsilon(0);
		}
		else
		{
			setEpsilon(getLastError() / this.diameterBuffer.getMaxDiameter());
			if (getEpsilon() > 1)
			{
				setEpsilon(1);
			}
		}
        // calculate the neighbourhood size
        setNeighbourhoodSize(this.getNeighbourhoodRange() * Math.log(1 + getEpsilon() * (Math.E - 1)));

		// calculate the new weights
		updateWeights();
	}
}
