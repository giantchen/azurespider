package phenom.stock.signal.pricemomentum;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import phenom.stock.signal.GenericComputableEntry;
import phenom.stock.signal.ISignal;

public abstract class AbstractPriceMomentumSignal implements ISignal {
	public static double INVALID_VALUE = Double.NaN;
	public int DEFAULT_CYCLE = 5;

	// key = symbol, value(key = date, List(value = CycleValuePair))
	protected Map<String, Map<String, List<CycleValuePair>>> cache = new HashMap<String, Map<String, List<CycleValuePair>>>();
	// protected List<Stock> values = new ArrayList<Stock>();
	protected Map<String, List<GenericComputableEntry>> values = new HashMap<String, List<GenericComputableEntry>>();

	@Override
	public double calculate(String symbol, String date) {
		return calculate(symbol, date, getDefaultCycle());
	}

	public abstract double calculate(String symbol_, String date_, int cycle_);

	public int getDefaultCycle() {
		return DEFAULT_CYCLE;
	}

	public void addPrices(List<? extends GenericComputableEntry> s_) {
		addPrices(s_, false);
	}

	public void addPrices(List<? extends GenericComputableEntry> s_,
			boolean sort_) {
		for (GenericComputableEntry s : s_) {
			List<GenericComputableEntry> st = values.get(s.getSymbol());
			if (st == null) {
				st = new ArrayList<GenericComputableEntry>();
				values.put(s.getSymbol(), st);
			}
			st.add(s);
		}

		if (sort_) {
			for (String sb : values.keySet()) {
				Collections.sort(values.get(sb));
			}
		}
	}

	public void addPrice(GenericComputableEntry s_) {
		addPrice(s_, false);
	}

	/**
	 * 
	 * @param s_
	 * @param sort_
	 *            if already sorting, should parse false
	 */
	public void addPrice(GenericComputableEntry s_, boolean sort_) {
		List<GenericComputableEntry> s = values.get(s_.getSymbol());
		if (s == null) {
			s = new ArrayList<GenericComputableEntry>();
			values.put(s_.getSymbol(), s);
		}
		s.add(s_);
		if (sort_) {
			Collections.sort(s);
		}
	}

	public void clear() {
		clear(true);
	}

	public void clear(boolean clearCache_) {
		values.clear();
		if (clearCache_) {
			cache.clear();
		}
	}

	public boolean isCalculated(String symbol_, String date_, int cycle_) {
		boolean flag = false;

		Map<String, List<CycleValuePair>> m = cache.get(symbol_);
		if (m != null) {
			List<CycleValuePair> l = m.get(date_);
			if (l != null) {
				for (CycleValuePair c : l) {
					if (c.getCycle() == cycle_) {
						flag = true;
						break;
					}
				}
			}
		}

		return flag;
	}

	/**
	 * used when calculate 1 tech indicator which depends on another indicator
	 * no need to keep 2 copies
	 * 
	 * @param s_
	 */
	protected void setPrices(Map<String, List<GenericComputableEntry>> s_) {
		this.values = s_;
	}

	protected Map<String, List<GenericComputableEntry>> getPrices() {
		return values;
	}

	protected void validate(String symbol_, String date_, int cycle_) {
		String s = null;
		if (symbol_ == null || date_ == null || cycle_ <= 0) {
			s = "Symbol and Date must be set up, Days must be positive";
		} else if (getPrices() == null || getPrices().size() == 0) {
			s = "Value is not initialized";
		}

		if (s != null) {
			throw new RuntimeException(s);
		}
	}

	public static boolean isValid(double val_) {
		return !Double.isNaN(val_);
	}

	public boolean isTradeDate(String symbol, String date) {
		if (Collections.binarySearch(values.get(symbol), new GenericComputableEntry(symbol, date, -1)) < 0) {
			return false;
		} else {
			return true;
		}
	}

	public static class CycleValuePair {
		private double value;
		private int cycle;

		/**
		 * @param value
		 * @param cycle
		 */
		public CycleValuePair(int cycle, double value) {
			super();
			this.value = value;
			this.cycle = cycle;
		}

		public double getValue() {
			return this.value;
		}

		public int getCycle() {
			return this.cycle;
		}
	}
}
