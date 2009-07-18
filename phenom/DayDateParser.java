package phenom.dataparse;


import java.io.*;
import java.io.FileInputStream;
import java.io.EOFException;
import java.util.Date;
import java.sql.*;
import java.io.BufferedInputStream;
import java.util.*;

import phenom.utils.io.ExtendedDataInputStream;


/**
date:LongInt;   //日期   
open:LongInt;   //开盘(元/1000)   
high:LongInt;   //最高价(元/1000)   
low:LongInt;   //最低价(元/1000)   
close:LongInt;   //收盘(元/1000)   
amount:LongInt;   //成交额(千元)   
volume:LongInt;   //成交量(手)  
Nouse1:LongInt;   //没用  
Nouse2:LongInt;   //没用  
Nouse3:LongInt;   //没用   
*/
public class DayDateParser {
	public static final String INSERT_STOCK_PRICE = "insert into STOCK_PRICE (Stock_Nb, Stock_Id, Stock_Ds, " +
			"Open_Price, Highest_Price, Lowest_Price, Close_Price, Amount, Volume, Last_UpdId, " +
			"Last_UpdDs) values (?,?,?,?,?,?,?,?,?,?,?)";
	
	public static final String INSERT_STOCK_WEIGHT = "insert into STOCK_WEIGHT (Weight_Nb, Stock_Id, Weight_Ds, "
		+ " Gift_Stock, Stock_Amount, Stock_Price, Bonus, Trans_Num, Total_Amount, Liquid_Amount, Last_UpdId, "
		+ "Last_UpdDs) values (?,?,?,?,?,?,?,?,?,?,?,?)";
	
	private static String _dbPath = "E:\\fei\\SQLite\\SuperT_STOCK.sqlite";
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, String> fileNames = new HashMap<String, String>();
		Map<String, String> duplicates = new HashMap<String, String>();
		/** 建立当前目录中文件的File对象 */
		File filesDir = new File(args[1]);
		
		System.out.println(filesDir);
		
		/** */
		/** 取得代表目录中所有文件的File对象数组 */
		File list[] = filesDir.listFiles();
		System.out.println("totally = " + list.length);
		
		for (int i = 0; i < list.length; i++) {
			if (list[i].isFile()) {
				String f = list[i].getName();
				if(fileNames.get(f) == null)
					fileNames.put(f, f);
				else {
					duplicates.put(f, f);
					continue;
				}
				String stockId = f.replaceAll(".day", "");
				/*if(Integer.parseInt(stockId) <= 600312) {
					System.out.println("pass " + stockId);
					continue;
				}*/
					
				
				/*if(Integer.parseInt(stockId) > 900000) {
					System.out.println("exit " + stockId);
					System.exit(0);
				}*/
				
				
				//readDailyPrice(filesDir + "\\" + f);
				readWeight(filesDir + "\\" + f);
				System.out.println("finish processing" + f);
			}
		}
		
		System.out.println(duplicates);
		//readWeight(args[0]);
	}

	public static void readDailyPrice(String path_) {
		BufferedInputStream fi = null;
		PreparedStatement prep = null;
		Connection conn = null;
		Date curDate = new java.sql.Date(System.currentTimeMillis());
		ExtendedDataInputStream di = null;
		StringBuilder sb = new StringBuilder();
		int id = 0;
		int count = 0;
		
		int date, open, high, low, close, amount, volumn, nouse1, nouse2, nouse3;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbPath = path_;
			String scon = "jdbc:sqlite:" + _dbPath;
			conn = DriverManager.getConnection(scon);
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery("select max(Stock_Nb) id from STOCK_PRICE");
			id = rs.getInt("id");
			rs.close();
			
			conn.setAutoCommit(false);
			
			String stockId = path_;
			//conn.setAutoCommit(false);
			int d1 = stockId.lastIndexOf("\\");
			stockId = stockId.substring(d1 + 1, stockId.length());
			stockId = stockId.substring(0, stockId.length() - 4);
			//String stockId = path_.replaceAll(".day", "");
			
			 prep = conn.prepareStatement(INSERT_STOCK_PRICE);
			
			fi = new BufferedInputStream(new FileInputStream(path_));
			di = new ExtendedDataInputStream(fi);
			byte []bs = new byte[4];
			/*sb.append("date   ").append("open   ").append("high   ").append("low   ").append("close   ").append("amount   ").append("volumn   ");
			sb.append("\n").append("----------------------------------------\n");*/
			
			while(true) {
				date = di.readInt1();
				java.util.Date d = new java.util.Date();
				open = di.readInt1();
				high = di.readInt1();
				low = di.readInt1();
				close = di.readInt1();
				amount = di.readInt1();
				volumn = di.readInt1();
				nouse1 = di.readInt1();
				nouse2 = di.readInt1();
				nouse3 = di.readInt1();
				
				prep.setInt(1, ++id);
				prep.setString(2, stockId);
				//prep.setDate(3, DateUtil.parse(date));
				prep.setInt(3, date);
				prep.setDouble(4, open);
				prep.setDouble(5, high);
				prep.setDouble(6, low);
				prep.setDouble(7, close);
				prep.setInt(8, amount);
				prep.setInt(9, volumn);
				prep.setString(10, "feiyang");
				prep.setDate(11, (java.sql.Date)curDate);
				prep.addBatch();
				count++;
				if(count % 500 == 0) {
					prep.executeBatch();
					conn.commit();
				}
				/*sb.append(date).append("   ")
				.append(open).append("   ")
				.append(high).append("   ")
				.append(low).append("   ")
				.append(amount).append("   ")
				.append(volumn).append("   ")
				.append(nouse1).append("   ")
				.append(nouse2).append("   ")
				.append(nouse3).append("   ");
				sb.append("\n");*/				
				
			}
			
		} catch (EOFException ex) {			
			
			try {
				// normal termination
				if(count % 500 != 0) {
					prep.executeBatch();
					conn.commit();
				}
				
				//conn.commit();
				prep.close();
				conn.close();
				di.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception ex) {
			// abnormal termination
			System.err.println(ex);
		}
		
		/*System.out.println(sb.toString());
		System.out.println(id);*/
		
		//System.out.println("done");

	}
	
	/**
	 * struct Weight {

    unsigned long date; // 日期高bit为年，接着bit为月，接着bit为日
    unsigned long gift_stock; // 送股数* 10000
    unsigned long stock_amount; // 配股数* 10000
    unsigned long stock_price; // 配股价* 1000
    unsigned long bonus; // 红利* 1000
    unsigned long trans_num; // 转增数
    unsigned long total_amount; // 总股本，单位万
    unsigned long liquid_amoun; // 流通股，单位万
    最后一个字节没用
	 * @param path_
	 */
	public static void readWeight(String path_) {
		BufferedInputStream fi = null;
		PreparedStatement prep = null;
		Connection conn = null;
		Date curDate = new java.sql.Date(System.currentTimeMillis());
		
		System.out.println("&&&&&&&&&&&&&&curDate=" + curDate);
		ExtendedDataInputStream di = null;
		StringBuilder sb = new StringBuilder();
		int id = 0;
		int count = 0;
		int date, gift_stock, stock_amount, stock_price, bonus, trans_num, total_amount, liquid_amount;
		try {
			fi = new BufferedInputStream(new FileInputStream(path_));
			di = new ExtendedDataInputStream(fi);
			byte []bs = new byte[4];
			/*sb.append("Weight-----\ndate   ").append("gift_stock   ").append("stock_amount   ").append("stock_price   ").append("bonus   ").append("trans_num   ")
			.append("total_amount").append("liquid_amoun");
			sb.append("\n").append("----------------------------------------\n");*/
			
			Class.forName("org.sqlite.JDBC");
			String dbPath = path_;
			String scon = "jdbc:sqlite:" + _dbPath;
			conn = DriverManager.getConnection(scon);
			
			conn.setAutoCommit(false);
			
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery("select max(Weight_Nb) id from STOCK_WEIGHT");
			id = rs.getInt("id");
			rs.close();
			
			String stockId = path_;
			int d1 = stockId.lastIndexOf("\\");
			stockId = stockId.substring(d1 + 1, stockId.length());
			stockId = stockId.substring(0, stockId.length() - 4);
			
			prep = conn.prepareStatement(INSERT_STOCK_WEIGHT);
			
			while(true) {
				date = di.readInt2();			
				
				gift_stock = di.readInt1();
				stock_amount = di.readInt1();
				stock_price = di.readInt1();
				bonus = di.readInt1();
				trans_num = di.readInt1();
				total_amount = di.readInt1();
				liquid_amount = di.readInt1();	
				
				di.readInt();// no use byte
				
				/*sb.append(date).append("   ")
				.append(gift_stock).append("   ")
				.append(stock_amount).append("   ")
				.append(bonus).append("   ")
				.append(stock_price).append("   ")
				.append(trans_num).append("   ")
				.append(total_amount).append("   ")
				.append(liquid_amount).append("   ");
				
				sb.append("\n");*/
				
				prep.setInt(1, ++id);
				prep.setString(2, stockId);
				//prep.setDate(3, DateUtil.parse(date));
				prep.setInt(3, date);
				prep.setInt(4, gift_stock);
				prep.setInt(5, stock_amount);
				prep.setDouble(6, stock_price);
				prep.setDouble(7, bonus);
				prep.setInt(8, trans_num);
				prep.setInt(9, total_amount);
				prep.setInt(10, liquid_amount);
				prep.setString(11, "feiyang");
				prep.setDate(11, (java.sql.Date)curDate);
				prep.addBatch();
				count++;
				if(count % 500 == 0) {
					prep.executeBatch();
					conn.commit();
				}				
			}
			
			
		} catch (EOFException ex) {
			try {
				// normal termination
				if(count % 500 != 0) {
					prep.executeBatch();
					conn.commit();
				}
				
				//conn.commit();
				prep.close();
				conn.close();
				di.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception ex) {
			// abnormal termination
			System.err.println(ex);
		}
		
		System.out.println(sb.toString());
		//System.out.println(count);
	}	
	
}
