package phenom.stock.signal.pricemomentum;

import java.math.BigDecimal;
import java.util.List;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import phenom.stock.Stock;
import phenom.stock.signal.pricemomentum.Delta;

public class DeltaTest {
	Delta m = null;

    public DeltaTest() {
        m = new Delta(3);
    }

    @Before
    public void setUp() throws Exception {
    	
    }    
    
    @Test
    public void testGetDelta() {
        m.clear();
        Stock s = new Stock("000001.sz");
        s.setDate("20090123");        
        
        List<Stock> stocks = Stock.getStock("000001.sz", "20090101", "20091231", true);
        m.addPrices(stocks);
        double d = m.calculate(s.getSymbol(), s.getDate());
        BigDecimal b = (BigDecimal.valueOf(d).setScale(4, BigDecimal.ROUND_HALF_UP));        
        Assert.assertEquals(-0.0127, b.doubleValue());
    }
    
    @After
    public void clear(){
    	m.clear();
    }    
}
