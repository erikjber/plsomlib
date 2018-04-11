package org.plsomlib.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * An array with an arbitrary number of dimensions.
 * In mathematical term, this is an implementation of a tensor with arbitrary rank.
 * Any type of object can be stored and retrieved. Each stored object is associated with an n-dimensional position vector, 
 * where n is the rank of the storage tensor.
 * 
 * @author Erik Berglund
 *
 * @param <V> the type of data to store in the array
 */
public class IterativeArray<V> implements Serializable, Iterable<V>
{
	private static final long serialVersionUID = 4126178059844194815L;
	
	private final int[] dimensions;
	private final int[] factors;
	private final Object[] data;
	private final int[][] positions;
	
	private int count;

	/**
	 * Copying constructor.
	 * 
	 * @param dimensions
	 * @param data
	 */
	private IterativeArray(int[] dimensions, int[] factors, int[][] positions, Object[] data)
	{
		this.dimensions = dimensions;
		this.factors = factors;
		this.positions = positions;
		this.data = data;
		count = data.length;
	}

	public IterativeArray(int... dimensions)
	{
		this.dimensions = dimensions;
		this.factors = new int[this.dimensions.length];
		count = 1;
		for (int x = 0; x < factors.length; x++)
		{
			// calculate factors
			factors[x] = count;
			count *= dimensions[x];
		}
		data = new Object[count];
		positions = new int[count][];
	}

	/**
	 * Translates from an n-dimensional position to an offset into the data.
	 * 
	 * @param position
	 * @return
	 */
	public int getOffset(int... position)
	{
		if (position.length != dimensions.length)
		{
			throw new ArrayIndexOutOfBoundsException("Array of " + dimensions.length + " dimensions cannot be accessed by an address of length "
					+ position.length + ".");
		}
		int res = 0;
		for (int x = 0; x < position.length; x++)
		{
			if (position[x] >= dimensions[x])
			{
				throw new ArrayIndexOutOfBoundsException("Index " + position[x] + " out of bounds (" + dimensions[x] + ") at dimension " + x);
			}
			res += position[x] * factors[x];
		}
		return res;
	}

	/**
	 * Translates from an offset into the data to an n-dimensional position.
	 * 
	 * @param offset
	 * @return
	 */
	public int[] getPosition(int offset)
	{
		int[] res = this.positions[offset];
		if (res == null)
		{
			res = new int[factors.length];
			this.positions[offset] = res;
			for (int x = factors.length - 1; x > 0; x--)
			{
				res[x] = offset / factors[x];
				offset -= res[x] * factors[x];
			}
			res[0] = offset / factors[0];
		}
		return res;
	}

	public int[] getDimensions()
	{
		return this.dimensions;
	}
	
	/**
	 * Return the value of at a given offset.
	 * The result is the same as returned by getValue(getPosition(offset)),
	 * but completes faster since it is only an array index lookup.
	 * 
	 * @param offset
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public V getValueFromOffset(int offset)
	{
		return  (V) data[offset];
	}
	
	/**
	 * Set the the value at a particular offset.
	 * The change in the object is the same as for calling setValue(value,getPosition(offset)),
	 * but completes faster since it is only an array index lookup.
	 * 
	 * @param value
	 * @param offset
	 */
	public void setValueAtOffset(V value, int offset)
	{
		data[offset]=value;
	}

	@SuppressWarnings("unchecked")
	public V getValue(int... position)
	{
		return (V) data[getOffset(position)];
	}

	public void setValue(V value, int... position)
	{
		data[getOffset(position)] = value;
	}

	/**
	 * Returns a reference to the actual data stored in the array.
	 * 
	 * @return
	 */
	public Object[] toArray()
	{
		return data;
	}

	/**
	 * Gets a deep copy of this object.
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()
	{
		Object[] dataClone = null;
		// check if the object is an array
		Class<?> objectClass = data[0].getClass();
		if (objectClass.isArray())
		{
			//if array, clone individual values
			dataClone = new Object[data.length];
			for (int x = 0; x < data.length; x++)
			{
				int len = Array.getLength(data[x]);
				Object clone = Array.newInstance(objectClass.getComponentType(), len);
				//transfer all individual values
				for (int i = 0; i < len; i++)
				{
					Array.set(clone, i, Array.get(data[x], i));
				}
				dataClone[x]=clone;
			}
		}
		else
		{
			dataClone = data.clone();
		}
		IterativeArray<V> res = new IterativeArray<V>(dimensions, factors, positions, dataClone);
		return res;
	}

	/**
	 * Return the number of entities in this IterativeArray.
	 * @return the count
	 */
	public int getCount()
	{
		return count;
	}

	/**
	 * Return a custom iterator for this object.
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<V> iterator()
	{
		return new IterativeArrayIterator<V>(this);		
	}
}
