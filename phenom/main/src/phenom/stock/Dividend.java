package phenom.stock;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Dividend implements Comparable<Dividend>{
	private double cashDiv;
	private double stockDiv;
	private double tranDiv;
	private String xDate; //除权除息日， 股票除息日 + 4 = 发放日
	private String listDate; //上市日期 
	private String symbol;
	public double getCashDiv() {
		return cashDiv;
	}
	public double getStockDiv() {
		return stockDiv;
	}
	public double getTranDiv() {
		return tranDiv;
	}
	public String getXDate() {
		return xDate;
	}
	public String getListDate() {
		return listDate;
	}
	public String getListDate1() {//现金分红可用日
		return listDate;
	}
	public String getSymbol() {
		return symbol;
	}	
	
	@Override
	public int compareTo(Dividend d_) {
		int result = this.getSymbol().compareTo(d_.getSymbol());
		
		if(result == 0) {
			result = this.getXDate().compareTo(d_.getXDate());
		}
		
		return result;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((xDate == null) ? 0 : xDate.hashCode());
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
		Dividend other = (Dividend) obj;
		if (xDate == null) {
			if (other.xDate != null)
				return false;
		} else if (!xDate.equals(other.xDate))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}	
	
	public void set(ResultSet rs_) throws SQLException{
		symbol = rs_.getString("Symbol");
		xDate = rs_.getString("XDate");
		listDate = rs_.getString("ListDate");
		cashDiv = Double.parseDouble(rs_.getString("Dividend")) / 10;
		stockDiv = Double.parseDouble(rs_.getString("BonusShare")) / 10;
		tranDiv = Double.parseDouble(rs_.getString("TransitShare")) / 10;
	}
}
