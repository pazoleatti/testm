/*
 * Отмена принятия
 * Вызывается при отмене принятия
 * Удаляет консолидированную нф при отмене последней формы источника
 */
formDataCompositionService.decompose(formDataDepartment.id, formData.formTemplateId, FormDataKind.SUMMARY)