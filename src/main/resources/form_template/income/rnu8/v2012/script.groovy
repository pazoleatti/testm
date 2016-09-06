package form_template.income.rnu8.v2012

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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
def allColumns = ['fix', 'number', 'code', 'balance', 'income', 'outcome']

// Редактируемые атрибуты
@Field
def editableColumns = ['balance', 'income', 'outcome']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['balance', 'income', 'outcome']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['income', 'outcome']

// графа 2
@Field
def groupColumns = ['code']

@Field
def sortColumns = ['code', 'balance', 'name', 'income', 'outcome']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number', 'code', 'name']

@Field
def startDate = null

@Field
def endDate = null

// Признак периода ввода остатков
@Field
def Boolean isBalancePeriod = null

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

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
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
        refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { (groupColumns + 'balance').contains(it.getAlias())})
        sortRows(dataRows, groupColumns)

        // Добавление подитогов
        addAllAliased(dataRows, new ScriptUtils.CalcAliasRow() {
            @Override
            DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
                return calcItog(i, rows)
            }
        }, groupColumns)
    }

    dataRows.add(getTotalRow(dataRows))

    updateIndexes(dataRows)
}

def calcSubTotalRows(def dataRows) {
    // создать копию без фикс строк, чтобы не влиять на оригинал
    def tmpRows = dataRows.findAll { it.getAlias() == null }
    // Добавление подитогов
    for (int i = 0; i < tmpRows.size(); i++) {
        def row = tmpRows[i]
        def nextRow = null
        if (i < tmpRows.size() - 1) {
            nextRow = tmpRows[i + 1]
        }
        if (row.getAlias() != null) {
            continue
        }
        if (nextRow == null || (getKnu(row.balance) != getKnu(nextRow.balance))) {
            def aliasedRow = calcItog(i, tmpRows)
            tmpRows.add(++i, aliasedRow)
        }
    }
    return tmpRows.findAll{ it.getAlias() != null }
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def tmpRow = dataRows.get(i)
    def key = getKey(tmpRow)
    def code = getKnu(tmpRow?.balance)
    def newRow = getSubTotalRow(code, key)

    // Расчеты подитоговых значений
    def rows = []
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        rows.add(dataRows.get(j))
    }
    calcTotalSum(rows, newRow, totalColumns)

    return newRow
}

/**
 * Получить подитоговую строку.
 *
 * @param code КНУ
 * @param key ключ для сравнения подитоговых строк при импорте
 */
def getSubTotalRow(def code, def key) {
    def title = 'Итого по КНУ ' + (!code || 'null'.equals(code?.trim()) ? '"КНУ не задано"' : code?.trim())
    def alias = 'total' + key.toString()
    return getTotalRow(title, alias)
}

def getTotalRow(def dataRows) {
    def totalRow = getTotalRow('Итого', 'total')
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

def getTotalRow(def title, def alias) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = title
    newRow.getCell('fix').colSpan = 4
    allColumns.each {
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

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }

    // 2. Проверка наличия всех фиксированных строк «Итого по КНУ»
    // 3. Проверка отсутствия лишних фиксированных строк «Итого по КНУ»
    // 4. Проверка итоговых значений по фиксированным строкам «Итого по КНУ»
    checkItog(dataRows)

    // 5. Арифметическая проверка итогового значения по всем строкам
    checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod())
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
    checkItogRows(groupRows, testItogRows, itogRows, groupColumns, logger, true, new ScriptUtils.GroupString() {
        @Override
        String getString(DataRow<Cell> row) {
            return getValuesByGroupColumn(row)
        }
    }, new ScriptUtils.CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            if (row1.fix != row2.fix) {
                return getColumnName(row1, 'code')
            }
            if (row1.income != row2.income) {
                return getColumnName(row1, 'income')
            }
            if (row1.outcome != row2.outcome) {
                return getColumnName(row1, 'outcome')
            }
            return null
        }
    }, new ScriptUtils.CheckDiffGroup() {
        @Override
        Boolean check(DataRow<Cell> row1, DataRow<Cell> row2, List<String> groupColumns) {
            if (row1.balance == null) {
                return null // для строк с пустыми графами группировки не надо проверять итоги
            }
            return compareGroup(row1, row2)
        }
    })
}

boolean compareGroup(def rowA, def rowB) {
    def valueA = (rowA.getAlias() != null) ? rowA.fix : getTitle(getKnu(rowA.balance))
    def valueB = (rowB.getAlias() != null) ? rowB.fix : getTitle(getKnu(rowB.balance))
    return valueA != valueB
}

String getTitle(def code) {
    return 'Итого по КНУ ' + (!code || 'null'.equals(code?.trim()) ? '"КНУ не задано"' : code?.trim())
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    // 3
    def code = getKnu(row.balance)
    return (code != null ? code : 'графа 3 не задана')
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

static String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}

void importTransportData() {
    checkTF(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 6
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ (1, 2, ..)
    def totalTF = null      // итоговая строка со значениями из тф для добавления
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)
    try {
        // проверить первые строки тф - заголовок и пустая строка
        checkFirstRowsTF(reader, logger)

        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex, true)
                }
                break
            }
            newRows.add(getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex))
        }
    } finally {
        reader.close()
    }

    // посчитать "итого по коду"
    refBookService.dataRowsDereference(logger, newRows, formData.getFormColumns().findAll { (groupColumns + 'balance').contains(it.getAlias())})
    addAllAliased(newRows, new ScriptUtils.CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)

    def totalRow = getTotalRow(newRows)
    newRows.add(totalRow)

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = ['income' : 5, 'outcome' : 6]
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR + " Из файла: $v1, рассчитано: $v2", totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
        // задать итоговой строке нф значения из итоговой строки тф
        totalColumns.each { alias ->
            totalRow[alias] = totalTF[alias]
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
        // очистить итоги
        totalColumns.each { alias ->
            totalRow[alias] = null
        }
    }

    updateIndexes(newRows)
    showMessages(newRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = newRows
    }
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 * @param isTotal признак итоговой строки (для пропуска получения справочных значении)
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex, def isTotal = false) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}'", fileRowIndex))
        return newRow
    }
    def cols = (isBalancePeriod() ? editableColumns + autoFillColumns : editableColumns)
    cols.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    (autoFillColumns - cols).each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    def int colOffset = 1
    if (!isTotal) {
        // графа 3 - поиск записи идет по графе 2
        String filter =  getFilter(pure(rowCells[2]), pure(rowCells[3]).replaceAll(/\./, ""))

        def records = refBookFactory.getDataProvider(28).getRecords(getReportPeriodEndDate(), null, filter, null)
        if (checkImportRecordsCount(records, refBookFactory.get(28), 'CODE', pure(rowCells[2]), getReportPeriodEndDate(), fileRowIndex, 2 + colOffset, logger, false)) {
            // графа 3
            newRow.balance = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue

            // графа 4 проверка
            formDataService.checkReferenceValue(28, pure(rowCells[4]), records.get(0).TYPE_INCOME?.stringValue, fileRowIndex, 4 + colOffset, logger, false)
        }
    }

    // графа 5
    newRow.income = parseNumber(pure(rowCells[5]), fileRowIndex, 5 + colOffset, logger, true)

    // графа 6
    newRow.outcome = parseNumber(pure(rowCells[6]), fileRowIndex, 6 + colOffset, logger, true)

    return newRow    
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def columns = sortColumns + (allColumns - sortColumns)
    // Сортировка (внутри групп)
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { columns.contains(it.getAlias())})
    def newRows = []
    def tempRows = []
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            if (!tempRows.isEmpty()) {
                sortRows(tempRows, columns)
                newRows.addAll(tempRows)
                tempRows = []
            }
            newRows.add(row)
            continue
        }
        tempRows.add(row)
    }
    if (!tempRows.isEmpty()) {
        sortRows(tempRows, columns)
        newRows.addAll(tempRows)
    }
    dataRowHelper.setAllCached(newRows)

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
    String TABLE_START_VALUE = tmpRow.getCell('number').column.name
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

            // наш ключ - row.getAlias()
            if (totalRowFromFileMap[subTotalRow.getAlias()] == null) {
                totalRowFromFileMap[subTotalRow.getAlias()] = []
            }
            totalRowFromFileMap[subTotalRow.getAlias()].add(subTotalRow)
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
            def totalRows = totalRowFromFileMap[subTotalRow.getAlias()]
            if (totalRows) {
                totalRows.each { totalRow ->
                    compareTotalValues(totalRow, subTotalRow, totalColumns, logger, false)
                }
                totalRowFromFileMap.remove(subTotalRow.getAlias())
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
            ([(headerRows[0][0]): tmpRow.getCell('number').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('code').column.name]),
            ([(headerRows[0][3]): 'Балансовый счёт']),
            ([(headerRows[1][3]): tmpRow.getCell('balance').column.name]),
            ([(headerRows[1][4]): tmpRow.getCell('name').column.name]),
            ([(headerRows[0][5]): tmpRow.getCell('income').column.name]),
            ([(headerRows[0][6]): tmpRow.getCell('outcome').column.name]),
            ([(headerRows[2][0]): '1'])
    ]
    (2..6).each { index ->
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

    // графа 3 - поиск записи идет по графе 2
    if (!isTotal) {
        String filter =  getFilter(values[2], values[3].replaceAll(/\./, ""))
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
 * @param key ключ для сравнения подитоговых строк при импорте
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewSubTotalRowFromXls(def key, def values, def colOffset, def fileRowIndex, def rowIndex) {
    def title = values[1]
    def code = title?.substring('Итого по КНУ '.size())?.trim()
    def newRow = getSubTotalRow(code, key)
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

def String getFilter(def String code, def String number){
    String filter = "LOWER(CODE) = LOWER('" + code + "')"
    if (number != '') {
        filter += " and LOWER(NUMBER) = LOWER('" + number + "')"
    }
    return filter
}

// Получить посчитанные подитоговые строки
def calcSubTotalRowsMap(def dataRows) {
    def tmpRows = dataRows.findAll { !it.getAlias() }
    // Добавление подитогов
    refBookService.dataRowsDereference(logger, tmpRows, formData.getFormColumns().findAll { (groupColumns + 'balance').contains(it.getAlias())})
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

/** Получить уникальный ключ группы. */
def getKey(def row) {
    def key = getKnu(row.balance) ?: ''
    return key.toLowerCase().hashCode()
}