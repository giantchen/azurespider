package phenom.stock;

import java.util.Comparator;

public class NameValuePair implements Comparable<NameValuePair>{
	private double value;
	private String name;

	public NameValuePair(String name, double value) {		
		this.value = value;
		this.name = name;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int compareTo(NameValuePair np) {
		return name.compareTo(np.getName());
	}

	public final static Comparator<NameValuePair> VALUE_COMPARATOR = new Comparator<NameValuePair>() {
		@Override
		public int compare(NameValuePair o1, NameValuePair o2) {
			if(o1.getValue() > o2.getValue()) {
				return 1;
			} else {
				return o1.getValue() == o2.getValue() ? 0 : -1;
			}
		}
	};
}
