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
    for (alias in ['fullName', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealType',
            'currencyCode', 'countryDealCode', 'incomeSum', 'outcomeSum', 'dealDoneDate']) {
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
        dealDateCell = row.getCell('dealDate')
        docDateCell = row.getCell('docDate')
        for (alias in ['rowNumber', 'fullName', 'inn', 'countryName', 'countryCode', 'docNumber', 'docDate', 'dealNumber', 'dealDate'
                , 'dealType', 'currencyCode', 'countryDealCode', 'price', 'total', 'dealDoneDate']) {
            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                logger.error('Графа «' + row.getCell(alias).column.name + '» в строке ' + rowNum + ' не заполнена!')
            }
        }

        // Проверка доходов и расходов
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
        if (docDateCell.value > dealDateCell.value) {
            logger.error('«' + dealDateCell.column.name + '» не может быть меньше «' +
                    docDateCell.column.name + '» в строке ' + rowNum + '!')
        }
        // Корректность даты совершения сделки
        if (row.getCell('dealDoneDate').value < dealDateCell.value) {
            logger.error('«' + row.getCell('dealDoneDate').column.name + '» в строке ' + rowNum + ' не может быть меньше «' +
                    dealDateCell.column.name + '» в строке ' + rowNum + '!')
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

    newRow.getCell('fullName').value = 'Подитог:'

    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fullName').colSpan = 11

    // Расчеты подитоговых значений
    BigDecimal incomeSumItg = 0, outcomeSumItg = 0, totalItg = 0
    for (int j = i; j >= 0 && formData.dataRows.get(j).getAlias() == null; j--) {
        row = formData.dataRows.get(j)

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