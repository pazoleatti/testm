package form_template.vat.vat_724_4.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * (724.4) Налоговые вычеты за прошедший налоговый период, связанные с изменением условий или расторжением договора,
 * в случае возврата ранее реализованных товаров (отказа от услуг) или возврата соответствующих сумм авансовых платежей
 * (Отчёт по сумме НДС, уплаченного в бюджет, в случае возврата ранее реализованных товаров (отказа от услуг)
 * или возврата соответствующих сумм авансовых платежей) (v.2015).
 *
 * formTemplateId=1603
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

// Группируемые атрибуты (графа 3, 5, 2, 4, 6, 7)
@Field
def sortColumns = ['number', 'number2', 'name', 'sum', 'sum2', 'nds']

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
                def number2Name = getColumnName(row, 'number2')
                def ndsName = getColumnName(row, 'nds')
                def columns = "«$number2Name», «$ndsName»"
                def endMessage = isSection1 ?
                        " Ожидаемое значение (раздел 1): («$number2Name» = «60309.01» и «$ndsName» = «10»/ «18»/ «10/110»/ «18/118») или («$number2Name» = «60309.04»/ «60309.05»  и «$ndsName» = «18/118»)." :
                        " Ожидаемое значение (раздел 2): «$number2Name» = «60309.02»/ «60309.03» и «$ndsName» = «18/118»."
                rowError(logger, row, errorMsg + 'Графы ' + columns + ' заполнены неверно!' + endMessage)
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
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
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
            (xml.row[0].cell[0]): getColumnName(tmpRow, 'rowNum'),
            (xml.row[0].cell[2]): 'Данные бухгалтерского учёта',
            (xml.row[0].cell[7]): getColumnName(tmpRow, 'nds'),

            (xml.row[1].cell[2]): 'Налоговая база',
            (xml.row[1].cell[5]): 'НДС',

            (xml.row[2].cell[2]): 'наименование балансового счёта расхода',
            (xml.row[2].cell[3]): 'номер балансового счёта расхода',
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
        record = getRecordImport(101, 'ACCOUNT', row.cell[3].text(), xlsIndexRow, 3 + colOffset, false)
        newRow.number = record?.record_id?.value

        // Графа 2 - зависит от графы 3 - атрибут 901 - ACCOUNT_NAME - «Наименование балансового счета», справочник 101 «План счетов бухгалтерского учета»
        if (record != null) {
            def value1 = record?.ACCOUNT_NAME?.value?.toString()
            def value2 = row.cell[2].text()
            formDataService.checkReferenceValue(101, value1, value2, xlsIndexRow, 2 + colOffset, logger, false)
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

        sortRows(sectionsRows, sortColumns)
    }

    dataRowHelper.saveSort()
}

void importTransportData() {
    int COLUMN_COUNT = 7
    int TOTAL_ROW_COUNT = 1
    int ROW_MAX = 1000
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\''

    checkBeforeGetXml(ImportInputStream, UploadFileName)

    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    deleteNotFixedRows(dataRows)
    dataRowHelper.save(dataRows)

    String[] rowCells
    int countEmptyRow = 0	// количество пустых строк
    int fileRowIndex = 0    // номер строки в файле
    int rowIndex = 0        // номер строки в НФ
    int totalRowCount = 0   // счетчик кол-ва итогов
    def total = null		// итоговая строка со значениями из тф для добавления
    def mapRows = [:]

    while ((rowCells = reader.readNext()) != null) {
        fileRowIndex++

        def isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
        if (isEmptyRow) {
            if (countEmptyRow > 0) {
                // если встретилась вторая пустая строка, то дальше только строки итогов и ЦП
                totalRowCount++
                // итоговая строка тф
                total = getNewRow(reader.readNext(), COLUMN_COUNT, ++fileRowIndex, ++rowIndex)
                break
            }
            countEmptyRow++
            continue
        }

        // если еще не было пустых строк, то это первая строка - заголовок (пропускается)
        // обычная строка
        if (countEmptyRow != 0 && !addRow(mapRows, rowCells, COLUMN_COUNT, fileRowIndex, ++rowIndex)) {
            break
        }

        // периодически сбрасываем строки
        if (getNewRowCount(mapRows) > ROW_MAX) {
            insertRows(dataRowHelper, mapRows)
            mapRows.clear()
        }
    }
    reader.close()

    // проверка итоговой строки
    if (TOTAL_ROW_COUNT != 0 && totalRowCount != TOTAL_ROW_COUNT) {
        logger.error(ROW_FILE_WRONG, fileRowIndex)
    }

    if (getNewRowCount(mapRows) != 0) {
        insertRows(dataRowHelper, mapRows)
    }

    // сравнение итогов
    if (total) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки в xml)
        def totalColumnsIndexMap = [ 'sum' : 4, 'sum2' : 6 ]

        // итоговая строка для сверки сумм
        def totalTmp = formData.createDataRow()
        totalColumnsIndexMap.keySet().asList().each { alias ->
            totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
        }

        // подсчет итогов
        dataRows.each { row ->
            totalColumnsIndexMap.keySet().asList().each { alias ->
                def value1 = totalTmp.getCell(alias).value
                def value2 = (row.getCell(alias).value ?: BigDecimal.ZERO)
                totalTmp.getCell(alias).setValue(value1 + value2, null)
            }
        }

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = total.getCell(alias).value
            def v2 = totalTmp.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
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

        dataRowHelper.update(lastRow)
    }
    updateIndexes(dataRows)
}

/** Добавляет строку в текущий буфер строк. */
boolean addRow(def mapRows, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells == null) {
        return true
    }
    def newRow = getNewRow(rowCells, columnCount, fileRowIndex, rowIndex)
    if (newRow == null) {
        return false
    }

    // определить раздел по техническому полю и добавить строку в нужный раздел
    sectionIndex = pure(rowCells[8])
    if (mapRows[sectionIndex] == null) {
        mapRows[sectionIndex] = []
    }
    mapRows[sectionIndex].add(newRow)

    return true
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
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    def int colOffset = 1
    def int colIndex

    // графа 3 - атрибут 900 - ACCOUNT - «Номер счета», справочник 101 «План счетов бухгалтерского учета»
    colIndex = 3
    def record101 = getRecordImport(101, 'ACCOUNT', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    newRow.number = record101?.record_id?.value

    // графа 2 - зависит от графы 3 - атрибут 901 - ACCOUNT_NAME - «Наименование счета», справочник 101 «План счетов бухгалтерского учета»
    if (record101 != null) {
        colIndex = 2
        def value1 = pure(rowCells[colIndex])
        def value2 = record101?.ACCOUNT_NAME?.value?.toString()
        formDataService.checkReferenceValue(101, value1, value2, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 4
    colIndex = 4
    newRow.sum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex = 5
    newRow.number2 = pure(rowCells[colIndex])

    // графа 6
    colIndex = 6
    newRow.sum2 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7
    colIndex = 7
    newRow.nds = pure(rowCells[colIndex])

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}

/** Получить количество новых строк в мапе во всех разделах. */
def getNewRowCount(def mapRows) {
    return mapRows.entrySet().sum { entry -> entry.value.size() }
}

/** Вставить данные в нф по разделам. */
def insertRows(def dataRowHelper, def mapRows) {
    sections.each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def dataRows = dataRowHelper.allCached
            def insertIndex = getDataRow(dataRows, getLastRowAlias(section)).getIndex()
            dataRowHelper.insert(copyRows, insertIndex)

            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }
}