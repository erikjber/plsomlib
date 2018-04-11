package org.plsomlib.recursive;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IEStateLessRecursivePLSOM2Test
{

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStates() throws Exception
	{
		int inputSize = 1200;
		double importanceScaling = Math.pow(10, -4.30102999566398);
		IEStateLessRecursivePLSOM2 plsom = new IEStateLessRecursivePLSOM2(
				0.565, inputSize, 60, 3, 3);
		plsom.setNeighbourhoodRange(24);
		plsom.setRecoveryScaling(1);
		plsom.setUseRecovery(false);
		plsom.setImportanceScalingFactor(importanceScaling);
		plsom.setLearningScale(0.165);

		// train the map
		Random rand = new Random();
		double[] input = new double[inputSize];
		for (int x = 0; x < 20000; x++)
		{
			RecursiveState rs = new RecursiveState(plsom);
			double scaling = rand.nextInt(60 * 3 * 3) / (60.0 * 3.0 * 3.0);
			for (int y = 0; y < 10; y++)
			{
				for (int t = 0; t < inputSize; t++)
				{
					input[t] = rand.nextDouble() * scaling;
				}
				rs.train(input);
				int[] winner = plsom.getWinner();
				for (int w : winner)
				{
					System.out.print(w + " ");
				}
				System.out.println();
			}
			System.out.println();
		}
	}

}
