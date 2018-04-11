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
public class SOMTest extends TestCase
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
    public SOMTest(String arg0)
    {
        super(arg0);
    }

    /**
     * Test the ordinary SOM algorithm.
     */
    public void testSOM() throws Exception
    {
        System.err.println("SOM test");
        SOM som = new SOM(2,mapWidth,mapHeight);
        double [] tmp = new double[2];
        Random r = new Random(1);
        som.setLearningRate(0.8);
        som.setNeighbourhoodSize(60);
        long mean = 0;
        int count = 2000; 
        //train
        for ( int x = 0;x< count;x++ )
        {
            //random input
            tmp[0] = r.nextDouble()*2-1;
            tmp[1] = r.nextDouble()*2-1;
            long start = System.nanoTime();
            som.train(tmp);
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
                som.setInput(tmp);
                long start = System.nanoTime();
                som.classify();
                long diff = System.nanoTime()-start;
                mean += diff;
            }
        }
        double meanTestTime = mean/((double)count*1000);
        System.err.println("Train mean query time is: " + meanTrainTime + " microseconds.");
        System.err.println("Test mean query time is: " + meanTestTime + " microseconds.");
    }
    
    /**
     * Test the read/write capabilities of the SOM class.
     */
    public void testReadWriteSOM() throws Exception
    {
        System.err.println("SOM Read/Write test");

        //test SOM
        SOM som = new SOM(2,mapWidth,mapHeight);
        double [] tmp = new double[]{0.3,0.8};
        som.setLearningRate(0.8);
        som.setNeighbourhoodSize(60);
        som.train(tmp);
        som.write(new FileOutputStream("som1.xml"));
        SOM nusom = (SOM)SOM.read(new FileInputStream("som1.xml"));
        nusom.write(new FileOutputStream("som2.xml"));

        //make sure the two files are equal.
        Assert.assertEquals(new File("som1.xml").length(),new File("som2.xml").length());
        FileInputStream f1 = new FileInputStream("som1.xml");
        FileInputStream f2 = new FileInputStream("som2.xml");
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
