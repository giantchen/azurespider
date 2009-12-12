package phenom.stock.strategy;

import java.util.List;
import java.util.ArrayList;
import phenom.stock.Stock;
import phenom.stock.Dividend;
import phenom.stock.signal.pricemomentum.EMovingAverage;
import phenom.utils.WeightUtil;
public class SingleStockStrategy {
	
	/**
	 * > 10�վ�����
	 * < 5�վ�����
	 * 
	 * ���赱����������̼����룬ʵ���п���������ǰ5�������Ƚϣ������̼�����
	 */
	public static void main(String[] args) {
		List<Div> sts = new ArrayList<Div>();
		
		double initialMoney = 30000;
		double position = 0;
		double cash = initialMoney;		
		
		double commission = 0.003;
		double stampTaxS = 0.001;
		double stampTaxB = 0;
		String buyDate = null;
		
		String symbol = "600118.sh";
		String fromDate = "20090101";
		String endDate = "20091231";
		List<Stock> lsts = Stock.getStock(symbol, fromDate, endDate, true);
		EMovingAverage em = new EMovingAverage();
		em.addValues(lsts, false);
		
		em.calculate(symbol, endDate, 10);
		em.calculate(symbol, endDate, 5);
				
		for(Stock s : lsts) {
			//����֮ǰ��������ʱ��û���ʵĹ�Ʊ���ֽ�
			if(sts.size() > 0) {
				for(Div d : sts) {
					if(s.getDate().equals(d.getDate())) {
						if(d.isCashDiv()) {
							cash = cash + d.getAmount();							
						} else {
							Stock k = Stock.getStock(d.getSymbol(), d.getDate());
							cash = cash + d.getAmount() * k.getClosePrice();
						}
						sts.remove(d);						
					}
				}
			}
			
			double mv5 = em.getAverage(symbol, s.getDate(), 30);
			double mv10 = em.getAverage(symbol, s.getDate(), 60);
			
			//���ڷֺ���ɵ�������ͷ����ʽ�
			
						
			if(s.getClosePrice() < mv10) {
				int p100 = (int)(cash / (s.getClosePrice() * 100));//����һ����
				if(p100 > 0) {
					buyDate = s.getDate();
					p100 = p100 * 100;//����һ����					
					cash = cash - p100 * s.getClosePrice() * (1 + commission + stampTaxB);
					position = position + p100;
				}
			} else if(s.getClosePrice() > mv5 && position > 0) {
				Dividend d = WeightUtil.applyDividend(s.getSymbol(), buyDate);
				
				if(d != null) {
					if(s.getDate().compareTo(d.getListDate()) > 0) {
						position = position * (1 + d.getTranDiv() + d.getStockDiv());						 
					} else if(s.getDate().compareTo(d.getDivDate()) > 0) {
						cash = cash + position * d.getStockDiv();
					} else {//����ʱ�����ã� ��Ҫ��¼�������������ڲ�����
						double inflightPos = position * (d.getTranDiv() + d.getStockDiv());
						//double inflightCash = position * d.getStockDiv();
						if(d.getCashDiv() > 0) {
							Div div = new Div(s.getSymbol(), d.getListDate(), inflightPos, "C");
							sts.add(div);
						} else if (d.getTranDiv() > 0 || d.getStockDiv() > 0) {
							Div div = new Div(s.getSymbol(), d.getListDate(), inflightPos, "S");
							sts.add(div);
						}
					}
				}
				
				cash = cash + position * s.getClosePrice() * (1 - commission - stampTaxS);
				position = 0;
			}
			
			System.out.println(s.getDate() + "--" + "position = " + position * s.getClosePrice() + " -- cash = " + cash);			
		}
				
		System.out.println("initial Money = " + initialMoney);
		System.out.println("cash = " + cash);
		System.out.println("position = " + position);		
	}
	
	static class Div {		
		public Div(String symbol, String date, double amount, String cash) {			
			this.amount = amount;			
			this.symbol = symbol;
			this.date = date;
			this.cash = cash;
		}
		private double amount;	
		private String symbol;
		private String date;
		private String cash;
		public double getAmount() {
			return amount;
		}
		public String getSymbol() {
			return symbol;
		}
		public String getDate() {
			return date;
		}
		public boolean isCashDiv() {
			return "C".equals(cash);
		}
	}
}