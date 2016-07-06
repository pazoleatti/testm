package form_template.deal.journal_settlements.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 807 - Журнал взаиморасчетов.
 *
 * formTemplateId=807
 *
 * Форма с фиксированными разделами. Каждый раздел имеет заголовок, в котором содержится еще и сумма.
 * Загрзука из экселя нестандартная: эксель не из нашей системы, а сформированный другой системой, формат xls, поэтому используется старая загрузка.
 * На всякий случай оставлена загрузка из xlsx/xlsm.
 */

// графа    - fix1
// графа 1  - sbrfCode1     - атрибут 166 - SBRF_CODE - «Код подразделения в нотации Сбербанка», справочник 30 «Подразделения»
// графа 2  - statReportId1 - атрибут 5216 - STATREPORT_ID - «ИД в АС "Статотчетность"», справочник 520 «Участники ТЦО»
// графа    - fix2
// графа 3  - depName1      - зависит от графы 1 - атрибут 161 - NAME - «Наименование подразделения», справочник 30 «Подразделения»
// графа 4  - orgName1      - зависит от графы 2 - атрибут 5201 - NAME - «Полное наименование юридического лица с указанием ОПФ», справочник 520 «Участники ТЦО»
// графа 5  - sbrfCode2     - атрибут 166 - SBRF_CODE - «Код подразделения в нотации Сбербанка», справочник 30 «Подразделения»
// графа 6  - statReportId2 - атрибут 5216 - STATREPORT_ID - «ИД в АС "Статотчетность"», справочник 520 «Участники ТЦО»
// графа 7  - depName2      - зависит от графы 5 - атрибут 161 - NAME - «Наименование подразделения», справочник 30 «Подразделения»
// графа 8  - orgName2      - зависит от графы 6 - атрибут 5201 - NAME - «Полное наименование юридического лица с указанием ОПФ», справочник 520 «Участники ТЦО»
// графа 9  - sum

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
def allColumns = ['fix1', 'sbrfCode1', 'statReportId1', 'fix2', 'depName1', 'orgName1', 'sbrfCode2', 'statReportId2', 'depName2', 'orgName2', 'sum']

// Редактируемые атрибуты
@Field
def editableColumns = ['sbrfCode1', 'statReportId1', 'sbrfCode2', 'statReportId2', 'sum']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['depName1', 'orgName1', 'depName2', 'orgName2']

// Проверяемые на пустые значения атрибуты (графа )
@Field
def nonEmptyColumns = ['sum']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 9)
@Field
def totalColumns = ['sum']

@Field
def calcRowAliases = [
        '01', '02', '03', '04', '05', '05.1', '05.2', '05.3', '06', '06.1', '06.2', '06.3', '06.3', '06.4', '06.5',
        '99.1',
        '20', '21', '22', '23', '23.1', '23.2', '23.3', '24', '24.1', '24.2', '24.3', '24.4', '24.5',
        '99.2',
        '30.', '31', '32', '40', '50', '51'
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
                break;
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
        row.sum = calc9(alias, dataRows)
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // пропускать строки с заголовками и надписями
        if (row.getAlias() && !(row.getAlias() in calcRowAliases)) {
            continue
        }

        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // пропустить фиксированные строки
        if (row.getAlias()) {
            continue
        }
        // 2. Обязательное заполнение идентификационного кода первой стороны сделки
        if (row.sbrfCode1 == null && row.statReportId1 == null) {
            logger.error("Строка %s: Необходимо заполнить графу «%s» или графу «%s»!",
                    row.getIndex(), getColumnName(row, 'sbrfCode1'), getColumnName(row, 'statReportId1'))
        }

        // 3. Корректное указание данных первой стороны сделки
        if (row.sbrfCode1 != null && row.statReportId1 != null) {
            logger.error("Строка %s: Графа «%s» и графа «%s» не могут быть одновременно заполнены!",
                    row.getIndex(), getColumnName(row, 'sbrfCode1'), getColumnName(row, 'statReportId1'))
        }

        // 4. Обязательное заполнение идентификационного кода второй стороны сделки
        if (row.sbrfCode2 == null && row.statReportId2 == null) {
            logger.error("Строка %s: Необходимо заполнить графу «%s» или графу «%s»!",
                    row.getIndex(), getColumnName(row, 'sbrfCode2'), getColumnName(row, 'statReportId2'))
        }

        // 5. Корректное указание данных второй стороны сделки
        if (row.sbrfCode2 != null && row.statReportId2 != null) {
            logger.error("Строка %s: Графа «%s» и графа «%s» не могут быть одновременно заполнены!",
                    row.getIndex(), getColumnName(row, 'sbrfCode2'), getColumnName(row, 'statReportId2'))
        }

        // . Обязательность заполнения гр.2
        if (row.statReportId1 && !getRefBookValue(520, row.statReportId1)?.STATREPORT_ID?.value) {
            logger.error(WRONG_NON_EMPTY, row.getIndex(), getColumnName(row, 'statReportId1'))
        }

        // 6. Обязательность заполнения гр.3
        if (row.sbrfCode1 && !getRefBookValue(30, row.sbrfCode1)?.NAME?.value) {
            logger.error(WRONG_NON_EMPTY, row.getIndex(), getColumnName(row, 'depName1'))
        }

        // 7. Обязательность заполнения гр.4
        if (row.statReportId1 && !getRefBookValue(520, row.statReportId1)?.NAME?.value) {
            logger.error(WRONG_NON_EMPTY, row.getIndex(), getColumnName(row, 'orgName1'))
        }

        // . Обязательность заполнения гр.6
        if (row.statReportId2 && !getRefBookValue(520, row.statReportId2)?.STATREPORT_ID?.value) {
            logger.error(WRONG_NON_EMPTY, row.getIndex(), getColumnName(row, 'statReportId2'))
        }

        // 8. Обязательность заполнения гр.7
        if (row.sbrfCode2 && !getRefBookValue(30, row.sbrfCode2)?.NAME?.value) {
            logger.error(WRONG_NON_EMPTY, row.getIndex(), getColumnName(row, 'depName2'))
        }

        // 9. Обязательность заполнения гр.8
        if (row.statReportId2 && !getRefBookValue(520, row.statReportId2)?.NAME?.value) {
            logger.error(WRONG_NON_EMPTY, row.getIndex(), getColumnName(row, 'orgName2'))
        }
    }

    // 10. Проверка итоговых значений по разделам
    def tmpRow = formData.createStoreMessagingDataRow()
    calcRowAliases.each { alias ->
        def row = getDataRow(dataRows, alias)
        tmpRow.sum = calc9(alias, dataRows)
        if (isDiffRow(row, tmpRow, totalColumns)) {
            logger.error("Строка %s: " + WRONG_TOTAL, row.getIndex(), getColumnName(row, 'sum'))
        }
    }
}

// мапа строк для суммирования (алиас строки -> список строк для суммирования по графе 9)
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
 * Получить сумму по графе 9 для указанного алиаса строки
 *
 * @param rowAlias алиас строки
 * @param rows строки нф
 */
def calc9(def rowAlias, def rows) {
    def result = 0;
    def rowsForSum = rowsForSumMap[rowAlias]
    if (rowsForSum == null) {
        // сумма только по своему разделу
        result = calcSome(rowAlias, rows)
    } else {
        // сумма по нескольким разделам
        rowsForSum.each {
            result += calc9(it, rows)
        }
    }
    return result
}

/**
 * Получить сумму по графе 9 только по строкам раздела указанного алиаса
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
    int COLUMN_COUNT = 5
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = '№'
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

        // все строки пустые - выход
        if (!rowValues || rowValues.isEmpty() || !rowValues.find { it }) {
            allValues.remove(rowValues)
            rowValues.clear()
            if (isSection51) {
                // после раздела 51 прекратить загрузку если попалась пустая строка
                break
            } else {
                // до раздела 51 пропускать пустые строки
                continue
            }
        }

        def sectionValue = rowValues[INDEX_FOR_SKIP]

        // после раздела 51 прекращать загрузку если: пустая строка, если надпись начинается с "*"
        if (isSection51 && sectionValue && (sectionValue[0] == '*')) {
            break
        }

        // если новый раздел, то взять его сумму
        rowIndex++
        if (!sectionValue) {
            // фиксированная строка с надписью
            def row = templateRows.find { rowValues[1] == it.fix2 }
            if (!row) {
                logger.error("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
            }
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (sectionValue && sectionRowMap[sectionValue]) {
            // фискированная строка с суммой
            def row = sectionRowMap[sectionValue]
            def colIndex = 4
            row.sum = parseNumber(rowValues[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

            sectionIndex = row?.fix1
            mapRows.put(sectionIndex, [])

            // для раздела 6.3 и 24.3 почему то в экселе сделано объединение со следующей строкой
            if (sectionValue == '06.3' || sectionValue == '24.3') {
                // пропустить следующую строку
                i++
                fileRowIndex++
                allValues.remove(rowValues)
                rowValues.clear()
                rowValues = allValues[0]
            }
            if (sectionValue == '51') {
                isSection51 = true
            }

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }

        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        mapRows[sectionIndex].add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // копирование данных по разделам
    def rows = []
    templateRows.each { row ->
        rows.add(row)
        if (row.fix1 && mapRows[row.fix1]) {
            rows.addAll(mapRows[row.fix1])
        }
    }
    updateIndexes(rows)

    // сравнение итогов
    calcRowAliases.each { alias ->
        def row = getDataRow(rows, alias)
        tmpRow.sum = calc9(alias, rows)
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
            ([(headerRows[0][0]) : '№']),
            ([(headerRows[0][1]) : 'Наименования статей']),
            ([(headerRows[0][2]) : 'Код организации - участника группы (подразделения ПАО Сбербанк)*, с которым осуществляются взаиморасчеты']),
            ([(headerRows[0][3]) : 'Наименование организации - участника группы (подразделения ПАО Сбербанк)*, с которым осуществляются взаиморасчеты']),
            ([(headerRows[0][4]) : 'Суммы доходов/ расходов с начала года']),

            ([(headerRows[1][0]) : '1']),
            ([(headerRows[1][1]) : '2']),
            ([(headerRows[1][2]) : '3.1']),
            ([(headerRows[1][3]) : '3.2']),
            ([(headerRows[1][4]) : '4'])
    ]
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

    // графа 1 - атрибут 166 - SBRF_CODE - «Код подразделения в нотации Сбербанка», справочник 30 «Подразделения»
    def colIndex = 0
    def value = getCodeDepartment(values[colIndex])
    def record30 = getRecordImport(30L, 'SBRF_CODE', value, fileRowIndex, colIndex + colOffset)
    newRow.sbrfCode1 = record30?.record_id?.value

    // графа 2 - атрибут 5216 - STATREPORT_ID - «ИД в АС "Статотчетность"», справочник 520 «Участники ТЦО»
    colIndex = 0
    value = getCodeOrganization(values[colIndex])
    def record520 = getRecordImport(520L, 'STATREPORT_ID', value, fileRowIndex, colIndex + colOffset)
    newRow.statReportId1 = record520?.record_id?.value

    // графа 5 - атрибут 166 - SBRF_CODE - «Код подразделения в нотации Сбербанка», справочник 30 «Подразделения»
    colIndex = 2
    value = getCodeDepartment(values[colIndex])
    record30 = getRecordImport(30L, 'SBRF_CODE', value, fileRowIndex, colIndex + colOffset)
    newRow.sbrfCode2 = record30?.record_id?.value

    // графа 6 - атрибут 5216 - STATREPORT_ID - «ИД в АС "Статотчетность"», справочник 520 «Участники ТЦО»
    colIndex = 2
    value = getCodeOrganization(values[colIndex])
    record520 = getRecordImport(520L, 'STATREPORT_ID', value, fileRowIndex, colIndex + colOffset)
    newRow.statReportId2 = record520?.record_id?.value

    // графа 9
    colIndex = 4
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

def getCodeDepartment(String code) {
    if (!code || !hasOnlyDecimal(code)) {
        // если пустое или не только цифры
        return null
    } else if (code == '99') {
        return '99_0000_00'
    } else if (code.endsWith('0000')) {
        return code.substring(0, code.size() - 4) + '_0000_00'
    }
    return code
}

def getCodeOrganization(String code) {
    if (code && !hasOnlyDecimal(code)) {
        // если не только цифры
        return code
    }
    return null
}

// проверка наличия только цифр
def hasOnlyDecimal(def value) {
    return value?.replaceAll('_', '').matches('\\d*')
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