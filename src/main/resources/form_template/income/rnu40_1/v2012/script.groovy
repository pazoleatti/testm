package form_template.income.rnu40_1.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * (РНУ-40.1) Регистр налогового учёта начисленного процентного дохода по прочим дисконтным облигациям. Отчёт 1
 * formTemplateId=338
 *
 * @author auldanov
 */

// графа    - fix
// графа 1  - number                - зависит от графы 2 - атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
// графа 2  - name                  - атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
// графа 3  - issuer                - зависит от графы 4 - атрибут 809 - ISSUER - «Эмитент», справочника 84 «Ценные бумаги»
// графа 4  - registrationNumber    - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочника 84 «Ценные бумаги»
// графа 5  - buyDate
// графа 6  - cost
// графа 7  - bondsCount
// графа 8  - upCost
// графа 9  - circulationTerm
// графа 10 - percent
// графа 11 - currencyCode          - зависит от графы 4 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочника 84 «Ценные бумаги»

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

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// все атрибуты
@Field
def allColumns = ['fix', 'number', 'name', 'issuer', 'registrationNumber', 'buyDate', 'cost',
                  'bondsCount', 'upCost', 'circulationTerm', 'percent', 'currencyCode']

// Редактируемые атрибуты (графа 2, 4..9)
@Field
def editableColumns = ['name', 'registrationNumber', 'buyDate', 'cost', 'bondsCount', 'upCost', 'circulationTerm']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 2, 4..10)
@Field
def nonEmptyColumns = ['name', 'registrationNumber', 'buyDate', 'cost', 'bondsCount', 'upCost', 'circulationTerm', 'percent']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 7, 10)
@Field
def totalSumColumns = ['bondsCount', 'percent']

// список алиасов подразделов
@Field
def sections = ['1', '2', '3', '4', '5', '6', '7', '8']

@Field
def taxPeriod = null

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

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
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

// Получить дату по строковому представлению (формата дд.ММ.гггг)
def getDate(def value, def indexRow, def indexCol) {
    return parseDate(value, 'dd.MM.yyyy', indexRow, indexCol + 1, logger, true)
}

// Добавить новую строку.
def addRow() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def index

    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'total1').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex() + 1
    } else {
        def alias = currentDataRow.getAlias()
        if (alias.contains('total')) {
            index = getDataRow(dataRows, alias).getIndex()
        } else {
            index = getDataRow(dataRows, 'total' + alias).getIndex()
        }
    }
    dataRows.add(index + 1, getNewRow())
    formDataService.saveCachedDataRows(formData, logger)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // отсортировать/группировать
    sort(dataRows)

    def lastDay = getReportPeriodEndDate()
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // графа 10
        row.percent = calc10(row, lastDay)
    }

    calcTotal(dataRows)

    // Сортировка групп и строк
    sortFormDataRows(false)
}

void calcTotal(def dataRows) {
    // посчитать итоги по разделам
    sections.each { section ->
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        totalSumColumns.each { alias ->
            lastRow.getCell(alias).setValue(getSum(dataRows, alias, firstRow, lastRow), null)
        }
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def lastDay = getReportPeriodEndDate()
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        // 1. Обязательность заполнения полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Арифметическая проверка графы 10
        def needValue = [:]
        needValue['percent'] = calc10(row, lastDay)
        checkCalc(row, ['percent'], needValue, logger, true)
    }

    // 3. Арифметическая проверка строк промежуточных итогов (графа 7, 10)
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        for (def col : totalSumColumns) {
            def value = roundValue(lastRow.getCell(col).value ?: 0, 6)
            def sum = roundValue(getSum(dataRows, col, firstRow, lastRow), 6)
            if (sum != value) {
                def name = lastRow.getCell(col).column.name
                logger.error("Неверно рассчитаны итоговые значения для раздела $section в графе \"$name\"!")
            }
        }
    }
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    deleteNotFixedRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allSaved
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(sourceDataRows, dataRows, section, 'total' + section)
                }
            }
        }
    }
    updateIndexes(rows)
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

// Получить новую стролу с заданными стилями.
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

// Отсорировать данные (по графе 1, 2).
void sort(def dataRows) {
    // список со списками строк каждого раздела для сортировки
    def sortRows = []

    // подразделы, собрать список списков строк каждого раздела
    sections.each { section ->
        def from = getDataRow(dataRows, section).getIndex()
        def to = getDataRow(dataRows, 'total' + section).getIndex() - 1
        if (from < to) {
            sortRows.add(dataRows.subList(from, to))
        }
    }

    // отсортировать строки каждого раздела
    sortRows.each { sectionRows ->
        sectionRows.sort { def a, def b ->
            // графа 1  - number (справочник)
            // графа 2  - name (справочник)
            def recordA = getRefBookValue(30, a.name)
            def recordB = getRefBookValue(30, b.name)
            def numberA = recordA?.SBRF_CODE?.value
            def numberB = recordB?.SBRF_CODE?.value
            if (numberA == numberB) {
                def nameA = recordA?.NAME?.value
                def nameB = recordB?.NAME?.value
                return nameA <=> nameB
            }
            return numberA <=> numberB
        }
    }
}

// Получить сумму столбца.
def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
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
 * Получить значение для графы 10.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 */
def calc10(def row, def lastDay) {
    if (row.buyDate == null || row.cost == null || row.bondsCount == null || row.upCost == null ||
            row.circulationTerm == null || row.upCost == null || row.bondsCount == 0 || row.circulationTerm == 0) {
        return null
    }
    def tmp
    tmp = ((row.cost / row.bondsCount) - row.upCost) * ((lastDay - row.buyDate) / row.circulationTerm) * row.bondsCount
    tmp = roundValue(tmp, 2)

    def rate = getRate(row, lastDay)
    if (rate == null) {
        return null
    }
    return roundValue(tmp * rate, 2)
}

def getTaxPeriod() {
    if (taxPeriod == null) {
        taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod
    }
    return taxPeriod
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

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 11, 0)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def int rnuIndexRow = 2
    def int colOffset = 1

    def mapRows = [:]

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }
        def newRow = getNewRow()
        newRow.setImportIndex(rnuIndexRow)

        // графа 2 - атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
        def xmlIndexCol = 2
        def record30 = getRecordImport(30, 'NAME', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, false)
        newRow.name = record30?.record_id?.value

        // графа 1 - зависит от графы 2 - атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
        if (record30 != null) {
            xmlIndexCol = 1
            formDataService.checkReferenceValue(30, row.cell[xmlIndexCol].text(), record30?.SBRF_CODE?.value, rnuIndexRow, xmlIndexCol + colOffset, logger, false)
        }

        // графа 4 - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочник 84 «Ценные бумаги»
        // TODO (Ramil Timerbaev) могут быть проблемы с нахождением записи,
        // если в справочнике 84 есть несколько записей с одинаковыми значениями в поле REG_NUM
        xmlIndexCol = 4
        def record84 = getRecordImport(84, 'REG_NUM', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, false)
        newRow.registrationNumber = record84?.record_id?.value

        // графа 3 - зависит от графы 4 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
        xmlIndexCol = 3
        def record100 = getRecordImport(100, 'FULL_NAME', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, false)
        if (record84 != null && record100 != null) {
            def value1 = record100?.record_id?.value?.toString()
            def value2 = record84?.ISSUER?.value?.toString()
            formDataService.checkReferenceValue(84, value1, value2, rnuIndexRow, xmlIndexCol + colOffset, logger, false)
        }

        // графа 11 - зависит от графы 4 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
        xmlIndexCol = 11
        def record15 = getRecordImport(15, 'CODE', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, false)
        if (record84 != null && record15 != null) {
            def value1 = record15?.record_id?.value?.toString()
            def value2 = record84?.CODE_CUR?.value?.toString()
            formDataService.checkReferenceValue(84, value1, value2, rnuIndexRow, xmlIndexCol + colOffset, logger, false)
        }

        // графа 5
        xmlIndexCol = 5
        newRow.buyDate = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 6..10
        ['cost', 'bondsCount', 'upCost', 'circulationTerm', 'percent'].each { alias ->
            xmlIndexCol++
            newRow[alias] = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        }

        // Техническое поле(группа)
        xmlIndexCol = 12
        sectionIndex = row.cell[xmlIndexCol].text()

        if (mapRows[sectionIndex] == null) {
            mapRows[sectionIndex] = []
        }
        mapRows[sectionIndex].add(newRow)
    }

    deleteNotFixedRows(dataRows)
    dataRows.each { row ->
        if (row.getAlias()?.contains('total')) {
            totalSumColumns.each {
                row[it] = null
            }
        }
    }

    // копирование данных по разделам
    sections.each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, 'total' + section).getIndex() - 1
            dataRows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }

    calcTotal(dataRows)
}

// Получить курс валюты по id записи из справочнкиа ценной бумаги (84)
def getRate(def row, def lastDay) {
    if (row.registrationNumber == null) {
        return null
    }
    // получить запись (поле Цифровой код валюты выпуска) из справочника ценные бумаги (84) по id записи
    def record15Id = getRefBookValue(84, row.registrationNumber)?.CODE_CUR?.value

    def code = getRefBookValue(15, record15Id)?.CODE?.value
    if (code in ['810', '643']) {
        return 1
    }

    // получить запись (поле курс валюты) из справочника курс валют (22) по цифровому коду валюты
    def record22 = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache, 'CODE_NUMBER',
            record15Id?.toString(), lastDay, row.getIndex(), getColumnName(row, 'currencyCode'), logger, true)
    return record22?.RATE?.value
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def section : sections) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1
        def sectionsRows = (from < to ? dataRows.subList(from, to) : [])

        // Массовое разыменование строк НФ
        def columnList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
        refBookService.dataRowsDereference(logger, sectionsRows, columnList)

        sortRowsSimple(sectionsRows)
    }

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 12
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = tmpRow.getCell('number').column.name
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
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

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    def templateRows = formTemplate.rows

    def rowIndex = 0
    def allValuesCount = allValues.size()
    def sectionIndex = null
    def mapRows = [:]
    def totalRowFromFileMap = [:]

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
        if (firstValue != null && firstValue != '' && firstValue != 'Всего') {
            sectionIndex = firstValue[0]
            mapRows.put(sectionIndex, [])

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (firstValue == 'Всего') {
            rowIndex++
            totalRowFromFileMap[sectionIndex] = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        mapRows[sectionIndex].add(newRow)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // копирование данных по разделам
    updateIndexes(templateRows)
    def rows = []
    sections.each { section ->
        def headRow = getDataRow(templateRows, section)
        def totalRow = getDataRow(templateRows, 'total' + section)
        rows.add(headRow)
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            rows.addAll(copyRows)
        }
        rows.add(totalRow)

        // сравнение итогов
        updateIndexes(rows)
        def totalRowFromFile = totalRowFromFileMap[section]
        compareSimpleTotalValues(totalRow, totalRowFromFile, copyRows, totalSumColumns, formData, logger, false)
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
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]) : tmpRow.getCell('number').column.name]),
            ([(headerRows[0][2]) : tmpRow.getCell('name').column.name]),
            ([(headerRows[0][3]) : tmpRow.getCell('issuer').column.name]),
            ([(headerRows[0][4]) : tmpRow.getCell('registrationNumber').column.name]),
            ([(headerRows[0][5]) : tmpRow.getCell('buyDate').column.name]),
            ([(headerRows[0][6]) : tmpRow.getCell('cost').column.name]),
            ([(headerRows[0][7]) : tmpRow.getCell('bondsCount').column.name]),
            ([(headerRows[0][8]) : tmpRow.getCell('upCost').column.name]),
            ([(headerRows[0][9]) : tmpRow.getCell('circulationTerm').column.name]),
            ([(headerRows[0][10]): tmpRow.getCell('percent').column.name]),
            ([(headerRows[0][11]): tmpRow.getCell('currencyCode').column.name])
    ]
    (1..11).each {
        headerMapping.add(([(headerRows[1][it]): it.toString()]))
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

    // графа 2 - атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
    def colIndex = 2
    def record30 = getRecordImport(30, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    newRow.name = record30?.record_id?.value

    // графа 1 - зависит от графы 2 - атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
    if (record30 != null) {
        colIndex = 1
        formDataService.checkReferenceValue(30, values[colIndex], record30?.SBRF_CODE?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 4 - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочник 84 «Ценные бумаги»
    // TODO (Ramil Timerbaev) могут быть проблемы с нахождением записи,
    // если в справочнике 84 есть несколько записей с одинаковыми значениями в поле REG_NUM
    colIndex = 4
    def record84 = getRecordImport(84, 'REG_NUM', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    newRow.registrationNumber = record84?.record_id?.value

    // графа 3 - зависит от графы 4 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
    colIndex = 3
    def record100 = getRecordImport(100, 'FULL_NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    if (record84 != null && record100 != null) {
        def value1 = record100?.record_id?.value?.toString()
        def value2 = record84?.ISSUER?.value?.toString()
        formDataService.checkReferenceValue(84, value1, value2, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 11 - зависит от графы 4 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
    colIndex = 11
    def record15 = getRecordImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    if (record84 != null && record15 != null) {
        def value1 = record15?.record_id?.value?.toString()
        def value2 = record84?.CODE_CUR?.value?.toString()
        formDataService.checkReferenceValue(84, value1, value2, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 5
    colIndex = 5
    newRow.buyDate = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 6..10
    ['cost', 'bondsCount', 'upCost', 'circulationTerm', 'percent'].each { alias ->
        colIndex++
        newRow[alias] = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    }

    return newRow
}