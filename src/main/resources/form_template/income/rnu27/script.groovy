import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * 6.12 (���-27) ������� ���������� ����� ������� ������� ��� ��������� ����������� �������������� � ������������� ���������, �����, ������������� �� � ������ ��������� � ����� ���������������
 * ��� http://conf.aplana.com/pages/viewpage.action?pageId=8588102 ���_�������_��_�2_�1_�2.doc
 * @author ekuvshinov
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK:
        allCheck()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        allCheck()
        break
    case FormDataEvent.ADD_ROW:
        //deleteAllStatic()
        addNewRowwarnrmData()
        break
    case FormDataEvent.DELETE_ROW:
        //deleteAllStatic()
        deleteRow()
        break
    // ����� �������� �� ������������
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        allCheck()
        break
    // ��������
    case FormDataEvent.COMPOSE :
        consolidation()
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        allCheck()
        break
}


void log(String message, Object... args) {
    logger.warn(message, args)
}

// ����� 1  - �����  number � ��
// ����� 2  - ������ issuer ������
// ����� 3  - ������ regNumber ��� �����
// ����� 4  - ������ tradeNumber ����� ������
// ����� 5  - ������ currency ������ ������� ���������
// ����� 6  - �����  prev ������ ���� �� �������� ���� �� ������������� ����� (��.). ����������
// ����� 7  - �����  current ������ ���� �� �������� ���� �� ������������� ����� (��.). �������
// ����� 8  - �����  reserveCalcValuePrev ��������� �������� ������� �� ���������� �������� ���� (���.���.)
// ����� 9  - �����  cost ��������� �� ���� ������������ (���.���.)
// ����� 10 - ������ signSecurity ������� ������ ������ �� ������� �������� ����
// ����� 11 - �����  marketQuotation Quotation �������� ��������� ����� ������ ������ � ����������� ������
// ����� 12 - �����  rubCourse ���� ����� � ������ �������� ���������
// ����� 13 - �����  marketQuotationInRub �������� ��������� ����� ������ ������ � ������
// ����� 14 - �����  costOnMarketQuotation costOnMarketQuotation
// ����� 15 - �����  reserveCalcValue ��������� �������� ������� �� ������� �������� ���� (���.���.)
// ����� 16 - �����  reserveCreation �������� ������� (���.���.)
// ����� 17 - �����  recovery �������������� ������� (���.���.)

/**
 * 6.11.2.4.1   ���������� ��������
 */
void logicalCheck() {
    formPrev
    for (DataRow row in formData.dataRows) {
        if (row.getAlias() == null) {
            if (row.currency == 0) {
                // LC �������� ��� ������� �������� ������� ���� �� ������� �������� ���� (����� 7 = 0)
                if (row.reserveCalcValuePrev != row.currency) {
                    logger.warn("����� 8 � 17 �������!")
                }
                // LC � �������� ��� ������� �������� ������� ���� �� ������� �������� ���� (����� 7 = 0)
                if (row.cost != row.costOnMarketQuotation || row.cost != row.reserveCalcValue || row.cost == 0) {
                    logger.warn("����� 9, 14 � 15 ���������!")
                }
            }
            // LC � �������� ��� ������� �������� ������� ���� �� ���������� �������� ���� (����� 6 = 0)
            if (row.prev == 0 && (row.reserveCalcValuePrev != row.recovery || row.recovery != 0)) {
                logger.error("����� 8 � 17 ���������!")
            }
            // LC � �������� �������������� ��������� (����� 10 = �x�)
            if (row.signSecurity == "x" && (row.reserveCalcValue != row.reserveCreation || row.reserveCreation != 0)) {
                logger.warn("��������� ��������������, ����� 15 � 16 ���������!")
            }
            if (row.signSecurity == "+") {
                // LC � �������� �������� (��������������) ������� �� ������������ ���������� (����� 10 = �+�)
                if (row.reserveCalcValue - row.reserveCalcValuePrev > 0 && row.recovery != 0) {
                    logger.error("��������� ������������ � ������ ����������� (������������) �����������!")
                }
                // LC � �������� �������� (��������������) ������� �� ������������ ���������� (����� 10 = �+�)
                if (row.reserveCalcValue - row.reserveCalcValuePrev < 0 && row.reserveCreation != 0) {
                    logger.error("��������� ������������ � ������ ����������� (������������) �����������!")
                }
                // LC � �������� �������� (��������������) ������� �� ������������ ���������� (����� 10 = �+�)
                if (row.reserveCalcValue - row.reserveCalcValuePrev == 0 && (row.reserveCreation != 0 || row.recovery != 0)) {
                    logger.error("��������� ������������ � ������ ����������� (������������) �����������!")
                }
            }
            // LC � �������� ������������ ������������ �������
            if (row.reserveCalcValuePrev != null && row.reserveCreation != null && row.reserveCalcValue != null && row.recovery != null
                    && row.reserveCalcValuePrev + row.reserveCreation != row.reserveCalcValue + row.recovery) {
                logger.error("������ ����������� �������!")
            }
            // LC � �������� �� ������������� �������� ��� ������� ���������� �������
            if (row.reserveCreation > 0 && (row.current < 0 || row.cost || row.costOnMarketQuotation < 0 || row.reserveCalcValue < 0)) {
                logger.warn("������ �����������. ����� 7, 9, 14 � 15 ���������������!")
            }
            // LC � �������� ������������ ���������� ���
            if (formPrev != null) {
                for (DataRow rowPrev in formPrev.dataRows) {
                    if (row.tradeNumber == rowPrev.tradeNumber && row.prev != rowPrev.current) {
                        logger.warn("��� ����������� �����������! �� ����������� �������: ����  ������  4� = ������ 4� ����� ���-27 �� ���������� �������� ������, �� ������ 6�  = ������ 7� ����� ���-27 �� ���������� �������� ������")
                    }
                }
            }
            // LC � �������� ������������ ���������� ���
            if (formPrev != null) {
                for (DataRow rowPrev in formPrev.dataRows) {
                    if (row.tradeNumber == rowPrev.tradeNumber && row.reserveCalcValuePrev != rowPrev.reserveCalcValue) {
                        logger.error("��� ����������� �����������! �� ����������� �������: ����  ������  4� = ������ 4� ����� ���-27 �� ���������� �������� ������, �� ����� 8  = ����� 15 ����� ���-27 �� ���������� �������� ������")
                    }
                }
            }

            // LC �������� �� ���������� ���� �<������������ ����>�
            // @FIXME ���� production ������ ����, ������ ����� �������� �������� ����� �����������
//            for (alias in ['number', 'issuer', 'regNumber', 'tradeNumber', 'currency', 'reserveCalcValuePrev',
//                    'marketQuotationInRub', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation']) {
//                if (row.getCell(alias).value == null) {
//                    setError(row.getCell(alias).column)
//                }
//            }
            for (alias in ['number', 'issuer', 'regNumber', 'tradeNumber', 'currency', 'reserveCalcValuePrev',
                    'reserveCalcValue', 'reserveCreation']) {
                if (row.getCell(alias).value == null) {
                    setError(row.getCell(alias).column)
                }
            }
            //�������� �� ������������ ���� �� ��
            if (row.currency == 'RUR') {
                // LC �������� ����� 11
                if (row.marketQuotation != null) {
                    logger.error("������� ��������� ����� ��������� ��������� ����� ������ ������ � ����������� ������!")
                }
                // LC �������� ����� 12
                if (row.rubCourse != null) {
                    logger.error("������� ��������� ����� ����� ����� � ������ �������� ���������!")
                }
            }
            // LC �������������� �������� ����� 13
            if (row.marketQuotation != null && row.rubCourse
                    && row.marketQuotationInRub != round((BigDecimal) (row.marketQuotation * row.rubCourse), 2)) {
                logger.error("������� ���������� ����� ��������� ��������� ����� ������ ������ � �������!")
            }
            // @todo LC 20 - 22
        }
    }
    // LC � �������� ������������ ���������� ���
    if (formPrev != null) {
        DataRow itogoPrev = formPrev.getDataRow('itogo')
        DataRow itogo = formData.getDataRow('itogo')
        if (itogo != null && itogoPrev != null && itogo.prev != itogoPrev.current) {
            logger.error("��� ����������� �����������! �� ����������� �������: ������ �� ����� 6 = ������ �� ����� 7 ����� ���-27 �� ���������� �������� ������")
        }
    }
    // LC � �������� ������������ ���������� ���
    if (formPrev != null) {
        DataRow itogoPrev = formPrev.getDataRow('itogo')
        DataRow itogo = formData.getDataRow('itogo')
        if (itogo != null && itogoPrev != null && itogo.reserveCalcValuePrev != itogoPrev.reserveCalcValue) {
            logger.error("��� ����������� �����������! �� ����������� �������: ������ �� ����� 8 = ������ �� ����� 15 ����� ���-27 �� ���������� �������� ������")
        }
    }

    /** @todo LC �������� �� ������� ��������� ������ ���������� �������� �������� (����� 15) � ������� �������� ������� (����������� ���� ��� ��� ����� ����������)
     * http://jira.aplana.com/browse/SBRFACCTAX-2609
     */
    if (formPrev != null) {
        List notFound = []
        List foundMany = []
        for (DataRow rowPrev in formPrev.dataRows) {
            if (rowPrev.getAlias() != null && rowPrev.reserveCalcValue > 0) {
                int count = 0
                for (DataRow row in formData.dataRows) {
                    if (row.getAlias() != null && row.tradeNumber == rowPrev.tradeNumber) {
                        count++
                    }
                }
                if (count == 0) {
                    notFound.add(rowPrev.tradeNumber)
                }
                if (count != 0 && count != 1) {
                    foundMany.add(rowPrev.tradeNumber)
                }
            }
        }
        if (!notFound.isEmpty()) {
            StringBuilder sb = new StringBuilder("����������� ������ � �������� ������ :")
            for (tradeNumber in notFound) {
                sb.append(" " + tradeNumber.toString() + ",")
            }
            String message = sb.toString()
            logger.warn(message.substring(0, message.length() - 1))
        }
        if (!foundMany.isEmpty()) {
            StringBuilder sb = new StringBuilder("����������� ������ � �������� ������ :")
            for (tradeNumber in foundMany) {
                sb.append(" " + tradeNumber.toString() + ",")
            }
            String message = sb.toString()
            logger.warn(message.substring(0, message.length() - 1))
        }
    }
}

void allCheck() {
    logicalCheck()
}

/**
 * ����������� ����������� ������
 */
void addAllStatic() {
    itogoColumns = ['prev', 'current', 'reserveCalcValuePrev', 'cost', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']
    if (!logger.containsLevel(LogLevel.ERROR)) {
        clearItogo = { Map<String, BigDecimal> itogo ->
            for (String column in itogoColumns) {
                itogo.put(column, new BigDecimal(0))
            }
        }
        addItogo = { Map<String, BigDecimal> itogo, int position ->
            DataRow<Cell> newRow = formData.createDataRow()
            for (column in itogoColumns) {
                newRow.getCell(column).value = itogo.get(column)
            }
            formData.dataRows.add(position, newRow)
            return newRow
        }
        sumItogo = { Map<String, BigDecimal> itogo, DataRow<Cell> row ->
            for (column in itogoColumns) {
                if (row.get(column) != null) {
                    itogo.put(column, itogo.get(column) + (BigDecimal) row.get(column))
                }
            }
        }
        getNextRow = { int from ->
            result = null
            int size = formData.dataRows.size()
            for (int i = from; i < size; i++) {
                DataRow row = formData.dataRows.get(i)
                if (row.getAlias() == null) {
                    return row
                }
            }
            return result
        }
        Map<String, BigDecimal> itogoRegNumber = [:]
        Map<String, BigDecimal> itogoIssuer = [:]
        clearItogo(itogoIssuer)
        clearItogo(itogoRegNumber)
        DataRow<Cell> rowItogo = formData.createDataRow()
        rowItogo.issuer = "����� �����"
        rowItogo.setAlias('itogo')
        for (column in itogoColumns) {
            rowItogo.getCell(column).value = new BigDecimal(0)
        }
        for (int i = 0; i < formData.dataRows.size(); i++) {
            DataRow<Cell> row = formData.dataRows.get(i)
            DataRow<Cell> nextRow = getNextRow(i + 1)
            int j = 0

            sumItogo(itogoRegNumber, row)
            sumItogo(itogoIssuer, row)
            for (column in itogoColumns) {
                if (row.getCell(column).value != null) {
                    rowItogo.getCell(column).value += row.getCell(column).value
                }
            }

            if (row.getAlias() == null && nextRow == null || row.issuer != nextRow.issuer) {
                DataRow<Cell> newRow = addItogo(itogoIssuer, i + 1)
                newRow.getCell('issuer').colSpan = 2
                newRow.issuer = row.issuer.toString().concat(' ����')
                newRow.setAlias('itogoIssuer#'.concat(i.toString()))
                clearItogo(itogoIssuer)
                j++
            }

            if (row.getAlias() == null && nextRow == null || row.regNumber != nextRow.regNumber || row.issuer != nextRow.issuer) {
                DataRow<Cell> newRow = addItogo(itogoRegNumber, i + 1)
                newRow.getCell('regNumber').colSpan = 2
                newRow.regNumber = row.regNumber.toString().concat(' ����')
                newRow.setAlias('itogoRegNumber#'.concat(i.toString()))
                clearItogo(itogoRegNumber)
                j++
            }

            i += j  // ����������� ����� �������� ������������ � �����������

        }
        formData.dataRows.add(rowItogo)
    }
}

/**
 * 3.1.1.1  ��������� ���������� ����� �����
 * ����. 59 ��������� ���������� ����� ����� �������� ���������� ����� ������� ������� ��� ��������� ����������� �������������� � ������������� ���������, �����, ������������� �� � ������ ��������� � ����� ����������������
 */

void calc() {
    for (row in formData.dataRows) {
        // �������� ����� ������� ������� ��� �� ��� ����������
        for (alias in ['issuer', 'regNumber', 'tradeNumber']) {
            if (row.getCell(alias).value == null) {
                setError(row.getCell(alias).column)
            }
        }
    }
    if (!logger.containsLevel(LogLevel.ERROR)) {
        BigDecimal i = 0
        formPrev
        for (DataRow row in formData.dataRows) {
            i++
            row.number = i  // @todo http://jira.aplana.com/browse/SBRFACCTAX-2548 ���������
            row.currency = 'RUR'// @todo  ������ ����� 5 ����� http://jira.aplana.com/browse/SBRFACCTAX-2376 ������ ��������� �������������

            // ������ ����� 8 � ����������� ���������� ��������� http://jira.aplana.com/browse/SBRFACCTAX-2562
            temp = new BigDecimal(0)
            tempCount = 0
            if (formPrev != null) {
                for (DataRow rowPrev in formPrev.dataRows) {
                    if (row.tradeNumber == rowPrev.tradeNumber) {
                        temp = rowPrev.reserveCalcValue
                        tempCount++
                    }
                }
            }
            if (tempCount == 1) {
                row.reserveCalcValuePrev = temp
            } else {
                row.reserveCalcValuePrev = 0
            }

            if (row.currency == 'RUR') {
                row.marketQuotation = null
            }
            if (row.currency == 'RUR') {
                row.rubCourse = null
            }
            if (row.marketQuotation != null && row.rubCourse != null) {
                row.marketQuotationInRub = round((BigDecimal) (row.marketQuotation * row.rubCourse), 2)
            }
        }
    }
}

/**
 * ��������� ����� � ���������� � ������������ 6.11.2.1 �������� ����� �����
 */
void sort() {
    formData.dataRows.sort({ DataRow a, DataRow b ->
        if (a.issuer == b.issuer && a.regNumber == b.regNumber) {
            return a.tradeNumber <=> b.tradeNumber
        }
        if (a.issuer == b.issuer) {
            return a.regNumber <=> b.regNumber
        }
        return a.issuer <=> b.issuer
    })
}

/**
 * ������� ������ �� �����
 */
void deleteRow() {
    if (currentDataRow != null && currentDataRow.getAlias() == null) {
        formData.dataRows.remove(currentDataRow)
    }
}

/**
 * ������� ��� ����������� ������(�����) �� ���� �����
 */
void deleteAllStatic() {
    Iterator<DataRow> iterator = formData.dataRows.iterator() as Iterator<DataRow>
    while (iterator.hasNext()) {
        row = (DataRow) iterator.next()
        if (row.getAlias() != null) {
            iterator.remove()
        }
    }
}

/**
 * ������ ��� ���������� �����
 * @param value
 * @param newScale
 * @return
 */
BigDecimal round(BigDecimal value, int newScale) {
    if (value != null) {
        return value.setScale(newScale, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

void setError(Column c) {

    if (!c.name.empty) {
        logger.error('���� ' + c.name.replace('%', '') + ' �� ���������')
    }
}

/**
 * ������� ������ � ������ ���� ����� �������� ����������� ������ ����� (�� ������ ������ �������� �������������)
 */
void addNewRowwarnrmData() {
    DataRow<Cell> newRow = formData.createDataRow()
    int index // ����� ����� ������� �������

    if (formData.dataRows.size() > 0) {
        DataRow<Cell> selectRow
        // ����� �� ������
        log("����� �� ������")
        log("size = " + formData.dataRows.size())
        if (currentDataRow != null && formData.dataRows.indexOf(currentDataRow) != -1) {
            // ������ ������ ������ ���� ���������
            log("������ ������� �������")
            log("indexOf = " + formData.dataRows.indexOf(currentDataRow))
            selectRow = currentDataRow
        } else {
            // ������ �� ������ ������� ��������� � ����� �����
            log("������ ������� �� �������, �������� � ����� �����")
            selectRow = formData.dataRows.get(formData.dataRows.size() - 1) // ������� � �����
        }

        int indexSelected = formData.dataRows.indexOf(selectRow)
        log("indexSelected = " + indexSelected.toString())

        // ��������� ������ ��� ���������� �����
        if (selectRow.getAlias() == null) {
            // ������� ������ �� �����
            log("������� ������ �� �����")
            index = indexSelected // �������� �� �� ����� ����� ������
        } else {
            // ������� ������ �����, ��� ����������� ����� ����� ��� ����� � ���� ������� ��� ���� �����
            // ��� ������������ ����� ����� ��� ����� ���� �� �������� ����� ����� ��� ������ �� �����
            log("������� ������ �����")

            for (index = indexSelected; index >= 0; index--) {
                log("loop index = " + index.toString())
                if (formData.dataRows.get(index).getAlias() == null) {
                    log("����� ������ �������� �� �����")
                    index++
                    break
                }
            }
            if (index < 0) {
                // ������ ���� ������ ����� ��� �����, ������� ����� � ������
                log("���� ������ ����� ��� �����")
                index = 0
            }
            log("result index = " + index.toString())
        }
    } else {
        // ����� ������ ������� �������� ������ � ������
        log("����� ������ ������� �������� ������ � ������")
        index = 0
    }
    formData.dataRows.add(index, newRow)
    [
            'issuer', 'regNumber', 'tradeNumber', 'prev', 'current', 'reserveCalcValuePrev', 'cost', 'signSecurity',
            'marketQuotation', 'rubCourse', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery'
    ].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('�������������')
    }
}

FormData getFormPrev() {
    reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    FormData formPrev = null
    if (reportPeriodPrev != null) {
        formPrev = FormDataService.find(formData.getFormType().id, FormDataKind.PRIMARY, formData.departmentId, reportPeriodPrev.id)
    }
    return formPrev
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