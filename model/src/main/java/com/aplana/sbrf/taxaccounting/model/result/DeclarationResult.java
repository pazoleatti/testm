package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Модель для передачи данных о ПНФ/КНФ
 */
@Getter
@Setter
public class DeclarationResult {
    /**
     * Существует ли форма в бд
     */
    private boolean declarationDataExists;
    /**
     * Подразделение
     */
    private String department;
    /**
     * Период
     */
    private String reportPeriod;
    /**
     * Год периода
     */
    private Integer reportPeriodYear;
    /**
     * Состояние
     */
    private String state;
    /**
     * АСНУ
     */
    private String asnuName;
    /**
     * Тип КНФ
     */
    private RefBookKnfType knfType;
    /**
     * Тип налоговой форы
     */
    private String declarationFormKind;
    /**
     * Создал
     */
    private String creationUserName;
    /**
     * Дата и время создания формы
     */
    private Date creationDate;
    /**
     * Права
     */
    private long permissions;
    /**
     * Вид НФ
     */
    private int declarationType;
    /**
     * Наименование вида НФ
     */
    private String declarationTypeName;
    /**
     * КПП
     */
    private String kpp;
    /**
     * ОКТМО
     */
    private String oktmo;
    /**
     * Код налогового органа
     */
    private String taxOrganCode;
    /**
     * Состояние ЭД
     */
    private String docState;
    /**
     * Дата последних изменений данных формы
     */
    private Date lastDataModifiedDate;
    /**
     * Дата актуальности запрошенных данных (время запроса данных)
     */
    private Date actualDataDate;
    /**
     * Дата сдачи корректировки
     */
    private Date correctionDate;
    /**
     * Создана в ручную
     */
    private Boolean manuallyCreated = false;
    /**
     * Признак, показывающий необходимость корректировки отрицательных значений
     */
    private boolean isAdjustNegativeValues;

}
