package com.aplana.sbrf.taxaccounting.web.widget.style.table;

import java.util.Comparator;

/**
 * Компоратор с обработкой пустый объектов
 * @author ivanov
 */
public abstract class ComparatorWithNull<T, K extends Comparable<K>> implements Comparator<T> {

    protected Integer compareWithNull(K o1, K o2){
        if (o1 == null && o2 != null) {
            return -1;
        } else if (o1 != null && o2 == null) {
            return 1;
        } else if (o1 == null && o2 == null) {
            return 0;
        } else {
            if (o1.getClass() == String.class) {
                return o1.toString().compareToIgnoreCase(o2.toString());
            }
            return o1.compareTo(o2);
        }
    }
}
