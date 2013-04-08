/**
 * Получения данных из простых расходов (getDataFromSimpleOutcome.groovy).
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 * 6.1.3.8.3.1	Логические проверки.
 *
 * @author auldanov
 * @author rtimerbaev
 * @since 21.03.2013 13:30
 */

import java.text.DecimalFormat

/**
 * Копирует данные для строк 500x.
 *
 * @param fromRowA псевдоним строки нф откуда копируем данные
 * @param toRowA псевдоним текущей строки куда копируем данные
 * @param fromRowSum псевдоним строки от которой считать сумму
 * @param toRowSum псевдоним строки до которой считать сумму
 */
void copyFor500x(String fromRowA, String toRowA, String fromRowSum, String toRowSum, def fromForm) {
    // строка для заполнения из текущей нф
    toRow = formData.getDataRow(toRowA)

    if (fromForm != null) {
        // строка для копирования из другой нф
        fromRow = fromForm.getDataRow(fromRowA)

        // 12 графа
        toRow.opuSumByEnclosure3 = summ(fromForm, new ColumnRange('rnu5Field5Accepted',
                fromForm.getDataRowIndex(fromRowSum), fromForm.getDataRowIndex(toRowSum)))
        // 13 графа
        toRow.opuSumByTableP = summ(fromForm, new ColumnRange('rnu5Field5PrevTaxPeriod',
                fromForm.getDataRowIndex(fromRowSum), fromForm.getDataRowIndex(toRowSum)))
    } else {
        // 12 графа
        toRow.opuSumByEnclosure3 = 0
        // 13 графа
        toRow.opuSumByTableP = 0
    }

    // 14 графа = графа 13 - графа 7
    if (toRow.opuSumByTableP != null && toRow.consumptionBuhSumPrevTaxPeriod != null) {
        toRow.opuSumTotal = toRow.opuSumByTableP - toRow.consumptionBuhSumPrevTaxPeriod
    } else {
        toRow.opuSumTotal = null
    }
    // 16 графа = графа 12 - графа 9
    if (toRow.opuSumByEnclosure3 != null && toRow.consumptionTaxSumS != null) {
        toRow.difference = toRow.opuSumByEnclosure3 - toRow.consumptionTaxSumS
    } else {
        toRow.difference = null
    }
}

/**
 * Копирует данные для строк 700x.
 *
 * @param fromRowA псевдоним строки нф откуда копируем данные
 * @param toRowA псевдоним текущей строки куда копируем данные
 * @param fromRowSum псевдоним строки от которой считать сумму
 * @param toRowSum псевдоним строки до которой считать сумму
 */
void copyFor700x(String fromRowA, String toRowA, String fromRowSum, String toRowSum, def fromForm) {
    // строка для заполнения из текущей нф
    toRow = formData.getDataRow(toRowA)

    if (fromForm != null) {
        // строка для копирования из другой нф
        fromRow = fromForm.getDataRow(fromRowA)

        // 11 графа
        def tmp = (BigDecimal) summ(fromForm, new ColumnRange('rnu7Field12Accepted', fromForm.getDataRowIndex(fromRowSum), fromForm.getDataRowIndex(toRowSum)))
        toRow.logicalCheck = new DecimalFormat("#0.##").format(tmp).replace(',', '.')
        // 12 графа
        toRow.opuSumByEnclosure3 = summ(fromForm, new ColumnRange('rnu7Field10Sum',
                fromForm.getDataRowIndex(fromRowSum), fromForm.getDataRowIndex(toRowSum)))
    } else {
        // 11 графа
        toRow.logicalCheck = '0'
        // 12 графа
        toRow.opuSumByEnclosure3 = 0
    }

    // 13 графа = графа 11 - графа 6
    if (toRow.logicalCheck != null && toRow.consumptionBuhSumAccepted != null) {
        toRow.opuSumByTableP = toBigDecimal(toRow.logicalCheck) - toRow.consumptionBuhSumAccepted
    } else {
        toRow.opuSumByTableP = null
    }
    // 16 графа = графа 12 - графа 9
    if (toRow.opuSumByEnclosure3 != null && toRow.consumptionTaxSumS != null) {
        toRow.difference = toRow.opuSumByEnclosure3 - toRow.consumptionTaxSumS
    } else {
        toRow.difference = null
    }
}

// получение нф расходов простых
def fromForm = FormDataService.find(304, formData.kind, formData.departmentId, formData.reportPeriodId)

// 50001 - 93 строка
copyFor500x('R85', 'R93', 'R3', 'R84', fromForm)
// 50002 - 110 строка
copyFor500x('R89', 'R110', 'R88', 'R88', fromForm)
// 50011 - 140 строка
copyFor500x('R194', 'R140', 'R92', 'R193', fromForm)


// 70001 - 94 строка
copyFor700x('R86', 'R94', 'R3', 'R84', fromForm)
// 70011 - 141 строка
copyFor700x('R195', 'R141', 'R92', 'R193', fromForm)

/**
 * Получить число из строки.
 */
def toBigDecimal(String value) {
    if (value == null) {
        return new BigDecimal(0)
    }
    def result
    try {
        result = Double.parseDouble(value)
    } catch (NumberFormatException e){
        result = new BigDecimal(0)
    }
    return result
}