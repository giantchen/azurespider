package phenom.stock.signal.pricemomentum;

import java.util.ArrayList;
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
	public void setPrices(Map<String, List<GenericComputableEntry>> s_) {
		pDelta.setPrices(s_);
	}

	@Override
	public Map<String, List<GenericComputableEntry>> getPrices() {
		return pDelta.getPrices();
	}
	
	@Override
	public double calculate(String symbol, String date, int cycle) {
		double pr = AbstractPriceMomentumSignal.INVALID_VALUE;
		validate(symbol, date, cycle);
		
		if (!isTradeDate(symbol, date)) {
			return pr;
		}
		
		if (!isCalculated(symbol, date, cycle)) {
			calculatePR(symbol, date, cycle);
		}
		List<CycleValuePair> pairs = cache.get(symbol).get(date);
		for (CycleValuePair c : pairs) {
			if (c.getCycle() == cycle) {
				pr = c.getValue();
				break;
			}
		}
		return pr;
	}

	private void calculatePR(String symbol, String date, int cycle) {
		pDelta.calculate(symbol, date, cycle);
		List<GenericComputableEntry> deltas = pDelta.getDeltas(symbol, cycle);
		Collections.sort(deltas);

		eMovingAverage.addPrices(deltas);
		eMovingAverage.calculate(symbol, date, cycle);

		double[] tmp = new double[deltas.size()];

		for (int i = 0; i < deltas.size(); i++) {
			CycleValuePair c = null;
			GenericComputableEntry s = deltas.get(i);
			tmp[i] = s.getValue();

			Map<String, List<CycleValuePair>> symbolAverages = cache.get(s
					.getSymbol());
			if (symbolAverages == null) {
				symbolAverages = new HashMap<String, List<CycleValuePair>>();
				cache.put(symbol, symbolAverages);
			}

			List<CycleValuePair> pairs = symbolAverages.get(s.getDate());
			if (pairs == null) {
				pairs = new ArrayList<CycleValuePair>();
				symbolAverages.put(s.getDate(), pairs);
			}
			
			int fi = (i + 1 - cycle >= 0) ? (i + 1 - cycle) : 0;
			int length = (i + 1 - cycle >= 0) ? cycle : (i + 1);
			
			double eMean = eMovingAverage.getAverage(symbol, date, cycle);
			double var = variance.evaluate(tmp, eMean, fi, length);
			double delta = pDelta.getDelta(symbol, date, cycle);
			
			c = new CycleValuePair(cycle, (delta - eMean) / Math.sqrt(var));
			pairs.add(c);
		}
	}
}
