package com.aplana.sbrf.taxaccounting.model.consolidation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Инкапсулирует аргументы для поиска источников для консолидации
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsolidationSourceDataSearchFilter {
    /**
     * Текущая дата
     */
    private Date currentDate;
    /**
     * Дата начала периода
     */
    private Date periodStartDate;
    /**
     * Дата окончания периода
     */
    private Date periodEndDate;
    /**
     * Глубина выборки даннных в годах
     */
    private Integer dataSelectionDepth;
    /**
     * Идентификатор подразделения
     */
    private Integer departmentId;
    /**
     * Вид налоговой формы
     */
    private Integer declarationType;
    /**
     * Год консолидированной налоговой формы
     */
    private Integer consolidateDeclarationDataYear;
}
