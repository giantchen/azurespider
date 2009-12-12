package phenom.stock.signal.pricemomentum;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import phenom.utils.DateUtil;
import phenom.stock.Cycle;
import phenom.stock.signal.GenericComputableEntry;

/**
 * Arithmetic Average Implementation
 * 
 * Before each public method call, the values must already be sorted, or parse true when call addValue/addValues method
 * 
 */
public class SMovingAverage extends AbstractPriceMomentumSignal{       
    DescriptiveStatistics stat = new DescriptiveStatistics();    
    
    /**
     * By Default calculate Week/Ten Day/Fifteen Day/Month Average
     * After set, the averages are available via getAverage
     */
    public void setAverage(GenericComputableEntry s_) {
        setAverage(s_.getSymbol(), s_.getDate());
    }
    
    /**
     * By Default calculate Week/Ten Day/Fifteen Day/Month Average
     * After set, the averages are available via getAverage
     */
    public void setAverage(String symbol_, String date_) {
        for(Cycle c : Cycle.values()) {
            getAverage(symbol_, date_, c); 
        }
    } 
    
    public double getAverage(GenericComputableEntry s_, Cycle c_) {
        return getAverage(s_, c_.numDays());
    }
    
    public double getAverage(GenericComputableEntry s_, int days_) {
        return calculate(s_.getSymbol(), s_.getDate(), days_);
    }    
    
    /**
     * Calculate Average Specified by cycle_
     */
    public double getAverage(String symbol_, String date_, Cycle cycle_) {
        return calculate(symbol_, date_, cycle_.numDays());
    }    
    
    /**
     * Calculate Average Specified by days
     */
    @Override
    public double calculate(String symbol_, String date_, int days_) {
        validate(symbol_, date_, days_);
        
        if(Collections.binarySearch(values.get(symbol_), new GenericComputableEntry(symbol_, date_, -1)) < 0) {
        	return AbstractPriceMomentumSignal.INVALID_VALUE; //data is not avaliable
        }
        
        CycleValuePair average = null;
        
        Map<String, List<CycleValuePair>> symbolAverages = cache.get(symbol_);;
        if (symbolAverages == null) {
            symbolAverages = new HashMap<String, List<CycleValuePair>>();
            cache.put(symbol_, symbolAverages);
        }
        
        List<CycleValuePair> pairs = symbolAverages.get(date_);        
        if(pairs == null) {
            pairs = new ArrayList<CycleValuePair>();
            symbolAverages.put(date_, pairs);
        }        
        
        for(CycleValuePair c : pairs) {
            if(c.getCycle() == days_) {
                average = c;
                break;
            }
        }
        
        if(average == null) {
            average = calculateMean(symbol_, date_, days_);
            pairs.add(average);
        }
        
        return average.getValue();
    }
    
    protected CycleValuePair calculateMean(String symbol_, String date_, int days_) {
    	stat.clear();    	
    	List<GenericComputableEntry> stocks = values.get(symbol_);
        CycleValuePair cv = null;
        GenericComputableEntry s = new GenericComputableEntry(symbol_, null, -1);          
        String curDate = date_;           
        
        for(int i = 0; i < days_; i++) {
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
        
        cv = new CycleValuePair(days_, stat.getMean());
        return cv;
    }
}

