package org.plsomlib;


import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExcitationPLSOMTest
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
	 * Assert that an identically initialized PLSOM and ExiationPLSOM produces the exact same result.
	 * @throws Exception
	 */
	@Test
	public void testEqualToPLSOM() throws Exception
	{
		int inputSize = 20;
		PLSOM plsom = new PLSOM(inputSize,5,5);
		plsom.setNeighbourhoodRange(6);
		ExcitationPLSOM eplsom = new ExcitationPLSOM(inputSize,5,5);
		eplsom.setNeighbourhoodRange(6);
		Random rand = new Random();
		long randomSeed = rand.nextLong();
		plsom.setRandomSeed(randomSeed);
		eplsom.setRandomSeed(randomSeed);
		plsom.initWeights();
		eplsom.initWeights();
		double [] input = new double[inputSize];
		for(int x = 0; x<20000;x++)
		{
			for(int i = 0;i<inputSize;i++)
			{
				input[i]=rand.nextDouble();
			}
			plsom.train(input);
			eplsom.train(input);
			assertEquals(plsom.getWinner()[0],eplsom.getWinner()[0]);
			assertEquals(plsom.getWinner()[1],eplsom.getWinner()[1]);
			System.out.println(eplsom.getExcitations()[eplsom.getWeights().getOffset(eplsom.getWinner())]);
		}
	}
}
