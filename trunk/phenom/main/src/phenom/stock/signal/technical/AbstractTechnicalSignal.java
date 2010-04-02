package phenom.stock.signal.technical;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phenom.database.ConnectionManager;
import phenom.stock.Stock;
import phenom.stock.signal.GenericComputableEntry;
import phenom.stock.signal.ISignal;

public abstract class AbstractTechnicalSignal implements ISignal {
	protected Map<String, List<GenericComputableEntry>> prices = new HashMap<String, List<GenericComputableEntry>>();
	protected Map<String, List<GenericComputableEntry>> returns = new HashMap<String, List<GenericComputableEntry>>();
	
	@Override
	public abstract double calculate(String symbol, String date);

	public void addPrice(GenericComputableEntry s_) {
		List<GenericComputableEntry> s = prices.get(s_.getSymbol());
		if (s == null) {
			s = new ArrayList<GenericComputableEntry>();
			prices.put(s_.getSymbol(), s);
		}
		s.add(s_);
		Collections.sort(prices.get(s_.getSymbol()));
		
		s = returns.get(s_.getSymbol());
		if (s == null) {
			s = new ArrayList<GenericComputableEntry>();
			returns.put(s_.getSymbol(), s);
		}
		
		Stock sto = (Stock)s_;
		GenericComputableEntry entry = new GenericComputableEntry(sto.getSymbol(), sto.getDate(), sto.getReturn());
		
		s.add(entry);
		Collections.sort(returns.get(s_.getSymbol()));
	}
	
	public void addPrices(List<? extends GenericComputableEntry> s_) {
		for (GenericComputableEntry s : s_) {
			List<GenericComputableEntry> st = prices.get(s.getSymbol());
			if (st == null) {
				st = new ArrayList<GenericComputableEntry>();
				prices.put(s.getSymbol(), st);
			}
			st.add(s);
			
			st = returns.get(s.getSymbol());
			if (st == null) {
				st = new ArrayList<GenericComputableEntry>();
				returns.put(s.getSymbol(), st);
			}
			Stock sto = (Stock)s;
			GenericComputableEntry entry = new GenericComputableEntry(sto.getSymbol(), sto.getDate(), sto.getReturn());
			
			st.add(entry);
		}
		
		for (String sb : prices.keySet()) {
			Collections.sort(prices.get(sb));
		}
		
		for (String sb : returns.keySet()) {
			Collections.sort(returns.get(sb));
		}
		
	}
	
	static public Map<String, Double> loadRiskFreeInterestRate()
	{
		Map<String, Double> ret = new HashMap<String, Double>(); 
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
			String sQL_GET_RISK_FREE_INTEREST_RATEString = "SELECT * FROM STOCK_RISKFREE_INTEREST";
			PreparedStatement statement;
			try {
				statement = conn.prepareStatement(sQL_GET_RISK_FREE_INTEREST_RATEString);
				ResultSet result = statement.executeQuery();
				while (result.next()) {
					String date = result.getString(2);
					double rate = result.getDouble(3);
					ret.put(date, rate);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return ret;
	}

	protected Map<String, List<GenericComputableEntry>> getPrices() {
		return prices;
	}

	public boolean isTradeDate(String symbol, String date) {
		if (Collections.binarySearch(getPrices().get(symbol), new GenericComputableEntry(symbol, date, -1)) < 0) {
			return false;
		} else {
			return true;
		}
	}
}
