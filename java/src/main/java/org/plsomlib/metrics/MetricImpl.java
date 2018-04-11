package org.plsomlib.metrics;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implements Metric and defines some common methods to simmplify subclassig.
 * 
 * @author Erik Berglund
 */
public abstract class MetricImpl implements Metric, Serializable
{
	private static final long serialVersionUID = -5175242792348080270L;

	/**
     * Get an XML element describing this metric.
     * This default implementation simply gives the class name.
     * 
     * @param doc the Document that creates the Element.
     * @param name the name of the element to create.
     */
    public Element getElement(Document doc, String name)
    {
        Element res = doc.createElement(name);
        Element className = doc.createElement("classname");
        className.appendChild(doc.createTextNode(""+getClass().getName()));
        res.appendChild(className);
        return res;
    }
    
    /**
     * Create an Metric subclass from the XML element describing it.
     * This default implementation simply loads the class based on the class name.
     * 
     * @param e the XML Element describing the metric.
     * @return an instance of a class implementing Metric.
     */
    public static Metric getMetric(Element e) throws Exception
    {
        String className = e.getFirstChild().getTextContent();
        //create a new object with the given constructor arguments
        Class<?> metricClass = ClassLoader.getSystemClassLoader().loadClass(className);
        Metric res = (Metric)metricClass.newInstance();
        return res;
    }
}
