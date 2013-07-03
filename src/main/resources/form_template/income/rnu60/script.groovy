import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * 6.41 (���-60) ������� ���������� ����� �������� ������ ���� � �������������� ������� �� 2-� �����
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
        addNewRowwarnrmData()
        break
    case FormDataEvent.DELETE_ROW:
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
    //logger.warn(message, args)
}

/**

 � �����    ALIAS               ������������ ����                                                           ��� ����        ������
 1.         tradeNumber         ����� ������ ������ ����� / ������ �����                                    ������ /41/
 2.         securityName        ������������ ������ ������                                                  ������ /255/
 3.         currencyCode        ��� ������                                                                  ������ /3/                      ������ ��������� �������� ���� ���� ������. �������� ����������� ��������������� ������������� �����          ��� ��
 4.         nominalPrice        ����������� ��������� ������ ����� (��. ���.)                               �����/17.2/
 5.         part1REPODate       ���� ������ ����� ����                                                      ����            ��.��.���
 6.         part2REPODate       ���� ������ ����� ����                                                      ����            ��.��.���
 7.         acquisitionPrice    ��������� ����������, � �.�. ���, �� ������ ����� ���� (���.���.)           �����/17.2/
 8.         salePrice           ��������� ������������, � �.�. ���, �� ������ ����� ���� (���.���.)         �����/17.2/
 9.         income              ������ (-) �� ������ ���� (���.���.)                                        �����/17.2/
 10.        outcome             ������� (+) �� ������ ���� (���.���.)                                       �����/17.2/
 11.        rateBR              ������ ����� ������ (%)                                                     �����/17.2/
 12.        outcome269st        ������� �� ������ ����, ������������ � ������ ��. 269 �� �� (���.���.)      �����/17.2/
 13.        outcomeTax          ������� �� ������ ����, ����������� ��� ����� ��������������� (���.���.)    �����/17.2/

 */

void allCheck() {
    logicalCheck()
}

/**
 * 6.41.3.2.1   ���������� ��������
 * ����. 209 ���������� �������� ����� �������� ���������� ����� �������� ������ ���� � �������������� ������� �� 2-� �����
 */
void logicalCheck() {
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            // 1. �������� �� ���������� ���� �<������������ ����>�
            for (alias in ['outcome269st', 'outcomeTax']) {
                if (row.getCell(alias).value == null) {
                    setError(row.getCell(alias).column)
                }
            }
            // 2. �������� ���� ������ ����� ����
            if (row.part1REPODate != null && reportDate.time.before((Date) row.part1REPODate)) {
                log(reportDate.time.toString())
                logger.error("������� ������� ���� ������ ����� ������!")
            }
            // 3. �������� ���� ������ ����� ����
            if (row.part2REPODate != null
                    && (reportPeriodService.getStartDate(formData.reportPeriodId).time.after((Date) row.part2REPODate) || reportPeriodService.getEndDate(formData.reportPeriodId).time.before((Date) row.part2REPODate)
            )) {
                log(reportPeriodService.getStartDate(formData.reportPeriodId).time.toString())
                log(reportPeriodService.getEndDate(formData.reportPeriodId).time.toString())
                logger.error("������� ������� ���� ������ ����� ������!")
            }

            if (row.outcome > 0 && row.income > 0) {
                logger.error("��������� ����������� ����������!")
            }

            // 6. �������� ����������� ����������
            if (row.outcome == 0 && !(row.outcome269st == 0 && row.outcomeTax == 0)) {
                logger.error("��������� ����������� ����������!")
            }

            // 7. �������� ����������� ����������
            BigDecimal temp = (row.salePrice ?: 0) - (row.acquisitionPrice ?: 0)
            if (temp < 0 && !(temp.abs() == row.income)) {
                logger.warn("������� ���������� ������")
            }

            // 8. �������� ����������� ����������
            if (temp > 0 && !(temp == row.outcome)) {
                log("temp = " + temp.toString())
                log("outcome = " + row.outcome.toString())
                logger.warn("������� ���������� �������")
            }

            // 9. �������������� �������� ���� 9, 10, 11, 12, 13
            List checks = ['income', 'outcome', 'rateBR', 'outcome269st', 'outcomeTax']
            Map<String, BigDecimal> value = [:]
            value.put('income', calc9(row))
            value.put('outcome', calc10(row))
            value.put('rateBR', calc11(row))
            value.put('outcome269st', calc12(row))
            value.put('outcomeTax', calc13(row))
            for (String check in checks) {
                if (row.getCell(check).value != value.get(check)) {
                    log("calc = " + value.get(check).toString())
                    log("row = " + row.getCell(check).value.toString())
                    logger.error("������� ���������� ����� " + row.getCell(check).column.name.replace('%', '') + "!")
                }
            }
        }
    }
    // 10. �������� �������� �������� �� ���� �����
    List itogoSum = ['nominalPrice', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax']
    DataRow realItogo = formData.dataRows.get(formData.dataRows.size() - 1)
    itogo
    for (String alias in itogoSum) {
        if (realItogo.getCell(alias).value != itogo.getCell(alias).value) {
            log("columnn = " + itogo.getCell(alias).column.name.toString())
            log("real = " + realItogo.getCell(alias).value.toString())
            log("calc = " + itogo.getCell(alias).value.toString())
            logger.error("�������� �������� ���������� �������!")
            break
        }
    }
}

void setError(Column c) {

    if (!c.name.empty) {
        logger.error('���� ' + c.name.replace('%', '') + ' �� ���������')
    }
}

Calendar getReportDate() {
    Calendar periodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)
    Calendar reportingDate = periodEndDate
    reportingDate.set(Calendar.DATE, reportingDate.get(Calendar.DATE) + 1)
    return reportingDate
}

/**
 * ����������� ����������� ������
 */
void addAllStatic() {
    formData.dataRows.add(itogo)
}

/**
 * �������� ������ �����
 * @return
 */
DataRow<Cell> getItogo() {
    DataRow<Cell> itogo = formData.createDataRow()
    itogo.setAlias('itogo')
    itogo.securityName = "�����"
    List itogoSum = ['nominalPrice', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax']
    for (DataRow row in formData.dataRows) {
        if (row.getAlias() == null) {
            for (String name in itogoSum) {
                if (itogo.getCell(name).value == null) {
                    itogo.getCell(name).value = row.getCell(name).value ?: 0
                } else {
                    itogo.getCell(name).value += row.getCell(name).value ?: 0
                }
            }
        }
    }
    return itogo
}

BigDecimal calc9(DataRow row) {
    BigDecimal result
    BigDecimal a = (row.salePrice ?: 0) - (row.acquisitionPrice ?: 0)
    BigDecimal c = a.abs().setScale(2, BigDecimal.ROUND_HALF_UP)

    /**
     * ����  .A>0, ��
     ������ 9� = 0
     ������ 10� = B
     ����� ����  A<0
     ������ 9� = C
     ������ 10� = 0
     �����
     ������ 9�= ������ 10� = 0

     ���
     A=������8� - ������7�
     B=������(A;2),
     C= ������(ABS(A);2),

     ABS() � �������� ��������� ������(����������� ��������)  �����.

     ���������� ������� ����� � �������� ��� � ���������, �� ��� ��� ���� ����� ����������
     ������� ���������������� �������

     */
    if (a < 0) {
        result = c
    } else {
        result = 0
    }
    return round(result, 2)
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

BigDecimal calc10(DataRow row) {
    BigDecimal result
    BigDecimal a = (row.salePrice ?: 0) - (row.acquisitionPrice ?: 0)
    BigDecimal b = a.setScale(2, BigDecimal.ROUND_HALF_UP)

    /**
     * ����  .A>0, ��
     ������ 9� = 0
     ������ 10� = B
     ����� ����  A<0
     ������ 9� = C
     ������ 10� = 0
     �����
     ������ 9�= ������ 10� = 0

     ���
     A=������8� - ������7�
     B=������(A;2),
     C= ������(ABS(A);2),

     ABS() � �������� ��������� ������(����������� ��������)  �����.

     ���������� ������� ����� � �������� ��� � ���������, �� ��� ��� ���� ����� ����������
     ������� ���������������� �������

     */

    if (a > 0) {
        result = b
    } else {
        result = 0
    }
    return round(result, 2)
}

BigDecimal calc11(DataRow row) {
    Date date01_09_2008 = new Date(1220227200000)       // 1220227200000 - 01.09.2008 00:00 GMT
    Date date31_12_2009 = new Date(1262217600000)       // 1262217600000 - 31.12.2009 00:00 GMT
    Date date01_01_2011 = new Date(1293840000000)       // 1293840000000 - 01.01.2011 00:00 GMT
    Date date31_12_2012 = new Date(1356912000000)       // 1356912000000 - 31.12.2012 00:00 GMT
    BigDecimal result
    BigDecimal stavka = new BigDecimal(5)   // @todo ������ ����������������  block http://jira.aplana.com/browse/SBRFACCTAX-2711
    /**
     * � �������� ��������� ��� ���������� �� ��� ������ �� ������� � ��� ���� ��� �����������.
     if (row.outcome == 0) {result = null}if (row.currencyCode == null) {result = null}*/
    if (row.currencyCode == 810) {
        result = stavka
    } else {
        if (row.part2REPODate != null && row.part2REPODate.compareTo(date01_09_2008) >= 0 && row.part2REPODate.compareTo(date31_12_2009) <= 0) {
            result = 22
        } else if (row.part2REPODate != null && row.part2REPODate.compareTo(date01_01_2011) >= 0 && row.part2REPODate.compareTo(date31_12_2012) <= 0) {
            result = stavka
        } else {
            result = 15
        }
    }
    return round(result, 2)
}

BigDecimal calc12(DataRow row) {
    Date date01_09_2008 = new Date(1220227200000)       // 1220227200000 - 01.09.2008 00:00 GMT
    Date date31_12_2009 = new Date(1262217600000)       // 1262217600000 - 31.12.2009 00:00 GMT
    Date date01_01_2011 = new Date(1293840000000)       // 1293840000000 - 01.01.2011 00:00 GMT
    Date date31_12_2012 = new Date(1356912000000)       // 1356912000000 - 31.12.2012 00:00 GMT
    Date date01_01_2010 = new Date(1262282400000)       // 1262282400000 - 01.01.2010 00:00 GMT
    Date date30_06_2010 = new Date(1277834400000)       // 1277834400000 - 30.06.2010 00:00 GMT
    Date date01_11_2009 = new Date(1257012000000)       // 1257012000000 - 01.11.2009 00:00 GMT
    BigDecimal result = null
    if (row.outcome != null && row.outcome > 0) {
        long difference = row.part2REPODate.getTime() - row.part1REPODate.getTime() / (1000 * 60 * 60 * 24) // ���������� �������� ���-�� � ����
        difference = difference == 0 ? 1 : difference   // ��� ���������� ��� ���� ����� �������� ������� � ����, ���� ��� ������� ������� ������ 1 ��� �������� � ���
        // @todo �������� ���� ��������� 365 �� 366 ������ � ����� ��� ��������� �� ������������� http://jira.aplana.com/browse/SBRFACCTAX-2844
        if (row.currencyCode == 810) {
            if (row.part2REPODate.compareTo(date01_09_2008) >= 0 && row.part2REPODate.compareTo(date31_12_2009) <= 0) {
                /*
                a.  ���� ������ 6� ����������� ������� � 01.09.2008 �� 31.12.2009, ��:
                    ������ 12� = (������ 7� ? ������ 11� ? 1,5) ? ((������6� - ������5�) / 365 (366)) / 100;

                 */
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 1.5) * (difference / countDaysInYear) / 100
            } else if (row.part2REPODate.compareTo(date01_01_2010) >= 0 && row.part2REPODate.compareTo(date30_06_2010) <= 0 && row.part1REPODate.compareTo(date01_11_2009) < 0) {
                /*
                b.  ���� ������ 6� ����������� ������� � 01.01.2010 �� 30.06.2010 � ������������ ������ ������� �� 01.11.2009 (������ 5� < 01.11.2009 �.), ��
                    ������ 12� = (������ 7� ? ������ 11� ? 2) ? ((������ 6� - ������ 5�) / 365 (366)) / 100;
                 */
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 2) * (difference / countDaysInYear) / 100
            } else if (row.part2REPODate.compareTo(date01_01_2010) >= 0 && row.part2REPODate.compareTo(date31_12_2012) <= 0) {
                /*
                c.  ���� ������ 6� ����������� ������� � 01.01.2010 �� 31.12.2012, ��:
                    ������ 12� = (������ 7� ? ������ 11� ? 1,8) ? ((������6� - ������5�) / 365(366)) / 100.
                 */
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 1.8) * (difference / countDaysInYear) / 100
            } else {
                /*
                d.  �����
                    ������ 12� = (������ 7� ? ������ 11� ? 1,1) ? ((������ 6� -� ����� 5�) / 365 (366)) / 100;.
                 */
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 1.1) * (difference / countDaysInYear) / 100
            }
        } else {
            result = (row.acquisitionPrice * (row.rateBR ?: 0)) * (difference / countDaysInYear) / 100
            if (row.part2REPODate.compareTo(date01_01_2011) >= 0 && row.part2REPODate.compareTo(date31_12_2012) <= 0) {
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 0.8) * (difference / countDaysInYear) / 100
            }
        }
    }
    if (row.outcome != null && row.outcome == 0) {
        result = 0
    }
    return round(result, 2)
}

BigDecimal calc13(DataRow row) {
    BigDecimal result = null
    if (row.outcome > 0) {
        if (row.outcome <= row.outcome269st) {
            result = row.outcome
        }
        if (row.outcome > row.outcome269st) {
            result = row.outcome269st
        }
    }
    if (row.outcome == 0) {
        result = 0
    }
    return result
}

/**
 * ����. 207 ��������� ���������� ����� ����� �������� ���������� ����� �������� ������ ���� � �������������� ������� �� 2-� �����
 */
void calc() {
    for (DataRow row in formData.dataRows) {
        if (row.getAlias() == null) {
            row.income = calc9(row)
            row.outcome = calc10(row)
            row.rateBR = calc11(row)
            row.outcome269st = calc12(row)
            row.outcomeTax = calc13(row)
        }
    }
}

/**
 * ���������� ���� � ���� �� ������� ������
 * @return
 */
int getCountDaysInYear() {
    Calendar periodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    return countDaysOfYear = (new GregorianCalendar()).isLeapYear(periodStartDate.get(Calendar.YEAR)) ? 365 : 366
}

/**
 * ��������� ����� � ���������� � ������������ 6.11.2.1 �������� ����� �����
 */
void sort() {
    formData.dataRows.sort({ DataRow a, DataRow b ->
        if (a.part1REPODate == b.part1REPODate) {
            return a.tradeNumber <=> b.tradeNumber
        }
        return a.part1REPODate <=> b.part1REPODate
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
            'tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice'
    ].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('�������������')
    }
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