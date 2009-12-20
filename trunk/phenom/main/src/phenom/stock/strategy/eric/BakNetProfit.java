package phenom.stock.strategy.eric;


public class BakNetProfit extends BasicFinanceReportIndicator {
	protected BakNetProfit(String symbol, String field, String startDate, String endDate) {
		super(symbol, field, startDate, endDate);
	}
	
	public BakNetProfit(String symbol) {
		this(symbol, "NetProfit", null, null);
	}
	
	public BakNetProfit(String symbol, String startDate, String endDate) {
		this(symbol, "NetProfit", startDate, endDate);
	}
	
	@Override
	public FinanceIndicator getIndicator(final String date) {
		// the map is in desc order, so return the first date that <= date
		for (String d : indicators.keySet()) {
			if (d.compareTo(date) <= 0) {
				// if (isTradeDay(date) && ea.calculate(symbol, date, 30) < stock.getClosePrice(date, true))
				if (isTradeDay(date))
					return new FinanceIndicator(symbol, indicators.get(d));
				else
					return null;
			}
		}
		return null;
	}
}
