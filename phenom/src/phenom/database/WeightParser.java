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
 * date:LongInt; //日期 open:LongInt; //开盘(元/1000) high:LongInt; //最高价(元/1000)
 * low:LongInt; //最低价(元/1000) close:LongInt; //收盘(元/1000) amount:LongInt;
 * //成交额(千元) volume:LongInt; //成交量(手) Nouse1:LongInt; //没用 Nouse2:LongInt; //没用
 * Nouse3:LongInt; //没用
 * 
 * STOCK_PRICE Schema: Stock_Nb: symbol: 股票代号 如 600010.sh 000001.sz date: 日期
 * open: 开盘价 high: 最高低 low: 最低价 close: 收盘价 amount: 成交额 volume: 成交量 Last_UpdId:
 * 最后更新人 Last_UpdDs: 最后更新日期
 * 
 * 
 * STOCK_WEIGHT Schema: Weiht_Nb, symbol: 股票代号 如 600010.sh 000001.sz date: 日期
 * allotstock: 配股数 allotprice: 配股价 dividend: 红利 split: 转增数 totalshare: 总股本
 * liquidshare: 流通股数
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
		
		/** 建立当前目录中文件的File对象 */
		for (String ex : exchanges) {

			File filesDir = new File(_baseDir + ex + "\\weight");

			System.out.println(filesDir);

			/** */
			/** 取得代表目录中所有文件的File对象数组 */
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
	 * unsigned long date; // 日期高bit为年，接着bit为月，接着bit为日 unsigned long gift_stock;
	 * // 送股数* 10000 unsigned long stock_amount; // 配股数* 10000 unsigned long
	 * stock_price; // 配股价* 1000 unsigned long bonus; // 红利* 1000 unsigned long
	 * trans_num; // 转增数 unsigned long total_amount; // 总股本，单位万 unsigned long
	 * liquid_amoun; // 流通股，单位万 最后一个字节没用
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
