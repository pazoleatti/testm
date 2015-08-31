package form_template.income.rnu8.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

/**
 * (РНУ-8) Простой регистр налогового учёта «Требования»
 * formTypeId=320
 *
 * графа 1  - number
 * графа 2  - code
 * графа 3  - balance
 * графа 4  - name
 * графа 5  - income
 * графа 6  - outcome
 *
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
    case FormDataEvent.COMPOSE: // Консолидация
        consolidation()
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
def editableColumns = ['balance', 'income', 'outcome']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['balance', 'income', 'outcome']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['income', 'outcome']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number', 'code', 'name']

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
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

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        // сортируем по кодам
        dataRows.sort { getKnu(it.balance) }

        dataRows.eachWithIndex { row, index ->
            row.setIndex(index + 1)
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
    def sum = 0, sum2 = 0
    def rows = dataRows.findAll { it.getAlias() == null }

    rows.eachWithIndex { row, i ->
        if (code == null) {
            code = getKnu(row.balance)
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (code != getKnu(row.balance)) {
            totalRows.put(i, getNewRow(code, sum, sum2))
            sum = 0
            sum2 = 0
            code = getKnu(row.balance)
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == rows.size() - 1) {
            sum += (row.income ?: 0)
            sum2 += (row.outcome ?: 0)
            def totalRowCode = getNewRow(code, sum, sum2)
            totalRows.put(i + 1, totalRowCode)
            sum = 0
            sum2 = 0
        }
        sum += (row.income ?: 0)
        sum2 += (row.outcome ?: 0)
    }

    return totalRows
}

def calcTotalRow(def dataRows) {
    def totalRow = getTotalRow('total', 'Итого')
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получить новую строку подитога
def getNewRow(def alias, def sum, def sum2) {
    def newRow = getTotalRow('total' + alias, 'Итого по КНУ ' + alias)
    newRow.income = sum
    newRow.outcome = sum2
    return newRow
}

def getTotalRow(def alias, def title) {
    def newRow = formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = title
    newRow.getCell('fix').colSpan = 4
    ['number', 'fix', 'income', 'outcome'].each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    //две карты: одна с реальными значениями итого по кодам, а вторая - с рассчитанными
    def totalRows = [:]
    def sum1 = [:]
    //две карты: одна с реальными значениями итого по кодам, а вторая - с рассчитанными
    def totalRows2 = [:]
    def sum2 = [:]

    for (def row : dataRows) {
        if (row.getAlias() ==~ /total\d+/) { // если подитог
            totalRows[row.getAlias().replace('total', '')] = row.income
            totalRows2[row.getAlias().replace('total', '')] = row.outcome
            continue
        }
        if (row.getAlias() != null) {
            continue
        }

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 3. Арифметическая проверка итоговых значений строк «Итого по КНУ»
        def code = getKnu(row.balance)
        if (sum1[code] != null) {
            sum1[code] += row.income ?: 0
        } else {
            sum1[code] = row.income ?: 0
        }
        if (sum2[code] != null) {
            sum2[code] += row.outcome ?: 0
        } else {
            sum2[code] = row.outcome ?: 0
        }
    }

    // 3. Арифметическая проверка итоговых значений строк «Итого по КНУ»
    totalRows.each { key, val ->
        if (totalRows.get(key) != sum1.get(key)) {
            def msg = formData.createDataRow().getCell('income').column.name
            logger.error("Неверное итоговое значение по коду '$key' графы «$msg»!")
        }
    }
    totalRows2.each { key, val ->
        if (totalRows2.get(key) != sum2.get(key)) {
            def msg = formData.createDataRow().getCell('outcome').column.name
            logger.error("Неверное итоговое значение по коду '$key' графы «$msg»!")
        }
    }

    // 4. Арифметическая проверка итогового значения по всем строкам
    checkTotalSum(dataRows, totalColumns, logger, true)
}

def String getKnu(def code) {
    return getRefBookValue(28, code)?.CODE?.stringValue
}

void consolidation() {
    def rows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).allSaved.each { sRow ->
                    if (sRow.getAlias() == null || sRow.getAlias() == '') {
                        def isFind = false
                        for (def row : rows) {
                            if (sRow.balance == row.balance) {
                                isFind = true
                                totalColumns.each { alias ->
                                    def tmp = (row.getCell(alias).value ?: 0) + (sRow.getCell(alias).value ?: 0)
                                    row.getCell(alias).setValue(tmp, null)
                                }
                                break
                            }
                        }
                        if (!isFind) {
                            rows.add(sRow)
                        }
                    }
                }
            }
        }
    }

    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 6, 1)
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

        // графа 3 - поиск записи идет по графе 2
        String filter = "LOWER(CODE) = LOWER('" + row.cell[2].text() + "') and LOWER(NUMBER) = LOWER('" + row.cell[3].text().replaceAll(/\./, "") + "')"
        def records = refBookFactory.getDataProvider(28).getRecords(getReportPeriodEndDate(), null, filter, null)
        if (checkImportRecordsCount(records, refBookFactory.get(28), 'CODE', row.cell[2].text(), getReportPeriodEndDate(), rnuIndexRow, 2 + colOffset, logger, false)) {
            // графа 3
            newRow.balance = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue

            // графа 4 проверка
            formDataService.checkReferenceValue(28, row.cell[4].text(), records.get(0).TYPE_INCOME?.stringValue, rnuIndexRow, 4 + colOffset, logger, false)
        }

        // графа 5
        newRow.income = parseNumber(row.cell[5].text(), rnuIndexRow, 5 + colOffset, logger, true)

        // графа 6
        newRow.outcome = parseNumber(row.cell[6].text(), rnuIndexRow, 6 + colOffset, logger, true)
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

        // графа 5
        total.income = parseNumber(row.cell[5].text(), rnuIndexRow, 5 + colOffset, logger, true)

        // графа 6
        total.outcome = parseNumber(row.cell[6].text(), rnuIndexRow, 6 + colOffset, logger, true)

        def colIndexMap = ['income': 5, 'outcome': 6]
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

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), dataRows.find { it.getAlias() == 'total' }, true)
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
    int COLUMN_COUNT = 7
    int HEADER_ROW_COUNT = 3
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
    checkHeaderSize(headerRows[0].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            (headerRows[0][0]): getColumnName(tmpRow, 'number'),
            (headerRows[0][2]): getColumnName(tmpRow, 'code'),
            (headerRows[0][3]): 'Балансовый счёт',
            (headerRows[1][3]): getColumnName(tmpRow, 'balance'),
            (headerRows[1][4]): getColumnName(tmpRow, 'name'),
            (headerRows[0][5]): getColumnName(tmpRow, 'income'),
            (headerRows[0][6]): getColumnName(tmpRow, 'outcome'),
            (headerRows[2][0]): '1'
    ]
    (2..6).each { index ->
        headerMapping.put((headerRows[2][index]), index.toString())
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

    // графа 3 - поиск записи идет по графе 2
    if (!isTotal) {
        String filter = "LOWER(CODE) = LOWER('" + values[2] + "') and LOWER(NUMBER) = LOWER('" + values[3].replaceAll(/\./, "") + "')"
        def records = refBookFactory.getDataProvider(28).getRecords(getReportPeriodEndDate(), null, filter, null)
        colIndex = 2
        if (checkImportRecordsCount(records, refBookFactory.get(28), 'CODE', values[colIndex], getReportPeriodEndDate(), fileRowIndex, colIndex + colOffset, logger, false)) {
            // графа 3
            newRow.balance = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue

            // графа 4
            colIndex = 4
            formDataService.checkReferenceValue(28, values[colIndex], records.get(0).TYPE_INCOME?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }

    // графа 5
    colIndex = 5
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6
    colIndex = 6
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

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
    def alias = title.substring("Итого по КНУ ".size())?.trim()

    def newRow = getNewRow(alias, 0, 0)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 5
    colIndex = 5
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6
    colIndex = 6
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}