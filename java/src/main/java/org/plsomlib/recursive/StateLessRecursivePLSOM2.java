package org.plsomlib.recursive;


/**
 * A RecursivePLSOM2 instance that does not maintain its own state.
 * 
 * @author Erik Berglund
 *
 */
public class StateLessRecursivePLSOM2 extends RecursivePLSOM2
{
	private static final long serialVersionUID = -6412180746284248546L;

	public StateLessRecursivePLSOM2(double alpha, int inputSize, int... outputDimensions)
	{
		super(alpha, inputSize, outputDimensions);
	}

	@Override
	public int[] classify()
	{
		int[] res = findWinner();
		setWinner(res);
		// copy excitations
		if(getExcitations()!=null)
		{
			System.arraycopy(getNuExcitations(), 0, getExcitations(), 0, getExcitations().length);
		}
		else
		{
			setExcitations(getNuExcitations().clone());
		}
		return res;
	}

	/**
	 * @see org.plsomlib.recursive.RecursivePLSOM2#train()
	 */
	@Override
	public void train()
	{
		getInputBuffer().updateBuffer(getInput());
		//do classification
		setWinner(findWinner());
		if(getExcitations()!=null)
		{
			getExcitationBuffer().updateBuffer(getExcitations());
		}
		// calculate epsilon
		if (this.isPredict())
		{
			setEpsilon(getInternalLastError() / ((1 - alpha) * getExcitationBuffer().getMaxDiameter()));
		}
		else if(getExcitations()!=null)
		{
			setEpsilon((getInternalLastError() + getLastError()) / (alpha * getInputBuffer().getMaxDiameter() + (1 - alpha) * getExcitationBuffer().getMaxDiameter()));
		}
		else
		{
			setEpsilon(getLastError() / (alpha * getInputBuffer().getMaxDiameter() ));
		}
		if (!((getEpsilon() <= 1) && (getEpsilon()>=0))  )
		{
			setEpsilon(1);
		}
		// calculate the neighbourhood size
		setNeighbourhoodSize(this.getNeighbourhoodRange() * Math.log(1 + getEpsilon() * (Math.E - 1)));

		// calculate the new weights
		updateWeights();
		// copy excitations
		if(getExcitations()!=null)
		{
			System.arraycopy(getNuExcitations(), 0, getExcitations(), 0, getExcitations().length);
		}
		else
		{
			setExcitations(getNuExcitations().clone());
		}
	}


}
