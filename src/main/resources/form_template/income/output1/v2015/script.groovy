package form_template.income.output1.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Сведения для расчёта налога с доходов в виде дивидендов (доходов от долевого участия в других организациях,
 * созданных на территории Российской Федерации)
 * formTemplateId=2306
 *
 * http://conf.aplana.com/pages/viewpage.action?pageId=8784122
 *
 * @author Stanislav Yasinskiy
 1. 	financialYear                   Отчетный год
 2.     taxPeriod                       Налоговый (отчетный) период
 3.     emitent                         Эмитент
 4.     decreeNumder                    Номер решения о распределении доходов от долевого участия
 5.     dividendType                    Вид дивидендов
 6. 	dividendSumRaspredPeriod        Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Всего
 7.     dividendSumNalogAgent           Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. По которым выступает в качестве налогового агента
 8. 	dividendForgeinOrgAll           Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России. Организациям
 9. 	dividendForgeinPersonalAll      Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России. Физическим лицам
 10. 	dividendStavka0                 Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке. 0%
 11. 	dividendStavkaLess5             Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке. До 5% включительно
 12. 	dividendStavkaMore5             Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке. Свыше 5% и до 10% включительно
 13. 	dividendStavkaMore10            Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке. Свыше 10%
 14. 	dividendRussianMembersAll       Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Всего
 15. 	dividendRussianOrgStavka9       Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Организациям (налоговая ставка - 9%)
 16. 	dividendRussianOrgStavka0       Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Организациям (налоговая ставка - 0%)
 17. 	dividendPersonRussia            Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Физическим лицам
 18. 	dividendMembersNotRussianTax    Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Не являющихся налогоплательщиками
 19. 	dividendAgentAll                Дивиденды, полученные. Всего
 20. 	dividendAgentWithStavka0        Дивиденды, полученные. В т. ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%
 21. 	dividendSumForTaxAll            Сумма дивидендов, используемых для исчисления налога по российским организациям. Всего
 22. 	dividendSumForTaxStavka9        Сумма дивидендов, используемых для исчисления налога по российским организациям. Налоговая ставка 9%
 23. 	dividendSumForTaxStavka0        Сумма дивидендов, используемых для исчисления налога по российским организациям. Налоговая ставка 0%
 24. 	taxSum                          Исчисленная сумма налога, подлежащая уплате в бюджет
 25. 	taxSumFromPeriod                Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды
 26. 	taxSumFromPeriodAll             Сумма налога, начисленная с дивидендов, выплаченных в отчетном квартале
 *
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
def editableColumns = ['financialYear', 'taxPeriod', 'emitent', 'decreeNumder', 'dividendType', 'dividendSumRaspredPeriod',
                       'dividendSumNalogAgent', 'dividendForgeinOrgAll',
                       'dividendForgeinPersonalAll', 'dividendStavka0', 'dividendStavkaLess5', 'dividendStavkaMore5',
                       'dividendStavkaMore10', 'dividendRussianMembersAll', 'dividendRussianOrgStavka9',
                       'dividendRussianOrgStavka0', 'dividendPersonRussia', 'dividendMembersNotRussianTax',
                       'dividendAgentAll', 'dividendAgentWithStavka0', 'taxSumFromPeriod', 'taxSumFromPeriodAll']

@Field
def nonEmptyColumns = ['financialYear', 'taxPeriod','dividendType', 'dividendSumRaspredPeriod', 'dividendSumNalogAgent',
                       'dividendForgeinOrgAll', 'dividendForgeinPersonalAll', 'dividendStavka0', 'dividendStavkaLess5',
                       'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendRussianOrgStavka9',
                       'dividendRussianOrgStavka0', 'dividendPersonRussia', 'dividendMembersNotRussianTax',
                       'dividendAgentAll', 'dividendAgentWithStavka0', 'taxSumFromPeriod', 'taxSumFromPeriodAll']

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
            // графа 21
            row.dividendSumForTaxAll = checkOverpower(calc21(row), row, "dividendSumForTaxAll")
            // графа 22
            row.dividendSumForTaxStavka9 = checkOverpower(calc22(row), row, "dividendSumForTaxStavka9")
            // графа 23
            row.dividendSumForTaxStavka0 = checkOverpower(calc23(row), row, "dividendSumForTaxStavka0")
            // графа 24
            row.taxSum = checkOverpower(calc24(row), row, "taxSum")
        }
        dataRowHelper.update(dataRows);
    }
}

def BigDecimal calc21(def row) {
    if (row.dividendSumRaspredPeriod == null || row.dividendAgentWithStavka0 == null) {
        return null
    }
    return roundValue(row.dividendSumRaspredPeriod - row.dividendAgentWithStavka0, 0)
}

def BigDecimal calc22(def row) {
    if (row.dividendRussianOrgStavka9 == null || !row.dividendSumRaspredPeriod || row.dividendSumForTaxAll == null) {
        return null
    }
    return roundValue(row.dividendRussianOrgStavka9 / row.dividendSumRaspredPeriod * row.dividendSumForTaxAll, 0)
}

def BigDecimal calc23(def row) {
    if (row.dividendRussianOrgStavka0 == null || !row.dividendSumRaspredPeriod || row.dividendSumForTaxAll == null) {
        return null
    }
    return roundValue(row.dividendRussianOrgStavka0 / row.dividendSumRaspredPeriod * row.dividendSumForTaxAll, 0)
}

def BigDecimal calc24(def row) {
    if (row.dividendSumForTaxStavka9 == null) {
        return null
    }
    return roundValue(row.dividendSumForTaxStavka9 * 0.09, 0)
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // Алиасы граф для арифметической проверки
    def arithmeticCheckAlias = ['dividendSumForTaxAll', 'dividendSumForTaxStavka9', 'dividendSumForTaxStavka0', 'taxSum']
    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    for (def row in dataRows) {

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
        checkNonEmptyColumns(row, row.getIndex(), ['emitent', 'decreeNumder'], logger, false)


        // Арифметические проверки расчета граф 21-24, 26
        needValue['dividendSumForTaxAll'] = calc21(row)
        needValue['dividendSumForTaxStavka9'] = calc22(row)
        needValue['dividendSumForTaxStavka0'] = calc23(row)
        needValue['taxSum'] = calc24(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // 2. Проверка наличия формы за предыдущий отчётный период
        if (formDataService.getFormDataPrev(formData, formData.departmentId) == null) {
            logger.warn('Форма за предыдущий отчётный период не создавалась!')
        }
    }
}

def roundValue(BigDecimal value, def precision) {
    value?.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

def checkOverpower(def value, def row, def alias) {
    if (value?.abs() >= 1e15) {
        def checksMap = [
                'dividendSumForTaxAll'    : "«графа 6» - «графа 20»",
                'dividendSumForTaxStavka9': "ОКРУГЛ («графа 15» / «графа 6» * «графа 21» ; 0) ",
                'dividendSumForTaxStavka0': "ОКРУГЛ («графа 16» / «графа 6» * «графа 21» ; 0) ",
                'taxSum'                  : "ОКРУГЛ («графа 22» * 9%; 0)"
        ]
        def aliasMap = [
                'dividendSumForTaxAll'    : '21',
                'dividendSumForTaxStavka9': '22',
                'dividendSumForTaxStavka0': '23',
                'taxSum'                  : '24'
        ]
        throw new ServiceException("Строка ${row.getIndex()}: значение «Графы ${aliasMap[alias]}» превышает допустимую " +
                "разрядность (15 знаков). «Графа ${aliasMap[alias]}» рассчитывается как «${checksMap[alias]}»!")
    }
    return value
}
