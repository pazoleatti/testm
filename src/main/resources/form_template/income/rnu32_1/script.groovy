package form_template.income.rnu32_1

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-32.1) Регистр налогового учёта начисленного процентного дохода по облигациям, по которым открыта короткая позиция. Отчёт 1".
 *
 * TODO:
 *      - логическая проверка 2 - проблемы с форматом TTBBBB - http://jira.aplana.com/browse/SBRFACCTAX-4780
 *      - импорт и миграция еще не сделаны
 *
 * @author rtimerbaev
 */

// графа 0  - fix
// графа 1  - number                - Номер территориального банка          - атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
// графа 2  - name                  - Наименование территориального банка   - атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
// графа 3  - code                  - Код валюты номинала                   - атрибут 64 - CODE - "Код валюты. Цифровой", справочник 15 "Общероссийский классификатор валют"
// графа 4  - issuer                - Эмитент
// графа 5  - regNumber             - Номер государственной регистрации     - атрибут 813 - REG_NUM - "Государственный регистрационный номер", справочник 84 "Ценные бумаги"
// графа 6  - shortPositionData     - Дата открытия короткой позиции
// графа 7  - faceValue             - Номинальная стоимость (ед. валюты)
// графа 8  - countsBonds           - Количество облигаций (шт.)
// графа 9  - averageWeightedPrice  - Средневзвешенная цена одной облигации на дату, когда выпуск признан размещенным (ед. вал.)
// графа 10 - termBondsIssued       - Срок обращения выпуска (дни)
// графа 11 - maturityDate          - Дата погашения предыдущего купона
// графа 12 - currentPeriod         - Текущий купонный период (дни)
// графа 13 - currentCouponRate     - Ставка текущего купона, (% год.)
// графа 14 - incomeCurrentCoupon   - Объявленный доход по текущему купону (руб.коп.)
// графа 15 - incomePrev            - Доход с даты погашения предыдущего купона (руб.коп.)
// графа 16 - incomeShortPosition   - Доход с даты открытия короткой позиции (руб.коп.)
// графа 17 - percIncome            - Процентный доход по дисконтным облигациям (руб.коп.)
// графа 18 - totalPercIncome       - Всего процентный доход (руб.коп.)

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkUniq()
        break
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        calc() && logicalCheck()
        break
    case FormDataEvent.ADD_ROW :
        addRow()
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
        logicalCheck()
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        break
    case FormDataEvent.IMPORT :
    case FormDataEvent.MIGRATION :
        importData()
        break
}

/**
 * Проверяет уникальность в отчётном периоде и вид
 * (не был ли ранее сформирован отчет, параметры которого совпадают с параметрами, указанными пользователем )
 */
void checkUniq() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

/**
 * Добавить новую строку.
 */
def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def newRow = getNewRow()
    def index

    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getIndexByAlias(dataRows, 'total1')
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex()
    } else {
        def alias = currentDataRow.getAlias()
        if (alias.contains('total')) {
            index = getIndexByAlias(dataRows, alias)
        } else {
            index = getIndexByAlias(dataRows, 'total' + alias)
        }
    }
    dataRowHelper.insert(newRow, index)
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

    // список проверяемых столбцов (графа 2..8, 10..14)
    def requiredColumns = ['name', 'code', 'issuer', 'regNumber', 'shortPositionData', 'faceValue',
            'countsBonds', /*'averageWeightedPrice',*/ 'termBondsIssued', 'maturityDate',
            'currentPeriod', 'currentCouponRate', 'incomeCurrentCoupon']
    // обязательность заполнения полей
    for (def row : dataRows) {
        if (row.getAlias() == null && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    def lastDay = getLastDayReportPeriod()
    def cache = [:]
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // графа 1
        row.number = row.name
        // графа 15
        row.incomePrev = calc15(row, lastDay, cache, dataRows)
        // графа 16
        row.incomeShortPosition = calc16(row, lastDay, cache, dataRows)
        // графа 17
        row.percIncome = calc17(row, lastDay, cache)
        // графа 18
        row.totalPercIncome = calc18(row)
    }

    sort(dataRows)

    // посчитать итоги по разделам
    def sumColumns = getSumColumns()
    getAliasesSections().each { section ->
        def firstRow = getRowByAlias(dataRows, section)
        def lastRow = getRowByAlias(dataRows, 'total' + section)
        sumColumns.each {
            lastRow.getCell(it).setValue(getSum(dataRows, it, firstRow, lastRow))
        }
    }
    dataRowHelper.save(dataRows)
    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // список проверяемых столбцов (графа 1..8, 10..14, 17, 18)
    def requiredColumns = ['number', 'name', 'code', 'issuer', 'regNumber', 'shortPositionData',
            'faceValue', 'countsBonds', 'termBondsIssued', 'maturityDate', 'currentPeriod',
            'currentCouponRate', 'incomeCurrentCoupon', 'percIncome', 'totalPercIncome']
    // 2. Обязательность заполнения полей
    for (def row : dataRows) {
        if (row.getAlias() == null && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    // алиасы графов для арифметической проверки (графа 15..18)
    def arithmeticCheckAlias = ['incomePrev', 'incomeShortPosition', 'percIncome', 'totalPercIncome']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    def colNames = []
    def index
    def errorMsg
    def lastDay = getLastDayReportPeriod()
    def cache = [:]

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        index = row.getIndex()
        errorMsg = "В строке $index "

        // TODO (Ramil Timerbaev) этой проверки нет в чтз, уточнить у аналитика нужна ли она (в рну 40.1 аналогичную проверку добавили)
        // . Проверка наименования террбанка
        if (row.number != row.name) {
            logger.error(errorMsg + 'номер территориального банка не соответствует названию.')
            return false
        }

        // 1. Проверка формата номера подразделения
        // def recordDivision = getRecordById(30, row.number, cache)
        // if (recordDivision.SBRF_CODE.value != row.name) { // TODO (Ramil Timerbaev) http://jira.aplana.com/browse/SBRFACCTAX-4780
        if (false) {
            logger.error(errorMsg + 'неверноуказан номер подразделения (формат: ТТВВВВ)!')
            return false
        }

        // 3. Арифметическая проверка графы 15..18
        needValue['incomePrev'] = calc15(row, lastDay, cache, dataRows)
        needValue['incomeShortPosition'] = calc16(row, lastDay, cache, dataRows)
        needValue['percIncome'] = calc17(row, lastDay, cache)
        needValue['totalPercIncome'] = calc18(row)

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

    // 4. Арифметическая проверка итоговых значений по разделам (графа 8, 15..18)
    def firstRow
    def lastRow
    def sumColumns = getSumColumns()
    for (def section : getAliasesSections()) {
        firstRow = getRowByAlias(dataRows, section)
        lastRow = getRowByAlias(dataRows, 'total' + section)
        for (def col : sumColumns) {
            def value = roundValue(lastRow.getCell(col).getValue() ?: 0, 6)
            def sum = roundValue(getSum(dataRows, col, firstRow, lastRow), 6)
            if (sum != value) {
                def name = getColumnName(lastRow, col)
                def number = section
                logger.error("Неверно рассчитаны итоговые значения для раздела $number в графе \"$name\"!")
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

        // 1. Проверка соответствия кода валюты значению из справочника	(графа 3)
        if (getRecordById(15, row.code, cache) == null) {
            logger.warn(errorMsg + "неверный номер территориального банка!")
        }

        // 2. Проверка соответствия поля «Номер территориального банка»	(графа 1)
        // 3. Проверка соответствия поля «Наименование территориального банка» (графа 2)
        def recordDivision = getRecordById(30, row.number, cache)
        if (recordDivision == null) {
            logger.error(errorMsg + 'неверный номер территориального банка!')
            logger.error(errorMsg + 'неверное наименование территориального банка/ подразделения Центрального аппарата!')
            return false
        }

        // 4. Проверка соответствия поля «Номер государственной регистрации» (графа 5)
        if (getRecordById(84, row.regNumber, cache) == null) {
            logger.warn(errorMsg + 'неверный номер государственной регистрации!')
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
    deleteRows(dataRowHelper)

    // собрать из источников строки и разместить соответствующим разделам
    def aliasesSections = getAliasesSections()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRowHelper = formDataService.getDataRowHelper(source)
                def sourceDataRows = sourceDataRowHelper.getAllCached()
                // копирование данных по разделам
                aliasesSections.each { section ->
                    copyRows(sourceDataRows, dataRows, section, 'total' + section)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Получение импортируемых данных.
 */
void importData() {

}

/*
 * Вспомогательные методы.
 */

/**
 * Отсорировать данные (по графе 1, 2).
 */
void sort(def dataRows) {
    // список со списками строк каждого раздела для сортировки
    def sortRows = []
    def from
    def to

    // подразделы, собрать список списков строк каждого раздела
    getAliasesSections().each { section ->
        from = getIndexByAlias(dataRows, section)
        to = getIndexByAlias(dataRows, 'total' + section) - 2
        if (from <= to) {
            sortRows.add(dataRows[from..to])
        }
    }

    def cache = [:]
    // отсортировать строки каждого раздела
    sortRows.each { sectionRows ->
        sectionRows.sort { def a, def b ->
            // графа 1  - number (справочник)
            // графа 2  - name (справочник)

            def recordA = getRecordById(30, a.number, cache)
            def recordB = getRecordById(30, b.number, cache)

            def numberA = (recordA != null ? recordA.SBRF_CODE.value : null)
            def numberB = (recordB != null ? recordB.SBRF_CODE.value : null)

            def nameA = (recordA != null ? recordA.NAME.value : null)
            def nameB = (recordB != null ? recordB.NAME.value : null)

            if (numberA == numberB) {
                return nameA <=> nameB
            }
            return numberA <=> numberB
        }
    }
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
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getIndexByAlias(destinationDataRows, toAlias) - 1, copyRows)
    updateIndexes(destinationDataRows)
}

/**
 * Получить список алиасов подразделов
 */
def getAliasesSections() {
    return ['1', '2', '3', '4', '5', '6', '7', '8']
}

/**
 * Получить список графов для которых вычисляются итоги (графа 8, 15..18).
 */
def getSumColumns() {
    return ['countsBonds', 'incomePrev', 'incomeShortPosition', 'percIncome', 'totalPercIncome']
}

/**
 * Получить номер строки в таблице по псевдонимиу (1..n).
 */
def getIndexByAlias(def dataRows, String rowAlias) {
    def row = getRowByAlias(dataRows, rowAlias)
    return (row != null ? row.getIndex() : -1)
}

/**
 * Поправить индексы, потому что они после вставки не пересчитываются.
 */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

/**
 * Получить строку по алиасу.
 *
 * @param dataRows данные нф
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
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()

    // графа 2..14
    ['name', 'code', 'issuer', 'regNumber', 'shortPositionData', 'faceValue',
            'countsBonds', 'averageWeightedPrice', 'termBondsIssued', 'maturityDate',
            'currentPeriod', 'currentCouponRate', 'incomeCurrentCoupon'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }

    // графа 15..18
    ['incomePrev', 'incomeShortPosition', 'percIncome', 'totalPercIncome'].each {
        newRow.getCell(it).styleAlias = 'Вычисляемая'
    }
    return newRow
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
        logger.error("В $index строке не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Получить значение для графы 15.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 * @param cache кеш
 * @param dataRows строки нф
 */
def calc15(def row, def lastDay, def cache, def dataRows) {
    def tmp = null
    if (row.shortPositionData < row.maturityDate) {
        def t = lastDay - row.maturityDate
        tmp = calc15or16(row, lastDay, cache, t, dataRows)
    }
    return roundValue(tmp, 2)
}

/**
 * Получить значение для графы 16.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 * @param cache кеш
 * @param dataRows данные нф
 */
def calc16(def row, def lastDay, def cache, def dataRows) {
    def tmp = null
    if (row.shortPositionData < row.maturityDate) {
        def t = lastDay - row.shortPositionData
        tmp = calc15or16(row, lastDay, cache, t, dataRows)
    }
    return roundValue(tmp, 2)
}

/**
 * Общая часть вычислений при расчете значения для графы 15 или 16.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 * @param cache кеш
 * @param dataRows строки нф
 * @param t количество дней между последним днем месяца и графой 6 или графой 11
 */
def calc15or16(def row, def lastDay, def cache, def t, def dataRows) {
    if (row.getIndex() < getIndexByAlias(dataRows, '7')) {
        // Для ценных бумаг, учтенных в подразделах 1, 2, 3, 4, 5, 6
        checkDivision(row.currentPeriod, "Деление на ноль в строке ${row.getIndex()}: графа 12.")
        return row.countsBonds * roundValue(row.incomeCurrentCoupon * t / row.currentPeriod, 2)
    } else {
        // Для ценных бумаг, учтенных в подразделах 7 и 8
        // справочник 22 "Курс валют", атрибут 81 RATE - "Курс валют", атрибут 80 CODE_NUMBER - Цифровой код валюты
        def record22 = getRecord(22, 'CODE_NUMBER', row.code, lastDay, cache)
        return roundValue(row.currentCouponRate * row.faceValue * t / 360, 2) * record22.RATE.value
    }
}

/**
 * Получить значение для графы 17.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 * @param cache кеш
 */
def calc17(def row, def lastDay, def cache) {
    // проверка делителя на ноль
    checkDivision(row.countsBonds, 'Деление на ноль в строке ' + row.getIndex() + ': графа 8.')
    checkDivision(row.termBondsIssued, 'Деление на ноль в строке ' + row.getIndex() + ': графа 10.')

    def tmp = ((row.faceValue / row.countsBonds - row.averageWeightedPrice) *
            ((lastDay - row.shortPositionData) / row.termBondsIssued) * row.countsBonds)
    tmp = roundValue(tmp, 2)

    // справочник 22 "Курс валют", атрибут 81 RATE - "Курс валют", атрибут 80 CODE_NUMBER - Цифровой код валюты
    def record22 = getRecord(22, 'CODE_NUMBER', row.currencyCode, lastDay, cache)
    tmp = tmp * record22.RATE.value
    return roundValue(tmp, 2)
}

def calc18(def row) {
    return row.incomePrev + row.incomeShortPosition + row.percIncome
}

/**
 * Получить сумму столбца.
 */
def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
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
    // def refBook = refBookFactory.get(refBookId)
    // def refBookName = refBook.name
    // logger.error("Не удалось найти запись (id = $recordId) в справочнике $refBookName (id = $refBookId)!")
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
 * @param msg сообщение
 *
 */
void checkDivision(def division, def msg) {
    if (division == 0) {
        throw new ServiceLoggerException(msg, logger.getEntries())
    }
}

/**
 * Удалить нефиксированные строки.
 */
void deleteRows(def dataRowHelper) {
    def dataRows = dataRowHelper.getAllCached()
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    dataRowHelper.delete(deleteRows)
}