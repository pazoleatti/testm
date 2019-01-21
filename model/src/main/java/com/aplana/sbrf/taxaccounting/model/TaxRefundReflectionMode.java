package com.aplana.sbrf.taxaccounting.model;

/**
 * Режим отражения в 6-НДФЛ суммы возвращенного налога (строка 090 Раздела 1 в 6-НДФЛ).
 */
public enum TaxRefundReflectionMode {
    NORMAL, // Показывать в строке 090 Раздела 1
    AS_NEGATIVE_WITHHOLDING_TAX, // Учитывать возврат как отрицательное удержание в Разделе 2
    NONE; // Не учитывать

    public int getId() {
        return ordinal() + 1;
    }

    public static TaxRefundReflectionMode valueOf(int id) {
        return values()[id - 1];
    }
}
