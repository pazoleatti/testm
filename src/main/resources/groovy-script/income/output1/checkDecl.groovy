/**
 * Проверка наличия декларации для текущего department
 * @autor EKuvshinov
 */

declarationType = 2;    // Тип декларации которую проверяем(Налог на прибыль)
declaration = declarationService.find(declarationType, formData.getDepartmentId(), formData.getReportPeriodId())
if (declaration != null || declaration.isAccepted()) {
    logger.error("Декларация по налогу на прибыль не принята")
}