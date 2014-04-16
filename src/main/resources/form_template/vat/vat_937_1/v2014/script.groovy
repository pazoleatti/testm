package form_template.vat.vat_937_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * (937.1) Итоговые данные книги покупок
 * formTemplate = 606
 *
 * 1    period      Налоговый период
 * 2    bill        Всего счетов-фактур (шт.)
 * 3    dealNds     Всего покупок, включая НДС (руб.)
 * 4    deal_20     В том числе (руб.). Покупки, облагаемые налогом по ставке. 20%. Стоимость без НДС
 * 5    deal_20_Nds В том числе (руб.). Покупки, облагаемые налогом по ставке. 20%. Сумма НДС
 * 6    deal_18     В том числе (руб.). Покупки, облагаемые налогом по ставке. 18%. Стоимость без НДС
 * 7    deal_18_Nds В том числе (руб.). Покупки, облагаемые налогом по ставке. 18%. Сумма НДС
 * 8    deal_10     В том числе (руб.). Покупки, облагаемые налогом по ставке. 10%. Стоимость без НДС
 * 9    deal_10_Nds В том числе (руб.). Покупки, облагаемые налогом по ставке. 10%. Сумма НДС
 * 10   deal_0      В том числе (руб.). Покупки, облагаемые налогом по ставке. 0%
 * 11   deal        В том числе (руб.). Покупки, освобождаемые от налога
 * 12   ndsBank     Сумма НДС, отнесенная на расходы Банка (руб.)
 * 13   ndsPre      Сумма НДС, начисленная с авансов и предоплаты засчитываемая в налоговом периоде при реализации (руб.)
 * 14   diff        Расхождение (руб.)
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        calcAfterCreate()
        break
    case FormDataEvent.CALCULATE:
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
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        break
}

@Field
def allColumns = ['period', 'bill', 'dealNds', 'deal_20', 'deal_20_Nds', 'deal_18', 'deal_18_Nds',
                  'deal_10', 'deal_10_Nds', 'deal_0', 'deal', 'ndsBank', 'ndsPre', 'diff']
@Field
def calcColumns = ['bill', 'dealNds', 'deal_20', 'deal_20_Nds', 'deal_18', 'deal_18_Nds',
                   'deal_10', 'deal_10_Nds', 'deal_0', 'deal']
@Field
def totalAEditableColumns = ['bill', 'dealNds', 'deal_20', 'deal_20_Nds', 'deal_18', 'deal_18_Nds', 'deal_10', 'deal_10_Nds', 'ndsBank', 'ndsPre']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def dateFormat = 'dd.MM.yyyy'

void calcAfterCreate() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalPeriod = getDataRow(dataRows, 'totalPeriod') // 4-я строка
    def totalAnnul = getDataRow(dataRows, 'totalAnnul') // 5-строка
    def totalB = getDataRow(dataRows, 'totalB') // 6-я строка

    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // строка 4 графа 1
    def String code = "2${reportPeriod.order}-${reportPeriod.taxPeriod.year}"
    totalPeriod.period = code
    totalAnnul.period = "Аннулирование " + code
    totalB.period = "Всего " + code
    dataRowHelper.update(dataRows)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalA = getDataRow(dataRows, 'totalA') // 2-я строка
    def totalPeriod = getDataRow(dataRows, 'totalPeriod') // 4-я строка
    def totalAnnul = getDataRow(dataRows, 'totalAnnul') // 5-строка
    def totalB = getDataRow(dataRows, 'totalB') // 6-я строка

    // строка 2 «Графа 14» = По строке 2 («Графа 12» + «Графа 13» - «Графа 5» - «Графа 7» - «Графа 9»)
    totalA.with {
        diff = (ndsBank ?: 0) + (ndsPre ?: 0) - (deal_20_Nds ?: 0) - (deal_18_Nds ?: 0) - (deal_10_Nds ?: 0)
    }
    // строка 6 графы с 2 по 11
    calcColumns.each {
        totalB[it] = (totalPeriod[it] ?: 0) - (totalAnnul[it] ?: 0)
    }
    dataRowHelper.update(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalA = getDataRow(dataRows, 'totalA') // 2-я строка
    def totalPeriod = getDataRow(dataRows, 'totalPeriod') // 4-я строка
    def totalAnnul = getDataRow(dataRows, 'totalAnnul') // 5-строка
    def totalB = getDataRow(dataRows, 'totalB') // 6-я строка

    // 1. Обязательность заполнения:
    //	Графы 2-9, 12-14 строки 2;
    //	Графы 2-11 строк 4, 5
    checkNonEmptyColumns(totalA, totalA.getIndex(), totalAEditableColumns, logger, true)
    checkNonEmptyColumns(totalPeriod, totalPeriod.getIndex(), calcColumns, logger, true)
    checkNonEmptyColumns(totalAnnul, totalAnnul.getIndex(), calcColumns, logger, true)
    // 2-4. По строкам 2, 6:
    // «Графа 5» = «Графа 4» * 20 / 100
    // «Графа 7» = «Графа 6» * 18 / 100
    // «Графа 9» = «Графа 8» * 10 / 100
    for (def row in [totalA, totalB]) {
        def errorMsg = "Строка ${row.getIndex()}: "
        if (row.deal_20_Nds != row.deal_20 * 0.2) {
            logger.warn(errorMsg + "Сумма НДС, облагаемая по ставке 20%% неверная!")
        }
        if (row.deal_18_Nds != row.deal_18 * 0.18) {
            logger.warn(errorMsg + "Сумма НДС, облагаемая по ставке 18%% неверная!")
        }
        if (row.deal_10_Nds != row.deal_10 * 0.1) {
            logger.warn(errorMsg + "Сумма НДС, облагаемая по ставке 10%% неверная!")
        }
    }
    // 5. По строке 2:
    // «Графа 3» = «Графа 4» + «Графа 5» + «Графа 6» + «Графа 7» + «Графа 8» + «Графа 9»
    if (totalA.dealNds != totalA.deal_20 + totalA.deal_20_Nds + totalA.deal_18 + totalA.deal_18_Nds + totalA.deal_10 + totalA.deal_10_Nds) {
        logger.warn("Строка ${totalA.getIndex()}: " + "Сумма покупок по разделу «А» неверная!")
    }
    // 6. По строке 6:
    // «Графа 3» = «Графа 4» + «Графа 5» + «Графа 6» + «Графа 7» + «Графа 8» + «Графа 9» + «Графа 10» + «Графа 11»
    if (totalB.dealNds != totalB.deal_20 + totalB.deal_20_Nds + totalB.deal_18 + totalB.deal_18_Nds + totalB.deal_10 + totalB.deal_10_Nds + totalB.deal_0 + totalB.deal) {
        logger.warn("Строка ${totalB.getIndex()}: " + "Сумма покупок по разделу «Б» неверная!")
    }
    // 7. По строке 2:
    // «Графа 14» = «Графа 12» + «Графа 13» - «Графа 5» - «Графа 7» - «Графа 9»
    if (totalA.diff != totalA.ndsBank + totalA.ndsPre - totalA.deal_20_Nds - totalA.deal_18_Nds - totalA.deal_10_Nds) {
        logger.error("Строка ${totalA.getIndex()}: " + "Неверно рассчитана графа «Расхождение (руб.)»!")
    }
    // 8. Если существует экземпляр налоговой формы 937.1.14, чье подразделение и  налоговый период,
    // соответствуют подразделению и налоговому периоду формы 937.1, то:
    //      a.	Выполняется проверка: «Графа 14» строки 2 формы 937.1 = «Графа 3» итоговой строки – «Графа 3» строки 13 (форма 937.1.14).
    //      b.	Если результат данной проверки неуспешный, то выдается сообщение об ошибке
    // Иначе если экземпляр налоговой формы 937.1.14, чье подразделение и  налоговый период,
    // соответствуют подразделению и налоговому периоду формы 937.1, не существует, то выдается сообщение об ошибке
    def appFormData = formDataService.find(607, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (appFormData) {
        def appDataRows = formDataService.getDataRowHelper(appFormData)?.allCached
        if (appDataRows) {
            def appR13Row = getDataRow(appDataRows, 'R13')
            def appTotalRow = getDataRow(appDataRows, 'total')
            if (totalA.ndsPre != (appTotalRow.differences - appR13Row.differences)) {
                logger.warn("Сумма расхождения не соответствует расшифровке! ")
            }
        }
    } else {
        logger.warn("Экземпляр налоговой формы 937.1.14 «Расшифровка графы 14» за период %s - %s не существует (отсутствуют первичные данные для проверки)!",
                getReportPeriodStartDate().format(dateFormat), getReportPeriodEndDate().format(dateFormat))
    }
    // 9. «Графа N» строки 6 = Графа N строки 4 - Графа N строки 5, где N = 2, 3, 4, 5, 6, 7, 8, 9, 10 или 11
    calcColumns.each {
        if (totalB[it] != totalPeriod[it] - totalAnnul[it]) {
            logger.error("Строка ${totalB.getIndex()}: " + "Итоговые значения рассчитаны неверно!")
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalA = getDataRow(dataRows, 'totalA') // 2-я строка
    def totalPeriod = getDataRow(dataRows, 'totalPeriod') // 4-я строка
    def totalAnnul = getDataRow(dataRows, 'totalAnnul') // 5-строка
    def totalB = getDataRow(dataRows, 'totalB') // 6-я строка

    //очистить форму
    totalAEditableColumns.each {
        totalA[it] = 0
    }
    calcColumns.each {
        totalPeriod[it] = 0
        totalAnnul[it] = 0
        totalB[it] = 0
    }

    for (formDataSource in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind())) {
        if (formDataSource.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(formDataSource.formTypeId, formDataSource.kind, formDataSource.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceForm = formDataService.getDataRowHelper(source)
                addRowsToRows(dataRows, sourceForm.allCached)
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

void addRowsToRows(def dataRows, def addRows) {
    def totalA = getDataRow(dataRows, 'totalA') // 2-я строка
    def totalPeriod = getDataRow(dataRows, 'totalPeriod') // 4-я строка
    def totalAnnul = getDataRow(dataRows, 'totalAnnul') // 5-строка
    def totalB = getDataRow(dataRows, 'totalB') // 6-я строка

    def addA = getDataRow(addRows, 'totalA') // 2-я строка
    def addPeriod = getDataRow(addRows, 'totalPeriod') // 4-я строка
    def addAnnul = getDataRow(addRows, 'totalAnnul') // 5-строка
    def addB = getDataRow(addRows, 'totalB') // 6-я строка

    totalAEditableColumns.each {
        totalA[it] += addA[it]
    }
    calcColumns.each {
        totalPeriod[it] += addPeriod[it]
        totalAnnul[it] += addAnnul[it]
        totalB[it] += addB[it]
    }
}

def getReportPeriodStartDate() {
    if (startDate == null) {
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
