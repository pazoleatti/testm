package form_template.vat.vat_724_6.v2014

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 *  6.5	(724.6)  Отчёт о суммах НДС начисленных налоговым агентом с сумм дохода иностранных юридических лиц
 *  (балансовый счёт 60309.02)
 *
 *  formTemplateId=604
 */

// графа 1  - rowNum
// графа    - fix
// графа 2  - operDate
// графа 3  - contragent
// графа 4  - type
// графа 5  - sum
// графа 6  - number
// графа 7  - sum2
// графа 8  - date
// графа 9  - number2

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
def editableColumns = ['operDate', 'contragent', 'type', 'sum', 'number', 'sum2', 'date', 'number2']

@Field
def allColumns = ['rowNum', 'operDate', 'contragent', 'type', 'sum', 'number', 'sum2', 'date', 'number2']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['operDate', 'contragent', 'type', 'sum', 'number', 'sum2', 'date', 'number2']

// Сумируемые колонки в фиксированной строке
@Field
def totalColumns = ['sum', 'sum2']

// Группируемые атрибуты (графа 4, 2, 3, 5, 6, 7, 8, 9)
@Field
def sortColumns = ['type', 'operDate', 'contragent', 'sum', 'number', 'sum2', 'date', 'number2']

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
        if (row.sum != null && row.sum2 != null &&
                !(row.sum * 0.18 + row.sum * 0.03 > row.sum2 && row.sum2 > row.sum * 0.18 - row.sum * 0.03)) {
            rowWarning(logger, row, "Строка $index: Сумма НДС по данным бухгалтерского учета не соответствует налоговой базе! Проверка: «Графа 5» * 18% + («Графа 5» * 3) / 100 > «Графа 7» > «Графа 5» * 18% - («Графа 5» * 3) / 100.")
        }

        // 4. Проверка формата заполнения
        // графа 9
        if (row.number2 && !row.number2.matches("^\\S{9}\\/\\S{2}\\-\\S{4}\$")) {
            loggerError(row, String.format(FORMAT_ERROR_MSG, index, getColumnName(row, 'number2'), "ХХХХХХХХХ/ХХ-ХХХХ"))
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

/** Получить произвольную фиксированную строку со стилями.
 * @param title текст в строке
 * @param alias псевдоним
 * @param isOuter внешний ли итог(влияет на объединение ячеек, не все названия влезают в 2 ячейки)
 * @return
 */
def getFixedRow(String title, String alias, boolean isOuter) {
    def total = formData.createDataRow()
    total.setAlias(alias)
    total.fix = title
    total.getCell('fix').colSpan = isOuter ? 2 : 4
    (allColumns + 'fix').each {
        total.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return total
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Дата операции',
            (xml.row[0].cell[3]): 'Контрагент',
            (xml.row[0].cell[4]): 'Доход контрагента',
            (xml.row[0].cell[6]): 'НДС',
            (xml.row[0].cell[8]): 'Счёт-фактура',
            (xml.row[1].cell[4]): 'Вид',
            (xml.row[1].cell[5]): 'Сумма',
            (xml.row[1].cell[6]): 'Номер мемориального ордера',
            (xml.row[1].cell[7]): 'Сумма',
            (xml.row[1].cell[8]): 'Дата',
            (xml.row[1].cell[9]): 'Номер',
            (xml.row[2].cell[0]): '1'
    ]
    (2..9).each { index ->
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
        newRow.contragent =  row.cell[3].text()

        // графа 4
        newRow.type = row.cell[4].text()

        // графа 5
        newRow.sum = parseNumber(row.cell[5].text(), xlsIndexRow, 5 + colOffset, logger, true)

        // графа 6
        newRow.number = row.cell[6].text()

        // графа 7
        newRow.sum2 = parseNumber(row.cell[7].text(), xlsIndexRow, 7 + colOffset, logger, true)

        // графа 8
        newRow.date = parseDate(row.cell[8].text(), "dd.MM.yyyy", xlsIndexRow, 8 + colOffset, logger, true)

        // графа 9
        newRow.number2 = row.cell[9].text()

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

    boolean isGroups = dataRows.find { it.getAlias() != null && it.getAlias().startsWith("head_") } != null
    if (!isGroups) {
        def totalRow = dataRows.find { it.getAlias() != null && it.getAlias().equals('total')}
        dataRows.remove(totalRow)
        sortRows(dataRows, sortColumns)
        dataRows.add(totalRow)
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
                sortRows(rows, sortColumns)
            } else {
                logger.warn("Ошибка при сортировке. Нарушена структура налоговой формы. Отсутствуют строки заголовоков/итогов по подразделениям.")
            }
        }
    }

    dataRowHelper.saveSort()
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
    int COLUMN_COUNT = 9
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
    def totalTF = null		// итоговая строка со значениями из тф для добавления
    def newRows = []

    while ((rowCells = reader.readNext()) != null) {
        fileRowIndex++

        def isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
        if (isEmptyRow) {
            if (countEmptyRow > 0) {
                // если встретилась вторая пустая строка, то дальше только строки итогов и ЦП
                // итоговая строка тф
                rowCells = reader.readNext()
                isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
                totalTF = (isEmptyRow ? null : getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, ++rowIndex))
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

    if (newRows.size() != 0) {
        dataRowHelper.insert(newRows, dataRowHelper.allCached.size() + 1)
    }

    // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
    def totalColumnsIndexMap = ['sum' : 5, 'sum2' : 7]
    def totalRow = getFixedRow('Итого', 'total', true)
    calcTotalSum(dataRowHelper.allCached, totalRow, totalColumnsIndexMap.keySet().asList())
    // добавить итоговую строку
    dataRowHelper.insert(totalRow, dataRowHelper.allCached.size() + 1)

    // сравнение итогов
    if (totalTF) {
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
    newRow.contragent =  pure(rowCells[colIndex])

    // графа 4
    colIndex++
    newRow.type = pure(rowCells[colIndex])

    // графа 5
    colIndex++
    newRow.sum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6
    colIndex++
    newRow.number = pure(rowCells[colIndex])

    // графа 7
    colIndex++
    newRow.sum2 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 8
    colIndex++
    newRow.date = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9
    colIndex++
    newRow.number2 = pure(rowCells[colIndex])

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}