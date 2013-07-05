package form_template.deal.guarantees

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Предоставление гарантий
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
    for (alias in ['fullName', 'docNumber', 'docDate', 'dealNumber', 'dealDate' ,'sum', 'dealDoneDate']) {
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
        for (alias in ['rowNumber', 'fullName', 'inn', 'countryCode', 'docNumber', 'docDate', 'dealNumber', 'dealDate',
                'sum', 'price', 'total', 'dealDoneDate']) {
            if (row.getCell(alias).value == null) {
                logger.error('Поле «' + row.getCell(alias).column.name + '» не заполнено!')
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