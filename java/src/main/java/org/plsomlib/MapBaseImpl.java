package org.plsomlib;

import org.plsomlib.metrics.*;
import org.plsomlib.neighbourhood.*;
import org.plsomlib.util.IterativeArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Default implementation of the MapBase interface. Classes can extend this
 * class instead of implementing MapBase for ease of use.
 * 
 * @author Erik Berglund
 */
public abstract class MapBaseImpl implements MapBase, Serializable
{
	private static final long serialVersionUID = -7709290976959528351L;

	/**
     * A temporary local reference to the last input vector.
     */
    private transient double[] input;

    /**
     * The input size this SOM expects.
     */
    private int inputDimension;

    protected transient Random random;

    private IterativeArray<double[]> weights;

    /**
     * Temporary winner location pointer.
     */
    private transient volatile int[] winner;

    /**
     * The distance metric used in the input space.
     */
    private Metric inputMetric;

    /**
     * The distance metric used in the output space.
     */
    private Metric outputMetric;

    /**
     * A neighbourhood function.
     */
    private NeighbourhoodFunction nhFunction;

    /**
     * Class constructor.
     * 
     * @param inputSize
     *            the number of inputs to the map.
     * @param outputDimensions
     *            the number of and size of output dimensions.
     * @param inputMetric
     *            the input metric to use for this class.
     * @param outputMetric
     *            the output metric to use for this class.
     * @param nhFunction
     *            the neighbourhood function that calculates neighbourhood
     *            scalings in this map.
     */
    public MapBaseImpl(Metric inputMetric, Metric outputMetric, NeighbourhoodFunction nhFunction, int inputSize, int... outputDimensions)
    {
        this.setInputMetric(inputMetric);
        this.setOutputMetric(outputMetric);
        this.setNeighbourhoodFunction(nhFunction);
        this.inputDimension = inputSize;
        this.weights = new IterativeArray<double[]>(outputDimensions);
        this.random = new Random();
        this.initWeights();
    }

    /**
     * Class constructor. Automatically crates two EuclideanMetric objects to
     * measure disances in the input and output spaces. Automatically creates a
     * GaussianNeighbourhoodFunction object to scale neighbourhoods.
     * 
     * @param inputSize
     *            the number of inputs to the SOM.
     * @param outputDimensions
     *            the number of and size of output dimensions.
     */
    public MapBaseImpl(int inputSize, int... outputDimensions)
    {
        this(new EuclideanMetric(), new EuclideanMetric(), new GaussianNeighbourhoodFunction(),inputSize, outputDimensions);
    }

    /**
     * @return the inputDimension
     */
    public int getInputDimension()
    {
        return inputDimension;
    }

    /**
     * @param inputDimension
     *            the inputDimension to set
     */
    public void setInputDimension(int inputDimension)
    {
        this.inputDimension = inputDimension;
    }

    /**
     * Access the underlying weight storage implementation.
     * @return the weights
     */
    public IterativeArray<double[]> getWeights()
    {
        return weights;
    }

    /**
     * Create the weights. All weights are initially set to random values
     * between -1 and 1.
     */
    public void initWeights()
    {
        for (int x = 0; x < this.weights.toArray().length; x++)
        {
            double[] tmpArray = new double[this.inputDimension];
            // initialize weight to random values
            if (random != null)
            {
                for (int t = 0; t < this.inputDimension; t++)
                {
                    tmpArray[t] = 0.1*(random.nextDouble() * 2 - 1);
                }
            }
            this.weights.toArray()[x]=tmpArray;
        }
    }

    /**
     * Method for retreiving the weight vector associated with a given node.
     * 
     * @param location
     *            an array indicating the position of the desired node along
     *            each of the SOM's dimensions.
     * @return the weight vector of the node at the given location, any changes
     *         will to the returned object will be reflected in the node.
     */
    public double[] getWeights(int... location)
    {
        return weights.getValue(location);
    }

    /**
     * Method for changing the weight vector associated with a given node.
     * 
     * @param location
     *            an array indicating the position of the desired node along
     *            each of the SOM's dimensions.
     * @param newWeights
     *            the new weights.
     */
    public void setWeights(double[] newWeights,int... location)
    {
        weights.setValue(newWeights, location);
    }

    /**
     * Set the input of this SOM. The input can be classified or used for
     * training.
     * 
     * @param input
     *            the input vector, also called data vector.
     * @see #classify()
     * @see #train()
     */
    public void setInput(double[] input)
    {
        this.input = input;
    }

    /**
     * Classify the most recently applied input according to this SOM.
     * 
     * @see #setInput(double [] input)
     */
    public int[] classify()
    {
        int[] res = new int[this.weights.getDimensions().length];
        findMinDist(res);
        return res;
    }

    /**
     * Classify the supplied input according to this SOM. This is a convenience
     * method, used where one wants to be sure that no other thread modifies the
     * input value before it is classified.
     */
    public int[] classify(double[] input)
    {
        setInput(input);
        return classify();
    }

    /**
     * Helper function for classify. Recursively compares all weight vectors to
     * the input.
     */
    protected double findMinDist( int[] res)
    {
        double minDist = Double.POSITIVE_INFINITY;
        Object [] data = getWeights().toArray();
        int winner = 0;
        for(int x = 0;x<data.length;x++)
        {
            if (getInputMetric().getDistance((double[])data[x], getInput()) < minDist)
            {
                minDist = getInputMetric().getDistance((double[])data[x], getInput());
                winner = x;
            }
        }
        System.arraycopy(getWeights().getPosition(winner),0,res,0,res.length);
        return minDist;
    }

    /**
     * Calculate the value of the neighbourhood function for the node located at
     * loc, given the location of the winner and the neighbourhood size.
     * 
     * @param loc
     *            the location of the node to calculate the neighbourhood for.
     * @param winner
     *            the location of the winner.
     * @param nhSize
     *            the neighbourhood size.
     */
    protected double getNeighbourhoodScaling(final int[] loc, final int[] winner, final double nhSize)
    {
        double dist = this.outputMetric.getDistance(loc, winner);
        return this.nhFunction.getScaling(dist, nhSize);
    }

    /**
     * Creates a new object from the input stream.
     * 
     * @param is
     *            the InputStream to read the object from.
     * @return A new instance created from the stream.
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     */
    public static MapBase read(InputStream is) throws TransformerFactoryConfigurationError, TransformerException, ClassNotFoundException, SecurityException,
            NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        StreamSource ss = new StreamSource(is);
        DOMResult dr = new DOMResult();
        t.transform(ss, dr);
        Element e = (Element) dr.getNode().getFirstChild();
        // extract the number/sixe of input/output dimensions of the map.
        Element dimensionsElement = (Element) e.getElementsByTagName("dimensions").item(0);
        Element input = (Element) dimensionsElement.getElementsByTagName("input").item(0);
        int inputDim = Integer.parseInt(input.getTextContent());
        NodeList outputDimsList = dimensionsElement.getElementsByTagName("output");
        int[] outputDims = new int[outputDimsList.getLength()];
        for (int x = 0; x < outputDims.length; x++)
        {
            outputDims[x] = Integer.parseInt(outputDimsList.item(x).getTextContent());
        }

        // create a new object with the given constructor arguments
        Class<?> somClass = ClassLoader.getSystemClassLoader().loadClass(e.getNodeName());
        Constructor<?> con = somClass.getConstructor(Integer.TYPE, outputDims.getClass());
        MapBase res = (MapBase) con.newInstance(inputDim, outputDims);
        // set the weights
        int[] loc = new int[outputDims.length];
        NodeList nodes = e.getElementsByTagName("node");
        outerloop: for (int node = 0; node < nodes.getLength(); node++)
        {
            // get the weight array of the node
            NodeList weights = ((Element) nodes.item(node)).getElementsByTagName("weight");
            double[] w = new double[weights.getLength()];
            for (int x = 0; x < w.length; x++)
            {
                w[x] = Double.parseDouble(weights.item(x).getTextContent());
            }
            res.setWeights(w,loc);
            // select the next location
            for (int x = loc.length - 1; x >= 0; x--)
            {
                loc[x]++;
                if (loc[x] >= outputDims[x])
                {
                    loc[x] = 0;
                    if (x == 0)
                    {
                        break outerloop;
                    }
                }
                else
                {
                    break;
                }
            }
        }

        /**
         * Add the input metric by using reflection to call getInputMetric(...)
         * on the actual MetricImpl subclass stored in the file.
         */
        Element inputMetricElement = (Element) e.getElementsByTagName("inputmetric").item(0);
        String inputMetricClassName = inputMetricElement.getFirstChild().getTextContent();
        // load the class and find the getInputMetric() method, then invoke it.
        Class<?> metricClass = ClassLoader.getSystemClassLoader().loadClass(inputMetricClassName);
        Method m = metricClass.getMethod("getMetric", Element.class);
        res.setInputMetric((Metric) m.invoke(null, inputMetricElement));

        /**
         * Add the output metric by using reflection to call
         * getOutputMetric(...) on the actual OutputMetricImpl subclass stored
         * in the file.
         */
        Element outputMetricElement = (Element) e.getElementsByTagName("outputmetric").item(0);
        String outputMetricClassName = outputMetricElement.getFirstChild().getTextContent();
        // load the class and find the getOutputMetric() method, then invoke it.
        metricClass = ClassLoader.getSystemClassLoader().loadClass(outputMetricClassName);
        m = metricClass.getMethod("getMetric", Element.class);
        res.setOutputMetric((Metric) m.invoke(null, outputMetricElement));

        /**
         * Add the neighbourhood function by using reflection to call
         * getNeighbourhoodFunction(...) on the actual NeighbourhoodFunctionImpl
         * subclass stored in the file.
         */
        Element nhElement = (Element) e.getElementsByTagName("neighbourhoodfunction").item(0);
        String nhClassName = nhElement.getFirstChild().getTextContent();
        // load the class and find the getOutputMetric() method, then invoke it.
        Class<?> nhClass = ClassLoader.getSystemClassLoader().loadClass(nhClassName);
        m = nhClass.getMethod("getNeighbourhoodFunction", Element.class);
        res.setNeighbourhoodFunction((NeighbourhoodFunction) m.invoke(null, nhElement));

        // implementation-specific read fuctions
        res.readHelper(e);

        return res;
    }

    /**
     * Helper function for read(...) Perform implementation-specific
     * initialization of the new object.
     */
    public abstract void readHelper(Element e);

    /**
     * Create a Document that represents this object, so that it can be
     * exported/serialized.
     * 
     * @throws ParserConfigurationException
     */
    protected Document createDocument() throws ParserConfigurationException
    {
        // prolog
        // <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.newDocument();
        doc.setXmlStandalone(true);
        doc.setXmlVersion("1.0");
        Element e = doc.createElement(getClass().getName());
        e.setAttribute("version", getVersion());
        doc.appendChild(e);
        // write the input dimensions
        Element e1 = doc.createElement("dimensions");
        e.appendChild(e1);
        Element in = doc.createElement("input");
        e1.appendChild(in);
        Text tx = doc.createTextNode("" + this.inputDimension);
        in.appendChild(tx);
        // write the output dimensions
        for (int x = 0; x < weights.getDimensions().length; x++)
        {
            Element out = doc.createElement("output");
            e1.appendChild(out);
            Text outputSize = doc.createTextNode("" + weights.getDimensions()[x]);
            out.appendChild(outputSize);
        }

        // write the weights of the entire map

        Element nodes = doc.createElement("nodes");
        e.appendChild(nodes);
        int[] loc = new int[weights.getDimensions().length];
        outerloop: while (true)
        {
            for (int x = 0; x < weights.getDimensions()[weights.getDimensions().length - 1]; x++)
            {
                loc[loc.length - 1] = x;
                // process the node
                double[] w = this.getWeights(loc);
                Element node = doc.createElement("node");
                nodes.appendChild(node);
                for (int windex = 0; windex < w.length; windex++)
                {
                    Element weight = doc.createElement("weight");
                    node.appendChild(weight);
                    weight.appendChild(doc.createTextNode("" + w[windex]));
                }
            }
            for (int x = loc.length - 2; x >= 0; x--)
            {
                loc[x]++;
                if (loc[x] >= weights.getDimensions()[x])
                {
                    loc[x] = 0;
                    if (x == 0)
                    {
                        break outerloop;
                    }
                }
                else
                {
                    break;
                }
            }
        }
        // add inputmetric
        e.appendChild(this.inputMetric.getElement(doc, "inputmetric"));
        // add outputmetric
        e.appendChild(this.outputMetric.getElement(doc, "outputmetric"));
        // add the neighbourhood function
        e.appendChild(this.nhFunction.getElement(doc));

        // add implementation-specific details.
        createDocumentHelper(e, doc);

        return doc;

    }

    /**
     * Helper function for createDocument(...). Use this to do subclass or
     * implementation-specific processing.
     * 
     * @param e
     *            the root element of the document.
     * @param doc
     *            the document.
     */
    protected abstract void createDocumentHelper(Element e, Document doc);

    /**
     * Write this SOM object to an OutputStream in a format that is
     * version-independent but flexible. The output format is the same that is
     * readable by read(...). The output is in XML format.
     * 
     * @param os
     *            the OutputStream to write to, must be open and writable, and
     *            will remain so after this method call.
     * @throws ParserConfigurationException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     * @throws IOException
     */
    public void write(OutputStream os) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException
    {
        long start = System.currentTimeMillis();
        Document doc = this.createDocument();
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Document created in " + (elapsed / 1000.0) + " seconds.");
        // doc.normalize();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        DOMSource ds = new DOMSource(doc);
        StreamResult sr = new StreamResult(os);
        t.transform(ds, sr);
        os.flush();
    }

    /**
     * Get a string representing the version of this object.
     */
    public String getVersion()
    {
        return "1.0";
    }

    /**
     * Train this map using the most recently applied input. Must be overridden
     * by subclasses.
     * Updates the Winner property.
     */
    public abstract void train();

    /**
     * Train the map using the supplied input.
     * 
     * @param input
     *            the input to use for training.
     */
    public void train(double[] input)
    {
        setInput(input);
        train();
    }

    public Metric getInputMetric()
    {
        return inputMetric;
    }

    public Metric getOutputMetric()
    {
        return outputMetric;
    }

    public void setInputMetric(Metric inputMetric)
    {
        this.inputMetric = inputMetric;
    }

    public void setOutputMetric(Metric outputMetric)
    {
        this.outputMetric = outputMetric;
    }

    /**
     * Get the neighbourhood function.
     * 
     * @return the function used for calculating neighbourhood scalings in this
     *         map.
     */
    public NeighbourhoodFunction getNeighbourhoodFunction()
    {
        return this.nhFunction;
    }

    /**
     * Set the neighbourhood function.
     * 
     * @param nhFunction
     *            the function used for calculating neighbourhood scalings in
     *            this map.
     */
    public void setNeighbourhoodFunction(NeighbourhoodFunction nhFunction)
    {
        this.nhFunction = nhFunction;
    }

    /**
     * Set the random number generator seed. This is useful if one wants to
     * initialize the weights to a known state.
     * 
     * @param seed
     *            the new random seed.
     */
    public void setRandomSeed(long seed)
    {
        this.random.setSeed(seed);
    }

    /**
     * @see org.plsomlib.MapBase#getOutputDimensions()
     */
    public int[] getOutputDimensions()
    {
        return weights.getDimensions();
    }

    public double[] getInput()
    {
        return input;
    }

    public int[] getWinner()
    {
        return winner;
    }

    public void setWinner(int[] winner)
    {
        this.winner = winner;
    }

	/**
	 * @param weights the weights to set
	 */
	protected void setWeights(IterativeArray<double[]> weights)
	{
		this.weights = weights;
	}

	/**
	 * @return the random
	 */
	protected Random getRandom()
	{
		return random;
	}

	/**
	 * Returns the weights of all the nodes as one vector.
	 * @see org.plsomlib.MapBase#getStateVector()
	 */
	public double[] getStateVector()
	{
		//find out how large the result vector will be
		int nodes = this.weights.toArray().length;
		if(nodes > 0)
		{
			nodes*=((double[])this.weights.toArray()[0]).length;
			//create the result array
			double [] res = new double[nodes];
			//copy the node weights into the result array
			int offset =0;
			for(Object o:weights.toArray())
			{
				double [] w = (double[])o;
				System.arraycopy(w, 0, res, offset, w.length);
				offset+=w.length;
			}
			return res;
		}
		else
		{
			//no nodes in map, return empty array
			return new double[0];
		}
	}
	
	

}
