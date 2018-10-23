package com.aplana.sbrf.taxaccounting.model.action;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Параметры создания отчетности
 */
@Setter
@Getter
@ToString
public class CreateDeclarationReportAction {
    private Integer departmentId;
    private Integer periodId;
    private Long knfId;
    private Integer declarationTypeId;
    private boolean adjustNegativeValues;
}
