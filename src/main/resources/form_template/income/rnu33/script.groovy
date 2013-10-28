package form_template.income.rnu33

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException

import java.text.SimpleDateFormat

/**
 * Форма "(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО".
 *
 * TODO:
 *		- проверка 7 не доделана, потому что про предыдущие месяцы пока не прояснилось
 *		- доделать получение нф за предыдущий месяц в методе getDataRowsOld() после того как будет готово: http://jira.aplana.com/browse/SBRFACCTAX-4515
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
// графа 2  - code                              атрибут 611 - CODE - "Код сделки", справочник 61 "Коды сделок"
// графа 3  - valuablePaper                     атрибут 621 - CODE - "Код признака", справочник 62 "Признаки ценных бумаг"
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def index = 0
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while (row.getAlias() != null && index > 0) {
            row = dataRows.get(--index)
        }
        if (index != currentDataRow.getIndex() && dataRows.get(index).getAlias() == null) {
            index++
        }
    } else if (dataRows.size() > 0) {
        for (int i = dataRows.size()-1; i >= 0; i--) {
            def row = dataRows.get(i)
            if (row.getAlias() == null) {
                index = row.getIndex()
                break
            }
        }
    }
    dataRowHelper.insert(getNewRow(), index + 1)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (currentDataRow.getAlias() == null) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        dataRowHelper.delete(currentDataRow)
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    /*
     * Проверка обязательных полей.
     */

    // список проверяемых столбцов (графа 2..11, 13..17, 19, 21..23)
    def requiredColumns = ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate',
            'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc',
            'marketPriceOnDateAcquisitionInRub', 'redemptionVal', 'exercisePrice',
            'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'costsRetirement',
            'parPaper', 'averageWeightedPricePaper', 'issueDays']

    for (def row : dataRows) {
        if (row.getAlias() == null && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    /*
     * Расчеты.
     */

    // удалить строку "итого" и "Итого за текущий месяц"
    def deleteRows = []
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
    }
    if (dataRows.isEmpty()) {
        return true
    }

    // отсортировать/группировать
    sort(dataRows)

    def cache = [:]
    dataRows.eachWithIndex { row, index ->
        def record61 = getRecordById(61, row.code, cache)
        def code = (record61 != null ? record61.CODE.value : null)

        // графа 1
        row.rowNumber = index + 1

        // графа 12
        row.taxPrice = column12(row)

        // графа 16
        row.marketPricePercent = column16(row)

        // графа 17
        row.marketPriceRuble = column17(row)

        // графа 18
        row.exercisePriceRetirement = column18(row, code)

        // графа 20
        row.allCost = column20(row)

        // графа 24
        row.tenureSkvitovannymiBonds = column24(row)

        // графа 25
        row.interestEarned = column25(row)

        // графа 26
        row.profitLoss = column26(row)

        // графа 27
        row.excessOfTheSellingPrice = column27(row, code)
    }
    dataRowHelper.save(dataRows)

    // посчитать "Итого за текущий месяц"
    def totalRow = getCalcTotalMonth(dataRows)
    dataRowHelper.insert(totalRow, dataRows.size() + 1)

    // добавить строку "Итого за текущий отчётный (налоговый) период"
    def totalAll = getCalcTotalAll(dataRows)
    dataRowHelper.insert(totalAll, dataRows.size() + 1)
    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // если нет строк то проверять не надо
    if (dataRows.isEmpty()) {
        return true
    }

    // 6. Проверка наличия данных предыдущих месяцев
    // TODO (Ramil Timerbaev) про предыдущие месяцы пока не прояснилось
    def monthPeriods = [] // reportPeriodService.listByTaxPeriod(formData.reportPeriodId)
    def monthDates = []
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    for (def monthPeriod : monthPeriods) {
        def form = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, monthPeriod.id)
        // если нет формы за какой то месяц, то получить даты начала и окончания месяца
        if (form == null) {
            def from = format.format(reportPeriodService.getStartDate(monthPeriod.id).time)
            def to = format.format(reportPeriodService.getEndDate(monthPeriod.id).time)
            monthDates.add("$from - $to")
        }
    }
    if (!monthDates.isEmpty()) {
        def periods = monthDates.join(', ')
        logger.error("Экземпляр за период(ы) $periods не существует (отсутствуют первичные данные для расчёта)")
        return false
    }

    // список проверяемых столбцов (графа 1..17, 19..26)
    def requiredColumns = ['rowNumber', 'code', 'valuablePaper', 'issue', 'purchaseDate',
            'implementationDate', 'bondsCount', 'purchaseCost', 'costs',
            'marketPriceOnDateAcquisitionInPerc', 'marketPriceOnDateAcquisitionInRub', 'taxPrice',
            'redemptionVal', 'exercisePrice', 'exerciseRuble',
            'marketPricePercent', 'marketPriceRuble',
            'costsRetirement', 'allCost', 'parPaper', 'averageWeightedPricePaper',
            'issueDays', 'tenureSkvitovannymiBonds', 'interestEarned',
            'profitLoss']
    for (def row : dataRows) {
        // . Обязательность заполнения полей
        if (row.getAlias() == null && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    // алиасы графов для арифметической проверки (12, 16, 17, 18, 20, 24, 25, 26, 27)
    def arithmeticCheckAlias = ['taxPrice', 'marketPricePercent',
            'marketPriceRuble', 'exercisePriceRetirement', 'allCost', 'tenureSkvitovannymiBonds',
            'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    def colNames = []
    def i = 1
    def index
    def errorMsg
    def cache = [:]
    def rowsRnu64 = getRnuRowsById(355)
    def codesFromRnu54 = []
    rowsRnu64.each { row ->
        codesFromRnu54.add(row.dealingNumber)
    }

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        index = row.getIndex()
        errorMsg = "В строке $index "
        def record61 = getRecordById(61, row.code, cache)
        def code = (record61 != null ? record61.CODE.value : null)

        // 1. Проверка рыночной цены в процентах к номиналу (графа 10, 13)
        if (row.marketPriceOnDateAcquisitionInPerc > 0 && row.redemptionVal != 100) {
            logger.error(errorMsg + 'неверно указана цена в процентах при погашении!')
            return false
        }

        // 2. Проверка Номера сделки
        if (code != null && code.toString() in codesFromRnu54) {
            logger.error("Строка $index учитывается в РНУ-64!")
            return false
        }

        // 2. Проверка даты приобретения и даты реализации (графа 2, 5, 6)
        if (code == 5 && row.purchaseDate <= row.implementationDate) {
            logger.error(errorMsg + 'неверно указаны даты приобретения и реализации')
            return false
        }

        // 3. Проверка рыночной цены в рублях к номиналу (графа 14)
        if (row.marketPriceOnDateAcquisitionInPerc > 0 && row.marketPriceOnDateAcquisitionInPerc != row.exercisePrice) {
            logger.error(errorMsg + 'неверно указана цена в рублях при погашении!')
            return false
        }

        // 4. Проверка определения срока короткой позиции (графа 2, 21)
        if (code == 5 && row.parPaper >= 0) {
            logger.error(errorMsg + 'неверно определен срок короткой позиции!')
            return false
        }

        // 5. Проверка определения процентного дохода по короткой позиции (графа 2, 22)
        if (code == 5 && row.averageWeightedPricePaper >= 0) {
            logger.error(errorMsg + 'неверно определен процентный доход по короткой позиции!')
            return false
        }

        // 7. Арифметическая проверка графы 12, 16, 17, 18, 20, 24, 25, 26, 27
        needValue['taxPrice'] = column12(row)
        needValue['marketPricePercent'] = column16(row)
        needValue['marketPriceRuble'] = column17(row)
        needValue['exercisePriceRetirement'] = column18(row, code)
        needValue['allCost'] = column20(row)
        needValue['tenureSkvitovannymiBonds'] = column24(row)
        needValue['interestEarned'] = column25(row)
        needValue['profitLoss'] = column26(row)
        needValue['excessOfTheSellingPrice'] = column27(row, code)

        arithmeticCheckAlias.each { alias ->
            if (needValue[alias] != row.getCell(alias).getValue()) {
                def name = getColumnName(row, alias)
                colNames.add('"' + name + '"')
            }
        }
        if (!colNames.isEmpty()) {
            def msg = colNames.join(', ')
            logger.error(errorMsg + "неверно рассчитано значение графы: $msg.")
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
    def totalRow = getRowByAlias(dataRows, 'total')
    if (totalRow == null) {
        logger.error('Не рассчитаны итоговые значения за текущий месяц.')
        return false
    }
    def totalRowMonth = getCalcTotalMonth(dataRows)
    for (def alias : totalColumns) {
        if (totalRow.getCell(alias).getValue() != totalRowMonth.getCell(alias).getValue()) {
            def name = getColumnName(totalRow, alias)
            logger.error("В графе \"$name\" итоговые значения за текущий месяц рассчитаны неверно!")
            return false
        }
    }

    // 9. Проверка итоговых значений за текущий отчётный (налоговый) период - подсчет сумм для общих итогов
    def totalRowAll = getRowByAlias(dataRows, 'totalAll')
    if (totalRowAll == null) {
        logger.error('Не рассчитаны итоговые значения за текущий отчётный (налоговый) период.')
        return false
    }
    def totalRowAllTmp = getCalcTotalAll(dataRows)
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def index
    def errorMsg
    def cache = [:]
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        index = row.getIndex()
        errorMsg = "В строке $index "

        // 1. Проверка актуальности поля «Код сделки» (графа 2)
        if (row.code != null && null == getRecordById(61, row.code, cache)) {
            logger.warn(errorMsg + 'код сделки в справочнике отсутствует!')
        }

        // 2. Проверка актуальности поля «Признак ценной бумаги» (графа 3)
        if (row.valuablePaper != null && null == getRecordById(62, row.valuablePaper, cache)) {
            logger.warn(errorMsg + 'Признак ценной бумаги не найден в справочнике!')
        }
    }
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    // удалить все строки и собрать из источников их строки
    dataRowHelper.clear()
    def newRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRowHelper = formDataService.getDataRowHelper(source)
                def sourceDataRows = sourceDataRowHelper.getAllCached()
                sourceDataRows.each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        newRows.add(row)
                    }
                }
            }
        }
    }
    if (!newRows.isEmpty()) {
        dataRowHelper.insert(newRows, 1)
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
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['fix', 'rowNumber', 'code', 'valuablePaper', 'issue', 'purchaseDate',
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
        def index = row.getIndex()
        def errorMsg = colNames.join(', ')
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
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
        return row.getCell(alias).column.name.replace('%', '%%')
    }
    return ''
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
 */
void sort(def dataRows) {
    def cache = [:]
    dataRows.sort { def a, def b ->
        // графа 4  - issue
        // графа 3  - valuablePaper (справочник)
        // графа 2  - code (справочник)
        if (a.valuablePaper == b.valuablePaper && a.issue == b.issue) {
            def recordA61 = getRecordById(61, a.code, cache)
            def recordB61 = getRecordById(61, b.code, cache)
            def codeA = (recordA61 != null ? recordA61.CODE.value : null)
            def codeB = (recordB61 != null ? recordB61.CODE.value : null)
            return codeA <=> codeB
        }
        if (a.valuablePaper == b.valuablePaper) {
            return a.issue <=> b.issue
        }

        def recordA62 = getRecordById(62, a.valuablePaper, cache)
        def recordB62 = getRecordById(62, b.valuablePaper, cache)
        def codeA = (recordA62 != null ? recordA62.CODE.value : null)
        def codeB = (recordB62 != null ? recordB62.CODE.value : null)
        return codeA <=> codeB
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
 * Получить итоговую строку с суммами за месяц.
 */
def getCalcTotalMonth(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.getCell('fix').setColSpan(4)
    totalRow.fix = 'Итого за текущий месяц'
    setTotalStyle(totalRow)

    def totalColumns = getTotalColumns()
    def tmp
    // задать нули
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(0)
    }
    // просуммировать значения неитоговых строк
    for (def row : dataRows) {
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
 * @param dataRows строки нф
 * @param alias алиас
 */
def getRowByAlias(def dataRows, def alias) {
    if (alias == null || alias == '' || dataRows == null) {
        return null
    }
    for (def row : dataRows) {
        if (alias.equals(row.getAlias())) {
            return row
        }
    }
    return null
}

def column12(def row) {
    return (row.purchaseCost > row.marketPriceOnDateAcquisitionInRub ? row.marketPriceOnDateAcquisitionInRub : row.purchaseCost)
}

def column16(def row) {
    return (row.redemptionVal > 0 ? 100 : row.redemptionVal)
}

def column17(def row) {
    return (row.redemptionVal > 0 ? row.redemptionVal : null)
}

/**
 * Посчитать значение для графы 18.
 *
 * @param row строка нф
 * @param code значение атрибута 611 - CODE - "Код сделки" справочника 61 "Коды сделок"
 */
def column18(def row, def code) {
    def tmp
    if (code == 1 ||
            (code in [2, 5] && row.marketPriceOnDateAcquisitionInRub > row.redemptionVal && row.taxPrice > row.exercisePrice) ||
            (code in [2, 5] && row.marketPricePercent == 0 && row.marketPriceRuble == 0)) {
        tmp = row.exerciseRuble
    } else if (code == 4) {
        tmp = row.redemptionVal
    } else if (code in [2, 5] && row.exercisePrice < row.marketPricePercent && row.exerciseRuble < row.marketPriceRuble) {
        tmp = row.marketPriceRuble
    } else {
        tmp = null
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
    return (row.exercisePriceRetirement ? row.exercisePriceRetirement - row.allCost - abs(row.interestEarned) : 0)
}

/**
 * Посчитать значение для графы 27.
 *
 * @param row строка нф
 * @param code значение атрибута 611 - CODE - "Код сделки" справочника 61 "Коды сделок"
 */
def column27(def row, def code) {
    return (code != 4 && row.exercisePriceRetirement != null ? row.exercisePriceRetirement - row.exerciseRuble : 0)
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
def getDataRowsOld() {
    // TODO (Ramil Timerbaev) сделать получение нф за предыдущий месяц
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    if (reportPeriodOld != null) {
        def formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
        if (formDataOld != null) {
            def dataRowHelperOld = formDataService.getDataRowHelper(formDataOld)
            return dataRowHelperOld.getAllCached()
        }
    }
    return null
}

def getCalcTotalAll(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('totalAll')
    totalRow.fix = 'Итого за текущий отчётный (налоговый) период'
    totalRow.getCell('fix').setColSpan(4)
    setTotalStyle(totalRow)
    def totalColumns = getTotalColumns()

    // получить итоги за текущий месяц
    def currentTotal = getRowByAlias(dataRows, 'total')

    // получить итоги за предыдущие месяцы текущего налогового периода
    def dataRowsOld = getDataRowsOld()
    def allTotal = getRowByAlias(dataRowsOld, 'totalAll')

    // сложить текущие суммы и за предыдущие месяцы
    def tmp
    totalColumns.each { alias ->
        tmp = currentTotal.getCell(alias).getValue() + (allTotal != null ? allTotal.getCell(alias).getValue() : 0)
        totalRow.getCell(alias).setValue(tmp)
    }
    return totalRow
}

/**
 * Получить запись из справочника по идентифкатору записи.
 *
 * @param refBookId идентификатор справончика
 * @param recordId идентификатор записи
 * @param cache кеш
 */
def getRecordById(def refBookId, def recordId, def cache) {
    if (cache[refBookId] != null) {
        if (cache[refBookId][recordId] != null) {
            return cache[refBookId][recordId]
        }
    } else {
        cache[refBookId] = [:]
    }
    def record = refBookService.getRecordData(refBookId, recordId)
    if (record != null) {
        cache[refBookId][recordId] = record
        return cache[refBookId][recordId]
    }
    // def refBook = refBookFactory.get(refBookId)
    // def refBookName = refBook.name
    // logger.error("Не удалось найти запись (id = $recordId) в справочнике $refBookName (id = $refBookId)")
    return null
}

/**
 * Получить запись из справочника по фильту на дату.
 *
 * @param refBookId идентификатор справончика
 * @param code атрибут справочника по которому искать данные
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return запись справочника
 */
def getRecord(def refBookId, String code, def value, Date date, def cache) {
    String filter = code + " = '" + value + "'"
    if (cache[refBookId] != null) {
        if (cache[refBookId][filter] != null) {
            return cache[refBookId][filter]
        }
    } else {
        cache[refBookId] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(refBookId)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[refBookId][filter] = records.get(0)
        return cache[refBookId][filter]
    }
    def refBook = refBookFactory.get(refBookId)
    def refBookName = refBook.name
    logger.error("Не удалось найти запись в справочнике $refBookName (id = $refBookId) с атрибутом $code равным $value!")
    return null
}

/**
 * Получить строки из нф по заданному идентификатору нф.
 */
def getRnuRowsById(def id) {
    def formDataRNU = formDataService.find(id, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formDataRNU != null) {
        def dataRowHelper = formDataService.getDataRowHelper(formDataRNU)
        return dataRowHelper.getAllCached()
    }
    return null
}