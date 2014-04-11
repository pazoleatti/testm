package form_template.income.rnu36_2.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Форма "Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 2"
 * formTemplateId=315
 *
 * @author bkinzyabulatov
 */

// графа 1 - amount
// графа 2 - percIncome

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
    case FormDataEvent.COMPOSE: // Консолидация
        consolidation()
        // расчет после консолидации не нужен
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
        break
}

@Field
def allColumns = ["amount", "percIncome"]

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def taxPeriod = null

@Field
def sourceFormData = null

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def currentFixedRowsMap = getFixedRows(dataRows)
    def totalRowA = currentFixedRowsMap['totalA']
    def totalRowB = currentFixedRowsMap['totalB']
    def totalRow = currentFixedRowsMap['total']

    //очистка значений
    allColumns.each {
        totalRowA[it] = 0
        totalRowB[it] = 0
    }
    totalRow.percIncome = 0

    // получение данных из первичной рну-36.1
    def dataRowsFromSource = getDataRowsFromSource()
    if (!dataRowsFromSource) {
        dataRowHelper.save(dataRows)
        return
    }

    def sourceFixedRowsMap = getFixedRows(dataRowsFromSource)
    def totalRowASource = sourceFixedRowsMap['totalA']
    def totalRowBSource = sourceFixedRowsMap['totalB']
    def totalRowSource = sourceFixedRowsMap['total']

    // раздел А
    totalRowA.amount = totalRowASource.amount
    totalRowA.percIncome = totalRowASource.percIncome

    // раздел Б
    totalRowB.amount = totalRowBSource.amount
    totalRowB.percIncome = totalRowBSource.percIncome

    // Итоги
    totalRow.percIncome = totalRowSource.percIncome

    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // . Обязательность заполнения поля графы 1..6
    for (def row : dataRows) {
        if (!row.getAlias()?.contains('total')) {
            continue
        }
        def columns = ('total' != row.getAlias() ? allColumns : ['percIncome'])
        checkNonEmptyColumns(row, row.getIndex(), columns, logger, true)
    }

    if (isConsolidated) {
        return
    }

    // . Арифметическая проверка графы 1, 2
    def dataRowsFromSource = getDataRowsFromSource()

    def currentFixedRowsMap = getFixedRows(dataRows)
    def totalRowA = currentFixedRowsMap['totalA']
    def totalRowB = currentFixedRowsMap['totalB']
    def totalRow = currentFixedRowsMap['total']

    def sourceFixedRowsMap = getFixedRows(dataRowsFromSource)
    def totalRowASource = sourceFixedRowsMap['totalA']
    def totalRowBSource = sourceFixedRowsMap['totalB']
    def totalRowSource = sourceFixedRowsMap['total']

    // раздел А, Б
    checkValues(totalRowA, totalRowASource, allColumns)
    checkValues(totalRowB, totalRowBSource, allColumns)
    // итого
    checkValues(totalRow, totalRowSource, ['percIncome'])
}

// Получить мапу с итоговыми и подитоговыми строками (доступ по алиасу)
def getFixedRows(def dataRows) {
    def map = [:]
    for (def row : dataRows) {
        if (row.getAlias() == 'totalA') {
            map.put('totalA', row)
        } else if (row.getAlias() == 'totalB') {
            map.put('totalB', row)
        } else if (row.getAlias() == 'total') {
            map.put('total', row)
        }
    }
    return map
}

void checkValues(def totalRow, totalRowSource, def columnAliases) {
    def msg = []
    columnAliases.each { alias ->
        if (totalRowSource[alias] != totalRow[alias]) {
            msg.add('«' + getColumnName(totalRow, alias) + '»')
        }
    }
    if (!msg.isEmpty()) {
        def index = totalRow.getIndex()
        def columns = msg.join(', ')
        logger.error("Строка $index: Неверное значение граф: $columns")
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def currentFixedRowsMap = getFixedRows(dataRows)
    def totalRowA = currentFixedRowsMap['totalA']
    def totalRowB = currentFixedRowsMap['totalB']
    def totalRow = currentFixedRowsMap['total']

    //очистка значений
    allColumns.each {
        totalRowA[it] = 0
        totalRowB[it] = 0
    }
    totalRow.percIncome = 0

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        def source = formDataService.findMonth(it.formTypeId, it.kind, it.departmentId, getTaxPeriod()?.id, formData.periodOrder)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            def dataRowsFromSource = formDataService.getDataRowHelper(source).allCached

            def sourceFixedRowsMap = getFixedRows(dataRowsFromSource)
            def totalRowASource = sourceFixedRowsMap['totalA']
            def totalRowBSource = sourceFixedRowsMap['totalB']
            def totalRowSource = sourceFixedRowsMap['total']

            // раздел А
            totalRowA.amount += totalRowASource.amount
            totalRowA.percIncome += totalRowASource.percIncome

            // раздел Б
            totalRowB.amount += totalRowBSource.amount
            totalRowB.percIncome += totalRowBSource.percIncome

            // Итоги
            totalRow.percIncome += totalRowSource.percIncome
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder)?.time
    }
    return endDate
}

def getTaxPeriod() {
    if (taxPeriod == null) {
        taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod
    }
    return taxPeriod
}

/** Получить данные формы РНУ-36.1 (id = 333) */
def getFormDataSource() {
    if (sourceFormData == null) {
        sourceFormData = formDataService.findMonth(333, formData.kind, formDataDepartment.id, getTaxPeriod()?.id, formData.periodOrder)
    }
    return sourceFormData
}

void checkSourceAccepted() {
    if (isConsolidated) {
        return
    }
    def form = getFormDataSource()
    if (form == null || form.state != WorkflowState.ACCEPTED) {
        throw new ServiceException('Не найдены экземпляры РНУ-36.1 за текущий отчетный период!')
    }
}

/** Получить строки из нф РНУ-36.1. */
def getDataRowsFromSource() {
    def formDataSource = getFormDataSource()
    if (formDataSource != null) {
        return formDataService.getDataRowHelper(formDataSource)?.allCached
    }
    return null
}