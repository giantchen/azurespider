package phenom.stock.signal.pricemomentum;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import phenom.utils.DateUtil;
import phenom.stock.signal.GenericComputableEntry;

/**
 * Arithmetic Average Implementation
 * 
 * Before each public method call, the values must already be sorted, or parse true when call addValue/addValues method
 * 
 */
public class SMovingAverage extends AbstractPriceMomentumSignal{    
	public SMovingAverage(int cycle) {
		super(cycle);
	}
	
    DescriptiveStatistics stat = new DescriptiveStatistics();        
    
    /**
     * Calculate Average Specified by days
     */
    @Override
    public double calculate(String symbol_, String date_) {
        validate(symbol_, date_, cycle);
        
        if (!isTradeDate(symbol_, date_)) {
			return AbstractPriceMomentumSignal.INVALID_VALUE;
		}
        
        Map<String, Double> symbolAverages = cache.get(symbol_);;
        if (symbolAverages == null) {
            symbolAverages = new HashMap<String, Double>();
            cache.put(symbol_, symbolAverages);
        }
        
        Double average = symbolAverages.get(date_);        
        
        if(average == null) {
            average = calculateMean(symbol_, date_);
            symbolAverages.put(date_, average);
        }
        
        return average;
    }
    
    protected double calculateMean(String symbol_, String date_) {
    	stat.clear();    	
    	List<GenericComputableEntry> stocks = prices.get(symbol_);
        
        GenericComputableEntry s = new GenericComputableEntry(symbol_, null, -1);          
        String curDate = date_;           
        
        for(int i = 0; i < cycle; i++) {
            s.setDate(curDate);
            int index = Collections.binarySearch(stocks, s);
            
            while(index < 0) {
                if(Math.abs(index + 1) == 0) {
                    //no data in available, should throw exception, refactor later
                    break;
                } else {
                    curDate = DateUtil.previousDay(s.getDate());
                    s.setDate(curDate);
                    index = Collections.binarySearch(stocks, s);
                }                
            }
            
            if(index >= 0) { //find 1 record               
                stat.addValue(stocks.get(index).getValue());       
            }
            
            curDate = DateUtil.previousDay(s.getDate());            
        }
        
        return stat.getMean();
    }
}

