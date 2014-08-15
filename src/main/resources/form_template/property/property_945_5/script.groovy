package form_template.property.property_945_5

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * "(945.5) Сводная форма данных бухгалтерского учета для расчета налога на имущество".
 * formTemplateId=10640
 *
 * TODO:
 *      - консолидация
 *      - группирока / объединение
 *      - расчеты итогов
 *      - логическая проверка 3
 *      - убрать лишнее
 *
 * @author Ramil Timerbaev
 */

// графа 1  - subject           - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
// графа 2  - taxAuthority
// графа 3  - kpp
// графа 4  - oktmo             - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
// графа    - title
// графа 5  - cost1
// графа 6  - cost2
// графа 7  - cost3
// графа 8  - cost4
// графа 9  - cost5
// графа 10 - cost6
// графа 11 - cost7
// графа 12 - cost8
// графа 13 - cost9
// графа 14 - cost10
// графа 15 - cost11
// графа 16 - cost12
// графа 17 - cost
// графа 18 - cost31_12

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        if (formData.kind != FormDataKind.SUMMARY) {
            logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
        }
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        // checkPrevForm()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        // checkPrevForm()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        checkPrevForm()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
        break

    // TODO (Ramil Timerbaev)
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
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
def allColumns = ['subject', 'taxAuthority', 'kpp', 'oktmo', 'title', 'cost1', 'cost2', 'cost3',
        'cost4', 'cost5', 'cost6', 'cost7', 'cost8', 'cost9', 'cost10', 'cost11', 'cost12', 'cost', 'cost31_12']

// Проверяемые на пустые значения атрибуты для 1 квартала (графа 1..8)
@Field
def nonEmptyColumnsOrder1 = ['subject', 'taxAuthority', 'kpp', 'oktmo', 'title', 'cost1', 'cost2', 'cost3', 'cost4']

// Проверяемые на пустые значения атрибуты для полгода (графа 1..11)
@Field
def nonEmptyColumnsOrder2 = ['subject', 'taxAuthority', 'kpp', 'oktmo', 'title', 'cost1', 'cost2', 'cost3', 'cost4', 'cost5', 'cost6', 'cost7']

// Проверяемые на пустые значения атрибуты для 9 месяцев (графа 1..14)
@Field
def nonEmptyColumnsOrder3 = ['subject', 'taxAuthority', 'kpp', 'oktmo', 'title', 'cost1', 'cost2', 'cost3', 'cost4', 'cost5', 'cost6', 'cost7', 'cost8', 'cost9', 'cost10']

// Проверяемые на пустые значения атрибуты для 9 месяцев (графа 1..17)
@Field
def nonEmptyColumnsOrder4 = ['subject', 'taxAuthority', 'kpp', 'oktmo', 'title', 'cost1', 'cost2', 'cost3', 'cost4', 'cost5', 'cost6', 'cost7', 'cost8', 'cost9', 'cost10', 'cost11', 'cost12', 'cost', 'cost31_12']

// Мапа со списком алиасов проверяемые на пустые значения атрибуты (графа 1..18)
@Field
def nonEmptyColumns = [
        1 : nonEmptyColumnsOrder1,
        2 : nonEmptyColumnsOrder2,
        3 : nonEmptyColumnsOrder3,
        4 : nonEmptyColumnsOrder4
]

// Редактируемые атрибуты (графа 5..18)
@Field
def editableColumns = ['cost1', 'cost2', 'cost3', 'cost4', 'cost5', 'cost6',
        'cost7', 'cost8', 'cost9', 'cost10', 'cost11', 'cost12', 'cost', 'cost31_12']

// Сортируемые атрибуты (графа 2, 3)
@Field
def sortColumns = ['taxAuthority', 'kpp']

// Группируевые атрибуты (графа 1..4)
@Field
def groupColumns = ['subject', 'taxAuthority', 'kpp', 'oktmo']

// TODO (Ramil Timerbaev) убрать лишние переменные
@Field
def startDate = null

@Field
def endDate = null

@Field
def reportPeriod = null

@Field
def yearStartDate = null

@Field
def prevForms = null

@Field
def prevReportPeriods = null

// для записей справочника 200
@Field
def recordsMap = [:]

@Field
def FIRST_ROW_VALUE = 'Стоимость  имущества  по субъекту Федерации'

@Field
def SUBJECT_ROW_VALUE = 'Стоимость недвижимого имущества по субъекту Федерации'

@Field
def OKTMO_ROW_VALUE = 'в т.ч. стоимость недвижимого имущества  по населенному пункту'

@Field
def ROW_2_VALUE = 'Стоимость движимого имущества, отраженного на балансе'

@Field
def ROW_TOTAL_VALUE = 'ИТОГО с учетом корректировки'

@Field
def ROW_18_VALUE = ''

@Field
def SEPARATOR = '#'

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getYearStartDate() {
    if (yearStartDate == null) {
        yearStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return yearStartDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить фиксированные строк
    deleteAllAliased(dataRows)

    // сортировка / групировка
    sortRows(dataRows, groupColumns)
    updateIndexes(dataRows)

    addFixedRows(dataRows)

    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def periodOrder = getReportPeriod().order
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей
        def columns = nonEmptyColumns[periodOrder]
        checkNonEmptyColumns(row, index, columns, logger, true)

        // 2. Проверка на не заполнение поля
        columns = allColumns - columns
        columns.each { alias ->
            if (row.getCell(alias).value) {
                String msg = String.format("Строка %d: В текущем периоде формы графа «%s» должна быть не заполнена!", index, getColumnName(row, alias))
                logger.error(msg)
            }
        }

        // 3. Проверка значений Граф 5-17 по строке «в т.ч. стоимость льготируемого имущества» (подсчет итогов)
        // По каждой группе строк Граф 1-4:
        // «Графа N» строки «в т.ч. стоимость льготируемого имущества» заполнена значением согласно Табл. 20, где N = 5, 6, …, 17
        // 1	Итоговые значения рассчитаны неверно в графе «<Наименование поля>»!

        // TODO (Ramil Timerbaev) доделать после того как станет понятно как заполнять форму при консолидации
    }
}

def consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    // форма 945.1
    def sourceFormTypeId = 610

    def taxPeriodId = getReportPeriod()?.taxPeriod?.id
    def order = getReportPeriod()?.order
    // получить количество месяцев (на 1 месяц больше если текущая форма не за последний отчетный период)
    def monthOrders = order * 3 + (order < 4 ? 1 : 0)

    // если форма за последний отчетный период года, то надо получить еще форму за 1 месяц следующего года
    def nextTaxPeriod = null
    if (order == 4) {
        def from = getReportPeriodEndDate() + 1
        def to = getNextMonthDate()
        def nextPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.PROPERTY, from, to)
        if (nextPeriods != null && nextPeriods.size() == 1) {
            nextTaxPeriod = nextPeriods.get(0)
        }
    }

    // мапа для хранения алиаса заполнения для каждолго источника (id источника -> alias)
    def aliasMap = [:]
    // найти все ежемесячные источники (принятые)
    def sources = []
    // найти все ежемесячные источники в текущем налоговом периоде
    monthOrders.each { monthOrder ->
        FormData source = formDataService.findMonth(sourceFormTypeId, FormDataKind.PRIMARY, formDataDepartment.id, taxPeriodId, monthOrder)
        if (source != null && source.getState() == WorkflowState.ACCEPTED) {
            sources.add(source)
            aliasMap[source.id] = 'cost' + monthOrder
        }
    }
    // найти все ежемесячные источники в следующем налоговом периоде (за первый месяц)
    if (nextTaxPeriod) {
        FormData source = formDataService.findMonth(sourceFormTypeId, FormDataKind.PRIMARY, formDataDepartment.id, nextTaxPeriod, 1)
        if (source != null && source.getState() == WorkflowState.ACCEPTED) {
            sources.add(source)
            aliasMap[source.id] = 'cost'
        }
    }

    // мапа для строк из источников, доступ к строкам по ключу субъект#октмо
    def groupRowsMap = [:]

    // получить данные из источников, cформировать новые строки по ним
    sources.each { source ->
        // получить алиас заполняемой графы - зависит от месяца источника: январь - графа 5, февраль - графа 6 ... декабрь - графа 16, январь следующего года - 17.
        def alias = aliasMap[source.id]
        def sourceRows = formDataService.getDataRowHelper(source).getAll()

        // получить список групп 1
        def rowsGroup1 = getRowsGroupBySubject(sourceRows)
        rowsGroup1.each { rows ->
            processRowsGroup1(rows, groupRowsMap, alias)
        }
    }

    // TODO (Ramil Timerbaev) надо ли тут делать сортировку/группировку, или вынести ее в cacl ?
    // добавить строки групп в форму
    groupRowsMap.keySet().each {
        dataRows.addAll(groupRowsMap[it])
    }

    dataRowHelper.save(dataRows)
}

def getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return reportPeriod
}

def roundValue(def value, int newScale) {
    if (value != null) {
        return ((BigDecimal) value).setScale(newScale, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/** Получить предыдущие формы за текущий год. */
def getPrevForms() {
    if (!prevForms) {
        prevForms = []
        def reportPeriods = getPrevReportPeriods()
        for (def reportPeriod : reportPeriods) {
            // получить формы за 1 кв, полгода, 9 месяцев
            def form = formDataService.find(formData.formType.id, FormDataKind.SUMMARY, formDataDepartment.id, reportPeriod.id)
            if (form) {
                prevForms.add(form)
            }
        }
    }
    return prevForms
}

/** Получить предыдущие преиоды за год. */
def getPrevReportPeriods() {
    if (prevReportPeriods == null) {
        prevReportPeriods = []
        // получить периоды за год
        def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.PROPERTY, getYearStartDate(), getReportPeriodEndDate())
        for (def reportPeriod : reportPeriods) {
            if (reportPeriod.id == formData.reportPeriodId) {
                continue
            }
            prevReportPeriods.add(reportPeriod)
        }
    }
    return prevReportPeriods
}

def checkPrevForm() {
    return // TODO (Ramil Timerbaev)
    if (!isPeriodYear()) {
        return
    }
    def reportPeriods = getPrevReportPeriods()
    // проверить существование и принятость форм, а также наличие данных в них.
    for (def reportPeriod : reportPeriods) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, FormDataKind.SUMMARY, formDataDepartment.id, reportPeriod.id, false, logger, true)
    }
}

def getNewRows(def categories) {
    def rows = []

    def row1 = formData.createDataRow()
    row1.title = 'признаваемых объектом налогообложения'
    def row2 = formData.createDataRow()
    row2.title = 'в т.ч. стоимость льготируемого имущества:'

    rows.add(row1)
    rows.add(row2)

    def list = null
    if (categories > 0) {
        list = (1..categories)
    }
    list.eachWithIndex { value, index ->
        def row = formData.createDataRow()
        row.title = "Категория ${index + 1}"
        rows.add(row)
    }

    rows.each { row ->
        editableColumns.each {
            row.getCell(it).editable = true
            row.getCell(it).setStyleAlias('Редактируемая')
        }
        row.getCell('title').setStyleAlias('Заголовок')
    }
    return rows
}

def getNewRow(def title, isEditable) {
    def row = formData.createDataRow()
    row.title = title
    row.getCell('title').setStyleAlias('Заголовок')

    if (!isEditable) {
        // сделать редактируемыми для периода: 1кв - 5..8 графы, полгода - 9..11 графа, 9 месяцев 12..14, год - 15..17
        def columns = editableColumns.grep(nonEmptyColumns[getReportPeriod().order])
        columns.each {
            row.getCell(it).editable = true
            row.getCell(it).setStyleAlias('Редактируемая')
        }
    }

    return row
}

// TODO (Ramil Timerbaev)
def getNewRow() {
    def row = formData.createDataRow()
    row.alias = '123456789_123456789_123456789_123456789_123456789'
    row.title = 'тестовая надпись'
    editableColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    row.getCell('title').setStyleAlias('Заголовок')

    // TODO (Ramil Timerbaev) для отладки
    ['subject', 'taxAuthority', 'kpp', 'oktmo', 'title'].each {
        row.getCell(it).editable = true
    }
    return row
}

// TODO (Ramil Timerbaev) для отладки
// Добавить новую строку
def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index = 1
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
    } else if (dataRows.size() > 0) {
        index = dataRows.size()
    }
    dataRowHelper.insert(getNewRow(), index)
}

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

// TODO (Ramil Timerbaev) доделать, проверить
/** Добавить фиксированные строки в начало каждой группы. */
void addFixedRows(def dataRows) {
    // добавить 2 фиксированные строки в начало каждой группы
    def row1 = getNewRow('признаваемых объектом налогообложения', true)
    def row2 = getNewRow('в т.ч. стоимость льготируемого имущества:', false)
    // TODO (Ramil Timerbaev)

    // объединить у групп первые 4 столбца
    def firstRowInGroup = null
    for (def row : dataRows) {
        def hasNull = groupColumns.findAll { row.getCell(it).value == null }
        if (hasNull) {
            continue
        }
        if (firstRowInGroup == null) {
            firstRowInGroup = row
            continue
        }

        // если строки различаются, то объединить предыдущие строки
        if (isDiffRow(firstRowInGroup, row, groupColumns)) {
            def countRowInGroup = row.getIndex() - firstRowInGroup.getIndex()
            groupColumns.each {
                firstRowInGroup.getCell(it).rowSpan = countRowInGroup
            }
            firstRowInGroup = row
        }

        // если строка последняя, то объединить последние строки
        if (row.getIndex() == dataRows.size()) {
            def countRowInGroup = row.getIndex() - firstRowInGroup.getIndex() + 1
            groupColumns.each {
                firstRowInGroup.getCell(it).rowSpan = countRowInGroup
            }
        }
    }
}

// TODO (Ramil Timerbaev) удалить?
def getTaxPeriod() {
    if (taxPeriod == null) {
        taxPeriod = getReportPeriod()?.taxPeriod
    }
    return taxPeriod
}

/** Получить последний день следующего месяца после конца текущего периода. */
def getNextMonthDate() {
    Calendar c = Calendar.getInstance()
    c.setTime(getReportPeriodEndDate())
    // выставить первое число, прибаврить 2 месяца и убрать один день - получится последний день следующего месяца после конца текущего периода
    c.set(Calendar.DAY_OF_MONTH, 1)
    c.add(Calendar.MONTH, 2)
    c.add(Calendar.DAY_OF_MONTH, -1)
}

/**
 * Обработать строки группы 1.
 *
 * @param rows строки группы 1
 * @param groupRowsMap мапа для хранения строк по группам
 * @param alias алиас графы которую надо заполнять
 */
void processRowsGroup1(def rows, def groupRowsMap, def alias) {
    // строка вида 8
    def subjectRow = getRowByName(rows, SUBJECT_ROW_VALUE)
    def subject = getValueInParentheses(subjectRow?.name)

    // строка вида 2
    def oktmoRow2 = getRowByName(rows, ROW_2_VALUE)
    def oktmo2 = getValueInParentheses(oktmoRow2?.name)

    // получить необходимые группы
    def group1_1 = getGroup1_1(rows)
    def group1_2_2 = getGroup1_2_2(rows)

    group1_2_2.each { groupRows ->
        for (def row : groupRows) {
            // строка вида 12
            def oktmoRow = groupRows.get(0)
            def oktmo = getValueInParentheses(oktmoRow?.name)
            def isEqualOktmo = (oktmo == oktmo2)


            // TODO (Ramil Timerbaev) возможно тут надо сделать расчет строк1 для всех rocords сразу и передавать в addNewRows
            // заполнить значениями строку1
            row1 = getNewRow('признаваемых объектом налогообложения', true)
            fillRow1(row1, isEqualOktmo, group1_1, group1_2_2, alias)

            def records = getRecords200(subject, oktmo)
            records.each { record ->
                addNewRows(record, groupRows, group1_1, isEqualOktmo, alias, groupRowsMap)
            }
        }
    }
}

// TODO (Ramil Timerbaev) удалить?
def getRowByAlias(def dataRows, def alias) {
    for (def row : dataRows) {
        if (row.getAlias() == alias) {
            return row
        }
    }
    return null
}

/**
 * Получить строку по надписи в графе 1.
 *
 * @param dataRows строки
 * @param name строка для поиска
 */
def getRowByName(def dataRows, def name) {
    for (def row : dataRows) {
        if (row.name != null && row.name.contains(name)) {
            return row
        }
    }
    return null
}

/**
 * Получить список групп 1.
 *
 * @param sourceDataRows строки источника
 */
def getRowsGroupBySubject(sourceDataRows) {
    // список со списком групп 1
    def result = []
    // списк строк группы 1
    def list = []
    for (def sourceRow : sourceDataRows) {
        // если значение первой графы источника равен FIRST_ROW_VALUE, то это начало группы 1
        if (sourceRow.name != null && sourceRow.name.contains(FIRST_ROW_VALUE) && !list.isEmpty()) {
            result.add(list)
            list = []
        }
        list.add(sourceRow)
    }
    return result
}

/** Получить значение из строки в круглых скобках. */
def getValueInParentheses(def value) {
    def from = value.indexOf('(') + 1
    def to = value.indexOf(')')
    if (from == to) {
        return null
    }
    return value.substring(from, to)
}

/**
 * Получить записи из справочника 200 "Параметры представления деклараций по налогу на имущество" по коду субъекта и октмо.

 * @param subject строковое значение кода субъекта из источника
 * @param oktmo строковое значение октмо из источника
 */
def getRecords200(def subject, def oktmo) {
    if (!subject || !oktmo) {
        return null
    }
    def key = subject + SEPARATOR + oktmo
    if (recordsMap[key] == null) {
        // получить id записи из справочника 4 "Коды субъектов Российской Федерации"
        def subjectId = getRefBookRecordId(4L, "CODE", subject)

        // получить id записи из справочника 96 "Общероссийский классификатор территорий муниципальных образований (ОКТМО)"
        def oktmoId = getRefBookRecordId(96L, "CODE", oktmo)

        // получить записи из справочника 200
        def filter = "REGION_ID = $subjectId and OKTMO = $oktmoId"
        def provider = formDataService.getRefBookProvider(refBookFactory, 200, providerCache)
        recordsMap[key] = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
    }
    return recordsMap[key]
}

/**
 * Получить id записи справочника.
 *
 * @param refBookId id справочника
 * @param alias атрибут по которому искать запись
 * @param value значение для поиска
 */
def getRefBookRecordId(Long refBookId, String alias, String value) {
    if (refBookId == null) {
        return null
    }
    def record = formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache,
            alias, value, getReportPeriodEndDate())
    return record?.record_id?.value
}

/**
 * Получить список групп 1.2.2. <br>
 * В группе 1 их может быть несколько, начинаются со строки с надписью
 * <b>"в т.ч. стоимость недвижимого имущества  по населенному пункту «1» (<Код ОКТМО 1>)"</b>.
 *
 * @param dataRows строки группы 1
 */
def getGroup1_2_2(def dataRows) {
    // хранит все группы 1.2.2
    def result = []
    // хранит список строк группы 1.2.2
    def list
    def canAdd = false
    for (def row : dataRows) {
        // если строка содержит надпись соответсвтующая первой строке группы 1.2.2,
        // то надо эту и следующие строки добавить в list
        if (row.name != null && row.name.contains(OKTMO_ROW_VALUE)) {
            canAdd = true
            // если строки группы до этого уже были добавлены, то текущая строка row - начало следующей группы,
            // и надо записать предыдущую в result, а строки новой группы в list
            if (list) {
                result.add(list)
            }
            list = []
        }
        if (canAdd) {
            list.add(row)
        }
    }
    // записать в result строки последней группы
    if (list) {
        result.add(list)
    }
    return result
}

/**
 * Получить группу 1.1. <br>
 * В группе 1 только одна группа 1.1, начинаются со строки с надписью
 * <b>"Стоимость движимого имущества, отраженного на балансе до 01.01.2013 (<Код ОКТМО 1>)"</b>
 * и заканчивается строкой с надписью <b>"ИТОГО с учетом корректировки"</b>.
 *
 * @param dataRows строки группы 1
 */
def getGroup1_1(def dataRows) {
    def result = []
    def canAdd = false
    for (def row : dataRows) {
        // если строка содержит надпись соответсвтующая первой строке группы 1.1,
        // то надо эту и следующие строки добавить в result
        if (row.name != null && row.name.contains(ROW_2_VALUE)) {
            canAdd = true
        }
        if (canAdd) {
            result.add(row)
        }
        // если строка содержит надпись соответсвтующая последней строке группы 1.1, то выход
        if (row.name != null && row.name.contains(ROW_TOTAL_VALUE)) {
            break
        }
    }
    return result
}

/**
 * Сформировать новые строки и добавить в мапу.
 *
 * @param record запись из справочника 200
 * @param group1_2_2 строки группы 1.2.2
 * @param group1_1 строки группы 1.1
 * @param isEqualOktmo значения ОКТМО из строк вида 2 и вида 12 равны
 * @param alias алиас графы для заполенения
 * @param groupRowsMap мапа для хранения строк по группам
 */
void addNewRows(def record, def group1_2_2, def group1_1, def isEqualOktmo, def alias, def groupRowsMap) {
    // ключ по значению 4ех графов (Код субъекта, Код НО, КПП, Код ОКТМО)
    def key = record.REGION_ID.value + SEPARATOR + record.TAX_ORGAN_CODE.value + SEPARATOR +
            record.KPP.value + SEPARATOR + record.OKTMO.value
    // первая строка в группе (с надписью 'признаваемых объектом налогообложения')
    def row1
    def newRows = []
    if (groupRowsMap[key] == null) {
        // добавить 2 фиксированные строки в начало каждой группы
        row1 = getNewRow('признаваемых объектом налогообложения', true)
        def row2 = getNewRow('в т.ч. стоимость льготируемого имущества:', false)
        newRows.add(row1)
        newRows.add(row2)
        groupRowsMap[key] = []
        groupRowsMap[key].add(row1)
        groupRowsMap[key].add(row2)
    } else {
        // если группа уже существует то получить первую строку в группе для дополнения значениями
        row1 = groupRowsMap[key].get(0)
    }
    // заполнить/дополнить значениями строку1
    fillRow1(row1, isEqualOktmo, group1_1, group1_2_2, alias)


    // TODO (Ramil Timerbaev) поменять "Категория N"
    // TODO (Ramil Timerbaev) сделать сортировку по алфавиту

    // TODO (Ramil Timerbaev) получить значение графы 7 из строки 27 источника

    def categoryRows = getNewRowsFromGroup1_2_2(group1_2_2, alias, groupRowsMap[key])
    categoryRows.each { row ->
        row.cost = row.cost31_12
    }
    newRows.addAll(categoryRows)

    // для каждой новой строки задать значения 4ех первых общих графов группы нф
    newRows.each { newRow ->
        newRow.subject      = record.REGION_ID.value
        newRow.taxAuthority = record.TAX_ORGAN_CODE.value
        newRow.kpp          = record.KPP.value
        newRow.oktmo        = record.OKTMO.value
    }

    groupRowsMap[key].add(categoryRows)
}

/**
 * Заполнить первую строку группы нф данными.
 *
 * @param row1 первая строка группы нф
 * @param isEqualOktmo значения ОКТМО из строк вида 2 и вида 12 равны
 * @param group1_1 строки группы 1.1
 * @param group1_2_2 строки группы 1.2.2
 * @param alias алиас графы для заполенения
 */
def fillRow1(def row1, def isEqualOktmo, def group1_1, def  group1_2_2, def alias) {
    def row18 = getRowByName(group1_2_2, ROW_TOTAL_VALUE)

    // СуммНедвиж - значение «Графы 7» строки вида 18 (формы-источника)
    def value18 = row18.getCell(alias).value
    // СуммДвиж - значение «Графы 7» строки вида 7 (формы-источника)
    def value7 = 0

    // посчитать СуммДвиж, если значение графы 7 строки 2 и строки 12 совпадают
    if (isEqualOktmo) {
        def row7 = getRowByName(group1_1, ROW_TOTAL_VALUE)
        value7 = row7.getCell(alias).value
    }

    // строка1.alias = СуммНедвиж + СуммДвиж
    def value = roundValue(value18 + value7, 2)
    row1.getCell(alias).setValue(value, null)

    if (getReportPeriod()?.order == 4) {
        row1.cost31_12 = roundValue(value18, 2)
    }
}

/**
 * Получить список новых строк из группы 1.2.2.
 *
 * @param group1_2_2 строки группы 1.2.2
 * @param alias алиас графы для заполенения
 * @param rows строки одной группы нф
 */
def getNewRowsFromGroup1_2_2(def group1_2_2, def alias, def rows) {
    // TODO (Ramil Timerbaev) возможно надо поиск уже сущестующей строки в row по названию категории через метод getRowsByName

    // получить список (строки 1.2.2.2) из строк 1.2.2
    // для каждой (строки 1.2.2.2)
    //     получить строку 27 из 1.2.2.2 по надписи "с учетом корректировки"
    //     из строки 27 получить название категории, значение графы 7 (СуммЛьготКатег)
    //     получить новую строку формы и задать название категории и значение графы 7 в нужную ячейку по alias
    // результат список новых строк
    def row = getNewRow('Категория N', true)
}