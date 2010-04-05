package phenom.stock.signal.technical;

import org.apache.commons.math.stat.descriptive.StatisticalSummary;

public class MeanSignal extends AbstractMeanVolatilitySignal {

	@Override
	protected double pickValue(StatisticalSummary stats) {
		return stats.getMean();
	}
}
