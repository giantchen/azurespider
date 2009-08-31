package phenom.stock.strategy;

import java.util.List;
import phenom.stock.Stock;
import phenom.stock.techind.EMovingAverage;
public class SingleStockStrategy {

	/**
	 * > 10日均线卖
	 * < 5日均线买
	 * 
	 * 假设当天可以用收盘价买入，实际中可以用收盘前5分钟做比较，以收盘价买入
	 */
	public static void main(String[] args) {
		double initialMoney = 30000;
		double position = 0;
		double cash = initialMoney;		
		
		String symbol = "600118.sh";
		String fromDate = "20090101";
		String endDate = "20091231";
		List<Stock> lsts = Stock.getStock(symbol, fromDate, endDate, true);
		EMovingAverage em = new EMovingAverage();
		em.addValues(lsts, false);
		
		em.calculate(symbol, endDate, 10);
		em.calculate(symbol, endDate, 5);
				
		for(Stock s : lsts) {
			double mv5 = em.getAverage(symbol, s.getDate(), 5);
			double mv10 = em.getAverage(symbol, s.getDate(), 10);			
						
			if(s.getClosePrice() < mv10) {
				int p100 = (int)(cash / (s.getClosePrice() * 100));//买卖一百手
				if(p100 > 0) {
					p100 = p100 * 100;//买卖一百手					
					cash = cash - p100 * s.getClosePrice();
					position = position + p100;
				}
			} else if(s.getClosePrice() > mv5) {
				cash = cash + position * s.getClosePrice();
				position = 0;
			}
			
			System.out.println(s.getDate() + "--" + "position = " + position * s.getClosePrice() + " -- cash = " + cash);
		}
				
		System.out.println("initial Money = " + initialMoney);
		System.out.println("cash = " + cash);
		System.out.println("position = " + position);
	}
}