package phenom.stock.signal.pricemomentum;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.moment.Variance;

import phenom.stock.signal.GenericComputableEntry;

public class PriceReverse extends AbstractPriceMomentumSignal {
	private static Variance variance = new Variance();
	private Delta pDelta = null;
	private EMovingAverage eMovingAverage = null;

	public PriceReverse(int cycle) {
		super(cycle);
		pDelta = new Delta(cycle);
		eMovingAverage = new EMovingAverage(cycle);
	}

	@Override
	public void clear(boolean clearCache_) {
		pDelta.clear(clearCache_);
		eMovingAverage.clear(clearCache_);
	}

	@Override
	public void addPrice(GenericComputableEntry s_) {
		pDelta.addPrice(s_);
	}
	
	@Override
	public void addPrices(List<? extends GenericComputableEntry> s_) {
		pDelta.addPrices(s_);
	}

	@Override
	public void setPrices(Map<String, List<GenericComputableEntry>> s_) {
		pDelta.setPrices(s_);
	}

	@Override
	public Map<String, List<GenericComputableEntry>> getPrices() {
		return pDelta.getPrices();
	}

	@Override
	public double calculate(String symbol, String date) {
		double pr = AbstractPriceMomentumSignal.INVALID_VALUE;
		validate(symbol, date, cycle);

		if (!isTradeDate(symbol, date)) {
			return pr;
		}

		if (!isCalculated(symbol, date, cycle)) {
			calculatePR(symbol, date);
		}
		Double pairs = cache.get(symbol).get(date);
		return pairs == null ? AbstractPriceMomentumSignal.INVALID_VALUE
				: pairs;
	}

	private void calculatePR(String symbol, String date) {
		pDelta.calculate(symbol, date);
		List<GenericComputableEntry> deltas = pDelta.getDeltas(symbol);
		Collections.sort(deltas);

		eMovingAverage.addPrices(deltas);
		eMovingAverage.calculate(symbol, date);

		double[] tmp = new double[deltas.size()];

		for (int i = 0; i < deltas.size(); i++) {
			GenericComputableEntry s = deltas.get(i);
			tmp[i] = s.getValue();

			Map<String, Double> symbolAverages = cache.get(s.getSymbol());
			if (symbolAverages == null) {
				symbolAverages = new HashMap<String, Double>();
				cache.put(symbol, symbolAverages);
			}

			int fi = (i + 1 - cycle >= 0) ? (i + 1 - cycle) : 0;
			int length = (i + 1 - cycle >= 0) ? cycle : (i + 1);

			double eMean = eMovingAverage.calculate(symbol, date);
			double var = variance.evaluate(tmp, eMean, fi, length);
			double delta = pDelta.calculate(symbol, date);

			symbolAverages.put(s.getDate(), (delta - eMean) / Math.sqrt(var));
		}
	}
}
