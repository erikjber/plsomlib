package org.plsomlib;

import org.plsomlib.metrics.Metric;
import org.plsomlib.neighbourhood.NeighbourhoodFunction;

/**
 * Implementation of the TASOM algorithm proposed by Shah-Hosseini and Safabakhsh in 2003.
 * 
 * @author Erik Berglund
 *
 */
public class TASOM extends SOM
{
	private static final long serialVersionUID = -3416920164044198211L;

	/**
	 * @param inputMetric
	 * @param outputMetric
	 * @param nhFunction
	 * @param inputSize
	 * @param outputDimensions
	 */
	public TASOM(Metric inputMetric, Metric outputMetric, NeighbourhoodFunction nhFunction, int inputSize, int... outputDimensions)
	{
		super(inputMetric, outputMetric, nhFunction, inputSize, outputDimensions);
	}

	/**
	 * @param inputSize
	 * @param outputDimensions
	 */
	public TASOM(int inputSize, int... outputDimensions)
	{
		super(inputSize, outputDimensions);
	}

}
