package phenom.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.Collections;
import java.util.Map;
import java.util.List;
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
	final static String DIVSQL = "select * from STOCK_BONUS where Symbol = ? and AnnounceDate != '' " +
			"and (ListDate != '' or (ListDate = '' and Dividend != 0))"; 
	
	//分红转增增发
	static Map<String, List<Dividend>> weights = new HashMap<String, List<Dividend>>();
	
	public static Dividend getEntitledDividend(PositionEntry pe_, String date_) {
		Dividend dv = null;
		if(!weights.containsKey(pe_.getSymbol())) {
			initDividend(pe_.getSymbol());
		}		
		List<Dividend> ds = weights.get(pe_.getSymbol());		
		if(ds.size() > 0) {
			for(Dividend d : ds) {
				if(date_.equals(d.getRegDate())){					
					dv = d;
				}
			}
		}
		return dv;
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
		/*if(!weightFactors.containsKey(s_.getSymbol())) {
			initFactor(s_.getSymbol());
		}
		SortedMap<String, Double> weights = weightFactors.get(s_.getSymbol());		
		double factor = 1.0;		
		
		for(String xDate : weights.keySet()) {
			if(s_.getDate().compareTo(xDate) < 0) {				
				factor = weights.get(xDate);
				break;
			}
		}*/
		
		s_.setWeightedClosePrice(s_.getClosePrice() * s_.getWeight());
		s_.setWeightedOpenPrice(s_.getOpenPrice() * s_.getWeight());
		s_.setWeightedHighPrice(s_.getHighPrice() * s_.getWeight());
		s_.setWeightedLowPrice(s_.getLowPrice() * s_.getWeight());		
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
