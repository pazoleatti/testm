package form_template.income.rnu64.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * РНУ-64 "Регистр налогового учёта затрат, связанных с проведением сделок РЕПО"
 * formTypeId=1355
 *
 * @author auldanov
 * @author bkinzyabulatov
 *
 * 1. number - № пп
 * 2. date - Дата сделки
 * 3. part - Часть сделки Справочник
 * 4. dealingNumber - Номер сделки
 * -5. bondKind - Вид ценных бумаг //графу удалили
 * 5. costs - Затраты (руб.коп.)
 */

@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        prevPeriodCheck()
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
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

@Field
def recordCache = [:]

@Field
def providerCache = [:]

@Field
def isBalancePeriod

// все атрибуты
@Field
def allColumns = ['number', 'fix', 'date', 'part', 'dealingNumber', 'costs']

// Редактируемые атрибуты
@Field
def editableColumns = ['date', 'part', 'dealingNumber', 'costs']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number']

@Field
def sortColumns = ['date', 'dealingNumber']

@Field
def totalColumns = ['costs']

@Field
def nonEmptyColumns = ['date', 'part', 'dealingNumber', 'costs']

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

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    if (formDataEvent != FormDataEvent.IMPORT) {
        sortRows(dataRows, sortColumns)
    }

    // пересчитываем строки итого
    calcTotalSum(dataRows, getDataRow(dataRows, 'totalQuarter'), totalColumns)

    if (formData.kind == FormDataKind.PRIMARY) {
        // строка Итого за текущий отчетный (налоговый) период
        def total = getDataRow(dataRows, 'total')
        def dataRowsPrev = getDataRowsPrev()
        total.costs = getTotalValue(dataRows, dataRowsPrev)
    }

    dataRowHelper.save(dataRows)

    sortFormDataRows()
}

def getDataRowsPrev() {
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY && reportPeriod.order != 1) {
        def formDataPrev = formDataService.getFormDataPrev(formData)
        formDataPrev = (formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null)
        if (formDataPrev != null) {
            return formDataService.getDataRowHelper(formDataPrev)?.allCached
        }
    }
    return null
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = null
    def totalQuarterRow = null
    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()

    for (def row : dataRows) {
        // 1. Проверка на заполнение поля
        if (row.getAlias() == null) {

            def index = row.getIndex()
            def errorMsg = "Строка $index: "

            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

            // 2. Проверка даты совершения операции и границ отчетного периода
            if (row.date != null && dFrom != null && dTo != null && !(row.date >= dFrom && row.date <= dTo)) {
                loggerError(row, errorMsg + "Дата совершения операции вне границ отчетного периода!")
            }

            // 4. Проверка на нулевые значения
            if (row.costs == 0) {
                loggerError(row, errorMsg + "Все суммы по операции нулевые!")
            }
        } else if (row.getAlias() == 'total') {
            totalRow = row
        } else if (row.getAlias() == 'totalQuarter') {
            totalQuarterRow = row
        }
    }
    def testRows = dataRows.findAll { it -> it.getAlias() == null }
    // проверка на наличие итоговых строк, иначе будет ошибка
    if (totalQuarterRow == null || totalRow == null) {
        // 5. Проверка итоговых значений за текущий квартал
        def testRow = formData.createDataRow()
        calcTotalSum(testRows, testRow, totalColumns)
        if (totalQuarterRow == null || totalQuarterRow != null && totalQuarterRow.costs != testRow.costs) {
            loggerError(null, "Итоговые значения за текущий квартал рассчитаны неверно!")
        }
        // 6. Проверка итоговых значений за текущий отчётный (налоговый) период
        if (!isConsolidated) {
            def dataRowsPrev = getDataRowsPrev()
            if (totalRow == null || totalRow != null && totalRow.costs != getTotalValue(dataRows, dataRowsPrev)) {
                loggerError(null, "Итоговые значения за текущий отчётный (налоговый ) период рассчитаны неверно!")
            }
        }
    }
}

// Функция возвращает итоговые значения за текущий отчётный (налоговый) период
def getTotalValue(def dataRows, def dataRowsPrev) {
    def quarterRow = getDataRow(dataRows, 'totalQuarter')
    def prevQuarterTotalRow
    if (dataRowsPrev != null) {
        prevQuarterTotalRow = getDataRow(dataRowsPrev, "total")
    }
    if (prevQuarterTotalRow != null) {
        return (quarterRow.costs ?: 0) + (prevQuarterTotalRow.costs ?: 0)
    } else {
        return quarterRow.costs ?: 0
    }
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Дата сделки',
            (xml.row[0].cell[3]): 'Часть сделки',
            (xml.row[0].cell[4]): 'Номер сделки',
            (xml.row[0].cell[5]): 'Затраты (руб.коп.)',
            (xml.row[1].cell[0]): '1'
    ]
    (2..5).each { index ->
        headerMapping.put((xml.row[1].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 1)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

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

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 2 - Дата сделки
        newRow.date = parseDate(row.cell[2].text(), "dd.MM.yyyy", xlsIndexRow, 2 + colOffset, logger, true)

        // графа 3 - Часть сделки
        newRow.part = getRecordIdImport(60, 'CODE', row.cell[3].text(), xlsIndexRow, 3 + colOffset)

        // графа 4 - Номер сделки
        newRow.dealingNumber = row.cell[4].text()

        // графа 6 - Затраты (руб.коп.)
        newRow.costs = parseNumber(row.cell[5].text(), xlsIndexRow, 6 + colOffset, logger, true)

        rows.add(newRow)
    }

    // Добавляем итоговые строки
    def existRows = dataRowHelper.allSaved
    rows.add(getDataRow(existRows, 'totalQuarter'))
    rows.add(getDataRow(existRows, 'total'))
    dataRowHelper.save(rows)
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
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

void consolidation() {
    def rows = []
    def sum = 0
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).getAllCached().each { row ->
                    if (row.getAlias() == null) {
                        rows.add(row)
                    } else if (row.getAlias() == 'total' && row.costs != null) {
                        sum += row.costs
                    }
                }
            }
        }
    }
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def totalRow = dataRowHelper.getDataRow(dataRows, 'total')
    totalRow.costs = sum

    rows.add(getDataRow(dataRows, 'totalQuarter'))
    rows.add(totalRow)
    dataRowHelper.save(rows)
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
void prevPeriodCheck() {
    if (isConsolidated || isBalancePeriod()) {
        return
    }
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    if (reportPeriod && reportPeriod.order != 1) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, true, logger, true)
    }
}

void importTransportData() {
    int COLUMN_COUNT = 5
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
    def existRows = dataRowHelper.allCached
    def totalQuarterRow = getDataRow(existRows, 'totalQuarter')
    def totalRow = getDataRow(existRows, 'total')
    dataRowHelper.clear()

    String[] rowCells
    int countEmptyRow = 0	// количество пустых строк
    int fileRowIndex = 0    // номер строки в файле
    int rowIndex = 0        // номер строки в НФ
    int totalRowCount = 0   // счетчик кол-ва итогов
    def total = null		// итоговая строка со значениями из тф для добавления
    def newRows = []

    while ((rowCells = reader.readNext()) != null) {
        fileRowIndex++

        def isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
        if (isEmptyRow) {
            if (countEmptyRow > 0) {
                // если встретилась вторая пустая строка, то дальше только строки итогов и ЦП
                totalRowCount++
                // итоговая строка тф
                total = getNewRow(reader.readNext(), COLUMN_COUNT, ++fileRowIndex, ++rowIndex)
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
    if (total) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = [
                'costs' : 5
        ]

        // итоговая строка для сверки сумм
        def totalTmp = formData.createDataRow()
        totalColumnsIndexMap.keySet().asList().each { alias ->
            totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
        }

        // подсчет итогов
        def dataRows = dataRowHelper.allCached
        for (def row : dataRows) {
            if (row.getAlias()) {
                continue
            }
            totalColumnsIndexMap.keySet().asList().each { alias ->
                def value1 = totalTmp.getCell(alias).value
                def value2 = (row.getCell(alias).value ?: BigDecimal.ZERO)
                totalTmp.getCell(alias).setValue(value1 + value2, null)
            }
        }

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = total.getCell(alias).value
            def v2 = totalTmp.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }

        // пересчитываем строки итого
        calcTotalSum(dataRows, totalQuarterRow, totalColumns)
        // добавить в нф итоговую строку
        dataRowHelper.insert(totalQuarterRow, dataRowHelper.allCached.size() + 1)
        // строка Итого за текущий отчетный (налоговый) период
        def dataRowsPrev = getDataRowsPrev()
        totalRow.costs = getTotalValue(dataRows, dataRowsPrev)
        dataRowHelper.insert(totalRow, dataRowHelper.allCached.size() + 1)
    }
}

/** Добавляет строку в текущий буфер строк. */
boolean addRow(def rows, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells == null) {
        return true
    }
    def newRow = getNewRow(rowCells, columnCount, fileRowIndex, rowIndex)
    if (newRow == null) {
        return false
    }
    rows.add(newRow)
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
    def newRow = formData.createDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    def int colOffset = 1

    // графа 2 - Дата сделки
    newRow.date = parseDate(pure(rowCells[2]), "dd.MM.yyyy", fileRowIndex, 2 + colOffset, logger, true)
    // графа 3 - Часть сделки
    newRow.part = getRecordIdImport(60, 'CODE', pure(rowCells[3]), fileRowIndex, 3 + colOffset, false)
    // графа 4 - Номер сделки
    newRow.dealingNumber = pure(rowCells[4])
    // графа 5 - Затраты (руб.коп.)
    newRow.costs = parseNumber(pure(rowCells[5]), fileRowIndex, 5 + colOffset, logger, true)

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, [getDataRow(dataRows, 'totalQuarter')], getDataRow(dataRows, 'total'), true)
    dataRowHelper.saveSort()
}