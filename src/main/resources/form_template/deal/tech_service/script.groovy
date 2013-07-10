package form_template.deal.tech_service

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

import java.math.RoundingMode

/**
 * Техническое обслуживание нежилых помещений
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
    def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

void addRow() {
    def row = formData.createDataRow()

    for (alias in ['jurName', 'bankSum', 'contractNum', 'contractDate', 'country', 'region', 'city', 'settlement', 'count', 'price', 'transactionDate']) {
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

    for (row in formData.dataRows[i..formData.dataRows.size() - 1]) {
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

            for (alias in ['jurName', 'innKio', 'countryCode', 'bankSum', 'contractNum', 'contractDate',
                    'country', 'price', 'cost', 'transactionDate']) {
                if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                    def msg = row.getCell(alias).column.name
                    logger.error("Графа «$msg» в строке $rowNum не заполнена!")
                }
            }

            // Проверка стоимости
            def cost = row.getCell('cost').value
            def price = row.getCell('price').value
            def count = row.getCell('count').value
            def bankSum = row.getCell('bankSum').value
            def transactionDate = row.getCell('transactionDate').value
            def contractDate = row.getCell('contractDate').value

            if (price == null || count == null && cost != price * count) {
                def msg1 = row.getCell('cost').column.name
                def msg2 = row.getCell('price').column.name
                def msg3 = row.getCell('count').column.name
                logger.warn("«$msg1» не равна произведению «$msg2» и «$msg3» в строке $rowNum!")
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

            // Корректность даты совершения сделки
            if (transactionDate < contractDate) {
                def msg1 = row.getCell('transactionDate').column.name
                def msg2 = row.getCell('contractDate').column.name
                logger.error("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
            }

            // Проверка цены сделки
            if (count != null) {
                def res = null

                if (bankSum != null && count != null) {
                    res = (bankSum / count).setScale(2, RoundingMode.HALF_UP)
                }

                if (bankSum == null || count == null || price != res) {
                    def msg1 = row.getCell('price').column.name
                    def msg2 = row.getCell('bankSum').column.name
                    def msg3 = row.getCell('count').column.name
                    logger.error("«$msg1» не равно отношению «$msg2» и «$msg3» в строке $rowNum!")
                }
            } else {
                if (price != bankSum) {
                    def msg1 = row.getCell('price').column.name
                    def msg2 = row.getCell('bankSum').column.name
                    logger.error("«$msg1» не равно «$msg2» в строке $rowNum!")
                }
            }

            // Проверка расходов
            if (cost != bankSum) {
                def msg1 = row.getCell('cost').column.name
                def msg2 = row.getCell('bankSum').column.name
                logger.error("«$msg1» не равно «$msg2» в строке $rowNum!")
            }

            // Проверка заполнения региона
            def country = row.getCell('country').value

            if (country != null) {
                // TODO проверки для страны по коду справочника
            } else {
                // TODO проверки для страны по коду справочника
            }

            // TODO Проверка населенного пункта
        }
    }

    checkNSI()
}

/**
 * Проверка соответствия НСИ
 */
void checkNSI() {
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            // TODO добавить проверки НСИ
        }
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {

            // TODO Расчет поля "Населенный пункт"
            count = row.getCell('count').value
            bankSum = row.getCell('bankSum').value
            // Расчет поля "Цена"
            row.getCell('price').value = count == null ? bankSum : bankSum / count
            // Расчет поля "Стоимость"
            row.getCell('cost').value = bankSum

            // TODO расчет полей по справочникам
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