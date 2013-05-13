import com.aplana.sbrf.taxaccounting.model.DataRow

/**
 * 6.3.5.9.1 Алгоритмы заполнения полей формы
 * @author ekuvshinov
 */
//com.aplana.sbrf.taxaccounting.model.FormData formData
//com.aplana.sbrf.taxaccounting.log.Logger logger
//com.aplana.sbrf.taxaccounting.service.script.FormDataService FormDataService
//com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService reportPeriodService

// Получим формы которые могут пригодиться при вычислениях
formIncomeComplex = null
formIncomeComplexId = 302   // Сводная форма начисленных доходов (доходы сложные)
formIncomeComplex = FormDataService.find(formIncomeComplexId, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
reportPeriod = reportPeriodService.get(formData.reportPeriodId)
reportPeriodPrev = reportPeriodService.getPrevReportPeriod(reportPeriod.id)

formPrev = null
if (reportPeriodPrev != null) {
    formPrev = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodPrev.id)
}

for (row in formData.dataRows) {
    // Графа 2
    temp = new BigDecimal(0)
    if (row.getAlias() == 'type1' && formIncomeComplex != null) {
        for (rowTemp in formIncomeComplex.dataRows) {
            if (rowTemp.incomeTypeId == '13850' && rowTemp.incomeTaxSumS != null) {
                temp += rowTemp.incomeTaxSumS
            }
        }
    } else if (row.getAlias() == 'type2' && formIncomeComplex != null) {
        for (rowTemp in formIncomeComplex.dataRows) {
            if (rowTemp.incomeTypeId == '14210' && rowTemp.incomeTaxSumS != null) {
                temp += rowTemp.incomeTaxSumS
            }
        }
    } else if (row.getAlias() == 'type3' && formIncomeComplex != null) {
        for (rowTemp in formIncomeComplex.dataRows) {
            if (rowTemp.incomeTypeId == '13880' && rowTemp.incomeTaxSumS != null) {
                temp += rowTemp.incomeTaxSumS
            }
        }
    }
    // Потому что значение в 4 строке может быть введено пользователем значение по умолчанию в другом месте проставляется
    if (row.getAlias() != 'type4') {
        row.base = temp.setScale(0, BigDecimal.ROUND_HALF_UP)
    }

    // Графа 5
    row.taxIncome = (BigDecimal) ((row.base - row.incomeDeductible) * row.taxRate / 100).setScale(0, BigDecimal.ROUND_HALF_UP)

    // Графа 6
    if (reportPeriod.order != 1 && row.getAlias() == 'type4') {
        if (formPrev != null) {
            for (rowTemp in formPrev.dataRows) {
                if (rowTemp.type == row.type) {
                    row.taxPaymentPrev = rowTemp.taxPaymentPrev == null ?: 0 + rowTemp.taxPayment    // elvis Оператор используется так как находясь во 2 отчётном переиоде в первом отчётном периоде значение в граффе 6 будет null и надо коректно считать, согласованно с А.Маламут
                }
            }
        } else {
            row.taxPaymentPrev = 0
        }
    }

    // Графа 7
    if (row.type != '4') {
        row.taxPayment = 0
    }

    // Графа 8
    if (reportPeriod.order == 1 && row.getAlias() != 'type3') {
        row.creditTax = 0
    }
    if (reportPeriod.order != 1) {
        if (formPrev != null) {
            for (rowTemp in formPrev.dataRows) {
                if (rowTemp.type == row.type) {
                    row.creditTax = rowTemp.creditTax == null ?: 0 + rowTemp.taxAll
                }
            }
        } else {
            row.creditTax = 0
        }
    }

    // Графа 9
    if (row.getAlias() != 'type3') {
        BigDecimal arg1 = row.taxIncome != null ? row.taxIncome : new BigDecimal(0)
        BigDecimal arg2 = row.taxPaymentPrev != null ? row.taxPaymentPrev : new BigDecimal(0)
        BigDecimal arg3 = row.taxPayment != null ? row.taxPayment : new BigDecimal(0)
        BigDecimal arg4 = row.creditTax != null ? row.creditTax : new BigDecimal(0)
        row.taxAll = arg1.minus(arg2.minus(arg3.minus(arg4)))
    }
}