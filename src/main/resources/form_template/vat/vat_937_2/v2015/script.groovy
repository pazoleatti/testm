package form_template.vat.vat_937_2.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
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
        formDataService.saveCachedDataRows(formData, logger)
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
        consolidation()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            formDataService.saveCachedDataRows(formData, logger)
        }
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
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
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def totalRow = getDataRow(dataRows, 'total')
    calcTotalSum(dataRows, totalRow, totalSumColumns)

    sortFormDataRows(false)
}

void changeDateFormat(def row){
    // графа 3
    if (row.invoiceNumDate && row.invoiceNumDate.matches(pattern1000DateImport)) {
        row.invoiceNumDate = row.invoiceNumDate?.replaceFirst(pattern1000DateImport, replaceDatePattern)
    }
    // графа 4
    if (row.invoiceCorrNumDate && row.invoiceCorrNumDate.matches(pattern3DateImport)) {
        row.invoiceCorrNumDate = row.invoiceCorrNumDate?.replaceFirst(pattern3DateImport, replaceDatePattern)
    }
    // графа 5
    if (row.corrInvoiceNumDate && row.corrInvoiceNumDate.matches(pattern256DateImport)) {
        row.corrInvoiceNumDate = row.corrInvoiceNumDate?.replaceFirst(pattern256DateImport, replaceDatePattern)
    }
    // графа 6
    if (row.corrInvCorrNumDate && row.corrInvCorrNumDate.matches(pattern3DateImport)) {
        row.corrInvCorrNumDate = row.corrInvCorrNumDate?.replaceFirst(pattern3DateImport, replaceDatePattern)
    }
    // графа 11
    if (row.paymentDocNumDate && row.paymentDocNumDate.matches(pattern256DateImport)) {
        row.paymentDocNumDate = row.paymentDocNumDate?.replaceFirst(pattern256DateImport, replaceDatePattern)
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

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
        if (row.invoiceNumDate && !row.invoiceNumDate.matches(pattern1000Date)) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceNumDate'), "<Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 4
        if (row.invoiceCorrNumDate && !row.invoiceCorrNumDate.matches(pattern3Date)) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrNumDate'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 5
        if (row.corrInvoiceNumDate && !row.corrInvoiceNumDate.matches(pattern256Date)) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'corrInvoiceNumDate'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 6
        if (row.corrInvCorrNumDate && !row.corrInvCorrNumDate.matches(pattern3Date)) {
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
        if (row.paymentDocNumDate && !row.paymentDocNumDate.matches(pattern256Date)) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'paymentDocNumDate'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 12
        if (row.currNameCode && !row.currNameCode.matches("^\\S.{0,254} \\S{3}\$")) {
            loggerError(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'currNameCode'), "<Наименование: тип поля «Строка/255/»> <Код: тип поля «Строка/3/», формат «ХХХ»>"))
        }
        // графа 2
        if (row.opTypeCode && (!row.opTypeCode.matches("^[0-9]{2}\$") || !(Integer.valueOf(row.opTypeCode) in ((1..13) + (16..28))))) {
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

// Консолидация с группировкой по подразделениям
void consolidation() {
    def rows = []

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
            rows.add(headRow)
            def final childDataRows = childData.all
            rows.addAll(childDataRows.findAll { it.getAlias() == null })
            def subTotalRow = getFixedRow("Всего по ${department.name}", "total_${department.id}")
            calcTotalSum(childDataRows, subTotalRow, totalSumColumns)
            rows.add(subTotalRow)
        }
    }

    def totalRow = getFixedRow('Всего','total')
    rows.add(totalRow)

    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
}

/** Получить произвольную фиксированную строку со стилями. */
def getFixedRow(String title, String alias) {
    def total = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    total.setAlias(alias)
    total.fix = title
    total.getCell('fix').colSpan = 15
    (allColumns + 'fix').each {
        total.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return total
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = formData.createStoreMessagingDataRow()
    def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
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
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // не производим сортировку в консолидированных формах
    if (dataRows[0].getAlias() == null) {
        def totalRow = getDataRow(dataRows, 'total')
        dataRows.remove(totalRow)
        sortRows(dataRows, sortColumns)
        dataRows.add(totalRow)

        if (saveInDB) {
            dataRowHelper.saveSort()
        } else {
            updateIndexes(dataRows);
        }
    }
}

def loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 20
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2, ..)
    int rowIndex = 0        // номер строки в НФ (1, 2, ..)
    def totalTF = null        // итоговая строка со значениями из тф для добавления
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
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }
            newRows.add(getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex))
        }
    } finally {
        reader.close()
    }

    showMessages(newRows, logger)

    // сравнение итогов
    if (totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = ['saleCostB18' : 15, 'saleCostB10' : 16, 'saleCostB0' : 17, 'vatSum18' : 18, 'vatSum10' : 19, 'bonifSalesSum' : 20]
        // подсчет итогов
        def totalRow = getFixedRow('Всего', 'total')
        calcTotalSum(newRows, totalRow, totalColumnsIndexMap.keySet().asList())
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
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

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(newRows)
        formDataService.getDataRowHelper(formData).allCached = newRows
    }
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
    def newRow = formData.createStoreMessagingDataRow()
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

    // графа 2..12

    ['opTypeCode', 'invoiceNumDate', 'invoiceCorrNumDate', 'corrInvoiceNumDate', 'corrInvCorrNumDate', 'buyerName',
            'buyerInnKpp', 'mediatorName', 'mediatorInnKpp', 'paymentDocNumDate', 'currNameCode'].each { alias ->
        colIndex++
        def cell = pure(rowCells[colIndex])
        if (alias in ['buyerInnKpp', 'mediatorInnKpp']) {
            cell = cell.replaceAll("[^0-9/]",'')
        }
        if (cell != null && cell != '') {
            newRow[alias] = cell
        }
    }

    // графа 13а..19 (20)
    ['saleCostACurr', 'saleCostARub', 'saleCostB18', 'saleCostB10', 'saleCostB0', 'vatSum18', 'vatSum10', 'bonifSalesSum'].each { alias ->
        colIndex++
        def cell = pure(rowCells[colIndex])?.replaceAll(",", ".")
        if (cell != null && cell != '') {
            newRow[alias] = parseNumber(cell, fileRowIndex, colIndex + colOffset, logger, true)
        }
    }
    return newRow
}

static String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 20
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
    }
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
        // Пропуск итоговых строк
        if (!rowValues[INDEX_FOR_SKIP]) {
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    def totalRow = getFixedRow('Всего', 'total')
    calcTotalSum(rows, totalRow, totalSumColumns)
    rows.add(totalRow)

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
            (headerRows[0][0])  : getColumnName(tmpRow, 'rowNumber'),
            (headerRows[0][1])  : getColumnName(tmpRow, 'opTypeCode'),
            (headerRows[0][2])  : getColumnName(tmpRow, 'invoiceNumDate'),
            (headerRows[0][3])  : getColumnName(tmpRow, 'invoiceCorrNumDate'),
            (headerRows[0][4])  : getColumnName(tmpRow, 'corrInvoiceNumDate'),
            (headerRows[0][5])  : getColumnName(tmpRow, 'corrInvCorrNumDate'),
            (headerRows[0][6])  : getColumnName(tmpRow, 'buyerName'),
            (headerRows[0][7])  : getColumnName(tmpRow, 'buyerInnKpp'),

            (headerRows[0][8])  : 'Сведения о посреднике (комиссионере, агенте)',
            (headerRows[1][8])  : 'наименование посредника',
            (headerRows[1][9])  : 'ИНН/КПП посредника',

            (headerRows[0][10]) : getColumnName(tmpRow, 'paymentDocNumDate'),
            (headerRows[0][11]) : getColumnName(tmpRow, 'currNameCode'),

            (headerRows[0][12]) : 'Стоимость продаж по счету-фактуре, разница стоимости по корректировочному счету-фактуре (включая НДС) в валюте счета-фактуры',
            (headerRows[1][12]) : 'в валюте счета-фактуры',
            (headerRows[1][13]) : 'в рублях и копейках',

            (headerRows[0][14]) : 'Стоимость продаж, облагаемых налогом, по счету-фактуре, разница стоимости по корректировочному счету-фактуре (без НДС) в рублях и копейках, по ставке',
            (headerRows[1][14]) : '18 процентов',
            (headerRows[1][15]) : '10 процентов',
            (headerRows[1][16]) : '0 процентов',

            (headerRows[0][17]) : 'Сумма НДС по счету-фактуре, разница стоимости по корректировочному счету-фактуре в рублях и копейках, по ставке',
            (headerRows[1][17]) : '18 процентов',
            (headerRows[1][18]) : '10 процентов',

            (headerRows[0][19]) : getColumnName(tmpRow, 'bonifSalesSum'),
            (headerRows[2][12]) : '13а',
            (headerRows[2][13]) : '13б'
    ]
    (0..11).each { index ->
        headerMapping.put((headerRows[2][index]), (index + 1).toString())
    }
    (14..19).each { index ->
        headerMapping.put((headerRows[2][index]), index.toString())
    }
    checkHeaderEquals(headerMapping, logger)
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
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 2..14
    def colIndex = 0
    ['opTypeCode', 'invoiceNumDate', 'invoiceCorrNumDate', 'corrInvoiceNumDate', 'corrInvCorrNumDate', 'buyerName',
            'buyerInnKpp', 'mediatorName', 'mediatorInnKpp', 'paymentDocNumDate', 'currNameCode'].each { alias ->
        colIndex++
        if (alias in ['buyerInnKpp', 'mediatorInnKpp']) {
            newRow[alias] = values[colIndex].replaceAll(' ', '')
        } else {
            newRow[alias] = values[colIndex]
        }
    }

    // графа 13а..19
    ['saleCostACurr', 'saleCostARub', 'saleCostB18', 'saleCostB10', 'saleCostB0', 'vatSum18', 'vatSum10', 'bonifSalesSum'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    changeDateFormat(newRow)
    return newRow
}