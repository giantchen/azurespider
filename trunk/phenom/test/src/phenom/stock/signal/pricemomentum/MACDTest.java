package phenom.stock.signal.pricemomentum;

import java.math.BigDecimal;
import java.util.List;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import phenom.stock.Stock;

public class MACDTest {
	MACD m = null;
	
	public MACDTest() {
		m = new MACD(MACD.DEFAULT_LONG_CYCLE + MACD.DEFAULT_SHORT_CYCLE);
	}
	
	@Before
	public void setUp() throws Exception {
		Stock s = new Stock("000001.sz");
        s.setDate("20090123");        
        List<Stock> stocks = s.getStock("20090101", "20090131", false);
		m.addPrices(stocks);        
	}
	
	@Test
	public void testGetDiff() {
		Stock s = new Stock("000001.sz");
		s.setDate("20090123");
		m.calculate(s);		
		int days = MACD.DEFAULT_LONG_CYCLE + MACD.DEFAULT_SHORT_CYCLE;		
		double d = m.getDIFF(s.getSymbol(), s.getDate(), days);
		
		Assert.assertEquals(0.4843, BigDecimal.valueOf(d).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());		
	}
	
	@Test
	public void testGetDEA() {
		Stock s = new Stock("000001.sz");
		s.setDate("20090123");
		m.calculate(s);		
		//int days = MACD.DEFAULT_LONG_CYCLE + MACD.DEFAULT_SHORT_CYCLE;		
		double d = m.getDEA(s.getSymbol(), s.getDate(), MACD.DEFAULT_DEA_CYCLE);
		
		Assert.assertEquals(0.2821, BigDecimal.valueOf(d).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
	}
	
	@After
	public void tearDown() throws Exception {
		m.clear(true);
	}
}
