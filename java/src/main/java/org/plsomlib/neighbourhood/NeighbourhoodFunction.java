package org.plsomlib.neighbourhood;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The base interface for the neighbourhood function.
 * This function determines a scaling variable based on the distance between two
 * nodes and a neighbourhood size variable.
 * 
 * @author Erik Berglund
 */
public interface NeighbourhoodFunction
{
    /**
     * Calculate the scaling according to this neighbourhood function.
     * The precise interpretation of the neighbourhoodSize variable depends on
     * the implementing class.
     * 
     * @param distance the distance between two nodes in output space.
     * @param neighbourhoodSize the size of the neighbourhood, that is to say how far it extends.
     * @return a scaling variable which typically decreases with increased distance and increased neighbourhoodSize.
     */
    public double getScaling(double distance, double neighbourhoodSize);
    
    /**
     * Get an XML element describing this neighbourhood function.
     * 
     * @param doc the Document that creates the Element.
     */
    public Element getElement(Document doc);
        
}
