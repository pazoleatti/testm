package form_template.income.rnu72.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * (РНУ-72) Регистр налогового учёта уступки права требования как реализации финансовых услуг и операций с закладными
 * formTypeId=358
 * TODO:
 *      - перед логическими проверками или расчетами проверка рну-71.1 - опечатка в чтз! надо будет проверять рну-4 для заполнения графы 6, а пока графа 6 заполняется в ручную
 *
 * @author rtimerbaev
 */

// графа 1  - number
// графа 0  - forLabel - fix
// графа 2  - date
// графа 3  - nominal
// графа 4  - price
// графа 5  - income
// графа 6  - cost279
// графа 7  - costReserve
// графа 8  - loss
// графа 9  - profit

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
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
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, logger, userInfo)
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

// все атрибуты
@Field
def allColumns = ['number', 'forLabel', 'date', 'nominal', 'price', 'income', 'cost279', 'costReserve', 'loss', 'profit']

// Редактируемые атрибуты (графа 2..7)
@Field
def editableColumns = ['date', 'nominal', 'price', 'income', 'cost279', 'costReserve']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты (графа 2, 3)
@Field
def groupColumns = ['date', 'nominal']

// Проверяемые на пустые значения атрибуты (графа 1..9)
@Field
def nonEmptyColumns = ['date', 'nominal', 'price', 'income', 'cost279', 'costReserve', 'loss', 'profit']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 5..9)
@Field
def totalSumColumns = ['income', 'cost279', 'costReserve', 'loss', 'profit']

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    //sortRows(dataRows, groupColumns)

    dataRows.eachWithIndex { row, i ->
        def tmp = calcFor8or9(row)
        // графа 8
        row.loss = calc8(tmp)
        // графа 9
        row.profit = calc9(tmp)
    }

    // добавить итого
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def arithmeticCheckAlias = ['loss', 'profit']
    def values = [:]

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 3. Проверка на нулевые значения
        def hasError = true
        for (def alias : ['income', 'cost279', 'costReserve', 'loss', 'profit']) {
            if (row.getCell(alias).value != 0) {
                hasError = false
                break
            }
        }
        if (hasError) {
            rowError(logger, row, errorMsg + "Все суммы по операции нулевые!")
        }

        // 4. Арифметическая проверка графы 8, 9
        def tmp = calcFor8or9(row)
        values['loss'] = calc8(tmp)
        values['profit'] = calc9(tmp)
        checkCalc(row, arithmeticCheckAlias, values, logger, true)
    }

    // 5. Проверка итогового значений по всей форме (графа 5..9)
    checkTotalSum(dataRows, totalSumColumns, logger, true)
}

/*
 * Вспомогательные методы.
 */

/** Получить одинаковую часть расчетов для графы 8 или 9. */
def calcFor8or9(def row) {
    if (row.income == null || row.cost279 == null || row.costReserve == null) {
        return null
    }
    return row.income - (row.cost279 - row.costReserve)
}

def BigDecimal calc8(def tmp) {
    if (tmp == null) {
        return null
    }
    return ((BigDecimal)(tmp < 0 ? -tmp : 0)).setScale(2, BigDecimal.ROUND_HALF_UP)
}

def BigDecimal calc9(def tmp) {
    if (tmp == null) {
        return null
    }
    return ((BigDecimal)(tmp >= 0 ? tmp : 0)).setScale(2, BigDecimal.ROUND_HALF_UP)
}

/** Сформировать итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.forLabel = 'Итого'
    newRow.getCell('forLabel').colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalSumColumns)
    return newRow
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 9, 1)
    addTransportData(xml)

    // TODO (Ramil Timerbaev) возможно надо поменять на общее сообщение TRANSPORT_FILE_SUM_ERROR
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRows = formDataService.getDataRowHelper(formData).allCached
        checkTotalSum(dataRows, totalSumColumns, logger, false)
    }
}

void addTransportData(def xml) {
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def int rowIndex = 1

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
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 2
        def int xmlIndexCol = 2
        newRow.date = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // графа 3..9
        ['nominal', 'price', 'income', 'cost279', 'costReserve', 'loss', 'profit'].each { alias ->
            xmlIndexCol++
            newRow[alias] = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        }

        rows.add(newRow)
    }

    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.forLabel = 'Итого'
    totalRow.getCell('forLabel').setColSpan(2)
    allColumns.each { alias ->
        totalRow.getCell(alias).setStyleAlias('Контрольные суммы')
    }
    rows.add(totalRow)

    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]
        // графа 5..9
        def xmlIndexCol = 5
        ['income', 'cost279', 'costReserve', 'loss', 'profit'].each { alias ->
            totalRow[alias] = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
        // очистить итоги
        totalColumns.each { alias ->
            totalRow[alias] = null
        }
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
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
    int COLUMN_COUNT = 10
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'number')
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

    def totalRow = getTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    // сравнение итогов
    compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalSumColumns, formData, logger, false)

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
            ([(headerRows[0][0]): getColumnName(tmpRow, 'number')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'date')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'nominal')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'income')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'cost279')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'costReserve')]),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'loss')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'profit')]),
            ([(headerRows[1][0]): '1'])
    ]
    (2..9).each {
        headerMapping.add(([(headerRows[1][it]): it.toString()]))
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

    // графа 2
    def int colIndex = 2
    newRow.date = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 3..9
    ['nominal', 'price', 'income', 'cost279', 'costReserve', 'loss', 'profit'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}