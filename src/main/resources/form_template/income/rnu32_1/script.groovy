package form_template.income.rnu32_1

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-32.1) Регистр налогового учёта начисленного процентного дохода по облигациям, по которым открыта короткая позиция. Отчёт 1".
 *
 * TODO:
 *      - логическая проверка 2 - проблемы с форматом TTBBBB - http://jira.aplana.com/browse/SBRFACCTAX-4780
 *      - переделать метод calc15or16() и calc17(), пока что в FormDataServiceImpl нет метода для получение записи из справочника поиском
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
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        addRow()
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        break
    case FormDataEvent.IMPORT :
        break
    case FormDataEvent.MIGRATION :
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// все атрибуты
@Field
def allColumns = ['fix', 'number', 'name', 'code', 'issuer', 'regNumber', 'shortPositionData', 'faceValue',
        'countsBonds', 'averageWeightedPrice', 'termBondsIssued', 'maturityDate', 'currentPeriod', 'currentCouponRate',
        'incomeCurrentCoupon', 'incomePrev', 'incomeShortPosition', 'percIncome', 'totalPercIncome']

// Редактируемые атрибуты (графа 2..14)
@Field
def editableColumns = ['name', 'code', 'issuer', 'regNumber', 'shortPositionData', 'faceValue',
            'countsBonds', 'averageWeightedPrice', 'termBondsIssued', 'maturityDate',
            'currentPeriod', 'currentCouponRate', 'incomeCurrentCoupon']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1..8, 10..14, 17, 18)
@Field
def nonEmptyColumns = ['number', 'name', 'code', 'issuer', 'regNumber', 'shortPositionData',
        'faceValue', 'countsBonds', 'termBondsIssued', 'maturityDate', 'currentPeriod',
        'currentCouponRate', 'incomeCurrentCoupon', 'percIncome', 'totalPercIncome']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 8, 15..18)
@Field
def totalSumColumns = ['countsBonds', 'incomePrev', 'incomeShortPosition', 'percIncome', 'totalPercIncome']

// список алиасов подразделов
@Field
sections = ['1', '2', '3', '4', '5', '6', '7', '8']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            currentDate, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def newRow = getNewRow()
    def index

    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getIndexByAlias(dataRowHelper, 'total1')
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex() + 1
    } else {
        def alias = currentDataRow.getAlias()
        if (alias.contains('total')) {
            index = getIndexByAlias(dataRowHelper, alias)
        } else {
            index = getIndexByAlias(dataRowHelper, 'total' + alias)
        }
    }
    dataRowHelper.insert(newRow, index)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def lastDay = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def cache = [:]
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // графа 1
        row.number = row.name
        // графа 15
        row.incomePrev = calc15(row, lastDay, cache, dataRowHelper)
        // графа 16
        row.incomeShortPosition = calc16(row, lastDay, cache, dataRowHelper)
        // графа 17
        row.percIncome = calc17(row, lastDay, cache)
        // графа 18
        row.totalPercIncome = calc18(row)
    }

    sort(dataRowHelper)
    updateIndexes(dataRows)

    // посчитать итоги по разделам
    for (def section : sections) {
        def firstRow = dataRowHelper.getDataRow(dataRows, section)
        def lastRow = dataRowHelper.getDataRow(dataRows, 'total' + section)
        totalSumColumns.each {
            lastRow.getCell(it).setValue(getSum(dataRows, it, firstRow, lastRow))
        }
    }
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // алиасы графов для арифметической проверки (графа 15..18)
    def arithmeticCheckAlias = ['incomePrev', 'incomeShortPosition', 'percIncome', 'totalPercIncome']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    def lastDay = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def cache = [:]

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // TODO (Ramil Timerbaev) этой проверки нет в чтз, уточнить у аналитика нужна ли она (в рну 40.1 аналогичную проверку добавили)
        // . Проверка наименования террбанка
        if (row.number != row.name) {
            logger.error(errorMsg + 'номер территориального банка не соответствует названию.')
        }

        // 1. Проверка формата номера подразделения
        // def recordDivision = getRefBookValue(30, row.number)
        // if (recordDivision.SBRF_CODE.value != row.name) { // TODO (Ramil Timerbaev) http://jira.aplana.com/browse/SBRFACCTAX-4780
        if (false) {
            logger.error(errorMsg + 'неверно указан номер подразделения (формат: ТТВВВВ)!')
        }

        // 2. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 3. Арифметическая проверка графы 15..18
        needValue['incomePrev'] = calc15(row, lastDay, cache, dataRowHelper)
        needValue['incomeShortPosition'] = calc16(row, lastDay, cache, dataRowHelper)
        needValue['percIncome'] = calc17(row, lastDay, cache)
        needValue['totalPercIncome'] = calc18(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // Проверки соответствия НСИ
        checkNSI(15, row, 'code')       // 1. Проверка соответствия кода валюты значению из справочника	(графа 3)
        checkNSI(30, row, 'number')     // 2. Проверка соответствия поля «Номер территориального банка»	(графа 1)
        checkNSI(30, row, 'name')       // 3. Проверка соответствия поля «Наименование территориального банка» (графа 2)
        checkNSI(84, row, 'regNumber')  // 4. Проверка соответствия поля «Номер государственной регистрации» (графа 5)
    }

    // 4. Арифметическая проверка итоговых значений по разделам (графа 8, 15..18)
    for (def section : sections) {
        def firstRow = dataRowHelper.getDataRow(dataRows, section)
        def lastRow = dataRowHelper.getDataRow(dataRows, 'total' + section)
        for (def alias : totalSumColumns) {
            def value = roundValue(lastRow.getCell(alias).value ?: 0, 6)
            def sum = roundValue(getSum(dataRows, alias, firstRow, lastRow), 6)
            if (sum != value) {
                def name = getColumnName(lastRow, alias)
                logger.error("Неверно рассчитаны итоговые значения для раздела $section в графе \"$name\"!")
            }
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить нефиксированные строки
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    dataRows.removeAll(deleteRows)
    // поправить индексы, потому что они после изменения не пересчитываются
    updateIndexes(dataRows)
    

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRowHelper = formDataService.getDataRowHelper(source)
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(sourceDataRowHelper, dataRowHelper, section, 'total' + section)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/*
 * Вспомогательные методы.
 */

/** Отсорировать данные (по графе 1, 2). */
void sort(def dataRowHelper) {
    // список со списками строк каждого раздела для сортировки
    def sortRows = []
    def dataRows = dataRowHelper.allCached

    // подразделы, собрать список списков строк каждого раздела
    sections.each { section ->
        def from = getIndexByAlias(dataRowHelper, section)
        def to = getIndexByAlias(dataRowHelper, 'total' + section) - 2
        if (from <= to) {
            sortRows.add(dataRows[from..to])
        }
    }

    // отсортировать строки каждого раздела
    sortRows.each { sectionRows ->
        sectionRows.sort { def a, def b ->
            // графа 1  - number (справочник)
            // графа 2  - name (справочник)

            def recordA = getRefBookValue(30, a.number)
            def recordB = getRefBookValue(30, b.number)

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
 * @param sourceDataRowHelper данные источника
 * @param destinationDataRowHelper данные приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def sourceDataRowHelper, def destinationDataRowHelper, def fromAlias, def toAlias) {
    def sourceDataRows = sourceDataRowHelper.allCached
    def from = getIndexByAlias(sourceDataRows, fromAlias)
    def to = getIndexByAlias(sourceDataRows, toAlias) - 1
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    def destinationDataRows = destinationDataRowHelper.allCached
    destinationDataRows.addAll(getIndexByAlias(destinationDataRows, toAlias) - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationDataRows)
}

/** Получить номер строки в таблице по псевдонимиу (1..n). */
def getIndexByAlias(def dataRowHelper, String rowAlias) {
    def row = dataRowHelper.getDataRow(dataRowHelper.allCached, rowAlias)
    return (row != null ? row.getIndex() : -1)
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }

    autoFillColumns.each {
        newRow.getCell(it).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

/**
 * Получить значение для графы 15.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 * @param cache кеш
 * @param dataRowHelper данные нф
 */
def calc15(def row, def lastDay, def cache, def dataRowHelper) {
    def tmp = null
    if (row.shortPositionData == null || row.maturityDate == null) {
        return null
    }
    if (row.shortPositionData < row.maturityDate) {
        def t = lastDay - row.maturityDate
        tmp = calc15or16(row, lastDay, cache, t, dataRowHelper)
    }
    return roundValue(tmp, 2)
}

/**
 * Получить значение для графы 16.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 * @param cache кеш
 * @param dataRowHelper данные нф
 */
def calc16(def row, def lastDay, def cache, def dataRowHelper) {
    if (row.shortPositionData == null || row.maturityDate == null) {
        return null
    }
    def tmp = null
    if (row.shortPositionData < row.maturityDate) {
        def t = lastDay - row.shortPositionData
        tmp = calc15or16(row, lastDay, cache, t, dataRowHelper)
    }
    return roundValue(tmp, 2)
}

/**
 * Общая часть вычислений при расчете значения для графы 15 или 16.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 * @param cache кеш
 * @param dataRowHelper данные нф
 * @param t количество дней между последним днем месяца и графой 6 или графой 11
 */
def calc15or16(def row, def lastDay, def cache, def t, def dataRowHelper) {
    if (row.getIndex() < getIndexByAlias(dataRowHelper, '7')) {
        if (row.currentPeriod == null || row.countsBonds == null || row.incomeCurrentCoupon == null) {
            return null
        }
        // Для ценных бумаг, учтенных в подразделах 1, 2, 3, 4, 5, 6
        checkDivision(row.currentPeriod, "Деление на ноль в строке ${row.getIndex()}: графа 12.")
        return row.countsBonds * roundValue(row.incomeCurrentCoupon * t / row.currentPeriod, 2)
    } else {
        if (row.code == null || row.currentCouponRate == null || row.faceValue == null) {
            return null
        }
        // Для ценных бумаг, учтенных в подразделах 7 и 8
        // справочник 22 "Курс валют", атрибут 81 RATE - "Курс валют", атрибут 80 CODE_NUMBER - Цифровой код валюты
        // TODO (Ramil Timerbaev) переделать потом, пока что в FormDataServiceImpl нет метода для получение записи из справочника поиском
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
    if (row.countsBonds == null || row.termBondsIssued == null || row.faceValue == null ||
            row.averageWeightedPrice == null || row.shortPositionData == null || row.code == null) {
        return null
    }
    // проверка делителя на ноль
    checkDivision(row.countsBonds, 'Деление на ноль в строке ' + row.getIndex() + ': графа 8.')
    checkDivision(row.termBondsIssued, 'Деление на ноль в строке ' + row.getIndex() + ': графа 10.')

    def tmp = ((row.faceValue / row.countsBonds - row.averageWeightedPrice) *
            ((lastDay - row.shortPositionData) / row.termBondsIssued) * row.countsBonds)
    tmp = roundValue(tmp, 2)

    // справочник 22 "Курс валют", атрибут 81 RATE - "Курс валют", атрибут 80 CODE_NUMBER - Цифровой код валюты
    // TODO (Ramil Timerbaev) переделать потом, пока что в FormDataServiceImpl нет метода для получение записи из справочника поиском
    def record22 = getRecord(22, 'CODE_NUMBER', row.code, lastDay, cache)
    tmp = tmp * record22.RATE.value
    return roundValue(tmp, 2)
}

def calc18(def row) {
    if (row.incomePrev == null || row.incomeShortPosition == null || row.percIncome == null) {
        return null
    }
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

// TODO (Ramil Timerbaev) потом убрать
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

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}