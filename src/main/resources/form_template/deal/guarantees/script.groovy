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
        for (alias in ['fullName', 'inn', 'countryName', 'docNumber', 'docDate', 'dealNumber', 'dealDate',
                'sum', 'price', 'total', 'dealDoneDate']) {

            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                logger.error('Графа «' + row.getCell(alias).column.name + '» в строке ' + rowNum + ' не заполнена!')
            }

            //  Корректность даты договора
            // TODO docDate должна относиться к календарному году, указанному для отчётного периода
            if (false) {
                logger.error('«' + row.getCell('docDate').column.name + '» в строке ' + rowNum + ' не может быть больше даты окончания отчётного периода!')
            }
            // Корректность даты сделки
            if (row.getCell('docDate').value > row.getCell('dealDate').value) {
                logger.error('«' + row.getCell('dealDate').column.name + '» не может быть меньше «' +
                        row.getCell('docDate').column.name + '» в строке ' + rowNum + '!')
            }
            // Проверка доходности
            if (row.getCell('price').value != row.getCell('sum').value) {
                logger.error('«' + row.getCell('price').column.name + '» не может отличаться от  «' +
                        row.getCell('sum').column.name + '» в строке ' + rowNum + '!')
            }
            // Проверка доходности
            if (row.getCell('total').value != row.getCell('sum').value) {
                logger.error('«' + row.getCell('total').column.name + '» не может отличаться от  «' +
                        row.getCell('sum').column.name + '» в строке ' + rowNum + '!')
            }
            // Корректность даты совершения сделки
            if (row.getCell('dealDate').value > row.getCell('dealDoneDate').value) {
                logger.error('«' + row.getCell('dealDoneDate').column.name + '» не может быть меньше «' +
                        row.getCell('dealDate').column.name + '» в строке ' + rowNum + '!')
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