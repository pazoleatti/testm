/**
 * ������ ��� ���-25 (rnu25.groovy).
 * ����� "(���-25) ������� ���������� ����� ������� ������� ��� ��������� ����������� ���, ��� � ��� � ����� ���������������".
 *
 * @version 65
 *
 * TODO:
 *      - ��� ������� � ��������� ������������ ��� (������ ��� ���� ������������)
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
// ����� 2  - regNumber
// ����� 3  - tradeNumber
// ����� 4  - lotSizePrev
// ����� 5  - lotSizeCurrent
// ����� 6  - reserve
// ����� 7  - cost
// ����� 8  - signSecurity
// ����� 9  - marketQuotation
// ����� 10 - costOnMarketQuotation
// ����� 11 - reserveCalcValue
// ����� 12 - reserveCreation
// ����� 13 - reserveRecovery

/**
 * �������� ����� ������.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)

    // ����� 2..5, 7..9
    ['regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent',
            'cost', 'signSecurity', 'marketQuotation',].each {
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

    // ������ ����������� �������� (����� 2, 3, 5, 7, 8)
    def requiredColumns = ['regNumber', 'tradeNumber', 'lotSizeCurrent', 'cost', 'signSecurity']
    for (def row : formData.dataRows) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    // �������������� �������� ����� 8
    for (def row : formData.dataRows) {
        // �������������� �������� ����� 8
        if (!isTotal(row) && row.signSecurity != '+' && row.signSecurity != '-') {
            logger.error('����� 8 ����� ��������� ������ ��������� ��������: "+" ��� "-".')
            return
        }
    }

    /*
     * �������.
     */

    // ������� ������ "�����" � "����� �� ���: ..."
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
    formData.dataRows.sort { it.regNumber }

    def tmp
    formData.dataRows.eachWithIndex { row, index ->
        // ����� 1
        row.rowNumber = index + 1

        // ����� 6
        row.reserve = getPrevPeriodValue('reserveCalcValue', 'tradeNumber', row.tradeNumber)

        // ����� 10
        row.costOnMarketQuotation = (row.marketQuotation ? round(row.lotSizeCurrent * row.marketQuotation, 2) : 0)

        // ����� 11
        if (row.signSecurity == '+') {
            def a = (row.cost ?: 0)
            tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
        } else {
            tmp = 0
        }
        row.reserveCalcValue = round(tmp, 2)

        // ����� 12
        tmp = round(row.reserveCalcValue - row.reserve, 2)
        row.reserveCreation = (tmp > 0 ? tmp : 0)

        // ����� 13
        row.reserveRecovery = (tmp < 0 ? Math.abs(tmp) : 0)
    }

    // ����� ��� ������� ���� ��������� ����� � ����� �� ��� (����� 4..7, 10..13)
    def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
            'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
    // �������� ������ "�����"
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.regNumber = '����� ����'
    setTotalStyle(totalRow)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }

    // ��������� "����� �� ���:..."
    def totalRows = [:]
    tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }
    formData.dataRows.eachWithIndex { row, i ->
        if (!isTotal(row)) {
            if (tmp == null) {
                tmp = row.regNumber
            }
            // ���� ��� ������� ��������� �� ������� ����� ������ "����� �� ���:..."
            if (tmp != row.regNumber) {
                totalRows.put(i, getNewRow(tmp, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // ���� ������ ��������� �� ������� ��� �� ���� ������� ����� ������ "����� �� ���:..."
            if (i == formData.dataRows.size() - 2) {
                totalColumns.each {
                    sums[it] += (row.getCell(it).getValue() ?: 0)
                }
                totalRows.put(i + 1, getNewRow(row.regNumber, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += (row.getCell(it).getValue() ?: 0)
            }
            tmp = row.regNumber
        }
    }
    // �������� "����� �� ���:..." � �������
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
    def formDataOld = getFormDataOld()

    if (formDataOld != null && !formDataOld.dataRows.isEmpty()) {
        // 1. �������� �� ������� ��������� ������ ���������� �������� �������� (����� 11)
        //      � ������� �������� ������� (����������� ���� ��� ��� ����� ����������)
        def count
        def missContract = []
        def severalContract = []
        formDataOld.dataRows.each { prevRow ->
            if (prevRow.reserveCalcValue > 0) {
                count = 0
                formDataOld.dataRows.each { row ->
                    if (row.tradeNumber == prevRow.tradeNumber) {
                        count += 1
                    }
                }
                if (count == 0) {
                    missContract.add(prevRow.tradeNumber)
                } else if (count > 1) {
                    severalContract.add(prevRow.tradeNumber)
                }
            }
        }
        if (!missContract.isEmpty()) {
            def message = missContract.join(', ')
            logger.warn("����������� ������ � �������� ������: $message!")
        }
        if (!severalContract.isEmpty()) {
            def message = severalContract.join(', ')
            logger.warn("���������� ��������� ����� � �������� ������: $message!")
        }
    }

    if (!formData.dataRows.isEmpty()) {
        def i = 1

        // ������ ����������� �������� (����� ..)
        def columns = ['rowNumber', 'regNumber', 'tradeNumber', 'lotSizeCurrent', 'reserve',
                'cost', 'signSecurity', 'costOnMarketQuotation',
                'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
        // ����� ������ ����� ������
        def totalSums = [:]
        // ����� ��� ������� ���� ��������� ����� � ����� �� ��� (����� 4..7, 10..13)
        def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
                'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
        // ������� ������� �������� �����
        def hasTotal = false
        // ������ ����� ����� ������������� ��� ������� ���� ����� ��������� �����
        def totalGroupsName = []

        def name
        def tmp

        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 15. �������������� ���������� ���� ����� 1..3, 5..13
            if (!checkRequiredColumns(row, columns, useLog)) {
                return false
            }

            // �������������� �������� ����� 8
            if (row.signSecurity != '+' && row.signSecurity != '-') {
                logger.error('����� 8 ����� ��������� ������ ��������� ��������: "+" ��� "-".')
                return
            }

            // 2. �������� ��� ������� �������� ������� ���� �� ������� �������� ���� (����� 5, 6, 13)
            if (row.lotSizeCurrent == 0 && row.reserve != row.reserveRecovery) {
                logger.warn('����� 6 � 13 �������!')
            }

            // 3. �������� ��� ������� �������� ������� ���� �� ������� �������� ���� (����� 5, 7, 10, 11)
            if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
                logger.warn('����� 7, 10 � 11 ���������!')
            }

            // 4. �������� ��� ������� �������� ������� ���� �� ���������� �������� ���� (����� 4, 6, 13)
            if (row.lotSizePrev == 0 && (row.reserve != 0 || row.reserveRecovery != 0)) {
                logger.error('����� 6 � 13 ���������!')
                return false
            }

            // 5. �������� �������������� ����� (����� 8, 11, 12)
            if (row.signSecurity == '-' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
                logger.warn('��������� ��������������, ����� 11 � 12 ���������!')
            }

            // 6. �������� �������� (��������������) ������� �� ������������ ������ (����� 8, 6, 11, 13)
            if (row.signSecurity == '+' && row.reserveCalcValue - row.reserve > 0 && row.reserveRecovery != 0) {
                logger.error('��������� ������������ � ������ ����������� (������������) �����������!')
                return false
            }

            // 7. �������� �������� (��������������) ������� �� ������������ ������ (����� 8, 6, 11, 12)
            if (row.signSecurity == '+' && row.reserveCalcValue - row.reserve < 0 && row.reserveCreation != 0) {
                logger.error('��������� ������������ � ������ ����������� (������������) �����������!')
                return false
            }

            // 8. �������� �������� (��������������) ������� �� ������������ ������ (����� 8, 6, 11, 13)
            if (row.signSecurity == '+' && row.reserveCalcValue - row.reserve == 0 &&
                    (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
                logger.error('��������� ������������ � ������ ����������� (������������) �����������!')
                return false
            }

            // 9. �������� �� ������������� �������� ��� ������� ���������� �������
            if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                    row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
                logger.warn('������ �����������. ����� 5, 7, 10 � 11 ���������������!')
            }

            // 10. �������� ������������ �������� ������� (����� 6, 11, 12, 13)
            if (row.reserve + row.reserveCreation != row.reserveCalcValue + row.reserveRecovery) {
                logger.error('������ ����������� �����������!')
                return false
            }

            // 11. �������� ������������ ���������� ��� (����� 3, 3 (�� ���������� ������), 4, 5 (�� ���������� ������) )
            if (checkOld(row, 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', formDataOld)) {
                def curCol = 3
                def curCol2 = 4
                def prevCol = 3
                def prevCol2 = 5
                logger.error("��� ����������� �����������! �� ����������� �������: ���� ������ $curCol� = ������ $prevCol� ����� ���-25 �� ���������� �������� ������, �� ������ $curCol2�  = ������ $prevCol2� ����� ���-25 �� ���������� �������� ������.")
                return false
            }

            // 12. �������� ������������ ���������� ��� (����� 3, 3 (�� ���������� ������), 6, 11 (�� ���������� ������) )
            if (checkOld(row, 'tradeNumber', 'reserve', 'reserveCalcValue', formDataOld)) {
                def curCol = 3
                def curCol2 = 3
                def prevCol = 6
                def prevCol2 = 11
                logger.error("��� ����������� �����������! �� ����������� �������: ���� ������ $curCol� = ������ $prevCol� ����� ���-25 �� ���������� �������� ������, �� ������ $curCol2�  = ������ $prevCol2� ����� ���-25 �� ���������� �������� ������.")
                return false
            }

            // 16. �������� �� ������������ ���� �� �� (����� 1)
            if (i != row.rowNumber) {
                logger.error('�������� ������������ ������ �� �������!')
                return false
            }
            i += 1

            // 17. �������������� �������� ���� 6, 10, 11, 12, 13 =========================
            // ����� 6
            if (row.reserve != getPrevPeriodValue('reserveCalcValue', 'tradeNumber', row.tradeNumber)) {
                name = getColumnName(row, 'reserve')
                logger.warn("������� ���������� ����� �$name�!")
            }

            // ����� 10
            tmp = (row.marketQuotation ? round(row.lotSizeCurrent * row.marketQuotation, 2) : 0)
            if (row.costOnMarketQuotation != tmp) {
                name = getColumnName(row, 'costOnMarketQuotation')
                logger.warn("������� ���������� ����� �$name�!")
            }

            // ����� 11
            if (row.signSecurity == '+') {
                def a = (row.cost == null ? 0 : row.cost)
                tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
            } else {
                tmp = 0
            }
            if (row.reserveCalcValue != round(tmp, 2)) {
                name = getColumnName(row, 'reserveCalcValue')
                logger.warn("������� ���������� ����� �$name�!")
            }

            // ����� 12
            tmp = round(row.reserveCalcValue - row.reserve, 2)
            if (row.reserveCreation != (tmp > 0 ? tmp : 0)) {
                name = getColumnName(row, 'reserveCreation')
                logger.warn("������� ���������� ����� �$name�!")
            }

            // ����� 13
            if (row.reserveRecovery != (tmp < 0 ? Math.abs(tmp) : 0)) {
                name = getColumnName(row, 'reserveRecovery')
                logger.warn("������� ���������� ����� �$name�!")
            }
            // 17. ����� �������������� �������� =================================

            // 18. �������� �������� �������� �� ���
            if (!totalGroupsName.contains(row.regNumber)) {
                totalGroupsName.add(row.regNumber)
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
            def totalRow = formData.getDataRow('total')
            totalRowOld = formDataOld.getDataRow('total')

            // 13. �������� ������������ ���������� ��� (����� 4, 5 (�� ���������� ������))
            if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                def curCol = 4
                def prevCol = 5
                logger.error("��� ����������� �����������! �� ����������� �������: ������ ���� �� ����� $curCol = ������ ���� �� ����� $prevCol ����� ���-25 �� ���������� �������� ������.")
                return false
            }

            // 14. �������� ������������ ���������� ��� (����� 6, 11 (�� ���������� ������))
            if (totalRow.reserve != totalRowOld.reserveCalcValue) {
                def curCol = 6
                def prevCol = 11
                logger.error("��� ����������� �����������! �� ����������� �������: ������ ���� �� ����� $curCol = ������ ���� �� ����� $prevCol ����� ���-25 �� ���������� �������� ������.")
                return false
            }
        }

        if (hasTotal) {
            def totalRow = formData.getDataRow('total')

            // 17. �������� �������� �������� �� ���
            for (def codeName : totalGroupsName) {
                def row = formData.getDataRow('total' + codeName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        logger.error("�������� �������� �� ��� $codeName ���������� �������!")
                        return false
                    }
                }
            }

            // 18. �������� ��������� �������� �� ���� �����
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
    // 1. �������� ������������ ���� �������� ������ ������ �� ������� �������� ����
    if (false) {
        logger.warn('������� ������ ������ �� ������� �������� ���� ������ �������!')
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
    newRow.regNumber = alias + ' ����'
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
 * ���������� ����� ��� �������� �����.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent',
            'reserve', 'cost', 'signSecurity', 'marketQuotation', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each {
        row.getCell(it).setStyleAlias('����������� �����')
    }
}

/**
 * �������� ������ �� ���������� �������� ������
 */
def getFormDataOld() {
    // ���������� �������� ������
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // ���-25 �� ���������� �������� ������
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = FormDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * ��������� ����� ���������� ����� ��� ����� � ����� ����� �������������
 *
 * @param regNumber ��� ������������� ������
 * @param alias �������� �����
 */
def calcSumByCode(def regNumber, def alias) {
    def sum = 0
    formData.dataRows.each { row ->
        if (!isTotal(row) && row.regNumber == regNumber) {
            sum += (row.getCell(alias).getValue() ?: 0)
        }
    }
    return sum
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
                return round(row.getCell(needColumnName).getValue(), 2)
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