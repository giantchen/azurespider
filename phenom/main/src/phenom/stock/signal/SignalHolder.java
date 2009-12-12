package phenom.stock.signal;

import java.util.List;

import phenom.stock.Stock;
import phenom.stock.signal.pricemomentum.EMovingAverage;
import phenom.stock.signal.pricemomentum.PriceReverse;
import phenom.stock.signal.pricemomentum.DeltaEMAverage;

public class SignalHolder {
	String startDate;
	String endDate;
	List<String> symbols;
	
	EMovingAverage eMovingAverage = new EMovingAverage();
	PriceReverse priceReverse = new PriceReverse();
	DeltaEMAverage deltaEMAverage = new DeltaEMAverage();
	
	public SignalHolder(List<String> symbols, String startDate, String endDate) {
		this.symbols = symbols;
		this.startDate = startDate;
		this.endDate = endDate;
		for(String s : symbols) {
			List<Stock> stocks = Stock.getStock(s, startDate, endDate, true);
			
			eMovingAverage.addPrices(stocks);
			priceReverse.addPrices(stocks);
			deltaEMAverage.addPrices(stocks);
			
			//add the fundamental
		}
	}

	public double getEMAverage(String symbol, String date, int cycle) {
		return eMovingAverage.calculate(symbol, date, cycle);
	}

	public double getPriceReverse(String symbol, String date, int cycle) {
		return priceReverse.calculate(symbol, date, cycle);
	}	
	
	public double getEMDelta(String symbol, String date, int cycle) {
		return deltaEMAverage.calculate(symbol, date, cycle);
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public List<String> getSymbols() {
		return symbols;
	}
}
