import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * 6.3.1    �������� ��� ������� ������ � ������� � ���� ���������� (������� �� �������� ������� � ������ ������������, ��������� �� ���������� ���������� ���������)
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        checkDecl()
        break
    case FormDataEvent.CALCULATE:
        logicalCheck()
        logicCheck()
        calc()
        break
    case FormDataEvent.CHECK:
        logicalCheck()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        checkDecl()
        logicalCheck()
        logicCheck()
        calc()
        break
    case FormDataEvent.MOVE_PREPARED_TO_CREATED:
        break
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        checkDecl()
        logicalCheck()
        logicCheck()
        calc()
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
void deleteRow() {
    // @todo ������ indexOf ����� http://jira.aplana.com/browse/SBRFACCTAX-2702
    if (currentDataRow != null && formData.dataRows.indexOf(currentDataRow) != -1) {
        formData.dataRows.remove(currentDataRow)
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
        row.getCell(alias).setStyleAlias('�������������')
    }
    formData.dataRows.add(row)

}

/**
 * ��������� ������������ � �������� ������� � ���
 */
void checkUniq() {

    FormData findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('��������� ����� � ��������� ����������� ��� ����������.')
    }
    if (formData.kind != FormDataKind.ADDITIONAL) {
        logger.error('������ ��������� ����� � ����� ${formData.kind?.name}')
    }
}

/**
 * �������� ������� ���������� ��� �������� department
 */
void checkDecl() {
    declarationType = 2;    // ��� ���������� ������� ���������(����� �� �������)
    declaration = declarationService.find(declarationType, formData.getDepartmentId(), formData.getReportPeriodId())
    if (declaration != null && declaration.isAccepted()) {
        logger.error("���������� ����� ���������� � ������� �������")
    }
}

void logicalCheck() {
    reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    formPrev = null
    if (reportPeriodPrev != null) {
        formPrev = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodPrev.id)
    }
    if (formPrev == null) {
        logger.warn('����� �� ���������� �������� ������ �� �����������!')
    }
}

/**
 * �������� ����� ������� ����������� ���� ��������� ������������
 */
void logicCheck() {
    for (row in formData.dataRows) {
        for (alias in ['financialYear', 'dividendSumRaspredPeriod', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll',
                'dividendStavka0', 'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10',
                'dividendRussianOrgStavka9', 'dividendRussianOrgStavka0', 'dividendPersonRussia',
                'dividendMembersNotRussianTax', 'dividendAgentAll', 'dividendAgentWithStavka0', 'taxSum', 'taxSumFromPeriodAll'
        ]) {
            if (row.getCell(alias).value == null) {
                logger.error('���� ' + row.getCell(alias).column.name.replace('%', '') + ' �� ���������')
            }
        }
    }
}

void calc() {
    if (!logger.containsLevel(LogLevel.ERROR)) {
        for (row in formData.dataRows) {
            row.dividendType = '2'
            row.taxPeriod = '34'
            row.dividendRussianMembersAll = row.dividendSumRaspredPeriod - row.dividendForgeinOrgAll - row.dividendForgeinPersonalAll
            row.dividendSumForTaxAll = (row.dividendRussianMembersAll ?: 0) - (row.dividendAgentWithStavka0 ?: 0)
            row.dividendSumForTaxStavka9 = (row.dividendRussianOrgStavka9 ?: 0)
            row.dividendSumForTaxStavka0 = (row.dividendRussianOrgStavka0 ?: 0)

            // ������� ���� 22 ����� ������, ����������� � ����������, ����������� � ���������� �������� (���������) �������
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
        }
    } else {
        logger.error('�� ���� ��������� ����, ���� ������')
    }
}