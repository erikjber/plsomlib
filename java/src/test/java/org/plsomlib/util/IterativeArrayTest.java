package org.plsomlib.util;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Erik Berglund
 *
 */
public class IterativeArrayTest
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
     * Test method for {@link org.plsomlib.util.IterativeArray#getPosition(int)}.
     */
    @Test
    public void testGetPosition()
    {
        Random rand=new Random();
        IterativeArray<Double>ia=new IterativeArray<Double>(4,3,1,8,10);
        Object [] data = ia.toArray();
        for(int x = 0;x<data.length;x++)
        {
            data[x]=rand.nextDouble();
        }
        for(int x = 0;x<data.length;x++)
        {
            int []address=ia.getPosition(x);
            assertTrue(ia.getValue(address)+" != "+data[x] + " @ " + x +" [" +arrayToString(address)+"]",ia.getValue(address)==data[x]);
        }
    }
    
    private String arrayToString(int... address)
    {
    	StringBuilder tmp = new StringBuilder();
        for(int x:address)
        {
            tmp.append(x);
            tmp.append(", ");
        }
        tmp.setLength(tmp.length()-2);
        return tmp.toString();
    }

}
