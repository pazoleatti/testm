package form_template.market.letter_of_credit.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Данные по непокрытым аккредитивам
 *
 * formTemplateId=913
 *
 * @author Stanislav Yasinskiy
 */

// графа 1  rowNumber       -   № п/п
// графа 2  productType     -   Вид Продукта (гарантия / непокрытый аккредитив)
// графа 3  name            -   Наименование контрагента и ОПФ
// графа 4  country         -   Страна регистрации (местоположения)
// графа 5  innKio          -   ИНН / КИО клиента
// графа 6  docDate         -   Дата подписания Договора / ГенСоглашения
// графа 7  docNumber       -   Номер обязательства (референс)
// графа 8  creditDate      -   Дата выдачи обязательства
// графа 9  creditEndDate   -   Дата окончания действия обязательства
// графа 10 sum             -   Сумма обязательства (в ед. валюты)
// графа 11 currency        -   Валюта выдачи
// графа 12 period          -   Срок в днях
// графа 13 creditRating    -   Кредитный рейтинг контрагента / класс кредитоспособности
// графа 14 faceValueStr    -   Номинальная ставка (в % или в абсолютном выражении) платы за выданный аккредитив / гарантию
// графа 15 faceValueNum    -   Номинальная ставка (в % годовых или в абсолютном выражении) плата за подтверждение платежа по аккредитиву
// графа 16 paymentSchedule -   График платежей по уплате комиссий
// графа 17 sign            -   Наличие обеспечения / поручительства (Да / Нет)

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        // Расчетные поля на форме отсутствуют.
        calc()
        logicCheck()
        // formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null) {
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
def allColumns = ['rowNumber', 'productType', 'name', 'country', 'innKio', 'docDate', 'docNumber', 'creditDate','creditEndDate',
                  'sum', 'currency', 'period', 'creditRating', 'faceValueStr', 'faceValueNum','paymentSchedule', 'sign']

// Редактируемые атрибуты
@Field
def editableColumns = allColumns - ['rowNumber']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['productType', 'name', 'innKio', 'docDate', 'docNumber', 'creditDate','creditEndDate',
                       'sum', 'currency', 'period', 'creditRating', 'faceValueStr', 'faceValueNum','paymentSchedule', 'sign']

@Field
def startDate = null

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

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def rowMap = [:]
    for (row in dataRows) {
        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Положительность графы
        ['sum'].each { alias ->
            if (row[alias] != null && row[alias] <= 0) {
                logger.error("Строка %s: Значение графы «%s» должно быть больше 0!", row.getIndex(), getColumnName(row, alias))
            }
        }

        // 3. Неотрицательность графы
        ['period', 'faceValueNum'].each { alias ->
            if (row[alias] != null && row[alias] < 0) {
                logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно 0!", row.getIndex(), getColumnName(row, alias))
            }
        }

        // 4. Проверка даты выдачи обязательства
        if (row.creditDate && row.creditEndDate && (row.creditDate > row.creditEndDate)) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»!",
                    row.getIndex(), getColumnName(row, 'creditEndDate'), getColumnName(row, 'creditDate'))
        }

        // 5. Дата выдачи обязательства должна попадать в отчетный период
        checkDatePeriod(logger, row, 'creditDate', getReportPeriodStartDate(), getReportPeriodEndDate(), true)

        // 6. Проверка валюты
        if (row.currency != null && ("RUB".equals(getRefBookValue(15, row.currency)?.CODE_2?.value))) {
            logger.error("Строка %s: Для российского рубля должно быть проставлено буквенное значение RUR!", row.getIndex())
        }

        // 7. Проверка на отсутствие нескольких записей по одном и тому же непокрытому аккредитиву
        def key = getKey(row)
        if (rowMap[key] == null) {
            rowMap[key] = row
        } else {
            logger.error("Строка %s: На форме уже существует строка со значениями граф «%s» = «%s», «%s» = «%s», «%s» = «%s»!",
                    row.getIndex(),
                    getColumnName(row, 'innKio'), row.innKio,
                    getColumnName(row, 'docNumber'), row.docNumber,
                    getColumnName(row, 'creditDate'), row.creditDate?.format('dd.MM.yyyy') ?: '')
        }
    }
}

/** Группировка по графе 5, 7, 8. */
String getKey(def row) {
    return (row.innKio?.trim() + "#" + row.docNumber?.trim() + "#" + row.creditDate?.format('dd.MM.yyyy')).toLowerCase()
}

void calc() {
    // Расчетные поля на форме отсутствуют.
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 17
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = tmpRow.getCell('rowNumber').column.name
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

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

    // формирование строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // простая строка
        rowIndex++
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
 * Проверить шапку таблицы.
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping = []
    def index = 0
    allColumns.each { alias ->
        headerMapping.add([(headerRows[0][index]): headers[0][alias]])
        headerMapping.add([(headerRows[1][index]): headers[1][alias]])
        index++
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
    def required = true
    def taxpayerColIndex

    // графа 2
    def colIndex = 1
    newRow.productType = values[colIndex]
    // графа 3
    colIndex++
    newRow.name = values[colIndex]
    // графа 4
    colIndex++
    newRow.country = getRecordIdImport(10L, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 5
    colIndex++
    newRow.innKio = values[colIndex]
    // графа 6
    colIndex++
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, false)
    // графа 7
    colIndex++
    newRow.docNumber = values[colIndex]
    // графа 8
    colIndex++
    newRow.creditDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, false)
    // графа 9
    colIndex++
    newRow.creditEndDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, false)
    // графа 10
    colIndex++
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 11
    colIndex++
    newRow.currency = getRecordIdImport(15L, 'CODE_2', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 12
    colIndex++
    newRow.period = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 13
    colIndex++
    newRow.creditRating = getRecordIdImport(603L, 'SHORT_NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 14
    colIndex++
    newRow.faceValueStr = values[colIndex]
    // графа 15
    colIndex++
    newRow.faceValueNum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 16
    colIndex++
    newRow.paymentSchedule = values[colIndex]
    // графа 17
    colIndex++
    newRow.sign = getRecordIdImport(38L, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    return newRow
}

def getNewRow() {
    def newRow = formData.createStoreMessagingDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
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