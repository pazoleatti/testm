/**
 * Скрипт для РНУ-33 (rnu33.groovy).
 * Форма "(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО".
 *
 * TODO:
 *      - нет уловии в проверках соответствия НСИ (потому что нету справочников)
 *		- проверка 5 не сделана, потому что про предыдущие месяцы пока не прояснилось
 *		- проверка 6 "№ пп" под вопросом
 * 		- про нумерацию пока не уточнили, пропустить
 *		- неясность с алгоритмом заполнения строки «Итого за текущий месяц» (после каких строк считать или по каким значениям группировать строки). Временно сгруппировал по графе 4 "Выпуск"
 *		- по какому полю группировать?
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
    case FormDataEvent.CALCULATE :
        calc()
        // logicalCheck()
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
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
// графа 12 - exercisePrice
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
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

    // графа 2..14, 16, 18..20
    ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate',
            'bondsCount','purchaseCost', 'costs', 'redemptionVal', 'exercisePrice',
            'exerciseRuble', 'marketPricePercent', 'marketPriceRuble',
            'costsRetirement', 'parPaper', 'averageWeightedPricePaper', 'issueDays'].each {
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
            // Список проверяемых столбцов (графа 2..14, 16, 18..20)
            def requiredColumns = ['code', 'valuablePaper', 'issue', 'purchaseDate',
                    'implementationDate', 'bondsCount','purchaseCost', 'costs',
                    'redemptionVal', 'exercisePrice', 'exerciseRuble',
                    'marketPricePercent', 'marketPriceRuble', 'costsRetirement',
                    'parPaper', 'averageWeightedPricePaper', 'issueDays']

            requiredColumns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                hasError = true
                def index = row.rowNumber
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

    // удалить строку "итого" и "Итого за текущий месяц"
    def delRow = []
    formData.dataRows.each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        formData.dataRows.remove(formData.dataRows.indexOf(row))
    }

    // отсортировать/группировать
    formData.dataRows.sort { it.issue } // TODO (Ramil Timerbaev) уточнить по какому полю группировать

    formData.dataRows.each { row ->

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

    /** Столбцы для которых надо вычислять итого и итого по эмитенту. Графа 7..10, 12, 14..17, 22..24. */
    def totalColumns = ['bondsCount', 'purchaseCost', 'costs', 'redemptionVal',
            'exerciseRuble', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement',
            'allCost', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']
    // добавить строку "Итого за текущий отчётный (налоговый) период"
    def totalRow = formData.appendDataRow()
    totalRow.setAlias('total')
    totalRow.getCell('valuablePaper').setColSpan(4)
    totalRow.getCell('valuablePaper').setValue('Итого за текущий отчётный (налоговый) период')
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
                totalRows.put(i + 1, getNewRow(tmp, totalColumns, sums))
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

    // графа 1, + поправить значения order
    formData.dataRows.eachWithIndex { row, index ->
        if (!isTotal(row)) {
            row.rowNumber = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить
        }
        row.setOrder(index + 1)
    }
}

/**
 * Логические проверки.
 */
void logicalCheck() {
    if (!formData.dataRows.isEmpty()) {
        def index = 1
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                continue
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

            // 6. Проверка на уникальность поля «№ пп» (графа 1)
            if (index != row.rowNumber) {
                logger.error('Нарушена уникальность номера по порядку!')
                break
            }
            index += 1
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
    def newRow = new DataRow('total' + alias, formData.getFormColumns(), formData.getFormStyles())
    newRow.valuablePaper = 'Итого за текущий месяц'
    newRow.getCell('valuablePaper').setColSpan(4)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}