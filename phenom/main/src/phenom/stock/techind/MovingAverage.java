package phenom.stock.techind;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import phenom.stock.Stock;
import phenom.utils.DateUtil;
import phenom.stock.Cycle;

/**
 * This should behave like a util class.
 * Will refactor later
 * 
 */
public class MovingAverage extends AbstractTechIndicator{       
    DescriptiveStatistics stat = new DescriptiveStatistics();    
    
    /**
     * By Default calculate Week/Ten Day/Fifteen Day/Month Average
     * After set, the averages are available via getAverage
     */
    public void setAverage(Stock s_) {
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
    
    public double getAverage(Stock s_, Cycle c_) {
        return getAverage(s_, c_.numDays());
    }
    
    public double getAverage(Stock s_, int days_) {
        return getAverage(s_.getSymbol(), s_.getDate(), days_);
    }    
    
    /**
     * Calculate Average Specified by cycle_
     */
    public double getAverage(String symbol_, String date_, Cycle cycle_) {
        return getAverage(symbol_, date_, cycle_.numDays());
    }    
    
    /**
     * Calculate Average Specified by days
     */
    public double getAverage(String symbol_, String date_, int days_) {
        if(symbol_ == null || date_ == null) {
            throw new RuntimeException("Symbol and Date must be set up");
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
            average = calculate(symbol_, date_, days_);
            pairs.add(average);
        }
        
        return average.getValue();
    }
    
    protected CycleValuePair calculate(String symbol_, String date_, int days_) {
        CycleValuePair cv = null;
        Stock s = new Stock(symbol_);          
        String curDate = date_;           
        
        for(int i = 0; i < days_; i++) {
            s.setDate(curDate);
            int index = Collections.binarySearch(values, s);
            
            while(index < 0) {
                if(Math.abs(index + 1) == 0) {
                    //no data in available, should throw exception, refactor later
                    break;
                } else {
                    curDate = DateUtil.previousDay(s.getDate());
                    s.setDate(curDate);
                    index = Collections.binarySearch(values, s);
                }                
            }
            
            if(index >= 0) { //find 1 record               
                stat.addValue(values.get(index).getClosePrice());       
            }
            
            curDate = DateUtil.previousDay(s.getDate());            
        }
        
        cv = new CycleValuePair(days_, stat.getMean());
        return cv;
    }
}

