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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import phenom.database.ConnectionManager;
import phenom.stock.Dividend;
import phenom.stock.Stock;

/**
 * 
 * 所有除权、工具方法都延迟加载：未来可考虑eager init，用多线程处理如 Latch
 * 
 */
public class DividendUtil {
	public enum DateType {
		REG_DATE, XDATE, LIST_DATE;
	}

	private final static Lock lock = new ReentrantLock();

	//分红除权等数据
	final static String DIVSQL = "select * from STOCK_BONUS where Symbol = ? and AnnounceDate != '' "
			+ "and (ListDate != '' or (ListDate = '' and Dividend != 0))";
	
	//配股
	final static String ALLOC_SQL = "select * from STOCK_ALLOC where Symbol = ? and AnnounceDate != ''";
		
	// 分红转增增发
	static Map<String, List<Dividend>> weights = new HashMap<String, List<Dividend>>();
	static Map<String, List<Dividend>> allocWeights = new HashMap<String, List<Dividend>>();

	public static Dividend getEntitlement(String symbol, String date, DateType dt) {
		Dividend dv = null;
		if (!weights.containsKey(symbol)) {
			initDividend(symbol, false);
		}
		List<Dividend> ds = weights.get(symbol);
		if (ds.size() > 0) {
			for (Dividend d : ds) {
				if (dt == DateType.LIST_DATE && date.equals(d.getListDate())) {
					dv = d;
					break;
				} else if (dt == DateType.XDATE && date.equals(d.getXDate())) {
					dv = d;
					break;
				} else if (dt == DateType.REG_DATE && date.equals(d.getRegDate())) {
					dv = d;
					break;
				}
			}
		}
		return dv;
	}
	
	public static Dividend getAllocEntitlement(String symbol, String date, DateType dt) {
		Dividend dv = null;
		if (!allocWeights.containsKey(symbol)) {
			initDividend(symbol, true);
		}
		List<Dividend> ds = allocWeights.get(symbol);
		if (ds.size() > 0) {
			for (Dividend d : ds) {
				if (dt == DateType.LIST_DATE && date.equals(d.getListDate())) {
					dv = d;
					break;
				} else if (dt == DateType.XDATE && date.equals(d.getXDate())) {
					dv = d;
					break;
				} else if (dt == DateType.REG_DATE && date.equals(d.getRegDate())) {
					dv = d;
					break;
				}
			}
		}
		return dv;
	} 

	public static void applyWeight(Stock s) {
		s.setWeightedClosePrice(s.getClosePrice() * s.getWeight());
		s.setWeightedOpenPrice(s.getOpenPrice() * s.getWeight());
		s.setWeightedHighPrice(s.getHighPrice() * s.getWeight());
		s.setWeightedLowPrice(s.getLowPrice() * s.getWeight());
	}

	public static Dividend applyDividend(String symbol, String buyDate) {
		if (!weights.containsKey(symbol)) {
			initDividend(symbol, false);
		}
		List<Dividend> ds = weights.get(symbol);
		Dividend di = null;
		if (ds.size() > 0) {
			for (Dividend d : ds) {
				if (buyDate.compareTo(d.getXDate()) < 0) {
					di = d;
					break;
				}
			}
		}

		return di;
	}

	// 初始化转增 除权 增发, alloc means 配股
	private static void initDividend(String symbol, boolean alloc) {
		lock.lock();

		List<Dividend> ws = new ArrayList<Dividend>();
		if(!alloc) {
			weights.put(symbol, ws);
		} else {
			allocWeights.put(symbol, ws);
		}
		
		Connection conn = null;
		PreparedStatement s = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManager.getConnection();			
			s = alloc ? conn.prepareStatement(ALLOC_SQL) : conn.prepareStatement(DIVSQL);
			s.setString(1, symbol);
			rs = s.executeQuery();
			while (rs.next()) {
				Dividend d = new Dividend();
				if(!alloc) {
					d.set(rs);
				} else {
					d.setAlloc(rs);
				}
				ws.add(d);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
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
		
		lock.unlock();
		Collections.sort(ws);
	}
}
