package form_template.deal.related_persons.v2015

import au.com.bytecode.opencsv.CSVWriter
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataXlsmReportBuilder
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.XlsxReportMetadata
import groovy.transform.Field
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle

import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.AreaReference
import org.apache.poi.ss.util.CellRangeAddress
import org.springframework.util.ClassUtils

/**
 * 800 - Взаимозависимые лица.
 *
 * formTemplateId=800
 *
 * TODO:
 *      - проверить расчеты для 1 квартала 4.2
 *      - дописать тесты
 */

// графа 1  (1)   - rowNumber
// графа 2  (2)   - name			- атрибут 5201 - NAME - «Полное наименование юридического лица с указанием ОПФ», справочник 520 «Юридические лица»
// графа 3  (3)   - address		    - зависит от графы 2 - атрибут 5202 - ADDRESS - «Место нахождения (юридический адрес) юридического лица (из устава)», справочник 520 «Юридические лица»
// графа 4  (4)   - orgCode		    - зависит от графы 2 - атрибут 5203 - ORG_CODE - «Код организации», справочник 520 «Юридические лица»
// графа 5  (5)   - countryCode	    - зависит от графы 2 - атрибут 5204 - COUNTRY_CODE - «Код страны по ОКСМ», справочник 520 «Юридические лица»
// графа 6  (6,1) - inn			    - зависит от графы 2 - атрибут 5205 - INN - «ИНН (заполняется для резидентов, некредитных организаций)», справочник 520 «Юридические лица»
// графа 7  (6,2) - kpp			    - зависит от графы 2 - атрибут 5206 - KPP - «КПП (заполняется для резидентов, некредитных организаций)», справочник 520 «Юридические лица»
// графа 8  (6,3) - swift			- зависит от графы 2 - атрибут 5208 - SWIFT - «Код SWIFT (заполняется для кредитных организаций, резидентов и нерезидентов)», справочник 520 «Юридические лица»
// графа 9  (6,4) - regNum		    - зависит от графы 2 - атрибут 5209 - REG_NUM - «Регистрационный номер в стране инкорпорации (заполняется для нерезидентов)», справочник 520 «Юридические лица»
// графа 10 (7)   - startData		- зависит от графы 2 - атрибут 5210 - START_DATE - «Дата наступления основания для включения в список», справочник 520 «Юридические лица»
// графа 11 (8)   - endData		    - зависит от графы 2 - атрибут 5211 - END_DATE - «Дата наступления основания для исключения из списка», справочник 520 «Юридические лица»
// графа 12 (9)   - category		- атрибут 5061 - CODE - «Код категории», справочник 506 «Категории юридического лица по системе «светофор»»
// графа 13 (10)  - vatStatus		- зависит от графы 2 - атрибут 5212 - VAT_STATUS - «Статус по НДС», справочник 520 «Юридические лица»
// графа 14 (11)  - taxStatus		- зависит от графы 2 - атрибут 5213 - TAX_STATUS - «Специальный налоговый статус», справочник 520 «Юридические лица»
// графа 15 (12)  - depCriterion	- зависит от графы 2 - атрибут 5214 - DEP_CRITERION - «Критерий взаимозависимости», справочник 520 «Юридические лица»

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.DELETE:
        deleteHistory(formDataService.getDataRowHelper(formData).allCached)
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
        updateHistoryOnCalcOrSave()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        updateHistoryOnAccept()
        break
    case FormDataEvent.SAVE:
        updateStylesAndSort()
        updateHistoryOnCalcOrSave()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.GET_SPECIFIC_REPORT_TYPES:
        specificReportType.add("Краткий список ВЗЛ (CSV)")
        specificReportType.add("Краткий список ВЗЛ (XLSM)")
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

    // добавить записи в историю (0) для строк newRows
    insertHistory(newRows, 0)
    // удалить записи из истории для строк deleteRows
    deleteHistory(deleteRows)
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

        // 3. Корректное указание категории для ИВЗЛ
        def orgCode = getRefBookValue(513L, record?.ORG_CODE?.value)?.CODE?.value
        def categoryName = getRefBookValue(506L, row.category)?.CODE?.value
        if (row.category && orgCode == 2 && categoryName != 'Категория 1') {
            String msg = String.format("Строка %d: Для иностранного ВЗЛ в графе «%s» можно указать только значение «Категория 1»!", row.getIndex(), getColumnName(row, 'category'))
            rowError(logger, row, msg)
        }

        // 4. Корректное указание категории для ВЗЛ СРН
        def taxStatus = getRefBookValue(511L, record?.TAX_STATUS?.value)?.CODE?.value
        if (row.category && orgCode == 1 && taxStatus == 1 && categoryName != 'Категория 1') {
            String msg = String.format("Строка %d: Для ВЗЛ со специальным режимом налогообложения в графе «%s» можно указать только значение «Категория 1»!", row.getIndex(), getColumnName(row, 'category'))
            rowError(logger, row, msg)
        }

        // 5. Корректное указание категории для ВЗЛ ОРН
        if (row.category && orgCode == 1 && taxStatus == 2 && !(categoryName in ['Категория 2', 'Категория 3', 'Категория 4'])) {
            String msg = String.format("Строка %d: Для ВЗЛ с общим режимом налогообложения в графе «%s» можно указать только одно из следующих значений: Категория 2, Категория 3, Категория 4!", row.getIndex(), getColumnName(row, 'category'))
            rowError(logger, row, msg)
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
    } else {
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
        def isPrevPeriod = (reportPeriod.order == 4)
        sourceRows = getSourceDataRows(formTypeId, FormDataKind.SUMMARY, isPrevPeriod)
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
        // TODO (Ramil Timerbaev) возможно надо переделать задание источников по периодам для наглядности
        def isPrevPeriod = sourceMap[id].isPrevPeriod
        if (reportPeriod.order == 4 && id == 802) {
            isPrevPeriod = true
        }
        def rows = getSourceDataRows(id, sourceMap[id].kind, isPrevPeriod)
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

void createSpecificReport() {
    switch (scriptSpecificReportHolder.getSpecificReportType()) {
        case 'Краткий список ВЗЛ (CSV)' :
            createSpecificReportShortListCSV()
            break
        case 'Краткий список ВЗЛ (XLSM)' :
            createSpecificReportShortListXLSM()
            break
    }
}

// Краткий список ВЗЛ (CSV)
def createSpecificReportShortListCSV() {
    // записать в файл
    PrintWriter printWriter = new PrintWriter(scriptSpecificReportHolder.getFileOutputStream())
    BufferedWriter bufferedWriter = new BufferedWriter(printWriter)
    CSVWriter csvWriter = new CSVWriter(bufferedWriter, (char) ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER)

    // заголовок
    def header = ['entityFullName', 'oksm', 'inn', 'kpp', 'swift', 'regno']
    csvWriter.writeNext(header.toArray() as String[])

    // данные
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    for (def row : dataRows) {
        def values = getShortRow(row.name)
        csvWriter.writeNext(values.toArray() as String[])
    }
    csvWriter.close()

    // название файла
    def periodCode = getPeriodCode(getReportPeriodEndDate())
    def year = getReportPeriod()?.taxPeriod?.year?.toString()
    def fileName = "interDep" + periodCode + year + ".csv"
    scriptSpecificReportHolder.setFileName(fileName)
}

/** Получить значения строки для краткого отчета. */
def getShortRow(def recordId) {
    def values = []

    // 1. entityFullName / name
    def record520 = getRefBookValue(520L, recordId)
    values.add(record520?.NAME?.value)

    // 2. oksm / countryCode
    def countryCode = getRefBookValue(10L, record520?.COUNTRY_CODE?.value)?.CODE?.value
    values.add(countryCode)

    // 3. inn / inn
    values.add(record520?.INN?.value)

    // 4. kpp / kpp
    values.add(record520?.KPP?.value)

    // 5. swift / swift
    values.add(record520?.SWIFT?.value)

    // 6. regno / regNum
    def value = null
    if (record520?.REG_NUM?.value) {
        value = record520?.REG_NUM?.value
    } else if (!record520?.REG_NUM?.value && !record520?.SWIFT?.value) {
        value = record520?.KIO?.value
    }
    values.add(value)

    return values
}

/** Получить код периода по дате. */
def getPeriodCode(def date) {
    def provider8 = refBookFactory.getDataProvider(8L)
    def record8s = provider8.getRecords(date, null, "D = 1", null)
    def dayAndMonth = date.format('dd.MM')
    def tmpDate = Date.parse('dd.MM.yyyy', dayAndMonth + '.1970')
    def record8 = record8s.find { it?.CALENDAR_START_DATE?.value <= tmpDate && tmpDate <= it?.END_DATE?.value }
    def periodCode = record8?.CODE?.value

    // если не нашлось кода в справочнике
    if (!periodCode) {
        Calendar c = Calendar.getInstance()
        c.setTime(date)
        def month = c.get(Calendar.MONTH)
        if (month < 4) {
            periodCode = '21'
        } else if (month < 7) {
            periodCode = '31'
        } else if (month < 10) {
            periodCode = '33'
        } else {
            periodCode = '34'
        }
    }
    return periodCode
}

@Field
Workbook workBook = null
@Field
Sheet sheet = null

void createSpecificReportShortListXLSM() {
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
    cell.setCellValue("ПАО Сбербанк")
    cell.setCellStyle(getCellStyle(StyleType.ROW_1))
    // объединение двух ячеек
    region = new CellRangeAddress(rowIndex, rowIndex, 0, 1)
    sheet.addMergedRegion(region)

    def currentDateStr = new Date().format('dd.MM.yyyy')
    rowIndex++
    newRow = sheet.createRow(rowIndex)
    cell = newRow.createCell(0)
    cell.setCellValue("Список Взаимозависимых лиц Банка по состоянию на $currentDateStr")
    cell.setCellStyle(getCellStyle(StyleType.ROW_2))
    // объединение трех ячеек
    region = new CellRangeAddress(rowIndex, rowIndex, 0, 2)
    sheet.addMergedRegion(region)

    // пустая строка
    rowIndex++
    sheet.createRow(rowIndex)

    // шапка
    rowIndex++
    def tmpDataRow = formData.createDataRow()
    def columnAliases = ['name', 'countryCode', 'inn', 'kpp', 'swift', 'regNum']
    def columnNames = columnAliases.collect { getColumnName(tmpDataRow, it) }
    addNewRowInXlsm(rowIndex, columnNames, StyleType.HEADER)

    // нумерация
    rowIndex++
    def columnNum = (1..6).collect { it.toString() }
    addNewRowInXlsm(rowIndex, columnNum, StyleType.NUMERATION)

    // задать ширину столбцов (в символах)
    def widths = [
            43, // name
            9,  // countryCode
            20, // inn
            15, // kpp
            15, // swift
            20, // regNum
    ]
    for (int i = 0; i < widths.size(); i++) {
        int width = widths[i] * 256 // умножить на 256, т.к. 1 единица ширины в poi = 1/256 ширины символа
        sheet.setColumnWidth(i, width)
    }

    // данные
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    for (def row : dataRows) {
        rowIndex++
        def values = getShortRow(row.name)
        // добавить значения
        addNewRowInXlsm(rowIndex, values, StyleType.DATA)
    }

    // область печати
    setPrintSetup(rowIndex, widths.size())

    workBook.write(scriptSpecificReportHolder.getFileOutputStream())

    // название файла
    scriptSpecificReportHolder.setFileName("Краткий список ВЗЛ.xlsm")
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
    ROW_1,      // строка 1
    ROW_2,      // строка 2
    HEADER,     // шапка
    NUMERATION, // нумерация
    DATA        // данные
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

            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_BOLD)
            font.setFontHeightInPoints(8 as short)
            font.setFontName('Arial')
            style.setFont(font)
            break
        case StyleType.ROW_2 :
            style.setWrapText(true)

            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_BOLD)
            font.setFontHeightInPoints(8 as short)
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
            font.setFontHeightInPoints(8 as short)
            font.setFontName('Arial')
            style.setFont(font)
            break
        case StyleType.NUMERATION :
            style.setAlignment(CellStyle.ALIGN_CENTER)
            style.setBorderRight(CellStyle.BORDER_THIN)
            style.setBorderLeft(CellStyle.BORDER_THIN)
            style.setBorderBottom(CellStyle.BORDER_THIN)
            style.setBorderTop(CellStyle.BORDER_THIN)
            style.setWrapText(true)

            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_BOLD)
            font.setFontHeightInPoints(8 as short)
            font.setFontName('Arial')
            font.setItalic(true)
            style.setFont(font)
            break
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
    }
    cellStyleMap.put(alias, style)
    return style
}

/** Ведение истории изменения категории ВЗЛ - удалить записи из истории для указанных строк нф. */
void deleteHistory(def deleteRows) {
    def records = getHistoryRecord(deleteRows)
    if (!records) {
        return
    }
    def uniqueRecordIds = records?.collect { it?.record_id?.value }

    logger.setTaUserInfo(userInfo)
    def provider = formDataService.getRefBookProvider(refBookFactory, 521L, providerCache)
    provider.deleteRecordVersions(logger, uniqueRecordIds)
}

/** Ведение истории изменения категории ВЗЛ - для всех строк нф добавить записи в историю со значением атрибута «Режим» = 2. */
void updateHistoryOnAccept() {
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    insertHistory(dataRows, 2)
}

/** Ведение истории изменения категории ВЗЛ - для измененных строк нф добавить записи в историю со значением атрибута «Режим» = 1. */
void updateHistoryOnCalcOrSave() {
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // найти последние записи в истории для каждой строки
    def records = getHistoryRecord(dataRows)
    if (!records) {
        return
    }
    def recordsMap = [:]
    for (def row : dataRows) {
        for (def record : records) {
            if (row.name == record?.JUR_PERSON?.value &&
                    (recordsMap[row] == null || recordsMap[row]?.CHANGE_DATE?.value >= record?.CHANGE_DATE?.value)) {
                recordsMap[row] = record
            }
        }
    }

    // среди найденых последних записях отобрать изменившиеся
    def changeRows = []
    for (def row : dataRows) {
        def record = recordsMap[row]
        if (record && record?.CATEGORY?.value != row.category) {
            changeRows.add(row)
        }
    }
    // добавить в историю новые записи
    insertHistory(changeRows, 1)
}

/** Получить все записи из истории для указанных строк. */
def getHistoryRecord(def dataRows) {
    if (!dataRows) {
        return null
    }
    def ids = dataRows.collect { it.name }
    def subFilter = 'JUR_PERSON = ' + ids.join(' or JUR_PERSON = ')

    def filter = "FORM_DATA_ID = ${formData.id} and ($subFilter)"
    def date = new Date()
    // необходимо отсорировать записи по атрибуту "дата изменения", т.к. из справочника значния приходят без времени
    RefBookAttribute sortAttribute = refBookFactory.get(521L).getAttribute('CHANGE_DATE')

    def provider = formDataService.getRefBookProvider(refBookFactory, 521L, providerCache)
    return provider.getRecords(date, null, filter, sortAttribute, true)
}

/**
 * Ведение истории изменения категории ВЗЛ - для указанных строк нф добавить записи в историю с заданным значением.
 *
 * @param dataRows строки нф
 * @param state значение атрибута «Режим»
 */
void insertHistory(def dataRows, def state) {
    // оставить только строки с заданным значением "Категория"
    def rows = dataRows.findAll { it.category }
    if (!rows) {
        return
    }
    def date = new Date()
    def records = []
    for (def row : rows) {
        def record = [:]

        // ИД НФ
        record.FORM_DATA_ID = new RefBookValue(RefBookAttributeType.NUMBER, formData.id)

        // ИД версии ВЗЛ
        record.JUR_PERSON = new RefBookValue(RefBookAttributeType.REFERENCE, row.name)

        // Значение категории
        record.CATEGORY = new RefBookValue(RefBookAttributeType.REFERENCE, row.category)

        // Дата изменения
        record.CHANGE_DATE = new RefBookValue(RefBookAttributeType.DATE, date)

        // Режим
        record.STATE = new RefBookValue(RefBookAttributeType.NUMBER, state)

        records.add(record)
    }
    def provider = formDataService.getRefBookProvider(refBookFactory, 521L, providerCache)
    provider.insertRecords(userInfo, date, records)
}