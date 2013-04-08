import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * ЧТЗ выходные налоговые формы Ф2 Э1-2 П6.3.1.9.1	Алгоритмы заполнения полей формы
 * Применяется для каждой строки
 * @author ekuvshinov
 * @since 11.02.2013
 */
//com.aplana.sbrf.taxaccounting.log.Logger logger
//com.aplana.sbrf.taxaccounting.model.DataRow row
//com.aplana.sbrf.taxaccounting.dao.script.FormDataService formDataService
//com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService ReportPeriodService

if (!logger.containsLevel(LogLevel.ERROR)) {
//noinspection GroovyVariableNotAssigned
    row.dividendType = '2'
    row.taxPeriod = '34'
    row.dividendRussianMembersAll = row.dividendSumRaspredPeriod - row.dividendForgeinOrgAll - row.dividendForgeinPersonalAll
    row.dividendSumForTaxAll = (row.dividendRussianMembersAll ?: 0) - (row.dividendAgentWithStavka0 ?: 0)
    row.dividendSumForTaxStavka9 = (row.dividendRussianOrgStavka9 ?: 0)
    row.dividendSumForTaxStavka0 = (row.dividendRussianOrgStavka0 ?: 0)

    // Подсчёт поля 22 Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды
    def period = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def result = 0

    if (period != null) {
        formPrev = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, period.id)
        if (formPrev != null) {
            for (rowPrev in formPrev.dataRows) {
                if (rowPrev.financialYear == row.financialYear) {
                    result += rowPrev.taxSumFromPeriod + rowPrev.taxSumFromPeriodAll
                }
            }
        }
    }
    row.taxSumFromPeriod = result

} else {
    logger.error('Не могу заполнить поля, есть ошибки')
}