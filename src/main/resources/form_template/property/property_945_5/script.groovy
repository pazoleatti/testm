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
 *      - расчеты
 *      - убрать лишнее
 *      - в чтз большие изменения: сбор данных (консолидация) выполняется при рассчитать, добавились логические проверки и т.д. сверить все по чтз
 *      - графа title возможно будет справочной
 *
 * @author Ramil Timerbaev
 */

// графа 1  - subject           - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
// графа 2  - taxAuthority
// графа 3  - kpp
// графа 4  - oktmo             - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
// графа    - title             // TODO (Ramil Timerbaev) возможно эта графа будет справочной
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
// графа 17 - cost13
// графа 18 - cost31_12

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        if (formData.kind != FormDataKind.SUMMARY) {
            logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
        }
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkPrevForm()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        checkPrevForm()
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
        // консолидация для этой формы выполняется при расчете, а не при принятии источников
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
        'cost4', 'cost5', 'cost6', 'cost7', 'cost8', 'cost9', 'cost10', 'cost11', 'cost12', 'cost13', 'cost31_12']

// первые 5 графы (графа 1..5)
@Field
def first5Columns = ['subject', 'taxAuthority', 'kpp', 'oktmo', 'title']

// графы 1 квартала (графа 5..8)
@Field
def сolumnsOrder1 = ['cost1', 'cost2', 'cost3', 'cost4']

// графы для полгода (графа 9..11)
@Field
def сolumnsOrder2 = ['cost5', 'cost6', 'cost7']

// графы 9 месяцев (графа 12..14)
@Field
def сolumnsOrder3 = ['cost8', 'cost9', 'cost10']

// графы 9 месяцев (графа 15..17)
@Field
def сolumnsOrder4 = [ 'cost11', 'cost12', 'cost13', 'cost31_12']

// Мапа со списком алиасов проверяемые на пустые значения (графа 1..18). Доступ к алиасам по номеру периода
@Field
def nonEmptyColumnsMap = [
        1 : first5Columns + сolumnsOrder1,
        2 : first5Columns + сolumnsOrder1 + сolumnsOrder2,
        3 : first5Columns + сolumnsOrder1 + сolumnsOrder2 + сolumnsOrder3,
        4 : first5Columns + сolumnsOrder1 + сolumnsOrder2 + сolumnsOrder3 + сolumnsOrder4
]

// Мапа со списком редактируемых алиасов (графа 1..18). Доступ к алиасам по номеру периода
@Field
def editableColumnsMap = [
        1 : сolumnsOrder1,
        2 : сolumnsOrder2,
        3 : сolumnsOrder3,
        4 : сolumnsOrder4
]

// Группируевые атрибуты (графа 1..4)
@Field
def groupColumns = ['subject', 'taxAuthority', 'kpp', 'oktmo']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 5..18)
@Field
def totalColumns = ['cost1', 'cost2', 'cost3', 'cost4', 'cost5', 'cost6', 'cost7',
        'cost8', 'cost9', 'cost10', 'cost11', 'cost12', 'cost13', 'cost31_12']

// TODO (Ramil Timerbaev) убрать лишние переменные
@Field
def startDate = null

@Field
def endDate = null

@Field
def reportPeriod = null

// Признак периода ввода остатков
@Field
def isBalancePeriod

@Field
def yearStartDate = null

// для записей справочника 200
@Field
def recordsMap = [:]

@Field
def FIRST_ROW_VALUE = 'Стоимость имущества по субъекту Федерации'

@Field
def SUBJECT_ROW_VALUE = 'Стоимость недвижимого имущества по субъекту Федерации'

@Field
def OKTMO_ROW_VALUE = 'в т.ч. стоимость недвижимого имущества по населенному пункту'

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
def TOTAL_ROW_TITLE = 'В т.ч. стоимость льготируемого имущества (всего):'

@Field
def SEPARATOR = '#'

// мапа для хранения первой строки группы нф (ключ - значение 2ух графов: Код субъекта, Код ОКТМО)
@Field
def rows1Map = [:]

// форма 945.1
@Field
def sourceFormTypeId = 610

// Мапа для хранения названий периода и года по id формы источника (id формы -> периода и год)
@Field
def periodNameMap = [:]

// мапа для хранения алиаса заполняемого столбца для каждолго источника (id источника -> alias)
@Field
def aliasMap = [:]

// список с formData ежемесячных источников
@Field
def formDataSources = null

@Field
def sourceFormName = null

@Field
def refBook200Name = null

@Field
def sourcesPeriodMap = null

@Field
def prevDataRows = null

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
    // def dataRows = dataRowHelper.allCached

    // TODO (Ramil Timerbaev) пока при расчете данные формируются занова, по чтз нужно обновлять не все группы
    def newDataRows = getConsolidationRows()

    // расчет итогов - во второй строке каждой группы
    // получить группы со строками
    def groupsMap = getGroupsMap(newDataRows)

    // расчет итогов для каждой группы
    groupsMap.keySet().each {
        def rows = groupsMap[it]
        def row2 = rows.get(1)
        def from = 2
        def to = rows.size() - 1
        def categoryRows = rows[from..to]
        calcTotalSum(categoryRows, row2, totalColumns)
    }

    dataRowHelper.save(newDataRows)
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
        def columns = nonEmptyColumnsMap[periodOrder]
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

def getConsolidationRows() {
    // мапа для хранения строк по группам (ключ - значение 4ех графов: Код субъекта, Код НО, КПП, Код ОКТМО)
    def groupRowsMap = [:]

    // получить список форм источников
    def sources = getFormDataSources()

    // получить данные из источников, cформировать новые строки по ним
    sources.each { source ->
        // получить алиас заполняемой графы - зависит от месяца источника: январь - графа 5, февраль - графа 6 ... декабрь - графа 16, январь следующего года - 17.
        def alias = aliasMap[source.id]
        def sourceRows = formDataService.getDataRowHelper(source).getAll()

        // получить список групп 1
        def rowsGroup1 = getRowsGroupBySubject(sourceRows)
        rowsGroup1.each { rows ->
            processRowsGroup1(rows, alias, source.id, groupRowsMap)
        }
    }

    // сортировка / группировка
    def dataRows = sort(groupRowsMap)

    // дополнить данными из формы предыдущего отчетного периода
    addPrevData(dataRows)

    return dataRows
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

def checkPrevForm() {
    // 1. Проверка наличия форм-источников 945.1 в статусе «Принята»
    def periodsMap = getSourcesPeriodMap()
    periodsMap.each { period, monthOrders ->
        monthOrders.each { monthOrder ->
            formDataService.checkMonthlyFormExistAndAccepted(sourceFormTypeId, FormDataKind.PRIMARY,
                    formDataDepartment.id, period.id, monthOrder,false, logger, true)
        }
    }


    // 2. Проверка наличия формы поставщика 945.5 в статусе «Принята»
    if (getReportPeriod()?.order == 1 || isBalancePeriod()) {
        return
    }
    // проверить существование и принятость предыдущей формы, а также наличие данных в них.
    formDataService.checkFormExistAndAccepted(formData.formType.id, FormDataKind.SUMMARY, formDataDepartment.id, reportPeriod.id, true, logger, true)
}

def getNewRow(def title, isEditable) {
    def row = formData.createDataRow()
    row.title = title
    row.getCell('title').setStyleAlias('Заголовок')

    if (isEditable) {
        // сделать редактируемыми для периода: 1кв - 5..8 графы, полгода - 9..11 графа, 9 месяцев 12..14, год - 15..17
        def columns = editableColumnsMap[getReportPeriod().order]
        columns.each {
            row.getCell(it).editable = true
            row.getCell(it).setStyleAlias('Редактируемая')
            row.getCell(it).setValue(0, null)
        }
    }
    return row
}

/** Получить последний день следующего месяца после конца текущего периода. */
def getNextMonthEndDate() {
    Calendar c = Calendar.getInstance()
    c.setTime(getReportPeriodEndDate())
    // выставить первое число, прибаврить 2 месяца и убрать один день - получится последний день следующего месяца после конца текущего периода
    c.set(Calendar.DAY_OF_MONTH, 1)
    c.add(Calendar.MONTH, 2)
    c.add(Calendar.DAY_OF_MONTH, -1)
    return c.time
}

/**
 * Обработать строки группы 1.
 *
 * @param rows строки группы 1
 * @param alias алиас графы которую надо заполнять
 * @param sourceFormId идентификатор формы источника
 * @param groupRowsMap мапа для хранения строк по группам (ключ - значение 4ех графов: Код субъекта, Код НО, КПП, Код ОКТМО)
 */
void processRowsGroup1(def rows, def alias, def sourceFormId, def groupRowsMap) {
    // строка вида 8
    def subjectRow = getRowByName(rows, 'name', SUBJECT_ROW_VALUE)
    def subject = getValueInParentheses(subjectRow?.name)

    // строка вида 2
    def oktmoRow2 = getRowByName(rows, 'name', ROW_2_VALUE)
    def oktmo2 = getValueInParentheses(oktmoRow2?.name)

    // получить необходимые группы
    def group1_1 = getGroup1_1(rows)
    def groups1_2_2 = getGroup1_2_2(rows)

    for (def groupRows : groups1_2_2) {
        // строка вида 12
        def oktmoRow = groupRows.get(0)
        def oktmo = getValueInParentheses(oktmoRow?.name)
        def isEqualOktmo = (oktmo == oktmo2)

        // расчет строк1 для всех records сразу, он одинаковый для всех групп относящихся к записям справочнкиа 200
        def row1 = getRow1(subject, oktmo, isEqualOktmo, group1_1, groupRows, alias)

        def records = getRecords200(subject, oktmo)
        if (!records) {
            def sourceFormName = getSourceFormName()
            // название периода и года (месяц и год)
            def periodName = periodNameMap[sourceFormId]
            def refBookName = getRefBook200Name()
            logger.error("Параметры декларации «Код субъекта» = $subject и «Код ОКТМО» = $oktmo " +
                    "формы «$sourceFormName» за $periodName г. не предусмотрены " +
                    "(в справочнике «$refBookName» отсутствует такая запись)!")
            continue
        }
        records.each { record ->
            addNewRows(record, groupRows, alias, row1, subject, oktmo, groupRowsMap)
        }
    }
}

/**
 * Получить строку по надписи.
 *
 * @param dataRows строки
 * @param alias строки
 * @param value строка для поиска
 */
def getRowByName(def dataRows, def alias, def value) {
    for (def row : dataRows) {
        def cellValue = row.getCell(alias).value
        if (isContain(cellValue, value)) {
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
        if (isContain(sourceRow.name, FIRST_ROW_VALUE) && !list.isEmpty()) {
            result.add(list)
            list = []
        }
        list.add(sourceRow)
    }
    result.add(list)

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
        if (isContain(row.name, OKTMO_ROW_VALUE)) {
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
        if (isContain(row.name, ROW_2_VALUE)) {
            canAdd = true
        }
        if (canAdd) {
            result.add(row)
        }
        // если строка содержит надпись соответсвтующая последней строке группы 1.1, то выход
        if (isContain(row.name, ROW_TOTAL_VALUE)) {
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
 * @param groupRowsMap мапа для хранения строк по группам (ключ - значение 4ех графов: Код субъекта, Код НО, КПП, Код ОКТМО)
 */
void addNewRows(def record, def group1_2_2, def alias, def row1, def subject, def oktmo, def groupRowsMap) {
    // ключ по значению 4ех графов (Код субъекта, Код НО, КПП, Код ОКТМО)
    def key = subject + SEPARATOR + record.TAX_ORGAN_CODE.value + SEPARATOR +
            record.KPP.value + SEPARATOR + oktmo
        if (groupRowsMap[key] == null) {
        // добавить 2 фиксированные строки в начало каждой группы
        def newRow1 = getCloneRow(row1)
        def newRow2 = getNewRow(TOTAL_ROW_TITLE, false)
        groupRowsMap[key] = []
        groupRowsMap[key].add(newRow1)
        groupRowsMap[key].add(newRow2)
    }

    // сформировать список новых строк из группы 1.2.2 или дополнить существующие строки
    addNewRowsFromGroup1_2_2(group1_2_2, alias, key, groupRowsMap)

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
def fillRow1(def row1, def isEqualOktmo, def group1_1, def group1_2_2, def alias) {
    def row18 = getRowByName(group1_2_2, 'name', ROW_TOTAL_VALUE)

    // СуммНедвиж - значение «Графы 7» строки вида 18 (формы-источника)
    def value18 = row18.taxBaseSum
    // СуммДвиж - значение «Графы 7» строки вида 7 (формы-источника)
    def value7 = 0

    // посчитать СуммДвиж, если значение графы 7 строки 2 и строки 12 совпадают
    if (isEqualOktmo) {
        def row7 = getRowByName(group1_1, 'name', ROW_TOTAL_VALUE)
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
 * @param groupRowsMap мапа для хранения строк по группам (ключ - значение 4ех графов: Код субъекта, Код НО, КПП, Код ОКТМО)
 */
void addNewRowsFromGroup1_2_2(def group1_2_2, def alias, def key, groupRowsMap) {
    def rows27 = []
    def isGroup1_2_2_2 = false
    // среди строк 1.2.2 отобрать строки 27 - строки с надписью "ИТОГО <Категория 1 имущества> с учетом корректировки"
    for (def row : group1_2_2) {
        if (isContain(row.name, GROUP_1_2_2_2_BEGIN)) {
            isGroup1_2_2_2 = true
            continue
        }
        if (isGroup1_2_2_2 && isContain(row.name, ROW_27_VALUE_BEGIN) && isContain(row.name, ROW_27_VALUE_END)) {
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

        // проверить название категории
        if (!categoryName) {
            categoryName = "Без категории"
        }

        // определить по глобальной мапе по ключу key есть ли в группе строка с такой категорией
        def categoryRow = getRowByName(groupRowsMap[key], 'title', categoryName)
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
        row.cost31_12 = row.cost13
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

/**
 * Получить список форм источников.
 * Заполняет и возвращает глобальную переменную formDataSources - список форм источников.
 * Заполняет глобальные переменные: aliasMap, periodNameMap
 */
def getFormDataSources() {
    if (formDataSources == null) {
        // найти все ежемесячные источники (принятые)
        formDataSources = []

        // найти все ежемесячные источники за текущий периоде и за первый месяц следующего периода
        def periodsMap = getSourcesPeriodMap()
        periodsMap.each { period, monthOrders ->
            monthOrders.each { monthOrder ->
                FormData source = formDataService.findMonth(sourceFormTypeId, FormDataKind.PRIMARY, formDataDepartment.id, period.taxPeriod.id, monthOrder)
                if (source != null && source.getState() == WorkflowState.ACCEPTED) {
                    def alias = 'cost' + monthOrder
                    // если форма за январь следующего года, то заполняется графа 17 (cost13)
                    if (period.taxPeriod.id != getReportPeriod().taxPeriod.id) {
                        alias = 'cost13'
                    }
                    aliasMap[source.id] = alias
                    periodNameMap[source.id] = Formats.getRussianMonthNameWithTier(monthOrder) + ' ' + period.taxPeriod.year
                    formDataSources.add(source)
                }
            }
        }
    }
    return formDataSources
}

/** Получить мапу с периодами и месяцами форм источников. Период -> список номеров месяцев. */
def getSourcesPeriodMap() {
    if (sourcesPeriodMap == null) {
        sourcesPeriodMap = [:]

        def reportPeriod = getReportPeriod()
        def order = reportPeriod?.order
        // получить список месяцев
        def monthMap = [
                1 : (1..3),
                2 : (4..6),
                3 : (7..9),
                4 : (10..12)
        ]
        // получить список месяцев в периоде без первого месяца (кроме 1 квартала, для него нужен первый месяц)
        def months
        if (order > 1) {
            months = monthMap[order] - (order * 3 - 2)
        } else {
            months = monthMap[order]
        }
        sourcesPeriodMap[reportPeriod] = months

        // получить еще форму за первый месяц следующего периода
        def from = getReportPeriodEndDate() + 1
        def to = getNextMonthEndDate()
        def nextPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.PROPERTY, from, to)
        if (nextPeriods != null && nextPeriods.size() == 1) {
            nextTaxPeriod = nextPeriods.get(0)
            nextMonthNumber = monthMap[nextTaxPeriod?.order].get(0)
            sourcesPeriodMap[nextTaxPeriod] = [nextMonthNumber]
        } else {
            println "property_945_5: impossible to get first month from next period." // TODO (Ramil Timerbaev)
        }
    }
    return sourcesPeriodMap
}

/** Получить строки за предыдущий отчетный период. */
def getPrevDataRows() {
    if (getReportPeriod()?.order == 1 || isBalancePeriod()) {
        return null
    }
    if (prevDataRows == null) {
        def prevFormData = formDataService.getFormDataPrev(formData, formDataDepartment.id)
        prevDataRows = (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allCached : null)
    }
    return prevDataRows
}

// Признак периода ввода остатков.
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

/** Дополнить данными из формы предыдущего отчетного периода. */
void addPrevData(def dataRows) {
    def prevRows = getPrevDataRows()
    // если нет предыдущих данных то выход
    if (!prevRows) {
        return
    }
    for (def row : dataRows) {
        // пропутсить строки с итогами (вторая строка в группе)
        if (row.title == TOTAL_ROW_TITLE) {
            continue
        }
        // найти строки группы из предыдущего года
        def prevGroupRows = []
        for (def prevRow : prevRows) {
            if (prevRow.subject == row.subject && prevRow.taxAuthority == row.taxAuthority &&
                    prevRow.kpp == row.kpp && prevRow.oktmo == row.oktmo) {
                prevGroupRows.add(prevRow)
            }
        }
        // скопировать если текущая форма: 1кв - не надо копировать, полгода - 5..8 графа, 9 месяцев 9..11, год - 12..14
        def someColumns = editableColumnsMap[getReportPeriod().order - 1]
        if (prevGroupRows) {
            def prevCategoryRow = getRowByName(prevGroupRows, 'title', row.title)
            someColumns.each { column ->
                def value = prevCategoryRow.getCell(column).value
                row.getCell(column).setValue(value, null)
            }
        } else {
            // если в предыдущей форме не было такой группы, то занулять
            someColumns.each { column ->
                row.getCell(column).setValue(BigDecimal.ZERO, null)
            }
        }
    }
}

/** Сортировка / группировка. */
def sort(def groupRowsMap) {
    def dataRows = []
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
    return dataRows
}

/**
 * Проверить содержит ли строка подстроку.
 *
 * @param value строка
 * @param sub подстрока
 */
def isContain(String value, String sub) {
    return value != null && sub != null && value.toLowerCase().contains(sub.toLowerCase())
}

/** Получить копию строки. */
def getCloneRow(def row) {
    if (row == null) {
        return null
    }
    def newRow = formData.createDataRow()
    allColumns.each {
        def newCell = newRow.getCell(it)
        def cell = row.getCell(it)

        newCell.setValue(cell.value, null)
        newCell.editable = cell.editable
        newCell.styleAlias = cell.styleAlias
        newCell.colSpan = cell.colSpan
        newCell.rowSpan = cell.rowSpan
    }
    return newRow
}