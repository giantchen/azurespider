package phenom.stock.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import phenom.stock.Dividend;
import phenom.stock.Index;
import phenom.stock.NameValuePair;
import phenom.stock.PositionEntry;
import phenom.stock.Stock;
import phenom.stock.techind.AbstractTechIndicator;
import phenom.stock.techind.DeltaEMAverage;
import phenom.utils.DateUtil;
import phenom.utils.WeightUtil;
import phenom.utils.graph.TimeSeriesGraph;

public class RollingStrategy {
	static Map<String, List<String>> indexStock = new HashMap<String, List<String>>();
	List<Double> pos = new ArrayList<Double>();
	// temporarily didn't track position on each day but only track the current
	// position
	Map<String, PositionEntry> position = new HashMap<String, PositionEntry>();
	List<String> indexSymbols = null;
	//TODO - Initialize trade date
	static List<String> tradeDates = null;
	//TODO Utilize minHolding days
	private int minHoldingDays = 5;
	private double cash = -1;
	private double commission = 0.003;
	private double buyStampTax = 0;
	private double sellStampTax = 0.001;
	private int maxPosCount = 10;

	String startDate = "20080101";
	String endDate = "20081231";

	public RollingStrategy(double cash_, int maxPosCount_, int minHoldingDays_, List<String> indexSymbols_) {
		cash = cash_;
		maxPosCount = maxPosCount_;
		minHoldingDays = minHoldingDays_;
		indexSymbols = indexSymbols_;
	}

	public List<Double> getPos() {
		return pos;
	}
	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}	

	public int getMinHoldingDays() {
		return minHoldingDays;
	}

	public void setMinHoldingDays(int minHoldingDays) {
		this.minHoldingDays = minHoldingDays;
	}

	public static void main(String[] args) {
		RollingStrategy rs = new RollingStrategy(3000000, 10, 5, null);
		DeltaEMAverage dt = new DeltaEMAverage();

		//init mapping and trade dates
		indexStock = Index.getIndexStockMapping();		
		tradeDates = DateUtil.tradeDates(rs.getStartDate(), rs.getEndDate());	
		
		try {
			rs.run(dt, 5);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
				
		TimeSeriesGraph graph = new TimeSeriesGraph("Stock", "Date",
		"Price & Money");
		graph.addDataSource("YangFei", rs.getPos());
		graph.display();
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("WOW&&&&&&&&&&&&&&&&&&" + rs.getFinalCash(tradeDates.get(tradeDates.size() - 1)));	
	}
	
	private void loadStockPrices(DeltaEMAverage dea_) {
		Set<String> loadedStocks = new HashSet<String>();
		for(String indexId : indexStock.keySet()) {
			if(!loadedStocks.contains(indexId)) {
				dea_.addValues(Stock.getStock(indexId, getStartDate(), getEndDate(), false));
				loadedStocks.add(indexId);
			}
			
			List<String> stocks = indexStock.get(indexId);
			for(String symbol : stocks) {
				if(!loadedStocks.contains(symbol)) {
					dea_.addValues(Stock.getStock(symbol, getStartDate(), getEndDate(), false));
					loadedStocks.add(symbol);
				}
			}
		}
	}
	
	public void run(DeltaEMAverage dea_, int days_) throws Exception {
		loadStockPrices(dea_);	
		
		List<NameValuePair> sortedIndex = new ArrayList<NameValuePair>();
		List<NameValuePair> sortedStock = new ArrayList<NameValuePair>();
		List<NameValuePair> allStock = new ArrayList<NameValuePair>();
		List<String> buySymbols = new ArrayList<String>();
		List<String> sellSymbols = new ArrayList<String>();

		// only for test purpose
		String indexSymbol = null, stockSymbol = null, date = null;

		try {
			//skip Zero as we measure the delta
			for(int i = 1; i < tradeDates.size(); i++) {
				String td = tradeDates.get(i);				
				System.out.println("------- Start tradeDate ------" + td);
				
				//adjust the avaliable amount and cash impacted by CA, try to move the inflight to available
				processInflightPB(td);
				
				date = td;
				for(String symbol : indexStock.keySet()) {
					indexSymbol = symbol;
					double delta = dea_.getDelta(symbol, td, days_);
					//index may not be available at that time. e.g. index created from 2009 not in 2008
					if(!AbstractTechIndicator.isValid(delta)) {
						continue;
					}
					NameValuePair s = new NameValuePair(symbol, delta);
					sortedIndex.add(s);
				}
				
				//only start to sell/buy after 5 days
				if(i >= 5 ) {
					Collections.sort(sortedIndex, NameValuePair.VALUE_COMPARATOR_ASC);					
					//asssume there are at least 5 elements, sort stocks within each index
					for(int j = 0; j < sortedIndex.size(); j++) {
						NameValuePair s = sortedIndex.get(j);
						indexSymbol = s.getName();
						System.out.println("****** Start indexSymbol  ****** " + indexSymbol);
						
						List<String> sts = indexStock.get(s.getName());
						for(String st : sts) {
							stockSymbol = st;							
							double delta = dea_.getDelta(st, td, days_);
							
							if(!AbstractTechIndicator.isValid(delta)) {
								continue;
							}
							
							sortedStock.add(new NameValuePair(st, delta));
						}
						
						Collections.sort(sortedStock, NameValuePair.VALUE_COMPARATOR_ASC);
						
						if(j == 0 || j == 1 || j == 2) {
							//pick up buy symbols, for top3 index pick 2 stocks for each
							buySymbols.add(sortedStock.get(0).getName());
							buySymbols.add(sortedStock.get(1).getName());
						}				
						allStock.addAll(sortedStock);
						sortedStock.clear();			
						System.out.println("****** Finish indexSymbol  ****** " + indexSymbol);
					}
					
					//Collections.sort(allStock, NameValuePair.VALUE_COMPARATOR_ASC);
					sellSymbols = evaluateSellSymbol(allStock, buySymbols);
					
					//sell first then buy, do not change the calling order
					sell(sellSymbols, td, sellStampTax, commission);
					buy(buySymbols, td, buyStampTax, commission);
				}			
				
				buySymbols.clear();
				sellSymbols.clear();
				sortedIndex.clear();
				allStock.clear();				
				
				processEntitlement(td);
				System.out.println("------- Finish tradeDate ------" + td);
				pos.add(getFinalCash(td));
			}			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("indexSymbol = " + indexSymbol + " | stockSymbol = "
					+ stockSymbol + " | date = " + date);
		}
	}
	
	private List<String> evaluateSellSymbol(List<NameValuePair> allStocks, List<String> buySymbols) {
		Collections.sort(allStocks, NameValuePair.NAME_COMPARATOR);
		List<String> symbols = new ArrayList<String>();		
		List<NameValuePair> vp = new ArrayList<NameValuePair>();
		int posCount = position.size() + buySymbols.size();
		NameValuePair nvp = new NameValuePair(null, -1);//only used to search
		
		for(String k : position.keySet()) {
			if(buySymbols.contains(k)) {
				posCount--;
				continue;
			}			
			nvp.setName(k);
			
			int i = Collections.binarySearch(allStocks, nvp);
			if(i <= 0) //the stock was suspended
				continue;
			
			vp.add(allStocks.get(i));
		}
		
		Collections.sort(vp, NameValuePair.VALUE_COMPARATOR_DESC);
		
		if(posCount > maxPosCount) {
			for(int i = 0; i < posCount - maxPosCount; i++) {
				if(vp.size() > i)
					symbols.add(vp.get(i).getName());
				else 
					break;
			}
		}
		
		return symbols;
	}
	
	//move inflight to available
	private void processInflightPB(String date_) {
		double val = 0;
		for(String symbol : position.keySet()) {
			PositionEntry pe = position.get(symbol);			
			//assume the next corporate actions comes after current ca being settled			
			val += pe.evaluateInflightCash(date_);
			pe.evaluateInflightPos(date_);
		}
		cash += val;
	}
	
	private void processEntitlement(String date_) {
		for(String symbol : position.keySet()) {
			PositionEntry pe = position.get(symbol);			
			Dividend d = WeightUtil.getEntitledDividend(pe, date_);
			if(d != null) {
				pe.increaseInflight(d);
			}
		}
	}
	
	private void buy(List<String> symbols_, String date_, double stampTax_, double com_) {
		double curCash = cash;		
		for(int i = 0; i < symbols_.size(); i++) {
			if(i == 6) { // only care about the 1st 6 stocks
				break;
			}			
			String symbol = symbols_.get(i);
			System.out.println(symbol + date_);
			double price = Stock.getClosePrice(symbol, date_);
			int bAmount = (int)((curCash * 0.1) / price / 100);
			PositionEntry pe = position.get(symbol);
			if(pe == null) {
				pe = new PositionEntry(symbol, bAmount * 100);
				position.put(symbol, pe);
			} else {
				pe.setAmount(pe.getAmount() + bAmount * 100);
			}				
			curCash -= bAmount * 100 * price * (1 + stampTax_ + com_);
		}
		
		cash = curCash;
	}
	
	private void sell(List<String> symbols_, String date_, double stampTax_, double com_) {
		double curCash = cash;
		for(String symbol : symbols_) {
			PositionEntry pe = position.get(symbol);
			curCash += pe.getAmount() * Stock.getClosePrice(symbol, date_) * (1 - stampTax_ - com_);
			pe.setAmount(0);
			if(pe.isSoldOut()) {
				position.remove(symbol);
			}
		}
		
		cash = curCash;
	}
	
	private double getFinalCash(String date_) {
		double curCash = cash;		
		for(String symbol : position.keySet()) {
			PositionEntry pe = position.get(symbol);
			double p = Stock.getClosePrice(symbol, date_);
			if(!AbstractTechIndicator.isValid(p))
				continue;
			curCash += (pe.getAmount() + pe.getInflightPos()) * p;
			curCash += pe.getInflightCash();			
		}		
		return curCash;
	}
}
