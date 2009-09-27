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
 * 个股基本数据
 */
public class FinanceParser {
	// 个股基本数据表
	private static final String CREATE_STOCK_FINANCE = "CREATE TABLE 'STOCK_FINANCE' (" +
			"Uid NUMERIC PRIMARY KEY  NOT NULL, " + // 物理主键
			"Symbol VARCHAR NOT NULL ," + 	// 股票代码.上市交易所，如600001.sh 000001.sz
			"DueDate VARCHAR, " + 			// 截止日期
			"AnnounceDate VARCHAR, " + 		// 公布日期
			"NetAssetsPerShare DOUBLE, " +	// 每股净资产
			"EarningPerShare DOUBLE, " +	// 每股收益
			"CashPerShare DOUBLE, " +		// 每股现金含量
			"CapitalReservePerShare DOUBLE, " +		// 每股资本公积金
			"FixedAssets DOUBLE, " +		// 固定资产
			"CurrentAssets DOUBLE, " +		// 流动资产
			"TotalAssets DOUBLE, " +		// 总资产
			"LongTermDebt DOUBLE, " +		// 长期负债
			"PrimeRevenue DOUBLE, " +		// 主营业务收入
			"FinancingExpense DOUBLE, " +	// 财务费用
			"NetProfit DOUBLE" + 			// 净利润
			")";
	private static final String INSERT_STOCK_FINANCE = "insert into STOCK_FINANCE " +
		"(Uid, Symbol, DueDate, AnnounceDate, NetAssetsPerShare, EarningPerShare, CashPerShare, " +
		"CapitalReservePerShare, FixedAssets, CurrentAssets, TotalAssets, LongTermDebt, PrimeRevenue, " +
		"FinancingExpense, NetProfit) "
			+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String CREATE_INDEX_ON_STOCK_FINANCE = "CREATE INDEX 'Symbol_DueDate' ON "
			+ "'STOCK_FINANCE' ('Symbol' ASC, 'DueDate' ASC)";

	private static final String DROP_STOCK_FINANCE = "DROP TABLE IF EXISTS STOCK_FINANCE";
	private static String _financeFile = "data/sina_finance.csv";
	private static String _dbPath = "data/SuperT_STOCK.sqlite";

	public static void main(String[] args) {
		FinanceParser parser = new FinanceParser();
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
					.executeQuery("select max(Uid) id from STOCK_FINANCE");
			int uid = rs.getInt("id");
			rs.close();
			prep = conn.prepareStatement(INSERT_STOCK_FINANCE);

			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(_financeFile)));
			String line = null;
			int count = 0;
			while ((line = reader.readLine()) != null) {
				// skip the first header line
				if (count++ == 0)
					continue;
				System.out.println(line);
				String[] fields = line.split(",");
				prep.setInt(1, ++uid);
				prep.setString(2, fields[0]); // symbol
				prep.setString(3, fields[1].replaceAll("-", "")); 	//DueDate 
				prep.setString(4, fields[2].replaceAll("-", "")); 	//AnnounceDate
				prep.setDouble(5, Double.parseDouble(fields[3])); 	//NetAssetsPerShare
				prep.setDouble(6, Double.parseDouble(fields[4])); 	//EarningPerShare
				prep.setDouble(7, Double.parseDouble(fields[5])); 	//CashPerShare
				prep.setDouble(8, Double.parseDouble(fields[6])); 	//CaptialReservePerShare
				prep.setDouble(9, Double.parseDouble(fields[7])); 	//FixedAssets
				prep.setDouble(10, Double.parseDouble(fields[8])); 	//CurrentAssets
				prep.setDouble(11, Double.parseDouble(fields[9])); 	//TotalAssets
				prep.setDouble(12, Double.parseDouble(fields[10])); //LongTermDebt
				prep.setDouble(13, Double.parseDouble(fields[11])); //PrimeRevernue
				prep.setDouble(14, Double.parseDouble(fields[12])); //FinancingExpense
				prep.setDouble(15, Double.parseDouble(fields[13])); //NetProfit
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
			s.executeUpdate(DROP_STOCK_FINANCE);
			s.executeUpdate(CREATE_STOCK_FINANCE);
			s.execute(CREATE_INDEX_ON_STOCK_FINANCE);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}
}
