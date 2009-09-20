package phenom.stock;

public class PositionEntry {
	private String symbol;
	private double amount;
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
	
	public boolean soldOut(double amt_) {
		amount -= amt_;
		if(amount == 0 && inflightAmt == null && inflightCash == null) {
			return true;
		} else {
			return false;
		}
	}
}
