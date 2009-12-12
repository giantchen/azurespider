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
	public void addValues(List<? extends GenericComputableEntry> s_) {
		emv.addValues(s_);
	}

	@Override
	public void addValue(GenericComputableEntry s_) {
		emv.addValue(s_);
	}

	@Override
	public void setValues(Map<String, List<GenericComputableEntry>> s_) {
		emv.setValues(s_);
	}

	public Map<String, List<GenericComputableEntry>> getValues() {
		return emv.values;
	}
	
	@Override
	public double calculate(String symbol_, String date_, int cycle_) {
		double delta = AbstractPriceMomentumSignal.INVALID_VALUE;
		validate(symbol_, date_, cycle_);
		
		if(!super.isCalculated(symbol_, date_, cycle_)) {
			String previousTD = DateUtil.previousTradeDate(symbol_, date_);
			double now = emv.getAverage(symbol_, date_, cycle_);			
			
			if(!isValid(now)) {
				return delta;
			}
			
			double yesterday = emv.getAverage(symbol_, previousTD, cycle_);			
			delta = (now - yesterday) / yesterday;
			
			//add into cache
			Map<String, List<CycleValuePair>> symbolAverages = new HashMap<String, List<CycleValuePair>>();
			cache.put(symbol_, symbolAverages);
			List<CycleValuePair> pairs = new ArrayList<CycleValuePair>();
			pairs.add(new CycleValuePair(cycle_, delta));
			symbolAverages.put(date_, pairs);
		}
		
		return delta;
	}
	
	public double getDelta(String symbol_, String date_, int cycle_) {
		return calculate(symbol_, date_, cycle_);
	}
}
