package form_template.income.rnu36_1

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1".
 *
 * @version 68
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
    def data = getData(formData)
    def newRow = getNewRow()
    def index

    if (currentDataRow == null || getIndex(currentDataRow) == -1) {
        index = getIndexByAlias(data, 'totalA')
    } else if (currentDataRow.getAlias() == null) {
        index = getIndex(currentDataRow) + 1
    } else {
        index = getIndexByAlias(data, 'totalA')
        switch (currentDataRow.getAlias()) {
            case 'A' :
            case 'totalA' :
                index = getIndexByAlias(data, 'totalA')
                break
            case 'B' :
            case 'totalB' :
            case 'total' :
                index = getIndexByAlias(data, 'totalB')
                break
        }
    }

    data.insert(newRow, index + 1)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isFixedRow(currentDataRow)) {
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

    // список проверяемых столбцов (графа 2..7)
    def requiredColumns = ['series', 'amount', 'nominal', 'shortPositionDate',
            'balance2', 'averageWeightedPrice', 'termBondsIssued']

    for (def row : getRows(data)) {
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    /*
     * Расчеты.
     */

    // последний день отчетного месяца
    def lastDay = getLastDayReportPeriod()

    getData(formData).dataRows.each { row ->
        if (!isFixedRow(row)) {
            // графа 8
            row.percIncome = getColumn8(row, lastDay)
        }
    }
    //save(data)

    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    def totalColumns = ['amount', 'percIncome']
    def tmp
    ['A', 'B'].each {
        def row = getRowByAlias(data, it)
        def totalRow = getRowByAlias(data, 'total' + it)

        // посчитать итоги раздела
        def from = getIndex(row) + 1
        def to = getIndex(totalRow) - 1
        totalColumns.each { alias ->
            // если нет строк в разделе то в итоги 0
            tmp = (from <= to ? summ(formData, getRows(data), new ColumnRange(alias, from, to)) : 0)
            totalRow.getCell(alias).setValue(tmp)
        }
    }

    // посчитать Итого
    getRowByAlias(data, 'total').percIncome = getCalcTotalValueByAlias(data, 'percIncome')
    save(data)
    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = getData(formData)

    // список проверяемых столбцов (графа 2..8)
    def requiredColumns = ['series', 'amount', 'nominal', 'shortPositionDate',
            'balance2', 'averageWeightedPrice', 'termBondsIssued', 'percIncome']

    for (def row : getRows(data)) {
        // . Проверка обязательных полей (графа 2..7)
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns)) {
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

    for (def row : getRows(data)) {
        if (isFixedRow(row)) {
            continue
        }

        index = getIndex(row) + 1
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
        def row = getRowByAlias(data, section)
        def totalRow = getRowByAlias(data, 'total' + section)

        // посчитать итоги раздела
        def from = getIndex(row) + 1
        def to = getIndex(totalRow) - 1
        totalColumns.each { alias ->
            // если нет строк в разделе то в итоги 0
            tmp = (from <= to ? summ(formData, getRows(data), new ColumnRange(alias, from, to)) : 0)
            if (totalRow.getCell(alias).getValue() != tmp) {
                logger.error("Итоговые значений для раздела $section рассчитаны неверно!")
            }
        }
    }
    if (hasError()) {
        return false
    }

    // 5. Проверка итоговых значений по всей форме
    def totalRow = getRowByAlias(data, 'total')
    if (totalRow.percIncome != getCalcTotalValueByAlias(data, 'percIncome')) {
        logger.error('Итоговые значений рассчитаны неверно!')
        return false
    }
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def data = getData(formData)
    def cache = [:]

    for (def row : getRows(data)) {
        // графа 5
        if (!isFixedRow(row) && row.balance2 != null && null == getRecordById(28, row.balance2, cache)) {
            index = getIndex(row) + 1
            logger.warn("В строке $index балансовый счёт в справочнике отсутствует!")
        }
    }
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)

    // удалить нефиксированные строки
    def deleteRows = []
    getRows(data).each { row ->
        if (!isFixedRow(row)) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        getRows(data).removeAll(deleteRows)
    }

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceData = getData(source)
                copyRows(sourceData, data, 'A', 'totalA')
                copyRows(sourceData, data, 'B', 'totalB')
            }
        }
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
 * Проверка является ли строка фиксированной.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
}

/**
 *	Посчитать значение графы 8.
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
 * Получить номер строки в таблице (0..n).
 *
 * @param row строка
 */
def getIndex(def row) {
    row.getIndex() - 1
}

/**
 * Получить номер строки в таблице по псевдонимиу (0..n).
 */
def getIndexByAlias(def data, String rowAlias) {
    def row = getRowByAlias(data, rowAlias)
    return (row != null ? getIndex(row) : -1)
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
        def index = row.series
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"Серия\" равной $index не заполнены колонки : $errorMsg.")
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
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceData форма источник
 * @param destinationData форма приемник
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def sourceData, def destinationData, def fromAlias, def toAlias) {
    def from = getIndexByAlias(sourceData, fromAlias) + 1
    def to = getIndexByAlias(sourceData, toAlias)
    if (from > to) {
        return
    }
    def copyRows = getRows(sourceData).subList(from, to)
    getRows(destinationData).addAll(getIndexByAlias(destinationData, toAlias), copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    getRows(destinationData).eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
    save(destinationData)
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
def getCalcTotalValueByAlias(def data, def alias) {
    def totalRowA = getRowByAlias(data, 'totalA')
    def totalRowB = getRowByAlias(data, 'totalB')
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