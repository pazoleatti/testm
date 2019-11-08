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

    /**
     * см {@link ReportTypeModeEnum}
     */
    private ReportTypeModeEnum reportTypeMode;

    /**
     * Выбранный номер справки в xml-файле на основании которой нужно сформировать аннулирующую 2-НДФЛ в ручном режиме
     */
    private Integer selectedSprNum;

    /**
     * Выбранный номер ОНФ на основании которого нужно сформировать аннулирующую 2-НДФЛ в ручном режиме
     */
    private Long declarationDataId;

    /**
     * Идентификатор отчетного периода
     */
    private int reportPeriodId;

    /**
     * Идентификатор тер.банка
     */
    private int departmentId;


    /**
     * Выбранное ФИО,  ИНН РФ, № ДУЛ для которого нужно сформировать аннулирующую 2-НДФЛ в ручном режиме
     */
    private String lastName;
    private String firstName;
    private String middleName;
    private String innNp;
    private String idDocNumber;

    public ReportFormsCreationParams(CreateReportFormsAction action) {
        declarationTypeId = action.getDeclarationTypeId();
        adjustNegativeValues= action.isAdjustNegativeValues();
        taxRefundReflectionMode = action.getTaxRefundReflectionMode();
        reportFormCreationMode = action.getReportFormCreationMode();
        kppOktmoPairs = action.getKppOktmoPairs();
        reportTypeMode = action.getReportTypeMode();
        selectedSprNum = action.getSelectedSprNum();
        declarationDataId = action.getDeclarationDataId();
        lastName = action.getLastName();
        firstName = action.getFirstName();
        middleName = action.getMiddleName();
        innNp = action.getInnNp();
        idDocNumber = action.getIdDocNumber();
        reportPeriodId = action.getPeriodId();
        departmentId = action.getDepartmentId();
    }
}
