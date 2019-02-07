package com.aplana.sbrf.taxaccounting.model.consolidation;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Класс используемый для получения источников консолидированной формы на основе доходов
 */
@Getter @Setter
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
     * Идентификатор ПНФ к которой относится операция
     */
    private Long declarationDataId;

    /**
     * Флаг указывающий принята ли ПНФ к которой относится операция
     */
    private Boolean accepted;

    /**
     * Год отчетного периода ПНФ операции
     */
    private Integer year;

    /**
     * Код отчетного периода ПНФ операции
     */
    private String periodCode;

    /**
     * Дата корректировки
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
        this.modifiedDate = income.getModifiedDate();
        this.operationDate = income.getOperationDate();
        this.actionDate = income.getActionDate();
        this.rowType = income.getRowType();
    }
}
