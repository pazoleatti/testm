package form_template.income.rnu16_1.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 *  Скрипт для РНУ-16 (с 9 месяцев 2015)
 *  Форма "(РНУ-16) Регистр налогового учёта доходов по поставочным сделкам с ПФИ, не признаваемыми ФИСС,
 *                          в соответствии с учётной политикой для целей налогообложения ПАО «Сбербанк России» (с 9 месяцев 2015)"
 * formTemplateId=505 (499 у старого макета)
 *
 * графа - rowNumber
 * графа - code
 * графа - incomeType
 * графа - sum
 *
 * @author akadyrgulov
 * @author Stanislav Yasinskiy
 * */
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
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, logger)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
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

// Редактируемые атрибуты
@Field
def editableColumns = ['incomeType', 'sum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['incomeType', 'sum']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['sum']

// Дата окончания отчетного периода
@Field
def endDate = null

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        // сортируем по кодам
        dataRows.sort { getKnu(it.incomeType) }
    }

    dataRows.add(calcTotalRow(dataRows))
}


def calcTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 3
    ['rowNumber', 'fix', 'code', 'incomeType', 'sum'].each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }

    // 3. Арифметические проверки расчета итоговой строки
    checkTotalSum(dataRows, totalColumns, logger, true)
}

def String getKnu(def code) {
    return getRefBookValue(28, code)?.CODE?.stringValue
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 4, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 4 - поиск записи по графе 2
        newRow.incomeType = getRecordIdImport(28, 'CODE', row.cell[2].text(), rnuIndexRow, 2 + colOffset, false)
        def map = getRefBookValue(28, newRow.incomeType)

        // проверка графы 3
        if (map != null) {
            formDataService.checkReferenceValue(28, row.cell[3].text(), map.TYPE_INCOME?.stringValue, rnuIndexRow, 3 + colOffset, logger, false)
        }

        // графа 4
        newRow.sum = parseNumber(row.cell[4].text(), rnuIndexRow, 4 + colOffset, logger, true)

        rows.add(newRow)
    }
    def totalRow = getDataRow(dataRows, 'total')
    calcTotalSum(rows, totalRow, totalColumns)
    rows.add(totalRow)

    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow += 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // графа 4
        total.sum = parseNumber(row.cell[4].text(), rnuIndexRow, 4 + colOffset, logger, true)

        def colIndexMap = ['sum': 4]
        for (def alias : totalColumns) {
            def v1 = total[alias]
            def v2 = totalRow[alias]
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
            }
        }
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, getDataRow(dataRows, 'total'), null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 5
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
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
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
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

    // итоговая строка
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    // сравнение итогов
    compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)


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
            (headerRows[0][0]): getColumnName(tmpRow, 'rowNumber'),
            (headerRows[0][2]): getColumnName(tmpRow, 'code'),
            (headerRows[0][3]): getColumnName(tmpRow, 'incomeType'),
            (headerRows[0][4]): getColumnName(tmpRow, 'sum'),
            (headerRows[1][0]): '1',
            (headerRows[1][2]): '2',
            (headerRows[1][3]): '3',
            (headerRows[1][4]): '4',
    ]
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

    // графа 4 - поиск записи по графе 2
    def colIndex = 2
    newRow.incomeType = getRecordIdImport(28, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    def map = getRefBookValue(28, newRow.incomeType)

    // проверка графы 3
    if (map != null) {
        colIndex = 3
        formDataService.checkReferenceValue(28, values[colIndex], map.TYPE_INCOME?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 4
    colIndex = 4
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}