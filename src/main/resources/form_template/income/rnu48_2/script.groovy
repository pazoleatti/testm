package form_template.income.rnu48_2

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode

/**
 * "(РНУ-48.2) Регистр налогового учёта «Сводная ведомость ввода в эксплуатацию инвентаря и принадлежностей до 40 000 руб.»
 * formTemplateId=313
 *
 * @author vsergeev
 */

// 0 - forTotalRow - костыль
// 1 - number      - № пп
// 2 - kind        - Вид расходов
// 3 - summ        - Сумма, включаемая в состав материальных расходов , (руб.)

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        checkRNU48_1()
        calc()
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicCheck()
        break
}

//// Кэши и константы

// Проверяемые на пустые значения атрибуты (графа )
@Field
def nonEmptyColumns = ['summ']

void checkCreation() {
    if (reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }
    formDataService.checkUnique(formData, logger)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalRow = getDataRow(dataRows, 'total')
    totalRow.summ = calcTotal(dataRows)

    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    for (def row : dataRows) {
        if (row.getAlias() != 'total') {
            // 1. Обязательность заполнения поля графы 1..3
            checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
        } else {
            //2. Проверка итоговых значений по всей форме
            if (row.summ != calcTotal(dataRows)) {
                logger.error('Итоговые значения рассчитаны неверно!')
            }
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    // сбросить строки
    dataRows.each { row ->
        row.summ = 0
    }
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source)?.allCached
                sourceDataRows.each { row ->
                    def curRow = getDataRow(dataRows, row.getAlias())
                    curRow.summ += (row.summ ?: 0)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

BigDecimal calcTotal(def dataRows) {
    def sum = 0
    dataRows.each { row ->
        sum += (row.getAlias() != 'total' ? (row.summ ?: 0) : 0)
    }
    return round(sum, 2)
}

def checkRNU48_1() {
    if (formData.kind == FormDataKind.PRIMARY && getFormDataRNU48_1() == null) {
        throw new ServiceException("Не найдены экземпляры «${formTemplateService.get(343).fullName}» за текущий отчетный период!")
    }
}

def getFormDataRNU48_1() {
    def form = formDataService.find(343, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    return (form != null && form.state == WorkflowState.ACCEPTED ? form : null)
}

// Округление
def BigDecimal round(BigDecimal value, def int precision = 2) {
    return value?.setScale(precision, RoundingMode.HALF_UP)
}