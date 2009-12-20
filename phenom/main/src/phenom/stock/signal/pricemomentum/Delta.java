package phenom.stock.signal.pricemomentum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phenom.stock.signal.GenericComputableEntry;
import phenom.utils.DateUtil;

/**
 * 
 * cycle is useless for this signal
 *
 */
public class Delta extends AbstractPriceMomentumSignal {
	public Delta(int cycle) {
		super(cycle);
	}
	
	@Override
	public double calculate(final String symbol, final String date) {
		double average = AbstractPriceMomentumSignal.INVALID_VALUE;
		validate(symbol, date, cycle);
		
		if(!isTradeDate(symbol, date)) {
			return average;
		}
		
		if(!super.isCalculated(symbol, date, cycle)) {
			calculate(symbol);
		}		
		return  cache.get(symbol).get(date);
	}
	
	/*
	 * The calculate must be called before calling this method.
	 * This method is used to provide a interface
	 * */	
	public List<GenericComputableEntry> getDeltas(String symbol_) {
		List<GenericComputableEntry> lst = new ArrayList<GenericComputableEntry>();
		Map<String, Double> r = cache.get(symbol_);
				
		for(String k : r.keySet()) {
			lst.add(new GenericComputableEntry(symbol_, k, r.get(k)));
		}
		
		return lst;
	}
	
	private void calculate(String symbol_) {
		List<GenericComputableEntry> stocks = prices.get(symbol_);
		
		for(int i = 0; i < stocks.size(); i++) {			
    		GenericComputableEntry s = stocks.get(i);
    		
    		Map<String, Double> symbolAverages = cache.get(s.getSymbol());
    		if (symbolAverages == null) {
                symbolAverages = new HashMap<String, Double>();
                cache.put(symbol_, symbolAverages);
            }   
           
            if(i == 0) {
            	symbolAverages.put(s.getDate(), 0d);
            } else {           
            	double pt = stocks.get(i).getValue();
            	double py = stocks.get(i - 1).getValue();
            	int days = DateUtil.workingDaySpan(symbol_, stocks.get(i - 1).getDate(), stocks.get(i).getDate());
            	symbolAverages.put(s.getDate(), (pt - py) / py / days);
            }
		}
	}
}
