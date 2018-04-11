package org.plsomlib.neighbourhood;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of NeighbourhoodFunction that provides the basic XML Read/Write functionality.
 * 
 * @author Erik Berglund
 *
 */
public abstract class NeighbourhoodFunctionImpl implements NeighbourhoodFunction, Serializable
{
	private static final long serialVersionUID = -8942503451173405801L;

	public abstract double getScaling(double distance, double neighbourhoodSize);

    /**
     * Get an XML element describing this neighbourhood function.
     * This default implementation simply gives the class name.
     * 
     * @param doc the Document that creates the Element.
     */
    public Element getElement(Document doc)
    {
        Element res = doc.createElement("neighbourhoodfunction");
        Element className = doc.createElement("classname");
        className.appendChild(doc.createTextNode(""+getClass().getName()));
        res.appendChild(className);
        return res;
    }

    /**
     * Create an NeighbourhoodFunction subclass from the XML element describing it.
     * This default implementation simply loads the class based on the class name.
     * 
     * @param e the XML Element describing the NeighbourhoodFunction.
     * @return an instance of a class implementing NeighbourhoodFunction.
     * @throws ClassNotFoundException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SecurityException 
     * @throws IllegalArgumentException 
     */
    public static NeighbourhoodFunction getNeighbourhoodFunction(Element e) throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        String className = e.getFirstChild().getTextContent();
        //create a new object with the given constructor arguments
        Class<?> metricClass = ClassLoader.getSystemClassLoader().loadClass(className);
        NeighbourhoodFunction res = (NeighbourhoodFunction)metricClass.getConstructor().newInstance();
        return res;
    }
}
