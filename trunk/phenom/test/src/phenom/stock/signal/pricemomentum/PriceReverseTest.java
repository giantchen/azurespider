package phenom.stock.signal.pricemomentum;

import java.math.BigDecimal;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

import phenom.stock.Stock;

public class PriceReverseTest {
	PriceReverse p = null;
	public PriceReverseTest() {
		p = new PriceReverse();
	}
	
	@Test
	public void testPv() {		
        Stock s = new Stock("000001.sz");
        s.setDate("20090123");        
        
        List<Stock> stocks = Stock.getStock("000001.sz", "20090101", "20091231", true);
        p.addValues(stocks);
        double d = p.calculate(s.getSymbol(), s.getDate(), 3);
        BigDecimal b = (BigDecimal.valueOf(d).setScale(4, BigDecimal.ROUND_HALF_UP));        
        Assert.assertEquals(-0.0127, b.doubleValue());
	}
	
	@After
	public void clear() {
		p.clear(true);
	}
}
