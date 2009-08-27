package phenom.stock;

import java.math.BigDecimal;
import java.util.Collections;
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
        s.setDate("20081031");
        int i = Collections.binarySearch(stocks, s);
        Stock t = stocks.get(i);
        
        //no ��Ȩ
        Assert.assertEquals("20081031",t.getDate());
        Assert.assertEquals(8.37, t.getClosePrice());
        
        //���һ�γ�Ȩ
        s.setDate("20081030");
        i = Collections.binarySearch(stocks, s);
        t = stocks.get(i);
        Assert.assertEquals("20081030",t.getDate());
        Assert.assertEquals(8.67, BigDecimal.valueOf(t.getClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        
        /*//��n�˳�Ȩ
        s.setDate("20070531");
        i = Collections.binarySearch(stocks, s);
        t = stocks.get(i);
        Assert.assertEquals("20070531",t.getDate());
        Assert.assertEquals(20.03, BigDecimal.valueOf(t.getClosePrice()).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
        
        //��ʷ�״δ˳�Ȩ
        s.setDate("20000104");
        i = Collections.binarySearch(stocks, s);
        t = stocks.get(i);
        Assert.assertEquals("20000104",t.getDate());
        Assert.assertEquals(4.27, BigDecimal.valueOf(t.getClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());*/
    }
    
    @Test
    public void testWeight1() {
        Stock s = new Stock("600518.sh");
        List<Stock> stocks = s.getStock("20000101", "20091231", true);        
        
        Assert.assertEquals(1985, stocks.size());        
        
        Assert.assertEquals("20010319", stocks.get(0).getDate());
        Assert.assertEquals(2.06, BigDecimal.valueOf(stocks.get(0).getClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
    }
}
