package phenom;

import java.util.Arrays;
import java.util.List;

public class Stock {

	private String symbol;
	private String exchange;
	private String[] dates;
	private double[] openPrices;
	private double[] highPrices;
	private double[] lowPrices;
	private double[] closePrices;
	private double[] amounts;
	private long[] volumes;

	public Stock(String sy, String ex, List<String> date, List<Double> open,
			List<Double> high, List<Double> low, List<Double> close,
			List<Double> amount, List<Long> volume) {
		symbol = sy;
		exchange = ex;
		dates = date.toArray(new String[0]);
		openPrices = listToArray(open);
		highPrices = listToArray(high);
		lowPrices = listToArray(low);
		closePrices = listToArray(close);
		amounts = listToArray(amount);
		volumes = listToArray(volume);
	}

	public String getSymbol() {
		return symbol;
	}

	public String getExchange() {
		return exchange;
	}

	public String[] getDates() {
		return Arrays.copyOf(dates, dates.length);
	}

	/**
	 * Deep copy of price.
	 */
	public double[] getOpenPrices() {
		return Arrays.copyOf(openPrices, openPrices.length);
	}

	public double[] getHighPrices() {
		return Arrays.copyOf(highPrices, highPrices.length);
	}

	public double[] getLowPrices() {
		return Arrays.copyOf(lowPrices, lowPrices.length);
	}

	public double[] getClosePrices() {
		return Arrays.copyOf(closePrices, closePrices.length);
	}

	public double[] getAmounts() {
		return Arrays.copyOf(amounts, amounts.length);
	}

	public long[] getVolumnes() {
		return Arrays.copyOf(volumes, volumes.length);
	}

	private static double[] listToArray(List<Double> list) {
		double[] array = new double[list.size()];
		for (int i = 0; i < array.length; ++i) {
			array[i] = list.get(i);
		}
		return array;
	}

	private static long[] listToArray(List<Long> list) {
		long[] array = new long[list.size()];
		for (int i = 0; i < array.length; ++i) {
			array[i] = list.get(i);
		}
		return array;
	}
}
