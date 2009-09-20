package phenom.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.HashMap;
import java.util.ArrayList;

import phenom.database.ConnectionManager;
import phenom.stock.Dividend;
import phenom.stock.PositionEntry;
import phenom.stock.Stock;

/**
 * 
 * 所有除权、工具方法都延迟加载
 * TODO：未来可考虑eager init，用多线程处理如 Latch
 *
 */
public class WeightUtil {
	final static String SQL = "select * from (select Symbol, XDate from STOCK_BONUS union select Symbol, " +
			"XDate from STOCK_ALLOC) t where t.Symbol = ? and t.XDate >= '20000101'";	
	final static String DIVSQL = "select * from STOCK_BONUS where Symbol = ?"; 
	
	//除权因子key = symbol value = {key = date, value = factor}
	static Map<String, SortedMap<String, Double>> weightFactors = new TreeMap<String, SortedMap<String, Double>>();
	//分红转增增发
	static Map<String, List<Dividend>> weights = new HashMap<String, List<Dividend>>();
			
	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		// TODO Auto-generated method stub

	}
	
	public static void applyDividend(PositionEntry pe_, String date_) {
		
	}
	
	public static String parseDate(String date_) {					
		int iMonth = Integer.parseInt(date_.substring(4, 6));
		int year = Integer.parseInt(date_.substring(0, 4));
		int day = Integer.parseInt(date_.substring(6));
		
		if(iMonth > 0 && iMonth <= 12) {
			return date_;
		}
		
		iMonth = iMonth % 12;
		
		if(iMonth == 0) {
			iMonth = 12;
		}
		
		String sMonth = String.valueOf(iMonth);
		String sDay = String.valueOf(day);
		
		if(sMonth.length() == 1) {
			sMonth = "0" + sMonth;
		}
		
		if(sDay.length() == 1) {
			sDay = "0" + sDay;
		}
		
		date_ = String.valueOf(year + 1) + sMonth + sDay;
		
		return date_;
	}
	
	public static int parseDate(int date_) {
		return Integer.parseInt(parseDate(String.valueOf(date_)));
	}
	
	public static void applyWeight(Stock s_) {
		if(!weightFactors.containsKey(s_.getSymbol())) {
			initFactor(s_.getSymbol());
		}
		SortedMap<String, Double> weights = weightFactors.get(s_.getSymbol());		
		double factor = 1.0;		
		
		for(String xDate : weights.keySet()) {
			if(s_.getDate().compareTo(xDate) < 0) {				
				factor = weights.get(xDate);
				break;
			}
		}
		
		s_.setWeightedClosePrice(s_.getClosePrice() * factor);
		s_.setWeightedOpenPrice(s_.getOpenPrice() * factor);
		s_.setWeightedHighPrice(s_.getHighPrice() * factor);
		s_.setWeightedLowPrice(s_.getLowPrice() * factor);		
	}
	
	public static Dividend applyDividend(String symbol_, String buyDate_) {
		if(!weights.containsKey(symbol_)) {
			initDividend(symbol_);
		}		
		List<Dividend> ds = weights.get(symbol_);
		Dividend di = null;
		if(ds.size() > 0) {
			for(Dividend d : ds) {
				if(buyDate_.compareTo(d.getXDate()) < 0) {
					di = d;
					break;
				}
			}
		}
		
		return di;
	}
	
	/**
	 * init symbol and keydates
	 */
	private synchronized static void initFactor(String symbol_) {
		SortedMap<String, Double> weights = new TreeMap<String, Double>();
		weightFactors.put(symbol_, weights);		
		
		Connection conn = null;		
		PreparedStatement s = null;
		ResultSet rs = null;
		
		try {			
			conn = ConnectionManager.getConnection();
			//System.out.println(SQL);
			
			//retrieve symbol and ex date
			s = conn.prepareStatement(SQL);
			s.setString(1, symbol_);
			rs = s.executeQuery();			
			while(rs.next()) {
				weights.put(rs.getString("XDate"), null);
			}			
			rs.close();
			//if weights exists
			if(weights.size() != 0) {
				Stock current = Stock.getStock(symbol_, weights.lastKey());
				/**
				 * 如果除权日那天没有日线数据的话，就会抛NULLPointerException。
				 * 比如说600369.sh，最后一次除权日期为20060720，但是20060720那天似乎是停牌，没有任何日线数据
				 */
				while(current == null) {
					current = Stock.getStock(symbol_, DateUtil.nextDay(weights.lastKey()));
				}
				//init factors
				for(String xDate : weights.keySet()) {
					Stock pStock = Stock.previousStock(symbol_, xDate);
					//weights.put(xDate, BigDecimal.valueOf(pStock.getWeight() / current.getWeight()).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
					weights.put(xDate, pStock.getWeight() / current.getWeight());
				}
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
	
	//初始化转增 除权 增发
	private static synchronized void initDividend(String symbol_) {
		List<Dividend> ws = new ArrayList<Dividend>();
		weights.put(symbol_, ws);
		
		Connection conn = null;		
		PreparedStatement s = null;
		ResultSet rs = null;
		
		try {			
			conn = ConnectionManager.getConnection();
			//System.out.println();
			
			s = conn.prepareStatement(DIVSQL);
			s.setString(1, symbol_);
			rs = s.executeQuery();			
			while(rs.next()) {
				Dividend d = new Dividend();
				d.set(rs);
				ws.add(d);
			}			
			rs.close();			
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
		
		Collections.sort(ws);
	}
}
