package form_template.deal.securities

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

import java.math.RoundingMode

/**
 * Приобретение и реализация ценных бумаг (долей в уставном капитале)
 *
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        break
    case FormDataEvent.CALCULATE:
        calc()
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
        recalcRowNum()
        formData.dataRows.remove(currentDataRow)
    }
}

void recalcRowNum() {
    def i = formData.dataRows.indexOf(currentDataRow)

    for (row in formData.dataRows[i..formData.dataRows.size() - 1]) {
        row.getCell('rowNumber').value = i++
    }
}

void addRow() {
    def row = formData.createDataRow()
    for (alias in ['fullNamePerson', 'dealSign', 'incomeSum', 'outcomeSum', 'docNumber', 'docDate', 'okeiCode', 'count', 'dealDate']) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }
    formData.dataRows.add(row)
    row.getCell('rowNumber').value = formData.dataRows.size()
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
        def rowNum = row.getCell('rowNumber').value
        def docDateCell = row.getCell('docDate')
        def okeiCodeCell = row.getCell('okeiCode')
        for (alias in ['fullNamePerson', 'inn', 'countryCode', 'docNumber', 'docDate', 'okeiCode', 'count', 'price', 'cost', 'dealDate']) {
            def rowCell = row.getCell(alias)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.error("Графа «$msg» в строке $rowNum не заполнена!")
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
            logger.error("Одна из граф «$msgIn» и «$msgOut» в строке $rowNum должна быть заполнена!")
        }
        // Проверка выбранной единицы измерения
        if (okeiCodeCell.value != '796' && okeiCodeCell.value != '744') {
            def msg = okeiCodeCell.column.name
            logger.error("В графе «$msg» строки $rowNum могут быть указаны только следующие элементы: шт., процент!")
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
        // Проверка цены
        def sumCell = row.getCell('incomeSum').value != null ? row.getCell('incomeSum') : row.getCell('outcomeSum')
        def countCell = row.getCell('count')
        def priceCell = row.getCell('price')
        if (okeiCodeCell.value == '796' && countCell.value != null && countCell.value != 0
                && priceCell.value != (sumCell.value / countCell.value).setScale(2, RoundingMode.HALF_UP)) {
            def msg1 =  priceCell.column.name
            def msg2 =  sumCell.column.name
            def msg3 =  countCell.column.name
            logger.error("«$msg1» в строке $rowNum не равно отношению «$msg2» и «$msg3»!")
        } else if (okeiCodeCell.value == '744' && priceCell.value != sumCell.value) {
            def msg1 =  priceCell.column.name
            def msg2 =  sumCell.column.name
            logger.error("«$msg1» в строке $rowNum не равно «$msg2»!")
        }
        // Корректность даты совершения сделки
        def dealDateCell = row.getCell('dealDate')
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.error("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
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
        priceValue = row.getCell('incomeSum').value != null ? row.getCell('incomeSum').value : row.getCell('outcomeSum').value
        okeiCode = row.getCell('okeiCode').value
        if (okeiCode == '744') {
            row.getCell('price').value = priceValue
        } else if (okeiCode == '796' && row.getCell('count').value != 0 && row.getCell('count').value != null) {
            row.getCell('price').value = (priceValue / row.getCell('count').value).setScale(2, RoundingMode.HALF_UP)
        } else {
            row.getCell('price').value = null
        }
        // Расчет поля "Стоимость"
        row.getCell('cost').value = priceValue

        // TODO расчет полей по справочникам
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