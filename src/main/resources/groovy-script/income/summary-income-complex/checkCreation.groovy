/**
 * Скрипт для проверки создания (checkCreation.groovy).
 * Форма "Сводная форма начисленных доходов (доходы сложные)".
 *
 * @author rtimerbaev
 * @since 21.02.2013 12:30
 */

def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

if (findForm != null) {
    logger.error('Налоговая форма с заданными параметрами уже существует.')
}

if (formData.kind != FormDataKind.SUMMARY) {
    logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
}