package phenom.stock;

public class PositionEntry {	
	private String symbol;
	private double amount;
	
	//TODO assume the next corporate actions comes after current ca being settled, otherwise can use a list to model this
	private NameValuePair inflightAmt;
	private NameValuePair inflightCash;

	public PositionEntry(String symbol, double amount,
			NameValuePair inflightAmt, NameValuePair inflightCash) {
		super();
		this.symbol = symbol;
		this.amount = amount;
		this.inflightAmt = inflightAmt;
		this.inflightCash = inflightCash;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public double getInflightPos() {
		double v = 0;
		if(inflightAmt != null) {
			v = inflightAmt.getValue();
		}
		return v;
	}
	
	public double getInflightCash() {
		double v = 0;
		if(inflightCash != null) {
			v = inflightCash.getValue();
		}
		return v;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getSymbol() {
		return symbol;
	}
	
	public double evaluateInflightCash(String date_) {
		double val = 0;
		if(inflightCash != null) {
			if(inflightCash.getName().equals(date_)) {
				val = inflightCash.getValue();
				inflightCash = null;
			}
		}
		return val;
	}
	
	public double evaluateInflightPos(String date_) {
		double val = 0;
		if(inflightAmt != null) {
			if(inflightAmt.getName().equals(date_)) {
				val = inflightAmt.getValue();
				amount += val;
				inflightAmt = null;
			}
		}
		return val;
	}
	
	public boolean isSoldOut() {	
		if(amount == 0 && inflightAmt == null && inflightCash == null) {
			return true;
		} else {
			return false;
		}
	}
	
	public void increaseInflight(double cash_, double shares_, String cashAvailableDate_, String listDate_) {
		//TODO assume the next corporate actions comes after current ca being settled, otherwise can use a list to model this
		if(cash_ > 0) {
			inflightCash = new NameValuePair(cashAvailableDate_, cash_);
		}
		if(shares_ > 0) {
			inflightAmt = new NameValuePair(listDate_, shares_);
		}
	}
}
