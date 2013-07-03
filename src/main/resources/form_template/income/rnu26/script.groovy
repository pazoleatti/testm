/**
 * ������ ��� ���-26 (rnu26.groovy).
 * ����� "(���-26) ������� ���������� ����� ������� ������� ��� ��������� ����������� �����, ���, ADR, GDR � �������� �������� � ����� ���������������".
 *
 * @version 65
 *
 * TODO:
 *      - ��� ������� � ��������� ������������ ��� (������ ��� ���� ������������)
 *      - ����� 8, 14-17 �������������, �� � ������� ����� ��� ����� ���������������
 *
 * @author rtimerbaev
 */

/** �������� ������. */
def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

/** ������� ������� ����� ��������. */
def isBalancePeriod = (reportPeriod != null && reportPeriod.isBalancePeriod())

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        if (!isBalancePeriod && !checkPrevPeriod()) {
            logger.error('����� ����������� ������� �� ����������, ��� �� ��������� � ������� ��������')
            return
        }
        logicalCheck(true)
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        if (!isBalancePeriod && !checkPrevPeriod()) {
            logger.error('����� ����������� ������� �� ����������, ��� �� ��������� � ������� ��������')
            return
        }
        calc()
        logicalCheck(false)
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
    // ����� �������� �� ������������
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        logicalCheck(true)
        checkNSI()
        break
    // ��������
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheck(false)
        checkNSI()
        break
}

// ����� 1  - rowNumber
// ����� 2  - issuer
// ����� 3  - shareType
// ����� 4  - tradeNumber
// ����� 5  - currency
// ����� 6  - lotSizePrev
// ����� 7  - lotSizeCurrent
// ����� 8  - reserveCalcValuePrev
// ����� 9  - cost
// ����� 10 - signSecurity
// ����� 11 - marketQuotation
// ����� 12 - rubCourse
// ����� 13 - marketQuotationInRub
// ����� 14 - costOnMarketQuotation
// ����� 15 - reserveCalcValue
// ����� 16 - reserveCreation
// ����� 17 - reserveRecovery

/**
 * �������� ����� ������.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)

    // ����� 2..7, 9..13
    ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent',
            'cost', 'signSecurity', 'marketQuotation', 'rubCourse'].each {
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
    for (def row : formData.dataRows) {
        if (!isTotal(row)) {
            // ������ ����������� �������� (����� 2..7, 9, 10, 11)
            def requiredColumns = ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev',
                    'lotSizeCurrent', 'cost', 'signSecurity', 'marketQuotationInRub']

            if (!checkRequiredColumns(row, requiredColumns, true)) {
                return
            }
        }
    }

    // �������������� �������� ����� 10
    for (def row : formData.dataRows) {
        // �������������� �������� ����� 10
        if (!isTotal(row) && row.signSecurity != '+' && row.signSecurity != '-') {
            logger.error('����� 10 ����� ��������� ������ ��������� ��������: "+" ��� "-".')
            return
        }
    }

    /*
     * �������.
     */

    // ������� ������ "�����" � "����� �� ��������: ..."
    def delRow = []
    formData.dataRows.each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        formData.dataRows.remove(getIndex(row))
    }
    if (formData.dataRows.isEmpty()) {
        return
    }

    // �������������/������������
    formData.dataRows.sort { it.issuer }

    def tmp
    formData.dataRows.eachWithIndex { row, index ->
        // ����� 1
        row.rowNumber = index + 1

        // ����� 8
        row.reserveCalcValuePrev = getPrevPeriodValue('reserveCalcValue', 'tradeNumber', row.tradeNumber)

        // ����� 13
        if (row.marketQuotation != null && row.rubCourse != null) {
            row.marketQuotationInRub = round(row.marketQuotation * row.rubCourse, 2)
        }

        // ����� 14
        tmp = (row.marketQuotationInRub == null ? 0 : round(row.lotSizeCurrent * row.marketQuotationInRub, 2))
        row.costOnMarketQuotation = tmp

        // ����� 15
        if (row.signSecurity == '+') {
            def a = (row.cost == null ? 0 : row.cost)
            tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
        } else {
            tmp = 0
        }
        row.reserveCalcValue = tmp

        // ����� 16
        tmp = row.reserveCalcValue - row.reserveCalcValuePrev
        row.reserveCreation = (tmp > 0 ? tmp : 0)

        // ����� 17
        row.reserveRecovery = (tmp < 0 ? Math.abs(tmp) : 0)
    }

    // ����� ��� ������� ���� ��������� ����� � ����� �� �������� (����� 6..9, 14..17)
    def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev',
            'cost', 'costOnMarketQuotation', 'reserveCalcValue',
            'reserveCreation', 'reserveRecovery']
    // �������� ������ "�����"
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.issuer = '����� ����'
    setTotalStyle(totalRow)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }

    // ��������� "����� �� ��������:..."
    def totalRows = [:]
    def sums = [:]
    tmp = null
    totalColumns.each {
        sums[it] = 0
    }
    formData.dataRows.eachWithIndex { row, i ->
        if (!isTotal(row)) {
            if (tmp == null) {
                tmp = row.issuer
            }
            // ���� ��� ������� ��������� �� ������� ����� ������ "����� �� ��������:..."
            if (tmp != row.issuer) {
                totalRows.put(i, getNewRow(tmp, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // ���� ������ ��������� �� ������� ��� �� ���� ������� ����� ������ "����� �� ��������:..."
            if (i == formData.dataRows.size() - 2) {
                totalColumns.each {
                    sums[it] += (row.getCell(it).getValue() ?: 0)
                }
                totalRows.put(i + 1, getNewRow(row.issuer, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += (row.getCell(it).getValue() ?: 0)
            }
            tmp = row.issuer
        }
    }
    // �������� "����� �� ��������:..." � �������
    def i = 0
    totalRows.each { index, row ->
        formData.dataRows.add(index + i, row)
        i = i + 1
    }
}

/**
 * ���������� ��������.
 *
 * @param useLog ����� �� ���������� � ��� ��������� � ��������������� ������������ �����
 */
def logicalCheck(def useLog) {
    // ������ ����������� ��������� �������
    def formDataOld = getFormDataOld()

    if (formDataOld != null && !formDataOld.dataRows.isEmpty()) {
        def i = 1

        // ������ ����������� �������� (����� 1..3, 5..10, 13, 14)
        columns = ['issuer', 'shareType', 'currency', 'lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev',
                'cost', 'signSecurity', 'marketQuotationInRub', 'costOnMarketQuotation']

        // ����� ������ ����� ������
        def totalSums = [:]

        // ����� ��� ������� ���� ��������� ����� � ����� �� �������� (����� 6..9, 14..17)
        def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev',
                'cost', 'costOnMarketQuotation', 'reserveCalcValue',
                'reserveCreation', 'reserveRecovery']

        // ������� ������� �������� �����
        def hasTotal = false

        // ������ ����� ����� ������������� ��� ������� ���� ����� ��������� �����
        def totalGroupsName = []

        def tmp
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 15. �������������� ���������� ���� ����� 1..3, 5..10, 13, 14
            if (!checkRequiredColumns(row, columns, useLog)) {
                return false
            }

            // �������������� �������� ����� 10
            if (row.signSecurity != '+' && row.signSecurity != '-') {
                logger.error('����� 10 ����� ��������� ������ ��������� ��������: "+" ��� "-".')
                return
            }

            // 2. �������� ��� ������� �������� ������� ���� �� ������� �������� ���� (����� 7, 8, 17)
            if (row.lotSizeCurrent == 0 && row.reserveCalcValuePrev != row.reserveRecovery) {
                logger.warn('����� 8 � 17 �������!')
            }

            // 3. �������� ��� ������� �������� ������� ���� �� ������� �������� ���� (����� 7, 9, 14, 15)
            if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
                logger.warn('����� 9, 14 � 15 ���������!')
            }

            // 4. �������� ��� ������� �������� ������� ���� �� ���������� �������� ���� (����� 6, 8, 17)
            if (row.lotSizePrev == 0 && (row.reserveCalcValuePrev != 0 || row.reserveRecovery != 0)) {
                logger.error('����� 8 � 17 ���������!')
                return false
            }

            // 5. �������� �������������� ����� (����� 10, 15, 16)
            if (row.signSecurity == '-' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
                logger.warn('����� ��������������, ����� 15 � 16 ���������!')
            }

            // 6. �������� �������� (��������������) ������� �� ������������ ������ (����� 8, 10, 15, 17)
            tmp = (row.reserveCalcValue ?: 0) - row.reserveCalcValuePrev
            if (row.signSecurity == '+' && tmp > 0 && row.reserveRecovery != 0) {
                logger.error('����� ������������ � ������ ����������� (������������) �����������!')
                return false
            }

            // 7. �������� �������� (��������������) ������� �� ������������ ������ (����� 8, 10, 15, 16)
            if (row.signSecurity == '+' && tmp < 0 && row.reserveCreation != 0) {
                logger.error('����� ������������ � ������ ����������� (������������) �����������!')
                return false
            }

            // 8. �������� �������� (��������������) ������� �� ������������ ������ (����� 8, 10, 15, 17)
            if (row.signSecurity == '+' && tmp == 0 &&
                    (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
                logger.error('����� ������������ � ������ ����������� (������������) �����������!')
                return false
            }

            // 9. �������� ������������ ������������ ������� (����� 8, 15, 16, 17)
            if (row.reserveCalcValuePrev + (row.reserveCreation ?: 0) != (row.reserveCalcValue ?: 0) + (row.reserveRecovery ?: 0)) {
                logger.error('������ ����������� �������!')
                return false
            }

            // 10. �������� �� ������������� �������� ��� ������� ���������� �������
            if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                    row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
                logger.warn('������ �����������. ����� 7, 9, 14 � 15 ���������������!')
            }

            // 11. �������� ������������ ���������� ��� (����� 4, 4 (�� ���������� ������), 6, 7 (�� ���������� ������) )
            if (checkOld(row, 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', formDataOld)) {
                def curCol = 4
                def curCol2 = 6
                def prevCol = 4
                def prevCol2 = 7
                logger.warn("��� ����������� �����������! �� ����������� �������: ���� ������ $curCol� = ������ $prevCol� ����� ���-26 �� ���������� �������� ������, �� ������ $curCol2�  = ������ $prevCol2� ����� ���-26 �� ���������� �������� ������.")
            }

            // 12. �������� ������������ ���������� ��� (����� 4, 4 (�� ���������� ������), 8, 15 (�� ���������� ������) )
            if (checkOld(row, 'tradeNumber', 'reserveCalcValuePrev', 'reserveCalcValue', formDataOld)) {
                def curCol = 4
                def curCol2 = 4
                def prevCol = 8
                def prevCol2 = 15
                logger.error("��� ����������� �����������! �� ����������� �������: ���� ������ $curCol� = ������ $prevCol� ����� ���-26 �� ���������� �������� ������, �� ������ $curCol2�  = ������ $prevCol2� ����� ���-26 �� ���������� �������� ������.")
                return false
            }

            // 16. �������� �� ������������ ���� �� �� (����� 1)
            if (i != row.rowNumber) {
                logger.error('�������� ������������ ������ �� �������!')
                return false
            }
            i += 1

            // 17. �������������� �������� ����� 8, 14..17
            // ����� 8
            if (row.reserveCalcValuePrev != getPrevPeriodValue('reserveCalcValue', 'tradeNumber', row.tradeNumber)) {
                name = getColumnName(row, 'reserveCalcValuePrev')
                logger.warn("������� ���������� ����� �$name�!")
            }

            // ����� 13
            if (row.marketQuotation != null && row.rubCourse != null &&
                    row.marketQuotationInRub != round(row.marketQuotation * row.rubCourse, 2)) {
                name = getColumnName(row, 'marketQuotationInRub')
                logger.warn("������� ���������� ����� �$name�!")
            }

            // ����� 14
            tmp = (row.marketQuotationInRub == null ? 0 : round(row.lotSizeCurrent * row.marketQuotationInRub, 2))
            if (row.costOnMarketQuotation != tmp) {
                name = getColumnName(row, 'costOnMarketQuotation')
                logger.warn("������� ���������� ����� �$name�!")
            }

            // ����� 15
            if (row.signSecurity == '+') {
                def a = (row.cost == null ? 0 : row.cost)
                tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
            } else {
                tmp = 0
            }
            if (row.reserveCalcValue != tmp) {
                name = getColumnName(row, 'reserveCalcValue')
                logger.warn("������� ���������� ����� �$name�!")
            }

            // ����� 16
            tmp = (row.reserveCalcValue ?: 0) - row.reserveCalcValuePrev
            if (row.reserveCreation != (tmp > 0 ? tmp : 0)) {
                name = getColumnName(row, 'reserveCreation')
                logger.warn("������� ���������� ����� �$name�!")
            }

            // ����� 17
            if (row.reserveRecovery != (tmp < 0 ? Math.abs(tmp) : 0)) {
                name = getColumnName(row, 'reserveRecovery')
                logger.warn("������� ���������� ����� �$name�!")
            }
            // 17. �����=========================================

            // 18. �������� �������� �������� �� ���������
            if (!totalGroupsName.contains(row.issuer)) {
                totalGroupsName.add(row.issuer)
            }

            // 19. �������� ��������� �������� �� ���� ����� - ������� ���� ��� ����� ������
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
            }
        }

        if (formDataOld != null && hasTotal) {
            totalRow = formData.getDataRow('total')
            totalRowOld = formDataOld.getDataRow('total')

            // 13. �������� ������������ ���������� ��� (����� 6, 7 (�� ���������� ������))
            if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                def curCol = 6
                def prevCol = 7
                logger.error("��� ����������� �����������! �� ����������� �������: ������ �� ����� $curCol = ������ �� ����� $prevCol ����� ���-26 �� ���������� �������� ������.")
                return false
            }

            // 14. �������� ������������ ���������� ��� (����� 8, 15 (�� ���������� ������))
            if (totalRow.cost != totalRowOld.reserveCalcValue) {
                def curCol = 8
                def prevCol = 15
                logger.error("��� ����������� �����������! �� ����������� �������: ������ �� ����� $curCol = ������ �� ����� $prevCol ����� ���-26 �� ���������� �������� ������.")
                return false
            }
        }

        if (hasTotal) {
            def totalRow = formData.getDataRow('total')

            // 18. �������� �������� �������� �� ��������
            for (def codeName : totalGroupsName) {
                def row = formData.getDataRow('total' + codeName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        logger.error("�������� �������� �� �������� $codeName ���������� �������!")
                        return false
                    }
                }
            }

            // 19. �������� ��������� �������� �� ���� �����
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    logger.error('�������� �������� ���������� �������!')
                    return false
                }
            }
        }
    }
    return true
}

/**
 * �������� ������������ ���.
 */
def checkNSI() {
    // 1. �������� ����� ������ �� ���������� - �������� ������������ ��������� ����� 6� �� ���� �� ������ 5�
    if (false) {
        logger.warn('�������� ���� ������!')
    }

    // 1. �������� ������������ ���� ������� ������� ������ ������ ����� ��������?
    if (false) {
        logger.warn('������ ������� ������ ������ ������� �������!')
    }

    // 2. �������� ������������ ���� �������� ������ ������ �� ������� �������� ����
    if (false) {
        logger.warn('')
    }

    // 3. �������� ������������ ���� ����� ����� � ������ �������� ���������
    if (false) {
        logger.warn('')
    }
    return true
}

/**
 * ������������.
 */
void consolidation() {
    // ������� ��� ������ � ������� �� ���������� �� ������
    formData.dataRows.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                source.getDataRows().each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        formData.dataRows.add(row)
                    }
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
 * �������� ����� �������.
 */
def getSum(def columnAlias) {
    def from = 0
    def to = formData.dataRows.size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
}

/**
 * �������� ����� ������.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    newRow.issuer = alias + ' ����'
    setTotalStyle(newRow)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}

/**
 * ������� ������ � ���������� ��������.
 *
 * @param row ������ �� �������� �������
 * @param likeColumnName ��������� ����� �� �������� ������ ����������������� ������
 * @param curColumnName ��������� ����� ������� �� ��� ������� �������
 * @param prevColumnName ��������� ����� ���������� �� ��� ������� �������
 * @param prevForm ������ �� ����������� �������
 */
def checkOld(def row, def likeColumnName, def curColumnName, def prevColumnName, def prevForm) {
    if (prevForm == null) {
        return false
    }
    if (row.getCell(likeColumnName).getValue() == null) {
        return false
    }
    for (def prevRow : prevForm.dataRows) {
        if (row.getCell(likeColumnName).getValue() == prevRow.getCell(likeColumnName).getValue() &&
                row.getCell(curColumnName).getValue() != prevRow.getCell(prevColumnName).getValue()) {
            return true
        }
    }
}

/**
 * �������� ������ �� ���������� �������� ������
 */
def getFormDataOld() {
    // ���������� �������� ������
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // ���-26 �� ���������� �������� ������
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = FormDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * * ��������� ����� ���������� ����� ��� ����� � ����� ���������
 *
 * @param value �������� ����� ��� ���� ����� ������������
 * @param alias �������� �����
 */
def calcSumByCode(def value, def alias) {
    def sum = 0
    formData.dataRows.each { row ->
        if (!isTotal(row) && row.issuer == value) {
            sum += (row.getCell(alias).getValue() ?: 0)
        }
    }
    return sum
}

/**
 * ���������� ����� ��� �������� �����.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'issuer', 'shareType', 'tradeNumber', 'currency',
            'lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev', 'cost', 'signSecurity',
            'marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation',
            'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each {
        row.getCell(it).setStyleAlias('����������� �����')
    }
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

    // ���� �� ��������� ����� 11 � ����� 12, �� ����� 13 ������ ���� ��������� �������
    if (row.marketQuotation != null && row.rubCourse != null) {
        columns -= 'marketQuotationInRub'
    }

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
        if (index != null) {
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
 * �������� �������� �� ���������� �������� ������.
 *
 * @param needColumnName ��������� ����� �������� ������� ���� �������� (����� ��������)
 * @param searchColumnName ��������� ����� �� ������� ����� �������� �������� (����� ������)
 * @param searchValue �������� ����� ������
 * @return ���������� �������� ��������, ����� ��������� 0
 */
def getPrevPeriodValue(def needColumnName, def searchColumnName, def searchValue) {
    def formDataOld = getFormDataOld()
    if (formDataOld != null && !formDataOld.dataRows.isEmpty()) {
        for (def row : formDataOld.dataRows) {
            if (row.getCell(searchColumnName).getValue() == searchValue) {
                return round(row.getCell(needColumnName), 2)
            }
        }
    }
    return 0
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
 * ��������� ������ �� ���������� �������� ������.
 */
def checkPrevPeriod() {
    def formDataOld = getFormDataOld()

    if (formDataOld != null && !formDataOld.dataRows.isEmpty() && formDataOld.state == WorkflowState.ACCEPTED) {
        return true
    }
    return false
}