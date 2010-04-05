package phenom.stock.signal.technical;

import org.apache.commons.math.stat.descriptive.StatisticalSummary;

public class VolatilitySignal extends AbstractMeanVolatilitySignal {

	@Override
	protected double pickValue(StatisticalSummary stats) {
		return stats.getStandardDeviation();
	}
}
