/**
 * ���-64
 * @author auldanov
 *
 * @version 55
 *
 * �������� ��������
 * 1. number - � ��
 * 2. date - ���� ������
 * 3. part - ����� ������
 * 4. dealingNumber - ����� ������
 * 5. bondKind - ��� ������ �����
 * 6. costs - ������� (���.���.)
 */

/**
 * ���������� �������� �� ��������
 *
 */
switch (formDataEvent){
// ������������� ������������� �������� ������ ����� � ������� ��������, �������������, �����������, ��������
    case FormDataEvent.CHECK:
        //1. ���������� �������� �������� ��������� �����
        logicalCheck()
        //2. �������� ������������ ���
        //NCICheck()
        break
// ������������� ������������� �������� �����
    case FormDataEvent.CREATE:
        checkBeforeCreate()
        //1.    �������� ������� � ������� �����, ��������������� ������ ������� ��������� �����, ��� �������� �����.
        //2.    ���������� �������� �������� ���������.
        // ?? logicalCheck()
        //3.    �������� ������������ ���.
        break
// ������������� ������������� �������� �������������
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        //1.    �������� ������� � ������� �����, ��������������� ������ ������� ��������� �����, ��� �������� � ������ �������������.
        //2.    ���������� �������� �������� ��������� �����.
        logicalCheck()
        //3.    �������� ������������ ���.
        break

// �������� ��� "������� �� ������� � ������������"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:
        // 1.   �������� ������� � ������� �����, ��������������� ������ ������� ��������� �����, ��� �������� ��������� ��������.
        checkOnCancelAcceptance()

        break

// ����� �������� �� ������������
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        acceptance()
        break

// ����� ������� �� ������� � ������������
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_PREPARED:
        acceptance()
        break

// ������������� �������������  ���������� �������� � ������ �������� �� ������������
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        //1.    �������� ������� � ������� �����, ��������������� ������ ������� ��������� �����, ��� �������� � ������ ��������.
        checkOnPrepareOrAcceptance('��������')
        // 2.   ���������� �������� �������� ��������� �����.
        //       logicalChecks()
        // 3.   �������� ������������ ���.
        //       checkNSI()

        break

// �������� ��������
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:
        // 1.   �������� ������� � ������� �����, ��������������� ������ ������� ��������� �����, ��� �������� ��������� ��������.
        checkOnCancelAcceptance()
        break

// ������� �������� ������
    case FormDataEvent.ADD_ROW:
        addNewRow()
        setRowIndex()
        break

// ������� ������� ������
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        setRowIndex()
        break

    case FormDataEvent.CALCULATE:
        fillForm()
        logicalCheck()

        sort()
        break

    case FormDataEvent.COMPOSE:
        consolidation()
        fillForm()
        break
    // ����� �������� �� ������������
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        logicalCheck()
        break
    // ��������
    case FormDataEvent.COMPOSE :
        consolidation()
        fillForm()
        logicalCheck()
        break
}



/**
 * ���������� ����� ������
 */
def addNewRow(){
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

    ['date', 'part', 'dealingNumber', 'bondKind', 'costs'].each {
        newRow.getCell(it).editable = true
        //newRow.getCell(it).setStyleAlias('�������������')
    }
}

def log(String message, Object... args) {
    //logger.info(message, args)
}
/**
 * �������� ������
 *
 * @author Ivildanov
 * ����� �������� ��� � ��������� ���������
 */
def deleteRow() {
    // def row = (DataRow)additionalParameter
    def row = currentDataRow
    if (!isTotalRow(row)) {
        // �������� ������
        formData.deleteDataRow(row)
    }
}

/**
 * ���������� ����� �����
 * 6.1.2.3  ��������� ���������� ����� �����
 */
def fillForm(){
    // ������� ������ �����
    formData.dataRows = formData.dataRows.findAll{ it.getAlias() != 'totalQuarter' && it.getAlias() != 'total'}

    // ��������� ������ �����
    def newRowQuarter = formData.createDataRow()
    newRowQuarter.setAlias("totalQuarter")
    //2,3,4 ����������� ������� ������ �� ������� �������
    newRowQuarter.getCell("fix").setColSpan(4)
    newRowQuarter.fix = "����� �� ������� �������"
    formData.dataRows.add(formData.dataRows.size() > 0 ? formData.dataRows.size(): 0, newRowQuarter )

    // 6 ����� �������� ����� �������� "����� 6" ��� ���� ����� ������ �������, �� ����������� �������� ����� (������ �� ������� �������, ������ �� ������� �������� (���������) ������)
    newRowQuarter.costs = getQuarterTotal()

    // ������ ����� �� ������� �������� (���������) ������
    def newRowTotal = formData.createDataRow()
    newRowTotal.setAlias("total")
    //2,3,4 ����������� ������� ������ �� ������� �������
    newRowTotal.getCell("fix").setColSpan(4)
    newRowTotal.fix = "����� �� ������� �������� (���������) ������"
    formData.dataRows.add(formData.dataRows.size(), newRowTotal)
    newRowTotal.costs = getTotalValue()
}

/**
 * ���������� ��������
 */
def logicalCheck(){

    formData.dataRows.each{ row ->
        // �������������� ���������� ���� ����� (� 1 �� 6); ���������; ���� ������������� ����� �� ���������!
        ['number', 'date', 'part', 'dealingNumber', 'bondKind', 'costs'].each{alias ->
            if (!isTotalRow(row) && (row[alias] == null || row[alias] == '')){
                logger.error('���� �'+row.getCell(alias).getColumn().getName()+'� �� ���������!')
            }
        }

        reportPeriodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
        reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)
        // �������� ���� ���������� �������� � ������ ��������� �������; ���������; ���� ���������� �������� ��� ������ ��������� �������!
        if (row.date != null && !(
                (reportPeriodStartDate.getTime().equals(row.date) || row.date.after(reportPeriodStartDate.getTime())) &&
                (reportPeriodEndDate.getTime().equals(row.date) || row.date.before(reportPeriodEndDate.getTime()))
        )){
            // TODO �������� ����� � ��������� ������� ����� ������
            logger.error('���� ���������� �������� ��� ������ ��������� �������!')
        }

        // �������� �� ������������ ���� �� ��
        // TODO �� �����������
        /**
         * @author Ivildanov
         *  �� �����������
         */
        formData.dataRows.each { rowItem ->
            if (row.number == rowItem.number && !row.equals(rowItem)) {
                //logger.error('�������� ������������ ������ �� �������!')
            }
        }

        // �������� �� ������� ��������; ���������; ��� ����� �� �������� �������!
        if (row.costs == 0){
            logger.error('��� ����� �� �������� �������!')
        }
    }

    // �������� �� ������� �������� �����, ����� ����� ������
    if ((formData.dataRows.findAll { it.getAlias() == 'totalQuarter' && it.getAlias() == 'total' }).size() > 0) {
        // �������� �������� �������� �� ������� �������; ���������; �������� �������� �� ������� ������� ���������� �������!
        if (formData.getDataRow('totalQuarter').costs != getQuarterTotal()) {
            logger.error('�������� �������� �� ������� ������� ���������� �������!')
        }

        // �������� �������� �������� �� ������� �������� (���������) ������; ���������; �������� �������� �� ������� �������� (��������� ) ������ ���������� �������!
        if (formData.getDataRow('total').costs != getTotalValue()) {
            logger.error('�������� �������� �� ������� �������� (��������� ) ������ ���������� �������!')
        }
    }

    // �������� ������������ ���� ������ ������; �� ���������; ���� ������������� ����� ������� ������� �� ���������!
    // TODO �� ���������� ���������

}

/**
 * ������ ��� �������� ��������.
 */
void checkBeforeCreate() {
    // �������� ������
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //�������� ������� ����� ��������
    if (reportPeriod != null && reportPeriod.isBalancePeriod()) {
        logger.error('��������� ����� �� ����� ����������� � ������� ����� ��������.')
        return
    }

    def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('��������� ����� � ��������� ����������� ��� ����������.')
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
 * �������� �������� �� ������ �������� �� ������� �������
 */
def isQuarterTotal(row){
    row.getAlias()=='totalQuarter'
}

/**
 * �������� �������� �� ������ �������� (��������� ������)
 */
def isMainTotalRow(row){
    row.getAlias()=='total'
}

/**
 * �������� �������� �� ������ �������� (����� ��������, �.�. �� �������, ���� ��������)
 */
def isTotalRow(row){
    return row.getAlias()=='total' || row.getAlias()=='totalQuarter'
}

// ������� ���������� �������� �������� �� ������� �������
def getQuarterTotal(){
    def row6val = 0
    formData.dataRows.each{ row->
        if (!isTotalRow(row)){
            row6val += row.costs?:0
        }
    }
    row6val
}

// ������� ���������� �������� �������� �� ������� �������� (���������) ������
def getTotalValue(){
    quarterRow = formData.getDataRow('totalQuarter')
    // ������� ����� �� ���������� �������� ������
    def prevQuarter = quarterService.getPrevReportPeriod(formData.reportPeriodId)
    if (prevQuarter != null) {
        log('������� ������ Id:' + formData.reportPeriodId)
        log('���������� ������ ������ Id:' + prevQuarter.id)
        prevQuarterFormData = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, prevQuarter.id);

        if (prevQuarterFormData != null && prevQuarterFormData.state == WorkflowState.ACCEPTED) {
            def prevQuarterTotalRow = prevQuarterFormData.getDataRow("total")
            return quarterRow.costs + prevQuarterTotalRow.costs
        } else {
            //  ���� ���������� ����� ��� (���� ��� �� �������)  �� B = 0
            return quarterRow.costs
        }

    } else {
        return quarterRow.costs
    }
}

/**
 * ��������� ������ ������.
 *
 * @author Ivildanov
 */
void setRowIndex() {
    def i = 1;
    formData.dataRows.each { rowItem ->
        rowItem.number = i++
    }
}

/**
 * ������ ��� ����������.
 *
 * @author Ivildanov
 */
void sort() {
    // ����������
    // 1 - ���� ������
    // 2 - ����� ������
    formData.dataRows.sort { a, b ->
        if (a == null || isTotalRow(a)) return 0
        int val = (a.date).compareTo(b.date)
        if (val == 0) {
            val = (a.dealingNumber ?: "").compareTo(b.dealingNumber ?: "")
        }
        return val
    }
}

/**
 * ��� �������� ������� ������� ����� � ������ "������".
 */
void acceptance() {
    departmentFormTypeService.getFormDestinations(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.PRIMARY).each()
            {
                formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
            }
}

/**
 * �������� ������� � ������� ����������������� ����� ��� ������������� �������� ����� � ������ "������������"/"�������".
 */
void checkOnPrepareOrAcceptance(def value) {
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // ���� ����� ���������� � ������ "�������"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error("$value ��������� ��������� ����� ����������, �.�. ��� ������������ ����������������� ��������� �����.")
            }
        }
    }
}

/**
 * �������� ��� �������� "�������� ��������" � ������������.
 */
void checkOnCancelAcceptance() {
    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(),
            formData.getFormType().getId(), formData.getKind());
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && (form.getState() == WorkflowState.PREPARED || form.getState() == WorkflowState.ACCEPTED)) {
            if (formData.getKind().getId() == 1) { // ���� ����� ���������
                logger.error("������ �������� �������� ��������� �����, ��� ��� ��� ����������� ��� �������� ����������������� ��������� �����.")
            } else {    // ���� ����� ����������������
                logger.error("������ �������� �������� ��������� �����, ��� ��� ��� ����������� ��� �������� ����������������� ��������� ����� ������������ ������.")
            }
        }
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