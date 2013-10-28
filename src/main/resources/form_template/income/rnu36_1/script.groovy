package form_template.income.rnu36_1

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1".
 *
 * TODO:
 *		- уточнить как получать дату последнего и отчетного дня для месяца в методе getLastDayReportPeriod() getReportDate()
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

// графа 1  - series
// графа 2  - amount
// графа 3  - nominal
// графа 4  - shortPositionDate
// графа 5  - balance2              атрибут 350 - NUMBER - "Номер", справочник 28 "Классификатор доходов Сбербанка России для целей налогового учёта"
// графа 6  - averageWeightedPrice
// графа 7  - termBondsIssued
// графа 8  - percIncome

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def newRow = getNewRow()
    def index

    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getIndexByAlias(dataRows, 'totalA')
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex()
    } else {
        index = getIndexByAlias(dataRows, 'totalA')
        switch (currentDataRow.getAlias()) {
            case 'A' :
            case 'totalA' :
                index = getIndexByAlias(dataRows, 'totalA')
                break
            case 'B' :
            case 'totalB' :
            case 'total' :
                index = getIndexByAlias(dataRows, 'totalB')
                break
        }
    }

    dataRowHelper.insert(newRow, index + 1)
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

    // список проверяемых столбцов (графа 2..7)
    def requiredColumns = ['series', 'amount', 'nominal', 'shortPositionDate',
            'balance2', 'averageWeightedPrice', 'termBondsIssued']
    for (def row : dataRows) {
        if (row.getAlias() == null && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    /*
     * Расчеты.
     */

    // последний день отчетного месяца
    def lastDay = getLastDayReportPeriod()

    dataRows.each { row ->
        if (row.getAlias() == null) {
            // графа 8
            row.percIncome = getColumn8(row, lastDay)
        }
    }

    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    def totalColumns = ['amount', 'percIncome']
    def tmp
    ['A', 'B'].each {
        def row = getRowByAlias(dataRows, it)
        def totalRow = getRowByAlias(dataRows, 'total' + it)

        // посчитать итоги раздела
        def from = row.getIndex()
        def to = totalRow.getIndex() - 2
        totalColumns.each { alias ->
            // если нет строк в разделе то в итоги 0
            tmp = (from <= to ? summ(formData, dataRows, new ColumnRange(alias, from, to)) : 0)
            totalRow.getCell(alias).setValue(tmp)
        }
    }

    // посчитать Итого
    getRowByAlias(dataRows, 'total').percIncome = getCalcTotalValueByAlias(dataRows, 'percIncome')
    dataRowHelper.save(dataRows)
    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // список проверяемых столбцов (графа 2..8)
    def requiredColumns = ['series', 'amount', 'nominal', 'shortPositionDate',
            'balance2', 'averageWeightedPrice', 'termBondsIssued', 'percIncome']

    for (def row : dataRows) {
        // . Проверка обязательных полей (графа 2..7)
        if (row.getAlias() == null && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    def totalColumns = ['amount', 'percIncome']

    // последний день отчетного месяца
    def lastDay = getLastDayReportPeriod()

    // отчетная дата
    def reportDay = getReportDate()
    def index
    def errorMsg

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        index = row.getIndex()
        errorMsg = "В строке $index "

        // 1. Проверка даты приобретения (открытия короткой позиции) (графа 4)
        if (row.shortPositionDate > reportDay) {
            logger.error(errorMsg + 'неверно указана дата приобретения (открытия короткой позиции)!')
            return false
        }

        // 2. Арифметическая проверка графы 8
        if (row.termBondsIssued != null || row.termBondsIssued != 0) {
            if (row.percIncome > getColumn8(row, lastDay)) {
                logger.warn(errorMsg + 'неверно рассчитана графа «Процентный доход с даты приобретения»!')
            }
        }
    }

    // 3, 4. Проверка итоговых значений по разделу А и B
    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    ['A', 'B'].each { section ->
        def row = getRowByAlias(dataRows, section)
        def totalRow = getRowByAlias(dataRows, 'total' + section)

        // посчитать итоги раздела
        def from = row.getIndex()
        def to = totalRow.getIndex() - 2
        totalColumns.each { alias ->
            // если нет строк в разделе то в итоги 0
            tmp = (from <= to ? summ(formData, dataRows, new ColumnRange(alias, from, to)) : 0)
            if (totalRow.getCell(alias).getValue() != tmp) {
                logger.error("Итоговые значений для раздела $section рассчитаны неверно!")
            }
        }
    }
    if (hasError()) {
        return false
    }

    // 5. Проверка итоговых значений по всей форме
    def totalRow = getRowByAlias(dataRows, 'total')
    if (totalRow.percIncome != getCalcTotalValueByAlias(dataRows, 'percIncome')) {
        logger.error('Итоговые значений рассчитаны неверно!')
        return false
    }
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def cache = [:]
    for (def row : dataRows) {
        // графа 5
        if (row.getAlias() == null && row.balance2 != null && null == getRecordById(28, row.balance2, cache)) {
            index = row.getIndex()
            logger.warn("В строке $index балансовый счёт в справочнике отсутствует!")
        }
    }
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить нефиксированные строки
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
    }

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRowHelper = formDataService.getDataRowHelper(source)
                def sourceDataRows = sourceDataRowHelper.getAllCached()
                copyRows(sourceDataRows, dataRows, 'A', 'totalA')
                copyRows(sourceDataRows, dataRows, 'B', 'totalB')
            }
        }
    }
    dataRowHelper.save(dataRows)
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
 * Посчитать значение графы 8.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def getColumn8(def row, def lastDay) {
    checkDivision(row.termBondsIssued, row.getIndex())
    def tmp = ((row.nominal - row.averageWeightedPrice) *
            (lastDay - row.shortPositionDate) / row.termBondsIssued) * row.amount
    return roundValue(tmp, 2)
}

/**
 * Получить номер строки в таблице по псевдонимиу (1..n).
 */
def getIndexByAlias(def dataRows, String rowAlias) {
    def row = getRowByAlias(dataRows, rowAlias)
    return (row != null ? row.getIndex() : -1)
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
            colNames.add('"' + row.getCell(it).column.name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def errorMsg = colNames.join(', ')
        def index = row.getIndex()
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows строки приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def sourceDataRows, def destinationDataRows, def fromAlias, def toAlias) {
    def from = getIndexByAlias(sourceDataRows, fromAlias)
    def to = getIndexByAlias(sourceDataRows, toAlias) - 1
    if (from > to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getIndexByAlias(destinationDataRows, toAlias) - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    destinationDataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()
    newRow.getCell('percIncome').styleAlias = 'Вычисляемая'
    // графа 1..7
    ['series', 'amount', 'nominal', 'shortPositionDate',
            'balance2', 'averageWeightedPrice', 'termBondsIssued'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    return newRow
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
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

// TODO (Ramil Timerbaev) уточнить как получать дату последнего дня для месяца
/**
 * Получить последний день отчетного периода (месяца)
 */
def getLastDayReportPeriod() {
    def last = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (last ? last.time : null)
}

/**
 * Получить отчетную дату.
 */
def getReportDate() {
    def reportDay = reportPeriodService.getReportDate(formData.reportPeriodId)
    return (reportDay ? reportDay.time : null)
}

/**
 * Посчитать значение итоговой строки по алиасу.
 */
def getCalcTotalValueByAlias(def dataRows, def alias) {
    def totalRowA = getRowByAlias(dataRows, 'totalA')
    def totalRowB = getRowByAlias(dataRows, 'totalB')
    return (totalRowA.getCell(alias).getValue() ?: 0) - (totalRowB.getCell(alias).getValue() ?: 0)
}

/**
 * Получить запись из справочника по идентифкатору записи.
 *
 * @param refBookId идентификатор справончика
 * @param recordId идентификатор записи
 * @param cache кеш
 * @return
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
    //def refBook = refBookFactory.get(refBookId)
    //def refBookName = refBook.name
    //logger.error("Не удалось найти запись (id = $recordId) в справочнике $refBookName (id = $refBookId)")
    return null
}