package form_template.vat.vat_937_1_1.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
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
        formDataService.saveCachedDataRows(formData, logger)
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
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
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
def totalColumns = ['nds']

// Сортируемые атрибуты (графа 8, 3, 2, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16)
@Field
def sortColumns = ['dateRegistration', 'invoice', 'typeCode', 'invoiceCorrecting', 'invoiceCorrection',
                   'invoiceCorrectingCorrection', 'documentPay', 'salesman', 'salesmanInnKpp', 'agentName',
                   'agentInnKpp', 'declarationNum', 'currency', 'cost', 'nds']

@Field
def pattern1000Date = "^\\S.{0,999}( ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4}))?\$"

@Field
def pattern3Date = "^(\\d{1,3}) ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$"

@Field
def pattern256Date = "^(\\S.{0,255}) ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$"

@Field
def pattern1000DateStrict = "^(\\S.{0,999}) ([0-2]\\d|3[01])\\.(0\\d|1[012])\\.(\\d{4})\$"

@Field
def selectDatePattern = /$2\.$3\.$4/

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

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def totalRow = getDataRow(dataRows, 'total')
    calcTotalSum(dataRows, totalRow, totalColumns)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def FILLED_FILLED_ERROR_MSG = "Строка %s: В случае если графа «%s» заполнена, должна быть заполнена графа «%s»!"
    def ONE_FMT_ERROR_MSG = "Строка %s: Графа «%s» заполнена неверно! Ожидаемый формат: «%s». Оба поля обязательны для заполнения."
    def TWO_FMT_ERROR_MSG = "Строка %s: Графа «%s» заполнена неверно! Ожидаемый формат: «%s»."
    boolean wasError = false

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
        if (row.invoice && !row.invoice.matches(pattern1000Date)) {
            loggerLog(row, String.format("Строка %s: Графа «%s» заполнена неверно! Ожидаемое значение: «%s». Только номер обязателен для заполнения.", index, getColumnName(row,'invoice'), "<Номер: тип поля «Строка/1000/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 4
        if (row.invoiceCorrecting && !row.invoiceCorrecting.matches(pattern3Date)) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrecting'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 5
        if (row.invoiceCorrection && !row.invoiceCorrection.matches(pattern256Date)) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrection'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 6
        if (row.invoiceCorrectingCorrection && !row.invoiceCorrectingCorrection.matches(pattern3Date)) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'invoiceCorrectingCorrection'), "<Номер: тип поля «Число/3/»> <Дата: тип поля «Дата», формат «ДД.ММ.ГГГГ»>"))
        }
        // графа 7
        if (row.documentPay && !row.documentPay.matches(pattern256Date)) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'documentPay'), "<Номер: тип поля «Строка/256/»> <Дата: тип поля «Дата» формат, «ДД.ММ.ГГГГ»>"))
        }
        // графа 14
        if (row.currency && !row.currency.matches("^\\S.{0,254} \\S{3}\$")) {
            loggerLog(row, String.format(ONE_FMT_ERROR_MSG, index, getColumnName(row,'currency'), "<Наименование: тип поля «Строка/255/»> <Код: тип поля «Строка/3/», формат «ХХХ»>"))
        }
        // графа 2
        if (row.typeCode && (!row.typeCode.matches("^[0-9]{2}\$") || !(Integer.valueOf(row.typeCode) in ((1..13) + (16..28))))) {
            loggerLog(row, String.format("Строка %s: Графа «%s» заполнена неверно! Графа «%s» должна принимать значение из следующего диапазона: 01, 02, …,13, 16, 17, …, 28.", index, getColumnName(row,'typeCode'), getColumnName(row,'typeCode')))
        }
        def innKppPatterns = [/([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}\/([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})/, /([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{10}/]
        ['salesmanInnKpp', 'agentInnKpp'].each { alias ->
            if (row[alias] && !row[alias].matches(/^(\S{12}|\S{10}\/\S{9})$/)) {
                loggerLog(row, String.format(TWO_FMT_ERROR_MSG, index, getColumnName(row, alias), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
            } else if (checkPattern(logger, row, alias, row[alias], innKppPatterns, null, !isBalancePeriod())) {
                checkControlSumInn(logger, row, alias, row[alias].split("/")[0], !isBalancePeriod())
            } else if (row[alias]) {
                if (!wasError) {
                    loggerLog(row, String.format("Строка %s: Расшифровка паттерна «%s»: %s.", index, INN_JUR_PATTERN, INN_JUR_MEANING))
                    loggerLog(row, String.format("Строка %s: Расшифровка паттерна «%s»: %s.", index, KPP_PATTERN, KPP_MEANING))
                    loggerLog(row, String.format("Строка %s: Расшифровка паттерна «%s»: %s.", index, INN_IND_PATTERN, INN_IND_MEANING))
                }
                wasError = true
            }
        }
        // Проверки формата дат (графы 3-8)
        // графа 3
        if (row.invoice && row.invoice.matches(pattern1000DateStrict)) {
            checkDateValid(logger, row, 'invoice', row.invoice?.replaceFirst(pattern1000DateStrict, selectDatePattern), !isBalancePeriod())
        }
        // графа 4
        if (row.invoiceCorrecting && row.invoiceCorrecting.matches(pattern3Date)) {
            checkDateValid(logger, row, 'invoiceCorrecting', row.invoiceCorrecting?.replaceFirst(pattern3Date, selectDatePattern), !isBalancePeriod())
        }
        // графа 5
        if (row.invoiceCorrection && row.invoiceCorrection.matches(pattern256Date)) {
            checkDateValid(logger, row, 'invoiceCorrection', row.invoiceCorrection?.replaceFirst(pattern256Date, selectDatePattern), !isBalancePeriod())
        }
        // графа 6
        if (row.invoiceCorrectingCorrection && row.invoiceCorrectingCorrection.matches(pattern3Date)) {
            checkDateValid(logger, row, 'invoiceCorrectingCorrection', row.invoiceCorrectingCorrection?.replaceFirst(pattern3Date, selectDatePattern), !isBalancePeriod())
        }
        // графа 7
        if (row.documentPay && row.documentPay.matches(pattern256Date)) {
            checkDateValid(logger, row, 'documentPay', row.documentPay?.replaceFirst(pattern256Date, selectDatePattern), !isBalancePeriod())
        }
        // графа 8
        if (row.dateRegistration) {
            checkDateValid(logger, row, 'dateRegistration', row.dateRegistration, !isBalancePeriod())
        }
    }

    checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod())
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
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
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    boolean isGroups = dataRows.find { it.getAlias() != null && it.getAlias().startsWith("head_") } != null
    if (!isGroups) {
        def totalRow = getDataRow(dataRows, 'total')
        dataRows.remove(totalRow)
        sortRows(dataRows, sortColumns)
        dataRows.add(totalRow)
    } else {
        def headMap = [:]
        def totalMap = [:]
        // находим строки начала и конца для каждого подразделения
        dataRows.each { row ->
            String alias = row.getAlias()
            if (alias != null) {
                if (alias.startsWith("head_")) {
                    headMap[alias.replace("head_","")] = row
                }
                if (alias.startsWith("total_")) {
                    totalMap[alias.replace("total_","")] = row
                }
            }
        }
        // по подразделениям
        headMap.keySet().each { key ->
            def headRow = headMap[key]
            def totalRow = totalMap[key]
            if (headRow && totalRow) {
                def groupFrom = headRow.getIndex()
                def groupTo = totalRow.getIndex() - 1
                def rows = (groupFrom < groupTo ? dataRows[groupFrom..(groupTo - 1)] : [])
                // Массовое разыменование строк НФ
                def columnList = headRow.keySet().collect { headRow.getCell(it).getColumn() }
                refBookService.dataRowsDereference(logger, rows, columnList)
                sortRows(rows, sortColumns)
            } else {
                logger.warn("Ошибка при сортировке. Нарушена структура налоговой формы. Отсутствуют строки заголовоков/итогов по подразделениям.")
            }
        }
    }

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
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
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def totalRow = getDataRow(dataRows, 'total')
    def rows = []

    // собрать из источников строки
    def formSources = departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate())
    // сортируем по наименованию подразделения
    formSources.sort { getDepartmentName(it.departmentId as Integer) }
    formSources.each {
        if (it.formTypeId == formData.formType.id) {
            def final child = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, null, formData.comparativePeriodId, formData.accruing)
            if (child != null && child.state == WorkflowState.ACCEPTED) {
                // получить все строки источника
                def final childDataRows = formDataService.getDataRowHelper(child).allCached
                def final department = departmentService.get(child.departmentId)
                def depHeadRow = getFixedRow(department.name, "head_${department.id}", true)
                rows.add(depHeadRow)
                // добавить только нефиксированные строки
                rows.addAll(childDataRows.findAll { row -> row.getAlias() == null || row.getAlias() == '' })
                def subTotalRow = getFixedRow("Всего по ${department.name}", "total_${department.id}", true)
                calcTotalSum(childDataRows, subTotalRow, totalColumns)
                rows.add(subTotalRow)
            }
        }
    }
    rows.add(totalRow)

    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
}

@Field
def departmentNameMap = [:]

def getDepartmentName(Integer id) {
    if (id != null && departmentNameMap[id] == null) {
        departmentNameMap[id] = departmentService.get(id).name
    }
    return departmentNameMap[id]
}

/** Получить произвольную фиксированную строку со стилями. */
def getFixedRow(String title, String alias, boolean isTotal) {
    def total = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
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
    checkTF(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 16
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null		// итоговая строка со значениями из тф для добавления
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    try {
        // проверить первые строки тф - заголовок и пустая строка
        checkFirstRowsTF(reader, logger)

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

    // подсчет итогов
    def totalRow = getFixedRow('Всего', 'total', true)
    newRows.add(totalRow)
    calcTotalSum(newRows, totalRow, totalColumns)

    // сравнение итогов
    checkAndSetTFSum(totalRow, totalTF, totalColumns, totalTF?.getImportIndex(), logger, false)

    updateIndexes(newRows)
    showMessages(newRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = newRows
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
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}'", fileRowIndex))
        return newRow
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
    newRow.dateRegistration = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9..14
    ['salesman', 'salesmanInnKpp', 'agentName', 'agentInnKpp', 'declarationNum', 'currency'].each { alias ->
        colIndex++
        newRow[alias] = pure(rowCells[colIndex])
    }

    // графа 15, 16
    ['cost', 'nds'].each { alias ->
        colIndex++
        def cell = pure(rowCells[colIndex])?.replaceAll(",", ".")
        newRow[alias] = parseNumber(cell, fileRowIndex, colIndex + colOffset, logger, true)
    }
    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 16
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
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
    def totalRowFromFile = null

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
            // последняя итоговая строка - ВСЕГО
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

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

    // подсчет итогов
    def totalRow = getFixedRow('Всего', 'total', true)
    rows.add(totalRow)
    updateIndexes(rows)
    // сравнение итогов
    compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
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
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNum')]),
            ([(headerRows[0][1]): getColumnName(tmpRow, 'typeCode')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'invoice')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'invoiceCorrecting')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'invoiceCorrection')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'invoiceCorrectingCorrection')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'documentPay')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'dateRegistration')]),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'salesman')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'salesmanInnKpp')]),
            ([(headerRows[0][10]): 'Сведения о посреднике (комиссионере, агенте)']),
            ([(headerRows[0][12]): getColumnName(tmpRow, 'declarationNum')]),
            ([(headerRows[0][13]): getColumnName(tmpRow, 'currency')]),
            ([(headerRows[0][14]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[0][15]): getColumnName(tmpRow, 'nds')]),

            ([(headerRows[1][10]): 'Наименование посредника']),
            ([(headerRows[1][11]): 'ИНН/КПП посредника']),
    ]
    (0..15).each { index ->
        headerMapping.add(([(headerRows[2][index]): (index + 1).toString()]))
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

    // графа 2..7
    def colIndex = 0
    ['typeCode', 'invoice', 'invoiceCorrecting', 'invoiceCorrection', 'invoiceCorrectingCorrection', 'documentPay'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 8
    colIndex++
    newRow.dateRegistration = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, false)

    // графа 9..14
    ['salesman', 'salesmanInnKpp', 'agentName', 'agentInnKpp', 'declarationNum', 'currency'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 15, 16
    ['cost', 'nds'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, false)
    }

    return newRow
}
