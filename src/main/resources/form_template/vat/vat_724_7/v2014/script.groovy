package form_template.vat.vat_724_7.v2014

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
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
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def FORMAT_ERROR_MSG = "Строка %s: Графа «%s» заполнена неверно! Ожидаемый формат: «%s»"

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
        }
        // графа 10
        if (row.sfNumber && !row.sfNumber.matches("^\\S{9}\\/\\S{2}\\-\\S{4}\$")) {
            loggerError(row, String.format(FORMAT_ERROR_MSG, index, getColumnName(row, 'sfNumber'), "ХХХХХХХХХ/ХХ-ХХХХ"))
        }
    }

    // 3. Проверка итоговых значений
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = getDataRow(dataRows, 'total')
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRowHelper.save(dataRows)

    // Сортировка групп и строк
    sortFormDataRows()
}

// Консолидация с группировкой по подразделениям
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    // получить данные из источников
    def formSources = departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate())
    // сортируем по наименованию подразделения
    formSources.sort { departmentService.get(it.departmentId).name }
    for (departmentFormType in formSources) {
        def final child = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == formData.formType.id) {
            def final childData = formDataService.getDataRowHelper(child)
            def final department = departmentService.get(child.departmentId)
            def headRow = getFixedRow(department.name, "head_${department.id}", false)
            dataRows.add(headRow)
            def final childDataRows = childData.all
            dataRows.addAll(childDataRows.findAll { it.getAlias() == null })
            def subTotalRow = getFixedRow("Итого по ${department.name}", "total_${department.id}", false)
            calcTotalSum(childDataRows, subTotalRow, totalColumns)
            dataRows.add(subTotalRow)
        }
    }

    def totalRow = getFixedRow('Итого', 'total', true)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
    dataRows = null
}

/**
 * Получить произвольную фиксированную строку со стилями.
 * @param title текст в строке
 * @param alias псевдоним
 * @param isOuter внешний ли итог(влияет на объединение ячеек, не все названия влезают в 2 ячейки)
 * @return
 */
def getFixedRow(String title, String alias, boolean isOuter) {
    def total = formData.createDataRow()
    total.setAlias(alias)
    total.fix = title
    total.getCell('fix').colSpan = isOuter ? 2 : 5
    (allColumns + 'fix').each {
        total.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return total
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNum'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): getColumnName(tmpRow, 'rowNum'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'operDate'),

            (xml.row[0].cell[3]): 'Арендодатель',
            (xml.row[1].cell[3]): 'наименование',
            (xml.row[1].cell[4]): 'ИНН',

            (xml.row[0].cell[5]): getColumnName(tmpRow, 'balanceNumber'),
            (xml.row[0].cell[6]): getColumnName(tmpRow, 'sum'),

            (xml.row[0].cell[7]): 'НДС',
            (xml.row[1].cell[7]): 'номер мемориального ордера',
            (xml.row[1].cell[8]): 'сумма',

            (xml.row[0].cell[9]): 'Счёт-фактура',
            (xml.row[1].cell[9]): 'дата',
            (xml.row[1].cell[10]): 'номер',
            (xml.row[2].cell[0]): '1'
    ]
    (2..10).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)

        // графа 2
        newRow.operDate = parseDate(row.cell[2].text(), "dd.MM.yyyy", xlsIndexRow, 2 + colOffset, logger, true)

        // графа 3
        newRow.name =  row.cell[3].text()

        // графа 4
        newRow.inn = row.cell[4].text()

        // графа 5
        newRow.balanceNumber = row.cell[5].text()

        // графа 6
        newRow.sum = parseNumber(row.cell[6].text(), xlsIndexRow, 6 + colOffset, logger, true)

        // графа 7
        newRow.orderNumber = parseDate(row.cell[7].text(), "dd.MM.yyyy", xlsIndexRow, 7 + colOffset, logger, true)

        // графа 8
        newRow.ndsSum = parseNumber(row.cell[8].text(), xlsIndexRow, 8 + colOffset, logger, true)

        // графа 9
        newRow.sfDate = parseDate(row.cell[9].text(), "dd.MM.yyyy", xlsIndexRow, 9 + colOffset, logger, true)

        // графа 10
        newRow.sfNumber = row.cell[10].text()

        rows.add(newRow)
    }

    // Добавляем итоговые строки
    rows.add(getDataRow(dataRows, 'total'))

    dataRowHelper.save(rows)
}

def getNewRow() {
    def newRow = formData.createDataRow()
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
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // не производим сортировку в консолидированных формах
    if (dataRows[0].getAlias() == null) {
        def totalRow = getTotalRow(dataRows)
        dataRows.remove(totalRow)
        sortRows(dataRows, sortColumns)
        dataRows.add(totalRow)

        dataRowHelper.saveSort()
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
    int COLUMN_COUNT = 10
    int TOTAL_ROW_COUNT = 1
    int ROW_MAX = 1000
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    checkBeforeGetXml(ImportInputStream, UploadFileName)

    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()

    String[] rowCells
    int countEmptyRow = 0	// количество пустых строк
    int fileRowIndex = 0    // номер строки в файле
    int rowIndex = 0        // номер строки в НФ
    int totalRowCount = 0   // счетчик кол-ва итогов
    def totalTF = null		// итоговая строка со значениями из тф для добавления
    def newRows = []

    while ((rowCells = reader.readNext()) != null) {
        fileRowIndex++

        def isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
        if (isEmptyRow) {
            if (countEmptyRow > 0) {
                // если встретилась вторая пустая строка, то дальше только строки итогов и ЦП
                totalRowCount++
                // итоговая строка тф
                totalTF = getNewRow(reader.readNext(), COLUMN_COUNT, ++fileRowIndex, ++rowIndex)
                break
            }
            countEmptyRow++
            continue
        }

        // если еще не было пустых строк, то это первая строка - заголовок (пропускается)
        // обычная строка
        if (countEmptyRow != 0 && !addRow(newRows, rowCells, COLUMN_COUNT, fileRowIndex, ++rowIndex)) {
            break
        }

        // периодически сбрасываем строки
        if (newRows.size() > ROW_MAX) {
            dataRowHelper.insert(newRows, dataRowHelper.allCached.size() + 1)
            newRows.clear()
        }
    }
    reader.close()

    // проверка итоговой строки
    if (TOTAL_ROW_COUNT != 0 && totalRowCount != TOTAL_ROW_COUNT) {
        logger.error(ROW_FILE_WRONG, fileRowIndex)
    }

    if (newRows.size() != 0) {
        dataRowHelper.insert(newRows, dataRowHelper.allCached.size() + 1)
    }

    // сравнение итогов
    if (totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = ['sum' : 6, 'ndsSum' : 8]
        def dataRows = dataRowHelper.allCached
        // итоговая строка для сверки сумм
        def totalRow = getFixedRow('Итого', 'total', true)
        calcTotalSum(dataRows, totalRow, totalColumnsIndexMap.keySet().asList())

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }

        // добавить итоговую строку
        dataRowHelper.insert(totalRow, dataRowHelper.allCached.size() + 1)
    }
}

/** Добавляет строку в текущий буфер строк. */
boolean addRow(def dataRowsCut, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells == null) {
        return true
    }
    def newRow = getNewRow(rowCells, columnCount, fileRowIndex, rowIndex)
    if (newRow == null) {
        return false
    }
    dataRowsCut.add(newRow)
    return true
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
    newRow.orderNumber = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

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