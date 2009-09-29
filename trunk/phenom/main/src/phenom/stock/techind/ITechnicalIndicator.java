package phenom.stock.techind;

import java.util.List;
import phenom.stock.Computable;
/**
 * Base Interface for Technical Indicator
 *
 */
public interface ITechnicalIndicator<T extends Comparable<? super T> & Computable> {
    void addValues(List<? extends T> v_);
    void addValue(T v_);
    void clear();
    double calculate(String symbol_, String date_, int cycle_);
    boolean isCalculated(String symbol_, String date_, int cycle_);
}
