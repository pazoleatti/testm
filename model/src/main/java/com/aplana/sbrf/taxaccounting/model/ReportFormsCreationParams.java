package com.aplana.sbrf.taxaccounting.model;


import com.aplana.sbrf.taxaccounting.model.action.CreateReportFormsAction;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Параметры создания отчетности
 */
@Setter
@Getter
@ToString
public class ReportFormsCreationParams implements Serializable {
    /**
     * Ид КНФ, по данным которой будут формироваться ОНФ
     */
    private Long sourceKnfId;
    /**
     * Ид типа ОНФ
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

    public ReportFormsCreationParams(CreateReportFormsAction action) {
        declarationTypeId = action.getDeclarationTypeId();
        adjustNegativeValues= action.isAdjustNegativeValues();
        taxRefundReflectionMode = action.getTaxRefundReflectionMode();
        reportFormCreationMode = action.getReportFormCreationMode();
        kppOktmoPairs = action.getKppOktmoPairs();
    }
}
