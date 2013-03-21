/**
 * Расчет (основные графы) (calculationBasicSum.groovy).
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * @since 19.03.2013 14:00
 */

/**
 * Функция суммирует диапазон строк определенного столбца и вставляет указаную сумму в последнюю ячейку.
 */
def setSum(String rowFromAlias, String rowToAlias, String columnAlias) {
    int rowFrom = formData.getDataRowIndex(rowFromAlias)
    int rowTo = formData.getDataRowIndex(rowToAlias)
    def sumRow = formData.getDataRow(rowToAlias)

    sumRow[columnAlias] = summ(formData, new ColumnRange(columnAlias, rowFrom + 1, rowTo - 1))
}

// A1
['consumptionBuhSumAccepted', 'consumptionTaxSumS'].each {
    setSum('R2', 'R95', it)
}

// Б
['consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each {
    setSum('R96', 'R111', it)
}

// А2
['consumptionBuhSumAccepted', 'consumptionTaxSumS'].each {
    setSum('R113', 'R142', it)
}

// д
setSum('R143', 'R148', 'consumptionTaxSumS')