reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

formPrev = null
if (reportPeriodPrev != null) {
    formPrev = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodPrev.id)
}
if (formPrev == null) {
    logger.info('Форма за предыдущий отчётный период не создавалась!')
}