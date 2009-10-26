package phenom.stock.strategy.eric;

public class NetAssetsPerShare extends BasicFinanceReportIndicator {
	protected NetAssetsPerShare(String symbol, String field, String startDate, String endDate) {
		super(symbol, field, startDate, endDate);
	}
	
	public NetAssetsPerShare(String symbol) {
		this(symbol, "NetAssetsPerShare", null, null);
	}
	
	public NetAssetsPerShare(String symbol, String startDate, String endDate) {
		this(symbol, "NetAssetsPerShare", startDate, endDate);
	}
}
