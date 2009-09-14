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
* 日线数据
*/
public class IndexParser {
	private static final String DROP_STOCK_INDEX = "DROP TABLE IF EXISTS STOCK_INDEX";	
	// 日线数据表
	private static final String CREATE_STOCK_INDEX = "CREATE TABLE STOCK_INDEX (" +
		"Uid NUMERIC PRIMARY KEY NOT NULL ," +		// 物理主键
		"IndexId NUMERIC NOT NULL ," +              // 指数代码
		"IndexName VARCHAR NOT NULL ," +            // 指数名称
		"Symbol VARCHAR NOT NULL ," +				// 股票代码.上市交易所，如600001.sh 000001.sz
		"Exchange VARCHAR" +						// 交易所 sh 或者 sz， 或者hk
		")";
	private static final String INSERT_STOCK_INDEX = "insert into STOCK_INDEX (Uid, IndexId, IndexName, Symbol, Exchange) "
		+ "values (?,?,?,?,?)";
	private static final String CREATE_INDEX_ON_STOCK_INDEX = "CREATE INDEX IDX_INDEX ON STOCK_INDEX(Symbol ASC, IndexName)";
	
	private static String _indexFile = "data/index.csv";
	private static String _dbPath = "data/SuperT_STOCK.sqlite";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IndexParser parser = new IndexParser(); 
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
			String scon = "jdbc:sqlite:" + _dbPath;
			conn = DriverManager.getConnection(scon);
			conn.setAutoCommit(false);			
			Statement s = conn.createStatement();			
			ResultSet rs = s.executeQuery("select max(Uid) Uid from STOCK_INDEX");
			int uid = rs.getInt("Uid");
			rs.close();
			
			// populate day table
			prep = conn.prepareStatement(INSERT_STOCK_INDEX);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(_indexFile)));
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;
				
				System.out.println(line);
				String [] fields = line.split(",");
				if (fields.length < 4)
					continue;
				
				prep.setInt(1, ++uid);
				prep.setString(2, fields[0]);												// IndexId
				prep.setString(3, fields[1]);												// IndexName
				prep.setString(4, fields[2]);												// Symbol
				prep.setString(5, fields[3]);												// Exchange
				prep.addBatch();
				
				if (uid % 5000 == 0) {
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
			
			String scon = "jdbc:sqlite:" + _dbPath;
			Connection conn;
			try {
				conn = DriverManager.getConnection(scon);
				Statement s = conn.createStatement();
				s.executeUpdate(DROP_STOCK_INDEX);
				s.executeUpdate(CREATE_STOCK_INDEX);
				s.execute(CREATE_INDEX_ON_STOCK_INDEX);

			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
	}
}
