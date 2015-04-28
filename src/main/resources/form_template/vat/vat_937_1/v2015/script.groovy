package form_template.vat.vat_937_1.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.NumericColumn
import com.aplana.sbrf.taxaccounting.model.StringColumn
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * (937.1 v2015) Итоговые данные книги покупок
 * formTemplate = 1606
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
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
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
def nonEmptyColumns = ['typeCode', 'invoice', 'cost', 'nds']

// Атрибуты итоговых строк для которых вычисляются суммы (графа )
@Field
def totalSumColumns = ['nds']

// Сортируемые атрибуты (графа 8, 3, 2, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16)
@Field
def sortColumns = ['dateRegistration', 'invoice', 'typeCode', 'invoiceCorrecting', 'invoiceCorrection',
        'invoiceCorrectingCorrection', 'documentPay', 'salesman', 'salesmanInnKpp', 'agentName',
        'agentInnKpp', 'declarationNum', 'currency', 'cost', 'nds']

@Field
def pattern1000DateImport = "^(\\S.{0,999}) ([0-2]\\d|3[01])(\\.|/)(0\\d|1[012])(\\.|/)(\\d{4})\$"

@Field
def pattern3DateImport = "^(\\d{1,3}) ([0-2]\\d|3[01])(\\.|/)(0\\d|1[012])(\\.|/)(\\d{4})\$"

@Field
def pattern256DateImport = "^(\\S.{0,255}) ([0-2]\\d|3[01])(\\.|/)(0\\d|1[012])(\\.|/)(\\d{4})\$"

@Field
def pattern1000Date = "^(\\S.{0,999})( ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4}))?\$"

@Field
def pattern3Date = "^(\\d{1,3}) ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$"

@Field
def pattern256Date = "^(\\S.{0,255}) ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$"

@Field
def replaceDatePattern = "\$1 \$2\\.\$4\\.\$6"

// Дата начала отчетного периода
@Field
def startDate = null

@Field
def calendarStartDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

// Признак периода ввода остатков
@Field
def isBalancePeriod

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = getDataRow(dataRows, 'total')

    calcTotalSum(dataRows, totalRow, totalSumColumns)

    dataRowHelper.update(totalRow)

    // Сортировка групп и строк
    sortFormDataRows()
}

void changeDateFormat(def row){
    // графа 3
    if (row.invoice && row.invoice.matches(pattern1000DateImport)) {
        row.invoice = row.invoice?.replaceFirst(pattern1000DateImport, replaceDatePattern)
    }
    // графа 4
    if (row.invoiceCorrecting && row.invoiceCorrecting.matches(pattern3DateImport)) {
        row.invoiceCorrecting = row.invoiceCorrecting?.replaceFirst(pattern3DateImport, replaceDatePattern)
    }
    // графа 5
    if (row.invoiceCorrection && row.invoiceCorrection.matches(pattern256DateImport)) {
        row.invoiceCorrection = row.invoiceCorrection?.replaceFirst(pattern256DateImport, replaceDatePattern)
    }
    // графа 6
    if (row.invoiceCorrectingCorrection && row.invoiceCorrectingCorrection.matches(pattern3DateImport)) {
        row.invoiceCorrectingCorrection = row.invoiceCorrectingCorrection?.replaceFirst(pattern3DateImport, replaceDatePattern)
    }
    // графа 7
    if (row.documentPay && row.documentPay.matches(pattern256DateImport)) {
        row.documentPay = row.documentPay?.replaceFirst(pattern256DateImport, replaceDatePattern)
    }
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
            loggerError(row, String.format(FILLED_FILLED_ERROR_MSG, index, getColumnName(row,'invoiceCorrectingCorrection'), getColumnName(row,'invoiceCorrection')))
        }
        //	Если «Графа 2» принимает хотя бы одно из значений диапазона: 01-05 | 07-13, то заполнена «Графа 10»
        if (row.typeCode && row.typeCode.matches("^[0-9]{2}\$") && Integer.valueOf(row.typeCode) in ((01..05) + (07..13)) && row.salesmanInnKpp == null){
            loggerError(row, String.format("Строка %s: В случае если графа «%s» принимает значение из диапазона: 01-05 | 07-13, должна быть заполнена графа «%s»!", index, getColumnName(row,'typeCode'), getColumnName(row,'salesmanInnKpp')))
        }
        // Проверки форматов
        // графа 3
        if (row.invoice && !row.invoice.matches(pattern1000Date)) {
            loggerError(row, String.format("Строка %s: Графа «%s» заполнена неверно! Ожидаемое значение: «%s». Только номер обязателен для заполнения.", index, getColumnName(row,'invoice'), "<Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 4
        if (row.invoiceCorrecting && !row.invoiceCorrecting.matches(pattern3Date)) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrecting'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 5
        if (row.invoiceCorrection && !row.invoiceCorrection.matches(pattern256Date)) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrection'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 6
        if (row.invoiceCorrectingCorrection && !row.invoiceCorrectingCorrection.matches(pattern3Date)) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrectingCorrection'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 7
        if (row.documentPay && !row.documentPay.matches(pattern256Date)) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'documentPay'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата» формат, «ДД.ММ.ГГГГ»>"))
        }
        // графа 10
        if (row.salesmanInnKpp && !row.salesmanInnKpp.matches("^(\\d{12}|\\d{10}/\\d{9})\$")) {
            loggerError(row, String.format(TWO_FMT_ERROR_MSG, index, getColumnName(row,'salesmanInnKpp'), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
        }
        // графа 12
        if (row.agentInnKpp && !row.agentInnKpp.matches("^(\\d{12}|\\d{10}/\\d{9})\$")) {
            loggerError(row, String.format(TWO_FMT_ERROR_MSG, index, getColumnName(row,'agentInnKpp'), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
        }
        // графа 14
        if (row.currency && !row.currency.matches("^\\S.{0,254} \\S{3}\$")) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'currency'), "<Наименование: тип поля «Строка/255/»> <Код: тип поля «Строка/3/», формат «ХХХ»>"))
        }
        // графа 2
        if (row.typeCode && (!row.typeCode.matches("^[0-9]{2}\$") || !(Integer.valueOf(row.typeCode) in ((1..13) + (16..28))))) {
            loggerError(row, String.format("Строка <Номер строки>: Графа «%s» заполнена неверно! Графа «%s» должна принимать значение из следующего диапазона: 01, 02, …,13, 16, 17, …, 28.", index, getColumnName(row,'typeCode'), getColumnName(row,'typeCode')))
        }
    }

    checkTotalSum(dataRows, totalSumColumns, logger, !isBalancePeriod())
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getCalendarStartDate() {
    if (!calendarStartDate) {
        calendarStartDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return calendarStartDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Консолидация с группировкой по подразделениям
void consolidation() {
    def dataRows = []

    // получить данные из источников
    def formSources = departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate())
    // сортируем по наименованию подразделения
    formSources.sort { departmentService.get(it.departmentId).name }
    for (departmentFormType in formSources) {
        def final child = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == departmentFormType.formTypeId) {
            def final childData = formDataService.getDataRowHelper(child)
            def final department = departmentService.get(child.departmentId)
            def headRow = getFixedRow(department.name, "head_${department.id}")
            dataRows.add(headRow)
            def final childDataRows = childData.all
            dataRows.addAll(childDataRows.findAll { it.getAlias() == null })
            def subTotalRow = getFixedRow("Всего по ${department.name}", "total_${department.id}")
            calcTotalSum(childDataRows, subTotalRow, totalSumColumns)
            dataRows.add(subTotalRow)
        }
    }

    def totalRow = getFixedRow('Всего','total')
    dataRows.add(totalRow)
    save(dataRows)
    dataRows = null
}

/** Получить произвольную фиксированную строку со стилями. */
def getFixedRow(String title, String alias) {
    def total = formData.createDataRow()
    total.setAlias(alias)
    total.fix = title
    total.getCell('fix').colSpan = 15
    (allColumns + 'fix').each {
        total.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return total
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
        newRow.dateRegistration = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)

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
        newRow.cost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 16
        xmlIndexCol++
        newRow.nds = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        changeDateFormat(newRow)
        rows.add(newRow)
    }
    rows.add(getFixedRow('Всего', 'total'))
    save(rows)
}

// TODO (Ramil Timerbaev) старая загрузка, потом удалить. Пока оставлено на случай сравнения производительности.
void importTransportDataOld() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 16, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
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
        newRow.dateRegistration = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)

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
        newRow.cost = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // Графа 16
        xmlIndexCol++
        newRow.nds = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

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

        def total = getFixedRow('Всего','total')

        // Графа 16
        total.nds = parseNumber(row.cell[16].text(), rnuIndexRow, 16 + colOffset, logger, true)

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
    save(rows)
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

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

def loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // не производим сортировку в консолидированных формах
    if (dataRows[0].getAlias() == null) {
        def totalRow = getDataRow(dataRows, 'total')
        dataRows.remove(totalRow)
        sortRows(dataRows, sortColumns)
        dataRows.add(totalRow)

        dataRowHelper.saveSort()
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

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 16
    int ROW_MAX = 1000
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 0    // номер строки в файле
    int rowIndex = 0        // номер строки в НФ
    def total = null        // итоговая строка со значениями из тф для добавления
    def newRows = []

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
            newRows.add(getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex))
        }
    }finally {
        reader.close()
    }

    // сравнение итогов
    if (total) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = ['nds' : 16]
        // подсчет итогов
        def totalRow = getFixedRow('Всего', 'total')
        calcTotalSum(newRows, totalRow, totalColumnsIndexMap.keySet().asList())
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = total.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR + " Из файла: $v1, рассчитано: $v2", totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
        // добавить итоговую строку
        newRows.add(totalRow)
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
    }

    // вставляем строки в БД
    //logger.error("Фиктивная ошибка, чтобы не было загрузки в БД") // отключил загрузку в БД
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        dataRowHelper.clear()

        def buffer = []
        def i = 0;
        newRows.each() {
            buffer.add(newRows[i++])
            if (buffer.size() == ROW_MAX) {
                dataRowHelper.insert(buffer, i - buffer.size() + 1)
                buffer = []
            }
        }
        if (buffer.size() > 0) {
            dataRowHelper.insert(buffer, i - buffer.size() + 1)
        }
    }
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

/** Добавляет строку в текущий буфер строк. */
boolean addRow(def dataRowsCut, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells == null) {
        return true
    }
    def newRow = getNewRow(rowCells, columnCount, fileRowIndex, rowIndex)
    if (newRow == null) {
        return false
    }
    dataRowsCut.add(newRow)
    return true
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
    def newRow = formData.createDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}", fileRowIndex))
        return newRow
    }

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def int colOffset = 1
    def int colIndex = 1

    // графа 2..7
    ['typeCode', 'invoice', 'invoiceCorrecting', 'invoiceCorrection', 'invoiceCorrectingCorrection', 'documentPay'].each { alias ->
        colIndex++
        def cell = pure(rowCells[colIndex])
        if (cell != null && cell != '') {
            if (checkString(newRow, alias, cell, fileRowIndex)) {
                newRow[alias] = cell
            }
        }
    }

    // графа 8
    colIndex++
    newRow.dateRegistration = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9..14
    ['salesman', 'salesmanInnKpp', 'agentName', 'agentInnKpp', 'declarationNum', 'currency'].each { alias ->
        colIndex++
        def cell = pure(rowCells[colIndex])
        if (cell != null && cell != '') {
            if (checkString(newRow, alias, cell, fileRowIndex)) {
                newRow[alias] = cell
            }
        }
    }

    // графа 15, 16
    ['cost', 'nds'].each { alias ->
        colIndex++
        def cell = pure(rowCells[colIndex])?.replaceAll(",", ".")
        if (cell != null && cell != '') {
            if (checkNumber(newRow, alias, cell, fileRowIndex)) {
                newRow[alias] = parseNumber(cell, fileRowIndex, colIndex + colOffset, logger, true)
            }
        }
    }
    return newRow
}

static String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}

boolean checkString(def tmpRow, def alias, def value, def fileRowIndex) {
    StringColumn column = tmpRow.getCell(alias).getColumn()
    if (column.getMaxLength() < value.size()) {
        logger.error("Строка $fileRowIndex, графа ${column.getOrder()}: Значение $value превышает допустимый размер " + column.getMaxLength())
        return false
    }
    return true
}

boolean checkNumber(def tmpRow, def alias, def value, def fileRowIndex) {
    NumericColumn column = tmpRow.getCell(alias).getColumn()
    def sepId = value.indexOf('.')
    def tmp = sepId == -1 ? value : value.substring(0, value.indexOf('.'))
    if (column.getMaxLength() - column.getPrecision() < tmp.size()) {
        logger.error("Строка $fileRowIndex, графа ${column.getOrder()}: Значение '$value' превышает допустимый размер до запятой " + (column.getMaxLength() - column.getPrecision()))
        return false
    }
    if (!value.matches("[0-9.,-]*")) {
        logger.error("Строка $fileRowIndex, графа ${column.getOrder()}: Значение '$value' содержит недопустимые символы")
        return false
    }
    return true
}