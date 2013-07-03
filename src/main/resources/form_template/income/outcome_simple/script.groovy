/**
 * ����� "����������� ����� ��������, ����������� � ������� ��� (������� �������)".
 *
 * @version 46
 */

switch (formDataEvent) {
    // �������
    case FormDataEvent.CREATE :
        checkCreation()
        break
    // ���������
    case FormDataEvent.CALCULATE :
        checkAndCalc()
        break
    // ��������
    case FormDataEvent.COMPOSE :
        consolidation()
        break
    // ���������
    case FormDataEvent.CHECK :
        checkAndCalc()
        break
    // ��������� ��� �������� � ����������
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :
        checkAndCalc()
        break
    // ������� �� ����������
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED :
        checkAndCalc()
        break
    // ������� �� �������
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :
        checkAndCalc()
        checkDeclarationBankOnAcceptance()
        break
    // ������� �� ������� � �������
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED :
        checkDeclarationBankOnCancelAcceptance()
        break
    // ����� ������� �� "�������" � "����������"
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_APPROVED :
        checkDeclarationBankOnCancelAcceptance()
        break
}

// �����  1 - consumptionTypeId
// �����  2 - consumptionGroup
// �����  3 - consumptionTypeByOperation
// �����  4 - consumptionAccountNumber
// �����  5 - rnu7Field10Sum
// �����  6 - rnu7Field12Accepted
// �����  7 - rnu7Field12PrevTaxPeriod
// �����  8 - rnu5Field5Accepted
// �����  9 - logicalCheck
// ����� 10 - accountingRecords
// ����� 11 - opuSumByEnclosure2
// ����� 12 - opuSumByTableP
// ����� 13 - opuSumTotal
// ����� 14 - difference

/**
 * ��������� � ���������.
 */
void checkAndCalc() {
    calculationBasicSum()
}

/**
 * ���������� ����.
 */
void calculationBasicSum() {

//    formData.dataRows.each { row ->
//        ['rnu7Field10Sum', 'rnu7Field12Accepted',
//                'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each {
//            def cell = row.getCell(it)
//            if (cell.isEditable()) {
//                cell.setValue(1)
//            }
//        }
//    }

    /*
     * �������� ������������� �����
     */
    def requiredColumns = ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted']
    for (def row : formData.dataRows) {
        if (!checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * ������ ����
     */
    def row50001 = formData.getDataRow('R107')
    def row50002 = formData.getDataRow('R212')

    // ����� ��� ����� 5..8
    ['rnu7Field10Sum', 'rnu7Field12Accepted',
            'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
        row50001.getCell(alias).setValue(getSum(alias, 'R2', 'R106'))
        row50002.getCell(alias).setValue(getSum(alias, 'R109', 'R211'))
    }

    calculationControlGraphs()
}

/**
 * ������ ��� ���������� ����������� �����.
 *
 * � ������� ������� ��� 10� �����, �����������
 * ����� ������ ��� ����� > 10 ��������� "-1"
 *
 * @author rtimerbaev
 * @since 21.03.2013 13:00
 * @version 14 05.03.2013
 */
void calculationControlGraphs() {
    def message = '��������� ����������'
    def tmp
    def value
    def formDataComplex = getFormDataComplex()
    def income102NotFound = []
    for (def row : formData.dataRows) {
        // ��������� �������� ������
        if (row.getAlias() in ['R107', 'R212']) {
            continue
        }
        if (!isEmpty(row.rnu7Field10Sum) && !isEmpty(row.rnu7Field12Accepted) &&
                !isEmpty(row.rnu7Field12PrevTaxPeriod)) {
            // ����� 9 = ������(������ 5� - (������ 6� - ������ 7�); 2)
            tmp = round(row.rnu7Field10Sum - (row.rnu7Field10Sum - row.rnu7Field12Accepted), 2)
            value = ((BigDecimal) tmp).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = (tmp < 0 ? message : value.toString())
        }

        // ����� 11
        row.opuSumByEnclosure2 = getSumFromComplex(formDataComplex,
                'consumptionBuhSumAccountNumber', 'consumptionBuhSumAccepted', row.consumptionAccountNumber)

        // ����� 12
        if (row.getAlias() in ['R105', 'R209']) {
            tmp = calcColumn6(['R105', 'R209'])
        } else if (row.getAlias() in ['R106', 'R211']) {
            tmp = calcColumn6(['R106', 'R211'])
        } else if (row.getAlias() in ['R104', 'R208']) {
            tmp = calcColumn6(['R104', 'R208'])
        } else {
            tmp = row.rnu5Field5Accepted
        }
        row.opuSumByTableP = tmp

        // ����� 13
        def income102 = income102Dao.getIncome102(formData.reportPeriodId, row.accountingRecords, formData.departmentId)
        if (income102 == null || income102.isEmpty()) {
            income102NotFound += getIndex(row)
            tmp = 0
        } else {
            tmp = (income102[0] != null ? income102[0].getTotalSum() : 0)
        }
        row.opuSumTotal = tmp

        // ����� 14
        row.difference = (getValue(row.opuSumByEnclosure2) + getValue(row.opuSumByTableP)) - getValue(row.opuSumTotal)
    }

    if (!income102NotFound.isEmpty()) {
        def rows = income102NotFound.join(', ')
        logger.warn("�� ������� ��������������� ������ � ������ � �������� � ������� ��� �����: $rows")
    }
}

/**
 * ������ ��� �������� ��������.
 *
 * @author rtimerbaev
 * @since 21.02.2013 13:40
 */
void checkCreation() {
    def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('��������� ����� � ��������� ����������� ��� ����������.')
    }

    if (formData.kind != FormDataKind.SUMMARY) {
        logger.error("������ ��������� ����� � ����� ${formData.kind?.name}")
    }
}

/**
 * �������� ������� ���������� ����� ��� �������� ��.
 *
 * @author rtimerbaev
 * @since 21.03.2013 11:00
 */
void checkDeclarationBankOnAcceptance() {
    if (!isBank()) {
        return
    }
    departmentFormTypeService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        def bank = declarationService.find(2, department.departmentId, formData.reportPeriodId)
        if (bank != null && bank.accepted) {
            logger.error('�������� ��������� ����� ����������, �.�. ��� ������� ���������� �����.')
        }
    }
}

/**
 * �������� ������� ���������� ����� ��� ������ �������� ��.
 *
 * @author rtimerbaev
 * @since 21.03.2013 11:00
 */
void checkDeclarationBankOnCancelAcceptance() {
    if (!isBank()) {
        return
    }
    departmentFormTypeService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        def bank = declarationService.find(2, department.departmentId, formData.reportPeriodId)
        if (bank != null && bank.accepted) {
            logger.error('������ �������� ��������� ����� ����������, �.�. ��� ������� ���������� �����.')
        }
    }
}

/**
 * ������ ��� ������������.
 *
 * @author rtimerbaev
 * @since 21.02.2013 13:50
 */
void consolidation() {
    if (isTerBank()) {
        return
    }
    // �������� �����
    formData.getDataRows().each { row ->
        ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias->
            row.getCell(alias).setValue(null)
        }
    }

    def needCalc = false

    // �������� ����������������� ����� � �������� �������������� � ������� ��������� �������
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
        def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == 304) {
            needCalc = true
            for (def row : child.getDataRows()) {
                if (row.getAlias() == null) {
                    continue
                }
                def rowResult = formData.getDataRow(row.getAlias())
                ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each {
                    if (row.getCell(it).getValue() != null) {
                        rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                    }
                }
            }
        }
    }
    if (needCalc) {
        checkAndCalc()
    }
    logger.info('������������ ������� ����� ������ ����� ������ �������.')
}


/*
 * ��������������� ������.
 */

/**
 * �������� �� ����.
 */
def isBank() {
    boolean isBank = true
    departmentFormTypeService.getDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
        if (it.departmentId != formData.departmentId) {
            isBank = false
        }
    }
    return isBank
}

/**
 * �������� �� ��������.
 */
def isTerBank() {
    boolean isTerBank = false
    departmentFormTypeService.getFormDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
        if (it.departmentId != formData.departmentId) {
            isTerBank = true
        }
    }
    return isTerBank
}

/**
 * �������� ����� ��������� ����� ������������� �������.
 */
def getSum(String columnAlias, String rowFromAlias, String rowToAlias) {
    def from = formData.getDataRowIndex(rowFromAlias) + 1
    def to = formData.getDataRowIndex(rowToAlias) - 1
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
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

    def cell
    columns.each {
        cell = row.getCell(it)
        if (cell.isEditable() && (cell.getValue() == null || row.getCell(it).getValue() == '')) {
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
        logger.error("� ������ $index �� ��������� ������� : $errorMsg.")
        return false
    }
    return true
}

/**
 * �������� ����� ������ � �������.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
}

/**
 * �������� ������ �� ��������.
 */
def isEmpty(def value) {
    return value == null || value == ''
}

/**
 * �������� ����� �������� �� �������� �������.
 *
 * @param data ������ �����
 * @param columnAliasCheck ����� �����, �� ������� ���������� ������ ��� ������������
 * @param columnAliasSum ����� �����, �������� ������� �����������
 * @param value ��������, �� �������� ���������� ������ ��� ������������
 */
def getSumFromComplex(data, columnAliasCheck, columnAliasSum, value) {
    def sum = 0
    if (data != null && (columnAliasCheck != null || columnAliasCheck != '') && value != null) {
        for (def row : data.dataRows) {
            if (row.getCell(columnAliasCheck).getValue() == value) {
                sum += (row.getCell(columnAliasSum).getValue() ?: 0)
            }
        }
    }
    return sum
}

/**
 * �������� �������� ��� ����� 12. ����� �������� ����� 6 ��������� �����
 *
 * @param aliasRows ������ ������� �������� ������� ���� ��������������
 */
def calcColumn6(def aliasRows) {
    def sum = 0
    aliasRows.each { alias ->
        sum += formData.getDataRow(alias).rnu7Field12Accepted
    }
    return sum
}

/**
 * �������� ������ ����� "������� �������" (id = 303)
 */
def getFormDataComplex() {
    return FormDataService.find(303, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

/**
 * �������� �������� ��� ����.
 *
 * @param value �������� ������� ���� ���������
 */
def getValue(def value) {
    return value ?: 0
}