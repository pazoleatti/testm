package form_template.vat.vat_937_3.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.util.StringUtils

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
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
        }
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

@Field
def pattern1000DateImport = "^(\\S.{0,999}) ([0-2]\\d|3[01])(\\.|/)(0\\d|1[012])(\\.|/)(\\d{4})\$"

@Field
def pattern3DateImport = "^(\\d{1,3}) ([0-2]\\d|3[01])(\\.|/)(0\\d|1[012])(\\.|/)(\\d{4})\$"

@Field
def pattern256DateImport = "^(\\S.{0,255}) ([0-2]\\d|3[01])(\\.|/)(0\\d|1[012])(\\.|/)(\\d{4})\$"

@Field
def pattern1000Date = "^(\\S.{0,999}) ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$"

@Field
def pattern3Date = "^(\\d{1,3}) ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$"

@Field
def pattern256Date = "^(\\S.{0,255}) ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$"

@Field
def replaceDatePattern = "\$1 \$2\\.\$4\\.\$6"

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

    calc1AndChangeDateFormat(dataRows)
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
        if (row.invoiceNumDate != null && !row.invoiceNumDate.matches(pattern1000Date)) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'invoiceNumDate'), "<Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // 3.1 графа 12
        if (isFirstSection && row.mediatorNumDate != null && !row.mediatorNumDate.matches(pattern1000Date)) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'mediatorNumDate'), "<Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }

        // 3.2 графа 5
        if (row.invoiceCorrNumDate != null && !row.invoiceCorrNumDate.matches(pattern3Date)) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'invoiceCorrNumDate'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // 3.2 графа 7
        if (row.corrInvCorrNumDate != null && !row.corrInvCorrNumDate.matches(pattern3Date)) {
            loggerError(row, String.format(WRONG1_ERROR_MSG, index, getColumnName(row,'corrInvCorrNumDate'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }

        // 3.3 графа 6
        if (row.corrInvoiceNumDate != null && !row.corrInvoiceNumDate.matches(pattern256Date)) {
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
    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows

    def firstRow = getDataRow(templateRows, 'part_1')
    def lastRow = getDataRow(templateRows, 'part_2')

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

    def newRows = []
    // заполнение строк + расчет итогов
    for (def section : sections) {
        def row1 = getDataRow(templateRows, "part_$section")
        def row2 = getDataRow(templateRows, "total_$section")
        def rows = mapRows["part_$section"]

        newRows.add(row1)
        newRows.addAll(rows)
        newRows.add(row2)
    }

    showMessages(newRows, logger)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    // вставляем строки в БД
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.save(newRows)
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
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

    if (dataRows.find { it.getAlias() != null && it.getAlias().startsWith("head_") } == null) {
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
void calc1AndChangeDateFormat(def dataRows) {
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

        if (formDataEvent == FormDataEvent.IMPORT) {
            if (row.invoiceNumDate != null && row.invoiceNumDate.matches(pattern1000DateImport)) {
                row.invoiceNumDate = row.invoiceNumDate?.replaceFirst(pattern1000DateImport, replaceDatePattern)
            }

            if (isFirstSection && row.mediatorNumDate != null && row.mediatorNumDate.matches(pattern1000DateImport)) {
                row.mediatorNumDate = row.mediatorNumDate?.replaceFirst(pattern1000DateImport, replaceDatePattern)
            }

            if (row.invoiceCorrNumDate != null && row.invoiceCorrNumDate.matches(pattern3DateImport)) {
                row.invoiceCorrNumDate = row.invoiceCorrNumDate?.replaceFirst(pattern3DateImport, replaceDatePattern)
            }

            if (row.corrInvCorrNumDate != null && row.corrInvCorrNumDate.matches(pattern3DateImport)) {
                row.corrInvCorrNumDate = row.corrInvCorrNumDate.replaceFirst(pattern3DateImport, replaceDatePattern)
            }

            if (row.corrInvoiceNumDate != null && row.corrInvoiceNumDate.matches(pattern256DateImport)) {
                row.corrInvoiceNumDate = row.corrInvoiceNumDate.replaceFirst(pattern256DateImport, replaceDatePattern)
            }
        }
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

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 19
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 0    // номер строки в файле
    int rowIndex = 0        // номер строки в НФ
    def total = null		// итоговая строка со значениями из тф для добавления
    def mapRows = [:]

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    try {
        // пропускаем заголовок
        rowCells = reader.readNext()
        if (isEmptyCells(rowCells)) {
            logger.error('Первой строкой должен идти заголовок, а не пустая строка')
        }
        // пропускаем пустую строку
        rowCells = reader.readNext()
        if (!isEmptyCells(rowCells)) {
            logger.error('Вторая строка должна быть пустой')
        }
        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    total = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }
            def newRow = getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex)

            // определить раздел по техническому полю и добавить строку в нужный раздел
            sectionIndex = pure(rowCells[20])
            if (mapRows[sectionIndex] == null) {
                mapRows[sectionIndex] = []
            }
            mapRows[sectionIndex].add(newRow)
        }
    } finally {
        reader.close()
    }

    int rowCount = 0
    // сравнение итогов
    if (total) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = ['cost' : 14, 'vatSum' : 15, 'diffDec' : 16, 'diffInc' : 17, 'diffVatDec' : 18, 'diffVatInc' : 19]
        // итоговая строка для сверки сумм
        def totalTmp = formData.createStoreMessagingDataRow()
        totalColumnsIndexMap.keySet().asList().each { alias ->
            totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
        }

        // подсчет итогов
        mapRows.each { sectionIndex, dataRows ->
            rowCount += dataRows.size()
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
                logger.warn(TRANSPORT_FILE_SUM_ERROR + " Из файла: $v1, рассчитано: $v2", totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
    }

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows
    def newRows = []

    // заполнение строк + расчет итогов
    for (def section : sections) {
        def firstRow = getDataRow(templateRows, "part_$section")
        def lastRow = getDataRow(templateRows, "total_$section")

        // посчитать итоги по разделам
        def rows = mapRows[section]
        calcTotalSum(rows, lastRow, totalSumColumns)

        newRows.add(firstRow)
        newRows.addAll(rows)
        newRows.add(lastRow)
    }

    showMessages(newRows, logger)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    // вставляем строки в БД
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.save(newRows)
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
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
    def newRow = getNewRow()
    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}", fileRowIndex))
        return null
    }

    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    def int colOffset = 1
    def int colIndex = 0

    // графа 1
    colIndex++
    newRow.rowNumber = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 2
    colIndex++
    newRow.date = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 3..13
    ['opTypeCode', 'invoiceNumDate', 'invoiceCorrNumDate', 'corrInvoiceNumDate', 'corrInvCorrNumDate', 'buyerName',
            'buyerInnKpp', 'mediatorName', 'mediatorInnKpp', 'mediatorNumDate', 'currNameCode'].each { alias ->
        colIndex++
        def cell = pure(rowCells[colIndex])
        if (alias in ['buyerInnKpp', 'mediatorInnKpp']) {
            cell = cell.replaceAll("[^0-9/]",'')
        }
        newRow[alias] = cell
    }

    // графа 14..19
    ['cost', 'vatSum', 'diffDec', 'diffInc', 'diffVatDec', 'diffVatInc'].each { alias ->
        colIndex++
        def cell = pure(rowCells[colIndex])?.replaceAll(",", ".")
        newRow[alias] = parseNumber(cell, fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}