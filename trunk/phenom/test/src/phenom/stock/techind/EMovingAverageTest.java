package phenom.stock.techind;

import java.util.List; 
import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import junit.framework.Assert;

import phenom.stock.Stock;
import phenom.stock.signal.pricemomentum.AbstractPriceMomentumSignal;
import phenom.stock.signal.pricemomentum.EMovingAverage;

public class EMovingAverageTest {

	EMovingAverage m = null;

    public EMovingAverageTest() {
        m = new EMovingAverage();
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
        m.addValues(stocks);
        BigDecimal b = BigDecimal.valueOf(m.getAverage(s, 12)).setScale(4, BigDecimal.ROUND_HALF_UP);        
        Assert.assertEquals(10.4853, b.doubleValue());
        
        
        s = new Stock("600518.sh", "20090220", -1);
        m.addValues(s.getStock("20090201", "20090229", false), true);
        b = BigDecimal.valueOf(m.getAverage(s, 12)).setScale(4, BigDecimal.ROUND_HALF_UP);        
        Assert.assertEquals(10.5890, b.doubleValue()); 
    }
    
    @Test
    public void testValid() {
    	Assert.assertEquals(false, AbstractPriceMomentumSignal.isValid(AbstractPriceMomentumSignal.INVALID_VALUE));
    	Assert.assertEquals(false, AbstractPriceMomentumSignal.isValid(Double.NaN));
    	Assert.assertEquals(true, AbstractPriceMomentumSignal.isValid(1.1));
    }
}
