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
        Stock s = new Stock("000001.sz"); //�չ���⣬2007����s��ƱתΪ�չ���ֺ첢�Ҳ����г�Ȩ����
        List<Stock> stocks = s.getStock("20000101", "20091231", true);        
        
        //Assert.assertEquals(2248, stocks.size());
        s.setDate("20081031");
        int i = Collections.binarySearch(stocks, s);
        Stock t = stocks.get(i);
        
        //no ��Ȩ
        Assert.assertEquals("20081031",t.getDate());
        Assert.assertEquals(279.40, BigDecimal.valueOf(t.getWeightedClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        
        //���һ�γ�Ȩ
        s.setDate("20081030");
        i = Collections.binarySearch(stocks, s);
        t = stocks.get(i);
        Assert.assertEquals("20081030",t.getDate());
        Assert.assertEquals(289.41, BigDecimal.valueOf(t.getWeightedClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
    }
}
