package form_template.vat.vat_724_2_1.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * (724.2.1) Операции, не подлежащие налогообложению (освобождаемые от налогообложения), не признаваемые объектом
 * налогообложения, операции по реализации товаров (работ, услуг), местом реализации которых не признается территория
 * Российской Федерации.
 *
 * formTemplateId=10601
 *
 * @author Stanislav Yasinskiy
 */

// графа 1 - rowNum         № пп
// графа 2 - code           Код операции
// графа 3 - name           Наименование операции
// графа 4 - realizeCost    Стоимость реализованных (переданных) товаров (работ, услуг) без НДС
// графа 5 - obtainCost     Стоимость приобретенных товаров  (работа, услуг), не облагаемых НДС

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
}

// Проверяемые на пустые значения атрибуты (графа  1..5)
@Field
def nonEmptyColumns = ['code', 'name', 'realizeCost', 'obtainCost']

// Поля, для которых подсчитываются итоговые значения (графа 4, 5)
@Field
def totalColumns = ['realizeCost', 'obtainCost']

// Дата начала отчетного периода
@Field
def startDate = null

@Field
def calendarStartDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def editableStyle = 'Редактируемая'

//// Кастомные методы

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // подсчет итогов
    def itogValues = calcItog(dataRows)
    def itog = getDataRow(dataRows, 'itog')
    totalColumns.each { alias ->
        itog.getCell(alias).setValue(itogValues[alias], itog.getIndex())
    }
    dataRowHelper.save(dataRows);
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row in dataRows) {
        if (row.getAlias() == 'itog') {
            continue
        }

        // 1. Проверка заполнения граф (по графе 5 обязательны только редактируемые)
        def columns = nonEmptyColumns
        if (row.getCell('obtainCost')?.style?.alias != editableStyle) {
            columns = nonEmptyColumns - 'obtainCost'
        }
        checkNonEmptyColumns(row, row.getIndex(), columns, logger, true)
    }

    // 2. Проверка итоговых значений
    def itogValues = calcItog(dataRows)
    def itog = getDataRow(dataRows, 'itog')
    totalColumns.each { alias ->
        if (itog.getCell(alias).value != itogValues[alias]) {
            logger.error(WRONG_TOTAL, getColumnName(itog, alias))
        }
    }
}

def getReportPeriodStartDate() {
    if (!startDate) {
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

// Консолидация
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    dataRows.each{
        it.realizeCost = null
        it.obtainCost = null
    }
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getCalendarStartDate(), getReportPeriodEndDate()).each {
        def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.formType.taxType == TaxType.VAT) {
            formDataService.getDataRowHelper(source).allCached.each { srcRow ->
                if (srcRow.getAlias() != null && !srcRow.getAlias().equals('itog')) {
                    def row = dataRowHelper.getDataRow(dataRows, srcRow.getAlias())
                    row.realizeCost = (row.realizeCost ?: 0) + (srcRow.realizeCost ?: 0)
                    row.obtainCost = (row.obtainCost ?: 0) + (srcRow.obtainCost ?: 0)
                }
            }
        }
    }

    dataRowHelper.update(dataRows)
}

def calcItog(def dataRows) {
    def itogValues = [:]
    totalColumns.each {alias ->
        itogValues[alias] = roundValue(0)
    }
    for (def row in dataRows) {
        if (row.getAlias() == 'itog') {
            continue
        }
        totalColumns.each { alias ->
            itogValues[alias] += roundValue(row.getCell(alias).value ?: 0)
        }
    }
    return itogValues
}

def roundValue(def value, int precision = 2) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNum'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): getColumnName(tmpRow, 'rowNum'),
            (xml.row[0].cell[1]): getColumnName(tmpRow, 'code'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'name'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'realizeCost'),
            (xml.row[0].cell[4]): getColumnName(tmpRow, 'obtainCost')
    ]

    (1..5).each { index ->
        headerMapping.put((xml.row[1].cell[index - 1]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()
    def indexRow = 0

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[2].text() == 'Итого') {
            break
        }

        def dataRow = dataRows.get(indexRow)
        dataRow.setImportIndex(xlsIndexRow)
        indexRow++

        def xmlIndexCol = -1

        def values = [:]
        xmlIndexCol++
        values.rowNum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        values.code = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        values.name = row.cell[xmlIndexCol].text()

        // Проверить фиксированные значения (графа 1..3)
        ['rowNum', 'code', 'name'].each { alias ->
            def value = StringUtils.cleanString(values[alias]?.toString())
            def valueExpected = StringUtils.cleanString(dataRow.getCell(alias).value?.toString())
            checkFixedValue(dataRow, value, valueExpected, indexRow, alias, logger, true)
        }

        // графа 4
        xmlIndexCol++
        dataRow.realizeCost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // графа 5
        xmlIndexCol++
        dataRow.obtainCost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
    }
    dataRowHelper.save(dataRows)
}

void importTransportData() {
    int COLUMN_COUNT = 5
    int TOTAL_ROW_COUNT = 1
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    checkBeforeGetXml(ImportInputStream, UploadFileName)

    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    String[] rowCells
    int countEmptyRow = 0	// количество пустых строк
    int fileRowIndex = 0    // номер строки в файле
    int rowIndex = 0        // номер строки в НФ
    int totalRowCount = 0   // счетчик кол-ва итогов
    def total = null		// итоговая строка со значениями из тф для добавления

    while ((rowCells = reader.readNext()) != null) {
        fileRowIndex++

        def isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
        if (isEmptyRow) {
            if (countEmptyRow > 0) {
                // если встретилась вторая пустая строка, то дальше только строки итогов и ЦП
                totalRowCount++
                total = formData.createDataRow()
                fillRow(total, reader.readNext(), COLUMN_COUNT, ++fileRowIndex, ++rowIndex, false)
                break
            }
            countEmptyRow++
            continue
        }
        // если еще не было пустых строк, то это первая строка - заголовок (пропускается)
        // обычная строка
        if (countEmptyRow != 0 && !setValues(dataRows, rowCells, COLUMN_COUNT, fileRowIndex, ++rowIndex)) {
            break
        }
    }
    reader.close()

    // проверка итоговой строки
    if (TOTAL_ROW_COUNT != 0 && totalRowCount != TOTAL_ROW_COUNT) {
        logger.error(ROW_FILE_WRONG, fileRowIndex)
    }

    // сравнение итогов
    if (total) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = [ 'realizeCost' : 4, 'obtainCost' : 5 ]
        // подсчет итогов
        def itogValues = calcItog(dataRows)
        def totalRow = getDataRow(dataRows, 'itog')
        totalColumnsIndexMap.keySet().asList().each { alias ->
            totalRow[alias] = itogValues[alias]
        }
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
    }
    dataRowHelper.update(dataRows)
}

/** Устанавливает значения из тф в строку нф. */
def setValues(def dataRows, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells == null) {
        return true
    }
    // найти нужную строку нф
    def dataRow = dataRows.get(rowIndex - 1)
    // заполнить строку нф значениями из тф
    return fillRow(dataRow, rowCells, columnCount, fileRowIndex, rowIndex, true)
}

/**
 * Заполняет заданную строку нф (любую) значениями из тф.
 *
 * @param dataRow строка нф
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 * @param checkFixedValues проверить ли фиксированные значения (при заполенении итоговой строки это не нужно делать)
 *
 * @return вернет true или false, если количество значений в строке тф меньше
 */
def fillRow(def dataRow, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex, def checkFixedValues) {
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
        values.rowNum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

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
            checkFixedValue(dataRow, value, valueExpected, rowIndex, alias, logger, true)
        }
    }

    // графа 4
    colIndex = 4
    dataRow.realizeCost = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex = 5
    dataRow.obtainCost = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    return true
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}