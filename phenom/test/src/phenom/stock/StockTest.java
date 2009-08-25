package phenom.stock;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class StockTest {
	

    public StockTest() {
        
    }

    @Before
    public void setUp() throws Exception {

    }    
    
    @Test
    public void testWeight() {
        Stock s = new Stock("000001.sz");
        List<Stock> stocks = s.getStock("20000101", "20091231", true);        
        
        Assert.assertEquals(2229, stocks.size());        
        
        Assert.assertEquals("20000104", stocks.get(0).getDate());
        Assert.assertEquals(10.84, stocks.get(0).getClosePrice());
    }
    
    @Test
    public void testWeight1() {
        Stock s = new Stock("600518.sh");
        List<Stock> stocks = s.getStock("20000101", "20091231", true);        
        
        Assert.assertEquals(1985, stocks.size());        
        
        Assert.assertEquals("20010319", stocks.get(0).getDate());
        Assert.assertEquals(2.06, stocks.get(0).getClosePrice());
    }
}
