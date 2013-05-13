formIncomeComplex = null
formIncomeComplexId = 302   // Сводная форма начисленных доходов (доходы сложные)
formIncomeComplex = FormDataService.find(formIncomeComplexId, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
reportPeriod = reportPeriodService.get(formData.reportPeriodId)
reportPeriodPrev = reportPeriodService.getPrevReportPeriod(reportPeriod.id)

formPrev = null
if (reportPeriodPrev != null) {
    formPrev = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodPrev.id)
}
if (formPrev == null) {
    logger.info('Форма за предыдущий отчётный период не создавалась!')
}
if (formIncomeComplex == null) {
    logger.info('Сводная форма начисленных доходов за текущий отчётный период не создавалась!')
}
