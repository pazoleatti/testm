package form_template.deal.precious_metals_deliver

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Поставочные срочные сделки с драгоценными металлами
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

    for (alias in ['name', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate', 'innerCode',
            'unitCountryCode', 'signPhis', 'signTransaction', 'countryCode2', 'region1', 'city1', 'settlement1',
            'countryCode3', 'region2', 'city2', 'settlement2', 'conditionCode', 'count', 'incomeSum', 'consumptionSum',
            'transactionDate']) {
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
        for (alias in ['rowNum', 'name', 'innKio', 'country', 'countryCode1', 'contractNum', 'contractDate',
                'transactionNum', 'transactionDeliveryDate', 'innerCode', 'okpCode', 'unitCountryCode', 'signPhis',
                'signTransaction', 'count', 'priceOne', 'totalNds', 'transactionDate']) {
            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                msg = row.getCell(alias).column.name
                logger.error("Поле «$msg» не заполнено!")
            }
        }
    }

    // Проверка взаимозависимых ячеек 1
    for (row in formData.dataRows) {
        signPhis =  row.getCell('signPhis').value
        if (signPhis == 1) {
            for (alias in ['region1', 'city1', 'settlement1', 'countryCode3', 'region2', 'city2', 'settlement2']) {
                if (row.getCell(alias).value != null && !row.getCell(alias).value.toString().isEmpty()) {
                    msg = row.getCell(alias).column.name
                    rowNum = row.getCell('rowNum').value
                    logger.error("«Признак физической поставки драгоценного металла» указан «ОМС», " +
                            "графа «$msg» строки $rownum заполняться не должна!")
                }
            }
        }
    }

    // Проверка взаимозависимых ячеек 2
    for (row in formData.dataRows) {
        settlement1 = row.getCell('settlement1').value
        city1 = row.getCell('city1').value
        if (settlement1 != null && !settlement1.toString().isEmpty() && city1 != null && !city1.toString().isEmpty()) {
            rowNum = row.getCell('rowNum').value
            logger.error("Если указан «Населенный пункт», не должен быть указан «Город» в строке $rowNum")
        }
    }

    // Проверка взаимозависимых ячеек 3
    for (row in formData.dataRows) {
        settlement2 = row.getCell('settlement2').value
        city2 = row.getCell('city2').value
        if (settlement2 != null && !settlement2.toString().isEmpty() && city2 != null && !city2.toString().isEmpty()) {
            rowNum = row.getCell('rowNum').value
            logger.error("Если указан «Населенный пункт», не должен быть указан «Город» в строке $rowNum")
        }
    }

    // Проверка взаимоисключающих ячеек
    for (row in formData.dataRows) {
        incomeSum = row.getCell('incomeSum').value
        consumptionSum = row.getCell('consumptionSum').value

        if (incomeSum != null && consumptionSum != null) {
            rowNum = row.getCell('rowNum').value
            logger.error("Поля «Сумма доходов Банка по данным бухгалтерского учета, руб.» и «Сумма расходов Банка по данным бухгалтерского учета, руб.» в строке $rowNum не могут быть одновременно заполнены!")
        }

        if (incomeSum == null && consumptionSum == null) {
            rowNum = row.getCell('rowNum').value
            logger.error("Одно из полей «Сумма доходов Банка по данным бухгалтерского учета, руб.» и «Сумма расходов Банка по данным бухгалтерского учета, руб.» в строке $rowNum должно быть заполнено!")
        }
    }
    // Проверка количества
    for (row in formData.dataRows) {
        count = row.getCell('count').value
        if (count != null && count != 1) {
            logger.error('В поле «Количество» может  быть указано только  значение «1»!')
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

        // Графы 27 и 28 из 25 и 26
        incomeSum = row.getCell('incomeSum').value
        consumptionSum = row.getCell('incomeSum').value

        if (incomeSum != null) {
            row.getCell('priceOne').value = incomeSum
            row.getCell('totalNds').value = incomeSum
        }

        if (consumptionSum != null) {
            row.getCell('priceOne').value = consumptionSum
            row.getCell('totalNds').value = consumptionSum
        }
    }
}