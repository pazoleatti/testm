package form_template.deal.app_4_1_9.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * 802 - Приложение 4.1. (9 месяцев) Отчет в отношении доходов и расходов Банка по сделкам с российскими ВЗЛ, применяющими Общий режим налогообложения
 * отличается от 801 только источниками данных
 *
 * formTemplateId=802
 *
 * TODO:
 *      - консолидация не полная, потому что не все макеты источников готовы
 *      - дополнить тесты
 */

// графа 1  (1)   - rowNumber        - № п/п
// графа 2  (2)   - name             - Полное наименование и ОПФ юридического лица
// графа 3  (3)   - ikksr            - ИНН и КПП или его аналог (при наличии)
// графа 4  (4.1) - sum4             - Сделки с ценными бумагами
// графа 5  (4.2) - sum42            - Сделки купли-продажи иностранной валюты
// графа 6  (4.3) - sum43            - Сделки купли-продажи драгоценных металлов
// графа 7  (4.4) - sum44            - Сделки, отраженные в Журнале взаиморасчетов
// графа 8  (4.5) - sum45            - Сделки с лицами, информация о которых не  отражена в Журнале взаиморасчетов
// графа 9  (4.6) - sum46            - Сумма дополнительно начисленных налогооблагаемых расходов
// графа 10 (5.1) - sum51            - Сделки с ценными бумагами
// графа 11 (5.2) - sum52            - Сделки купли-продажи иностранной валюты
// графа 12 (5.3) - sum53            - Сделки купли-продажи драгоценных металлов
// графа 13 (5.4) - sum54            - Сделки, отраженные в Журнале взаиморасчетов
// графа 14 (5.5) - sum55            - Сделки с лицами, информация о которых не  отражена в Журнале взаиморасчетов
// графа 15 (5.6) - sum56            - Сумма дополнительно начисленных налогооблагаемых расходов
// графа 16 (6)   - sum6             - Итого объем доходов и расходов, руб.
// графа 17 (7)   - category         - Категория Взаимозависимого лица на начало Отчетного периода
// графа 18 (8)   - sum8             - Ожидаемый объем доходов и расходов за отчетный Налоговый период, руб.
// графа 19 (9)   - sum9             - Скорректированный ожидаемый объем доходов и расходов, руб.
// графа 20 (10)  - categoryRevised  - Пересмотренная Категория по итогам Отчетного периода
// графа 21 (11)  - categoryPrimary  - Первичная Категория юридического лица на следующий Налоговый период

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
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
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

// Редактируемые атрибуты
@Field
def editableColumns = []

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['sum4', 'sum42', 'sum43', 'sum44', 'sum45', 'sum46', 'sum51', 'sum52', 'sum53', 'sum54', 'sum55',
                       'sum56', 'sum6', 'category', 'sum8', 'sum9', 'categoryRevised', 'categoryPrimary']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'sum4', 'sum42', 'sum43', 'sum44', 'sum45', 'sum46', 'sum51', 'sum52', 'sum53', 'sum54',
                       'sum55', 'sum56', 'sum6', 'category', 'sum8', 'sum9', 'categoryRevised', 'categoryPrimary']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def prevReportPeriod = null

@Field
def reportPeriod = null

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
                    def boolean required = true) {
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

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // графа 16
        row.sum6 = calc16(row)

        // графа 18
        row.sum8 = calc18(row)

        // графа 19
        row.sum9 = calc19(row)

        // графа 20
        row.categoryRevised = calc20(row)

        // графа 21
        row.categoryPrimary = row.categoryRevised
    }
}

def BigDecimal calc16(def row) {
    // Графа 16 = сумма значений в графах 4-15
    value = 0
    ['sum4', 'sum42', 'sum43', 'sum44', 'sum45', 'sum46', 'sum51', 'sum52', 'sum53', 'sum54', 'sum55', 'sum56'].each {
        value += row[it] ?: 0
    }
    return value
}

def BigDecimal calc18(def row) {
    // Графа 18 = Графа 16 * 4/3
    return (row.sum6 ?: 0) * (4/3)
}

def BigDecimal calc19(def row) {
    // строки формы "Прогноз крупных сделок"
    def sourceRows = getSourceDataRows(810, FormDataKind.PRIMARY)
    def value = 0
    sourceRows.each { sourceRow ->
        if (sourceRow.ikksr == row.name) {
            value += (sourceRow.sum ?: 0)
        }
    }
    return row.sum8 + value
}

def Long calc20(def row) {
    def records = getRecords515()
    for (def record : records) {
        if (record?.MIN_VALUE?.value <= row.sum9 &&
                (record?.MAX_VALUE?.value == null || row.sum9 <= record?.MAX_VALUE?.value)) {
            return record?.CATEGORY?.value
        }
    }
    return null
}

@Field
def records515 = null

// Получить записи "ВЗЛ ОРН" из справочника "Правила назначения категории юридическому лицу"
def getRecords515() {
    if (records515 == null) {
        // получить id записи "ВЗЛ ОРН" из справончика "Типы участников ТЦО (расширенный)"
        def provider = formDataService.getRefBookProvider(refBookFactory, 505L, providerCache)
        def filter = "CODE = 'ВЗЛ ОРН'"
        def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        def id
        if (records && records.size() == 1) {
            id = records.get(0)?.record_id?.value
        } else {
            records515 = []
            return records515
        }

        // получить записи из справончика "Правила назначения категории юридическому лицу"
        provider = formDataService.getRefBookProvider(refBookFactory, 515L, providerCache)
        filter = "CODE = $id"
        records515 = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        if (records515 == null) {
            records515 = []
        }
    }
    return records515
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def records520 = getRecords520()
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Проверка на отсутствие в списке не ВЗЛ ОРН
        def isVZL = records520?.find { it?.record_id?.value == row.name }
        if (records520 && !isVZL) {
            def value2 = getRefBookValue(520L, row.name)?.NAME?.value
            logger.error("Строка %s: Организация «%s» не является взаимозависимым лицом с общим режимом налогообложения в данном отчетном периоде!", rowNum, value2)
        }

        // 3. Наличие правила назначения категории
        def tmp = calc20(row)
        if (tmp == null) {
            logger.error("Строка $rowNum: Для ожидаемого объема доходов и расходов не задано правило назначения категории в данном отчетном периоде!")
        }

        // 4. Проверка соответствия категории пороговым значениям
        if (tmp != row.categoryRevised) {
            logger.error("Строка $rowNum: Для ожидаемого объема доходов и расходов указана неверная категория!")
        }
    }
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

// TODO (Ramil Timerbaev) список неполный, потому что не все макеты реализованы
// список id типов источников
@Field
def sourceFormTypeIds = [
        807, // Журнал взаиморасчетов
        818, // РНУ-101
        820, // РНУ-102
        821, // РНУ-107
             // РНУ-108
        822, // РНУ-110
        808, // РНУ-111
        824, // РНУ-112
        829, // РНУ-114
        842, // РНУ-115
        844, // РНУ-116
        809, // РНУ-117
             // РНУ-120
        840, // РНУ-122
        841, // РНУ-123
        843, // РНУ-171
        816, // 6.1
        804, // 6.2
        812, // 6.3
        813, // 6.4
        814, // 6.5
        806, // 6.6
        805, // 6.7
        815, // 6.8
        817, // 6.9
        823, // 6.10-1
        825, // 6.10-2
        827, // 6.11
        819, // 6.12
        826, // 6.13
        835, // 6.14
        837, // 6.15
        839, // 6.16
        811, // 6.17
        838, // 6.18
        828, // 6.19
        831, // 6.20
        830, // 6.21
        834, // 6.22
        832, // 6.23
        833, // 6.24
        836, // 6.25
]

// Консолидация очень похожа на 4.2, отличие в:
//  - дополнительные условия при получении данных из справочника "Участники ТЦО"
//  - дополнительном расчете (графы 17)
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    // мапа для хранения всех строк каджого типа форм (id типа формы - список строк всех форм этого типа формы)
    def sourceAllDataRowsMap = [:]
    // мапа с formData'ами источников (id типа формы - список formData)
    def sourceFormDatasMap = [:]
    // мапа со строками источников (formData - список строк отдельной формы)
    def sourceDataRowsMap = [:]

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId in sourceFormTypeIds) {
            def reportPeriodId = (it.taxType == TaxType.DEAL ? formData.reportPeriodId : getReportPeriodByTaxType(it.taxType))
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, reportPeriodId, formData.periodOrder, null, false)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                // все строки источников одного типа
                if (sourceAllDataRowsMap[it.formTypeId] == null) {
                    sourceAllDataRowsMap[it.formTypeId] = []
                }
                def rows = formDataService.getDataRowHelper(source).allSaved
                sourceAllDataRowsMap[it.formTypeId].addAll(rows)

                // formData'ы источников
                if (sourceFormDatasMap[it.formTypeId] == null) {
                    sourceFormDatasMap[it.formTypeId] = []
                }
                sourceFormDatasMap[it.formTypeId].add(source)

                // строки источников
                sourceDataRowsMap[source] = rows
            }
        }
    }

    // получить значения из справочника "Участники ТЦО"
    def records520 = getRecords520()
    def dataRows = []
    records520.each { record520 ->
        // для каждой записи сформировать строку в приемнике
        def newRow = getNewRow(record520, sourceAllDataRowsMap, sourceFormDatasMap, sourceDataRowsMap)
        dataRows.add(newRow)
    }

    sortRows(refBookService, logger, dataRows, null, null, null)
    updateIndexes(dataRows)
    dataRowHelper.allCached = dataRows
}

/**
 * Получить новую строку при консолидации.
 *
 * @param record520 запись из справочнкиа "Участники ТЦО"
 * @param sourceAllDataRowsMap мапа со всеми строками источников одного типа
 * @param sourceFormDatasMap мапа со списками formData источников
 * @param sourceDataRowsMap мапа со строками источников
 */
def getNewRow(def record520, def sourceAllDataRowsMap, def sourceFormDatasMap, def sourceDataRowsMap) {
    def newRow = formData.createDataRow()

    // графа 2
    newRow.name = record520?.record_id?.value

    // графа 4
    newRow.sum4 = calc4(record520, sourceAllDataRowsMap)

    // графа 5
    newRow.sum42 = calc5(record520, sourceAllDataRowsMap)

    // графа 6
    newRow.sum43 = calc6(record520, sourceAllDataRowsMap)

    // графа 7
    newRow.sum44 = calc7(record520, sourceFormDatasMap, sourceDataRowsMap)

    // графа 8
    newRow.sum45 = calc8(record520, sourceAllDataRowsMap)

    // графа 9
    newRow.sum46 = calc9(record520, sourceAllDataRowsMap)

    // графа 10
    newRow.sum51 = calc10(record520, sourceAllDataRowsMap)

    // графа 11
    newRow.sum52 = calc11(record520, sourceAllDataRowsMap)

    // графа 12
    newRow.sum53 = calc12(record520, sourceAllDataRowsMap)

    // графа 13
    newRow.sum54 = calc13(record520, sourceFormDatasMap, sourceDataRowsMap)

    // графа 14
    newRow.sum55 = calc14(record520, sourceAllDataRowsMap)

    // графа 15
    newRow.sum56 = calc15(record520, sourceAllDataRowsMap)

    // графа 17
    newRow.category = calc17(record520)

    return newRow
}

def calc4(def record520, def sourceAllDataRowsMap) {
    return calc4or10(record520, sourceAllDataRowsMap, true)
}

def calc5(def record520, def sourceAllDataRowsMap) {
    return calc5or11(record520, sourceAllDataRowsMap, true)
}

def calc6(def record520, def sourceAllDataRowsMap) {
    return calc6or12(record520, sourceAllDataRowsMap, true)
}

def calc7(def record520, def sourceFormDatasMap, def sourceDataRowsMap) {
    return calc7or13(record520, sourceFormDatasMap, sourceDataRowsMap, true)
}

def calc8(def record520, def sourceAllDataRowsMap) {
    return calc8or14(record520, sourceAllDataRowsMap, true)
}

def calc9(def record520, def sourceAllDataRowsMap) {
    return calc9or15(record520, sourceAllDataRowsMap, true)
}

def calc10(def record520, def sourceAllDataRowsMap) {
    return calc4or10(record520, sourceAllDataRowsMap, false)
}

def calc11(def record520, def sourceAllDataRowsMap) {
    return calc5or11(record520, sourceAllDataRowsMap, false)
}

def calc12(def record520, def sourceAllDataRowsMap) {
    return calc6or12(record520, sourceAllDataRowsMap, false)
}

def calc13(def record520, def sourceFormDatasMap, def sourceDataRowsMap) {
    return calc7or13(record520, sourceFormDatasMap, sourceDataRowsMap, false)
}

def calc14(def record520, def sourceAllDataRowsMap) {
    return calc8or14(record520, sourceAllDataRowsMap, false)
}

def calc15(def record520, def sourceAllDataRowsMap) {
    return calc9or15(record520, sourceAllDataRowsMap, false)
}

def calc4or10(def record520, def sourceAllDataRowsMap, def isCalc4) {
    def result = 0
    def formTypeIds = [
            806, // 6.6
            827, // 6.11
            819  // 6.12
    ]
    formTypeIds.each { formTypeId ->
        def rows = sourceAllDataRowsMap[formTypeId]
        for (def row : rows) {
            if (row.name == record520?.record_id?.value) {
                def date = formTypeId == 827 ? row.dealDate : row.dealDoneDate
                if (!checkRow(date, record520)) {
                    continue
                }
                switch (formTypeId) {
                    case 806 : // 6.6
                    case 819 : // 6.12
                        result += ((isCalc4 ? row.incomeSum : row.outcomeSum) ?: 0)
                        break
                    case 827 : // 6.11
                        def transactionType = getRefBookValue(16L, row.transactionType)?.CODE?.value
                        if (isCalc4) {
                            result += (transactionType == 'S' ? row.sum : 0)
                        } else {
                            result += (transactionType == 'B' ? row.sum : 0)
                        }
                        break
                }
            }
        }
    }
    return result
}

def calc5or11(def record520, def sourceAllDataRowsMap, def isCalc5) {
    def result = 0
    def formTypeIds = [
            835, // 6.14
            839, // 6.16
            811  // 6.17
    ]
    formTypeIds.each { formTypeId ->
        def rows = sourceAllDataRowsMap[formTypeId]
        for (def row : rows) {
            if (row.name == record520?.record_id?.value && checkRow(row.dealDoneDate, record520)) {
                switch (formTypeId) {
                    case 835 : // 6.14
                    case 811 : // 6.17
                        if (isCalc5) {
                            result += (row.income >= row.outcome ? row.cost : 0)
                        } else {
                            result += (row.income < row.outcome ? row.cost : 0)
                        }
                        break
                    case 839 : // 6.16
                        if (isCalc5) {
                            result += (row.incomeSum >= row.outcomeSum ? row.cost : 0)
                        } else {
                            result += (row.incomeSum < row.outcomeSum ? row.cost : 0)
                        }
                        break
                }
            }
        }
    }
    return result
}

def calc6or12(def record520, def sourceAllDataRowsMap, def isCalc6) {
    def result = 0
    def formTypeIds = [
            804, // 6.2
            837, // 6.15
            838, // 6.18
            831  // 6.20
    ]
    formTypeIds.each { formTypeId ->
        def rows = sourceAllDataRowsMap[formTypeId]
        for (def row : rows) {
            if (row.name == record520?.record_id?.value && checkRow(row.dealDoneDate, record520)) {
                switch (formTypeId) {
                    case 804 : // 6.2
                        if (isCalc6) {
                            result += (row.sum ?: 0)
                        }
                        break
                    case 837 : // 6.15
                        if (isCalc6) {
                            result += (row.income >= row.outcome ? row.cost : 0)
                        } else {
                            result += (row.income < row.outcome ? row.cost : 0)
                        }
                        break
                    case 838 : // 6.18
                        if (isCalc6) {
                            result += (row.incomeSum >= row.outcomeSum ? row.total : 0)
                        } else {
                            result += (row.incomeSum < row.outcomeSum ? row.total : 0)
                        }
                        break
                    case 831 : // 6.20
                        if (!isCalc6) {
                            result += (row.outcome ?: 0)
                        }
                        break
                }
            }
        }
    }
    return result
}

def calc7or13(def record520, def sourceFormDatasMap, def sourceDataRowsMap, def isCalc7) {
    def result = 0
    def formTypeId = 807
    sourceFormDatas = sourceFormDatasMap[formTypeId]
    sourceFormDatas.each { sourceFormData ->
        def rows = getNeedRowsForCalc7or13(sourceDataRowsMap[sourceFormData], isCalc7)
        for (def row : rows) {
            if (row.sbrfCode1 && row.statReportId2 == record520?.record_id?.value) {
                result += (row.sum ?: 0)
            }
        }
    }
    return result * 1000
}

def getNeedRowsForCalc7or13(def dataRows, def isCalc7) {
    def rows = []
    def findSection = isCalc7
    for (def row : dataRows) {
        if (isCalc7) {
            // строки доходов
            if (!row.getAlias()) {
                rows.add(row)
            }
            if (row.getAlias() == '99.1') {
                break
            }
        } else {
            // строки расходов
            if (row.getAlias() == '99.1') {
                findSection = true
            }
            if (findSection && !row.getAlias()) {
                rows.add(row)
            }
            if (row.getAlias() == '99.2') {
                break
            }
        }
    }
    return rows
}

def calc8or14(def record520, def sourceAllDataRowsMap, def isCalc8) {
    def result = 0
    def formTypeIds = [
            816, // 6.1
            812, // 6.3
            813, // 6.4
            814, // 6.5
            805, // 6.7
            815, // 6.8
            817, // 6.9
            823, // 6.10-1
            825, // 6.10-2
            826, // 6.13
            828, // 6.19
            830, // 6.21
            834, // 6.22
            832, // 6.23
            833, // 6.24
            836, // 6.25
    ]
    formTypeIds.each { formTypeId ->
        def rows = sourceAllDataRowsMap[formTypeId]
        for (def row : rows) {
            if (row.name == record520?.record_id?.value && checkRow(row.dealDoneDate, record520)) {
                switch (formTypeId) {
                    case 816 : // 6.1
                    case 812 : // 6.3
                    case 813 : // 6.4
                    case 805 : // 6.7
                    case 823 : // 6.10-1
                    case 825 : // 6.10-2
                    case 832 : // 6.23
                        if (isCalc8) {
                            result += (row.sum ?: 0)
                        }
                        break
                    case 814 : // 6.5
                    case 815 : // 6.8
                    case 828 : // 6.19
                    case 830 : // 6.21
                    case 834 : // 6.22
                    case 833 : // 6.24
                        if (!isCalc8) {
                            result += (row.sum ?: 0)
                        }
                        break
                    case 817 : // 6.9
                        if (isCalc8) {
                            result += (row.finResult ?: 0)
                        }
                        break
                    case 836 : // 6.25
                        if (!isCalc8) {
                            result += (row.finResult ?: 0)
                        }
                        break
                    case 826 : // 6.13
                        if (!isCalc8) {
                            result += (row.outcomeSum ?: 0)
                        }
                        break
                }
            }
        }
    }
    return result
}

def calc9or15(def record520, def sourceAllDataRowsMap, def isCalc9) {
    def result = 0
    def formTypeIds = [
            818, // РНУ-101
            820, // РНУ-102
            821, // РНУ-107
                 // РНУ-108
            822, // РНУ-110
            808, // РНУ-111
            824, // РНУ-112
            829, // РНУ-114
            842, // РНУ-115
            844, // РНУ-116
            809, // РНУ-117
                 // РНУ-120
            840, // РНУ-122
            841, // РНУ-123
            843  // РНУ-171
    ]
    formTypeIds.each { formTypeId ->
        def rows = sourceAllDataRowsMap[formTypeId]
        for (def row : rows) {
            if (row.name == record520?.record_id?.value) {
                switch (formTypeId) {
                    case 818 : // РНУ-101
                    case 822 : // РНУ-110
                    case 808 : // РНУ-111
                        if (isCalc9) {
                            result += (row.sum3 ?: 0)
                        }
                        break
                    case 820 : // РНУ-102
                    case 809 : // РНУ-117
                        if (!isCalc9) {
                            result += (row.sum3 ?: 0)
                        }
                        break
                    case 821 : // РНУ-107
                        if (isCalc9) {
                            result += (row.sum4 ?: 0)
                        }
                        break
                    case 824 : // РНУ-112
                        if (isCalc9) {
                            result += (row.incomeCorrection ?: 0)
                        }
                        break
                    case 829 : // РНУ-114
                        if (isCalc9) {
                            result += (row.sum1 ?: 0)
                        }
                        break
                    case 842 : // РНУ-115
                    case 844 : // РНУ-116
                        if (isCalc9) {
                            result += (row.incomeDelta?.abs() ?: 0)
                        } else {
                            result += (row.outcomeDelta?.abs() ?: 0)
                        }
                        break
                    case 840 : // РНУ-122
                        if ((isCalc9 && "10345".equals(row.code)) ||
                                (!isCalc9 && "10355".equals(row.code))) {
                            result += (row.sum6 ?: 0)
                        }
                        break
                    case 841 : // РНУ-123
                        if (isCalc9) {
                            result += (row.sum10 ?: 0)
                        }
                        break
                    case 845 : // РНУ-171
                        if ((isCalc9 && "10360".equals(row.code)) ||
                                (!isCalc9 && "10361".equals(row.code))) {
                            result += (row.incomeCorrection ?: 0)
                        }
                        break
                // TODO (Timerbaev/Kinzyabulatov) пока не реализованы макеты
                              // РНУ-108
                              // РНУ-120
                }
            }
        }
    }
    return result
}

def calc17(def record520) {
    // формы ВЗЛ за предыдущий период
    def rows = getSourceDataRows(800, FormDataKind.PRIMARY, true)
    if (rows == null) {
        return getDefaultCategory()
    }
    // мапа для хранения всех версии записи (строка нф - список всех версии записи "участников ТЦО")
    def record520Map = getVersionRecords520Map(rows)
    def findRow = findPrevRow(record520?.record_id?.value, record520Map)
    return (findRow ? findRow.category : getDefaultCategory())
}

@Field
def category4 = null

// Получить id записи "Категория 4" справочника "Категории юридического лица по системе "светофор""
def getDefaultCategory() {
    if (category4 == null) {
        def provider = formDataService.getRefBookProvider(refBookFactory, 506L, providerCache)
        def filter = "CODE = 'Категория 4'"
        def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        if (records != null && !records.isEmpty() && records.size() == 1) {
            category4 = records.get(0)?.record_id?.value
        }
    }
    return category4
}

@Field
def sourceDataRowsMap = [:]

/** Получить строки за предыдущий отчетный период. */
def getSourceDataRows(int formTypeId, FormDataKind kind, boolean isPrevPeriod = false) {
    def key = formTypeId.toString() + '#' + kind + '#' + isPrevPeriod
    if (sourceDataRowsMap[key] != null) {
        return sourceDataRowsMap[key]
    }
    // период - текущйи или предыдущий
    def reportPeriod = (isPrevPeriod ? getPrevReportPeriod() : getReportPeriod())
    def fd = null
    if (reportPeriod?.id) {
        fd = formDataService.getLast(formTypeId, kind, formData.departmentId, reportPeriod?.id, null, null, false)
    }
    if (fd == null || fd.state != WorkflowState.ACCEPTED) {
        sourceDataRowsMap[key] = null
    } else {
        sourceDataRowsMap[key] = formDataService.getDataRowHelper(fd)?.allSaved
    }
    return sourceDataRowsMap[key]
}

def getPrevReportPeriod() {
    if (prevReportPeriod == null) {
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    }
    return prevReportPeriod
}

ReportPeriod getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return reportPeriod
}

@Field
def versionRecords520Maps = [:]

/**
 * Получить мапу со всеми версиями для каждой записи справочника (строка нф - список всех версии записи "участников ТЦО").
 * Потом используется в методе findPrevRow().
 *
 * Необходимо потому что в предыдущем периоде у записи справочника могла быть другая версия записи,
 * и id в ячейках форм будут отличаться, поэтому для нахождения соответствия между двумя версиями записи в разных периодах используются все версии записи.
 *
 * @param prevRows строки за предыдущий период
 */
def getVersionRecords520Map(def prevRows) {
    if (versionRecords520Maps[prevRows]) {
        return versionRecords520Maps[prevRows]
    }
    def map = [:]
    prevRows.each { row ->
        def recordId = row.name
        // все версии записи
        def versionRecords = getRecordVersionsById(recordId)
        map[row] = versionRecords
    }
    versionRecords520Maps[prevRows] = map
    return versionRecords520Maps[prevRows]
}

@Field
def versionRecords = [:]

def getRecordVersionsById(def recordId) {
    if (versionRecords[recordId] == null) {
        def provider = formDataService.getRefBookProvider(refBookFactory, 520L, providerCache)
        versionRecords[recordId] = provider.getRecordVersionsById(recordId, null, null, null)
    }
    return versionRecords[recordId]
}

/**
 * Найти запись из предыдущего периода, которая ссылается на версию одной записи справочника.
 *
 * @param recordId идентификатор на версию записи (простой record_id записи)
 * @param versionRecords520Map мапа полученная из метода getVersionRecords520Map()
 */
def findPrevRow(def recordId, def versionRecords520Map) {
    def prevFindRow = null
    for (def prevRow : versionRecords520Map.keySet().toArray()) {
        def records = versionRecords520Map[prevRow]
        // если среди версии записи есть подходящая, то строка найдена
        def find = records.find { it?.record_id?.value == recordId }
        if (find) {
            prevFindRow = prevRow
            break
        }
    }
    return prevFindRow
}

@Field
def records520 = null

// Получить значения из справочника "Участники ТЦО".
def getRecords520() {
    if (records520 != null) {
        return records520
    }
    // получить id записи с кодом "2" из справончика "Специальный налоговый статус"
    def provider = formDataService.getRefBookProvider(refBookFactory, 511L, providerCache)
    def filter = "CODE = 2"
    def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
    def taxStatusId
    if (records && records.size() == 1) {
        taxStatusId = records.get(0)?.record_id?.value
    } else {
        records520 =[]
        return records520
    }

    // получить записи из справончика "Участники ТЦО"
    provider = formDataService.getRefBookProvider(refBookFactory, 520L, providerCache)
    filter = "TAX_STATUS = $taxStatusId"
    records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
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
def isVZL(def start, def end, typeId) {
    if (start <= getReportPeriodEndDate() &&
            (end == null || (end >= getReportPeriodStartDate() && end <= getReportPeriodEndDate())) &&
            getRefBookValue(525L, typeId)?.CODE?.value == "ВЗЛ") {
        return true
    }
    return false
}

/**
 * Проверить необходимость использования строки источника.
 * Для строк с участником ТЦО дату совершения сделки.
 *
 * @param date дата совершения сделки
 * @param record запись справочника "Участники ТЦО"
 * @return true - если дата входит в период нахождения организации в списке ВЗЛ
 */
def checkRow(def date, def record) {
    if (record == null) {
        return false
    }
    // для организации с типом отличном от ВЗЛ - использовать все записи
    if (getVZL() != record?.TYPE?.value) {
        return true
    }
    // для организации с типом ВЗЛ - использовать записи подходящие по условиям
    def start = record?.START_DATE?.value
    def end = record?.END_DATE?.value
    if (start <= date && (end == null || date <= end)) {
        return true
    }
    return false
}

@Field
def recordVZLId = null

/** Получить id записи ВЗЛ справочника "Типы участников ТЦО" (525). */
def getVZL() {
    if (recordVZLId == null) {
        def records = getRecordsByRefbookId(525L)
        def recordVZL = records.find { it?.CODE?.value == 'ВЗЛ' }
        recordVZLId = recordVZL?.record_id?.value
    }
    return recordVZLId
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

@Field
def reportPeriodIdsMap = [:]

def getReportPeriodByTaxType(def taxType) {
    if (reportPeriodIdsMap[taxType] == null) {
        def periods = reportPeriodService.getReportPeriodsByDate(taxType, getReportPeriodEndDate(), getReportPeriodEndDate())
        if (periods) {
            reportPeriodIdsMap[taxType] = periods[periods.size() - 1].id
        }
    }
    return reportPeriodIdsMap[taxType]
}