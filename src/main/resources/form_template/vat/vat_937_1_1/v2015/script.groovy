package form_template.vat.vat_937_1.v2015

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType
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
def allColumns = ['rowNum', 'typeCode', 'invoice', 'invoiceCorrecting', 'invoiceCorrection', 'invoiceCorrectingCorrection', 'documentPay', 'dateRegistration',
                  'salesman', 'salesmanInnKpp', 'agentName', 'agentInnKpp', 'declarationNum', 'currency', 'cost', 'nds']

// Редактируемые атрибуты (графа )
@Field
def editableColumns = allColumns - 'rowNum'

// Проверяемые на пустые значения атрибуты (графа )
@Field
def nonEmptyColumns = ['rowNum', 'typeCode', 'invoice']

// Атрибуты итоговых строк для которых вычисляются суммы (графа )
@Field
def totalSumColumns = ['nds']

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
    def headRow = getDataRow(dataRows, 'head')

    def bookTotalRow = getBookTotalRow(true)
    if (bookTotalRow) {
        totalSumColumns.each { column ->
            // Графы 14-19 фиксированной строки «Итого» заполнены согласно алгоритму
            headRow[column] = bookTotalRow[column]
        }
    }

    calcTotalSum(dataRows, totalRow, totalSumColumns)

    dataRowHelper.save(dataRows)
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
        if (checkFormat(row.typeCode, "^[0-9]{2}\$") && Integer.valueOf(row.typeCode) in ((01..05) + (07..13)) && row.salesmanInnKpp == null){
            loggerLog(row, String.format("Строка %s: В случае если графа «%s» принимает значение из диапазона: 01-05 | 07-13, должна быть заполнена графа «%s»!", index, getColumnName(row,'typeCode'), getColumnName(row,'salesmanInnKpp')))
        }
        // Проверки форматов
        // графа 3
        if (row.invoice == null || !checkFormat(row.invoice.trim(), "^\\S.{0,999}( ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4}))?\$")) {
            loggerLog(row, String.format("Строка %s: Графа «%s» заполнена неверно! Ожидаемое значение: «%s». Только номер обязателен для заполнения.", index, getColumnName(row,'invoice'), "<Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 4
        if (row.invoiceCorrecting && !checkFormat(row.invoiceCorrecting, "^\\d{1,3} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrecting'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 5
        if (row.invoiceCorrection && !checkFormat(row.invoiceCorrection.trim(), "^\\S.{0,255} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrection'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 6
        if (row.invoiceCorrectingCorrection && !checkFormat(row.invoiceCorrectingCorrection.trim(), "^\\d{1,3} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrectingCorrection'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 7
        if (row.documentPay && !checkFormat(row.documentPay.trim(), "^\\S.{0,255} ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$")) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'documentPay'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата» формат, «ДД.ММ.ГГГГ»>"))
        }
        // графа 10
        if (row.salesmanInnKpp && !checkFormat(row.salesmanInnKpp, "^(\\d{12}|\\d{10}/\\d{9})\$")) {
            loggerLog(row, String.format(TWO_FMT_ERROR_MSG, index, getColumnName(row,'salesmanInnKpp'), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
        }
        // графа 12
        if (row.agentInnKpp && !checkFormat(row.agentInnKpp, "^(\\d{12}|\\d{10}/\\d{9})\$")) {
            loggerLog(row, String.format(TWO_FMT_ERROR_MSG, index, getColumnName(row,'agentInnKpp'), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
        }
        // графа 14
        if (row.currency && !checkFormat(row.currency.trim(), "^\\S.{0,254} \\S{3}\$")) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'currency'), "<Наименование: тип поля «Строка/255/»> <Код: тип поля «Строка/3/», формат «ХХХ»>"))
        }
        // графа 2
        if (row.typeCode == null || !checkFormat(row.typeCode, "^[0-9]{2}\$") || !(Integer.valueOf(row.typeCode) in ((1..13) + (16..28)))) {
            loggerLog(row, String.format("Строка <Номер строки>: Графа «%s» заполнена неверно! Графа «%s» должна принимать значение из следующего диапазона: 01, 02, …,13, 16, 17, …, 28.", index, getColumnName(row,'typeCode'), getColumnName(row,'typeCode')))
        }
    }

    def headRow = getDataRow(dataRows, 'head')
    def bookTotalRow = getBookTotalRow()
    if (bookTotalRow) {
        totalSumColumns.each { column ->
            if (bookTotalRow[column] != headRow[column]) {
                loggerLog(bookTotalRow, "Строка ${headRow.getIndex()}: Итоговые значения рассчитаны неверно в графе «${getColumnName(bookTotalRow, column)}»!", formDataEvent == FormDataEvent.CALCULATE ? LogLevel.WARNING : LogLevel.ERROR)
            }
        }
    }

    checkTotalSum(dataRows, totalSumColumns, logger, !isBalancePeriod())
}

// Получить строку итогов из Итоговой книги покупок
def getBookTotalRow(boolean skip = false) {
    int bookFormType = 606
    String bookFormTypeName = formTypeService.get(bookFormType).name
    List<DepartmentFormType> bookFormSources = departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).findAll { it.formTypeId == bookFormType }
    // 1. ищем назначение
    if (bookFormSources == null || bookFormSources.isEmpty()) {
        if (!skip) {
            loggerLog(null, "Не назначена источником налоговая форма «$bookFormTypeName» в текущем периоде. " +
                    "Расчеты строки «Итого» не могут быть выполнены!", formDataEvent == FormDataEvent.CALCULATE ? LogLevel.WARNING : LogLevel.ERROR)
        }
    } else {
        for (DepartmentFormType formDataSource in bookFormSources) {
            def bookFormData = formDataService.getLast(bookFormType, formData.kind, formData.departmentId, formData.reportPeriodId, formData.periodOrder)
            // 2. ищем форму в статусе принята
            if (bookFormData != null && bookFormData.state == WorkflowState.ACCEPTED) {
                def bookDataRows = formDataService.getDataRowHelper(bookFormData)?.allCached
                if (bookDataRows) {
                    return getDataRow(bookDataRows, 'total')
                }
            } else {
                if (!skip) {
                    loggerLog(null, "Не найден экземпляр налоговой формы-источника «$bookFormTypeName» текущего периода " +
                            "в статусе «Принята». Расчеты строки «Итого» не могут быть выполнены!", formDataEvent == FormDataEvent.CALCULATE ? LogLevel.WARNING : LogLevel.ERROR)
                }
            }
        }
    }
    return null
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

        rows.add(newRow)
    }
    calcTotalSum(rows, totalRow, totalSumColumns)
    rows.add(totalRow)
    dataRowHelper.save(rows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 16, 1)
    addTransportData(xml)

    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    checkTotalSum(dataRows, totalSumColumns, logger, false)
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

        rows.add(newRow)
    }

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        def total = getTotalRow()

        // Графа 16
        total.nds = parseNumber(row.cell[16].text(), rnuIndexRow, 16 + colOffset, logger, true)

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
    total.fix = 'Итого'
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
    sortRows(refBookService, logger, dataRows, null, getDataRow(dataRows, 'total'), true)
    dataRowHelper.saveSort()
}

def loggerLog(def row, def msg, LogLevel logLevel = LogLevel.ERROR) {
    if (isBalancePeriod() || logLevel == LogLevel.WARNING) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}