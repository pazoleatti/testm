/**
 * Расчет основных граф НФ (calculationBasicSum.groovy).
 * Форма "Расшифровка видов доходов, учитываемых в простых РНУ (доходы простые)".
 *
 * @author auldanov
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


// Расчет сумм
["rnu4Field5Accepted", "rnu4Field5PrevTaxPeriod"].each {
    setSum('R2', 'R87', it)
}


["rnu6Field10Sum", "rnu6Field10Field2", "rnu6Field12Accepted"].each {
    setSum('R2', 'R88', it)
}

["rnu4Field5Accepted", "rnu4Field5PrevTaxPeriod"].each {
    setSum('R89', 'R94',  it)
}

["rnu6Field10Sum", "rnu6Field12Accepted"].each {
    setSum('R89', 'R95', it)
}

["rnu4Field5Accepted", "rnu4Field5PrevTaxPeriod"].each {
    setSum('R97', 'R210', it)
}

["rnu6Field10Sum", "rnu6Field10Field2", "rnu6Field12Accepted"].each {
    setSum('R97', 'R211', it)
}

["rnu4Field5Accepted", "rnu4Field5PrevTaxPeriod"].each {
    setSum('R212', 'R215', it)
}

["rnu4Field5Accepted", "rnu4Field5PrevTaxPeriod"].each {
    setSum('R216', 'R218', it)
}

setSum('R219', 'R222', "rnu4Field5Accepted")


