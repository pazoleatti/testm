package form_template.deal.nondeliverable

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Беспоставочные срочные сделки
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

    for (alias in ['name', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate',
            'transactionType', 'incomeSum', 'price', 'cost', 'transactionDate']) {
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
        for (alias in ['rowNum', 'name', 'innKio', 'country', 'countryCode', 'contractNum', 'contractDate',
                'transactionNum', 'transactionDeliveryDate', 'transactionType', 'price', 'cost', 'transactionDate']) {
            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                msg = row.getCell(alias).column.name
                logger.error("Поле «$msg» не заполнено!")
            }
        }
    }

    // Проверка взаимоисключающих ячеек
    for (row in formData.dataRows) {
        consumptionSum = row.getCell('consumptionSum').value
        price = row.getCell('price').value

        // В одной строке не должны быть одновременно заполнены графы 12 и 13
        if (consumptionSum != null && price != null) {
            rowNum = row.getCell('rowNum')
            logger.error("Поля «Сумма доходов Банка по данным бухгалтерского учета, руб.» и «Сумма расходов Банка по данным бухгалтерского учета, руб.» в строке $rowNum не могут быть одновременно заполнены!")
        }

        // В одной строке если не заполнена графа 12, то должна быть заполнена графа 13 и наоборот
        if (consumptionSum == null && price == null) {
            rowNum = row.getCell('rowNum')
            logger.error("Одно из полей «Сумма доходов Банка по данным бухгалтерского учета, руб.» и «Сумма расходов Банка по данным бухгалтерского учета, руб.» в строке $rowNum должно быть заполнено!")
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
        // TODO расчет полей по справочникам

        // Графы 13 и 14 из 11 и 12
        incomeSum =  rowNum = row.getCell('incomeSum')
        consumptionSum =  rowNum = row.getCell('consumptionSum')

        if (incomeSum != null) {
            row.getCell('price').value = incomeSum
            row.getCell('cost').value = incomeSum
        }

        if (consumptionSum != null) {
            row.getCell('price').value = consumptionSum
            row.getCell('cost').value = consumptionSum
        }
    }
}