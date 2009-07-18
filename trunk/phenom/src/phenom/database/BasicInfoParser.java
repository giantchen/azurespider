package phenom.database;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
* 个股基本数据
* Schema:
* symbol: 股票代码.上市交易所，如600001.sh 000001.sz
* name: 股票名，中文
* type: 类型，英文，用于区分股票，指数，lof，etf及其他，暂时为空
* industryid: 股票行业代号，非股票的设为0
* 
* 行业数据
* Schema:
* industryid: 行业代号
* desc: 中文描述
*/
public class BasicInfoParser {
	private static final String INSERT_STOCK_INFO = "insert into STOCK_INFO (symbol, name, type, industryid) " +
	 	"values (?,?,?,?)";
	private static final String INSERT_INDUSTRY_INFO = "insert into INDUSTRY_INFO (industryid, desc) " +
 	"values (?,?)";
	
	private static final String CREATE_INDUSTRY_INFO = "CREATE TABLE INDUSTRY_INFO (industryid int, desc varchar(256))";
	private static final String CREATE_STOCK_INFO = "CREATE TABLE STOCK_INFO (symbol varchar(10), name varchar(256), type varchar(20), industryid int)";
	private static final String DROP_STOCK_INFO = "DROP TABLE IF EXISTS STOCK_INFO";
	private static final String DROP_INDUSTRY_INFO = "DROP TABLE IF EXISTS INDUSTRY_INFO";
	private static String _industryFile = "data/hybk.ini";
	private static String _basicInfoFile = "data/basic.csv";
	private static String _dbPath = "data/SuperT_STOCK.sqlite";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicInfoParser parser = new BasicInfoParser(); 
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
			
			
			// populate industry info table
			prep = conn.prepareStatement(INSERT_INDUSTRY_INFO);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(_industryFile)));
			String line = null;		
			int bkno = 50200;
			Map<String, Integer> indMap = new HashMap<String, Integer>();
			Map<String, Integer> stockIndustryMap = new HashMap<String, Integer>();
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;
				
				System.out.println(line);
				if (line.startsWith("[")) {
					String desc = line.replaceAll("[\\[|\\]]", "");
					indMap.put(desc, ++bkno);
					prep.setInt(1, bkno); // industryid
					prep.setString(2, desc); // industry description
					prep.addBatch();
					continue;
				}
								
				String [] fields = line.split(",");
				stockIndustryMap.put(Integer.parseInt(fields[0]) == 0 ? fields[1] + ".sh" : fields[1] + ".sz", bkno);
			}
			prep.executeBatch();
			conn.commit();
			
			// Processing stock basic information
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(_basicInfoFile)));
			prep = conn.prepareStatement(INSERT_STOCK_INFO);
			while ((line = reader.readLine()) != null) {
				String [] fields = line.split("\t");
				prep.setString(1, fields[0]); // symbol
				prep.setString(2, fields.length > 1 ? fields[1] : ""); // name
				prep.setString(3, "");	// type
				prep.setInt(4, stockIndustryMap.get(fields[0]) == null ? 0 : stockIndustryMap.get(fields[0]));
				prep.addBatch();
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
				s.executeUpdate(DROP_INDUSTRY_INFO);
				s.executeUpdate(CREATE_INDUSTRY_INFO);
				s.executeUpdate(DROP_STOCK_INFO);
				s.executeUpdate(CREATE_STOCK_INFO);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
	}
}
