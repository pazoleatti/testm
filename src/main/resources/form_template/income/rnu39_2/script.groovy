package form_template.income.rnu39_2

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-39.2) Регистр налогового учёта процентного дохода по коротким позициям. Отчёт 2(квартальный)".
 *
 * @version 68
 *
 * TODO:
 *      - уточнить как получать дату последнего и отчетного дня для месяца в методе getLastDayReportPeriod() getReportDate()
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
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        calc() && logicalCheck() && checkNSI()
        break
}

// графа 1  - currencyCode
// графа 2  - issuer
// графа 3  - regNumber
// графа 4  - amount
// графа 5  - cost
// графа 6  - shortPositionOpen
// графа 7  - shortPositionClose
// графа 8  - pkdSumOpen
// графа 9  - pkdSumClose
// графа 10 - maturityDatePrev
// графа 11 - maturityDateCurrent
// графа 12 - currentCouponRate
// графа 13 - incomeCurrentCoupon
// графа 14 - couponIncome
// графа 15 - totalPercIncome

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    def newRow = getNewRow()
    def index

    if (currentDataRow == null || getIndex(currentDataRow) == -1) {
        index = getIndexByAlias(data, 'totalA1')
    } else if (currentDataRow.getAlias() == null) {
        index = getIndex(currentDataRow) + 1
    } else {
        def alias = currentDataRow.getAlias()
        if (alias == 'A') {
            index = getIndexByAlias(data, 'totalA1')
        } else if (alias == 'B') {
            index = getIndexByAlias(data, 'totalB1')
        } else if (alias.contains('total')) {
            index = getIndexByAlias(data, alias)
        } else {
            index = getIndexByAlias(data, 'total' + alias)
        }
    }
    data.insert(newRow, index + 1)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (currentDataRow.getAlias() == null) {
        def data = getData(formData)
        data.delete(currentDataRow)
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

    // список проверяемых столбцов (графа 1..13)
    def requiredColumns = ['currencyCode', 'issuer', 'regNumber', 'amount', 'cost', 'shortPositionOpen',
            /*'shortPositionClose',*/ 'pkdSumOpen', 'pkdSumClose', 'maturityDatePrev',
            'maturityDateCurrent', 'currentCouponRate', 'incomeCurrentCoupon']
    for (def row : getRows(data)) {
        if (isFixedRow(row)) {
            continue
        }

        // для раздела А графа 7 необязательна
        if (isSectionA(data, row)) {
            requiredColumns -= 'shortPositionClose'
        } else {
            requiredColumns += 'shortPositionClose'
        }

        if (!checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    /*
     * Расчеты.
     */

    sort(data)

    def dateStart = getFirstDayReportPeriod()
    def dateEnd =  getLastDayReportPeriod()
    def cache = [:]
    for (def row :getRows(data)) {
        if (isFixedRow(row)) {
            continue
        }
        // графа 14
        row.couponIncome = calc14(data, row, dateStart, dateEnd, cache)

        // графа 15
        row.totalPercIncome = calc15(data, row)
    }

    // посчитать итоги по разделам
    def firstRow
    def lastRow
    def sumColumns = getSumColumns()
    getAliasesSections().each { section ->
        firstRow = getRowByAlias(data, section)
        lastRow = getRowByAlias(data, 'total' + section)
        sumColumns.each {
            lastRow.getCell(it).setValue(getSum(it, firstRow, lastRow))
        }
    }
    save(data)
    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = getData(formData)

    def reportDay = getReportDate()
    def dateStart = getFirstDayReportPeriod()
    def dateEnd =  getLastDayReportPeriod()

    // список проверяемых столбцов (графа 1..13)
    def requiredColumns = ['currencyCode', 'issuer', 'regNumber', 'amount', 'cost', 'shortPositionOpen',
            /*'shortPositionClose',*/ 'pkdSumOpen', 'pkdSumClose', 'maturityDatePrev',
            'maturityDateCurrent', 'currentCouponRate', 'incomeCurrentCoupon']
    for (def row : getRows(data)) {
        if (isFixedRow(row)) {
            continue
        }

        // для раздела А графа 7 необязательна
        if (isSectionA(data, row)) {
            requiredColumns -= 'shortPositionClose'
        } else {
            requiredColumns += 'shortPositionClose'
        }

        // 3. Обязательность заполнения поля графа 1..13
        if (!checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    // алиасы графов для арифметической проверки (графа 14, 15)
    def arithmeticCheckAlias = ['couponIncome', 'totalPercIncome']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    def colNames = []
    def index
    def errorMsg
    def isA
    def cache = [:]

    for (def row : getRows(data)) {
        if (isFixedRow(row)) {
            continue
        }

        index = getIndex(row) + 1
        errorMsg = "В строке $index "

        // 1. Проверка даты первой части сделки
        if (row.shortPositionOpen > reportDay) {
            logger.error(errorMsg + 'неверно указана дата первой части сделки!')
            return false
        }

        // 2. Проверка даты второй части сделки
        // Графа 7 (раздел А) = не заполнена;
        // Графа 7 (раздел Б) - принадлежит отчётному периоду
        isA = isSectionA(data, row)
        if ((isA && row.shortPositionClose != null) ||
                (!isA && row.shortPositionClose < dateStart || dateEnd < row.shortPositionClose)) {
            logger.error(errorMsg + 'неверно указана дата второй части сделки!')
            return false
        }

        // 4. Арифметическая проверка графы 14 и 15
        needValue['couponIncome'] = calc14(data, row, dateStart, dateEnd, cache)
        needValue['totalPercIncome'] = calc15(data, row)

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
    }

    // 5. Проверка итоговых значений для подраздела 1..5 раздела А и Б
    def firstRow
    def lastRow
    def sumColumns = getSumColumns()
    getAliasesSections().each { section ->
        firstRow = getRowByAlias(data, section)
        lastRow = getRowByAlias(data, 'total' + section)
        for (def col : sumColumns) {
            def value = lastRow.getCell(col).getValue() ?: 0
            if (value != getSum(col, firstRow, lastRow)) {
                def name = getColumnName(lastRow, col)
                def number = section[1]
                def sectionName = (section.contains('A') ? 'А' : 'Б')
                logger.error("Неверно рассчитаны итоговые значения для подраздела $number раздела $sectionName в графе \"$name\"!")
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
    def cache = [:]
    for (def row : getRows(data)) {
        if (isFixedRow(row)) {
            continue
        }

        index = getIndex(row) + 1
        errorMsg = "В строке $index "

        // 1. Проверка кода валюты со справочным (графа 1)
        if (getRecordById(15, row.currencyCode, cache) == null) {
            logger.warn(errorMsg + 'неверный код валюты!')
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
    data.delete(deleteRows)

    // собрать из источников строки и разместить соответствующим разделам
    def aliasesSections = getAliasesSections()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceData = getData(source)
                // копирование данных по разделам
                aliasesSections.each { section ->
                    copyRows(sourceData, data, section, 'total' + section)
                }
            }
        }
    }
    save(data)
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
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Проверка принадлежит ли строка разделу A.
 */
def isSectionA(def data, def row) {
    return row != null && getIndex(row) < getIndex(getRowByAlias(data, 'B'))
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias, def rowStart, def rowEnd) {
    def from = getIndex(rowStart) + 1
    def to = getIndex(rowEnd) - 1
    if (from > to) {
        return 0
    }
    return summ(formData, getRows(getData(formData)), new ColumnRange(columnAlias, from, to))
}

/**
 * Получить номер строки в таблице (0..n).
 */
def getIndex(def row) {
    return row.getIndex() - 1
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
 * Получить номер строки в таблице по псевдонимиу (0..n).
 */
def getIndexByAlias(def data, String rowAlias) {
    def row = getRowByAlias(data, rowAlias)
    return (row != null ? getIndex(row) : -1)
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
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void update(def data) {
    data.update(getRows(data))
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
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceData хелпер источника
 * @param destinationData хелпер приемника
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
    // save(destinationData)
}

/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()

    // графа 1..13
    ['currencyCode', 'issuer', 'regNumber', 'amount', 'cost', 'shortPositionOpen',
            'shortPositionClose', 'pkdSumOpen', 'pkdSumClose', 'maturityDatePrev',
            'maturityDateCurrent', 'currentCouponRate', 'incomeCurrentCoupon'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }

    // графа 14..15
    ['couponIncome', 'totalPercIncome'].each {
        newRow.getCell(it).styleAlias = 'Вычисляемая'
    }
    return newRow
}

// TODO (Ramil Timerbaev) уточнить как получать дату последнего дня для месяца
/**
 * Получить дату окончания отчетного периода (месяца)
 */
def getLastDayReportPeriod() {
    def last = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (last ? last.time : null)
}

/**
 * Получить дату начала отчетного периода (месяца)
 */
def getFirstDayReportPeriod() {
    def last = reportPeriodService.getStartDate(formData.reportPeriodId)
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
 * Получить значение для графы 14.
 *
 * @param data хелпер нф
 * @param row строка нф
 * @param dateStart дата начала отчетного периода
 * @param dateEnd дата окончания отчетного периода
 * @param cache кеш
 */
def calc14(def data, def row, def dateStart, def dateEnd, def cache) {
    def tmp
    def isA = isSectionA(data, row)
    if ((isA && dateStart <= row.maturityDateCurrent && row.maturityDateCurrent <= dateEnd) ||
            (!isA && row.shortPositionOpen <= row.maturityDateCurrent && row.maturityDateCurrent <= row.shortPositionClose)) {
        // справочник 15 "Общероссийский классификатор валют", атрибут 64 CODE - "Код валюты. Цифровой"
        def record15 = getRecordById(15, row.currencyCode, cache)
        if (record15 != null && record15.CODE.value == '810') {
            tmp = row.amount * row.incomeCurrentCoupon
        } else {
            def t = row.maturityDateCurrent - row.maturityDatePrev
            // справочник 22 "Курс валют", атрибут 81 RATE - "Курс валют", атрибут 80 CODE_NUMBER - Цифровой код валюты
            def record22 = getRecord(22, 'CODE_NUMBER', row.currencyCode, row.maturityDateCurrent, cache)
            tmp = roundValue(row.currentCouponRate * row.cost * t / 360, 2) * record22.RATE.value
        }
    } else {
        tmp = 0
    }
    return roundValue(tmp, 2)
}

def calc15(def data, def row) {
    return (isSectionA(data, row) ? row.couponIncome : row.pkdSumClose + row.couponIncome - row.pkdSumOpen)
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
    logger.error("Не удалось найти запись (id = $recordId) в справочнике (id = $refBookId)!")
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
    logger.error("Не удалось найти запись в справочнике (id = $refBookId) с атрибутом $code равным $value!")
    return null
}

/**
 * Отсорировать данные (по графе 1, 2).
 *
 * @param data данные нф (хелпер)
 */
void sort(def data) {
    def rows = getRows(data)
    // список со списками строк каждого раздела для сортировки
    def sortRows = []
    def from
    def to

    // подразделы, собрать список списков строк каждого раздела
    getAliasesSections().each { section ->
        from = getIndexByAlias(data, section) + 1
        to = getIndexByAlias(data, 'total' + section) - 1
        if (from <= to) {
            sortRows.add(rows[from..to])
        }
    }

    def cache = [:]
    // отсортировать строки каждого раздела
    sortRows.each { sectionRows ->
        sectionRows.sort { def a, def b ->
            // графа 1  - currencyCode (справочник)
            // графа 2  - issuer
            def record15A = getRecordById(15, a.currencyCode, cache)
            def record15B = getRecordById(15, b.currencyCode, cache)
            def codeA = (record15A != null ? record15A.CODE.value : null)
            def codeB = (record15B != null ? record15B.CODE.value : null)
            if (codeA == codeB) {
                return a.issuer <=> b.issuer
            }
            return codeA <=> codeB
        }
    }
}

/**
 * Получить список алиасов подразделов
 */
def getAliasesSections() {
    return ['A1', 'A2', 'A3', 'A4', 'A5', 'B1', 'B2', 'B3', 'B4', 'B5']
}

/**
 * Получить список графов для которых вычисляются итоги (графа 4, 5, 8, 9, 14, 15).
 */
def getSumColumns() {
    return ['amount', 'cost', 'pkdSumOpen', 'pkdSumClose', 'couponIncome', 'totalPercIncome']
}