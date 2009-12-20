package phenom.stock.signal.pricemomentum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import phenom.stock.signal.GenericComputableEntry;

/**
 * 
 * Should refactor the 2 moving average algorithm so this MACD didn't depend on concrete implementation
 * Add a calculated method
 * Add the calculated
 */
public class MACD extends AbstractPriceMomentumSignal {
    public static final int DEFAULT_SHORT_CYCLE = 12;
    public static final int DEFAULT_LONG_CYCLE = 26;
    public static final int DEFAULT_DEA_CYCLE = 9; 
    
    private int shortCycle;
    private int longCycle;
    private int deaCycle;
    
    Map<String, Map<String, Double>> DEACache = new HashMap<String, Map<String, Double>>();
    
    Set<String> calculatedCycleCache = new HashSet<String>();    
    
    ExponentialMA emaShort = null;
    ExponentialMA emaLong = null;   
    
    public MACD() {
    	this(DEFAULT_SHORT_CYCLE, DEFAULT_LONG_CYCLE, DEFAULT_DEA_CYCLE);
    }
    public MACD(int shortCycle, int longCycle, int deaCycle) {
    	super(shortCycle);
    	this.shortCycle = shortCycle;
    	this.longCycle = longCycle;
    	this.deaCycle = deaCycle;
    	emaShort = new ExponentialMA(shortCycle);
    	emaLong = new ExponentialMA(longCycle);
    }
    
    public int getLongCycle() {
    	return longCycle;
    }
    
    public int getShortCycle() {
    	return shortCycle;
    }
    
    public int getDEACycle() {
    	return deaCycle;
    }
    @Override
    public void addPrices(List<? extends GenericComputableEntry> s) {
        super.addPrices(s);
        emaShort.setPrices(prices);
        emaLong.setPrices(prices);
    }
    
    @Override
    public void clear(boolean clearCache_) {
        super.clear(clearCache_);
        DEACache.clear();
        emaShort.clear(clearCache_);
        emaLong.clear(clearCache_);
        calculatedCycleCache.clear();
    }
    
    public double getDIFF(String symbol_, String date_) {
        Double d = cache.get(symbol_).get(date_);
        return d == null ? AbstractPriceMomentumSignal.INVALID_VALUE : d;
    }
    
    public double getDEA(String symbol_, String date_) {
    	Double d = DEACache.get(symbol_).get(date_);
        return d == null ? AbstractPriceMomentumSignal.INVALID_VALUE : d;
    }
    
    @Override    
    public double calculate(String symbol, String date) {
        double average = AbstractPriceMomentumSignal.INVALID_VALUE;
        validate(symbol, date, shortCycle);
        validate(symbol, date, longCycle);
        
        if(!isTradeDate(symbol, date)) {
			return average;
		}
        
        String k = symbol + String.valueOf(shortCycle) + String.valueOf(longCycle);
        if(!calculatedCycleCache.contains(k)) {
            calculateDIF(symbol, date);
            calculateDEA(symbol, date);
            calculatedCycleCache.add(k);
        }
        
        average = DEACache.get(symbol).get(date);        
        return DEACache.get(symbol).get(date);
    }
    
    /**
     * Eager Calculation
     */
    private void calculateDIF(String symbol_, String date_) {
        emaShort.calculate(symbol_, date_);
        emaLong.calculate(symbol_, date_);
        
        List<GenericComputableEntry> stocks = prices.get(symbol_);        
        Map<String, Double> macds = cache.get(symbol_);            
        if (macds == null) {
            macds = new HashMap<String, Double>();
            cache.put(symbol_, macds);
        }      
            
        for(GenericComputableEntry s : stocks) {
            macds.put(s.getDate(),
            			emaShort.calculate(symbol_, s.getDate()) - emaLong.calculate(symbol_, s.getDate()));                  
        }
    }
    
    /**
     * Eager Calulation
     * @param cycle_
     */
    private void calculateDEA(String symbol_, String date_) {        
        List<GenericComputableEntry> stocks = prices.get(symbol_);
        
        Map<String, Double> symbolDEAs = DEACache.get(symbol_);            
        if (symbolDEAs == null) {
            symbolDEAs = new HashMap<String, Double>();
            DEACache.put(symbol_, symbolDEAs);
        }
        
        for(int i = 0; i < stocks.size(); i++) {
            Double c = null;
            GenericComputableEntry s = stocks.get(i);
            
            if(i == 0) { 
            	symbolDEAs.put(s.getDate(), getDIFF(symbol_, s.getDate()));              
            } else {                
                Double previousDEA = symbolDEAs.get(stocks.get(i - 1).getDate());                
                double []factor = calculateFactor(deaCycle);                
                c = getDIFF(symbol_, s.getDate()) * factor[0]
                        + previousDEA * factor[1];
                symbolDEAs.put(s.getDate(), c);  
            }
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
    private static class ExponentialMA extends EMovingAverage {
    	public ExponentialMA(int cycle) {
			super(cycle);
		}
        @Override
        protected double[] calculateFactor(int... cycle_) {        
            int p1 = cycle_[0];
            double factor1 = 2 / ((double)(p1 + 1));
            double factor2 = ((double)(p1 - 1)) / (p1 + 1);
            return new double[]{factor1, factor2};
        }
    }
}

