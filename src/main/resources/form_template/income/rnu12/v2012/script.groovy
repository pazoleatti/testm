package form_template.income.rnu12.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
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

// графа 4
@Field
def groupColumns = ['opy']

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

        // Добавление подитогов
        addAllAliased(dataRows, new ScriptUtils.CalcAliasRow() {
            @Override
            DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
                return calcItog(i, rows)
            }
        }, groupColumns)
    }

    dataRows.add(getTotalRow(dataRows))

    sortFormDataRows(false)
}

/** Получить общую итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def totalRow = getTotalRow('Итого', 'total')
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

/**
 * Получить подитоговую строку.
 *
 * @param rowNumber номер строки
 * @param code КНУ
 * @param key ключ для сравнения подитоговых строк при импорте
 */
def getSubTotalRow(def rowNumber, def code, def key) {
    def title = 'Итого по КНУ ' + (!code || 'null'.equals(code?.trim()) ? '"КНУ не задано"' : code?.trim())
    def alias = 'total' + key.toString() + '#' + rowNumber
    return getTotalRow(title, alias)
}

def getTotalRow(def title, def alias) {
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
    // Проверка наличия всех фиксированных строк
    // Проверка отсутствия лишних фиксированных строк
    // Проверка итоговых значений по фиксированным строкам
    checkItog(dataRows)

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

    // Добавление подитогов
    addAllAliased(rows, new ScriptUtils.CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> tmpRows) {
            return calcItog(i, tmpRows)
        }
    }, groupColumns)

    def totalRow = getTotalRow(rows)
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
        } else if (rowValues[INDEX_FOR_SKIP].toLowerCase().contains("итого по кну ")) {
            // для расчета уникального среди групп(groupColumns) ключа берем строку перед Подитоговой
            def key = !rows.isEmpty() ? getKey(rows[-1]) : null
            def subTotalRow = getNewSubTotalRowFromXls(key, rowValues, colOffset, fileRowIndex, rowIndex)

            // наш ключ - row.getAlias() до решетки. так как индекс после решетки не равен у расчитанной и импортированной подитогововых строк
            if (totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]] == null) {
                totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]] = []
            }
            totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]].add(subTotalRow)
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
    updateIndexes(rows)

    // сравнение подитогов
    if (!totalRowFromFileMap.isEmpty()) {
        def tmpSubTotalRowsMap = calcSubTotalRowsMap(rows)
        tmpSubTotalRowsMap.each { subTotalRow, groupValues ->
            def totalRows = totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]]
            if (totalRows) {
                totalRows.each { totalRow ->
                    compareTotalValues(totalRow, subTotalRow, totalColumns, logger, false)
                }
                totalRowFromFileMap.remove(subTotalRow.getAlias().split('#')[0])
            } else {
                rowWarning(logger, null, String.format(GROUP_WRONG_ITOG, groupValues))
            }
        }
        if (!totalRowFromFileMap.isEmpty()) {
            // для этих подитогов из файла нет групп
            totalRowFromFileMap.each { key, totalRows ->
                totalRows.each { totalRow ->
                    rowWarning(logger, totalRow, String.format(GROUP_WRONG_ITOG_ROW, totalRow.getIndex()))
                }
            }
        }
    }

    // сравнение итогов
    def totalRow = getTotalRow(rows)
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
    checkHeaderSize(headerRows, colCount, rowCount)

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
 * @param key ключ для сравнения подитоговых строк при импорте
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewSubTotalRowFromXls(def key, def values, def colOffset, def fileRowIndex, def rowIndex) {
    // графа fix
    def title = values[1]
    def code = title?.substring('Итого по КНУ '.size())?.trim()
    def newRow = getSubTotalRow(rowIndex, code, key)
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

// Получить посчитанные подитоговые строки
def calcSubTotalRowsMap(def dataRows) {
    def tmpRows = dataRows.findAll { !it.getAlias() }
    // Добавление подитогов
    addAllAliased(tmpRows, new ScriptUtils.CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)

    // сформировать мапу (строка подитога -> значения группы)
    def map = [:]
    def prevRow = null
    for (def row : tmpRows) {
        if (!row.getAlias()) {
            prevRow = row
            continue
        }
        if (row.getAlias() && prevRow) {
            map[row] = getValuesByGroupColumn(prevRow)
        }
    }

    return map
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def tmpRow = dataRows.get(i)
    def key = getKey(tmpRow)
    def code = getKnu(tmpRow?.opy)
    def newRow = getSubTotalRow(i, code, key)

    // Расчеты подитоговых значений
    def rows = []
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        rows.add(dataRows.get(j))
    }
    calcTotalSum(rows, newRow, totalColumns)

    return newRow
}

// Проверки подитоговых сумм
void checkItog(def dataRows) {
    // Рассчитанные строки итогов
    def testItogRowsMap = calcSubTotalRowsMap(dataRows)
    // Имеющиеся строки итогов
    def itogRows = dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
    // все строки, кроме общего итога
    def groupRows = dataRows.findAll { !'total'.equals(it.getAlias()) }
    def testItogRows = testItogRowsMap.keySet().asList()
    checkItogRows(groupRows, testItogRows, itogRows, groupColumns, logger, new ScriptUtils.GroupString() {
        @Override
        String getString(DataRow<Cell> row) {
            return getValuesByGroupColumn(row)
        }
    }, new ScriptUtils.CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            for (def alias : totalColumns) {
                if (row1[alias] != row2[alias]) {
                    return getColumnName(row1, alias)
                }
            }
            return null
        }
    })
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    // 4
    def code = getKnu(row.opy)
    return (code != null ? code : 'графа 4 не задана')
}

/** Получить уникальный ключ группы. */
def getKey(def row) {
    def key = ''
    groupColumns.each { def alias ->
        key = key + (row[alias] != null ? row[alias] : "").toString()
    }
    return key.toLowerCase().hashCode()
}