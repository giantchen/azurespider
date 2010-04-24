package phenom.database;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
* 日线数据
*/
public class DayParser {
	private static final String DROP_STOCK_PRICE = "DROP TABLE IF EXISTS STOCK_PRICE";	
	// 日线数据表
	private static final String CREATE_STOCK_PRICE = "CREATE TABLE STOCK_PRICE (" +
		"Uid NUMERIC PRIMARY KEY NOT NULL ," +		// 物理主键
		"Symbol VARCHAR NOT NULL ," +				// 股票代码.上市交易所，如600001.sh 000001.sz
		"Date VARCHAR NOT NULL ," +					// 日期 格式20090722
		"Open DOUBLE NOT NULL ," +					// 开盘价格 如 12.34
		"High DOUBLE NOT NULL ," +					// 最高价格 如 15.31
		"Low DOUBLE NOT NULL ," +					// 最低价 如10.15
		"Close DOUBLE NOT NULL ," +					// 收盘价 如 13.55
		"Amount DOUBLE NOT NULL ," +				// 成交量
		"Volume NUMERIC NOT NULL , " +				// 成交额 !! 数据为空 !!
		"Exchange VARCHAR, " +						// 交易所 sh 或者 sz
		"Weight DOUBLE NOT NULL, " +					// 复权因子
		"Return DOUBLE NOT NULL" +                  // 日回报率
		")";
	private static final String INSERT_STOCK_PRICE = "insert into STOCK_PRICE (Uid, Symbol, Date, "
		+ "Open, High, Low, Close, Amount,Volume, Exchange, Weight, Return)"
		+ "values (?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String CREATE_INDEX_ON_STOCK_PRICE = "CREATE INDEX IDX_PRICE ON STOCK_PRICE(Symbol ASC, Date ASC, Exchange ASC)";
	
	private static String _historyFile = "data/history.csv";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DayParser parser = new DayParser(); 
		parser.prepareTable();
		parser.populateTable();
	}
	
	public void populateTable() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return ;
		}
		
		PreparedStatement prep = null;
		Connection conn = null;
		
		try {
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);			
			Statement s = conn.createStatement();			
			ResultSet rs = s.executeQuery("select max(Uid) Uid from STOCK_PRICE");
			int uid = rs.getInt("Uid");
			rs.close();
			
			// populate day table
			prep = conn.prepareStatement(INSERT_STOCK_PRICE);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(_historyFile)));
			String line = null;
			double prevPrice = 0;
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;
				
				System.out.println(line);
				String [] fields = line.split(",");
				if (fields.length < 9)
					continue;
				
				double weight = Double.parseDouble(fields[8]);
				
				prep.setInt(1, ++uid);
				prep.setString(2, fields[0]);												// symbol
				prep.setString(3, fields[1]);												// Date
				
				BigDecimal b = new BigDecimal(Double.parseDouble(fields[2]) / weight); 
				prep.setDouble(4, b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());	// Open

				b = new BigDecimal(Double.parseDouble(fields[3]) / weight);
				prep.setDouble(5, b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());	// High
				
				b = new BigDecimal(Double.parseDouble(fields[5]) / weight);
				prep.setDouble(6, b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());	// Low
				
				b = new BigDecimal(Double.parseDouble(fields[4]) / weight);
				double close = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				prep.setDouble(7, close);	// Close
				
				prep.setDouble(8, Double.parseDouble(fields[6]));							// Amount								// Amount = 0 
				prep.setDouble(9, Double.parseDouble(fields[7]));							// Volume
				prep.setString(10, fields[0].substring(fields[0].length() - 2));			// Exchange
				prep.setDouble(11, weight);	// Weight
				if (prevPrice == 0)
					prep.setDouble(12, 0); // Return
				else {
					b = new BigDecimal(Math.log(close / prevPrice));
					prep.setDouble(12, b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
				}
				prevPrice = close;
				prep.addBatch();
				
				if (uid % 500000 == 0) {
					prep.executeBatch();
					conn.commit();
				}
			}
			prep.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();	
			return ;
		} catch (IOException e) {	
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	private void prepareTable() {
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			Connection conn;
			try {
				conn = ConnectionManager.getConnection();
				Statement s = conn.createStatement();
				s.executeUpdate(DROP_STOCK_PRICE);
				s.executeUpdate(CREATE_STOCK_PRICE);
				s.execute(CREATE_INDEX_ON_STOCK_PRICE);

			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
	}
}
