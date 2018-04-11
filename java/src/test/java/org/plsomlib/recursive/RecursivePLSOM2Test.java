package org.plsomlib.recursive;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.plsomlib.MapBase;


/**
 * @author Erik Berglund
 *
 */
public class RecursivePLSOM2Test
{

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * Serialize the map, write it to the output stream.
	 * @param map
	 * @param out
	 * @throws IOException 
	 */
	protected void serializeMap(MapBase map, OutputStream out) throws IOException
	{
		ObjectOutputStream oo = new ObjectOutputStream(out);
		oo.writeObject(map);
	}
	
	/**
	 * Test method for {@link org.plsomlib.recursive.RecursivePLSOM#clone()}.
	 * @throws Exception 
	 */
	@Test
	public void testClone() throws Exception
	{
		//create, train a RecursivePLSOM2 map
		RecursivePLSOM2 map = new RecursivePLSOM2(0.99,1,10,10);
		map.setNeighbourhoodRange(20);
		//train with random data
		Random rand = new Random();
		for(int x = 0;x<1000;x++)
		{
			map.train(new double[]{rand.nextDouble()});
		}
		//clone map
		RecursivePLSOM2 clone = (RecursivePLSOM2)map.clone();
		//serialize original and cloned map
		ByteArrayOutputStream origSer=new ByteArrayOutputStream();
		ByteArrayOutputStream cloneSer=new ByteArrayOutputStream();
		serializeMap(map,origSer);
		serializeMap(clone,cloneSer);
		//compare serializations byte by byte
		byte [] origBytes = origSer.toByteArray();
		byte [] cloneBytes = cloneSer.toByteArray();
		assertEquals("Serialization lenght",cloneBytes.length,origBytes.length);
		for(int x =0;x<origBytes.length;x++)
		{
			assertEquals("at "+x+" ",cloneBytes[x], origBytes[x]);
		}
	}

}
