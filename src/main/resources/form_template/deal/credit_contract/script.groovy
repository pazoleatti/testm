package form_template.deal.credit_contract

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Уступка прав требования по кредитным договорам
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
    row = formData.createDataRow()

    for (alias in ['name', 'contractNum', 'contractDate', 'okeiCode', 'price', 'transactionDate']) {
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
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            for (alias in ['name', 'innKio', 'country', 'contractNum', 'contractDate', 'okeiCode', 'count',
                    'price', 'totalCost', 'transactionDate']) {
                if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                    msg = row.getCell(alias).column.name
                    rowNum = row.getCell('rowNum').value
                    logger.error("Графа «$msg» в строке $rowNum не заполнена!")
                }
            }
        }
    }

    // Проверка выбранной единицы измерения
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            // TODO поле справочника "код"
            // logger.error('В поле «Код единицы измерения по ОКЕИ» могут быть указаны только следующие элементы: шт.!')
        }
    }

    // Проверка количества
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            if (row.getCell('count').value != 1) {
                msg = row.getCell('transactionDate').column.name
                rowNum = row.getCell('rowNum').value
                logger.error("В графе «$msg» может быть указано только значение «1» в строке $rowNum!")
            }
        }
    }

    // Отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // Налоговый период
    def taxPeriod = taxPeriodService.get(reportPeriod.taxPeriodId)

    def dFrom = taxPeriod.getStartDate()
    def dTo = taxPeriod.getEndDate()

    // Корректность даты договора
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {

            dt = row.getCell('contractDate').value
            if (dt != null && (dt < dFrom || dt > dTo)) {
                msg = row.getCell('contractDate').column.name
                rowNum = row.getCell('rowNum').value

                if (dt > dTo) {
                    logger.error("«$msg» не может быть больше даты окончания отчётного периода в строке $rowNum!")
                }

                if (dt < dFrom) {
                    logger.error("«$msg» не может быть меньше даты начала отчётного периода в строке $rowNum!")
                }
            }
        }
    }

    // Корректность даты совершения сделки
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            transactionDate = row.getCell('transactionDate').value
            contractDate = row.getCell('contractDate').value

            if (transactionDate < contractDate) {
                msg1 = row.getCell('transactionDate').column.name
                msg2 = row.getCell('contractDate').column.name
                rowNum = row.getCell('rowNum').value
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
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {

    for (row in formData.dataRows) {
        // Количество
        row.getCell('count').value = 1
        // Итого стоимость без учета НДС, акцизов и пошлин, руб.
        row.getCell('totalCost').value = row.getCell('price').value
        // TODO расчет полей по справочникам
    }
}