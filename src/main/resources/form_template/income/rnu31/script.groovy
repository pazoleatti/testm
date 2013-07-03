/**
 * ������ ��� ���-31 (rnu31.groovy).
 * ����� "(���-31) ������� ���������� ����� ����������� ������ �� �������� ����������".
 *
 * @version 59
 *
 * TODO:
 *      - ��� �c����� � ��������� ������������ ��� (������ ��� ���� ������������)
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck(true)
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        break
    case FormDataEvent.ADD_ROW :
        // addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        // deleteRow()
        break
    // �������� ��� "������� �� ������� � ������������"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED :
        checkOnCancelAcceptance()
        break
    // ����� �������� �� ������������
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        logicalCheck(true)
        break
    // ��������
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheck(false)
        break
}

// ����� 1  - number
// ����� 2  - securitiesType
// ����� 3  - ofz
// ����� 4  - municipalBonds
// ����� 5  - governmentBonds
// ����� 6  - mortgageBonds
// ����� 7  - municipalBondsBefore
// ����� 8  - rtgageBondsBefore
// ����� 9  - ovgvz
// ����� 10 - eurobondsRF
// ����� 11 - itherEurobonds
// ����� 12 - corporateBonds

/**
 * �������� ����� ������.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)

    // ����� 3..12
    ['ofz', 'municipalBonds', 'governmentBonds  ', 'mortgageBonds',
            'municipalBondsBefore', 'rtgageBondsBefore', 'ovgvz',
            'eurobondsRF', 'itherEurobonds', 'corporateBonds'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('�������������')
    }
}

/**
 * ������� ������.
 */
def deleteRow() {
    formData.dataRows.remove(currentDataRow)
}

/**
 * �������. ��������� ���������� ����� �����.
 */
void calc() {
    /*
     * �������� ������������� �����.
     */

    // ������ ����������� �������� (����� 3..12)
    def requiredColumns = ['ofz', 'municipalBonds', 'governmentBonds',
            'mortgageBonds', 'municipalBondsBefore', 'rtgageBondsBefore',
            'ovgvz', 'eurobondsRF', 'itherEurobonds', 'corporateBonds']

    for (def row : formData.dataRows) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }
}

/**
 * ���������� ��������.
 *
 * @param useLog ����� �� ���������� � ��� ��������� � ��������������� ������������ �����
 */
def logicalCheck(def useLog) {
    // ������ ����������� ������
    def formDataOld = getFormDataOld()

    /** ������ �� ����������� ������. */
    def rowOld = (formDataOld != null && !formDataOld.dataRows.isEmpty() ? formDataOld.getDataRow('total') : null)

    /** ������ �� �������� ������. */
    def row = (formData != null && !formData.dataRows.isEmpty() ? formData.getDataRow('total') : null)
    if (row == null) {
        return true
    }

    // ������ ����������� �������� (����� 1..12)
    def requiredColumns = ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds',
            'municipalBondsBefore', 'rtgageBondsBefore', 'ovgvz', 'eurobondsRF',
            'itherEurobonds', 'corporateBonds']

    // 22. �������������� ���������� ����� ����� 1..12
    if (!checkRequiredColumns(row, requiredColumns, useLog)) {
        return false
    }

    // ����� ��� ������� ��� ������ ����������� (����� 5, 9, 10, 11)
    def warnColumns = ['governmentBonds', 'ovgvz', 'eurobondsRF', 'itherEurobonds']

    // TODO (Ramil Timerbaev) �������������� �������� "������� � ������ �� �������"
    if (!isFirstMonth()) {
        // 1. �������� ������� ����������� ���������� ������
        if (rowOld == null) {
            logger.error('����������� ���������� ��������� ������')
            return false
        }

        // 2..11 �������� ����������� (���������) ������ �� ����� �������� ������ ����� (����� 3..12)
        for (def column : requiredColumns) {
            if (row.getCell(column).getValue() < rowOld.getCell(column).getValue()) {
                def securitiesType = row.securitiesType
                def message = "���������� (��������) ����� �� $securitiesType ����������!"
                if (column in warnColumns) {
                    logger.warn(message)
                } else {
                    logger.error(message)
                }
                return false
            }
        }
    }

    // 12..21. �������� �� ��������������� �������� (����� 3..12)
    for (def column : requiredColumns) {
        if (row.getCell(column).getValue() < 0) {
            def columnName = getColumnName(row, column)
            def message = "�������� ����� \"$columnName\" �� ������ 1 �������������!"
            if (column in warnColumns) {
                logger.warn(message)
            } else {
                logger.error(message)
            }
            return false
        }
    }
    return true
}

/**
 * ������������.
 */
void consolidation() {
    // �������� ������ � �������������� �� ����������

    def row = formData.getDataRow('total')

    // ����� 3..12
    def columns = ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds',
            'municipalBondsBefore', 'rtgageBondsBefore', 'ovgvz',
            'eurobondsRF', 'itherEurobonds', 'corporateBonds']
    columns.each { alias ->
        row.getCell(alias).setValue(0)
    }

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            def sourceRow
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceRow = source.getDataRow('total')
                columns.each { alias ->
                    row.getCell(alias).setValue(sourceRow.getCell(alias).getValue())
                }
            }
        }
    }
    logger.info('������������ ����������������� ����� ������ �������.')
}

/**
 * �������� ��� �������� �����.
 */
void checkCreation() {
    // �������� ������
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //�������� ������� ����� ��������
    if (reportPeriod != null && reportPeriod.isBalancePeriod()) {
        logger.error('��������� ����� �� ����� ���� � ������� ����� ��������.')
        return
    }

    def findForm = FormDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('��������� ����� � ��������� ����������� ��� ����������.')
    }
}

/*
 * ��������������� ������.
 */

/**
 * �������� �������� �� ������ ��������.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * �������� ������ �� ��������.
 */
def isEmpty(def value) {
    return value == null || value == ''
}

/**
 * �������� ������ �� ���������� �������� ������
 */
def getFormDataOld() {
    // ���������� �������� ������
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // ���-31 �� ���������� �������� ������
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = FormDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * ������ �� ��� ����� (������)
 */
def isFirstMonth() {
    // �������� ������
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    if (reportPeriod != null && reportPeriod.getOrder() == 1) {
        return true
    }
    return false
}

/**
 * �������� ����� ������ � �������.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
}

/**
 * ��������� ������������ ������������ �����.
 *
 * @param row ������
 * @param columns ������ ������������ ������
 * @param useLog ����� �� ���������� ��������� � ���
 * @return true - ��� ������, false - ���� ������������� ����
 */
def checkRequiredColumns(def row, def columns, def useLog) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        if (!isEmpty(index)) {
            logger.error("� ������ \"� ��\" ������ $index �� ��������� ������� : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("� ������ $index �� ��������� ������� : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * �������� �������� ����� �� ����������.
 *
 * @param row ������
 * @param alias ��������� �����
 */
def getColumnName(def row, def alias) {
    if (row != null && alias != null) {
        return row.getCell(alias).getColumn().getName().replace('%', '%%')
    }
    return ''
}