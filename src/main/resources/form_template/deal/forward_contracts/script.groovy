package form_template.deal.forward_contracts

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Поставочные срочные сделки, базисным активом которых является иностранная валюта
 *
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
// После принятия из Утверждено
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
        logicCheck()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicCheck()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
}

void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
    dataRowHelper.save(dataRowHelper.getAllCached())
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? currentDataRow.getIndex() : (size == 0 ? 1 : size)
    dataRowHelper.insert(row, index)
    dataRows.add(row)
    ['fullName', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealType',
            'currencyCode', 'countryDealCode', 'incomeSum', 'outcomeSum', 'dealDoneDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.save(dataRows)
}

/**
 * Проверяет уникальность в отчётном периоде и вид
 * (не был ли ранее сформирован отчет, параметры которого совпадают с параметрами, указанными пользователем )
 */
void checkUniq() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()
        def dealDateCell = row.getCell('dealDate')
        def docDateCell = row.getCell('docDate')
        ['rowNumber', 'fullName', 'inn', 'countryName', 'countryCode', 'docNumber', 'docDate', 'dealNumber', 'dealDate'
                , 'dealType', 'currencyCode', 'countryDealCode', 'price', 'total', 'dealDoneDate'].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }
        // Проверка доходов и расходов
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            logger.error("«$msgIn» и «$msgOut» в строке $rowNum не могут быть одновременно заполнены!")
        }
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            logger.warn("Одна из граф «$msgIn» и «$msgOut» в строке $rowNum должна быть заполнена!")
        }
        //  Корректность даты договора
        def taxPeriod = taxPeriodService.get(reportPeriodService.get(formData.reportPeriodId).taxPeriodId)
        def dFrom = taxPeriod.getStartDate()
        def dTo = taxPeriod.getEndDate()
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            if (dt > dTo) {
                logger.warn("«$msg» в строке $rowNum не может быть больше даты окончания отчётного периода!")
            }
            if (dt < dFrom) {
                logger.warn("«$msg» в строке $rowNum не может быть меньше даты начала отчётного периода!")
            }
        }
        // Корректность даты заключения сделки
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
    }
    checkNSI()
}

/**
 * Проверка соответствия НСИ
 */
void checkNSI() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    for (row in dataRowHelper.getAllCached()) {
        // TODO добавить проверки НСИ
    }
}

/**
 * Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    for (row in dataRowHelper.getAllCached()) {
        // Расчет поля "Цена"
        row.price = row.incomeSum != null ? row.incomeSum : row.outcomeSum
        // Расчет поля "Итого"
        row.total = row.price
    }
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {

        for (int i = 0; i < formData.dataRows.size(); i++) {
            DataRow<Cell> row = formData.dataRows.get(i)
            DataRow<Cell> nextRow = null

            if (i < formData.dataRows.size() - 1) {
                nextRow = formData.dataRows.get(i + 1)
            }

            // TODO сравнение по полям  'inn', docNumber', 'docDate', 'dealType'
            if (row.getAlias() == null && nextRow == null || row.fullName != nextRow.fullName) {
                def itogRow = calcItog(i)
                formData.dataRows.add(i + 1, itogRow)
                i++
            }
        }
        recalcRowNum()
    }
}

/**
 * Расчет подитогового значения
 * @param i
 * @return
 */
def calcItog(int i) {
    def newRow = formData.createDataRow()

    newRow.fullName = 'Подитог:'

    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fullName').colSpan = 11

    // Расчеты подитоговых значений
    BigDecimal incomeSumItg = 0, outcomeSumItg = 0, totalItg = 0
    for (int j = i; j >= 0 && formData.dataRows.get(j).getAlias() == null; j--) {
        row = formData.dataRows.get(j)

        incomeSum = row.incomeSum
        outcomeSum = row.outcomeSum
        total = row.total

        incomeSumItg += incomeSum != null ? incomeSum : 0
        outcomeSumItg += outcomeSum != null ? outcomeSum : 0
        totalItg += total != null ? total : 0
    }

    newRow.incomeSum = incomeSumItg
    newRow.outcomeSum = outcomeSumItg
    newRow.total = totalItg

    newRow
}

/**
 * Сортировка строк
 */
void sort() {
    formData.dataRows.sort({ DataRow a, DataRow b ->
        // name - innKio - contractNum - contractDate - transactionType
        if (a.fullName == b.fullName) {
            if (a.inn == b.inn) {
                if (a.docNumber == b.docNumber) {
                    if (a.docDate == b.docDate) {
                        return a.dealType <=> b.dealType
                    }
                    return a.docDate <=> b.docDate
                }
                return a.docNumber <=> b.docNumber
            }
            return a.inn <=> b.inn
        }
        return a.fullName <=> b.fullName;
    })
    recalcRowNum()
}

/**
 * Удаление всех статическиех строк "Подитог" из списка строк
 */
void deleteAllStatic() {
    for (Iterator<DataRow> iter = formData.dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            iter.remove()
        }
    }
    recalcRowNum()
}

/**
 * Консолидация
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    dataRows.clear()

    int index = 1;
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            formDataService.getDataRowHelper(source).getAllCached().each { row ->
                if (row.getAlias() == null) {
                    dataRowHelper.insert(row, index++)
                    dataRows.add(row)
                }
            }
        }
    }
    dataRowHelper.save(dataRows);
}