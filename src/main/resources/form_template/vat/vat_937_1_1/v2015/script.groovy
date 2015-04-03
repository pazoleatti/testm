package form_template.vat.vat_937_1_1.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
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

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // не производим сортировку в консолидированных формах
    if (dataRows[0].getAlias() == null) {
        def headRow = getDataRow(dataRows, 'head')
        def totalRow = getDataRow(dataRows, 'total')
        dataRows.remove(headRow)
        dataRows.remove(totalRow)

        sortRows(dataRows, sortColumns)

        dataRows.add(0, headRow)
        dataRows.add(totalRow)

        dataRowHelper.saveSort()
    }
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
    dataRows = [headRow]

    // собрать из источников строки
    def formSources = departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate())
    // сортируем по наименованию подразделения
    formSources.sort { departmentService.get(it.departmentId).name }
    formSources.each {
        if (it.formTypeId == formData.formType.id) {
            def final child = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, null)
            if (child != null && child.state == WorkflowState.ACCEPTED) {
                // получить все строки источника
                def final childDataRows = formDataService.getDataRowHelper(child).allCached
                def final department = departmentService.get(child.departmentId)
                def depHeadRow = getFixedRow(department.name, "head_${department.id}", true)
                dataRows.add(depHeadRow)
                def subHeadRow = getFixedRow("Итого по ${department.name}", "sub_head_${department.id}", false)
                // получить заголовок
                def sourceHeadRow = getDataRow(childDataRows, 'head')
                subHeadRow.nds = (sourceHeadRow.nds ?: BigDecimal.ZERO)
                dataRows.add(subHeadRow)
                // просуммировать значения заголовков
                headRow.nds = headRow.nds + (sourceHeadRow.nds ?: BigDecimal.ZERO)
                // добавить только нефиксированные строки
                dataRows.addAll(childDataRows.findAll { row -> row.getAlias() == null || row.getAlias() == '' })
                def subTotalRow = getFixedRow("Всего по ${department.name}", "total_${department.id}", true)
                calcTotalSum(childDataRows, subTotalRow, totalSumColumns)
                dataRows.add(subTotalRow)
            }
        }
    }
    dataRows.add(totalRow)

    dataRowHelper.save(dataRows)
}

/** Получить произвольную фиксированную строку со стилями. */
def getFixedRow(String title, String alias, boolean isTotal) {
    def total = formData.createDataRow()
    total.setAlias(alias)
    total.fix = title
    total.getCell('fix').colSpan = 16
    if (isTotal) {
        (allColumns + 'fix').each {
            total.getCell(it).setStyleAlias('Контрольные суммы')
        }
    } else {
        total.getCell('nds').setStyleAlias('Редактируемая')
        total.getCell('nds').editable = true
    }
    return total
}

void importTransportData() {
    int COLUMN_COUNT = 16
    int TOTAL_ROW_COUNT = 1
    int ROW_MAX = 1000
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\''

    checkBeforeGetXml(ImportInputStream, UploadFileName)

    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()

    String[] rowCells
    int countEmptyRow = 0	// количество пустых строк
    int fileRowIndex = 0    // номер строки в файле
    int rowIndex = 0        // номер строки в НФ
    int totalRowCount = 0   // счетчик кол-ва итогов
    def total = null		// итоговая строка со значениями из тф для добавления
    def newRows = []

    while ((rowCells = reader.readNext()) != null) {
        fileRowIndex++

        def isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
        if (isEmptyRow) {
            if (countEmptyRow > 0) {
                // если встретилась вторая пустая строка, то дальше только строки итогов и ЦП
                totalRowCount++
                // итоговая строка тф
                total = getNewRow(reader.readNext(), COLUMN_COUNT, ++fileRowIndex, ++rowIndex)
                break
            }
            countEmptyRow++
            continue
        }

        // если еще не было пустых строк, то это первая строка - заголовок (пропускается)
        // обычная строка
        if (countEmptyRow != 0 && !addRow(newRows, rowCells, COLUMN_COUNT, fileRowIndex, ++rowIndex)) {
            break
        }

        // периодически сбрасываем строки
        if (newRows.size() > ROW_MAX) {
            dataRowHelper.insert(newRows, dataRowHelper.allCached.size() + 1)
            newRows.clear()
        }
    }
    reader.close()

    // проверка итоговой строки
    if (TOTAL_ROW_COUNT != 0 && totalRowCount != TOTAL_ROW_COUNT) {
        logger.error(ROW_FILE_WRONG, fileRowIndex)
    }

    if (newRows.size() != 0) {
        dataRowHelper.insert(newRows, dataRowHelper.allCached.size() + 1)
    }

    // сравнение итогов
    if (total) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = [ 'nds' : 16 ]

        // подсчет итогов
        def dataRows = dataRowHelper.allCached
        def totalRow = getFixedRow('Всего', 'total', true)
        calcTotalSum(dataRows, totalRow, totalSumColumns)

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = total.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }

        // добавить в нф заголовок и итоговую строку
        def headRow = dataRows.find { it.getAlias() == 'head' }
        if (!headRow) {
            headRow = getFixedRow('Итого', 'head', false)
            dataRowHelper.insert(headRow, 1)
        }
        dataRowHelper.insert(totalRow, dataRowHelper.allCached.size() + 1)
    }
}

/** Добавляет строку в текущий буфер строк. */
boolean addRow(def rows, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells == null) {
        return true
    }
    def newRow = getNewRowOrHeadRow(rowCells, columnCount, fileRowIndex, rowIndex)
    if (newRow == null) {
        return false
    }
    rows.add(newRow)
    return true
}

/** Получить новую строку нф или заголовок по строке из тф (*.rnu). */
def getNewRowOrHeadRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowIndex == 1) {
        return getNewHeadRow(rowCells, columnCount, fileRowIndex, rowIndex)
    } else {
        return getNewRow(rowCells, columnCount, fileRowIndex, rowIndex)
    }
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
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    def int colOffset = 1
    def int colIndex = 1

    // графа 2..7
    ['typeCode', 'invoice', 'invoiceCorrecting', 'invoiceCorrection', 'invoiceCorrectingCorrection', 'documentPay'].each { alias ->
        colIndex++
        newRow[alias] = pure(rowCells[colIndex])
    }

    // графа 8
    colIndex++
    newRow.dateRegistration = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, false)

    // графа 9..14
    ['salesman', 'salesmanInnKpp', 'agentName', 'agentInnKpp', 'declarationNum', 'currency'].each { alias ->
        colIndex++
        newRow[alias] = pure(rowCells[colIndex])
    }

    // Графа 15
    colIndex++
    newRow.cost = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, false)

    // Графа 16
    colIndex++
    newRow.nds = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, false)

    return newRow
}

/**
 * Получить новую строку нф или заголовок по строке из тф (*.rnu).
 * Проверяет наличие надписи "Итого" (значит это заголовок) и формирует или
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewHeadRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells.length != columnCount + 2) {
        def tmpRow = formData.createDataRow()
        tmpRow.setIndex(rowIndex)
        tmpRow.setImportIndex(fileRowIndex)
        rowError(logger, tmpRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    def newRow
    if ('Итого' == pure(rowCells[1])) {
        newRow = getFixedRow('Итого', 'head', false)
        newRow.setIndex(rowIndex)
        newRow.setImportIndex(fileRowIndex)

        def int colOffset = 1

        // Графа 16
        def colIndex = 16
        newRow.nds = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, false)
    } else {
        newRow = getNewRow(rowCells, columnCount, fileRowIndex, rowIndex)
    }

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}