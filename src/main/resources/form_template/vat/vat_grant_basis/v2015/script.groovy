package form_template.vat.vat_grant_basis.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Расшифровка операций по реализации товаров (работ, услуг) на безвозмездной основе (v.2015)
 * formTemplateId = 622
 */

// графа 1 - rowNum
// графа 2 - operName
// графа 3 - taxBase
// графа 4 - taxRate
// графа 5 - taxSum
// графа 6 - check
// графа 7 - comment

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    //TODO консолидация
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
}

@Field
def allColumns = ['rowNum', 'operName', 'taxBase', 'taxRate', 'taxSum', 'check', 'comment']

@Field
def editableColumns = ['operName', 'taxBase', 'taxSum', 'comment']

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'total').getIndex()
    } else {
        def alias = currentDataRow.getAlias()
        if (alias == null || !'total'.equals(alias)) {
            index = currentDataRow.getIndex() + 1
        } else {
            index = currentDataRow.getIndex()
        }
    }
    dataRowHelper.insert(getNewRow(), index)
}

// Получить новую строку с заданными стилями.
def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

void calc() {
    //TODO
}

void logicCheck() {
    //TODO
}

