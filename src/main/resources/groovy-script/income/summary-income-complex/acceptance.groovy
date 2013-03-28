/**
 * Скрипт для перевода сводной налогой формы в статус "принят" (acceptance.groovy).
 * Форма "Сводная форма начисленных доходов (доходы сложные)".
 *
 * @author rtimerbaev
 * @since 21.02.2013 18:10
 */

departmentFormTypeService.getDestinations(formDataDepartment.id, formData.formTemplateId, FormDataKind.SUMMARY).each{
    formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
}