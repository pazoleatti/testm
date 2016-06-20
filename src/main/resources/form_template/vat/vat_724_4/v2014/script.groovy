package form_template.vat.vat_724_4.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * (724.4) Отчёт по сумме НДС, уплаченного в бюджет, в случае возврата ранее реализованных товаров (отказа от услуг)
 * или возврата соответствующих сумм авансовых платежей.
 *
 * formTemplateId=603
 */

// графа 1 - rowNum
// графа   - fix
// графа 2 - name       - зависит от графы 3 - атрибут 901 - ACCOUNT_NAME - «Наименование счета», справочник 101 «План счетов бухгалтерского учета»
// графа 3 - number     - атрибут 900 - ACCOUNT - «Номер счета», справочник 101 «План счетов бухгалтерского учета»
// графа 4 - sum
// графа 5 - number2
// графа 6 - sum2
// графа 7 - nds

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
    case FormDataEvent.ADD_ROW :
        addRow()
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
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

// все атрибуты
@Field
def allColumns = ['rowNum', 'fix', 'name', 'number', 'sum', 'number2', 'sum2', 'nds']

// Редактируемые атрибуты (графа 3..8)
@Field
def editableColumns = ['number', 'sum', 'number2', 'sum2', 'nds']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1, 3, 4, 6, 8)
@Field
def nonEmptyColumns = ['number', 'sum', 'number2', 'sum2', 'nds']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4, 6)
@Field
def totalColumns = ['sum', 'sum2']

// Группируемые атрибуты (графа 3, 5)
@Field
def sortColumns = ['number', 'number2']

// список алиасов подразделов
@Field
def sections = ['1', '2']

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

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

//// Кастомные методы

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index
    if (currentDataRow != null) {
        def alias = currentDataRow.getAlias()
        index = currentDataRow.getIndex()
        if (alias == null || alias.startsWith('head')) {
            index++
        }
    } else {
        def lastRow = getDataRow(dataRows, 'total2')
        index = lastRow.getIndex()
    }
    dataRowHelper.insert(getNewRow(), index)
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    autoFillColumns.each {
        newRow.getCell(it).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def section : sections) {
        def firstRow = getDataRow(dataRows, getFirstRowAlias(section))
        def lastRow = getDataRow(dataRows, getLastRowAlias(section))
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

        // отсортировать/группировать
        sortRows(sectionsRows, sortColumns)

        // посчитать итоги по разделам
        def rows = (from <= to ? dataRows[from..to] : [])
        calcTotalSum(rows, lastRow, totalColumns)
    }
    updateIndexes(dataRows)

    dataRowHelper.save(dataRows)

    // Сортировка групп и строк
    sortFormDataRows()
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def isSection1 = false
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            isSection1 = (row.getAlias() == 'head1')
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 3..4. Проверка номера балансового счета (графа 5) по разделам
        if (row.number2 != null && row.nds != null) {
            def logicCheck5 = isSection1 &&
                    ((row.number2 == '60309.01' && row.nds in ['10', '18', '10/110', '18/118']) ||
                            (row.number2 in ['60309.04', '60309.05'] && row.nds == '18/118'))
            def logicCheck6 = (!isSection1 && row.number2 in ['60309.02', '60309.03'] && row.nds == '18/118')
            if (isSection1 ? !logicCheck5 : !logicCheck6) {
                def columns = "«${getColumnName(row, 'number2')}», «${getColumnName(row, 'nds')}»"
                rowError(logger, row, errorMsg + 'Графы ' + columns + ' заполнены неверно!')
            }
        }
    }

    // 2. Проверка итоговых значений по разделам
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, getFirstRowAlias(section))
        def lastRow = getDataRow(dataRows, getLastRowAlias(section))
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])
        def tmpTotal = getTotalRow(sectionsRows, lastRow.getIndex())
        def hasError = false
        totalColumns.each { alias ->
            if (lastRow[alias] != tmpTotal[alias]) {
                logger.error(WRONG_TOTAL, getColumnName(lastRow, alias))
                hasError = true
            }
        }
        if (hasError) {
            break
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить нефиксированные строки
    deleteNotFixedRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(sourceDataRows, dataRows, getFirstRowAlias(section), getLastRowAlias(section))
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
}

// Удалить нефиксированные строки
void deleteNotFixedRows(def dataRows) {
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        updateIndexes(dataRows)
    }
}

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows строки приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def sourceDataRows, def destinationDataRows, def fromAlias, def toAlias) {
    def from = getDataRow(sourceDataRows, fromAlias).getIndex()
    def to = getDataRow(sourceDataRows, toAlias).getIndex() - 1
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getDataRow(destinationDataRows, toAlias).getIndex() - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationDataRows)
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNum'), null, 8, 4)

    checkHeaderSize(xml.row[3].cell.size(), xml.row.size(), 8, 4)

    def headerMapping = [
            (xml.row[0].cell[0]): tmpRow.getCell('rowNum').column.name,
            (xml.row[0].cell[2]): 'Данные бухгалтерского учёта',
            (xml.row[0].cell[7]): tmpRow.getCell('nds').column.name,

            (xml.row[1].cell[2]): 'Налоговая база',
            (xml.row[1].cell[5]): 'НДС',

            (xml.row[2].cell[2]): 'наименование балансового счёта',
            (xml.row[2].cell[3]): 'номер балансового счёта',
            (xml.row[2].cell[4]): 'сумма',
            (xml.row[2].cell[5]): 'номер балансового счёта',
            (xml.row[2].cell[6]): 'сумма',

            (xml.row[3].cell[0]): '1'
    ]

    (2..7).each { index ->
        headerMapping.put((xml.row[3].cell[index]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 4)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def sectionIndex = null
    def mapRows = [:]

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

        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        def firstValue = row.cell[1].text()
        if (firstValue != null && firstValue != '' && firstValue != 'Итого') {
            sectionIndex = firstValue[0]
            mapRows.put(sectionIndex, [])
            continue
        } else if (firstValue == 'Итого') {
            continue
        }

        def newRow = getNewRow()
        newRow.setImportIndex(xlsIndexRow)

        // Графа 3 - атрибут 900 - ACCOUNT - «Номер балансового счета», справочник 101 «План счетов бухгалтерского учета»
        record = getRecordImport(101, 'ACCOUNT', row.cell[3].text(), xlsIndexRow, 3 + colOffset)
        newRow.number = record?.record_id?.value

        // Графа 2 - зависит от графы 3 - атрибут 901 - ACCOUNT_NAME - «Наименование балансового счета», справочник 101 «План счетов бухгалтерского учета»
        if (record != null) {
            def value1 = record?.ACCOUNT_NAME?.value?.toString()
            def value2 = row.cell[2].text()
            formDataService.checkReferenceValue(101, value1, value2, xlsIndexRow, 2 + colOffset, logger, true)
        }

        // графа 4
        newRow.sum = getNumber(row.cell[4].text(), xlsIndexRow, 4 + colOffset)

        // графа 5
        newRow.number2 = row.cell[5].text()

        // графа 6
        newRow.sum2 = getNumber(row.cell[6].text(), xlsIndexRow, 6 + colOffset)

        // графа 7
        newRow.nds = row.cell[7].text()

        mapRows[sectionIndex].add(newRow)
    }

    deleteNotFixedRows(dataRows)

    // копирование данных по разделам
    sections.each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, getLastRowAlias(section)).getIndex() - 1
            dataRows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }

    dataRowHelper.save(dataRows)
}

def getTotalRow(sectionsRows, def index) {
    def newRow = formData.createDataRow()
    totalColumns.each { alias ->
        newRow.getCell(alias).setValue(BigDecimal.ZERO, null)
    }
    for (def row : sectionsRows) {
        totalColumns.each { alias ->
            def value1 = newRow.getCell(alias).value
            def value2 = (row.getCell(alias).value ?: BigDecimal.ZERO)
            newRow[alias] = value1 + value2
        }
    }
    return newRow
}

def getFirstRowAlias(def section) {
    return 'head' + section
}

def getLastRowAlias(def section) {
    return 'total' + section
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 7, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached
    def int rnuIndexRow = 2
    def int colOffset = 1

    def mapRows = [:]

    def totalTmp = formData.createDataRow()
    totalColumns.each { alias ->
        totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
    }

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }
        def newRow = getNewRow()
        newRow.setImportIndex(rnuIndexRow)

        // графа 3 - атрибут 900 - ACCOUNT - «Номер балансового счета», справочник 101 «План счетов бухгалтерского учета»
        rnuIndexCol = 3
        record = getRecordImport(101, 'ACCOUNT', row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        newRow.number = record?.record_id?.value

        // графа 2 - зависит от графы 3 - атрибут 901 - ACCOUNT_NAME - «Наименование балансового счета», справочник 101 «План счетов бухгалтерского учета»
        if (record != null) {
            rnuIndexCol = 2
            def value1 = record?.ACCOUNT_NAME?.value?.toString()
            def value2 = row.cell[rnuIndexCol].text()
            formDataService.checkReferenceValue(101, value1, value2, rnuIndexRow, rnuIndexCol + colOffset, logger, true)
        }

        // графа 4
        rnuIndexCol = 4
        newRow.sum = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 5
        rnuIndexCol = 5
        newRow.number2 = row.cell[rnuIndexCol].text()

        // графа 6
        rnuIndexCol = 6
        newRow.sum2 = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 7
        rnuIndexCol = 7
        newRow.nds = row.cell[rnuIndexCol].text()

        totalColumns.each { alias ->
            def value1 = totalTmp.getCell(alias).value
            def value2 = (newRow.getCell(alias).value ?: BigDecimal.ZERO)
            totalTmp.getCell(alias).setValue(value1 + value2, null)
        }

        // Техническое поле(группа)
        rnuIndexCol = 8
        def sectionIndex = row.cell[rnuIndexCol].text()

        if (mapRows[sectionIndex] == null) {
            mapRows[sectionIndex] = []
        }
        mapRows[sectionIndex].add(newRow)
    }

    deleteNotFixedRows(dataRows)
    // копирование данных по разделам
    sections.each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, getLastRowAlias(section)).getIndex() - 1
            dataRows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }

    // сравнение итогов
    if (xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 4
        rnuIndexCol = 4
        total.sum = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 6
        rnuIndexCol = 6
        total.sum2 = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        def colIndexMap = ['sum' : 4, 'sum2' : 6]

        for (def alias : totalColumns) {
            def v1 = total.getCell(alias).value
            def v2 = totalTmp.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.error(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
                break
            }
        }
    }

    // расчет итогов
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, getFirstRowAlias(section))
        def lastRow = getDataRow(dataRows, getLastRowAlias(section))
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        // посчитать итоги по разделам
        def rows = (from <= to ? dataRows[from..to] : [])
        calcTotalSum(rows, lastRow, totalColumns)
    }
    updateIndexes(dataRows)

    dataRowHelper.save(dataRows)
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def section : sections) {
        def firstRow = getDataRow(dataRows, getFirstRowAlias(section))
        def lastRow = getDataRow(dataRows, getLastRowAlias(section))
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1
        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

        // Массовое разыменование строк НФ
        def columnList = firstRow.keySet().collect{firstRow.getCell(it).getColumn()}
        refBookService.dataRowsDereference(logger, sectionsRows, columnList)

        sortRowsSimple(sectionsRows)
    }

    dataRowHelper.saveSort()
}
