package form_template.vat.vat_724_2_2.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
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
        def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
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
            (xml.row[0].cell[0]): getColumnName(tmpRow, 'rowNum'),
            (xml.row[0].cell[1]): getColumnName(tmpRow, 'code'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'name'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'base'),
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
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 4, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def int rnuIndexRow = 2
    def int colOffset = 1

    dataRows.each {
        it.base = null
    }

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def indexRow = rnuIndexRow - 2
        def dataRow = dataRows.get(indexRow - 1)

        def values = [:]
        values.rowNum = parseNumber(row.cell[1].text(), rnuIndexRow, 1 + colOffset, logger, true)
        values.code = row.cell[2].text()
        values.name = row.cell[3].text()

        // Проверить фиксированные значения (графа 1..3)
        ['rowNum', 'code', 'name'].each { alias ->
            def value = StringUtils.cleanString(values[alias]?.toString())
            def valueExpected = StringUtils.cleanString(dataRow.getCell(alias).value?.toString())
            checkFixedValue(dataRow, value, valueExpected, indexRow, alias, logger, true)
        }

        // графа 4
        dataRow.base = parseNumber(row.cell[4].text(), rnuIndexRow, 4 + colOffset, logger, true)
    }
    def totalRow = getDataRow(dataRows, 'itog')
    totalRow.base = calcItog(dataRows)

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow += 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // графа 4
        total.base = parseNumber(row.cell[4].text(), rnuIndexRow, 4 + colOffset, logger, true)

        def colIndexMap = ['base' : 4]
        for (def alias : ['base']) {
            def v1 = total[alias]
            def v2 = totalRow[alias]
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
            }
        }
    }
    dataRowHelper.save(dataRows)
}
