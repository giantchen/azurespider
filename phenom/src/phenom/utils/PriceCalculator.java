package phenom.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;

import java.math.BigDecimal;

import phenom.stock.Weight;
import phenom.stock.Stock;
import phenom.database.ConnectionManager;

/**
 * 
 * Only support the weight after year 2000 as we don't have the stock price before 2000
 * 
 * As a result we should use 向前除权
 */
public class PriceCalculator {	
	//the exists clause is to exclude the warrant as STOCK_WEIGHT including the warrant information
	private static String weightSql = "select distinct Symbol from STOCK_WEIGHT t1 where Date >= '20000101' " +
			" and exists(select 1 from STOCK_PRICE t2 where t1.Symbol = t2.Symbol) order by Symbol ";
	private static List<String> _allStockSymbols = new ArrayList<String>();	
	
	//key = symbol value = {key = date, value = factor}
	static Map<String, Map<String, BigDecimal>> weightFactors1 = new TreeMap<String, Map<String, BigDecimal>>();
	static Set<String> notInitilizedSymbols = new TreeSet<String>();
	
	//leave here for test purpose, in future can replace via JUnit and JMock
	static Map<String, List<BigDecimal>> weightFactors = new TreeMap<String, List<BigDecimal>>();
	
	public enum WeightType{
		FORWARD, BACKWARD
	}
	
	static {
		initSymbols();		
		initFactors(_allStockSymbols);	
	}
	
	public static Map<String, Map<String, BigDecimal>> getFactorsCache() {
		return weightFactors1;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		runTest();
		/*initSymbols();		
		initFactors(_allStockSymbols);	
		System.out.println("******" + notInitilizedSymbols);*/
	}
	
		
	public static void initSymbols() {
		Connection conn = null;		
		Statement s = null;
		ResultSet rs = null;
		
		try {			
			conn = ConnectionManager.getConnection();			
			s = conn.createStatement();		
			rs = s.executeQuery(weightSql);
			
			while(rs.next()) {
				_allStockSymbols.add(rs.getString("Symbol"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}  finally {
			if (conn != null) {
				try {
					s.close();					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void initFactors(List<String> symbols_) {		
		for(String symbol : symbols_) {
			try {
				getFactorBackward(symbol);
			} catch (Exception e) {
				e.printStackTrace();
				notInitilizedSymbols.add(symbol);			
			}
		}
	}
	
	/**
	 * 除权除息价＝(股权登记日的收盘价－每股所分红利现金额＋配股价×每股配股数)÷(1＋每股送红股数＋每股配股数 + 每股转增股数)
	 * 向前除权
	 */
	public static void getFactorBackward(String symbol_) {
		Map<String, Map<String, Weight>> weights = getWeight(symbol_, WeightType.BACKWARD);
		Map<String, BigDecimal> stockWeights = new TreeMap<String, BigDecimal>();
		
		Weight w;
		Stock s;
		List<BigDecimal> factors = new ArrayList<BigDecimal>();//only for test purpose		
		System.out.println("---------GetFactorBackward Stock = " + symbol_ + "-------------------");
		for(String k : weights.keySet()) {			
			Map<String, Weight> ws= weights.get(k);
			int count = 0;
			for(String date : ws.keySet()) {						
				count++;
				
				w = weights.get(symbol_).get(date);				
				s = Stock.getStock(symbol_, date);
				
				if(s == null) { //IPO date has no previous price
					continue;
				}
				
				//除权除息价＝(股权登记日的收盘价－每股所分红利现金额＋配股价×每股配股数)÷(1＋每股送红股数＋每股配股数 + 每股转增股数)				
				double weightedPrice = (s.getClosePrice() - w.getBonus()/10 + w.getPrice() * w.getAmount() / 10)
					 / (1 + w.getGift()/10 + w.getAmount()/10 + w.getTrans()/10);
				double factor = weightedPrice / s.getClosePrice();
				BigDecimal bFactor = BigDecimal.valueOf(factor);
				
				if(factors.size() == 0) {
					bFactor = bFactor.setScale(5, BigDecimal.ROUND_HALF_UP);
				} else {
					bFactor = bFactor.multiply(factors.get(count - 2));
					bFactor = bFactor.setScale(5, BigDecimal.ROUND_HALF_UP);
				}				
				
				stockWeights.put(date, bFactor);
				factors.add(bFactor);			
			}
		}		
		
		weightFactors1.put(symbol_, stockWeights);	
	}
	
	/**
	 * 除权除息价＝(股权登记日的收盘价－每股所分红利现金额＋配股价×每股配股数)÷(1＋每股送红股数＋每股配股数 + 每股转增股数)
	 * 向后除权
	 */
	public static void getFactorForward(String symbol_) {
		Map<String, Map<String, Weight>> weights = getWeight(symbol_, WeightType.FORWARD);	
		
		Weight w;
		Stock s;
		List<BigDecimal> factors = new ArrayList<BigDecimal> ();
		for(String k : weights.keySet()) {
			Map<String, Weight> ws= weights.get(k);
			int count = 0;
			for(String date : ws.keySet()) {
				if(count++ == 0) {
					continue; //skip the IPO date
				}
				
				w = weights.get(symbol_).get(date);				
				s = Stock.getStock(symbol_, date);
				
				//除权除息价＝(股权登记日的收盘价－每股所分红利现金额＋配股价×每股配股数)÷(1＋每股送红股数＋每股配股数 + 每股转增股数)				
				double weightedPrice = (s.getClosePrice() - w.getBonus()/10 + w.getPrice() * w.getAmount() / 10)
					 / (1 + w.getGift()/10 + w.getAmount()/10 + w.getTrans()/10);
				double factor = s.getClosePrice() / weightedPrice;
				BigDecimal bFactor = BigDecimal.valueOf(factor);
				
				if(factors.size() == 0) {
					bFactor = bFactor.setScale(4, BigDecimal.ROUND_HALF_UP);
				} else {
					bFactor = bFactor.multiply(factors.get(count - 3));
					bFactor = bFactor.setScale(4, BigDecimal.ROUND_HALF_UP);
				}			
				
				factors.add(bFactor);
			}
		}	
		
		weightFactors.put(symbol_, factors);	
	}
	
	/**
	 * 
	 * @param symbol_
	 * @param date_
	 * @return Map<key = 'Symbol', <key = 'Date', value = Weight>>
	 * 
	 */
	private static Map<String, Map<String, Weight>> getWeight(String symbol_, WeightType wt_) {
		Connection conn = null;
		ResultSet rs = null;
		String sql = "select * from STOCK_WEIGHT where Symbol = '" + symbol_ + "' and Date >= '20000101'";		
		
		Map<String, Map<String, Weight>> weights = new HashMap<String, Map<String, Weight>>();
		try {
			conn = ConnectionManager.getConnection();
			rs = conn.createStatement().executeQuery(sql);
			
			while(rs.next()) {
				String symbol = rs.getString("Symbol");
				Map<String, Weight> s_weights = weights.get(symbol);
				
				if(s_weights == null) {
					if(wt_.equals(WeightType.BACKWARD))
						s_weights = new TreeMap<String, Weight>(
							new Comparator<String>() {
								@Override
								public int compare(String o1, String o2) {
									// TODO Auto-generated method stub
									return o2.compareTo(o1);
								}								
							});
					else if(wt_.equals(WeightType.FORWARD)) {
						s_weights = new TreeMap<String, Weight>();
					}
					
					weights.put(symbol, s_weights);					
				}
				
				Weight w = new Weight();
				w.set(rs);
				
				s_weights.put(w.getDate(), w);
			}			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {						
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}		
		
		return weights;
	}
	
	public static void applyWeight(List<Stock> stocks_) {
		for(Stock s : stocks_) {
			applyWeight(s);
		}
	}
	
	public static void applyWeight(Stock stock_) {
		//key = symbol value = {key = date, value = factor}
		Map<String, BigDecimal> weights = weightFactors1.get(stock_.getSymbol());		
		for(String key : weights.keySet()) {
			if(stock_.getDate().compareTo(key) < 0) {
				double factor = weights.get(key).doubleValue();
				stock_.setClosePrice(stock_.getClosePrice() * factor);
				stock_.setOpenPrice(stock_.getOpenPrice() * factor);
				stock_.setHighPrice(stock_.getHighPrice() * factor);
				stock_.setLowPrice(stock_.getLowPrice() * factor);				
				break;
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void runTest() {
		initSymbols();			
	
		getFactorForward("600588.sh");
		getFactorForward("601899.sh");
		getFactorForward("600258.sh");
		getFactorForward("600257.sh");
		getFactorForward("600259.sh");
		getFactorForward("000875.sz");
		getFactorForward("000957.sz");
		getFactorForward("000977.sz");		
		getFactorForward("000070.sz");

		System.out.println("*******Test Forward**********");
		for(String symbol : weightFactors.keySet()) {
			List<BigDecimal> ls = weightFactors.get(symbol);
			StringBuilder sb = new StringBuilder();
			sb.append("symbol = ").append(symbol).append(" | ");
			
			for(BigDecimal b : ls) {
				sb.append("factor = ").append(b).append(" | ");						
			}
							
			System.out.println(sb.toString());
		}
		
		getFactorBackward("600518.sh");
		/*getFactorBackward("000977.sz");		
		getFactorBackward("000070.sz");*/
		
		System.out.println("**********Test Backward*******");
		for(String symbol : weightFactors1.keySet()) {
			Map<String, BigDecimal> ls = weightFactors1.get(symbol);
			StringBuilder sb = new StringBuilder();
			sb.append("symbol = ").append(symbol).append(" | ");
			
			for(String date : ls.keySet()) {
				sb.append("  date=").append(date).append("  factor=").append(ls.get(date));						
			}
							
			System.out.println(sb.toString());
		}
		
		//print out the not-initialized
		System.out.println("******" + notInitilizedSymbols);
	}
}