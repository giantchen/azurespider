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
    //protected List<Stock> values = new ArrayList<Stock>();
    protected Map<String, List<Stock>> values = new HashMap<String, List<Stock>>();
    
    @Override
    public void addValues(List<Stock> s_) {
    	addValues(s_, false);
    }
    
    public void addValues(List<Stock> s_, boolean sort_) {
    	for(Stock s : s_) {
    		List<Stock> st = values.get(s.getSymbol());
    		if(st == null) {
    			st = new ArrayList<Stock>();
    			values.put(s.getSymbol(), st);
    		}
    		st.add(s);
    	}    	
        
        if(sort_) {
        	for(String sb : cache.keySet()) {
        		Collections.sort(values.get(sb));
        	}
        }
    }
    
    @Override
    public void addValue(Stock s_) {    	
    	addValue(s_, false);
    }
    /**
     * 
     * @param s_
     * @param sort_ if already sorting, should parse false
     */
    public void addValue(Stock s_, boolean sort_) {
    	List<Stock> s = values.get(s_.getSymbol());
    	if(s == null) {
    		s = new ArrayList<Stock>();
    		values.put(s_.getSymbol(), s);
    	}
    	s.add(s_);
        if(sort_) {
        	Collections.sort(s);
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

