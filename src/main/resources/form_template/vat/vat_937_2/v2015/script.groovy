package form_template.vat.vat_937_2.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Итоговые данные книги продаж
 * formTemplateId=1608
 */

// fix
// графа 1 -  rowNumber
// графа 2 -  opTypeCode ограничение 01-28
// графа 3 -  invoiceNumDate ограничение <Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>
// графа 4 -  invoiceCorrNumDate <Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>
// графа 5 -  corrInvoiceNumDate <Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>
// графа 6 -  corrInvCorrNumDate <Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>
// графа 7 -  buyerName
// графа 8 -  buyerInnKpp ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)
// графа 9 -  mediatorName
// графа 10 - mediatorInnKpp ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)
// графа 11 - paymentDocNumDate <Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>
// графа 12 - currNameCode <Наименование: тип поля «Строка/255/»> <Код: тип поля «Строка/3/», формат «ХХХ»>
// графа 13а - saleCostACurr
// графа 13б - saleCostARub
// графа 14 - saleCostB18
// графа 15 - saleCostB10
// графа 16 - saleCostB0
// графа 17 - vatSum18
// графа 18 - vatSum10
// графа 19 - bonifSalesSum

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
        formDataService.consolidationTotal(formData, logger, ['total'])
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
def nonEmptyColumns = ['opTypeCode', 'invoiceNumDate']

// Редактируемые атрибуты (графа )
@Field
def editableColumns = allColumns - ['rowNumber']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Атрибуты итоговых строк для которых вычисляются суммы (графа )
@Field
def totalSumColumns = ['saleCostB18', 'saleCostB10', 'saleCostB0', 'vatSum18', 'vatSum10', 'bonifSalesSum']

// Сортируемые атрибуты (графа 3, 2, 4..19)
@Field
def sortColumns = ['invoiceNumDate', 'opTypeCode', 'invoiceCorrNumDate', 'corrInvoiceNumDate', 'corrInvCorrNumDate',
        'buyerName', 'buyerInnKpp', 'mediatorName', 'mediatorInnKpp', 'paymentDocNumDate', 'currNameCode',
        'saleCostACurr', 'saleCostARub', 'saleCostB18', 'saleCostB10', 'saleCostB0', 'vatSum18', 'vatSum10', 'bonifSalesSum']

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

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = getDataRow(dataRows, 'total')

    calcTotalSum(dataRows, totalRow, totalSumColumns)

    dataRowHelper.update(totalRow)

    // Сортировка групп и строк
    sortFormDataRows()
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def FILLED_FILLED_ERROR_MSG = "Строка %s: В случае если графа «%s» заполнена, должна быть заполнена графа «%s»!"
    def NOT_FILLED_FILLED_ERROR_MSG = "Строка %s: В случае если графа «%s» не заполнена, должна быть заполнена графа «%s»!"
    def FILLED_CURRENCY_ERROR_MSG = "Строка %s: В случае если графа «%s» заполнена и код валюты графы «%s» не равен «643», должна быть заполнена графа «%s»!"
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
        if (row.corrInvCorrNumDate != null && row.corrInvoiceNumDate == null){
            loggerError(row, String.format(FILLED_FILLED_ERROR_MSG, index, getColumnName(row,'corrInvCorrNumDate'), getColumnName(row,'corrInvoiceNumDate')))
        }
        //	Если заполнена «Графа 12» и код валюты «Графы 12» заполнен и не равен «643», то заполнена «Графа 13а»
        if (row.currNameCode == null || row.currNameCode.matches("^\\S.{0,254} \\S{3}\$")) {
            String currency = getLastTextPart(row.currNameCode, "(\\S.{0,254}) ")
            if (currency != null && !'643'.equals(currency) && row.saleCostACurr == null){
                loggerError(row, String.format(FILLED_CURRENCY_ERROR_MSG, index, getColumnName(row,'currNameCode'), getColumnName(row,'currNameCode'), getColumnName(row,'saleCostACurr')))
            }
        }
        //	Если не заполнена «Графа 13б», то заполнена «Графа 19»
        if (row.saleCostARub == null && row.bonifSalesSum == null){
            loggerError(row, String.format(NOT_FILLED_FILLED_ERROR_MSG, index, getColumnName(row,'saleCostARub'), getColumnName(row,'bonifSalesSum')))
        }
        //	Если не заполнена «Графа 19», то заполнена «Графа 13б»
        if (row.bonifSalesSum == null && row.saleCostARub == null){
            loggerError(row, String.format(NOT_FILLED_FILLED_ERROR_MSG, index, getColumnName(row,'bonifSalesSum'), getColumnName(row,'saleCostARub')))
        }
        //	Если заполнена «Графа 14», то заполнена «Графа 17»
        if (row.saleCostB18 && row.vatSum18 == null){
            loggerError(row, String.format(FILLED_FILLED_ERROR_MSG, index, getColumnName(row,'saleCostB18'), getColumnName(row,'vatSum18')))
        }
        //	Если заполнена «Графа 15», то «Графа 18» заполнена
        if (row.saleCostB10 && row.vatSum10 == null){
            loggerError(row, String.format(FILLED_FILLED_ERROR_MSG, index, getColumnName(row,'saleCostB10'), getColumnName(row,'vatSum10')))
        }
        // Проверки форматов
        // графа 3
        if (row.invoiceNumDate && !row.invoiceNumDate.matches("^\\S.{0,999} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceNumDate'), "<Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 4
        if (row.invoiceCorrNumDate && !row.invoiceCorrNumDate.matches("^\\d{1,3} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrNumDate'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 5
        if (row.corrInvoiceNumDate && !row.corrInvoiceNumDate.matches("^\\S.{0,255} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'corrInvoiceNumDate'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 6
        if (row.corrInvCorrNumDate && !row.corrInvCorrNumDate.matches("^\\d{1,3} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'corrInvCorrNumDate'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 8
        if (row.buyerInnKpp && !row.buyerInnKpp.matches("^(\\d{12}|\\d{10}/\\d{9})\$")) {
            loggerError(row, String.format(TWO_FMT_ERROR_MSG, index, getColumnName(row,'buyerInnKpp'), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
        }
        // графа 10
        if (row.mediatorInnKpp && !row.mediatorInnKpp.matches("^(\\d{12}|\\d{10}/\\d{9})\$")) {
            loggerError(row, String.format(TWO_FMT_ERROR_MSG, index, getColumnName(row,'mediatorInnKpp'), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
        }
        // графа 11
        if (row.paymentDocNumDate && !row.paymentDocNumDate.matches("^\\S.{0,255} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'paymentDocNumDate'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 12
        if (row.currNameCode && !row.currNameCode.matches("^\\S.{0,254} \\S{3}\$")) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'currNameCode'), "<Наименование: тип поля «Строка/255/»> <Код: тип поля «Строка/3/», формат «ХХХ»>"))
        }
        // графа 2
        if (row.opTypeCode && (!row.opTypeCode.matches("^[0-9]{2}\$") || !(Integer.valueOf(row.opTypeCode) in 1..28))) {
            loggerError(row, String.format("Строка %s: Графа «%s» заполнена неверно! Графа «%s» должна принимать значение из следующего диапазона: 01, 02, …,13, 16, 17, …, 28.", index, getColumnName(row,'opTypeCode'), getColumnName(row,'opTypeCode')))
        }
    }

    checkTotalSum(dataRows, totalSumColumns, logger, !isBalancePeriod())
}

// получить кусок текста
String getLastTextPart(String value, def pattern) {
    def parts = value?.split(pattern)
    return parts?.length == 2 ? parts[1] : null
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

            (xml.row[0].cell[12]) : 'Стоимость продаж по счету-фактуре, разница стоимости по корректировочному счету-фактуре (включая НДС) в валюте счета-фактуры',
            (xml.row[1].cell[12]) : 'в валюте счета-фактуры',
            (xml.row[1].cell[13]) : 'в рублях и копейках',

            (xml.row[0].cell[14]) : 'Стоимость продаж, облагаемых налогом, по счету-фактуре, разница стоимости по корректировочному счету-фактуре (без НДС) в рублях и копейках, по ставке',
            (xml.row[1].cell[14]) : '18 процентов',
            (xml.row[1].cell[15]) : '10 процентов',
            (xml.row[1].cell[16]) : '0 процентов',

            (xml.row[0].cell[17]) : 'Сумма НДС по счету-фактуре, разница стоимости по корректировочному счету-фактуре в рублях и копейках, по ставке',
            (xml.row[1].cell[17]) : '18 процентов',
            (xml.row[1].cell[18]) : '10 процентов',

            (xml.row[0].cell[19]) : getColumnName(tmpRow, 'bonifSalesSum'),
            (xml.row[2].cell[12]) : '13а',
            (xml.row[2].cell[13]) : '13б'
    ]
    (0..11).each { index ->
        headerMapping.put((xml.row[2].cell[index]), (index + 1).toString())
    }
    (14..19).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def dataRows = dataRowHelper.allCached
    def totalRow = getDataRow(dataRows, 'total')

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
        if ((row.cell[0].text() == null || row.cell[0].text() == "") &&
                (row.cell[1].text() == null || row.cell[1].text() == "")) {
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

        // Графа 13а
        xmlIndexCol++
        newRow.saleCostACurr = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 13б(14)
        xmlIndexCol++
        newRow.saleCostARub = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 14(15)
        xmlIndexCol++
        newRow.saleCostB18 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 15(16)
        xmlIndexCol++
        newRow.saleCostB10 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 16(17)
        xmlIndexCol++
        newRow.saleCostB0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 17(18)
        xmlIndexCol++
        newRow.vatSum18 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 18(19)
        xmlIndexCol++
        newRow.vatSum10 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 19(20-я)
        xmlIndexCol++
        newRow.bonifSalesSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    calcTotalSum(rows, totalRow, totalSumColumns)
    rows.add(totalRow)
    save(rows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 20, 0)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def int rnuIndexRow = 2
    def int colOffset = 1

    def rows = []
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

        def colIndexMap = ['saleCostB18' : 15, 'saleCostB10' : 16, 'saleCostB0' : 17, 'vatSum18' : 18, 'vatSum10' : 19, 'bonifSalesSum' : 20]

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
    save(rows)
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
    total.fix = 'Всего'
    total.getCell('fix').colSpan = 15
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

    def totalRow = getDataRow(dataRows, 'total')
    dataRows.remove(totalRow)
    sortRows(dataRows, sortColumns)
    dataRows.add(totalRow)

    dataRowHelper.saveSort()
}

def loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

void save(def dataRows) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    // запись
    dataRowHelper.clear()
    def rows = []
    dataRows.each { row ->
        rows.add(row)
        if (rows.size() > 1000) {
            dataRowHelper.insert(rows, dataRowHelper.allCached.size() + 1)
            rows.clear()
        }
    }
    if (rows.size() > 0) {
        dataRowHelper.insert(rows, dataRowHelper.allCached.size() + 1)
        rows.clear()
    }
}