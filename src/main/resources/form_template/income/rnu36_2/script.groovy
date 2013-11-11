package form_template.income.rnu36_2

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Форма "Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 2"
 *
 * @author bkinzyabulatov
 *
 * Графа 1  amount
 * Графа 2  percIncome
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        consolidation()
        logicCheck()
        break
}

@Field
def allColumns = ["amount", "percIncome"]

//// Кастомные методы

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    // если нф консолидированная, то не надо проверять данные из рну 36.1
    def dataRowHelper36_1
    def dataRows36_1
    if (formData.kind != FormDataKind.CONSOLIDATED) {
        def formData36_1 = formDataService.find(333, formData.kind, formData.departmentId, formData.reportPeriodId)
        if (formData36_1 != null && formData36_1.state != WorkflowState.ACCEPTED){
            formData36_1 = null
        }
        dataRowHelper36_1 = formDataService.getDataRowHelper(formData36_1)
        dataRows36_1 = dataRowHelper36_1.allCached
    }
    for (def DataRow row : dataRows){
        if (!row?.getAlias()?.contains('total')) {
            continue
        }
        ('total' != row?.getAlias()) ?
            checkNonEmptyColumns(row, row.getIndex(), allColumns, logger, true) :
            checkNonEmptyColumns(row, row.getIndex(), ["percIncome"], logger, true)
        // если нф консолидированная, то не надо проверять данные из рну 36.1
        if (formData.kind == FormDataKind.CONSOLIDATED) {
            return
        }
        def row36_1 = dataRowHelper36_1.getDataRow(dataRows36_1, row.getAlias())
        def rowStart = "Строка ${row.getIndex()}: "
        if ('total' != row?.getAlias()) {
            allColumns.each { alias ->
                if (row[alias] != row36_1[alias]){
                    logger.error("${rowStart}Неверно рассчитано значение графы \"${getColumnName(row,alias)}\"")
                }
            }
        } else {
            def totalRowA = dataRowHelper.getDataRow(dataRows, 'totalA')
            def totalRowB = dataRowHelper.getDataRow(dataRows, 'totalB')
            if (row.percIncome != totalRowA.percIncome - totalRowB.percIncome) {
                logger.error("${rowStart}Неверно рассчитано итоговое значение графы \"${getColumnName(row,alias)}\"")
            }
        }
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRowA = dataRowHelper.getDataRow(dataRows, 'totalA')
    def totalRowB = dataRowHelper.getDataRow(dataRows, 'totalB')
    def totalRow = dataRowHelper.getDataRow(dataRows, 'total')
    //очистка значений
    allColumns.each{
        totalRowA[it] = 0
        totalRowB[it] = 0
    }
    totalRow.percIncome = 0
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            def sourceData = formDataService.getDataRowHelper(source)
            def sourceRows = sourceData.allCached
            if (it.formTypeId == formData.getFormType().getId()) {
                def totalRowASource = sourceData.getDataRow(sourceRows, 'totalA')
                totalRowA.amount += totalRowASource.amount
                totalRowA.percIncome += totalRowASource.percIncome
                def totalRowBSource = sourceData.getDataRow(sourceRows, 'totalB')
                totalRowB.amount += totalRowBSource.amount
                totalRowB.percIncome += totalRowBSource.percIncome
                totalRow.percIncome += (totalRowA.percIncome - totalRowB.percIncome)
            }
        }
    }
    dataRowHelper.save(dataRows)
    // Консолидация данных из первичной рну-36.2 в консолидированную рну-36.2.
    if (formData.kind == FormDataKind.CONSOLIDATED) {
        logger.info('Формирование консолидированной формы прошло успешно.')
    } else {
        // Консолидация данных из первичной рну-36.1 в первичную рну-36.2.
        logger.info('Формирование первичной формы РНУ-36.2 прошло успешно.')
    }
}

