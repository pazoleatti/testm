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
        acceptance()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        acceptance()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
}

void deleteRow() {
    if (currentDataRow != null) {
        formData.dataRows.remove(currentDataRow)
        recalcRowNum()
    }
}

/**
 * Пересчет индексов строк перед удалением строки
 */
void recalcRowNum() {
    int i = 1
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            row.getCell('rowNumber').value = i++
        }
    }
}

void addRow() {
    def row = formData.createDataRow()
    for (alias in ['fullName', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'sum', 'dealDoneDate']) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }
    formData.dataRows.add(row)
    recalcRowNum()
}
/**
 * Проверяет уникальность в отчётном периоде и вид
 * (не был ли ранее сформирован отчет, параметры которого совпадают с параметрами, указанными пользователем )
 */
void checkUniq() {
    def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    for (row in formData.dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getCell('rowNumber').value
        def docDateCell = row.getCell('docDate')
        def dealDateCell = row.getCell('dealDate')
        for (alias in ['fullName', 'inn', 'countryName', 'docNumber', 'docDate', 'dealNumber',
                'dealDate', 'sum', 'price', 'total', 'dealDoneDate']) {
            def rowCell = row.getCell(alias)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.error("Графа «$msg» в строке $rowNum не заполнена!")
            }
            //  Корректность даты договора
            def taxPeriod = taxPeriodService.get(reportPeriodService.get(formData.reportPeriodId).taxPeriodId)
            def dFrom = taxPeriod.getStartDate()
            def dTo = taxPeriod.getEndDate()
            def dt = docDateCell.value
            if (dt != null && (dt < dFrom || dt > dTo)) {
                def msg = docDateCell.column.name
                if (dt > dTo) {
                    logger.error("«$msg» в строке $rowNum не может быть больше даты окончания отчётного периода!")
                }
                if (dt < dFrom) {
                    logger.error("«$msg» в строке $rowNum не может быть меньше даты начала отчётного периода!")
                }
            }
            // Корректность даты заключения сделки
            if (docDateCell.value > dealDateCell.value) {
                def msg1 = dealDateCell.column.name
                def msg2 = docDateCell.column.name
                logger.error("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
            }
            // Проверка доходности
            def sumCell = row.getCell('sum')
            def priceCell = row.getCell('price')
            def totalCell = row.getCell('total')
            def msgSum = sumCell.column.name
            if (priceCell.value != sumCell.value) {
                def msg = priceCell.column.name
                logger.error("«$msg» в строке $rowNum не может отличаться от «$msgSum»!")
            }
            if (totalCell.value != sumCell.value) {
                def msg = totalCell.column.name
                logger.error("«$msg» в строке $rowNum не может отличаться от «$msgSum»!")
            }
            // Корректность даты совершения сделки
            def dealDoneDateCell = row.getCell('dealDoneDate')
            if (dealDoneDateCell.value < dealDateCell.value) {
                def msg1 = dealDoneDateCell.column.name
                def msg2 = dealDateCell.column.name
                logger.error("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
            }
        }
    }
    checkNSI()
}

/**
 * Проверка соответствия НСИ
 */
void checkNSI() {
    for (row in formData.dataRows) {
        // TODO добавить проверки НСИ
    }
}

/**
 * Алгоритмы заполнения полей формы.
 */
void calc() {
    for (row in formData.dataRows) {
        // Расчет поля "Цена"
        row.getCell('price').value = row.getCell('sum').value
        // Расчет поля "Итого"
        row.getCell('total').value = row.getCell('sum').value
        // TODO расчет полей по справочникам
    }
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {

        def newRow = formData.createDataRow()

        newRow.getCell('fullName').value = 'Подитог:'
        newRow.setAlias('itg')
        newRow.getCell('fullName').colSpan = 7

        // Расчеты подитоговых значений
        BigDecimal sumItg = 0, totalItg = 0
        for (row in formData.dataRows) {

            sum = row.getCell('sum').value
            total = row.getCell('total').value

            sumItg += sum != null ? sum : 0
            totalItg += total != null ? total : 0
        }

        newRow.getCell('sum').value = sumItg
        newRow.getCell('total').value = totalItg

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
 * Инициация консолидации
 */
void acceptance() {
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() {
        formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
    }
}

/**
 * Консолидация
 */
void consolidation() {
    // Удалить все строки и собрать из источников их строки
    formData.dataRows.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(),
            formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                source.getDataRows().each { row ->
                    if (row.getAlias() == null) {
                        formData.dataRows.add(row)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}