package org.plsomlib.util.labelling;

import java.util.ArrayList;

import javax.vecmath.GVector;

import org.plsomlib.MapBaseImpl;
import org.plsomlib.metrics.EuclideanMetric;
import org.plsomlib.metrics.Metric;
import org.plsomlib.util.Orthonormalisation;


/**
 * Estimates the label of the actual input by interpolating between the labels
 * of the nodes closest to the input.
 * 
 * 
 * @author Erik Berglund
 * 
 */
public class LabelSmoother
{
	private MapBaseImpl map;
	private CentroidLabeller labeller;
	private Metric metric = new EuclideanMetric();

	public LabelSmoother(MapBaseImpl map, CentroidLabeller labeller)
	{
		this.map = map;
		this.labeller = labeller;
	}

	public double[] classify(int[] loc, double[] input) throws Exception
	{
		// get the set of nodes that are closest in each grid dimension
		ArrayList<NodeDistanceTuple> close = findClosestNodesByLabel(loc, input);
		return interpolateByLabel(input, loc, close);
	}

	public double[] classify(double[] input) throws Exception
	{
		// get the location of the node closest to the input
		int[] loc = map.classify(input);
		// get the set of nodes that are closest in each grid dimension
		ArrayList<NodeDistanceTuple> close = findClosestNodes(loc, input);
		return interpolate(input, loc, close);
	}

	public double[] classifyNormalized(double[] input) throws Exception
	{
		// get the set of nodes and distances
		ArrayList<NodeDistanceTuple> close = findDistanceForAllNodes(input);
		return interpolateNormalized(input, close);
	}

	private ArrayList<NodeDistanceTuple> findClosestNodesByLabel(int[] loc,
			double[] input) throws Exception
	{
		ArrayList<NodeDistanceTuple> close = new ArrayList<NodeDistanceTuple>(
				loc.length);
		for (int x = 0; x < loc.length; x++)
		{
			int[] up = loc.clone();
			int[] down = loc.clone();
			while (true)
			{
				up[x]++;
				down[x]--;
				// check edge conditions
				if (up[x] >= map.getOutputDimensions()[x] && down[x] < 0)
				{
					throw new Exception(
							"Can not find label along neighbourhood dimension "
									+ x);
				}

				double[] upLabel = null;
				double[] downLabel = null;
				if (down[x] >= 0)
				{
					downLabel = labeller.getLabel(down);
				}
				if (up[x] < map.getOutputDimensions()[x])
				{
					upLabel = labeller.getLabel(up);
				}

				if (upLabel == null && downLabel == null)
				{
					// both labels are null, keep searching
				}
				else
				{
					if (upLabel == null)
					{
						double downDistance = map.getInputMetric().getDistance(
								downLabel, input);
						close.add(new NodeDistanceTuple(down, downDistance));
					}
					else if (downLabel == null)
					{
						double upDistance = map.getInputMetric().getDistance(
								upLabel, input);
						close.add(new NodeDistanceTuple(up, upDistance));
					}
					else
					{
						// calculate the distance for the labels for each
						double upDistance = map.getInputMetric().getDistance(
								upLabel, input);
						double downDistance = map.getInputMetric().getDistance(
								downLabel, input);
						if (upDistance < downDistance)
						{
							close.add(new NodeDistanceTuple(up, upDistance));
						}
						else
						{
							close.add(new NodeDistanceTuple(down, downDistance));
						}
					}
					break;
				}
			}
		}
		return close;
	}

	private ArrayList<NodeDistanceTuple> findDistanceForAllNodes(double[] input)
	{
		ArrayList<NodeDistanceTuple> close = new ArrayList<NodeDistanceTuple>();
		for (int node = 0; node < map.getWeights().getCount(); node++)
		{
			int[] pos = map.getWeights().getPosition(node).clone();
			if (labeller.getLabel(pos) != null)
			{
				double distance = metric
						.getDistance(input, map.getWeights(pos));
				NodeDistanceTuple tuple = new NodeDistanceTuple(pos, distance);
				close.add(tuple);
			}
		}
		return close;
	}

	private ArrayList<NodeDistanceTuple> findClosestNodes(int[] loc,
			double[] input) throws Exception
	{
		ArrayList<NodeDistanceTuple> close = new ArrayList<NodeDistanceTuple>(
				loc.length);
		for (int x = 0; x < loc.length; x++)
		{
			int[] up = loc.clone();
			int[] down = loc.clone();
			while (true)
			{
				up[x]++;
				down[x]--;
				// check edge conditions
				if (up[x] >= map.getOutputDimensions()[x] && down[x] < 0)
				{
					throw new Exception(
							"Can not find label along neighbourhood dimension "
									+ x);
				}
				else if (down[x] < 0)
				{
					double upDistance = map.getInputMetric().getDistance(
							map.getWeights(up), input);
					close.add(new NodeDistanceTuple(up, upDistance));
				}
				else if (up[x] >= map.getOutputDimensions()[x])
				{
					double downDistance = map.getInputMetric().getDistance(
							map.getWeights(down), input);
					close.add(new NodeDistanceTuple(down, downDistance));
				}
				else
				// no edge condition
				{
					// calculate the distance for the labels for each
					double upDistance = map.getInputMetric().getDistance(
							map.getWeights(up), input);
					double downDistance = map.getInputMetric().getDistance(
							map.getWeights(down), input);
					if (upDistance < downDistance)
					{
						close.add(new NodeDistanceTuple(up, upDistance));
					}
					else
					{
						close.add(new NodeDistanceTuple(down, downDistance));
					}
				}
				// check that we got a finite distance
				if (!Double.isInfinite(close.get(close.size() - 1)
						.getDistance()))
				{
					break;
				}
				else
				{
					close.remove(close.size() - 1);
				}
			}
		}
		return close;
	}

	/**
	 * 
	 * @param input
	 *            in the label space.
	 * @param loc
	 * @param close
	 * @return
	 */
	private double[] interpolateByLabel(double[] input, int[] loc,
			ArrayList<NodeDistanceTuple> close)
	{
		// create a set of vectors from the closest to the nearest
		ArrayList<GVector> inputVectors = new ArrayList<GVector>();
		ArrayList<GVector> labelVectors = new ArrayList<GVector>();
		GVector closestInput = new GVector(map.getWeights(loc));
		GVector closestLabel = new GVector(labeller.getLabel(loc));
		for (NodeDistanceTuple t : close)
		{
			int[] tmpLoc = t.getNodeLocation();
			GVector tmpInput = new GVector(map.getWeights(tmpLoc));
			GVector tmpLabel = new GVector(labeller.getLabel(tmpLoc));
			tmpInput.sub(closestInput);
			tmpLabel.sub(closestLabel);
			inputVectors.add(tmpInput);
			labelVectors.add(tmpLabel);
		}
		// orthonormalise the vectors
		Orthonormalisation.doGramSchmidt(inputVectors);
		Orthonormalisation.doGramSchmidt(labelVectors);

		// calculate a vector from the closest label to the input
		GVector labelVector = new GVector(input);
		labelVector.sub(closestLabel);

		// add scaled vectors
		for (int x = 0; x < inputVectors.size(); x++)
		{
			GVector vec = labelVectors.get(x);
			// find the length of the projection of inputVector onto vec
			double scale = labelVector.dot(vec) / vec.dot(vec);
			GVector scaledW = inputVectors.get(x);
			scaledW.scale(scale);
			closestInput.add(scaledW);
		}

		// copy the interpolated array to an element
		double[] res = new double[closestInput.getSize()];
		for (int x = 0; x < res.length; x++)
		{
			res[x] = closestInput.getElement(x);
		}
		return res;
	}

	private double[] interpolate(double[] input, int[] loc,
			ArrayList<NodeDistanceTuple> close)
	{
		// create a set of vectors from the closest to the nearest
		ArrayList<GVector> inputVectors = new ArrayList<GVector>();
		ArrayList<GVector> labelVectors = new ArrayList<GVector>();
		GVector closestInput = new GVector(map.getWeights(loc));
		GVector closestLabel = new GVector(labeller.getLabel(loc));
		for (NodeDistanceTuple t : close)
		{
			int[] tmpLoc = t.getNodeLocation();
			GVector tmpInput = new GVector(map.getWeights(tmpLoc));
			GVector tmpLabel = new GVector(labeller.getLabel(tmpLoc));
			tmpInput.sub(closestInput);
			tmpLabel.sub(closestLabel);
			inputVectors.add(tmpInput);
			labelVectors.add(tmpLabel);
		}
		// orthonormalise the vectors
		Orthonormalisation.doGramSchmidt(inputVectors);
		Orthonormalisation.doGramSchmidt(labelVectors);

		// calculate a vector from the closest node to the input
		GVector inputVector = new GVector(input);
		inputVector.sub(closestInput);

		// add scaled vectors
		for (int x = 0; x < inputVectors.size(); x++)
		{
			GVector vec = inputVectors.get(x);
			// find the length of the projection of inputVector onto vec
			double scale = inputVector.dot(vec) / vec.dot(vec);
			GVector scaledLabel = labelVectors.get(x);
			scaledLabel.scale(scale);
			closestLabel.add(scaledLabel);
		}

		// copy the interpolated array to an element
		double[] res = new double[closestLabel.getSize()];
		for (int x = 0; x < res.length; x++)
		{
			res[x] = closestLabel.getElement(x);
		}
		return res;
	}

	private double[] interpolateNormalized(double[] input,
			ArrayList<NodeDistanceTuple> close)
	{

		double[] activations = new double[close.size()];
		double minActivation = Double.MAX_VALUE;
		double maxActivation = Double.NEGATIVE_INFINITY;
		int node = 0;
		for (NodeDistanceTuple theNode : close)
		{
			activations[node] = -theNode.getDistance();
			if (activations[node] < minActivation)
			{
				minActivation = activations[node];
			}
			if (activations[node] > maxActivation)
			{
				maxActivation = activations[node];
			}
			node++;
		}
		// normalize activations scale activations to 0-1 range
		double range = maxActivation - minActivation;
		double totalActivations = 0;
		for (node = 0; node < activations.length; node++)
		{
			activations[node] -= minActivation;
			activations[node] /= range;
			// compute the exponential
			activations[node] = Math.exp(Math.pow(activations[node], 16)) - 1;
			totalActivations+=activations[node];
		}

		// normalize activations
		for (node = 0; node < activations.length; node++)
		{
			activations[node]/=totalActivations;
		}

		int firstNonNullOffset = 0;
		while (this.labeller.labels.getValueFromOffset(firstNonNullOffset) == null)
		{
			firstNonNullOffset++;
		}
		double[] res = new double[this.labeller.labels
				.getValueFromOffset(firstNonNullOffset).length];
		for (node = 0; node < activations.length; node++)
		{
			int[] pos = close.get(node).getNodeLocation();
			double[] tmp = this.labeller.getLabel(pos);
			for (int x = 0; x < res.length; x++)
			{
				res[x] += tmp[x] * activations[node];
			}
		}
		return res;
	}

	private class NodeDistanceTuple
	{
		// the location of a node
		private int[] nodeLocation;
		// the distance from this node's input vector to the input.
		private double distance;

		public NodeDistanceTuple(int[] node, double distance)
		{
			this.nodeLocation = node;
			this.distance = distance;
		}

		/**
		 * @return the nodeLocation
		 */
		public int[] getNodeLocation()
		{
			return nodeLocation;
		}

		/**
		 * @return the distance
		 */
		public double getDistance()
		{
			return distance;
		}

	}

}
