package form_template.vat.vat_724_7.v2014

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 *  (724.7)  Отчёт о суммах НДС начисленных налоговым агентом по договорам аренды имущества (балансовый счёт 60309.03)
 *
 *  formTemplateId=605
 */

// графа 1  - rowNum
// графа    - fix
// графа 2  - operDate
// графа 3  - name
// графа 4  - inn
// графа 5  - balanceNumber
// графа 6  - sum
// графа 7  - orderNumber
// графа 8  - ndsSum
// графа 9  - sfDate
// графа 10 - sfNumber

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
        if (currentDataRow.getAlias() == null) formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
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

// Редактируемые атрибуты
@Field
def editableColumns = ['operDate', 'name', 'inn', 'balanceNumber', 'sum', 'orderNumber', 'ndsSum', 'sfDate', 'sfNumber']

@Field
def allColumns = ['rowNum', 'operDate', 'name', 'inn', 'balanceNumber', 'sum', 'orderNumber', 'ndsSum', 'sfDate', 'sfNumber']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['operDate', 'name', 'inn', 'balanceNumber', 'sum', 'orderNumber', 'ndsSum', 'sfDate', 'sfNumber']

// Сумируемые колонки в фиксированной строке
@Field
def totalColumns = ['sum', 'ndsSum']

// Группируемые атрибуты (графа 4, 2, 3, 5, 6, 7, 8, 9, 10)
@Field
def sortColumns = ['inn', 'operDate', 'name', 'balanceNumber', 'sum', 'orderNumber', 'ndsSum', 'sfDate', 'sfNumber']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

// Признак периода ввода остатков
@Field
def isBalancePeriod

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

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def FORMAT_ERROR_MSG = "Строка %s: Графа «%s» заполнена неверно! Ожидаемый формат: «%s»"
    def meanings = [INN_JUR_MEANING]

    for (def row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()

        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка суммы НДС
        if (row.sum != null && row.ndsSum != null &&
                !(row.sum * 0.18 + row.sum * 0.03 > row.ndsSum && row.ndsSum > row.sum * 0.18 - row.sum * 0.03)) {
            rowWarning(logger, row, "Строка $index: Сумма НДС по данным бухгалтерского учета не соответствует налоговой базе! Проверка: «Графа 6» * 18% + («Графа 6» * 3) / 100 > «Графа 8» > «Графа 6» * 18% - («Графа 6» * 3) / 100.")
        }

        // 4. Проверка формата заполнения
        // графа 4
        if (row.inn && !row.inn.matches("^\\S{10}\$")) {
            loggerError(row, String.format(FORMAT_ERROR_MSG, index, getColumnName(row, 'inn'), "ХХХХХХХХХХ"))
        } else if (row.inn && checkPattern(logger, row, 'inn', row.inn, [INN_JUR_PATTERN], meanings, !isBalancePeriod())) {
            checkControlSumInn(logger, row, 'inn', row.inn, !isBalancePeriod())
        } else if (row.inn) { // чтобы отображать подсказку один раз
            meanings = null
        }
        ['operDate', 'sfDate'].each{
            if (row[it]) {
                checkDateValid(logger, row, it, row[it], !isBalancePeriod())
            }
        }
    }

    // 3. Проверка итоговых значений
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def totalRow = getDataRow(dataRows, 'total')
    calcTotalSum(dataRows, totalRow, totalColumns)
}

// Консолидация с группировкой по подразделениям
void consolidation() {
    def rows = []

    // получить данные из источников
    def formSources = departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate())
    // сортируем по наименованию подразделения
    formSources.sort { getDepartmentName(it.departmentId as Integer) }
    for (departmentFormType in formSources) {
        def final child = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == formData.formType.id) {
            def final childData = formDataService.getDataRowHelper(child)
            def final department = departmentService.get(child.departmentId)
            def headRow = getFixedRow(department.name, "head_${department.id}", false)
            rows.add(headRow)
            def final childDataRows = childData.all
            rows.addAll(childDataRows.findAll { it.getAlias() == null })
            def subTotalRow = getFixedRow("Итого по ${department.name}", "total_${department.id}", false)
            calcTotalSum(childDataRows, subTotalRow, totalColumns)
            rows.add(subTotalRow)
        }
    }

    def totalRow = getFixedRow('Итого', 'total', true)
    rows.add(totalRow)

    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
}

@Field
def departmentNameMap = [:]

def getDepartmentName(Integer id) {
    if (id != null && departmentNameMap[id] == null) {
        departmentNameMap[id] = departmentService.get(id).name
    }
    return departmentNameMap[id]
}

/**
 * Получить произвольную фиксированную строку со стилями.
 * @param title текст в строке
 * @param alias псевдоним
 * @param isOuter внешний ли итог(влияет на объединение ячеек, не все названия влезают в 2 ячейки)
 * @return
 */
def getFixedRow(String title, String alias, boolean isOuter) {
    def total = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    total.setAlias(alias)
    total.fix = title
    total.getCell('fix').colSpan = isOuter ? 2 : 5
    (allColumns + 'fix').each {
        total.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return total
}

def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def columns = sortColumns + (allColumns - sortColumns)
    boolean isGroups = dataRows.find { it.getAlias() != null && it.getAlias().startsWith("head_") } != null
    if (!isGroups) {
        def totalRow = getTotalRow(dataRows)
        dataRows.remove(totalRow)
        sortRows(dataRows, columns)
        dataRows.add(totalRow)

        dataRowHelper.saveSort()
    } else {
        def headMap = [:]
        def totalMap = [:]
        // находим строки начала и конца для каждого подразделения
        dataRows.each { row ->
            String alias = row.getAlias()
            if (alias != null) {
                if (alias.startsWith("head_")) {
                    headMap[alias.replace("head_","")] = row
                }
                if (alias.startsWith("total_")) {
                    totalMap[alias.replace("total_","")] = row
                }
            }
        }
        // по подразделениям
        headMap.keySet().each { key ->
            def headRow = headMap[key]
            def totalRow = totalMap[key]
            if (headRow && totalRow) {
                def groupFrom = headRow.getIndex()
                def groupTo = totalRow.getIndex() - 1
                def rows = (groupFrom < groupTo ? dataRows[groupFrom..(groupTo - 1)] : [])
                // Массовое разыменование строк НФ
                def columnList = headRow.keySet().collect { headRow.getCell(it).getColumn() }
                refBookService.dataRowsDereference(logger, rows, columnList)
                sortRows(rows, columns)
            } else {
                logger.warn("Ошибка при сортировке. Нарушена структура налоговой формы. Отсутствуют строки заголовоков/итогов по подразделениям.")
            }
        }
    }

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}

// Получение подитоговых строк
def getTotalRow(def dataRows) {
    return dataRows.find { it.getAlias() != null && it.getAlias().equals('total')}
}

def loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

void importTransportData() {
    checkTF(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 10
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null        // итоговая строка со значениями из тф для добавления
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
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }
            def newRow = getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex)
            if (newRow) {
                newRows.add(newRow)
            }
        }
    } finally {
        reader.close()
    }

    def totalRow = getFixedRow('Итого', 'total', true)
    newRows.add(totalRow)
    // подсчет итогов
    calcTotalSum(newRows, totalRow, totalColumns)

    // сравнение итогов
    checkAndSetTFSum(totalRow, totalTF, totalColumns, totalTF?.getImportIndex(), logger, false)

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
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    def int colOffset = 1
    def int colIndex = 1

    // графа 2
    colIndex++
    newRow.operDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 3
    colIndex++
    newRow.name =  pure(rowCells[colIndex])

    // графа 4
    colIndex++
    newRow.inn = pure(rowCells[colIndex])

    // графа 5
    colIndex++
    newRow.balanceNumber = pure(rowCells[colIndex])

    // графа 6
    colIndex++
    newRow.sum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7
    colIndex++
    newRow.orderNumber = pure(rowCells[colIndex])

    // графа 8
    colIndex++
    newRow.ndsSum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9
    colIndex++
    newRow.sfDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9
    colIndex++
    newRow.sfNumber = pure(rowCells[colIndex])

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 10
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('rowNum').column.name
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

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
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP]) {
            if (rowValues[INDEX_FOR_SKIP] == 'Итого' && totalRowFromFile == null) {
                rowIndex++
                totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            }
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // Добавляем итоговые строки
    def totalRow = getFixedRow('Итого', 'total', true)
    rows.add(totalRow)
    updateIndexes(rows)
    // сравнение итогов
    compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
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
            ([(headerRows[0][0]): tmpRow.getCell('rowNum').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('operDate').column.name]),

            ([(headerRows[0][3]): 'Арендодатель']),
            ([(headerRows[1][3]): 'наименование']),
            ([(headerRows[1][4]): 'ИНН']),

            ([(headerRows[0][5]): tmpRow.getCell('balanceNumber').column.name]),
            ([(headerRows[0][6]): tmpRow.getCell('sum').column.name]),

            ([(headerRows[0][7]): 'НДС']),
            ([(headerRows[1][7]): 'номер мемориального ордера']),
            ([(headerRows[1][8]): 'сумма']),

            ([(headerRows[0][9]): 'Счёт-фактура']),
            ([(headerRows[1][9]): 'дата']),
            ([(headerRows[1][10]): 'номер']),
            ([(headerRows[2][0]): '1'])
    ]
    (2..10).each { index ->
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
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 2
    def colIndex = 2
    newRow.operDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 3, 4, 5
    ['name', 'inn', 'balanceNumber'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 6
    colIndex++
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7
    colIndex++
    newRow.orderNumber = values[colIndex]

    // графа 8
    colIndex++
    newRow.ndsSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9
    colIndex++
    newRow.sfDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 10
    colIndex++
    newRow.sfNumber = values[colIndex]

    return newRow
}