package org.plsomlib.util.visualisation;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.plsomlib.MapBase;

/**
 * A Canvas that draws the locations of the weight vectors of a SOM or PLSOM
 * with 2 input and output dimensions and rectangular network structure.
 * 
 * @author Erik Berglund
 * 
 */
public class MapCanvas extends Canvas
{
	private static final long serialVersionUID = -9143504365029430603L;

	protected MapBase map;

	protected BufferedImage backBuffer;

	private Color backGroundColor = Color.white;
	private Color nodeColor = Color.black;
	private Color interConnectColor = Color.black;
	private int nodeSize = 1;
	
	private double minValue = -1;
	private double maxValue = 1;

	/**
	 * Class constructor.
	 * 
	 * @param map
	 *            the map to render each time this canvas is updated.
	 */
	public MapCanvas(MapBase map)
	{
		this.map = map;
	}

	/**
	 * @param backGroundColor
	 *            the backGroundColor to set
	 */
	public void setBackGroundColor(Color backGroundColor)
	{
		this.backGroundColor = backGroundColor;
	}

	/**
	 * @param nodeColor
	 *            the nodeColor to set
	 */
	public void setNodeColor(Color nodeColor)
	{
		this.nodeColor = nodeColor;
	}

	/**
	 * @param interConnectColor
	 *            the interConnectColor to set
	 */
	public void setInterConnectColor(Color interConnectColor)
	{
		this.interConnectColor = interConnectColor;
	}

	/**
	 * Set the radius of the node, in pixels.
	 * 
	 * @param px
	 */
	public void setNodeSize(int px)
	{
		this.nodeSize = px;
	}

	/**
	 * Set the minimum and maximum values of the input space.
	 * @param min
	 * @param max
	 */
	public void setMinMax(double min, double max)
	{
		minValue = min;
		maxValue = max;
	}
	
	/**
	 * 
	 * @see java.awt.Canvas#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g)
	{
		Graphics2D g2 = this.backBuffer.createGraphics();
		int w = getWidth();
		int h = getHeight();
		// clear the back buffer
		g2.setColor(this.backGroundColor);
		g2.fillRect(0, 0, w, h);
		// draw the map
		renderMap(g2, minValue, maxValue, w, h);
		// draw an edge around the map
		g2.drawRect(0, 0, w - 1, h - 1);
		// draw the back buffer to the front buffer
		g.drawImage(this.backBuffer, 0, 0, this);
	}

	/**
	 * Render the map as a series of interconnected points.
	 * 
	 * @param g
	 *            the surface to render to.
	 * @param min
	 *            the minimum value of any component of a weight in the map.
	 * @param max
	 *            the maximum value of any component of a weight in the map.
	 * @param w
	 *            the width of the output.
	 * @param h
	 *            the height of the output.
	 */
	public void renderMap(Graphics2D g, double min, double max, int w, int h)
	{
		double diff = max - min;
		int[] outputDims = map.getOutputDimensions();
		if (outputDims.length == 2)
		{
			int[] current = new int[2];
			int[] next = new int[2];
			double[] w1 = null;
			double[] w2 = null;
			for (int x = 0; x < outputDims[0]; x++)
			{
				current[0] = x;
				for (int y = 0; y < outputDims[1]; y++)
				{
					current[1] = y;
					w1 = this.map.getWeights(current);
					g.setColor(this.interConnectColor);
					if (y < (outputDims[1] - 1))
					{
						next[0] = x;
						next[1] = y + 1;
						w2 = this.map.getWeights(next);
						// draw line
						g.drawLine((int) (w * (w1[0] - min) / diff), (int) (h
								* (w1[1] - min) / diff), (int) (w
								* (w2[0] - min) / diff), (int) (h
								* (w2[1] - min) / diff));
					}
					if (x < (outputDims[0] - 1))
					{
						next[0] = x + 1;
						next[1] = y;
						w2 = this.map.getWeights(next);
						// draw line
						g.drawLine((int) (w * (w1[0] - min) / diff), (int) (h
								* (w1[1] - min) / diff), (int) (w
								* (w2[0] - min) / diff), (int) (h
								* (w2[1] - min) / diff));
					}
					// draw the node
					if (nodeSize > 0)
					{
						g.setColor(this.nodeColor);
						g.fillOval((int) (w * (w1[0] - min) / diff) - nodeSize,
								(int) (h * (w1[1] - min) / diff) - nodeSize,
								2 * nodeSize, 2 * nodeSize);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @see java.awt.Component#setBounds(int, int, int, int)
	 */
	@Override
	public void setBounds(int x, int y, int width, int height)
	{
		// create or re-create the back buffer
		if (width > 0 && height > 0)
		{
			this.backBuffer = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
		}
		super.setBounds(x, y, width, height);
	}

	/**
	 * 
	 * @see java.awt.Canvas#update(java.awt.Graphics)
	 */
	@Override
	public void update(Graphics g)
	{
		// don't do anything, just call paint
		paint(g);
	}

}
