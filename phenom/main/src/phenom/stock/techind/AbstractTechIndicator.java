package phenom.stock.techind;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import phenom.stock.Stock;
/**
 * This should behave like a util class.
 * Will refactor later
 * 
 */
public abstract class AbstractTechIndicator implements ITechnicalIndicator<Stock>{
    //key = symbol, value(key = date, List(value = CycleValuePair))
    protected Map<String, Map<String, List<CycleValuePair>>> cache = new HashMap<String, Map<String, List<CycleValuePair>>>();    
    protected List<Stock> values = new ArrayList<Stock>();
    
    public void addValues(List<Stock> s_) {
    	addValues(s_, false);
    }
    
    public void addValues(List<Stock> s_, boolean sort_) {
        values.addAll(s_);
        if(sort_) {
        	Collections.sort(values);
        }
    }
    
    public void addValue(Stock s_) {
    	addValue(s_, false);
    }
    /**
     * 
     * @param s_
     * @param sort_ if already sorting, should parse false
     */
    public void addValue(Stock s_, boolean sort_) {
        values.add(s_);
        if(sort_) {
        	Collections.sort(values);
        }
    }
    
    public void clear() {
        values.clear();
        cache.clear();
    } 
    
    public static class CycleValuePair {
        private double value;
        private int cycle;
        
        /**
         * @param value
         * @param cycle
         */
        public CycleValuePair(int cycle, double value) {
            super();
            this.value = value;
            this.cycle = cycle;
        }        
        
        public double getValue() {
            return this.value;
        }
        
        public int getCycle() {
            return this.cycle;
        }
    }    
}

