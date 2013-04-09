/**
 * Скрипт для перевода сводной налогой формы в статус "принят" (acceptance.groovy).
 * Форма "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)".
 *
 * @author rtimerbaev
 * @since 13.02.2013 12:00
 */

departmentFormTypeService.getDestinations(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each() {
    formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
}