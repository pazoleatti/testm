package form_template.income.rnu14

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Форма "УНП" ("РНУ-14 - Регистр налогового учёта нормируемых расходов")
 * formTemplateId=321
 *
 * @author lhaziev
 * @author bkinzyabulatov
 *
 * графа 1  - knu
 * графа 2  - mode
 * графа 3  - sum
 * графа 4  - normBase
 * графа 5  - normCoef
 * графа 6  - limitSum
 * графа 7  - inApprovedNprms
 * графа 8  - overApprovedNprms
 *
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
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
    case FormDataEvent.COMPOSE:
        calc()
        logicCheck()
        break
}

// все атрибуты
@Field
def allColumns = ['knu', 'mode', 'sum', 'normBase', 'normCoef', 'limitSum', 'inApprovedNprms', 'overApprovedNprms']

@Field
def nonEmptyColumns = ['normBase']

@Field
def col = ['21270', '21410', '20698', '20700', '20690']

@Field
def knuBase = ['20480', '20485', '20490', '20500', '20505', '20530']

@Field
def knuTax = ['20480', '20485', '20490', '20500', '20505', '20530', '20510', '20520']

@Field
def knuComplex = ['10633', '10634', '10650', '10670', '10855', '10880', '10900', '10850',
            '11180', '11190', '11200', '11210', '11220', '11230', '11240', '11250',
            '11260', '10840', '10860', '10870', '10890']

@Field
def knuSimpleRNU4 = ['10001', '10006', '10041', '10300', '10310', '10320', '10330', '10340',
            '10350', '10360', '10370', '10380', '10390', '10450', '10460', '10470',
            '10480', '10490', '10571', '10580', '10590', '10600', '10610', '10630',
            '10631', '10632', '10640', '10680', '10690', '10740', '10744', '10748',
            '10752', '10756', '10760', '10770', '10790', '10800', '11140', '11150',
            '11160', '11170', '11320', '11325', '11330', '11335', '11340', '11350',
            '11360', '11370', '11375']

@Field
def knuSimpleRNU6 = ['10001', '10006', '10300', '10310', '10320', '10330', '10340', '10350',
            '10360', '10470', '10480', '10490', '10571', '10590', '10610', '10640',
            '10680', '10690', '11340', '11350', '11370', '11375']

@Field
def koeffNormBase = [4 / 100, 1 / 100, 6 / 100, 12 / 100, 15000]

void prevPeriodCheck() {
    if (getFormDataOutcomeSimple() == null) {
        throw new ServiceException("Не найден экземпляр Сводной формы «Расходы, учитываемые в простых РНУ» за текущий отчетный период!")
    }
    if (getFormDataSimple() == null) {
        throw new ServiceException("Не найден экземпляр Сводной формы «Доходы, учитываемые в простых РНУ»!")
    }
    if (getFormDataComplex() == null) {
        throw new ServiceException("Не найден экземпляр Сводной формы «Сводная форма начисленных доходов»!")
    }
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def formDataComplex = getFormDataComplex()
    def formDataSimple = getFormDataSimple()
    def formDataOutcome = getFormDataOutcomeSimple()
    def dataRowsComplex
    if (formDataComplex != null) {
        dataRowsComplex = formDataService.getDataRowHelper(formDataComplex)?.allCached
    }
    def dataRowsSimple
    if (formDataSimple != null) {
        dataRowsSimple = formDataService.getDataRowHelper(formDataSimple)?.allCached
    }
    def dataRowsRNU
    if (formDataOutcome != null) {
        dataRowsRNU = formDataService.getDataRowHelper(formDataOutcome)?.allCached
    }
    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    for (def row : dataRows) {
        def rowA = getTotalRowFromRNU(col[dataRows.indexOf(row)], dataRowsRNU)
        if (rowA != null) {
            // 3 - графа 8 строки А + (графа 5 строки А – графа 6 строки А)
            row.sum = (rowA.rnu5Field5Accepted ?: 0) + (rowA.rnu7Field10Sum ?: 0) - (rowA.rnu7Field12Accepted ?: 0)
            // 4 - сумма по всем (графа 8 строки B + (графа 5 строки B – графа 6 строки B)),
            // КНУ которых совпадает со значениями в colBase (или colTax если налоговый период)
            if (dataRows.indexOf(row) != 4 && dataRows.indexOf(row) != 1) {//не 5-я и 2-я строка
                def normBase = 0
                /** Признак налоговый ли это период. */
                def isTaxPeriod = (reportPeriod != null && reportPeriod.order == 4)
                for (def knu : (isTaxPeriod ? knuTax : knuBase)) {
                    def rowB = getTotalRowFromRNU(knu, dataRowsRNU)
                    if (rowB != null) {
                        normBase += (rowB.rnu5Field5Accepted ?: 0) + (rowB.rnu7Field10Sum ?: 0) - (rowB.rnu7Field12Accepted ?: 0)
                    }
                }
                row.normBase = normBase
            } else if (dataRows.indexOf(row) == 1) {//2-я строка(сложнее)
                def normBase = 0
                //Сумма значений по графе 9 (столбец «Доход по данным налогового учёта. Сумма») в сложных доходах где КНУ = ...
                //объединил
                if (dataRowsComplex != null) {
                    for (def rowComplex : dataRowsComplex) {
                        if (rowComplex.incomeTypeId in knuComplex) {
                            normBase += (rowComplex.incomeTaxSumS?:0)
                        }
                    }
                }
                //простые доходы
                if (dataRowsSimple != null) {
                    for (def rowSimple : dataRowsSimple) {
                        //+ Сумма значений по графе 8 (столбец «РНУ-4 (графа 5) сумма») в простых доходах
                        if (rowSimple.incomeTypeId in knuSimpleRNU4) {
                            normBase += (rowSimple.rnu4Field5Accepted?:0)
                        }
                        //+ Сумма значений по графе 5 (столбец «РНУ-6 (графа 10) сумма»)
                        //- Сумма значений по графе 6 (столбец «РНУ-6 (графа 12). Сумма»)
                        // КНУ одни, поэтому объединил
                        if (rowSimple.incomeTypeId in knuSimpleRNU6) {
                            normBase += ((rowSimple.rnu6Field10Sum?:0) - (rowSimple.rnu6Field12Accepted?:0))
                        }
                    }
                }
                row.normBase = normBase
            }
            // 6
            if (row.normBase != null) {
                row.limitSum = koeffNormBase[dataRows.indexOf(row)] * row.normBase
            }
            def diff6_3 = row.limitSum - row.sum
            if (diff6_3 != null) {
                // 7 - 1. ЕСЛИ («графа 6» – «графа 3») ≥ 0, то «графа 3»;
                //     2. ЕСЛИ («графа 6» – «графа 3») < 0, то «графа 6»;
                if (diff6_3 >= 0) {
                    row.inApprovedNprms = row.sum
                } else {
                    row.inApprovedNprms = row.limitSum
                }
                // 8 - 1. ЕСЛИ («графа 6» – «графа 3») ≥ 0, то 0;
                //     2. ЕСЛИ («графа 6» – «графа 3») < 0, то «графа 3» - «графа 6».
                if (diff6_3 >= 0) {
                    row.overApprovedNprms = 0
                } else {
                    row.overApprovedNprms = -diff6_3
                }
            }
        }
    }
    dataRowHelper.save(dataRows);
}

void logicCheck() {
    for (def row : formDataService.getDataRowHelper(formData).allCached) {
        // 1. Обязательность заполнения полей графы 4
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }
}

/**
 * Получить строку из сводной налоговой формы "Расходы, учитываемые в простых РНУ", у которой графа 1 ("КНУ") = knu
 * @param knu КНУ
 * @param dataRowsOutcome строки НФ простые доходы
 */
def getTotalRowFromRNU(def knu, def dataRowsOutcome) {
    for (def row : dataRowsOutcome) {
        if (row.consumptionTypeId == knu) {
            return row
        }
    }
    return null
}

// Получить данные формы "расходы простые" (id = 304)
def getFormDataOutcomeSimple() {
    return formDataService.find(304, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId)
}

// Получить данные формы "доходы сложные" (id = 302)
def getFormDataComplex() {
    return formDataService.find(302, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId)
}

// Получить данные формы "доходы простые" (id = 301)
def getFormDataSimple() {
    return formDataService.find(301, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId)
}