package phenom.stock.signal.technical;

import org.apache.commons.math.stat.regression.SimpleRegression;

public class AlphaSignal extends AbstractAlphaBetaSignal {

	@Override
	protected double pickValue(SimpleRegression regression) {
		return regression.getIntercept();
	}

}
