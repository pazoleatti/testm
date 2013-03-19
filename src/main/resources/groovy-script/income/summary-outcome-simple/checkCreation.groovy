/**
 * Скрипт для проверки создания (checkCreation.groovy).
 * Форма "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)".
 *
 * @author rtimerbaev
 * @since 21.02.2013 13:40
 */

def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

if (findForm != null) {
    logger.error('Налоговая форма с заданными параметрами уже существует.')
}

if (formData.kind != FormDataKind.SUMMARY) {
    logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
}