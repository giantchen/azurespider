package phenom.stock.signal;

import java.util.List;

import phenom.stock.Stock;
import phenom.stock.signal.fundmental.EarningToPrice;
import phenom.stock.signal.fundmental.FundmentalData;
import phenom.stock.signal.fundmental.NetAssetsPerShare;
import phenom.stock.signal.pricemomentum.DeltaEMAverage;
import phenom.stock.signal.pricemomentum.EMovingAverage;
import phenom.stock.signal.pricemomentum.PriceReverse;

public class SignalHolder {
	String startDate;
	String endDate;
	List<String> symbols;
	
	EMovingAverage eMovingAverage = new EMovingAverage(1);
	PriceReverse priceReverse = new PriceReverse(1);
	DeltaEMAverage deltaEMAverage = new DeltaEMAverage(1);
	EarningToPrice earningToPrice = new EarningToPrice();
	NetAssetsPerShare netAssetsPerShare = new NetAssetsPerShare();
	
	public SignalHolder(List<String> symbols, String startDate, String endDate) {
		this.symbols = symbols;
		this.startDate = startDate;
		this.endDate = endDate;
		for(String s : symbols) {
			List<Stock> stocks = Stock.getStock(s, startDate, endDate, true);
			
			eMovingAverage.addPrices(stocks);
			priceReverse.addPrices(stocks);
			deltaEMAverage.addPrices(stocks);
			earningToPrice.addPrices(stocks);
			
		}
		
		//add the fundamental
		List<FundmentalData> fundmentalDatas = FundmentalData.loadFundmentalData(symbols, startDate, endDate);
		earningToPrice.addFundmentalData(fundmentalDatas);
		netAssetsPerShare.addFundmentalData(fundmentalDatas);
	}
	
	

	// Price Momentum signals
	public double getEMAverage(String symbol, String date, int cycle) {
		return eMovingAverage.calculate(symbol, date, cycle);
	}
	
	public ISignal getEMSignal(int cycle) {
		return eMovingAverage;
	}
	

	public double getPriceReverse(String symbol, String date, int cycle) {
		return priceReverse.calculate(symbol, date, cycle);
	}	
	
	public double getEMDelta(String symbol, String date, int cycle) {
		return deltaEMAverage.calculate(symbol, date, cycle);
	}
	
	// Fundmental signals
	public EarningToPrice getEarningToPriceSignal() {
		return this.earningToPrice;
	}
	
	public double getEarningToPrice(String symbol, String date) {
		return earningToPrice.calculate(symbol, date);
	}
	
	public NetAssetsPerShare getNetAssetsPerShareSignal() {
		return this.netAssetsPerShare;
	}

	public double getNetAssetsPerShare(String symbol, String date) {
		return netAssetsPerShare.calculate(symbol, date);
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
