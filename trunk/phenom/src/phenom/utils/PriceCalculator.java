package phenom.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

import java.math.BigDecimal;

import phenom.stock.Weight;
import phenom.stock.Stock;
import phenom.database.ConnectionManager;

public class PriceCalculator {

	private static String weightSql = "select distinct Symbol from STOCK_WEIGHT where Date > '20000100' order by Symbol ";
	private static List<String> _allStockSymbols = new ArrayList<String>();
	private static String _dbPath = "E:\\fei\\SQLite\\SuperT_STOCK.sqlite";
	
	//List<BigDecimal> factors = new ArrayList<BigDecimal> ();
	static Map<String, List<BigDecimal>> weightFactors = new TreeMap<String, List<BigDecimal>>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initSymbol(_dbPath);		
		
		calculate("600588.sh");
		calculate("601899.sh");
		calculate("600258.sh");
		calculate("600257.sh");
		calculate("600259.sh");
		calculate("000875.sz");
		calculate("000957.sz");
		calculate("000977.sz");
		
		for(String symbol : weightFactors.keySet()) {
			List<BigDecimal> ls = weightFactors.get(symbol);
			StringBuilder sb = new StringBuilder();
			sb.append("symbol = ").append(symbol).append(" | ");
			
			for(BigDecimal b : ls) {
				sb.append(b).append(" | ");
			}
							
			System.out.println(sb.toString());
		}
	}
	
	public static void initSymbol(String dbPath_) {
		Connection conn = null;		
		Statement s = null;
		ResultSet rs = null;
		
		try {
			Class.forName("org.sqlite.JDBC");
			String scon = "jdbc:sqlite:" + dbPath_;
			conn = DriverManager.getConnection(scon);			
			s = conn.createStatement();		
			rs = s.executeQuery(weightSql);
			
			while(rs.next()) {
				_allStockSymbols.add(rs.getString("Symbol"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
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
	
	/**
	 * 除权除息价＝(股权登记日的收盘价－每股所分红利现金额＋配股价×每股配股数)÷(1＋每股送红股数＋每股配股数 + 每股转增股数)
	 * @param symbol_
	 */
	public static void calculate(String symbol_) {
		Map<String, Map<String, Weight>> weights = getWeight(symbol_, null);
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
				
				//System.out.println("------ Process " + ws.get(date).getDate() + "-------------");
				w = weights.get(symbol_).get(date);				
				s = getStock(symbol_, date);
				/*System.out.println("w = " + w);
				System.out.println("s = " + s);*/
				//除权除息价＝(股权登记日的收盘价－每股所分红利现金额＋配股价×每股配股数)÷(1＋每股送红股数＋每股配股数 + 每股转增股数)	
				
				double weightedPrice = (s.getClosePrice() - w.getBonus()/10 + w.getPrice() * w.getAmount() / 10)
					 / (1 + w.getGift()/10 + w.getAmount()/10 + w.getTrans()/10);
				double factor = s.getClosePrice() / weightedPrice;
				BigDecimal bFactor = BigDecimal.valueOf(factor);
				
				if(factors.size() == 0) {
					bFactor = bFactor.setScale(3, BigDecimal.ROUND_HALF_UP);
				} else {
					bFactor = bFactor.multiply(factors.get(count - 3));
					bFactor = bFactor.setScale(3, BigDecimal.ROUND_HALF_UP);
				}			
				
				factors.add(bFactor);
				
				//bFactor.setScale(4);
				/*System.out.println("Weighted Price = " + weightedPrice + " | factor=" + bFactor + " sinaFactor = " );
				System.out.println();*/
			}
		}	
		
		weightFactors.put(symbol_, factors);
	}
	
	/**
	 * 
	 * @param symbol_
	 * @param date_
	 * @return Map<key = 'Symbol', <key = 'Date', value = Weight>>
	 */
	private static Map<String, Map<String, Weight>> getWeight(String symbol_, String date_) {
		Connection conn = null;
		ResultSet rs = null;
		String sql = "select * from STOCK_WEIGHT where Symbol = '" + symbol_ + "'";		
		
		Map<String, Map<String, Weight>> weights = new HashMap<String, Map<String, Weight>> ();
		try {
			conn = ConnectionManager.getConnection(_dbPath);
			rs = conn.createStatement().executeQuery(sql);
			
			while(rs.next()) {
				String symbol = rs.getString("Symbol");
				Map<String, Weight> s_weights = weights.get(symbol);
				
				if(s_weights == null) {
					s_weights = new TreeMap<String, Weight>();
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
	
	public static Stock getStock(String symbol_, String date_) {
		Stock s = null;
		Connection conn = null;
		ResultSet rs = null;
		String subSql = "(select max(Date) from STOCK_PRICE where Symbol = '" + symbol_ + "' and Date < '" + date_ + "')";
		String sql = "select * from STOCK_PRICE where Symbol = '" + symbol_ + "' and Date = " + subSql;		
		
		try {
			conn = ConnectionManager.getConnection(_dbPath);
			rs = conn.createStatement().executeQuery(sql);
			
			while(rs.next()) {
				s = new Stock();
				s.set(rs);
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
		
		return s;
	}
}