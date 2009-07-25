package phenom;

import org.junit.Before;
import org.junit.Test;

public class SampleStrategy extends Strategy {

	static final String symbol = "600001.sh";
	static final double initCash = 100000;

	@Before
	public void before() {
		super.before(symbol, initCash);
	}

	@Test
	public void OneDayStrategy() {
		for (int day = 0; day < dates.length; ++day) {
			Operation op;
			int s;
			if (day == 0)
			{
				op = Operation.Buy;
				s = 10000;
			}
			else if (day == dates.length - 3)
			{
				// sell all on the last day
				op = Operation.Sell;
				s = shares;
			}
			else 
			{
				op = Operation.Noop;
				s = 0;
			}
			endDay(op, day, s);
		}
	}

}
