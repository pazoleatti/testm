package form_template.property.property_945_5

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.Formats
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * "(945.5) Сводная форма данных бухгалтерского учета для расчета налога на имущество".
 * formTemplateId=10640
 *
 * TODO:
 *      - консолидация
 *      - расчеты
 *      - убрать лишнее
 *      - консолидация не будет отрабатывать при принятии источника за первый месяц следующего периода
 *      - поправить получение источников при консолидации:
 *          смещение на 1 месяц - второй, третий месяц текущего квартала и первый месяц следующего,
 *          для первого кваратала надо брать еще источник за январь
 *      - в чтз большие изменения: сбор данных (консолидация) выполняется при рассчитать, добавились логические проверки и т.д. сверить все по чтз
 *      - добавить получение данных из предыдущей принятой фомры 945.5
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
        // checkPrevForm()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        // TODO (Ramil Timerbaev) консолидация не будет отрабатывать при принятии источника за первый месяц следующего периода
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
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

// Группируевые атрибуты (графа 1..4)
@Field
def groupColumns = ['subject', 'taxAuthority', 'kpp', 'oktmo']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 5..18)
@Field
def totalColumns = ['cost1', 'cost2', 'cost3', 'cost4', 'cost5', 'cost6', 'cost7',
        'cost8', 'cost9', 'cost10', 'cost11', 'cost12', 'cost', 'cost31_12']

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
def GROUP_1_2_2_2_BEGIN = 'Льготируемое имущество (всего)'

@Field
def ROW_27_VALUE_BEGIN = 'ИТОГО'

@Field
def ROW_27_VALUE_END = 'с учетом корректировки'

@Field
def SEPARATOR = '#'

// мапа для хранения строк по группам (ключ - значение 4ех графов: Код субъекта, Код НО, КПП, Код ОКТМО)
@Field
def groupRowsMap = [:]

// мапа для хранения первой строки группы нф (ключ - значение 2ух графов: Код субъекта, Код ОКТМО)
@Field
def rows1Map = [:]

// форма 945.1
@Field
def sourceFormTypeId = 610

// Мапа для хранения названий периода и года по id формы источника (id формы -> периода и год)
@Field
def periodNameMap = [:]

@Field
def sourceFormName = null

@Field
def refBook200Name = null

// TODO (Ramil Timerbaev) убрать?
//def getReportPeriodStartDate() {
//    if (startDate == null) {
//        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
//    }
//    return startDate
//}

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

// TODO (Ramil Timerbaev) убрать?
//// Разыменование записи справочника
//def getRefBookValue(def long refBookId, def Long recordId) {
//    if (recordId == null) {
//        return null
//    }
//    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
//}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // расчет итогов - во второй строке каждой группы
    // получить группы со строками
    def groupsMap = getGroupsMap(dataRows)
    // расчет итогов для каждой группы
    groupsMap.keySet().each {
        def rows = groupsMap[it]
        def row2 = rows.get(1)
        def from = 2
        def to = rows.size() - 1
        def categoryRows = rows[from..to]
        calcTotalSum(categoryRows, row2, totalColumns)
    }

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

        // 3. Проверка значений Граф 5-17 по строке «В т.ч. стоимость льготируемого имущества (всего):» (подсчет итогов)
        // получить группы со строками
        def groupsMap = getGroupsMap(dataRows)
        // расчет итогов для каждой группы
        groupsMap.keySet().each {
            def tmpRow = formData.createDataRow()
            def rows = groupsMap[it]
            def row2 = rows.get(1)
            def from = 2
            def to = rows.size() - 1
            def categoryRows = rows[from..to]
            calcTotalSum(categoryRows, tmpRow, totalColumns)
            totalColumns.each { alias ->
                def value1 = row2.getCell(alias).value
                def value2 = tmpRow.getCell(alias).value
                if (value1 != value2) {
                    logger.error(WRONG_TOTAL, getColumnName(row2, alias))
                }
            }
        }
    }
    // 4. Проверка корректности разбития пользователем сумм по группам строк с одинаковым значением параметров «Код субъекта», «Код ОКТМО» (для строки вида «Признаваемых объектом налогообложения»)
    // По каждой группе строк с одинаковым значением Граф 1, 4:
    // Сумма значений по «Графе N» по строке «Признаваемых объектом налогообложения» равна значению, рассчитанному согласно Табл. 27,
    // где N = 5, 6, …, 18
    // TODO (Ramil Timerbaev)
    if (false) {
        def list = []
        someColumns.each { alias ->
            def columnName = getColumnName(row, alias)
            def value = null
            list.add("Графа «$columnName» = $value")
        }
        if (list) {
            def msgColumnNames = list.join(', ')
            logger.error("Параметры декларации «Код субъекта» = $subject, «Код ОКТМО» = $oktmo: " +
                    "Итоговые значения остаточной стоимости основных средств, " +
                    "признаваемых объектом налогообложения, заполнены неверно! " +
                    "Ожидаются следующие значения: $msgColumnNames")
        }
    }

    // 5. Проверка корректности разбития пользователем сумм по группам строк с одинаковым значением параметров «Код субъекта», «Код ОКТМО» (для каждой строки вида «Категория K»)
    // По каждой категории группы строк с одинаковым значением Граф 1, 4:
    // Сумма значений по «Графе N» по строке «Категория К» равна значению, рассчитанному согласно Табл. 27,
    // где N = 5, 6, …, 18
    // logger.error("Параметры декларации «Код субъекта» = <Код субъекта>, «Код ОКТМО» = <Код ОКТМО>: Итоговые значения остаточной стоимости основных средств по категории «<Категория K>» заполнены неверно! Ожидаются следующие значения: Графа «Наименование поля 5» = <Значение поля 5>, Графа «Наименование поля 6» = <Значение поля 6>, …, Графа «Наименование поля N» = <Значение поля N>")
    // TODO (Ramil Timerbaev) аналогично логической проверке 4, только для строк с критериями
}

def consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

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

    // мапа для хранения алиаса заполняемого столбца для каждолго источника (id источника -> alias)
    def aliasMap = [:]
    // найти все ежемесячные источники (принятые)
    def sources = []
    // найти все ежемесячные источники в текущем налоговом периоде
    monthOrders.each { monthOrder ->
        FormData source = formDataService.findMonth(sourceFormTypeId, FormDataKind.PRIMARY, formDataDepartment.id, taxPeriodId, monthOrder)
        if (source != null && source.getState() == WorkflowState.ACCEPTED) {
            sources.add(source)
            aliasMap[source.id] = 'cost' + monthOrder
            periodNameMap[source.id] = Formats.getRussianMonthNameWithTier(monthOrder) + ' ' + getReportPeriod()?.taxPeriod?.year
        }
    }
    // найти все ежемесячные источники в следующем налоговом периоде (за первый месяц)
    if (nextTaxPeriod) {
        FormData source = formDataService.findMonth(sourceFormTypeId, FormDataKind.PRIMARY, formDataDepartment.id, nextTaxPeriod.id, 1)
        if (source != null && source.getState() == WorkflowState.ACCEPTED) {
            sources.add(source)
            aliasMap[source.id] = 'cost'
            periodNameMap[source.id] = Formats.getRussianMonthNameWithTier(1) + ' ' + nextTaxPeriod?.taxPeriod?.year
        }
    }

    // получить данные из источников, cформировать новые строки по ним
    sources.each { source ->
        // получить алиас заполняемой графы - зависит от месяца источника: январь - графа 5, февраль - графа 6 ... декабрь - графа 16, январь следующего года - 17.
        def alias = aliasMap[source.id]
        def sourceRows = formDataService.getDataRowHelper(source).getAll()

        // получить список групп 1
        def rowsGroup1 = getRowsGroupBySubject(sourceRows)
        rowsGroup1.each { rows ->
            processRowsGroup1(rows, alias, source.id)
        }
    }

    // сортировка / группировка
    groupRowsMap.keySet().sort().each {
        def rows = groupRowsMap[it]
        if (rows) {
            // сгруппировать строки по графам 1..4
            def row1 = rows.get(0)
            def row2 = rows.get(1)
            groupColumns.each {
                row1.getCell(it).rowSpan = rows.size()
            }

            // сортировка категории группы по алфавиту
            def categoryRows = rows - row1 - row2
            sortRows(categoryRows, ['title'])
            def sortedRows = [row1, row2] + categoryRows

            // добавить строки групп в форму
            dataRows.addAll(sortedRows)
        }
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

// TODO (Ramil Timerbaev) убрать?
///** Получить предыдущие формы за текущий год. */
//def getPrevForms() {
//    if (!prevForms) {
//        prevForms = []
//        def reportPeriods = getPrevReportPeriods()
//        for (def reportPeriod : reportPeriods) {
//            // получить формы за 1 кв, полгода, 9 месяцев
//            def form = formDataService.find(formData.formType.id, FormDataKind.SUMMARY, formDataDepartment.id, reportPeriod.id)
//            if (form) {
//                prevForms.add(form)
//            }
//        }
//    }
//    return prevForms
//}

// TODO (Ramil Timerbaev) убрать?
///** Получить предыдущие преиоды за год. */
//def getPrevReportPeriods() {
//    if (prevReportPeriods == null) {
//        prevReportPeriods = []
//        // получить периоды за год
//        def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.PROPERTY, getYearStartDate(), getReportPeriodEndDate())
//        for (def reportPeriod : reportPeriods) {
//            if (reportPeriod.id == formData.reportPeriodId) {
//                continue
//            }
//            prevReportPeriods.add(reportPeriod)
//        }
//    }
//    return prevReportPeriods
//}

// TODO (Ramil Timerbaev) убрать?
//def checkPrevForm() {
//    return // TODO (Ramil Timerbaev)
//    if (!isPeriodYear()) {
//        return
//    }
//    def reportPeriods = getPrevReportPeriods()
//    // проверить существование и принятость форм, а также наличие данных в них.
//    for (def reportPeriod : reportPeriods) {
//        formDataService.checkFormExistAndAccepted(formData.formType.id, FormDataKind.SUMMARY, formDataDepartment.id, reportPeriod.id, false, logger, true)
//    }
//}

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
            row.getCell(it).setValue(0, null)
        }
    }

    return row
}

// TODO (Ramil Timerbaev) убрать?
//def getNewRow() {
//    def row = formData.createDataRow()
//    row.alias = '123456789_123456789_123456789_123456789_123456789'
//    row.title = 'тестовая надпись'
//    editableColumns.each {
//        row.getCell(it).editable = true
//        row.getCell(it).setStyleAlias('Редактируемая')
//    }
//    row.getCell('title').setStyleAlias('Заголовок')
//
//    // TODO (Ramil Timerbaev) для отладки
//    ['subject', 'taxAuthority', 'kpp', 'oktmo', 'title'].each {
//        row.getCell(it).editable = true
//    }
//    return row
//}

// TODO (Ramil Timerbaev) Убрать? для отладки
//// Добавить новую строку
//def addRow() {
//    def dataRowHelper = formDataService.getDataRowHelper(formData)
//    def dataRows = dataRowHelper.allCached
//    def index = 1
//    if (currentDataRow != null) {
//        index = currentDataRow.getIndex()
//    } else if (dataRows.size() > 0) {
//        index = dataRows.size()
//    }
//    dataRowHelper.insert(getNewRow(), index)
//}

// TODO (Ramil Timerbaev) убрать?
//def getTaxPeriod() {
//    if (taxPeriod == null) {
//        taxPeriod = getReportPeriod()?.taxPeriod
//    }
//    return taxPeriod
//}

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
 * @param alias алиас графы которую надо заполнять
 * @param sourceFormId идентификатор формы источника
 */
void processRowsGroup1(def rows, def alias, def sourceFormId) {
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

            // расчет строк1 для всех records сразу, он одинаковый для всех групп относящихся к записям справочнкиа 200
            def row1 = getRow1(subject, oktmo, isEqualOktmo, group1_1, group1_2_2, alias)

            def records = getRecords200(subject, oktmo)
            if (!record) {
                def sourceFormName = getSourceFormName()
                // название периода и года (месяц и год)
                def periodName = periodNameMap[sourceFormId]
                def refBookName = getRefBook200Name()
                logger.error("Параметры представления декларации Код субъекта = $subject и Код ОКТМО = $oktmo " +
                        "формы «$sourceFormName» за $periodName г. не предусмотрены " +
                        "(в справочнике «$refBookName» отсутствует такая запись)!")
                continue
            }
            records.each { record ->
                addNewRows(record, groupRows, alias, row1, subject, oktmo)
            }
        }
    }
}

// TODO (Ramil Timerbaev) убрать?
//def getRowByAlias(def dataRows, def alias) {
//    for (def row : dataRows) {
//        if (row.getAlias() == alias) {
//            return row
//        }
//    }
//    return null
//}

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
 * Сформировать новые строки и добавить в мапу или дополнить существующие строки.
 *
 * @param record запись из справочника 200
 * @param group1_2_2 строки группы 1.2.2
 * @param alias алиас графы для заполенения
 * @param row1 первая строка группы
 * @param subject субъект
 * @param oktmo октмо
 */
void addNewRows(def record, def group1_2_2, def alias, def row1, def subject, def oktmo) {
    // ключ по значению 4ех графов (Код субъекта, Код НО, КПП, Код ОКТМО)
    def key = subject + SEPARATOR + record.TAX_ORGAN_CODE.value + SEPARATOR +
            record.KPP.value + SEPARATOR + oktmo
    def newRows = []
    if (groupRowsMap[key] == null) {
        // добавить 2 фиксированные строки в начало каждой группы
        def row2 = getNewRow('В т.ч. стоимость льготируемого имущества (всего):', false)
        newRows.add(row1)
        newRows.add(row2)
        groupRowsMap[key] = []
        groupRowsMap[key].add(row1)
        groupRowsMap[key].add(row2)
    }

    // сформировать список новых строк из группы 1.2.2 или дополнить существующие строки
    addNewRowsFromGroup1_2_2(group1_2_2, alias, key)

    // для каждой строки группы задать значения 4ех первых общих графов группы нф
    groupRowsMap[key].each { newRow ->
        newRow.subject      = record.REGION_ID.value
        newRow.taxAuthority = record.TAX_ORGAN_CODE.value
        newRow.kpp          = record.KPP.value
        newRow.oktmo        = record.OKTMO.value
    }
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
    def value18 = row18.taxBaseSum
    // СуммДвиж - значение «Графы 7» строки вида 7 (формы-источника)
    def value7 = 0

    // посчитать СуммДвиж, если значение графы 7 строки 2 и строки 12 совпадают
    if (isEqualOktmo) {
        def row7 = getRowByName(group1_1, ROW_TOTAL_VALUE)
        value7 = row7.taxBaseSum
    }

    // строка1.alias = СуммНедвиж + СуммДвиж
    def value = roundValue(value18 + value7, 2)
    row1.getCell(alias).setValue(value, null)

    if (getReportPeriod()?.order == 4) {
        row1.cost31_12 = roundValue(value18, 2)
    }
}

/**
 * Сформировать список новых строк из группы 1.2.2 или дополнить существующие строки.
 *
 * @param group1_2_2 строки группы 1.2.2
 * @param alias алиас графы для заполенения
 * @param key ключ по значению 4ех графов (Код субъекта, Код НО, КПП, Код ОКТМО)
 */
void addNewRowsFromGroup1_2_2(def group1_2_2, def alias, def key) {
    def rows27 = []
    def isGroup1_2_2_2 = false
    // среди строк 1.2.2 отобрать строки 27 - строки с надписью "ИТОГО <Категория 1 имущества> с учетом корректировки"
    for (def row : group1_2_2) {
        if (row.name != null && row.contains(GROUP_1_2_2_2_BEGIN)) {
            isGroup1_2_2_2 = true
            continue
        }
        if (isGroup1_2_2_2 && row.name != null && row.contains(ROW_27_VALUE_BEGIN)) {
            rows27.add(row)
        }
    }
    for (def row27 : rows27) {
        // 	из строки 27 получить название категории
        def from = ROW_27_VALUE_BEGIN.length()
        def to = row27.name.indexOf(ROW_27_VALUE_END)
        def categoryName = row27.name
        if (to > -1) {
            categoryName = row27.name.substring(from, to).trim()
        } else {
            println "property_945_5: impossible to get the category name from ${row27.name}." // TODO (Ramil Timerbaev)
        }

        // определить по глобальной мапе по ключу key есть ли в группе строка с такой категорией
        def categoryRow = getRowByName(groupRowsMap[key], categoryName)
        if (categoryRow == null) {
            // строка с такой категорией не найден, сформировать новую строку
            categoryRow = getNewRow(categoryName, true)
            groupRowsMap[key].add(categoryRow)
        }
        // задать значение графы 7 (СуммЛьготКатег) в нужную ячейку по alias
        categoryRow.getCell(alias).setValue(row27.taxBaseSum, null)
    }
    // для каждой "строка категория" задать графа 18 = графа 17
    groupRowsMap[key].each { row ->
        row.cost = row.cost31_12
    }
}

/**
 * Получить заполненную первую строку группы нф по субъекту и октмо.
 *
 * @param subject субъект
 * @param oktmo октмо
 * @param isEqualOktmo значения ОКТМО из строк вида 2 и вида 12 равны
 * @param group1_1 строки группы 1.1
 * @param group1_2_2 строки группы 1.2.2
 * @param alias алиас графы для заполенения
 */
def getRow1(def subject, def oktmo, def isEqualOktmo, def group1_1, def group1_2_2, def alias) {
    // ключ по значению 2ух графов (Код субъекта, Код ОКТМО)
    def key = subject + SEPARATOR + oktmo
    if (rows1Map[key] == null) {
        rows1Map[key] = getNewRow('Признаваемых объектом налогообложения', true)
    }
    def row1 = rows1Map[key]

    // заполнить значениями строку1
    fillRow1(row1, isEqualOktmo, group1_1, group1_2_2, alias)

    return row1
}

/** Получить мапу со строками групп. Ключ (Код субъекта, Код НО, КПП, Код ОКТМО) -> строки группы. */
def getGroupsMap(def dataRows) {
    def groupsMap = [:]
    // найти группы
    dataRows.each { row ->
        def key = row.subject + SEPARATOR + row.taxAuthority + SEPARATOR + row.kpp + SEPARATOR + row.oktmo
        if (groupsMap[key] == null) {
            groupsMap[key] = []
        }
        groupsMap[key].add(row)
    }
    return groupsMap
}

/** Получить название формы источника. */
def getSourceFormName() {
    if (sourceFormName == null) {
        sourceFormName = formTypeService.get(sourceFormTypeId)?.name
    }
    return sourceFormName
}


/** Получить название справочника 200. */
def getRefBook200Name() {
    if (refBook200Name == null) {
        refBook200Name = refBookFactory.get(200L)?.name
    }
    return refBook200Name
}