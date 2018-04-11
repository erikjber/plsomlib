package org.plsomlib.metrics;

import org.w3c.dom.*;
    
/**
 * An interface defining the distance between two points in the input or output space of
 * a MapBase. 
 * 
 * @author Erik Berglund
 */
public interface Metric
{
    /**
     * Calculates the distance between a and b according to this metric.
     * @param a one point in the input space.
     * @param b another point in the input space.
     * @return the distance from a to b.
     */
    public double getDistance(final double [] a,final double [] b);
    
    /**
     * Calculates the distance betewen a and b according to this metric.
     * @param a one map node in the output space.
     * @param b another map node in the output space.
     * @return the distance from a to b.
     */
    public double getDistance(final int [] a, final int [] b);
    
    /**
     * Get an XML element describing this metric.
     * 
     * @param doc the Document that creates the Element.
     * @param name the name of the element to create.
     */
    public Element getElement(Document doc, String name);
}
