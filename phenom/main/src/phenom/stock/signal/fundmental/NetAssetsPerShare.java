package phenom.stock.signal.fundmental;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import phenom.stock.signal.SignalConstants;


public class NetAssetsPerShare extends AbstractFundmentalSignal {
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
			m.put(data.getAnnounceDate(), data.getNetAssetsPerShare());
		}
	}
	
	@Override
	public double calculate(final String symbol, final String date) {
		if (!values.containsKey(symbol))
			return SignalConstants.INVALID_VALUE;
		
		DescriptiveStatistics stat = cachedStat.get(symbol);
		if (stat == null) {
			stat = new DescriptiveStatistics();
			cachedStat.put(symbol, stat);
		}
		
		TreeMap<String, Double> map = (TreeMap<String, Double>) values.get(symbol);
		Entry<String, Double> entry = map.floorEntry(date);
		if (entry == null)
			return SignalConstants.INVALID_VALUE;
		
		
		stat.addValue(entry.getValue());
		double mean = stat.getMean();
		double sd = stat.getStandardDeviation();
		// return the normalized value
		return (entry.getValue() - mean) / sd;
	}
}
