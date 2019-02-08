package com.aplana.sbrf.taxaccounting.model;

/**
 * Признак нераспределенных сумм.
 * Признак, отображающий информацию о том из какой формы были взяты нераспределенные отрицательные значения дохода и налога.
 */
public enum NegativeSumsSign {
    /**
     * Из текущей формы
     */
    FROM_CURRENT_FORM,
    /**
     * Из предыдущей формы
     */
    FROM_PREV_FORM;

    public static NegativeSumsSign valueOf(Integer id) {
        if (id == null) {
            return null;
        }
        return values()[id];
    }
}
