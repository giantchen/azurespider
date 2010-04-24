package phenom.stock.signal.technical;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import phenom.stock.signal.GenericComputableEntry;
import phenom.stock.signal.SignalConstants;

public abstract class AbstractMeanVolatilitySignal extends AbstractTechnicalSignal {	
	private int _cycle = 0;
	
	public AbstractMeanVolatilitySignal() {
		cache = new HashMap<String, Map<String, Double>>();
	}
	
	public void setCycle(int cycle_) { _cycle = cycle_; }
	public void reset() { cache.clear(); }

	protected void doCalculation(final String symbol) {
		List<GenericComputableEntry> entries = returns.get(symbol);		
		Map<String, Double> s = new HashMap<String, Double>();
		
		// Stock
		for (GenericComputableEntry e : entries) {
			s.put(e.getDate(), e.getValue());
		}
		
		SummaryStatistics stats = new SummaryStatistics();
		TreeSet<String> keySet = new TreeSet<String>(s.keySet());
		List<Double> moving_data = new LinkedList<Double>();
		
		for (String date : keySet) {
			if (cache.get(symbol) == null) {
				Map<String, Double> v = new HashMap<String, Double>();
				cache.put(symbol, v);
			}
			
			// need the risk-free interest rate
			double riskFreeInterestRate = risk_free_interest_rate(date);
			double val = s.get(date) - riskFreeInterestRate;
			if (_cycle > 0) {
				if (moving_data.size() + 1 >= _cycle) {
					moving_data.remove(0);		
					moving_data.add(val);
					stats.clear();
					for (double d : moving_data) {
						stats.addValue(d);
					}
				} else {
					moving_data.add(val);
					stats.addValue(val);
				}				
			} else {
				stats.addValue(val);
			}
			
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
