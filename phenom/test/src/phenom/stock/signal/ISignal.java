package phenom.stock.signal;

import java.util.List;

import phenom.stock.signal.pricemomentum.Computable;
/**
 * Base Interface for Signal
 *
 */
public interface ISignal<T extends Comparable<? super T> & Computable> {
    void addValues(List<? extends T> v_);
    void addValue(T v_);
    void clear();
    double calculate(String symbol_, String date_);    
}
