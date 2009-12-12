package phenom.stock.signal.fundmental;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import phenom.database.ConnectionManager;
import phenom.stock.signal.GenericComputableEntry;
import phenom.stock.signal.ISignal;

public abstract class AbstractFundmentalSignal implements ISignal {
	protected Map<String, Map<String, Double>> values = new HashMap<String, Map<String, Double>>();
	protected Map<String, Map<String, Double>> prices = new HashMap<String, Map<String, Double>>();
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	public abstract void addFundmentalData(List<FundmentalData> dataList);
	public void addPrices(List<? extends GenericComputableEntry> priceList) {
		for (GenericComputableEntry data : priceList) {
			Map<String, Double> m = null; 
			if (prices.containsKey(data.getSymbol()))
				m = prices.get(data.getSymbol());
			else {
				m = new TreeMap<String, Double>(new Comparator<String>() {
						@Override
						public int compare(String o1, String o2) {
							return o2.compareTo(o1);
					}
				});
				prices.put(data.getSymbol(), m);
			}
			m.put(data.getDate(), data.getValue());
		}
	}
}
