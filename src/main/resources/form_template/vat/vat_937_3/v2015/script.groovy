package form_template.vat.vat_937_3.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Итоговые данные из журнала полученных и выставленных счетов-фактур по посреднической деятельности.
 * formTemplateId=619
 */

// графа 1  - rowNumber
// графа    - fix
// графа 2  - date
// графа 3  - opTypeCode
// графа 4  - invoiceNumDate
// графа 5  - invoiceCorrNumDate
// графа 6  - corrInvoiceNumDate
// графа 7  - corrInvCorrNumDate
// графа 8  - buyerName
// графа 9  - buyerInnKpp
// графа 10 - mediatorName
// графа 11 - mediatorInnKpp
// графа 12 - mediatorNumDate
// графа 13 - currNameCode
// графа 14 - cost
// графа 15 - vatSum
// графа 16 - diffDec
// графа 17 - diffInc
// графа 18 - diffVatDec
// графа 19 - diffVatInc

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

@Field
def allColumns = ['fix', 'rowNumber', 'date', 'opTypeCode', 'invoiceNumDate', 'invoiceCorrNumDate', 'corrInvoiceNumDate', 'corrInvCorrNumDate', 'buyerName', 'buyerInnKpp', 'mediatorName', 'mediatorInnKpp', 'mediatorNumDate', 'currNameCode', 'cost', 'vatSum', 'diffDec', 'diffInc', 'diffVatDec', 'diffVatInc']

// Проверяемые на пустые значения атрибуты раздела 1 (графа 2..4, 6!, 10..12, 14, 15!, 16!, 17!, 18!, 19!)
@Field
def nonEmptyColumns1 = ['date', 'opTypeCode', 'invoiceNumDate', /*'corrInvoiceNumDate',*/ 'mediatorName', 'mediatorInnKpp', 'mediatorNumDate', 'cost', /*'vatSum', 'diffDec', 'diffInc', 'diffVatDec', 'diffVatInc'*/]

// Проверяемые на пустые значения атрибуты раздела 2 (графа 2..4, 6!, 12, 14, 15!, 16!, 17!, 18!, 19!)
@Field
def nonEmptyColumns2 = ['date', 'opTypeCode', 'invoiceNumDate', /*'corrInvoiceNumDate',*/ 'mediatorNumDate', 'cost', /*'vatSum', 'diffDec', 'diffInc', 'diffVatDec', 'diffVatInc'*/]

// Редактируемые атрибуты (графа 2..19)
@Field
def editableColumns = allColumns - ['fix', 'rowNumber']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 14..19)
@Field
def totalSumColumns = ['cost', 'vatSum', 'diffDec', 'diffInc', 'diffVatDec', 'diffVatInc']

// Алиасы заголовков разделов 1 и 2.
@Field
def sections = ['part_1', 'part_2']

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

void calc() {
    sortFormDataRows()
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def FILLED_FILLED_ERROR_MSG = "Строка %s: В случае если графа «%s» заполнена, должна быть заполнена графа «%s»!"
    def SOME_CONDITION_FILLED_ERROR_MSG = "Строка %s: В случае если графы «%s» и «%s» не заполнены, или заполнена графа «%s», должна быть заполнена графа «%s»!"
    def NOT_FILLED_2_FILLED_ERROR_MSG = "Строка %s: В случае если графы «%s» и «%s» не заполнены, должна быть заполнена графа «%s»!"

    def COLUMN_12_ERROR_MSG = "Строка %s: Графа «%s» заполнена неверно! Графа «%s» должна принимать значение из следующего диапазона: 1-4."

    def WRONG1_ERROR_MSG = "Строка %s: Графа «%s» заполнена неверно! Ожидаемое значение: «%s». Оба поля обязательны для заполнения."
    def WRONG2_ERROR_MSG = "Строка %s: Графа «%s» заполнена неверно! Ожидаемый формат: «%s»."

    def isFirstSection = true
    def codeValues = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28']

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            isFirstSection = row.getAlias() == sections[0]
            continue
        }
        def index = row.getIndex()

        // 1. Проверка заполнения граф
        def columns = (isFirstSection ? nonEmptyColumns1 : nonEmptyColumns2)
        checkNonEmptyColumns(row, index, columns, logger, !isBalancePeriod())


        // 2. Проверка на заполненность зависимых граф
        // 2.1 Если заполнена «Графа 7», то заполнена «Графа 6»
        if (row.corrInvCorrNumDate != null && row.corrInvoiceNumDate == null) {
            loggerError(row, String.format(FILLED_FILLED_ERROR_MSG, index, getColumnName(row, 'corrInvCorrNumDate'), getColumnName(row, 'corrInvoiceNumDate')))
        }
        // 2.2 Если заполнена «Графа 16», то заполнена «Графа 18»
        if (row.diffDec != null && row.diffVatDec == null){
            loggerError(row, String.format(FILLED_FILLED_ERROR_MSG, index, getColumnName(row, 'diffDec'), getColumnName(row, 'diffVatDec')))
        }
        // 2.3 Если заполнена «Графа 17», то заполнена «Графа 19»
        if (row.diffInc != null && row.diffVatInc == null) {
            loggerError(row, String.format(FILLED_FILLED_ERROR_MSG, index, getColumnName(row, 'diffInc'), getColumnName(row, 'diffVatInc')))
        }
        // 2.4 Если не заполнены «Графа 6» и «Графа 7», то заполнена «Графа 15»
        if (row.corrInvoiceNumDate == null && row.corrInvCorrNumDate == null && row.vatSum == null) {
            loggerError(row, String.format(NOT_FILLED_2_FILLED_ERROR_MSG, index, getColumnName(row, 'corrInvoiceNumDate'),
                    getColumnName(row, 'corrInvCorrNumDate'), getColumnName(row, 'vatSum')))
        }
        // 2.5 Если не заполнены «Графа 6» и «Графа 7» или заполнена «Графа 17», то заполнена «Графа 16»
        if (((row.corrInvoiceNumDate == null && row.corrInvCorrNumDate == null) || row.diffInc != null) && row.diffDec == null) {
            loggerError(row, String.format(SOME_CONDITION_FILLED_ERROR_MSG, index, getColumnName(row, 'corrInvoiceNumDate'),
                    getColumnName(row, 'corrInvCorrNumDate'), getColumnName(row, 'diffInc'), getColumnName(row, 'diffDec')))
        }
        // 2.6 Если не заполнены «Графа 6» и «Графа 7» или заполнена «Графа 16», то заполнена «Графа 17»
        if (((row.corrInvoiceNumDate == null && row.corrInvCorrNumDate == null) || row.diffDec != null) && row.diffInc == null) {
            loggerError(row, String.format(SOME_CONDITION_FILLED_ERROR_MSG, index, getColumnName(row, 'corrInvoiceNumDate'),
                    getColumnName(row, 'corrInvCorrNumDate'), getColumnName(row, 'diffDec'), getColumnName(row, 'diffInc')))
        }

        // 3. Проверка формата заполнения
        // 3.1 графа 4
        if (row.invoiceNumDate != null && !checkFormat(row.invoiceNumDate.trim(), "^\\S.{0,999} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'invoiceNumDate'), "<Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // 3.1 графа 12
        if (isFirstSection && row.mediatorNumDate != null && !checkFormat(row.mediatorNumDate.trim(), "^\\S.{0,999} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'mediatorNumDate'), "<Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }

        // 3.2 графа 5
        if (row.invoiceCorrNumDate != null && !checkFormat(row.invoiceCorrNumDate.trim(), "^\\d{1,3} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'invoiceCorrNumDate'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // 3.2 графа 7
        if (row.corrInvCorrNumDate != null && !checkFormat(row.corrInvCorrNumDate.trim(), "^\\d{1,3} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'corrInvCorrNumDate'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }

        // 3.3 графа 6
        if (row.corrInvoiceNumDate != null && !checkFormat(row.corrInvoiceNumDate.trim(), "^\\S.{0,255} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'corrInvoiceNumDate'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }

        // 3.4 графа 13
        if (row.currNameCode != null && !checkFormat(row.currNameCode.trim(), "^\\S.{0,254} \\S{3}\$")) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'currNameCode'), "<Наименование: тип поля «Строка/255/»> <Код: тип поля «Строка/3/», формат «ХХХ»>"))
        }

        // 3.5 графа 9
        if (row.buyerInnKpp != null && !checkFormat(row.buyerInnKpp, "^(\\d{12}|\\d{10}/\\d{9})\$")) {
            loggerError(row, String.format(WRONG2_ERROR_MSG, index, getColumnName(row,'buyerInnKpp'), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
        }
        // 3.6 графа 11
        if (row.mediatorInnKpp != null && !checkFormat(row.mediatorInnKpp, "^(\\d{12}|\\d{10}/\\d{9})\$")) {
            loggerError(row, String.format(WRONG2_ERROR_MSG, index, getColumnName(row,'mediatorInnKpp'), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
        }


        // 4. Проверка значения графы «Код вида операции»
        if (!(row.opTypeCode in codeValues)) {
            def name = getColumnName(row, 'opTypeCode')
            loggerError(row, "Строка $index: Графа «$name» заполнена неверно! Графа $name должна принимать значение из следующего диапазона: 01, 02, …, 13, 16, 17, …, 28.")
        }

        // 5. Проверка значения «Графы 12» (часть 2)
        if (!isFirstSection && row.mediatorNumDate != null && !checkFormat(row.mediatorNumDate.trim(), "^\\[1234]\$")) {
            def name = getColumnName(row,'mediatorNumDate')
            loggerError(row, String.format(COLUMN_12_ERROR_MSG, index, name, name))
        }
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNumber'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 20, 3)

    def headerMapping = [
            (xml.row[0].cell[0])  : getColumnName(tmpRow, 'rowNumber'),
            // вторая ячейка скрытая
            (xml.row[0].cell[2])  : getColumnName(tmpRow, 'date'),
            (xml.row[0].cell[3])  : getColumnName(tmpRow, 'opTypeCode'),
            (xml.row[0].cell[4])  : getColumnName(tmpRow, 'invoiceNumDate'),
            (xml.row[0].cell[5])  : getColumnName(tmpRow, 'invoiceCorrNumDate'),
            (xml.row[0].cell[6])  : getColumnName(tmpRow, 'corrInvoiceNumDate'),
            (xml.row[0].cell[7])  : getColumnName(tmpRow, 'corrInvCorrNumDate'),
            (xml.row[0].cell[8])  : getColumnName(tmpRow, 'buyerName'),
            (xml.row[0].cell[9])  : getColumnName(tmpRow, 'buyerInnKpp'),

            (xml.row[0].cell[10]) : 'Сведения о посреднической деятельности, указываемые комиссионером (агентом), застройщиком или техническим заказчиком',
            (xml.row[1].cell[10]) : 'наименование продавца (из графы 8 части 2)/ субкомиссионера (субагента)',
            (xml.row[1].cell[11]) : 'ИНН/КПП продавца (из графы 9 части 2)/ субкомиссионера (субагента)',
            (xml.row[1].cell[12]) : 'номер и дата счета-фактуры, полученного от продавца (из графы 4 части 2)/ код вида сделки',

            (xml.row[0].cell[13]) : getColumnName(tmpRow, 'currNameCode'),
            (xml.row[0].cell[14]) : getColumnName(tmpRow, 'cost'),
            (xml.row[0].cell[15]) : getColumnName(tmpRow, 'vatSum'),

            (xml.row[0].cell[16]) : 'Разница стоимости с учетом НДС по корректировочному счету-фактуре',
            (xml.row[1].cell[16]) : 'уменьшение',
            (xml.row[1].cell[17]) : 'увеличение',

            (xml.row[0].cell[18]) : 'Разница НДС по корректировочному счету-фактуре',
            (xml.row[1].cell[18]) : 'уменьшение',
            (xml.row[1].cell[19]) : 'увеличение',

            (xml.row[2].cell[0]) : '1',
    ]
    (2..19).each { index ->
        headerMapping.put(xml.row[2].cell[index], index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def firstRow = getDataRow(dataRows, sections[0])
    def lastRow = getDataRow(dataRows, sections[1])

    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def xmlIndexRow = -1
    def int rowIndex = 1

    def sectionIndex = null
    def mapRows = [:]

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

        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        def firstValue = row.cell[1].text()

        if (firstValue != null && firstValue != '' && (firstValue == firstRow.fix || firstValue == lastRow.fix)) {
            def isFirstSection = (firstValue == firstRow.fix)
            sectionIndex = (isFirstSection ? firstRow : lastRow).getAlias()
            mapRows.put(sectionIndex, [])
            continue
        }

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)

        // Графа 2
        def xmlIndexCol = 2
        newRow.date = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 3
        xmlIndexCol++
        newRow.opTypeCode = row.cell[xmlIndexCol].text() ?: null

        // Графа 4
        xmlIndexCol++
        newRow.invoiceNumDate = row.cell[xmlIndexCol].text() ?: null

        // Графа 5
        xmlIndexCol++
        newRow.invoiceCorrNumDate = row.cell[xmlIndexCol].text() ?: null

        // Графа 6
        xmlIndexCol++
        newRow.corrInvoiceNumDate = row.cell[xmlIndexCol].text() ?: null

        // Графа 7
        xmlIndexCol++
        newRow.corrInvCorrNumDate = row.cell[xmlIndexCol].text() ?: null

        // Графа 8
        xmlIndexCol++
        newRow.buyerName = row.cell[xmlIndexCol].text() ?: null

        // Графа 9
        xmlIndexCol++
        newRow.buyerInnKpp = row.cell[xmlIndexCol].text() ?: null

        // Графа 10
        xmlIndexCol++
        newRow.mediatorName = row.cell[xmlIndexCol].text() ?: null

        // Графа 11
        xmlIndexCol++
        newRow.mediatorInnKpp = row.cell[xmlIndexCol].text() ?: null

        // Графа 12
        xmlIndexCol++
        newRow.mediatorNumDate = row.cell[xmlIndexCol].text() ?: null

        // Графа 13
        xmlIndexCol++
        newRow.currNameCode = row.cell[xmlIndexCol].text() ?: null

        // Графа 14
        xmlIndexCol++
        newRow.cost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 15
        xmlIndexCol++
        newRow.vatSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 16
        xmlIndexCol++
        newRow.diffDec = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 17
        xmlIndexCol++
        newRow.diffInc = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 18
        xmlIndexCol++
        newRow.diffVatDec = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 19
        xmlIndexCol++
        newRow.diffVatInc = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        mapRows[sectionIndex].add(newRow)
    }

    deleteNotFixedRows(dataRows)

    // копирование данных по разделам
    sections.each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, section).getIndex()
            dataRows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }

    dataRowHelper.save(dataRows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 19, 0)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached

    // мапа для проверки итоговых значений
    def tmpSums = [:]
    totalSumColumns.each {
        tmpSums[it] = BigDecimal.ZERO
    }

    def int rnuIndexRow = 2
    def int colOffset = 1
    def int rowIndex = 1
    def mapRows = [:]
    def sectionIndex

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
        newRow.date = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 3
        xmlIndexCol++
        newRow.opTypeCode = row.cell[xmlIndexCol].text() ?: null

        // Графа 4
        xmlIndexCol++
        newRow.invoiceNumDate = row.cell[xmlIndexCol].text() ?: null

        // Графа 5
        xmlIndexCol++
        newRow.invoiceCorrNumDate = row.cell[xmlIndexCol].text() ?: null

        // Графа 6
        xmlIndexCol++
        newRow.corrInvoiceNumDate = row.cell[xmlIndexCol].text() ?: null

        // Графа 7
        xmlIndexCol++
        newRow.corrInvCorrNumDate = row.cell[xmlIndexCol].text() ?: null

        // Графа 8
        xmlIndexCol++
        newRow.buyerName = row.cell[xmlIndexCol].text() ?: null

        // Графа 9
        xmlIndexCol++
        newRow.buyerInnKpp = row.cell[xmlIndexCol].text() ?: null

        // Графа 10
        xmlIndexCol++
        newRow.mediatorName = row.cell[xmlIndexCol].text() ?: null

        // Графа 11
        xmlIndexCol++
        newRow.mediatorInnKpp = row.cell[xmlIndexCol].text() ?: null

        // Графа 12
        xmlIndexCol++
        newRow.mediatorNumDate = row.cell[xmlIndexCol].text() ?: null

        // Графа 13
        xmlIndexCol++
        newRow.currNameCode = row.cell[xmlIndexCol].text() ?: null

        // Графа 14
        xmlIndexCol++
        newRow.cost = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 15
        xmlIndexCol++
        newRow.vatSum = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 16
        xmlIndexCol++
        newRow.diffDec = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 17
        xmlIndexCol++
        newRow.diffInc = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 18
        xmlIndexCol++
        newRow.diffVatDec = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 19
        xmlIndexCol++
        newRow.diffVatInc = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Техническое поле(группа)
        xmlIndexCol++
        sectionIndex = 'part_' + row.cell[xmlIndexCol].text()

        // подсчет итоговых значений
        totalSumColumns.each {
            tmpSums[it] += roundValue((newRow[it] ?: BigDecimal.ZERO), 2)
        }

        if (mapRows[sectionIndex] == null) {
            mapRows[sectionIndex] = []
        }
        mapRows[sectionIndex].add(newRow)
    }

    deleteNotFixedRows(dataRows)

    // копирование данных по разделам
    sections.each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, section).getIndex()
            dataRows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // Графа 14
        def xmlIndexCol = 14
        total.cost = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 15
        xmlIndexCol++
        total.vatSum = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 16
        xmlIndexCol++
        total.diffDec = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 17
        xmlIndexCol++
        total.diffInc = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 18
        xmlIndexCol++
        total.diffVatDec = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 19
        xmlIndexCol++
        total.diffVatInc = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        totalSumColumns.each {
            def v1 = total[it]
            def v2 = tmpSums[it]
            if ((v1 == null && v2 != null) || v1 != null && v1 != v2) {
                // определить колонку в тф: 14 - это номер графы с которой начинаются подсчеты сумм
                def col = 14 + totalSumColumns.indexOf(it) + colOffset
                logger.warn(TRANSPORT_FILE_SUM_ERROR, col, rnuIndexRow)
            }
        }
    }
    dataRowHelper.save(dataRows)
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
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

    def firstRow = getDataRow(dataRows, sections[0])
    def lastRow = getDataRow(dataRows, sections[1])
    def columnNameList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }

    // раздел 1
    def from = firstRow.getIndex()
    def to = lastRow.getIndex() - 1
    def section1Rows = (from < to ? dataRows[from..(to - 1)] : [])
    // Массовое разыменовывание граф НФ
    refBookService.dataRowsDereference(logger, section1Rows, columnNameList)
    sortRowsSimple(section1Rows)

    // раздел 2
    from = lastRow.getIndex()
    to = dataRows.size()
    def section2Rows = (from < to ? dataRows[from..(to - 1)] : [])
    // Массовое разыменовывание граф НФ
    refBookService.dataRowsDereference(logger, section2Rows, columnNameList)
    sortRowsSimple(section2Rows)

    dataRowHelper.saveSort()
}

def loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, sections[1]).getIndex()
    } else {
        index = currentDataRow.getIndex() + 1
    }
    dataRowHelper.insert(getNewRow(), index)
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить нефиксированные строки
    deleteNotFixedRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, null)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached

                // копирование данных по разделам
                copyRows(sourceDataRows, dataRows, sections[0], sections[1])
                copyRows(sourceDataRows, dataRows, sections[1], null)
            }
        }
    }
    dataRowHelper.save(dataRows)
}

// Удалить нефиксированные строки
void deleteNotFixedRows(def dataRows) {
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        updateIndexes(dataRows)
    }
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows строки приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно)
 */
void copyRows(def sourceDataRows, def destinationDataRows, def fromAlias, def toAlias) {
    def from = getDataRow(sourceDataRows, fromAlias).getIndex()
    def to = (toAlias ? getDataRow(sourceDataRows, toAlias).getIndex() - 1 : sourceDataRows.size())
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getDataRow(destinationDataRows, fromAlias).getIndex(), copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationDataRows)
}

// Поправить индексы.
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}