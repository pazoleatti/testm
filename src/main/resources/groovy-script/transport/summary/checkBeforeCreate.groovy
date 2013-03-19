/**
 * Скрипт для проверки создания.
 * Форма "Расчет суммы налога по каждому транспортному средству".
 *
 * @since 15.02.2013 18:20
 */

def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

if (findForm != null) {
    logger.error('Налоговая форма с заданными параметрами уже существует.');
}

if (formData.kind != FormDataKind.SUMMARY) {
    logger.error("Нельзя создавать форму с типом ${formData.kind?.name}");
}

if (formDataDepartment.type == DepartmentType.ROOT_BANK) {
    logger.error('Нельзя создавать форму на уровне банка.')
}