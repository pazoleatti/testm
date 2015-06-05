package form_template.income.output3_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

/**
 * Сумма налога, подлежащая уплате в бюджет, по данным налогоплательщика (начиная с год 2014)
 * formTemplateId=1412
 *
 * http://conf.aplana.com/pages/viewpage.action?pageId=8784122
 *
 * @author Stanislav Yasinskiy
 * @author Bulat Kinzyabulatov
 */

// графа 1 - paymentType
// графа 2 - okatoCode
// графа 3 - budgetClassificationCode
// графа 4 - dateOfPayment
// графа 5 - sumTax

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
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
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
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]
@Field
def providerCache = [:]
@Field
def recordCache = [:]

@Field
def editableColumns = ['paymentType', 'dateOfPayment', 'sumTax']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['okatoCode', 'budgetClassificationCode']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['paymentType', 'okatoCode', 'budgetClassificationCode', 'dateOfPayment', 'sumTax']

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

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Получение Id записи с использованием кэширования
def getRecordId(def ref_id, String alias, String value, Date date) {
    String filter = "LOWER($alias) = LOWER('$value')"
    if (value == '') filter = "$alias is null"
    if (recordCache[ref_id] != null) {
        if (recordCache[ref_id][filter] != null) {
            return recordCache[ref_id][filter]
        }
    } else {
        recordCache[ref_id] = [:]
    }
    def records = refBookFactory.getDataProvider(ref_id).getRecords(date, null, filter, null)
    if (records.size() == 1) {
        recordCache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        return recordCache[ref_id][filter]
    }
    return null
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (!dataRows.isEmpty()) {

        for (def row in dataRows) {
            // графа 2
            row.okatoCode = "45397000"
            // графа 3
            def paymentType = getRefBookValue(24, row.paymentType)?.CODE?.stringValue
            if ('1'.equals(paymentType)) {
                row.budgetClassificationCode = '18210101040011000110'
            } else if ('3'.equals(paymentType)) {
                row.budgetClassificationCode = '18210101070011000110'
            } else if ('4'.equals(paymentType)) {
                row.budgetClassificationCode = '18210101060011000110'
            }
        }

        sortFormDataRows(false)
    }
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row in dataRows) {
        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }
}

void consolidation() {
    def rows = []

    // «Расчет налога на прибыль организаций с доходов, удерживаемого налоговым агентом (источником выплаты доходов)»
    def sourceFormType03 = 10070
    def sourceFormType03Alt = 419
    // «Сведения о уплаченных суммах налога по операциям с ГЦБ»
    def sourceFormTypeGCB = 420
    // «Сведения о суммах налога на прибыль, уплаченного Банком за рубежом»
    def sourceFormTypeFRN = 421

    // получить формы-источники в текущем налоговом периоде
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        def sourceFormData = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
        if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
            def sourceDataRows = formDataService.getDataRowHelper(sourceFormData)?.allSaved
            def newDataRows = []
            switch (it.formTypeId) {
                case sourceFormType03:
                case sourceFormType03Alt:
                    newDataRows = formNewRows03(sourceDataRows)
                    break
                case sourceFormTypeGCB:
                    newDataRows = formNewRowsGCB(sourceDataRows)
                    break
                case sourceFormTypeFRN:
                    newDataRows = formNewRowsFRN(sourceDataRows)
                    break
            }
            if(!newDataRows.isEmpty()) {
                rows.addAll(newDataRows)
            }
        }
    }

    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
}

def formNewRows03(def rows) {
    def newRows = []
    rows.each { row ->
        def newRow = formData.createDataRow()
        newRow.paymentType = getRecordId(24, 'CODE', '1', getReportPeriodEndDate())
        newRow.okatoCode = '45397000'
        newRow.budgetClassificationCode = '18210101040011000110'
        // 28-я графа
        newRow.dateOfPayment = row.withheldDate
        // 27-я графа
        newRow.sumTax = row.withheldSum
        newRows.add(newRow)
    }
    return newRows
}

def formNewRowsGCB(def rows) {
    def newRows = []
    for (row in rows) {
        if (!(row.getAlias() in ['R3', 'R4', 'R5'])) {
            continue
        }
        def newRow = formData.createDataRow()
        newRow.paymentType = getRecordId(24, 'CODE', '3', getReportPeriodEndDate())
        newRow.okatoCode = '45397000'
        newRow.budgetClassificationCode = '18210101070011000110'
        // есть графа 3 источника
        newRow.dateOfPayment = row.date
        // есть графа 4 источника
        newRow.sumTax = row.sum
        newRows.add(newRow)
    }
    return newRows
}

def formNewRowsFRN(def rows) {
    def newRows = []
    def row = getDataRow(rows, 'SUM_DIVIDENDS')
    def newRow = formData.createDataRow()
    newRow.paymentType = getRecordId(24, 'CODE', '4', getReportPeriodEndDate())
    newRow.okatoCode = '45397000'
    newRow.budgetClassificationCode = '18210101060011000110'
    // есть графа 3 источника
    newRow.dateOfPayment = row.dealDate
    // есть графа 4 источника
    newRow.sumTax = row.taxSum
    newRows.add(newRow)
    return newRows
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 5
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'paymentType')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

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
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
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
            (headerRows[0][0]) : getColumnName(tmpRow, 'paymentType'),
            (headerRows[0][1]) : getColumnName(tmpRow, 'okatoCode'),
            (headerRows[0][2]) : getColumnName(tmpRow, 'budgetClassificationCode'),
            (headerRows[0][3]) : getColumnName(tmpRow, 'dateOfPayment'),
            (headerRows[0][4]) : getColumnName(tmpRow, 'sumTax'),
    ]
    (0..4).each { index ->
        headerMapping.put((headerRows[1][index]), (index + 1).toString())
    }
    checkHeaderEquals(headerMapping)
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
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    // графа 1
    def colIndex = 0
    newRow.paymentType = getRecordIdImport(24, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 2
    colIndex++
    newRow.okatoCode = values[colIndex]

    // графа 3
    colIndex++
    newRow.budgetClassificationCode = values[colIndex]

    // графа 4
    colIndex++
    newRow.dateOfPayment = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex++
    newRow.sumTax = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}