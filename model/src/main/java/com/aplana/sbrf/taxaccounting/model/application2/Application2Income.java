package com.aplana.sbrf.taxaccounting.model.application2;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;

/**
 * Класс представляющий объект доходов для операции "Создание приложения 2"
 */
public class Application2Income extends NdflPersonIncome {
    /**
     * Идентификатор физлица в справочнике физлиц
     */
    private Long refBookPersonId;

    public Application2Income(NdflPersonIncome income) {
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

    public Long getRefBookPersonId() {
        return refBookPersonId;
    }

    public void setRefBookPersonId(Long refBookPersonId) {
        this.refBookPersonId = refBookPersonId;
    }
}
