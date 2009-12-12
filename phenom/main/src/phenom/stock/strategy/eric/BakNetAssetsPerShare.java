package phenom.stock.strategy.eric;

public class BakNetAssetsPerShare extends BasicFinanceReportIndicator {
	protected BakNetAssetsPerShare(String symbol, String field, String startDate, String endDate) {
		super(symbol, field, startDate, endDate);
	}
	
	public BakNetAssetsPerShare(String symbol) {
		this(symbol, "NetAssetsPerShare", null, null);
	}
	
	public BakNetAssetsPerShare(String symbol, String startDate, String endDate) {
		this(symbol, "NetAssetsPerShare", startDate, endDate);
	}
}
