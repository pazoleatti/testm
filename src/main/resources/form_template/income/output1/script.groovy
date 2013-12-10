package form_template.income.output1

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import groovy.transform.Field

/**
 * Сведения для расчёта налога с доходов в виде дивидендов (доходов от долевого участия в других организациях,
 * созданных на территории Российской Федерации)
 * formTemplateId=306
 *
 * http://conf.aplana.com/pages/viewpage.action?pageId=8784122
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        if (formData.kind != FormDataKind.ADDITIONAL) {
            logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
        }
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]

@Field
def editableColumns = ['financialYear', 'dividendSumRaspredPeriod', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll',
        'dividendStavka0', 'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10',
        'dividendRussianOrgStavka9', 'dividendRussianOrgStavka0', 'dividendPersonRussia', 'dividendMembersNotRussianTax',
        'dividendAgentAll', 'dividendAgentWithStavka0', 'taxSum', 'taxSumFromPeriodAll']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['dividendType', 'taxPeriod', 'financialYear', 'dividendSumRaspredPeriod', 'dividendForgeinOrgAll',
        'dividendForgeinPersonalAll', 'dividendStavka0', 'dividendStavkaLess5', 'dividendStavkaMore5',
        'dividendStavkaMore10', 'dividendRussianMembersAll', 'dividendRussianOrgStavka9', 'dividendRussianOrgStavka0',
        'dividendPersonRussia', 'dividendMembersNotRussianTax', 'dividendAgentAll', 'dividendAgentWithStavka0',
        'dividendSumForTaxAll', 'dividendSumForTaxStavka9', 'dividendSumForTaxStavka0', 'taxSum', 'taxSumFromPeriod',
        'taxSumFromPeriodAll']

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    if (!dataRows.isEmpty()) {

        for (def row in dataRows) {
            // графа 1
            row.dividendType = '2'
            // графа 2
            row.taxPeriod = '34'
            // графа 11
            row.dividendRussianMembersAll = calc11(row)
            // графа 18
            row.dividendSumForTaxAll = calc18(row)
            // графа 19
            row.dividendSumForTaxStavka9 = calc19(row)
            // графа 20
            row.dividendSumForTaxStavka0 = calc20(row)
            // графа 22
            row.taxSumFromPeriod = calc22(row)
        }
        dataRowHelper.update(dataRows);
    }
}

def BigDecimal calc11(def row) {
    if (row.dividendSumRaspredPeriod == null || row.dividendForgeinOrgAll == null || row.dividendForgeinPersonalAll == null) {
        return null
    }
    return row.dividendSumRaspredPeriod - row.dividendForgeinOrgAll - row.dividendForgeinPersonalAll
}

def BigDecimal calc18(def row) {
    if (row.dividendRussianMembersAll == null || row.dividendAgentWithStavka0 == null) {
        return null
    }
    return (row.dividendRussianMembersAll ?: 0) - (row.dividendAgentWithStavka0 ?: 0)
}

def BigDecimal calc19(def row) {
    return row.dividendRussianOrgStavka9
}

def BigDecimal calc20(def row) {
    return row.dividendRussianOrgStavka0
}

def BigDecimal calc22(def row) {
    // TODO сумма или одна строка? если одна строка - то закоментаренный вариант
    // [15:10:41] Sariya Mustafina: в прошлом периоде должна быть одна строка
    // [15:10:55] Sariya Mustafina: но это нигде не проверяется
    // [15:10:58] Sariya Mustafina: спрошу
    def result = 0
    // --
    formPrev = formDataService.getFormDataPrev(formData, formData.departmentId)
    if (formPrev != null) {
        for (rowPrev in formDataService.getDataRowHelper(formPrev).getAll()) {
            if (rowPrev.financialYear.format('yyyy') == row.financialYear.format('yyyy')) {
                result += rowPrev.taxSumFromPeriod ?: 0 + rowPrev.taxSumFromPeriodAll ?: 0
                // return rowPrev.taxSumFromPeriod ?: 0 + rowPrev.taxSumFromPeriodAll ?: 0
            }
        }
    }
    return result
    // return 0
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // Алиасы граф для арифметической проверки
    def arithmeticCheckAlias = ['dividendRussianMembersAll', 'dividendSumForTaxAll', 'dividendSumForTaxStavka9',
            'dividendSumForTaxStavka0', 'taxSumFromPeriod']
    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    for (def row in dataRows) {
        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // Арифметические проверки расчета граф 11, 18, 19, 20, 22
        needValue['dividendRussianMembersAll'] = calc11(row)
        needValue['dividendSumForTaxAll'] = calc18(row)
        needValue['dividendSumForTaxStavka9'] = calc19(row)
        needValue['dividendSumForTaxStavka0'] = calc20(row)
        needValue['taxSumFromPeriod'] = calc22(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // 2. Проверка наличия формы за предыдущий отчётный период
        if (formDataService.getFormDataPrev(formData, formData.departmentId) == null) {
            logger.warn('Форма за предыдущий отчётный период не создавалась!')
        }
    }
}