package org.plsomlib.recursive;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Random;

import org.plsomlib.MapBase;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Erik Berglund
 *
 */
public class RecursivePLSOMTest extends TestCase
{

    /**
     * The width of the test network.
     */
    private int mapWidth = 20;
    
    /**
     * The height of the test network.
     */
    private int mapHeight = 15;
    
    /**
     * @param arg0
     */
    public RecursivePLSOMTest(String arg0)
    {
        super(arg0);
    }
    
    public void testRecrusivePLSOM() throws Exception
    {
        System.err.println("Recursive PLSOM test");
        RecursivePLSOM plsom = new RecursivePLSOM(2.0/(mapWidth*mapHeight),2,mapWidth,mapHeight);
        plsom.setNeighbourhoodRange(60);
        double [] tmp = new double[2];
        Random r = new Random(1);
        long mean = 0;
        int count = 2000; 
        //train
        for ( int x = 0;x< count;x++ )
        {
            //random input
            tmp[0] = r.nextDouble()*2-1;
            tmp[1] = r.nextDouble()*2-1;
            long start = System.nanoTime();
            plsom.train(tmp);
            long diff = System.nanoTime()-start;
            mean += diff;
        }
        double meanTrainTime = mean/((double)count*1000);
        //test
        mean = 0;
        int sqrtCount = (int)Math.sqrt(count);
        for ( int x = 0;x< sqrtCount;x++ )
        {
            for ( int y = 0;y<sqrtCount;y++ )
            {
                tmp[0] = (x/(double)sqrtCount)*2-1;
                tmp[1] = (y/(double)sqrtCount)*2-1;
                plsom.setInput(tmp);
                long start = System.nanoTime();
                plsom.classify();
                long diff = System.nanoTime()-start;
                mean += diff;
            }
        }
        double meanTestTime = mean/((double)count*1000);
        System.err.println("Train mean query time is: " + meanTrainTime + " microseconds.");
        System.err.println("Test mean query time is: " + meanTestTime + " microseconds.");
        
    }
    public void testReadWrite() throws Exception
    {

        System.err.println("Recursive PLSOM Read/Write test");
        
        //test plsom
        RecursivePLSOM plsom = new RecursivePLSOM(2.0/(mapWidth*mapHeight),2,mapWidth,mapHeight);
        double [] tmp = new double[]{0.3,0.8};
        plsom.setNeighbourhoodRange(60);
        plsom.train(tmp);
        plsom.write(new FileOutputStream("rplsom1.xml"));
        RecursivePLSOM nusom = (RecursivePLSOM)RecursivePLSOM.read(new FileInputStream("rplsom1.xml"));
        nusom.write(new FileOutputStream("rplsom2.xml"));

        //make sure the two files are equal.
        Assert.assertEquals(new File("rplsom1.xml").length(),new File("rplsom2.xml").length());
        FileInputStream f1 = new FileInputStream("rplsom1.xml");
        FileInputStream f2 = new FileInputStream("rplsom2.xml");
        try
        {
            while ( true )
            {
                int a = f1.read();
                int b = f2.read();
                if ( a!=b )
                {
                    throw new Exception("Files are not equal.");
                }
                if ( a<0 )
                    break;
            }
        }
        catch ( EOFException e )
        {
            //ignore
        }
        finally
        {
        	f1.close();
        	f2.close();
        }
        //make sure the implementation-specific properties are equal
        assertEquals(plsom.getAlpha(),nusom.getAlpha());
        double [] orig = plsom.getExcitations();
        double [] copy = nusom.getExcitations();
        for(int x = 0;x<orig.length;x++)
        {
            assertEquals(orig[x],copy[x]);
        }

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
	
    public void testClone()throws Exception
    {

		RecursivePLSOM map = new RecursivePLSOM(0.99,1,10,10);
		map.setNeighbourhoodRange(15);
		//train with random data
		Random rand = new Random();
		for(int x = 0;x<1000;x++)
		{
			map.train(new double[]{rand.nextDouble()});
		}
		//clone map
		RecursivePLSOM clone = (RecursivePLSOM)map.clone();
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
