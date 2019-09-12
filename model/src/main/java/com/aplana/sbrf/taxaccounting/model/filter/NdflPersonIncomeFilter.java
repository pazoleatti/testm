package com.aplana.sbrf.taxaccounting.model.filter;

import com.aplana.sbrf.taxaccounting.model.URM;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Модель для параметров Фильтра вкладки "Сведения о доходах и НДФЛ" страницу РНУ НДФЛ
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class NdflPersonIncomeFilter implements Serializable {
    /**
     * Общий фильтр
     */
    @ToString.Exclude
    private NdflFilter ndflFilter;
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
     * Процентная ставка
     */
    private String taxRate;
    /**
     * Код дохода
     */
    private String incomeCode;
    /**
     * Признак дохода
     */
    private String incomeAttr;

    /**
     * Дата начисления с
     */
    private Date accruedDateFrom;
    /**
     * Дата начисления по
     */
    private Date accruedDateTo;
    /**
     * Дата выплаты с
     */
    private Date payoutDateFrom;
    /**
     * Дата выплаты по
     */
    private Date payoutDateTo;
    /**
     * Дата расчета НДФЛ с
     */
    private Date calculationDateFrom;
    /**
     * Дата расчета НДФЛ по
     */
    private Date calculationDateTo;
    /**
     * Срок перечисления в бюджет с
     */
    private Date transferDateFrom;
    /**
     * Срок перечисления в бюджет по
     */
    private Date transferDateTo;
    /**
     * Дата платежного поручения с
     */
    private Date paymentDateFrom;
    /**
     * Дата платежного поручения по
     */
    private Date paymentDateTo;
    /**
     * Номер платежного поручения
     */
    private String numberPaymentOrder;
    /**
     * Условие для возвращенного налога
     */
    private FilterCondition taxRefundCondition;

    /**
     * Данные УРМ
     */
    private List<URM> urmList = new ArrayList<>();

    /**
     * Номер строки
     */
    private String rowNum;
    /**
     * Идентификатор строки
     */
    private String id;
    /**
     * Дата редактирования с
     */
    private Date modifiedDateFrom;
    /**
     * Дата редактирования по
     */
    private Date modifiedDateTo;
    /**
     * Обновил
     */
    private String modifiedBy;
    /**
     * АСНУ
     */
    private List<RefBookAsnu> asnu;

    public NdflPersonIncomeFilter(NdflFilter ndflFilter) {
        this.ndflFilter = ndflFilter;
    }
}
