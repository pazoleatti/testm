package form_template.income.rnu31.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Форма "(РНУ-31) Регистр налогового учёта процентного дохода по купонным облигациям".
 * formTemplateId=1328
 *
 * @author rtimerbaev
 */

// графа 1  - number
// графа 2  - securitiesType
// графа 3  - ofz
// графа 4  - municipalBonds
// графа 5  - governmentBonds
// графа 6  - mortgageBonds
// графа 7  - municipalBondsBefore
// графа 8  - rtgageBondsBefore
// графа 9  - ovgvz
// графа 10 - eurobondsRF
// графа 11 - itherEurobonds
// графа 12 - corporateBonds

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        // Всего форма должна содержать одну строку
        break
    case FormDataEvent.DELETE_ROW:
        // Всего форма должна содержать одну строку
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        prevPeriodCheck()
        logicCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        if (UploadFileName.endsWith(".rnu")) {
            importTransportData()
        } else {
            importData()
        }
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

// Редактируемые атрибуты (графа 3..12)
@Field
def editableColumns = ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds', 'municipalBondsBefore',
                       'rtgageBondsBefore', 'ovgvz', 'eurobondsRF', 'itherEurobonds', 'corporateBonds']

// Проверяемые на пустые значения атрибуты (графа 3..12)
@Field
def nonEmptyColumns = editableColumns

@Field
def totalSumColumns = editableColumns

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def row = getDataRow(dataRows, 'total')
    row.number = formData.periodOrder
}

void logicCheck() {
    if (formData.periodOrder == null) {
        throw new ServiceException("Месячная форма создана как квартальная!")
    }
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // строка из текущего отчета
    def row = getDataRow(dataRows, 'total')

    // 1. Обязательность заполнения полей графы 3..12
    checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

    // 2. Проверка на уникальность поля «№ п.п»
    if (formData.periodOrder != row.number) {
        rowError(logger, row, "Нарушена уникальность номера по порядку!")
    }

    // 3-12. Проверка процентного (купонного) дохода по виду ценной бумаги
    if (formData.periodOrder != 1 && formData.kind == FormDataKind.PRIMARY) {
        // строка из предыдущего отчета
        def rowOld = getPrevMonthTotalRow()
        if (rowOld != null) {
            for (def column : editableColumns) {
                if (row.getCell(column).value != null
                        && rowOld.getCell(column).value != null
                        && row.getCell(column).value < rowOld.getCell(column).value) {
                    def msg = "Процентный (купонный) доход по «${getColumnName(row, column)}» уменьшился!"
                    // нефатальная для графы 5, 9, 10, 11
                    if (column in ['governmentBonds', 'ovgvz', 'eurobondsRF', 'itherEurobonds']) {
                        rowWarning(logger, row, msg)
                    } else {
                        rowError(logger, row, msg)
                    }
                }
            }
        }
    }

    // 14-22. Проверка на неотрицательные значения
    for (
            def column : ['ofz', 'municipalBonds', 'mortgageBonds', 'municipalBondsBefore', 'rtgageBondsBefore', 'corporateBonds']) {
        def value = row.getCell(column).value
        if (value != null && value < 0) {
            def columnName = getColumnName(row, column)
            rowError(logger, row, "Значение графы «$columnName» по строке 1 отрицательное!")
        }
    }
    for (def column : ['governmentBonds', 'ovgvz', 'eurobondsRF', 'itherEurobonds']) {
        def value = row.getCell(column).value
        if (value != null && value < 0) {
            def columnName = getColumnName(row, column)
            rowWarning(logger, row, "Значение графы «$columnName» по строке 1 отрицательное!")
        }
    }
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // занулить данные и просуммировать из источников
    def row = getDataRow(dataRows, 'total')
    editableColumns.each { alias ->
        row.getCell(alias).setValue(0, row.getIndex())
    }

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def sourceFormData = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(sourceFormData)?.allSaved
                def sourceRow = getDataRow(sourceDataRows, 'total')
                editableColumns.each { alias ->
                    row.getCell(alias).setValue(sourceRow.getCell(alias).value + row.getCell(alias).getValue(), row.getIndex())
                }
            }
        }
    }
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 12, 0)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def int rnuIndexRow = 3
    def int colOffset = 1
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows
    def row = getDataRow(templateRows, 'total')
    row.keySet().each { row.getCell(it).setCheckMode(true) }

    def xmlRow = xml.row[0]

    // графа 3
    row.ofz = getNumber(xmlRow.cell[3].text(), rnuIndexRow, 3 + colOffset)
    // графа 4
    row.municipalBonds = getNumber(xmlRow.cell[4].text(), rnuIndexRow, 4 + colOffset)
    // графа 5
    row.governmentBonds = getNumber(xmlRow.cell[5].text(), rnuIndexRow, 5 + colOffset)
    // графа 6
    row.mortgageBonds = getNumber(xmlRow.cell[6].text(), rnuIndexRow, 6 + colOffset)
    // графа 7
    row.municipalBondsBefore = getNumber(xmlRow.cell[7].text(), rnuIndexRow, 7 + colOffset)
    // графа 8
    row.rtgageBondsBefore = getNumber(xmlRow.cell[8].text(), rnuIndexRow, 8 + colOffset)
    // графа 9
    row.ovgvz = getNumber(xmlRow.cell[9].text(), rnuIndexRow, 9 + colOffset)
    // графа 10
    row.eurobondsRF = getNumber(xmlRow.cell[10].text(), rnuIndexRow, 10 + colOffset)
    // графа 11
    row.itherEurobonds = getNumber(xmlRow.cell[11].text(), rnuIndexRow, 11 + colOffset)
    // графа 12
    row.corporateBonds = getNumber(xmlRow.cell[12].text(), rnuIndexRow, 12 + colOffset)

    row.number = formData.periodOrder

    showMessages([row], logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = [row]
    }
}

// Получить строку за прошлый месяц
def getPrevMonthTotalRow() {
    // проверка на январь и если не задан месяц формы
    if (formData.periodOrder == null || formData.periodOrder == 1) {
        return null
    }
    def prevFormData = formDataService.getFormDataPrev(formData)
    if (prevFormData != null) {
        def prevDataRows = formDataService.getDataRowHelper(prevFormData)?.allSaved
        return prevDataRows?.find{ 'total'.equals(it.getAlias()) }
    }
    return null
}

void prevPeriodCheck() {
    // Проверка наличия формы за предыдущий период начиная с отчета за 2-й отчетный период,
    // т.е. проверка отчёта за январь не осуществляется
    if (formData.periodOrder != 1 && formData.kind == FormDataKind.PRIMARY) {
        formDataService.checkMonthlyFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder, true, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

void importData() {
    int COLUMN_COUNT = 12
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = '№ пп'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
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

    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def dataRows = formTemplate.rows

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
        // простая строка
        rowIndex++
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
    if (rowIndex < dataRows.size()) {
        logger.error("Структура файла не соответствует макету налоговой формы.")
    }
    showMessages(dataRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = dataRows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    if (headerRows.isEmpty() || headerRows.size() < 3) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    // размер заголовка проверяется по последней строке (нумерация столбцов) потому что в первых строках есть объединения
    checkHeaderSize(headerRows[rowCount - 1].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]): '№ пп']),
            ([(headerRows[0][1]): 'Ставка налога на прибыль']),
            ([(headerRows[0][2]): '15%']),
            ([(headerRows[0][6]): '9%']),
            ([(headerRows[0][8]): '0%']),
            ([(headerRows[0][9]): '20%']),
            ([(headerRows[1][1]): 'Вид ценных бумаг']),
            ([(headerRows[1][2]): 'ОФЗ']),
            ([(headerRows[1][3]): 'Субфедеральные и муниципальные облигации, за исключением муниципальных облигаций, выпущенных до 1 января 2007 года на срок не менее 3 лет']),
            ([(headerRows[1][4]): 'Государственные облигации Республики Беларусь']),
            ([(headerRows[1][5]): 'Ипотечные облигации, выпущенные после 1 января 2007 года']),
            ([(headerRows[1][6]): 'Муниципальные облигации, выпущенные до 1 января 2007 года на срок не менее 3 лет']),
            ([(headerRows[1][7]): 'Ипотечные облигации, выпущенные до 1 января 2007 года']),
            ([(headerRows[1][8]): 'ОВГВЗ']),
            ([(headerRows[1][9]): 'Еврооблигации РФ']),
            ([(headerRows[1][10]): 'Прочие еврооблигации']),
            ([(headerRows[1][11]): 'Корпоративные облигации']),
    ]
    (1..12).each { index ->
        headerMapping.add(([(headerRows[2][index - 1]): index.toString()]))
    }
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
    dataRow.keySet().each { dataRow.getCell(it).setCheckMode(true) }

    // Проверить фиксированные значения (графа 2)
    def value = StringUtils.cleanString(values[1])
    def valueExpected = StringUtils.cleanString(dataRow.securitiesType)
    checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), 'securitiesType', logger, true)

    // графа 3..12
    def colIndex = 1
    ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds', 'municipalBondsBefore',
     'rtgageBondsBefore', 'ovgvz', 'eurobondsRF', 'itherEurobonds', 'corporateBonds'].each { alias ->
        colIndex++
        dataRow[alias] = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    }
}