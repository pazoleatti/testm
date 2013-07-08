package form_template.deal.bonds_trade

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Реализация и приобретение ценных бумаг
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
        for (alias in ['rowNum', 'transactionDeliveryDate', 'contraName', 'transactionMode', 'innKio', 'contraCountry',
                'contraCountryCode', 'transactionSumCurrency', 'currency', 'courseCB', 'transactionSumRub',
                'contractNum', 'contractDate', 'transactionDate', 'bondRegCode', 'bondCount', 'priceOne',
                'transactionType']) {
            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                msg = row.getCell(alias).column.name
                logger.error("Поле «$msg» не заполнено!")
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