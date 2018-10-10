package org.plsomlib.recursive;

import org.plsomlib.metrics.Metric;
import org.plsomlib.neighbourhood.NeighbourhoodFunction;
import org.plsomlib.util.IterativeArray;

/**
 * This doesn't work. See NewMultilayerRecursivePLSOM2 instead.
 * @author Erik Berglund
 *
 */
public class MultilayerRecursivePLSOM2 extends RecursivePLSOM2
{
    private MultilayerRecursivePLSOM2 otherNet;
    private IterativeArray otherWeights;
    
    /**
     * @param inputMetric
     * @param outputMetric
     * @param nhFunction
     * @param alpha
     * @param inputSize
     * @param outputDimensions
     */
    public MultilayerRecursivePLSOM2(Metric inputMetric, Metric outputMetric, NeighbourhoodFunction nhFunction,
            double alpha, int inputSize, int... outputDimensions)
    {
        super(inputMetric, outputMetric, nhFunction, alpha, inputSize, outputDimensions);
    }

    /**
     * @param alpha
     * @param inputSize
     * @param outputDimensions
     */
    public MultilayerRecursivePLSOM2(double alpha, int inputSize, double nhRange,int... outputDimensions)
    {
        super(alpha, inputSize,nhRange, outputDimensions);
    }
    
    public void setOtherNet(MultilayerRecursivePLSOM2 net)
    {
        this.otherNet = net;
        
        // Create the othernet weights
        int nodeCount = otherNet.getExcitations().length;
        this.otherWeights = new IterativeArray<>(getOutputDimensions());

        for (int x = 0; x < this.otherWeights.toArray().length; x++)
        {
            double[] tmpArray = new double[nodeCount];
            // initialize weight to random values
            if (random != null)
            {
                for (int t = 0; t < nodeCount; t++)
                {
                    tmpArray[t] = 0.1*(random.nextDouble() * 2 - 1);
                }
            }
            this.otherWeights.toArray()[x]=tmpArray;
        }
    }
}
