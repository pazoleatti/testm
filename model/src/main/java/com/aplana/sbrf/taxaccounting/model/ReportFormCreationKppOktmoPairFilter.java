package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Фильтр для запроса пар КПП/ОКТМО в форме создания отчетности
 */
@Getter
@Setter
public class ReportFormCreationKppOktmoPairFilter {
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
}
