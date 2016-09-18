package form_template.market.summary_2_6.v2016

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * 2.6 (Сводный) Отчет о состоянии кредитного портфеля
 * Совпадает во многом с первичкой 2.6 (но есть консолидация и отсутсвует импорт с редактированием)
 * formTemplateId = 904
 *
 * @author Bulat Kinzyabulatov
 *
 * графа 1  - rowNum             - № п/п
 * графа 2  - codeBank           - Код банка
 * графа 3  - nameBank           - Наименование банка
 * графа 4  - depNumber          - Номер отделения, выдавшего кредит / кредитующее подразделение ЦА, к компетенции которого относится договор
 * графа 5  - okved              - Код отрасли по ОКВЭД
 * графа 6  - opf                - Организационно-правовая форма
 * графа 7  - debtorName         - Наименование заемщика
 * графа 8  - inn                - ИНН заемщика
 * графа 9  - sign               - Признак СМП
 * графа 10 - direction          - Направление бизнес плана, к которому относится кредит
 * графа 11 - law                - Номер Регламента, в рамках которого предоставлен кредит
 * графа 12 - creditType         - Тип кредита
 * графа 13 - docNum             - № кредитного договора
 * графа 14 - docDate            - Дата кредитного договора
 * графа 15 - creditDate         - Дата выдачи кредита
 * графа 16 - closeDate          - Дата погашения с учетом последней пролонгации
 * графа 17 - extendNum          - Количество пролонгаций
 * графа 18 - creditMode         - Режим кредитования
 * графа 19 - currencySum        - Валюта суммы кредита (лимита кредитной линии)
 * графа 20 - sumDoc             - Сумма кредита (по договору), лимит кредитной линии, тыс. ед. валюты
 * графа 21 - sumGiven           - Сумма выданного кредита, тыс. ед. валюты
 * графа 22 - rate               - Действующая процентная ставка
 * графа 23 - payFrequency       - Периодичность уплаты процентов
 * графа 24 - currencyCredit     - Валюта выдачи кредита
 * графа 25 - debtSum            - Остаток задолженности на отчетную дату (тыс. руб.). Ссудная задолженность, всего
 * графа 26 - inTimeDebtSum      - Остаток задолженности на отчетную дату (тыс. руб.). В т.ч. срочная
 * графа 27 - overdueDebtSum     - Остаток задолженности на отчетную дату (тыс. руб.). В т.ч. просроченная
 * графа 28 - percentDebtSum     - Остаток задолженности на отчетную дату (тыс. руб.). Задолженность по просроченным %
 * графа 29 - deptDate           - Дата вынесения на просрочку основного долга
 * графа 30 - percentDate        - Дата вынесения на просрочку процентов
 * графа 31 - percentPeriod      - Срок нахождения на счетах просроченных требований и/или процентов, дней
 * графа 32 - provision          - Обеспечение
 * графа 33 - provisionComment   - Примечание к обеспечению
 * графа 34 - loanSign           - Признак реструктурированной ссуды
 * графа 35 - loanQuality        - Категория качества ссуды
 * графа 36 - finPosition        - Финансовое положение
 * графа 37 - debtService        - Обслуживание долга
 * графа 38 - creditRisk         - Категория кредитного риска / класс кредитоспособности
 * графа 39 - portfolio          - Портфель однородных требований
 * графа 40 - reservePercent     - Величина отчислений в резерв, %
 * графа 41 - reserveSum         - Сформированный резерв в тыс. руб.
 */

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
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

@Field
def allColumns = ['rowNum', 'codeBank', 'nameBank', 'depNumber', 'okved', 'opf', 'debtorName', 'inn', 'sign', 'direction',
                  'law', 'creditType', 'docNum', 'docDate', 'creditDate', 'closeDate', 'extendNum', 'creditMode',
                  'currencySum', 'sumDoc', 'sumGiven', 'rate', 'payFrequency', 'currencyCredit', 'debtSum', 'inTimeDebtSum',
                  'overdueDebtSum', 'percentDebtSum', 'deptDate', 'percentDate', 'percentPeriod', 'provision', 'provisionComment',
                  'loanSign', 'loanQuality', 'finPosition', 'debtService', 'creditRisk', 'portfolio', 'reservePercent', 'reserveSum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNum', 'codeBank', 'nameBank', 'depNumber', 'okved', 'opf', 'debtorName', 'inn', 'sign', 'direction',
                       'law', 'creditType', 'docNum', 'docDate', 'closeDate', 'extendNum', 'creditMode',
                       'currencySum', 'sumDoc', 'sumGiven', 'rate', 'payFrequency', 'currencyCredit', 'debtSum', 'inTimeDebtSum',
                       'overdueDebtSum', 'percentDebtSum', 'provision', 'loanSign', 'loanQuality', 'finPosition', 'debtService', 'creditRisk', 'reservePercent', 'reserveSum']

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

@Field
def refBookCache = [:]

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

@Field
def nonNegativeColumns = ['extendNum', 'sumDoc', 'sumGiven', 'rate', 'debtSum', 'inTimeDebtSum',
                  'overdueDebtSum', 'percentDebtSum', 'percentPeriod', 'reservePercent', 'reserveSum']

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }
}

String getKey(def row) {
    return (row.inn?.trim() + "#" + row.docNum?.trim() + "#" + row.docDate?.format('dd.MM.yyyy')).toLowerCase()
}

@Field
def recordCache = [:]

@Field
def providerCache = [:]

/**
 * Получить список записей из справочника "Участники ТЦО" (id = 520) по ИНН или КИО.
 *
 * @param value значение для поиска по совпадению
 */
def getRecords520(def value) {
    return getRecordsByValue(520L, value, ['INN', 'KIO'])
}

// мапа хранящая мапы с записями справочника (ключ "id справочника" -> мапа с записями, ключ "значение атрибута" -> список записией)
// например:
// [ id 520 : мапа с записям ]
//      мапа с записями = [ инн 1234567890 : список подходящих записей ]
@Field
def recordsMap = [:]

/**
 * Получить список записей из справочника атрибуты которых равны заданному значению.
 *
 * @param refBookId id справочника
 * @param value значение для поиска
 * @param attributesForSearch список атрибутов справочника по которым искать совпадения
 */
def getRecordsByValue(def refBookId, def value, def attributesForSearch) {
    if (recordsMap[refBookId] == null) {
        recordsMap[refBookId] = [:]
        // получить все записи справочника и засунуть в мапу
        def allRecords = getAllRecords(refBookId)?.values()
        allRecords.each { record ->
            attributesForSearch.each { attribute ->
                def tmpKey = getKeyValue(record[attribute]?.value)
                if (tmpKey) {
                    if (recordsMap[refBookId][tmpKey] == null) {
                        recordsMap[refBookId][tmpKey] = []
                    }
                    if (!recordsMap[refBookId][tmpKey].contains(record)) {
                        recordsMap[refBookId][tmpKey].add(record)
                    }
                }
            }
        }
    }
    def key = getKeyValue(value)
    return recordsMap[refBookId][key]
}

def getKeyValue(def value) {
    return value?.trim()?.toLowerCase()
}

@Field
def allRecordsMap = [:]

/**
 * Получить все записи справочника.
 *
 * @param refBookId id справочника
 * @return мапа с записями справочника (ключ "id записи" -> запись)
 */
def getAllRecords(def refBookId) {
    if (allRecordsMap[refBookId] == null) {
        def date = getReportPeriodEndDate()
        def provider = formDataService.getRefBookProvider(refBookFactory, refBookId, providerCache)
        List<Long> uniqueRecordIds = provider.getUniqueRecordIds(date, null)
        allRecordsMap[refBookId] = provider.getRecordData(uniqueRecordIds)
    }
    return allRecordsMap[refBookId]
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        row.debtorName = calc7(row)
    }
}

@Field
def exclusiveInns = ['9999999999', '9999999998']

def calc7(def row) {
    def tmp = row.debtorName
    if (!exclusiveInns.contains(row.inn)) {
        def records = getRecords520(row.inn)
        if (records != null && records.size() == 1) {
            tmp = records.get(0)?.NAME?.value
        }
    }
    return tmp
}

void consolidation() {
    def rowsMap = [:]
    def sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)
    // сортируем по месяцам
    sourcesInfo.sort { it.month }.each { Relation relation ->
        FormData sourceFormData = formDataService.get(relation.formDataId, null)
        def dataRows = formDataService.getDataRowHelper(sourceFormData).allSaved
        dataRows.each { row ->
            // перезаписываем если совпадает по ключу
            def key = getKey(row)
            rowsMap[key]= row
        }
    }
    List<DataRow> dataRows = []
    rowsMap.values().each { row ->
        def newRow = formData.createDataRow()
        row.each { alias, value ->
            newRow[alias] = value
        }
        dataRows.add(newRow)
    }
    //sortRows(refBookService, logger, dataRows, null, null, null)
    //updateIndexes(dataRows)
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.allCached = dataRows
}