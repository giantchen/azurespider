package phenom.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.Map;
import java.util.TreeMap;
import java.util.SortedMap;

import phenom.database.ConnectionManager;
import phenom.stock.Stock;

public class WeightUtil {
	final static String SQL = "select * from (select Symbol, XDate from STOCK_BONUS union select Symbol, " +
			"XDate from STOCK_ALLOC) t where t.Symbol = ? and t.XDate >= '20000101'";
	//key = symbol value = {key = date, value = factor}
	static Map<String, SortedMap<String, Double>> weightFactors = new TreeMap<String, SortedMap<String, Double>>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

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
		SortedMap<String, Double> weights = weightFactors.get(s_.getSymbol());
		if(weights == null) {
			init(s_.getSymbol());
			weights = weightFactors.get(s_.getSymbol());
		}
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
	
	/**
	 * init symbol and keydates
	 */
	private synchronized static void init(String symbol_) {
		SortedMap<String, Double> weights = new TreeMap<String, Double>();
		weightFactors.put(symbol_, weights);		
		
		Connection conn = null;		
		PreparedStatement s = null;
		ResultSet rs = null;
		
		try {			
			conn = ConnectionManager.getConnection();
			System.out.println(SQL);
			
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
			
			System.out.println(weights);
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
}
