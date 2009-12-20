package phenom.stock.signal.pricemomentum;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import phenom.stock.signal.GenericComputableEntry;

/**
 * Exponent Average Implementation 1/N Today + (N - 1)N * yesterday EMV
 * 
 * Before each public method call, the values must already be sorted, or parse
 * true when call addValue/addValues method
 * 
 * Currently only support eager calculation
 */
public class EMovingAverage extends AbstractPriceMomentumSignal {
	public EMovingAverage(int cycle) {
		super(cycle);
	}
	
	/**
	 * Calculate Average Specified by days
	 */
	@Override
	public double calculate(String symbol, String date) {
		double average = AbstractPriceMomentumSignal.INVALID_VALUE;
		validate(symbol, date, cycle);

		if (!isTradeDate(symbol, date)) {
			return average;
		}

		if (!isCalculated(symbol, date, cycle)) {
			calculateEM(symbol, cycle);
		}

		return cache.get(symbol).get(date);
	}

	/**
	 * Eager Calculation
	 */
	private void calculateEM(String symbol, int cycle) {
    	List<GenericComputableEntry> stocks = prices.get(symbol);
    	
    	for(int i = 0; i < stocks.size(); i++) {    	
    		GenericComputableEntry s = stocks.get(i);
    		
    		Map<String, Double> symbolAverages = cache.get(s.getSymbol());
    		if (symbolAverages == null) {
                symbolAverages = new HashMap<String, Double>();
                cache.put(symbol, symbolAverages);
            }
            
            if(i == 0) {
            	symbolAverages.put(s.getDate(), stocks.get(0).getValue());            
            } else {
            	Double previosPairs = symbolAverages.get(stocks.get(i - 1).getDate());
            	            	
            	//calculate factors
            	double []factor = calculateFactor(cycle);            	
            	double v = stocks.get(i).getValue() * factor[0] + previosPairs * factor[1];  
            	symbolAverages.put(s.getDate(), v);
            }
    	}
    }

	/**
	 * different EMV could override this method to
	 * 
	 * @param cycle
	 * @return
	 */
	protected double[] calculateFactor(int... cycle) {
		int c = cycle[0];
		double f1 = 1 / ((double) c);
		double f2 = ((double) (c - 1)) / c;
		return new double[] { f1, f2 };
	}
}
