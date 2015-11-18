package form_template.deal.app_4_2.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * 803 - Приложение 4.2. Отчет в отношении доходов и расходов Банка по сделкам с ВЗЛ, РОЗ, НЛ по итогам окончания Налогового периода
 *
 * formTemplateId=803
 *
 * TODO:
 *      - консолидация не полная, потому что не все макеты источников готовы
 *      - дополнить тесты
 */

// графа 1  (1)   - rowNumber        - № п/п
// графа 2  (2)   - name             - Полное наименование и ОПФ юридического лица
// графа 3  (3)   - ikksr            - ИНН и КПП или его аналог (при наличии)
// графа 4  (4)   - group            - Группа юридического лица
// графа 5  (5.1) - sum51            - Сделки с ценными бумагами
// графа 6  (5.2) - sum52            - Сделки купли-продажи иностранной валюты
// графа 7  (5.3) - sum53            - Сделки купли-продажи драгоценных металлов
// графа 8  (5.4) - sum54            - Сделки, отраженные в Журнале взаиморасчетов
// графа 9  (5.5) - sum55            - Сделки с лицами, информация о которых не  отражена в Журнале взаиморасчетов
// графа 10 (5.6) - sum56            - Сумма дополнительно начисленных налогооблагаемых расходов
// графа 11 (6.1) - sum61            - Сделки с ценными бумагами
// графа 12 (6.2) - sum62            - Сделки купли-продажи иностранной валюты
// графа 13 (6.3) - sum63            - Сделки купли-продажи драгоценных металлов
// графа 14 (6.4) - sum64            - Сделки, отраженные в Журнале взаиморасчетов
// графа 15 (6.5) - sum65            - Сделки с лицами, информация о которых не  отражена в Журнале взаиморасчетов
// графа 16 (6.6) - sum66            - Сумма дополнительно начисленных налогооблагаемых расходов
// графа 17 (7)   - sum7             - Итого объем доходов и расходов, руб.
// графа 18 (8)   - thresholdValue   - Применимое Пороговое значение, руб.
// графа 19 (9)   - sign             - Признак наличия Контролируемых сделок (Да / Нет)
// графа 20 (10)  - categoryRevised  - Пересмотренная Категория юридического лица по состоянию на 1 апреля следующего Налогового периода

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
def autoFillColumns = ['name', 'group', 'sum51', 'sum52', 'sum53', 'sum54', 'sum55', 'sum56', 'sum61', 'sum62', 'sum63',
                       'sum64', 'sum65', 'sum66', 'sum7', 'thresholdValue', 'sign', 'categoryRevised']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'group', 'sum51', 'sum52', 'sum53', 'sum54', 'sum55', 'sum56', 'sum61', 'sum62', 'sum63',
                       'sum64', 'sum65', 'sum66', 'sum7', 'thresholdValue', 'sign', 'categoryRevised']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

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

// "Да"
@Field
def Long recYesId

def Long getRecYesId() {
    if (recYesId == null)
        recYesId = getRecordId(38, 'CODE', '1')
    return recYesId
}

// "Нет"
@Field
def Long recNoId

def Long getRecNoId() {
    if (recNoId == null)
        recNoId = getRecordId(38, 'CODE', '0')
    return recNoId
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

// Поиск записи в справочнике по значению (для расчетов)
def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), -1, null, logger, true)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // графа 4
        row.group = calc4(row)

        // графа 17
        row.sum7 = calc17(row)

        // графа 18
        row.thresholdValue = calc18(row)

        // графа 19
        row.sign = calc19(row)

        // графа 20
        row.categoryRevised = calc20(row)
    }
}

def Long calc4(def row) {
    if (row.name == null) {
        return null
    }
    // записи из справочника "Участники ТЦО"
    def records520 = getRecordsByRefbookId(520L)
    def record520 = records520?.find { it?.record_id?.value == row.name }
    if (record520?.TYPE?.value == null) {
        return null
    }
    def code = null

    // записи из справочника "Типы участников ТЦО"
    def records525 = getRecordsByRefbookId(525L)
    def record525 =records525?.find { it?.record_id?.value == record520?.TYPE?.value }
    if (record525?.CODE?.value && record525?.CODE?.value != 'ВЗЛ') {
        // РОЗ и НЛ
        code = record525?.CODE?.value
    } else if (record520?.ORG_CODE?.value != null) {
        // ВЗЛ ОРН, ВЗЛ СРН, ИВЗЛ
        // записи из справочника "Код организации"
        def records513 = getRecordsByRefbookId(513L)
        def record513 = records513?.find { it?.record_id?.value == record520?.ORG_CODE?.value }
        if (record513?.CODE?.value == 2) {
            // ИВЗЛ
            code = 'ИВЗЛ'
        } else if (record520?.TAX_STATUS?.value && record513?.CODE?.value == 1) {
            // записи из справочника "Специальный налоговый статус"
            def records511 = getRecordsByRefbookId(511L)
            def record511 = records511?.find { it?.record_id?.value == record520?.TAX_STATUS?.value }
            if (record511?.CODE?.value == 1) {
                // ВЗЛ СРН
                code = 'ВЗЛ СРН'
            } else if (record511?.CODE?.value == 2) {
                // ВЗЛ ОРН
                code = 'ВЗЛ ОРН'
            }
        }
    }
    if (code == null) {
        return null
    }
    // записи из справочника "Типы участников ТЦО (расширенный)"
    def records505 = getRecordsByRefbookId(505L)
    def record505 = records505?.find { it?.CODE?.value == code }
    return record505?.record_id?.value
}

def BigDecimal calc17(def row) {
    // Графа 17 = сумма значений в графах 5-16
    def value = 0
    ['sum51', 'sum52', 'sum53', 'sum54', 'sum55', 'sum56', 'sum61', 'sum62', 'sum63', 'sum64', 'sum65', 'sum66'].each {
        value += row[it] ?: 0
    }
    return value
}

def BigDecimal calc18(def row) {
    // записи из справочника "Пороговые значения"
    def records = getRecordsByRefbookId(514L)
    def record = records?.find { it?.CODE?.value == row.group }
    return record?.record_id?.value
}

def Long calc19(def row) {
    if (row.sum7 == null || row.thresholdValue == null) {
        return null
    }
    // записи из справочника "Пороговые значения"
    def records = getRecordsByRefbookId(514L)
    def record = records?.find { it?.record_id?.value == row.thresholdValue }
    if (row.sum7 >= record?.VALUE?.value) {
        return getRecYesId()
    }
    return getRecNoId()
}

def Long calc20(def row) {
    // записи из справочника "Правила назначения категории юридическому лицу"
    def records = getRecordsByRefbookId(515L)
    for (def record : records) {
        if (row.group == record?.CODE?.value && record?.MIN_VALUE?.value <= row.sum7 &&
                (record?.MAX_VALUE?.value == null || row.sum7 <= record?.MAX_VALUE?.value)) {
            return record?.CATEGORY?.value
        }
    }
    return null
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

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def records520 = getRecordsByRefbookId(520L)
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Отсутствие нулевых значений
        if (calc17(row) == 0) {
            logger.error("Строка $rowNum: Объем доходов и расходов по всем сделкам не может быть нулевым!")
        }

        // 3. Наличие порогового значения
        if (calc18(row) == null) {
            // записи из справочника "Типы участников ТЦО (расширенный)"
            def records505 = getRecordsByRefbookId(505L)
            def record505 = records505?.find { it?.record_id?.value == row.group }
            def value4 = record505?.CODE?.value
            logger.error("Строка %d: Для типа участника ТЦО «%s» не задано пороговое значение в данном Налоговом периоде!", rowNum, value4)
        }

        // 4. Наличие правила назначения категории
        def tmp = calc20(row)
        if (tmp == null) {
            logger.error("Строка $rowNum: Для ожидаемого объема доходов и расходов не задано правило назначения категории в данном Налоговом периоде!")
        }

        // 5. Проверка соответствия категории пороговым значениям
        if (tmp != row.categoryRevised) {
            def record520 = records520?.find { it.record_id?.value == row.name }
            def value2 = record520?.NAME?.value
            logger.error("Строка %d: Организация «%s» не является взаимозависимым лицом в данном отчетном периоде!", rowNum, value2)
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

// Консолидация очень похожа на 4.1 9 месяцев, отличие в:
//  - из справочника "Участники ТЦО" получаются все записи
//  - нет удаления нулевых строк в конце консолидации
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
    def records520 = getRecordsByRefbookId(520L)
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

    // графа 5
    newRow.sum51 = calc5(recordId, sourceAllDataRowsMap)

    // графа 6
    newRow.sum52 = calc6(recordId, sourceAllDataRowsMap)

    // графа 7
    newRow.sum53 = calc7(recordId, sourceAllDataRowsMap)

    // графа 8
    newRow.sum54 = calc8(recordId, sourceFormDatasMap, sourceDataRowsMap)

    // графа 9
    newRow.sum55 = calc9(recordId, sourceAllDataRowsMap)

    // графа 10
    newRow.sum56 = calc10(record520, sourceAllDataRowsMap)

    // графа 11
    newRow.sum61 = calc11(recordId, sourceAllDataRowsMap)

    // графа 12
    newRow.sum62 = calc12(recordId, sourceAllDataRowsMap)

    // графа 13
    newRow.sum63 = calc13(recordId, sourceAllDataRowsMap)

    // графа 14
    newRow.sum64 = calc14(recordId, sourceFormDatasMap, sourceDataRowsMap)

    // графа 15
    newRow.sum65 = calc15(recordId, sourceAllDataRowsMap)

    // графа 16
    newRow.sum66 = calc16(record520, sourceAllDataRowsMap)

    return newRow
}

def calc5(def record520Id, def sourceAllDataRowsMap) {
    return calc5or11(record520Id, sourceAllDataRowsMap, true)
}

def calc6(def record520Id, def sourceAllDataRowsMap) {
    return calc6or12(record520Id, sourceAllDataRowsMap, true)
}

def calc7(def record520Id, def sourceAllDataRowsMap) {
    return calc7or13(record520Id, sourceAllDataRowsMap, true)
}

def calc8(def record520Id, def sourceFormDatasMap, def sourceDataRowsMap) {
    return calc8or14(record520Id, sourceFormDatasMap, sourceDataRowsMap, true)
}

def calc9(def record520Id, def sourceAllDataRowsMap) {
    return calc9or15(record520Id, sourceAllDataRowsMap, true)
}

def calc10(def record520, def sourceAllDataRowsMap) {
    return calc10or16(record520, sourceAllDataRowsMap, true)
}

def calc11(def record520Id, def sourceAllDataRowsMap) {
    return calc5or11(record520Id, sourceAllDataRowsMap, false)
}

def calc12(def record520Id, def sourceAllDataRowsMap) {
    return calc6or12(record520Id, sourceAllDataRowsMap, false)
}

def calc13(def record520Id, def sourceAllDataRowsMap) {
    return calc7or13(record520Id, sourceAllDataRowsMap, false)
}

def calc14(def record520Id, def sourceFormDatasMap, def sourceDataRowsMap) {
    return calc8or14(record520Id, sourceFormDatasMap, sourceDataRowsMap, false)
}

def calc15(def record520Id, def sourceAllDataRowsMap) {
    return calc9or15(record520Id, sourceAllDataRowsMap, false)
}

def calc16(def record520, def sourceAllDataRowsMap) {
    return calc10or16(record520, sourceAllDataRowsMap, false)
}

def calc5or11(def record520Id, def sourceAllDataRowsMap, def isCalc5) {
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
                        result += (isCalc5 ? row.incomeSum - row.outcomeSum : row.outcomeSum - row.incomeSum)
                    } else {
                        result += ((isCalc5 ? row.incomeSum : row.outcomeSum) ?: 0)
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

def calc6or12(def record520Id, def sourceAllDataRowsMap, def isCalc6) {
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

def calc7or13(def record520Id, def sourceAllDataRowsMap, def isCalc7) {
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
                    if (isCalc7) {
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

def calc8or14(def record520Id, def sourceFormDatasMap, def sourceDataRowsMap, def isCalc8) {
    def result = 0
    def formTypeId = 807
    sourceFormDatas = sourceFormDatasMap[formTypeId]
    sourceFormDatas.each { sourceFormData ->
        def rows = getNeedRowsForCalc8or14(sourceDataRowsMap[sourceFormData], isCalc8)
        for (def row : rows) {
            if (row.sbrfCode1 && row.statReportId2 == record520Id) {
                result += (row.sum ?: 0)
            }
        }
    }
    return result * 1000
}

def getNeedRowsForCalc8or14(def dataRows, def isCalc8) {
    def rows = []
    def findSection = isCalc8
    for (def row : dataRows) {
        if (isCalc8) {
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

def calc9or15(def record520Id, def sourceAllDataRowsMap, def isCalc9) {
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
                    if (isCalc9) {
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

def calc10or16(def record520, def sourceAllDataRowsMap, def isCalc10) {
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
                    if (isCalc10) {
                        result += (row.sum3 ?: 0)
                    }
                } else if (809 == formTypeId) {
                    // рну 117
                    if (isCalc10) {
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