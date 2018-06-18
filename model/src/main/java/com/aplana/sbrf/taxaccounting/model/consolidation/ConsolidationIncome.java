package com.aplana.sbrf.taxaccounting.model.consolidation;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;

import java.util.Date;

/**
 * Класс используемый для получения источников консолидированной формы на основе доходов
 */
public class ConsolidationIncome extends NdflPersonIncome {
    /**
     * ИНП физлица к которому относится доход
     */
    private String inp;

    /**
     * Идентификатор АСНУ ПНФ к которой относится операция
     */
    private Long asnuId;

    /**
     *  Идентификатор ПНФ к которой относится операция
     */
    private Long declarationDataId;

    /**
     *  Флаг указывающий принята ли ПНФ к которой относится операция
     */
    private Boolean accepted;

    /**
     *  Год отчетного периода ПНФ операции
     */
    private Integer year;

    /**
     *  Код отчетного периода ПНФ операции
     */
    private String periodCode;

    /**
     *  Дата корректировки
     */
    private Date correctionDate;

    public ConsolidationIncome() {
    }

    public ConsolidationIncome(NdflPersonIncome income) {
        this.id = income.getId();
        this.sourceId = income.getId();
        this.rowNum = income.getRowNum();
        this.operationId = income.getOperationId();
        this.ndflPersonId = income.getNdflPersonId();
        this.incomeCode = income.getIncomeCode();
        this.incomeType = income.getIncomeType();
        this.incomeAccruedDate = income.getIncomeAccruedDate();
        this.incomePayoutDate = income.getIncomePayoutDate();
        this.oktmo = income.getOktmo();
        this.kpp = income.getKpp();
        this.incomeAccruedSumm = income.getIncomeAccruedSumm();
        this.incomePayoutSumm = income.getIncomePayoutSumm();
        this.totalDeductionsSumm = income.getTotalDeductionsSumm();
        this.taxBase = income.getTaxBase();
        this.taxRate = income.getTaxRate();
        this.taxDate = income.getTaxDate();
        this.calculatedTax = income.getCalculatedTax();
        this.withholdingTax = income.getWithholdingTax();
        this.notHoldingTax = income.getNotHoldingTax();
        this.overholdingTax = income.getOverholdingTax();
        this.refoundTax = income.getRefoundTax();
        this.taxTransferDate = income.getTaxTransferDate();
        this.paymentNumber = income.getPaymentNumber();
        this.paymentDate = income.getPaymentDate();
        this.taxSumm = income.getTaxSumm();
    }

    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
        this.inp = inp;
    }

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    public Long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getPeriodCode() {
        return periodCode;
    }

    public void setPeriodCode(String periodCode) {
        this.periodCode = periodCode;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }
}
