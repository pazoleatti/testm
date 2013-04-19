/**
 * Скрипт для РНУ-7 (rnu7.groovy).
 * Форма "(РНУ-7) Справка бухгалтера для отражения расходов, учитываемых в РНУ-5, учёт которых требует применения метода начисления".
 *
 * TODO:
 *      - нет уловии в проверках соответствия НСИ (потому что нету справочников)
 *		- получение даты начала и окончания отчетного периода
 *		- про нумерацию пока не уточнили, пропустить
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck()
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

// графа 1  - number
// графа 2  - balance
// графа 3  - date
// графа 4  - outcomeCode
// графа 5  - docNumber
// графа 6  - docDate
// графа 7  - currencyCode
// графа 8  - rateOfTheBankOfRussia
// графа 9  - taxAccountingCurrency
// графа 10 - taxAccountingRuble
// графа 11 - accountingCurrency
// графа 12 - ruble

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())
    ['balance', 'date', 'outcomeCode', 'docNumber', 'docDate', 'currencyCode',
            'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'accountingCurrency'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    def pos = (currentDataRow != null && !formData.dataRows.isEmpty() ? currentDataRow.getOrder() : formData.dataRows.size)
    formData.dataRows.add(pos, newRow)

    setOrder()
}

/**
 * Удалить строку.
 */
def deleteRow() {
    formData.dataRows.remove(currentDataRow)

    setOrder()
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    /*
      * Проверка объязательных полей.
      */
    def hasError = false
    formData.dataRows.each { row ->
        if (!isTotal(row)) {
            def colNames = []
            // Список проверяемых столбцов
            def columns = ['balance', 'date', 'outcomeCode', 'docNumber', 'docDate', 'currencyCode',
                    'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'accountingCurrency']

            // если "графа 7" == 'RUR', то "графа 8" = 1 и ее проверять не надо
            def needColumn8 = (row.currencyCode != null && row.currencyCode == 'RUR')
            if (!needColumn8) {
                columns -= 'rateOfTheBankOfRussia'
            }

            columns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                hasError = true
                def index = row.number
                def errorMsg = colNames.join(', ')
                if (index != null) {
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = row.getOrder()
                    logger.error("В строке $index не заполнены колонки : $errorMsg.")
                }
            }
        }
    }
    if (hasError) {
        return
    }

    /*
      * Расчеты.
      */

    // удалить строки "итого" и "итого по коду"
    def delRow = []
    formData.dataRows.each {
        if (isTotal(it)) {
            delRow += it
        }
    }
    delRow.each {
        formData.dataRows.remove(formData.dataRows.indexOf(it))
    }

    // отсортировать/группировать
    formData.dataRows.sort { it.outcomeCode }

    // графа 8 (для строк НЕитого)
    formData.dataRows.each { row ->
        if (row.currencyCode != null && row.currencyCode == 'RUR') {
            row.rateOfTheBankOfRussia = 1
        }
    }

    // графа 10, 12
    formData.dataRows.each { row ->
        // графа 10 (для строк НЕитого) = графа 9 * графа 8
        row.taxAccountingRuble = round(row.taxAccountingCurrency * row.rateOfTheBankOfRussia, 2)

        // графа 12 (для строк НЕитого) = графа 11 * графа 8
        row.ruble = round(row.accountingCurrency * row.rateOfTheBankOfRussia, 2)
    }

    // графа 10, 12 для последней строки "итого"
    def total10 = 0
    def total12 = 0
    formData.dataRows.each { row ->
        total10 += row.taxAccountingRuble
        total12 += row.ruble
    }

    /** Столбцы для которых надо вычислять итого и итого по эмитенту. Графа 10, 12. */
    def totalColumns = ['taxAccountingRuble', 'ruble']

    // посчитать "итого по коду"
    def totalRows = [:]
    def tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }

    formData.dataRows.eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.outcomeCode
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (tmp != row.outcomeCode) {
            totalRows.put(i, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == formData.dataRows.size() - 1) {
            totalColumns.each {
                sums[it] += row.getCell(it).getValue()
            }
            totalRows.put(i + 1, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        totalColumns.each {
            sums[it] += row.getCell(it).getValue()
        }
        tmp = row.outcomeCode
    }

    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        formData.dataRows.add(index + i, row)
        i = i + 1
    }

    // добавить строки "итого"
    def totalRow = formData.appendDataRow()
    totalRow.setAlias('total')
    totalRow.outcomeCode = 'Итого'
    totalRow.taxAccountingRuble = total10
    totalRow.ruble = total12

    // графа 1, + поправить значения order
    formData.dataRows.eachWithIndex { row, index ->
        row.number = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить
        row.setOrder(index + 1)
    }
}

/**
 * Логические проверки.
 */
void logicalCheck() {
    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    /** Налоговый период. */
    def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.taxPeriodId) : null)

    /** Дата начала отчетного периода. */
    def a = (taxPeriod != null ? taxPeriod.getStartDate() : null )

    /** Дата окончания отчетного периода. */
    def b = (taxPeriod != null ? taxPeriod.getEndDate() : null)

    if (!formData.dataRows.isEmpty()) {
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                continue
            }

            // графа 3 - Проверка даты совершения операции и границ отчётного периода
            if (row.date < a || b < row.date) {
                logger.error('Дата совершения операции вне границ отчётного периода!')
                break
            }

            // графа 9, 10, 11, 12 - Проверка на нулевые значения
            if (!isTotal(row) &&
                    row.taxAccountingCurrency == 0 && row.taxAccountingRuble == 0 &&
                    row.accountingCurrency == 0 && row.ruble == 0) {
                logger.error('Все суммы по операции нулевые!')
                break
            }
        }
    }

    // графа 10, 12 - Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода
    def totalRow = formData.getDataRow('total')
    if (totalRow.ruble > 0 && totalRow.taxAccountingRuble >= totalRow.ruble) {
        logger.warn('Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!')
    }
}

/**
 * Проверки соответствия НСИ.
 */
void checkNSI() {
    // 1. Проверка балансового счёта для кода классификации дохода - Проверка актуальности «графы 2»
    if (false) {
        logger.warn('Балансовый счёт в справочнике отсутствует!')
    }

    // 2. Проверка балансового счёта для кода классификации дохода - Проверка актуальности «графы 4»
    if (false) {
        logger.warn('Балансовый счёт в справочнике отсутствует!')
    }

    // 3. Проверка кода классификации дохода для данного РНУ - Проверка актуальности «графы 4» на дату по «графе 3»
    if (false) {
        logger.error('Операция в РНУ не учитывается!')

    }
    // 4. Проверка кода валюты - Проверка актуальности «графы 7»	1
    if (false) {
        logger.error('Код валюты в справочнике отсутствует!')
    }

    // 5. Проверка курса валюты со справочным - Проверка актуальности «графы 8» на дату по «графе 3»
    if (false) {
        logger.warn('Неверный курс валюты!')
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Поправить значания order.
 */
void setOrder() {
    formData.dataRows.eachWithIndex { row, index ->
        row.setOrder(index + 1)
    }
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = new DataRow('total' + alias, formData.getFormColumns(), formData.getFormStyles())
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    newRow.outcomeCode = 'Итого по коду'
    return newRow
}