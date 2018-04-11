package org.plsomlib.util.labelling;

import java.io.Serializable;

import org.plsomlib.MapBase;
import org.plsomlib.util.IterativeArray;

/**
 * Maintains a set of labels (which can be anything) associated with the nodes of a SOM-type object.
 * 
 * @author Erik Berglund
 *
 */
public  class SomLabeller <V> implements Serializable
{
	private static final long serialVersionUID = 8466724409384658959L;
	protected IterativeArray<V> labels;
	public SomLabeller(MapBase map)
	{
		
		labels = new IterativeArray<V>(map.getOutputDimensions());
	}
	
	public void setLabel( V label, int ... location)
	{
		labels.setValue(label,location);
	}
	
	public V getLabel(int ... location)
	{
		return labels.getValue(location);
	}
	
}
