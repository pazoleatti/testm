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
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        checkMatrix()
        logicCheck()
        calc()
        break
    case FormDataEvent.MOVE_PREPARED_TO_CREATED:
        break
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        checkMatrix()
        logicCheck()
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
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
    row = formData.createDataRow()
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
 * Cформирована ли для выбранного пользователем отчета форма-приемник (консолидированный отчет или «Матрица»)
 */
void checkMatrix() {
    // TODO
    if (false) {
        // сформирована и имеет статус, отличный от «Создана»
        logger.error("Принятие отчета невозможно, т.к. уже подготовлена форма-приемник.")
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    for (row in formData.dataRows) {
        rowNum = row.getCell('rowNumber').value
        docDateCell = row.getCell('docDate')
        okeiCodeCell = row.getCell('okeiCode')
        for (alias in ['fullNamePerson', 'inn', 'countryCode', 'docNumber', 'docDate',
                'okeiCode', 'count', 'price', 'cost', 'dealDate']) {
            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                logger.error('Графа «' + row.getCell(alias).column.name + '» в строке ' + rowNum + ' не заполнена!')
            }
        }
        // Проверка доходов и расходов
        incomeSumCell = row.getCell('incomeSum')
        outcomeSumCell = row.getCell('outcomeSum')
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            logger.error('«' + incomeSumCell.column.name + '» и «' + outcomeSumCell.column.name + '» в строке ' +
                    rowNum + ' не могут быть одновременно заполнены!')
        }
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            logger.error('Одна из граф «' + incomeSumCell.column.name + '» и «' + outcomeSumCell.column.name + '» в строке ' +
                    rowNum + ' должна быть заполнена!')
        }
        // Проверка выбранной единицы измерения
        if (okeiCodeCell.value!= '796' && okeiCodeCell.value!= '744') {
            logger.error('В графе «' + okeiCodeCell.column.name + '» могут быть указаны только следующие элементы: шт., процент!')
        }
        //  Корректность даты договора
        def taxPeriod = taxPeriodService.get(reportPeriodService.get(formData.reportPeriodId).taxPeriodId)
        def dFrom = taxPeriod.getStartDate()
        def dTo = taxPeriod.getEndDate()
        dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            msg = docDateCell.column.name
            if (dt > dTo) {
                logger.error("«$msg» в строке $rowNum не может быть больше даты окончания отчётного периода!")
            }
            if (dt < dFrom) {
                logger.error("«$msg» в строке $rowNum не может быть меньше даты начала отчётного периода!")
            }
        }
        // Проверка цены
        sumCell = row.getCell('incomeSum').value != null ? row.getCell('incomeSum') : row.getCell('outcomeSum')
        countCell = row.getCell('count')
        priceCell = row.getCell('price')

        if (okeiCodeCell.value == '796' && countCell.value!=null && countCell.value!=0
                && priceCell.value != (sumCell.value / countCell.value).setScale(2, RoundingMode.HALF_UP)) {
            logger.error('«' + priceCell.column.name + '» в строке ' + rowNum + ' не равно отношению «' +
                    sumCell.column.name + '» и «' + countCell.column.name + '»!')
        } else if (okeiCodeCell.value == '744' && priceCell.value != sumCell.value) {
            logger.error('«' + priceCell.column.name + '» в строке ' + rowNum + ' не равно «' + sumCell.column.name + '»!')
        }
        // Корректность даты совершения сделки
        dealDateCell = row.getCell('dealDate')
        if (docDateCell.value > dealDateCell.value) {
            logger.error('«' + dealDateCell.column.name + '» не может быть меньше «' + docDateCell.column.name + '» в строке ' + rowNum + '!')
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
        } else if (okeiCode == '796' && row.getCell('count').value!=0 && row.getCell('count').value!=null) {
            row.getCell('price').value = (priceValue / row.getCell('count').value).setScale(2, RoundingMode.HALF_UP)
        } else{
            row.getCell('price').value = null
        }
        // Расчет поля "Стоимость"
        row.getCell('cost').value = priceValue

        // TODO расчет полей по справочникам
    }
}