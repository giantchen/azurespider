package phenom.stock.strategy.eric;


public class BakEarningToPrice extends BasicFinanceReportIndicator {
	protected BakEarningToPrice(String symbol, String field, String startDate, String endDate) {
		super(symbol, field, startDate, endDate);
	}
	
	public BakEarningToPrice(String symbol) {
		this(symbol, "EarningPerShare", null, null);
	}
	
	public BakEarningToPrice(String symbol, String startDate, String endDate) {
		this(symbol, "EarningPerShare", startDate, endDate);
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
