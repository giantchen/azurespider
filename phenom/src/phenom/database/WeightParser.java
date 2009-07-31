package phenom.database;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import phenom.utils.WeightUtil;

/**
 * date:LongInt; //���� open:LongInt; //����(Ԫ/1000) high:LongInt; //��߼�(Ԫ/1000)
 * low:LongInt; //��ͼ�(Ԫ/1000) close:LongInt; //����(Ԫ/1000) amount:LongInt;
 * //�ɽ���(ǧԪ) volume:LongInt; //�ɽ���(��) Nouse1:LongInt; //û�� Nouse2:LongInt; //û��
 * Nouse3:LongInt; //û��
 * 
 * STOCK_PRICE Schema: Stock_Nb: symbol: ��Ʊ���� �� 600010.sh 000001.sz date: ����
 * open: ���̼� high: ��ߵ� low: ��ͼ� close: ���̼� amount: �ɽ��� volume: �ɽ��� Last_UpdId:
 * �������� Last_UpdDs: ����������
 * 
 * 
 * STOCK_WEIGHT Schema: Weiht_Nb, symbol: ��Ʊ���� �� 600010.sh 000001.sz date: ����
 * allotstock: ����� allotprice: ��ɼ� dividend: ���� split: ת���� totalshare: �ܹɱ�
 * liquidshare: ��ͨ����
 */
public class WeightParser {
	private static final String DROP_STOCK_WEIGHT = "DROP TABLE IF EXISTS STOCK_WEIGHT";
	private static final String CREATE_STOCK_WEIGHT = "CREATE TABLE STOCK_WEIGHT (Uid NUMERIC, Symbol varchar(10), Date varchar, "
			+ "Gift NUMERIC, Amount NUMERIC, Price Double, Bonus Double, Trans NUMERIC, Total_Share NUMERIC, Liquid_Share NUMERIC)";
	
	private static final String CREATE_INDEX1 = "CREATE UNIQUE INDEX 'STOCK_WEIGHT_I1' ON 'STOCK_WEIGHT' ('Uid' ASC)";
	private static final String CREATE_INDEX2 = "CREATE UNIQUE INDEX 'STOCK_WEIGHT_I2' ON 'STOCK_WEIGHT' ('Symbol' ASC, 'Date' ASC)";
	
	private static final String INSERT_STOCK_WEIGHT = "insert into STOCK_WEIGHT (Uid, Symbol, Date, "
			+ " Gift, Amount, Price, Bonus, Trans, Total_Share, Liquid_Share)"
			+ " values (?,?,?,?,?,?,?,?,?,?)";
	private static String _dbPath = "E:\\fei\\SQLite\\SuperT_STOCK.sqlite";
	private static String _baseDir = "D:\\Program Files\\qianlong\\qijian\\QLDATA\\history\\";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, String> fileNames = new HashMap<String, String>();
		Map<String, String> duplicates = new HashMap<String, String>();
		// String[] exchanges = { "SHASE", "SZASE" };
		
		prepareTables();
		
		String[] exchanges = { "SHASE", "SZNSE" };
		
		/** ������ǰĿ¼���ļ���File���� */
		for (String ex : exchanges) {

			File filesDir = new File(_baseDir + ex + "\\weight");

			System.out.println(filesDir);

			/** */
			/** ȡ�ô���Ŀ¼�������ļ���File�������� */
			File list[] = filesDir.listFiles();
			System.out.println("totally = " + list.length);

			for (int i = 0; i < list.length; i++) {
				if (list[i].isFile()) {
					String f = list[i].getName();
					if (fileNames.get(f) == null)
						fileNames.put(f, f);
					else {
						duplicates.put(f, f);
						continue;
					}		
					String exchange = ex.substring(0, 2).toLowerCase();
					readWeight(filesDir + "\\" + f, exchange);
					System.out.println("finish processing" + f);
				}
			}

			System.out.println(duplicates);
		}
		// readWeight(args[0]);
	}

	private static void prepareTables() {
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String scon = "jdbc:sqlite:" + _dbPath;
			conn = DriverManager.getConnection(scon);
			Statement s = conn.createStatement();
			s.execute(DROP_STOCK_WEIGHT);
			s.execute(CREATE_STOCK_WEIGHT);
			s.execute(CREATE_INDEX1);
			s.execute(CREATE_INDEX2);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		} catch (ClassNotFoundException e) {
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

	/**
	 * struct Weight {
	 * 
	 * unsigned long date; // ���ڸ�bitΪ�꣬����bitΪ�£�����bitΪ�� unsigned long gift_stock;
	 * // �͹���* 10000 unsigned long stock_amount; // �����* 10000 unsigned long
	 * stock_price; // ��ɼ�* 1000 unsigned long bonus; // ����* 1000 unsigned long
	 * trans_num; // ת���� unsigned long total_amount; // �ܹɱ�����λ�� unsigned long
	 * liquid_amoun; // ��ͨ�ɣ���λ�� ���һ���ֽ�û��
	 * 
	 * @param path_
	 */
	public static void readWeight(String path_, String exchange_) {
		BufferedInputStream fi = null;
		PreparedStatement prep = null;
		Connection conn = null;
		Date curDate = new java.sql.Date(System.currentTimeMillis());
		
		ExtendedDataInputStream di = null;
		
		int id = 0;
		int count = 0;
		int date, gift_stock, stock_amount, stock_price, bonus, trans_num, total_amount, liquid_amount;
		try {
			fi = new BufferedInputStream(new FileInputStream(path_));
			di = new ExtendedDataInputStream(fi);
			
			Class.forName("org.sqlite.JDBC");
			
			String scon = "jdbc:sqlite:" + _dbPath;
			conn = DriverManager.getConnection(scon);

			conn.setAutoCommit(false);

			Statement s = conn.createStatement();

			ResultSet rs = s
					.executeQuery("select max(Uid) id from STOCK_WEIGHT");
			id = rs.getInt("id");
			rs.close();

			String stockId = path_;
			int d1 = stockId.lastIndexOf("\\");
			stockId = stockId.substring(d1 + 1, stockId.length());
			stockId = stockId.substring(0, stockId.length() - 4);

			prep = conn.prepareStatement(INSERT_STOCK_WEIGHT);

			while (true) {
				date = di.readInt2();
				date = WeightUtil.parseDate(date);
				
				gift_stock = di.readInt1();
				stock_amount = di.readInt1();
				stock_price = di.readInt1();
				bonus = di.readInt1();
				trans_num = di.readInt1();
				total_amount = di.readInt1();
				liquid_amount = di.readInt1();

				di.readInt();// no use byte
				
				prep.setInt(1, ++id);
				prep.setString(2, stockId + ".sz");
				// prep.setDate(3, DateUtil.parse(date));
				prep.setString(3, String.valueOf(date));
				prep.setInt(4, gift_stock);
				prep.setInt(5, stock_amount);
				prep.setDouble(6, stock_price);
				prep.setDouble(7, bonus);
				prep.setInt(8, trans_num);
				prep.setInt(9, total_amount);
				prep.setInt(10, liquid_amount);				
				
				prep.addBatch();
				count++;
				if (count % 500 == 0) {
					prep.executeBatch();
					conn.commit();
				}
			}

		} catch (EOFException ex) {
			try {
				// normal termination
				if (count % 500 != 0) {
					prep.executeBatch();
					conn.commit();
				}

				// conn.commit();
				prep.close();
				conn.close();
				di.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception ex) {
			// abnormal termination
			ex.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
