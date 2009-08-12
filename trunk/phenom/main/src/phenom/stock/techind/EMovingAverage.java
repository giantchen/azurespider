package phenom.stock.techind;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import phenom.stock.Stock;
import phenom.utils.DateUtil;
import phenom.stock.Cycle;
import phenom.stock.techind.AbstractTechIndicator.CycleValuePair;

/**
 * Exponent Average Implementation
 * 1/N Today + (N - 1)N * yesterday EMV
 * 
 * Before each public method call, the values must already be sorted, or parse true when call addValue/addValues method
 *
 * Currently only support eager calculation
 */
public class EMovingAverage extends AbstractTechIndicator{   
	Set<String> calculaedCycleCache = new HashSet<String>();    
    
	@Override
    public void clear() {
    	clear(false);
    }
	
	public void clear(boolean clearCache_) {
		super.clear(clearCache_);
		calculaedCycleCache.clear();
	}
    
    /**
     * Calculate Average Specified by days
     */
    public double getAverage(String symbol_, String date_, int days_) {
    	double average = 0;
        validate(symbol_, date_, days_);        
        if(Collections.binarySearch(values.get(symbol_), new Stock(symbol_, date_)) < 0) {
        	return Double.NaN; //data is not avaliable
        }
        
        if(!calculaedCycleCache.contains(symbol_ + days_)) {
        	calculate(symbol_, days_);
        	calculaedCycleCache.add(symbol_ + days_);
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
    private void calculate(String symbol_, int cycle_) {
    	List<Stock> stocks = values.get(symbol_);
    	
    	for(int i = 0; i < stocks.size(); i++) {
    		CycleValuePair c = null;
    		Stock s = stocks.get(i);
    		
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
            	c = new CycleValuePair(cycle_, stocks.get(0).getClosePrice());
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
            	double factor1 = 1/((double)cycle_);
            	double factor2 = ((double)(cycle_ - 1)) / cycle_;
            	c = new CycleValuePair(cycle_, stocks.get(i).getClosePrice() * factor1 +
            			previousPair.getValue() * factor2);            	
            }
            
            pairs.add(c);
    	}
    }
}

