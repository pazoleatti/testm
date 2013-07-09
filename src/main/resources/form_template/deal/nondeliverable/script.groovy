package form_template.deal.nondeliverable

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Беспоставочные срочные сделки
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
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
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId,
            formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

void addRow() {
    row = formData.createDataRow()

    for (alias in ['name', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate',
            'transactionType', 'incomeSum', 'consumptionSum',  'transactionDate']) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }

    formData.dataRows.add(row)

    recalcRowNum()
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
            row.getCell('rowNum').value = i++
        }
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            for (alias in ['name', 'innKio', 'country', 'countryCode', 'contractNum', 'contractDate',
                    'transactionNum', 'transactionDeliveryDate', 'transactionType', 'price', 'cost', 'transactionDate']) {
                if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                    msg = row.getCell(alias).column.name
                    rowNum = row.getCell('rowNum').value
                    logger.error("Графа «$msg» в строке $rowNum не заполнена!")
                }
            }
        }
    }

    // Проверка доходов и расходов
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            consumptionSum = row.getCell('consumptionSum').value
            price = row.getCell('price').value

            // В одной строке не должны быть одновременно заполнены графы 12 и 13
            if (consumptionSum != null && price != null) {
                msg1 = row.getCell('consumptionSum').column.name
                msg2 = row.getCell('cost').column.name
                rowNum = row.getCell('rowNum').value
                logger.error("«$msg1» и «$msg2» в строке $rowNum не могут быть одновременно заполнены!")
            }

            // В одной строке если не заполнена графа 12, то должна быть заполнена графа 13 и наоборот
            if (consumptionSum == null && price == null) {
                msg1 = row.getCell('consumptionSum').column.name
                msg2 = row.getCell('cost').column.name
                rowNum = row.getCell('rowNum').value
                logger.error("Одна из граф «$msg1» и «$msg2» в строке $rowNum должно быть заполнена!")
            }
        }
    }

    // Отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // Налоговый период
    def taxPeriod = taxPeriodService.get(reportPeriod.taxPeriodId)

    def dFrom = taxPeriod.getStartDate()
    def dTo = taxPeriod.getEndDate()

    // Корректность даты договора
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {

            dt = row.getCell('contractDate').value
            if (dt != null && (dt < dFrom || dt > dTo)) {
                msg = row.getCell('contractDate').column.name
                rowNum = row.getCell('rowNum').value

                if (dt > dTo) {
                    logger.error("«$msg» не может быть больше даты окончания отчётного периода в строке $rowNum!")
                }

                if (dt < dFrom) {
                    logger.error("«$msg» не может быть меньше даты начала отчётного периода в строке $rowNum!")
                }
            }
        }
    }

    // Корректность даты заключения сделки
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            transactionDeliveryDate = row.getCell('transactionDeliveryDate').value
            contractDate = row.getCell('contractDate').value

            if (transactionDeliveryDate < contractDate) {
                msg1 = row.getCell('transactionDeliveryDate').column.name
                msg2 = row.getCell('contractDate').column.name
                rowNum = row.getCell('rowNum').value
                logger.error("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
            }
        }
    }

    // Проверка заполнения стоимости сделки
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            price = row.getCell('price').value
            cost = row.getCell('cost').value

            if (cost != price) {
                msg1 = row.getCell('cost').column.name
                msg2 = row.getCell('price').column.name
                rowNum = row.getCell('rowNum').value
                logger.error("«$msg1» не может отличаться от «$msg2» сделки в строке $rowNum!")
            }
        }
    }

    // Корректность дат сделки
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            transactionDate = row.getCell('transactionDate').value
            transactionDeliveryDate = row.getCell('transactionDeliveryDate').value

            if (transactionDate < transactionDeliveryDate) {
                msg1 = row.getCell('transactionDate').column.name
                msg2 = row.getCell('transactionDeliveryDate').column.name
                rowNum = row.getCell('rowNum').value
                logger.error("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
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

            if (row.getAlias() == null && nextRow == null
                    || row.name != nextRow.name
                    || row.innKio != nextRow.innKio
                    || row.contractNum != nextRow.contractNum
                    || row.contractDate != nextRow.contractDate
                    || row.transactionType != nextRow.transactionType) {
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

    newRow.name = 'Подитог:'

    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('name').colSpan = 9

    // Расчеты подитоговых значений
    BigDecimal incomeSumItg = 0, consumptionSumItg = 0, costItg = 0
    for (int j = i; j >= 0 && formData.dataRows.get(j).getAlias() == null; j--) {
        row = formData.dataRows.get(j)

        incomeSum = row.getCell('incomeSum').value
        consumptionSum = row.getCell('consumptionSum').value
        cost = row.getCell('cost').value

        incomeSumItg += incomeSum != null ? incomeSum : 0
        consumptionSumItg += consumptionSum != null ? consumptionSum : 0
        costItg += cost != null ? cost : 0
    }

    newRow.getCell('incomeSum').value = incomeSumItg
    newRow.getCell('consumptionSum').value = consumptionSumItg
    newRow.getCell('cost').value = costItg

    newRow
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {

    for (row in formData.dataRows) {
        // TODO расчет полей по справочникам

        // Графы 13 и 14 из 11 и 12
        incomeSum = row.getCell('incomeSum').value
        consumptionSum = row.getCell('consumptionSum').value

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

/**
 * Сортировка строк по гр. 2, гр. 3, гр. 6, гр. 7, гр. 10
 */
void sort() {
    formData.dataRows.sort({ DataRow a, DataRow b ->
        // name - innKio - contractNum - contractDate - transactionType
        if (a.name == b.name) {
            if (a.innKio == b.innKio) {
                if (a.contractNum == b.contractNum) {
                    if (a.contractDate == b.contractDate) {
                        return a.transactionType <=> b.transactionType
                    }
                    return a.contractDate <=> b.contractDate
                }
                return a.contractNum <=> b.contractNum
            }
            return a.innKio <=> b.innKio
        }
        return a.name <=> b.name;
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
