package form_template.deal.app_9.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * 854 - Приложение 9. Отчет в отношении доходов и расходов ПАО "Сбербанк России"
 *
 * formTemplateId=854
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

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_LOAD:
        afterLoad()
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
                       'sum56', 'sum6']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'sum4', 'sum42', 'sum43', 'sum44', 'sum45', 'sum46', 'sum51', 'sum52', 'sum53', 'sum54',
                       'sum55', 'sum56', 'sum6']

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
    }
}

def BigDecimal calc16(def row) {
    // Графа 16 = сумма значений в графах 4-15
    def value = 0
    ['sum4', 'sum42', 'sum43', 'sum44', 'sum45', 'sum46', 'sum51', 'sum52', 'sum53', 'sum54', 'sum55', 'sum56'].each {
        value += row[it] ?: 0
    }
    return value
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def reportPeriod = getReportPeriod()
    def records520 = getRecords520()
    def records520Map = [:]
    records520.each { record ->
        records520Map[record?.record_id?.value] = record
    }

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        def isVZL = records520Map[row.name]

        // 2. Проверка на отсутствие в списке организаций, которые не являются ВЗЛ в данном налоговом периоде (год)
        if (reportPeriod.order == 4 && row.name && !isVZL) {
            def name = getRefBookValue(520L, row.name)?.NAME?.value
            logger.error("Строка %s: Организация «%s» не является взаимозависимым лицом в данном отчетном периоде!", rowNum, name)
        }

        // 3. Проверка на отсутствие в списке не ВЗЛ ОРН (9 месяцев)
        if (reportPeriod.order == 3 && row.name && !isVZL) {
            def name = getRefBookValue(520L, row.name)?.NAME?.value
            logger.error("Строка %s: Организация «%s» не является взаимозависимым лицом с общим режимом налогообложения в данном отчетном периоде!", rowNum, name)
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

// мапа в которой хранится id формы - список алиасов ссылающихся на справочник "участники ТЦО"
@Field
def sourceRefbook520AliasMap = [
        // Журнал взаиморасчетов по ТБ
        853 : ['statReportId'],

        // формы РНУ
        818 : ['name'], // РНУ-101
        820 : ['name'], // РНУ-102
        821 : ['name'], // РНУ-107
        822 : ['name'], // РНУ-110
        808 : ['name'], // РНУ-111
        824 : ['name'], // РНУ-112
        829 : ['name'], // РНУ-114
        842 : ['name'], // РНУ-115
        844 : ['name'], // РНУ-116
        809 : ['name'], // РНУ-117
        840 : ['name'], // РНУ-122
        841 : ['name'], // РНУ-123
        843 : ['name'], // РНУ-171

        // формы приложений 6
        816 : ['name'], // 6.1
        804 : ['name'], // 6.2
        812 : ['name'], // 6.3
        813 : ['name'], // 6.4
        814 : ['name'], // 6.5
        806 : ['name'], // 6.6
        805 : ['name'], // 6.7
        815 : ['name'], // 6.8
        817 : ['name'], // 6.9
        823 : ['name'], // 6.10-1
        825 : ['name'], // 6.10-2
        827 : ['name'], // 6.11
        819 : ['name'], // 6.12
        826 : ['name'], // 6.13
        835 : ['name'], // 6.14
        837 : ['name'], // 6.15
        839 : ['name'], // 6.16
        811 : ['name'], // 6.17
        838 : ['name'], // 6.18
        828 : ['name'], // 6.19
        831 : ['name'], // 6.20
        830 : ['name'], // 6.21
        834 : ['name'], // 6.22
        832 : ['name'], // 6.23
        833 : ['name'], // 6.24
        836 : ['name'], // 6.25
]

// Консолидация очень похожа на 4.2 и 4.1.9
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
    def formTypeId = 853
    def sourceFormDatas = sourceFormDatasMap[formTypeId]
    sourceFormDatas.each { sourceFormData ->
        def rows = getNeedRowsForCalc7or13(sourceDataRowsMap[sourceFormData], isCalc7)
        for (def row : rows) {
            if (row.statReportId == record520?.record_id?.value) {
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
            822, // РНУ-110
            808, // РНУ-111
            824, // РНУ-112
            829, // РНУ-114
            842, // РНУ-115
            844, // РНУ-116
            809, // РНУ-117
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
                            result += (row.sum3?.abs() ?: 0)
                        }
                        break
                    case 820 : // РНУ-102
                    case 809 : // РНУ-117
                        if (!isCalc9) {
                            result += (row.sum3?.abs() ?: 0)
                        }
                        break
                    case 821 : // РНУ-107
                        if (isCalc9) {
                            result += (row.sum4?.abs() ?: 0)
                        }
                        break
                    case 824 : // РНУ-112
                        if (isCalc9) {
                            result += (row.incomeCorrection?.abs() ?: 0)
                        }
                        break
                    case 829 : // РНУ-114
                        if (isCalc9) {
                            result += (row.sum1?.abs() ?: 0)
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
                        if ((isCalc9 && "19300".equals(row.code)) ||
                                (!isCalc9 && "19510".equals(row.code))) {
                            result += (row.sum6?.abs() ?: 0)
                        }
                        break
                    case 841 : // РНУ-123
                        if (isCalc9) {
                            result += (row.sum10?.abs() ?: 0)
                        }
                        break
                    case 843 : // РНУ-171
                        if ((isCalc9 && "19540".equals(row.code)) ||
                                (!isCalc9 && "19570".equals(row.code))) {
                            result += (row.incomeCorrection?.abs() ?: 0)
                        }
                        break
                }
            }
        }
    }
    return result
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
def records520 = null

// Получить значения из справочника "Участники ТЦО".
def getRecords520() {
    if (records520 != null) {
        return records520
    }
    def filter = null
    def reportPeriod = getReportPeriod()
    if (reportPeriod.order != 4) {
        // получить id записи с кодом "2" из справончика "Специальный налоговый статус"
        def provider = formDataService.getRefBookProvider(refBookFactory, 511L, providerCache)
        filter = "CODE = 2"
        def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        def taxStatusId
        if (records && records.size() == 1) {
            taxStatusId = records.get(0)?.record_id?.value
        } else {
            records520 = []
            return records520
        }
        filter = "TAX_STATUS = $taxStatusId"
    }
    // получить записи из справончика "Участники ТЦО"
    def provider = formDataService.getRefBookProvider(refBookFactory, 520L, providerCache)
    def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
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
            (end == null || end >= getReportPeriodStartDate()) &&
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

void afterLoad() {
    if (binding.variables.containsKey("specialPeriod")) {
        // имя периода и конечная дата корректны
        // устанавливаем дату для справочников
        specialPeriod.calendarStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
}