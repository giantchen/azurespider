package phenom.stock.signal.technical;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.math.stat.regression.SimpleRegression;

import phenom.stock.signal.GenericComputableEntry;
import phenom.stock.signal.SignalConstants;


public abstract class AbstractAlphaBetaSignal extends AbstractTechnicalSignal {
	
	public AbstractAlphaBetaSignal() {
		cache = new HashMap<String, Map<String, Double>>();
		_if = new HashMap<String, Double>();
	}
	
	public void addRiskFreeRate(final String date, double value) {
		_if.put(date, value);
	}
	

	public void addRiskFreeRate(Map<String, Double> riskFreeRate) {
		_if = riskFreeRate;
	} 

	protected void doCalculation(final String symbol) {
		List<GenericComputableEntry> entries = returns.get(symbol);
		
		List<GenericComputableEntry> benchmark = null;
		if (symbol.endsWith("sh")) { // Shanghai stock
			benchmark = returns.get("000001.sh");
		} else if (symbol.endsWith("sz")) { // ShenZhen stock
			benchmark = returns.get("399001.sz");
		} else {
			return;
		}
		
		Map<String, Double> s = new HashMap<String, Double>();
		Map<String, Double> b = new HashMap<String, Double>(); 
		
		// Stock
		for (GenericComputableEntry e : entries) {
			s.put(e.getDate(), e.getValue());
		}
		
		// Benchmark
		for (GenericComputableEntry e : benchmark) {
			b.put(e.getDate(), e.getValue());
		}
		
		TreeSet<String> keySet = new TreeSet<String>(b.keySet());
		SimpleRegression regression = new SimpleRegression();

		for (String date : keySet) {
			// need the risk-free interest rate
			double riskFreeInterestRate = risk_free_interest_rate(date);

			if (s.get(date) == null) {
				regression.addData(b.get(date) - riskFreeInterestRate, 0 - riskFreeInterestRate);
			} else {
				regression.addData(b.get(date) - riskFreeInterestRate, s.get(date) - riskFreeInterestRate);
			}
			if (cache.get(symbol) == null) {
				Map<String, Double> v = new HashMap<String, Double>();
				cache.put(symbol, v);
			}
			cache.get(symbol).put(date, pickValue(regression));
		}
	}
	
	abstract protected double pickValue(SimpleRegression regression_);
	
	@Override
	public double calculate(String symbol, String date) {
		if (!isTradeDate(symbol, date)) {
			return SignalConstants.INVALID_VALUE;
		}
		
		if (cache.get(symbol) == null)
			doCalculation(symbol);
		
		return cache.get(symbol).get(date);
	}
	
	protected double risk_free_interest_rate(final String date_) {
		return _if.get(date_);
	}
	
	protected Map<String, Map<String, Double>> cache;
	protected Map<String, Double> _if;
}
