package com.aplana.sbrf.taxaccounting.model.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Общий фильтр по форме НДФЛ
 */
@Getter
@Setter
@ToString
public class NdflFilter implements Serializable {
    /**
     * id формы
     */
    private long declarationDataId;

    /**
     * Значения фильтра из раздела "Реквизиты страницу РНУ НДФЛ"
     */
    private NdflPersonFilter person = new NdflPersonFilter(this);

    /**
     * Значения фильтра из раздела "Сведения о доходах и НДФЛ"
     */
    private NdflPersonIncomeFilter income = new NdflPersonIncomeFilter(this);

    /**
     * Значения фильтра из раздела "Сведения о вычетах"
     */
    private NdflPersonDeductionFilter deduction = new NdflPersonDeductionFilter(this);

    /**
     * Значения фильтра из раздела "Сведения о доходах в виде авансовых платежей"
     */
    private NdflPersonPrepaymentFilter prepayment = new NdflPersonPrepaymentFilter(this);

    public void setPerson(NdflPersonFilter person) {
        person.setNdflFilter(this);
        this.person = person;
    }

    public void setIncome(NdflPersonIncomeFilter income) {
        income.setNdflFilter(this);
        this.income = income;
    }

    public void setDeduction(NdflPersonDeductionFilter deduction) {
        deduction.setNdflFilter(this);
        this.deduction = deduction;
    }

    public void setPrepayment(NdflPersonPrepaymentFilter prepayment) {
        prepayment.setNdflFilter(this);
        this.prepayment = prepayment;
    }
}
