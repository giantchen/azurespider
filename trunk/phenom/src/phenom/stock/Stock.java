package phenom.stock;

import java.sql.SQLException;
import java.sql.ResultSet;

public class Stock {
	private String symbol;
	private String exchange;
	private String date;
	private double openPrice;
	private double highPrice;
	private double lowPrice;
	private double closePrice;
	private double amount;
	private double volume;
	
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
	}	
	
	public Stock() {
		
	}
	
	@Override
	public String toString() {
		return "Stock [amount=" + amount + ", closePrice=" + closePrice
				+ ", date=" + date + ", exchange=" + exchange + ", highPrice="
				+ highPrice + ", lowPrice=" + lowPrice + ", openPrice="
				+ openPrice + ", symbol=" + symbol + ", volume=" + volume + "]";
	}
	
	
}