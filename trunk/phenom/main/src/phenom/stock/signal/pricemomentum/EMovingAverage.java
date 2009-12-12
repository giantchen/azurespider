package phenom.stock.signal.pricemomentum;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import phenom.stock.signal.GenericComputableEntry;

/**
 * Exponent Average Implementation
 * 1/N Today + (N - 1)N * yesterday EMV
 * 
 * Before each public method call, the values must already be sorted, or parse true when call addValue/addValues method
 *
 * Currently only support eager calculation
 */
public class EMovingAverage extends AbstractPriceMomentumSignal{	
	public EMovingAverage(int cycle) {
		super(cycle);
	}
	
	public double getAverage(String symbol_, String date_, int cycle) {
		return calculate(symbol_, date_, cycle);
	}
	
	public double getAverage(GenericComputableEntry s_, int cycle) {
		return calculate(s_.getSymbol(), s_.getDate(), cycle);
	}
	
    /**
     * Calculate Average Specified by days
     */
	@Override
    public double calculate(String symbol, String date, int cycle) {
    	double average = AbstractPriceMomentumSignal.INVALID_VALUE;
        validate(symbol, date, cycle);    
        
        if(!isTradeDate(symbol, date)) {
			return average;
		}
        
        if(!isCalculated(symbol, date, cycle)) {
        	calculateEM(symbol, cycle);        	
        }
        
        List<CycleValuePair> pairs = cache.get(symbol).get(date);        
        for(CycleValuePair c : pairs) {
            if(c.getCycle() == cycle) {
                average = c.getValue();
                break;
            }
        }
        
        return average;
    }
    
    /**
     * Eager Calculation
     */
    private void calculateEM(String symbol, int cycle) {
    	List<GenericComputableEntry> stocks = values.get(symbol);
    	
    	for(int i = 0; i < stocks.size(); i++) {
    		CycleValuePair c = null;
    		GenericComputableEntry s = stocks.get(i);
    		
    		Map<String, List<CycleValuePair>> symbolAverages = cache.get(s.getSymbol());
    		if (symbolAverages == null) {
                symbolAverages = new HashMap<String, List<CycleValuePair>>();
                cache.put(symbol, symbolAverages);
            }
    		
    		List<CycleValuePair> pairs = symbolAverages.get(s.getDate());        
            if(pairs == null) {
                pairs = new ArrayList<CycleValuePair>();
                symbolAverages.put(s.getDate(), pairs);
            }
            
            if(i == 0) {
            	c = new CycleValuePair(cycle, stocks.get(0).getValue());
            } else {
            	CycleValuePair previousPair = null;
            	List<CycleValuePair> previosPairs = symbolAverages.get(stocks.get(i - 1).getDate());
            	for(CycleValuePair cv : previosPairs) {
            		if(cv.getCycle() == cycle) {
            			previousPair = cv;
            			break;
            		}
            	}
            	
            	//calculate factors
            	double []factor = calculateFactor(cycle);            	
            	c = new CycleValuePair(cycle, stocks.get(i).getValue() * factor[0] +
            			previousPair.getValue() * factor[1]);            	
            }
            
            pairs.add(c);
    	}
    }
    
    /**
     * different EMV could override this method to 
     * @param cycle
     * @return
     */
    protected double[] calculateFactor(int... cycle) {
    	int c = cycle[0];    	
    	double f1 = 1 / ((double)c);
    	double f2 = ((double)(c - 1)) / c;
    	return new double[]{f1, f2};    	    	
    }
}

