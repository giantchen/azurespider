package phenom.stock.trading;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import phenom.stock.Dividend;
import phenom.utils.DividendUtil;
import phenom.utils.DividendUtil.DateType;

public class MyTrade {
	protected double commision; // 佣金
	protected double minCommision; // 最小佣金，单位：元
	protected double sellTax; // 卖出 印花税
	protected double buyTax; // 买入 印花税

	protected double cash;
	protected double cashOnTheWay;
	protected int shares;
	protected int sharesOnTheWay;
	private Integer currentDate; // the current processing day
	private Integer lastTradeDate;
	private Integer endDate;
	private MyStock stock;
	int entitledVolume; // if this trade is entitled for XR XD
	double bonusCash, bonusShares, tranShares, allocShares, allocPrice;
	private static int COEFFICENT = 1;
	
	private List<IOperation> operations = new ArrayList<IOperation>();
	
	interface IOperation {
		public void action();
		public void withdraw();
	}
	
	class DROperation implements IOperation {
		private int volume;
		public DROperation(int volume) {
			this.volume = volume;
		}
		
		@Override
		public void action() {
			try {
				// allocate shares
				double alloc = (int) (volume / COEFFICENT * allocShares);
				sharesOnTheWay += alloc;
				cashOnTheWay -= round(alloc * allocPrice);
				if (alloc > 0) {
					System.out.println("symbol = " + getStock().getSymbol() + " alloc = " + allocShares + " price = " + allocPrice);
				}
				sharesOnTheWay += (int) (volume / COEFFICENT * bonusShares) + (int) (volume / COEFFICENT * tranShares);
				cashOnTheWay += round(volume / COEFFICENT * bonusCash);
				bonusShares = 0;
				bonusCash = 0;
				tranShares = 0;
				entitledVolume = 0;			
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}

		@Override
		public void withdraw() {
		}
	}
	
	class BuyOperation implements IOperation {
		private double money;
		public BuyOperation(double money) {
			this.money = money;
		}
		
		public double getMoney() {
			return money;
		}
		
		@Override
		public void action() {
			try {
				double price = stock.getOpenPrice(currentDate);
				int volume = (int)(money / price / 100) * 100;
				double amount = round(price * volume);
				if (amount > 0) {
					double comm = round(Math.max(minCommision, amount * commision));
					double cost = round(comm + amount * buyTax);
					sharesOnTheWay += volume;
					money -= amount + cost;
					cash += round(money);
					money = 0;
				} else {
					cash =+ money;
					money = 0;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}

		@Override
		public void withdraw() {
			cash += money;
			money = 0;
		}
	}	
	
	class SellOperation implements IOperation {
		private int volume;
		boolean sellAll = true;
		public SellOperation(int volume) {
			this.volume = volume;
			sellAll = false;
		}
		
		public SellOperation() {
			sellAll = true;
		}
		
		@Override
		public void withdraw() {
			shares += volume;
			volume = 0;
		}
		
		@Override
		public void action() {
			try {
				double price = stock.getOpenPrice(currentDate);
				if (sellAll) { // sell all
					double amount = round(shares * price);
					if (amount > 0) {
						double comm = round(Math.max(minCommision, amount * commision));
						double cost = round(comm + amount * sellTax);
						cashOnTheWay += amount - cost;
						shares = 0;
					}
				} else {
					int v = volume > shares ? shares : volume;
					double amount = round(v * price);
					if (amount > 0) {
						double comm = round(Math.max(minCommision, amount * commision));
						double cost = round(comm + amount * sellTax);
						cashOnTheWay += round(amount - cost);
						shares -= v;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	public enum Action {
		Buy, Sell
	}

	public MyTrade(final String symbol) {
		stock = new MyStock(symbol);
		init(stock.getStartDate(), stock.getEndDate());
	}
	
	public void setCurrentDate(final String date) {
		currentDate = Integer.parseInt(date);
		lastTradeDate = currentDate;
	}
	
	public MyTrade(final String symbol, final String startDate, final String endDate) {
		stock = new MyStock(symbol, startDate, endDate);
		init(startDate, endDate);
	}
	
	public MyTrade(final MyStock stock) {
		this.stock = stock;
		init(stock.getStartDate(), stock.getEndDate());
	}
	
	public MyStock getStock() {
		return this.stock;
	}
	
	private void init(final String startDate, final String endDate) {
		entitledVolume = 0;
		bonusCash = 0.0;
		bonusShares = 0.0;
		tranShares = 0.0;
		commision = 0.001;
		minCommision = 5.0;
		sellTax = 0.001;
		buyTax = 0.0;
		lastTradeDate = Integer.parseInt(startDate);
		currentDate = Integer.parseInt(startDate);
		this.endDate = Integer.parseInt(endDate);
	}
	
	public String getCurrentDate() {
		return currentDate.toString();
	}
	
	@Override
	public String toString() {
		return "[" + stock.getSymbol() + ":" + (shares + sharesOnTheWay) + "]";
	}
	
	public double getTotalMoney() {
		double ret = getCash() + getCashOnTheWay() + getAmount();
		for (IOperation op : operations) {
			if (op instanceof BuyOperation)
				ret += ((BuyOperation)op).getMoney();
		}
		return new BigDecimal(ret).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	public double getLastClosePrice() {
		double closePrice =  0;
		if (stock.isTradeDate(currentDate.toString()))
			closePrice = stock.getClosePrice(currentDate.toString());
		else
			closePrice = stock.getClosePrice(lastTradeDate.toString());
		return closePrice;
	}
	
	public int getShares() {
		return shares;
	}
	
	public int getSharesOnTheWay() {
		return sharesOnTheWay;
	}
	
	public double getCashOnTheWay() {
		return new BigDecimal(cashOnTheWay).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	public double getAmount() {
		return (shares + sharesOnTheWay) * getLastClosePrice();		
	}
	
	public void nextDay() {
		currentDate = stock.getNextMarketOpenDate(currentDate);
		cash += cashOnTheWay;
		cashOnTheWay = 0;
		shares += sharesOnTheWay;
		sharesOnTheWay = 0;		
		
		// DX DR date
		if ((DividendUtil.getAllocEntitlement(stock.getSymbol(), currentDate.toString(), DateType.XDATE) != null
				|| DividendUtil.getEntitlement(stock.getSymbol(), currentDate.toString(), DateType.XDATE) != null)
				&& entitledVolume > 0)
			operations.add(new DROperation(entitledVolume));
		
		if (stock.isTradeDate(currentDate.toString())) {
			lastTradeDate = currentDate;
			this.shares += this.sharesOnTheWay;
			sharesOnTheWay = 0;
			for (IOperation op : operations) {
				op.action();
			}
		} else {
			for (IOperation op : operations) {
				op.withdraw();
			}
		}
		
		// DX DR registration 
		// Bonus Stock || Bonus Cash
		Dividend bonus = DividendUtil.getEntitlement(stock.getSymbol(), currentDate.toString(), DateType.REG_DATE);
		if (bonus != null) {
			entitledVolume = shares + sharesOnTheWay;
			bonusCash = bonus.getCashDiv();
			bonusShares = bonus.getStockDiv();
			tranShares = bonus.getTranDiv();
		}
		
		// Allocate stock
		bonus = DividendUtil.getAllocEntitlement(stock.getSymbol(), currentDate.toString(), DateType.REG_DATE);
		if (bonus != null) {
			entitledVolume = shares + sharesOnTheWay;
			allocShares = bonus.getAllocShare();
			allocPrice = bonus.getAllocPrice();
		}
		operations.clear();
	}
	
	public double getCash() {
		return new BigDecimal(cash).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	public void buy(double cash) {
		IOperation buy = new BuyOperation(cash);
		operations.add(buy);
	}
	
	public void sell(int amount) {
		IOperation sell = new SellOperation(amount);
		operations.add(sell);
	}
	
	public void sell() {
		IOperation sell = new SellOperation();
		operations.add(sell);
	}
	
	private double round(double number) {
		return new BigDecimal(number).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public void setCash(double cash) {
		this.cash = cash;		
	}

	public boolean DREntitled() {
		return entitledVolume > 0;
	}
}
