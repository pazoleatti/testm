package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Налоговая/Отчетная форма.
 */
@Getter
@Setter
@ToString
public class DeclarationData extends IdentityObject<Long> implements SecuredEntity {

    /**
     * Ид макета
     */
    private int declarationTemplateId;
    /**
     * Тип КНФ
     */
    private RefBookKnfType knfType;
    /**
     * Ид подразделения
     */
    private int departmentId;
    /**
     * Ид периода для подразделения
     */
    private Integer departmentReportPeriodId;
    /**
     * Ид периода
     */
    private int reportPeriodId;
    /**
     * Налоговый орган
     */
    private String taxOrganCode;
    /**
     * КПП
     */
    private String kpp;
    /**
     * Код ОКТМО
     */
    private String oktmo;
    /**
     * Идентификатор АСНУ
     */
    private Long asnuId;
    /**
     * Комментарий к НФ
     */
    private String note;
    /**
     * Имя файла
     */
    private String fileName;
    /**
     * Статус налоговой формы
     */
    private State state;
    /**
     * Статус ЭД
     */
    private Long docState;
    /**
     * Права
     */
    private long permissions;
    /**
     * Создана в ручную
     */
    private Boolean manuallyCreated = false;
    /**
     * Дата последних изменений данных формы
     */
    private Date lastDataModifiedDate;
    /**
     * Признак, показывающий необходимость корректировки отрицательных значений
     */
    private boolean isAdjustNegativeValues;

}