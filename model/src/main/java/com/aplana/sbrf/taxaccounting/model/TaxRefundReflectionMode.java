package com.aplana.sbrf.taxaccounting.model;

/**
 * Перечисление для режима отражения суммы возвращенного налога в строке 090 разделе 1 отчетности 6-НДФЛ.
 */
public enum TaxRefundReflectionMode {
    NORMAL, // Показывать в строке 090 Раздела 1
    AS_NEGATIVE_WITHHOLDING_TAX // Учитывать возврат как отрицательное удержание в Разделе 2
}
