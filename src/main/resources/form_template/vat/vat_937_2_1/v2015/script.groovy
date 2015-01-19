package form_template.vat.vat_937_2_1.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Сведения из дополнительных листов книги продаж
 * formTemplateId=617
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
                  'paymentDocNumDat', 'currNameCode saleCostACurr', 'saleCostARub', 'saleCostB18', 'saleCostB1', ' saleCostB0', 'vatSum18', 'vatSum10', 'bonifSalesSum']

// Редактируемые атрибуты (графа 3..6, 8)
@Field
//TODO:Добавить алиасы редактируемых колонок, когда будут известны
def editableColumns = []

// Проверяемые на пустые значения атрибуты для разделов 1, 2, 3
@Field
def nonEmptyColumns1 = ['rowNumber', 'opTypeCode', 'invoiceNumDate']

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

// Получение импортируемых данных
void importData() {
    //TODO: Реализовать метод.
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    //TODO: Реализовать данный метод, когда будет известна логика.
}

void importTransportData() {
    //TODO: Реализовать данный метод при необходимости.
}

void addTransportData(def xml) {
    //TODO: Реализовать данный метод при необходимости.
}

// Сортировка групп и строк
void sortFormDataRows() {
    //TODO: Реализовать данный метод при необходимости.
}
