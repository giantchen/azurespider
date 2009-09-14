package phenom.database;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

/**
 * 个股基本数据
 */
public class BasicInfoParser {
	// 个股基本数据表
	private static final String CREATE_STOCK_INFO = "CREATE TABLE 'STOCK_INFO' ("
			+ "Uid NUMERIC PRIMARY KEY  NOT NULL, " + // 物理主键
			"Symbol VARCHAR NOT NULL ," + // 股票代码.上市交易所，如600001.sh 000001.sz
			"Name VARCHAR, " + // 股票名，中文
			"Type VARCHAR, " + // 类型，中文，如开放基金，LOF，ETF
			"Industry_Id NUMERIC DEFAULT 0, " + // 行业代码
			"Exchange VARCHAR" + // 交易所 sz或sh
			")";
	private static final String INSERT_STOCK_INFO = "insert into STOCK_INFO (Uid, Symbol, Name, Type, Industry_Id, Exchange) "
			+ "values (?,?,?,?,?,?)";
	private static final String CREATE_INDEX_ON_STOCK_INFO = "CREATE INDEX 'Symbol_Exchange' ON "
			+ "'STOCK_INFO' ('Symbol' ASC, 'Exchange' ASC)";

	private static final String CREATE_INDUSTRY_INFO = "CREATE TABLE INDUSTRY_INFO ("
			+ "Uid NUMERIC PRIMARY KEY  NOT NULL , " + // 物理主键
			"Industry_Id NUMERIC NOT NULL , " + // 行业ID
			"Name VARCHAR" + // 行业名称
			")";
	private static final String INSERT_INDUSTRY_INFO = "insert into INDUSTRY_INFO (Uid, Industry_Id, Name) "
			+ "values (?,?,?)";

	private static final String DROP_STOCK_INFO = "DROP TABLE IF EXISTS STOCK_INFO";
	private static final String DROP_INDUSTRY_INFO = "DROP TABLE IF EXISTS INDUSTRY_INFO";
	private static String _industryFile = "data/hybk.ini";
	private static String _basicInfoFile = "data/comminfo.";
	private static String _dbPath = "data/SuperT_STOCK.sqlite";
	private Map<Integer, String> typeMap;

	public BasicInfoParser() {
		typeMap = new HashMap<Integer, String>();
		// Shanghai Exchange
		typeMap.put(0x0000, "上证指数");
		typeMap.put(0x0100, "上证A股");
		typeMap.put(0x0200, "上证B股");
		typeMap.put(0x0300, "上证基金");
		typeMap.put(0x0400, "上证债券");
		typeMap.put(0x0500, "上证转债");
		typeMap.put(0x0600, "上证回购");
		typeMap.put(0x1600, "上证其它");
		typeMap.put(0x0800, "上证ETF");
		typeMap.put(0x1700, "基金通");
		typeMap.put(0x0E00, "上证权证");

		// Shenzheng Exchange
		typeMap.put(0x0001, "深证成指");
		typeMap.put(0x0101, "深证A股");
		typeMap.put(0x0201, "深证B股");
		typeMap.put(0x0301, "深证基金");
		typeMap.put(0x0401, "深证债券");
		typeMap.put(0x0501, "深证转债");
		typeMap.put(0x0601, "深证回购");
		typeMap.put(0x1601, "深证其它");
		typeMap.put(0x0A01, "中小板块");
		typeMap.put(0x0B01, "LOF基金");
		typeMap.put(0x0C01, "非LOF基金");
		typeMap.put(0x0E01, "深证权证");
		typeMap.put(0x0801, "深证ETF");
		typeMap.put(0x0D01, "股份转让");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicInfoParser parser = new BasicInfoParser();
		parser.prepareTable();
		parser.populateTable();
	}

	@SuppressWarnings("deprecation")
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

			// populate industry info table
			prep = conn.prepareStatement(INSERT_INDUSTRY_INFO);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(_industryFile)));
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
					prep.setInt(1, ++uid);
					prep.setInt(2, bkno); // industryid
					prep.setString(3, desc); // industry description
					prep.addBatch();
					continue;
				}

				String[] fields = line.split(",");
				stockIndustryMap.put(
						Integer.parseInt(fields[0]) == 0 ? fields[1] + ".sh"
								: fields[1] + ".sz", bkno);
			}
			prep.executeBatch();
			conn.commit();

			// Processing stock basic information
			s = conn.createStatement();
			rs = s.executeQuery("select max(Uid) id from STOCK_INFO");
			uid = rs.getInt("id");
			rs.close();
			prep = conn.prepareStatement(INSERT_STOCK_INFO);
			String[] exchanges = { "sh", "sz" };
			for (String ex : exchanges) {
				DataInputStream is = new DataInputStream(
						new BufferedInputStream(new FileInputStream(
								_basicInfoFile + ex)));
				byte[] bytes = new byte[508];
				while (is.read(bytes) == 508) {
					int typeid = bytes[1] << 8 | bytes[0];
					byte[] stock_id_bytes = new byte[6];
					for (int i = 0; i < 6; ++i)
						stock_id_bytes[i] = bytes[4 + i];
					String stockid = new String(stock_id_bytes);

					ByteOutputStream os = new ByteOutputStream();
					int index = 0x11;
					while (bytes[index] != 0) {
						os.write(bytes[index++]);
					}
					String stockname = new String(os.toByteArray(), "GB2312");
					String symbol = stockid + "." + ex;
					System.out.println(symbol + " : " + stockname + " : "
							+ typeid + " : " + typeMap.get(typeid));
					prep.setInt(1, ++uid);
					prep.setString(2, symbol);
					prep.setString(3, stockname);
					prep.setString(4, typeMap.get(typeid));
					prep.setInt(5, stockIndustryMap.get(symbol) == null ? 0
							: stockIndustryMap.get(symbol));
					prep.setString(6, ex);
					prep.addBatch();
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
			s.executeUpdate(DROP_STOCK_INFO);
			s.executeUpdate(CREATE_STOCK_INFO);
			s.execute(CREATE_INDEX_ON_STOCK_INFO);
			s.executeUpdate(DROP_INDUSTRY_INFO);
			s.executeUpdate(CREATE_INDUSTRY_INFO);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}
}
