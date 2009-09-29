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
        Stock s = new Stock("000001.sz"); //深发展特殊，2007年由s股票转为深发展，分红并且不进行除权计算
        List<Stock> stocks = s.getStock("20000101", "20091231", true);        
        
        //Assert.assertEquals(2248, stocks.size());
        s.setDate("20081031");
        int i = Collections.binarySearch(stocks, s);
        Stock t = stocks.get(i);
        
        //no 除权
        Assert.assertEquals("20081031",t.getDate());
        Assert.assertEquals(279.40, BigDecimal.valueOf(t.getWeightedClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        
        //最近一次除权
        s.setDate("20081030");
        i = Collections.binarySearch(stocks, s);
        t = stocks.get(i);
        Assert.assertEquals("20081030",t.getDate());
        Assert.assertEquals(289.41, BigDecimal.valueOf(t.getWeightedClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
    }
}
