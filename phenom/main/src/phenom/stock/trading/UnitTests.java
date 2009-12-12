package phenom.stock.trading;

import junit.framework.Assert;

import org.junit.Test;

public class UnitTests {
	@Test
	public void testStock() throws Exception {
		MyStock stock = new MyStock("601600.sh");
		Assert.assertEquals(15.59, stock.getOpenPrice("20090825"));
		Assert.assertEquals(15.89, stock.getOpenPrice("20090825", true));
		Assert.assertEquals(15.12, stock.getClosePrice("20090825"));
		Assert.assertEquals(15.41, stock.getClosePrice("20090825", true));
		Assert.assertEquals(15.64, stock.getHighPrice("20090825"));
		Assert.assertEquals(15.94, stock.getHighPrice("20090825", true));
		Assert.assertEquals(14.01, stock.getLowPrice("20090825"));
		Assert.assertEquals(14.28, stock.getLowPrice("20090825", true));
		Assert.assertEquals("20090504", stock.getNextMarketOpenDate(20090430).toString());
		Assert.assertEquals("0", stock.getNextMarketOpenDate(20000101).toString());
	}
	
	@Test
	public void testXDateAndBouns() {
		MyTrade trade = new MyTrade("600118.sh");
		Assert.assertEquals("20000104", trade.getCurrentDate());
		
		trade.setCurrentDate("20090417");
		trade.buy(10000);
		trade.nextDay();
		
		Assert.assertEquals("20090420", trade.getCurrentDate());
		Assert.assertEquals(400, trade.getSharesOnTheWay());
		Assert.assertEquals(170.18, trade.getCash());
		Assert.assertEquals(10250.18, trade.getTotalMoney());
		Assert.assertEquals(0.0, trade.getCashOnTheWay());
		Assert.assertEquals(0, trade.getShares());
		Assert.assertEquals(true, trade.DREntitled());
		
		trade.nextDay();
		
		Assert.assertEquals("20090421", trade.getCurrentDate());
		Assert.assertEquals(false, trade.DREntitled());
		Assert.assertEquals(80, trade.getSharesOnTheWay());
		Assert.assertEquals(170.18, trade.getCash());
		Assert.assertEquals(40.0, trade.getCashOnTheWay());
		Assert.assertEquals(400, trade.getShares());
		Assert.assertEquals(10198.98, trade.getTotalMoney());
		
		trade.nextDay();
		Assert.assertEquals("20090422", trade.getCurrentDate());
		Assert.assertEquals(false, trade.DREntitled());
		Assert.assertEquals(0, trade.getSharesOnTheWay());
		Assert.assertEquals(210.18, trade.getCash());
		Assert.assertEquals(0.0, trade.getCashOnTheWay());
		Assert.assertEquals(480, trade.getShares());
		Assert.assertEquals(9642.18, trade.getTotalMoney());
	}
	
	@Test
	public void testTrade() throws Exception {
		MyTrade trade = new MyTrade("601600.sh");
		trade.setCurrentDate("20000104");
		trade.buy(10000);
		trade.nextDay();
	
		Assert.assertEquals("20000105", trade.getCurrentDate());
		Assert.assertEquals(10000.0, trade.getCash());
		Assert.assertEquals(10000.0, trade.getTotalMoney());
		Assert.assertEquals(0.0, trade.getCashOnTheWay());
		Assert.assertEquals(0, trade.getShares());
		Assert.assertEquals(0, trade.getSharesOnTheWay());
		
		trade.sell();
		trade.nextDay();
		Assert.assertEquals("20000106", trade.getCurrentDate());
		Assert.assertEquals(10000.0, trade.getCash());
		Assert.assertEquals(10000.0, trade.getTotalMoney());
		Assert.assertEquals(0.0, trade.getCashOnTheWay());
		Assert.assertEquals(0, trade.getShares());
		Assert.assertEquals(0, trade.getSharesOnTheWay());
		
		trade.setCurrentDate("20070430");
		trade.setCash(0);
		Assert.assertEquals("20070430", trade.getCurrentDate());
		Assert.assertEquals(0.0, trade.getCash());
		Assert.assertEquals(0.0, trade.getTotalMoney());
		Assert.assertEquals(0.0, trade.getCashOnTheWay());
		Assert.assertEquals(0, trade.getShares());
		Assert.assertEquals(0, trade.getSharesOnTheWay());
		
		// Buy stocks
		trade.buy(10000);
		trade.nextDay();
		
		Assert.assertEquals("20070508", trade.getCurrentDate());
		Assert.assertEquals(745.75, trade.getCash());
		Assert.assertEquals(9950.75, trade.getTotalMoney());
		Assert.assertEquals(0.0, trade.getCashOnTheWay());
		Assert.assertEquals(0, trade.getShares());
		Assert.assertEquals(500, trade.getSharesOnTheWay());
		
		trade.nextDay();
		Assert.assertEquals("20070509", trade.getCurrentDate());
		Assert.assertEquals(745.75, trade.getCash());
		Assert.assertEquals(10445.75, trade.getTotalMoney());
		Assert.assertEquals(0.0, trade.getCashOnTheWay());
		Assert.assertEquals(500, trade.getShares());
		Assert.assertEquals(0, trade.getSharesOnTheWay());

		// Sell 
		trade.sell(100);
		trade.nextDay();
		Assert.assertEquals("20070510", trade.getCurrentDate());
		Assert.assertEquals(745.75, trade.getCash());		
		Assert.assertEquals(1922.07, trade.getCashOnTheWay());
		Assert.assertEquals(400, trade.getShares());
		Assert.assertEquals(0, trade.getSharesOnTheWay());
		Assert.assertEquals(10363.82, trade.getTotalMoney());
		
		trade.nextDay();
		Assert.assertEquals("20070511", trade.getCurrentDate());
		Assert.assertEquals(2667.82, trade.getCash());		
		Assert.assertEquals(0.0, trade.getCashOnTheWay());
		Assert.assertEquals(400, trade.getShares());
		Assert.assertEquals(0, trade.getSharesOnTheWay());
		Assert.assertEquals(10691.82, trade.getTotalMoney());
		
		// buy and sell 
		trade.buy(10000.0);
		trade.sell();
		trade.nextDay();
		Assert.assertEquals("20070514", trade.getCurrentDate());
		Assert.assertEquals(7824.32, trade.getCashOnTheWay());
		Assert.assertEquals(500, trade.getSharesOnTheWay());
		Assert.assertEquals(2858.02, trade.getCash());		
		Assert.assertEquals(0, trade.getShares());		
		Assert.assertEquals(20562.34, trade.getTotalMoney());

		trade.nextDay();
		Assert.assertEquals("20070515", trade.getCurrentDate());
		Assert.assertEquals(10682.34, trade.getCash());		
		Assert.assertEquals(0.0, trade.getCashOnTheWay());
		Assert.assertEquals(500, trade.getShares());
		Assert.assertEquals(0, trade.getSharesOnTheWay());
		Assert.assertEquals(20017.34, trade.getTotalMoney());
	}

}