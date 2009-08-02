package phenom.database;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 个股十大流通股东数据
 */
public class BonusParser {
	private static final String INSERT_STOCK_BONUS = "insert into STOCK_BONUS (Uid, Symbol, " +
		"AnnounceDate, BonusShare, TransitShare, Dividend, TotalShare, XDate, RegDate, ListDate)" +
		" values (?,?,?,?,?,?,?,?,?,?)";
	private static final String INSERT_STOCK_ALLOC = "insert into STOCK_ALLOC (Uid, Symbol, " +
		"AnnounceDate, AllocShare, AllocPrice, TotalShare, XDate, RegDate, StartPayDate, EndPayDate, " +
		"ListDate, TotalAmount) " +
		"values (?,?,?,?,?,?,?,?,?,?,?,?)";

	// 红利或红股
	private static final String CREATE_STOCK_BONUS = "CREATE  TABLE STOCK_BONUS (" +
		"Uid NUMERIC PRIMARY KEY  NOT NULL, " +			// 物理主键
		"Symbol VARCHAR NOT NULL, " +					// 股票代码.上市交易所，如600001.sh 000001.sz
		"AnnounceDate VARCHAR, " +						// 公告日
		"BonusShare DOBULE, " + 						// 分红股数(每十股)
		"TransitShare DOBULE, " +						// 转增股数(每十股)
		"Dividend DOBULE, " + 							// 派息(每十股)
		"TotalShare DOBULE, " +							// 基准股本（单位万股)
		"XDate VARCHAR, " +								// 除权除息日
		"RegDate VARCHAR, " +							// 股权登记日
		"ListDate VARCHAR" +							// 红股上市日
		")";
	
	// 配股
	private static final String CREATE_STOCK_ALLOC = "CREATE TABLE STOCK_ALLOC (" +
		"Uid NUMERIC PRIMARY KEY  NOT NULL , " + 	// 物理主键
		"Symbol VARCHAR, " + 						// 股票代码.上市交易所，如600001.sh 000001.sz
		"AnnounceDate VARCHAR, " +					// 公告日
		"AllocShare DOBULE, " + 					// 配股数(每十股)
		"AllocPrice DOBULE, " + 					// 配股价
		"TotalShare DOBULE, " +						// 基准股本（单位万股)
		"XDate VARCHAR, " +							// 除权除息日
		"RegDate VARCHAR, " +						// 股权登记日
		"StartPayDate VARCHAR, " +					// 缴款起始日
		"EndPayDate VARCHAR, " +					// 缴款结束日
		"ListDate VARCHAR, " +						// 配股上市日
		"TotalAmount DOUBLE" + 						// 募集资金合计
		")";

	private static final String DROP_STOCK_BONUS = "DROP TABLE IF EXISTS STOCK_BONUS";
	private static final String DROP_STOCK_ALLOC = "DROP TABLE IF EXISTS STOCK_ALLOC";
	private static String _bonusFileName = "data/bonus.csv";
	private static String _allocFileName = "data/alloc.csv";
	private static String _dbPath = "data/SuperT_STOCK.sqlite";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BonusParser parser = new BonusParser();
		parser.prepareTable();
		parser.populateTable();
	}

	public void populateTable() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}

		PreparedStatement prep = null;
		Connection conn = null;
		
		// Processing bonus table
		try {
			String scon = "jdbc:sqlite:" + _dbPath;
			conn = DriverManager.getConnection(scon);
			conn.setAutoCommit(false);
			
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery("select max(Uid) Uid from STOCK_BONUS");
			int uid = rs.getInt("Uid");
			rs.close();
			
			prep = conn.prepareStatement(INSERT_STOCK_BONUS);
			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(_bonusFileName)));
			String line = null;
			int count = 0;
			while ((line = reader.readLine()) != null) {
				// skip the first header line
				if (count++ == 0)
					continue;
				System.out.println(line);
				line += " ,";
				String[] fields = line.split(",");
				// Uid, Symbol, AnnounceDate, BonusShare, TransitShare, Dividend, TotalShare, XDate, RegDate, ListDate
				prep.setInt(1, ++uid);
				prep.setString(2, fields[0]); 						// symbol
				prep.setString(3, fields[1]); 						// announce date
				prep.setDouble(4, fields[2].length() == 0 ? 0 : Double.parseDouble(fields[2])); 	// bonus share
				prep.setDouble(5, fields[3].length() == 0 ? 0 : Double.parseDouble(fields[3])); 	// transit share
				prep.setDouble(6, fields[4].length() == 0 ? 0 : Double.parseDouble(fields[4])); 	// Dividend
				prep.setDouble(7, fields[5].length() == 0 ? 0 : Double.parseDouble(fields[5])); 	// Total share
				prep.setString(8, fields[6]); 						// XDate
				prep.setString(9, fields[7]);						// RegDate
				prep.setString(10, fields[8]); 						// ListDate
				prep.addBatch();

				if (count % 500 == 0) {
					prep.executeBatch();
					conn.commit();
				}
			}
			prep.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
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
		
		
		// Processing alloc table
		try {
			String scon = "jdbc:sqlite:" + _dbPath;
			conn = DriverManager.getConnection(scon);
			conn.setAutoCommit(false);
			
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery("select max(Uid) Uid from STOCK_ALLOC");
			int uid = rs.getInt("Uid");
			rs.close();
			
			prep = conn.prepareStatement(INSERT_STOCK_ALLOC);
			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(_allocFileName)));
			String line = null;
			int count = 0;
			while ((line = reader.readLine()) != null) {
				// skip the first header line
				if (count++ == 0)
					continue;
				System.out.println(line);
				line += " ,";
				String[] fields = line.split(",");
				
				// Uid, Symbol, AnnounceDate, AllocShare, AllocPrice, TotalShare, XDate, RegDate, StartPayDate, EndPayDate, ListDate, TotalAmount
				prep.setInt(1, ++uid);
				prep.setString(2, fields[0]); 						// symbol
				prep.setString(3, fields[1]); 						// announce date
				prep.setDouble(4, fields[2].length() == 0 ? 0 : Double.parseDouble(fields[2])); 	// alloc share
				prep.setDouble(5, fields[3].length() == 0 ? 0 : Double.parseDouble(fields[3])); 	// alloc price
				prep.setDouble(6, fields[4].length() == 0 ? 0 : Double.parseDouble(fields[4])); 	// total share
				prep.setString(7, fields[5]); 						// XDate
				prep.setString(8, fields[6]);						// RegDate
				prep.setString(9, fields[7]); 						// StartPayDate
				prep.setString(10, fields[8]); 						// EndPayDate
				prep.setString(11, fields[9]); 						// ListDate
				prep.setDouble(12, fields[10].length() == 0 ? 0 : Double.parseDouble(fields[10])); // TotalAmount
				prep.addBatch();

				if (count % 500 == 0) {
					prep.executeBatch();
					conn.commit();
				}
			}
			prep.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
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

		String scon = "jdbc:sqlite:" + _dbPath;
		Connection conn;
		try {
			conn = DriverManager.getConnection(scon);
			Statement s = conn.createStatement();
			s.executeUpdate(DROP_STOCK_BONUS);
			s.executeUpdate(CREATE_STOCK_BONUS);
			s.executeUpdate(DROP_STOCK_ALLOC);
			s.executeUpdate(CREATE_STOCK_ALLOC);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}
}
