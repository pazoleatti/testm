package form_template.deal.foreign_currency

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * Купля-продажа иностранной валюты
 * (похож на nondeliverable " Беспоставочные срочные сделки")
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
    row = formData.createDataRow()
    for (alias in ['fullName', 'docNum', 'docDate', 'dealNumber', 'dealDate', 'currencyCode',
            'countryDealCode', 'incomeSum', 'outcomeSum', 'dealDoneDate']) {
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
        if (row.getAlias() != null) {
            continue
        }
        rowNum = row.getCell('rowNumber').value
        docDateCell = row.getCell('docDate')
        for (alias in ['fullName', 'inn', 'countryName', 'countryCode', 'docNum', 'docDate', 'dealNumber', 'dealDate'
                , 'currencyCode', 'countryDealCode', 'price', 'total', 'dealDoneDate']) {
            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                logger.error('Графа «' + row.getCell(alias).column.name + '» в строке ' + rowNum + ' не заполнена!')
            }
        }
        // Проверка заполнения доходов и расходов Банка
        incomeSumCell = row.getCell('incomeSum')
        outcomeSum = row.getCell('outcomeSum')
        if (incomeSumCell.value != null && row.getCell('outcomeSum').value != null) {
            logger.error('«' + incomeSumCell.column.name + '» и «' + outcomeSum.column.name + '» в строке ' +
                    rowNum + ' не могут быть одновременно заполнены!')
        }
        if (incomeSumCell.value == null && row.getCell('outcomeSum').value == null) {
            logger.error('Одна из граф «' + incomeSumCell.column.name + '» и «' + outcomeSum.column.name + '» в строке ' +
                    rowNum + ' должна быть заполнена!')
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
        // Корректность даты заключения сделки
        if (docDateCell.value > row.getCell('dealDate').value) {
            logger.error('«' + row.getCell('dealDate').column.name + '» не может быть меньше «' +
                    docDateCell.column.name + '» в строке ' + rowNum + '!')
        }
        // Проверка заполнения стоимости сделки
        if (row.getCell('total').value != row.getCell('price').value) {
            logger.error('«' + row.getCell('price').column.name + '» не может отличаться от  «' +
                    row.getCell('total').column.name + '» в строке ' + rowNum + '!')
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
        row.getCell('price').value = row.getCell('incomeSum').value != null ? row.getCell('incomeSum').value : row.getCell('outcomeSum').value
        // Расчет поля "Итого"
        row.getCell('total').value = row.getCell('price').value
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
        newRow.getCell('fullName').colSpan = 10

        // Расчеты подитоговых значений
        BigDecimal incomeSumItg = 0, outcomeSumItg = 0, totalItg = 0
        for (row in formData.dataRows) {

            incomeSum = row.getCell('incomeSum').value
            outcomeSum = row.getCell('outcomeSum').value
            total = row.getCell('total').value

            incomeSumItg += incomeSum != null ? incomeSum : 0
            outcomeSumItg += outcomeSum != null ? outcomeSum : 0
            totalItg += total != null ? total : 0
        }

        newRow.getCell('incomeSum').value = incomeSumItg
        newRow.getCell('outcomeSum').value = outcomeSumItg
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