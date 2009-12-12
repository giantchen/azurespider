package phenom.stock.signal.pricemomentum;

import java.util.Collections;
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
	public double getAverage(String symbol_, String date_, int cycle_) {
		return calculate(symbol_, date_, cycle_);
	}
	
	public double getAverage(GenericComputableEntry s_, int cycle_) {
		return calculate(s_.getSymbol(), s_.getDate(), cycle_);
	}
	
    /**
     * Calculate Average Specified by days
     */
	@Override
    public double calculate(String symbol_, String date_, int days_) {
    	double average = 0;
        validate(symbol_, date_, days_);        
        if(Collections.binarySearch(values.get(symbol_), new GenericComputableEntry(symbol_, date_, -1)) < 0) {
        	return AbstractPriceMomentumSignal.INVALID_VALUE; //data is not avaliable
        }
        
        if(!isCalculated(symbol_, date_, days_)) {
        	calculateEM(symbol_, days_);        	
        }
        
        List<CycleValuePair> pairs = cache.get(symbol_).get(date_);        
        for(CycleValuePair c : pairs) {
            if(c.getCycle() == days_) {
                average = c.getValue();
                break;
            }
        }
        
        return average;
    }
    
    /**
     * Eager Calculation
     */
    private void calculateEM(String symbol_, int cycle_) {
    	List<GenericComputableEntry> stocks = values.get(symbol_);
    	
    	for(int i = 0; i < stocks.size(); i++) {
    		CycleValuePair c = null;
    		GenericComputableEntry s = stocks.get(i);
    		
    		Map<String, List<CycleValuePair>> symbolAverages = cache.get(s.getSymbol());
    		if (symbolAverages == null) {
                symbolAverages = new HashMap<String, List<CycleValuePair>>();
                cache.put(symbol_, symbolAverages);
            }
    		
    		List<CycleValuePair> pairs = symbolAverages.get(s.getDate());        
            if(pairs == null) {
                pairs = new ArrayList<CycleValuePair>();
                symbolAverages.put(s.getDate(), pairs);
            }
            
            if(i == 0) {
            	c = new CycleValuePair(cycle_, stocks.get(0).getValue());
            } else {
            	CycleValuePair previousPair = null;
            	List<CycleValuePair> previosPairs = symbolAverages.get(stocks.get(i - 1).getDate());
            	for(CycleValuePair cv : previosPairs) {
            		if(cv.getCycle() == cycle_) {
            			previousPair = cv;
            			break;
            		}
            	}
            	
            	//calculate factors
            	double []factor = calculateFactor(cycle_);            	
            	c = new CycleValuePair(cycle_, stocks.get(i).getValue() * factor[0] +
            			previousPair.getValue() * factor[1]);            	
            }
            
            pairs.add(c);
    	}
    }
    
    /**
     * different EMV could override this method to 
     * @param cycle_
     * @return
     */
    protected double[] calculateFactor(int... cycle_) {
    	int c = cycle_[0];    	
    	double f1 = 1 / ((double)c);
    	double f2 = ((double)(c - 1)) / c;
    	return new double[]{f1, f2};    	    	
    }
}

