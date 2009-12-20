package phenom.stock.signal.fundmental;

import java.util.TreeMap;
import java.util.Map.Entry;

import phenom.stock.signal.SignalConstants;

public class CashFlowToPrice extends AbstractFundmentalSignal {
	@Override
	protected Double getData(FundmentalData data){
		return data.getCashPerShare();
	}
	
	@Override
	public double calculate(final String symbol, final String date) {
		if (!values.containsKey(symbol) || !prices.containsKey(symbol) || !prices.get(symbol).containsKey(date))
			return SignalConstants.INVALID_VALUE;
		
		TreeMap<String, Double> map = (TreeMap<String, Double>) values.get(symbol);
		Entry<String, Double> entry = map.floorEntry(date);
		if (entry == null)
			return SignalConstants.INVALID_VALUE;
		
		double earnings = entry.getValue();
		double price = prices.get(symbol).get(date);
		return earnings / price;
	}
}
