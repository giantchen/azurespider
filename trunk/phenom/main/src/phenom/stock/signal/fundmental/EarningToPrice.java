package phenom.stock.signal.fundmental;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class EarningToPrice extends AbstractFundmentalSignal {
		
	@Override
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
			m.put(data.getAnnounceDate(), data.getEarningPerShare());
		}
	}
	
	@Override
	public double calculate(final String symbol, final String date) {
		if (!values.containsKey(symbol) || !prices.containsKey(symbol) || !prices.get(symbol).containsKey(date))
			return Double.NaN;
		
		// the map is in desc order, so return the first date that <= date
		for (String d : values.get(symbol).keySet()) {
			if (d.compareTo(date) <= 0) {
				return values.get(symbol).get(d) / prices.get(symbol).get(date);
			}
		}
		return Double.NaN;
	}
}
