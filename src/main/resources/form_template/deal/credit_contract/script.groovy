package form_template.deal.credit_contract

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Уступка прав требования по кредитным договорам
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

    for (alias in ['name', 'contractNum', 'contractDate', 'okeiCode', 'price', 'transactionDate']) {
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
        for (alias in ['rowNum', 'name', 'innKio', 'country', 'contractNum', 'contractDate', 'okeiCode', 'count',
                'price', 'totalCost', 'transactionDate']) {
            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                msg = row.getCell(alias).column.name
                logger.error("Поле «$msg» не заполнено!")
            }
        }
    }

    // Проверка выбранной единицы измерения
    for (row in formData.dataRows) {
        // TODO поле справочника "код"
        // logger.error('В поле «Код единицы измерения по ОКЕИ» могут быть указаны только следующие элементы: шт.!')
    }
    // Проверка количества
    for (row in formData.dataRows) {
        if (row.getCell('count').value != 1) {
            logger.error('В поле «Количество» может быть указано только значение «1»!')
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