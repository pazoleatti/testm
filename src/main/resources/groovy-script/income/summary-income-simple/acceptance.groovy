/**
 * Скрипт для перевода сводной налогой формы в статус "принят" (acceptance.groovy).
 * Форма "Расшифровка видов доходов, учитываемых в простых РНУ (доходы простые)".
 *
 * @author rtimerbaev
 * @since 07.02.2013 13:10
 */

departmentFormTypeService.getDestinations(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each{
    formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
}