package phenom.stock.techind;

import java.util.*;
/**
 * Base Interface for Technical Indicator
 *
 */
public interface ITechnicalIndicator<T extends Comparable<? super T>> {
    void addValues(List<T> v_);
    void addValue(T v_);
    void clear();
    double calculate(String symbol_, String date_, int cycle_);
    boolean isCalculated(String symbol_, String date_, int cycle_);
}
