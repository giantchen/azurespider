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
        	for(String sb : values.keySet()) {
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
    
    @Override
    public void clear() {
        clear(false);
    } 
    
    public void clear(boolean clearCache_) {
    	values.clear();
    	if(clearCache_) {
    		cache.clear();
    	}
    }
    
    @Override
    public boolean isCalculated(String symbol_, String date_, int cycle_) {
    	boolean flag = false;
    	
    	Map<String, List<CycleValuePair>> m = cache.get(symbol_);
    	if(m != null) {
    		List<CycleValuePair> l = m.get(date_);
    		if(l != null) {
    			for(CycleValuePair c : l) {
    				if(c.getCycle() == cycle_) {
    					flag = true;
    					break;
    				}
    			}
    		}
    	}
    	
    	return flag;
    }
    
    /**
     * used when calculate 1 tech indicator which depends on another indicator
     * no need to keep 2 copies
     * @param s_
     */
    protected void setValues(Map<String, List<Stock>> s_) {
    	this.values = s_;
    }
    
    protected void validate(String symbol_, String date_, int cycle_) {
    	String s = null;
    	if(symbol_ == null || date_ == null || cycle_ <= 0) {
    		s = "Symbol and Date must be set up, Days must be positive";
    	} else if (values == null || values.size() == 0) {
    		s = "Value is not initialized";
    	}
    	
    	if(s != null) {
    		throw new RuntimeException(s);
    	}
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
