package com.aplana.sbrf.taxaccounting.model;

/**
 * <b>Разделы декларации</b>
 * Created by <i><b>s.molokovskikh</i></b> on 23.10.19.
 */
public enum DeclarationDataSection {
    /**
     * Раздел 1 (Реквизиты)
     */
    SECTION1(1),
    /**
     * Раздел 2
     */
    SECTION2(2);


    private final int code;

    DeclarationDataSection(int code){
        this.code = code;
    }
}
