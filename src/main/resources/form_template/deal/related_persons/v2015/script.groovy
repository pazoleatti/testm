package form_template.deal.related_persons.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * 800 - Взаимозависимые лица.
 *
 * formTemplateId=800
 *
 * TODO:
 *      - добавить обработку собития по кнопке "Обновить"
 *      - сделать заполнение формы по кнопке "Обновить"
 *      - сделать расчет графы 12
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
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        // TODO (Ramil Timerbaev)
        formDataService.consolidationSimple(formData, logger)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        // TODO (Ramil Timerbaev)
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
def editableColumns = ['category']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = []

// Проверяемые на пустые значения атрибуты (графа 2..5, 10, 12, 13, 15)
@Field
def nonEmptyColumns = ['name', 'address', 'orgCode', 'countryCode', 'startData', 'category', 'vatStatus', 'depCriterion']

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

void update() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // TODO (Ramil Timerbaev) получить значения из справочника "Юридические лица"
    // TODO (Ramil Timerbaev) получить форму за предыдущий период
    // TODO (Ramil Timerbaev) получить форму "Прогноз крупных сделок"
    // TODO (Ramil Timerbaev) в зависимости от отчетного периода:
    //      - Отчетный период = 1 квартал: Форма «Приложение 4.2. Отчет в отношении доходов и расходов Банка по сделкам с ВЗЛ, РОЗ, НЛ по итогам окончания Налогового периода» за предыдущий налоговый период
    //      - Отчетный период = 6 месяцев: Форма «Приложение 4.1. (6 месяцев) Отчет в отношении доходов и расходов Банка по сделкам с российскими ВЗЛ, применяющими Общий режим налогообложения» за отчетный период
    //      - Отчетный период = 9 месяцев: Форма «Приложение 4.1. (9 месяцев) Отчет в отношении доходов и расходов Банка по сделкам с российскими ВЗЛ, применяющими Общий режим налогообложения» за отчетный период
    //      - Отчетный период = год:       Форма «Приложение 4.1 (9 месяцев) Отчет в отношении доходов и расходов Банка по сделкам с российскими ВЗЛ, применяющими Общий режим налогообложения» за предыдущий отчетный период
}

void calc() {
    // TODO (Ramil Timerbaev)
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // графа 12
        row.category = calc12(row)
        setRowStyles(row)
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Проверка на отсутствие в списке не ВЗЛ
        def record = getRefBookValue(520L, row.name)
        if (record && record?.START_DATE?.value) {
            def start = record?.START_DATE?.value
            def end = record?.END_DATE?.value
            if ((start <= getReportPeriodEndDate() && end == null) || (getReportPeriodStartDate() <= end && end <= getReportPeriodEndDate())) {
                def index = row.getIndex()
                def value2 = record?.NAME?.value
                def msg = "Строка $index: организация «$value2» не является взаимозависимым лицом в данном отчетном периоде!"
                rowError(logger, row, msg)
            }
        }
    }
}

def calc12(def row) {
    // TODO (Ramil Timerbaev)
    return null
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

// TODO (Ramil Timerbaev) выставить цвет по категории
void setRowStyles(def row) {
//    def columns = (isSection7 ? editableColumns + ['ndsRate', 'ndsDealSum'] : editableColumns)
//    columns.each {
//        row.getCell(it).editable = true
//        row.getCell(it).setStyleAlias('Редактируемая')
//    }
//    (allColumns - columns).each {
//        row.getCell(it).setStyleAlias('Автозаполняемая')
//    }
}

// TODO (Ramil Timerbaev) нужна ли сортировка
// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), null, true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null}
}