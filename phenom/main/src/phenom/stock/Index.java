package phenom.stock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import phenom.database.ConnectionManager;

public class Index {
	private final static String INDEX_SQL = "select distinct IndexId from STOCK_INDEX where " +
			"IndexId between '000908.sh' and '000916.sh'";
	@SuppressWarnings("unused")
	private final static String INDEX_SQL1 = "select distinct IndexId from STOCK_INDEX where " +
	"IndexId between '000914.sh' and '000915.sh'";
	
	private final static String INDEX_STOCK_SQL = "select distinct Symbol from STOCK_INDEX where IndexId = ? order by Symbol";
	public static Map<String, List<String>> getIndexStockMapping() {
		Map<String, List<String>> is = new HashMap<String, List<String>>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;		

		try {
			conn = ConnectionManager.getConnection();
			rs = conn.createStatement().executeQuery(INDEX_SQL);

			while (rs.next()) {
				is.put(rs.getString("IndexId"), null);
			}
			rs.close();
			
			ps = conn.prepareStatement(INDEX_STOCK_SQL);
			for(String indexId : is.keySet()) {
				List<String> ls = is.get(indexId);
				if(ls == null) {
					ls = new ArrayList<String>();
					is.put(indexId, ls);
				}
				ps.setString(1, indexId);
				rs = ps.executeQuery();
				while(rs.next()) {
					ls.add(rs.getString("Symbol"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return is;
	}
}