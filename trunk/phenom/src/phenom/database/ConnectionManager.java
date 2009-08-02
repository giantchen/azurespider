package phenom.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionManager {
	private static String _dbPath;
	
	public static void init(String dbPath_) {
		_dbPath = dbPath_;
	}
	
	public static Connection getConnection(String dbPath_) {
		if(_dbPath == null) {
			_dbPath = dbPath_;
		}
		
		Connection conn = null;			
		try {
			Class.forName("org.sqlite.JDBC");
			String scon = "jdbc:sqlite:" + _dbPath;
			conn = DriverManager.getConnection(scon);		
			
		} catch (Exception e) {
			e.printStackTrace();			
		}
		
		return conn;
	}
}
