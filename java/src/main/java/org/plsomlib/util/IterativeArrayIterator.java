package org.plsomlib.util;

import java.util.Iterator;

/**
 * An Iterator that allows foreach loops to iterate over IterativeArrays.
 * 
 * @author Erik Berglund
 * @param <V>
 *
 */
public class IterativeArrayIterator<V> implements Iterator<V>
{
	private IterativeArray<V> array;
	private int offset;

	/**
	 * @param array the IterativeArray to iterate over.
	 */
	public IterativeArrayIterator(IterativeArray<V> array)
	{
		this.array = array;
		this.offset = 0;
	}

	@Override
	public boolean hasNext()
	{
		return offset<array.getCount();
	}

	@Override
	public V next()
	{
		return array.getValueFromOffset(offset++);
	}

	@Override
	public void remove()
	{		
		throw new UnsupportedOperationException("IterativeArray cannot be resized.");
	}

}
