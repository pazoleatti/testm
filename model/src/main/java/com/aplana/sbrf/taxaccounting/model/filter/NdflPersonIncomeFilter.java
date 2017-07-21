package com.aplana.sbrf.taxaccounting.model.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Модель для параметров Фильтра вкладки "Сведения о доходах и НДФЛ" страницу РНУ НДФЛ
 */
@Getter
@Setter
@ToString
public class NdflPersonIncomeFilter {
    /**
     * id формы
     */
    private long declarationDataId;
    /**
     * ИНП
     */
    private String inp;
    /**
     * id операции
     */
    private String operationId;
    /**
     * КПП
     */
    private String kpp;
    /**
     * ОКТМО
     */
    private String oktmo;
    /**
     * Код дохода
     */
    private String incomeCode;
    /**
     * Признак дохода
     */
    private String incomeAttr;
    /**
     * Процентная ставка
     */
    private String taxRate;
    /**
     * Номер платежного поручения
     */
    private String numberPaymentOrder;
    /**
     * Срок перечисления в бюджет с
     */
    private Date transferDateFrom;
    /**
     * Срок перечисления в бюджет по
     */
    private Date transferDateTo;
    /**
     * Дата расчета НДФЛ с
     */
    private Date calculationDateFrom;
    /**
     * Дата расчета НДФЛ по
     */
    private Date calculationDateTo;
    /**
     * Дата платежного поручения с
     */
    private Date paymentDateFrom;
    /**
     * Дата платежного поручения по
     */
    private Date paymentDateTo;
}
