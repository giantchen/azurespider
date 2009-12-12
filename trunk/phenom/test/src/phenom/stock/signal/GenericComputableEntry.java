package phenom.stock.signal;

import phenom.stock.signal.pricemomentum.Computable;

public class GenericComputableEntry implements Computable, Comparable<GenericComputableEntry> {
	private String symbol;
	private String date;
	private double value;	
	
	public GenericComputableEntry(String symbol_, String date_, double value_) {		
		this.symbol = symbol_;
		this.date = date_;
		this.value = value_;
	}

	@Override
	public String getDate() {
		return date;
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	public void setDate(String d_) {
		this.date = d_;
	}

	@Override
	public void setValue(double v_) {
		this.value = v_;
	}

	@Override
	public int compareTo(GenericComputableEntry o) {
		int res = getSymbol().compareTo(o.getSymbol());
		return res == 0 ? getDate().compareTo(o.getDate()) : res;
	}	

	@Override
	public String toString() {
		return "GenericComputableEntry [date=" + date + ", symbol=" + symbol
				+ ", value=" + value + "]";
	}
}
