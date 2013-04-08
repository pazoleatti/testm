package groovy
/*
 * Проверяет уникальность в отчётном периоде и вид
 */

def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

if (findForm != null) {
    logger.error('Налоговая форма с заданными параметрами уже существует.')
}

if (formData.kind != FormDataKind.ADDITIONAL) {
    logger.error('Нельзя создавать форму с типом ${formData.kind?.name}')
}