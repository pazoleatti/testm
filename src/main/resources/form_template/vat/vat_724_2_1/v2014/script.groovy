package form_template.vat.vat_724_2_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * (724.2.1) Операции, не подлежащие налогообложению (освобождаемые от налогообложения), операции, не признаваемые объектом
 * налогообложения, операции по реализации товаров (работ, услуг), местом реализации которых не признается территория
 * Российской Федерации, а также суммы оплаты, частичной оплаты в счет предстоящих поставок (выполнения работ,
 * оказания услуг), длительность производственного цикла изготовления которых составляет свыше шести месяцев
 *
 * formTemplateId=601
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkIncome102()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        checkIncome102()
        consolidation()
        calc()
        logicCheck()
        break
}

// 1 № пп  -  rowNum
// 2 Код операции  -  code
// 3 Наименование операции  -  name
// 4 Стоимость реализованных (переданных) товаров (работ, услуг) без НДС  -  realizeCost
// 5 Стоимость приобретенных товаров  (работа, услуг), не облагаемых НДС  -  obtainCost

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
def autoFillColumns = ['realizeCost', 'obtainCost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNum', 'code', 'name', 'realizeCost']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

// Cправочник «Отчет о прибылях и убытках (Форма 0409102-СБ)»
@Field
def income102Data = null

@Field
def dateFormat = "dd.MM.yyyy"

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def i4 = 0, i5 = 0

    for (def row in dataRows) {
        // TODO посчитать графы 4 и 5 из "форма 102 бухгалтерской отчетности"
        // TODO Нужны соответствия кодов ОПУ о кодов операций
        // getIncome102DataByOPU()
        // income102Row.TOTAL_SUM.numberValue

        i4 += row.realizeCost
        i5 += row.obtainCost
    }
    def itog = dataRowHelper.getDataRow(dataRows, 'itog')
    itog.realizeCost = i4
    itog.obtainCost = i5
    dataRowHelper.save(dataRows);
}

// Строки справочника «Отчет о прибылях и убытках» по ОПУ-коду
def getIncome102DataByOPU(def String opuCode) {
    def retVal = []
    for (def income102Row : getIncome102Data()) {
        if (income102Row.OPU_CODE.numberValue == opuCode) {
            retVal.add(income102Row)
        }
    }
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def i4 = 0, i5 = 0
    def f4 = 0, f5 = 0
    for (def row in dataRows) {
        if (row.getAlias().equals('itog')) {
            i4 = row.realizeCost
            i5 = row.obtainCost
        } else {
            def index = row.getIndex()

            // 1. Проверка заполнения граф
            checkNonEmptyColumns(row, index ?: 0, nonEmptyColumns, logger, true)

            f4 += row.realizeCost ?: 0
            f5 += row.obtainCost ?: 0
        }
    }

    // 2. Проверка итоговых значений
    if (i4 != f4 || i5 != f5) {
        logger.error("Итоговые значения рассчитаны неверно!")
    }

}

def getReportPeriodStartDate() {
    if (!startDate) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Получение данных из справочника «Отчет о прибылях и убытках» для текужего подразделения и отчетного периода
def getIncome102Data() {
    if (income102Data == null) {
        income102Data = refBookFactory.getDataProvider(52L)?.getRecords(getReportPeriodEndDate(), null,
                "REPORT_PERIOD_ID = '${formData.reportPeriodId}' AND DEPARTMENT_ID = ${formData.departmentId}", null)
    }
    return income102Data
}

// Проверка наличия необходимых записей в справочнике «Отчет о прибылях и убытках»
void checkIncome102() {
    // Наличие экземпляра Отчета о прибылях и убытках подразделения и периода, для которых сформирована текущая форма
    if (getIncome102Data() == null) {
        throw new ServiceException("Экземпляр Отчета о прибылях и убытках за период " +
                "${getReportPeriodStartDate().format(dateFormat)} - ${getReportPeriodEndDate().format(dateFormat)} " +
                "не существует (отсутствуют данные для расчета)!")
    }
}

// Консолидация
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.getFormType().getTaxType() == TaxType.VAT) {
            formDataService.getDataRowHelper(source).getAllCached().each { srcRow ->
                if (srcRow.getAlias() != null && !srcRow.getAlias().equals('itog')) {
                    def row = dataRowHelper.getDataRow(dataRows, srcRow.getAlias())
                    row.realizeCost = (row.realizeCost ?: 0) + (srcRow.realizeCost ?: 0)
                    row.obtainCost = (row.obtainCost ?: 0) + (srcRow.obtainCost ?: 0)
                }
            }
        }
    }

    dataRowHelper.update(dataRows)
}