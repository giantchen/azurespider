package phenom.stock.strategy.eric;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import phenom.stock.NameValuePair;
import phenom.stock.signal.ISignal;
import phenom.stock.signal.SignalHolder;
import phenom.stock.trading.MyPortfolio;
import phenom.stock.trading.MyStock;
import phenom.stock.trading.MyTrade;
import phenom.utils.DateUtil;
import phenom.utils.graph.TimeSeriesGraph;

public class EricStrategy {
	private double initCash;
	static private String dbPath = "data/superT_STOCK.sqlite";
	static private String scon = "jdbc:sqlite:" + dbPath;
	static private String SQL_ALLSYMBOLS = "SELECT Symbol FROM STOCK_INFO WHERE Type = '深证A股' OR Type = '上证A股'";
	private Connection conn;
	
	@Before
	public void before() {
		initCash = 100000.0;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(scon);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	@Test
	public void ReturnOnEquityStrategy() throws Exception {
		String startDate = "20090105";
		String endDate = "20091225";	

		List<String> symbols = this.getAllSymbols();
		SignalHolder signals = new SignalHolder(symbols, startDate, endDate);
		
		basicIndicatorTest(startDate, endDate, symbols, signals.getReturnOnEquity());
	}
	
	@Test
	public void NetAssetsStrategy() throws Exception {
		String startDate = "20090105";
		String endDate = "20091204";	

		List<String> symbols = this.getAllSymbols();
		SignalHolder signals = new SignalHolder(symbols, startDate, endDate);
		
		basicIndicatorTest(startDate, endDate, symbols, signals.getNetAssetsPerShareSignal());
	}
	
	@Test
	public void CashFlowToPriceStrategy() throws Exception {
		String startDate = "20090105";
		String endDate = "20091211";	

		List<String> symbols = this.getAllSymbols();
		SignalHolder signals = new SignalHolder(symbols, startDate, endDate);
		
		basicIndicatorTest(startDate, endDate, symbols, signals.getCashFlowToPrice());
	}	
	
	@Test
	public void EarningPerShareStrategy() throws Exception {
		String startDate = "20090105";
		String endDate = "20100205";	

		List<String> symbols = this.getAllSymbols();
		SignalHolder signals = new SignalHolder(symbols, startDate, endDate);
		
		basicIndicatorTest(startDate, endDate, symbols, signals.getEarningPerShare());
	}	
	
	@Test
	public void EarningToPriceStrategy() throws Exception {
		String startDate = "20090105";
		String endDate = "20100305";	

		List<String> symbols = this.getAllSymbols();
		SignalHolder signals = new SignalHolder(symbols, startDate, endDate);
		
		basicIndicatorTest(startDate, endDate, symbols, signals.getEarningToPriceSignal());
	}
	
	@Test
	public void NetProfitStrategy() throws Exception {
		String startDate = "20090105";
		String endDate = "20100205";	

		List<String> symbols = this.getAllSymbols();
		SignalHolder signals = new SignalHolder(symbols, startDate, endDate);
		
		basicIndicatorTest(startDate, endDate, symbols, signals.getNetProfit());
	}

	private void basicIndicatorTest(final String startDate, final String endDate, final List<String> symbols, ISignal signal) throws Exception {
		MyPortfolio portfolio = new MyPortfolio(startDate, endDate, initCash);
		MyStock index = new MyStock("000001.sh", startDate,endDate);
		List<Double> PandL = new ArrayList<Double>();
		List<Double> indexPrice = new ArrayList<Double>();
		System.out.println(portfolio.getToday() + " = " + portfolio.PandL() + " (" + portfolio.getCash() +") （0) " + portfolio);
		PandL.add(portfolio.PandL() / initCash);
		double base = index.getClosePrice(startDate);
		indexPrice.add(1.0);
		
		while (!portfolio.nextDay().equals(endDate)) {
			List<NameValuePair> indicators = new ArrayList<NameValuePair>();
			System.out.println(portfolio.getToday() + " = " + portfolio.PandL() + " (" + portfolio.getCash() +") （" + (portfolio.PandL() - PandL.get(PandL.size() - 1) * initCash) + ") " + portfolio);
			System.out.println("Strategy for tomorrow");
			PandL.add(portfolio.PandL() / initCash);
			indexPrice.add(index.getClosePrice(portfolio.getToday()) / base);
			
			for (String symbol : symbols){
				if (signal.calculate(symbol, portfolio.getToday()) != Double.NaN)
					indicators.add(new NameValuePair(symbol, signal.calculate(symbol, portfolio.getToday())));
			}
			
			Collections.sort(indicators, NameValuePair.VALUE_COMPARATOR_DESC);
			
			Set<String> top5symbols = new HashSet<String>();
			Set<String> symbolsToBuy = new HashSet<String>();
			
			int i = 0;
			while (top5symbols.size() < 5 && i < indicators.size()) {
				String symbol = indicators.get(i++).getName();
				if (!DateUtil.isTradeDate(symbol, portfolio.getToday()))
					continue;
				
				top5symbols.add(symbol);
				if (!portfolio.getSymbols().contains(symbol)) {
						symbolsToBuy.add(symbol);
				}						
			}
			System.out.println(top5symbols);
			
			List<String> symbolsToSell = new ArrayList<String>();
			// sell the symbols not in the top 5 list
			for (String symbol : portfolio.getSymbols())
				if (!top5symbols.contains(symbol) 
						&& DateUtil.isTradeDate(symbol, portfolio.getToday())
						) {
					symbolsToSell.add(symbol);
					portfolio.sell(symbol);
					System.out.println("Sell " + symbol);
				}
			
			if (symbolsToBuy.size() == 0 && portfolio.getCash() / portfolio.PandL() >= 0.1) {
				// 加仓
				for (MyTrade t : portfolio.getTrades()) {
					if (t.getShares() + t.getSharesOnTheWay() != 0
							&& DateUtil.isTradeDate(t.getStock().getSymbol(), portfolio.getToday()))
						symbolsToBuy.add(t.getStock().getSymbol());
				}
				symbolsToBuy.removeAll(symbolsToSell);
				System.out.print("加仓 ");
				System.out.println(symbolsToBuy);
			}
			
			// buy the symbols in the top 5 list
			for (String symbol : symbolsToBuy) {
				portfolio.buy(symbol, portfolio.getCash() / symbolsToBuy.size());
				System.out.println("Buy " + symbol);
			}
		}

		System.out.println(PandL);
		TimeSeriesGraph graph = new TimeSeriesGraph(startDate + " - " + endDate, "Date", "Price & Money");
		graph.addDataSource(signal.getClass().getCanonicalName(), PandL);
		graph.addDataSource("Index", indexPrice);
		graph.display();
		try {
			Thread.sleep(600 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private List<String> getAllSymbols() {
		List<String> symbols = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = conn.prepareStatement(SQL_ALLSYMBOLS);
			result = statement.executeQuery();
			while (result.next())
				symbols.add(result.getString(1));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
				conn.close();				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
		return symbols;
	}
}