package org.plsomlib;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Erik Berglund
 *
 */
public class PLSOMTest extends TestCase
{
    /**
     * The width of the test network.
     */
    private int mapWidth = 50;
    
    /**
     * The height of the test network.
     */
    private int mapHeight = 60;
    
    /**
     * @param arg0
     */
    public PLSOMTest(String arg0)
    {
        super(arg0);
    }

    /**
     * Test the PLSOM algorithm.
     */
    public void testPLSOM() throws Exception
    {
        System.err.println("PLSOM test");
        PLSOM plsom = new PLSOM(2,mapWidth,mapHeight);
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


    /**
     * Test the read/write capabilities of the PLSOM class.
     */
    public void testReadWritePLSOM() throws Exception
    {
        System.err.println("PLSOM Read/Write test");
        
        //test plsom
        PLSOM plsom = new PLSOM(2,mapWidth,mapHeight);
        double [] tmp = new double[]{0.3,0.8};
        plsom.setNeighbourhoodRange(60);
        plsom.train(tmp);
        plsom.write(new FileOutputStream("plsom1.xml"));
        PLSOM nusom = (PLSOM)PLSOM.read(new FileInputStream("plsom1.xml"));
        nusom.write(new FileOutputStream("plsom2.xml"));

        //make sure the two files are equal.
        Assert.assertEquals(new File("plsom1.xml").length(),new File("plsom2.xml").length());
        FileInputStream f1 = new FileInputStream("plsom1.xml");
        FileInputStream f2 = new FileInputStream("plsom2.xml");
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

    }
}
