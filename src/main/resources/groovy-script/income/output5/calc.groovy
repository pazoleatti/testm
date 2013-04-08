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
    temp = 0
    if (row.getAlias() == 'type1') {
        for (rowTemp in formIncomeComplex.dataRows) {
            if (rowTemp.incomeTypeId == 13850 && rowTemp.incomeTaxSumS != null) {
                temp += rowTemp.incomeTaxSumS
            }
        }
        row.base = temp
    } else if (row.getAlias() == 'type2') {
        for (rowTemp in formIncomeComplex.dataRows) {
            if (rowTemp.incomeTypeId == 14210 && rowTemp.incomeTaxSumS != null) {
                temp += rowTemp.incomeTaxSumS
            }
        }
        row.base = temp
    } else if (row.getAlias() == 'type3') {
        for (rowTemp in formIncomeComplex.dataRows) {
            if (rowTemp.incomeTypeId == 13880 && rowTemp.incomeTaxSumS != null) {
                temp += rowTemp.incomeTaxSumS
            }
        }
        row.base = temp
    }


    // Графа 5
    row.taxIncome = (row.base - row.incomeDeductible) * row.taxRate

    // Графа 6
    row.taxPaymentPrev = 0
    if (reportPeriod.order != 1 && formPrev != null) {
        for (rowTemp in formPrev.dataRows) {
            if (rowTemp.type == row.type) {
                row.taxPaymentPrev = rowTemp.taxPaymentPrev + rowTemp.taxPayment
            }
        }
    }

    // Графа 7
    if (row.type != 4) {
        row.taxPayment = 0
    }

    // Графа 8
    row.creditTax = 0
    if (reportPeriod.order != 1 && formPrev != null) {
        for (rowTemp in formPrev.dataRows) {
            if (rowTemp.type == row.type) {
                row.creditTax = rowTemp.creditTax + rowTemp.taxAll
            }
        }
    }

    // Графа 9
    row.taxAll = row.taxIncome - row.taxPaymentPrev - row.taxPayment - row.creditTax
}