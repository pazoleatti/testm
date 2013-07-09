package form_template.income.output3

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind

/**
 * 6.3.3    Сумма налога, подлежащая уплате в бюджет, по данным налогоплательщика
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        checkDecl()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        checkDecl()
        calc()
        logicCheck()
        break
    case FormDataEvent.MOVE_PREPARED_TO_CREATED:
        break
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        checkDecl()
        calc()
        logicCheck()
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
}
void calc() {
    for(DataRow row in formData.dataRows) {
        row.okatoCode = "45293554000"
        if (row.paymentType == '1') {
            row.budgetClassificationCode = '18210101040011000110'
        }
        if (row.paymentType == '3') {
            row.budgetClassificationCode = '18210101070011000110'
        }
        if (row.paymentType == '4') {
            row.budgetClassificationCode = '18210101060011000110'
        }
    }
}

void deleteRow() {
    if (currentDataRow != null) {
        formData.dataRows.remove(currentDataRow)
    }
}

void addRow() {
    row = formData.createDataRow()
    for (alias in ['paymentType', 'dateOfPayment', 'sumTax']) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }
    formData.dataRows.add(row)
}
/**
 * Проверяет уникальность в отчётном периоде и вид
 */
void checkUniq() {

    FormData findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
    if (formData.kind != FormDataKind.ADDITIONAL) {
        logger.error('Нельзя создавать форму с типом ${formData.kind?.name}')
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

/**
 * Логические проверки
 */
void logicCheck() {
    for (row in formData.dataRows) {
        for (alias in ['paymentType', 'okatoCode', 'budgetClassificationCode', 'dateOfPayment', 'sumTax']) {
            if (row.getCell(alias).value == null) {
                logger.error('Поле ' + row.getCell(alias).column.name.replace('%', '') + ' не заполнено')
            }
        }
    }
}