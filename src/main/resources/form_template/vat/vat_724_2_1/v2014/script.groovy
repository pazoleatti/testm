package form_template.vat.vat_724_2_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
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
 * TODO:
 *      - расчет графов 4 и 5 не доделан (по чтз много вопросов к заказчику)
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
        checkIncome102()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        checkIncome102()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        checkIncome102()
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
        break
}

// графа 1 - rowNum         № пп
// графа 2 - code           Код операции
// графа 3 - name           Наименование операции  -
// графа 4 - realizeCost    Стоимость реализованных (переданных) товаров (работ, услуг) без НДС
// графа 5 - obtainCost     Стоимость приобретенных товаров  (работа, услуг), не облагаемых НДС

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Автозаполняемые атрибуты (графа 4, 5)
@Field
def autoFillColumns = ['realizeCost', 'obtainCost']

// Проверяемые на пустые значения атрибуты (группа 1..4)
@Field
def nonEmptyColumns = ['rowNum', 'code', 'name', 'realizeCost']

// Поля, для которых подсчитываются итоговые значения (графа 4, 5)
@Field
def totalColumns = ['realizeCost', 'obtainCost']

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

    for (def row in dataRows) {
        if (row.getAlias() == 'itog') {
            continue
        }
        // TODO посчитать графы 4 и 5 из "форма 102 бухгалтерской отчетности"
        // TODO Нужны соответствия кодов ОПУ о кодов операций
        // getIncome102DataByOPU()
        // income102Row.TOTAL_SUM.numberValue
        def values = calc4_5(row)
        // графа 4
        row.realizeCost = values.realizeCost
        // графа 5
        row.obtainCost = values.obtainCost
    }

    // подсчет итогов
    def itogValues = calcItog(dataRows)
    def itog = getDataRow(dataRows, 'itog')
    totalColumns.each { alias ->
        itog.getCell(alias).setValue(itogValues[alias], itog.getIndex())
    }
    dataRowHelper.save(dataRows);
}

// Строки справочника «Отчет о прибылях и убытках» по ОПУ-коду
def getIncome102DataByOPU(def String opuCode) {
    def retVal = []
    for (def income102Row : getIncome102Data()) {
        if (income102Row.OPU_CODE.value == opuCode) {
            retVal.add(income102Row)
        }
    }
    return retVal
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row in dataRows) {
        if (row.getAlias() == 'itog') {
            continue
        }
        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }

    // 2. Проверка итоговых значений
    def itogValues = calcItog(dataRows)
    def itog = getDataRow(dataRows, 'itog')
    totalColumns.each { alias ->
        if (itog.getCell(alias).value != itogValues[alias]) {
            logger.error(WRONG_TOTAL, getColumnName(itog, alias))
        }
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
        def filter = "REPORT_PERIOD_ID = ${formData.reportPeriodId} AND DEPARTMENT_ID = ${formData.departmentId}"
        income102Data = refBookFactory.getDataProvider(52L)?.getRecords(getReportPeriodEndDate(), null, filter, null)
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
    def dataRows = dataRowHelper.allCached

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.formType.taxType == TaxType.VAT) {
            formDataService.getDataRowHelper(source).allCached.each { srcRow ->
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

// TODO (Ramil Timerbaev) дополнить по чтз, много вопросов к заказчику
// расчитать значения графы 4 и 5, вернуть в мапе
def calc4_5(row) {
    def for4 = null
    def for5 = null
    switch (row.code) {
        case '1010201' :
            for4 = getIncome102DataByOPU('16301') + getIncome102DataByOPU('17306.17')
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010239' :
            for4 = getIncome102DataByOPU('17306.17')
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010242' :
            for4 = getIncome102DataByOPU('12403.03')
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010243' :
            for4 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010275' :
            for4 = getIncome102DataByOPU('26404')
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010276' :
            for4 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010277' :
            for4 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010285' :
            for4 = getIncome102DataByOPU('12403.01')
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010288' :
            for4 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010298' :
            for4 = getIncome102DataByOPU('16302.01')
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010801' :
            for4 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010802' :
            for4 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010805' :
            for4 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
        case '1010806' :
            for4 = getIncome102DataByOPU('16302.01')
            for5 = getIncome102DataByOPU('') // TODO (Ramil Timerbaev)
            break
    }
    def result = [:]
    result.realizeCost = roundValue(for4 ? for4.sum { it -> it.TOTAL_SUM.value } : 0)
    result.obtainCost = roundValue(for5 ? for5.sum { it -> it.TOTAL_SUM.value } : 0)
    return result
}

def calcItog(def dataRows) {
    def itogValues = [:]
    totalColumns.each {alias ->
        itogValues[alias] = roundValue(0)
    }
    for (def row in dataRows) {
        if (row.getAlias() == 'itog') {
            continue
        }
        totalColumns.each { alias ->
            itogValues[alias] += roundValue(row.getCell(alias).value ?: 0)
        }
    }
    return itogValues
}

def roundValue(def value, int precision = 2) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}