/**
 * Расчеты. Алгоритмы заполнения полей формы (calc.groovy).
 * Форма "(РНУ-38.2) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 2".
 *
 * @author rtimerbaev
 */

/** (РНУ-38.1) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 1. */
def formDataRNU_38_1 = FormDataService.find(334, FormDataKind.PRIMARY, formDataDepartment.id, formData.reportPeriodId)
if (formDataRNU_38_1 == null) {
    /*
     // TODO (Ramil Timerbaev) костыль, потом убать
     formData.dataRows.each { row ->
         row.amount = 1
         row.incomePrev = 1
         row.incomeShortPosition = 1
         row.totalPercIncome = row.incomePrev + row.incomeShortPosition
     }
     // конец
     */
    return
}

def totalRow = formDataRNU_38_1.getDataRow('total')

/*
 * Расчеты.
 */

formData.dataRows.each { row ->

    // графа 1
    row.amount = totalRow.amount

    // графа 2
    row.incomePrev = totalRow.incomePrev

    // графа 3
    row.incomeShortPosition = totalRow.incomeShortPosition

    // графа 4
    row.totalPercIncome = row.incomePrev + row.incomeShortPosition
}