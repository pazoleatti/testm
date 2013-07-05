package form_template.deal.foreign_currency

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Купля-продажа иностранной валюты
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

    for (row in formData.dataRows[i..formData.dataRows.size()-1]) {
        row.getCell('rowNumber').value = i++
    }
}

void addRow() {
    row = formData.createDataRow()
    for (alias in ['fullName', 'docNum', 'docDate', 'dealNumber', 'dealDate' ,'currencyCode',
            'countryDealCode','incomeSum','outcomeSum','dealDoneDate']) {
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
    // TODO
    FormData findForm = null
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
        for (alias in ['rowNumber', 'fullName', 'inn','countryName', 'countryCode', 'docNum', 'docDate', 'dealNumber', 'dealDate'
                , 'currencyCode', 'countryDealCode', 'price', 'total', 'dealDoneDate']) {
            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                logger.error('Поле «' + row.getCell(alias).column.name + '» не заполнено!')
            }
        }

        if ( row.getCell('incomeSum').value != null && row.getCell('outcomeSum').value != null) {
            logger.error('Поля «Сумма доходов Банка по данным бухгалтерского учета, руб.» ' +
                    'и «Сумма расходов Банка по данным бухгалтерского учета, руб.» в строке '+
                    (formData.dataRows.indexOf(row)+1)+' не могут быть одновременно заполнены!')
        }

        if ( row.getCell('incomeSum').value == null && row.getCell('outcomeSum').value == null) {
            logger.error('Одно из полей «Сумма доходов Банка по данным бухгалтерского учета, руб.» ' +
                    'и «Сумма расходов Банка по данным бухгалтерского учета, руб.» в строке ' +
                    (formData.dataRows.indexOf(row)+1)+' должно быть заполнено!')
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
        row.getCell('price').value = row.getCell('incomeSum').value!=null ? row.getCell('incomeSum').value : row.getCell('outcomeSum').value
        // Расчет поля "Итого"
        row.getCell('total').value = row.getCell('price').value
        // TODO расчет полей по справочникам
    }
}