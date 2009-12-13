package phenom.stock.signal.fundmental;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

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
		
		TreeMap<String, Double> map = (TreeMap<String, Double>) values.get(symbol);
		Entry<String, Double> entry = map.floorEntry(date);
		if (entry == null)
			return Double.NaN;
		
		DescriptiveStatistics stat = cachedStat.get(symbol);
		if (stat == null) {
			stat = new DescriptiveStatistics();
			cachedStat.put(symbol, stat);
		}
		
		double earnings = entry.getValue();
		double v = prices.get(symbol).get(date);
		stat.addValue(earnings / v);
		
		double mean = cachedStat.get(symbol).getMean();
		double sd = cachedStat.get(symbol).getStandardDeviation();
		return (earnings / v - mean) / sd;
	}
}
