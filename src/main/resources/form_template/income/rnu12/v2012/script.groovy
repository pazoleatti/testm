package form_template.income.rnu12.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Скрипт для РНУ-12 (rnu12.groovy).
 * Форма "(РНУ-12) Регистр налогового учёта расходов по хозяйственным операциям и оказанным Банку услугам".
 * formTemplateId=364
 *
 * графа 1  - rowNumber
 * графа    - fix
 * графа 2  - code
 * графа 3  - numberFirstRecord
 * графа 4  - opy
 * графа 5  - operationDate
 * графа 6  - name
 * графа 7  - documentNumber
 * графа 8  - date
 * графа 9  - periodCounts
 * графа 10 - advancePayment
 * графа 11 - outcomeInNalog
 * графа 12 - outcomeInBuh
 *
 * @author rtimerbaev
 * @author Stanislav Yasinskiy
 */
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

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def editableColumns = ['numberFirstRecord', 'opy', 'operationDate', 'name', 'documentNumber', 'date',
                       'periodCounts', 'advancePayment', 'outcomeInBuh']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'code']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['numberFirstRecord', 'opy', 'operationDate', 'name', 'documentNumber',
                       'date', 'periodCounts', 'advancePayment', 'outcomeInBuh']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['advancePayment', 'outcomeInNalog', 'outcomeInBuh']

@Field
def start = null

@Field
def endDate = null

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    if (!dataRows.isEmpty()) {
        // Удаление подитогов
        deleteAllAliased(dataRows)

        // сортируем по кодам
        dataRows.sort { getKnu(it.opy) }

        for (row in dataRows) {
            // графа 11
            row.outcomeInNalog = calc11(row)
        }

        // посчитать "итого по коду"
        def totalRows = calcSubTotalRows(dataRows)

        // добавить "итого по коду" в таблицу
        def i = 0
        totalRows.each { index, row ->
            dataRows.add(index + i++, row)
        }
    }

    dataRows.add(calcTotalRow(dataRows))

    sortFormDataRows(false)
}

def calcSubTotalRows(def dataRows) {
    // посчитать "итого по коду"
    def totalRows = [:]
    def code = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }
    def rows = dataRows.findAll { it.getAlias() == null }

    rows.eachWithIndex { row, i ->
        def knu = getKnu(row.opy)
        if (code == null) {
            code = knu
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (code != knu) {
            totalRows.put(i, getNewRow(code, sums))
            totalColumns.each {
                sums[it] = 0
            }
            code = knu
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == rows.size() - 1) {
            totalColumns.each {
                def val = row.getCell(it).getValue()
                if (val != null)
                    sums[it] += val
            }
            totalRows.put(i + 1, getNewRow(knu, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        totalColumns.each {
            def val = row.getCell(it).getValue()
            if (val != null)
                sums[it] += val
        }
    }

    return totalRows
}

def calcTotalRow(def dataRows) {
    def totalRow = getTotalRow('total', 'Итого')
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получить новую строку.
def getNewRow(def alias, def sums) {
    def newRow = getTotalRow('total' + alias, 'Итого по КНУ ' + alias)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it], null)
    }
    return newRow
}


def getTotalRow(def alias, def title) {
    def newRow = formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = title
    newRow.getCell('fix').colSpan = 9
    ['rowNumber', 'fix', 'code', 'numberFirstRecord', 'numberFirstRecord', 'opy', 'operationDate',
     'name', 'documentNumber', 'date', 'periodCounts', 'advancePayment', 'outcomeInNalog', 'outcomeInBuh'].each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

def BigDecimal calc11(def row) {
    if (row.advancePayment != null && row.advancePayment > 0 && row.periodCounts != null && row.periodCounts != 0) {
        return (row.advancePayment / row.periodCounts).setScale(2, RoundingMode.HALF_UP)
    }
    return null
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    // календарная дата начала отчетного периода
    def startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    // дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // алиасы графов для арифметической проверки
    def arithmeticCheckAlias = ['outcomeInNalog']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 3. Проверка даты совершения операции и границ отчетного периода (графа 5)
        if (row.operationDate != null && (row.operationDate.after(endDate) || row.operationDate.before(startDate))) {
            rowError(logger, row, errorMsg + 'Дата совершения операции вне границ отчётного периода!')
        }

        // 4. Проверка количества отчетных периодов при авансовых платежах (графа 9)
        if (row.periodCounts != null && (row.periodCounts < 1 || 999 < row.periodCounts)) {
            rowError(logger, row, errorMsg + 'Неверное количество отчетных периодов при авансовых платежах!')
        }

        // 5. Проверка на нулевые значения (графа 11, 12)
        if (row.outcomeInNalog == 0 && row.outcomeInBuh == 0) {
            rowError(logger, row, errorMsg + 'Все суммы по операции нулевые!')
        }

        // 6. Проверка формата номера первой записи
        if (row.numberFirstRecord != null && !row.numberFirstRecord.matches('\\d{2}-\\w{6}')) {
            rowError(logger, row, errorMsg + 'Неправильно указан номер первой записи (формат: ГГ-НННННН, см. №852-р в актуальной редакции)!')
        }

        needValue['outcomeInNalog'] = calc11(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
    }

    // Арифметическая проверка итоговых значений строк «Итого по КНУ»
    checkSubTotalSum(dataRows, totalColumns, logger, true)

    // Арифметическая проверка итогового значения по всем строкам
    checkTotalSum(dataRows, totalColumns, logger, true)
}

def String getKnu(def code) {
    return getRefBookValue(27, code)?.CODE?.stringValue
}

def getStartDate() {
    if (!start) {
        start = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return start
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 12, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
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
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 3
        newRow.numberFirstRecord = row.cell[3].text()

        // графа 4
        String filter = "LOWER(CODE) = LOWER('" + row.cell[2].text() + "') and LOWER(OPU) = LOWER('" + row.cell[4].text() + "')"
        def records = refBookFactory.getDataProvider(27).getRecords(getReportPeriodEndDate(), null, filter, null)
        if (checkImportRecordsCount(records, refBookFactory.get(27), 'CODE', row.cell[2].text(), getReportPeriodEndDate(), rnuIndexRow, 2, logger, false)) {
            newRow.opy = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        }

        // графа 5
        newRow.operationDate = parseDate(row.cell[5].text(), "dd.MM.yyyy", rnuIndexRow, 5 + colOffset, logger, true)

        // графа 6
        newRow.name = row.cell[6].text()

        // графа 7
        newRow.documentNumber = row.cell[7].text()

        // графа 8
        newRow.date = parseDate(row.cell[8].text(), "dd.MM.yyyy", rnuIndexRow, 8 + colOffset, logger, true)

        // графа 9
        newRow.periodCounts = parseNumber(row.cell[9].text(), rnuIndexRow, 9 + colOffset, logger, true)

        // графа 10
        newRow.advancePayment = parseNumber(row.cell[10].text(), rnuIndexRow, 10 + colOffset, logger, true)

        // графа 11
        newRow.outcomeInNalog = parseNumber(row.cell[11].text(), rnuIndexRow, 11 + colOffset, logger, true)

        // графа 12
        newRow.outcomeInBuh = parseNumber(row.cell[12].text(), rnuIndexRow, 12 + colOffset, logger, true)

        rows.add(newRow)
    }
    // посчитать "итого по коду"
    def totalRows = calcSubTotalRows(rows)
    def i = 0
    totalRows.each { index, row ->
        rows.add(index + i++, row)
    }
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)

    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow += 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // графа 10
        total.advancePayment = parseNumber(row.cell[10].text(), rnuIndexRow, 10 + colOffset, logger, true)

        // графа 11
        total.outcomeInNalog = parseNumber(row.cell[11].text(), rnuIndexRow, 11 + colOffset, logger, true)

        // графа 12
        total.outcomeInBuh = parseNumber(row.cell[12].text(), rnuIndexRow, 12 + colOffset, logger, true)

        def colIndexMap = ['advancePayment': 10, 'outcomeInNalog': 11, 'outcomeInBuh': 12]
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
        // задать итоговой строке нф значения из итоговой строки тф
        totalColumns.each { alias ->
            totalRow[alias] = total[alias]
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
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), getDataRow(dataRows, 'total'), true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 13
    int HEADER_ROW_COUNT = 3
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
    def totalRowFromFileMap = [:]           // мапа для хранения строк подитогов со значениями из файла (стили простых строк)

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
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, true)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (rowValues[INDEX_FOR_SKIP].contains("Итого по КНУ ")) {
            def subTotalRow = getNewSubTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            if (totalRowFromFileMap[subTotalRow.fix] == null) {
                totalRowFromFileMap[subTotalRow.fix] = []
            }
            totalRowFromFileMap[subTotalRow.fix].add(subTotalRow)
            rows.add(subTotalRow)

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

    // сравнение подитогов
    if (!totalRowFromFileMap.isEmpty()) {
        def totalRowsMap = calcSubTotalRows(rows)
        totalRowsMap.values().toArray().each { calcTotalRowTmp ->
            def totalRows = totalRowFromFileMap[calcTotalRowTmp.fix]
            if (totalRows) {
                totalRows.each { totalRow ->
                    compareTotalValues(totalRow, calcTotalRowTmp, totalColumns, logger, false)
                }
                totalRowFromFileMap.remove(calcTotalRowTmp.fix)
            }
        }
        if (!totalRowFromFileMap.isEmpty()) {
            // для этих подитогов из файла нет групп
            totalRowFromFileMap.each { key, totalRows ->
                totalRows.each { totalRow ->
                    totalColumns.each { alias ->
                        def msg = String.format(COMPARE_TOTAL_VALUES, totalRow.getIndex(), getColumnName(totalRow, alias), totalRow[alias], BigDecimal.ZERO)
                        rowWarning(logger, totalRow, msg)
                    }
                }
            }
        }
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
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'code')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'numberFirstRecord')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'opy')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'operationDate')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][7]): 'Первичный документ']),
            ([(headerRows[1][7]): getColumnName(tmpRow, 'documentNumber')]),
            ([(headerRows[1][8]): getColumnName(tmpRow, 'date')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'periodCounts')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'advancePayment')]),
            ([(headerRows[0][11]): getColumnName(tmpRow, 'outcomeInNalog')]),
            ([(headerRows[0][12]): getColumnName(tmpRow, 'outcomeInBuh')]),
            ([(headerRows[2][0]): '1'])
    ]
    (2..12).each { index ->
        headerMapping.add(([(headerRows[2][index]): index.toString()]))
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
 * @param isTotal признак итоговой строки (для пропуска получения справочных значении)
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def isTotal = false) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    // графа 3
    def colIndex = 3
    newRow.numberFirstRecord = values[colIndex]

    // графа 4 - поиск записи идет по графе 2
    if (!isTotal) {
        colIndex = 2
        String filter = "LOWER(CODE) = LOWER('" + values[colIndex] + "') and LOWER(OPU) = LOWER('" + values[4] + "')"
        def records = refBookFactory.getDataProvider(27).getRecords(getReportPeriodEndDate(), null, filter, null)
        if (checkImportRecordsCount(records, refBookFactory.get(27), 'CODE', values[colIndex], getReportPeriodEndDate(), fileRowIndex, colIndex, logger, false)) {
            newRow.opy = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        }
    }

    // графа 5
    colIndex = 5
    newRow.operationDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6
    colIndex++
    newRow.name = values[colIndex]

    // графа 7
    colIndex++
    newRow.documentNumber = values[colIndex]

    // графа 8
    colIndex++
    newRow.date = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9..12
    ['periodCounts', 'advancePayment', 'outcomeInNalog', 'outcomeInBuh'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

/**
 * Получить новую подитоговую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewSubTotalRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    // графа fix
    def title = values[1]
    def rowAlias = title.substring("Итого по КНУ ".size())?.trim()

    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }
    def newRow = getNewRow(rowAlias, sums)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 10..12
    def colIndex = 9
    ['advancePayment', 'outcomeInNalog', 'outcomeInBuh'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}