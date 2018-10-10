package org.plsomlib.recursive;

import java.util.Random;

import org.plsomlib.metrics.EuclideanMetric;
import org.plsomlib.metrics.Metric;
import org.plsomlib.metrics.SquaredEuclideanMetric;
import org.plsomlib.neighbourhood.GaussianNeighbourhoodFunction;
import org.plsomlib.neighbourhood.NeighbourhoodFunction;
import org.plsomlib.util.DiameterBuffer;
import org.plsomlib.util.IterativeArray;

/**
 * @author Erik Berglund
 *
 */
public class NewMulitlayerRecursivePLSOM2
{
    // Weights 
    protected IterativeArray<double[]> directWeights;
    protected IterativeArray<double[]> selfWeights;
    protected IterativeArray<double[]> feedbackWeights;
    
    // Diameter buffers
    protected DiameterBuffer directDiameterBuffer = new DiameterBuffer();
    protected DiameterBuffer selfDiameterBuffer = new DiameterBuffer();
    protected DiameterBuffer feedbackDiameterBuffer = new DiameterBuffer();
    
    protected double [] excitations;
    protected double [] nuExcitations;
    
    // How much weight to give to the direct weights relative to the selfWeights+feedbackWeights;
    private double alpha;
    private NewMulitlayerRecursivePLSOM2 otherNet;
    private int[] outputDimensions;
    
    protected Random rand = new Random();
    private int inputDimension;
    private double[] input;
    private double lastError;
    private int[] winner;
    
    private Metric inputMetric = new SquaredEuclideanMetric();
    private Metric outputMetric = new EuclideanMetric();
    private NeighbourhoodFunction nhFunction = new GaussianNeighbourhoodFunction();
    private boolean predict;
    private double epsilon;
    private double neighbourhoodRange;
    private double neighbourhoodSize;
    
    
    
    /**
     * 
     */
    public NewMulitlayerRecursivePLSOM2( double alpha,int inputDim, double neighbourhoodRange, int ... dimensions)
    {
        this.alpha = alpha;
        this.outputDimensions = dimensions;
        this.inputDimension = inputDim;
        this.neighbourhoodRange = neighbourhoodRange;
        directWeights = new IterativeArray<>(dimensions);
        selfWeights = new IterativeArray<>(dimensions);
        

        excitations = new double[directWeights.getCount()];
        nuExcitations = new double[directWeights.getCount()];
        
        initWeights();
    }
    
    public int [] getOutputDimensions()
    {
        return outputDimensions;
    }
    
    /**
     * Assign small random values to the direct and self weights
     */
    protected void initWeights()
    {
        int nodeCount = this.directWeights.toArray().length;

        for (int x = 0; x < nodeCount; x++)
        {
            double[] tmpArray = new double[inputDimension];
            // initialize weight to random values
            for (int t = 0; t < tmpArray.length; t++)
            {
                tmpArray[t] = 0.1*(rand.nextDouble() * 2 - 1);
            }
            this.directWeights.setValueAtOffset(tmpArray, x);
        }
        for (int x = 0; x < this.selfWeights.toArray().length; x++)
        {
            double[] tmpArray = new double[nodeCount];
            // initialize weight to random values
            for (int t = 0; t < tmpArray.length; t++)
            {
                tmpArray[t] = 0.1*(rand.nextDouble() * 2 - 1);
            }
            this.selfWeights.setValueAtOffset(tmpArray, x);
        }
    }
    
    /**
     * Assign small random values to the weights leading back from the next layer.
     */
    protected void initFeedbackWeights()
    {
        int nodeCount = otherNet.directWeights.toArray().length;
        for (int x = 0; x < this.feedbackWeights.toArray().length; x++)
        {
            double[] tmpArray = new double[nodeCount];
            // initialize weight to random values
            for (int t = 0; t < tmpArray.length; t++)
            {
                tmpArray[t] = 0.1*(rand.nextDouble() * 2 - 1);
            }
            this.feedbackWeights.setValueAtOffset(tmpArray, x);
        }
    }
    
    public void setOtherNet(NewMulitlayerRecursivePLSOM2 other)
    {
        this.otherNet = other;
        feedbackWeights = new IterativeArray<>(other.getOutputDimensions());
        initFeedbackWeights();
    }
    
    public double [] getExcitations()
    {
        return excitations;
    }

    public int[] classify()
    {
        int[] res = new int[getOutputDimensions().length];
        setLastError(findMinDist(res));
        setWinner(res);
        return res;
    }
    
    public void train(double [] input)
    {
        setInput(input);
        setWinner(classify());
        // calculate epsilon
        if (getLastError() == 0)
        {
            setEpsilon(0);
        }
        else
        {
            setEpsilon(getLastError() / getMaxDiameter());
            if (getEpsilon() > 1)
            {
                setEpsilon(1);
            }
        }
        // calculate the neighbourhood size
        setNeighbourhoodSize(neighbourhoodRange * Math.log(1 + getEpsilon() * (Math.E - 1)));

        // calculate the new weights
        updateWeights();
        // copy excitations
        System.arraycopy(nuExcitations, 0, excitations, 0, excitations.length);
    }
   
    protected void updateWeights()
    {
        Object[] data = directWeights.toArray();
        for (int x = 0; x < data.length; x++)
        {
            // calculate the neighbourhood scaling, multiply by epsilon
            double anhc = getEpsilon() * getNeighbourhoodScaling(directWeights.getPosition(x), getWinner(), getNeighbourhoodSize());
            if(!isPredict())
            {
                double[] weight = (double[]) data[x];
                // update the weights
                for (int wIndex = 0; wIndex < weight.length; wIndex++)
                {
                    weight[wIndex] += anhc * (input[wIndex] - weight[wIndex]);
                }
            }
            double[] selfW = selfWeights.getValueFromOffset(x);
            for (int wIndex = 0; wIndex < selfW.length; wIndex++)
            {
                selfW[wIndex] += anhc * (excitations[wIndex] - selfW[wIndex]);
            }
            if(this.feedbackWeights != null)
            {
                double[] feedbackW = feedbackWeights.getValueFromOffset(x);
                for (int wIndex = 0; wIndex < feedbackW.length; wIndex++)
                {
                    feedbackW[wIndex] += anhc * (otherNet.getExcitations()[wIndex] - feedbackW[wIndex]);
                }
            }
        }
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
    
    protected double getNeighbourhoodSize()
    {
        return this.neighbourhoodSize;
    }

    protected void setNeighbourhoodSize(double d)
    {
        this.neighbourhoodSize = d;
    }

    protected void setEpsilon(double e)
    {
        epsilon = e;
    }
    protected double getEpsilon()
    {
        return epsilon;
    }

    protected double getMaxDiameter()
    {
        double direct = alpha;
        double indirect = 1-direct;
        if(isPredict())
        {
            indirect = 1;
        }
        if(this.otherNet!=null)
        {
            indirect/=2;
        }
        double res = this.selfDiameterBuffer.getMaxDiameter()*indirect;
        if(!isPredict())
        {
            res+=this.directDiameterBuffer.getMaxDiameter()*direct;
        }
        if(this.otherNet!=null)
        {
            res+=this.feedbackDiameterBuffer.getMaxDiameter()*indirect;
        }
        return res;
    }

    private void setWinner(int[] res)
    {
        this.winner = res;
    }
    
    private int [] getWinner()
    {
        return this.winner;
    }

    private void setLastError(double minDist)
    {
        this.lastError = minDist;
    }
    
    protected double getLastError()
    {
        return this.lastError;
    }

    /**
     * Helper function for classify. Recursively compares all weight vectors to
     * the input. Returns the minimum distance, i.e. the error.
     */
    protected double findMinDist( int[] res)
    {
        double minDist = Double.POSITIVE_INFINITY;
   
        double direct = alpha;
        double indirect = 1-alpha;
        if(isPredict())
        {
            indirect = 1;
        }
        if(otherNet != null)
        {
            indirect/=2;
        }
        
        double totalExpExitation = 0;
        int winner = 0;
        Object [] directWeightsArray = directWeights.toArray();
        for(int x = 0;x<directWeightsArray.length;x++)
        {
            double [] directW = (double[])directWeightsArray[x];
            double [] selfW = (double[])selfWeights.getValueFromOffset(x);
            double [] feedbackW = null;
            if(this.feedbackWeights != null)
            {
                feedbackW = (double[])feedbackWeights.getValueFromOffset(x);
            }
            
            double dist = inputMetric.getDistance(selfW, getExcitations())*indirect;
            if(!isPredict())
            {
                dist += inputMetric.getDistance(directW, input)*direct;
            }
            if(this.feedbackWeights!=null)
            {
                dist += inputMetric.getDistance(feedbackW, otherNet.getExcitations())*indirect;
            }
            double excitation = Math.exp(-dist);
            nuExcitations[x] = excitation;
            totalExpExitation += Math.exp(excitation);
            if(dist<minDist)
            {
                minDist = dist;
                winner = x;
            }
        }
        System.arraycopy(directWeights.getPosition(winner),0,res,0,res.length);
        for (int node = 0; node < nuExcitations.length; node++)
        {
            nuExcitations[node] = Math.exp(nuExcitations[node])/totalExpExitation;
        }
        return minDist;
    }
    
    private boolean isPredict()
    {
        return predict;
    }
    public void setPredict(boolean p)
    {
        predict = p;
    }


    private void setInput(double[] input)
    {
        this.input = input;
        if(!this.isPredict())
        {
            this.directDiameterBuffer.updateBuffer(input);
        }
        this.selfDiameterBuffer.updateBuffer(excitations);
        if(this.otherNet!=null)
        {
            this.feedbackDiameterBuffer.updateBuffer(otherNet.excitations);
        }
    }

    public void train()
    {
        train(null);
    }

}
