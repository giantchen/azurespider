package phenom.stock;

/**
 * 
 * TODO - Dividend Overlap : 1 comes before the previous settled
 * assume the next corporate actions comes after current ca being settled, otherwise can use a list to model this
 */
public class PositionEntry {	
	private String symbol;
	private double amount;	
	private PEDateValue inflightAmt;
	private PEDateValue inflightCash;

	public PositionEntry(String symbol, double amount) {
		super();
		this.symbol = symbol;
		this.amount = amount;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public double getInflightPos() {
		return inflightAmt == null ? 0 : inflightAmt.getValue();		
	}
	
	public double getInflightCash() {
		return inflightCash == null ? 0 : inflightCash.getValue();		
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getSymbol() {
		return symbol;
	}
	
	public double evaluateInflightCash(String date_, Dividend div_) {
		double val = 0;
		if(div_ != null) {
			inflightCash = new PEDateValue(div_.getRegDate(), div_.getListDate(), div_.getCashDiv()* amount);
		}
		if(inflightCash != null) {
			if(inflightCash.getListDate().equals(date_)) {
				val = inflightCash.getValue();
				inflightCash = null;
			}
		}
		return val;
	}
	
	/**
	 * 1. move the inflight to available
	 * 2. check to see if there are any new dividend eligible
	 * @param date_
	 * @param div_
	 * @return
	 */
	public double evaluateInflightPos(String date_, Dividend div_) {
		double val = 0;		
		if(div_ != null) {
			inflightAmt = new PEDateValue(div_.getRegDate(), div_.getListDate(), div_.getEntitledPos() * amount);
		}		
		if(inflightAmt != null) {
			if(inflightAmt.getListDate().equals(date_)) {
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
	
	public void increaseInflight(Dividend d_) {		
		if(d_.getCashDiv() > 0) {
			inflightCash = new PEDateValue(d_.getRegDate(), d_.getListDate(), d_.getCashDiv() * amount);
		}
		if(d_.getEntitledPos() > 0) {
			inflightAmt = new PEDateValue(d_.getRegDate(), d_.getListDate(), d_.getEntitledPos() * amount);
		}
	}
	
	private static class PEDateValue {
		private double value;
		private String regDate;
		private String listDate;
		public PEDateValue(String regDate, String listDate, double value) {
			super();
			this.value = value;
			this.regDate = regDate;
			this.listDate = listDate;
		}
		public double getValue() {
			return value;
		}		
		@SuppressWarnings("unused")
		public String getRegDate() {
			return regDate;
		}
		public String getListDate() {
			return listDate;
		}		
	}
}
