package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;

/**
 * Поля с датами, которые можно редактировать в алгоритме "Массовое изменение дат раздела 2 формы РНУ НДАФЛ".
 * https://conf.aplana.com/pages/viewpage.action?pageId=45826316
 */
public enum EditableDateField {
    /**
     * Дата начисления дохода (гр. 6).
     */
    ACCRUED("Дата начисления дохода"),
    /**
     * Дата выплаты дохода (гр. 7)
     */
    PAYOUT("Дата выплаты дохода"),
    /**
     * Дата НДФЛ (гр. 15)
     */
    TAX("Дата НДФЛ"),
    /**
     * Срок перечисления в бюджет (гр. 21)
     */
    TRANSFER("Срок перечисления");

    private final String title;

    EditableDateField(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
