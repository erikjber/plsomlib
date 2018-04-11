package org.plsomlib.recursive;

import org.plsomlib.metrics.Metric;
import org.plsomlib.neighbourhood.NeighbourhoodFunction;
import org.plsomlib.util.DiameterBuffer;
import org.plsomlib.util.IterativeArray;

/**
 * 
 * @author Erik Berglund
 * 
 */
public class RecursivePLSOM2 extends RecursivePLSOM
{
	private static final long serialVersionUID = -6830815444854593854L;
	private DiameterBuffer inputBuffer = new DiameterBuffer();
	private DiameterBuffer excitationBuffer = new DiameterBuffer();
	// this value is used for excitement scaling
	private double internalLastError;

	// Experimental: allows switching from normalized to softmax exitation
	private boolean useSoftMax = false;

	/**
	 * @param inputMetric
	 * @param outputMetric
	 * @param nhFunction
	 * @param alpha
	 * @param inputSize
	 * @param outputDimensions
	 */
	public RecursivePLSOM2(Metric inputMetric, Metric outputMetric, NeighbourhoodFunction nhFunction, double alpha, int inputSize,
			int... outputDimensions)
	{
		super(inputMetric, outputMetric, nhFunction, alpha, inputSize, outputDimensions);
	}

	/**
	 * @param alpha
	 * @param inputSize
	 * @param outputDimensions
	 */
	public RecursivePLSOM2(double alpha, int inputSize, int... outputDimensions)
	{
		super(alpha, inputSize, outputDimensions);
	}

	@Override
	public void setInput(double[] input)
	{
		super.setInput(input);
	}

	/**
	 * @see org.plsomlib.PLSOM#train()
	 */
	@Override
	public void train()
	{
		inputBuffer.updateBuffer(getInput());
		// do classification
		setWinner(findWinner());
		this.excitationBuffer.updateBuffer(getExcitations());
		// calculate epsilon
		if (this.isPredict())
		{
			setEpsilon(getInternalLastError() / ((1 - alpha) * excitationBuffer.getMaxDiameter()));
		}
		else
		{
			setEpsilon((getInternalLastError() + getLastError())
					/ (alpha * inputBuffer.getMaxDiameter() + (1 - alpha) * excitationBuffer.getMaxDiameter()));
		}
		if (getEpsilon() > 1)
		{
			setEpsilon(1);
		}
		else if (Double.isNaN(getEpsilon()))
		{
			setEpsilon(0);
		}
		// calculate the neighbourhood size
		setNeighbourhoodSize(this.getNeighbourhoodRange() * Math.log(1 + getEpsilon() * (Math.E - 1)));

		// calculate the new weights
		updateWeights();
		// copy excitations
		System.arraycopy(getNuExcitations(), 0, getExcitations(), 0, getExcitations().length);
	}

	/**
	 * Get the last error of the recursive part of the weight/data matching.
	 * 
	 * @return
	 */
	protected double getInternalLastError()
	{
		return internalLastError;
	}

	/**
	 * Set the last error of the recursive part of the weight/data matching.
	 * 
	 * @return
	 */
	public void setInternalLastError(double error)
	{
		internalLastError = error;
	}

	public void setUseSoftMax(boolean useSoftMax)
	{
		this.useSoftMax = useSoftMax;
	}

	/**
	 * Find the winning node.
	 * 
	 * @return
	 */
	public int[] findWinner()
	{
		int[] res = new int[getOutputDimensions().length];
		double minDist = Double.POSITIVE_INFINITY;
		double minExcitation = 1;
		double maxExcitation = 0;
		double totalExpExitation = 0;
		Object[] weights = getWeights().toArray();
		int winner = 0;
		for (int x = 0; x < weights.length; x++)
		{
			// calculate distances
			double inputDistance = calculateInputDistance(x);
			// always calculate distance on recursive component
			double recDistance = calculateRecursiveDistance(x);
			double distance = recDistance + inputDistance;

			double scale = 1;
			if (useRecovery)
			{
				scale = recovery[x];
			}
			// calculate excitation
			double excitation = Math.exp(-distance) * scale;
			this.getNuExcitations()[x] = excitation;
			if ((1 - excitation) < minDist)
			{
				minDist = (1 - excitation);
				this.setLastError(inputDistance);
				this.setInternalLastError(recDistance);
				winner = x;
				maxExcitation = excitation;
			}
			if (excitation < minExcitation)
			{
				minExcitation = excitation;
			}
			totalExpExitation += Math.exp(excitation);
		}
		System.arraycopy(getWeights().getPosition(winner), 0, res, 0, res.length);

		if (useSoftMax)
		{
			for (int node = 0; node < getNuExcitations().length; node++)
			{
				getNuExcitations()[node] = Math.exp(getNuExcitations()[node])/totalExpExitation;
			}

		}
		else
		{
			double diff = maxExcitation - minExcitation;
			if (diff <= 0)
			{
				// all excitations are equal
				diff = 1;
			}

			// normalise excitations
			for (int node = 0; node < getNuExcitations().length; node++)
			{
				getNuExcitations()[node] -= minExcitation;
				getNuExcitations()[node] /= diff;
			}
		}

		if (useRecovery)
		{
			// update recovery values
			for (int x = 0; x < recovery.length; x++)
			{
				recovery[x] += (1 - recovery[x]) / recoveryScaling;
			}
			recovery[winner] = 0;
		}
		return res;
	}

	protected double calculateRecursiveDistance(int x)
	{
		double recDistance = 0;
		if (getExcitations() != null)
		{
			recDistance = (1 - alpha) * this.getInputMetric().getDistance(getExcitations(), (double[]) recursiveWeights.toArray()[x]);
		}
		return recDistance;
	}

	protected double calculateInputDistance(int x)
	{
		double inputDistance = 0;
		if (!this.isPredict())
		{
			// if we have input (non-predict mode) calculate the input
			// distance
			Object[] weights = getWeights().toArray();
			inputDistance = alpha * this.getInputMetric().getDistance(getInput(), (double[]) weights[x]);
		}
		return inputDistance;
	}

	@Override
	public int[] classify()
	{
		int[] res = findWinner();
		setWinner(res);
		// copy excitations
		System.arraycopy(getNuExcitations(), 0, getExcitations(), 0, getExcitations().length);
		return res;
	}

	/**
	 * Creates a new state vector. The state vector is the state vector of the
	 * superclass concatenated with the maxDistance and maxExcitationDistance.
	 * 
	 * @see org.plsomlib.recursive.RecursivePLSOM#getStateVector()
	 */
	@Override
	public double[] getStateVector()
	{
		double[] tmp = super.getStateVector();
		double[] res = new double[tmp.length + 2];
		System.arraycopy(tmp, 0, res, 0, tmp.length);
		res[tmp.length] = inputBuffer.getMaxDiameter();
		res[tmp.length + 1] = excitationBuffer.getMaxDiameter();
		return res;
	}

	/**
	 * @see org.plsomlib.recursive.RecursivePLSOM#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object clone()
	{
		RecursivePLSOM2 res = new RecursivePLSOM2(this.getInputMetric(), this.getOutputMetric(), this.getNeighbourhoodFunction(), this.alpha,
				this.getInputDimension(), this.getOutputDimensions().clone());
		res.setExcitations(getExcitations().clone());
		res.setPredict(isPredict());
		if (recovery != null)
		{
			res.recovery = recovery.clone();
		}
		res.setRho(this.getRho());
		res.setEpsilon(this.getEpsilon());
		res.setLastError(this.getLastError());
		if (getInput() != null)
		{
			res.setInput(this.getInput().clone());
		}
		res.useRecovery = this.useRecovery;
		res.setWeights((IterativeArray<double[]>) this.getWeights().clone());
		res.setWinner(this.getWinner());
		res.setNeighbourhoodRange(this.getNeighbourhoodRange());
		res.inputBuffer = (DiameterBuffer) inputBuffer.clone();
		res.excitationBuffer = (DiameterBuffer) excitationBuffer.clone();
		res.internalLastError = internalLastError;
		res.recursiveWeights = (IterativeArray<double[]>) recursiveWeights.clone();
		int x = 0;
		for (double d : getNuExcitations())
		{
			res.getNuExcitations()[x] = d;
			x++;
		}
		res.useSoftMax = useSoftMax;
		return res;
	}

	/**
	 * @return the excitationBuffer
	 */
	public DiameterBuffer getExcitationBuffer()
	{
		return excitationBuffer;
	}

	/**
	 * @return the inputBuffer
	 */
	public DiameterBuffer getInputBuffer()
	{
		return inputBuffer;
	}

}
