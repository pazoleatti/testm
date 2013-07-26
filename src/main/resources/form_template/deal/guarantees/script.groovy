package form_template.deal.guarantees

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * Предоставление гарантий
 *
 * (похож на letter_of_credit "Предоставление инструментов торгового финансирования и непокрытых аккредитивов")
 * (похож на  interbank_credits "Предоставление межбанковских кредитов")
 *
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
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
    ['fullName', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'sum', 'dealDoneDate'].each {
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
        def docDateCell = row.getCell('docDate')
        def dealDateCell = row.getCell('dealDate')
        ['fullName', 'inn', 'countryName', 'docNumber', 'docDate', 'dealNumber',
                'dealDate', 'sum', 'price', 'total', 'dealDoneDate'].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
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
            // Проверка доходности
            def sumCell = row.getCell('sum')
            def priceCell = row.getCell('price')
            def totalCell = row.getCell('total')
            def msgSum = sumCell.column.name
            if (priceCell.value != sumCell.value) {
                def msg = priceCell.column.name
                logger.warn("«$msg» в строке $rowNum не может отличаться от «$msgSum»!")
            }
            if (totalCell.value != sumCell.value) {
                def msg = totalCell.column.name
                logger.warn("«$msg» в строке $rowNum не может отличаться от «$msgSum»!")
            }
            // Корректность даты совершения сделки
            def dealDoneDateCell = row.getCell('dealDoneDate')
            if (dealDoneDateCell.value < dealDateCell.value) {
                def msg1 = dealDoneDateCell.column.name
                def msg2 = dealDateCell.column.name
                logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
            }
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
        row.price = row.sum
        // Расчет поля "Итого"
        row.total = row.sum
        // TODO расчет полей по справочникам
    }
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {

        def newRow = formData.createDataRow()

        newRow.fullName = 'Подитог:'
        newRow.setAlias('itg')
        newRow.getCell('fullName').colSpan = 7

        // Расчеты подитоговых значений
        BigDecimal sumItg = 0, totalItg = 0
        for (row in formData.dataRows) {

            sum = row.sum
            total = row.total

            sumItg += sum != null ? sum : 0
            totalItg += total != null ? total : 0
        }

        newRow.sum = sumItg
        newRow.total = totalItg

        formData.dataRows.add(newRow)
    }
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