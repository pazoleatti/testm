package form_template.income.rnu38_2

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Форма "РНУ-38.2 Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 2".
 * formTemplateId=335
 *
 * TODO:
 *      - импорт и миграция
 *
 * @author rtimerbaev
 */

// графа 1  - amount
// графа 2  - incomePrev
// графа 3  - incomeShortPosition
// графа 4  - totalPercIncome

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        logicCheck()
        break
}

// Все атрибуты
@Field
def allColumns = ['amount', 'incomePrev', 'incomeShortPosition', 'totalPercIncome']

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    if (dataRows.isEmpty()) {
        return
    }
    def row = dataRows.get(0)

    // 1. Обязательность заполнения полей (графа 1..4)
    checkNonEmptyColumns(row, 1, allColumns, logger, true)

    // если нф консолидированная, то не надо проверять данные из рну 38.1
    if (formData.kind == FormDataKind.CONSOLIDATED) {
        return
    }

    def totalRow = getTotalRowFromRNU38_1()
    if (totalRow == null) {
        logger.error('Отсутствует РНУ-38.1.')
        return
    }

    // 2, 3, 4, 5. Арифметическая проверка (графа 1..4)
    def needValue = [:]
    allColumns.each { alias ->
        needValue[alias] = totalRow.getCell(alias).value
    }
    checkCalc(row, allColumns, needValue, logger, false)
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить все строки и собрать из источников их строки
    dataRows.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            def sourceDataRowHelper = formDataService.getDataRowHelper(source)
            def sourceDataRows = sourceDataRowHelper.allCached
            if (it.formTypeId == formData.getFormType().getId()) {
                // Консолидация данных из первичной рну-38.2 в консолидированную рну-38.2.
                sourceDataRows.each { row ->
                    dataRows.add(row)
                }
            } else {
                // Консолидация данных из первичной рну-38.1 в первичную рну-38.2.
                def totalRow = getDataRow(sourceDataRows, 'total')
                def newRow = formData.createDataRow()
                // графа 1..4
                newRow.amount = totalRow.amount
                newRow.incomePrev = totalRow.incomePrev
                newRow.incomeShortPosition = totalRow.incomeShortPosition
                newRow.totalPercIncome = newRow.incomePrev + newRow.incomeShortPosition
                dataRows.add(newRow)
            }
        }
    }
    dataRowHelper.save(dataRows)
    if (formData.kind == FormDataKind.CONSOLIDATED) {
        logger.info('Формирование консолидированной формы прошло успешно.')
    } else {
        logger.info('Формирование первичной формы РНУ-38.2 прошло успешно.')
    }
}

/** Получить итоговую строку из нф (РНУ-38.1) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 1. */
def getTotalRowFromRNU38_1() {
    def formDataRNU_38_1 = formDataService.find(334, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formDataRNU_38_1 != null) {
        def dataRows = formDataService.getDataRowHelper(formDataRNU_38_1)?.allCached
        return getDataRow(dataRows, 'total')
    }
    return null
}