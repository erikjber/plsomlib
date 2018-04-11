package org.plsomlib;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Erik Berglund
 * 
 */
public class IEPLSOM2Test
{
	private Random random;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		random = new Random();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * Test whether the map will react correctly to repeated identical inputs.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRepetitiveInputs() throws Exception
	{
		IEPLSOM2 plsom = new IEPLSOM2(2, 4, 4, 4);
		plsom.setNeighbourhoodRange(15);
		double[] testData = new double[] { 0, 0 };
		for (int x = 0; x < 10000; x++)
		{
			plsom.train(testData);
			for (int node = 0; node < plsom.getWeights().getCount(); node++)
			{
				double[] data = plsom.getWeights().getValueFromOffset(node);
				for (int d = 0; d < data.length; d++)
				{
					if (Double.isInfinite(data[d]) || Double.isNaN(data[d]))
					{
						throw new Exception("" + data[d]);
					}
				}
			}
		}
	}
	
	/**
	 * Check if the fuzzy xor is computed correctly.
	 */
	@Test
	public void testFuzzyXor() throws Exception
	{
		IEPLSOM2 plsom = new IEPLSOM2(2, 4, 4, 4);
		plsom.setNeighbourhoodRange(15);
		//test some important points
		double t = plsom.fuzzyXor(0, 0);
		assertEquals(0.0,t);
		t = plsom.fuzzyXor(0, 1);
		assertEquals(1.0,t);
		t = plsom.fuzzyXor(0, 0.5);
		assertEquals(0.5,t);
		t = plsom.fuzzyXor(1, 0);
		assertEquals(1.0,t);
		t = plsom.fuzzyXor(0.5, 0);
		assertEquals(0.5,t);
		t = plsom.fuzzyXor(0.5, 0.5);
		assertEquals(0.5,t);
		

		t = plsom.fuzzyXor(1, 0.5);
		assertEquals(0.5,t);
		t = plsom.fuzzyXor(0.25, 0.75);
		assertEquals(0.75,t);
		t = plsom.fuzzyXor(0.75, 0.25);
		assertEquals(0.75,t);
		t = plsom.fuzzyXor(1, 1);
		assertEquals(0.0,t);
		
	}
	
	/**
	 * Test the speed of the fuzzy xor.
	 */
	@Test
	public void testFuzzyXorSpeed() throws Exception
	{
		IEPLSOM2 plsom = new IEPLSOM2(2, 4, 4, 4);
		plsom.setNeighbourhoodRange(15);
		
		//generate a set of random inputs
		double [][] data = new double[1000000][2];
		for(int x = 0; x < data.length;x++)
		{
			data[x][0]=random.nextDouble();
			data[x][1]=random.nextDouble();
		}
		//time the loop over all the inputs
		long startNanos = System.nanoTime();
		double sum = 0;
		for(int x = 0;x<data.length;x++)
		{
			sum+= plsom.fuzzyXor(data[x][0],data[x][1]);
		}
		double diff = (System.nanoTime()-startNanos)/(1000000000.0);
		System.out.println("Completed " + data.length+ " fuzzy xors in " + diff + " seconds.");
		System.out.println("Sum: " + sum);
	}

	/**
	 * Test that each update results in a change in the weights.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWeightUpdate() throws Exception
	{
		IEPLSOM2 plsom = new IEPLSOM2(2, 4, 4, 4);
		plsom.setNeighbourhoodRange(15);
		double[] testData = new double[] { 0, 0 };
		// get the bytes of the current weights
		ByteArrayOutputStream oldBos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(oldBos);
		oos.writeObject(plsom.getWeights());
		byte[] oldData = oldBos.toByteArray();
		for (int x = 0; x < 10000; x++)
		{
			// create random test data
			for (int d = 0; d < testData.length; d++)
			{
				testData[d] = random.nextGaussian();
			}
			plsom.train(testData);
			// compare new and old data
			// by serializing them and comparing the byte arrays
			ByteArrayOutputStream nuBos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(nuBos);
			oos.writeObject(plsom.getWeights());
			byte[] nuData = nuBos.toByteArray();
			if (nuData.length == oldData.length)
			{
				boolean foundDifference = false;
				for (int t = 0; t < nuData.length; t++)
				{
					if (nuData[t] != oldData[t])
					{
						foundDifference = true;
						break;
					}
				}
				assertTrue("Found no difference at iteration " + x, foundDifference);
			}
			oldData = nuData;
		}
	}
}
