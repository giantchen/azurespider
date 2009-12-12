package phenom.stock.signal.pricemomentum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phenom.stock.signal.GenericComputableEntry;
import phenom.utils.DateUtil;

public class DeltaEMAverage extends AbstractPriceMomentumSignal {
	EMovingAverage emv;

	public DeltaEMAverage() {
		emv = new EMovingAverage();
	}

	@Override
	public void clear(boolean clearCache_) {
		emv.clear(clearCache_);
	}

	@Override
	public void addPrices(List<? extends GenericComputableEntry> s_) {
		emv.addPrices(s_);
	}

	@Override
	public void addPrice(GenericComputableEntry s_) {
		emv.addPrice(s_);
	}

	@Override
	public void setPrices(Map<String, List<GenericComputableEntry>> s_) {
		emv.setPrices(s_);
	}

	public Map<String, List<GenericComputableEntry>> getValues() {
		return emv.values;
	}
	
	@Override
	public double calculate(String symbol, String date, int cycle) {
		double delta = AbstractPriceMomentumSignal.INVALID_VALUE;
		validate(symbol, date, cycle);
		
		if(!isTradeDate(symbol, date)) {
			return delta;
		}
		
		if(!super.isCalculated(symbol, date, cycle)) {
			String previousTD = DateUtil.previousTradeDate(symbol, date);
			double now = emv.getAverage(symbol, date, cycle);			
			
			if(!isValid(now)) {
				return delta;
			}
			
			double yesterday = emv.getAverage(symbol, previousTD, cycle);			
			delta = (now - yesterday) / yesterday;
			
			//add into cache
			Map<String, List<CycleValuePair>> symbolAverages = new HashMap<String, List<CycleValuePair>>();
			cache.put(symbol, symbolAverages);
			List<CycleValuePair> pairs = new ArrayList<CycleValuePair>();
			pairs.add(new CycleValuePair(cycle, delta));
			symbolAverages.put(date, pairs);
		}
		
		return delta;
	}
	
	public double getDelta(String symbol_, String date_, int cycle_) {
		return calculate(symbol_, date_, cycle_);
	}
}
