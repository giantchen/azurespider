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
 * ��������
 */
public class DayParser {
	private static final String DROP_STOCK_PRICE = "DROP TABLE IF EXISTS STOCK_PRICE";
	// �������ݱ�
	private static final String CREATE_STOCK_PRICE = "CREATE TABLE STOCK_PRICE ("
			+ "Uid NUMERIC PRIMARY KEY NOT NULL ," + // ��������
			"Symbol VARCHAR NOT NULL ," + // ��Ʊ����.���н���������600001.sh 000001.sz
			"Date VARCHAR NOT NULL ," + // ���� ��ʽ20090722
			"Open DOUBLE NOT NULL ," + // ���̼۸� �� 12.34
			"High DOUBLE NOT NULL ," + // ��߼۸� �� 15.31
			"Low DOUBLE NOT NULL ," + // ��ͼ� ��10.15
			"Close DOUBLE NOT NULL ," + // ���̼� �� 13.55
			"Amount DOUBLE NOT NULL ," + // �ɽ���
			"Volume NUMERIC NOT NULL , " + // �ɽ��� !! ����Ϊ�� !!
			"Exchange VARCHAR" + // ������ sh ���� sz
			")";
	private static final String INSERT_STOCK_PRICE = "insert into STOCK_PRICE (Uid, Symbol, Date, "
			+ "Open, High, Low, Close, Amount,Volume, Exchange)"
			+ "values (?,?,?,?,?,?,?,?,?,?)";
	private static final String CREATE_INDEX_ON_STOCK_PRICE = "CREATE INDEX IDX_PRICE ON STOCK_PRICE(Symbol)";

	private static String _historyFile = "data/history.csv";
	private static String _dbPath = "data/SuperT_STOCK.sqlite";

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
					.executeQuery("select max(Uid) id from STOCK_PRICE");
			int uid = rs.getInt("id");
			rs.close();

			// populate day table
			prep = conn.prepareStatement(INSERT_STOCK_PRICE);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(_historyFile)));
			String line = null;

			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;

				System.out.println(line);
				String[] fields = line.split(",");

				prep.setInt(1, ++uid);
				prep.setString(2, fields[0] + "." + fields[7]); // symbol
				prep.setString(3, fields[1]); // Date
				prep.setDouble(4, Double.parseDouble(fields[2])); // Open
				prep.setDouble(5, Double.parseDouble(fields[3])); // High
				prep.setDouble(6, Double.parseDouble(fields[4])); // Low
				prep.setDouble(7, Double.parseDouble(fields[5])); // Close
				prep.setDouble(8, 0); // Amount = 0
				prep.setLong(9, Long.parseLong(fields[6])); // Volume
				prep.setString(10, fields[7]); // Exchange
				prep.addBatch();
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
			s.executeUpdate(DROP_STOCK_PRICE);
			s.executeUpdate(CREATE_STOCK_PRICE);
			s.execute(CREATE_INDEX_ON_STOCK_PRICE);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}
}