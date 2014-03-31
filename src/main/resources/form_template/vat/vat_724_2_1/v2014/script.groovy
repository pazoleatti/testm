package form_template.vat.vat_724_2_1.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Операции, не подлежащие налогообложению (освобождаемые от налогообложения), операции, не признаваемые объектом
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
        if (hasReport()) {
            calc()
        }
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
        consolidation()
        if (hasReport()) {
            calc()
        }
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

// дата начала отчетного периода
@Field
def startDate = null

// дата окончания отчетного периода
@Field
def endDate = null

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

        i4 += row.realizeCost
        i5 += row.obtainCost
    }
    def itog = dataRowHelper.getDataRow(dataRows, 'itog')
    itog.realizeCost = i4
    itog.obtainCost = i5
    dataRowHelper.save(dataRows);
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
    if (i4 != f4 || i5 != f5)
        logger.error("Итоговые значения рассчитаны неверно!")

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

def boolean hasReport() {
    boolean have = false
    // TODO have = Наличие "форма 102 бухгалтерской отчетности" подразделения и периода, для которых сформирована текущая форма
    def String dFormat = "dd.MM.yyyy"
    if (!have) {
        logger.error("Экземпляр Отчета о прибылях и убытках за период " + getReportPeriodStartDate().format(dFormat) +
                " - " + getReportPeriodEndDate().format(dFormat) + " не существует (отсутствуют данные для расчета)!")
    }
}

// Консолидация
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.getFormType().getTaxType() == TaxType.DEAL) {
            formDataService.getDataRowHelper(source).getAllCached().each { srcRow ->
                if (srcRow.getAlias()!= null && !srcRow.getAlias().equals('itog')) {
                    def row= dataRowHelper.getDataRow(dataRows, srcRow.getAlias())
                    row.realizeCost = (row.realizeCost ?: 0) + (srcRow.realizeCost ?: 0)
                    row.obtainCost = (row.obtainCost ?: 0) + (srcRow.obtainCost ?: 0)
                }
            }
        }
    }

    dataRowHelper.update(dataRows)
}