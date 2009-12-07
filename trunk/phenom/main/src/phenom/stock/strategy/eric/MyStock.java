package phenom.stock.strategy.eric;

import java.math.BigDecimal;
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

public class MyStock {	
	static private final String dbPath = "data\\SuperT_STOCK.sqlite";
	static private final String SQL_GETPRICE = "SELECT Date, Open, Close, High, Low, Weight FROM STOCK_PRICE WHERE Symbol = ?";
	static private final String SQL_GETPRICE_BY_DATE = "SELECT Date, Open, Close, High, Low, Weight FROM STOCK_PRICE WHERE Symbol = ? AND Date >= ? AND Date <= ?";
	static private final String SQL_BONUS_INFO = "SELECT * from STOCK_BONUS WHERE Symbol = ?";
	static private final String SQL_ALLOC_INFO = "SELECT * from STOCK_ALLOC WHERE Symbol = ?";
	static private final String SQL_BONUS_INFO_BY_DATE = "SELECT * from STOCK_BONUS WHERE Symbol = ? AND RegDate >= ? AND RegDate <= ?";
	static private final String SQL_ALLOC_INFO_BY_DATE = "SELECT * from STOCK_ALLOC WHERE Symbol = ? AND RegDate >= ? AND RegDate <= ?";
	static private final String SQL_GET_MARKET_OPEN_DATE = "SELECT Date from STOCK_PRICE WHERE Symbol = '000001.sh'";
	private String symbol;
	private Map<String, Double> openPrices, closePrices, highPrices, lowPrices;
	private Map<String, Double> openPricesWeighted, closePricesWeighted, highPricesWeighted, lowPricesWeighted;
	private List<String> tradeDates, marketOpenDates, xDates;
	private Map<String, Map<String, Double>> regDates;
	private String startDate, endDate;
	
	public MyStock(final String symbol) {
		this(symbol, null, null);
	}
	
	public MyStock(final String symbol, final String startDate, final String endDate) {
		init(symbol, startDate, endDate);
		
		// Get prices (weighted and non-weighted) information from the database
		getPrices(symbol, startDate, endDate);
		
		// Get market open dates
		getMarketOpenDates();
		
		// Get bonus information from the database
		getBonusInfo(symbol, startDate, endDate);
		
		// Get alloc information from the database
		getAllocateInfo(symbol, startDate, endDate);
		
		// Get industry information from the database
		
		// Get basic information from the database
		
		Collections.sort(xDates);
	}
	
	private void getMarketOpenDates() {
		Connection conn = ConnectionManager.getConnection(dbPath);
		try {
			PreparedStatement s = conn.prepareStatement(SQL_GET_MARKET_OPEN_DATE);			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				String date = rs.getString(1);
				marketOpenDates.add(date);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}

	public String getSymbol() {
		return this.symbol;
	}
	
	public List<String> getTradeDates() {
		return this.tradeDates;
	}
	
	public boolean isTradeDate(final String date) {
		return tradeDates.contains(date);
	}
	
	public double getOpenPrice(final Integer date) throws Exception {
		return getOpenPrice(date.toString());
	}
	
	public double getOpenPrice(final Integer date, boolean weighted) throws Exception {
		return getOpenPrice(date.toString(), weighted);
	}
	
	public double getOpenPrice(final String date) throws Exception {
		if (!openPrices.containsKey(date))
			throw new Exception("No open price for symbol [" + symbol + "] date [" + date + "]");
		
		return openPrices.get(date);
	}
	
	public double getOpenPrice(final String date, boolean weighted) throws Exception {
		if (!weighted)
			return getOpenPrice(date);
		
		if (!openPricesWeighted.containsKey(date))
			throw new Exception("No open price for symbol [" + symbol + "] date [" + date + "]");
		
		return openPricesWeighted.get(date);
	}
	
	public double getClosePrice(final String date) {
		if (!closePrices.containsKey(date)) {
			System.err.println("No close price for symbol [" + symbol + "] date [" + date + "]");
			return 0;
		}
		
		return closePrices.get(date);
	}
	
	public double getClosePrice(final String date, boolean weighted) {
		if (!weighted)
			return getClosePrice(date);
		
		if (!closePricesWeighted.containsKey(date)) {
			System.err.println("No close price for symbol [" + symbol + "] date [" + date + "]");
			return 0;
		}
		
		return closePricesWeighted.get(date);
	}
	
	public double getHighPrice(final String date) throws Exception {
		if (!highPrices.containsKey(date))
			throw new Exception("No high price for symbol [" + symbol + "] date [" + date + "]");
		
		return highPrices.get(date);
	}
	
	public double getHighPrice(final String date, boolean weighted) throws Exception {
		if (!weighted)
			return getHighPrice(date);
		
		if (!highPricesWeighted.containsKey(date))
			throw new Exception("No high price for symbol [" + symbol + "] date [" + date + "]");
		
		return highPricesWeighted.get(date);
	}
	
	public double getLowPrice(final String date) throws Exception {
		if (!lowPrices.containsKey(date))
			throw new Exception("No low price for symbol [" + symbol + "] date [" + date + "]");
		
		return lowPrices.get(date);
	}
	
	public double getLowPrice(final String date, boolean weighted) throws Exception {
		if (!weighted)
			return getLowPrice(date);
		
		if (!lowPricesWeighted.containsKey(date))
			throw new Exception("No low price for symbol [" + symbol + "] date [" + date + "]");
		
		return lowPricesWeighted.get(date);
	}
	
	private void init(final String symbol, final String startDate, final String endDate) {
		this.symbol = symbol;
		openPrices = new HashMap<String, Double>();
		closePrices = new HashMap<String, Double>();
		highPrices = new HashMap<String, Double>();
		lowPrices = new HashMap<String, Double>();
		openPricesWeighted = new HashMap<String, Double>();
		closePricesWeighted = new HashMap<String, Double>();
		highPricesWeighted = new HashMap<String, Double>();
		lowPricesWeighted = new HashMap<String, Double>();
		tradeDates = new ArrayList<String>();
		marketOpenDates = new ArrayList<String>();
		regDates = new HashMap<String, Map<String, Double>>();
		xDates = new ArrayList<String>();
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	private void getPrices(final String symbol, final String startDate, final String endDate) {
		Connection conn = ConnectionManager.getConnection(dbPath);
		try {
			PreparedStatement s = null;
			if (startDate == null || endDate == null) {
				s = conn.prepareStatement(SQL_GETPRICE);
				s.setString(1, symbol);
			} else {
				s = conn.prepareStatement(SQL_GETPRICE_BY_DATE);
				s.setString(1, symbol);
				s.setString(2, startDate);
				s.setString(3, endDate);
			}
			
			ResultSet rs = s.executeQuery();
			int count = 0;
			while (rs.next()) {
				String date = rs.getString(1);
				tradeDates.add(date);
				if (count++ == 0)
					this.startDate = date;
				this.endDate = date;
				
				double weight = rs.getDouble("Weight"); // Weight
				double price = rs.getDouble("Open"); // open
				openPrices.put(date, price);
				openPricesWeighted.put(date, new BigDecimal(price * weight).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
				
				price = rs.getDouble("Close"); // close
				closePrices.put(date, price);
				closePricesWeighted.put(date, new BigDecimal(price * weight).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
				
				price = rs.getDouble("High"); // high
				highPrices.put(date, price);
				highPricesWeighted.put(date, new BigDecimal(price * weight).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
				
				price = rs.getDouble("Low"); // Low
				lowPrices.put(date, price);
				lowPricesWeighted.put(date, new BigDecimal(price * weight).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());				
			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void getBonusInfo(final String symbol, final String startDate, final String endDate) {
		Connection conn = ConnectionManager.getConnection(dbPath);
		try {
			PreparedStatement s = null;
			if (startDate == null || endDate == null) {
				s = conn.prepareStatement(SQL_BONUS_INFO);
				s.setString(1, symbol);
			} else {
				s = conn.prepareStatement(SQL_BONUS_INFO_BY_DATE);
				s.setString(1, symbol);
				s.setString(2, startDate);
				s.setString(3, endDate);
			}
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				Map<String, Double> m = null;
				String regDate = rs.getString("RegDate");
				if (regDates.containsKey(regDate))
					m = regDates.get(regDate);
				else
					m = new HashMap<String, Double>();
				m.put("BonusShares", rs.getDouble("BonusShare"));
				m.put("BonusCash", rs.getDouble("Dividend"));
				m.put("TranShares", rs.getDouble("TransitShare"));
				regDates.put(regDate, m);
				xDates.add(rs.getString("XDate"));
			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void getAllocateInfo(final String symbol, final String startDate, final String endDate) {
		Connection conn = ConnectionManager.getConnection(dbPath);
		try {
			PreparedStatement s = null;
			if (startDate == null || endDate == null) {
				s = conn.prepareStatement(SQL_ALLOC_INFO);
				s.setString(1, symbol);
			} else {
				s = conn.prepareStatement(SQL_ALLOC_INFO_BY_DATE);
				s.setString(1, symbol);
				s.setString(2, startDate);
				s.setString(3, endDate);
			}
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {	
				Map<String, Double> m = null;
				String regDate = rs.getString("RegDate");
				if (regDates.containsKey(regDate))
					m = regDates.get(regDate);
				else
					m = new HashMap<String, Double>();
				m.put("AllocShares", rs.getDouble("AllocShare"));
				m.put("AllocPrice", rs.getDouble("AllocPrice"));
				regDates.put(regDate, m);
				xDates.add(rs.getString("XDate"));
			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public String getEndDate() {
		return this.endDate;
	}
	
	public String getStartDate() {
		return this.startDate;
	}

	public Integer getNextMarketOpenDate(Integer currentDate) {
		int index = marketOpenDates.indexOf(currentDate.toString());
		if (index >= 0)
			return Integer.parseInt(marketOpenDates.get(index + 1));
		else 
			return 0;
	}

	public boolean isXRegDate(Integer currentDate) {
		return regDates.containsKey(currentDate.toString());
	}

	public double getBonusCash(Integer currentDate) {
		if (!regDates.get(currentDate.toString()).containsKey("BonusCash"))
			return 0;
		else
			return regDates.get(currentDate.toString()).get("BonusCash");
	}

	public double getBonusShares(Integer currentDate) {
		if (!regDates.get(currentDate.toString()).containsKey("BonusShares"))
			return 0;
		else
			return regDates.get(currentDate.toString()).get("BonusShares");
	}

	public double getTranShares(Integer currentDate) {
		if (!regDates.get(currentDate.toString()).containsKey("TranShares"))
			return 0;
		else
			return regDates.get(currentDate.toString()).get("TranShares");
	}

	public boolean isXDate(Integer currentDate) {
		return xDates.contains(currentDate.toString());
	}
	
	public Map<String, Double> getClosePrice() {
		return closePrices;
	}

	public Map<String, Double> getClosePrice(boolean weighted) {
		return closePricesWeighted;
	}

	public double getAllocShares(Integer currentDate) {
		if (!regDates.get(currentDate.toString()).containsKey("AllocShares"))
			return 0;
		else
			return regDates.get(currentDate.toString()).get("AllocShares");			
	}

	public double getAllocPrice(Integer currentDate) {
		if (!regDates.get(currentDate.toString()).containsKey("AllocPrice"))
			return 0;
		else
			return regDates.get(currentDate.toString()).get("AllocPrice");	
	}
}
