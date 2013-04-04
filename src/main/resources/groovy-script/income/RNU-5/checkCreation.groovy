/**
 * Скрипт для проверки создания (checkCreation.groovy).
 * Форма "(РНУ-5) Простой регистр налогового учёта «расходы»".
 *
 * @author rtimerbaev
 */

def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

if (findForm != null) {
    logger.error('Налоговая форма с заданными параметрами уже существует.');
}

if (formData.kind != FormDataKind.PRIMARY) {
    logger.error("Нельзя создавать форму с типом ${formData.kind?.name}");
}

// if (formDataDepartment.type == DepartmentType.ROOT_BANK) {
//     logger.error('Нельзя создавать форму на уровне банка.')
// }