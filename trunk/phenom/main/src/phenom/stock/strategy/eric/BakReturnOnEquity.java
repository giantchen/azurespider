package phenom.stock.strategy.eric;


public class BakReturnOnEquity extends BasicFinanceReportIndicator {
	private BakNetAssetsPerShare netAssets;
	private BakEarningPerShare earning;
	
	public BakReturnOnEquity(String symbol) {
		this(symbol, null, null);
	}
	
	public BakReturnOnEquity(String symbol, String startDate, String endDate) {
		super(symbol, "", startDate, endDate);
		netAssets = new BakNetAssetsPerShare(symbol, startDate, endDate);
		earning = new BakEarningPerShare(symbol, startDate, endDate);
	}
	
	@Override
	public FinanceIndicator getIndicator(final String date) {
		// the map is in desc order, so return the first date that <= date
		double assets_value = 0;
		double earning_vlaue = 0;
		for (String d : netAssets.indicators.keySet()) {
			if (d.compareTo(date) <= 0) {
				assets_value = netAssets.indicators.get(d);
				break;
			}
		}
		
		for (String d : earning.indicators.keySet()) {
			if (d.compareTo(date) <= 0) {
				earning_vlaue = earning.indicators.get(d);
				break;
			}
		}
		
		if (isTradeDay(date))
			return new FinanceIndicator(symbol, earning_vlaue / assets_value);
		else
			return null;
	}
}
