package form_template.vat.vat_937_3.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
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
        formDataService.saveCachedDataRows(formData, logger)
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
def totalColumns = ['cost', 'vatSum', 'diffDec', 'diffInc', 'diffVatDec', 'diffVatInc']

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

@Field
def selectDatePattern = /$2\.$4\.$6/

// Признак периода ввода остатков
@Field
def isBalancePeriod

@Field
def startDate = null

@Field
def endDate = null

// признак корректирующего периода
@Field
def isCorrectionPeriodMap = [:]

@Field
def reportPeriod = null

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

    calc1(dataRows)
    calcChangeDateFormat(dataRows)
    calcTotal(dataRows)
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
    boolean wasError = false

    def isFirstSection = true
    // 01, 02, …, 13, 16, 17, …, 28
    def codeValues = ((1..13) + (16..28))

    def index1 = 0
    def index2 = 0
    def needRecalc = false

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
        if (!needRecalc && row.rowNumber != (isFirstSection ? ++index1 : ++index2)) {
            needRecalc = true
            def name = getColumnName(row, 'rowNumber')
            loggerError(row, "Графа «$name» заполнена неверно. Для обновления значения необходимо нажать на «Рассчитать».")
        }
        def innKppPatterns = [/([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}\/([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})/, /([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{10}/]
        ['buyerInnKpp', 'mediatorInnKpp'].each { alias ->
            if (row[alias] && !row[alias].matches(/^(\S{12}|\S{10}\/\S{9})$/)) {
                loggerError(row, String.format(WRONG2_ERROR_MSG, index, getColumnName(row, alias), "ХХХХХХХХХХ/ХХХХХХХХХ (организация) или ХХХХХХХХХХХХ (ИП)"))
            } else if (checkPattern(logger, row, alias, row[alias], innKppPatterns, null, !isBalancePeriod())) {
                checkControlSumInn(logger, row, alias, row[alias].split("/")[0], !isBalancePeriod())
            } else if (row[alias]) {
                if (!wasError) {
                    loggerError(row, String.format("Строка %s: Расшифровка паттерна «%s»: %s.", index, INN_JUR_PATTERN, INN_JUR_MEANING))
                    loggerError(row, String.format("Строка %s: Расшифровка паттерна «%s»: %s.", index, KPP_PATTERN, KPP_MEANING))
                    loggerError(row, String.format("Строка %s: Расшифровка паттерна «%s»: %s.", index, INN_IND_PATTERN, INN_IND_MEANING))
                }
                wasError = true
            }
        }
        // Проверки формата дат (графы 2, 4-7, 12)
        // графа 2
        if (row.date) {
            checkDateValid(logger, row, 'date', row.date, !isBalancePeriod())
        }
        // графа 4
        if (row.invoiceNumDate && row.invoiceNumDate.matches(pattern1000DateImport)) {
            checkDateValid(logger, row, 'invoiceNumDate', row.invoiceNumDate?.replaceFirst(pattern1000DateImport, selectDatePattern), !isBalancePeriod())
        }
        // графа 5
        if (row.invoiceCorrNumDate && row.invoiceCorrNumDate.matches(pattern3DateImport)) {
            checkDateValid(logger, row, 'invoiceCorrNumDate', row.invoiceCorrNumDate?.replaceFirst(pattern3DateImport, selectDatePattern), !isBalancePeriod())
        }
        // графа 6
        if (row.corrInvoiceNumDate && row.corrInvoiceNumDate.matches(pattern256DateImport)) {
            checkDateValid(logger, row, 'corrInvoiceNumDate', row.corrInvoiceNumDate?.replaceFirst(pattern256DateImport, selectDatePattern), !isBalancePeriod())
        }
        // графа 7
        if (row.corrInvCorrNumDate && row.corrInvCorrNumDate.matches(pattern3DateImport)) {
            checkDateValid(logger, row, 'corrInvCorrNumDate', row.corrInvCorrNumDate?.replaceFirst(pattern3DateImport, selectDatePattern), !isBalancePeriod())
        }
        // графа 21
        if (row.mediatorNumDate && row.mediatorNumDate.matches(pattern1000DateImport)) {
            checkDateValid(logger, row, 'mediatorNumDate', row.mediatorNumDate?.replaceFirst(pattern1000DateImport, selectDatePattern), !isBalancePeriod())
        }
    }

    // 7. Проверка итоговых значений (графа 14..19)
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, 'part_' + section)
        def lastRow = getDataRow(dataRows, 'total_' + section)
        for (def alias : totalColumns) {
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
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def sortColumns = allColumns - ['rowNumber', 'fix']
    boolean isGroups = dataRows.find { it.getAlias() != null && it.getAlias().startsWith("head_") } != null
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, 'part_' + section)
        def lastRow = getDataRow(dataRows, 'total_' + section)
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1
        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

        if (!isGroups) {
            // Массовое разыменование строк НФ
            def columnList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
            refBookService.dataRowsDereference(logger, sectionsRows, columnList)
            sortRows(sectionsRows, sortColumns)
        } else {
            def headMap = [:]
            def totalMap = [:]
            // находим строки начала и конца для каждого подразделения
            sectionsRows.each { row ->
                String alias = row.getAlias()
                if (alias != null) {
                    if (alias.startsWith("head_")) {
                        headMap[alias.replace("head_","")] = row
                    }
                    if (alias.startsWith("sub_total_")) {
                        totalMap[alias.replace("sub_total_","")] = row
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
    }
    calc1(dataRows)

    if (saveInDB) {
        formDataService.saveCachedDataRows(formData, logger)
    } else {
        updateIndexes(dataRows);
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
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить нефиксированные строки
    deleteExtraRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    def formSources = departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate())
    // сортируем по наименованию подразделения
    formSources.sort { getDepartmentName(it.departmentId as Integer) }
    formSources.each {
        if (it.formTypeId == formData.formType.id) {
            def child = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, null, formData.comparativePeriodId, formData.accruing)
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
}

@Field
def departmentNameMap = [:]

def getDepartmentName(Integer id) {
    if (id != null && departmentNameMap[id] == null) {
        departmentNameMap[id] = departmentService.get(id).name
    }
    return departmentNameMap[id]
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
    def total = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
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
    calcTotalSum(copyRows, subTotalRow, totalColumns)
    destinationDataRows.add(getDataRow(destinationDataRows, toAlias).getIndex() - 1, subTotalRow)
    updateIndexes(destinationDataRows)
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
        totalColumns.each { alias ->
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
    def index1 = 0
    def index2 = 0
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

void calcChangeDateFormat(def dataRows) {
    if (formDataEvent != FormDataEvent.IMPORT) {
        return
    }
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

// TODO (SBRFACCTAX-15074) убрать
void checkTFLocal(BufferedInputStream inputStream, String fileName) {
    checkBeforeGetXml(inputStream, fileName);
    if (fileName != null && !fileName.toLowerCase().endsWith(".rnu")) {
        throw new ServiceException("Выбранный файл не соответствует формату rnu!");
    }
}

void importTransportData() {
    // TODO (SBRFACCTAX-15074) заменить на "ScriptUtils.checkTF(ImportInputStream, UploadFileName)"
    checkTFLocal(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 19
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null		// итоговая строка со значениями из тф для добавления
    def mapRows = [:]

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

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && totalTF) {
        // итоговая строка для сверки сумм
        def totalTmp = formData.createStoreMessagingDataRow()
        totalColumns.each { alias ->
            totalTmp[alias] = BigDecimal.ZERO
        }

        // подсчет итогов
        mapRows.each { sectionIndex, dataRows ->
            for (def row : dataRows) {
                if (row.getAlias()) {
                    continue
                }
                totalColumns.each { alias ->
                    totalTmp[alias] = totalTmp[alias] + (row[alias] ?: BigDecimal.ZERO)
                }
            }
        }

        checkTFSum(totalTmp, totalTF, totalColumns, fileRowIndex, logger, false)
        // итог в файле не должен совпадать с итогами в НФ
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
        def rows = (mapRows[section] ?: [])
        calcTotalSum(rows, lastRow, totalColumns)

        newRows.add(firstRow)
        newRows.addAll(rows)
        newRows.add(lastRow)
    }

    showMessages(newRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(newRows)
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
    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}'", fileRowIndex))
        return newRow
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

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 19
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
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
    def allValuesCount = allValues.size()
    def mapRows = [:]
    def sectionIndex = null

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows
    def firstRow = getDataRow(templateRows, 'part_1')
    def lastRow = getDataRow(templateRows, 'part_2')
    def totalRowFromFileMap = [:]

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
        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        def firstValue = rowValues[INDEX_FOR_SKIP]
        rowIndex++
        if (firstValue == firstRow.fix || firstValue == lastRow.fix) {
            def isFirstSection = (firstValue == firstRow.fix)
            sectionIndex = (isFirstSection ? sections[0] : sections[1])
            mapRows.put(sectionIndex, [])

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (firstValue == 'Всего') {
            def alias = 'part_' + sectionIndex
            totalRowFromFileMap[alias] = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (firstValue) {
            rowIndex--
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }

        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        mapRows[sectionIndex].add(newRow)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    def newRows = []
    // заполнение строк + расчет итогов
    for (def section : sections) {
        def row1 = getDataRow(templateRows, "part_$section")
        def row2 = getDataRow(templateRows, "total_$section")
        def rows = mapRows[section]

        newRows.add(row1)
        newRows.addAll(rows)
        newRows.add(row2)
    }
    updateIndexes(newRows)

    // сравнение итогов
    if (!totalRowFromFileMap.isEmpty()) {
        mapRows.each { section, sectionRows ->
            def totalRowFromFile = totalRowFromFileMap["part_$section"]
            def totalRow = getDataRow(templateRows, "total_$section")
            compareSimpleTotalValues(totalRow, totalRowFromFile, sectionRows, totalColumns, formData, logger, false)
        }
    }

    showMessages(newRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = newRows
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
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNumber')]),
            // вторая ячейка скрытая
            ([(headerRows[0][2]): getColumnName(tmpRow, 'date')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'opTypeCode')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'invoiceNumDate')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'invoiceCorrNumDate')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'corrInvoiceNumDate')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'corrInvCorrNumDate')]),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'buyerName')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'buyerInnKpp')]),

            ([(headerRows[0][10]): 'Сведения о посреднической деятельности, указываемые комиссионером (агентом), застройщиком или техническим заказчиком']),
            ([(headerRows[1][10]): 'наименование продавца (из графы 8 части 2)/ субкомиссионера (субагента)']),
            ([(headerRows[1][11]): 'ИНН/КПП продавца (из графы 9 части 2)/ субкомиссионера (субагента)']),
            ([(headerRows[1][12]): 'номер и дата счета-фактуры, полученного от продавца (из графы 4 части 2)/ код вида сделки']),

            ([(headerRows[0][13]): getColumnName(tmpRow, 'currNameCode')]),
            ([(headerRows[0][14]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[0][15]): getColumnName(tmpRow, 'vatSum')]),

            ([(headerRows[0][16]): 'Разница стоимости с учетом НДС по корректировочному счету-фактуре']),
            ([(headerRows[1][16]): 'уменьшение']),
            ([(headerRows[1][17]): 'увеличение']),

            ([(headerRows[0][18]): 'Разница НДС по корректировочному счету-фактуре']),
            ([(headerRows[1][18]): 'уменьшение']),
            ([(headerRows[1][19]): 'увеличение']),

            ([(headerRows[2][0]): '1']),
    ]
    (2..19).each { index ->
        headerMapping.add(([(headerRows[2][index]): index.toString()]))
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

    // графа 1
    def colIndex = 0
    newRow.rowNumber = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 2
    colIndex = 2
    newRow.date = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 3..13
    ['opTypeCode', 'invoiceNumDate', 'invoiceCorrNumDate', 'corrInvoiceNumDate', 'corrInvCorrNumDate', 'buyerName',
            'buyerInnKpp', 'mediatorName', 'mediatorInnKpp', 'mediatorNumDate', 'currNameCode'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex] ?: null
    }

    // графа 14..19
    ['cost', 'vatSum', 'diffDec', 'diffInc', 'diffVatDec', 'diffVatInc'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}