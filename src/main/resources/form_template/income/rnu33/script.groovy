package form_template.income.rnu33

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО".
 *
 * @version 68
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- проверка 5 не сделана, потому что про предыдущие месяцы пока не прояснилось
 *		- неясность с алгоритмом заполнения строки «Итого за текущий месяц»
 *              (после каких строк считать или по каким значениям группировать строки).
 *              Временно сгруппировал по графе 4 "Выпуск"
 *		- по какому полю группировать?
 *	    - заполнение графы 15 не доописано
 *	    - нет проверок заполнения полей перед логической проверкой
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck() && checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        calc() && logicalCheck() && checkNSI()
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
// графа 10 - marketPriceOnDateAcquisitionInPerc
// графа 11 - marketPriceOnDateAcquisitionInRub
// графа 12 - taxPrice
// графа 13 - redemptionVal
// графа 14 - exercisePrice
// графа 15 - exerciseRuble
// графа 16 - marketPricePercent
// графа 17 - marketPriceRuble
// графа 18 - exercisePriceRetirement
// графа 19 - costsRetirement
// графа 20 - allCost
// графа 21 - parPaper
// графа 22 - averageWeightedPricePaper
// графа 23 - issueDays
// графа 24 - tenureSkvitovannymiBonds
// графа 25 - interestEarned
// графа 26 - profitLoss
// графа 27 - excessOfTheSellingPrice

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    def newRow = getNewRow()
    // TODO (Ramil Timerbaev)
    insert(data, newRow)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isTotal(currentDataRow)) {
        def data = getData(formData)
        deleteRow(data, currentDataRow)
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
def calc() {
    def data = getData(formData)
    /*
     * Проверка обязательных полей.
     */

    // TODO (Ramil Timerbaev) в чтз не указаны обязательные поля
    // список проверяемых столбцов (графа 2..17, 19, 21..23)
    def requiredColumns = ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate',
            'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc',
            'marketPriceOnDateAcquisitionInRub', 'redemptionVal', 'exercisePrice',
            'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'costsRetirement',
            'parPaper', 'averageWeightedPricePaper', 'issueDays']

    for (def row : getRows(data)) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    /*
     * Расчеты.
     */

    // удалить строку "итого" и "Итого за текущий месяц"
    def deleteRows = []
    for (def row : getRows(data)) {
        if (isTotal(row)) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        getRows(data).removeAll(deleteRows)
    }
    if (getRows(data).isEmpty()) {
        return true
    }

    // отсортировать/группировать
    sort(data)

    def tmp
    getRows(data).eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // TODO (Ramil Timerbaev) графа 10 в чтз непонятно
        // графа 10
        row.marketPriceOnDateAcquisitionInPerc = row.marketPriceOnDateAcquisitionInRub / row.parPaper

        // графа 12
        row.taxPrice = (row.purchaseCost > row.marketPriceOnDateAcquisitionInRub ?
            row.marketPriceOnDateAcquisitionInRub : row.purchaseCost)

        // TODO (Ramil Timerbaev) графа 15, 16, 17

        // графа 18
        if (row.code == 1 ||
                ((row.code == 2 || row.code == 5) && row.exercisePrice > row.marketPricePercent && row.exerciseRuble > row.marketPriceRuble) ||
                ((row.code == 2 || row.code == 5) && row.marketPricePercent == 0 && row.marketPriceRuble == 0)) {
            tmp = row.exerciseRuble
        } else if (row.code == 4) {
            tmp = row.redemptionVal
        } else if ((row.code == 2 || row.code == 5) && row.exercisePrice < row.marketPricePercent && row.exerciseRuble < row.marketPriceRuble) {
            tmp = row.marketPriceRuble
        } else {
            // TODO (Ramil Timerbaev) иначе что?
            tmp = 0
        }
        row.exercisePriceRetirement = tmp

        // графа 20
        row.allCost = row.purchaseCost + row.costs + row.costsRetirement

        // графа 24
        row.tenureSkvitovannymiBonds = row.implementationDate - row.purchaseDate

        // графа 25
        def column22 = (row.parPaper - row.averageWeightedPricePaper) * row.tenureSkvitovannymiBonds * row.bondsCount / row.issueDays
        row.interestEarned = roundValue(column22, 0)

        // графа 23
        row.profitLoss = row.exercisePriceRetirement - row.allCost - abs(row.interestEarned)

        // графа 27
        row.excessOfTheSellingPrice = (row.code != 4 ? row.exercisePriceRetirement - row.exerciseRuble : 0)
    }
    save(data)

    // графы для которых надо вычислять итого и итого по эмитенту (графа 7..9, 13, 15, 17..20, 25..27)
    def totalColumns = getTotalColumns()

    // TODO (Ramil Timerbaev) разобраться как считать
    // добавить строку "Итого за текущий отчётный (налоговый) период"
    def totalRow = getCalcTotalRow()
    insert(data, totalRow)

    // посчитать "Итого за текущий месяц"
    def totalRows = [:]
    def sums = [:]
    tmp = null
    totalColumns.each {
        sums[it] = 0
    }
    getRows(data).eachWithIndex { row, i ->
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
            if (i == getRows(data).size() - 2) {
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
        data.insert(row, index + i)
        i++
    }
    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = getData(formData)
    def i = 1
    def index
    def errorMsg

    // 6. Проверка наличия данных предыдущих месяцев
    // Наличие экземпляров отчетов за предыдущие месяцы с начала текущего отчётного (налогового) периода
    // TODO (Ramil Timerbaev) про предыдущие месяцы пока не прояснилось
    for (def row : getRows(data)) {
        if (false) {
            // TODO (Ramil Timerbaev) поправить сообщение
            index = getIndex(row) + 1
            errorMsg = "В строке $index "
            logger.error(errorMsg + 'экземпляр за период(ы) <Дата начала отчетного периода1> - <Дата окончания отчетного периода1>, <Дата начала отчетного периода N> - <Дата окончания отчетного периода N> не существует (отсутствуют первичные данные для расчёта)')
            return false
        }
    }

    // список проверяемых столбцов (графа 2..17, 19, 21..23)
    def requiredColumns = ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate',
            'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc',
            'marketPriceOnDateAcquisitionInRub', 'redemptionVal', 'exercisePrice',
            'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'costsRetirement',
            'parPaper', 'averageWeightedPricePaper', 'issueDays']
    // суммы строки общих итогов
    def totalSums = [:]
    // графы для которых надо вычислять итого и итого по эмитенту (графа 7..9, 13, 15, 17..20, 25..27)
    def totalColumns = getTotalColumns()
    // признак наличия итоговых строк
    def hasTotal = false
    // список групп кодов классификации для которых надо будет посчитать суммы
    def totalGroupsName = []
    def tmp
    for (def row : getRows(data)) {
        if (isTotal(row)) {
            hasTotal = true
            continue
        }

        // TODO (Ramil Timerbaev) нет проверок заполнения полей перед логической проверкой
        // . Обязательность заполнения полей (графа 2..17, 19, 21..23)
        if (!checkRequiredColumns(row, requiredColumns)) {
            return false
        }

        index = getIndex(row) + 1
        errorMsg = "В строке $index "

        // 1. Проверка рыночной цены в процентах к номиналу (графа 10, 13)
        if (row.marketPriceOnDateAcquisitionInPerc > 0 && row.redemptionVal != 100) {
            logger.error(errorMsg + 'неверно указана цена в процентах при погашении!')
            return false
        }

        // 2. Неверно указаны даты приобретения и реализации (графа 2, 5, 6)
        if (row.code == 5 && row.purchaseDate <= row.implementationDate) {
            logger.error(errorMsg + 'неверно указаны даты приобретения и реализации')
            return false
        }

        // 3. Проверка рыночной цены в рублях к номиналу (графа 14)
        if (row.marketPriceOnDateAcquisitionInPerc > 0 && row.marketPriceOnDateAcquisitionInPerc != row.exercisePrice) {
            logger.error(errorMsg + 'неверно указана цена в рублях при погашении!')
            return false
        }

        // 4. Проверка определения срока короткой позиции (графа 2, 21)
        if (row.code == 5 && row.parPaper >= 0) {
            logger.error(errorMsg + 'неверно определен срок короткой позиции!')
            return false
        }

        // 5. Проверка определения процентного дохода по короткой позиции (графа 2, 22)
        if (row.code == 5 && row.averageWeightedPricePaper >= 0) {
            logger.error(errorMsg + 'неверно определен процентный доход по короткой позиции!')
            return false
        }

        // 7. Арифметическая проверка графы 18
        if (row.code == 1 ||
                (row.code in [2, 5] && row.exercisePrice > row.marketPricePercent && row.exerciseRuble > row.marketPriceRuble) ||
                (row.code in [2, 5] && row.marketPricePercent == 0 && row.marketPriceRuble)) {
            tmp = row.exerciseRuble
        } else if (row.code == 4) {
            tmp = row.redemptionVal
        } else if (row.code in [2, 5] &&
                row.exercisePrice < row.marketPricePercent &&
                row.exerciseRuble < row.marketPriceRuble) {
            tmp = row.marketPriceRuble
        }
        if (row.exercisePriceRetirement != tmp) {
            logger.warn(errorMsg + 'неверное значение поля «Цена реализации (выбытия) для целей налогообложения (руб.коп.)»!')
        }

        // TODO (Ramil Timerbaev)
        // 7. Арифметическая проверка графы 20
        tmp = row.purchaseCost + row.costs + row.costsRetirement
        if (row.allCost != tmp) {
            logger.warn(errorMsg + 'неверное значение поля «Всего расходы (руб.коп.)»!')
        }

        // 8. Арифметическая проверка графы 24
        if (row.tenureSkvitovannymiBonds != row.implementationDate - row.purchaseDate) {
            logger.warn(errorMsg + 'неверное значение поля «Показатели для расчёта процентного дохода за время владения сквитованными облигациями.Срок владения сквитованными облигациями (дни)»!')
        }

        // 9. Арифметическая проверка графы 25
        tmp = roundValue((row.parPaper - row.averageWeightedPricePaper) * row.tenureSkvitovannymiBonds * row.bondsCount / row.issueDays, 2)
        if (row.interestEarned != tmp) {
            logger.warn(errorMsg + 'неверное значение поля «Процентный доход, полученный за время владения сквитованными облигациями (руб.коп.)»!')
        }

        // 10. Арифметическая проверка графы 27
        tmp = row.exercisePriceRetirement - row.allCost - abs(row.interestEarned)
        if (row.profitLoss != tmp) {
            logger.warn(errorMsg + 'неверное значение поля «Прибыль (+), убыток (-) от реализации (погашения) за вычетом процентного дохода (руб.коп.)»!')
        }

        // 11. Арифметическая проверка графы 24
        tmp = row.exercisePriceRetirement - row.exerciseRuble
        if ((row.code != 4 && row.excessOfTheSellingPrice != tmp) ||
                (row.code == 4 && row.excessOfTheSellingPrice != 0)) {
            logger.warn(errorMsg + 'неверное значение поля «Превышение цены реализации для целей налогообложения над ценой реализации (руб.коп.)»!')
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
        if (i != row.rowNumber) {
            logger.error(errorMsg + 'нарушена уникальность номера по порядку!')
            return false
        }
        i += 1
    }

    if (hasTotal) {
        def totalRow = getRowByAlias(data, 'total')

        // 12. Проверка итоговых значений за текущий месяц
        for (def codeName : totalGroupsName) {
            def row = getRowByAlias(data, 'total' + codeName)
            for (def alias : totalColumns) {
                if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                    logger.error('Итоговые значения за текущий месяц рассчитаны неверно!')
                    return false
                }
            }
        }

        // 13. Проверка итоговых значений за текущий отчётный (налоговый) период
        for (def alias : totalColumns) {
            if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                logger.error('Итоговые значения за текущий отчётный (налоговый) период рассчитаны неверно!')
                return false
            }
        }
    }
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def data = getData(formData)
    def index
    def errorMsg
    for (def row : getRows(data)) {
        index = getIndex(row) + 1
        errorMsg = "В строке $index "

        // 1. Проверка актуальности поля «Код сделки» (графа 2)
        if (false) {
            logger.warn(errorMsg + '')
        }

        // 2. Проверка актуальности поля «Признак ценной бумаги» (графа 3)
        if (false) {
            logger.warn(errorMsg + '')
        }
    }
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    def data =getData(formData)
    // удалить все строки и собрать из источников их строки
    data.clear()
    def newRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        newRows.add(row)
                    }
                }
            }
        }
    }
    if (!newRows.isEmpty()) {
        data.insert(newRows, 1)
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

    //проверка периода ввода остатков
    if (isBalancePeriod) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
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
    def data = getData(formData)
    def rows = getRows(data)
    return summ(formData, rows, new ColumnRange(columnAlias, 0, rows.size() - 2))
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
            'marketPriceOnDateAcquisitionInPerc', 'marketPriceOnDateAcquisitionInRub', 'taxPrice',
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
    def data = getData(formData)
    getRows(data).each { row ->
        if (!isTotal(row) && row.issue == value) {
            sum += (row.getCell(alias).getValue() ?: 0)
        }
    }
    return sum
}

/**
 * Получить номер строки в таблице (1..n).
 *
 * @param row строка
 */
def getIndex(def row) {
    row.getIndex() - 1
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.rowNumber
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Получить название графы по псевдониму.
 *
 * @param row строка
 * @param alias псевдоним графы
 */
def getColumnName(def row, def alias) {
    if (row != null && alias != null) {
        return row.getCell(alias).getColumn().getName().replace('%', '%%')
    }
    return ''
}


/**
 * Получить список строк формы.
 *
 * @param data данные нф (helper)
 */
def getRows(def data) {
    return data.getAllCached();
}

/**
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void save(def data) {
    data.save(getRows(data))
}

/**
 * Вставить новую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
}

/**
 * Удалить строку из нф
 *
 * @param data данные нф (helper)
 * @param row строка для удаления
 */
void deleteRow(def data, def row) {
    data.delete(row)
}

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()

    // графа 2..17, 19, 21..23
    ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate',
            'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc',
            'marketPriceOnDateAcquisitionInRub', 'redemptionVal', 'exercisePrice',
            'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'costsRetirement',
            'parPaper', 'averageWeightedPricePaper', 'issueDays'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

/**
 * Отсорировать данные (по графе 3, 4, 2).
 *
 * @param data данные нф (хелпер)
 */
void sort(def data) {
    getRows(data).sort { def a, def b ->
        // графа 2  - code
        // графа 3  - valuablePaper
        // графа 4  - issue
        if (a.valuablePaper == b.valuablePaper && a.issue == b.issue) {
            return a.code <=> b.code
        }
        if (a.valuablePaper == b.valuablePaper) {
            return a.issue <=> b.issue
        }
        return a.valuablePaper <=> b.valuablePaper
    }
}

/**
 * Получить модуль числа. Вместо Math.abs() потому что возможна потеря точности.
 */
def abs(def value) {
    return value < 0 ? -value : value
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/**
 * Получить графы для которых надо вычислять итого и итого по эмитенту (графа 7..9, 13, 15, 17..20, 25..27).
 */
def getTotalColumns() {
    return ['bondsCount', 'purchaseCost', 'costs', 'redemptionVal',
            'exerciseRuble', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement',
            'allCost', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']
}

/**
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.getCell('valuablePaper').setColSpan(4)
    totalRow.getCell('valuablePaper').setValue('Итого за текущий отчётный (налоговый) период')

    def totalColumns = getTotalColumns()
    def data = getData(formData)
    def tmp
    // задать нули
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(0)
    }
    // просуммировать значения неитоговых строк
    for (def row : getRows(data)) {
        if (row.getAlias() != null) {
            continue
        }
        totalColumns.each { alias ->
            tmp = totalRow.getCell(alias).getValue() + (row.getCell(alias).getValue() ?: 0)
            totalRow.getCell(alias).setValue(tmp)
        }
    }
    return totalRow
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    if (alias == null || alias == '' || data == null) {
        return null
    }
    for (def row : getRows(data)) {
        if (alias.equals(row.getAlias())) {
            return row
        }
    }
    return null
}