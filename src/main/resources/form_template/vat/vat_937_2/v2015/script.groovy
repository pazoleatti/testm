package form_template.vat.vat_937_2.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Итоговые данные книги продаж
 * formTemplateId=1608
 */

// графа 1 -  rowNumber
// графа 2 -  opTypeCode
// графа 3 -  invoiceNumDate
// графа 4 -  invoiceCorrNumDate
// графа 5 -  corrInvoiceNumDate
// графа 6 -  corrInvCorrNumDate
// графа 7 -  buyerName
// графа 8 -  buyerInnKpp
// графа 9 -  mediatorName
// графа 10 - mediatorInnKpp
// графа 11 - paymentDocNumDate
// графа 12 - currNameCode
// графа 13 - saleCostACurr
// графа 14   saleCostARub
// графа 15 - saleCostB18
// графа 16 - saleCostB10
// графа 17 - saleCostB0
// графа 18 - vatSum18
// графа 19 - vatSum10
// графа 20 - bonifSalesSum

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
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
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
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
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

@Field
def allColumns = ['rowNumber', 'opTypeCode', 'invoiceNumDate', 'invoiceCorrNumDate', 'corrInvoiceNumDate', 'corrInvCorrNumDate', 'buyerName', 'buyerInnKpp', 'mediatorName', 'mediatorInnKpp',
                  'paymentDocNumDate', 'currNameCode', 'saleCostACurr', 'saleCostARub', 'saleCostB18', 'saleCostB10', 'saleCostB0', 'vatSum18', 'vatSum10', 'bonifSalesSum']

// Проверяемые на пустые значения атрибуты для разделов 1, 2, 3
@Field
def nonEmptyColumns1 = ['rowNumber', 'opTypeCode', 'invoiceNumDate']

// TODO (Ramil Timerbaev) пока редактируемыми сделал все поля кроме нумерации
// Редактируемые атрибуты (графа )
@Field
def editableColumns = allColumns - 'rowNumber'

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// TODO (Ramil Timerbaev)
// Атрибуты итоговых строк для которых вычисляются суммы (графа )
@Field
def totalSumColumns = []

// Признак периода ввода остатков
@Field
def isBalancePeriod

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
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Добавить новую строку (строки между заглавными строками и строками итогов)
def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index = 1
    if (currentDataRow != null) {
        def alias = currentDataRow.getAlias()
        index = currentDataRow.getIndex()
        if (alias == null || alias.startsWith('head_')) {
            index++
        }
    } else {
        def lastRow = getDataRow(dataRows, 'total_7')
        if (lastRow != null) {
            index = lastRow.getIndex()
        }
    }
    def isSection7 = (index > getDataRow(dataRows, 'head_7').getIndex())
    dataRowHelper.insert(getNewRow(isSection7), index)
}

// Получить новую строку с заданными стилями
def getNewRow(def isSection7) {
    def row = formData.createDataRow()
    def columns = (isSection7 ? editableColumns + 'ndsRate' : editableColumns)
    columns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    (allColumns - columns).each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return row
}

void calc() {
    //TODO: Реализовать данный метод, когда будет известна логика.
}

void logicCheck() {
    //TODO: Реализовать данный метод, когда будет известна логика.
}

void consolidation() {
    //TODO: Реализовать данный метод, когда будет известна логика.
}

void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNumber'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 20, 3)

    def headerMapping = [
            (xml.row[0].cell[0])  : getColumnName(tmpRow, 'rowNumber'),
            (xml.row[0].cell[1])  : getColumnName(tmpRow, 'opTypeCode'),
            (xml.row[0].cell[2])  : getColumnName(tmpRow, 'invoiceNumDate'),
            (xml.row[0].cell[3])  : getColumnName(tmpRow, 'invoiceCorrNumDate'),
            (xml.row[0].cell[4])  : getColumnName(tmpRow, 'corrInvoiceNumDate'),
            (xml.row[0].cell[5])  : getColumnName(tmpRow, 'corrInvCorrNumDate'),
            (xml.row[0].cell[6])  : getColumnName(tmpRow, 'buyerName'),
            (xml.row[0].cell[7])  : getColumnName(tmpRow, 'buyerInnKpp'),

            (xml.row[0].cell[8])  : 'Сведения о посреднике (комиссионере, агенте)',
            (xml.row[1].cell[8])  : 'наименование посредника',
            (xml.row[1].cell[9])  : 'ИНН/КПП посредника',

            (xml.row[0].cell[10]) : getColumnName(tmpRow, 'paymentDocNumDate'),
            (xml.row[0].cell[11]) : getColumnName(tmpRow, 'currNameCode'),

            (xml.row[0].cell[12]) : 'Стоимость продаж по счету-фактуре, разница стоимости по корректировочному счету-фактуре (включая НДС)',
            (xml.row[1].cell[12]) : 'в валюте счета-фактуры',
            (xml.row[1].cell[13]) : 'в рублях и копейках',

            (xml.row[0].cell[14]) : 'Стоимость продаж, облагаемых налогом, по счету-фактуре, разница стоимости по корректировочному счету-фактуре (без НДС) в рублях и копейках, по ставке',
            (xml.row[1].cell[14]) : '18 процентов',
            (xml.row[1].cell[15]) : '10 процентов',
            (xml.row[1].cell[16]) : '0 процентов',

            (xml.row[0].cell[17]) : 'Сумма НДС по счету-фактуре, разница стоимости по корректировочному счету-фактуре в рублях и копейках, по ставке',
            (xml.row[1].cell[17]) : '8 процентов',
            (xml.row[1].cell[18]) : '10 процентов',

            (xml.row[0].cell[19]) : getColumnName(tmpRow, 'bonifSalesSum')
    ]
    (0..19).each { index ->
        headerMapping.put((xml.row[2].cell[index]), (index + 1).toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def xmlIndexRow = -1
    def int rowIndex = 1
    def rows = []

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        /* Пропуск строк шапок */
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[0].text() == null || row.cell[0].text() == "") {
            continue
        }

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)

        // Графа 2
        def xmlIndexCol = 1
        newRow.opTypeCode = row.cell[xmlIndexCol].text()

        // Графа 3
        xmlIndexCol++
        newRow.invoiceNumDate = row.cell[xmlIndexCol].text()

        // Графа 4
        xmlIndexCol++
        newRow.invoiceCorrNumDate = row.cell[xmlIndexCol].text()

        // Графа 5
        xmlIndexCol++
        newRow.corrInvoiceNumDate = row.cell[xmlIndexCol].text()

        // Графа 6
        xmlIndexCol++
        newRow.corrInvCorrNumDate = row.cell[xmlIndexCol].text()

        // Графа 7
        xmlIndexCol++
        newRow.buyerName = row.cell[xmlIndexCol].text()

        // Графа 8
        xmlIndexCol++
        newRow.buyerInnKpp = row.cell[xmlIndexCol].text()

        // Графа 9
        xmlIndexCol++
        newRow.mediatorName = row.cell[xmlIndexCol].text()

        // Графа 10
        xmlIndexCol++
        newRow.mediatorInnKpp = row.cell[xmlIndexCol].text()

        // Графа 11
        xmlIndexCol++
        newRow.paymentDocNumDate = row.cell[xmlIndexCol].text()

        // Графа 12
        xmlIndexCol++
        newRow.currNameCode = row.cell[xmlIndexCol].text()

        // Графа 13
        xmlIndexCol++
        newRow.saleCostACurr = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 14
        xmlIndexCol++
        newRow.saleCostARub = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 15
        xmlIndexCol++
        newRow.saleCostB18 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 16
        xmlIndexCol++
        newRow.saleCostB10 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 17
        xmlIndexCol++
        newRow.saleCostB0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 18
        xmlIndexCol++
        newRow.vatSum18 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 19
        xmlIndexCol++
        newRow.vatSum10 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 20
        xmlIndexCol++
        newRow.bonifSalesSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

// TODO: После получения постановки при необходимости удалить данный метод
void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 20, 0)
    addTransportData(xml)

    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    checkTotalSum(dataRows, totalSumColumns, logger, false)
}

// TODO: После получения постановки при необходимости исправить или удалить данный метод
void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def int rnuIndexRow = 2
    def int colOffset = 1

    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(rnuIndexRow)

        // Графа 2
        def xmlIndexCol = 2
        newRow.opTypeCode = row.cell[xmlIndexCol].text()

        // Графа 3
        xmlIndexCol++
        newRow.invoiceNumDate = row.cell[xmlIndexCol].text()

        // Графа 4
        xmlIndexCol++
        newRow.invoiceCorrNumDate = row.cell[xmlIndexCol].text()

        // Графа 5
        xmlIndexCol++
        newRow.corrInvoiceNumDate = row.cell[xmlIndexCol].text()

        // Графа 6
        xmlIndexCol++
        newRow.corrInvCorrNumDate = row.cell[xmlIndexCol].text()

        // Графа 7
        xmlIndexCol++
        newRow.buyerName = row.cell[xmlIndexCol].text()

        // Графа 8
        xmlIndexCol++
        newRow.buyerInnKpp = row.cell[xmlIndexCol].text()

        // Графа 9
        xmlIndexCol++
        newRow.mediatorName = row.cell[xmlIndexCol].text()

        // Графа 10
        xmlIndexCol++
        newRow.mediatorInnKpp = row.cell[xmlIndexCol].text()

        // Графа 11
        xmlIndexCol++
        newRow.paymentDocNumDate = row.cell[xmlIndexCol].text()

        // Графа 12
        xmlIndexCol++
        newRow.currNameCode = row.cell[xmlIndexCol].text()

        // Графа 13
        xmlIndexCol++
        newRow.saleCostACurr = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 14
        xmlIndexCol++
        newRow.saleCostARub = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 15
        xmlIndexCol++
        newRow.saleCostB18 = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 16
        xmlIndexCol++
        newRow.saleCostB10 = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 17
        xmlIndexCol++
        newRow.saleCostB0 = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 18
        xmlIndexCol++
        newRow.vatSum18 = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 19
        xmlIndexCol++
        newRow.vatSum10 = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 20
        xmlIndexCol++
        newRow.bonifSalesSum = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        def total = getTotalRow()

        // TODO (Ramil Timerbaev) когда будет готово чтз, уточнить какие графы испольвовать
        // Графа 13
        def xmlIndexCol = 13
        total.saleCostACurr = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 14
        xmlIndexCol++
        total.saleCostARub = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 15
        xmlIndexCol++
        total.saleCostB18 = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 16
        xmlIndexCol++
        total.saleCostB10 = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 17
        xmlIndexCol++
        total.saleCostB0 = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 18
        xmlIndexCol++
        total.vatSum18 = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 19
        xmlIndexCol++
        total.vatSum10 = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 20
        xmlIndexCol++
        total.bonifSalesSum = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(total)
    }
    dataRowHelper.save(rows)
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
    columns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

/** Получить пустую итоговую строку со стилями. */
def getTotalRow() {
    def total = formData.createDataRow()
    total.setAlias('total')
    // TODO (Ramil Timerbaev) возможно надо будет добавить скрытый столбец fix
    // total.КАКАЯ_ТО_СТРОКА = 'Итого'
    // total.getCell('КАКАЯ_ТО_СТРОКА').colSpan = 2
    allColumns.each {
        total.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return total
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

// Сортировка групп и строк
void sortFormDataRows() {
    //TODO: Реализовать данный метод при необходимости.
}