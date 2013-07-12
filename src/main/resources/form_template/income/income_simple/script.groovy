/**
 * ������� ����� " ������, ����������� � ������� ���" ������ ������������� �������������
 *
 * @since 6.06.2013
 * @author auldanov
 */

/**
 * ������ ������
 * 1. ��� - incomeTypeId
 * 2. ������ ������� - incomeGroup
 * 3. ��� ������ �� �������� - incomeTypeByOperation
 * 4. ���������� ���� �� ����� ������ - accountNo
 * 5. ���-6 (����� 10) c���� - rnu6Field10Sum
 * 6. ����� - rnu6Field12Accepted
 * 7. � �.�. ������ � ���������� ��������� �������� �� ����� 10 - rnu6Field12PrevTaxPeriod
 * 8. ���-4 (����� 5) ����� - rnu4Field5Accepted
 * 9. ���������� �������� - logicalCheck
 * 10. ���� �������������� ����� - accountingRecords
 * 11. � ���������� �5 - opuSumByEnclosure2
 * 12. � ������� "�" - opuSumByTableD
 * 13. � ������������� ���������� - opuSumTotal
 * 14. ����������� - difference
 *
 */


/**
 * ������� ����������
 * formDataEvent (com.aplana.sbrf.taxaccounting.model.FormDataEvent)
 */
switch (formDataEvent){
// TODO �������� �������
// �������
    case FormDataEvent.CREATE :
        checkCreation()
        break
// ���������
    case FormDataEvent.CALCULATE :
        logicalCheck()
        calcForm()
        break
// ��������
    case FormDataEvent.COMPOSE :
        consolidation()
        logicalCheck()
        calcForm()
        break
// ���������
    case FormDataEvent.CHECK :
        logicalCheck()
        calcForm()
        break
// ���������
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :
        logicalCheck()
        calcForm()
        break
// ������� �� ����������
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED :
        logicalCheck()
        calcForm()
        break
// ������� �� ������� � ����������
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED :
        break
// ������� �� �������
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :
        logicalCheck()
        calcForm()
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

/**
 * 6.1.2.8.2    ��������� ���������� ����� ����� ��� ������� ������ �����
 */
def calcForm(){
    /** ��� 40001 */
    def row40001 = formData.getDataRow("R53")
    (2..52).each{ n ->
        // ������ 5� =����� ��������  ������ 5� ��� ����� � 2 �� 52 (������ ������� �� ����������)
        row40001.rnu6Field10Sum = (row40001.rnu6Field10Sum?:0)  + (formData.getDataRow("R"+n).rnu6Field10Sum?:0)

        // ������ 6� =����� ��������  ������ 6� ��� ����� � 2 �� 52 (������ ������� �� ����������)
        row40001.rnu6Field12Accepted = (row40001.rnu6Field12Accepted?:0) + (formData.getDataRow("R"+n).rnu6Field12Accepted ?:0)

        // ������ 7� =����� ��������  ������ 7� ��� ����� � 2 �� 52 (������ ������� �� ����������)
        row40001.rnu6Field12PrevTaxPeriod = (row40001.rnu6Field12PrevTaxPeriod?:0) + (formData.getDataRow("R"+n).rnu6Field12PrevTaxPeriod ?:0)

        // ������ 8� =����� ��������  ������ 8� ��� ����� � 2 �� 52 (������ ������� �� ����������)
        row40001.rnu4Field5Accepted = (row40001.rnu4Field5Accepted?:0) + (formData.getDataRow("R"+n).rnu4Field5Accepted ?:0)
    }


    /** ��� 40002 */
    def row40002 = formData.getDataRow("R156")
    (55..155).each{ n ->
        // ������ 5� =����� ��������  ������ 5� ��� ����� � 55 �� 155 (������ ������������������ �������)
        row40001.rnu6Field10Sum = (row40001.rnu6Field10Sum?:0) + (formData.getDataRow("R"+n).rnu6Field10Sum?:0)

        // ������ 6� =����� ��������  ������ 6� ��� ����� � 55 �� 155 (������ ������������������ �������)
        row40001.rnu6Field12Accepted = (row40001.rnu6Field12Accepted?:0) + (formData.getDataRow("R"+n).rnu6Field12Accepted?:0)

        // ������ 7� =����� ��������  ������ 7� ��� ����� � 55 �� 155 (������ ������������������ �������)
        row40001.rnu6Field12PrevTaxPeriod = (row40001.rnu6Field12PrevTaxPeriod?:0) + (formData.getDataRow("R"+n).rnu6Field12PrevTaxPeriod?:0)

        // ������ 8� =����� ��������  ������ 8� ��� ����� � 55 �� 155 (������ ������������������ �������)
        row40001.rnu4Field5Accepted = (row40001.rnu4Field5Accepted?:0) + (formData.getDataRow("R"+n).rnu4Field5Accepted?:0)
    }
}

/**
 * 6.1.2.8.3.1  ���������� ��������
 * ��������� �� ����� � �� �� ������ �����,
 * �-�� ������ ����������� ���������
 */
def logicalCheck(){
    logger.info('-->');
    formData.dataRows.each{ row ->
        /**
         * ����� 9
         * ������ �����: ��� ������. ��� �����, ������������ ��� ����������� (��. ����. 12)
         *
         * �������� ����������:
         * ���� ����� <0, �� ������ 9�= ���������� ���������� � ����� ������ 9�= ����� ,���
         * �����= ������( ������5�-(������ 6�-������ 7�);2)
         */
        if (isCalcField(row.getCell("logicalCheck"))){
            row.logicalCheck = ((BigDecimal) ((row.rnu6Field10Sum?:0) - (row.rnu6Field12Accepted?:0) + (row.rnu6Field12PrevTaxPeriod?:0))).setScale(2, BigDecimal.ROUND_HALF_UP).toString() ?: "��������� ����������"
        }


        /**
         * ����� 11
         * ������ �����: ��� ������. ��� ������. ��� �����, ������������ ��� ����������� (��. ����. 12)
         *
         * �������� ����������:
         * ������ 11� = ����� �������� ������ 6� ����� �������� ����� ����������� ������� ������ ������������� ��������������(��. ������ 6.1.1)
         * ��� ��� �����, ��� ������� �������� ������ 4�  ����� �������� ������ 4� ������� ������
         *
         * TODO ����� ������ ��� ������ ��� ������ ���� ����� � �� ������� ���������
         * TODO ��� ��������� ������� ������� ��������� ������ �����?
         */
        if (isCalcField(row.getCell("opuSumByEnclosure2"))){
            // ������� ����� �������� ����� ����������� ������� ������ ������������� ��������������(��. ������ 6.1.1)
            def sum6ColumnOfForm302 = 0
            def formData302 = FormDataService.find(302, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
            if (formData302 != null){
                formData302.dataRows.each{ rowOfForm302 ->
                    if (rowOfForm302.incomeBuhSumAccountNumber == row.accountNo){
                        sum6ColumnOfForm302 += rowOfForm302.incomeBuhSumAccepted ?:0
                    }
                }
                row.opuSumByEnclosure2 = sum6ColumnOfForm302
            }
        }


        /**
         * ����� 12
         * ������ �����: ��� ������. ��� �����, ������������ ��� ����������� (��. ����. 12)
         *
         * �������� ����������:
         * ������ 12� = ����� �������� ������ 8� ��� ��� �����,
         * ��� ������� �������� ������ 4�  ����� �������� ������ 4� ������� ������.
         *
         * TODO ����� ������ ��� ������ ��� ������ ���� ����� � �� ������� ���������
         */
        if (isCalcField(row.getCell("opuSumByTableD"))){
            def sum8Column = 0
            formData.dataRows.each{ irow ->
                if (irow.accountNo == row.accountNo){
                    sum8Column += irow.rnu4Field5Accepted?:0
                }
            }
            row.opuSumByTableD = sum8Column
        }


        /**
         * ����� 13
         * ������ �����:
         * ��� ������. ��� �����, ������������ ��� ����������� (��. ����. 12)
         * �� ����������� �����  118-119, 141-142
         *
         * �������� ����������:
         * ������13� =  ����� �������� ���� ������, ��� ��� ���� ������� ������� � �������� � ��������  ��� ������� ����������� ��������� �������:
         * ���������� ������ ������ �� ������, ��� �������� ������������ ������� �����
         * �������� ���� ����� ��ӻ ������ � �������� � ������� ��������� �� ��������� ������ 10� ������� ������ ������� �����.
         */
        if (isCalcField(row.getCell("opuSumTotal")) && !(row.getAlias() in ['R118', 'R119', 'R141', 'R142'])){
            income102Dao.getIncome102(formData.reportPeriodId, row.accountingRecords, formData.departmentId).each{ income102 ->
                row.opuSumTotal = (row.opuSumTotal?:0) + income102.totalSum
            }
        }

        /**
         * ����� 13
         * ������ �����: 118-119, 141-142
         *
         * �������� ����������:
         *  ������13� =����� �������� ���� �������� �� ������, ��� ��� ���� ������� ���������� ���������, ��� ������� ����������� ��������� �������:
         *  ���������� ������ ������ �� ������, ��� �������� ������������ ������� �����
         *  �������� ���� ������ ����� ��������� ��������� �� ��������� ������ 10� ������� ������ ������� �����.
         */
        if (row.getAlias() in ['R118', 'R119', 'R141', 'R142']){
            income101Dao.getIncome101(formData.reportPeriodId, row.accountingRecords, formData.departmentId).each{ income101 ->
                row.opuSumTotal =  (row.opuSumTotal?:0) + income101.debetRate
            }
        }




        /**
         * ����� 14
         * ������ �����:
         * ��� ������. ��� �����, ������������ ��� ����������� (��. ����. 12)
         * �� ����������� �����  118-119, 141-142
         *
         * �������� ����������:
         *  ������ 14� = (������ 11� + ������ 12�) � ������ 13�
         */
        if (isCalcField(row.getCell("difference")) && !(row.getAlias() in ['R118', 'R119', 'R141', 'R142'])){
            row.difference = (row.opuSumByEnclosure2?:0) + (row.opuSumByTableD ?:0)- (row.opuSumTotal ?:0)
        }


        /**
         * ����� 14
         * ������ �����:
         * 118-119
         *
         * �������� ����������:
         *  ������ 14� = ������ 13� - ������ 8�
         */
        if (row.getAlias() in ['R118', 'R119']){
            row.difference = (row.opuSumTotal?:0) - (row.rnu4Field5Accepted?:0)
        }

        /**
         * ����� 14
         * ������ �����:
         * 141-142
         *
         * �������� ����������:
         *  ������ 14� = ������ 13� - ( �+ �)
         *   � � �������� ������ 8� ��� ������ 141
         *   � � �������� ������ 8� ��� ������ 142
         */
        if (row.getAlias() in ['R141', 'R142']){
            row.difference = (row.opuSumTotal?:0) - ( (formData.getDataRow("R141").rnu4Field5Accepted?:0) + (formData.getDataRow("R142").rnu4Field5Accepted?:0))
        }
    }
    logger.info('<--');
}

/**
 * ������� ���������� ��� ������ ����������� ��� ��� ���
 * @param cell
 * @return
 */
def isCalcField(Cell cell){
    return cell.getStyleAlias() == "����������� �����"
}

/**
 * ������� �������� �������� �� ������ �������������
 * @param cell
 * @return
 */
def isEditableField(Cell cell){
    return cell.getStyleAlias() == "�������������"
}

/**
 * ������������ �����
 */
def consolidation(){
    if (!isTerBank()) {
        return
    }

    // �������� �����
    formData.getDataRows().each{ row ->
        ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted', 'logicalCheck', 'accountingRecords'].each{ alias->
            row.getCell(alias).setValue(null)
        }
    }
    // �������� ������ �� ����������
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
        def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            child.getDataRows().eachWithIndex() { row, i ->
                def rowResult = formData.getDataRows().get(i)
                ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted', 'logicalCheck', 'accountingRecords'].each {
                    if (row.getCell(it).getValue() != null) {
                        if (isCalcField(row.getCell(it)) || isEditableField(row.getCell(it)))
                            rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                    }
                }
            }
        }
    }
}


/**
 * ������ ��� �������� ��������.
 *
 * @author auldanov
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


// ������� ��������������� ��� ������ ������� ������� ��� formData
BigDecimal summ(ColumnRange cr) {
    return summ(formData, cr, cr, {return true;})
}

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