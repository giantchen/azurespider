package phenom.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.List;
import java.util.LinkedList;

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
		Map<String, Double> weights = weightFactors.get(s_.getSymbol());
		if(weights == null) {
			init(s_.getSymbol());
			weights = weightFactors.get(s_.getSymbol());
		}
		
		for(String xDate : weights.keySet()) {
			if(s_.getDate().compareTo(xDate) <= 0) {
				double factor = weights.get(xDate);
				s_.setClosePrice(factor * s_.getClosePrice());
				s_.setOpenPrice(factor * s_.getOpenPrice());
				s_.setHighPrice(factor * s_.getHighPrice());
				s_.setLowPrice(factor = s_.getLowPrice());
			}
		}
	}
	
	/**
	 * init symbol and keydates
	 */
	private synchronized static void init(String symbol_) {
		SortedMap<String, Double> weights = new TreeMap<String, Double>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {				
				return o2.compareTo(o1);
			}								
		});
		weightFactors.put(symbol_, weights);
		List<Double> tmpWeights = new LinkedList<Double>();
		
		Connection conn = null;		
		PreparedStatement s = null;
		ResultSet rs = null;
		
		try {			
			conn = ConnectionManager.getConnection();
			
			//retrieve symbol and ex date
			s = conn.prepareStatement(SQL);
			s.setString(1, symbol_);
			rs = s.executeQuery();			
			while(rs.next()) {
				weights.put(rs.getString("XDate"), null);
			}			
			rs.close();
			
			int index = 0;
			//init factors
			for(String xDate : weights.keySet()) {				
				index++;
				if(tmpWeights.size() == 0) {
					tmpWeights.add(1.0);
				} else {
					double pw = tmpWeights.get(index - 1);
					Stock stock = Stock.getStock(symbol_, xDate);
					Stock pStock = Stock.previousStock(symbol_, xDate);
					tmpWeights.add(tmpWeights.get(0) * stock.getWeight() / pStock.getWeight());
				}
				
				weights.put(xDate, tmpWeights.get(0));
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
}
