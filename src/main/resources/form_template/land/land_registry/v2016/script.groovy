package form_template.land.land_registry.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Реестр земельных участков.
 *
 * formTemplateId = 912
 * formTypeId = 912
 *
 * @author Bulat Kinzyabulatov
 */

// графа 1  - rowNumber
// графа 2  - name
// графа 3  - oktmo                - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований (ОКТМО)»
// графа 4  - cadastralNumber
// графа 5  - landCategory         - атрибут 7021 - CODE - «Код», справочник 702 «Категории земли»
// графа 6  - constructionPhase    - атрибут 7011 - CODE - «Код», справочник 701 «Периоды строительства»
// графа 7  - cadastralCost
// графа 8  - taxPart
// графа 9  - ownershipDate
// графа 10 - terminationDate
// графа 11 - benefitCode          - атрибут 7053.7041 - TAX_BENEFIT_ID.CODE - «Код налоговой льготы».«Код», справочник 705 «Параметры налоговых льгот земельного налога»
// графа 12 - benefitBase          - зависит от графы 11 - атрибут 7053.7043 - TAX_BENEFIT_ID.BASE - «Код налоговой льготы».«Основание», справочник 705 «Параметры налоговых льгот земельного налога»
// графа 13 - benefitParam         - зависит от графы 11 - атрибут 7061 - REDUCTION_PARAMS - «Параметры льготы», справочник 705 «Параметры налоговых льгот земельного налога»
// графа 14 - startDate
// графа 15 - endDate
// графа 16 - benefitPeriod

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        copyFromPrevForm()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        if (preCalcCheck()) {
            calc()
            logicCheck()
            formDataService.saveCachedDataRows(formData, logger)
        }
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
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

@Field
def allColumns = ['rowNumber', 'name', 'oktmo', 'cadastralNumber', 'landCategory', 'constructionPhase',
        'cadastralCost', 'taxPart', 'ownershipDate', 'terminationDate', 'benefitCode', 'benefitBase',
        'benefitParam', 'startDate', 'endDate', 'benefitPeriod']

// Редактируемые атрибуты (графа 2..11, 14, 15)
@Field
def editableColumns = ['name', 'oktmo', 'cadastralNumber', 'landCategory', 'constructionPhase', 'cadastralCost',
        'taxPart', 'ownershipDate', 'terminationDate', 'benefitCode', 'startDate', 'endDate']

// Автозаполняемые атрибуты (графа 1, 16)
@Field
def autoFillColumns = ['rowNumber', 'benefitPeriod']

// Проверяемые на пустые значения атрибуты (графа 3, 4, 5, 7, 9)
@Field
def nonEmptyColumns = ['oktmo', 'cadastralNumber', 'landCategory', 'cadastralCost', 'ownershipDate']

@Field
def sortColumns = ['oktmo', 'cadastralNumber']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

@Field
def reportPeriod = null

def getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return reportPeriod
}

//// Обертки методов

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def reportPeriod = getReportPeriod()
    def year = reportPeriod.taxPeriod.year
    def startYearDate = Date.parse('dd.MM.yyyy', '01.01.' + year)
    def cadastralNumberMap = [:]
    def allRecords705 = (dataRows.size() > 0 ? getAllRecords705() : null)

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowIndex = row.getIndex()

        // 1. Проверка обязательности заполнения граф
        checkNonEmptyColumns(row, rowIndex, nonEmptyColumns, logger, true)

        // 2. Проверка одновременного заполнения данных о налоговой льготе
        def value11 = (row.benefitCode ? true : false)
        def value14 = (row.startDate ? true : false)
        if (value11 ^ value14) {
            logger.error("Строка %s: Данные о налоговой льготе указаны не полностью", rowIndex)
        }

        // 3. Проверка корректности заполнения даты возникновения права собственности
        if (row.ownershipDate != null && row.ownershipDate > getReportPeriodEndDate()) {
            def columnName9 = getColumnName(row, 'ownershipDate')
            def dateStr = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно %s", rowIndex, columnName9, dateStr)
        }

        // 4. Проверка корректности заполнения даты прекращения права собственности
        if (row.terminationDate != null && (row.terminationDate < row.ownershipDate || row.terminationDate < startYearDate)) {
            def columnName10 = getColumnName(row, 'terminationDate')
            def dateStr = '01.01.' + year
            def columnName9 = getColumnName(row, 'ownershipDate')
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значению графы «%s»",
                    rowIndex, columnName10, dateStr, columnName9)
        }

        if (row.taxPart != null) {
            def partArray = row.taxPart.split('/')

            // 5. Проверка доли налогоплательщика в праве на земельный участок
            if (!(row.taxPart ==~ /\d{1,10}\/\d{1,10}/) ||
                    partArray[0].toString().startsWith('0') ||
                    partArray[1].toString().startsWith('0') ||
                    partArray[0].toBigDecimal() > partArray[1].toBigDecimal()) {
                def columnName8 = getColumnName(row, 'taxPart')
                logger.error("Строка %s: Графа «%s» должна быть заполнена согласно формату: " +
                        "«(от 1 до 10 числовых знаков) / (от 1 до 10 числовых знаков)», " +
                        "без лидирующих нулей в числителе и знаменателе, числитель должен быть меньше либо равен знаменателю",
                        rowIndex, columnName8)
            }

            // 6. Проверка значения знаменателя доли налогоплательщика в праве на земельный участок
            if (partArray.size() == 2 && partArray[1] ==~ /\d{1,}/ && partArray[1].toBigDecimal() == 0) {
                def columnName8 = getColumnName(row, 'taxPart')
                logger.error("Строка %s: Значение знаменателя в графе «%s» не может быть равным нулю", rowIndex, columnName8)
            }
        }

        // 7. Проверка корректности заполнения даты начала действия льготы
        if (row.startDate && row.ownershipDate && row.startDate < row.ownershipDate) {
            def columnName14 = getColumnName(row, 'startDate')
            def columnName9 = getColumnName(row, 'ownershipDate')
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»",
                    rowIndex, columnName14, columnName9)
        }

        // 8. Проверка корректности заполнения даты окончания действия льготы
        if (row.startDate && row.endDate && (row.endDate < row.startDate || row.terminationDate && row.terminationDate < row.endDate)) {
            def columnName15 = getColumnName(row, 'endDate')
            def columnName14 = getColumnName(row, 'startDate')
            def columnName10 = getColumnName(row, 'terminationDate')
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s» и быть меньше либо равно значению графы «%s»",
                    rowIndex, columnName15, columnName14, columnName10)
        }

        // 9. Проверка наличия в реестре земельных участков с одинаковым кадастровым номером и кодом ОКТМО, периоды владения которых пересекаются
        // сбор данных
        if (row.oktmo && row.cadastralNumber) {
            def groupKey = getRefBookValue(96L, row.oktmo)?.CODE?.value + "#" + row.cadastralNumber
            if (cadastralNumberMap[groupKey] == null) {
                cadastralNumberMap[groupKey] = []
            }
            cadastralNumberMap[groupKey].add(row)
        }

        // 10. Проверка корректности заполнения кода налоговой льготы (графа 11)
        if (row.benefitCode && row.oktmo) {
            def findRecord = allRecords705.find { it?.record_id?.value == row.benefitCode }
            if (findRecord && findRecord?.OKTMO?.value != row.oktmo) {
                def columnName11 = getColumnName(row, 'benefitCode')
                def columnName3 = getColumnName(row, 'oktmo')
                logger.error("Строка %s: Код ОКТМО, в котором действует выбранная в графе «%s» льгота, должен быть равен значению графы «%s»",
                        rowIndex, columnName11, columnName3)
            }
        }

        // 11. Проверка корректности заполнения графы 16
        if(formDataEvent != FormDataEvent.CALCULATE && row.benefitPeriod != calc16(row)){
            def columnName16 = getColumnName(row, 'benefitPeriod')
            logger.error("Строка %s: Графа «%s» заполнена неверно. Выполните расчет формы",
                    rowIndex, columnName16)
        }
    }

    // 9. Проверка наличия в реестре земельных участков с одинаковым кадастровым номером и кодом ОКТМО, периоды владения которых пересекаются
    if (!cadastralNumberMap.isEmpty()) {
        def groupKeys = cadastralNumberMap.keySet().toList()
        for (def groupKey : groupKeys) {
            def rows = cadastralNumberMap[groupKey]
            if (rows.size() <= 1) {
                continue
            }
            def rowIndexes = []
            def tmpRows = rows.collect { it }
            rows.each { row ->
                def start = row.ownershipDate
                def end = (row.terminationDate ?: getReportPeriodEndDate())
                tmpRows.remove(row)
                tmpRows.each { row2 ->
                    def start2 = row2.ownershipDate
                    def end2 = (row2.terminationDate ?: getReportPeriodEndDate())
                    if (!(start > end2 || start2 > end)) {
                        rowIndexes.add(row.getIndex())
                        rowIndexes.add(row2.getIndex())
                    }
                }
            }
            rowIndexes = rowIndexes.unique().sort()
            if (rowIndexes) {
                rowIndexes = rowIndexes.join(', ')
                def value4 = rows[0].cadastralNumber
                def value3 = getRefBookValue(96L, rows[0].oktmo)?.CODE?.value
                logger.error("Строки %s. Кадастровый номер земельного участка «%s», Код ОКТМО «%s»: на форме не должно быть строк с одинаковым кадастровым номером, кодом ОКТМО и пересекающимися периодами владения правом собственности",
                        rowIndexes, value4, value3)
            }
        }
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // графа 16
        row.benefitPeriod = calc16(row)
    }
}

def BigDecimal calc16(def row) {
    def tmp
    if (row.endDate && row.endDate < getReportPeriodStartDate() || row.startDate > getReportPeriodEndDate()) {
        tmp = BigDecimal.ZERO
    } else {
        def end = (row.endDate == null || row.endDate > getReportPeriodEndDate() ? getReportPeriodEndDate() : row.endDate)
        def start = (row.startDate < getReportPeriodStartDate() ? getReportPeriodStartDate() : row.startDate)
        tmp = end.format('M').toInteger() - start.format('M').toInteger() + 1
    }
    return round(tmp, 0)
}
void importData() {
    if (UploadFileName?.toLowerCase()?.endsWith('.xlsm')) {
        importDataXlsm()
    } else {
        importDataXlsx()
    }
}

// стандартная загрузка из xlsm
void importDataXlsm() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 16
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXlsm(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
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

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()

    def hasRegion = (formDataDepartment.regionId != null)
    if (!hasRegion) {
        def columnName11 = getColumnName(tmpRow, 'benefitCode')
        logger.warn("На форме невозможно заполнить графу «%s», так как атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)",
                columnName11)
    }

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
        rowIndex++
        // простая строка
        def newRow = getNewRowFromXlsm(rowValues, colOffset, fileRowIndex, rowIndex, hasRegion)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 */
void checkHeaderXlsm(def headerRows, def colCount, rowCount) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping = [
            [(headerRows[0][0]) : headers[0].rowNumber],
            [(headerRows[0][1]) : headers[0].name],
            [(headerRows[0][2]) : headers[0].oktmo],
            [(headerRows[0][3]) : headers[0].cadastralNumber],
            [(headerRows[0][4]) : headers[0].landCategory],
            [(headerRows[0][5]) : headers[0].constructionPhase],
            [(headerRows[0][6]) : headers[0].cadastralCost],
            [(headerRows[0][7]) : headers[0].taxPart],
            [(headerRows[0][8]) : headers[0].ownershipDate],
            [(headerRows[0][9]) : headers[0].terminationDate],
            [(headerRows[0][10]): headers[0].benefitCode],
            [(headerRows[1][10]): headers[1].benefitCode],
            [(headerRows[1][11]): headers[1].benefitBase],
            [(headerRows[1][12]): headers[1].benefitParam],
            [(headerRows[1][13]): headers[1].startDate],
            [(headerRows[1][14]): headers[1].endDate],
            [(headerRows[1][15]): headers[1].benefitPeriod]
    ]
    (0..15).each {
        headerMapping.add([(headerRows[2][it]) : (it + 1).toString()])
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
 * @param hasRegion признак необходимости заполнения графы 11, 12, 13
 */
def getNewRowFromXlsm(def values, def colOffset, def fileRowIndex, def rowIndex, def hasRegion) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 2
    def int colIndex = 1
    newRow.name = values[colIndex]

    // графа 3 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований (ОКТМО)»
    colIndex++
    if (values[colIndex]) {
        newRow.oktmo = getRecordIdImport(96, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    } else {
        def xlsColumnName3 = getXLSColumnName(colIndex + colOffset)
        def columnName11 = getColumnName(newRow, 'benefitCode')
        def columnName3 = getColumnName(newRow, 'oktmo')
        logger.warn("Строка %s, столбец %s: На форме невозможно заполнить графу «%s», так как не заполнена графа «%s»",
                fileRowIndex, xlsColumnName3, columnName11, columnName3)
    }

    // графа 4
    colIndex++
    newRow.cadastralNumber = values[colIndex]

    // графа 5 - атрибут 7021 - CODE - «Код», справочник 702 «Категории земли»
    colIndex++
    newRow.landCategory = getRecordIdImport(702, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 6 - атрибут 7011 - CODE - «Код», справочник 701 «Периоды строительства»
    colIndex++
    newRow.constructionPhase = getRecordIdImport(701, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 7
    colIndex++
    newRow.cadastralCost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 8
    colIndex++
    newRow.taxPart = values[colIndex]

    // графа 9
    colIndex++
    newRow.ownershipDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 10
    colIndex++
    newRow.terminationDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 11, 12, 13
    if (hasRegion) {
        // графа 11 - атрибут 7053 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 705 «Параметры налоговых льгот земельного налога»
        colIndex = 10
        def record704 = (values[2] ? getRecordImport(704, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset) : null)
        def code = record704?.record_id?.value
        def oktmo = newRow.oktmo
        def param = values[12] ?: null
        def record705 = getRecord705Import(code, oktmo, param)
        newRow.benefitCode = record705?.record_id?.value
        if (record704 && record705 == null) {
            def xlsColumnName11 = getXLSColumnName(colIndex + colOffset)
            def columnName11 = getColumnName(newRow, 'benefitCode')
            def columnName12 = getColumnName(newRow, 'benefitBase')
            def columnName13 = getColumnName(newRow, 'benefitParam')
            def date = getReportPeriodEndDate()?.format('dd.MM.yyyy')
            def regionName = getRefBookValue(4L, formDataDepartment.regionId)?.CODE?.value ?: ""
            def value11 = values[colIndex]
            def value3 = values[2]
            def value13 = values[12]
            logger.warn("Строка %s, столбец %s: На форме невозможно заполнить графы: «%s», «%s», «%s», " +
                    "так как в справочнике «Параметры налоговых льгот земельного налога» не найдена запись, " +
                    "актуальная на дату «%s», в которой поле «Код субъекта РФ представителя декларации» = «%s», " +
                    "поле «Код» = «%s», поле «Код ОКТМО» = «%s», поле «Параметры льготы» = «%s»",
                    fileRowIndex, xlsColumnName11, columnName11, columnName12, columnName13,
                    date, regionName, value11, value3, value13)
        }

        // графа 12 - зависит от графы 11 - атрибут 7053.7043 - TAX_BENEFIT_ID.BASE - «Код налоговой льготы».«Основание», справочник 705 «Параметры налоговых льгот земельного налога»
        if (record704 && record705) {
            colIndex++
            def expectedValues = [record704?.BASE?.value]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'benefitBase'), record704?.CODE?.value, fileRowIndex, colIndex + colOffset, logger, false)
        }

        // графа 13 - зависит от графы 11 - атрибут 7061 - REDUCTION_PARAMS - «Параметры льготы», справочник 705 «Параметры налоговых льгот земельного налога»
        // проверять не надо, т.к. участвует в поиске родительской записи
    }

    // графа 14
    colIndex = 13
    newRow.startDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 15
    colIndex++
    newRow.endDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 16
    colIndex++
    newRow.benefitPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// специфичная загрузка из xlsx
void importDataXlsx() {
    int COLUMN_COUNT = 7
    int HEADER_ROW_COUNT = 1
    String TABLE_START_VALUE = 'Наименование'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXlsx(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
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

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // пропустить если: все строки пустые или столбец 2 "Кадастровый номер участка" пустой
        if (!rowValues || rowValues.size() < 2 || !rowValues[1]) {
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        rowIndex++
        // простая строка
        def newRow = getNewRowFromXlsx(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы.
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 */
void checkHeaderXlsx(def headerRows, def colCount, rowCount) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [
            [(headerRows[0][0]) : 'Наименование'],
            [(headerRows[0][1]) : 'Кадастровый номер участка'],
            [(headerRows[0][2]) : 'Кадастровая стоимость'],
            [(headerRows[0][3]) : 'Ставка'],
            [(headerRows[0][4]) : 'Период'],
            [(headerRows[0][5]) : 'Расчётная сумма зем.налога'],
            [(headerRows[0][6]) : 'Сумма земельного  налога']
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
def getNewRowFromXlsx(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 4
    def int colIndex = 1
    newRow.cadastralNumber = values[colIndex]

    // графа 7
    colIndex++
    newRow.cadastralCost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def columns = sortColumns + (allColumns - sortColumns)
    // массовое разыменование справочных и зависимых значений
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns())
    sortRows(dataRows, columns)
    dataRowHelper.saveSort()
}

// Предрасчетные проверки
def preCalcCheck() {
    // 1. Проверка заполнения атрибута «Регион»
    if (formDataDepartment.regionId == null) {
        logger.error("В справочнике «Подразделения» не заполнено поле «Регион» для подразделения «%s»", formDataDepartment.name)
        return false
    }
    return true
}

@Field
def isConsolidated = null

def isConsolidated() {
    if (isConsolidated == null) {
        isConsolidated = formData.kind == FormDataKind.CONSOLIDATED
    }
    return isConsolidated
}

/** Получить строки за предыдущий отчетный период. */
def getPrevDataRows() {
    if (isConsolidated() || getPrevReportPeriod()?.period == null) {
        return null
    }
    def prevFormData = formDataService.getFormDataPrev(formData)
    return (prevFormData?.state == WorkflowState.ACCEPTED ? formDataService.getDataRowHelper(prevFormData)?.allSaved : null)
}

@Field
def prevReportPeriodMap = null

/**
 * Получить предыдущий отчетный период
 *
 * @return мапа с данными предыдущего периода:
 *      period - период (может быть null, если предыдущего периода нет);
 *      periodName - название;
 *      year - год;
 */
def getPrevReportPeriod() {
    if (prevReportPeriodMap != null) {
        return prevReportPeriodMap
    }
    def reportPeriod = getReportPeriod()
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def find = false
    // предыдущий период в том же году, что и текущий, и номера периодов отличаются на единицу
    if (prevReportPeriod && reportPeriod.order > 1 && reportPeriod.order - 1 == prevReportPeriod.order &&
            reportPeriod.taxPeriod.year == prevReportPeriod.taxPeriod.year) {
        find = true
    }
    // если текущий период первый в налоговом периоде, то предыдущий период должен быть последним, и года налоговых периодов должны отличаться на единицу
    if (!find && prevReportPeriod && reportPeriod.order == 1 && prevReportPeriod.order == 4 &&
            reportPeriod.taxPeriod.year - 1 == prevReportPeriod.taxPeriod.year) {
        find = true
    }
    prevReportPeriodMap = [:]
    if (find) {
        prevReportPeriodMap.period = prevReportPeriod
        prevReportPeriodMap.periodName = prevReportPeriod.name
        prevReportPeriodMap.year = prevReportPeriod.taxPeriod.year
    } else {
        // получение названии периодов
        def filter = 'L = 1'
        def provider = formDataService.getRefBookProvider(refBookFactory, 8L, providerCache)
        def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        records?.sort { it?.END_DATE?.value }

        prevReportPeriodMap.period = null
        prevReportPeriodMap.periodName = records[reportPeriod.order - 2].NAME?.value
        prevReportPeriodMap.year = (reportPeriod.order == 1 ? reportPeriod.taxPeriod.year - 1 : reportPeriod.taxPeriod.year)
    }
    return prevReportPeriodMap
}

/** Копирование данных из форм предыдущего периода. */
void copyFromPrevForm() {
    // копировать только для первичной формы
    if (formData.kind != FormDataKind.PRIMARY) {
        return
    }

    // Логическая проверка 9 - нет формы предыдущего периода
    def prevDataRows = getPrevDataRows()
    if (prevDataRows == null) {
        def prevReportPeriod = getPrevReportPeriod()
        def periodName = prevReportPeriod?.periodName
        def year = prevReportPeriod?.year
        logger.warn("Данные по земельным участкам из предыдущего отчетного периода не были скопированы. " +
                "В Системе отсутствует форма за период: %s %s для подразделения «%s»",
                periodName, year, formDataDepartment.name)
        return
    }

    def dataRows = []
    def reportPeriod = getReportPeriod()
    def year = reportPeriod.taxPeriod.year
    def endYearDate = Date.parse('dd.MM.yyyy', '31.12.' + year)
    def copyColumns = allColumns - 'rowNumber' - 'benefitPeriod'

    if (reportPeriod.order == 1) {
        // 1 квартал - отбор подходящих строк
        for (def prevRow : prevDataRows) {
            def useRow = prevRow.ownershipDate <= endYearDate && (prevRow.terminationDate == null || prevRow.terminationDate >= getReportPeriodStartDate())
            if (!useRow) {
                continue
            }
            def newRow = getNewRow()
            copyColumns.each { alias ->
                newRow[alias] = prevRow[alias]
            }
            if (newRow.endDate && newRow.endDate < getReportPeriodStartDate()) {
                // графа 11..15
                ['benefitCode', 'benefitBase', 'benefitParam', 'startDate', 'endDate'].each { alias ->
                    newRow[alias] = null
                }
            }
            dataRows.add(newRow)
        }
    } else {
        // 2, 3, 4 квартал - простое копирование
        for (def prevRow : prevDataRows) {
            def newRow = getNewRow()
            copyColumns.each { alias ->
                newRow[alias] = prevRow[alias]
            }
            dataRows.add(newRow)
        }
    }
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

/**
 * Получить запись справочника 705 "Параметры налоговых льгот земельного налога".
 *
 * @param code графа 11 - код налоговой льготы (id справочника 704)
 * @param oktmo графа 3 - код ОКТМО (id справочника 96)
 * @param param графа 13 - параметры льготы (строка, может быть null)
 */
def getRecord705Import(def code, def oktmo, def param) {
    if (code == null || oktmo == null) {
        return null
    }
    def allRecords = getAllRecords705()
    for (def record : allRecords) {
        if (code == record?.TAX_BENEFIT_ID?.value && oktmo == record?.OKTMO?.value &&
                ((param ?: null) == (record?.REDUCTION_PARAMS?.value ?: null) || param?.equalsIgnoreCase(record?.REDUCTION_PARAMS?.value))) {
            return record
        }
    }
    return null
}

@Field
def allRecords705 = null

def getAllRecords705() {
    if (allRecords705 == null) {
        def filter = 'DECLARATION_REGION_ID = ' + formDataDepartment.regionId
        def provider = formDataService.getRefBookProvider(refBookFactory, 705L, providerCache)
        allRecords705 = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        if (allRecords705 == null) {
            allRecords705 = []
        }
    }
    return allRecords705
}