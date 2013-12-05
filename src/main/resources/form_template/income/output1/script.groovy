package form_template.income.output1

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 * Сведения для расчёта налога с доходов в виде дивидендов (доходов от долевого участия в других организациях,
 * созданных на территории Российской Федерации)
 * formTemplateId=306
 */

DataRowHelper getDataRowsHelper() {
    DataRowHelper dataRowsHelper = null
    if (formData.id != null) {
        dataRowsHelper = formDataService.getDataRowHelper(formData)
    }
    return dataRowsHelper
}

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        break
    case FormDataEvent.CALCULATE:
        logicalCheck()
        logicCheck()
        calc()
        dataRowsHelper.save(dataRowsHelper.getAllCached());
        break
    case FormDataEvent.CHECK:
        logicalCheck()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        checkDecl()
        logicalCheck()
        logicCheck()
        dataRowsHelper.save(dataRowsHelper.getAllCached());
        break
    case FormDataEvent.MOVE_PREPARED_TO_CREATED:
        break
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        checkDecl()
        logicalCheck()
        logicCheck()
        dataRowsHelper.save(dataRowsHelper.getAllCached());
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:
        checkDecl()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
}

void deleteRow() {
    if (currentDataRow != null) {
        dataRowsHelper.delete(currentDataRow)
    }
}

void addRow() {
    row = formData.createDataRow()
    row.taxPeriod = '34'
    row.dividendType = '2'
    for (alias in ['financialYear', 'dividendSumRaspredPeriod', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll',
            'dividendStavka0', 'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendRussianOrgStavka9',
            'dividendRussianOrgStavka0', 'dividendPersonRussia', 'dividendMembersNotRussianTax', 'dividendAgentAll',
            'dividendAgentWithStavka0', 'taxSum', 'taxSumFromPeriodAll']) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }
    dataRowsHelper.insert(row, dataRowsHelper.getAllCached().size() + 1)

}

/**
 * Проверяет уникальность в отчётном периоде и вид
 */
void checkUniq() {
    FormData findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
    if (formData.kind != FormDataKind.ADDITIONAL) {
        logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
    }
}

/**
 * Проверка наличия декларации для текущего department
 */
void checkDecl() {
    declarationType = 2;    // Тип декларации которую проверяем(Налог на прибыль)
    declaration = declarationService.find(declarationType, formData.getDepartmentId(), formData.getReportPeriodId())
    if (declaration != null && declaration.isAccepted()) {
        logger.error("Декларация банка находиться в статусе принята")
    }
}

void logicalCheck() {
    reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    formPrev = null
    if (reportPeriodPrev != null) {
        formPrev = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodPrev.id)
    }
    if (formPrev == null) {
        logger.warn('Форма за предыдущий отчётный период не создавалась!')
    }
}

/**
 * Проверка полей которые обязательно надо заполнить пользователю
 */
void logicCheck() {
    def int index = 0
    for (row in dataRowsHelper.getAllCached()) {
        index++
        for (alias in ['financialYear', 'dividendSumRaspredPeriod', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll',
                'dividendStavka0', 'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10',
                'dividendRussianOrgStavka9', 'dividendRussianOrgStavka0', 'dividendPersonRussia',
                'dividendMembersNotRussianTax', 'dividendAgentAll', 'dividendAgentWithStavka0', 'taxSum', 'taxSumFromPeriodAll'
        ]) {
            if (row.getCell(alias).value == null) {
                def msg = row.getCell(alias).column.name.replace('%', '%%')
                logger.error("Строка $index: поле «$msg» не заполнено!")
            }
        }
    }
}

void calc() {
    if (!logger.containsLevel(LogLevel.ERROR)) {
        for (row in dataRowsHelper.getAllCached()) {
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
                    for (rowPrev in formDataService.getDataRowHelper(formPrev).getAll()) {
                        if (rowPrev.financialYear.format('yyyy') == row.financialYear.format('yyyy')) {
                            result += rowPrev.taxSumFromPeriod + rowPrev.taxSumFromPeriodAll
                        }
                    }
                }
            }
            row.taxSumFromPeriod = result
        }
    } else {
        logger.error('Не могу заполнить поля, есть ошибки')
    }
}