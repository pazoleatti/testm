/**
 * ����� "������� ����� ����������� �������� (������� �������)".
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
    // ���������
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :
        checkAndCalc()
        break
    // ������� �� ����������
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED :
        checkAndCalc()
        break
    // ������� �� ������� � ����������
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED :
        checkDeclarationBankOnCancelAcceptance()
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
    // ����� �������� �� ����������
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED :
        break
    // ����� ������� �� "�������" � "����������"
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_APPROVED :
        checkDeclarationBankOnCancelAcceptance()
        break
}

// �����  1 - consumptionTypeId
// �����  2 - consumptionGroup
// �����  3 - consumptionTypeByOperation
// �����  4 - consumptionBuhSumAccountNumber
// �����  5 - consumptionBuhSumRnuSource
// �����  6 - consumptionBuhSumAccepted
// �����  7 - consumptionBuhSumPrevTaxPeriod
// �����  8 - consumptionTaxSumRnuSource
// �����  9 - consumptionTaxSumS
// ����� 10 - rnuNo
// ����� 11 - logicalCheck
// ����� 12 - accountingRecords
// ����� 13 - opuSumByEnclosure3
// ����� 14 - opuSumByTableP
// ����� 15 - opuSumTotal
// ����� 16 - difference

/**
 * ��������� � ���������.
 */
void checkAndCalc() {
    calculation()
}

/**
 * ������.
 */
void calculation() {
    def needExit = true
    if ((formDataEvent == FormDataEvent.COMPOSE && isBank()) || formDataEvent != FormDataEvent.COMPOSE) {
        needExit = false
    }
    if (needExit) {
        return
    }

//    formData.dataRows.each { row ->
//        ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each {
//            def cell = row.getCell(it)
//            if (cell.isEditable()) {
//                cell.setValue(1)
//            }
//        }
//    }

    /*
     * �������� ������������� �����
     */
    def requiredColumns = ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS']
    for (def row : formData.dataRows) {
        if (!checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * ������ ����
     */
    def totalRow1 = formData.getDataRow('R67')
    def totalRow2 = formData.getDataRow('R93')

    // ����� ��� ����� 9
    ['consumptionTaxSumS'].each { alias ->
        totalRow1.getCell(alias).setValue(getSum(alias, 'R2', 'R66'))
        totalRow2.getCell(alias).setValue(getSum(alias, 'R69', 'R92'))
    }

    calculationControlGraphs()
}

/**
 * ������ (����������� �����).
 */
void calculationControlGraphs() {
    def needExit = true
    if ((formDataEvent == FormDataEvent.COMPOSE && isBank()) || formDataEvent != FormDataEvent.COMPOSE) {
        needExit = false
    }
    if (needExit) {
        return
    }

    def message = '��������� ����������'
    def tmp
    def value
    def formDataSimple = getFormDataSimple()
    def income102NotFound = []
    for (def row : formData.dataRows) {
        // ��������� �������� ������
        if (row.getAlias() in ['R67', 'R93']) {
            continue
        }
        if (!isEmpty(row.consumptionTaxSumS) && !isEmpty(row.consumptionBuhSumAccepted) &&
                !isEmpty(row.consumptionBuhSumPrevTaxPeriod)) {
            // ����� 11 = ������(������ 9� - (������ 6� - ������ 7�); 2)
            tmp = round(row.consumptionTaxSumS - (row.consumptionBuhSumAccepted - row.consumptionBuhSumPrevTaxPeriod), 2)
            value = ((BigDecimal) tmp).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = (tmp < 0 ? message : value.toString())
        }

        if (!isEmpty(row.consumptionBuhSumAccepted) && !isEmpty(row.consumptionBuhSumPrevTaxPeriod)) {
            // ����� 13
            if (row.getAlias() in ['R3', 'R11']) {
                tmp = calcColumn6(['R3', 'R11'])
            } else {
                tmp = row.consumptionBuhSumAccepted
            }
            row.opuSumByTableP = tmp

            // ����� 14
            row.opuSumByTableP = getSumFromSimple(formDataSimple, 'consumptionAccountNumber',
                    'rnu5Field5Accepted', row.consumptionBuhSumAccountNumber)

            // ����� 15
            def income102 = income102Dao.getIncome102(formData.reportPeriodId, row.accountingRecords, formData.departmentId)
            if (income102 == null || income102.isEmpty()) {
                income102NotFound += getIndex(row)
                tmp = 0
            } else {
                tmp = (income102[0] != null ? income102[0].getTotalSum() : 0)
            }
            row.opuSumTotal = tmp

            // ����� 16
            row.difference = (getValue(row.opuSumByEnclosure3) + getValue(row.opuSumByTableP)) - getValue(row.opuSumTotal)
        }
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
 * @since 22.02.2013 12:30
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
    if (isTerBank()) {
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
    if (isTerBank()) {
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
 * @since 22.02.2013 15:30
 */
void consolidation() {
    if (isTerBank()) {
        return
    }
    // �������� �����
    formData.getDataRows().each { row ->
        ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each { it ->
            row.getCell(it).setValue(null)
        }
    }

    def needCalc = false

    // �������� ����������������� ����� �� ����������
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
        def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == 303) {
            needCalc = true
            for (def row : child.getDataRows()) {
                if (row.getAlias() == null) {
                    continue
                }
                def rowResult = formData.getDataRow(row.getAlias())
                ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each {
                    if (row.getCell(it).getValue() != null && !row.getCell(it).hasValueOwner()) {
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

double summ(String columnName, String fromRowA, String toRowA) {
    def from = formData.getDataRowIndex(fromRowA)
    def to = formData.getDataRowIndex(toRowA)
    if (from > to) {
        return 0
    }
    def result = summ(formData, new ColumnRange(columnName, from, to))
    return result ?: 0;
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
 * �������� �������� ��� ����.
 *
 * @param value �������� ������� ���� ���������
 */
def getValue(def value) {
    return value ?: 0
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
 * �������� �������� ��� ����� 13. ����� �������� ����� 6 ��������� �����
 *
 * @param aliasRows ������ ������� �������� ������� ���� ��������������
 */
def calcColumn6(def aliasRows) {
    def sum = 0
    aliasRows.each { alias ->
        sum += formData.getDataRow(alias).consumptionBuhSumAccepted
    }
    return sum
}

/**
 * �������� ������ ����� "������� �������" (id = 304)
 */
def getFormDataSimple() {
    return FormDataService.find(304, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

/**
 * �������� ����� �������� �� �������� �������.
 *
 * @param data ������ �����
 * @param columnAliasCheck ����� �����, �� ������� ���������� ������ ��� ������������
 * @param columnAliasSum ����� �����, �������� ������� �����������
 * @param value ��������, �� �������� ���������� ������ ��� ������������
 */
def getSumFromSimple(data, columnAliasCheck, columnAliasSum, value) {
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