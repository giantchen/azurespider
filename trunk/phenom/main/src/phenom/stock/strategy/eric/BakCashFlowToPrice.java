package phenom.stock.strategy.eric;


public class BakCashFlowToPrice extends BasicFinanceReportIndicator {
	protected BakCashFlowToPrice(String symbol, String field, String startDate, String endDate) {
		super(symbol, field, startDate, endDate);
	}
	
	public BakCashFlowToPrice(String symbol) {
		this(symbol, "CashPerShare", null, null);
	}
	
	public BakCashFlowToPrice(String symbol, String startDate, String endDate) {
		this(symbol, "CashPerShare", startDate, endDate);
	}
	
	@Override
	public FinanceIndicator getIndicator(final String date) {
		// the map is in desc order, so return the first date that <= date
		for (String d : indicators.keySet()) {
			if (d.compareTo(date) <= 0) {
				if (isTradeDay(date))
					return new FinanceIndicator(symbol, indicators.get(d) / stock.getClosePrice(date, true));
				else
					return null;
			}
		}
		return null;
	}
}
