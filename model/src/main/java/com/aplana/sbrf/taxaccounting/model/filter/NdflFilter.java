package com.aplana.sbrf.taxaccounting.model.filter;

/**
 * Общий фильтр по форме НДФЛ
 */
public class NdflFilter {
    /**
     * id формы
     */
    private long declarationDataId;

    /**
     * Значения фильтра из раздела "Реквизиты страницу РНУ НДФЛ"
     */
    private NdflPersonFilter person;

    /**
     * Значения фильтра из раздела "Сведения о доходах и НДФЛ"
     */
    private NdflPersonIncomeFilter income;

    /**
     * Значения фильтра из раздела "Сведения о вычетах"
     */
    private NdflPersonDeductionFilter deduction;

    /**
     * Значения фильтра из раздела "Сведения о доходах в виде авансовых платежей"
     */
    private NdflPersonPrepaymentFilter prepayment;

    public long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public NdflPersonFilter getPerson() {
        if (person == null) {
            person = new NdflPersonFilter();
        }
        return person;
    }

    public void setPerson(NdflPersonFilter person) {
        this.person = person;
    }

    public NdflPersonIncomeFilter getIncome() {
        if (income == null) {
            income = new NdflPersonIncomeFilter();
            income.setNdflFilter(this);
        }
        return income;
    }

    public void setIncome(NdflPersonIncomeFilter income) {
        income.setNdflFilter(this);
        this.income = income;
    }

    public NdflPersonDeductionFilter getDeduction() {
        if (deduction == null) {
            deduction = new NdflPersonDeductionFilter();
        }
        return deduction;
    }

    public void setDeduction(NdflPersonDeductionFilter deduction) {
        this.deduction = deduction;
    }

    public NdflPersonPrepaymentFilter getPrepayment() {
        if (prepayment == null) {
            prepayment = new NdflPersonPrepaymentFilter();
        }
        return prepayment;
    }

    public void setPrepayment(NdflPersonPrepaymentFilter prepayment) {
        this.prepayment = prepayment;
    }
}
