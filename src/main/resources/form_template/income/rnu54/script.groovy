/**
 * ������ ��� ���-54 (rnu54.groovy).
 * ����� "(���-54) ������� ���������� ����� �������� ������ ���� � �������������� ������� �� 2-� �����".
 *
 * @version 65
 *
 * TODO:
 *      - ��� ������� � ��������� ������������ ��� (������ ��� ���� ������������)
 *      - ������ ����� ���� �� �� �� �������� ���� ��� �������� ����� 12 � ��� 5�� � 6�� ���������� ��������
 *
 * @author rtimerbaev
 */

import java.text.SimpleDateFormat

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck(true)
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
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

// ����� 1  - tadeNumber
// ����� 2  - securityName
// ����� 3  - currencyCode
// ����� 4  - nominalPriceSecurities
// ����� 5  - salePrice
// ����� 6  - acquisitionPrice
// ����� 7  - part1REPODate
// ����� 8  - part2REPODate
// ����� 9  - income
// ����� 10 - outcome
// ����� 11 - rateBR
// ����� 12 - outcome269st
// ����� 13 - outcomeTax

/**
 * �������� ����� ������.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    formData.dataRows.add(newRow)

    // ����� 1..10
    ['tadeNumber', 'securityName', 'currencyCode', 'nominalPriceSecurities',
            'salePrice', 'acquisitionPrice', 'part1REPODate', 'part2REPODate'].each {
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

    // ������ ����������� �������� (����� 1..10)
    def requiredColumns = ['tadeNumber', 'securityName', 'currencyCode',
            'nominalPriceSecurities', 'salePrice', 'acquisitionPrice',
            'part1REPODate', 'part2REPODate']

    for (def row : formData.dataRows) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * �������
     */

    // ������� ������ "�����"
    def delRow = []
    formData.dataRows.each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        formData.dataRows.remove(getIndex(row))
    }

    /** �������� ����. */
    def reportDate = getReportDate()

    /** ���� ������ ��� �������� ����� 12. */
    def someDate = getDate('01.11.2009')

    /** ���������� ���� � ����. */
    def daysInYear = getCountDaysInYaer(new Date())

    /** ���� �� �� �� �������� ����. */
    def course = 1 // TODO (Ramil Timerbaev) ������ ����� ���� �� �� �� �������� ����

    def tmp
    def a, b ,c

    formData.dataRows.eachWithIndex { row, i ->

        // ����� 9, 10
        a = calcAForColumn9or10(row, reportDate, course)
        b = 0
        c = 0
        if (a < 0) {
            c = round(Math.abs(a), 2)
        } else if (a > 0) {
            b = round(a, 2)
        }
        row.income = b
        row.outcome = c

        // ����� 11
        if (row.outcome == 0 || isEmpty(row.currencyCode)) {
            tmp = null
        } else if (row.currencyCode == '810') {
            // TODO (Ramil Timerbaev) ������ 11� = ������ ���������������� ����� ������ �� ����������� ������� ���������������� �� �Ի �� ��������� ����
            tmp = 0
        } else {
            if (inPeriod(reportDate, '01.09.2008', '31.12.2009')) {
                tmp = 22
            } else if (inPeriod(reportDate, '01.01.2011', '31.12.2012')) {
                // TODO (Ramil Timerbaev) ������ ���������������� ����� ������ �� ����������� ������� ���������������� �� �Ի  �� ��������� ����
                tmp = 0
            } else {
                tmp = 15
            }
        }
        row.rateBR = round(tmp, 2)

        // ����� 12
        if (row.outcome == 0) {
            tmp = 0
        } else if (row.outcome > 0 && row.currencyCode == '810') {
            if (inPeriod(reportDate, '01.09.2008', '31.12.2009')) {
                tmp = calc12Value(row, 1.5, reportDate, daysInYear)
            } else if (inPeriod(reportDate, '01.01.2010', '30.06.2010') && row.part1REPODate < someDate) {
                tmp = calc12Value(row, 2, reportDate, daysInYear)
            } else if (inPeriod(reportDate, '01.01.2010', '31.12.2012')) {
                tmp = calc12Value(row, 1.8, reportDate, daysInYear)
            } else {
                tmp = calc12Value(row, 1.1, reportDate, daysInYear)
            }
        } else if (row.outcome > 0 && row.currencyCode != '810') {
            if (inPeriod(reportDate, '01.01.20011', '31.12.2012')) {
                tmp = calc12Value(row, 0.8, reportDate, daysInYear) * course
            } else {
                tmp = calc12Value(row, 1, reportDate, daysInYear) * course
            }
        }
        row.outcome269st = tmp

        // ����� 13
        if (row.outcome == 0) {
            tmp = 0
        } else if (row.outcome > 0 && row.outcome <= row.outcome269st) {
            tmp = row.outcome
        } else if (row.outcome > 0 && row.outcome > row.outcome269st) {
            tmp = row.outcome269st
        }
        row.outcomeTax = tmp
    }

    // ������ �����
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.tadeNumber = '�����'
    totalRow.getCell('tadeNumber').colSpan = 2
    setTotalStyle(totalRow)
    ['salePrice', 'acquisitionPrice', 'income', 'outcome', 'outcome269st', 'outcomeTax'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }
}

/**
 * ���������� ��������.
 *
 * @param useLog ����� �� ���������� � ��� ��������� � ��������������� ������������ �����
 */
def logicalCheck(def useLog) {
    if (!formData.dataRows.isEmpty()) {

        // ������ ����������� �������� (����� 12, 13)
        def requiredColumns = ['outcome269st', 'outcomeTax']

        /** �������� ����. */
        def reportDate = getReportDate()

        /** ���� ������ ��� �������� ����� 12. */
        def someDate = getDate('01.11.2009')

        /** ���������� ���� � ����. */
        def daysInYear = getCountDaysInYaer(new Date())

        /** ���� �� �� �� �������� ����. */
        def course = 1 // TODO (Ramil Timerbaev) ������ ����� ���� �� �� �� �������� ����

        def hasTotalRow = false
        def hasError
        def tmp
        def a, b, c

        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotalRow = true
                continue
            }

            // 1. �������������� ���������� ���� ����� 12 � 13
            if (!checkRequiredColumns(row, requiredColumns, true)) {
                return false
            }

            // 2. �������� ���� ������ ����� ���� (����� 7)
            if (row.part1REPODate > reportDate) {
                logger.error('������� ������� ���� ������ ����� ������!')
                return false
            }
            // 3. �������� ���� ������ ����� ���� (����� 8)
            if (row.part2REPODate <= reportDate) {
                logger.error('������� ������� ���� ������ ����� ������!')
                return false
            }

            // 4. �������� ����������� ���������� (����� 9, 10, 12, 13)
            if (row.income != 0 && row.outcome != 0) {
                logger.error('��������� ����������� ����������!')
                return false
            }

            // 5. �������� ��������� ����������
            if (row.outcome != 0 && (row.outcome269st != 0 || row.outcomeTax != 0)) {
                logger.error('��������� ����������� ����������!')
                return false
            }

            // 6. �������� ����������� ����������
            tmp = ((row.acquisitionPrice - row.salePrice) * (reportDate - row.part1REPODate) / (row.part2REPODate - row.part1REPODate)) * course
            if (tmp < 0 && row.income != round(Math.abs(tmp), 2)) {
                logger.warn('������� ���������� ������')
            }

            // 7. �������� ����������� ����������
            if (tmp > 0 && row.outcome != round(Math.abs(tmp), 2)) {
                logger.warn('������� ���������� �������')
            }

            // 8. �������������� �������� ����� 9, 10, 11, 12, 13 ===============================������
            // ����� 9, 10
            a = calcAForColumn9or10(row, reportDate, course)
            b = 0
            c = 0
            if (a < 0) {
                c = round(Math.abs(a), 2)
            } else if (a > 0) {
                b = round(a, 2)
            }
            // ����� 9
            if (row.income != b) {
                name = getColumnName(row, 'income')
                logger.warn("������� ���������� ����� �$name�!")
            }
            // ����� 10
            if (row.outcome != c) {
                name = getColumnName(row, 'outcome')
                logger.warn("������� ���������� ����� �$name�!")
            }

            // ����� 11
            if (row.outcome == 0 || isEmpty(row.currencyCode)) {
                tmp = null
            } else if (row.currencyCode == '810') {
                // TODO (Ramil Timerbaev) ������ 11� = ������ ���������������� ����� ������ �� ����������� ������� ���������������� �� �Ի �� ��������� ����
                tmp = 0
            } else {
                if (inPeriod(reportDate, '01.09.2008', '31.12.2009')) {
                    tmp = 22
                } else if (inPeriod(reportDate, '01.01.2011', '31.12.2012')) {
                    // TODO (Ramil Timerbaev) ������ ���������������� ����� ������ �� ����������� ������� ���������������� �� �Ի  �� ��������� ����
                    tmp = 0
                } else {
                    tmp = 15
                }
            }
            if (row.rateBR != round(tmp, 2)) {
                name = getColumnName(row, 'rateBR')
                logger.warn("������� ���������� ����� �$name�!")
            }

            // ����� 12
            if (row.outcome == 0) {
                tmp = 0
            } else if (row.outcome > 0 && row.currencyCode == '810') {
                if (inPeriod(reportDate, '01.09.2008', '31.12.2009')) {
                    tmp = calc12Value(row, 1.5, reportDate, daysInYear)
                } else if (inPeriod(reportDate, '01.01.2010', '30.06.2010') && row.part1REPODate < someDate) {
                    tmp = calc12Value(row, 2, reportDate, daysInYear)
                } else if (inPeriod(reportDate, '01.01.2010', '31.12.2012')) {
                    tmp = calc12Value(row, 1.8, reportDate, daysInYear)
                } else {
                    tmp = calc12Value(row, 1.1, reportDate, daysInYear)
                }
            } else if (row.outcome > 0 && row.currencyCode != '810') {
                if (inPeriod(reportDate, '01.01.20011', '31.12.2012')) {
                    tmp = calc12Value(row, 0.8, reportDate, daysInYear) * course
                } else {
                    tmp = calc12Value(row, 1, reportDate, daysInYear) * course
                }
            }
            if (row.outcome269st != tmp) {
                name = getColumnName(row, 'outcome269st')
                logger.warn("������� ���������� ����� �$name�!")
            }

            // ����� 13
            if (row.outcome == 0) {
                tmp = 0
            } else if (row.outcome > 0 && row.outcome <= row.outcome269st) {
                tmp = row.outcome
            } else if (row.outcome > 0 && row.outcome > row.outcome269st) {
                tmp = row.outcome269st
            }
            if (row.outcomeTax != tmp) {
                name = getColumnName(row, 'outcomeTax')
                logger.warn("������� ���������� ����� �$name�!")
            }
            // 8. �������������� �������� ����� 9, 10, 11, 12, 13 ===============================�����
        }

        // 9. �������� �������� �������� �����  ����������� ������������� (����� 5, 6, 9, 10, 12, 13).
        if (hasTotalRow) {
            def totalRow = formData.getDataRow('total')
            def totalSumColumns = ['salePrice', 'acquisitionPrice', 'income',
                    'outcome', 'outcome269st', 'outcomeTax']
            for (def alias : totalSumColumns) {
                if (totalRow.getCell(alias).getValue() != getSum(alias)) {
                    logger.error('�������� �������� ����� ���������� �������!')
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
    if (!formData.dataRows.isEmpty()) {
        /** �������� ����. */
        def reportDate = getReportDate()

        def hasError
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                continue
            }

            // 1. �������� ���� ������ �� ���������� (����� 3)
            if (false) {
                logger.warn('�������� ��� ������!')
            }

            // 2. �������� ������������ ������ ���������������� �� (����� 11) ���� ������ (����� 3)
            hasError = false
            if (((row.outcome == 0 || isEmpty(row.currencyCode)) && row.rateBR == null)) {
                hasError = false
            } else if ((row.outcome == 0 || isEmpty(row.currencyCode)) && row.rateBR != null) {
                hasError = true
            } else if (row.currencyCode == '810' && true) {
                // TODO (Ramil Timerbaev) �������: ������ 11� != ������ ���������������� ����� ������ �� ����������� ������� ���������������� �� �Ի �� ��������� ����
                // row.rateBR != �������� �� �����������
                hasError = true
            } else if (row.currencyCode != '810') {
                if (inPeriod(reportDate, '01.09.2008', '31.12.2009') && row.rateBR != 22) {
                    hasError = true
                } else if (inPeriod(reportDate, '01.01.2011', '31.12.2012') && true) {
                    // TODO (Ramil Timerbaev) �������: ����� 11 != ������ ���������������� ����� ������ �� ����������� ������� ���������������� �� �Ի  �� ��������� ����
                    // row.rateBR != �������� �� �����������
                    hasError = true
                } else if (row.rateBR != 15) {
                    hasError = true
                }
            }
            if (hasError) {
                logger.error('������� ������� ������ ����� ������!')
                return false
            }
        }
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
                source.getDataRows().each { row ->
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
    // �������� ������
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //�������� ������� ����� ��������
    if (reportPeriod != null && reportPeriod.isBalancePeriod()) {
        logger.error('��������� ����� �� ����� ����������� � ������� ����� ��������.')
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
    return value == null || value == '' || value == 0
}

/**
 * ��������� �������� �� ��������� ���� � ������
 */
def inPeriod(def date, def from, to) {
    if (date == null) {
        return false
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def dateFrom = format.parse(from)
    def dateTo = format.parse(to)
    return (dateFrom < date && date <= dateTo)
}

/**
 * �������� ���� �� ���������� ������������� (������� ��.��.����)
 */
def getDate(def value) {
    if (isEmpty(value)) {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    return format.parse(value)
}

/**
 * ��������� �������� ��� ����� 12.
 *
 * @paam row ������ ��
 * @paam coef ����������
 * @paam reportDate �������� ����
 * @paam days ���������� ���� � ����
 */
def calc12Value(def row, def coef, def reportDate, def days) {
    def tmp = (row.salePrice * row.rateBR * coef) * ((reportDate - row.part1REPODate) / days) / 100
    return round(tmp, 2)
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
 * ���������� ����� ��� �������� �����.
 */
void setTotalStyle(def row) {
    ['tadeNumber', 'securityName', 'currencyCode', 'nominalPriceSecurities',
            'salePrice', 'acquisitionPrice', 'part1REPODate', 'part2REPODate',
            'income', 'outcome', 'rateBR', 'outcome269st', 'outcomeTax'].each {
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
 * �������� ���������� ���� � ���� �� ��������� ����.
 */
def getCountDaysInYaer(def date) {
    if (date == null) {
        return 0
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def year = date.format('yyyy')
    def end = format.parse("31.12.$year")
    def begin = format.parse("01.01.$year")
    return end - begin + 1
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
            logger.error("� ������ \"����� ������\" ������ $index �� ��������� ������� : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("� ������ $index �� ��������� ������� : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * �������� �������� ����.
 */
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
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
 * �������� �������� ��� ����� 9 � ����� 10
 *
 * @param row ������
 * @param reportDate �������� ����
 * @param course ����
 */
def calcAForColumn9or10(def row, def reportDate, def course) {
    // ((������ 6� - ������ 5�) � (�������� ���� � ������ 7�) / (������ 8� - ������ 7�)) � ���� �� ��
    def tmp = ((row.acquisitionPrice - row.salePrice) *
            (reportDate - row.part1REPODate) / (row.part2REPODate - row.part1REPODate)) * course
    return tmp
}