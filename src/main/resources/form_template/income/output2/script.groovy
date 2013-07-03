import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * 6.3.2    ������ ������ �� ������� � �������, ������������� ��������� �������
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        checkDecl()
        break
    case FormDataEvent.CALCULATE:
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        checkDecl()
        logicCheck()
        break
    case FormDataEvent.MOVE_PREPARED_TO_CREATED:
        break
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        checkDecl()
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

void deleteRow() {
    // @todo ������ indexOf ����� http://jira.aplana.com/browse/SBRFACCTAX-2702
    if (currentDataRow != null && formData.dataRows.indexOf(currentDataRow) != -1) {
        formData.dataRows.remove(currentDataRow)
    }
}

void addRow() {
    row = formData.createDataRow()
    for (alias in ['title', 'zipCode', 'subdivisionRF', 'area', 'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment',
            'surname', 'name', 'patronymic', 'phone', 'dividendDate', 'sumDividend', 'sumTax']) {
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

void logicCheck() {
    for (row in formData.dataRows) {

        for (alias in ['title', 'subdivisionRF', 'surname', 'name', 'dividendDate', 'sumDividend', 'sumTax']) {
            if (row.getCell(alias).value == null) {
                logger.error('���� ' + row.getCell(alias).column.name.replace('%', '') + ' �� ���������')
            }
        }

        String zipCode = (String) row.zipCode;
        if (zipCode == null || zipCode.length() != 6 || !zipCode.matches('[0-9]*')) {
            logger.error('����������� ������ �������� ������!')
        }
        if (!logger.containsLevel(LogLevel.ERROR)) {
            if (!dictionaryRegionService.isValidCode((String) row.subdivisionRF)) {
                logger.error('�������� ������������ �������� ��!')
            }
        }
    }
}