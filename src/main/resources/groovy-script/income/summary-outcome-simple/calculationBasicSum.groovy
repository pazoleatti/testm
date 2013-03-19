/**
 * Вычисление сумм (calculationBasicSum.groovy).
 * Форма "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)".
 * @since 15.03.2013 15:00
 */

/**
 * Функция суммирует диапазон строк определенного столбца и вставляет указаную сумму в последнюю ячейку.
 * Форма "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)".
 */

def setSum(String rowFromAlias, String rowToAlias, String columnAlias) {
    int rowFrom = formData.getDataRowIndex(rowFromAlias)
    int rowTo = formData.getDataRowIndex(rowToAlias)
    def sumRow = formData.getDataRow(rowToAlias)

    sumRow[columnAlias] = summ(formData, new ColumnRange(columnAlias, rowFrom + 1, rowTo - 1))
}


// Расчет сумм

['rnu5Field5Accepted', 'rnu5Field5PrevTaxPeriod'].each {
    setSum('R2', 'R85', it)
}

['rnu7Field10Sum', 'rnu7Field12Accepted'].each {
    setSum('R2', 'R86', it)
}

['rnu5Field5Accepted', 'rnu5Field5PrevTaxPeriod'].each {
    setSum('R87', 'R89', it)
}

['rnu5Field5Accepted', 'rnu5Field5PrevTaxPeriod'].each {
    setSum('R91', 'R194', it)
}

['rnu7Field10Sum', 'rnu7Field12Accepted'].each {
    setSum('R92', 'R195', it)
}