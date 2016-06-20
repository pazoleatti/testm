package form_template.vat.vat_724_2_2.v2014

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * 6.3	(724.2.2) Расчёт суммы налога по операциям по реализации товаров (работ, услуг), обоснованность применения
 * налоговой ставки 0 процентов по которым документально подтверждена
 *
 * formTemplateId=602
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

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['base']

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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def itog = getDataRow(dataRows, 'itog')
    itog?.base = calcItog(dataRows)
    dataRowHelper.update(dataRows);
}

// Расчет итога
def calcItog(def dataRows) {
    def sum = 0 as BigDecimal
    for (def row in dataRows) {
        if (row.getAlias() != 'itog') {
            sum += row.base == null ? 0 : row.base
        }
    }
    return sum
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
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
    dataRowHelper.update(dataRows)
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNum'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 4, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): tmpRow.getCell('rowNum').column.name,
            (xml.row[0].cell[1]): tmpRow.getCell('code').column.name,
            (xml.row[0].cell[2]): tmpRow.getCell('name').column.name,
            (xml.row[0].cell[3]): tmpRow.getCell('base').column.name,
            (xml.row[1].cell[0]): '1',
            (xml.row[1].cell[1]): '2',
            (xml.row[1].cell[2]): '3',
            (xml.row[1].cell[3]): '4'
    ]


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
        if (row.cell[1].text() == 'Итого') {
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
        dataRow.base = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
    }
    dataRowHelper.save(dataRows)
}

void importTransportData() {
    int COLUMN_COUNT =4
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
    def totalTF = null		// итоговая строка со значениями из тф для добавления

    while ((rowCells = reader.readNext()) != null) {
        fileRowIndex++

        def isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
        if (isEmptyRow) {
            if (countEmptyRow > 0) {
                // если встретилась вторая пустая строка, то дальше только строки итогов и ЦП
                rowCells = reader.readNext()
                isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
                if (!isEmptyRow) {
                    totalTF = formData.createDataRow()
                    fillRow(totalTF, rowCells, COLUMN_COUNT, ++fileRowIndex, ++rowIndex, false)
                }
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

    // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
    def totalColumnsIndexMap = [ 'base' : 4 ]
    // подсчет итогов
    def itogValues = ['base' : calcItog(dataRows)]
    def totalRow = getDataRow(dataRows, 'itog')
    totalColumnsIndexMap.keySet().asList().each { alias ->
        totalRow[alias] = itogValues[alias]
    }

    // сравнение итогов
    if (totalTF) {
        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
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
    if (rowIndex - 1 >= dataRows.size()) {
        return false
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
def fillRow(def dataRow, String[] rowCells, def columnCount, def fileRowIndex, def rowInde, def checkFixedValues) {
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