package phenom.stock;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Weight {	
	private int uid;
	private String symbol;
	private String date;
	private double gift;
	private double amount;
	private double price;
	private double bonus;
	private double trans;
	private int totalShare;
	private int liquidShare;	
	
	public Weight() {
		
	}
	public Weight(int uid, String symbol, String date, double gift, double amount,
			double price, double bonus, double trans, int totalShare,
			int liquidShare) {
		super();
		this.uid = uid;
		this.symbol = symbol;
		this.date = date;
		this.gift = gift;
		this.amount = amount;
		this.price = price;
		this.bonus = bonus;
		this.trans = trans;
		this.totalShare = totalShare;
		this.liquidShare = liquidShare;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public double getGift() {
		return gift;
	}
	public void setGift(double gift) {
		this.gift = gift;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getBonus() {
		return bonus;
	}
	public void setBonus(double bonus) {
		this.bonus = bonus;
	}
	public double getTrans() {
		return trans;
	}
	public void setTrans(double trans) {
		this.trans = trans;
	}
	public int getTotalShare() {
		return totalShare;
	}
	public void setTotalShare(int totalShare) {
		this.totalShare = totalShare;
	}
	public int getLiquidShare() {
		return liquidShare;
	}
	public void setLiquidShare(int liquidShare) {
		this.liquidShare = liquidShare;
	}
	
	public void set(ResultSet rs) throws SQLException {		
		uid = rs.getInt("Uid");
		symbol = rs.getString("Symbol");
		date = rs.getString("Date");
		gift = rs.getDouble("Gift");//10000;
		amount = rs.getDouble("Amount");//10000;
		price = rs.getDouble("Price");//1000;
		bonus = rs.getDouble("Bonus");//1000;
		trans = rs.getDouble("Trans");//10000;
		totalShare = rs.getInt("Total_Share")*10000;
		liquidShare = rs.getInt("Liquid_Share")*10000;
	}
	@Override
	public String toString() {
		return "Weight [amount=" + amount + ", bonus=" + bonus + ", date="
				+ date + ", gift=" + gift + ", liquidShare=" + liquidShare
				+ ", price=" + price + ", symbol=" + symbol + ", totalShare="
				+ totalShare + ", trans=" + trans + ", uid=" + uid + "]";
	}
}
