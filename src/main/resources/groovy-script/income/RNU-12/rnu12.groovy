/**
 * Скрипт для РНУ-12 (rnu12.groovy).
 * Форма "(РНУ-12) Регистр налогового учёта расходов по хозяйственным операциям и оказанным Банку услугам".
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- получение даты начала и окончания отчетного периода
 * 		- про нумерацию пока не уточнили, пропустить
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
        logicalCheck(true)
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
}

// графа 1  - number
// графа 2  - code
// графа 3  - numberFirstRecord
// графа 4  - opy
// графа 5  - operationDate
// графа 6  - name
// графа 7  - documentNumber
// графа 8  - date
// графа 9  - periodCounts
// графа 10 - advancePayment
// графа 11 - outcomeInNalog
// графа 12 - outcomeInBuh

/**
 * Добавить новую строку.
 */
def addNewRow() {
    newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

    // графа 2..10, 12
    ['code', 'numberFirstRecord', 'opy', 'operationDate',
            'name', 'documentNumber', 'date', 'periodCounts',
            'advancePayment', 'outcomeInBuh'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def index = formData.dataRows.indexOf(currentDataRow)

    // если данных еще нет или строка не выбрана
    if (formData.dataRows.isEmpty() || index == -1) {
        formData.dataRows.add(newRow)
    } else {
        formData.dataRows.add(index + 1, newRow)
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    if (formData.dataRows.isEmpty()) {
        return
    }
    /*
	 * Проверка объязательных полей.
	 */
    def hasError = false
    formData.dataRows.each { row ->
        if (!isTotal(row)) {
            def colNames = []
            // Список проверяемых столбцов (графа 2..10, 12)
            def requiredColumns = ['code', 'numberFirstRecord', 'opy',
                    'operationDate', 'name', 'documentNumber', 'date',
                    'periodCounts', 'advancePayment', 'outcomeInBuh']

            requiredColumns.each {
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
                    index = formData.dataRows.indexOf(row) + 1
                    logger.error("В строке $index не заполнены колонки : $errorMsg.")
                }
            }
        }
    }
    if (hasError) {
        return
    }

    // удалить строки "итого" и "итого по коду"
    def delRow = []
    formData.dataRows.each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        formData.dataRows.remove(formData.dataRows.indexOf(row))
    }
    if (formData.dataRows.isEmpty()) {
        return
    }

    // отсортировать/группировать
    formData.dataRows.sort { it.code }

    /*
     * Расчеты.
     */

    formData.dataRows.eachWithIndex { row, index ->
        // графа 1
        row.number = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить

        // графа 11
        if (row.periodCounts != null) {
            row.outcomeInNalog = round(row.advancePayment / row.periodCounts, 2)
        }
    }


    /** Столбцы для которых надо вычислять итого и итого по коду. Графа 10, 11, 12. */
    def totalColumns = ['advancePayment', 'outcomeInNalog', 'outcomeInBuh']

    // добавить строку "итого"
    def totalRow = formData.appendDataRow()
    totalRow.setAlias('total')
    totalRow.code = 'Итого'
    setTotalStyle(totalRow)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }

    // посчитать "итого по коду"
    def totalRows = [:]
    def tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }
    formData.dataRows.eachWithIndex { row, i ->
        if (!isTotal(row)) {
            if (tmp == null) {
                tmp = row.code
            }
            // если код расходы поменялся то создать новую строку "итого по коду"
            if (tmp != row.code) {
                totalRows.put(i, getNewRow(tmp, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
            if (i == formData.dataRows.size() - 2) {
                totalColumns.each {
                    sums[it] += row.getCell(it).getValue()
                }
                totalRows.put(i + 1, getNewRow(row.code, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += row.getCell(it).getValue()
            }
            tmp = row.code
        }
    }

    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        formData.dataRows.add(index + i, row)
        i = i + 1
    }
}

/**
 * Логические проверки.
 *
 * @param checkRequiredColumns проверять ли обязательные графы
 */
void logicalCheck(def checkRequiredColumns) {
    // TODO (Ramil Timerbaev) как получить границы отчетного периода?
    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    /** Налоговый период. */
    def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.taxPeriodId) : null)

    /** Дата начала отчетного периода. */
    def a = (taxPeriod != null ? taxPeriod.getStartDate() : null )

    /** Дата окончания отчетного периода. */
    def b = (taxPeriod != null ? taxPeriod.getEndDate() : null)

    if (!formData.dataRows.isEmpty()) {
        def i = 1
        // суммы строки общих итогов
        def totalSums = [:]
        // столбцы для которых надо вычислять итого и итого по коду классификации дохода. Графа 10, 11, 12
        def totalColumns = ['advancePayment', 'outcomeInNalog', 'outcomeInBuh']
        // признак наличия итоговых строк
        def hasTotal = false
        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 5. Обязательность заполнения поля графы 1-12
            def colNames = []
            // Список проверяемых столбцов (графа 1..12)
            def requiredColumns = ['number', 'code', 'numberFirstRecord', 'opy',
                    'operationDate', 'name', 'documentNumber', 'date', 'periodCounts',
                    'advancePayment', 'outcomeInNalog', 'outcomeInBuh']

            requiredColumns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                if (!checkRequiredColumns) {
                    return
                }
                def index = row.number
                def errorMsg = colNames.join(', ')
                if (index != null) {
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = formData.dataRows.indexOf(row) + 1
                    logger.error("В строке $index не заполнены колонки : $errorMsg.")
                }
                return
            }

            // 1. Проверка даты совершения операции и границ отчетного периода (графа 5)
            if (row.operationDate < a || b < row.operationDate) {
                logger.error('Дата совершения операции вне границ отчётного периода!')
                return
            }

            // 2. Проверка количества отчетных периодов при авансовых платежах (графа 9)
            if (row.periodCounts < 1 || 999 < row.periodCounts) {
                logger.error('Неверное количество отчетных периодов при авансовых платежах!')
                return
            }

            // 3. Проверка на нулевые значения (графа 11, 12)
            if (row.outcomeInNalog == 0 && row.outcomeInBuh == 0) {
                logger.error('Все суммы по операции нулевые!')
                return
            }

            // 6. Проверка на уникальность поля «№ пп» (графа 1)
            if (i != row.number) {
                logger.error('Нарушена уникальность номера по порядку!')
                break
            }
            i += 1

            // 1. Арифметическая проверка графы 11
            if (row.periodCounts == 0) {
                logger.error('Деление на ноль. При проверке значения поля «Сумма расхода, начисленная в налоговом учёте».')
                return
            }
            def tmp = round(row.advancePayment / row.periodCounts, 2)
            if (row.outcomeInNalog != tmp) {
                logger.error('Неверное значение поля «Сумма расхода, начисленная в налоговом учёте»!')
                return
            }

            // 8. Проверка итоговых значений по кодам классификации дохода - нахождение кодов классификации расхода
            if (!totalGroupsName.contains(row.code)) {
                totalGroupsName.add(row.code)
            }

            // 9. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += row.getCell(alias).getValue()
            }
        }

        if (hasTotal) {
            def totalRow = formData.getDataRow('total')

            // 4. Проверка на превышение суммы расхода по данным бухгалтерского учёта над суммой начисленного расхода (графа 11, 12)
            if (totalRow.outcomeInNalog <= totalRow.outcomeInBuh) {
                logger.warn('Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!')
                return
            }

            // 8. Проверка итоговых значений по кодам классификации расхода
            for (def codeName : totalGroupsName) {
                def row = formData.getDataRow('total' + codeName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        logger.error("Итоговые значения по коду $codeName рассчитаны неверно!")
                        return
                    }
                }
            }

            // 9. Проверка итогового значений по всей форме
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    hindError = true
                    logger.error('Итоговые значения рассчитаны неверно!')
                    return
                }
            }
        }
    }
}

/**
 * Проверки соответствия НСИ.
 */
void checkNSI() {
    // 1. Проверка кода классификации расхода для данного РНУ (графа 2)
    if (false) {
        logger.error('Операция в РНУ не учитывается!')
    }

    // 2. Проверка символа ОПУ для кода классификации расхода (графа 4)
    if (false) {
        logger.warn('Символ ОПУ в справочнике отсутствует!')
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
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def from = 0
    def to = formData.dataRows.size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = new DataRow('total' + alias, formData.getFormColumns(), formData.getFormStyles())
    newRow.code = 'Итого по коду'
    setTotalStyle(newRow)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'code', 'numberFirstRecord', 'opy', 'operationDate',
            'name', 'documentNumber', 'date', 'periodCounts',
            'advancePayment', 'outcomeInNalog', 'outcomeInBuh'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}