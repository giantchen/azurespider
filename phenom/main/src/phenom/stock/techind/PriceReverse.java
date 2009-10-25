package phenom.stock.techind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.moment.Variance;

import phenom.stock.GenericComputableEntry;

public class PriceReverse extends AbstractTechIndicator {
	private static Variance variance = new Variance();
	private Delta pDelta = null;
	private EMovingAverage eMovingAverage = null;

	public PriceReverse() {
		pDelta = new Delta();
		eMovingAverage = new EMovingAverage();
	}

	@Override
	public void clear(boolean clearCache_) {
		pDelta.clear(clearCache_);
		eMovingAverage.clear(clearCache_);
	}

	@Override
	public void addValue(GenericComputableEntry s_) {
		pDelta.addValue(s_);
	}

	@Override
	public void setValues(Map<String, List<GenericComputableEntry>> s_) {
		pDelta.setValues(s_);
	}

	@Override
	public Map<String, List<GenericComputableEntry>> getValues() {
		return pDelta.getValues();
	}
	
	@Override
	public double calculate(String symbol_, String date_, int cycle_) {
		double pr = 0;
		validate(symbol_, date_, cycle_);

		if (!isCalculated(symbol_, date_, cycle_)) {
			calculatePR(symbol_, date_, cycle_);
		}

		List<CycleValuePair> pairs = cache.get(symbol_).get(date_);
		for (CycleValuePair c : pairs) {
			if (c.getCycle() == cycle_) {
				pr = c.getValue();
				break;
			}
		}
		return pr;
	}

	private void calculatePR(String symbol_, String date_, int cycle_) {
		pDelta.calculate(symbol_, date_, cycle_);
		List<GenericComputableEntry> deltas = pDelta.getDeltas(symbol_, cycle_);
		Collections.sort(deltas);

		eMovingAverage.addValues(deltas);
		eMovingAverage.calculate(symbol_, date_, cycle_);

		double[] tmp = new double[deltas.size()];

		for (int i = 0; i < deltas.size(); i++) {
			CycleValuePair c = null;
			GenericComputableEntry s = deltas.get(i);
			tmp[i] = s.getValue();

			Map<String, List<CycleValuePair>> symbolAverages = cache.get(s
					.getSymbol());
			if (symbolAverages == null) {
				symbolAverages = new HashMap<String, List<CycleValuePair>>();
				cache.put(symbol_, symbolAverages);
			}

			List<CycleValuePair> pairs = symbolAverages.get(s.getDate());
			if (pairs == null) {
				pairs = new ArrayList<CycleValuePair>();
				symbolAverages.put(s.getDate(), pairs);
			}
			
			int fi = (i + 1 - cycle_ >= 0) ? (i + 1 - cycle_) : 0;
			int length = (i + 1 - cycle_ >= 0) ? cycle_ : (i + 1);
			
			double eMean = eMovingAverage.getAverage(symbol_, date_, cycle_);
			double var = variance.evaluate(tmp, eMean, fi, length);
			double delta = pDelta.getDelta(symbol_, date_, cycle_);
			
			c = new CycleValuePair(cycle_, (delta - eMean) / Math.sqrt(var));
			pairs.add(c);
		}
	}
}
