package phenom.stock.signal;

import java.util.Collection;
import java.util.LinkedList;
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
import phenom.stock.signal.technical.CovarianceSignal;
import phenom.stock.signal.technical.MeanSignal;
import phenom.stock.signal.technical.SharpSignal;
import phenom.stock.signal.technical.VolatilitySignal;

public class SignalHolder {
	String startDate;
	String endDate;
	Collection<String> symbols;
	
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
	CovarianceSignal covarianceSignal = new CovarianceSignal();
	SharpSignal sharpSignal = new SharpSignal();
	List<Stock> _stocks;
	List<FundmentalData> _fundementals;
	Map<String, Double> _risk_free_rate;
	
	public SignalHolder(Collection<String> symbols, String startDate, String endDate) {
		this.symbols = symbols;
		this.startDate = startDate;
		this.endDate = endDate;
		_stocks = new LinkedList<Stock>();
		for(String s : symbols) {
			List<Stock> stocks = Stock.getStock(s, startDate, endDate, true);
			_stocks.addAll(stocks);
			
			eMovingAverage.addPrices(stocks);
			priceReverse.addPrices(stocks);
			deltaEMAverage.addPrices(stocks);
		}
		
		//add the fundamental
		_fundementals = FundmentalData.loadFundmentalData(symbols, startDate, endDate);	
		
		// risk-free interest rate
		_risk_free_rate = AbstractTechnicalSignal.loadRiskFreeInterestRate();
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
	
	// Fundemental signals
	
	public CashFlowToPrice getCashFlowToPrice() {
		cashFlowToPrice.addPrices(_stocks);
		cashFlowToPrice.addFundmentalData(_fundementals);
		return cashFlowToPrice;
	}
	
	public NetAssetsPerShare getNetAssetsPerShareSignal() {
		netAssetsPerShare.addFundmentalData(_fundementals);
		return this.netAssetsPerShare;
	}

	public EarningPerShare getEarningPerShare() {
		earningPerShare.addFundmentalData(_fundementals);
		return earningPerShare;
	}
	
	public NetProfit getNetProfit() {
		netProfit.addFundmentalData(_fundementals);
		return netProfit;
	}
	
	public ReturnOnEquity getReturnOnEquity() {
		returnOnEquity.addFundmentalData(_fundementals);
		return returnOnEquity;
	}
	
	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public List<String> getSymbols() {
		List<String> ret = new LinkedList<String>();
		ret.addAll(symbols);
		return ret;
	}
	
	public AlphaSignal getAlphaSignal() {
		alphaSignal.addPrices(_stocks);
		alphaSignal.addRiskFreeRate(_risk_free_rate);
		return alphaSignal;
	}

	public BetaSignal getBetaSignal() {
		betaSignal.addPrices(_stocks);
		betaSignal.addRiskFreeRate(_risk_free_rate);
		return betaSignal;
	}

	public VolatilitySignal getVolatilitySignal() {
		volatilitySignal.addPrices(_stocks);
		volatilitySignal.addRiskFreeRate(_risk_free_rate);
		return volatilitySignal;
	}

	public MeanSignal getMeanSignal() {
		meanSignal.addPrices(_stocks);
		meanSignal.addRiskFreeRate(_risk_free_rate);
		return meanSignal;
	}

	public CovarianceSignal getCovarianceSignal() {
		covarianceSignal.addPrices(_stocks);
		covarianceSignal.addRiskFreeRate(_risk_free_rate);
		return covarianceSignal;
	}

	public SharpSignal getSharpSignal() {
		sharpSignal.addPrices(_stocks);
		sharpSignal.addRiskFreeRate(_risk_free_rate);
		return sharpSignal;
	}

	public EarningToPrice getEarningToPriceSignal() {
		this.earningToPrice.addPrices(_stocks);
		this.earningToPrice.addFundmentalData(_fundementals);
		return earningToPrice;
	}
}
