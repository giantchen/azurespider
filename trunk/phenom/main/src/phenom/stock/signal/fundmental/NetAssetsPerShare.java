package phenom.stock.signal.fundmental;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class NetAssetsPerShare extends AbstractFundmentalSignal {
	@Override
	public void addFundmentalData(List<FundmentalData> dataList) {
		for (FundmentalData data : dataList) {
			Map<String, Double> m = null; 
			if (values.containsKey(data.getSymbol()))
				m = values.get(data.getSymbol());
			else {
				m = new TreeMap<String, Double>(new Comparator<String>() {
						@Override
						public int compare(String o1, String o2) {
							return o2.compareTo(o1);
					}
				});
				values.put(data.getSymbol(), m);
			}
			m.put(data.getAnnounceDate(), data.getNetAssetsPerShare());
		}
	}
}
