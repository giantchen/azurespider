package phenom.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * 钱龙数据库做了简单的mangle
 * 破解如下：
       月份如果大于12， 则年份进一位，月份为除以12的余数
       例如SELECT * FROM STOCK_WEIGHT where Symbol='600001' and Exchange = 'sh'
       其中有条20039408 实际应该为 20041008
 */
public class WeightValidator {	
	private static final String UPDATE_STOCK_WEIGHT = "update STOCK_WEIGHT set Date = ? where Uid = ?";	
	private static String _dbPath = "E:\\fei\\SQLite\\SuperT_STOCK.sqlite";	
	
	private static final String STOCK_WEIGHT_RECORDS = "SELECT Uid, Symbol, Date, Gift, Amount, " +
			" Price, Bonus, Trans, Total_Share, Liquid_Share, Exchange from STOCK_WEIGHT_Bak where substr(Date, 5, 2) > '12'";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		correctWeightData();
	}

	private static void correctWeightData() {
		Map<Integer, String> to_be_update = new HashMap<Integer, String>(10000);
		
		Connection conn = null;
		Connection conn1 = null;
		Statement s = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String scon = "jdbc:sqlite:" + _dbPath;
			conn = DriverManager.getConnection(scon);
			conn1 = DriverManager.getConnection(scon);
			
			s = conn.createStatement();
			ps = conn1.prepareStatement(UPDATE_STOCK_WEIGHT);			
			
			rs = s.executeQuery(STOCK_WEIGHT_RECORDS);
			conn1.setAutoCommit(false);
			int count = 0;
			while(rs.next()) {
				String date = rs.getString("Date");				
				int iMonth = Integer.parseInt(date.substring(4, 6));
				int year = Integer.parseInt(date.substring(0, 4));
				int day = Integer.parseInt(date.substring(6));
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
				
				date = String.valueOf(year + 1) + sMonth + sDay;
				
				to_be_update.put(rs.getInt("Uid"), date);				
			}
			
			Iterator<Integer> it = to_be_update.keySet().iterator();
			
			while(it.hasNext()) {
				Integer uid = it.next();
				ps.setString(1, to_be_update.get(uid));
				ps.setInt(2, uid.intValue());
				ps.addBatch();			
				count++;
				
				System.out.println("Id = " + uid + " || date = " + to_be_update.get(uid));
				
				if (count % 500 == 0) {
					ps.executeBatch();
					conn1.commit();
				}
			}
			
			if (count % 500 != 0) {
				ps.executeBatch();
				conn1.commit();
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
			
			if (conn1 != null) {
				try {
					ps.close();					
					conn1.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
