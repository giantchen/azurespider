package phenom.stock.strategy.eric;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import phenom.stock.trading.MyStock;

public abstract class BasicFinanceReportIndicator {
	protected Map<String, Double> indicators;
	protected MyStock stock;
	protected String symbol, field;
	static private String dbPath = "data/superT_STOCK.sqlite";
	static private String scon = "jdbc:sqlite:" + dbPath;
	// protected EMovingAverage ea;
	
	protected BasicFinanceReportIndicator(final String symbol, final String field, final String startDate, final String endDate) {
		this.symbol = symbol;
		this.field = field;
		this.stock = new MyStock(symbol, startDate, endDate);
		if (field != null && field.length() > 0)
			indicators = loadFinanceData(symbol, field);
	}

	public String getSymbol() {
		return symbol;
	}
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	public FinanceIndicator getIndicator(final String date) {
		// the map is in desc order, so return the first date that <= date
		for (String d : indicators.keySet()) {
			if (d.compareTo(date) <= 0) {
				if (isTradeDay(date))
					return new FinanceIndicator(symbol, indicators.get(d));
				else
					return null;
			}
		}
		return null;
	}
	
	public boolean isTradeDay(String date) {
		return stock.isTradeDate(date);
	}

	protected Map<String, Double> loadFinanceData(final String symbol, final String field) {
		Map<String, Double> ret = new TreeMap<String, Double>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}
		});
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(scon);
		} catch (Exception e) {
			e.printStackTrace();
			return ret;
		}
		
		try {
			String SQL_GET_FINANCE_INDICATOR_BY_SYMBOL = "SELECT AnnounceDate, " + field + " FROM STOCK_FINANCE WHERE symbol = ?";
			PreparedStatement statement = conn.prepareStatement(SQL_GET_FINANCE_INDICATOR_BY_SYMBOL);
			statement.setString(1, symbol);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				String date = result.getString(1);
				Double value = result.getDouble(2);
				ret.put(date, value);
				}			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
}
