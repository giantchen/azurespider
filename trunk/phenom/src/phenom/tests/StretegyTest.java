package phenom.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import phenom.Strategy;

public class StretegyTest extends Strategy {

	public StretegyTest() {
		super(0.001, // 佣金为千分之一
				5, // 最少5元佣金
				0.001, // 卖出印花税千分之一
				0.0 // 买入印花税无
		);
	}

	@Before
	public void before() {
		cash = 10000;
		cashOnTheWay = 0.0;
		shares = 0;
		sharesOnTheWay = 0;

		dates = new String[] { "20090105", "20090106", "20090107", "20090108",
				"20090109" };
		openPrices = new double[] { 8.0, 8.0, 8.0, 8.0, 8.0, 8.0 };
	}

	@Test
	public void testNoop() {
		for (int day = 0; day < dates.length; ++day) {
			assertEquals(10000, cash, 0.001);
			assertEquals(shares, 0);
			endDay(Operation.Noop, day, 0);
		}
	}

	@Test
	public void testOneBuyOneSell() {
		assertEquals(10000, cash, 0.001);
		assertEquals(shares, 0);

		endDay(Operation.Buy, 0, 1000);

		assertEquals(1992, cash, 0.001);
		assertEquals(1000, sharesOnTheWay);

		endDay(Operation.Noop, 1, 0);

		assertEquals(1992, cash, 0.001);
		assertEquals(1000, shares);

		endDay(Operation.Sell, 2, 1000);
		assertEquals(1992, cash, 0.001);
		assertEquals(7984, cashOnTheWay, 0.001);
		assertEquals(0, shares);

		endDay(Operation.Noop, 3, 0);

		assertEquals(9976, cash, 0.001);
		assertEquals(0, cashOnTheWay, 0.001);
		assertEquals(0, shares);

		endDay(Operation.Noop, 4, 0);
	}
}
