package form_template.deal.bonds_trade

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

import java.math.RoundingMode

/**
 * Реализация и приобретение ценных бумаг
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
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

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId,
            formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

void addRow() {
    def row = formData.createDataRow()

    for (alias in ['transactionDeliveryDate', 'contraName', 'transactionMode', 'transactionSumCurrency', 'currency',
            'courseCB', 'transactionSumRub', 'contractNum', 'contractDate', 'transactionDate', 'bondRegCode',
            'bondCount', 'transactionType']) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }

    formData.dataRows.add(row)

    row.getCell('rowNum').value = formData.dataRows.size()
}

void deleteRow() {
    if (currentDataRow != null) {
        recalcRowNum()
        formData.dataRows.remove(currentDataRow)
    }
}
/**
 * Пересчет индексов строк перед удалением строки
 */
void recalcRowNum() {
    def i = formData.dataRows.indexOf(currentDataRow)

    for (row in formData.dataRows[i..formData.dataRows.size()-1]) {
        row.getCell('rowNum').value = i++
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    // Отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // Налоговый период
    def taxPeriod = taxPeriodService.get(reportPeriod.taxPeriodId)

    def dFrom = taxPeriod.getStartDate()
    def dTo = taxPeriod.getEndDate()

    for (row in formData.dataRows) {
        if (row.getAlias() == null) {

            def rowNum = row.getCell('rowNum').value

            for (alias in ['transactionDeliveryDate', 'contraName', 'transactionMode', 'innKio', 'contraCountry',
                    'contraCountryCode', 'transactionSumCurrency', 'currency', 'courseCB', 'transactionSumRub',
                    'contractNum', 'contractDate', 'transactionDate', 'bondRegCode', 'bondCount', 'priceOne',
                    'transactionType']) {
                if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                    def msg = row.getCell(alias).column.name
                    logger.error("Графа «$msg» в строке $rowNum не заполнена!")
                }
            }

            def transactionDeliveryDate = row.getCell('transactionDeliveryDate').value
            def transactionDate = row.getCell('transactionDate').value
            def transactionSumRub = row.getCell('transactionSumRub').value
            def bondCount = row.getCell('bondCount').value
            def priceOne = row.getCell('priceOne').value
            def courseCB = row.getCell('courseCB').value
            def transactionSumCurrency = row.getCell('transactionSumCurrency').value
            def contractDate = row.getCell('contractDate').value

            // Корректность даты сделки
            if (transactionDeliveryDate < transactionDate) {
                def msg1 = row.getCell('transactionDeliveryDate').column.name
                def msg2 = row.getCell('transactionDate').column.name
                logger.error("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
            }

            // Проверка стоимости
            if (bondCount == null || priceOne == null || transactionSumRub != bondCount * priceOne) {
                def msg1 = row.getCell('transactionSumRub').column.name
                def msg2 = row.getCell('bondCount').column.name
                def msg3 = row.getCell('priceOne').column.name
                logger.error("«$msg1» не равна произведению «$msg2» и «$msg3» в строке $rowNum!")
            }

            // Проверка конверсии
            if (courseCB == null || transactionSumCurrency == null || transactionSumRub != (courseCB * transactionSumCurrency).setScale(2, RoundingMode.HALF_UP)) {
                def msg1 = row.getCell('transactionSumRub').column.name
                def msg2 = row.getCell('courseCB').column.name
                def msg3 = row.getCell('transactionSumCurrency').column.name
                logger.error("«$msg1» не соответствует «$msg2» с учетом данных «$msg3» в строке $rowNum!")
            }

            // Корректность даты договора
            def dt = row.getCell('contractDate').value
            if (dt != null && (dt < dFrom || dt > dTo)) {
                def msg = row.getCell('contractDate').column.name

                if (dt > dTo) {
                    logger.error("«$msg» не может быть больше даты окончания отчётного периода в строке $rowNum!")
                }

                if (dt < dFrom) {
                    logger.error("«$msg» не может быть меньше даты начала отчётного периода в строке $rowNum!")
                }
            }

            // Корректность даты заключения сделки
            if (transactionDeliveryDate < contractDate) {
                def msg1 = row.getCell('transactionDate').column.name
                def msg2 = row.getCell('contractDate').column.name
                logger.error("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
            }

            // Проверка цены сделки
            def res = null

            if (transactionSumRub != null && bondCount != null) {
                res = (transactionSumRub / bondCount).setScale(2, RoundingMode.HALF_UP)
            }

            if (transactionSumRub != null || bondCount == null || priceOne != res) {
                def msg1 = row.getCell('priceOne').column.name
                def msg2 = row.getCell('transactionSumRub').column.name
                def msg3 = row.getCell('bondCount').column.name
                logger.error("«$msg1» не равно отношению «$msg2» и «$msg3» в строке $rowNum!")
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
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {

    for (row in formData.dataRows) {
        // Расчет поля "Цена за 1 шт., руб."
        transactionSumRub = row.getCell('transactionSumRub').value
        bondCount = row.getCell('bondCount').value

        if (transactionSumRub != null && bondCount != null && bondCount != 0)
        {
            row.getCell('priceOne').value = transactionSumRub / bondCount;
        }
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