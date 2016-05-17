package form_template.deal.forecast_major_transactions.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

/**
 * 810 - Прогноз крупных сделок
 *
 * formTemplateId=810
 *
 * @author Lhaziev
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_LOAD:
        afterLoad()
        break
    case FormDataEvent.AFTER_CREATE:
        create()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        logicCheck()
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

@Field
def allColumns = ['rowNum', 'ikksr', 'name', 'transactionName', 'sum']

// Редактируемые атрибуты
@Field
def editableColumns = ['ikksr', 'transactionName', 'sum']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['name']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['ikksr', 'transactionName', 'sum']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

// Дата окончания отчетного периода
@Field
Calendar calendarStartDate = null

@Field
def refBookCache = [:]

@Field
def recordCache = [:]

@Field
def providerCache = [:]

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

Calendar getCalendarStartDate() {
    if (calendarStartDate == null) {
        calendarStartDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId)
    }
    return calendarStartDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы
def getFormDataPrevPeriod(boolean check) {
    if (getCalendarStartDate().get(Calendar.MONTH) != Calendar.JANUARY) {
        def formDataPrev = formDataService.getFormDataPrev(formData)
        if (formDataPrev == null || !formDataPrev.getState().equals(WorkflowState.ACCEPTED)) {
            def prevPeriod = reportPeriodService.getPrevReportPeriod(formData.getReportPeriodId())
            // предыдущего период может не найтись, потому что периода он не существует
            String periodName = "предыдущий период";
            if (prevPeriod != null) {
                periodName = prevPeriod.getName() + " " + prevPeriod.getTaxPeriod().getYear();
            }
            if (check) {
                logger.warn("Прогнозы крупных сделок не были скопированы из предыдущего отчетного периода. " +
                        "В Системе не найдена форма «%s» в статусе «Принята» за «%s», Подразделение: «%s», Тип: «%s».",
                        formData.getFormType().getName(), periodName, formDataDepartment.name, formData.getKind().getTitle())
            }
            return null
        }
        return formDataPrev
    } else {
        return null
    }
}

void create() {
    def formDataPrev = getFormDataPrevPeriod(true);
    if (formDataPrev != null) {
        def prevDataRows = formDataService.getDataRowHelper(formDataPrev)?.allSaved
        if (prevDataRows != null) {
            def dataRows = formDataService.getDataRowHelper(formData).allCached
            dataRows.addAll(prevDataRows)
        }
    }
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (row in dataRows) {
        def rowNum = row.getIndex()

        // 1. Проверка на заполнение графы
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка на положительное значение ожидаемого дохода
        if (row.sum && row.sum < 0) {
            def msg1 = row.getCell('sum').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» не может быть меньше «0»!")
        }

        // 3. Проверка на отсутствие в списке не ВЗЛ ОРН
        if (row.ikksr) {
            boolean isVZL = false
            def record = getRefBookValue(520, row.ikksr)
            def start = record?.START_DATE?.value
            def end = record?.END_DATE?.value
            if (record.TYPE?.referenceValue && record.TAX_STATUS?.referenceValue &&
                    start <= getReportPeriodEndDate() && (end == null || getReportPeriodStartDate() <= end)) {
                def recordType = getRefBookValue(525, record.TYPE.referenceValue)
                def recordStatus = getRefBookValue(511, record.TAX_STATUS.referenceValue)
                if (recordType?.CODE.stringValue == "ВЗЛ" && recordStatus?.CODE.numberValue.longValue() == 2)
                    isVZL = true
            }
            if (!isVZL) {
                def msg1 = record.NAME.stringValue
                rowError(logger, row, "Строка $rowNum: Организация «$msg1» не является взаимозависимым лицом с общим режимом налогообложения в данном отчетном периоде!")
            }
        }
    }

    // 4. Проверка наличия формы предыдущего периода
    getFormDataPrevPeriod(true)
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 5
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null
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
            ([(headerRows[0][0]) : getColumnName(tmpRow, 'rowNum')]),
            ([(headerRows[0][1]) : 'Информация о лице']),
            ([(headerRows[0][3]) : 'Информация о сделке']),
            ([(headerRows[1][1]) : getColumnName(tmpRow, 'ikksr')]),
            ([(headerRows[1][2]) : getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][3]) : getColumnName(tmpRow, 'transactionName')]),
            ([(headerRows[1][4]) : getColumnName(tmpRow, 'sum')])
    ]
    (1..5).each {
        headerMapping.add(([(headerRows[2][it - 1]): it.toString()]))
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

    // графа 2
    def colIndex = 1
    def ikksr = values[1]
    def recordId = null
    def map = null
    if (ikksr?.contains('/')) {
        def parts = ikksr.split(' / ')
        def filter = "INN = '${parts[0]}' AND KPP = '${parts[1]}'"
        map = getRefBookRecord(520L, getReportPeriodEndDate(), filter)
        recordId = map?.record_id?.value
    }
    if (recordId == null){
        recordId = getTcoRecordId(values[2], ikksr, getColumnName(newRow, 'ikksr'), fileRowIndex, colIndex, getReportPeriodEndDate(), true, logger, refBookFactory, recordCache)
        map = getRefBookValue(520, recordId)
    }
    newRow.ikksr = recordId
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.NAME?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 4
    colIndex++
    newRow.transactionName = values[colIndex]

    // графа 5
    colIndex++
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

def getRefBookRecord(def refBookId, def date, def filter) {
    String dateStr = date?.format('dd.MM.yyyy')
    if (recordCache.containsKey(refBookId)) {
        Long recordId = recordCache.get(refBookId).get(dateStr + filter);
        if (recordId != null) {
            // Нашли в кэше
            if (refBookCache != null) {
                return refBookCache.get(getRefBookCacheKey(refBookId, recordId));
            } else {
                Map<String, RefBookValue> retVal = new HashMap<String, RefBookValue>();
                retVal.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId));
                return retVal;
            }
        }
    } else {
        recordCache.put(refBookId, new HashMap<String, Long>());
    }
    def provider = formDataService.getRefBookProvider(refBookFactory, refBookId, providerCache)
    def records = provider.getRecords(date, null, filter, null)
    if (records.size() == 1) {
        Map<String, RefBookValue> retVal = records[0]
        Long recordId = retVal.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
        recordCache.get(refBookId).put(dateStr + filter, recordId);
        if (refBookCache != null) {
            refBookCache.put(getRefBookCacheKey(refBookId, recordId), retVal);
        }
        return retVal
    }
    return null
}

void afterLoad() {
    if (binding.variables.containsKey("specialPeriod")) {
        // имя периода и конечная дата корректны
        // устанавливаем дату для справочников
        specialPeriod.calendarStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
}