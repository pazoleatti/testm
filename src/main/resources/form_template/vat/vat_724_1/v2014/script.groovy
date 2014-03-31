package form_template.vat.vat_724_1.v2014.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Отчёт о суммах начисленного НДС по операциям Банка
 * formTemplateId=600
 */
// 1 rowNum
// 2 baseAccName
// 3 baseAccNum
// 4 baseSum
// 5 ndsNum
// 6 ndsSum
// 7 ndsRate
// 8 ndsBookSum
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        break
    case FormDataEvent.CHECK:
        break
    case FormDataEvent.ADD_ROW:
        addRow()
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
        break
    case FormDataEvent.COMPOSE:
        break
}

// Редактируемые атрибуты
@Field
def editableColumns = ['baseAccNum', 'baseSum', 'ndsNum', 'ndsSum', 'ndsRate', 'ndsBookSum']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'baseAccName']

// Добавить новую строку (строки между заглавными строками и строками итогов)
def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def newRow = getNewRow()
    def index = 0
    if (currentDataRow != null) {
        def alias = currentDataRow.getAlias()
        index = currentDataRow.getIndex() - 1
        if (alias == null || alias.startsWith('head_')) {
            index++
        }
    } else {
        def lastRow = getDataRow(dataRows, 'total_7')
        if (lastRow != null) {
            index = dataRows.indexOf(lastRow)
        }
    }
    dataRowHelper.insert(newRow, index + 1)
}

// Получить новую строку с заданными стилями
def getNewRow() {
    def row = formData.createDataRow()
    editableColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return row
}