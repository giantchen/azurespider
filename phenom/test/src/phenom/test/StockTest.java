package phenom.test;

import java.util.List;
import phenom.stock.Stock;

public class StockTest { 

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		Stock s1 = new Stock("600518.sh");
		List<Stock> ls = s1.getStock("20010101", "20090803", true);
		
		System.out.println("done = " + ls.size());
		
		for(Stock s : ls) {
			StringBuilder sb = new StringBuilder();
			sb.append("date = ").append(s.getDate()).append(" | closePrice = ").append(s.getClosePrice());
			System.out.println(sb.toString());
		}
	}

}
