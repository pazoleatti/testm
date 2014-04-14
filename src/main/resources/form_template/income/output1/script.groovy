package form_template.income.output1

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
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
    case FormDataEvent.IMPORT:
        noImport(logger)
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]

@Field
def editableColumns = ['financialYear', 'dividendSumRaspredPeriod', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll',
        'dividendStavka0', 'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10',
        'dividendRussianOrgStavka9', 'dividendRussianOrgStavka0', 'dividendPersonRussia', 'dividendMembersNotRussianTax',
        'dividendAgentAll', 'dividendAgentWithStavka0', 'taxSumFromPeriodAll']

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
            row.dividendRussianMembersAll = checkOverpower(calc11(row), row, "dividendRussianMembersAll")
            // графа 18
            row.dividendSumForTaxAll = checkOverpower(calc18(row), row, "dividendSumForTaxAll")
            // графа 19
            row.dividendSumForTaxStavka9 = checkOverpower(calc19(row), row, "dividendSumForTaxStavka9")
            // графа 20
            row.dividendSumForTaxStavka0 = checkOverpower(calc20(row), row, "dividendSumForTaxStavka0")
            // графа 21
            row.taxSum = checkOverpower(calc21(row), row, "taxSum")
            // графа 22
            row.taxSumFromPeriod = checkOverpower(calc22(row), row, "taxSumFromPeriod")
        }
        dataRowHelper.update(dataRows);
    }
}

def BigDecimal calc11(def row) {
    if (row.dividendSumRaspredPeriod == null || row.dividendForgeinOrgAll == null || row.dividendForgeinPersonalAll == null) {
        return null
    }
    return roundValue(row.dividendSumRaspredPeriod - row.dividendForgeinOrgAll - row.dividendForgeinPersonalAll, 0)
}

def BigDecimal calc18(def row) {
    if (row.dividendRussianMembersAll == null || row.dividendAgentWithStavka0 == null) {
        return null
    }
    return roundValue((row.dividendRussianMembersAll ?: 0) - (row.dividendAgentWithStavka0 ?: 0), 0)
}

def BigDecimal calc19(def row) {
    if (row.dividendRussianOrgStavka9 == null || !row.dividendRussianMembersAll || row.dividendSumForTaxAll == null) {
        return null
    }
    return roundValue(row.dividendRussianOrgStavka9 / row.dividendRussianMembersAll * row.dividendSumForTaxAll, 0)
}

def BigDecimal calc20(def row) {
    if (row.dividendRussianOrgStavka0 == null || !row.dividendRussianMembersAll || row.dividendSumForTaxAll == null) {
        return null
    }
    return roundValue(row.dividendRussianOrgStavka0 / row.dividendRussianMembersAll * row.dividendSumForTaxAll, 0)
}

def BigDecimal calc21(def row) {
    if (row.dividendSumForTaxStavka9 == null) {
        return null
    }
    return roundValue(row.dividendSumForTaxStavka9 * 0.09, 0)
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
            'dividendSumForTaxStavka0', 'taxSum', 'taxSumFromPeriod']
    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    for (def row in dataRows) {

        // проверка на превышение разрядности
        checkOverpower(row)

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // Арифметические проверки расчета граф 11, 18, 19, 20, 22
        needValue['dividendRussianMembersAll'] = calc11(row)
        needValue['dividendSumForTaxAll'] = calc18(row)
        needValue['dividendSumForTaxStavka9'] = calc19(row)
        needValue['dividendSumForTaxStavka0'] = calc20(row)
        needValue['taxSum'] = calc21(row)
        needValue['taxSumFromPeriod'] = calc22(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // 2. Проверка наличия формы за предыдущий отчётный период
        if (formDataService.getFormDataPrev(formData, formData.departmentId) == null) {
            logger.warn('Форма за предыдущий отчётный период не создавалась!')
        }
    }
}

def roundValue(BigDecimal value, def precision) {
    value.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

def checkOverpower(def value, def row, def alias) {
    def checksMap = [
            'dividendRussianMembersAll': "ОКРУГЛ ( «графа 4» – «графа 5» – «графа 6» ; 0)",
            'dividendSumForTaxAll'     : "ОКРУГЛ ( «графа 11» – «графа 17» ; 0)",
            'dividendSumForTaxStavka9' : "ОКРУГЛ ( «графа 12» / «графа 11» * «графа 18» ; 0) ",
            'dividendSumForTaxStavka0' : "ОКРУГЛ ( «графа 13» / «графа 11» * «графа 18» ; 0) ",
            'taxSum'                   : "ОКРУГЛ ( «графа 19» / 100 * 9; 0)",
            'taxSumFromPeriod'         : "«графа 22» предыдущего отчётного периода + «графа 23» предыдущего отчётного периода.\n Значения граф текущей формы и формы предудущего отчётного периода берутся для строк с одинаковым годом , т. е. «графа 3» в текущем отчётном периоде = «графе 3» в предыдущем отчётном периоде.\n" +
                    "Если отчёт по году («графа 3») впервые, то «графа 22» принимает значение «0»"
    ]
    def aliasMap = [
            'dividendRussianMembersAll' : '11',
            'dividendSumForTaxAll' : '18',
            'dividendSumForTaxStavka9' : '19',
            'dividendSumForTaxStavka0' : '20',
            'taxSum' : '21',
            'taxSumFromPeriod' : '22'
    ]
    if (value?.abs() >= 1e15) {
        throw new ServiceException("Строка ${row.getIndex()}: значение «Графы ${aliasMap[alias]}» превышает допустимую разрядность (15 знаков). «Графа ${aliasMap[alias]}» рассчитывается как «${checksMap[alias]}»!")
    }
    return value
}
