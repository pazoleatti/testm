package form_template.deal.related_persons.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 800 - Взаимозависимые лица.
 *
 * formTemplateId=800
 *
 * TODO:
 *      - проверить расчеты для 1 квартала 4.2
 *      - дописать тесты
 */

// графа 1  - rowNumber
// графа 2  - name			- атрибут 5201 - NAME - «Полное наименование юридического лица с указанием ОПФ», справочник 520 «Юридические лица»
// графа 3  - address		- зависит от графы 2 - атрибут 5202 - ADDRESS - «Место нахождения (юридический адрес) юридического лица (из устава)», справочник 520 «Юридические лица»
// графа 4  - orgCode		- зависит от графы 2 - атрибут 5203 - ORG_CODE - «Код организации», справочник 520 «Юридические лица»
// графа 5  - countryCode	- зависит от графы 2 - атрибут 5204 - COUNTRY_CODE - «Код страны по ОКСМ», справочник 520 «Юридические лица»
// графа 6  - inn			- зависит от графы 2 - атрибут 5205 - INN - «ИНН (заполняется для резидентов, некредитных организаций)», справочник 520 «Юридические лица»
// графа 7  - kpp			- зависит от графы 2 - атрибут 5206 - KPP - «КПП (заполняется для резидентов, некредитных организаций)», справочник 520 «Юридические лица»
// графа 8  - swift			- зависит от графы 2 - атрибут 5208 - SWIFT - «Код SWIFT (заполняется для кредитных организаций, резидентов и нерезидентов)», справочник 520 «Юридические лица»
// графа 9  - regNum		- зависит от графы 2 - атрибут 5209 - REG_NUM - «Регистрационный номер в стране инкорпорации (заполняется для нерезидентов)», справочник 520 «Юридические лица»
// графа 10 - startData		- зависит от графы 2 - атрибут 5210 - START_DATE - «Дата наступления основания для включения в список», справочник 520 «Юридические лица»
// графа 11 - endData		- зависит от графы 2 - атрибут 5211 - END_DATE - «Дата наступления основания для исключения из списка», справочник 520 «Юридические лица»
// графа 12 - category		- атрибут 5061 - CODE - «Код категории», справочник 506 «Категории юридического лица по системе «светофор»»
// графа 13 - vatStatus		- зависит от графы 2 - атрибут 5212 - VAT_STATUS - «Статус по НДС», справочник 520 «Юридические лица»
// графа 14 - taxStatus		- зависит от графы 2 - атрибут 5213 - TAX_STATUS - «Специальный налоговый статус», справочник 520 «Юридические лица»
// графа 15 - depCriterion	- зависит от графы 2 - атрибут 5214 - DEP_CRITERION - «Критерий взаимозависимости», справочник 520 «Юридические лица»

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        refresh()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.REFRESH:
        refresh()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkSourceForm()
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
    case FormDataEvent.SAVE:
        updateStylesAndSort()
        formDataService.saveCachedDataRows(formData, logger)
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
def allColumns = ['rowNumber', 'name', 'address', 'orgCode', 'countryCode', 'inn', 'kpp', 'swift', 'regNum', 'startData', 'endData', 'category', 'vatStatus', 'taxStatus', 'depCriterion']

// Редактируемые атрибуты
@Field
def editableColumns = ['category']

// Проверяемые на пустые значения атрибуты (графа 2..5, 10, 12, 13, 15)
@Field
def nonEmptyColumns = ['name', /*'address', 'orgCode', 'countryCode', 'startData',*/ 'category', /*'vatStatus', 'depCriterion'*/]

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def prevReportPeriod = null

@Field
ReportPeriod reportPeriod = null

@Field
def sourceDataRowsMap = [:]

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

// Обновление формы
//  - берутся записи из справочника "Участники ТЦО", которые подходят для этой формы
//  - если для какой-нибудь записи нет строки в форме, то добавляется новая строка, и если такая же строка есть в предыдущей форме то берется ее "цвет/категория", иначе цвет по умолчанию
//  - если для какой-то строки формы нет уже записи в справочнике, то удалить эту строку из формы
void refresh() {
    if (formData.state != WorkflowState.CREATED) {
        return
    }

    // 1. Проверка наличия формы предыдущего отчетного периода
    def prevDataRows = getSourceDataRows(800, FormDataKind.PRIMARY, true)
    if (prevDataRows == null) {
        def prevReportPeriod = getPrevReportPeriod()
        def prevPeriodName = (prevReportPeriod ? prevReportPeriod.name + ' ' +  prevReportPeriod.taxPeriod.year : getPrevPeriodName())
        def msg = "Категории ВЗЛ из предыдущего отчетного периода не были скопированы. " +
                "В Системе не найдена форма «%s» в статусе «Принята»: Тип: %s, Период: %s, Подразделение: %s."
        logger.warn(msg, formData.formType.name, formData.kind.title, prevPeriodName, formDataDepartment.name)
    }

    // мапа для хранения всех версии записи (строка нф - список всех версии записи "участников ТЦО")
    def record520Map = getVersionRecords520Map(prevDataRows)

    // получить значения из справочника "Юридические лица"
    def relatedPersonRecords = getRecords520()

    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // сформировать новые строки
    def newRows = []
    for (def record : relatedPersonRecords) {
        def recordId = record?.record_id?.value
        def findRow = dataRows.find { it.name == recordId }
        // поиск строки для записи справочника на форме
        if (findRow && findRow.category) {
            continue
        }
        // поиск строки для записи справочника в предыдущей форме (с соответствующей ссылкой в графе 12)
        def prevFindRow = (prevDataRows ? findPrevRow(recordId, record520Map) : null)
        if (findRow) {
            // обновление существующей
            findRow.category = prevFindRow?.category
        } else {
            // новая строка
            def newRow = formData.createDataRow()
            newRow.name = recordId
            newRow.category = prevFindRow?.category
            newRows.add(newRow)
        }
    }

    // найти лишние строки
    def deleteRows = []
    for (def row : dataRows) {
        def findRecord = relatedPersonRecords.find { it?.record_id?.value == row.name }
        if (!findRecord) {
            deleteRows.add(row)
        }
    }

    // удалить лишние
    dataRows.removeAll(deleteRows)
    // добавить новые
    dataRows.addAll(newRows)

    // задать категорию и цвет
    for (def row : dataRows) {
        // графа 12
        row.category = calc12(row)
    }
    updateStylesAndSort()
}

void calc() {
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // графа 12
        row.category = calc12(row, true)
    }
    updateStylesAndSort()
}

// обновить цвета и сортировку
void updateStylesAndSort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    // обновление стилей
    for (def row : dataRows) {
        setRowStyles(row)
    }

    // обновление сортировки
    // распределить строки по категориям
    def categoryMap = [:]
    dataRows.each { row ->
        def categoryName = getRefBookValue(506L, row.category)?.CODE?.value
        if (categoryMap[categoryName] == null) {
            categoryMap[categoryName] = []
        }
        categoryMap[categoryName].add(row)
    }
    def sortedRows = []
    // сортировка категории и строк внутри каждой категории
    categoryMap.keySet().toArray().sort().each { categoryName ->
        def categoryRows = categoryMap[categoryName]
        if (categoryRows) {
            sortRows(refBookService, logger, categoryRows, null, null, false)
            sortedRows.addAll(categoryRows)
        }
    }
    if (sortedRows) {
        dataRowHelper.allCached = sortedRows
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // def nonEmptyColumnsMap = [
    //         'address' : 'ADDRESS',
    //         'orgCode' : 'ORG_CODE',
    //         'countryCode' : 'COUNTRY_CODE',
    //         'startData' : 'START_DATE',
    //         'vatStatus' : 'VAT_STATUS',
    //         'depCriterion' : 'DEP_CRITERION'
    // ]
    for (def row : dataRows) {
        def record = getRefBookValue(520L, row.name)

        // 1. Проверка заполнения обязательных полей (графа 2, 12)
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
       // // проверка ссылочных графов обязательных для заполнения (графа 3, 4, 5, 10, 13, 15)
       // nonEmptyColumnsMap.each { alias, refAlias ->
       //     def value = record?.get(refAlias)?.value
       //     if (value == null || '' == value) {
       //         String msg = String.format(WRONG_NON_EMPTY, row.getIndex(), getColumnName(row, alias));
       //         rowError(logger, row, msg);
       //     }
       // }

        // 2. Проверка на отсутствие в списке не ВЗЛ
        if (record && record?.START_DATE?.value) {
            def start = record?.START_DATE?.value
            def end = record?.END_DATE?.value
            def typeId = record?.TYPE?.value
            if (!isVZL(start, end, typeId)) {
                def index = row.getIndex()
                def value2 = record?.NAME?.value
                def msg = "Строка $index: организация «$value2» не является взаимозависимым лицом в данном отчетном периоде!"
                rowError(logger, row, msg)
            }
        }
    }
}

def calc12(def row, def isCalc = false) {
    if (!isCalc && row.category) {
        return row.category
    }
    def record520 = getRefBookValue(520L, row.name)
    def taxStatus = getRefBookValue(511L, record520?.TAX_STATUS?.value)?.CODE?.value
    def orgCode = getRefBookValue(513L, record520?.ORG_CODE?.value)?.CODE?.value
    def categoryCode = null
    if (taxStatus == 1 && orgCode == 1) {
        // ВЗЛ СРН - «Категория 1»
        categoryCode = 'Категория 1'
    } else if (taxStatus == 2 && orgCode == 1) {
        // ВЗЛ ОРН - «Категория 4» (по умполчанию)
        if (isCalc) {
            def list = subCalc12(row)
            if (list != null && list[1]) {
                return list[1]
            }
            categoryCode = (list ? list[0] : null)
        } else {
            categoryCode = 'Категория 4'
        }
    } else if (orgCode == 2) {
        // ИВЗЛ - «Категория 1»
        categoryCode = 'Категория 1'
    }
    if (categoryCode) {
        return getRecordId506ByCode(categoryCode)
    }
    return null
}

/**
 * Должен выполнятся только при расчете, а не при выставлении значения по умолчанию при обновлении/создании формы.
 *
 * @param row строка нф
 * @return возвращает список: первый элемент - код категории, второй элемент - id записи (заполняется только один из элементов)
 */
def subCalc12(def row) {
    // форма "Прогноз крупных сделок" - одинаково для всех периодов
    def sourceRows = getSourceDataRows(810, FormDataKind.PRIMARY)
    def findRow = sourceRows?.find { it.ikksr == row.name }
    if (findRow) {
        return ['Категория 2', null]
    }

    ReportPeriod reportPeriod = getReportPeriod()
    if (reportPeriod.order == 1) {
        // 1 квартал
        // форма "Приложение 4.2" за предыдущий налоговый период
        sourceRows = getSourceDataRows(803, FormDataKind.SUMMARY, true)
        // отобрать записи в которых графа 4 == 'ВЗЛ ОРН'
        findRow = sourceRows?.find { 'ВЗЛ ОРН' == getRefBookValue(505, it.group) }
        if (findRow) {
            // для подходящих строк получить все версии записей
            def records520Map = getVersionRecords520Map([findRow])
            findRow = findPrevRow(row.name, records520Map)
            if (findRow) {
                return [null, findRow.categoryRevised]
            }
        }
    } else if (reportPeriod.order == 4) {
        // полугодие / 9 месяцев / год
        // форма "ВЗЛ" за предыдущий налоговый период
        sourceRows = getSourceDataRows(800, FormDataKind.PRIMARY, true)
        // для подходящих строк получить все версии записей
        def records520Map = getVersionRecords520Map(sourceRows)
        def findPrevRow = findPrevRow(row.name, records520Map)
        // если категория в предыдущем периода равна "Категория 2", то оставить ее, в других формах не ищется ничего
        if (findPrevRow && 'Категория 2' == getRefBookValue(506L, findPrevRow.category)?.CODE?.value) {
            return [null, findPrevRow.category]
        }

        // форма "Приложение 4.1. (6 месяцев)" / "Приложение 4.1. (9 месяцев)"
        def formTypeId = (reportPeriod.order == 2 ? 801 : 802)
        sourceRows = getSourceDataRows(formTypeId, FormDataKind.SUMMARY)
        findRow = sourceRows?.find { it.name == row.name }
        if (findRow) {
            // для полугодия и 9 месяцев использовать графа 20, для периода год - графу 21
            def resultId = (reportPeriod.order == 4 ? findRow.categoryPrimary : findRow.categoryRevised)
            return [null, resultId]
        } else if (findPrevRow) {
            return [null, findPrevRow.category]
        }
    }
    // значение по умолчанию для всех периодов
    return ['Категория 4', null]
}

// Получить новую строку
def getNewRow() {
    def row = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

// выставить цвет по категории
void setRowStyles(def row) {
    allColumns.each {
        def colorId = getRefBookValue(506L, row.category)?.COLOR?.value
        def styleName = getRefBookValue(1L, colorId)?.NAME?.value
        row.getCell(it).setStyleAlias(styleName)
    }
    row.getCell('category').editable = true
}

void checkSourceForm() {
    // 1. Проверка наличия принятой формы-источника
    def sourceMap = [
            800 : [ 'kind' : FormDataKind.PRIMARY, 'isPrevPeriod' : true ],  // форма "ВЗЛ" за предыдущий налоговый период
            801 : [ 'kind' : FormDataKind.SUMMARY, 'isPrevPeriod' : false ], // форма "Приложение 4.1. (6 месяцев)"
            802 : [ 'kind' : FormDataKind.SUMMARY, 'isPrevPeriod' : false ], // форма "Приложение 4.1. (9 месяцев)"
            803 : [ 'kind' : FormDataKind.SUMMARY, 'isPrevPeriod' : true ],  // форма "Приложение 4.2" за предыдущий налоговый период
            810 : [ 'kind' : FormDataKind.PRIMARY, 'isPrevPeriod' : false ]  // форма "Прогноз крупных сделок"
    ]
    ReportPeriod reportPeriod = getReportPeriod()
    def sourceIds
    if (reportPeriod.order == 1) {
        sourceIds = [810, 800, 803]
    } else if (reportPeriod.order == 2) {
        sourceIds = [810, 800, 801]
    } else {
        sourceIds = [810, 800, 802]
    }
    sourceIds.each { id ->
        def rows = getSourceDataRows(id, sourceMap[id].kind, sourceMap[id].isPrevPeriod)
        if (rows == null) {
            def formTypeName = getFormTypeById(id)?.name
            def kindName = sourceMap[id].kind.title
            def period = (sourceMap[id].isPrevPeriod ? getPrevReportPeriod() : getReportPeriod())
            def periodName = (period ? period?.name + ' ' + period?.taxPeriod?.year : getPrevPeriodName())
            msg = "Не найдена форма «%s» в статусе «Принята»: Тип: %s, Период: %s, Подразделение: %s."
            logger.error(msg, formTypeName, kindName, periodName, formDataDepartment.name)
        }
    }
}

def getPrevPeriodName() {
    def period = getReportPeriod()
    def periodNameMap = [
            0 : 'год',
            1 : 'первый квартал',
            2 : 'полугодие',
            3 : 'девять месяцев'
    ]
    def name = periodNameMap[period.order - 1]
    def year = period?.taxPeriod?.year - (period.order == 1 ? 1 : 0)
    return "$name $year"
}

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

// Получить значения из справочника "Юридические лица" / "Участники ТЦО".
def getRecords520() {
    def provider = formDataService.getRefBookProvider(refBookFactory, 520L, providerCache)
    // TODO (Ramil Timerbaev) возможно надо сделать фильтр
    def filter = null
    def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
    def relatedPersons = []
    records.each { record ->
        def start = record?.START_DATE?.value
        def end = record?.END_DATE?.value
        def typeId = record?.TYPE?.value
        if (isVZL(start, end, typeId)) {
            relatedPersons.add(record)
        }
    }
    return relatedPersons
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

@Field
def record506Map = [:]

// Получить id записи справочника "Категории юридического лица по системе "светофор"" по коду
def getRecordId506ByCode(def categoryCode) {
    if (record506Map[categoryCode] == null) {
        def provider = formDataService.getRefBookProvider(refBookFactory, 506L, providerCache)
        def filter = "CODE = '$categoryCode'"
        def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        if (records != null && !records.isEmpty() && records.size() == 1) {
            record506Map[categoryCode] = records.get(0)?.record_id?.value
        }
    }
    return record506Map[categoryCode]
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

// Мапа для хранения типов форм (id типа формы -> тип формы)
@Field
def formTypeMap = [:]

/** Получить тип фомры по id. */
def getFormTypeById(def id) {
    if (formTypeMap[id] == null) {
        formTypeMap[id] = formTypeService.get(id)
    }
    return formTypeMap[id]
}