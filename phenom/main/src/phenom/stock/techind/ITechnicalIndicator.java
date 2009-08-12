package phenom.stock.techind;

import java.util.*;
/**
 * TODO: type comment.
 *
 */
public interface ITechnicalIndicator<T extends Comparable<? super T>> {
    void addValues(List<T> v_);
    void addValue(T v_);
}
