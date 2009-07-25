package phenom;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PriceData {
	private static final String SQL = "SELECT Date, Open, High, Low, Close, Amount, Volume, Exchange "
			+ " FROM STOCK_PRICE WHERE Symbol = ? AND Exchange = ? ORDER BY Date";

	static private String dbPath = "data/superT_STOCK.sqlite";
	static private String scon = "jdbc:sqlite:" + dbPath;

	Map<String, Stock> stocks;

	private Connection conn;

	public PriceData() {
		try {
			stocks = new HashMap<String, Stock>();
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(scon);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param symbol
	 */
	private void readFromDatabase(String id) {
		try {
			String[] fields = id.split("\\.");
			String symbol = fields[0];
			String exchange = fields[1];
			PreparedStatement s = conn.prepareStatement(SQL);
			s.setString(1, symbol);
			s.setString(2, exchange);
			ResultSet rs = s.executeQuery();
			List<String> dates = new ArrayList<String>();
			List<Double> openPrices = new ArrayList<Double>();
			List<Double> highPrices = new ArrayList<Double>();
			List<Double> lowPrices = new ArrayList<Double>();
			List<Double> closePrices = new ArrayList<Double>();
			List<Double> amounts = new ArrayList<Double>();
			List<Long> volumnes = new ArrayList<Long>();

			while (rs.next()) {
				dates.add(rs.getString("Date"));
				openPrices.add(rs.getDouble("Open") / 1000.0);
				highPrices.add(rs.getDouble("High") / 1000.0);
				lowPrices.add(rs.getDouble("Low") / 1000.0);
				closePrices.add(rs.getDouble("Close") / 1000.0);
				amounts.add(rs.getDouble("Amount"));
				volumnes.add(rs.getLong("Volume"));
			}
			rs.close();
			stocks.put(id, new Stock(symbol, exchange, dates, openPrices,
					highPrices, lowPrices, closePrices, amounts, volumnes));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Stock getStock(String symbol) {
		if (!stocks.containsKey(symbol)) {
			readFromDatabase(symbol);
		}
		return stocks.get(symbol);
	}
}
