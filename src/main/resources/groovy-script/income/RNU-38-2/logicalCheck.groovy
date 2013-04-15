/**
 * Логические проверки (logicalCheck.groovy).
 * Форма "(РНУ-38.2) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 2".
 *
 * @author rtimerbaev
 */

/*
 * Проверка объязательных полей.
 */
formData.dataRows.each { row ->
    def colNames = []
    // Список проверяемых столбцов (графа 1..4)
    ['amount', 'incomePrev', 'incomeShortPosition', 'totalPercIncome'].each {
        if (row.getCell(it).getValue() == null) {
            colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def errorMsg = colNames.join(', ')
        def index = row.getOrder()
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
    }
}