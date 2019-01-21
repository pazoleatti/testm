package com.aplana.sbrf.taxaccounting.model.action;

import com.aplana.sbrf.taxaccounting.model.KppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationModeEnum;
import com.aplana.sbrf.taxaccounting.model.TaxRefundReflectionMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Параметры создания отчетности, заданные пользователем
 */
@Setter
@Getter
@ToString
public class CreateReportFormsAction implements Serializable {
    /**
     * Ид подразделения
     */
    private Integer departmentId;
    /**
     * Ид периода
     */
    private Integer periodId;
    /**
     * Ид КНФ. Должно быть задано, если не заданы параметры departmentId и periodId
     */
    private Long knfId;
    /**
     * Ид типа отчетной формы
     */
    private Integer declarationTypeId;
    /**
     * Надо ли выполнять корректировку отрицательных значений для 6-НДФЛ
     */
    private boolean adjustNegativeValues;
    /**
     * см {@link TaxRefundReflectionMode}
     */
    private TaxRefundReflectionMode taxRefundReflectionMode;
    /**
     * см {@link ReportFormCreationModeEnum}
     */
    private ReportFormCreationModeEnum reportFormCreationMode;
    /**
     * Пары КПП/ОКТМО, по которым будут создаваться ОНФ
     */
    @ToString.Exclude
    private List<KppOktmoPair> kppOktmoPairs;
}
