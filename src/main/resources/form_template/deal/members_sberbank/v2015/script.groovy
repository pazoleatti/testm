package form_template.deal.members_sberbank.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * 845 - Участники группы ПАО Сбербанк
 *
 * formTemplateId=845
 */

//    rowNumber     1.	№ п/п
//    name          2.	Полное наименование юридического лица - участника группы
//    pseudoName    3.	Полное наименование юридического лица - участника группы (в случае изменения названия)
//    statReportId  4.	Код участника в соответствие с классификатором АС «Статотчетность»
//    code          5.	Номер (код) юридического лица (заполняется в порядке, установленном /3/ для формы 0409801)
//    depName       6.	Наименование подразделения, ответственного за взаимодействие
//    consoType     7.	Метод консолидации (1-полная консолидация, 2 - пропорционально доле участия)
//    controlPart   8.	Доля контроля Группы в уставном капитале участника, %
//    sharePart     9.	Доля участия Группы в уставном капитале участника, %
//    iksr          10.	ИНН юридического лица
//    date          11.	Дата включения участника в состав группы
//    sign          12.	Является группой компаний (0 - нет, 1 - да)


switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_LOAD:
        afterLoad()
        break
    // расчет ничего не делает
    case FormDataEvent.CALCULATE:
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
def allColumns = ['rowNumber', 'name', 'pseudoName', 'statReportId', 'code', 'depName', 'consoType', 'controlPart', 'sharePart', 'iksr', 'date', 'sign']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'pseudoName', 'code', 'depName', 'consoType', 'controlPart', 'sharePart', 'date', 'sign']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['statReportId', 'iksr']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'code', 'depName', 'consoType', 'controlPart', 'sharePart', 'date', 'sign']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

// Номер отчетного периода
@Field
def periodOrder = null

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

def getPeriodOrder() {
    if (periodOrder == null) {
        periodOrder = reportPeriodService.get(formData.reportPeriodId).getOrder()
    }
    return periodOrder
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

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def records520 = getRecords520()
    for (def row : dataRows) {
        def rowNum = row.getIndex()

        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Проверка метода консолидации
        if (row.consoType != null && ![1, 2].contains(row.consoType.intValue())) {
            logger.error("Строка $rowNum: Графа «${getColumnName(row, 'consoType')}» должна принимать значение из следующего списка: «1» или «2»!")
        }

        // 3. Проверка доли контроля группы
        if (row.controlPart != null && (row.controlPart < 0 || row.controlPart > 100)) {
            logger.error("Строка %s: Значение графы «%s» должно принимать значение из диапазона: «0» - «100»!", rowNum, getColumnName(row, 'controlPart'))
        }

        // 4. Проверка доли участия группы
        if (row.sharePart != null && (row.sharePart < 0 || row.sharePart > 100)) {
            logger.error("Строка %s: Значение графы «%s» должно принимать значение из диапазона: «0» - «100»!", rowNum, getColumnName(row, 'sharePart'))
        }

        // 5. Проверка даты включение участника в состав группы
        checkDatePeriod(logger, row, 'date', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // 6. Проверка признака группы компаний
        if (row.sign != null && ![0, 1].contains(row.sign.intValue())) {
            logger.error("Строка $rowNum: Графа «${getColumnName(row, 'sign')}» должна принимать значение из следующего списка: «0» или «1»!")
        }

        // 7. Проверка на отсутствие в списке не ВЗЛ
        def isVZL = records520?.find { it?.record_id?.value == row.name }
        if (row.name && records520 && !isVZL) {
            def value2 = getRefBookValue(520L, row.name)?.NAME?.value
            logger.error("Строка %s: Организация «%s» не является взаимозависимым лицом в данном отчетном периоде!", rowNum, value2)
        }
    }
}

@Field
def records520 = null

/**
 * Получить значения из справочника "Участники ТЦО".
 * @param useCode true для 9 месяцев, false для года
 * @return
 */
def getRecords520() {
    if (records520 != null) {
        return records520
    }
    // получить записи из справочника "Участники ТЦО"
    def provider = formDataService.getRefBookProvider(refBookFactory, 520L, providerCache)
    def records = provider.getRecords(getReportPeriodEndDate(), null, null, null)
    records520 = []
    records.each { record ->
        def start = record?.START_DATE?.value
        def end = record?.END_DATE?.value
        def typeId = record?.TYPE?.value
        if (isVZL(start, end, typeId)) {
            records520.add(record)
        }
    }
    return records520
}

// проверка принадлежности организации к ВЗЛ в отчетном периоде
def isVZL(def start, def end, def typeId) {
    if (start <= getReportPeriodEndDate() && (end == null || end >= getReportPeriodStartDate()) &&
            getRefBookValue(525L, typeId)?.CODE?.value == "ВЗЛ") {
        return true
    }
    return false
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

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 12
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
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
        if (!rowValues || rowValues.isEmpty() || !rowValues.find { it }) {
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
            ([(headerRows[0][0]) : getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[0][1]) : getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][2]) : getColumnName(tmpRow, 'pseudoName')]),
            ([(headerRows[0][3]) : StringUtils.cleanString(getColumnName(tmpRow, 'statReportId'))]),
            ([(headerRows[0][4]) : getColumnName(tmpRow, 'code')]),
            ([(headerRows[0][5]) : getColumnName(tmpRow, 'depName')]),
            ([(headerRows[0][6]) : getColumnName(tmpRow, 'consoType')]),
            ([(headerRows[0][7]) : getColumnName(tmpRow, 'controlPart')]),
            ([(headerRows[0][8]) : getColumnName(tmpRow, 'sharePart')]),
            ([(headerRows[0][9]) : getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[0][10]) : getColumnName(tmpRow, 'date')]),
            ([(headerRows[0][11]) : getColumnName(tmpRow, 'sign')])
    ]
    (1..12).each {
        headerMapping.add(([(headerRows[1][it - 1]): it.toString()]))
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

    def int colIndex = 1

    def recordId = getTcoRecordId(values[1], values[9], getColumnName(newRow, 'iksr'), fileRowIndex, colIndex, getReportPeriodEndDate(), true, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 2
    newRow.name = recordId
    colIndex++

    // графа 3
    newRow.pseudoName = values[colIndex]
    colIndex++

    // графа 4 - атрибут 5216 - STATREPORT_ID - «ИД в АС "Статотчетность"», справочник 520 «Участники ТЦО»
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.STATREPORT_ID?.stringValue as String, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 5
    newRow.code = values[colIndex]
    colIndex++

    // графа 6
    newRow.depName = values[colIndex]
    colIndex++

    // графа 7
    newRow.consoType = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 8
    newRow.controlPart = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.sharePart = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 11
    newRow.date = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.sign = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

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

void afterLoad() {
    if (binding.variables.containsKey("specialPeriod")) {
        // имя периода и конечная дата корректны
        // устанавливаем дату для справочников
        specialPeriod.calendarStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
}