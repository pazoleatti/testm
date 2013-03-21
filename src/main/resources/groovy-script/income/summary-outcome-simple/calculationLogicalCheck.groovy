/**
 * Скрипт для заполнения контрольных полей (calculationLogicalCheck.groovy).
 * Форма "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)".
 *
 *
 * В текущей таблице нет 10й графы, следственно
 * нужно учесть что графы > 10 считаются "-1"
 *
 * @author rtimerbaev
 * @since 19.03.2013 12:00
 * @version 14 05.03.2013
 */

/**
 * Пустое ли значение.
 */
boolean isEquals(Double value1, Double value2) {
    def eps = 1e-8
    return value1 != null && (Math.abs(value1 - value2) < eps)
}

/**
 * Установить значение для 10ого столбца (если 9ый столбец = 0).
 *
 * @param row строка
 */
void setColumn9Equals0(def row) {
    if (row != null && row.rnu5Field5PrevTaxPeriod != null) {
        def result = round(row.rnu5Field5PrevTaxPeriod, 2)
        row.logicalCheck = isEquals(result, 0) ? result.toString() : 'Требуется объяснение'
    }
}

/**
 * Установить значение для 10ого столбца (если 9ый столбец < 0).
 *
 * @param row строка
 */
void setColumn9Less0(def row) {
    if (row != null && row.rnu5Field5Accepted != null && row.rnu7Field12Accepted != null) {
        def result = round(row.rnu5Field5Accepted - row.rnu7Field12Accepted, 2)
        row.logicalCheck = result < 0 ? 'Требуется объяснение' : result.toString()
    }
}

// графа 10
([10] + (13..15) + (23..25) + (30..33) + [35, 36, 39, 52, 55, 56, 63] +
        (70..73) + [76, 77, 81, 82, 88, 155] + (166..176) + [178] + (180..183)).each {
    setColumn9Equals0(formData.getDataRow('R' + it))
}
[37, 38].each {
    setColumn9Less0(formData.getDataRow('R' + it))
}

// получение данных из расходов сложных (303) для вычисления 11 графы
def formData303 = FormDataService.find(303, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
if (formData303 != null) {
    ((3..84) + [88] + (92..192)).each() {
        def row = formData.getDataRow('R' + it)

        // графа 11
        if (row.consumptionAccountNumber != null) {
            def from = 0
            def to = formData303.getDataRows().size() - 1
            ColumnRange columnRange4 = new ColumnRange('consumptionBuhSumAccountNumber', from, to)
            ColumnRange columnRange6 = new ColumnRange('consumptionBuhSumAccepted', from, to)
            row.opuSumByEnclosure2 = summIfEquals(formData303, columnRange4, row.consumptionAccountNumber, columnRange6)
        }
    }
} else {
    // если источников нет то зануляем поля в которые должны были перетянуться данные
    ((3..84) + [88] + (92..192)).each() {
        def row = formData.getDataRow('R' + it)

        // графа 11
        row.opuSumByEnclosure2 = 0
    }
}

// строка 193 графа 15
def row193 = formData.getDataRow('R193')
def bVal15 = new StringBuffer(row193.consumptionAccountNumber)
bVal15.delete(4,5)
def data193 = income101Dao.getIncome101(formData.reportPeriodId, bVal15.toString(), formData.departmentId)
row193.opuSumByOpu = data193 ? data193.creditRate: 0
// строка 193 графа 16 («графа 16»= «графа 15»-«графа 6»)
row193.difference = (row193.opuSumByOpu ?:0) - (row193.rnu7Field12Accepted ?:0)

// вычислять только для заданных строк
((3..84) + [88] + (92..192)).each() {
    def row = formData.getDataRow('R' + it)

    // графа 12
    if (row.consumptionAccountNumber != null) {
        def from = 0
        def to = formData.getDataRows().size() - 1
        ColumnRange columnRange4 = new ColumnRange('consumptionAccountNumber', from, to)
        ColumnRange columnRange8 = new ColumnRange('rnu5Field5Accepted', from, to)
        row.opuSumByTableP = summIfEquals(formData, columnRange4, row.consumptionAccountNumber, columnRange8)
    }

    // графа 13
    if (row.opuSumByEnclosure2 != null && row.opuSumByTableP != null) {
        row.opuSumTotal = row.opuSumByEnclosure2 + row.opuSumByTableP
    } else {
        row.opuSumTotal = null
    }

    // графа 14
    def bVal = new StringBuffer(row.consumptionAccountNumber)
    bVal.delete(1,8)
    def dataThisRow = income102Dao.getIncome102(formData.reportPeriodId, bVal.toString(), formData.departmentId)
    row.opuSumByOpu = dataThisRow ? dataThisRow.creditRate : 0

    // графа 15
    if (row.opuSumTotal != null && row.opuSumByOpu != null) {
        row.difference = row.opuSumTotal - row.opuSumByOpu
    } else {
        row.difference = null
    }
}
