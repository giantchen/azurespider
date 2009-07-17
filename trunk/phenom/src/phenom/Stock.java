package phenom;

import java.util.Arrays;
import java.util.List;

public class Stock {

	private double[] closePrices;
	private double[] openPrices;

	public Stock(List<Double> open, List<Double> close) {
		openPrices = listToArray(open);
		closePrices = listToArray(close);
	}

	private double[] listToArray(List<Double> list) {
		double[] array = new double[list.size()];
		for (int i = 0; i < array.length; ++i) {
			array[i] = list.get(i);
		}
		return array;
	}

	/**
	 * Deep copy of price.
	 */
	public double[] getOpenPrices() {
		return Arrays.copyOf(openPrices, openPrices.length);
	}

	/**
	 * Deep copy of price.
	 */
	public double[] getClosePrices() {
		return Arrays.copyOf(closePrices, closePrices.length);
	}
}
