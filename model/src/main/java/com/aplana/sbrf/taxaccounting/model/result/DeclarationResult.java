package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.NegativeSumsSign;
import com.aplana.sbrf.taxaccounting.model.TaxRefundReflectionMode;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
     * Имеются ли в форме строки в разделе 1
     */
    private boolean hasNdflPersons;
    /**
     * Ид формы
     */
    private long id;
    /**
     * Ид подразделения
     */
    private int departmentId;
    /**
     * Подразделение
     */
    private String department;
    /**
     * Ид ФЛ
     */
    private Long personId;
    /**
     * ФИО ФЛ
     */
    private String person;
    /**
     * Подписант
     */
    private String signatory;
    /**
     * Ид периода
     */
    private int reportPeriodId;
    /**
     * Период
     */
    private String reportPeriod;
    /**
     * Год периода
     */
    private Integer reportPeriodYear;
    /**
     * Ссылка на вид отчетности (период)
     */
    private Integer reportPeriodTaxFormTypeId;
    /**
     * Календарная дата начала отчетного периода (квартала)
     */
    private Date calendarStartDate;
    /**
     * Дата окончания отчетного периода
     */
    private Date endDate;
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
     * КПП для КНФ по обособленному подразделению
     */
    private List<String> kppList;
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
    private boolean adjustNegativeValues;
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
     * см {@link TaxRefundReflectionMode}
     */
    private TaxRefundReflectionMode taxRefundReflectionMode;

}
