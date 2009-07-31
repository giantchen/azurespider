package phenom;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import phenom.utils.graph.TimeSeriesGraph;

public class Hello {
	static PriceData priceData;

	private String[] dates;
	private double[] closePrices;
	private double[] openPrices;

	@BeforeClass
	public static void setup() throws Exception {
		System.out.println("setup.");
		priceData = new PriceData();
	}

	@AfterClass
	public static void tearDown() {
		System.out.println("tear down.");
	}

	@Before
	public void before() {
		System.out.println("before a case");
		Stock stock = priceData.getStock("600001.sh");
		dates = stock.getDates();
		closePrices = stock.getClosePrices();
		openPrices = stock.getOpenPrices();
	}

	@After
	public void after() {
		System.out.println("after a case");
	}

	@Test
	public void hello() {
		System.out.println("Hello world.");
	}

	@Test
	public void world() {
		System.out.println("Hello world2.");
		assertEquals(true, true);
	}

	@Test
	public void NaiveStrategy() {
		double average = 0;
		int day = 0;
		double cash = 100000; // Initial money
		double ratio = 0; // Normalize
		int stocks = 0;
		List<Double> prices = new ArrayList<Double>();
		List<Double> cashes = new ArrayList<Double>();
		List<Double> averages = new ArrayList<Double>();

		for (int i = 0; i < openPrices.length; ++i) {
			if (dates[i].compareTo("20080101") < 0)
				continue;
			if (dates[i].compareTo("20091231") > 0)
				break;

			double open = openPrices[i];
			double close = closePrices[i];
			average = (average * day + close) / (day + 1);
			day++;
			if (ratio == 0)
				ratio = cash / close;

			// if the open price is 10% lower than the average, buy as much as
			// possible
			// if the close price is 10% higher than the average, sell all
			if (open / average >= 1.1) {
				// sell all
				cash += stocks * open;
				stocks = 0;
			} else if (open / average <= 0.9) {
				// buy as much as possible
				stocks = (int) (cash / open / 100);
				cash -= stocks * open;
			}
			prices.add(close);
			cashes.add((cash + stocks * close) / ratio);
			averages.add(average);
			System.out
					.println("open: " + open + " close: " + close + " "
							+ " average: " + average + " cash: " + cash
							+ " stocks: " + stocks + " total money: "
							+ cashes.get(cashes.size() - 1));
		}

		TimeSeriesGraph graph = new TimeSeriesGraph("Stock", "Date",
				"Price & Money");
		graph.addDataSource("600001", prices);
		graph.addDataSource("Average", averages);
		graph.addDataSource("MyMoney", cashes);
		graph.display();

		sleepSeconds(10);
	}

	private static void sleepSeconds(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
		}
	}
}
