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
        
        Assert.assertEquals(2229, stocks.size());
        s.setDate("20081031");
        int i = Collections.binarySearch(stocks, s);
        Stock t = stocks.get(i);
        
        //no ��Ȩ
        Assert.assertEquals("20081031",t.getDate());
        Assert.assertEquals(8.37, t.getWeightedClosePrice());
        
        //���һ�γ�Ȩ
        s.setDate("20081030");
        i = Collections.binarySearch(stocks, s);
        t = stocks.get(i);
        Assert.assertEquals("20081030",t.getDate());
        Assert.assertEquals(8.67, BigDecimal.valueOf(t.getWeightedClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        
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
    //����ҩҵ
    public void testWeight1() {
        Stock s = new Stock("600518.sh");
        List<Stock> stocks = s.getStock("20000101", "20091231", true);        
        
        Assert.assertEquals(1985, stocks.size());        
        
        Assert.assertEquals("20010319", stocks.get(0).getDate());
        Assert.assertEquals(2.06, BigDecimal.valueOf(stocks.get(0).getWeightedClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
    }
    
    @Test
    //�������
    public void testWeight2() {
        Stock s = new Stock("600588.sh");
        int i = -1;
        Stock t = null;
        List<Stock> stocks = s.getStock("20000101", "20091231", true);        
        
        Assert.assertEquals(1960, stocks.size());      
        
        //1
        s.setDate("20090428");
        i = Collections.binarySearch(stocks, s);
        t = stocks.get(i);        
        Assert.assertEquals("20090428", t.getDate());
        Assert.assertEquals(19.65, BigDecimal.valueOf(t.getWeightedClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        
        //2
        s.setDate("20080410");
        i = Collections.binarySearch(stocks, s);
        t = stocks.get(i);        
        Assert.assertEquals("20080410", t.getDate());
        //ͬ��˳was 21.69
        Assert.assertEquals(21.67, BigDecimal.valueOf(t.getWeightedClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        
        //3
        s.setDate("20070510");
        i = Collections.binarySearch(stocks, s);
        t = stocks.get(i);        
        Assert.assertEquals("20070510", t.getDate());
        //ͬ��˳16.83
        Assert.assertEquals(16.95, BigDecimal.valueOf(t.getWeightedClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        
    }
    
    @Test
    //����֤ȯ
    public void testWeight3() {
        Stock s = new Stock("600369.sh");
        int i = -1;
        Stock t = null;
        List<Stock> stocks = s.getStock("20000101", "20091231", true);        
        
        Assert.assertEquals(1774, stocks.size());      
        
        //1
        s.setDate("20090420");//("20000428");
        i = Collections.binarySearch(stocks, s);
        t = stocks.get(i);        
        Assert.assertEquals("20090420", t.getDate());
        Assert.assertEquals(15.21, BigDecimal.valueOf(t.getWeightedClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        
        
        //2
        s.setDate("20060609");//("20000428");
        i = Collections.binarySearch(stocks, s);
        t = stocks.get(i);        
        Assert.assertEquals("20060609", t.getDate());
        Assert.assertEquals(1.49, BigDecimal.valueOf(t.getWeightedClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());      
    }
}
