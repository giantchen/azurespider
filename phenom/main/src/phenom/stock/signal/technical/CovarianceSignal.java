package phenom.stock.signal.technical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.correlation.Covariance;

import phenom.stock.signal.GenericComputableEntry;


public class CovarianceSignal extends AbstractTechnicalSignal {	
	public CovarianceSignal() {
		super();
		cache = new HashMap<String, RealMatrix>();
	}

	@Override
	public double calculate(String symbol, String date) {
		return Double.NaN;
	}

	public RealMatrix calculate(String date){
		if (!cache.containsKey(date))
			doCalculation(date);
		
		return cache.get(date);
	}
	
	public List<String> getColumnNames() {
		List<String> ret = new ArrayList<String>();
		ret.addAll(new TreeSet<String>(returns.keySet()));
		return ret;
	}
	
	protected void doCalculation(final String d_) {
		Map<String, Map<String, Double>> values = new HashMap<String, Map<String, Double>>();
		
		// get all the available days
		Set<String> dates = new TreeSet<String>();
		Set<String> symbols = new TreeSet<String>(returns.keySet());
		for (String sym : symbols) {
			for (GenericComputableEntry e : returns.get(sym)) {
				dates.add(e.getDate());
				if (values.get(sym) == null)
					values.put(sym, new HashMap<String, Double>());
				// need the risk-free interest rate
				double riskFreeInterestRate = risk_free_interest_rate(e.getDate());
				
				values.get(sym).put(e.getDate(), e.getValue() - riskFreeInterestRate);				
			}
		}
		
		Map<String, List<Double>> vals = new HashMap<String, List<Double>>();
		
		int n = 0;
		for (String date : dates) {
			for (String s : symbols){
				if (vals.get(s) == null)
					vals.put(s, new LinkedList<Double>());
				
				if (values.get(s).get(date) == null)
					vals.get(s).add(0.0);
				else
					vals.get(s).add(values.get(s).get(date));
				n = vals.get(s).size();
			}
			
			if (n < 2)
				continue;
			
			if (!date.equals(d_))
				continue;
			
			double b[][] = new double[n][vals.keySet().size()];
			
			int i = 0;
			for (String s : symbols){
				for (int j = 0; j < vals.get(s).size(); ++j)
					b[j][i] = vals.get(s).get(j);
				++i;				
			}

			cache.put(d_, new Covariance(b).getCovarianceMatrix());
		}		
	}
	
	protected Map<String, RealMatrix> cache;
}
