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
 * ������ɺͷֺ��
 */
public class BonusParser {
	private static final String INSERT_STOCK_BONUS = "insert into STOCK_BONUS (Uid, Symbol, " +
		"AnnounceDate, BonusShare, TransitShare, Dividend, TotalShare, XDate, RegDate, ListDate)" +
		" values (?,?,?,?,?,?,?,?,?,?)";
	private static final String INSERT_STOCK_ALLOC = "insert into STOCK_ALLOC (Uid, Symbol, " +
		"AnnounceDate, AllocShare, AllocPrice, TotalShare, XDate, RegDate, StartPayDate, EndPayDate, " +
		"ListDate, TotalAmount) " +
		"values (?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String FIND_LIST_DATE = "SELECT Date FROM STOCK_PRICE WHERE Symbol = ? AND Date >= ? LIMIT 1 OFFSET 4";

	// ��������
	private static final String CREATE_STOCK_BONUS = "CREATE  TABLE STOCK_BONUS (" +
		"Uid NUMERIC PRIMARY KEY  NOT NULL, " +			// ��������
		"Symbol VARCHAR NOT NULL, " +					// ��Ʊ����.���н���������600001.sh 000001.sz
		"AnnounceDate VARCHAR, " +						// ������
		"BonusShare DOBULE, " + 						// �ֺ����(ÿʮ��)
		"TransitShare DOBULE, " +						// ת������(ÿʮ��)
		"Dividend DOBULE, " + 							// ��Ϣ(ÿʮ��)
		"TotalShare DOBULE, " +							// ��׼�ɱ�����λ���)
		"XDate VARCHAR, " +								// ��Ȩ��Ϣ��
		"RegDate VARCHAR, " +							// ��Ȩ�Ǽ���
		"ListDate VARCHAR" +							// ���������
		")";
	
	// ���
	private static final String CREATE_STOCK_ALLOC = "CREATE TABLE STOCK_ALLOC (" +
		"Uid NUMERIC PRIMARY KEY  NOT NULL , " + 	// ��������
		"Symbol VARCHAR, " + 						// ��Ʊ����.���н���������600001.sh 000001.sz
		"AnnounceDate VARCHAR, " +					// ������
		"AllocShare DOBULE, " + 					// �����(ÿʮ��)
		"AllocPrice DOBULE, " + 					// ��ɼ�
		"TotalShare DOBULE, " +						// ��׼�ɱ�����λ���)
		"XDate VARCHAR, " +							// ��Ȩ��Ϣ��
		"RegDate VARCHAR, " +						// ��Ȩ�Ǽ���
		"StartPayDate VARCHAR, " +					// �ɿ���ʼ��
		"EndPayDate VARCHAR, " +					// �ɿ������
		"ListDate VARCHAR, " +						// ���������
		"TotalAmount DOUBLE" + 						// ļ���ʽ�ϼ�
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
								
				String listDate = fields[8].trim();
				if (listDate.length() == 0 && fields[6].trim().length() > 0) {
					PreparedStatement stat = null;
					try {
						stat = conn.prepareStatement(FIND_LIST_DATE);
						stat.setString(1, fields[0]); // symbol
						stat.setString(2, fields[6]); // XDate
						ResultSet results = stat.executeQuery();
						listDate = results.getString(1);
					} catch(Exception e) {
						e.printStackTrace();
					} finally {
						stat.close();
					}
				}
				
				prep.setString(10, listDate); 						// ListDate
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
