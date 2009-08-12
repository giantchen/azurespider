package phenom.stock.techind;

import java.util.List;
import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import junit.framework.Assert;

import phenom.stock.techind.SMovingAverage;
import phenom.stock.Stock;
import phenom.stock.Cycle;

public class SMovingAverageTest {

	SMovingAverage m = null;

    public SMovingAverageTest() {
        m = new SMovingAverage();
    }

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetAverage() {
        m.clear();
        Stock s = new Stock("000001.sz");
        s.setDate("20090102");        
        
        //no records
        List<Stock> stocks = s.getStock("20090101", "20090102", false);
        m.addValues(stocks);
        //BigDecimal b = BigDecimal.valueOf(m.getAverage(s, Cycle.FIVE_DAYS)).setScale(4, BigDecimal.ROUND_HALF_UP);        
        Assert.assertEquals(Double.NaN, m.getAverage(s, Cycle.FIVE_DAYS));      
    }
    
    @Test
    public void testGetAverage1() {
        m.clear();
        Stock s = new Stock("000001.sz");
        s.setDate("20090131");        
        
        //data with weekends
        List<Stock> stocks = s.getStock("20090101", "20090131", false);
        m.addValues(stocks);
        BigDecimal b = BigDecimal.valueOf(m.getAverage(s, Cycle.THIRTY_DAYS)).setScale(4, BigDecimal.ROUND_HALF_UP);        
        Assert.assertEquals(10.5060, b.doubleValue());
    }
}
