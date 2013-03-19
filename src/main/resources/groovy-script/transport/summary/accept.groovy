/**
 * Принятие
 * Скрипт для перевода сводной налогой формы в статус "принят".
 * Форма "Расчет суммы налога по каждому транспортному средству".
 *
 * @author rtimerbaev
 * @since 22.02.2013 13:20
 */



departmentFormTypeService.getDestinations(formDataDepartment.id, formData.formTemplateId, FormDataKind.SUMMARY).each() {
    formDataCompositionService.compose(it.departmentId, it.formTypeId, it.kind, logger)
}