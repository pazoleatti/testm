/**
 * Скрипт для РНУ-7 (rnu7.groovy).
 * Форма "(РНУ-7) Справка бухгалтера для отражения расходов, учитываемых в РНУ-5, учёт которых требует применения метода начисления".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- про нумерацию пока не уточнили, пропустить
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
// графа 2  - balance
// графа 3  - date
// графа 4  - code
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
    def newRow = formData.createDataRow()
    formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)

    ['balance', 'date', 'code', 'docNumber', 'docDate', 'currencyCode',
            'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'accountingCurrency'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
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
            def columns = ['balance', 'date', 'code', 'docNumber', 'docDate', 'currencyCode',
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
                    index = getIndex(row) + 1
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
        formData.dataRows.remove(getIndex(it))
    }
    if (formData.dataRows.isEmpty()) {
        return
    }

    // отсортировать/группировать
    formData.dataRows.sort { it.code }

    // графа 8 (для строк НЕитого)
    formData.dataRows.each { row ->
        if (row.currencyCode != null && row.currencyCode == 'RUR') {
            row.rateOfTheBankOfRussia = 1
        }
    }

    formData.dataRows.eachWithIndex { row, index ->
        // графа 1
        row.number = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить

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
        if (i == formData.dataRows.size() - 1) {
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

    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        formData.dataRows.add(index + i, row)
        i = i + 1
    }

    // добавить строки "итого"
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.code = 'Итого'
    setTotalStyle(totalRow)
    totalRow.taxAccountingRuble = total10
    totalRow.ruble = total12
}

/**
 * Логические проверки.
 *
 * @param checkRequiredColumns проверять ли обязательные графы
 */
void logicalCheck(def checkRequiredColumns) {
    def tmp

    /** Дата начала отчетного периода. */
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def a = (tmp ? tmp.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def b = (tmp ? tmp.getTime() : null)

    if (!formData.dataRows.isEmpty()) {
        def i = 1
        // суммы строки общих итогов
        def totalSums = [:]
        // столбцы для которых надо вычислять итого и итого по коду классификации дохода. Графа 10, 12
        def totalColumns = ['taxAccountingRuble', 'ruble']
        // признак наличия итоговых строк
        def hasTotal = false
        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 4. Обязательность заполнения полей (графа 1..12)
            def colNames = []

            // Список проверяемых столбцов (графа 1..12)
            def requiredColumns = ['number', 'balance', 'date', 'code', 'docNumber',
                    'docDate', 'currencyCode', 'rateOfTheBankOfRussia', 'taxAccountingCurrency',
                    'taxAccountingRuble', 'accountingCurrency', 'ruble']

            // если "графа 7" == 'RUR', то "графа 8" = 1 и ее проверять не надо
            def needColumn8 = (row.currencyCode != null && row.currencyCode == 'RUR')
            if (!needColumn8) {
                requiredColumns -= 'rateOfTheBankOfRussia'
            }

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
                    index = getIndex(row) + 1
                    logger.error("В строке $index не заполнены колонки : $errorMsg.")
                }
                return
            }

            // 1. Проверка даты совершения операции и границ отчётного периода (графа 3)
            if (row.date < a || b < row.date) {
                logger.error('Дата совершения операции вне границ отчётного периода!')
                return
            }

            // 2. Проверка на нулевые значения (графа 9, 10, 11, 12)
            if (row.taxAccountingCurrency == 0 && row.taxAccountingRuble == 0 &&
                    row.accountingCurrency == 0 && row.ruble == 0) {
                logger.error('Все суммы по операции нулевые!')
                return
            }

            // 5. Проверка на уникальность поля «№ пп» (графа 1)
            if (i != row.number) {
                logger.error('Нарушена уникальность номера по порядку!')
                return
            }
            i += 1

            // 6. Арифметическая проверка графы 10
            tmp = round(row.rateOfTheBankOfRussia * row.taxAccountingCurrency , 2)
            if (row.taxAccountingRuble != tmp) {
                logger.error('Неверное значение в поле «Сумма дохода, начисленная в налоговом учёте. Рубли»!')
                return
            }

            // 7. Арифметическая проверка графы 12
            tmp = round(row.accountingCurrency * row.taxAccountingCurrency , 2)
            if (row.ruble != tmp) {
                logger.error('Неверное значение поле «Сумма дохода, отражённая в бухгалтерском учёте. Рубли»!')
                return
            }

            // 8. Проверка итоговых значений по кодам классификации дохода - нахождение кодов классификации
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

            // 3. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода (графа 10, 12)
            if (totalRow.ruble > 0 && totalRow.taxAccountingRuble >= totalRow.ruble) {
                logger.warn('Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!')
            }

            // 8. Проверка итоговых значений по кодам классификации дохода
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
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    newRow.code = 'Итого по коду'
    setTotalStyle(newRow)
    return newRow
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'balance', 'date', 'code', 'docNumber', 'docDate',
            'currencyCode', 'rateOfTheBankOfRussia', 'taxAccountingCurrency',
            'taxAccountingRuble', 'accountingCurrency', 'ruble'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Посчитать сумму указанного графа для строк с общим кодом классификации
 *
 * @param code код классификации дохода
 * @param alias название графа
 */
def calcSumByCode(def code, def alias) {
    def sum = 0
    formData.dataRows.each { row ->
        if (!isTotal(row) && row.code == code) {
            sum += row.getCell(alias).getValue()
        }
    }
    return sum
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
}