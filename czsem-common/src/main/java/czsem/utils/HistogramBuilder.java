package czsem.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HistogramBuilder
{
	List<Double> data = new ArrayList<Double>();
	double min = Double.MAX_VALUE;
	double max = Double.MIN_VALUE;

	public void add(double probability) {
		min = Math.min(min, probability);
		max = Math.max(max, probability);
		data.add(probability);
	}

	public int[] calcHistogram(int numBins)
	{
		final int[] result = new int[numBins];
		final double binSize = (max - min) / numBins;

		for (double d : data) {
			int bin = (int) ((d - min) / binSize); // changed this from numBins
			if (bin < 0) { /* this data is smaller than min */
			} else if (bin >= numBins) { /* this data point is bigger than max */
				if (d == max) result[numBins-1] += 1;
			} else {
				result[bin] += 1;
			}
		}
		return result;
	}
	
	public static int maxBin(int[] hist)
	{
		int ret = Integer.MIN_VALUE;

		for (int i = 0; i < hist.length; i++) {
			ret = Math.max(ret, hist[i]);
		}
		
		return ret;
	}

	public static interface HistogramWriter {
		void write(double left, double right, int count, int cur_stars);
	}  
	
	public void printHistogram(int numBins, int max_stars, HistogramWriter wr) {
		int[] hist = calcHistogram(numBins);
		final double binSize = (max - min) / numBins;
		final int maxBin = maxBin(hist);
		
		for (int i = 0; i < hist.length; i++) {
			double left = min+i*binSize;
			int cur_stars = hist[i] * max_stars / maxBin;
			wr.write(left, left+binSize, hist[i], cur_stars);
		}		
	}
	
	public void printHistogram(int numBins, int max_stars, final PrintStream out) {
		printHistogram(numBins, max_stars, new HistogramWriter() {

			@Override
			public void write(double left, double right, int count, int cur_stars) {
				out.format("( %7.4f  ; %7.4f ) %7d  ", left, right, count);				
				for (int s=0;  s < cur_stars; s++) out.print('*');				
				out.println();				
			}
		});
	}
	
	public static void main(String [] args) 
	{
		HistogramBuilder hbSimple = new HistogramBuilder();
		hbSimple.add(0);
		hbSimple.add(1);		
		hbSimple.printHistogram(5, 50, System.err);
		
		System.err.println("---------------------------------------");

		
		HistogramBuilder hb = new HistogramBuilder();
		
		Random r = new Random();
		
		for (int a=0; a<10000; a++ )
		{
			hb.add(r.nextGaussian());
		}
		
		hb.printHistogram(20, 50, System.err);
		
	}
	
}