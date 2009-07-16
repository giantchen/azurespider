package phenom;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import phenom.utils.graph.TimeSeriesGraph;

public class Hello {
	@BeforeClass
	public static void setup() {
		System.out.println("setup.");
	}

	@AfterClass
	public static void tearDown() {
		System.out.println("tear down.");
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
	public void NaiveStrategy() throws ClassNotFoundException, InterruptedException {
		Class.forName("org.sqlite.JDBC");
		String dbPath = "data/superT_STOCK.sqlite";
		String scon = "jdbc:sqlite:" + dbPath;
		try {
		Connection conn = DriverManager.getConnection(scon);
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery("select Open_Price, Close_Price from STOCK_PRICE where Stock_Id = '600001' and Stock_Ds >= '20080101' and Stock_Ds <= '20081230'");
		double average = 0;
		int day = 0;
		double cash = 100000; // Initial money
		double ratio = 0; // Normalize 
		int stocks = 0;
		List<Double> prices = new ArrayList<Double>();
		List<Double> cashes = new ArrayList<Double>();
		List<Double> averages = new ArrayList<Double>();
		
		while (rs.next()) {
			double open = rs.getDouble("Open_Price") / 1000.0;
			double close = rs.getDouble("Close_Price") / 1000.0;
			average = (average * day + close) / (day + 1);
			day++;
			if (ratio == 0)
				ratio = cash / close;
			
			// if the open price is 10% lower than the average, buy as much as possible 
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
			System.out.println("open: " + open + " close: " + close + " " + " average: " + average + " cash: " + cash + " stocks: " + stocks + " total money: " + cashes.get(cashes.size() - 1));
		}
		
		rs.close();
		
		TimeSeriesGraph graph = new TimeSeriesGraph("Stock", "Date", "Price & Money");
		graph.addDataSource("600010", prices);
		graph.addDataSource("Average", averages);
		graph.addDataSource("MyMoney", cashes);
		graph.display();
		
		Thread.sleep(10 * 1000);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
