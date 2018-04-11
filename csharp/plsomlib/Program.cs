using System;
using org.plsomlib;

namespace main
{
	class MainClass
	{
		private static int NETWORK_WIDTH = 10;
		private static int NETWORK_HEIGHT = 10;

		public static void Main (string[] args)
		{
			// Create the network
			PLSOM2 plsom2 = new PLSOM2 (2, NETWORK_WIDTH, NETWORK_HEIGHT);
			plsom2.setNeighbourhoodRange (20);

			// Create source of training data
			Random rand = new Random ();
			double[] data = new double[2];

			// Train on randomly generated training data
			for (int x = 0; x < 10000; x++) 
			{
				data [0] = rand.NextDouble();
				data [1] = rand.NextDouble();
				plsom2.train (data);
			}
			Console.WriteLine ("Training complete.");

			// Print the weight locations for each node
			for (int x = 0; x < NETWORK_WIDTH; x++) 
			{
				for (int y = 0; y < NETWORK_HEIGHT; y++) 
				{
					double[] w = plsom2.getWeights ().getValue (x, y);
					Console.WriteLine (w [0] + "," + w [1]);
				}
			}
		}
	}
}
