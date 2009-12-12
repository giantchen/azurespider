package phenom.stock.signal.pricemomentum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phenom.stock.signal.GenericComputableEntry;
import phenom.utils.DateUtil;

public class Delta extends AbstractPriceMomentumSignal {
	public Delta(int cycle) {
		super(cycle);
	}
	
	@Override
	public double calculate(final String symbol, final String date, final int cycle) {
		double average = AbstractPriceMomentumSignal.INVALID_VALUE;
		validate(symbol, date, cycle);
		
		if(!isTradeDate(symbol, date)) {
			return average;
		}
		
		if(!super.isCalculated(symbol, date, cycle)) {
			calculate(symbol, cycle);
		}		
		List<CycleValuePair> pairs = cache.get(symbol).get(date);        
        for(CycleValuePair c : pairs) {
            if(c.getCycle() == cycle) {
                average = c.getValue();
                break;
            }
        }
        
		return average;
	}
	
	public double getDelta(final String symbol, final String date, final int cycle) {
		return calculate(symbol, date, cycle);
	}
	
	public List<GenericComputableEntry> getDeltas(String symbol_, int cycle_) {
		List<GenericComputableEntry> lst = new ArrayList<GenericComputableEntry>();
		Map<String, List<CycleValuePair>> r = cache.get(symbol_);
		double v = -1;
		
		for(String k : r.keySet()) {
			List<CycleValuePair> l = r.get(k);
			for(CycleValuePair c : l) {
				if(c.getCycle() == cycle_) {
					v = c.getValue();
					break;
				}
			}
			lst.add(new GenericComputableEntry(symbol_, k, v));
		}		
		return lst;
	}
	
	private void calculate(String symbol_, int cycle_) {
		List<GenericComputableEntry> stocks = values.get(symbol_);
		
		for(int i = 0; i < stocks.size(); i++) {
			CycleValuePair c = null;
    		GenericComputableEntry s = stocks.get(i);
    		
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
            	c = new CycleValuePair(cycle_, 0);
            } else {           
            	double pt = stocks.get(i).getValue();
            	double py = stocks.get(i - 1).getValue();
            	int days = DateUtil.workingDaySpan(symbol_, stocks.get(i - 1).getDate(), stocks.get(i).getDate());
            	c = new CycleValuePair(cycle_, (pt - py) / py / days);
            }
            
            pairs.add(c);
		}
	}
}
