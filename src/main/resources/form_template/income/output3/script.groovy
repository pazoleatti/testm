package form_template.income.output3

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import groovy.transform.Field

/**
 * Сумма налога, подлежащая уплате в бюджет, по данным налогоплательщика
 * formTemplateId=308
 *
 * http://conf.aplana.com/pages/viewpage.action?pageId=8783216
 * http://conf.aplana.com/pages/viewpage.action?pageId=8784122
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        if (formData.kind != FormDataKind.ADDITIONAL) {
            logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
        }
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
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]

@Field
def editableColumns = ['paymentType', 'dateOfPayment', 'sumTax']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['paymentType', 'okatoCode', 'budgetClassificationCode', 'dateOfPayment', 'sumTax']

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    if (!dataRows.isEmpty()) {

        for (def row in dataRows) {
            // графа 2
            row.okatoCode = "45293554000"
            // графа 3
            def paymentType = getRefBookValue(24, row.paymentType)?.CODE?.stringValue
            if ('1'.equals(paymentType)) {
                row.budgetClassificationCode = '18210101040011000110'
            } else if ('3'.equals(paymentType)) {
                row.budgetClassificationCode = '18210101070011000110'
            } else if ('4'.equals(paymentType)) {
                row.budgetClassificationCode = '18210101060011000110'
            }
        }
        dataRowHelper.update(dataRows);
    }
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (def row in dataRows) {
        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }
}