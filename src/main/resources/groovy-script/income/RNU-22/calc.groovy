/**
 * Расчеты. Алгоритмы заполнения полей формы (calc.groovy).
 * Форма "(РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования".
 *
 * TODO:
 * 		- про нумерацию пока не уточнили, пропустить
 *		- графа 17 и графа 18 уточняют
 *
 * @author rtimerbaev
 */

/** Предыдущий отчётный период. */
def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

/** (РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования (За предыдущий отчетный период). */
def formDataOld = (prevReportPeriod != null ? FormDataService.find(322, FormDataKind.PRIMARY, formDataDepartment.id, prevReportPeriod.id) : null)

/*
 * Проверка объязательных полей.
 */
def hasError = false
formData.dataRows.each { row ->
    if (!isTotal(row)) {
        def colNames = []
        // Список проверяемых столбцов
        def columns = ['contractNumber', 'contraclData', 'base', 'transactionDate', 'course',
                'interestRate', 'basisForCalc']

        columns.each {
            if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
            }
        }
        if (!colNames.isEmpty()) {
            hasError = true
            def index = row.rowNumber
            def errorMsg = colNames.join(', ')
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        }
    }
}
if (hasError) {
    return
}

/*
 * Расчеты.
 */

// удалить строку "итого"
def total = null
if (formData != null && !formData.dataRows.isEmpty()) {
    for (def row : formData.dataRows) {
        if (row.getAlias() == 'total') {
            total = row
            break
        }
    }
}
if (total != null) {
    formData.dataRows.remove(total)
}

// графа 13..20
formData.dataRows.each { row ->
    // графа 13
    row.accruedCommisCurrency = getColumn13or15or19(row, row.calcPeriodAccountingBeginDate, row.calcPeriodAccountingEndDate)

    // графа 14
    row.accruedCommisRub = round(row.accruedCommisCurrency * row.course, 2)

    // графа 15
    // TODO (Ramil Timerbaev) уточнить у Карины... совпадает с 13ой строкой
    row.commisInAccountingCurrency = getColumn13or15or19(row, row.calcPeriodAccountingBeginDate, row.calcPeriodAccountingEndDate)

    // графа 16
    row.commisInAccountingRub = round(row.commisInAccountingCurrency * row.course, 2)

    // графа 17
    // TODO (Ramil Timerbaev) уточнят
    row.accrualPrevCurrency = getSum(formDataOld, 'reportPeriodCurrency')

    // графа 18
    // TODO (Ramil Timerbaev) уточнят
    row.accrualPrevRub = getSum(formDataOld, 'reportPeriodRub')

    // графа 19
    row.reportPeriodCurrency = getColumn13or15or19(row, row.calcPeriodBeginDate, row.calcPeriodEndDate)

    // графа 20
    row.reportPeriodRub = round(row.reportPeriodCurrency * row.course, 2)
}

// графа 1
formData.dataRows.eachWithIndex { row, index ->
    row.rowNumber = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить
}

// добавить строку "итого"
def totalRow = formData.appendDataRow()
totalRow.setAlias('total')
totalRow.contractNumber = 'Итого'
// графа 13..20
['accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency', 'commisInAccountingRub',
        'accrualPrevCurrency', 'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub'].each { alias ->
    totalRow.getCell(alias).setValue(getSum(formData, alias))
}


/*
 * Вспомогалельные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Получить значение графа 13 (аналогично для графа 15 и графа 19)
 *
 * @param row строка нф
 * @param date1 дата начала
 * @param date2 дата окончания
 */
def getColumn13or15or19(def row, def date1, def date2) {
    if (date1 == null || date2 == null) {
        return 0
    }
    def division = row.basisForCalc * (date2 - date1 + 1)
    if (division == 0) {
        logger.error('Деление на ноль. Возможно неправильно выбраны даты.')
        return 0
    }
    return round((row.base * row.interestRate) / (division), 2)
}

/**
 * Получить сумму столбца.
 */
def getSum(def form, def columnAlias) {
    if (form == null) {
        return 0
    }
    return summ(form, new ColumnRange(columnAlias, 0, form.dataRows.size() - 2))
}