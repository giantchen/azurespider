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
 * 个股基本数据
 */
public class RiskFreeInterestRateParser {
	// 个股基本数据表
	private static final String CREATE_STOCK_RISKFREE_INTEREST = "CREATE TABLE 'STOCK_RISKFREE_INTEREST' (" +
			"Uid NUMERIC PRIMARY KEY  NOT NULL, " + // 物理主键
			"Date VARCHAR NOT NULL ," + 	
			"InterestRate DOUBLE" + 			// 无风险利率
			")";
	private static final String INSERT_STOCK_RISKFREE_INTEREST = "insert into STOCK_RISKFREE_INTEREST " +
		"(Uid, Date, InterestRate) "
			+ "values (?,?,?)";
	private static final String CREATE_INDEX_ON_STOCK_RISKFREE_INTEREST = "CREATE INDEX 'Date' ON "
			+ "'STOCK_RISKFREE_INTEREST' ('Date' ASC)";

	private static final String DROP_STOCK_RISKFREE_INTEREST = "DROP TABLE IF EXISTS STOCK_RISKFREE_INTEREST";
	private static String _interestFile = "data/interestrate.csv";

	public static void main(String[] args) {
		RiskFreeInterestRateParser parser = new RiskFreeInterestRateParser();
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
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);

			Statement s = conn.createStatement();
			ResultSet rs = s
					.executeQuery("select max(Uid) id from STOCK_RISKFREE_INTEREST");
			int uid = rs.getInt("id");
			rs.close();
			prep = conn.prepareStatement(INSERT_STOCK_RISKFREE_INTEREST);

			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(_interestFile)));
			String line = null;
			int count = 0;
			double rate = 0;
			
			while ((line = reader.readLine()) != null) {
				// skip the first header line
				if (count++ == 0)
					continue;
				System.out.println(line);
				String[] fields = line.split(",");
				prep.setInt(1, ++uid);
				prep.setString(2, fields[0].replaceAll("/", "")); // Date
				if (fields.length >= 2) {
					BigDecimal v = new BigDecimal(Double.parseDouble(fields[1]) / 100);	
				
					if (v.doubleValue() != 0)
						rate = v.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
				}
				
				prep.setDouble(3, rate);
				prep.addBatch();

				if (count % 5000 == 0) {
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

		Connection conn;
		try {
			conn = ConnectionManager.getConnection();
			Statement s = conn.createStatement();
			s.executeUpdate(DROP_STOCK_RISKFREE_INTEREST);
			s.executeUpdate(CREATE_STOCK_RISKFREE_INTEREST);
			s.execute(CREATE_INDEX_ON_STOCK_RISKFREE_INTEREST);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}
}
