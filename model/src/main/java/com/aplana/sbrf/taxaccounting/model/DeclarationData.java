package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

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
    private Long docStateId;
    /**
     * Права
     */
    private long permissions;
    /**
     * Создана вручную
     */
    private boolean manuallyCreated = false;
    /**
     * Дата последних изменений данных формы
     */
    private Date lastDataModifiedDate;
    /**
     * Признак, показывающий необходимость корректировки отрицательных значений
     */
    private boolean adjustNegativeValues;
    /**
     * см {@link TaxRefundReflectionMode}
     */
    private TaxRefundReflectionMode taxRefundReflectionMode;
    /**
     * КПП, включаемые в КНФ для обособленного подразделения (см {@link RefBookKnfType})
     */
    private Set<String> includedKpps;
    /**
     * Номер корректировки
     */
    private Integer correctionNum;
    /**
     * Нераспределенный отрицательный Доход
     */
    private BigDecimal negativeIncome;
    /**
     * Нераспределенный отрицательный Налог
     */
    private BigDecimal negativeTax;
    /**
     * Признак нераспределенных сумм
     */
    private NegativeSumsSign negativeSumsSign;
    /**
     * Проверка статуса формы.
     */
    public boolean is(State checkedState) {
        return this.state == checkedState;
    }
}