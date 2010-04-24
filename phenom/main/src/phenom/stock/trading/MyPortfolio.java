package phenom.stock.trading;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyPortfolio {
	private List<MyTrade> trades;
	private List<String> tradingDates;
	private int dateIndex = 0;
	private double cash;
	static private String dbPath = "data/superT_STOCK.sqlite";
	static private String scon = "jdbc:sqlite:" + dbPath;
	static private String SQL_GET_MARKET_OPEN_DATES = "SELECT Date FROM STOCK_PRICE WHERE Symbol = '000001.sh' AND Date >= ? AND Date <= ?";
	private String startDate, endDate;
	
	public MyPortfolio(String startDate, String endDate, double initCash) {
		cash = initCash;
		trades = new ArrayList<MyTrade>();
		tradingDates = getTradingDates(startDate, endDate);
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public double PandL() {
		double total = cash;
		for (MyTrade t : trades) {
			total += t.getTotalMoney();
			System.out.print(t.getStock().getSymbol() + "<" + t.getTotalMoney() + "> ");
		}
		return total;
	}
	
	public void deposit(double cash) {
		this.cash += cash;
	}
	
	public List<String> getSymbols() {
		List<String> ret = new ArrayList<String>();
		for (MyTrade t : trades)
			ret.add(t.getStock().getSymbol());
		return ret;			
	}
	
	public String nextDay() {
		List<MyTrade> tradesToRemove = new ArrayList<MyTrade>();
		for (MyTrade t : trades) {
			t.nextDay();
			cash += t.getCash();
			t.setCash(0f);

			if (t.getTotalMoney() == 0 && t.getShares() + t.getSharesOnTheWay() == 0)
				tradesToRemove.add(t);
		}
		trades.removeAll(tradesToRemove);
		return tradingDates.get(++dateIndex);
	}
	
	public String getToday() {
		return tradingDates.get(dateIndex);
	}
	
	public double getCash() {
		return cash;
	}
	
	public void buy(String symbol, double amount) {
		if (amount > cash || amount < 1000)
			return;
		MyTrade trade = null;
		for (MyTrade t : trades) {
			if (t.getStock().getSymbol().equals(symbol)) {
				trade = t;
				break;
			}
		}
		if (trade == null) {
			trade = new MyTrade(symbol, getToday(), endDate);
			trades.add(trade);
		}
		trade.buy(amount);
		cash -= amount;
	}
	
	public void sell(String symbol, int volume) {
		for (MyTrade trade : trades) {
			if (trade.getStock().getSymbol().equals(symbol)){
				trade.sell(volume);
				return;
			}
		}
	}
	
	public void sellAmount(String symbol, double amount) {
		// do not sell if the amount is too smal to avoid transaction cost
		if (amount < 1000)
			return;
		for (MyTrade trade : trades) {
			if (trade.getStock().getSymbol().equals(symbol)){
				trade.sell((int)(amount / trade.getLastClosePrice()));
				return;
			}
		}
	}
	
	public void sell(String symbol) {
		for (MyTrade trade : trades) {
			if (trade.getStock().getSymbol().equals(symbol)){
				trade.sell();
				return;
			}
		}
	}
	
	public void trade(Map<String, Double> weights) {
		System.out.print("{ Holdings = ");
		for (MyTrade t : trades) {
			System.out.print(t.getStock().getSymbol() + ":" + t.getAmount() + " ");
		}
		System.out.println(" }");
		
		System.out.print("{ Cash = " + getCash() + " ");
		// Calculate the allocation of current portfolio
		Map<String, Double> holdings = new HashMap<String, Double>();
		for (MyTrade trade : trades) {
			if (weights.keySet().contains(trade.getStock().getSymbol()))
				holdings.put(trade.getStock().getSymbol(), trade.getAmount() / this.PandL());
			else {
				System.out.print("Sell: " + trade.getStock().getSymbol() + "[" + trade.getShares() + " -all] ");
				trade.sell();
			}
		}
		
		double currentCash = getCash();
		
		for (String sym : weights.keySet()) {
			if (!holdings.keySet().contains(sym)) {
				System.out.print("Buy: " + sym + "[" + currentCash * weights.get(sym) + "] ");
				buy(sym, currentCash * weights.get(sym));
			} else {
				if (weights.get(sym) >= holdings.get(sym)) {
					System.out.print("Buy: " + sym + "[" + currentCash * (weights.get(sym) - holdings.get(sym)) + "] ");
					buy(sym, currentCash * (weights.get(sym) - holdings.get(sym)));
				} else {
					System.out.print("Sell: " + sym + "[" + currentCash * (holdings.get(sym) - weights.get(sym)) + "] ");
					sellAmount(sym, (holdings.get(sym) - weights.get(sym)) * currentCash);
				}
			}
		}
		System.out.println("}");
	}
	
	public List<MyTrade> getTrades() {
		return trades;
	}
	
	private List<String> getTradingDates(final String startDate, final String endDate) {
		List<String> tradingDates = new ArrayList<String>();
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(scon);
			PreparedStatement statement = conn.prepareStatement(SQL_GET_MARKET_OPEN_DATES);
			statement.setString(1, startDate);
			statement.setString(2, endDate);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				tradingDates.add(result.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return tradingDates;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (MyTrade t : trades) {
			sb.append(t);
		}
		return sb.toString();
	}


}
