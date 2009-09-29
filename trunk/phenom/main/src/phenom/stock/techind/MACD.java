package phenom.stock.techind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import phenom.stock.GenericComputableEntry;

/**
 * 
 * Should refactor the 2 moving average algorithm so this MACD didn't depend on concrete implementation
 * Add a calculated method
 * Add the calculated
 */
public class MACD extends AbstractTechIndicator {
    public static final int DEFAULT_SHORT_CYCLE = 12;
    public static final int DEFAULT_LONG_CYCLE = 26;
    public static final int DEFAULT_DEA_CYCLE = 9; 
    
    Map<String, Map<String, List<CycleValuePair>>> DEACache = new HashMap<String, Map<String, List<CycleValuePair>>>();
    
    Set<String> calculatedCycleCache = new HashSet<String>();    
    
    EMovingAverage ema = null;
    
    public MACD() {
        ema = new ExponentialMA();        
    }
    
    @Override
    public void addValues(List<? extends GenericComputableEntry> s_) {
        super.addValues(s_);
        ema.setValues(values);
    }
    
    @Override
    public void clear(boolean clearCache_) {
        super.clear(clearCache_);
        DEACache.clear();
        ema.clear(clearCache_);
        calculatedCycleCache.clear();
    }
    
    public double getDIFF(String symbol_, String date_, int cycle_) {
        List<CycleValuePair> pairs = cache.get(symbol_).get(date_);
        for(CycleValuePair c : pairs) {
            if(c.getCycle() == cycle_) {
                return c.getValue();
            }
        }
        
        return AbstractTechIndicator.INVALID_VALUE;
    }
    
    public double getDEA(String symbol_, String date_, int cycle_) {
        List<CycleValuePair> pairs = DEACache.get(symbol_).get(date_);
        for(CycleValuePair c : pairs) {
            if(c.getCycle() == cycle_) {
                return c.getValue();
            }
        }
        
        return AbstractTechIndicator.INVALID_VALUE;
    }    
    
    public double calculate(GenericComputableEntry s_) {
        return calculate(s_, DEFAULT_SHORT_CYCLE, DEFAULT_LONG_CYCLE);
    }
    
    public double calculate(GenericComputableEntry s_, int shortCycle_, int longCycle_) {
        return calculate(s_.getSymbol(), s_.getDate(), shortCycle_, longCycle_);
    }
    
    @Override
    public double calculate(String symbol_, String date_, int cycle_) {
        return calculate(symbol_, date_, DEFAULT_SHORT_CYCLE, DEFAULT_LONG_CYCLE);
    }
    
    /**
     * Calculate Average Specified by days
     */    
    public double calculate(String symbol_, String date_, int shortCycle_, int longCycle_) {
        double average = 0;
        validate(symbol_, date_, shortCycle_);
        validate(symbol_, date_, longCycle_);
        
        String k = symbol_ + String.valueOf(shortCycle_) + String.valueOf(longCycle_);
        if(!calculatedCycleCache.contains(k)) {
            calculateDIF(symbol_, date_, shortCycle_, longCycle_);
            calculateDEA(symbol_, date_, DEFAULT_DEA_CYCLE);
            calculatedCycleCache.add(k);
        }
        
        int days = shortCycle_ + longCycle_;
        List<CycleValuePair> pairs = DEACache.get(symbol_).get(date_);
        for(CycleValuePair c : pairs) {
            if(c.getCycle() == days) {
                average = c.getValue();
                break;
            }
        }
        
        return average;
    }
    
    /**
     * Eager Calculation
     */
    protected void calculateDIF(String symbol_, String date_, int shortCycle_, int longCycle_) {
        int days_ = shortCycle_ + longCycle_;
        ema.calculate(symbol_, date_, shortCycle_);
        ema.calculate(symbol_, date_, longCycle_);
        
        List<GenericComputableEntry> stocks = values.get(symbol_);        
        Map<String, List<CycleValuePair>> macds = cache.get(symbol_);            
        if (macds == null) {
            macds = new HashMap<String, List<CycleValuePair>>();
            cache.put(symbol_, macds);
        }      
            
        for(GenericComputableEntry s : stocks) {
            List<CycleValuePair> pairs = macds.get(s.getDate());        
            if(pairs == null) {
                pairs = new ArrayList<CycleValuePair>();
                macds.put(s.getDate(), pairs);
            }
            CycleValuePair v = new CycleValuePair(days_,
                    ema.calculate(symbol_, s.getDate(), shortCycle_) - ema.calculate(symbol_, s.getDate(), longCycle_)); 
            pairs.add(v);
        }
    }
    
    /**
     * Eager Calulation
     * @param cycle_
     */
    protected void calculateDEA(String symbol_, String date_, int cycle_) {
        int diffCycle = DEFAULT_SHORT_CYCLE + DEFAULT_LONG_CYCLE;
        List<GenericComputableEntry> stocks = values.get(symbol_);
        
        Map<String, List<CycleValuePair>> symbolDEAs = DEACache.get(symbol_);            
        if (symbolDEAs == null) {
            symbolDEAs = new HashMap<String, List<CycleValuePair>>();
            DEACache.put(symbol_, symbolDEAs);
        }
        
        for(int i = 0; i < stocks.size(); i++) {
            CycleValuePair c = null;
            GenericComputableEntry s = stocks.get(i);            
            
            List<CycleValuePair> pairs = symbolDEAs.get(s.getDate());        
            if(pairs == null) {
                pairs = new ArrayList<CycleValuePair>();
                symbolDEAs.put(s.getDate(), pairs);
            }            
            
            if(i == 0) {                
                c = new CycleValuePair(cycle_, getDIFF(symbol_, s.getDate(), diffCycle));                
            } else {
                //find previous dea
                CycleValuePair previosPair = null;
                List<CycleValuePair> previousPairs = symbolDEAs.get(stocks.get(i - 1).getDate());
                for(CycleValuePair cv : previousPairs) {
                    if(cv.getCycle() == cycle_) {
                        previosPair = cv;
                        break;
                    }
                }                
                
                double []factor = calculateFactor(cycle_);                
                c = new CycleValuePair(cycle_, getDIFF(symbol_, s.getDate(), diffCycle) * factor[0]
                        + previosPair.getValue() * factor[1]);                
            }
            pairs.add(c);
        }
    }    
   
    protected double[] calculateFactor(int... cycle_) {
        int p1 = cycle_[0];
        double factor1 = 2 / ((double)(p1 + 1));
        double factor2 = ((double)(p1 - 1)) / (p1 + 1);
        return new double[]{factor1, factor2};
    }
    
    /**
     * A different factor calculation method
     * 2/(N+1) * Today MV + (N - 1)/(N + 1) * Yesterday MV
     *
     */
    static class ExponentialMA extends EMovingAverage {
        @Override
        protected double[] calculateFactor(int... cycle_) {        
            int p1 = cycle_[0];
            double factor1 = 2 / ((double)(p1 + 1));
            double factor2 = ((double)(p1 - 1)) / (p1 + 1);
            return new double[]{factor1, factor2};
        }
    }
}

