package phenom.stock.signal.pricemomentum;

public interface Computable {
	public double getValue();
	public String getSymbol();
	public String getDate();
	public void setValue(double v_);
	public void setDate(String d_);
}
