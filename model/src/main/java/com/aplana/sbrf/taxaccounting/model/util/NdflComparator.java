package com.aplana.sbrf.taxaccounting.model.util;

import java.util.Comparator;

public abstract class NdflComparator<T> implements Comparator<T> {

    protected int compareValues(Comparable v1, Comparable v2, Comparator comparator) {
        int result = 0;
        if (v1 != null && v2 != null) {
            if (comparator != null) {
                result = comparator.compare(v1, v2);
            } else {
                result = v1.compareTo(v2);
            }
        } else if (v1 == null && v2 != null) {
            return Integer.MAX_VALUE;
        } else if (v1 != null) {
            return Integer.MIN_VALUE;
        }
        return result;
    }
}
