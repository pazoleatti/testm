package com.aplana.sbrf.taxaccounting.model;

/**
 * Перечисление для режима отражения суммы возвращенного налога в строке 090 разделе 1 отчетности 6-НДФЛ.
 */
public enum TaxRefundReflectMode {
    NORMAL,
    AS_NEGATIVE_WITHHOLDING_TAX
}
