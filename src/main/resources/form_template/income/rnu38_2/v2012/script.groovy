package form_template.income.rnu38_2.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Форма "РНУ-38.2 Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 2".
 * formTemplateId=335
 *
 * @author rtimerbaev
 */

// графа 1  - amount
// графа 2  - incomePrev
// графа 3  - incomeShortPosition
// графа 4  - totalPercIncome

@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkSourceAccepted()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        checkSourceAccepted()
        if (!isConsolidated) {
            calc()
            formDataService.saveCachedDataRows(formData, logger)
        }
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        checkSourceAccepted()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        // расчет после консолидации не нужен
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
        break
}

// Все атрибуты
@Field
def allColumns = ['amount', 'incomePrev', 'incomeShortPosition', 'totalPercIncome']

@Field
def taxPeriod = null

@Field
def sourceFormData = null

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def totalRow = getDataRow(dataRows, 'total')

    //очистка значений
    allColumns.each {
        totalRow[it] = 0
    }

    // получение данных из первичной рну-36.1
    def dataRowsFromSource = getDataRowsFromSource()
    if (!dataRowsFromSource) {
        return
    }

    def totalRowSource = getDataRow(dataRowsFromSource, 'total')
    allColumns.each {
        totalRow[it] = totalRowSource[it]
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def totalRow = getDataRow(dataRows, 'total')

    // . Обязательность заполнения поля графы 1..4
    checkNonEmptyColumns(totalRow, totalRow.getIndex(), allColumns, logger, true)

    if (isConsolidated) {
        return
    }

    // 1. Проверка соответствия значениям формы РНУ-38.1
    def dataRowsFromSource = getDataRowsFromSource()
    def totalRowSource = getDataRow(dataRowsFromSource, 'total')
    if (isDiffRow(totalRow, totalRowSource, allColumns)) {
        logger.error('Значения не соответствуют данным РНУ-38.1')
    }
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def totalRow = getDataRow(dataRows, 'total')

    //очистка значений
    allColumns.each {
        totalRow[it] = 0
    }

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            def sourceRows = formDataService.getDataRowHelper(source).allSaved
            def totalRowSource = getDataRow(sourceRows, 'total')
            allColumns.each {
                totalRow[it] += totalRowSource[it]
            }
        }
    }
    updateIndexes(dataRows)
}

def getTaxPeriod() {
    if (taxPeriod == null) {
        taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod
    }
    return taxPeriod
}

/** Получить данные формы РНУ-38.1 (id = 334) */
def getFormDataSource() {
    if (sourceFormData == null) {
        sourceFormData = formDataService.getLast(334, formData.kind, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
    }
    return sourceFormData
}

void checkSourceAccepted() {
    if (!isConsolidated) {
        formDataService.checkMonthlyFormExistAndAccepted(334, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder, false, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

/** Получить строки из нф РНУ-38.1. */
def getDataRowsFromSource() {
    def formDataSource = getFormDataSource()
    if (formDataSource != null) {
        return formDataService.getDataRowHelper(formDataSource)?.allSaved
    }
    return null
}