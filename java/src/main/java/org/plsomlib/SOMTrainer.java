package org.plsomlib;

import java.io.Serializable;

/**
 * This class is responsible for enforcing learning rates and neighbourhood sizes on
 * a SOM during training.
 * 
 * @see SOM
 * 
 * @author Erik Berglund
 */
public class SOMTrainer implements Serializable
{
	private static final long serialVersionUID = -4011503263922342168L;
	private double nhDecayRate=0.998;
	private double lrDecayRate=0.997;
	
	/**
	 * The SOM to train.
	 */
	private SOM som;

	/**
	 * Counter for the number of weight updates.
	 */
	private int updateCount;

	/**
	 * Class constructor.
	 * Receives a reference to the SOM that will be trained.
	 */
	public SOMTrainer(SOM som)
	{
		this.som = som;
	}

	
	/**
	 * Decay of the neighbourhood size. Range: 0-1.
	 * The larger this number is, the less the neighbourhood size will change with each iteration.
	 * @param nhDecayRate
	 */
	public void setNhDecayRate(double nhDecayRate)
	{
		this.nhDecayRate = nhDecayRate;
	}
	
	/**
	 * Decay of the learning rate. Range: 0-1.
	 * The larger this number is, the less the learning rate will change with each iteration.
	 * @param lrDecayRate
	 */
	public void setLrDecayRate(double lrDecayRate)
	{
		this.lrDecayRate = lrDecayRate;
	}

	/**
	 * Get the number of weight updates this trainer has done on the SOM.
	 */
	public int getTrainingIterationCount()
	{
		return this.updateCount;
	}

	/**
	 * Train the SOM with this input.
	 * The input is passed to the SOM along with the appropriate learning rate and neighbourhood size,
	 * and the SOM's weights are updated.
	 * @param input the data to train the SOM with.
	 */
	public void setInput(double [] input)
	{
		updateCount++;
		double learningRate = som.getLearningRate()*lrDecayRate;
		double neighbourhoodSize = som.getNeighbourhoodSize() * nhDecayRate;
		som.train(input,learningRate,neighbourhoodSize);
	}
}
