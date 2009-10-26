package phenom.utils;

import java.util.List;
import java.util.ArrayList;
import phenom.stock.Stock;

public class StockUtil {
	public static List<Double> transfer(List<Stock> stocks_) {
		List<Double> dStock = new ArrayList<Double>(stocks_.size());
		for(Stock s : stocks_) {
			dStock.add(s.getClosePrice() / stocks_.get(0).getClosePrice());
		}
		return dStock;
	}
}
