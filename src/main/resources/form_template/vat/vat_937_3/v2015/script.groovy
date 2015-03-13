package form_template.vat.vat_937_3.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

import java.text.SimpleDateFormat
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

// Проверяемые на пустые значения атрибуты раздела 1 (графа 2..4, 6!, 10, 12, 15!, 16!, 17!, 18!, 19!)
@Field
def nonEmptyColumns1 = ['date', 'opTypeCode', 'invoiceNumDate', /*'corrInvoiceNumDate',*/ 'mediatorName', 'mediatorNumDate', /*'vatSum', 'diffDec', 'diffInc', 'diffVatDec', 'diffVatInc'*/]

// Проверяемые на пустые значения атрибуты раздела 2 (графа 2..4, 6!, 12, 14, 15!, 16!, 17!, 18!, 19!)
@Field
def nonEmptyColumns2 = ['date', 'opTypeCode', 'invoiceNumDate', /*'corrInvoiceNumDate',*/ 'mediatorNumDate', 'cost', /*'vatSum', 'diffDec', 'diffInc', 'diffVatDec', 'diffVatInc'*/]

// Редактируемые атрибуты (графа 2..19)
@Field
def editableColumns = allColumns - ['fix', 'rowNumber']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 14..19)
@Field
def totalSumColumns = ['cost', 'vatSum', 'diffDec', 'diffInc', 'diffVatDec', 'diffVatInc']

// список алиасов подразделов
@Field
def sections = ['1', '2']

// Признак периода ввода остатков
@Field
def isBalancePeriod

@Field
def startDate = null

@Field
def endDate = null

// данные предыдущего преиода
@Field
def prevDataRows = null

// признак корректирующего периода
@Field
def isCorrectionPeriodMap = [:]

@Field
def reportPeriod = null

@Field
def formTypeName = null

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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    calc1(dataRows)
    calcTotal(dataRows)

    dataRowHelper.save(dataRows)

    sortFormDataRows()
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def MSG_1_2_3 = "Строка %s: В случае если графа «%s» заполнена, должна быть заполнена графа «%s»!"
    def MSG_4_5 = "Строка %s: В случае если (одновременно заполнены графы «%s» и «%s») и/или (не заполнена графа «%s»), должна быть заполнена графа «%s»!"
    def MSG_6 = "Строка %s: Только для части 1. В случае если графа «%s» не заполнена, должны быть заполнены графы «%s» и «%s»!"
    def MSG_7 = "Строка %s: Только для части 1. В случае если графа «%s» заполнена, должны быть не заполнены графы «%s» и «%s»!"
    def MSG_8 = "Строка %s: Только для части 2. В случае если графы «%s» и «%s» не заполнены, должна быть заполнена графа «%s»!"

    def COLUMN_12_ERROR_MSG = "Строка %s: Графа «%s» заполнена неверно! Графа «%s» должна принимать значение из следующего диапазона: 1-4."

    def WRONG1_ERROR_MSG = "Строка %s: Графа «%s» заполнена неверно! Ожидаемое значение: «%s». Оба поля обязательны для заполнения."
    def WRONG2_ERROR_MSG = "Строка %s: Графа «%s» заполнена неверно! Ожидаемый формат: «%s»."

    def isFirstSection = true
    // 01, 02, …, 13, 16, 17, …, 28
    def codeValues = ((1..13) + (16..28))

    def index1 = getPrevLastIndex(true)
    def index2 = getPrevLastIndex(false)

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            if (row.getAlias() == 'part_1') {
                isFirstSection = true
            }
            if (row.getAlias() == 'part_2') {
                isFirstSection = false
            }
            continue
        }
        def index = row.getIndex()

        // 1. Проверка заполнения граф
        def columns = (isFirstSection ? nonEmptyColumns1 : nonEmptyColumns2)
        checkNonEmptyColumns(row, index, columns, logger, !isBalancePeriod())


        // 2. Проверка на заполненность зависимых граф
        // 2.1 Если заполнена «Графа 7», то заполнена «Графа 6»
        if (row.corrInvCorrNumDate != null && row.corrInvoiceNumDate == null) {
            loggerError(row, String.format(MSG_1_2_3, index, getColumnName(row, 'corrInvCorrNumDate'), getColumnName(row, 'corrInvoiceNumDate')))
        }
        // 2.2 Если заполнена «Графа 16», то заполнена «Графа 18»
        if (row.diffDec != null && row.diffVatDec == null){
            loggerError(row, String.format(MSG_1_2_3, index, getColumnName(row, 'diffDec'), getColumnName(row, 'diffVatDec')))
        }
        // 2.3 Если заполнена «Графа 17», то заполнена «Графа 19»
        if (row.diffInc != null && row.diffVatInc == null) {
            loggerError(row, String.format(MSG_1_2_3, index, getColumnName(row, 'diffInc'), getColumnName(row, 'diffVatInc')))
        }
        // 2.4 Если (одновременно заполнены «Графа 6» и «Графа 7») и/или (не заполнена «Графа 17»), то заполнена «Графа 16»
        if (((row.corrInvoiceNumDate != null && row.corrInvCorrNumDate != null) || row.diffInc == null) && row.diffDec == null) {
            loggerError(row, String.format(MSG_4_5, index, getColumnName(row, 'corrInvoiceNumDate'),
                    getColumnName(row, 'corrInvCorrNumDate'), getColumnName(row, 'diffInc'), getColumnName(row, 'diffDec')))
        }
        // 2.5 Если (одновременно заполнены «Графа 6» и «Графа 7») и/ или (не заполнена «Графа 16»), то заполнена «Графа 17»
        if (((row.corrInvoiceNumDate != null && row.corrInvCorrNumDate != null) || row.diffDec == null) && row.diffInc == null) {
            loggerError(row, String.format(MSG_4_5, index, getColumnName(row, 'corrInvoiceNumDate'),
                    getColumnName(row, 'corrInvCorrNumDate'), getColumnName(row, 'diffDec'), getColumnName(row, 'diffInc')))
        }
        // 2.6 Часть 1: Если не заполнена «Графа 6», то заполнена «Графа 14» и «Графа 15»
        if (isFirstSection && row.corrInvoiceNumDate == null && (row.cost == null || row.vatSum == null)) {
            loggerError(row, String.format(MSG_6, index, getColumnName(row, 'corrInvoiceNumDate'), getColumnName(row, 'cost'), getColumnName(row, 'vatSum')))
        }
        // 2.7 Часть 1: Если «Графа 6» заполнена, то «Графа 14» и «Графа 15» не заполнены
        if (isFirstSection && row.corrInvoiceNumDate != null && (row.cost != null || row.vatSum != null)) {
            loggerError(row, String.format(MSG_7, index, getColumnName(row, 'corrInvoiceNumDate'), getColumnName(row, 'cost'), getColumnName(row, 'vatSum')))
        }
        // 2.8 Часть 2: Если не заполнены «Графа 6» и «Графа 7», то заполнена «Графа 15»
        if (!isFirstSection && row.corrInvoiceNumDate == null && row.corrInvCorrNumDate == null && row.vatSum == null) {
            loggerError(row, String.format(MSG_8, index, getColumnName(row, 'corrInvoiceNumDate'), getColumnName(row, 'corrInvCorrNumDate'), getColumnName(row, 'vatSum')))
        }


        // 3. Проверка формата заполнения
        // 3.1 графа 4
        if (row.invoiceNumDate != null && !row.invoiceNumDate.matches("^\\S.{0,999} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'invoiceNumDate'), "<Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // 3.1 графа 12
        if (isFirstSection && row.mediatorNumDate != null && !row.mediatorNumDate.matches("^\\S.{0,999} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'mediatorNumDate'), "<Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }

        // 3.2 графа 5
        if (row.invoiceCorrNumDate != null && !row.invoiceCorrNumDate.matches("^\\d{1,3} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'invoiceCorrNumDate'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // 3.2 графа 7
        if (row.corrInvCorrNumDate != null && !row.corrInvCorrNumDate.matches("^\\d{1,3} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'corrInvCorrNumDate'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }

        // 3.3 графа 6
        if (row.corrInvoiceNumDate != null && !row.corrInvoiceNumDate.matches("^\\S.{0,255} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'corrInvoiceNumDate'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }

        // 3.4 графа 13
        if (row.currNameCode != null && !row.currNameCode.matches("^\\S.{0,254} \\S{3}\$")) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'currNameCode'), "<Наименование: тип поля «Строка/255/»> <Код: тип поля «Строка/3/», формат «ХХХ»>"))
        }

        // 3.5 графа 9
        if (row.buyerInnKpp != null && !row.buyerInnKpp.matches("^(\\d{12}|\\d{10}/\\d{9})\$")) {
            loggerError(row, String.format(WRONG2_ERROR_MSG, index, getColumnName(row,'buyerInnKpp'), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
        }
        // 3.6 графа 11
        if (row.mediatorInnKpp != null && !row.mediatorInnKpp.matches("^(\\d{12}|\\d{10}/\\d{9})\$")) {
            loggerError(row, String.format(WRONG2_ERROR_MSG, index, getColumnName(row,'mediatorInnKpp'), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
        }


        // 4. Проверка значения графы «Код вида операции»
        if (row.opTypeCode && (!row.opTypeCode.matches("^[0-9]{2}\$") || !(Integer.valueOf(row.opTypeCode) in codeValues))) {
            def name = getColumnName(row, 'opTypeCode')
            loggerError(row, "Строка $index: Графа «$name» заполнена неверно! Графа $name должна принимать значение из следующего диапазона: 01, 02, …, 13, 16, 17, …, 28.")
        }

        // 5. Проверка значения «Графы 12» (часть 2)
        if (!isFirstSection && row.mediatorNumDate != null && !row.mediatorNumDate.matches("^[1234]\$")) {
            def name = getColumnName(row,'mediatorNumDate')
            loggerError(row, String.format(COLUMN_12_ERROR_MSG, index, name, name))
        }

        // 6. Проверка значения «Графы 1»
        if (row.rowNumber != (isFirstSection ? ++index1 : ++index2)) {
            def name = getColumnName(row, 'rowNumber')
            def formTypeName = getFormTypeName()
            def periodName = getReportPeriod().name
            def year = getReportPeriod().taxPeriod.year
            loggerError(row, "Строка $index: Графа «$name» заполнена неверно (в первичной налоговой форме «$formTypeName» " +
                    "текущего подразделения за $periodName $year изменено количество строк). " +
                    "Для обновления значения графы необходимо нажать на «Рассчитать».")
        }
    }

    // 7. Проверка итоговых значений (графа 14..19)
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, 'part_' + section)
        def lastRow = getDataRow(dataRows, 'total_' + section)
        for (def alias : totalSumColumns) {
            def value = roundValue(lastRow.getCell(alias).value ?: 0)
            def sum = roundValue(getSum(dataRows, alias, firstRow, lastRow))
            if (sum != value) {
                def index = lastRow.getIndex()
                def name = getColumnName(lastRow, alias)
                loggerError(lastRow, "Строка $index: Итоговые значения рассчитаны неверно в графе «$name»!")
            }
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

    def firstRow = getDataRow(dataRows, 'part_1')
    def lastRow = getDataRow(dataRows, 'part_2')

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
        } else if (firstValue == 'Всего') {
            continue
        }

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)

        // Графа 1
        def xmlIndexCol = 0
        newRow.rowNumber = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 2
        xmlIndexCol = 2
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

    deleteExtraRows(dataRows)

    // копирование данных по разделам
    sections.each { section ->
        def alias = 'part_' + section
        def copyRows = mapRows[alias]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, alias).getIndex()
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

        // Графа 1
        def xmlIndexCol = 1
        newRow.rowNumber = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 2
        xmlIndexCol = 2
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
            tmpSums[it] += roundValue(newRow[it] ?: BigDecimal.ZERO)
        }

        if (mapRows[sectionIndex] == null) {
            mapRows[sectionIndex] = []
        }
        mapRows[sectionIndex].add(newRow)
    }

    deleteExtraRows(dataRows)
    dataRows.each { row ->
        if (row.getAlias()?.contains('total')) {
            totalSumColumns.each {
                row[it] = null
            }
        }
    }

    // копирование данных по разделам
    sections.each { section ->
        def alias = 'part_' + section
        def copyRows = mapRows[alias]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, alias).getIndex()
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

    if (dataRows[1].getAlias() in [null, 'total_1']) {
        for (def section : sections) {
            def firstRow = getDataRow(dataRows, 'part_' + section)
            def lastRow = getDataRow(dataRows, 'total_' + section)
            def from = firstRow.getIndex()
            def to = lastRow.getIndex() - 1
            def sectionRows = (from < to ? dataRows[from..(to - 1)] : [])

            // Массовое разыменовывание граф НФ
            def columnNameList = firstRow.keySet().collect{firstRow.getCell(it).getColumn()}
            refBookService.dataRowsDereference(logger, sectionRows, columnNameList)

            sortRowsSimple(sectionRows)
        }
        dataRowHelper.saveSort()
    }
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
        index = getDataRow(dataRows, 'total_2').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex() + 1
    } else {
        def alias = currentDataRow.getAlias()
        if (alias.contains('total')) {
            index = getDataRow(dataRows, alias).getIndex()
        } else {
            def tmp = 'total_' + alias[alias.size() - 1]
            index = getDataRow(dataRows, tmp).getIndex()
        }
    }
    dataRowHelper.insert(getNewRow(), index)
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить нефиксированные строки
    deleteExtraRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    def formSources = departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate())
    // сортируем по наименованию подразделения
    formSources.sort { departmentService.get(it.departmentId).name }
    formSources.each {
        if (it.formTypeId == formData.formType.id) {
            def child = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, null)
            if (child != null && child.state == WorkflowState.ACCEPTED) {
                def childDataRows = formDataService.getDataRowHelper(child).allCached
                def final department = departmentService.get(child.departmentId)
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(childDataRows, dataRows, 'part_' + section, 'total_' + section, department)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
}

// Удалить нефиксированные строки
void deleteExtraRows(def dataRows) {
    def deleteRows = []
    dataRows.each { row ->
        if (!(row.getAlias() in ['part_1', 'total_1', 'part_2', 'total_2'])) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        updateIndexes(dataRows)
    }
}

/** Получить произвольную фиксированную строку со стилями. */
def getFixedRow(String title, String alias) {
    def total = formData.createDataRow()
    total.setAlias(alias)
    total.fix = title
    total.getCell('fix').colSpan = 13
    (allColumns + 'fix').each {
        total.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return total
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows строки приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 * @param department подразделение источника
 */
void copyRows(def sourceDataRows, def destinationDataRows, def fromAlias, def toAlias, def department) {
    def from = getDataRow(sourceDataRows, fromAlias).getIndex()
    def to = getDataRow(sourceDataRows, toAlias).getIndex() - 1
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)

    def headRow = getFixedRow(department.name, "head_${department.id}")
    destinationDataRows.add(getDataRow(destinationDataRows, toAlias).getIndex() - 1, headRow)
    updateIndexes(destinationDataRows)

    destinationDataRows.addAll(getDataRow(destinationDataRows, toAlias).getIndex() - 1, copyRows)
    updateIndexes(destinationDataRows)

    def subTotalRow = getFixedRow("Всего по ${department.name}", "sub_total_${department.id}")
    calcTotalSum(copyRows, subTotalRow, totalSumColumns)
    destinationDataRows.add(getDataRow(destinationDataRows, toAlias).getIndex() - 1, subTotalRow)
    updateIndexes(destinationDataRows)
}

// Поправить индексы.
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

def roundValue(def value, int precision = 2) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

void calcTotal(def dataRows) {
    // посчитать итоги по разделам
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, 'part_' + section)
        def lastRow = getDataRow(dataRows, 'total_' + section)
        totalSumColumns.each { alias ->
            def sum = roundValue(getSum(dataRows, alias, firstRow, lastRow))
            lastRow.getCell(alias).setValue(sum, null)
        }
    }
}

// Получить сумму столбца.
def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return dataRows[from..to].findAll { it.getAlias() == null }.sum { it[columnAlias] ?: 0 } ?: 0
}

/** Рассчитать нумерацию строк. Для каждой части нф нумерация начинается с 1. */
void calc1(def dataRows) {
    def index1 = getPrevLastIndex(true)
    def index2 = getPrevLastIndex(false)
    def isFirstSection = null
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            if (row.getAlias() == 'part_1') {
                isFirstSection = true
            }
            if (row.getAlias() == 'part_2') {
                isFirstSection = false
            }
            continue
        }

        // графа 1
        row.rowNumber = (isFirstSection ? ++index1 : ++index2)
    }
}

/** Получить последний номер строки из формы предыдушего периода из указаной части. */
def getPrevLastIndex(def isFirstPart) {
    def prevDataRows = getPrevDataRows()
    // если предыдущих данных нет или в предыдущей форме только фиксированные строки, то 0
    if (!prevDataRows || prevDataRows.size() == sections.size() * 2) {
        return 0
    }
    def lastRow
    def totalAlias = (isFirstPart ? 'total_1' : 'total_2')
    // находим строку итоги и по ней получаем последнюю строку части
    def tmpRow = getDataRow(prevDataRows, totalAlias)
    lastRow = prevDataRows.get(tmpRow.getIndex() - 2)

    return roundValue(lastRow.getAlias() == null ? lastRow.rowNumber : 0)
}

/** Получить строки предыдущего периода не в статусе "создана". */
def getPrevDataRows() {
    if (getReportPeriod()?.order == 1 || isCorrectionPeriod(formData.departmentReportPeriodId)) {
        return null
    }
    if (prevDataRows == null) {
        // получить предыдущие периоды текущего года
        SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
        def start = format.parse("01.01." + getReportPeriodStartDate().format('yyyy'))
        def end = getReportPeriodStartDate() - 1
        def reportPeriods = reportPeriodService.getReportPeriodsByDate(formData.formType.taxType, start, end)?.reverse()

        // поиск формы предыдущего периода в статусе отличной от "создана"
        for (def report : reportPeriods) {
            def formDataTmp = formDataService.getLast(formData.formType.id, formData.kind,  formDataDepartment.id, report.id, null)
            // форма подходил если: она существует, она не в состоянии "создана" и не в корректирующем периоде
            if (formDataTmp != null && !isCorrectionPeriod(formDataTmp.departmentReportPeriodId)
                    && formDataTmp.state != WorkflowState.CREATED) {
                prevDataRows = formDataService.getDataRowHelper(formDataTmp)?.getAllSaved()
                break
            }
        }
    }
    return prevDataRows
}

def isCorrectionPeriod(def departmentReportPeriodId) {
    if (isCorrectionPeriodMap[departmentReportPeriodId] == null) {
        def correctionDate = departmentReportPeriodService.get(departmentReportPeriodId).correctionDate
        isCorrectionPeriodMap[departmentReportPeriodId] = (correctionDate ? true : false)
    }
    return isCorrectionPeriodMap[departmentReportPeriodId]
}

def getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return reportPeriod
}

def getFormTypeName() {
    if (formTypeName == null) {
        formTypeName = formTypeService.get(formData.formType.id).name
    }
    return formTypeName
}