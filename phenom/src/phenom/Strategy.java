package phenom;

import static java.lang.System.out;
import static java.lang.System.err;

public class Strategy {
	protected String[] dates;
	protected double[] openPrices;
	protected double[] highPrices;
	protected double[] lowPrices;
	protected double[] closePrices;

	protected final double commision; // 佣金
	protected final double minCommision; // 最小佣金，单位：元
	protected final double sellTax; // 卖出 印花税
	protected final double buyTax; // 买入 印花税

	protected PriceData priceData;
	protected double cash;
	protected double cashOnTheWay;
	protected int shares;
	protected int sharesOnTheWay;

	private int lastDay = -1;

	public enum Operation {
		Buy, Sell, Noop
	}

	protected Strategy() {
		commision = 0.001;
		minCommision = 5.0;
		sellTax = 0.001;
		buyTax = 0.0;

		priceData = new PriceData();
	}

	protected Strategy(double commision, double minCommision, double sellTax,
			double buyTax) {
		this.commision = commision;
		this.minCommision = minCommision;
		this.sellTax = sellTax;
		this.buyTax = buyTax;

		priceData = new PriceData();
	}

	protected void before(String symbol, double initcash) {
		cash = initcash;
		cashOnTheWay = 0.0;
		shares = 0;
		sharesOnTheWay = 0;

		Stock stock = priceData.getStock(symbol);
		dates = stock.getDates();
		closePrices = stock.getClosePrices();
		highPrices = stock.getHighPrices();
		lowPrices = stock.getLowPrices();
		openPrices = stock.getOpenPrices();
	}

	protected void endDay(Operation op, int day, int howmany) {
		if (lastDay == -1) {
			lastDay = day;
		} else {
			if (lastDay + 1 != day) {
				err.printf("Warning: last trade day %d, this trade day %d\n",
						lastDay, day);
			}
			lastDay = day;
		}

		out.printf("Date %s, open %.3f, cash %.2f, shares %d\n", dates[day],
				openPrices[day], cash, shares);

		shares += sharesOnTheWay;
		sharesOnTheWay = 0;
		cash += cashOnTheWay;
		cashOnTheWay = 0;

		switch (op) {
		case Buy: {
			double amount = howmany * openPrices[day + 1]; // 第二天的开盘价
			double comm = Math.max(minCommision, amount * commision);
			double cost = comm + amount * buyTax;
			cash -= amount + cost;
			sharesOnTheWay += howmany;
		}
			break;

		case Sell: {
			double amount = howmany * openPrices[day + 1]; // 第二天的开盘价
			double comm = Math.max(minCommision, amount * commision);
			double cost = comm + amount * sellTax;
			cashOnTheWay += amount - cost;
			shares -= howmany;
		}
			break;

		case Noop:
			break;
		}
	}

	protected static void sleepSeconds(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
		}
	}
}
