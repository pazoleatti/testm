package form_template.deal.securities

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

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

    for (row in formData.dataRows[i..formData.dataRows.size()-1]) {
        row.getCell('rowNumber').value = i++
    }
}

void addRow() {
    row = formData.createDataRow()
    for (alias in ['fullNamePerson', 'dealSign', 'incomeSum', 'outcomeSum', 'docNumber', 'docDate', 'okeiCode', 'count' ,'dealDate']) {
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
        for (alias in ['rowNumber', 'fullNamePerson', 'inn', 'countryCode', 'docNumber', 'docDate',
                'okeiCode', 'count', 'price', 'cost', 'dealDate']) {
            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                logger.error('Поле «' + row.getCell(alias).column.name + '» не заполнено!')
            }
        }

        if ( row.getCell('incomeSum').value != null && row.getCell('outcomeSum').value != null) {
            logger.error('Поля «Сумма доходов (стоимость реализации) Банка, руб.» ' +
                    'и «Сумма расходов (стоимость приобретения) Банка, руб.» в строке ' +
                    (formData.dataRows.indexOf(row)+1)+' не могут быть одновременно заполнены!')
        }

        if ( row.getCell('incomeSum').value == null && row.getCell('outcomeSum').value == null) {
            logger.error('Одно из полей «Сумма доходов (стоимость реализации) Банка, руб.» ' +
                    'и «Сумма расходов (стоимость приобретения) Банка, руб.» в строке ' +
                    (formData.dataRows.indexOf(row)+1)+' должно быть заполнено!')
        }
        if (! row.getCell('okeiCode').value in ['796', '744']) {
            logger.error('В поле «Код единицы измерения по ОКЕИ» могут быть указаны только следующие элементы: шт., процент!')
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
        pricaValue = row.getCell('incomeSum').value!=null ? row.getCell('incomeSum').value : row.getCell('outcomeSum').value
        if (row.getCell('okeiCode').value == '744'){
            row.getCell('price').value = pricaValue
        } else if (row.getCell('okeiCode').value == '796'){
            row.getCell('price').value = pricaValue +' / '+ row.getCell('count').value
        }
        // Расчет поля "Стоимость"
        row.getCell('cost').value =  pricaValue

        // TODO расчет полей по справочникам
    }
}