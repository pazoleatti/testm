/*
 * Проверка при "отменить принятие"
 */
if (declarationService.find(1, formData.departmentId, formData.reportPeriodId) != null){
    logger.error("Отмена принятия сводной налоговой формы невозможно, т.к. уже подготовлена декларация.")
}