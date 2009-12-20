package phenom.stock.signal.pricemomentum;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import phenom.stock.signal.GenericComputableEntry;
import phenom.stock.signal.ISignal;
import phenom.stock.signal.SignalConstants;

public abstract class AbstractPriceMomentumSignal implements ISignal {
	public static double INVALID_VALUE = SignalConstants.INVALID_VALUE;
	public int cycle = 5;

	// key = symbol, value(key = date, List(value = double))
	protected Map<String, Map<String, Double>> cache = new HashMap<String, Map<String, Double>>();
	// protected List<Stock> values = new ArrayList<Stock>();
	protected Map<String, List<GenericComputableEntry>> prices = new HashMap<String, List<GenericComputableEntry>>();

	public AbstractPriceMomentumSignal(int cycle) {
		this.cycle = cycle;
	}

	@Override
	public abstract double calculate(String symbol, String date);

	public int getCycle() {
		return cycle;
	}

	public void addPrices(List<? extends GenericComputableEntry> s_) {
		addPrices(s_, false);
	}

	public void addPrices(List<? extends GenericComputableEntry> s_,
			boolean sort_) {
		for (GenericComputableEntry s : s_) {
			List<GenericComputableEntry> st = prices.get(s.getSymbol());
			if (st == null) {
				st = new ArrayList<GenericComputableEntry>();
				prices.put(s.getSymbol(), st);
			}
			st.add(s);
		}

		if (sort_) {
			for (String sb : prices.keySet()) {
				Collections.sort(prices.get(sb));
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
		List<GenericComputableEntry> s = prices.get(s_.getSymbol());
		if (s == null) {
			s = new ArrayList<GenericComputableEntry>();
			prices.put(s_.getSymbol(), s);
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
		prices.clear();
		if (clearCache_) {
			cache.clear();
		}
	}

	public boolean isCalculated(String symbol_, String date_, int cycle_) {
		boolean flag = false;
		Map<String, Double> m = cache.get(symbol_);
		if (m != null) {
			if (m.get(date_) != null)
				flag = true;
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
		this.prices = s_;
	}

	protected Map<String, List<GenericComputableEntry>> getPrices() {
		return prices;
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
		if (Collections.binarySearch(getPrices().get(symbol),
				new GenericComputableEntry(symbol, date, -1)) < 0) {
			return false;
		} else {
			return true;
		}
	}
}
