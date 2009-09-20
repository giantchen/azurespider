package phenom.utils;

import junit.framework.Assert;

import org.junit.Test;


public class DateUtilTest {

	@Test
	public void testPreviousTradeDate() {
		Assert.assertEquals("20060901", DateUtil.previosTradeDate("20060904"));
	}

	@Test
	public void testPreviosTradeDate() {
		Assert.assertEquals("20060630", DateUtil.previosTradeDate("20060703"));
	}
	
	@Test
	public void testTradeDates() {
		Assert.assertEquals(171, DateUtil.tradeDates("20090101", "20091231").size());
	}

}
