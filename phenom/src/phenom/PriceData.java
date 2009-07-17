package phenom;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceData {
	private static final String SQL = "select Open_Price, Close_Price from STOCK_PRICE where Stock_Id = '600001' and Stock_Ds >= '20080101' and Stock_Ds <= '20081230'";

	Map<String, Stock> stocks;

	public PriceData() throws Exception {
		stocks = new HashMap<String, Stock>();
		Class.forName("org.sqlite.JDBC");
		String dbPath = "data/superT_STOCK.sqlite";
		String scon = "jdbc:sqlite:" + dbPath;
		Connection conn = DriverManager.getConnection(scon);
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(SQL);
		List<Double> openPrices = new ArrayList<Double>();
		List<Double> closePrices = new ArrayList<Double>();
		while (rs.next()) {
			double open = rs.getDouble("Open_Price") / 1000.0;
			double close = rs.getDouble("Close_Price") / 1000.0;
			openPrices.add(open);
			closePrices.add(close);
		}
		rs.close();
		stocks.put("600001", new Stock(openPrices, closePrices));
	}

	public Stock getStockPrice(String id) {
		return stocks.get(id);
	}
}
