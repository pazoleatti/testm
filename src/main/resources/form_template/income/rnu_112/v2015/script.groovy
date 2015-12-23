package form_template.income.rnu_112.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

/**
 * 824 - РНУ 112. Регистр налогового учёта доходов по сделкам РЕПО, возникающих в связи с применением в
 * сделках c Взаимозависимыми лицами и резидентами оффшорных зон ставок, не соответствующих рыночному уровню
 *
 * formTemplateId=824
 *
 * @author Bulat Kinzyabulatov
 */

// rowNumber        (1) - № пп
// fix
// dealNum          (2) - Номер сделки
// name             (3) - Наименование Взаимозависимого лица/резидента оффшорной зоны
// countryName      (4) - Страна местонахождения контрагента
// currency         (5) - Валюта расчетов
// date1Part        (6) - Дата первой части сделки
// date2Part        (7) - Дата второй части сделки
// dealRate         (8) - Ставка сделки, % годовых
// dealLeftSum      (9) - Сумма остаточных обязательств (требований) контрагента по сделке
// bondSum          (10) - Сумма выплаты по ценным бумагам
// payDate          (11) - Дата выплаты (гр. 10)
// accrStartDate    (12) - Период начисления доходов на сумму остаточных обязательств контрагента (гр. 9). Дата начала начисления
// accrEndDate      (13) - Период начисления доходов на сумму остаточных обязательств контрагента (гр. 9). Дата окончания начисления
// yearBase         (14) - База (360/365/366)
// dealIncome       (15) - Доходы по сделке
// rateDiff         (16) - Отклонение от рыночной процентной ставки для целей налогообложения
// incomeCorrection (17) - Сумма корректировки доходов

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def allColumns = ['rowNumber', 'fix', 'dealNum', 'name', 'countryName', 'currency', 'date1Part', 'date2Part', 'dealRate', 'dealLeftSum',
                  'bondSum', 'payDate', 'accrStartDate', 'accrEndDate', 'yearBase', 'dealIncome', 'rateDiff', 'incomeCorrection']

// Редактируемые атрибуты
@Field
def editableColumns = ['dealNum', 'name',  'currency', 'date1Part', 'date2Part', 'dealRate', 'dealLeftSum', 'bondSum', 'dealIncome',
                       'rateDiff']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'countryName', 'payDate', 'accrStartDate', 'accrEndDate', 'yearBase', 'incomeCorrection']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['dealNum', 'name', 'countryName', 'currency', 'date1Part', 'date2Part', 'dealRate', 'dealLeftSum',
                       'bondSum', 'payDate', 'accrStartDate', 'accrEndDate', 'yearBase', 'dealIncome', 'rateDiff', 'incomeCorrection']

// Группируемые атрибуты
@Field
def groupColumns = ['name', 'reasonNumber', 'reasonDate']

@Field
def totalColumns = ['incomeCorrection']

// Дата окончания отчетного периода
@Field
def endDate = null

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка на заполнение граф
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // TODO возможно лишняя
        // 2. Проверка заполнения графы 2 справочным значением по ВЗЛ/РОЗ
        if (row.name) {
            def typeId = getRefBookValue(520, row.name).TYPE?.referenceValue
            if (typeId) {
                def type = getRefBookValue(525, typeId).CODE?.stringValue
                if (!['ВЗЛ', 'РОЗ'].contains(type)) {
                    def msg1 = row.getCell('name').column.name
                    rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть заполнено наименованием Взаимозависимого лица/резидента оффшорной зоны!")
                }
            }
        }

        if (row.yearBase != null && ![360, 365, 366].contains(row.yearBase)) { // TODO уточнить текст
            rowError(logger, row, "Строка $rowNum: Неверное значение в графе «${getColumnName(row, 'yearBase')}». Допустимые значения: 360, 365, 366")
        }
    }

    // 9. Проверка итоговых значений пофиксированной строке «Итого»
    if (dataRows.find { it.getAlias() == 'total' }) {
        checkTotalSum(dataRows, totalColumns, logger, true)
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Сортировка
    sortRows(dataRows)

    updateIndexes(dataRows)

    for (row in dataRows) {
        if(row.getAlias() != null){
            continue
            // TODO добавить расчеты
        }
    }

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    updateIndexes(dataRows)
}

void sortRows(def dataRows) {
    dataRows.sort{ def rowA, def rowB ->
        def aValue = rowA.dealNum
        def bValue = rowB.dealNum
        if (aValue != bValue) {
            return aValue <=> bValue
        }
        aValue = rowA.accrStartDate
        bValue = rowB.accrStartDate
        if (aValue != bValue) {
            return bValue <=> aValue
        }
        return 0
    }
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 17
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = '№ пп'
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()
    def totalRowFromFile = null

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        rowIndex++
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Итого") {
            totalRowFromFile = getNewTotalFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // сравнение итогов
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    if (totalRowFromFile) {
        compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][12]): 'Период начисления доходов на сумму остаточных обязательств контрагента (гр. 9)']),
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'dealNum')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'currency')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'date1Part')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'date2Part')]),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'dealRate')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'dealLeftSum')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'bondSum')]),
            ([(headerRows[0][11]): getColumnName(tmpRow, 'payDate')]),
            ([(headerRows[1][12]): 'Дата начала начисления']),
            ([(headerRows[1][13]): 'Дата окончания начисления']),
            ([(headerRows[0][14]): getColumnName(tmpRow, 'yearBase')]),
            ([(headerRows[0][15]): getColumnName(tmpRow, 'dealIncome')]),
            ([(headerRows[0][16]): getColumnName(tmpRow, 'rateDiff')]),
            ([(headerRows[0][17]): getColumnName(tmpRow, 'incomeCorrection')]),
            ([(headerRows[2][0]): '1'])
    ]
    (2..17).each {
        headerMapping.add([(headerRows[2][it]): it.toString()])
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    def int colIndex = 2

    // графа 2
    newRow.dealNum = values[2]
    colIndex++

    // графа 3
    newRow.name = getRefBookRecordIdImport(520L, getReportPeriodEndDate(), "LOWER(NAME) = LOWER('${values[3]}') AND COUNTRY_CODE = ${values[4]}", getColumnName(newRow, 'name'), fileRowIndex, colIndex)
    colIndex++

    // графа 4
    colIndex++

    // графа 5
    newRow.currency = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 6
    newRow.date1Part = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.date2Part = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графы 8-10
    ['dealRate', 'dealLeftSum', 'bondSum'].each{
        newRow[it]= parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    // графы 11-13
    ['payDate', 'accrStartDate', 'accrEndDate'].each{
        newRow[it]= parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    // графы 14-17
    ['yearBase', 'dealIncome', 'rateDiff', 'incomeCorrection'].each{
        newRow[it]= parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    return newRow
}

@Field
String REF_BOOK_NOT_FOUND_IMPORT_ERROR_NEW = "Проверка файла: Строка %d, столбец %s: В справочнике «%s» не найдена запись для графы «%s» актуальная на дату %s!";

@Field
String REF_BOOK_TOO_MANY_FOUND_IMPORT_ERROR_NEW = "Проверка файла: Строка %d, столбец %s: В справочнике «%s» найдено более одной записи для графы «%s» актуальная на дату %s!";

/**
 * Получить id записи из справочника по фильтру.
 * Не используется унифицированный метод formDataService.getRefBookRecordIdImport потому что в нем нет возможности
 * искать запись по фильтру.
 *
 * @param refBookId идентификатор справочника
 * @param date дата актуальности записи
 * @param filter фильтр для поиска
 * @param columnName название графы формы для которого ищется значение
 * @param rowIndex номер строки в файле
 * @param colIndex номер колонки в файле
 * @param required фатальность
 */
Long getRefBookRecordIdImport(Long refBookId, Date date, String filter, String columnName,
                              int rowIndex, int colIndex, boolean required = false) {
    if (refBookId == null) {
        return null
    }
    def records = refBookFactory.getDataProvider(refBookId).getRecords(date, null, filter, null)
    if (records != null && records.size() == 1) {
        return records.get(0).record_id.value
    }

    def tooManyValue = (records != null && records.size() > 1)
    RefBook rb = refBookFactory.get(refBookId)

    String msg = String.format(tooManyValue ? REF_BOOK_TOO_MANY_FOUND_IMPORT_ERROR_NEW : REF_BOOK_NOT_FOUND_IMPORT_ERROR_NEW,
            rowIndex, getXLSColumnName(colIndex), rb.getName(), columnName, date.format('dd.MM.yyyy'))
    if (required) {
        throw new ServiceException("%s", msg)
    } else {
        logger.warn("%s", msg)
    }
    return null
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(dataRows.findAll{ it.getAlias() == null})
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

/**
 * Получить итоговую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewTotalFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) { // TODO
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 17
    colIndex = 17
    newRow.incomeCorrection = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}