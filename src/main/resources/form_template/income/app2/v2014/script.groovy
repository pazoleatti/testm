package form_template.income.app2.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с
 * финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов
 * formTemplateId=1415
 *
 * @author SYasinskiy
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
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        calc()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, logger)
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

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def editableColumns = [
        'column1', 'column2', 'column3', 'column4', 'column5', 'column6', 'column7', 'column8', 'column9', 'column10',
        'column11', 'column12', 'column13', 'column14', 'column15', 'column16', 'column17', 'column18', 'column19', 'column20',
        'column21', 'column22', 'column23', 'column24', 'column28', 'column29', 'column20',
        'column31', 'column32', 'column33', 'column34', 'column35', 'column36', 'column37', 'column38', 'column39', 'column40',
        'column41', 'column42', 'column43', 'column44', 'column45', 'column46', 'column47', 'column48', 'column49', 'column50',
        'column51', 'column52', 'column53', 'column54', 'column55', 'column56', 'column57', 'column58', 'column59', 'column60',
        'column61', 'column62', 'column63', 'column64', 'column65', 'column66', 'column67', 'column68', 'column69', 'column70',
        'column71', 'column72'
]

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['column25', 'column26', 'column27']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['column1', 'column2', 'column3', 'column5', 'column6', 'column8', 'column24',
                       'column25', 'column27', 'column28']

@Field
def endDate = null

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(
        def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
        boolean required) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

//// Кастомные методы

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    def address = ['column13', 'column15', 'column16', 'column17', 'column18', 'column19', 'column20', 'column21']

    // Алиасы граф для арифметической проверки
    def arithmeticCheckAlias = ['column25', 'column26', 'column27']
    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1-9. Проверка на заполнение поля «<Наименование поля>»
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 10. Проверка на заполнение поля «<Наименование поля>»
        if (row.column14 == null && address.find { row.getCell(it).value != null }) {
            logger.error(errorMsg + "Графа «Регион (код)» не заполнена!")
        }

        // 11. Проверка вводимых символов в поле «Серия и номер документа»
        if (!checkFormat(row.column12, "^[а-яА-ЯёЁa-zA-Z0-9]+\$")) {
            logger.error(errorMsg + "Графа «Серия и номер документа» содержит недопустимые символы!")
        }

        // 12. Проверка заполнения поля «Номер дома (владения)»
        if (!checkFormat(row.column12, "^[а-яА-ЯёЁa-zA-Z0-9/ ]+\$")) {
            logger.error(errorMsg + "Графа «Серия и номер документа» содержит недопустимые символы!")
        }

        // 13-20. Проверка заполнения полей 13, 15, 16, 17, 18, 19, 20, 21
        if (row.column14 != null) {
            checkNonEmptyColumns(row, row.getIndex(), address, logger, true)
        }

        // 21. Проверка заполнения графы «Регион (код)»
        if (row.column22 == null && row.column23 == null && row.column21 == null) {
            logger.error(errorMsg + "Графа «Номер квартиры» не заполнена!")
        }

        // 22-24. Проверка соответствия суммы дохода суммам вычета
        def String errorMsg1 = errorMsg + "Сумма граф «Сумма вычета» для кода дохода = «%s» превышает значение поля «Сумма дохода» для данного кода."
        if (getSum1(row) > (row.column35 ?: 0)) {
            logger.info(String.format(errorMsg1, row.column34))
        }
        if (getSum2(row) > (row.column47 ?: 0)) {
            logger.info(String.format(errorMsg1, row.column46))
        }
        if (getSum3(row) > (row.column59 ?: 0)) {
            logger.info(String.format(errorMsg1, row.column58))
        }

        // Арифметические проверки расчета граф 25, 26, 27
        needValue['column25'] = calc25(row)
        needValue['column26'] = calc26(row)
        needValue['column27'] = calc27(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    for (DataRow<Cell> row in dataRows) {
        def BigDecimal value25 = calc25(row)
        def BigDecimal value26 = calc26(row)
        def BigDecimal value27 = calc27(row)

        // проверки, выполняемые до расчёта
        def int index = row.getIndex()
        checkOverflow(value25, row, 'column25', index, 15, '«Графа 35» - «Графа 47» - «Графа 59»')
        checkOverflow(value26, row, 'column26', index, 15,
                '«Графа 37» - «Графа 39» - «Графа 41» - «Графа 43» - «Графа 455» - ' +
                        '«Графа 49» - «Графа 51» - «Графа 53» - «Графа 55» - «Графа 57» - ' +
                        '«Графа 61» - «Графа 63» - «Графа 65» - «Графа 67» - «Графа 69» - ' +
                        '«Графа 71» - «Графа 73»')
        checkOverflow(value27, row, 'column27', index, 15, '«Графа 25» - «Графа 26»')

        row.column25 = value25
        row.column26 = value26
        row.column27 = value27
    }

    dataRowHelper.update(dataRows)

    // Сортировка групп и строк
    sortFormDataRows()
}

def BigDecimal calc25(def row) {
    return (row.column35 ?: 0) + (row.column47 ?: 0) + (row.column59 ?: 0)
}

def BigDecimal calc26(def row) {
    return getSum1(row) + getSum2(row) + getSum3(row)
}

def BigDecimal calc27(def row) {
    return (row.column25 ?: 0) + (row.column26 ?: 0)
}

def BigDecimal getSum1(def row) {
    return (row.column37 ?: 0) + (row.column39 ?: 0) + (row.column41 ?: 0) + (row.column43 ?: 0) + (row.column45 ?: 0)
}

def BigDecimal getSum2(def row) {
    return (row.column49 ?: 0) + (row.column51 ?: 0) + (row.column53 ?: 0) + (row.column55 ?: 0) + (row.column57 ?: 0)
}

def BigDecimal getSum3(def row) {
    return (row.column61 ?: 0) + (row.column63 ?: 0) + (row.column65 ?: 0) + (row.column67 ?: 0) + (row.column69 ?: 0)
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def int i = 1
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'column1'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 73, 1)

    def headerMapping = [:]
    (0..72).each { index ->
        headerMapping.put((xml.row[0].cell[index]), getColumnName(tmpRow, 'column'+ (index+1)))
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def int xlsIndexRow = xmlIndexRow + rowOffset

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)

       // TODO

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

def getNewRow() {
    def newRow = formData.createDataRow()

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 10, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)

        // TODO

        rows.add(newRow)
    }

    dataRowHelper.save(rows)
}

boolean checkFormat(String enteredValue, String pat) {
    Pattern p = Pattern.compile(pat);
    Matcher m = p.matcher(enteredValue);
    return m.matches();
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    dataRowHelper.saveSort()
}