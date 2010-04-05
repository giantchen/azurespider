package phenom.stock.signal.technical;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import phenom.stock.signal.GenericComputableEntry;
import phenom.stock.signal.SignalConstants;

public abstract class AbstractMeanVolatilitySignal extends AbstractTechnicalSignal {	
	public AbstractMeanVolatilitySignal() {
		cache = new HashMap<String, Map<String, Double>>();
	}

	protected void doCalculation(final String symbol) {
		List<GenericComputableEntry> entries = returns.get(symbol);		
		Map<String, Double> s = new HashMap<String, Double>();
		
		// Stock
		for (GenericComputableEntry e : entries) {
			s.put(e.getDate(), e.getValue());
		}
		
		SummaryStatistics stats = new SummaryStatistics();
		TreeSet<String> keySet = new TreeSet<String>(s.keySet());
		for (String date : keySet) {
			if (cache.get(symbol) == null) {
				Map<String, Double> v = new HashMap<String, Double>();
				cache.put(symbol, v);
			}
			stats.addValue(s.get(date));
			System.out.print("date = " + date + " ");
			System.out.print("return = " + s.get(date) + " ");
			System.out.print("mean = " + stats.getMean() + " ");
			System.out.println("std = " + stats.getStandardDeviation());
			cache.get(symbol).put(date, pickValue(stats));
		}
	}
	
	abstract protected double pickValue(StatisticalSummary stats);
	
	@Override
	public double calculate(String symbol, String date) {
		if (!isTradeDate(symbol, date)) {
			return SignalConstants.INVALID_VALUE;
		}
		
		if (cache.get(symbol) == null)
			doCalculation(symbol);
		
		return cache.get(symbol).get(date);
	}

	protected Map<String, Map<String, Double>> cache;
}
