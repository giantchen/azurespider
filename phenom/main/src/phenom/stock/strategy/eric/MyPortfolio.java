package phenom.stock.strategy.eric;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
		for (MyTrade t : trades) 
			total += t.getTotalMoney();
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
		if (amount > cash)
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
	
	public void sell(String symbol) {
		for (MyTrade trade : trades) {
			if (trade.getStock().getSymbol().equals(symbol)){
				trade.sell();
				return;
			}
		}
	}
	
	public List<MyTrade> getTrades() {
		return trades;
	}
	
	public List<String> getTradingDates(final String startDate, final String endDate) {
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
