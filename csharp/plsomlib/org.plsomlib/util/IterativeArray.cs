using System;
using System.Collections;

namespace org.plsomlib.util
{
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
	public class IterativeArray<V> : IEnumerable
	{

		private int[] dimensions;
		private int[] factors;
		private Object[] data;
		private int[][] positions;
		private int offset;

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
			count = data.Length;
		}

		public IterativeArray(params int[] dimensions)
		{
			this.dimensions = dimensions;
			this.factors = new int[this.dimensions.Length];
			count = 1;
			for (int x = 0; x < factors.Length; x++)
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
		public int getOffset(params int[] position)
		{
			if (position.Length != dimensions.Length)
			{
				throw new IndexOutOfRangeException("Array of " + dimensions.Length + " dimensions cannot be accessed by an address of length "
					+ position.Length + ".");
			}
			int res = 0;
			for (int x = 0; x < position.Length; x++)
			{
				if (position[x] >= dimensions[x])
				{
					throw new IndexOutOfRangeException("Index " + position[x] + " out of bounds (" + dimensions[x] + ") at dimension " + x);
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
				res = new int[factors.Length];
				this.positions[offset] = res;
				for (int x = factors.Length - 1; x > 0; x--)
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

		public V getValue(params int[] position)
		{
			return (V) data[getOffset(position)];
		}

		public void setValue(V value, params int[] position)
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
	 * Return the number of entities in this IterativeArray.
	 * @return the count
	 */
		public int getCount()
		{
			return count;
		}

		public IEnumerator GetEnumerator()  
		{
			yield return getValueFromOffset (offset++);
		}

		public void resetEnum()
		{
			offset = 0;
		}
	}
}

