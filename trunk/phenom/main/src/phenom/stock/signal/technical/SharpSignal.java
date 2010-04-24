package phenom.stock.signal.technical;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phenom.stock.signal.GenericComputableEntry;
import phenom.stock.signal.SignalConstants;


public class SharpSignal extends AbstractTechnicalSignal {
	private MeanSignal meanSignal = new MeanSignal();
	private VolatilitySignal volatilitySignal = new VolatilitySignal();
	
	public SharpSignal()
	{
		_if = new HashMap<String, Double>();
	}
	
	public void setCycle(int cycle_) { 
		volatilitySignal.setCycle(cycle_); 
		meanSignal.setCycle(cycle_);
	}
	
	public void reset() {
		volatilitySignal.reset();
		meanSignal.reset();
	}
	
	@Override
	public void addPrice(GenericComputableEntry s) {
		meanSignal.addPrice(s);
		volatilitySignal.addPrice(s);
		super.addPrice(s);
	}

	@Override
	public void addPrices(List<? extends GenericComputableEntry> s) {
		meanSignal.addPrices(s);
		volatilitySignal.addPrices(s);
		super.addPrices(s);
	}
	
	@Override
	public void addRiskFreeRate(Map<String, Double> riskFreeRate) {
		meanSignal.addRiskFreeRate(riskFreeRate);
		volatilitySignal.addRiskFreeRate(riskFreeRate);
	}

	@Override
	public void addRiskFreeRate(String date, double value) {
		meanSignal.addRiskFreeRate(date, value);
		volatilitySignal.addRiskFreeRate(date, value);
	}

	@Override
	public double calculate(String symbol, String date) {
		if (!isTradeDate(symbol, date)) {
			return SignalConstants.INVALID_VALUE;
		}
		
		return meanSignal.calculate(symbol, date) / volatilitySignal.calculate(symbol, date); 
	}
}
