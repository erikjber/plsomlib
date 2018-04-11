package org.plsomlib.recursive;

import org.plsomlib.MapBase;
import org.plsomlib.metrics.Metric;
import org.plsomlib.neighbourhood.NeighbourhoodFunction;
import org.w3c.dom.Element;

/**
 * Class that maintains a state for a StateLessRecursivePLSOM2.
 * 
 * @author Erik Berglund
 * 
 */
public class RecursiveState implements MapBase
{
	private StateLessRecursivePLSOM2 plsom;

	/**
	 * The excitations for this iteration
	 */
	private double[] excitations;

	public RecursiveState(StateLessRecursivePLSOM2 plsom)
	{
		this.plsom = plsom;
	}

	public int[] classify()
	{
		plsom.setExcitations(excitations);
		int[] res = plsom.classify();
		excitations = plsom.getExcitations();
		return res;
	}

	public int[] classify(double[] input)
	{
		setInput(input);
		return classify();
	}

	public int getInputDimension()
	{
		return plsom.getInputDimension();
	}

	public Metric getInputMetric()
	{
		return plsom.getInputMetric();
	}

	public NeighbourhoodFunction getNeighbourhoodFunction()
	{
		return plsom.getNeighbourhoodFunction();
	}

	public int[] getOutputDimensions()
	{
		return plsom.getOutputDimensions();
	}

	public Metric getOutputMetric()
	{
		return plsom.getOutputMetric();
	}

	public double[] getStateVector()
	{
		//cache the old excitation
		double[] tmp = plsom.getExcitations();
		//plug in the excitation of this object
		plsom.setExcitations(this.excitations);
		//get the state vector
		double[] res = plsom.getStateVector();
		//put in the old excitations
		plsom.setExcitations(tmp);
		//return the calculated value
		return res;
	}

	public String getVersion()
	{
		return plsom.getVersion();
	}

	public double[] getWeights(int... location)
	{
		return plsom.getWeights(location);
	}

	public void readHelper(Element e)
	{
	}

	public void setInput(double[] input)
	{
		this.plsom.setInput(input);
	}

	public void setInputMetric(Metric inputMetric)
	{
		plsom.setInputMetric(inputMetric);
	}

	public void setNeighbourhoodFunction(NeighbourhoodFunction nhFunction)
	{
		plsom.setNeighbourhoodFunction(nhFunction);
	}

	public void setOutputMetric(Metric outputMetric)
	{
		plsom.setOutputMetric(outputMetric);
	}

	public void setWeights(double[] newWeights, int... location)
	{
		plsom.setWeights(newWeights, location);
	}

	public void train()
	{
		plsom.setExcitations(excitations);
		plsom.train();
		excitations = plsom.getExcitations();
	}

	public void update()
	{
		plsom.setExcitations(excitations);
		plsom.classify();
		excitations = plsom.getExcitations();
	}

	public void train(double[] input)
	{
		setInput(input);
		train();
	}

	public void update(double[] input)
	{
		setInput(input);
		update();
	}
}
