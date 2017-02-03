package com.aplana.sbrf.taxaccounting.model.util;

/**
 * @author Andrey Drunk
 */
public interface WeigthCalculator<T> {
    double calc(T o1, T o2);
}
