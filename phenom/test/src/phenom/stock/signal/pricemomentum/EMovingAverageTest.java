package phenom.stock.signal.pricemomentum;

import java.util.List; 
import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import junit.framework.Assert;

import phenom.stock.Stock;

public class EMovingAverageTest {

	EMovingAverage m = null;

    public EMovingAverageTest() {
        m = new EMovingAverage(12);
    }

    @Before
    public void setUp() throws Exception {

    }    
    
    @Test
    public void testGetAverage() {
        m.clear();
        Stock s = new Stock("000001.sz");
        s.setDate("20090123");        
        
        //data with weekends
        List<Stock> stocks = s.getStock("20090101", "20090131", false);
        m.addPrices(stocks);
        BigDecimal b = BigDecimal.valueOf(m.calculate(s.getSymbol(), s.getDate())).setScale(4, BigDecimal.ROUND_HALF_UP);        
        Assert.assertEquals(10.4853, b.doubleValue());
        
        
        s = new Stock("600518.sh", "20090220", -1);
        m.addPrices(s.getStock("20090201", "20090229", false), true);
        b = BigDecimal.valueOf(m.calculate(s.getSymbol(), s.getDate())).setScale(4, BigDecimal.ROUND_HALF_UP);        
        Assert.assertEquals(10.5890, b.doubleValue()); 
    }
    
    @Test
    public void testValid() {
    	Assert.assertEquals(false, AbstractPriceMomentumSignal.isValid(AbstractPriceMomentumSignal.INVALID_VALUE));
    	Assert.assertEquals(false, AbstractPriceMomentumSignal.isValid(Double.NaN));
    	Assert.assertEquals(true, AbstractPriceMomentumSignal.isValid(1.1));
    }
}
