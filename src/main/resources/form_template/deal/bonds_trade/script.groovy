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
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            for (alias in ['transactionDeliveryDate', 'contraName', 'transactionMode', 'innKio', 'contraCountry',
                    'contraCountryCode', 'transactionSumCurrency', 'currency', 'courseCB', 'transactionSumRub',
                    'contractNum', 'contractDate', 'transactionDate', 'bondRegCode', 'bondCount', 'priceOne',
                    'transactionType']) {
                if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                    msg = row.getCell(alias).column.name
                    rowNum = row.getCell('rowNum').value
                    logger.error("Графа «$msg» в строке $rowNum не заполнена!")
                }
            }
        }
    }

    // Корректность даты сделки
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            transactionDeliveryDate = row.getCell('transactionDeliveryDate').value
            transactionDate = row.getCell('transactionDate').value

            if (transactionDeliveryDate < transactionDate) {
                msg1 = row.getCell('transactionDeliveryDate').column.name
                msg2 = row.getCell('transactionDate').column.name
                rowNum = row.getCell('rowNum').value
                logger.error("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
            }
        }
    }

    // Проверка стоимости
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {

            transactionSumRub = row.getCell('transactionSumRub').value
            bondCount = row.getCell('bondCount').value
            priceOne = row.getCell('priceOne').value

            if (bondCount == null || priceOne == null || transactionSumRub != bondCount * priceOne) {
                msg1 = row.getCell('transactionSumRub').column.name
                msg2 = row.getCell('bondCount').column.name
                msg3 = row.getCell('priceOne').column.name
                rowNum = row.getCell('rowNum').value
                logger.warn("«$msg1» не равна произведению «$msg2» и «$msg3» в строке $rowNum!")
            }
        }
    }

    // Проверка конверсии
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {

            transactionSumRub = row.getCell('transactionSumRub').value
            courseCB = row.getCell('courseCB').value
            transactionSumCurrency = row.getCell('transactionSumCurrency').value

            if (courseCB == null || transactionSumCurrency == null || transactionSumRub != (courseCB * transactionSumCurrency).setScale(2, RoundingMode.HALF_UP)) {
                msg1 = row.getCell('transactionSumRub').column.name
                msg2 = row.getCell('courseCB').column.name
                msg3 = row.getCell('transactionSumCurrency').column.name
                rowNum = row.getCell('rowNum').value
                logger.warn("«$msg1» не соответствует «$msg2» с учетом данных «$msg3» в строке $rowNum!")
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

    // Корректность даты заключения сделки
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            contractDate = row.getCell('contractDate').value
            transactionDeliveryDate = row.getCell('transactionDeliveryDate').value

            if (transactionDeliveryDate < contractDate) {
                msg1 = row.getCell('transactionDate').column.name
                msg2 = row.getCell('contractDate').column.name
                rowNum = row.getCell('rowNum').value
                logger.error("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
            }
        }
    }

    // Проверка цены сделки
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            bondCount = row.getCell('bondCount').value
            priceOne = row.getCell('priceOne').value
            transactionSumRub = row.getCell('transactionSumRub').value

            res = null

            if (transactionSumRub != null && bondCount != null) {
                res = (transactionSumRub/bondCount).setScale(2, RoundingMode.HALF_UP)
            }

            if (transactionSumRub != null || bondCount == null || priceOne != res) {
                msg1 = row.getCell('priceOne').column.name
                msg2 = row.getCell('transactionSumRub').column.name
                msg3 = row.getCell('bondCount').column.name
                rowNum = row.getCell('rowNum').value
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