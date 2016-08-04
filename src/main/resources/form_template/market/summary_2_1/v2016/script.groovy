package form_template.market.summary_2_1.v2016

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * 2.1 (Сводный) Реестр выданных Банком гарантий (контргарантий, поручительств).
 *
 * formTemplateId = 910
 * formType = 910
 */

// графа 1  (1.1)  - code
// графа 2  (1.2)  - name
// графа 3  (2)    - rowNum
// графа 4  (3)    - guarantor
// графа 5  (3.1)  - vnd
// графа 6  (3.2)  - level
// графа 7  (4.1)  - procuct1
// графа 8  (4.2)  - procuct2
// графа 9  (4.3)  - procuct3
// графа 10 (5)    - taxpayerName
// графа 11 (6)    - taxpayerInn
// графа 12 (6.1)  - okved
// графа 13 (7.1)  - creditRating              - атрибут 6034 - SHORT_NAME - «Краткое наименование», справочник 603 «Кредитные рейтинги»
// графа 14 (7.2)  - creditClass               - атрибут 6011 - CREDIT_QUALITY_CLASS - «Краткое наименование», справочник 601 «Классы кредитоспособности»
// графа 15 (8)    - beneficiaryName
// графа 16 (9)    - beneficiaryInn
// графа 17 (10)   - emitentName
// графа 18 (11)   - instructingName
// графа 19 (12)   - number
// графа 20 (13)   - issuanceDate
// графа 21 (14)   - additionDate
// графа 22 (15)   - startDate
// графа 23 (16)   - conditionEffective
// графа 24 (17)   - endDate
// графа 25 (18.1) - sumInCurrency
// графа 26 (18.2) - sumInRub
// графа 27 (19)   - currency                  - атрибут 65 - CODE_2 - «Код валюты. Буквенный», справочник 15 «Общероссийский классификатор валют»
// графа 28 (20)   - debtBalance
// графа 29 (21.1) - isNonRecurring            - атрибут 250 - VALUE - «Значение», справочник 38 «Да/Нет»
// графа 30 (21.2) - paymentPeriodic
// графа 31 (22.1) - isCharged                 - атрибут 250 - VALUE - «Значение», справочник 38 «Да/Нет»
// графа 32 (22.2) - tariff
// графа 33 (22.3) - remuneration
// графа 34 (22.4) - remunerationStartYear
// графа 35 (22.5) - remunerationIssuance
// графа 36 (23)   - provide
// графа 37 (24)   - numberGuarantee
// графа 38 (25)   - numberAddition
// графа 39 (26)   - isGuaranetee              - атрибут 250 - VALUE - «Значение», справочник 38 «Да/Нет»
// графа 40 (26.1) - dateGuaranetee
// графа 41 (26.2) - sumGuaranetee
// графа 42 (26.3) - term
// графа 43 (26.4) - sumDiversion
// графа 44 (27.1) - arrears
// графа 45 (27.2) - arrearsDate
// графа 46 (28.1) - arrearsGuarantee
// графа 47 (28.2) - arrearsGuaranteeDate
// графа 48 (29)   - reserve
// графа 49 (30)   - comment
// графа 50 (31)   - segment

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
    case FormDataEvent.COMPOSE:
        consolidation()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

@Field
def allColumns = ['code', 'name', 'rowNum', 'guarantor', 'vnd', 'level', 'procuct1', 'procuct2', 'procuct3',
        'taxpayerName', 'taxpayerInn', 'okved', 'creditRating', 'creditClass', 'beneficiaryName', 'beneficiaryInn',
        'emitentName', 'instructingName', 'number', 'issuanceDate', 'additionDate', 'startDate', 'conditionEffective',
        'endDate', 'sumInCurrency', 'sumInRub', 'currency', 'debtBalance', 'isNonRecurring', 'paymentPeriodic',
        'isCharged', 'tariff', 'remuneration', 'remunerationStartYear', 'remunerationIssuance', 'provide',
        'numberGuarantee', 'numberAddition', 'isGuaranetee', 'dateGuaranetee', 'sumGuaranetee', 'term', 'sumDiversion',
        'arrears', 'arrearsDate', 'arrearsGuarantee', 'arrearsGuaranteeDate', 'reserve', 'comment', 'segment']

// Редактируемые атрибуты (графа 1, 2, 4..50)
@Field
def editableColumns = ['code', 'name', 'guarantor', 'vnd', 'level', 'procuct1', 'procuct2', 'procuct3', 'taxpayerName',
        'taxpayerInn', 'okved', 'creditRating', 'creditClass', 'beneficiaryName', 'beneficiaryInn', 'emitentName',
        'instructingName', 'number', 'issuanceDate', 'additionDate', 'startDate', 'conditionEffective', 'endDate',
        'sumInCurrency', 'sumInRub', 'currency', 'debtBalance', 'isNonRecurring', 'paymentPeriodic', 'isCharged',
        'tariff', 'remuneration', 'remunerationStartYear', 'remunerationIssuance', 'provide', 'numberGuarantee',
        'numberAddition', 'isGuaranetee', 'dateGuaranetee', 'sumGuaranetee', 'term', 'sumDiversion', 'arrears',
        'arrearsDate', 'arrearsGuarantee', 'arrearsGuaranteeDate', 'reserve', 'comment', 'segment']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1..4, 7, 10, 11, 15, 16, 19, 20, 22, 25..29, 31..37)
@Field
def nonEmptyColumns = ['code', 'name', 'rowNum', 'guarantor', 'procuct1', 'taxpayerName', 'taxpayerInn',
        'beneficiaryName', 'beneficiaryInn', 'number', 'issuanceDate', 'startDate', 'sumInCurrency',
        'sumInRub', 'currency', 'debtBalance', 'isNonRecurring', 'isCharged', 'tariff', 'remuneration',
        'remunerationStartYear', 'remunerationIssuance', 'provide', 'numberGuarantee']

@Field
def endDate = null

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }
}

/** Группировка по графе 11, 19, 20. */
String getKey(def row) {
    return (row.taxpayerInn?.trim() + "#" + row.number?.trim() + "#" + row.issuanceDate?.format('dd.MM.yyyy')).toLowerCase()
}

@Field
def recordCache = [:]

@Field
def providerCache = [:]

def getRecords(def inn) {
    def filter = "LOWER(INN) = LOWER('$inn') OR LOWER(KIO) = LOWER('$inn')".toString()
    def provider = formDataService.getRefBookProvider(refBookFactory, 520L, providerCache)
    if (recordCache[filter] == null) {
        recordCache.put(filter, provider.getRecords(getReportPeriodEndDate(), null, filter, null))
    }
    return recordCache[filter]
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        row.taxpayerName = calc10(row)
    }
}

@Field
def exclusiveInns = ['9999999999', '9999999998']

// аналогичный алгоритм в форме 2.6 (Ежемесячный) метод calc7()
def calc10(def row) {
    def tmp = row.taxpayerName
    if (!exclusiveInns.contains(row.taxpayerInn)) {
        def records = getRecords(row.taxpayerInn?.trim()?.toLowerCase())
        if (records != null && records.size() == 1) {
            tmp = records.get(0)?.NAME?.value
        }
    }
    return tmp
}

void consolidation() {
    // мапа со списоком строк НФ (ключ по критериям сопоставимости -> строки НФ)
    def groupRowsMap = [:]
    // мапа для строк источников и номер источника (строка источника -> номер месяца)
    def sourcePeriodOrderMap = [:]

    def sourceFormTypeId = 909
    def sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)
    sourcesInfo = sourcesInfo.findAll { it.formType.id == sourceFormTypeId }
    sourcesInfo.each { Relation relation ->
        FormData sourceFormData = formDataService.get(relation.formDataId, null)
        def dataRows = formDataService.getDataRowHelper(sourceFormData).allSaved
        // сгруппировать строки в группы
        for (def row : dataRows) {
            def key = getKey(row)
            if (groupRowsMap[key] == null) {
                groupRowsMap[key] = []
            }
            groupRowsMap[key].add(row)
            sourcePeriodOrderMap[row] = sourceFormData.periodOrder
        }
    }

    def dataRows = []
    groupRowsMap.keySet().toList().each { key ->
        def rows = groupRowsMap[key]
        def row = rows.max { sourcePeriodOrderMap[it] }
        def newRow = getNewRow()
        allColumns.each { alias ->
            newRow[alias] = row[alias]
        }
        dataRows.add(newRow)
    }

    updateIndexes(dataRows)
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.allCached = dataRows
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