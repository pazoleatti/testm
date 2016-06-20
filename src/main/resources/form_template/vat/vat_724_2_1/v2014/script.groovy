package form_template.vat.vat_724_2_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * (724.2.1) Операции, не подлежащие налогообложению (освобождаемые от налогообложения), операции, не признаваемые объектом
 * налогообложения, операции по реализации товаров (работ, услуг), местом реализации которых не признается территория
 * Российской Федерации, а также суммы оплаты, частичной оплаты в счет предстоящих поставок (выполнения работ,
 * оказания услуг), длительность производственного цикла изготовления которых составляет свыше шести месяцев
 *
 * formTemplateId=601
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
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
}

// Проверяемые на пустые значения атрибуты (группа 1..5)
@Field
def nonEmptyColumns = ['code', 'name', 'realizeCost', 'obtainCost']

// Поля, для которых подсчитываются итоговые значения (графа 4, 5)
@Field
def totalColumns = ['realizeCost', 'obtainCost']

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

        // 1. Проверка заполнения граф (по графе 5 обязательны тока строки 8, 9 и 13)
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
        def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
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
            (xml.row[0].cell[0]): tmpRow.getCell('rowNum').column.name,
            (xml.row[0].cell[1]): tmpRow.getCell('code').column.name,
            (xml.row[0].cell[2]): tmpRow.getCell('name').column.name,
            (xml.row[0].cell[3]): tmpRow.getCell('realizeCost').column.name,
            (xml.row[0].cell[4]): tmpRow.getCell('obtainCost').column.name
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
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 5, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def int rnuIndexRow = 2
    def int colOffset = 1

    dataRows.each {
        it.realizeCost = null
        it.realizeCost = null
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
        dataRow.realizeCost = parseNumber(row.cell[4].text(), rnuIndexRow, 4 + colOffset, logger, true)

        // графа 5
        dataRow.obtainCost = parseNumber(row.cell[5].text(), rnuIndexRow, 5 + colOffset, logger, true)
    }
    def itogValues = calcItog(dataRows)
    def totalRow = getDataRow(dataRows, 'itog')
    totalColumns.each { alias ->
        totalRow[alias] = itogValues[alias]
    }

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow += 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // графа 4
        total.realizeCost = parseNumber(row.cell[4].text(), rnuIndexRow, 4 + colOffset, logger, true)
        // графа 5
        total.obtainCost = parseNumber(row.cell[5].text(), rnuIndexRow, 5 + colOffset, logger, true)

        def colIndexMap = ['realizeCost' : 4, 'obtainCost' : 5]
        for (def alias : totalColumns) {
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