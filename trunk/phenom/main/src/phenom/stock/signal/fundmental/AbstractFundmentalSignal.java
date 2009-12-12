package phenom.stock.signal.fundmental;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import phenom.database.ConnectionManager;
import phenom.stock.signal.GenericComputableEntry;
import phenom.stock.signal.ISignal;

public abstract class AbstractFundmentalSignal implements ISignal {
	protected Map<String, Map<String, Double>> values = new HashMap<String, Map<String, Double>>();
	protected Map<String, Map<String, Double>> prices = new HashMap<String, Map<String, Double>>();
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	public abstract void addFundmentalData(List<FundmentalData> dataList);
	public void addPrices(List<? extends GenericComputableEntry> priceList) {
		for (GenericComputableEntry data : priceList) {
			Map<String, Double> m = null; 
			if (prices.containsKey(data.getSymbol()))
				m = prices.get(data.getSymbol());
			else {
				m = new TreeMap<String, Double>(new Comparator<String>() {
						@Override
						public int compare(String o1, String o2) {
							return o2.compareTo(o1);
					}
				});
				prices.put(data.getSymbol(), m);
			}
			m.put(data.getDate(), data.getValue());
		}
	}
	
	static public List<FundmentalData> loadFundmentalData(final List<String> symbols, final String startDate, final String endDate) {
		List<FundmentalData> ret = new ArrayList<FundmentalData>();
		Connection conn = ConnectionManager.getConnection();
		
		for (String symbol : symbols) {
			try {
				String SQL_GET_FINANCE_INDICATOR_BY_SYMBOL = "SELECT * FROM STOCK_FINANCE WHERE symbol = ?";
				PreparedStatement statement = conn.prepareStatement(SQL_GET_FINANCE_INDICATOR_BY_SYMBOL);
				statement.setString(1, symbol);
				ResultSet result = statement.executeQuery();
				while (result.next()) {
					FundmentalData data = new FundmentalData();
					data.setAnnounceDate(result.getString("AnnounceDate"));
					data.setCapitalReservPerShare(result.getDouble("CapitalReservePerShare"));
					data.setCashPerShare(result.getDouble("CashPerShare"));
					data.setCurrentAssets(result.getDouble("CurrentAssets"));
					data.setDueDate(result.getString("DueDate"));
					data.setEarningPerShare(result.getDouble("EarningPerShare"));
					data.setFinancingExpense(result.getDouble("EarningPerShare"));
					data.setFixedAssets(result.getDouble("FixedAssets"));
					data.setLongTermDebt(result.getDouble("FixedAssets"));
					data.setNetAssetsPerShare(result.getDouble("NetAssetsPerShare"));
					data.setNetProfit(result.getDouble("NetProfit"));
					data.setPrimeRevenue(result.getDouble("PrimeRevenue"));
					data.setSymbol(result.getString("Symbol"));
					data.setTotalAssets(result.getDouble("TotalAssets"));
					ret.add(data);
					}			
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
}