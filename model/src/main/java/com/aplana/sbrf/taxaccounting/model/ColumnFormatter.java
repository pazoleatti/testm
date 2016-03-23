package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Используюется для форматирования значения ячеек
 * Сериализация данного класса переопределена для GWT
 */
public class ColumnFormatter implements Serializable {
    private static final long serialVersionUID = 1L;
    public String format(String valueToFormat) {
        return valueToFormat;
    }
}
