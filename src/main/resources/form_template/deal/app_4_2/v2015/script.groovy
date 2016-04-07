package form_template.deal.app_4_2.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataXlsmReportBuilder
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.XlsxReportMetadata
import groovy.transform.Field
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.DataFormat
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.AreaReference
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.springframework.util.ClassUtils

/**
 * 803 - Приложение 4.2. Отчет в отношении доходов и расходов Банка по сделкам с ВЗЛ, РОЗ, НЛ по итогам окончания Налогового периода
 *
 * formTemplateId=803
 *
 * TODO:
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
// графа 10 (5.6) - sum56            - Сумма дополнительно начисленных налогооблагаемых доходов
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
    case FormDataEvent.GET_SPECIFIC_REPORT_TYPES:
        //specificReportType.add("Контролируемые лица") TODO вернуть для 1.0
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        createSpecificReport()
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

        // 2. Наличие порогового значения
        if (calc18(row) == null) {
            // записи из справочника "Типы участников ТЦО (расширенный)"
            def records505 = getRecordsByRefbookId(505L)
            def record505 = records505?.find { it?.record_id?.value == row.group }
            def value4 = record505?.CODE?.value
            logger.error("Строка %d: Для типа участника ТЦО «%s» не задано пороговое значение в данном Налоговом периоде!", rowNum, value4)
        }

        // 3. Наличие правила назначения категории
        def tmp = calc20(row)
        if (tmp == null) {
            logger.error("Строка $rowNum: Для ожидаемого объема доходов и расходов не задано правило назначения категории в данном Налоговом периоде!")
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

// мапа в которой хранится id формы - список алиасов ссылающихся на справочник "участники ТЦО"
@Field
def sourceRefbook520AliasMap = [
        // Журнал взаиморасчетов
        807 : ['statReportId1', 'statReportId2'],

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

    // графа 2
    newRow.name = record520?.record_id?.value

    // графа 5
    newRow.sum51 = calc5(record520, sourceAllDataRowsMap)

    // графа 6
    newRow.sum52 = calc6(record520, sourceAllDataRowsMap)

    // графа 7
    newRow.sum53 = calc7(record520, sourceAllDataRowsMap)

    // графа 8
    newRow.sum54 = calc8(record520, sourceFormDatasMap, sourceDataRowsMap)

    // графа 9
    newRow.sum55 = calc9(record520, sourceAllDataRowsMap)

    // графа 10
    newRow.sum56 = calc10(record520, sourceAllDataRowsMap)

    // графа 11
    newRow.sum61 = calc11(record520, sourceAllDataRowsMap)

    // графа 12
    newRow.sum62 = calc12(record520, sourceAllDataRowsMap)

    // графа 13
    newRow.sum63 = calc13(record520, sourceAllDataRowsMap)

    // графа 14
    newRow.sum64 = calc14(record520, sourceFormDatasMap, sourceDataRowsMap)

    // графа 15
    newRow.sum65 = calc15(record520, sourceAllDataRowsMap)

    // графа 16
    newRow.sum66 = calc16(record520, sourceAllDataRowsMap)

    return newRow
}

def calc5(def record520, def sourceAllDataRowsMap) {
    return calc5or11(record520, sourceAllDataRowsMap, true)
}

def calc6(def record520, def sourceAllDataRowsMap) {
    return calc6or12(record520, sourceAllDataRowsMap, true)
}

def calc7(def record520, def sourceAllDataRowsMap) {
    return calc7or13(record520, sourceAllDataRowsMap, true)
}

def calc8(def record520, def sourceFormDatasMap, def sourceDataRowsMap) {
    return calc8or14(record520, sourceFormDatasMap, sourceDataRowsMap, true)
}

def calc9(def record520, def sourceAllDataRowsMap) {
    return calc9or15(record520, sourceAllDataRowsMap, true)
}

def calc10(def record520, def sourceAllDataRowsMap) {
    return calc10or16(record520, sourceAllDataRowsMap, true)
}

def calc11(def record520, def sourceAllDataRowsMap) {
    return calc5or11(record520, sourceAllDataRowsMap, false)
}

def calc12(def record520, def sourceAllDataRowsMap) {
    return calc6or12(record520, sourceAllDataRowsMap, false)
}

def calc13(def record520, def sourceAllDataRowsMap) {
    return calc7or13(record520, sourceAllDataRowsMap, false)
}

def calc14(def record520, def sourceFormDatasMap, def sourceDataRowsMap) {
    return calc8or14(record520, sourceFormDatasMap, sourceDataRowsMap, false)
}

def calc15(def record520, def sourceAllDataRowsMap) {
    return calc9or15(record520, sourceAllDataRowsMap, false)
}

def calc16(def record520, def sourceAllDataRowsMap) {
    return calc10or16(record520, sourceAllDataRowsMap, false)
}

def calc5or11(def record520, def sourceAllDataRowsMap, def isCalc5) {
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
                        result += ((isCalc5 ? row.incomeSum : row.outcomeSum) ?: 0)
                        break
                    case 827 : // 6.11
                        def transactionType = getRefBookValue(16L, row.transactionType)?.CODE?.value
                        if (isCalc5) {
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

def calc6or12(def record520, def sourceAllDataRowsMap, def isCalc6) {
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
                        if (isCalc6) {
                            result += (row.income >= row.outcome ? row.cost : 0)
                        } else {
                            result += (row.income < row.outcome ? row.cost : 0)
                        }
                        break
                    case 839 : // 6.16
                        if (isCalc6) {
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

def calc7or13(def record520, def sourceAllDataRowsMap, def isCalc7) {
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
                        if (isCalc7) {
                            result += (row.sum ?: 0)
                        }
                        break
                    case 837 : // 6.15
                        if (isCalc7) {
                            result += (row.income >= row.outcome ? row.cost : 0)
                        } else {
                            result += (row.income < row.outcome ? row.cost : 0)
                        }
                        break
                    case 838 : // 6.18
                        if (isCalc7) {
                            result += (row.incomeSum >= row.outcomeSum ? row.total : 0)
                        } else {
                            result += (row.incomeSum < row.outcomeSum ? row.total : 0)
                        }
                        break
                    case 831 : // 6.20
                        if (!isCalc7) {
                            result += (row.outcome ?: 0)
                        }
                        break
                }
            }
        }
    }
    return result
}

def calc8or14(def record520, def sourceFormDatasMap, def sourceDataRowsMap, def isCalc8) {
    def result = 0
    def formTypeId = 807
    sourceFormDatas = sourceFormDatasMap[formTypeId]
    sourceFormDatas.each { sourceFormData ->
        def rows = getNeedRowsForCalc8or14(sourceDataRowsMap[sourceFormData], isCalc8)
        for (def row : rows) {
            if (row.sbrfCode1 && row.statReportId2 == record520?.record_id?.value) {
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

def calc9or15(def record520, def sourceAllDataRowsMap, def isCalc9) {
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
                        if (isCalc9) {
                            result += (row.sum ?: 0)
                        }
                        break
                    case 814 : // 6.5
                    case 815 : // 6.8
                    case 828 : // 6.19
                    case 830 : // 6.21
                    case 834 : // 6.22
                    case 833 : // 6.24
                        if (!isCalc9) {
                            result += (row.sum ?: 0)
                        }
                        break
                    case 817 : // 6.9
                        if (isCalc9) {
                            result += (row.finResult ?: 0)
                        }
                        break
                    case 836 : // 6.25
                        if (!isCalc9) {
                            result += (row.finResult ?: 0)
                        }
                        break
                    case 826 : // 6.13
                        if (!isCalc9) {
                            result += (row.outcomeSum ?: 0)
                        }
                        break
                }
            }
        }
    }
    return result
}

def calc10or16(def record520, def sourceAllDataRowsMap, def isCalc10) {
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
                        if (isCalc10) {
                            result += (row.sum3?.abs() ?: 0)
                        }
                        break
                    case 820 : // РНУ-102
                    case 809 : // РНУ-117
                        if (!isCalc10) {
                            result += (row.sum3?.abs() ?: 0)
                        }
                        break
                    case 821 : // РНУ-107
                        if (isCalc10) {
                            result += (row.sum4?.abs() ?: 0)
                        }
                        break
                    case 824 : // РНУ-112
                        if (isCalc10) {
                            result += (row.incomeCorrection?.abs() ?: 0)
                        }
                        break
                    case 829 : // РНУ-114
                        if (isCalc10) {
                            result += (row.sum1?.abs() ?: 0)
                        }
                        break
                    case 842 : // РНУ-115
                    case 844 : // РНУ-116
                        if (isCalc10) {
                            result += (row.incomeDelta?.abs() ?: 0)
                        } else {
                            result += (row.outcomeDelta?.abs() ?: 0)
                        }
                        break
                    case 840 : // РНУ-122
                        if ((isCalc10 && "19300".equals(row.code)) ||
                                (!isCalc10 && "19510".equals(row.code))) {
                            result += (row.sum6?.abs() ?: 0)
                        }
                        break
                    case 841 : // РНУ-123
                        if (isCalc10) {
                            result += (row.sum10?.abs() ?: 0)
                        }
                        break
                    case 843 : // РНУ-171
                        if ((isCalc10 && "19540".equals(row.code)) ||
                                (!isCalc10 && "19570".equals(row.code))) {
                            result += (row.incomeCorrection?.abs() ?: 0)
                        }
                        break
                }
            }
        }
    }
    return result
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

@Field
Workbook workBook = null
@Field
Sheet sheet = null

/** Специфический отчет "Контролируемые лица" XLSM*/
void createSpecificReport() {
    // для работы с эксель
    String TEMPLATE = ClassUtils.classPackageAsResourcePath(FormDataXlsmReportBuilder.class)+ "/acctax.xlsm"
    InputStream templeteInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE)
    workBook = WorkbookFactory.create(templeteInputStream)
    sheet = workBook.getSheetAt(0)
    Row tmpRow
    Cell cell
    CellRangeAddress region
    def rowIndex = 0

    // очистить шаблон
    clearSheet()

    // заголовок
    tmpRow = sheet.createRow(rowIndex)
    cell = tmpRow.createCell(0)

    def year = reportPeriodService.get(formData.reportPeriodId)?.taxPeriod?.year
    cell.setCellValue("Налоговый период: " + (year ? year + " г." : ""))
    cell.setCellStyle(getCellStyle(StyleType.ROW_1))
    // объединение трех ячеек
    region = new CellRangeAddress(rowIndex, rowIndex, 0, 2)
    sheet.addMergedRegion(region)

    def dateStr = new Date()?.format('dd.MM.yyyy') ?: ''
    rowIndex++
    tmpRow = sheet.createRow(rowIndex)
    cell = tmpRow.createCell(0)
    cell.setCellValue("Дата составления отчета: $dateStr")
    cell.setCellStyle(getCellStyle(StyleType.ROW_2))

    // объединение трех ячеек
    region = new CellRangeAddress(rowIndex, rowIndex, 0, 2)
    sheet.addMergedRegion(region)

    // пустая строка
    rowIndex++
    sheet.createRow(rowIndex)

    rowIndex++
    tmpRow = sheet.createRow(rowIndex)
    tmpRow.setHeightInPoints(42.75)
    cell = tmpRow.createCell(0)
    cell.setCellValue("Отчет в отношении Взаимозависимых лиц / Резидентов Оффшорных зон / Независимых лиц, с которыми совершались Внешнеторговые сделки, все сделки с которыми признаются Контролируемыми сделками по итогам окончания Налогового периода")
    cell.setCellStyle(getCellStyle(StyleType.TITLE))

    // объединение трех ячеек
    region = new CellRangeAddress(rowIndex, rowIndex, 0, 2)
    sheet.addMergedRegion(region)

    // пустая строка
    rowIndex++
    sheet.createRow(rowIndex)

    // шапка
    rowIndex++
    def tmpDataRow = formData.createDataRow()
    def columnAliases = ['rowNumber', 'name', 'ikksr']
    def columnNames = columnAliases.collect { getColumnName(tmpDataRow, it) }
    addNewRowInXlsm(rowIndex, columnNames, StyleType.HEADER)

    // нумерация
    rowIndex++
    def columnNum = (1..3).collect { 'гр. ' + it.toString() }
    addNewRowInXlsm(rowIndex, columnNum, StyleType.NUMERATION)

    // задать ширину столбцов (в символах)
    def widths = [
            6,  // rowNumber
            43, // name
            39  // ikksr
    ]
    for (int i = 0; i < widths.size(); i++) {
        int width = widths[i] * 256 // умножить на 256, т.к. 1 единица ширины в poi = 1/256 ширины символа
        sheet.setColumnWidth(i, width)
    }

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    dataRows.sort { row ->
        getRefBookValue(520L, row.name)?.NAME?.value?.toLowerCase()
    }
    int count = 0
    for (def row : dataRows) {
        if (row.sign != getRecYesId()) {
            continue
        }
        rowIndex++
        def values = getXlsRowValues(++count, row.name)
        // добавить значения
        addNewRowInXlsm(rowIndex, values, StyleType.DATA)
    }

    // область печати
    setPrintSetup(rowIndex, widths.size())

    workBook.write(scriptSpecificReportHolder.getFileOutputStream())

    // название файла
    scriptSpecificReportHolder.setFileName("Контролируемые лица.xlsm")
}

def getXlsRowValues(def count, def recordId) {
    def values = []

    values.add(count)

    // 2. name
    def record520 = getRefBookValue(520L, recordId)
    values.add(record520?.NAME?.value)

    // 3. ikksr
    def ikksr = record520?.IKKSR?.value
    values.add(ikksr)

    return values
}

/** Очистить шаблон, т.к в нем есть значения, поименованные ячейки, стили и т.д. */
void clearSheet() {
    // убрать объединения
    def count = sheet.getNumMergedRegions()
    for (int i = count - 1; i >= 0; i--) {
        sheet.removeMergedRegion(i)
    }

    // очистить ячейки
    def addRowCount = 0
    int maxColumnNum = 0
    for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i)
        if (!row) {
            continue
        }
        if (row.getLastCellNum() > maxColumnNum) {
            maxColumnNum = row.getLastCellNum()
        }
        sheet.removeRow(row)
        // добавить новую строку что б не удалился макрос после удаления всех строк (две строки используются макросом)
        if (addRowCount < 2) {
            Row tmp = sheet.createRow(i)
            tmp.createCell(2).setCellValue(' ')
            addRowCount++
        }
    }
    // поправить ширину столбцов
    int width = (sheet.getDefaultColumnWidth() + 1) * 256
    for (int i = 0; i <= maxColumnNum; i++) {
        sheet.setColumnWidth(i, width)
    }

    // удалить именованные ячейки
    def cellNames = [
            XlsxReportMetadata.RANGE_DATE_CREATE,
            XlsxReportMetadata.RANGE_REPORT_CODE,
            XlsxReportMetadata.RANGE_REPORT_PERIOD,
            XlsxReportMetadata.RANGE_REPORT_NAME,
            XlsxReportMetadata.RANGE_SUBDIVISION,
            // XlsxReportMetadata.RANGE_POSITION, // используется макросом
            XlsxReportMetadata.RANGE_SUBDIVISION_SIGN,
            XlsxReportMetadata.RANGE_FIO,
    ]
    cellNames.each { name ->
        workBook.removeName(name)
    }

    // сместить XlsxReportMetadata.RANGE_POSITION в начало листа, что бы макрос не смещал границы страницы в конце таблицы
    AreaReference ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_POSITION).getRefersToFormula())
    def startRow = ar.getFirstCell().getRow()
    sheet.shiftRows(startRow, startRow + 1, -startRow)
}

/**
 * Задать область печати.
 * Взято отсюда FormDataXlsmReportBuilder.setPrintSetup(...)
 *
 * @param rowCount количество строк для области печти (количество используемых строк)
 * @param columnCount количество столбцов для области печти (количество используемых столбцов)
 */
void setPrintSetup(def rowCount, def columnCount) {
    workBook.setPrintArea(0, 0, columnCount, 0, rowCount)
    sheet.setFitToPage(true)
    sheet.setAutobreaks(true)
    sheet.getPrintSetup().setFitHeight((short) 0)
    sheet.getPrintSetup().setFitWidth((short) 1)
}

@Field
def cellStyleMap = [:]

enum StyleType {
    ROW_1,        // строка 1
    ROW_2,        // строка 2
    TITLE,        // строка 4
    HEADER,       // шапка
    NUMERATION,   // нумерация
    DATA,         // данные
    DATA_CENTER,  // данные (по центру)
    BOLT,         // жирный
    DATE,         // дата
    GROUP_HEADER, // заголовок
}

CellStyle getCellStyle(StyleType styleType, def rowNF = null) {
    def subAlias = (rowNF ? rowNF.getCell('name').getStyle().getAlias() : '')
    def alias = styleType.name() + subAlias
    if (cellStyleMap.containsKey(alias)) {
        return cellStyleMap.get(alias)
    }
    CellStyle style = workBook.createCellStyle()
    switch (styleType) {
        case StyleType.ROW_1 :
            style.setWrapText(true)
            style.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM)
            style.setAlignment(CellStyle.ALIGN_LEFT)

            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_NORMAL)
            font.setFontHeightInPoints(10 as short)
            font.setFontName('Arial')
            style.setFont(font)
            break
        case StyleType.ROW_2 :
            style.setWrapText(true)
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            style.setAlignment(CellStyle.ALIGN_LEFT)

            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_NORMAL)
            font.setFontHeightInPoints(10 as short)
            font.setFontName('Arial')
            style.setFont(font)
            break
        case StyleType.TITLE :
            style.setWrapText(true)
            style.setVerticalAlignment(CellStyle.VERTICAL_TOP)
            style.setAlignment(CellStyle.ALIGN_CENTER)

            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_BOLD)
            font.setFontHeightInPoints(10 as short)
            font.setFontName('Arial')
            style.setFont(font)
            break
        case StyleType.HEADER :
            style.setAlignment(CellStyle.ALIGN_CENTER)
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            style.setBorderRight(CellStyle.BORDER_THIN)
            style.setBorderLeft(CellStyle.BORDER_THIN)
            style.setBorderBottom(CellStyle.BORDER_THIN)
            style.setBorderTop(CellStyle.BORDER_THIN)
            style.setWrapText(true)

            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_BOLD)
            font.setFontHeightInPoints(9 as short)
            font.setFontName('Arial')
            style.setFont(font)
            break
        case StyleType.NUMERATION :
            style.setAlignment(CellStyle.ALIGN_CENTER)
            style.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM)
            style.setBorderRight(CellStyle.BORDER_THIN)
            style.setBorderLeft(CellStyle.BORDER_THIN)
            style.setBorderBottom(CellStyle.BORDER_THIN)
            style.setBorderTop(CellStyle.BORDER_THIN)
            style.setWrapText(true)

            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_BOLD)
            font.setFontHeightInPoints(8 as short)
            font.setFontName('Arial')
            style.setFont(font)
            break
        case StyleType.DATA_CENTER :
            style.setAlignment(CellStyle.ALIGN_CENTER)
        case StyleType.DATA :
            style.setBorderRight(CellStyle.BORDER_THIN)
            style.setBorderLeft(CellStyle.BORDER_THIN)
            style.setBorderBottom(CellStyle.BORDER_THIN)
            style.setBorderTop(CellStyle.BORDER_THIN)
            style.setWrapText(true)

            Font font = workBook.createFont()
            font.setFontHeightInPoints(8 as short)
            font.setFontName('Arial')
            style.setFont(font)
            break
        case StyleType.BOLT :
            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_BOLD)
            font.setFontHeightInPoints(8 as short)
            font.setFontName('Arial')
            style.setFont(font)
            break
        case StyleType.DATE :
            style.setAlignment(CellStyle.ALIGN_CENTER)
            style.setBorderRight(CellStyle.BORDER_THIN)
            style.setBorderLeft(CellStyle.BORDER_THIN)
            style.setBorderBottom(CellStyle.BORDER_THIN)
            style.setBorderTop(CellStyle.BORDER_THIN)
            style.setWrapText(true)

            Font font = workBook.createFont()
            font.setFontHeightInPoints(8 as short)
            font.setFontName('Arial')
            style.setFont(font)

            DataFormat dataFormat = workBook.createDataFormat()
            style.setDataFormat(dataFormat.getFormat(XlsxReportMetadata.sdf.toPattern()))
            break
        case StyleType.GROUP_HEADER :
            style.setAlignment(CellStyle.ALIGN_CENTER)
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            style.setWrapText(true)

            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_BOLD)
            font.setFontHeightInPoints(11 as short)
            font.setFontName('Arial')
            font.setItalic(true)
            style.setFont(font)
            break
    }
    // заливка для данных и дат
    if (rowNF && styleType in [StyleType.DATE, StyleType.DATA, StyleType.DATA_CENTER]) {
        XSSFCellStyle tmpStyle = (XSSFCellStyle) style
        XSSFColor color = getColor(rowNF.getCell('name').getStyle().getBackColor())
        tmpStyle.setFillForegroundColor(color)
        tmpStyle.setFillBackgroundColor(color)
        tmpStyle.setFillPattern(CellStyle.SOLID_FOREGROUND)
    }
    cellStyleMap.put(alias, style)
    return style
}

/**
 * Добавить новую строку в эксель и заполнить данными.
 *
 * @param sheet лист эксель
 * @param rowIndex номер строк (0..n)
 * @param values список строковых значении
 */
Row addNewRowInXlsm(int rowIndex, def values, StyleType styleType = null) {
    Row newRow = sheet.createRow(rowIndex)
    def cellIndex = 0
    for (def value : values) {
        Cell cell = newRow.createCell(cellIndex)
        cell.setCellValue(value)

        // стили
        if (styleType) {
            CellStyle cellStyle = getCellStyle(styleType)
            cell.setCellStyle(cellStyle)
        }
        cellIndex++
    }
    return newRow
}

