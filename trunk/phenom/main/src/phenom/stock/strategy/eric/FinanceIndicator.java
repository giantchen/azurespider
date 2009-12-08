package phenom.stock.strategy.eric;

public final class FinanceIndicator implements Comparable<FinanceIndicator> {
	private String symbol;
	private double value;
	
	@Override
	public int compareTo(FinanceIndicator o) {
		if (value == o.value)
			return 0;
		else if (value < o.value)
			return 1;
		else
			return -1;
	}
	
	public FinanceIndicator(final String symbol, final Double value) {
		this.symbol = symbol;
		this.value = value;
	}
	
	public double getValue() {
		return this.value;
	}	

	public String getSymbol() {
		return symbol;
	}
	
	@Override
	public String toString() {
		return "[" + value + "=" + symbol + "]";
	}
}
