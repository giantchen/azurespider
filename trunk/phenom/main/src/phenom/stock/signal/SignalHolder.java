package phenom.stock.signal;

import java.util.Collections;
import java.util.List;

import phenom.stock.signal.pricemomentum.EMovingAverage;
import phenom.stock.signal.pricemomentum.PriceReverse;
import phenom.stock.signal.pricemomentum.DeltaEMAverage;

public class SignalHolder {
	EMovingAverage eMovingAverage = new EMovingAverage();
	PriceReverse priceReverse = new PriceReverse();
	DeltaEMAverage deltaEMAverage = new DeltaEMAverage();
	
	public SignalHolder(List<String> symbols, String startDate, String endDate) {
		
	}
	
	public SignalHolder(String startDate, String endDate) {
		
	}
	
	public void setValues(List<GenericComputableEntry> entries, boolean sort) {
		if (sort) {
			Collections.sort(entries);
		}

		eMovingAverage.addPrices(entries, false);
		priceReverse.addPrices(entries, false);
	}

	public double getEMAverage(String symbol, String date, int cycle) {
		return eMovingAverage.calculate(symbol, date, cycle);
	}

	public double getPriceReverse(String symbol, String date, int cycle) {
		return priceReverse.calculate(symbol, date, cycle);
	}

	public void init(List<String> symbol, String startDate, String endDate) {
		
	}
}
