/*
* Условие
 */
// проверка на банк
boolean isBank = true
departmentFormTypeService.getDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
    if (it.departmentId != formData.departmentId) {
        isBank = false
    }
}
return isBank


/**
 * Скрипт для консолидации.
 * Форма "Расчет суммы налога по каждому транспортному средству".
 *
 * @author rtimerbaev
 * @since 22.02.2013 13:20
 */

// Удалить все строки
formData.dataRows.clear()

// получить источники
departmentFormTypeService.getSources(formDataDepartment.id, formData.formTemplateId, FormDataKind.SUMMARY).each {
    def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
    if (child != null && child.state == WorkflowState.ACCEPTED) {
        // скопировать строки форм в текущую форму
        child.dataRows.each { row->
            if (row.getAlias() != 'total') {
                formData.appendDataRow().putAll(row);
            }
        }
    }
}
logger.info('Формирование сводной формы уровня Банка прошло успешно.')