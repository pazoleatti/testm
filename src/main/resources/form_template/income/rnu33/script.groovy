package form_template.income.rnu33

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО".
 *
 * @version 68
 *
 * TODO:
 *      - проверки нси 2: неясности со справочником "Признаки ценных бумаг"
 *		- проверка 6 не сделана, потому что про предыдущие месяцы пока не прояснилось
 *		- доделать получение нф за предыдущий месяц в методе getFormDataOld() после того как будет готово: http://jira.aplana.com/browse/SBRFACCTAX-4515
 *	    - заполнение графы 15 не доописано
 *	    - нет проверок заполнения полей перед логической проверкой
 *	    - алгоритмы заполнения графы 16, 17, 18 не указано что присваивать если ни одно из условий не срабатывает http://jira.aplana.com/browse/SBRFACCTAX-4569
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
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicalCheck() && checkNSI()
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

    def index = 0
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while (row.getAlias() != null && index > 0) {
            row = getRows(data).get(--index)
        }
        if (index != currentDataRow.getIndex() && getRows(data).get(index).getAlias() == null) {
            index++
        }
    } else if (getRows(data).size() > 0) {
        for (int i = getRows(data).size()-1; i >= 0; i--) {
            def row = getRows(data).get(i)
            if (!isFixedRow(row)) {
                index = getIndex(row) + 1
                break
            }
        }
    }
    data.insert(getNewRow(),index + 1)
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

    getRows(data).eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 10
        row.marketPriceOnDateAcquisitionInPerc = column10(row)

        // графа 12
        row.taxPrice = column12(row)

        // TODO (Ramil Timerbaev) графа 15 сказали пока оставить

        // графа 16
        row.marketPricePercent = column16(row)

        // графа 17
        row.marketPriceRuble = column17(row)

        // графа 18
        row.exercisePriceRetirement = column18(row)

        // графа 20
        row.allCost = column20(row)

        // графа 24
        row.tenureSkvitovannymiBonds = column24(row)

        // графа 25
        row.interestEarned = column25(row)

        // графа 26
        row.profitLoss = column26(row)

        // графа 27
        row.excessOfTheSellingPrice = column27(row)
    }
    save(data)

    // посчитать "Итого за текущий месяц"
    def totalRow = getCalcTotalRow()
    insert(data, totalRow)

    // добавить строку "Итого за текущий отчётный (налоговый) период"
    def totalAll = getCalcAllTotal()
    insert(data, totalAll)
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
    def formDataOld = getFormDataOld()
    if (false && formDataOld == null) {
        // TODO (Ramil Timerbaev) поправить сообщение
        logger.error('Экземпляр за период(ы) <Дата начала отчетного периода1> - <Дата окончания отчетного периода1>, <Дата начала отчетного периода N> - <Дата окончания отчетного периода N> не существует (отсутствуют первичные данные для расчёта)')
        return false
    }

    // список проверяемых столбцов (графа 1..27)
    def requiredColumns = ['rowNumber', 'code', 'valuablePaper', 'issue', 'purchaseDate',
            'implementationDate', 'bondsCount', 'purchaseCost', 'costs',
            'marketPriceOnDateAcquisitionInPerc', 'marketPriceOnDateAcquisitionInRub', 'taxPrice',
            'redemptionVal', 'exercisePrice', 'exerciseRuble',
            'marketPricePercent', 'marketPriceRuble', 'exercisePriceRetirement',
            'costsRetirement', 'allCost', 'parPaper', 'averageWeightedPricePaper',
            'issueDays', 'tenureSkvitovannymiBonds', 'interestEarned',
            'profitLoss', 'excessOfTheSellingPrice']

    // алиасы графов для арифметической проверки (графы 10, 12, 16, 17, 18, 20, 24, 25, 26, 27)
    def arithmeticCheckAlias = ['marketPriceOnDateAcquisitionInPerc', 'taxPrice', 'marketPricePercent',
            'marketPriceRuble', 'exercisePriceRetirement', 'allCost', 'tenureSkvitovannymiBonds',
            'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    def colNames = []

    for (def row : getRows(data)) {
        if (isTotal(row)) {
            continue
        }

        // TODO (Ramil Timerbaev) в чтз нет проверок заполнения полей перед логической проверкой
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

        // 7. Арифметическая проверка графы 10, 12, 16, 17, 18, 20, 24, 25, 26, 27
        needValue['marketPriceOnDateAcquisitionInPerc'] = column10(row)
        needValue['taxPrice'] = column12(row)
        needValue['marketPricePercent'] = column16(row)
        needValue['marketPriceRuble'] = column17(row)
        needValue['exercisePriceRetirement'] = column18(row)
        needValue['allCost'] = column20(row)
        needValue['tenureSkvitovannymiBonds'] = column24(row)
        needValue['interestEarned'] = column25(row)
        needValue['profitLoss'] = column26(row)
        needValue['excessOfTheSellingPrice'] = column27(row)

        arithmeticCheckAlias.each { alias ->
            if (needValue[alias] != row.getCell(alias).getValue()) {
                def name = getColumnName(row, alias)
                colNames.add('"' + name + '"')
            }
        }
        if (!colNames.isEmpty()) {
            def msg = colNames.join(', ')
            logger.error(errorMsg + "неверно рассчитана графы: $msg.")
            return false
        }

        // 10. Проверка на уникальность поля «№ пп» (графа 1)
        if (i != row.rowNumber) {
            logger.error(errorMsg + 'нарушена уникальность номера по порядку!')
            return false
        }
        i += 1
    }

    // графы для которых надо вычислять итого и итого по эмитенту (графа 7..9, 13, 15, 17..20, 25..27)
    def totalColumns = getTotalColumns()

    // 8. Проверка итоговых значений за текущий месяц
    def totalRow = getRowByAlias(data, 'total')
    if (totalRow == null) {
        logger.error('Не рассчитаны итоговые значения за тукущий месяц.')
        return false
    }
    def totalRowTmp = getCalcTotalRow()
    for (def alias : totalColumns) {
        if (totalRow.getCell(alias).getValue() != totalRowTmp.getCell(alias).getValue()) {
            def name = getColumnName(totalRow, alias)
            logger.error("В графе \"$name\" итоговые значения за текущий месяц рассчитаны неверно!")
            return false
        }
    }

    // 9. Проверка итоговых значений за текущий отчётный (налоговый) период - подсчет сумм для общих итогов
    def totalRowAll = getRowByAlias(data, 'totalAll')
    if (totalRowAll == null) {
        logger.error('Не рассчитаны итоговые значения за тукущий отчётный (налоговый) период.')
        return false
    }
    def totalRowAllTmp = getCalcAllTotal()

    for (def alias : totalColumns) {
        if (totalRowAll.getCell(alias).getValue() != totalRowAllTmp.getCell(alias).getValue()) {
            def name = getColumnName(totalRow, alias)
            logger.error("В графе \"$name\" итоговые значения за текущий отчётный (налоговый) период рассчитаны неверно!")
            return false
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
    def date = new Date()
    def cache = [:]
    for (def row : getRows(data)) {
        if (row.getAlias() != null) {
            continue
        }
        index = getIndex(row) + 1
        errorMsg = "В строке $index "

        // 1. Проверка актуальности поля «Код сделки» (графа 2)
        // справочника 61 «Коды сделок»
        // атрибут 611 "Код сделки" - CODE
        if (row.code != null && null == getRecordId(61, 'CODE', row.code, date, cache)) {
            logger.warn(errorMsg + 'код сделки в справочнике отсутствует!')
        }

        // 2. Проверка актуальности поля «Признак ценной бумаги» (графа 3)
        // TODO (Ramil Timerbaev) неясности со справочником "Признаки ценных бумаг"
        if (false) {
            logger.warn(errorMsg + 'Признак ценной бумаги не найден в справочнике!')
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
 * Установить стиль для итоговых строк.
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
 * Получить номер строки в таблице (0..n).
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
    setTotalStyle(totalRow)

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

def column10(def row) {
    // TODO (Ramil Timerbaev) графа 10 в чтз непонятно
    checkDivision(row.parPaper, row.getIndex())
    return row.marketPriceOnDateAcquisitionInRub / row.parPaper
}

def column12(def row) {
    return (row.purchaseCost > row.marketPriceOnDateAcquisitionInRub ? row.marketPriceOnDateAcquisitionInRub : row.purchaseCost)
}

def column16(def row) {
    // TODO (Ramil Timerbaev) иначе null или 0?
    return (row.redemptionVal > 0 ? 100 : null)
}

def column17(def row) {
    // TODO (Ramil Timerbaev) иначе null или 0?
    return (row.redemptionVal > 0 ? row.redemptionVal : null)
}

def column18(def row) {
    def tmp
    if (row.code == 1 ||
            (row.code in [2, 5] && row.marketPriceOnDateAcquisitionInRub > row.redemptionVal && row.taxPrice > row.exercisePrice) ||
            (row.code in [2, 5] && row.marketPricePercent == 0 && row.marketPriceRuble == 0)) {
        tmp = row.exerciseRuble
    } else if (row.code == 4) {
        tmp = row.redemptionVal
    } else if (row.code in [2, 5] && row.exercisePrice < row.marketPricePercent && row.exerciseRuble < row.marketPriceRuble) {
        tmp = row.marketPriceRuble
    } else {
        // TODO (Ramil Timerbaev) иначе что?
        tmp = 0
    }
    return tmp
}

def column20(def row) {
    return row.taxPrice + row.costs + row.costsRetirement
}

def column24(def row) {
    return row.implementationDate - row.purchaseDate
}

def column25(def row) {
    checkDivision(row.issueDays, row.getIndex())
    def tmp = (row.parPaper - row.averageWeightedPricePaper) * row.tenureSkvitovannymiBonds * row.bondsCount / row.issueDays
    return roundValue(tmp, 0)
}

def column26(def row) {
    return row.exercisePriceRetirement - row.allCost - abs(row.interestEarned)
}

def column27(def row) {
    return (row.code != 4 ? row.exercisePriceRetirement - row.exerciseRuble : 0)
}

// TODO (Ramil Timerbaev) для отладки, потом убрать
def log(def msg) {
    logger.info('===== ' + msg)
    // System.out.println('===== ' + msg)
}

/**
 * Проверка деления на ноль.
 *
 * @param division делитель
 * @param index номер строки
 */
void checkDivision(def division, def index) {
    if (division == 0) {
        throw new ServiceLoggerException("Деление на ноль в строке $index.", logger.getEntries())
    }
}

/**
 * Получить нф за предыдущий месяц
 */
def getFormDataOld() {
    // TODO (Ramil Timerbaev) сделать получение нф за предыдущий месяц
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

def getCalcAllTotal() {
    // новая строка
    def newRow = formData.createDataRow()
    newRow.setAlias('totalAll')
    newRow.valuablePaper = 'Итого за текущий месяц'
    newRow.getCell('valuablePaper').setColSpan(4)
    setTotalStyle(newRow)
    def totalColumns = getTotalColumns()

    // получить итоги за текущий месяц
    def data = getData(formData)
    def currentTotal = getRowByAlias(data, 'total')

    // получить итоги за предыдущие месяцы текущего налогового периода
    def dataOld = getData(getFormDataOld())
    def allTotal = getRowByAlias(dataOld, 'totalAll')

    // сложить текущие суммы и за предыдущие месяцы
    def tmp
    totalColumns.each { alias ->
        tmp = currentTotal.getCell(alias).getValue() + (allTotal != null ? allTotal.getCell(alias).getValue() : 0)
        newRow.getCell(alias).setValue(tmp)
    }
    return newRow
}

/**
 * Получить id справочника.
 *
 * @param ref_id идентификатор справончика
 * @param code атрибут справочника
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return
 */
def getRecordId(def ref_id, String code, def value, Date date, def cache) {
    String filter = code + " = '" + value + "'"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter] != null) {
            return cache[ref_id][filter]
        }
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    // logger.error("Не удалось найти запись в справочнике (id=$ref_id) с атрибутом $code равным $value!")
    return null
}