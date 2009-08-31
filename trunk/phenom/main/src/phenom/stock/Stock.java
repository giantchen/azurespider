package phenom.stock;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import phenom.database.ConnectionManager;
import phenom.utils.WeightUtil;;

public class Stock implements Comparable<Stock>{
	private String symbol;
	private String exchange;
	private String date;
	private double openPrice;
	private double highPrice;
	private double lowPrice;
	private double closePrice;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
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
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}

	private double amount;
	private double volume;
	private double weight;
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
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
		symbol = rs_.getString("Symbol");
		exchange = rs_.getString("Exchange");
		date = rs_.getString("Date");
		openPrice = rs_.getDouble("Open");
		highPrice = rs_.getDouble("High");
		lowPrice = rs_.getDouble("Low");
		closePrice = rs_.getDouble("Close");
		amount = rs_.getDouble("amount");
		volume = rs_.getDouble("Volume");
		weight = rs_.getDouble("Weight");
	}	
	
	public Stock() {
		
	}
	
	public Stock(String symbol_) {
		this(symbol_, null);
	}
	
	public Stock(String symbol_, String date_) {
		this.symbol = symbol_;
		this.date = date_;
	}
	
	@Override
	public String toString() {
		return "Stock [amount=" + amount + ", closePrice=" + closePrice
				+ ", date=" + date + ", exchange=" + exchange + ", highPrice="
				+ highPrice + ", lowPrice=" + lowPrice + ", openPrice="
				+ openPrice + ", symbol=" + symbol + ", volume=" + volume + "]";
	}
	
	@Override
	public int compareTo(Stock s) {
		int result = this.getSymbol().compareTo(s.getSymbol());
		
		if(result == 0) {
			result = this.getDate().compareTo(s.getDate());
		}
		
		return result;
	}
	
	public List<Stock> getStock(String startDate_, String endDate_, boolean applyWeight) {
		List<Stock> stocks = new ArrayList<Stock>();
		Stock s = null;
		Connection conn = null;
		ResultSet rs = null;
		
		String sql = "select * from STOCK_PRICE where Symbol = '" + getSymbol() + "' and Date between '" 
			+ startDate_ + "' and '" + endDate_ + "' order by Symbol, Date";		
		
		System.out.println(sql);
		
		try {
			conn = ConnectionManager.getConnection();
			rs = conn.createStatement().executeQuery(sql);
			
			while(rs.next()) {
				s = new Stock();
				s.set(rs);
				stocks.add(s);
				
				if(applyWeight) {
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
				s = new Stock();
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
	
	public static Stock getStock(String symbol_, String date_) {		
		Stock s = null;
		Connection conn = null;
		ResultSet rs = null;		
		String sql = "select * from STOCK_PRICE where Symbol = '" + symbol_ + "' and Date = '" + date_ + "'";		
		
		try {
			conn = ConnectionManager.getConnection();
			rs = conn.createStatement().executeQuery(sql);
			
			while(rs.next()) {
				s = new Stock();
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
}