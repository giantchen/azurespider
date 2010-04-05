package phenom.stock.signal;

import java.util.List;
import java.util.Map;

import phenom.stock.Stock;
import phenom.stock.signal.fundmental.CashFlowToPrice;
import phenom.stock.signal.fundmental.EarningPerShare;
import phenom.stock.signal.fundmental.EarningToPrice;
import phenom.stock.signal.fundmental.FundmentalData;
import phenom.stock.signal.fundmental.NetAssetsPerShare;
import phenom.stock.signal.fundmental.NetProfit;
import phenom.stock.signal.fundmental.ReturnOnEquity;
import phenom.stock.signal.pricemomentum.DeltaEMAverage;
import phenom.stock.signal.pricemomentum.EMovingAverage;
import phenom.stock.signal.pricemomentum.PriceReverse;
import phenom.stock.signal.technical.AbstractTechnicalSignal;
import phenom.stock.signal.technical.AlphaSignal;
import phenom.stock.signal.technical.BetaSignal;
import phenom.stock.signal.technical.MeanSignal;
import phenom.stock.signal.technical.VolatilitySignal;

public class SignalHolder {
	String startDate;
	String endDate;
	List<String> symbols;
	
	EMovingAverage eMovingAverage = new EMovingAverage(1);
	PriceReverse priceReverse = new PriceReverse(1);
	DeltaEMAverage deltaEMAverage = new DeltaEMAverage(1);
	EarningToPrice earningToPrice = new EarningToPrice();
	NetAssetsPerShare netAssetsPerShare = new NetAssetsPerShare();
	CashFlowToPrice cashFlowToPrice = new CashFlowToPrice();
	EarningPerShare earningPerShare = new EarningPerShare();
	NetProfit netProfit = new NetProfit();
	ReturnOnEquity returnOnEquity = new ReturnOnEquity();
	AlphaSignal alphaSignal = new AlphaSignal();
	BetaSignal betaSignal = new BetaSignal();
	VolatilitySignal volatilitySignal = new VolatilitySignal();
	MeanSignal meanSignal = new MeanSignal();
	
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
			cashFlowToPrice.addPrices(stocks);
			
			// add technical signals
			alphaSignal.addPrices(stocks);
			betaSignal.addPrices(stocks);
			volatilitySignal.addPrices(stocks);
			meanSignal.addPrices(stocks);
		}
		
		//add the fundamental
		List<FundmentalData> fundmentalDatas = FundmentalData.loadFundmentalData(symbols, startDate, endDate);
		earningToPrice.addFundmentalData(fundmentalDatas);
		netAssetsPerShare.addFundmentalData(fundmentalDatas);
		cashFlowToPrice.addFundmentalData(fundmentalDatas);
		earningPerShare.addFundmentalData(fundmentalDatas);
		netProfit.addFundmentalData(fundmentalDatas);
		returnOnEquity.addFundmentalData(fundmentalDatas);
		
		// risk-free interest rate
		Map<String, Double> risk_free_rate = AbstractTechnicalSignal.loadRiskFreeInterestRate();
		alphaSignal.addRiskFreeRate(risk_free_rate);
		betaSignal.addRiskFreeRate(risk_free_rate);
	}	
	
	// Price Momentum signals
	public double getEMAverage(String symbol, String date) {
		return eMovingAverage.calculate(symbol, date);
	}
	
	public ISignal getEMSignal(int cycle) {
		return eMovingAverage;
	}
	

	public double getPriceReverse(String symbol, String date) {
		return priceReverse.calculate(symbol, date);
	}	
	
	public double getEMDelta(String symbol, String date) {
		return deltaEMAverage.calculate(symbol, date);
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
	
	public CashFlowToPrice getCashFlowToPrice() {
		return cashFlowToPrice;
	}
	
	public double getCashFlowToPrice(String symbol, String date) {
		return cashFlowToPrice.calculate(symbol, date);
	}
	
	public EarningPerShare getEarningPerShare() {
		return earningPerShare;
	}
	
	public double getEarningPerShare(String symbol, String date) {
		return earningPerShare.calculate(symbol, date);
	}
	
	public NetProfit getNetProfit() {
		return netProfit;
	}
	
	public double getNetProfit(String symbol, String date) {
		return netProfit.calculate(symbol, date);
	}
	
	public ReturnOnEquity getReturnOnEquity() {
		return returnOnEquity;
	}
	
	public double getReturnOnEquity(String symbol, String date) {
		return returnOnEquity.calculate(symbol, date);
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
	
	public AlphaSignal getAlphaSignal() {
		return alphaSignal;
	}

	public BetaSignal getBetaSignal() {
		return betaSignal;
	}

	public VolatilitySignal getVolatilitySignal() {
		return volatilitySignal;
	}

	public MeanSignal getMeanSignal() {
		return meanSignal;
	}
}
