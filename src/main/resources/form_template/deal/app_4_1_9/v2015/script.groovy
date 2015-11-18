package form_template.deal.app_4_1_9.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
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
            return null
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

        // 2. Отсутствие нулевых значений
        if (calc16(row) == 0) {
            logger.error("Строка $rowNum: Объем доходов и расходов по всем сделкам не может быть нулевым!")
        }

        // 3. Проверка на отсутствие в списке не ВЗЛ ОРН
        def isVZL = records520?.find { it?.record_id?.value == row.name }
        if (records520 && !isVZL) {
            def value2 = getRefBookValue(520L, row.name)?.NAME?.value
            logger.error("Строка %s: Организация «%s» не является взаимозависимым лицом с общим режимом налогообложения в данном отчетном периоде!", rowNum, value2)
        }

        // 4. Наличие правила назначения категории
        def tmp = calc20(row)
        if (tmp == null) {
            logger.error("Строка $rowNum: Для ожидаемого объема доходов и расходов не задано правило назначения категории в данном отчетном периоде!")
        }

        // 5. Проверка соответствия категории пороговым значениям
        if (tmp != row.categoryRevised) {
            def value2 = getRefBookValue(520L, row.name)?.NAME?.value
            logger.error("Строка %s: Организация «%s» не является взаимозависимым лицом в данном отчетном периоде!", rowNum, value2)
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

// TODO (Ramil Timerbaev) мапа неполная, потому что не все макеты реализованы
// мапа в которой хранится id формы - список алиасов ссылающихся на справочник "участники ТЦО"
@Field
def sourceRefbook520AliasMap = [
        // Журнал взаиморасчетов
        807 : ['statReportId1', 'statReportId2'],

                        // формы РНУ
                        // РНУ-101
                        // РНУ-102
                        // РНУ-107
                        // РНУ-108
                        // РНУ-110
        808 : ['name'], // РНУ-111
                        // РНУ-112
                        // РНУ-114
                        // РНУ-115
                        // РНУ-116
        809 : ['name'], // РНУ-117
                        // РНУ-120
                        // РНУ-122
                        // РНУ-123
                        // РНУ-171

        // формы приложений 6
                        // 6.1
        804 : ['name'], // 6.2
                        // 6.3
                        // 6.4
                        // 6.5
        806 : ['name'], // 6.6
        805 : ['name'], // 6.7
                        // 6.8
                        // 6.9
                        // 6.10-1
                        // 6.10-2
                        // 6.11
                        // 6.12
                        // 6.13
                        // 6.14
                        // 6.15
                        // 6.16
                        // 6.17
                        // 6.18
                        // 6.19
                        // 6.20
                        // 6.21
                        // 6.22
                        // 6.23
                        // 6.24
                        // 6.25
]

// Консолидация очень похожа на 4.2, отличие в:
//  - дополнительные условия при получении данных из справочника "Участники ТЦО"
//  - дополнительном расчете (графы 17)
//  - удаление нулевых строк в конце консолидации
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    // мапа для хранения всех строк каджого типа форм (id типа формы - список строк всех форм этого типа формы)
    def sourceAllDataRowsMap = [:]
    // мапа с formData'ами источников (id типа формы - список formData)
    def sourceFormDatasMap = [:]
    // мапа со строками источников (formData - список строк отдельной формы)
    def sourceDataRowsMap = [:]
    // id типов источников
    def sourceFormTypeIds = sourceRefbook520AliasMap.keySet().toArray()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId in sourceFormTypeIds) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, null, false)
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

    // если источников нет, то выход
    if (sourceFormDatasMap.isEmpty()) {
        dataRowHelper.allCached = []
        return
    }

    // получить значения из справочника "Участники ТЦО"
    def records520 = getRecords520()
    def dataRows = []
    // найти среди строк источников используемые записи справочника "Участники ТЦО"
    def useIds = getUseRecord520IsFromSources(sourceAllDataRowsMap)
    records520.each { record520 ->
        // если запись используется хотя бы в одном источнике то сформироавть для нее строку в приемнике
        if (record520?.record_id?.value in useIds) {
            def newRow = getNewRow(record520, sourceAllDataRowsMap, sourceFormDatasMap, sourceDataRowsMap)
            dataRows.add(newRow)
        }
    }

    // удалить строки у которых в графе 4..15 все нули
    def deleteRows = []
    def checkColumns = ['sum4', 'sum42', 'sum43', 'sum44', 'sum45', 'sum46', 'sum51', 'sum52', 'sum53', 'sum54', 'sum55', 'sum56']
    dataRows.each { row ->
        def allZero = true
        for (def alias : checkColumns) {
            if (row[alias]) {
                allZero = false
                break
            }
        }
        if (allZero) {
            deleteRows.add(row)
        }
    }
    dataRows.removeAll(deleteRows)

    sortRows(refBookService, logger, dataRows, null, null, null)
    updateIndexes(dataRows)
    dataRowHelper.allCached = dataRows
}

/**
 * Получить список идентификаторов записи справочника "Участники ТЦО" используемых в источниках.
 *
 * @param sourceAllDataRowsMap мапа со всеми строками источников одного типа
 */
def getUseRecord520IsFromSources(def sourceAllDataRowsMap) {
    def list = []
    def formTypeIds = sourceAllDataRowsMap.keySet().toArray()
    for (def formTypeId : formTypeIds) {
        def rows = sourceAllDataRowsMap[formTypeId]
        def aliases = sourceRefbook520AliasMap[formTypeId]
        for (def row : rows) {
            for (def alias : aliases) {
                if (row[alias]) {
                    list.add(row[alias])
                }
            }
        }
    }
    return list.unique()
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
    def recordId = record520?.record_id?.value

    // графа 2
    newRow.name = recordId

    // графа 4
    newRow.sum4 = calc4(recordId, sourceAllDataRowsMap)

    // графа 5
    newRow.sum42 = calc5(recordId, sourceAllDataRowsMap)

    // графа 6
    newRow.sum43 = calc6(recordId, sourceAllDataRowsMap)

    // графа 7
    newRow.sum44 = calc7(recordId, sourceFormDatasMap, sourceDataRowsMap)

    // графа 8
    newRow.sum45 = calc8(recordId, sourceAllDataRowsMap)

    // графа 9
    newRow.sum46 = calc9(record520, sourceAllDataRowsMap)

    // графа 10
    newRow.sum51 = calc10(recordId, sourceAllDataRowsMap)

    // графа 11
    newRow.sum52 = calc11(recordId, sourceAllDataRowsMap)

    // графа 12
    newRow.sum53 = calc12(recordId, sourceAllDataRowsMap)

    // графа 13
    newRow.sum54 = calc13(recordId, sourceFormDatasMap, sourceDataRowsMap)

    // графа 14
    newRow.sum55 = calc14(recordId, sourceAllDataRowsMap)

    // графа 15
    newRow.sum56 = calc15(record520, sourceAllDataRowsMap)

    // графа 17
    newRow.category = calc17(recordId)

    return newRow
}

def calc4(def record520Id, def sourceAllDataRowsMap) {
    return calc4or10(record520Id, sourceAllDataRowsMap, true)
}

def calc5(def record520Id, def sourceAllDataRowsMap) {
    return calc5or11(record520Id, sourceAllDataRowsMap, true)
}

def calc6(def record520Id, def sourceAllDataRowsMap) {
    return calc6or12(record520Id, sourceAllDataRowsMap, true)
}

def calc7(def record520Id, def sourceFormDatasMap, def sourceDataRowsMap) {
    return calc7or13(record520Id, sourceFormDatasMap, sourceDataRowsMap, true)
}

def calc8(def record520Id, def sourceAllDataRowsMap) {
    return calc8or14(record520Id, sourceAllDataRowsMap, true)
}

def calc9(def record520, def sourceAllDataRowsMap) {
    return calc9or15(record520, sourceAllDataRowsMap, true)
}

def calc10(def record520Id, def sourceAllDataRowsMap) {
    return calc4or10(record520Id, sourceAllDataRowsMap, false)
}

def calc11(def record520Id, def sourceAllDataRowsMap) {
    return calc5or11(record520Id, sourceAllDataRowsMap, false)
}

def calc12(def record520Id, def sourceAllDataRowsMap) {
    return calc6or12(record520Id, sourceAllDataRowsMap, false)
}

def calc13(def record520Id, def sourceFormDatasMap, def sourceDataRowsMap) {
    return calc7or13(record520Id, sourceFormDatasMap, sourceDataRowsMap, false)
}

def calc14(def record520Id, def sourceAllDataRowsMap) {
    return calc8or14(record520Id, sourceAllDataRowsMap, false)
}

def calc15(def record520, def sourceAllDataRowsMap) {
    return calc9or15(record520, sourceAllDataRowsMap, false)
}

def calc4or10(def record520Id, def sourceAllDataRowsMap, def isCalc4) {
    def result = 0
    def formTypeIds = [
            806, // 6.6
                 // 6.11
    ]
    formTypeIds.each { formTypeId ->
        def rows = sourceAllDataRowsMap[formTypeId]
        for (def row : rows) {
            if (row.name == record520Id) {
                if (806 == formTypeId) {
                    // 6.6
                    if (row.incomeSum && row.outcomeSum) {
                        result += (isCalc4 ? row.incomeSum - row.outcomeSum : row.outcomeSum - row.incomeSum)
                    } else {
                        result += ((isCalc4 ? row.incomeSum : row.outcomeSum) ?: 0)
                    }
                } else {
                    // 6.11
                    // TODO (Ramil Timerbaev) пока не реализован макет
                }
            }
        }
    }
    return result
}

def calc5or11(def record520Id, def sourceAllDataRowsMap, def isCalc5) {
    def result = 0
    def formTypeIds = [
            // 6.14
            // 6.16
            // 6.17
    ]
    formTypeIds.each { formTypeId ->
        def rows = sourceAllDataRowsMap[formTypeId]
        for (def row : rows) {
            if (row.name == record520Id) {
                // 6.14
                // 6.14
                // 6.16
                // 6.17
                // TODO (Ramil Timerbaev) пока не реализованы макеты
            }
        }
    }
    return result
}

def calc6or12(def record520Id, def sourceAllDataRowsMap, def isCalc6) {
    def result = 0
    def formTypeIds = [
            804, // 6.2
                 // 6.15
                 // 6.18
                 // 6.20
    ]
    formTypeIds.each { formTypeId ->
        def rows = sourceAllDataRowsMap[formTypeId]
        for (def row : rows) {
            if (row.name == record520Id) {
                if (804 == formTypeId) {
                    // 6.2
                    if (isCalc6) {
                        result += (row.sum ?: 0)
                    }
                } else {
                    // 6.15
                    // 6.18
                    // TODO (Ramil Timerbaev) пока не реализованы макеты
                }
            }
        }
    }
    return result
}

def calc7or13(def record520Id, def sourceFormDatasMap, def sourceDataRowsMap, def isCalc7) {
    def result = 0
    def formTypeId = 807
    sourceFormDatas = sourceFormDatasMap[formTypeId]
    sourceFormDatas.each { sourceFormData ->
        def rows = getNeedRowsForCalc7or13(sourceDataRowsMap[sourceFormData], isCalc7)
        for (def row : rows) {
            if (row.sbrfCode1 && row.statReportId2 == record520Id) {
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

def calc8or14(def record520Id, def sourceAllDataRowsMap, def isCalc8) {
    def result = 0
    def formTypeIds = [
                 // 6.1
                 // 6.3
                 // 6.4
                 // 6.5
            805, // 6.7
                 // 6.8
                 // 6.9
                 // 6.10-1
                 // 6.10-2
                 // 6.12
                 // 6.13
                 // 6.19
                 // 6.21
                 // 6.22
                 // 6.23
                 // 6.24
                 // 6.25
    ]
    formTypeIds.each { formTypeId ->
        def rows = sourceAllDataRowsMap[formTypeId]
        for (def row : rows) {
            if (row.name == record520Id) {
                if (805 == formTypeId) {
                    // 6.7
                    if (isCalc8) {
                        result += (row.sum ?: 0)
                    }
                } else {
                    // TODO (Ramil Timerbaev) пока не реализованы макеты
                }
            }
        }
    }
    return result
}

def calc9or15(def record520, def sourceAllDataRowsMap, def isCalc9) {
    def result = 0
    def formTypeIds = [
                 // РНУ-101
                 // РНУ-102
                 // РНУ-107
                 // РНУ-108
                 // РНУ-110
            808, // РНУ-111
                 // РНУ-112
                 // РНУ-114
                 // РНУ-115
                 // РНУ-116
            809, // РНУ-117
                 // РНУ-120
                 // РНУ-122
                 // РНУ-123
                 // РНУ-171
    ]
    formTypeIds.each { formTypeId ->
        def rows = sourceAllDataRowsMap[formTypeId]
        for (def row : rows) {
            if (checkRnuRow(row, record520?.NAME?.value)) {
                if (808 == formTypeId) {
                    // рну 111
                    if (isCalc9) {
                        result += (row.sum3 ?: 0)
                    }
                } else if (809 == formTypeId) {
                    // рну 117
                    if (isCalc9) {
                        result += (row.sum3 ?: 0)
                    }
                } else {
                    // TODO (Ramil Timerbaev) пока не реализованы макеты
                }
            }
        }
    }
    return result
}

/**
 * Проверить является ли строка источника рну подитоговой и относится ли к указанному участнику.
 *
 * @param row строка
 * @param name имя участника
 * @return
 */
def checkRnuRow(def row, def name) {
    def head = 'Итого по "'
    if (!row.getAlias() || !row.fix || !row.fix.contains(head)) {
        return false
    }
    def start = row.fix.indexOf(head) + head.size()
    def end = row.fix.size() - 1
    return row.fix.substring(start, end).equals(StringUtils.cleanString(name))
}

def calc17(def record520Id) {
    // формы ВЗЛ за предыдущий период
    def rows = getSourceDataRows(800, FormDataKind.PRIMARY, true)
    if (!rows) {
        return getDefaultCategory()
    }
    // мапа для хранения всех версии записи (строка нф - список всех версии записи "участников ТЦО")
    def record520Map = getVersionRecords520Map(rows)
    def findRow = findPrevRow(record520Id, record520Map)
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
        sourceDataRowsMap[key] = []
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
    def versionRecords520Map = [:]
    prevRows.each { row ->
        def provider = formDataService.getRefBookProvider(refBookFactory, 520L, providerCache)
        def recordId = row.name
        // все версии записи
        def versionRecords = provider.getRecordVersionsById(recordId, null, null, null)
        versionRecords520Map[row] = versionRecords
    }
    return versionRecords520Map
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