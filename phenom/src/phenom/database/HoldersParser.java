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

/**
* 个股十大流通股东数据
* Schema:
* date: 日期
* symbol: 股票代码.上市交易所，如600001.sh 000001.sz
* rank: 十大流通股东排名 1 - 10
* holder: 股东名字
* percentage: 控股比例
*/
public class HoldersParser {
	private static final String INSERT_STOCK_HOLDERS = "insert into STOCK_HOLDERS (date, symbol, rank, holder, percentage) " +
	 	"values (?,?,?,?,?)";
	
	private static final String CREATE_STOCK_HOLDERS = "CREATE TABLE STOCK_HOLDERS (date varchar(8), symbol varchar(10), rank int, holder varchar(256), percentage float)";
	
	private static final String DROP_STOCK_HOLDERS = "DROP TABLE IF EXISTS STOCK_HOLDERS";
	private static String _holdersFileName = "data/holders.csv";
	private static String _dbPath = "data/SuperT_STOCK.sqlite";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HoldersParser parser = new HoldersParser(); 
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
			prep = conn.prepareStatement(INSERT_STOCK_HOLDERS);		
			
			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(_holdersFileName)));
			String line = null;		
			int count = 0;
			while ((line = reader.readLine()) != null) {
				// skip the first header line
				if (count++ == 0)
					continue;
				System.out.println(line);
				String [] fields = line.split("\t");
				prep.setString(1, fields[1]); // date
				prep.setString(2, fields[0]); // symbol
				prep.setInt(3, Integer.parseInt(fields[2])); // rank
				prep.setString(4, fields[3]); // holder name
				prep.setDouble(5, Double.parseDouble(fields[4])); // percentage
				prep.addBatch();

				if (count % 500 == 0) {
					prep.executeBatch();
					conn.commit();
				}
			}
			prep.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
			return ;
		} catch (IOException e) {	
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
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
				s.executeUpdate(DROP_STOCK_HOLDERS);
				s.executeUpdate(CREATE_STOCK_HOLDERS);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
	}
}
