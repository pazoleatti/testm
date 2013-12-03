package form_template.income.rnu48_2

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 * "(РНУ-48.2) Регистр налогового учёта «Сводная ведомость ввода в эксплуатацию инвентаря
 * и принадлежностей до 40 000 руб.»
 * formTemplateId=313
 *
 * Версия ЧТЗ: 64
 *
 * Вопросы аналитикам: http://jira.aplana.com/browse/SBRFACCTAX-2469
 *
 * TODO:
 *          -   добавить справочник в графу 2
 *
 * @author vsergeev
 *
 * Графы:
 * number   -   № пп
 * kind     -   Вид расходов
 * summ     -   Сумма, включаемая в состав материальных расходов , (руб.)
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheckWithTotalDataRowCheck()
        break
    case FormDataEvent.CALCULATE :
        if (logicalCheckWithoutTotalDataRowCheck()) {
            calc()
        }
        break
    case FormDataEvent.ADD_ROW :
        //addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        //deleteRow()
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        if (logicalCheckWithTotalDataRowCheck()) {
            // для сохранения изменений приемников
            data.commit()
        }
        break
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

void calc() {
    def totalRow = getTotalDataRow()
    totalRow.summ = getTotal()
    data.save(getRows(data))
}

boolean logicalCheckWithTotalDataRowCheck() {
    return (requiredColsFilled()) ?  totalRowCheck() : false
}

boolean logicalCheckWithoutTotalDataRowCheck() {
    return requiredColsFilled()
}

/**
 * 2.   Проверка итоговых значений по всей форме
 */
boolean totalRowCheck() {
    boolean isValid = true
    def totalRow = getTotalDataRow()
    if (isBlankOrNull(totalRow.summ) || ! totalRow.summ.equals(getTotal())) {
        isValid = false
        logger.error('Итоговые значения рассчитаны неверно!')
    }

    return isValid
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = data
    // сбросить строки
    for (def row : getRows(data)){
        row.summ = 0
    }

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row->
                    def curRow = data.getDataRow(getRows(data),row.getAlias())
                    curRow.summ += row.summ
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * 1.    Проверка на заполнение поля «<Наименование поля>»
 */
boolean requiredColsFilled() {
    def data = data
    boolean isValid = true
    def requiredRows = getRowsWithDataAliases()
    requiredRows.each {
        def dataRow = data.getDataRow(getRows(data),it)
        def fieldNumber = dataRow.number
        if (isBlankOrNull(dataRow.summ)) {
            isValid = false
            def name = dataRow.getCell('summ').getColumn().getName().replace('%', '%%')
            logger.error("В строке \"№ пп\" равной $fieldNumber поле \"$name\" не заполнено!")
        }
    }

    return isValid
}

BigDecimal getTotal() {
    def rowsForSumm = getRowsWithDataAliases()
    return getRows(data).sum() {dataRow ->
        if (dataRow.getAlias() in rowsForSumm) {
            return dataRow.summ
        } else {
            return 0
        }
    }
}

def getTotalDataRow() {
    def data = data
    return data.getDataRow(getRows(data),'R4')
}

List<String> getRowsWithDataAliases() {
    return ['R0', 'R1', 'R2', 'R3']
}

boolean isBlankOrNull(value) {
    value == null || value.equals('')
}

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def DataRowHelper getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

def DataRowHelper getData(){
    return getData(formData)
}

/**
 * Получить строки формы.
 *
 * @param formData форма
 */
def List<DataRow<Cell>> getRows(def DataRowHelper data) {
    return data.getAllCached()
}