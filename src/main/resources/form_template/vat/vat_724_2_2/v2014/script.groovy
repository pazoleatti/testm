package form_template.vat.vat_724_2_2.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * 6.3	(724.2.2) Расчёт суммы налога по операциям по реализации товаров (работ, услуг), обоснованность применения
 * налоговой ставки 0 процентов по которым документально подтверждена
 *
 * formTemplateId=602
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
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
}

// 1 № пп  -  rowNum
// 2 Код операции  -  code
// 3 Наименование операции  -  name
// 4 Налоговая база  -  base

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['base']

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def itog = dataRowHelper.getDataRow(dataRows, 'itog')
    itog?.base = calcItog(dataRows)
    dataRowHelper.update(dataRows);
}

// Расчет итога
def calcItog(def dataRows) {
    def sum = 0 as BigDecimal
    for (def row in dataRows) {
        if (row.getAlias() != 'itog') {
            sum += row.base == null ? 0 : row.base
        }
    }
    return sum
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    for (def row in dataRows) {
        def index = row.getIndex()
        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, index ?: 0, nonEmptyColumns, logger, true)
    }
    // 2. Проверка итоговых значений
    def itog = dataRowHelper.getDataRow(dataRows, 'itog')
    if (itog.base != calcItog(dataRows)) {
        logger.error(WRONG_TOTAL, getColumnName(itog, 'base'))
    }
}

// Консолидация
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.getFormType().getTaxType() == TaxType.VAT) {
            formDataService.getDataRowHelper(source).getAllCached().each { srcRow ->
                if (srcRow.getAlias() != null && !srcRow.getAlias().equals('itog')) {
                    def row = dataRowHelper.getDataRow(dataRows, srcRow.getAlias())
                    row.base = (row.base ?: 0) + (srcRow.base ?: 0)
                }
            }
        }
    }
    dataRowHelper.update(dataRows)
}