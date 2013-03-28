/**
 * Скрипт для перевода сводной налогой формы в статус "принят" (acceptance.groovy).
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * @author rtimerbaev
 * @since 22.02.2013 12:50
 */

departmentFormTypeService.getDestinations(formDataDepartment.id, formData.formTemplateId, FormDataKind.SUMMARY).each() {
    formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
}