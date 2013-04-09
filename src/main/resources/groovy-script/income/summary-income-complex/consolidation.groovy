import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.service.script.DepartmentFormTypeService

//com.aplana.sbrf.taxaccounting.log.Logger logger
//com.aplana.sbrf.taxaccounting.service.script.FormDataService FormDataService
//com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService reportPeriodService
//com.aplana.sbrf.taxaccounting.model.FormData formData
//com.aplana.sbrf.taxaccounting.service.script.DepartmentService departmentService
//com.aplana.sbrf.taxaccounting.service.script.dictionary.DictionaryRegionService dictionaryRegionService
/* Конец условия. */

/*
 * 6.2.1.8.2	Алгоритмы консолидации данных сводных форм обособленных подразделений (consolidation.groovy).
 *
 * @author auldanov
 * @since 14.02.2013 11:30
 */

/*
 * В цикле по всем формам-источникам с типом «Сводная форма "Расшифровка видов доходов, учитываемых в простых РНУ" уровня обособленного подразделения»
 * расчитать данные для текущей формы путем складывания значений редактируемых ячеек.
 */

// Обнулим данные в связи http://jira.aplana.com/browse/SBRFACCTAX-1861
for(row in formData.dataRows ) {
    ['incomeBuhSumAccepted', 'incomeBuhSumPrevTaxPeriod', 'incomeTaxSumS'].each {
        row.getCell(it).setValue(null);
    }
}

departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
    def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
    if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == 302) {
        child.getDataRows().eachWithIndex() { row, i ->
            def rowResult = formData.getDataRows().get(i)
            /*
                *	Для каждой ячейки граф с 1 по 10, обозначенной либо как редактируемая,
                *	либо как расчетная, Система должна выполнять консолидацию данных из форм-источников.
                *	(6, 7, 9)
                */
            ['incomeBuhSumAccepted', 'incomeBuhSumPrevTaxPeriod', 'incomeTaxSumS'].each {
                if (row.getCell(it).getValue() != null) {
                    rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                }
            }
        }
    }
}
logger.info('Формирование сводной формы уровня Банка прошло успешно.')