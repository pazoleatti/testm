package form_template.land.land_registry.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Реестр земельных участков
 * TODO расчет и проверка 12-й графы
 * TODO специфичный импорт
 * formTemplateId = 912
 *
 * @author Bulat Kinzyabulatov
 *
 * графа 1  - rowNumber            - № пп
 * графа 2  - oktmo                - Код ОКТМО 96 840 CODE
 * графа 3  - cadastralNumber      - Кадастровый номер земельного участка
 * графа 4  - landCategory         - Категория земель (код) 702 7021 CODE
 * графа 5  - constructionPhase    - Период строительства 701 7012 NAME
 * графа 6  - cadastralCost        - Кадастровая стоимость (доля стоимости) земельного участка
 * графа 7  - taxPart              - Доля налогоплательщика в праве на земельный участок
 * графа 8  - ownershipDate        - Дата возникновения права собственности на земельный участок
 * графа 9  - terminationDate      - Дата прекращения права собственности на земельный участок
 * графа 10 - benefitCode          - Информация о налоговых льготах (в случае наличия). Код 704 7041 CODE
 * графа 11 - benefitBase          - Информация о налоговых льготах (в случае наличия). Основание 704 7041 BASE reference benefitCode
 * графа 12 - benefitParam         - Информация о налоговых льготах (в случае наличия). Параметры льготы
 * графа 13 - benefitPeriod        - Информация о налоговых льготах (в случае наличия). Срок использования льготы
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
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
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
//    case FormDataEvent.IMPORT:
//        importData() TODO
//        formDataService.saveCachedDataRows(formData, logger)
//        break
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
def allColumns = ['rowNumber', 'oktmo', 'cadastralNumber', 'landCategory', 'constructionPhase', 'cadastralCost',
                  'taxPart', 'ownershipDate', 'terminationDate', 'benefitCode', 'benefitBase', 'benefitParam', 'benefitPeriod']

// Редактируемые атрибуты
@Field
def editableColumns = ['oktmo', 'cadastralNumber', 'landCategory', 'constructionPhase', 'cadastralCost',
                       'taxPart', 'ownershipDate', 'terminationDate', 'benefitCode', 'benefitParam', 'benefitPeriod']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'benefitBase']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['oktmo', 'cadastralNumber', 'landCategory', 'cadastralCost', 'ownershipDate']

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

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def startYear = reportPeriod.taxPeriod.year
    def startYearDate = Date.parse('dd.MM.yyyy', '01.01.' + startYear)
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowIndex = row.getIndex()

        // 1. Проверка обязательности заполнения граф
        checkNonEmptyColumns(row, rowIndex, nonEmptyColumns, logger, true)

        // 2. Проверка одновременного заполнения данных о налоговой льготе
        if (((row.benefitCode != null) || (row.benefitParam != null) || (row.benefitPeriod != null)) &&
                !((row.benefitCode != null) && (row.benefitParam != null) && (row.benefitPeriod != null))) {
            logger.error("Строка %s: Данные о налоговой льготе указаны не полностью", rowIndex)
        }

        // 3. Проверка корректности заполнения даты возникновения права собственности
        if ((row.ownershipDate != null) && (row.ownershipDate > getReportPeriodEndDate())) {
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно %s",
                    rowIndex, getColumnName(row, 'ownershipDate'), getReportPeriodEndDate().format('dd.MM.yyyy'))
        }

        // 4. Проверка корректности заполнения даты прекращения права собственности
        if ((row.terminationDate != null) && ((row.terminationDate < row.ownershipDate) || (row.terminationDate < startYearDate))) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно %s и больше либо равно значению графы «%s»",
                    rowIndex, getColumnName(row, 'terminationDate'), '01.01.' + startYear, getColumnName(row, 'ownershipDate'))
        }

        // 5. Проверка формата значения доли налогоплательщика в праве на земельный участок
        if (row.taxPart != null) {
            def partArray = row.taxPart.split('/')
            if (!(row.taxPart ==~ /\d{1,10}\/\d{1,10}/)
                    || partArray[0].toString().startsWith('0') || partArray[1].toString().startsWith('0')) {
                logger.error("Строка %s: Графа «%s» должна быть заполнена согласно формату: «(от 1 до 10 числовых знаков) / (от 1 до 10 числовых знаков)», без лидирующих нулей в числителе и знаменателе, числитель должен быть меньше либо равен знаменателю",
                        rowIndex, getColumnName(row, 'taxPart'))
            }
        }

        // 6. Проверка корректности заполнения срока использования льготы (графа 13)
        if(row.benefitCode != null) {
            def periodMax = (reportPeriod.order != 4) ? 3 : 12
            if (row.benefitPeriod < 0 || row.benefitPeriod > periodMax) {
                logger.error("Строка %s: Значение в графе «%s» для форм в периодах 1 кв., 2 кв., 3 кв. должно быть больше либо равно 0 " +
                        "и меньше либо равно 3, для форм за период «Год» должно быть больше либо равно 0 и меньше либо равно 12",
                        rowIndex, getColumnName(row, 'benefitPeriod'))
            }
        }

        // 7 TODO
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    // TODO 12-я графа
}

// Получение импортируемых данных
void importData() { // TODO
    int COLUMN_COUNT = 6
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = '№ пп'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
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
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        rowIndex++
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
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
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers

    def headerMapping = [
            [(headerRows[0][0]) : headers[0].rowNumber],
            [(headerRows[0][1]) : headers[0].oktmo],
            [(headerRows[0][2]) : headers[0].cadastralNumber],
            [(headerRows[0][3]) : headers[0].landCategory],
            [(headerRows[0][4]) : headers[0].constructionPhase],
            [(headerRows[0][5]) : headers[0].cadastralCost],
            [(headerRows[0][6]) : headers[0].taxPart],
            [(headerRows[0][7]) : headers[0].ownershipDate],
            [(headerRows[0][8]) : headers[0].terminationDate],
            [(headerRows[0][9]) : headers[0].benefitCode], // colSpan = 4
            [(headerRows[1][9]) : headers[1].benefitCode],
            [(headerRows[1][10]): headers[1].benefitBase],
            [(headerRows[1][11]): headers[1].benefitParam],
            [(headerRows[1][12]): headers[1].benefitPeriod]
    ]
    (0..13).each {
        headerMapping.add([(headerRows[1][it]): (it + 1).toString()])
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
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    def int colIndex = 1

    // графа 2
    newRow.oktmo = getRecordIdImport(96, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    colIndex++

    // графа 3
    newRow.cadastralNumber = values[colIndex]
    colIndex++

    // графа 4
    newRow.landCategory = getRecordIdImport(1000, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    colIndex++

    // графа 5
    newRow.cadastralCost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 6
    newRow.taxPart = values[colIndex]

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}