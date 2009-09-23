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
public class HoldersParser {
	private static final String INSERT_STOCK_HOLDERS = "insert into STOCK_HOLDERS (Uid, Date, Symbol, Rank, Holder, Amount, Percentage) "
			+ "values (?,?,?,?,?,?,?)";

	private static final String CREATE_STOCK_HOLDERS = "CREATE TABLE STOCK_HOLDERS ("
			+ "Uid NUMERIC PRIMARY KEY  NOT NULL , " + // 物理主键
			"Date VARCHAR, " + // 日期
			"Symbol VARCHAR, " + // 股票代码.上市交易所，如600001.sh 000001.sz
			"Rank NUMERIC, " + // 十大流通股东排名 1 - 10
			"Holder VARCHAR, " + // 股东名字
			"Amount NUMERIC, " + // 控股量
			"Percentage DOUBLE" + // 控股比例
			")";

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
			return;
		}

		PreparedStatement prep = null;
		Connection conn = null;
		try {
			String scon = "jdbc:sqlite:" + _dbPath;
			conn = DriverManager.getConnection(scon);
			conn.setAutoCommit(false);

			Statement s = conn.createStatement();
			ResultSet rs = s
					.executeQuery("select max(Uid) id from INDUSTRY_INFO");
			int uid = rs.getInt("id");
			rs.close();
			prep = conn.prepareStatement(INSERT_STOCK_HOLDERS);

			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(_holdersFileName)));
			String line = null;
			int count = 0;
			while ((line = reader.readLine()) != null) {
				// skip the first header line
				if (count++ == 0)
					continue;
				System.out.println(line);
				String[] fields = line.split("\t");
				prep.setInt(1, ++uid);
				prep.setString(2, fields[1].replaceAll("-", "")); // date
				prep.setString(3, fields[0]); // symbol
				prep.setInt(4, Integer.parseInt(fields[2])); // rank
				prep.setString(5, fields[3]); // holder name
				prep.setInt(6, Integer.parseInt(fields[4])); // amount
				prep.setDouble(7, Double.parseDouble(fields[5])); // percentage
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
			s.executeUpdate(DROP_STOCK_HOLDERS);
			s.executeUpdate(CREATE_STOCK_HOLDERS);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}
}
