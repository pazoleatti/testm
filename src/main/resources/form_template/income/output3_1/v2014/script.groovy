package form_template.income.output3_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
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
        break
    case FormDataEvent.IMPORT:
        importData()
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
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
        dataRowHelper.update(dataRows);

        // Сортировка групп и строк
        sortFormDataRows()
    }
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (def row in dataRows) {
        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    // «Расчет налога на прибыль организаций с доходов, удерживаемого налоговым агентом (источником выплаты доходов)»
    def sourceFormType03 = 419
    // «Сведения о уплаченных суммах налога по операциям с ГЦБ»
    def sourceFormTypeGCB = 420
    // «Сведения о суммах налога на прибыль, уплаченного Банком за рубежом»
    def sourceFormTypeFRN = 421

    // получить формы-источники в текущем налоговом периоде
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        def sourceFormData = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
        if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
            def sourceDataRows = formDataService.getDataRowHelper(sourceFormData)?.all
            def newDataRows = []
            switch (it.formTypeId) {
                case sourceFormType03:
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
                dataRows.addAll(newDataRows)
            }
        }
    }
    dataRowHelper.save(dataRows)
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
        // 29-я графа
        newRow.sumTax = row.withheldNumber
        newRows.add(newRow)
    }
    return newRows
}

def formNewRowsGCB(def rows) {
    def newRows = []
    for (row in rows) {
        if (!row.getAlias() in ['R3', 'R4', 'R5'])
            continue
        def newRow = formData.createDataRow()
        newRow.paymentType = getRecordId(24, 'CODE', '2', getReportPeriodEndDate())
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

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'Вид платежа', null, 5, 2)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]) : 'Вид платежа',
            (xml.row[0].cell[1]) : 'Код по ОКТМО',
            (xml.row[0].cell[2]) : 'Код бюджетной классификации',
            (xml.row[0].cell[3]) : 'Срок уплаты',
            (xml.row[0].cell[4]) : 'Сумма налога, подлежащая уплате'
    ]
    (0..4).each { index ->
        headerMapping.put((xml.row[1].cell[index]), (index + 1).toString())
    }

    checkHeaderEquals(headerMapping)

    // добавить данные в форму
    addData(xml, 2)
}

void addData(def xml, headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    // количество графов в таблице
    def columnCount = 5
    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

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

        // графа 1
        def xmlIndexCol = 0
        newRow.paymentType = getRecordIdImport(24, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)
        xmlIndexCol++

        // графа 2
        newRow.okatoCode = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 3
        newRow.budgetClassificationCode = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 4
        newRow.dateOfPayment = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 5
        newRow.sumTax = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    dataRowHelper.saveSort()
}