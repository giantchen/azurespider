package phenom.stock;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import phenom.database.ConnectionManager;
import phenom.stock.signal.GenericComputableEntry;
import phenom.stock.signal.pricemomentum.AbstractPriceMomentumSignal;
import phenom.utils.DateUtil;
import phenom.utils.WeightUtil;;

public class Stock extends GenericComputableEntry{
	public static String SZZS = "000001.sh";	
	private String exchange;	
	private double openPrice;
	private double highPrice;
	private double lowPrice;
	private double closePrice;
	private double amount;
	private double volume;
	private double weight;
	private double weightedOpenPrice;
	private double weightedClosePrice;
	private double weightedLowPrice;
	private double weightedHighPrice;
	
	public double getWeightedOpenPrice() {
		return weightedOpenPrice;
	}

	public void setWeightedOpenPrice(double weightedOpenPrice) {
		this.weightedOpenPrice = weightedOpenPrice;
	}

	public double getWeightedClosePrice() {
		return weightedClosePrice;
	}

	public void setWeightedClosePrice(double weightedClosePrice) {
		this.weightedClosePrice = weightedClosePrice;
	}

	public double getWeightedLowPrice() {
		return weightedLowPrice;
	}

	public void setWeightedLowPrice(double weightedLowPrice) {
		this.weightedLowPrice = weightedLowPrice;
	}

	public double getWeightedHighPrice() {
		return weightedHighPrice;
	}

	public void setWeightedHighPrice(double weightedHighPrice) {
		this.weightedHighPrice = weightedHighPrice;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDate() == null) ? 0 : getDate().hashCode());
		result = prime * result + ((getSymbol() == null) ? 0 : getSymbol().hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stock other = (Stock) obj;
		if (getDate() == null) {
			if (other.getDate() != null)
				return false;
		} else if (!getDate().equals(other.getDate()))
			return false;
		if (getSymbol() == null) {
			if (other.getSymbol() != null)
				return false;
		} else if (!getSymbol().equals(other.getSymbol()))
			return false;
		return true;
	}
	
	@Override
	public double getValue() {
		return getWeightedClosePrice();
	}
	
	@Override
	public void setValue(double v_) {
		setWeightedClosePrice(v_);
	}

	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public double getOpenPrice() {
		return openPrice;
	}
	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
	}
	public double getHighPrice() {
		return highPrice;
	}
	public void setHighPrice(double highPrice) {
		this.highPrice = highPrice;
	}
	public double getLowPrice() {
		return lowPrice;
	}
	public void setLowPrice(double lowPrice) {
		this.lowPrice = lowPrice;
	}
	public double getClosePrice() {
		return closePrice;
	}
	public void setClosePrice(double closePrice) {
		this.closePrice = closePrice;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public double getVolume() {
		return volume;
	}
	public void setVolume(long volume) {
		this.volume = volume;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public double getWeight() {
		return this.weight;
	}

	public void set(ResultSet rs_) throws SQLException{		
		exchange = rs_.getString("Exchange");
		setDate( rs_.getString("Date"));
		openPrice = rs_.getDouble("Open");
		highPrice = rs_.getDouble("High");
		lowPrice = rs_.getDouble("Low");
		closePrice = rs_.getDouble("Close");
		amount = rs_.getDouble("amount");
		volume = rs_.getDouble("Volume");
		weight = rs_.getDouble("Weight");		
		weightedOpenPrice = openPrice;
		weightedClosePrice = closePrice;
		weightedLowPrice = lowPrice;
		weightedHighPrice = highPrice;	 
	}	
	
	public Stock(String symbol_) {
		this(symbol_, null, -1);
	}
	
	public Stock(String symbol_, String date_, double value_) {
		super(symbol_, date_, value_);
	}
	
	@Override
	public String toString() {
		return "Stock [amount=" + amount + ", closePrice=" + closePrice
				+ ", date=" + getDate() + ", exchange=" + exchange + ", highPrice="
				+ highPrice + ", lowPrice=" + lowPrice + ", openPrice="
				+ openPrice + ", symbol=" + getSymbol() + ", volume=" + volume + "]";
	}
	
	@Override
	public int compareTo(GenericComputableEntry s) {
		return super.compareTo(s);
	}
	
	public List<Stock> getStock(String startDate_, String endDate_, boolean applyWeight_) {
		return Stock.getStock(getSymbol(), startDate_, endDate_, applyWeight_);
	}
	
	public static List<Stock> getStock(String symbol_, String startDate_, String endDate_, boolean applyWeight_) {
		List<Stock> stocks = new ArrayList<Stock>();
		Stock s = null;
		Connection conn = null;
		ResultSet rs = null;
		
		String sql = "select * from STOCK_PRICE where Symbol = '" + symbol_ + "' and Date between '" 
			+ startDate_ + "' and '" + endDate_ + "' order by Symbol, Date";		
		
		System.out.println(sql);
		
		try {
			conn = ConnectionManager.getConnection();
			rs = conn.createStatement().executeQuery(sql);
			
			while(rs.next()) {
				s = new Stock(rs.getString("Symbol"));
				s.set(rs);
				stocks.add(s);
				
				if(applyWeight_) {
					WeightUtil.applyWeight(s);
					//PriceCalculator.applyWeight(s);
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {						
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}		
		
		return stocks;
	}
	
	//return the recent price before date_
	public static Stock previousStock(String symbol_, String date_) {		
		Stock s = null;
		Connection conn = null;
		ResultSet rs = null;
		String subSql = "(select max(Date) from STOCK_PRICE where Symbol = '" + symbol_ + "' and Date < '" + date_ + "')";
		String sql = "select * from STOCK_PRICE where Symbol = '" + symbol_ + "' and Date = " + subSql;		
		
		try {
			conn = ConnectionManager.getConnection();
			rs = conn.createStatement().executeQuery(sql);
			
			while(rs.next()) {
				s = new Stock(rs.getString("Symbol"));
				s.set(rs);
			}			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {						
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}		
		
		return s;
	}
		
	public static String previousTradeDate(String symbol_, String date_) {		
		Stock s = previousStock(symbol_, date_);		
		return s == null ? null : s.getDate();
	}
	
	//上证指数或者深证指数的所有交易日
	public static String previousTradeDate(String date_) {
		return previousTradeDate(SZZS, date_);
	}
	
	public static List<String> tradeDates(String startDate_, String endDate_) {
		List<Stock> stocks = getStock(SZZS, startDate_, endDate_, false);
		List<String> tradeDates = new ArrayList<String>();
		for(Stock s : stocks) {
			tradeDates.add(s.getDate());
		}
		Collections.sort(tradeDates);
		return tradeDates;
	}
	
	public static Stock getStock(String symbol_, String date_) {		
		Stock s = null;
		Connection conn = null;
		ResultSet rs = null;		
		String sql = "select * from STOCK_PRICE where Symbol = '" + symbol_ + "' and Date = '" + date_ + "'";		
		
		try {
			conn = ConnectionManager.getConnection();
			rs = conn.createStatement().executeQuery(sql);
			
			while(rs.next()) {
				s = new Stock(rs.getString("Symbol"));
				s.set(rs);
			}			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {						
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}		
		
		return s;
	}
	
	public static double getClosePrice(String symbol_, String date_) {
		Stock s = getStock(symbol_, date_);
		while(s == null) {
			System.out.println("no price for symbol = " + symbol_ + " on date = " + date_);
			date_ = DateUtil.previosTradeDate(date_);
			s = getStock(symbol_, date_);
		}
		return s.getClosePrice();
	}
}