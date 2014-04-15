package form_template.vat.vat_724_6.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 *  6.5	(724.6)  Отчёт о суммах НДС начисленных налоговым агентом с сумм дохода иностранных юридических лиц
 *  (балансовый счёт 60309.02)
 *
 *  formTemplateId=604
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
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
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow.getAlias() == null) formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
}

// Редактируемые атрибуты
@Field
def editableColumns = ['operDate', 'contragent', 'type', 'sum', 'number', 'sum2', 'date', 'number2']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = autoFillColumns + editableColumns

// Сумируемые колонки в фиксированной строке
@Field
def totalColumns = ['sum', 'sum2']

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    for (def row in dataRows) {
        def index = row.getIndex()
        if (row.getAlias() != 'total') {
            // Проверка заполнения граф
            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)
        }
    }
    // Проверка суммы НДС
    def totalRow = getDataRow(dataRows, 'total')
    if (totalRow.sum != null && totalRow.sum2 != null &&
            !(totalRow.sum2 > totalRow.sum * 0.15 && totalRow.sum2 < totalRow.sum * 0.21)) {
        logger.warn("Строка ${totalRow.getIndex()}: " +
                "Сумма НДС по данным бухгалтерского учета не соответствует налоговой базе!")
    }
    // Проверка итоговых значений
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = getDataRow(dataRows, 'total')
    deleteAllAliased(dataRows)
    def rowNum = 1
    for (def row in dataRows) {
        row.rowNum = rowNum++
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}