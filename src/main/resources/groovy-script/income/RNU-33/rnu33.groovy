/**
 * Скрипт для РНУ-33 (rnu33.groovy).
 * Форма "(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- проверка 5 не сделана, потому что про предыдущие месяцы пока не прояснилось
 *		- проверка 6 "№ пп" под вопросом
 * 		- про нумерацию пока не уточнили, пропустить
 *		- неясность с алгоритмом заполнения строки «Итого за текущий месяц» (после каких строк считать или по каким значениям группировать строки). Временно сгруппировал по графе 4 "Выпуск"
 *		- по какому полю группировать?
 *	    - заполнение графы 15 не доописано
 *	    - нет проверок заполнения полей перед логической проверкой
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

// графа 1  - rowNumber
// графа 2  - code
// графа 3  - valuablePaper
// графа 4  - issue
// графа 5  - purchaseDate
// графа 6  - implementationDate
// графа 7  - bondsCount
// графа 8  - purchaseCost
// графа 9  - costs
// графа 10 - redemptionVal
// графа 11 - exercisePrice
// графа 12 - exerciseRuble
// графа 13 - marketPricePercent
// графа 14 - marketPriceRuble
// графа 15 - exercisePriceRetirement
// графа 16 - costsRetirement
// графа 17 - allCost
// графа 18 - parPaper
// графа 19 - averageWeightedPricePaper
// графа 20 - issueDays
// графа 21 - tenureSkvitovannymiBonds
// графа 22 - interestEarned
// графа 23 - profitLoss
// графа 24 - excessOfTheSellingPrice

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)

    // графа 2..14, 16, 18..20
    ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate',
            'bondsCount','purchaseCost', 'costs', 'redemptionVal', 'exercisePrice',
            'exerciseRuble', 'marketPricePercent', 'marketPriceRuble',
            'costsRetirement', 'parPaper', 'averageWeightedPricePaper', 'issueDays'].each {
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
            // Список проверяемых столбцов (графа 2..14, 16, 18..20)
            def requiredColumns = ['code', 'valuablePaper', 'issue', 'purchaseDate',
                    'implementationDate', 'bondsCount','purchaseCost', 'costs',
                    'redemptionVal', 'exercisePrice', 'exerciseRuble',
                    'marketPricePercent', 'marketPriceRuble', 'costsRetirement',
                    'parPaper', 'averageWeightedPricePaper', 'issueDays']

            requiredColumns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    def name = row.getCell(it).getColumn().getName().replace('%', '%%')
                    colNames.add('"' + name + '"')
                }
            }
            if (!colNames.isEmpty()) {
                hasError = true
                def index = row.rowNumber
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

    // удалить строку "итого" и "Итого за текущий месяц"
    def delRow = []
    formData.dataRows.each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        formData.dataRows.remove(getIndex(row))
    }
    if (formData.dataRows.isEmpty()) {
        return
    }

    // отсортировать/группировать
    formData.dataRows.sort { it.issue } // TODO (Ramil Timerbaev) уточнить по какому полю группировать

    formData.dataRows.eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить

        // графа 15
        if (row.code == 1 ||
                ((row.code == 2 || row.code == 5) && row.exercisePrice > row.marketPricePercent && row.exerciseRuble > row.marketPriceRuble) ||
                ((row.code == 2 || row.code == 5) && row.marketPricePercent == 0 && row.marketPriceRuble == 0)) {
            row.exercisePriceRetirement = row.exerciseRuble
        } else if (row.code == 4) {
            row.exercisePriceRetirement = row.redemptionVal
        } else if ((row.code == 2 || row.code == 5) && row.exercisePrice < row.marketPricePercent && row.exerciseRuble < row.marketPriceRuble) {
            row.exercisePriceRetirement = row.marketPriceRuble
        } else {
            // TODO (Ramil Timerbaev) иначе что?
            row.exercisePriceRetirement = 0
        }

        // графа 17
        row.allCost = row.purchaseCost + row.costs + row.costsRetirement

        // графа 21
        row.tenureSkvitovannymiBonds = row.implementationDate - row.purchaseDate

        // графа 22
        def column22 = (row.parPaper - row.averageWeightedPricePaper) * row.tenureSkvitovannymiBonds * row.bondsCount / row.issueDays
        row.interestEarned = round(column22, 0)

        // графа 23
        row.profitLoss = row.exercisePriceRetirement - row.allCost - Math.abs(row.interestEarned)

        // графа 24
        row.excessOfTheSellingPrice = (row.code != 4 ? row.exercisePriceRetirement - row.exerciseRuble : 0)
    }

    // графы для которых надо вычислять итого и итого по эмитенту (графа 7..10, 12, 14..17, 22..24)
    def totalColumns = ['bondsCount', 'purchaseCost', 'costs', 'redemptionVal',
            'exerciseRuble', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement',
            'allCost', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']

    // добавить строку "Итого за текущий отчётный (налоговый) период"
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.getCell('valuablePaper').setColSpan(4)
    totalRow.getCell('valuablePaper').setValue('Итого за текущий отчётный (налоговый) период')
    setTotalStyle(totalRow)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }

    // посчитать "Итого за текущий месяц"
    def totalRows = [:]
    def sums = [:]
    def tmp = null
    totalColumns.each {
        sums[it] = 0
    }
    formData.dataRows.eachWithIndex { row, i ->
        if (!isTotal(row)) {
            if (tmp == null) {
                tmp = row.issue // TODO (Ramil Timerbaev) уточнить по какому полю группировать
            }
            // если код расходы поменялся то создать новую строку "Итого за текущий месяц"
            if (tmp != row.issue) { // TODO (Ramil Timerbaev) уточнить по какому полю группировать
                totalRows.put(i, getNewRow(tmp, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "Итого за текущий месяц"
            if (i == formData.dataRows.size() - 2) {
                totalColumns.each {
                    sums[it] += row.getCell(it).getValue()
                }
                totalRows.put(i + 1, getNewRow(row.issue, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += row.getCell(it).getValue()
            }
            tmp = row.issue // TODO (Ramil Timerbaev) уточнить по какому полю группировать
        }
    }
    // добавить "Итого за текущий месяц" в таблицу
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
    if (!formData.dataRows.isEmpty()) {
        def i = 1
        // суммы строки общих итогов
        def totalSums = [:]
        // графы для которых надо вычислять итого и итого по эмитенту (графа 7..10, 12, 14..17, 22..24)
        def totalColumns = ['bondsCount', 'purchaseCost', 'costs', 'redemptionVal',
                'exerciseRuble', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement',
                'allCost', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']
        // признак наличия итоговых строк
        def hasTotal = false
        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // TODO (Ramil Timerbaev) нет проверок заполнения полей перед логической проверкой
            // . Обязательность заполнения полей (графа 2..14, 16, 18..20)
            def colNames = []
            // Список проверяемых столбцов (графа 2..14, 16, 18..20)
            ['code', 'valuablePaper', 'issue', 'purchaseDate',
                    'implementationDate', 'bondsCount','purchaseCost', 'costs',
                    'redemptionVal', 'exercisePrice', 'exerciseRuble',
                    'marketPricePercent', 'marketPriceRuble', 'costsRetirement',
                    'parPaper', 'averageWeightedPricePaper', 'issueDays'].each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    def name = row.getCell(it).getColumn().getName().replace('%', '%%')
                    colNames.add('"' + name + '"')
                }
            }
            if (!colNames.isEmpty()) {
                if (!checkRequiredColumns) {
                    return
                }
                def index = row.rowNumber
                def errorMsg = colNames.join(', ')
                if (index != null) {
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = getIndex(row) + 1
                    logger.error("В строке $index не заполнены колонки : $errorMsg.")
                }
                return
            }

            // 1. Проверка рыночной цены в процентах к номиналу (графа 10, 13)
            if (row.redemptionVal > 0 && row.marketPricePercent != 100) {
                logger.error('Неверно указана цена в процентах при погашении!')
                break
            }

            // 2. Проверка рыночной цены в рублях к номиналу (графа 10, 14)
            if (row.redemptionVal > 0 && row. redemptionVal != row.marketPriceRuble) {
                logger.error('Неверно указана цена в рублях при погашении!')
                break
            }

            // 3. Проверка определения срока короткой позиции (графа 2, 21)
            if (row.code == 5 && row.tenureSkvitovannymiBonds >= 0) {
                logger.error('Неверно определен срок короткой позиции!')
                break
            }

            // 4. Проверка определения процентного дохода по короткой позиции (графа 2, 22)
            if (row.code == 5 && row.interestEarned >= 0) {
                logger.error('Неверно определен процентный доход по короткой позиции!')
                break
            }

            // 5. Проверка наличия данных предыдущих месяцев
            // Наличие экземпляров отчетов за предыдущие месяцы с начала текущего отчётного (налогового) периода
            // TODO (Ramil Timerbaev) про предыдущие месяцы пока не прояснилось
            if (false) {
                // TODO (Ramil Timerbaev) поправить сообщение
                logger.error('Экземпляр за период(ы) <Дата начала отчетного периода1> - <Дата окончания отчетного периода1>, <Дата начала отчетного периода N> - <Дата окончания отчетного периода N> не существует (отсутствуют первичные данные для расчёта)')
                break
            }

            // 6. Арифметическая проверка графы 15
            condition1 = (row.code == 1 ||
                    (row.code in [2, 5] && row.exercisePrice > row.marketPricePercent && row.exerciseRuble > row.marketPriceRuble) ||
                    (row.code in [2, 5] && row.marketPricePercent == 0 && row.marketPriceRuble)) &&
                    row.exercisePriceRetirement != row.exerciseRuble

            condition2 = row.code == 4 && row.exercisePriceRetirement != row.redemptionVal

            condition3 = row.code in [2, 5] &&
                    row.exercisePrice < row.marketPricePercent &&
                    row.exerciseRuble < row.marketPriceRuble &&
                    row.exercisePriceRetirement != row.marketPriceRuble

            if (condition1 || condition2 || condition3) {
                logger.info('Неверное значение поля «Цена реализации (выбытия) для целей налогообложения (руб.коп.)»!')
                return
            }

            // 7. Арифметическая проверка графы 17
            tmp = row.purchaseCost + row.costs + row.costsRetirement
            if (row.allCost != tmp) {
                logger.info('Неверное значение поля «Всего расходы (руб.коп.)»!')
                return
            }

            // 8. Арифметическая проверка графы 21
            if (row.tenureSkvitovannymiBonds != row.implementationDate - row.purchaseDate) {
                logger.info('Неверное значение поля «Показатели для расчёта процентного дохода за время владения сквитованными облигациями.Срок владения сквитованными облигациями (дни)»!')
                return
            }

            // 9. Арифметическая проверка графы 22
            tmp = round((row.parPaper - row.averageWeightedPricePaper) * row.tenureSkvitovannymiBonds * row.bondsCount / row.issueDays, 2)
            if (row.interestEarned != tmp) {
                logger.info('Неверное значение поля «Процентный доход, полученный за время владения сквитованными облигациями (руб.коп.)»!')
                return
            }

            // 10. Арифметическая проверка графы 23
            tmp = row.exercisePriceRetirement - row.allCost - Math.abs(row.interestEarned)
            if (row.profitLoss != tmp) {
                logger.info('Неверное значение поля «Прибыль (+), убыток (-) от реализации (погашения) за вычетом процентного дохода (руб.коп.)»!')
                return
            }

            // 11. Арифметическая проверка графы 24
            tmp = row.exercisePriceRetirement - row.exerciseRuble
            if ((row.code != 4 && row.excessOfTheSellingPrice != tmp) ||
                    (row.code == 4 && row.excessOfTheSellingPrice != 0)) {
                logger.info('Неверное значение поля «Превышение цены реализации для целей налогообложения над ценой реализации (руб.коп.)»!')
                return
            }

            // 12. Проверка итоговых значений за текущий месяц
            if (!totalGroupsName.contains(row.issue)) {
                totalGroupsName.add(row.issue)
            }

            // 13. Проверка итоговых значений за текущий отчётный (налоговый) период - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
            }

            // 14. Проверка на уникальность поля «№ пп» (графа 1)
            // TODO (Ramil Timerbaev) под вопросом
            if (i != row.rowNumber) {
                logger.error('Нарушена уникальность номера по порядку!')
                return
            }
            i += 1
        }

        if (hasTotal) {
            def totalRow = formData.getDataRow('total')

            // 12. Проверка итоговых значений за текущий месяц
            for (def codeName : totalGroupsName) {
                def row = formData.getDataRow('total' + codeName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        logger.error('Итоговые значения за текущий месяц рассчитаны неверно!')
                        return
                    }
                }
            }

            // 13. Проверка итоговых значений за текущий отчётный (налоговый) период
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    hindError = true
                    logger.error('Итоговые значения за текущий отчётный (налоговый) период рассчитаны неверно!')
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
    // 1. Проверка актуальности поля «Код сделки» (графа 2)
    if (false) {
        logger.warn('')
    }

    // 2. Проверка актуальности поля «Признак ценной бумаги» (графа 3)
    if (false) {
        logger.warn('')
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
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    return summ(formData, new ColumnRange(columnAlias, 0, formData.dataRows.size() - 2))
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    newRow.valuablePaper = 'Итого за текущий месяц'
    newRow.getCell('valuablePaper').setColSpan(4)
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
    ['rowNumber', 'code', 'valuablePaper', 'issue', 'purchaseDate',
            'implementationDate', 'bondsCount', 'purchaseCost', 'costs',
            'redemptionVal', 'exercisePrice', 'exerciseRuble',
            'marketPricePercent', 'marketPriceRuble', 'exercisePriceRetirement',
            'costsRetirement', 'allCost', 'parPaper', 'averageWeightedPricePaper',
            'issueDays', 'tenureSkvitovannymiBonds', 'interestEarned',
            'profitLoss', 'excessOfTheSellingPrice'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Посчитать сумму указанного графа для строк с общим значением
 *
 * @param value значение общее для всех строк суммирования
 * @param alias название графа
 */
def calcSumByCode(def value, def alias) {
    def sum = 0
    formData.dataRows.each { row ->
        if (!isTotal(row) && row.issue == value) {
            sum += (row.getCell(alias).getValue() ?: 0)
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