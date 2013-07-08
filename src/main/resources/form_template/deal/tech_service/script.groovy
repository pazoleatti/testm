package form_template.deal.tech_service

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Техническое обслуживание нежилых помещений
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {

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

void addRow() {
    row = formData.createDataRow()

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

    for (row in formData.dataRows[i..formData.dataRows.size()-1]) {
        row.getCell('rowNum').value = i++
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    for (row in formData.dataRows) {
        for (alias in ['rowNum', 'jurName', 'innKio', 'countryCode', 'bankSum', 'contractNum', 'contractDate',
                'country', 'count', 'price', 'cost', 'transactionDate']) {
            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                msg = row.getCell(alias).column.name
                logger.error("Поле «$msg» не заполнено!")
            }
        }
    }

    // Проверка стоимости
    for (row in formData.dataRows) {
        cost = row.getCell('cost').value
        price = row.getCell('price').value
        count = row.getCell('count').value

        if (price == null || count || cost !=  price * count) {
           logger.warn('Стоимость не равна произведению цены и количества!')
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
        // Расчет поля "Стоимость"
        row.getCell('cost').value = row.getCell('bankSum').value
        // TODO расчет полей по справочникам
    }
}