package phenom.stock.signal.fundmental;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import phenom.stock.signal.GenericComputableEntry;
import phenom.stock.signal.ISignal;
import phenom.stock.signal.SignalConstants;

public abstract class AbstractFundmentalSignal implements ISignal {
	protected Map<String, Map<String, Double>> values = new HashMap<String, Map<String, Double>>();
	protected Map<String, Map<String, Double>> prices = new HashMap<String, Map<String, Double>>();
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public double calculate(final String symbol, final String date) {
		if (!values.containsKey(symbol))
			return SignalConstants.INVALID_VALUE;
		
		TreeMap<String, Double> map = (TreeMap<String, Double>) values.get(symbol);
		Entry<String, Double> entry = map.floorEntry(date);
		if (entry == null)
			return SignalConstants.INVALID_VALUE;

		return entry.getValue();
	}
	
	public void addFundmentalData(List<FundmentalData> dataList) {
		for (FundmentalData data : dataList) {
			Map<String, Double> m = null; 
			if (values.containsKey(data.getSymbol()))
				m = values.get(data.getSymbol());
			else {
				m = new TreeMap<String, Double>(new Comparator<String>() {
						@Override
						public int compare(String o1, String o2) {
							return o2.compareTo(o1);
					}
				});
				values.put(data.getSymbol(), m);
			}
			m.put(data.getAnnounceDate(), getData(data));
		}
	}
	
	protected Double getData(FundmentalData data) {
		return null;		
	}
	
	public void addPrices(List<? extends GenericComputableEntry> priceList) {
		for (GenericComputableEntry data : priceList) {
			Map<String, Double> m = null; 
			if (prices.containsKey(data.getSymbol()))
				m = prices.get(data.getSymbol());
			else {
				m = new TreeMap<String, Double>(new Comparator<String>() {
						@Override
						public int compare(String o1, String o2) {
							return o2.compareTo(o1);
					}
				});
				prices.put(data.getSymbol(), m);
			}
			m.put(data.getDate(), data.getValue());
		}
	}
}
