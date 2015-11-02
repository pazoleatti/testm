package form_template.property.property_945_5.v2014

import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.FormType
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.Formats
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * "(945.5) Сводная форма данных бухгалтерского учета для расчета налога на имущество".
 * formTemplateId=615
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
// графа 17 - cost13
// графа 18 - cost31_12

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkPrevForm()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
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
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.GET_SOURCES:
        getSources()
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

// графа 2..8 первичной 945.1
@Field
def columnsFromPrimary945_5 = ['taxBase1', 'taxBase2', 'taxBase3', 'taxBase4', 'taxBase5', 'taxBaseSum']

@Field
def endDate = null

@Field
def startDate = null

@Field
def reportPeriod = null

// Признак периода ввода остатков
@Field
def isBalancePeriod

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
def GROUP_1_2_2_2_BEGIN = 'Льготируемое имущество'

@Field
def ROW_27_VALUE_BEGIN = 'ИТОГО'

@Field
def ROW_27_VALUE_END = 'с учетом корректировки'

@Field
def TOTAL_ROW_TITLE = 'В т.ч. стоимость льготируемого имущества (всего):'

@Field
def UNCATEGORIZED = 'Без категории'

@Field
def SEPARATOR = '#'

// мапа для хранения первой строки группы нф (ключ - значение 2ух графов: Код субъекта, Код ОКТМО)
@Field
def rows1Map = [:]

// мапа для хранения строк по группам (ключ - значение 4ех графов: Код субъекта, Код НО, КПП, Код ОКТМО)
// нужно чтобы при проверке после расчетов повторно не обращаться к источникам
@Field
def consolidationGroupRowsMap = [:]

// форма 945.1
@Field
def int sourceFormTypeId = 610

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
def refBookNameMap = [:]

// мапа с иточниками (ключ -период, значение - список месяцев)
@Field
def sourcesPeriodMap = null

@Field
def prevDataRows = null

// мапа для хранения первых строк новых групп для вывода информационных сообщении для них
@Field
def infoMessagesRowMap = [:]

// источники 945.1 (важны только подразделения и тип формы)
@Field
def formSources945_1 = null

// Мапа для хранения полного названия подразделения (id подразделения  -> полное название)
@Field
def departmentFullNameMap = [:]

// Мапа для хранения подразделений (id подразделения  -> подразделение)
@Field
def departmentMap = [:]

// Мапа для хранения типов форм (id типа формы -> тип формы)
@Field
def formTypeMap = [:]

@Field
def departmentReportPeriodMap = [:]

@Field
def formTemplateMap = [:]

// Мапа для хранения переодичности форм источников-приемников (id типа формы + id периода -> периодичность ежемесячная или квартальная)
@Field
def monthlyMap = [:]

// Мапа для хранения номером месяцев по кварталам (номер квартала -> список с номерами месяцев квартала)
@Field
def monthsInQuarterMap = [
        1 : (1..3),
        2 : (4..6),
        3 : (7..9),
        4 : (10..12)
]

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // получить группы со строками
    def groupsMap = getGroupsMap(dataRows)

    // получить группы из источников
    def tmpGroupsMap = getConsolidationGroupsRows()

    // получить обновленные группы формы из текущих групп и временных групп из источников
    def newGroupsMap = getNewGroupsMap(groupsMap, tmpGroupsMap)

    // сортировка / группировка
    def newDataRows = sort(newGroupsMap)

    // дополнить данными из формы предыдущего отчетного периода
    addPrevData(newDataRows)

    // расчет итогов - во второй строке каждой группы
    newGroupsMap.keySet().each {
        def rows = newGroupsMap[it]
        def row2 = rows.get(1)
        def from = 2
        def to = rows.size() - 1
        def categoryRows = rows[from..to]
        calcTotalSum(categoryRows, row2, totalColumns)
    }

    updateIndexes(newDataRows)
    infoMessagesRowMap.keySet().each { key ->
        def row = infoMessagesRowMap[key]

        def index = row.getIndex()
        def subject = getRefBookValue(4L, row.subject)?.CODE?.value
        def taxAuthority = row.taxAuthority
        def kpp = row.kpp
        def oktmo = getRefBookValue(96L, row.oktmo)?.CODE?.value
        logger.info("Строка $index: Создана группа строк по параметрам декларации " +
                "«Код субъекта» = $subject, «Код НО» = $taxAuthority, «КПП» = $kpp, «Код ОКТМО» = $oktmo")
    }

    updateIndexes(newDataRows)
    formDataService.getDataRowHelper(formData).allCached = newDataRows
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def periodOrder = getReportPeriod().order

    // получить группы со строками
    def groupsMap = getGroupsMap(dataRows)

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей
        def columns = nonEmptyColumnsMap[periodOrder]
        checkNonEmptyColumns(row, index, columns - first5Columns, logger, true)// TODO исправить в ScriptUtils на getOwnerValue

        // 2. Проверка на не заполнение поля
        columns = allColumns - columns
        columns.each { alias ->
            if (getOwnerValue(row, alias)) {
                String msg = String.format(errorMsg + "В текущем периоде формы графа «%s» должна быть не заполнена!", getColumnName(row, alias))
                logger.error(msg)
            }
        }

        // 3. Проверка значений Граф 5-17 по строке «В т.ч. стоимость льготируемого имущества (всего):» (подсчет итогов)
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

    // получить группы из источников
    def tmpGroupsMap = getConsolidationGroupsRows()

    // получить обновленные группы формы из текущих групп и временных групп из источников
    def newGroupsMap = getNewGroupsMap(groupsMap, tmpGroupsMap)

    // сортировка / группировка
    def newDataRows = sort(newGroupsMap)

    // дополнить данными из формы предыдущего отчетного периода
    addPrevData(newDataRows)
    newDataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
    newGroupsMap = getGroupsMap(newDataRows)//TODO упростить

    // 4. Проверка корректности разбития пользователем сумм по группам строк с одинаковым значением параметров «Код субъекта», «Код ОКТМО» (для строки вида «Признаваемых объектом налогообложения»)
    // По каждой группе строк с одинаковым значением Граф 1, 4:
    // Сумма значений по «Графе N» по строке «Признаваемых объектом налогообложения» равна значению, рассчитанному согласно Табл. 31,
    // где N = 5, 6, …, 18
    // 5. Проверка корректности разбития пользователем сумм по группам строк с одинаковым значением параметров «Код субъекта», «Код ОКТМО» (для каждой строки вида «Категория K»)
    // По каждой категории группы строк с одинаковым значением Граф 1, 4:
    // Сумма значений по «Графе N» по строке «Категория К» равна значению, рассчитанному согласно Табл. 31,
    // где N = 5, 6, …, 18
    newGroupsMap.keySet().each {
        // если не вторая строка в группе
        def rows = groupsMap[it]
        def newRows = newGroupsMap[it]
        def row1 = newRows.get(0)
        def row2 = newRows.get(1)
        newRows.each{ row ->
            if(row != row2){
                def list = []
                (allColumns - first5Columns).each { alias ->
                    def index = newRows.indexOf(row)
                    if (rows && newRows && rows[index] && newRows[index] && rows[index][alias] != newRows[index][alias]) {
                        def columnName = getColumnName(row, alias)
                        def value = row[alias]?:"''"
                        list.add("Графа «$columnName» = $value")
                    }
                }
                def title = row.title
                if (!list.isEmpty()) {
                    def subject = getRefBookValue(4, getOwnerValue(row, 'subject'))?.CODE?.value
                    def oktmo = getRefBookValue(96, getOwnerValue(row, 'oktmo'))?.CODE?.value
                    def msgColumnNames = list.join(', ')
                    logger.error("По группам строк с параметрами декларации «Код субъекта» = $subject, «Код ОКТМО» = $oktmo: " +
                            "остаточная стоимость основных средств" +
                            ((row == row1) ?
                                    ", признаваемых объектом налогообложения, заполнены неверно! " :
                                    " по строке «$title» заполнена неверно! "
                            ) +
                            "Ожидается следующая сумма значений по группам строк: $msgColumnNames")
                }
            }
        }
    }
}

/** Получить мапу с группами строк из источников. */
def getConsolidationGroupsRows() {
    if (!consolidationGroupRowsMap.isEmpty()) {
        return consolidationGroupRowsMap
    }
    // получить список форм источников
    def sources = getFormDataSources()

    // получить данные из источников, cформировать новые строки по ним
    sources.each { source ->
        // получить алиас заполняемой графы - зависит от месяца источника: январь - графа 5, февраль - графа 6 ... декабрь - графа 16, январь следующего года - 17.
        def alias = aliasMap[source.id]
        def sourceRows = formDataService.getDataRowHelper(source).allSaved

        // получить список групп 1
        def rowsGroup1 = getRowsGroupBySubject(sourceRows)
        rowsGroup1.each { rows ->
            processRowsGroup1(rows, alias, source.id, consolidationGroupRowsMap)
        }
    }
    return consolidationGroupRowsMap
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
    // 3. Проверка заполнения атрибута «Регион» подразделения текущей формы (справочник «Подразделения»)
    if (formDataDepartment.regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }

    // 1. Проверка наличия форм-источников 945.1 в статусе «Принята»
    def formSources945_1 = getFormSources()
    if (formSources945_1.isEmpty()) {
        def sourceFormType = getFormTypeById(sourceFormTypeId)
        throw new Exception("Не назначена источником налоговая форма «${sourceFormType.name}» в текущем периоде!")
    }
    def periodsMap = getSourcesPeriodMap()
    periodsMap.each { period, monthOrders ->
        monthOrders.each { monthOrder ->
            formSources945_1.each { formSource ->
                formDataService.checkMonthlyFormExistAndAccepted(sourceFormTypeId, formSource.kind,
                        formSource.departmentId, period.id, monthOrder,false, logger, true,
                        formData.comparativePeriodId, formData.accruing)
            }
        }
    }


    // 2. Проверка наличия формы поставщика 945.5 в статусе «Принята»
    if (getReportPeriod()?.order == 1 || isBalancePeriod()) {
        return
    }
    // проверить существование и принятость предыдущей формы, а также наличие данных в них.
    formDataService.checkFormExistAndAccepted(formData.formType.id, FormDataKind.SUMMARY, formDataDepartment.id,
            reportPeriod.id, true, logger, true, formData.comparativePeriodId, formData.accruing)
}

def getNewRow(def title, isEditable) {
    def row = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
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

        // расчет строк1 для всех records сразу, он одинаковый для всех групп относящихся к записям справочника 200
        def row1 = getRow1(subject, oktmo, isEqualOktmo, group1_1, groupRows, alias)

        def records = getRecords200(subject, oktmo)
        if (!records) {
            def sourceFormName = getSourceFormName()
            // название периода и года (месяц и год)
            def periodName = periodNameMap[sourceFormId]
            def refBookName = getRefBookName(200L)
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
            alias, value, getReportPeriodEndDate(), 0, null, logger, true)
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
    // добавить 2 фиксированные строки в начало каждой группы
    if (groupRowsMap[key] == null) {
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

    // СуммНедвижПоЛьготе2012000 для граф 5-18 строки "Признаваемых объектом налогообложения"
    if (getRowBenefitCodes(groupRowsMap[key][2]).contains('2012000')){
        groupRowsMap[key][0][alias] += groupRowsMap[key][2][alias]
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
        def categoryName
        if (to > -1) {
            categoryName = row27.name.substring(from, to).trim()
        } else {
            throw new Exception("Ошибка при получении наименования категории из '${row27.name}'.")
        }

        // проверить название категории - если названия нет, то это группа 1.2.2.2 из строк 19-21 (без категории)
        if (!categoryName) {
            // если у строки 21 (из группы где нет категории) все значения равны 0, то для нее не добавляем строку в сводную
            def hasOnlyZero = true
            for (def column : columnsFromPrimary945_5) {
                if (row27.getCell(column).value) {
                    hasOnlyZero = false
                    break
                }
            }
            // все значения в строке 21 равны 0, тогда пропускаем эту строку
            if (hasOnlyZero) {
                continue
            }
            categoryName = UNCATEGORIZED
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
        def key = getGroupKey(row)
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
        sourceFormName = getFormTypeById(sourceFormTypeId)?.name
    }
    return sourceFormName
}


/** Получить название справочника по идентификатору. */
def getRefBookName(def refBookId) {
    if (refBookNameMap[refBookId] == null) {
        refBookNameMap[refBookId] = refBookFactory.get(refBookId)?.name
    }
    return refBookNameMap[refBookId]
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
            // получить источники (важны только тип формы и подразделение)
            def formSources945_1 = getFormSources()
            // получить данные источников
            monthOrders.each { monthOrder ->
                formSources945_1.each { formSource ->
                    FormData source = formDataService.getLast(sourceFormTypeId, formSource.kind, formSource.departmentId, period.id, monthOrder, null, false)
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
    }
    return formDataSources
}

/** Получить мапу с периодами и месяцами форм источников. Период -> список номеров месяцев. */
def getSourcesPeriodMap() {
    if (sourcesPeriodMap == null) {
        sourcesPeriodMap = [:]

        def reportPeriod = getReportPeriod()
        def order = reportPeriod?.order

        // получить список месяцев в периоде без первого месяца (кроме 1 квартала, для него нужен первый месяц)
        def months
        if (order > 1) {
            months = monthsInQuarterMap[order] - (order * 3 - 2)
        } else {
            months = monthsInQuarterMap[order]
        }
        sourcesPeriodMap[reportPeriod] = months

        // получить еще форму за первый месяц следующего периода
        def from = getReportPeriodEndDate() + 1
        def to = getNextMonthEndDate()
        def nextPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.PROPERTY, from, to)
        if (nextPeriods != null && nextPeriods.size() == 1) {
            def nextTaxPeriod = nextPeriods.get(0)
            def nextMonthNumber = monthsInQuarterMap[nextTaxPeriod?.order].get(0)
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
        def prevFormData = formDataService.getFormDataPrev(formData)
        prevDataRows = (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allSaved : null)
    }
    return prevDataRows
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = getDepartmentReportPeriodById(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

def getDepartmentReportPeriodById(def id) {
    if (id != null && departmentReportPeriodMap[id] == null) {
        departmentReportPeriodMap[id] = departmentReportPeriodService.get(id)
    }
    return departmentReportPeriodMap[id]
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
            def addFlag = true
            for (def alias : groupColumns) {
                if (getOwnerValue(prevRow, alias) != getOwnerValue(row, alias)) {
                    addFlag = false
                    break
                }
            }
            if (addFlag) {
                prevGroupRows.add(prevRow)
            }
        }
        // скопировать если текущая форма: 1кв - не надо копировать, полгода - 5..8 графа, 9 месяцев 5..11, год - 5..14
        def someColumns = nonEmptyColumnsMap[getReportPeriod().order - 1] - first5Columns
        def prevCategoryRow = getRowByName(prevGroupRows, 'title', row.title)
        if (prevCategoryRow) {
            someColumns.each { column ->
                def value = prevCategoryRow.getCell(column).value
                row.getCell(column).setValue(value, null)
            }
        } else {
            // если в предыдущей форме не было такой категории в группе, то занулять
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
            sortCategoryRows(categoryRows)
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

/**
 * Отсортировать категории внутри группы. Сортировать по кодам льгот.
 * Порядок сортировки: 2012000, 2012500, 2012400, (2012400 и 2012500).
 */
void sortCategoryRows(def rows) {
    rows.sort { def a, def b ->
        def codesA = getRowBenefitCodes(a)
        def codesB = getRowBenefitCodes(b)
        if (codesA.size() == codesB.size()) {
            if (codesA.size() > 1) {
                return -1
            } else if (codesA.size() == 1) {
                def sortedCodes = ['2012000' : 1, '2012500' : 2, '2012400' : 3]
                def indexA = sortedCodes[codesA[0]]
                def indexB = sortedCodes[codesB[0]]
                return indexA <=> indexB
            }
        }
        return codesA.size() <=> codesB.size()
    }
}

/** Получить список кодов льгот категории для сортировки. */
def getRowBenefitCodes(def row) {
    def hasCategory = !UNCATEGORIZED.equals(row.title)

    // получение данных их справочника 203 "Параметры налоговых льгот налога на имущество"
    def regionId = formDataDepartment.regionId
    def subjectId = getOwnerValue(row, 'subject')
    def paramDestination = (hasCategory ? 1 : 0)

    def filter = "DECLARATION_REGION_ID = $regionId " +
            "and REGION_ID = $subjectId " +
            "and PARAM_DESTINATION = $paramDestination"
    if (hasCategory) {
        filter = filter + " and LOWER(ASSETS_CATEGORY) = LOWER('${row.title}')"
    }
    def provider = formDataService.getRefBookProvider(refBookFactory, 203, providerCache)
    def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
    if (!records) {
        // если записей нет, то ошибка
        def subject = getRefBookValue(4, subjectId)?.CODE?.value
        def refBookName = getRefBookName(203L)
        def categoryMessage = hasCategory ? "с категорией «${row.title}» " : "без категории "
        throw new Exception("Для кода субъекта $subject не предусмотрена налоговая льгота " + categoryMessage +
                "(в справочнике «$refBookName» отсутствует необходимая запись)!")
    }

    // получение налоговых льгот - записи из справочника 202 "Коды налоговых льгот налога на имущество"
    def list = []
    records.each { record ->
        def record202 = getRefBookValue(202L, record.TAX_BENEFIT_ID.value)
        list.add(record202?.CODE?.value)
    }
    return list
}

/**
 * Обновить строки формы из источников.
 *
 * @param groupsMap мапа с текущими группами
 * @param tmpGroupsMap мапа с временными обновленными группами (из источников)
 */
def getNewGroupsMap(def groupsMap, def tmpGroupsMap) {
    // для новых групп
    def newGroupsMap = [:]

    // мапа для хранения списка строк по ключу: Код субъекта, Код ОКТМО (для проверки количества групп с заданным ключом)
    def checkGroupsMap = [:]

    // пройтись по текущим строкам и сравнить их с обновленными (временными), пропустить удаленные, найти группы для дальнейшей проверки
    groupsMap.keySet().each { key ->
        def tmpGroupRows = tmpGroupsMap[key]
        def row = groupsMap[key][0]
        if (tmpGroupRows) {
            // ключ (Код субъекта, Код ОКТМО)
            def subjectAndOktmoKey = getOwnerValue(row,'subject') + SEPARATOR + getOwnerValue(row,'oktmo')
            if (!checkGroupsMap[subjectAndOktmoKey]) {
                checkGroupsMap[subjectAndOktmoKey] = []
            }
            checkGroupsMap[subjectAndOktmoKey].add(tmpGroupRows)
        } else {
            // среди временных обновленных строк нет такой группы - удалить ее (пропустить)
            def subject = getRefBookValue(4L, getOwnerValue(row,'subject'))?.CODE?.value
            def taxAuthority = getOwnerValue(row,'taxAuthority')
            def kpp = getOwnerValue(row,'kpp')
            def oktmo = getRefBookValue(96L, getOwnerValue(row,'oktmo'))?.CODE?.value
            logger.info("Удалена группа строк по параметрам декларации «Код субъекта» = $subject, " +
                    "«Код НО» = $taxAuthority, «КПП» = $kpp, «Код ОКТМО» = $oktmo")
        }
    }

    // проверить наличие обновленных групп в существующих, если нет среди существующих, то добавить
    tmpGroupsMap.keySet().each { key ->
        def groupRows = groupsMap[key]
        if (!groupRows) {
            newGroupsMap[key] = tmpGroupsMap[key]
            // для вывода информационного сообещения №2
            infoMessagesRowMap[key] = tmpGroupsMap[key][0]
        }
    }

    // проверить найденые группы соответствующие текущим и обновленным группам
    checkGroupsMap.keySet().each { key ->
        def groups = checkGroupsMap[key]
        if (groups.size() == 1) {
            // группа не имеет схожих групп по "Код субъекта" и "Код ОКТМО", обновить группу (взять обновленную временную)
            // 1ая строка 1ой группы
            def row = groups[0][0]
            def groupKey = getGroupKey(row)
            newGroupsMap[groupKey] = groups[0]
        } else {
            // группа имеет схожие группы по "Код субъекта" и "Код ОКТМО"
            groups.each { groupRows ->
                def groupKey = getGroupKey(groupRows[0])
                def group = groupsMap[groupKey]
                def tmpGroup = tmpGroupsMap[groupKey]

                def newGroup = [group[0], group[1]]
                def categoryRows = group - group[0] - group[1]
                def tmpCategoryRows = tmpGroup - tmpGroup[0] - tmpGroup[1]

                // если текущая категория существует во временных, то не меняем ее, если такой категории уже не существует, то пропускаем
                for (def row : categoryRows) {
                    if (containCategory(tmpCategoryRows, row.title)) {
                        newGroup.add(row)
                    }
                }
                // если временной категории нет в текущих, то добавить ее
                for (def tmpRow : tmpCategoryRows) {
                    if (!containCategory(categoryRows, tmpRow.title)) {
                        newGroup.add(tmpRow)
                    }
                }
                newGroupsMap[groupKey] = newGroup
            }
        }
    }

    return newGroupsMap
}

/** Получить ключ группы (Код субъекта, Код НО, КПП, Код ОКТМО). */
def getGroupKey(def row) {
    def subject = getRefBookValue(4L, getOwnerValue(row, 'subject'))?.CODE?.value
    def oktmo = getRefBookValue(96L, getOwnerValue(row, 'oktmo'))?.CODE?.value
    return subject + SEPARATOR + getOwnerValue(row, 'taxAuthority') + SEPARATOR + getOwnerValue(row, 'kpp') + SEPARATOR + oktmo
}

/**
 * Проверить если ли среди строк заданная категория.
 *
 * @param rows строки
 * @param value название категории
 */
def containCategory(def rows, def value) {
    for (def row : rows) {
        if (row.title == value) {
            return true
        }
    }
    return false
}

/** Получить источники (важны только тип формы и подразделение). */
def getFormSources() {
    if (formSources945_1 == null) {
        // получить источники (важны только тип формы и подразделение)
        formSources945_1 = []
        def formSources = departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
                getReportPeriodStartDate(), getReportPeriodEndDate())
        if (formSources != null && !formSources.isEmpty()) {
            for (def source : formSources) {
                if (source.formTypeId == sourceFormTypeId) {
                    formSources945_1.add(source)
                }
            }
        }
    }
    return formSources945_1
}

/** Получить полное название подразделения по id подразделения. */
def getDepartmentFullName(def id) {
    if (departmentFullNameMap[id] == null) {
        departmentFullNameMap[id] = departmentService.getParentsHierarchy(id)
    }
    return departmentFullNameMap[id]
}

/** Получить подразделение по id. */
def getDepartmentById(def id) {
    if (id != null && departmentMap[id] == null) {
        departmentMap[id] = departmentService.get(id)
    }
    return departmentMap[id]
}

/** Получить тип фомры по id. */
def getFormTypeById(def id) {
    if (formTypeMap[id] == null) {
        formTypeMap[id] = formTypeService.get(id)
    }
    return formTypeMap[id]
}

/** Получить результат для события FormDataEvent.GET_SOURCES. */
def getSources() {
    def currentPeriod = reportPeriodService.get(formData.reportPeriodId)
    def start = getReportPeriodStartDate()
    def end = getReportPeriodEndDate()

    // источники
    def sourceDepartmentFormTypes = departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind, start, end)

    // приемники
    def destinationDepartmentFormTypes = departmentFormTypeService.getFormDestinations(formDataDepartment.id, formData.formType.id, formData.kind, start, end)

    // найти все ежемесячные источники 945.1 за текущий периоде и за первый месяц следующего периода
    // Мапа с периодами и номерами месяцев для источников 945.1 (период -> список номеров месяцев)
    def source945_1periodsMap = getSourcesPeriodMap()

    // номера месяцев для остальных ежемесячных источников-приемников
    def otherMonthlyFormsPeriods = monthsInQuarterMap[currentPeriod.order]
    def otherMonthlyFormsPeriodsMap = [currentPeriod : otherMonthlyFormsPeriods]

    // приемники
    destinationDepartmentFormTypes.each { departmentFormType ->
        def isMonthly = isMonthlyForm(departmentFormType.formTypeId, currentPeriod.id)
        def isSource = false
        if (isMonthly) {
            // другая ежемесячная форма
            otherMonthlyFormsPeriods.each { monthOrder ->
                FormData tmpFormData = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind,
                        departmentFormType.departmentId, currentPeriod.id, monthOrder, null, false)
                def relation = getRelation(tmpFormData, departmentFormType, isSource, currentPeriod, monthOrder)
                if (relation) {
                    sources.sourceList.add(relation)
                }
            }
        } else {
            // квартальная форма
            FormData tmpFormData = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind,
                    departmentFormType.departmentId, currentPeriod.id, null, null, false)
            def relation = getRelation(tmpFormData, departmentFormType, isSource, currentPeriod, null)
            if (relation) {
                sources.sourceList.add(relation)
            }
        }
    }

    // источники
    for (def departmentFormType : sourceDepartmentFormTypes) {
        def isSource = true
        def monthlyFormsPeriodsMap
        if (departmentFormType.formTypeId == sourceFormTypeId) {
            // 945.1
            monthlyFormsPeriodsMap = source945_1periodsMap
        } else if (isMonthlyForm(departmentFormType.formTypeId, currentPeriod.id)) {
            // другая ежемесячная форма
            monthlyFormsPeriodsMap = otherMonthlyFormsPeriodsMap
        } else {
            // квартальная форма
            FormData tmpFormData = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind,
                    departmentFormType.departmentId, currentPeriod.id, null, null, false)
            def relation = getRelation(tmpFormData, departmentFormType, isSource, currentPeriod, null)
            if (relation) {
                sources.sourceList.add(relation)
            }
            continue
        }
        // ежемесячные
        monthlyFormsPeriodsMap.each { period, monthOrders ->
            monthOrders.each { monthOrder ->
                FormData tmpFormData = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind,
                        departmentFormType.departmentId, period.id, monthOrder, null, false)
                def relation = getRelation(tmpFormData, departmentFormType, isSource, period, monthOrder)
                if (relation) {
                    sources.sourceList.add(relation)
                }
            }
        }
    }

    sources.sourcesProcessedByScript = true
    return sources.sourceList
}

/**
 * Получить запись для источника-приемника.
 *
 * @param tmpFormData нф
 * @param departmentFormType информация об источнике приемнике
 * @param isSource признак источника
 * @param period период нф
 * @param monthOrder номер месяца (для ежемесячной формы)
 */
def getRelation(FormData tmpFormData, DepartmentFormType departmentFormType, boolean isSource, ReportPeriod period, Integer monthOrder) {
    // boolean excludeIfNotExist - исключить несозданные источники
    if (excludeIfNotExist && tmpFormData == null) {
        return null
    }
    // WorkflowState stateRestriction - ограничение по состоянию для созданных экземпляров
    if (stateRestriction && tmpFormData != null && stateRestriction != tmpFormData.state) {
        return null
    }
    Relation relation = new Relation()

    DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(tmpFormData?.departmentReportPeriodId) as DepartmentReportPeriod
    DepartmentReportPeriod comparativePeriod = getDepartmentReportPeriodById(tmpFormData?.comparativePeriodId) as DepartmentReportPeriod
    FormType formType = getFormTypeById(departmentFormType.formTypeId) as FormType
    Department performer = getDepartmentById(departmentFormType.performerId) as Department

    // boolean light - заполняются только текстовые данные для GUI и сообщений
    if (light) {
        /**************  Параметры для легкой версии ***************/
        /** Идентификатор подразделения */
        relation.departmentId = departmentFormType.departmentId
        /** полное название подразделения */
        relation.fullDepartmentName = getDepartmentFullName(departmentFormType.departmentId)
        /** Дата корректировки */
        relation.correctionDate = departmentReportPeriod?.correctionDate
        /** Вид нф */
        relation.formTypeName = formType?.name
        /** Год налогового периода */
        relation.year = period.taxPeriod.year
        /** Название периода */
        relation.periodName = period.name
        /** Название периода сравнения */
        relation.comparativePeriodName = comparativePeriod?.reportPeriod?.name
        /** Дата начала периода сравнения */
        relation.comparativePeriodStartDate = comparativePeriod?.reportPeriod?.startDate
        /** Год периода сравнения */
        relation.comparativePeriodYear = comparativePeriod?.reportPeriod?.taxPeriod?.year
        /** название подразделения-исполнителя */
        relation.performerName = performer?.name
    }
    /**************  Общие параметры ***************/
    /** подразделение */
    relation.department = getDepartmentById(departmentFormType.departmentId) as Department
    /** Период */
    relation.departmentReportPeriod = departmentReportPeriod
    /** Статус ЖЦ */
    relation.state = tmpFormData?.state
    /** форма/декларация создана/не создана */
    relation.created = (tmpFormData != null)
    /** является ли форма источников, в противном случае приемник*/
    relation.source = isSource
    /** Введена/выведена в/из действие(-ия) */
    if (!light) {
        relation.status = getFormTemplateById(departmentFormType.formTypeId, period.id)
    }

    /**************  Параметры НФ ***************/
    /** Идентификатор созданной формы */
    relation.formDataId = tmpFormData?.id
    /** Вид НФ */
    relation.formType = formType
    /** Тип НФ */
    relation.formDataKind = departmentFormType.kind
    /** подразделение-исполнитель*/
    relation.performer = performer
    /** Период сравнения. Может быть null */
    relation.comparativePeriod = comparativePeriod
    /** Номер месяца */
    relation.month = monthOrder
    if (tmpFormData) {
        /** Признак расчета значений нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения) */
        relation.accruing = tmpFormData.accruing
        /** Признак ручного ввода */
        relation.manual = tmpFormData.manual
    }
    return relation
}

def isMonthlyForm(def formTypeId, def periodId) {
    def key = formTypeId?.toString() + "#" + periodId?.toString()
    if (monthlyMap[key] == null) {
        monthlyMap[key] = getFormTemplateById(formTypeId, periodId)?.monthly
    }
    return monthlyMap[key]
}

def getFormTemplateById(def formTypeId, def periodId) {
    if (formTypeId == null || periodId == null) {
        return null
    }
    def key = formTypeId + '#' + periodId
    if (formTemplateMap[key] == null) {
        formTemplateMap[key] = formDataService.getFormTemplate(formTypeId, periodId)
    }
    return formTemplateMap[key]
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 19
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'subject')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        rowIndex++
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }
    updateIndexes(rows)

    // объединение групп
    def firstRowInGroup = null
    def rowInGroup = 0
    rows.each { row ->
        if (firstRowInGroup == null || !isEquals(row, firstRowInGroup) || row.getIndex() == rows.size()) {
            if (firstRowInGroup) {
                if (row.getIndex() == rows.size()) {
                    rowInGroup++
                }
                groupColumns.each { alias ->
                    firstRowInGroup.getCell(alias).rowSpan = rowInGroup
                }
            }
            firstRowInGroup = row
            rowInGroup = 0
        }
        rowInGroup++
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    // для проверки шапки
    def headerMapping = [
            (headerRows[0][0])  : getColumnName(tmpRow, 'subject'),
            (headerRows[0][1])  : getColumnName(tmpRow, 'taxAuthority'),
            (headerRows[0][2])  : getColumnName(tmpRow, 'kpp'),
            (headerRows[0][3])  : getColumnName(tmpRow, 'oktmo'),
            (headerRows[0][4])  : '',
            (headerRows[1][4])  : '',
            (headerRows[2][4])  : '',
            (headerRows[0][5])  : 'Остаточная стоимость основных средств',
            (headerRows[1][5])  : getColumnName(tmpRow, 'cost1'),
            (headerRows[0][6])  : getColumnName(tmpRow, 'cost2'),
            (headerRows[0][7])  : getColumnName(tmpRow, 'cost3'),
            (headerRows[0][8])  : getColumnName(tmpRow, 'cost4'),
            (headerRows[0][9])  : getColumnName(tmpRow, 'cost5'),
            (headerRows[0][10]) : getColumnName(tmpRow, 'cost6'),
            (headerRows[0][11]) : getColumnName(tmpRow, 'cost7'),
            (headerRows[0][12]) : getColumnName(tmpRow, 'cost8'),
            (headerRows[0][13]) : getColumnName(tmpRow, 'cost9'),
            (headerRows[0][14]) : getColumnName(tmpRow, 'cost10'),
            (headerRows[0][15]) : getColumnName(tmpRow, 'cost11'),
            (headerRows[0][16]) : getColumnName(tmpRow, 'cost12'),
            (headerRows[0][17]) : getColumnName(tmpRow, 'cost13'),
            (headerRows[0][18]) : getColumnName(tmpRow, 'cost31_12'),
            (headerRows[2][0]) : '1',
            (headerRows[2][1]) : '2',
            (headerRows[2][2]) : '3',
            (headerRows[2][3]) : '4'
    ]
    (5..18).each { index ->
        headerMapping.put((headerRows[2][index]), index.toString())
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def isEditable = (TOTAL_ROW_TITLE != values[4])
    def newRow = getNewRow(values[4], isEditable)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 1 - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
    def colIndex = 0
    newRow.subject = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 2
    colIndex++
    newRow.taxAuthority = values[colIndex]

    // графа 3
    colIndex++
    newRow.kpp = values[colIndex]

    // графа 4 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
    colIndex++
    newRow.oktmo = getRecordIdImport(96, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа - title
    colIndex++
    newRow.title = values[colIndex]

    // графа 5..18
    ['cost1', 'cost2', 'cost3', 'cost4', 'cost5', 'cost6', 'cost7', 'cost8', 'cost9', 'cost10',
            'cost11', 'cost12', 'cost13', 'cost31_12'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

def isEquals(def rowA, def rowB) {
    if (rowA == null || rowB == null) {
        return false
    }
    for (def alias : groupColumns) {
        if (rowA[alias] != rowB[alias]) {
            return false
        }
    }
    return true
}