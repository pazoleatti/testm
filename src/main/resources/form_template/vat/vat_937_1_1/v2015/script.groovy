package form_template.vat.vat_937_1_1.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * (937.1.1 v2015) Сведения из дополнительных листов книги покупок
 * formTemplate = 616
 *
 * fix
 * 1  rowNum                      № п/п
 * 2  typeCode 		              Код вида операции
 * 3  invoice                     Номер и дата счета-фактуры продавца
 * 4  invoiceCorrecting           Номер и дата исправления счета-фактуры продавца
 * 5  invoiceCorrection           Номер и дата корректировочного счета-фактуры продавца
 * 6  invoiceCorrectingCorrection Номер и дата исправления корректировочного счета-фактуры продавца
 * 7  documentPay                 Номер и дата документа, подтверждающего уплату налога
 * 8  dateRegistration            Дата принятия на учет товаров (работ, услуг), имущественных прав
 * 9  salesman                    Наименование продавца
 * 10 salesmanInnKpp              ИНН/КПП продавца
 * 11 agentName                   Сведения о посреднике (комиссионере, агенте). Наименование посредника
 * 12 agentInnKpp                 Сведения о посреднике (комиссионере, агенте). ИНН/КПП посредника
 * 13 declarationNum              Номер таможенной декларации
 * 14 currency                    Наименование и код валюты
 * 15 cost                        Стоимость покупок по счету-фактуре, разница стоимости по корректировочному счету-фактуре (включая НДС) в валюте счета-фактуры
 * 16 nds                         Сумма НДС по счету-фактуре, разница суммы НДС по корректировочному счету-фактуре, принимаемая к вычету, в рублях и копейках
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
        addNewRow()
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
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

@Field
def allColumns = ['rowNum', 'typeCode', 'invoice', 'invoiceCorrecting', 'invoiceCorrection', 'invoiceCorrectingCorrection', 'documentPay', 'dateRegistration',
                  'salesman', 'salesmanInnKpp', 'agentName', 'agentInnKpp', 'declarationNum', 'currency', 'cost', 'nds']

// Редактируемые атрибуты (графа )
@Field
def editableColumns = allColumns - 'rowNum'

// Проверяемые на пустые значения атрибуты (графа )
@Field
def nonEmptyColumns = ['typeCode', 'invoice']

// Атрибуты итоговых строк для которых вычисляются суммы (графа )
@Field
def totalSumColumns = ['nds']

// Сортируемые атрибуты (графа 8, 3, 2, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16)
@Field
def sortColumns = ['dateRegistration', 'invoice', 'typeCode', 'invoiceCorrecting', 'invoiceCorrection',
        'invoiceCorrectingCorrection', 'documentPay', 'salesman', 'salesmanInnKpp', 'agentName',
        'agentInnKpp', 'declarationNum', 'currency', 'cost', 'nds']

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

void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }

    def index
    if (currentDataRow != null && currentDataRow.getIndex() != -1 && currentDataRow.getAlias() in [null, 'head']) {
        index = currentDataRow.getIndex() + 1
    } else {
        index = getDataRow(dataRows, 'total').getIndex()
    }
    dataRowHelper.insert(newRow, index)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = getDataRow(dataRows, 'total')

    calcTotalSum(dataRows, totalRow, totalSumColumns)

    dataRowHelper.save(dataRows)

    // Сортировка групп и строк
    sortFormDataRows()
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def FILLED_FILLED_ERROR_MSG = "Строка %s: В случае если графа «%s» заполнена, должна быть заполнена графа «%s»!"
    def ONE_FMT_ERROR_MSG = "Строка %s: Графа «%s» заполнена неверно! Ожидаемый формат: «%s». Оба поля обязательны для заполнения."
    def TWO_FMT_ERROR_MSG = "Строка %s: Графа «%s» заполнена неверно! Ожидаемый формат: «%s»."

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        // Проверка заполнения граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        //	Если заполнена «Графа 6», то заполнена «Графа 5»
        if (row.invoiceCorrectingCorrection != null && row.invoiceCorrection == null){
            loggerLog(row, String.format(FILLED_FILLED_ERROR_MSG, index, getColumnName(row,'invoiceCorrectingCorrection'), getColumnName(row,'invoiceCorrection')))
        }
        //	Если «Графа 2» принимает хотя бы одно из значений диапазона: 01-05 | 07-13, то заполнена «Графа 10»
        if (row.typeCode && row.typeCode.matches("^[0-9]{2}\$") && Integer.valueOf(row.typeCode) in ((01..05) + (07..13)) && row.salesmanInnKpp == null){
            loggerLog(row, String.format("Строка %s: В случае если графа «%s» принимает значение из диапазона: 01-05 | 07-13, должна быть заполнена графа «%s»!", index, getColumnName(row,'typeCode'), getColumnName(row,'salesmanInnKpp')))
        }
        // Проверки форматов
        // графа 3
        if (row.invoice && !row.invoice.matches("^\\S.{0,999}( ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4}))?\$")) {
            loggerLog(row, String.format("Строка %s: Графа «%s» заполнена неверно! Ожидаемое значение: «%s». Только номер обязателен для заполнения.", index, getColumnName(row,'invoice'), "<Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 4
        if (row.invoiceCorrecting && !row.invoiceCorrecting.matches("^\\d{1,3} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrecting'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 5
        if (row.invoiceCorrection && !row.invoiceCorrection.matches("^\\S.{0,255} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrection'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 6
        if (row.invoiceCorrectingCorrection && !row.invoiceCorrectingCorrection.matches("^\\d{1,3} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrectingCorrection'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 7
        if (row.documentPay && !row.documentPay.matches("^\\S.{0,255} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'documentPay'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата» формат, «ДД.ММ.ГГГГ»>"))
        }
        // графа 10
        if (row.salesmanInnKpp && !row.salesmanInnKpp.matches("^(\\d{12}|\\d{10}/\\d{9})\$")) {
            loggerLog(row, String.format(TWO_FMT_ERROR_MSG, index, getColumnName(row,'salesmanInnKpp'), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
        }
        // графа 12
        if (row.agentInnKpp && !row.agentInnKpp.matches("^(\\d{12}|\\d{10}/\\d{9})\$")) {
            loggerLog(row, String.format(TWO_FMT_ERROR_MSG, index, getColumnName(row,'agentInnKpp'), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
        }
        // графа 14
        if (row.currency && !row.currency.matches("^\\S.{0,254} \\S{3}\$")) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'currency'), "<Наименование: тип поля «Строка/255/»> <Код: тип поля «Строка/3/», формат «ХХХ»>"))
        }
        // графа 2
        if (row.typeCode && (!row.typeCode.matches("^[0-9]{2}\$") || !(Integer.valueOf(row.typeCode) in ((1..13) + (16..28))))) {
            loggerLog(row, String.format("Строка <Номер строки>: Графа «%s» заполнена неверно! Графа «%s» должна принимать значение из следующего диапазона: 01, 02, …,13, 16, 17, …, 28.", index, getColumnName(row,'typeCode'), getColumnName(row,'typeCode')))
        }
    }

    def headRow = getDataRow(dataRows, 'head')
    checkNonEmptyColumns(headRow, headRow.getIndex(), totalSumColumns, logger, !isBalancePeriod())

    checkTotalSum(dataRows, totalSumColumns, logger, !isBalancePeriod())
}

void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNum'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 16, 3)

    def headerMapping = [
            (xml.row[0].cell[0])  : getColumnName(tmpRow, 'rowNum'),
            (xml.row[0].cell[1])  : getColumnName(tmpRow, 'typeCode'),
            (xml.row[0].cell[2])  : getColumnName(tmpRow, 'invoice'),
            (xml.row[0].cell[3])  : getColumnName(tmpRow, 'invoiceCorrecting'),
            (xml.row[0].cell[4])  : getColumnName(tmpRow, 'invoiceCorrection'),
            (xml.row[0].cell[5])  : getColumnName(tmpRow, 'invoiceCorrectingCorrection'),
            (xml.row[0].cell[6])  : getColumnName(tmpRow, 'documentPay'),
            (xml.row[0].cell[7])  : getColumnName(tmpRow, 'dateRegistration'),
            (xml.row[0].cell[8])  : getColumnName(tmpRow, 'salesman'),
            (xml.row[0].cell[9])  : getColumnName(tmpRow, 'salesmanInnKpp'),
            (xml.row[0].cell[10]) : 'Сведения о посреднике (комиссионере, агенте)',
            (xml.row[0].cell[12]) : getColumnName(tmpRow, 'declarationNum'),
            (xml.row[0].cell[13]) : getColumnName(tmpRow, 'currency'),
            (xml.row[0].cell[14]) : getColumnName(tmpRow, 'cost'),
            (xml.row[0].cell[15]) : getColumnName(tmpRow, 'nds'),

            (xml.row[1].cell[10]) : 'Наименование посредника',
            (xml.row[1].cell[11]) : 'ИНН/КПП посредника',
    ]
    (0..15).each { index ->
        headerMapping.put((xml.row[2].cell[index]), (index + 1).toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def dataRows = dataRowHelper.allCached
    def headRow = getDataRow(dataRows, 'head')
    def totalRow = getDataRow(dataRows, 'total')
    totalSumColumns.each {headRow[it] = null}

    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def xmlIndexRow = -1
    def int rowIndex = 1
    def rows = [headRow]
    boolean isHead = true

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

        // Итоговые строки
        if (row.cell[0].text() == null || row.cell[0].text() == "") {
            if (isHead) {
                headRow.nds = parseNumber(row.cell[15].text(), xlsIndexRow, 15 + colOffset, logger, false)
                isHead = false
            }
            if (row.cell[1].text() == null || row.cell[1].text() == "") {
                continue
            }
        }

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)

        // Графа 2
        def xmlIndexCol = 1
        newRow.typeCode = row.cell[xmlIndexCol].text()

        // Графа 3
        xmlIndexCol++
        newRow.invoice = row.cell[xmlIndexCol].text()

        // Графа 4
        xmlIndexCol++
        newRow.invoiceCorrecting = row.cell[xmlIndexCol].text()

        // Графа 5
        xmlIndexCol++
        newRow.invoiceCorrection = row.cell[xmlIndexCol].text()

        // Графа 6
        xmlIndexCol++
        newRow.invoiceCorrectingCorrection = row.cell[xmlIndexCol].text()

        // Графа 7
        xmlIndexCol++
        newRow.documentPay = row.cell[xmlIndexCol].text()

        // Графа 8
        xmlIndexCol++
        newRow.dateRegistration = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        // Графа 9
        xmlIndexCol++
        newRow.salesman = row.cell[xmlIndexCol].text()

        // Графа 10
        xmlIndexCol++
        newRow.salesmanInnKpp = row.cell[xmlIndexCol].text()

        // Графа 11
        xmlIndexCol++
        newRow.agentName = row.cell[xmlIndexCol].text()

        // Графа 12
        xmlIndexCol++
        newRow.agentInnKpp = row.cell[xmlIndexCol].text()

        // Графа 13
        xmlIndexCol++
        newRow.declarationNum = row.cell[xmlIndexCol].text()

        // Графа 14
        xmlIndexCol++
        newRow.currency = row.cell[xmlIndexCol].text()

        // Графа 15
        xmlIndexCol++
        newRow.cost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        // Графа 16
        xmlIndexCol++
        newRow.nds = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    calcTotalSum(rows, totalRow, totalSumColumns)
    rows.add(totalRow)
    dataRowHelper.save(rows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 16, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def int rnuIndexRow = 2
    def int colOffset = 1

    def dataRows = dataRowHelper.allCached
    def headRow = getDataRow(dataRows, 'head')
    totalSumColumns.each {headRow[it] = null}

    def rows = [headRow]
    def int rowIndex = 1

    def totalTmp = formData.createDataRow()
    totalSumColumns.each { alias ->
        totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
    }

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
        newRow.typeCode = row.cell[xmlIndexCol].text()

        // Графа 3
        xmlIndexCol++
        newRow.invoice = row.cell[xmlIndexCol].text()

        // Графа 4
        xmlIndexCol++
        newRow.invoiceCorrecting = row.cell[xmlIndexCol].text()

        // Графа 5
        xmlIndexCol++
        newRow.invoiceCorrection = row.cell[xmlIndexCol].text()

        // Графа 6
        xmlIndexCol++
        newRow.invoiceCorrectingCorrection = row.cell[xmlIndexCol].text()

        // Графа 7
        xmlIndexCol++
        newRow.documentPay = row.cell[xmlIndexCol].text()

        // Графа 8
        xmlIndexCol++
        newRow.dateRegistration = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, false)

        // Графа 9
        xmlIndexCol++
        newRow.salesman = row.cell[xmlIndexCol].text()

        // Графа 10
        xmlIndexCol++
        newRow.salesmanInnKpp = row.cell[xmlIndexCol].text()

        // Графа 11
        xmlIndexCol++
        newRow.agentName = row.cell[xmlIndexCol].text()

        // Графа 12
        xmlIndexCol++
        newRow.agentInnKpp = row.cell[xmlIndexCol].text()

        // Графа 13
        xmlIndexCol++
        newRow.declarationNum = row.cell[xmlIndexCol].text()

        // Графа 14
        xmlIndexCol++
        newRow.currency = row.cell[xmlIndexCol].text()

        // Графа 15
        xmlIndexCol++
        newRow.cost = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, false)

        // Графа 16
        xmlIndexCol++
        newRow.nds = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, false)

        totalSumColumns.each { alias ->
            def value1 = totalTmp.getCell(alias).value
            def value2 = (newRow.getCell(alias).value ?: BigDecimal.ZERO)
            totalTmp.getCell(alias).setValue(value1 + value2, null)
        }

        rows.add(newRow)
    }

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        def total = getTotalRow()

        // Графа 16
        total.nds = parseNumber(row.cell[16].text(), rnuIndexRow, 16 + colOffset, logger, false)

        def colIndexMap = ['nds' : 16]

        for (def alias : totalSumColumns) {
            def v1 = total.getCell(alias).value
            def v2 = totalTmp.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
            }
        }

        rows.add(total)
    }
    dataRowHelper.save(rows)
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    def columns = (isBalancePeriod() ? allColumns - 'rowNum' : editableColumns)
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
    total.fix = 'Всего'
    total.getCell('fix').colSpan = 16
    (allColumns + 'fix').each {
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def headRow = getDataRow(dataRows, 'head')
    def totalRow = getDataRow(dataRows, 'total')
    dataRows.remove(headRow)
    dataRows.remove(totalRow)

    sortRows(dataRows, sortColumns)

    dataRows.add(0, headRow)
    dataRows.add(totalRow)

    dataRowHelper.saveSort()
}

def loggerLog(def row, def msg, LogLevel logLevel = LogLevel.ERROR) {
    if (isBalancePeriod() || logLevel == LogLevel.WARNING) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def headRow = getDataRow(dataRows, 'head')
    def totalRow = getDataRow(dataRows, 'total')
    headRow.nds = BigDecimal.ZERO
    dataRows = []

    // собрать из источников строки
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, null)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                // получить все строки источника
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                // получить только нефиксированные строки
                def notFixedRows = sourceDataRows.findAll { row -> row.getAlias() == null || row.getAlias() == '' }
                // получить заголовок
                def sourceHeadRow = getDataRow(sourceDataRows, 'head')
                // просуммировать значения заголовков
                headRow.nds = headRow.nds + (sourceHeadRow.nds ?: BigDecimal.ZERO)
                dataRows.addAll(notFixedRows)
            }
        }
    }
    dataRows.add(0, headRow)
    dataRows.add(totalRow)

    dataRowHelper.save(dataRows)
}