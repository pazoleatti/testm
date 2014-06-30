package form_template.income.rnu50.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-50) Регистр налогового учёта «ведомость понесённых убытков от реализации амортизируемого имущества»"
 * formTypeId=365
 *
 * @author rtimerbaev
 */

// графа 1  - rowNumber
// графа    - fix
// графа 2  - rnu49rowNumber
// графа 3  - invNumber
// графа 4  - lossReportPeriod
// графа 5  - lossTaxPeriod

@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkSourceAccepted()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        checkSourceAccepted()
        calc()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        checkSourceAccepted()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
}

// Все атрибуты
@Field
def allColumns = ['rowNumber', 'fix', 'rnu49rowNumber', 'invNumber', 'lossReportPeriod', 'lossTaxPeriod']

// Группируемые атрибуты (графа 2, 3)
@Field
def groupColumns = ['rnu49rowNumber', 'invNumber']

// Проверяемые на пустые значения атрибуты (графа 1..5)
@Field
def nonEmptyColumns = ['rowNumber', 'rnu49rowNumber', 'invNumber', 'lossReportPeriod', 'lossTaxPeriod']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4, 5)
@Field
def totalColumns = ['lossReportPeriod', 'lossTaxPeriod']

// Дата начала отчетного периода
@Field
def reportPeriodStartDate = null

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

@Field
def sourceFormData = null

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    if (!isConsolidated && formDataEvent != FormDataEvent.IMPORT) {
        // удалить все строки
        dataRows.clear()
        dataRows = getCalcDataRows()
    } else {
        // удалить строку "итого"
        deleteAllAliased(dataRows)
    }

    if (dataRows) {
        // сортировка
        sortRows(dataRows, groupColumns)

        def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')?.intValue()
        dataRows.eachWithIndex { row, i ->
            row.setIndex(i + 1)

            // графа 1
            row.rowNumber = ++rowNumber
        }
    }

    // итоги
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)

    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')?.intValue()
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей (графа 1..5)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «№ пп» (графа 1)
        if (++rowNumber != row.rowNumber) {
            rowWarning(logger, row, errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // 3. Проверка на нулевые значения
        if (row.lossReportPeriod == 0 && row.lossTaxPeriod == 0) {
            rowError(logger, row, errorMsg + "Все суммы по операции нулевые!")
        }

        // 4. Проверка формата номера записи в РНУ-49 (графа 2)
        if (!row.rnu49rowNumber.matches('\\w{2}-\\w{6}')) {
            rowError(logger, row, errorMsg + "Неправильно указан номер записи в РНУ-49 (формат: ГГ-НННННН, см. №852-р в актуальной редакции)!")
        }
    }

    // 8. Проверка итоговых значений формы	Заполняется автоматически
    checkTotalSum(dataRows, totalColumns, logger, true)

    if (isConsolidated) {
        return
    }

    // 5, 6, 7. Проверка соответствия данным формы РНУ-49. Арифметическая проверка графы 1..5
    def currentRows = dataRows.findAll { row -> row.getAlias() == null }
    def tmpRows = getCalcDataRows()
    if ((!currentRows && tmpRows) || (currentRows && !tmpRows)) {
        logger.error('Значения не соответствуют данным РНУ-49')
    } else if (currentRows && tmpRows) {
        def arithmeticCheckAlias = nonEmptyColumns - 'rowNumber'
        def errorRows = []
        for (def row : currentRows) {
            def tmpRow = tmpRows.find { it.invNumber == row.invNumber }
            if (tmpRow == null) {
                errorRows.add(row.getIndex())
                continue
            }
            def msg = []
            arithmeticCheckAlias.each { alias ->
                def value1 = row.getCell(alias).value
                def value2 = tmpRow.getCell(alias).value
                if (value1 != value2) {
                    msg.add('«' + getColumnName(row, alias) + '»')
                }
            }
            if (!msg.isEmpty()) {
                def columns = msg.join(', ')
                def index = row.getIndex()
                rowError(logger, row, "Строка $index: Неверное значение граф: $columns")
            }
            tmpRows.remove(tmpRow)
        }
        if (!errorRows.isEmpty()) {
            def indexes = errorRows.join(', ')
            logger.error("Значения не соответствуют данным РНУ-49 в строках: $indexes")
        }
        if (!tmpRows.isEmpty()) {
            logger.error('Значения не соответствуют данным РНУ-49. Необходимо рассчитать данные')
        }
    }
}

// Выполняется при расчете. Получение данных из рну 49
def getCalcDataRows() {
    def dataRows = []
    def dataRows49 = getDataRowsFromSource()
    if (dataRows49 == null) {
        return dataRows
    }

    def start = getStartDate()
    def end = getEndDate()

    for (def row49 : dataRows49) {
        if (row49.getAlias() == null && row49.usefullLifeEnd != null &&
                row49.monthsLoss != null && row49.expensesSum != null) {
            def newRow = formData.createDataRow()

            // графа 3
            newRow.invNumber = calc3(row49)
            // графа 2
            newRow.rnu49rowNumber = calc2(row49, newRow)
            // графа 4
            newRow.lossReportPeriod = calc4(row49, start, end)
            // графа 5
            newRow.lossTaxPeriod = calc5(row49, start, end)

            dataRows.add(newRow)
        }
    }
    return dataRows
}

/** Получить данные формы РНУ-49 (id = 312) */
def getFormDataSource() {
    if (sourceFormData == null) {
        sourceFormData = formDataService.find(312, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    }
    return sourceFormData
}

void checkSourceAccepted() {
    if (!isConsolidated) {
        formDataService.checkFormExistAndAccepted(312, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, false, logger, true)
    }
}

/** Получить строки из нф РНУ-32.1. */
def getDataRowsFromSource() {
    def formDataSource = getFormDataSource()
    if (formDataSource != null) {
        return formDataService.getDataRowHelper(formDataSource)?.allCached
    }
    return null
}

def getStartDate() {
    if (reportPeriodStartDate == null) {
        reportPeriodStartDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId)?.time
    }
    return reportPeriodStartDate
}

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

def calc3(def row49) {
    return row49.invNumber
}

def calc2(def row49, def row) {
    return (row.invNumber ? row49.firstRecordNumber : null)
}

def calc4(def row49, def startDate, def endDate) {
    def result = null
    def column3 = row49.operationDate
    def column18 = row49.usefullLifeEnd
    def column20 = row49.expensesSum

    if (startDate <= column3 && column3 <= endDate) {
        if (column18 > column3) {
            result = column20 * (endDate[Calendar.MONTH] - column3[Calendar.MONTH])
        } else {
            result = column20
        }
    }
    return result?.setScale(2, RoundingMode.HALF_UP)
}

def calc5(def row49, def startDate, def endDate) {
    def result = null
    def column18 = row49.usefullLifeEnd
    def column20 = row49.expensesSum

    if (column18 < startDate) {
        result = column20 * 3
    } else if (startDate <= column18 && column18 <= endDate) {
        result = column20 * (endDate[Calendar.MONTH] - column18[Calendar.MONTH])
    }
    return result?.setScale(2, RoundingMode.HALF_UP)
}

def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.rnu49rowNumber = 'Итого'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNumber'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 3)

    def headerMapping = [
            (xml.row[0].cell[0]): getColumnName(tmpRow, 'rowNumber'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'rnu49rowNumber'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'invNumber'),
            (xml.row[0].cell[4]): 'Убыток, приходящийся на отчётный период',
            (xml.row[1].cell[4]): 'от реализации в отчётном налоговом периоде',
            (xml.row[1].cell[5]): 'от реализации в предыдущих налоговых периодах',
            (xml.row[2].cell[0]): '1'
    ]
    (2..5).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 3)
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

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[0].text() == null || row.cell[0].text() == '') {
            continue
        }

        def int xlsIndexRow = xmlIndexRow + rowOffset
        def xmlIndexCol = 0

        def newRow = formData.createDataRow()
        newRow.setImportIndex(xlsIndexRow)

        // графа 1
        xmlIndexCol++
        // fix
        xmlIndexCol++
        // графа 2
        newRow.rnu49rowNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 3
        newRow.invNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 4
        newRow.lossReportPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 5
        newRow.lossTaxPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}