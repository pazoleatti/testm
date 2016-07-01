package form_template.vat.vat_724_2_2.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * 6.3	(724.2.2) Расчёт суммы налога по операциям по реализации товаров (работ, услуг), обоснованность применения
 * налоговой ставки 0 процентов по которым документально подтверждена
 *
 * formTemplateId=1602
 */

// графа 1 - rowNum     № пп
// графа 2 - code       Код операции
// графа 3 - name       Наименование операции
// графа 4 - base       Налоговая база

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
}

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['base']

@Field
def totalColumns = ['base']

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

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def itog = getDataRow(dataRows, 'itog')
    itog?.base = calcItog(dataRows)
}

// Расчет итога
def calcItog(def dataRows) {
    def sum = BigDecimal.ZERO
    for (def row in dataRows) {
        if (row.getAlias() != 'itog') {
            sum += row.base == null ? 0 : row.base
        }
    }
    return sum
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row in dataRows) {
        def index = row.getIndex()
        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, index ?: 0, nonEmptyColumns, logger, true)
    }
    // 2. Проверка итоговых значений
    def itog = getDataRow(dataRows, 'itog')
    if (itog.base != calcItog(dataRows)) {
        logger.error(WRONG_TOTAL, getColumnName(itog, 'base'))
    }
}

// Консолидация
void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    dataRows.each {
        it.base = null
    }
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.getFormType().getTaxType() == TaxType.VAT) {
            formDataService.getDataRowHelper(source).getAllCached().each { srcRow ->
                if (srcRow.getAlias() != null && !srcRow.getAlias().equals('itog')) {
                    def row = getDataRow(dataRows, srcRow.getAlias())
                    row.base = (row.base ?: 0) + (srcRow.base ?: 0)
                }
            }
        }
    }
}

void importTransportData() {
    checkTF(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 4
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null        // итоговая строка со значениями из тф для добавления

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    def dataRows = formDataService.getDataRowHelper(formData).allCached

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
                    totalTF = formData.createStoreMessagingDataRow()
                    fillRow(totalTF, rowCells, COLUMN_COUNT, fileRowIndex, false)
                }
                break
            }
            setValues(dataRows, rowCells, COLUMN_COUNT, fileRowIndex, rowIndex)
        }
    } finally {
        reader.close()
    }

    def totalRow = getDataRow(dataRows, 'itog')
    // подсчет итогов
    totalRow.base = calcItog(dataRows)

    // сравнение итогов
    checkAndSetTFSum(totalRow, totalTF, totalColumns, totalTF?.getImportIndex(), logger, false)
    showMessages(dataRows, logger)
}

/** Устанавливает значения из тф в строку нф. */
def setValues(def dataRows, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells == null) {
        return true
    }
    if (rowIndex - 1 >= dataRows.size()) {
        return false
    }
    // найти нужную строку нф
    def dataRow = dataRows.get(rowIndex - 1)
    // заполнить строку нф значениями из тф
    return fillRow(dataRow, rowCells, columnCount, fileRowIndex, true)
}

/**
 * Заполняет заданную строку нф (любую) значениями из тф.
 *
 * @param dataRow строка нф
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param checkFixedValues проверить ли фиксированные значения (при заполенении итоговой строки это не нужно делать)
 *
 * @return вернет true или false, если количество значений в строке тф меньше
 */
def fillRow(def dataRow, String[] rowCells, def columnCount, def fileRowIndex, def checkFixedValues) {
    dataRow.setImportIndex(fileRowIndex)
    if (rowCells.length != columnCount + 2) {
        rowError(logger, dataRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return false
    }
    def colOffset = 1
    def colIndex

    if (checkFixedValues) {
        def values = [:]

        // графа 1
        colIndex = 1
        values.rowNum = round(parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true), 0)

        // графа 2
        colIndex = 2
        values.code = pure(rowCells[colIndex])

        // графа 3
        colIndex = 3
        values.name = pure(rowCells[colIndex])

        // Проверить фиксированные значения (графа 1..3)
        ['rowNum', 'code', 'name'].each { alias ->
            def value = values[alias].toString()
            def valueExpected = pure(dataRow.getCell(alias).value?.toString())
            checkFixedValue(dataRow, value, valueExpected, fileRowIndex, alias, logger, true)
        }
    }

    // графа 4
    colIndex = 4
    dataRow.base = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    return true
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 4
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = tmpRow.getCell('rowNum').column.name
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

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

    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def totalRow = getDataRow(dataRows, 'itog')

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
        rowIndex++
        // итоговая строка
        if (rowValues[INDEX_FOR_SKIP] == 'Итого') {
            fillTotalFromXls(totalRow, rowValues, fileRowIndex, rowIndex, colOffset)

            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // простая строка
        if (rowIndex > dataRows.size()) {
            break
        }
        // найти нужную строку нф
        def dataRow = dataRows.get(rowIndex - 1)
        // заполнить строку нф значениями из эксель
        fillRowFromXls(dataRow, rowValues, fileRowIndex, rowIndex, colOffset)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // сравнение итогов
    def totalRowTmp = formData.createStoreMessagingDataRow()
    // подсчитанные итоговые значения для сравнения
    totalRowTmp.base = calcItog(dataRows)
    compareTotalValues(totalRow, totalRowTmp, ['base'], logger, false)

    showMessages(dataRows, logger)
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
            ([(headerRows[0][0]): tmpRow.getCell('rowNum').column.name]),
            ([(headerRows[0][1]): tmpRow.getCell('code').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('name').column.name]),
            ([(headerRows[0][3]): tmpRow.getCell('base').column.name]),
            ([(headerRows[1][0]): '1']),
            ([(headerRows[1][1]): '2']),
            ([(headerRows[1][2]): '3']),
            ([(headerRows[1][3]): '4'])
    ]
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Заполняет заданную строку нф значениями из экселя.
 *
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
def fillRowFromXls(def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)
    def colIndex = -1

    def tmpValues = [:]
    colIndex++
    tmpValues.rowNum = round(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true), 0)
    colIndex++
    tmpValues.code = values[colIndex]
    colIndex++
    tmpValues.name = values[colIndex]

    // Проверить фиксированные значения (графы 2, 3)
    ['code', 'name'].each { alias ->
        def value = StringUtils.cleanString(tmpValues[alias]?.toString())
        def valueExpected = StringUtils.cleanString(dataRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
    }

    // графа 4
    colIndex++
    dataRow.base = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
}

/**
 * Заполняет итоговую строку нф значениями из экселя.
 *
 * @param dataRow итоговая строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
void fillTotalFromXls(def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)

    // графа 4
    colIndex = 3
    dataRow.base = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
}