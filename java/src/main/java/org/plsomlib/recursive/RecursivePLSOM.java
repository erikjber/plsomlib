package org.plsomlib.recursive;

import org.plsomlib.*;
import org.plsomlib.metrics.Metric;
import org.plsomlib.neighbourhood.NeighbourhoodFunction;
import org.plsomlib.util.IterativeArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An application of the PLSOM algorithms to the Recursive SOM structure. Part
 * of the input of the PLSOM is the excitation from the previous input.
 * 
 * @author Erik Berglund
 * 
 */
public class RecursivePLSOM extends ExcitationPLSOM
{
	private static final long serialVersionUID = -3745290770456018072L;
	protected boolean useRecovery = true;
	protected double recoveryScaling = 15;

	protected double[] recovery;

	protected double alpha;

	/**
	 * The excitations for this iteration
	 */
	private double[] nuExcitations;

	/**
	 * If this is true the input will be ignored when classifying. Default is
	 * false, i.e. the map functions like a normal SOM.
	 */
	private boolean predict;

	/**
	 * The weights for the recursive input
	 */
	protected IterativeArray<double[]> recursiveWeights;

	/**
	 * Constructor.
	 */
	public RecursivePLSOM(Metric inputMetric, Metric outputMetric, NeighbourhoodFunction nhFunction, double alpha, int inputSize,
			int... outputDimensions)
	{
		super(inputMetric, outputMetric, nhFunction, inputSize, outputDimensions);
		this.alpha = alpha;
	}

	/**
	 * Class constructor.
	 * 
	 * @param inputSize
	 *            the number of inputs to the SOM.
	 * @param outputDimensions
	 *            the number of and size of output dimensions.
	 */
	public RecursivePLSOM(double alpha, int inputSize, int... outputDimensions)
	{
		super(inputSize, outputDimensions);
		this.alpha = alpha;
	}

	/**
	 * Class constructor. Used for reading from file.
	 */
	public RecursivePLSOM(int inputSize, int... outputDimensions)
	{
		super(inputSize, outputDimensions);
	}

	/**
	 * @return the alpha
	 */
	public double getAlpha()
	{
		return alpha;
	}

	/**
	 * @param alpha
	 *            the alpha to set
	 */
	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}

	/**
	 * Since there must be weights for the recursive input, generate them.
	 * 
	 * @see org.plsomlib.MapBaseImpl#initWeights()
	 */
	@Override
	public void initWeights()
	{
		// calculate the number of nodes in the map

		int nodeCount = this.getWeights().toArray().length;

		this.recovery = new double[nodeCount];
		for (int x = 0; x < nodeCount; x++)
		{
			recovery[x] = 1;
		}
		super.initWeights();
		nuExcitations = new double[nodeCount];
		recursiveWeights = new IterativeArray<double[]>(getOutputDimensions());

		for (int x = 0; x < recursiveWeights.toArray().length; x++)
		{
			double[] tmpArray = new double[nodeCount];
			// initialize weight to random values
			for (int t = 0; t < nodeCount; t++)
			{
				tmpArray[t] = 0.01 * (getRandom().nextDouble() * 2 - 1);
			}
			recursiveWeights.toArray()[x] = tmpArray;
		}
	}

	/**
	 * @see org.plsomlib.PLSOM#classify()
	 */
	@Override
	public int[] classify()
	{
		int[] res = super.classify();
		// copy excitations
		System.arraycopy(getNuExcitations(), 0, getExcitations(), 0, getExcitations().length);
		return res;
	}

	/**
	 * Helper function for classify. Recursively compares all weight vectors to
	 * the input. This method overrides findMinDist in MapBaseImpl so that it
	 * also calculates and stores the excitation.
	 */
	@Override
	protected double findMinDist(int[] res)
	{
		double minDist = Double.POSITIVE_INFINITY;
		double minExcitation = 1;
		double maxExcitation = 0;
		Object[] weight = getWeights().toArray();
		int winner = 0;
		for (int x = 0; x < weight.length; x++)
		{
			double distance = (1 - alpha)
					* this.getInputMetric().getDistance(getExcitations(), (double[]) recursiveWeights.toArray()[x]);
			if (!this.isPredict())
			{
				distance += alpha * this.getInputMetric().getDistance(getInput(), (double[]) weight[x]);
			}

			double scale = 1;
			if (useRecovery)
			{
				scale = recovery[x];
			}
			// calculate excitation
			double excitation = Math.exp( -distance);
			this.getNuExcitations()[x] = excitation;
			if ((1 - excitation * scale) < minDist)
			{
				minDist = (1 - Math.exp(-distance) * scale);
				winner = x;
			}
			if (excitation > maxExcitation)
			{
				maxExcitation = excitation;
			}
			if (excitation < minExcitation)
			{
				minExcitation = excitation;
			}
		}
		double diff = maxExcitation - minExcitation;

		// normalise excitations
		for (int x = 0; x < getNuExcitations().length; x++)
		{
			getNuExcitations()[x] -= minExcitation;
			getNuExcitations()[x] /= diff;
		}
		System.arraycopy(getWeights().getPosition(winner), 0, res, 0, res.length);

		if (useRecovery)
		{
			// update recovery values
			for (int x = 0; x < recovery.length; x++)
			{
				recovery[x] += (1 - recovery[x]) / recoveryScaling;
			}
			recovery[winner] = 0;
			for (int x = 0; x < recovery.length; x++)
			{
				getNuExcitations()[x] *= recovery[x];
			}

		}
		return Math.max(minDist, 0);
	}

	/**
	 * Update all weights given the current winner.
	 * 
	 */
	@Override
	protected void updateWeights()
	{
		final double e = getEpsilon()*getLearningScale();
		final double nh = getNeighbourhoodSize();
		Object[] data = this.getWeights().toArray();
		for (int x = 0; x < data.length; x++)
		{
			// calculate the neighbourhood scaling, multiply by epsilon
			double anhc = e * getNeighbourhoodScaling(getWeights().getPosition(x), getWinner(), nh);
			// update the non-recursive weights
			if (!isPredict())
			{
				// get the weight vector
				double[] weight = (double[]) data[x];
				for (int wIndex = 0; wIndex < getInputDimension(); wIndex++)
				{
					weight[wIndex] += anhc * (getInput()[wIndex] - weight[wIndex]);
				}
			}
			// update the recursive weights
			double[] recWeight = (double[]) recursiveWeights.toArray()[x];
			for (int wIndex = 0; wIndex < recWeight.length; wIndex++)
			{
				recWeight[wIndex] += anhc * (getExcitations()[wIndex] - recWeight[wIndex]);
			}
		}
	}

	/**
	 * @return the predict value
	 */
	public boolean isPredict()
	{
		return predict;
	}

	/**
	 * @param predict
	 *            the predict to set
	 */
	public void setPredict(boolean predict)
	{
		this.predict = predict;
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
	protected void createDocumentHelper(Element e, Document doc)
	{
		super.createDocumentHelper(e, doc);
		Element alpha = doc.createElement("alpha");
		alpha.setAttribute("value", "" + getAlpha());
		e.appendChild(alpha);
		double[] excitations = getExcitations();
		for (Double d : excitations)
		{
			Element excite = doc.createElement("excitation");
			excite.setAttribute("value", d.toString());
			e.appendChild(excite);
		}
	}

	/**
	 * @return the nuExcitations
	 */
	protected double[] getNuExcitations()
	{
		return nuExcitations;
	}

	/**
	 * Helper function for read(...) Perform implementation-specific
	 * initialization of the new object.
	 */
	public void readHelper(Element e)
	{
		super.readHelper(e);
		String alpha = e.getElementsByTagName("alpha").item(0).getAttributes().getNamedItem("value").getTextContent();
		this.setAlpha(Double.parseDouble(alpha));
		NodeList exciteList = e.getElementsByTagName("excitation");
		for (int x = 0; x < exciteList.getLength(); x++)
		{
			getExcitations()[x] = Double.parseDouble(exciteList.item(x).getAttributes().getNamedItem("value").getTextContent());
		}
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object clone()
	{
		RecursivePLSOM res = new RecursivePLSOM(this.getInputMetric(), this.getOutputMetric(), this.getNeighbourhoodFunction(),
				this.alpha, this.getInputDimension(), this.getOutputDimensions().clone());
		res.setExcitations(getExcitations().clone());
		res.predict = predict;
		if (recovery != null)
		{
			res.recovery = recovery.clone();
		}
		res.useRecovery = this.useRecovery;
		res.setRho(this.getRho());
		res.setEpsilon(this.getEpsilon());
		res.setLastError(this.getLastError());
		res.setInput(this.getInput().clone());
		res.setWeights((IterativeArray<double[]>) this.getWeights().clone());
		res.setWinner(this.getWinner());
		res.setNeighbourhoodRange(this.getNeighbourhoodRange());
		res.recursiveWeights = (IterativeArray<double[]>) recursiveWeights.clone();
		int x = 0;
		for (double d : getNuExcitations())
		{
			res.getNuExcitations()[x] = d;
			x++;
		}
		return res;
	}

	/**
	 * Creates a new state vector and returns it. The returned state vector is
	 * the state vector of the superclass concatenated with the recovery and
	 * recursive weight values.
	 * 
	 * @see org.plsomlib.ExcitationPLSOM#getStateVector()
	 */
	@Override
	public double[] getStateVector()
	{
		double[] tmp = super.getStateVector();
		// add the recursive weights
		int nodes = this.recursiveWeights.toArray().length;
		int recWeightCount = nodes;
		if (nodes > 0)
		{
			recWeightCount *= ((double[]) this.recursiveWeights.toArray()[0]).length;
		}
		double[] res = null;
		if (this.useRecovery)
		{
			res = new double[tmp.length + nodes + recWeightCount];
		}
		else
		{
			res = new double[tmp.length + recWeightCount];
		}
		// copy the superclass state vector
		System.arraycopy(tmp, 0, res, 0, tmp.length);
		// append the recovery values
		int offset = tmp.length;
		if (this.useRecovery)
		{
			offset += recovery.length;
			System.arraycopy(this.recovery, 0, res, tmp.length, recovery.length);
		}
		// append the recursive weights
		for (Object o : this.recursiveWeights.toArray())
		{
			double[] w = (double[]) o;
			System.arraycopy(w, 0, res, offset, w.length);
			offset += w.length;
		}
		return res;
	}

	/**
	 * Set the recovery scaling, which determines how long it takes for a node
	 * to recover after firing. Default is 15.
	 * 
	 * @param recoveryScaling
	 *            the recoveryScaling to set
	 */
	public void setRecoveryScaling(double recoveryScaling)
	{
		this.recoveryScaling = recoveryScaling;
	}

	/**
	 * @see org.plsomlib.PLSOM#train()
	 */
	@Override
	public void train()
	{
		super.classify();
		// calculate epsilon
		setEpsilon(getLastError() / getRho());
		if (getEpsilon() > 1)
		{
			setRho(getLastError());
			setEpsilon(1);
		}
		// calculate the neighbourhood size
		setNeighbourhoodSize(this.getNeighbourhoodRange() * Math.log(1 + getEpsilon() * (Math.E - 1)));

		// calculate the new weights
		updateWeights();
		// copy the excitations
		System.arraycopy(getNuExcitations(), 0, getExcitations(), 0, getExcitations().length);
	}

	/**
	 * Turn on or off the recovery scaling. Default is on.
	 * 
	 * @param b
	 */
	public void setUseRecovery(boolean b)
	{
		this.useRecovery = b;
	}

	/**
	 * @return the recursiveWeights
	 */
	public IterativeArray<double[]> getRecursiveWeights()
	{
		return recursiveWeights;
	}
}
