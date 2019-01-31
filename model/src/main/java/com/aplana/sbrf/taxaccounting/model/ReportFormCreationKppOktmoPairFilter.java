package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Фильтр для запроса пар КПП/ОКТМО в форме создания отчетности
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    /**
     * Дата актуальности настроек подразделений
     */
    Date relevanceDate = new Date();
}
