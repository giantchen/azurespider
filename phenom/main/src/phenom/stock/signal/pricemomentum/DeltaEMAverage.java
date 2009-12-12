package phenom.stock.signal.pricemomentum;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phenom.stock.signal.GenericComputableEntry;
import phenom.utils.DateUtil;

public class DeltaEMAverage extends AbstractPriceMomentumSignal {
	EMovingAverage emv;

	public DeltaEMAverage(int cycle) {
		super(cycle);
		emv = new EMovingAverage(cycle);
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

	public Map<String, List<GenericComputableEntry>> getPrices() {
		return emv.values;
	}
	
	@Override
	public boolean isTradeDate(String symbol, String date) {
		if (Collections.binarySearch(emv.getPrices().get(symbol), new GenericComputableEntry(symbol, date, -1)) < 0) {
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public double calculate(String symbol, String date) {
		double delta = AbstractPriceMomentumSignal.INVALID_VALUE;
		validate(symbol, date, cycle);
		
		if(!isTradeDate(symbol, date)) {
			return delta;
		}
		
		if(!super.isCalculated(symbol, date, cycle)) {
			String previousTD = DateUtil.previousTradeDate(symbol, date);
			double now = emv.calculate(symbol, date);			
			
			if(!isValid(now)) {
				return delta;
			}
			
			double yesterday = emv.calculate(symbol, previousTD);			
			delta = (now - yesterday) / yesterday;
			
			//add into cache
			Map<String, Double> symbolAverages = new HashMap<String, Double>();
			cache.put(symbol, symbolAverages);			
			symbolAverages.put(date, delta);
		}
		
		return delta;
	}
}
