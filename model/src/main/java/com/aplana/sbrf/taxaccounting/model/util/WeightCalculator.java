package com.aplana.sbrf.taxaccounting.model.util;

/**
 * @author Andrey Drunk
 */
public interface WeightCalculator<T> {
    double calc(T o1, T o2);
}
