package phenom.stock.signal.fundmental;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import phenom.stock.signal.ISignal;

public abstract class AbstractFundmentalSignal implements ISignal {
	protected Map<String, Map<String, Double>> values = new HashMap<String, Map<String, Double>>();
	protected String field;
	static private String dbPath = "data/superT_STOCK.sqlite";
	static private String scon = "jdbc:sqlite:" + dbPath;
	
	protected AbstractFundmentalSignal(final String symbol, final String field, final String startDate, final String endDate) {
		init(symbol, field, startDate, endDate);
	}
	
	private void init(final String symbol, final String field, final String startDate, final String endDate) {
		this.field = field;
		
		if (field != null && field.length() > 0)
			values.put(symbol, loadFinanceData(symbol, field));
	}
	
	protected AbstractFundmentalSignal(final List<String> symbols, final String field, final String startDate, final String endDate) {
		for (String symbol : symbols){
			init(symbol, field, startDate, endDate);
		}
	}
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public double calculate(final String symbol, final String date) {
		if (!values.containsKey(symbol))
			return Double.NaN;
		
		// the map is in desc order, so return the first date that <= date
		for (String d : values.get(symbol).keySet()) {
			if (d.compareTo(date) <= 0) {
				return values.get(symbol).get(d);
			}
		}
		return Double.NaN;
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
