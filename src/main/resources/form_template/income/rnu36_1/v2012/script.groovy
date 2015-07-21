package form_template.income.rnu36_1.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1".
 * formTemplateId=333
 *
 * @author rtimerbaev
 */

// графа 1  - series
// графа 2  - amount
// графа 3  - nominal
// графа 4  - shortPositionDate
// графа 5  - balance2              атрибут 350 - NUMBER - "Номер", справочник 28 "Классификатор доходов Сбербанка России для целей налогового учёта"
// графа 6  - averageWeightedPrice
// графа 7  - termBondsIssued
// графа 8  - percIncome

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
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

// все атрибуты
@Field
def allColumns = ['series', 'amount', 'nominal', 'shortPositionDate', 'balance2', 'averageWeightedPrice', 'termBondsIssued', 'percIncome']

// Редактируемые атрибуты (графа 1..7)
@Field
def editableColumns = ['series', 'amount', 'nominal', 'shortPositionDate', 'balance2', 'averageWeightedPrice', 'termBondsIssued']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1..8)
@Field
def nonEmptyColumns = ['series', 'amount', 'nominal', 'shortPositionDate', 'balance2', 'averageWeightedPrice', 'termBondsIssued', 'percIncome']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 2, 8)
@Field
def totalColumns = ['amount', 'percIncome']

@Field
def providerCache = [:]

@Field
def recordCache = [:]

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

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

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index

    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'totalA').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex()
    } else {
        index = getDataRow(dataRows, 'totalA').getIndex()
        switch (currentDataRow.getAlias()) {
            case 'A':
            case 'totalA':
                index = getDataRow(dataRows, 'totalA').getIndex()
                break
            case 'B':
            case 'totalB':
            case 'total':
                index = getDataRow(dataRows, 'totalB').getIndex()
                break
        }
    }
    dataRowHelper.insert(getNewRow(), index)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // последний день отчетного месяца
    def lastDay = getReportPeriodEndDate()

    dataRows.each { row ->
        if (row.getAlias() == null) {
            // графа 8
            row.percIncome = calcPercIncome(row, lastDay)
        }
    }

    // расчет итогов
    calcTotalRows(dataRows)

    sortFormDataRows(false)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // последний день отчетного месяца
    def lastDay = getReportPeriodEndDate()
    // отчетная дата
    def reportDay = reportPeriodService.getMonthReportDate(formData.reportPeriodId, formData.periodOrder)?.time

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // . Проверка обязательных полей (графа 2..8)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 1. Проверка даты приобретения (открытия короткой позиции) (графа 4)
        if (row.shortPositionDate > reportDay) {
            logger.error(errorMsg + 'Неверно указана дата приобретения (открытия короткой позиции)!')
        }

        // 2. Арифметическая проверка графы 8
        def needValue = [:]
        needValue.percIncome = calcPercIncome(row, lastDay)
        checkCalc(row, ['percIncome'], needValue, logger, false)
    }

    // 3. Проверка итоговых значений по разделу А
    def rowA = getDataRow(dataRows, 'A')
    def totalRowA = getDataRow(dataRows, 'totalA')
    for (def alias : totalColumns) {
        def value = roundValue(totalRowA.getCell(alias).value, 6)
        def tmpValue = roundValue(getSum(dataRows, rowA, totalRowA, alias), 6)
        if (value != tmpValue) {
            logger.error("Итоговые значений для раздела A рассчитаны неверно!")
            break
        }
    }

    // 4. Проверка итоговых значений по разделу Б
    def totalRowB = getDataRow(dataRows, 'totalB')
    def rowB = getDataRow(dataRows, 'B')
    for (def alias : totalColumns) {
        def tmpB = getSum(dataRows, rowB, totalRowB, alias)
        if (totalRowB.getCell(alias).value != tmpB) {
            logger.error("Итоговые значений для раздела Б рассчитаны неверно!")
            break
        }
    }

    // 5. Проверка итоговых значений по всей форме
    def total = (totalRowA.percIncome ?: 0) - (totalRowB.percIncome ?: 0)
    if (getDataRow(dataRows, 'total').percIncome != total) {
        logger.error('Итоговые значения рассчитаны неверно!')
    }
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить нефиксированные строки
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        // поправить индексы, потому что они после изменения не пересчитываются
        updateIndexes(dataRows)
    }

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allSaved
                copyRows(sourceDataRows, dataRows, 'A', 'totalA')
                copyRows(sourceDataRows, dataRows, 'B', 'totalB')
            }
        }
    }
}

// Посчитать значение графы 8
def calcPercIncome(def row, def lastDay) {
    if (row.termBondsIssued == null || row.termBondsIssued == 0 ||
            lastDay == null || row.nominal == null || row.averageWeightedPrice == null ||
            row.shortPositionDate == null || row.amount == null) {
        return null
    }
    def tmp = ((row.nominal - row.averageWeightedPrice) *
            (lastDay - row.shortPositionDate) / row.termBondsIssued) * row.amount
    return roundValue(tmp, 2)
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
    if (from > to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getDataRow(destinationDataRows, toAlias).getIndex() - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationDataRows)
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    autoFillColumns.each {
        newRow.getCell(it).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/**
 * Получить сумму для указанной графы.
 *
 * @param dataRows строки
 * @param labelRow строка начала раздела
 * @param totalRow строка итогов раздела
 * @param alias алиас графы для которой суммируются данные
 */
def getSum(def dataRows, def labelRow, def totalRow, def alias) {
    def from = labelRow.getIndex()
    def to = totalRow.getIndex() - 2
    // если нет строк в разделе то в итоги 0
    if (from > to) {
        return 0
    }
    return ((BigDecimal)summ(formData, dataRows, new ColumnRange(alias, from, to))).setScale(2, BigDecimal.ROUND_HALF_UP)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 8, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rowsA = []
    def rowsB = []
    def totalTmp = formData.createDataRow()
    totalColumns.each { alias ->
        totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
    }

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def rnuIndexCol
        def newRow = getNewRow()

        // графа 1
        rnuIndexCol = 1
        newRow.series = row.cell[rnuIndexCol].text()

        // графа 2
        rnuIndexCol = 2
        newRow.amount = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 3
        rnuIndexCol = 3
        newRow.nominal = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 4
        rnuIndexCol = 4
        newRow.shortPositionDate = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        //  графа 5
        rnuIndexCol = 5
        newRow.balance2 = getRecordIdImport(28L, 'NUMBER', row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, false)

        // графа 6..8
        ['averageWeightedPrice', 'termBondsIssued', 'percIncome'].each { alias ->
            rnuIndexCol++
            newRow[alias] = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)
        }

        totalColumns.each { alias ->
            def value1 = totalTmp.getCell(alias).value
            def value2 = (newRow.getCell(alias).value ?: BigDecimal.ZERO)
            totalTmp.getCell(alias).setValue(value1 + value2, null)
        }

        // раздел
        rnuIndexCol = 9
        def section = row.cell[rnuIndexCol].text()
        if (section == 'А') {
            rowsA.add(newRow)
        } else {
            rowsB.add(newRow)
        }
    }

    def dataRows = formDataService.getDataRowHelper(formData).allCached
    dataRows = dataRows.grep { row -> row.getAlias() != null }
    updateIndexes(dataRows)

    def indexA = getDataRow(dataRows, 'A').getIndex()
    dataRows.addAll(indexA, rowsA)
    updateIndexes(dataRows)

    def indexB = getDataRow(dataRows, 'B').getIndex()
    dataRows.addAll(indexB, rowsB)
    updateIndexes(dataRows)

    // сравнение итогов
    if (xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 2
        rnuIndexCol = 2
        total.amount = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 8
        rnuIndexCol = 8
        total.percIncome = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        def colIndexMap = ['amount' : 2, 'percIncome' : 8]

        for (def alias : totalColumns) {
            def v1 = total.getCell(alias).value
            def v2 = totalTmp.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
            }
        }

    }
    calcTotalRows(dataRows)
}

// Расчет итогов
void calcTotalRows(def dataRows) {
    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    def rowA = getDataRow(dataRows, 'A')
    def rowB = getDataRow(dataRows, 'B')
    def totalRowA = getDataRow(dataRows, 'totalA')
    def totalRowB = getDataRow(dataRows, 'totalB')
    totalColumns.each { alias ->
        totalRowA.getCell(alias).setValue(getSum(dataRows, rowA, totalRowA, alias), null)
        totalRowB.getCell(alias).setValue(getSum(dataRows, rowB, totalRowB, alias), null)
    }
    // посчитать Итого
    getDataRow(dataRows, 'total').percIncome = (totalRowA.percIncome ?: 0) - (totalRowB.percIncome ?: 0)
}


void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def section : ['A', 'B']) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, "total$section")
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1
        def sectionRows = (from < to ? dataRows[from..(to - 1)] : [])

        // Массовое разыменовывание граф НФ
        def columnNameList = firstRow.keySet().collect{firstRow.getCell(it).getColumn()}
        refBookService.dataRowsDereference(logger, sectionRows, columnNameList)

        sortRowsSimple(sectionRows)
    }

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 8
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'series')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

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

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def rows = formTemplate.rows
    def headerARow = getDataRow(rows, 'A')
    def totalARow = getDataRow(rows, 'totalA')
    def headerBRow = getDataRow(rows, 'B')
    def totalBRow = getDataRow(rows, 'totalB')
    def totalRow = getDataRow(rows, 'total')

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def allValuesCount = allValues.size()

    def sectionAlias = ''
    def mapRows = [:]
    def sectionAEnd = false
    def sectionBEnd = false

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
        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        def firstValue = rowValues[INDEX_FOR_SKIP]
        if (firstValue in [headerARow.series, headerBRow.series]) {
            sectionAlias = (firstValue == headerARow.series ? headerARow.getAlias() : headerBRow.getAlias())
            mapRows.put(sectionAlias, [])
            allValues.remove(rowValues)
            rowValues.clear()
            // проверка надписей начал разделов
            if (fileRowIndex == 1 && firstValue != headerARow.series ||
                    sectionAEnd && firstValue != headerBRow.series) {
                logger.error("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
            }
            continue
        } else if (firstValue in [totalARow.series, totalBRow.series, totalRow.series]) {
            // проверка итогов разделов
            if (sectionAlias && !sectionBEnd && firstValue != getDataRow(rows, "total$sectionAlias").series) {
                logger.error("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
            }
            if (sectionBEnd && firstValue != totalRow.series) {
                logger.error("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
            }
            if (!sectionAEnd && firstValue == totalARow.series) {
                sectionAEnd = true
            }
            if (!sectionBEnd && firstValue == totalBRow.series) {
                sectionBEnd = true
            }
            // Пропуск итоговых строк
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        mapRows[sectionAlias].add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // копирование данных по разделам
    updateIndexes(rows)
    [headerARow.getAlias(), headerBRow.getAlias()].each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(rows, 'total' + section).getIndex() - 1
            rows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(rows)
        }
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = rows
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
            (headerRows[0][0]): getColumnName(tmpRow, 'series'),
            (headerRows[0][1]): getColumnName(tmpRow, 'amount'),
            (headerRows[0][2]): getColumnName(tmpRow, 'nominal'),
            (headerRows[0][3]): getColumnName(tmpRow, 'shortPositionDate'),
            (headerRows[0][4]): getColumnName(tmpRow, 'balance2'),
            (headerRows[0][5]): getColumnName(tmpRow, 'averageWeightedPrice'),
            (headerRows[0][6]): getColumnName(tmpRow, 'termBondsIssued'),
            (headerRows[0][7]): getColumnName(tmpRow, 'percIncome')
    ]
    (1..8).each { index ->
        headerMapping.put(headerRows[1][index - 1], index.toString())
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
    newRow.series = values[colIndex]

    // графа 2
    colIndex++
    newRow.amount = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 3
    colIndex++
    newRow.nominal = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 4
    colIndex++
    newRow.shortPositionDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex++
    newRow.balance2 = getRecordIdImport(28L, 'NUMBER', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // графа 6..8
    ['averageWeightedPrice', 'termBondsIssued', 'percIncome'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}