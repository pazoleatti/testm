package form_template.deal.journal_settlements_tb.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

/**
 * Журнал взаиморасчетов по ТБ.
 *
 * formTemplateId=853
 *
 * Форма с фиксированными разделами. Каждый раздел имеет заголовок, в котором содержится еще и сумма.
 * Загрзука из экселя нестандартная: эксель не из нашей системы, а сформированный другой системой, формат xls, поэтому используется старая загрузка.
 */

// графа 1 (1.1) - rowNum
// графа 2 (1.2) - name
// графа 3 (2.1) - statReportId  - атрибут 5216 - STATREPORT_ID - «ИД в АС "Статотчетность"», справочник 520 «Участники ТЦО»
// графа 4 (2.2) - orgName       - зависит от графы 3 - атрибут 5201 - NAME - «Полное наименование юридического лица с указанием ОПФ», справочник 520 «Участники ТЦО»
// графа 5 (3)   - currency      - атрибут 66 - NAME - «Наименование», справочник 15 «Общероссийский классификатор валют»
// графа 6 (4.1) - currencySum
// графа 7 (4.2) - sum
// графа 8 (5)   - otherSum
// графа 9 (6)   - description

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_LOAD:
        afterLoad()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def allColumns = ['rowNum', 'name', 'statReportId', 'orgName', 'currency', 'currencySum', 'sum', 'otherSum', 'description']

// Редактируемые атрибуты (графа 3, 5..9)
@Field
def editableColumns = ['statReportId', 'currency', 'currencySum', 'sum', 'otherSum', 'description']

// Автозаполняемые атрибуты (графа 1, 2, 5)
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 3..7)
@Field
def nonEmptyColumns = ['statReportId', /* 'orgName', */ 'currency', 'currencySum', 'sum']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 7)
@Field
def totalColumns = ['sum']

@Field
def calcRowAliases = [
        '01', '02', '03', '04', '05', '05.1', '05.2', '05.3', '06', '06.1', '06.2', '06.3', '06.3', '06.4', '06.5',
        '99.1',
        '20', '21', '22', '23', '23.1', '23.2', '23.3', '24', '24.1', '24.2', '24.3', '24.4', '24.5',
        '99.2',
        '30', '31', '32', '40', '50', '51'
]

@Field
def titleRowAliases = ['header1', 'subHeader1', 'header2', 'subHeader2', 'subHeader3']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = false) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

def addRow() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def index
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        // если строка не выбрана, то вставить в конец
        index = dataRows.size()
    } else if (currentDataRow.getAlias() in ['99.1', '99.2']) {
        // если строка итоговая, то добавить перед итоговой
        index = currentDataRow.getIndex() - 1
    } else if (currentDataRow.getAlias() in titleRowAliases) {
        // если строка заголовка раздела или подзаголовка, то добавить в следующий раздел
        for (index = currentDataRow.getIndex(); index < dataRows.size(); ) {
            if (!dataRows[index].getAlias()?.contains('subHeader')) {
                index++
                break
            }
            index++
        }
    } else if (currentDataRow.getAlias() in ['05', '06', '23', '24']) {
        index = currentDataRow.getIndex() + 1
    } else {
        index = currentDataRow.getIndex()
    }
    dataRows.add(index, getNewRow())
    formDataService.saveCachedDataRows(formData, logger)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    calcRowAliases.each { alias ->
        def row = getDataRow(dataRows, alias)
        row.sum = calc7(alias, dataRows)
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // пропускать строки с заголовками и надписями
        if (row.getAlias() && row.getAlias() in titleRowAliases) {
            continue
        }

        // 1. Проверка заполнения обязательных полей
        if (row.getAlias() && row.getAlias() in calcRowAliases) {
            checkNonEmptyColumns(row, row.getIndex(), ['sum'], logger, true)
          } else {
            // обыячные строки
            checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
        }
    }

    // 2. Проверка итоговых значений по разделам
    def tmpRow = formData.createStoreMessagingDataRow()
    calcRowAliases.each { alias ->
        def row = getDataRow(dataRows, alias)
        tmpRow.sum = calc7(alias, dataRows)
        if (isDiffRow(row, tmpRow, totalColumns)) {
            logger.error("Строка %s: " + WRONG_TOTAL, row.getIndex(), getColumnName(row, 'sum'))
        }
    }
}

// мапа строк для суммирования (алиас строки -> список строк для суммирования по графе 7)
@Field
def rowsForSumMap = [
        '05'   : ['05.1', '05.2', '05.3'],
        '06'   : ['06.1', '06.2', '06.3', '06.4', '06.5'],
        '99.1' : ['01', '02', '03', '04', '05', '06'],
        '23'   : ['23.1', '23.2', '23.3'],
        '24'   : ['24.1', '24.2', '24.3', '24.4', '24.5'],
        '99.2' : ['20', '21', '22', '23', '24'],
]

/**
 * Получить сумму по графе 7 для указанного алиаса строки
 *
 * @param rowAlias алиас строки
 * @param rows строки нф
 */
def calc7(def rowAlias, def rows) {
    def result = 0
    def rowsForSum = rowsForSumMap[rowAlias]
    if (rowsForSum == null) {
        // сумма только по своему разделу
        result = calcSome(rowAlias, rows)
    } else {
        // сумма по нескольким разделам
        rowsForSum.each {
            result += calc7(it, rows)
        }
    }
    return result
}

/**
 * Получить сумму по графе 7 только по строкам раздела указанного алиаса
 *
 * @param rowAlias алиас строки
 * @param rows строки нф
 */
def calcSome(def rowAlias, def rows) {
    def result = 0
    def findSection = false
    for (def row : rows) {
        // найден нужный раздел - начать суммировать
        if (row.getAlias() == rowAlias) {
            findSection = true
            continue
        }
        // закончился нужный раздел - выход
        if (findSection && row.getAlias()) {
            break
        }
        // суммировать для нужного раздела
        if (findSection) {
            result += (row.sum ?: 0)
        }
    }
    return result
}

// Получить новую строку
def getNewRow() {
    def row = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return row
}

// Загрузка из экселя НЕстандартная, грузится файл специального вида, выгруженный из другой системы
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 9
    int HEADER_ROW_COUNT = 15
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    readFile(allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, COLUMN_COUNT, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    def templateRows = formTemplate.rows

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def allValuesCount = allValues.size()
    def sectionIndex = null
    def mapRows = [:]
    def isSection51 = false
    def isData = false

    def sectionRowMap = [:]
    templateRows.each { row ->
        if (row.getAlias() in calcRowAliases) {
            sectionRowMap[row.getAlias()] = row
        }
    }

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        def rowValues = allValues[0]
        fileRowIndex++

        // пропускать пустые строки
        if (!rowValues || rowValues.isEmpty() || !rowValues.find { it }) {
            allValues.remove(rowValues)
            rowValues.clear()
            isData = !isData
            continue
        }

        def sectionValue = rowValues[INDEX_FOR_SKIP]

        // после раздела 51 прекращать загрузку если пустая строка, если надпись начинается с "*"
        if (isSection51 && sectionValue && (sectionValue[0] == '*')) {
            break
        }
        rowIndex++
        isData = (isData && sectionRowMap[sectionValue] == null)

        // простая строка
        if (isData) {
            // пропускать строки, в которых не заполнены графа 3..9
            def isNotEmpty = rowValues[2..8].find { it }
            if (isNotEmpty) {
                def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
                mapRows[sectionIndex].add(newRow)
            }
            // освободить ненужные данные - иначе не хватит памяти
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }

        // если новый раздел, то взять его сумму
        if (!sectionValue) {
            // фиксированная строка с надписью
            def row = templateRows.find {
                def isEquals = rowValues[1] == it.name
                def tmpFromFile = rowValues[1]?.replaceAll('\\*', '')
                def tmpFromTemplate = it.name?.replaceAll(':', '')
                def isContains = tmpFromFile?.contains(tmpFromTemplate)
                return isEquals || isContains
            }
            if (!row) {
                logger.error("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
            }
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (sectionValue && sectionRowMap[sectionValue]) {
            // фискированная строка с суммой
            def row = sectionRowMap[sectionValue]
            def colIndex = 6
            row.sum = parseNumber(rowValues[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
            row.setImportIndex(fileRowIndex)

            sectionIndex = row?.rowNum
            mapRows.put(sectionIndex, [])

            if (sectionValue == '51') {
                isSection51 = true
            }
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }

        // строка не определена, должна быть:
        //  - пустая строка
        //  - простая строка (с пустой строкой до и после блока с данными; если данных в разделе нет, то пустых строк быть не должно)
        //  - строка с надписями (заголовки)
        //  - строка с итоговыми значениями и наименованиями статей
        logger.error("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // копирование данных по разделам
    def rows = []
    templateRows.each { row ->
        rows.add(row)
        if (row.rowNum && mapRows[row.rowNum]) {
            rows.addAll(mapRows[row.rowNum])
        }
    }
    updateIndexes(rows)

    // сравнение итогов
    calcRowAliases.each { alias ->
        def row = getDataRow(rows, alias)
        tmpRow.sum = calc7(alias, rows)
        tmpRow.setImportIndex(row.getImportIndex())
        compareTotalValues(row, tmpRow, totalColumns, logger, 0, false)
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
            (headerRows[0][0]) : '№',
            (headerRows[0][1]) : 'Наименования статей',
            (headerRows[0][2]) : 'Код организации - участника группы (подразделения ПАО Сбербанк)*, с которым осуществляются взаиморасчеты',
            (headerRows[0][3]) : 'Наименование организации - участника группы (подразделения ПАО Сбербанк)*, с которым осуществляются взаиморасчеты',
            (headerRows[0][4]) : 'Наименование валюты**',
            (headerRows[0][5]) : 'Суммы доходов/расходов, учтенные на счетах бухгалтерского учета по состоянию на отчетную дату',
            (headerRows[0][7]) : 'Суммы доходов/расходов, относящиеся к отчетному периоду, но не проведенные по счетам бухгалтерского учета по состоянию на отчетную дату в номинале валюты, тыс.единиц',
            (headerRows[0][8]) : 'Краткое описание операции,сделки',
            (headerRows[10][5]): 'в номинале валюты, тыс.единиц',
            (headerRows[10][6]): 'в пересчете на рубли, тыс.рублей',

            (headerRows[14][0]) : '1.1',
            (headerRows[14][1]) : '1.2',
            (headerRows[14][2]) : '2.1',
            (headerRows[14][3]) : '2.2',
            (headerRows[14][4]) : '3',
            (headerRows[14][5]) : '4.1',
            (headerRows[14][6]) : '4.2',
            (headerRows[14][7]) : '5',
            (headerRows[14][8]) : '6'
    ]
    checkHeaderEqualsLocal(headerMapping, logger)
}

// метод используется вместо ScriptUtils.checkHeaderEquals, потому что необходима еще проверка на вхождение,
// потому что в экселе рядом с обычными надписями еще добавлено по-английский
void checkHeaderEqualsLocal(Map<Object, String> headerMapping, def logger) {
    for (Object currentString : headerMapping.keySet()) {
        String referenceString = headerMapping.get(currentString)
        if (currentString == null || referenceString == null) {
            continue
        }
        String s1 = currentString.toString().trim().replaceAll("  ", " ")
        String s2 = referenceString.trim().replaceAll("  ", " ")
        if (s1.equalsIgnoreCase(s2)) {
            continue
        }
        // дополнительная проверка на вхождение русских надписей
        String tmp1 = s1.toLowerCase()
        String tmp2 = s2.toLowerCase()
        if (tmp1.contains(tmp2)) {
            continue
        }
        if (logger == null) {
            throw new ServiceException(WRONG_HEADER_EQUALS, s2, s1)
        }
        logger.error(WRONG_HEADER_EQUALS, s2, s1)
    }
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
    def colIndex

    // графа 3  - атрибут 5216 - STATREPORT_ID - «ИД в АС "Статотчетность"», справочник 520 «Участники ТЦО»
    colIndex = 2
    def record520 = getRecordImport(520L, 'STATREPORT_ID', values[colIndex], fileRowIndex, colIndex + colOffset)
    newRow.statReportId = record520?.record_id?.value

    // графа 4 - зависит от графы 3 - атрибут 5201 - NAME - «Полное наименование юридического лица с указанием ОПФ», справочник 520 «Участники ТЦО»
    if (record520) {
        colIndex = 3
        def expectedValues = [ record520.NAME?.value ]
        formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'orgName'), record520?.STATREPORT_ID?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 5 - атрибут 66 - NAME - «Наименование», справочник 15 «Общероссийский классификатор валют»
    colIndex = 4
    // используется нестанадартный метод вместо formDataService.getRefBookRecordImport или formDataService.getRefBookRecordIdImport
    // потому что валюта ищется по названия, а название у справочника 15 не являтеся обязательным и уникальным
    // и в случае если найдены несколько записей не надо выводить сообщения об этом, а надо брать одну из записей
    newRow.currency = getCurrencyIdByName(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 6
    colIndex++
    newRow.currencySum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7
    colIndex++
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 8
    colIndex++
    newRow.otherSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9
    colIndex++
    newRow.description = values[colIndex]

    return newRow
}

void readFile(def allValues, def headerValues, def tableStartValue, def tableEndValue, def columnCount, def headerRowCount, def paramsMap) {
    if (!UploadFileName.endsWith(".xls")) {
        // для xlsm/xlsx использовать обычную загрузку
        checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, tableStartValue, tableEndValue, headerRowCount, paramsMap)
        return
    }
    // для xls использовать старую загрузку
    def xml = getXML(ImportInputStream, importService, UploadFileName, tableStartValue, tableEndValue, columnCount, headerRowCount)
    paramsMap.rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger() + headerRowCount - 1
    paramsMap.colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()
    xml.row.eachWithIndex { row, i ->
        def rowValues = []
        row.cell.each { cell ->
            rowValues.add(cell.text())
        }
        if (i < headerRowCount) {
            // заполнение шапки
            headerValues.add(rowValues)
        } else {
            // заполнение данных
            allValues.add(rowValues)
        }
    }
}

void afterLoad() {
    if (binding.variables.containsKey("specialPeriod")) {
        // имя периода и конечная дата корректны
        // устанавливаем дату для справочников
        specialPeriod.calendarStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
}

@Field
def currencyIdsMap = [:]

// нестанадартный метод вместо formDataService.getRefBookRecordImport или formDataService.getRefBookRecordIdImport
// используется только в этой форме
def getCurrencyIdByName(def value, def rowIndex, colIndex) {
    def refBookId = 15L
    if (currencyIdsMap[value] == null) {
        // получить все записи
        def records = getRecordsByRefbookId(refBookId)

        // поиск подходящих записей
        def findIds = []
        for (def record : records) {
            if (record?.NAME?.value?.equalsIgnoreCase(value)) {
                findIds.add(record?.record_id?.value)
            }
        }
        currencyIdsMap[value] = findIds
    }

    if (currencyIdsMap[value].isEmpty()) {
        // если нет подходящих записей, то выдать сообщение
        RefBook rb = refBookFactory.get(refBookId)
        def attributeName = rb.getAttribute('NAME').getName()
        def dateStr = getReportPeriodEndDate().format('dd.MM.yyyy')
        logger.warn(REF_BOOK_NOT_FOUND_IMPORT_ERROR, rowIndex, getXLSColumnName(colIndex), rb.getName(), attributeName, value, dateStr)
        return null
    }
    return currencyIdsMap[value][0]
}

@Field
def recordsMap = [:]

def getRecordsByRefbookId(long id) {
    if (recordsMap[id] == null) {
        // получить записи из справончика
        def provider = formDataService.getRefBookProvider(refBookFactory, id, providerCache)
        recordsMap[id] = provider.getRecords(getReportPeriodEndDate(), null, null, null)
        if (recordsMap[id] == null) {
            recordsMap[id] = []
        }
    }
    return recordsMap[id]
}