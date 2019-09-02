package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Фильтр для запроса пар КПП/ОКТМО в форме создания отчетности
 */
@Getter
@Setter
public class KppOktmoPairFilter {
    /**
     * Фильтр по наименованию элемента select2
     */
    String name;
    /**
     * Ид ТерБанк настроек подразделений
     */
    Integer departmentId;
    /**
     * Ид периода формы
     */
    Integer reportPeriodId;
    /**
     * Ид КНФ, из которой выполняется создание отчетности
     */
    Long declarationId;
    /**
     * Дата актуальности настроек подразделений
     */
    Date relevanceDate;

    public KppOktmoPairFilter name(String name) {
        this.name = name;
        return this;
    }

    public KppOktmoPairFilter departmentId(Integer departmentId) {
        this.departmentId = departmentId;
        return this;
    }

    public KppOktmoPairFilter reportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
        return this;
    }

    public KppOktmoPairFilter declarationId(Long declarationId) {
        this.declarationId = declarationId;
        return this;
    }

    public KppOktmoPairFilter relevanceDate(Date relevanceDate) {
        this.relevanceDate = relevanceDate;
        return this;
    }
}
