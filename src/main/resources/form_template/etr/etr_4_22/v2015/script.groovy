package form_template.etr.etr_4_22.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Профессиональное суждение (Приложение 4-22)
 * formTemplateId = 722
 *
 * графа 1 - rowNum     - № п/п
 * графа 2 - taxName    - Наименование показателей
 * графа 3 - dynamics   - Динамика (+;-)
 * графа 4 - factors    - Оценка факторов, оказавших влияние на уровень и динамику показателя
 * графа 5 - areas      - Проблемные зоны и зоны потенциального риска
 * графа 6 - offers     - Предложения по мерам
 * графа 7 - offersCA   - Предложения на уровень ЦА Банка
 * графа 8 - other      - Прочее
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
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
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def allColumns = ['rowNum', 'taxName', 'dynamics', 'factors', 'areas', 'offers', 'offersCA', 'other']

@Field
def nonEmptyColumns = allColumns

@Field
def editableColumns = ['dynamics', 'factors', 'areas', 'offers', 'offersCA', 'other']

@Field
def reportPeriodEndDate

def getEndDate(int reportPeriodId) {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(reportPeriodId)?.time
    }
    return reportPeriodEndDate
}
// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getEndDate(formData.reportPeriodId), rowIndex, colIndex, logger, required)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // 1. Проверка заполнения ячеек
    for (def row : dataRows) {
        // 1. Проверка заполнения ячеек
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
        // 2. Проверка заполнения графы 3
        if (!['+', '-'].contains(row.dynamics)) {
            logger.error("Строка %s: Графа «%s» заполнена неверно! Ожидаемое значение «+» или «-».", row.getIndex(), getColumnName(row, 'dynamics') )
        }
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 8
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null

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

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def allValuesCount = allValues.size()

    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows

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
        // прервать по загрузке нужных строк
        if (rowIndex > dataRows.size()) {
            break
        }
        // найти нужную строку нф
        def dataRow = dataRows.get(rowIndex - 1)
        def templateRow = getDataRow(templateRows, dataRow.getAlias())
        // заполнить строку нф значениями из эксель
        fillRowFromXls(templateRow, dataRow, rowValues, fileRowIndex, rowIndex, colOffset)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }
    if (rowIndex < dataRows.size()) {
        logger.error("Структура файла не соответствует макету налоговой формы.")
    }
    showMessages(dataRows, logger)
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 */
void checkHeaderXls(def headerRows, def colCount, def rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNum')]),
            ([(headerRows[0][1]): getColumnName(tmpRow, 'taxName')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'dynamics')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'factors')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'areas')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'offers')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'offersCA')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'other')])
    ]
    (0..7).each { index ->
        headerMapping.add(([(headerRows[1][index]): (index + 1).toString()]))
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Заполняет заданную строку нф значениями из экселя.
 *
 * @param templateRow строка макета
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
def fillRowFromXls(def templateRow, def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)
    def tmpValues = formData.createStoreMessagingDataRow()

    // графа 1
    def colIndex = 0
    tmpValues.rowNum = values[colIndex]

    // графа 2
    colIndex++
    tmpValues.taxName = values[colIndex]

    // Проверить фиксированные значения
    tmpValues.keySet().toArray().each { alias ->
        def value = tmpValues[alias]?.toString()
        def valueExpected = StringUtils.cleanString(templateRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
    }

    // графа 3
    colIndex++
    dataRow.dynamics = values[colIndex]

    // графа 4
    colIndex++
    dataRow.factors = values[colIndex]

    // графа 5
    colIndex++
    if (formDataDepartment.regionId) {
        def filter = "REGION_ID = $formDataDepartment.regionId and NAME like '${values[colIndex]}'"
        def provider = formDataService.getRefBookProvider(refBookFactory, 504L, providerCache)
        def records = provider.getRecords(getEndDate(formData.reportPeriodId), null, filter, null)
        if (records) {
            dataRow.areas = records.get(0)?.record_id?.value
        } else {
            def columnIndex = getXLSColumnName(colIndex + colOffset)
            def dateStr = getEndDate(formData.reportPeriodId).format('dd.MM.yyyy')
            // наименование субъекта РФ для атрибута «Регион» подразделения формы
            def regionName = getRefBookValue(4L, formDataDepartment.regionId)?.NAME?.value
            logger.warn('Строка %s, столбец %s: В региональном справочнике «%s» не найдена запись, актуальная на дату %s: ' +
                    'поле «Код субъекта РФ» = «%s», поле «Проблемная зона» = «%s»',
                    fileRowIndex, columnIndex, getRefBook(504L).name, dateStr, regionName, values[colIndex])
        }
    } else if (needShowRegionMsg) {
        needShowRegionMsg = false
        logger.warn('Невозможно выполнить поиск записи в справочнике «%s» для заполнения графы «%s» формы! ' +
                'Атрибут «Регион» подразделения текущей формы не заполнен (справочник «Подразделения»).',
                getRefBook(504L).name, getColumnName(dataRow, 'areas'))
    }
    // графа 6
    colIndex++
    dataRow.offers = values[colIndex]

    // графа 7
    colIndex++
    dataRow.offersCA = values[colIndex]

    // графа 8
    colIndex++
    dataRow.other = values[colIndex]
}
// для хранения информации о справочниках
@Field
def refBooks = [:]

def getRefBook(def id) {
    if (refBooks[id] == null) {
        refBooks[id] = refBookFactory.get(id)
    }
    return refBooks[id]
}
