package phenom.stock.signal.fundmental;

import java.util.List;


public class NetAssetsPerShare extends AbstractFundmentalSignal {
	protected NetAssetsPerShare(String symbol, String field, String startDate, String endDate) {
		super(symbol, field, startDate, endDate);
	}
	
	protected NetAssetsPerShare(List<String> symbols, String field, String startDate, String endDate) {
		super(symbols, field, startDate, endDate);
	}
	
	public NetAssetsPerShare(String symbol) {
		this(symbol, "NetAssetsPerShare", null, null);
	}
	
	public NetAssetsPerShare(List<String> symbols) {
		this(symbols, "NetAssetsPerShare", null, null);
	}
	
	public NetAssetsPerShare(String symbol, String startDate, String endDate) {
		this(symbol, "NetAssetsPerShare", startDate, endDate);
	}
	
	public NetAssetsPerShare(List<String> symbols, String startDate, String endDate) {
		this(symbols, "NetAssetsPerShare", startDate, endDate);
	}
}
