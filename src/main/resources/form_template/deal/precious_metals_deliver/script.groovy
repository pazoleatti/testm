package form_template.deal.precious_metals_deliver

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Поставочные срочные сделки с драгоценными металлами
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

    for (alias in ['name', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate', 'innerCode',
            'unitCountryCode', 'signPhis', 'countryCode2', 'region1', 'city1', 'settlement1',
            'countryCode3', 'region2', 'city2', 'settlement2', 'conditionCode', 'count', 'incomeSum', 'consumptionSum',
            'transactionDate']) {
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
            for (alias in ['name', 'innKio', 'country', 'countryCode1', 'contractNum', 'contractDate',
                    'transactionNum', 'transactionDeliveryDate', 'innerCode', 'okpCode', 'unitCountryCode', 'signPhis',
                    'signTransaction', 'count', 'priceOne', 'totalNds', 'transactionDate']) {
                if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                    msg = row.getCell(alias).column.name
                    rowNum = row.getCell('rowNum').value
                    logger.error("Графа «$msg» в строке $rowNum не заполнена!")
                }
            }
        }
    }

    // Проверка зависимости от признака физической поставки
    for (row in formData.dataRows) {
        signPhis =  row.getCell('signPhis').value
        // TODO Если графа 13 = элемент с кодом «1», заменить условие когда будет справочник!
        if (signPhis == 1) {
            for (alias in ['countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2', 'city2',
                    'settlement2', 'conditionCode']) {
                if (row.getCell(alias).value != null && !row.getCell(alias).value.toString().isEmpty()) {
                    msg1 = row.getCell('signPhis').column.name
                    msg2 = row.getCell(alias).column.name
                    rowNum = row.getCell('rowNum').value
                    logger.error("«$msg1» указан «ОМС», графа «$msg2» строки $rownum заполняться не должна!")
                }
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

    // Корректность заполнения признака внешнеторговой сделки
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            countryCode2 = row.getCell('countryCode2').value
            countryCode3 = row.getCell('countryCode3').value
            signTransaction = row.getCell('signTransaction').value
            // TODO Проверка по справочникам
            // Если графа 15 = графе 19, то графа 14 должна иметь значение «Нет», иначе – «Да»
        }
    }

    // Проверка населенного пункта 1
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            settlement1 = row.getCell('settlement1').value
            city1 = row.getCell('city1').value
            if (settlement1 != null && !settlement1.toString().isEmpty() && city1 != null && !city1.toString().isEmpty()) {
                msg1 = row.getCell('settlement1').column.name
                msg2 = row.getCell('city1').column.name
                rowNum = row.getCell('rowNum').value
                logger.error("Если указан «$msg1», не должен быть указан «$msg2» в строке $rowNum")
            }
        }
    }

    // Проверка населенного пункта 2
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            settlement2 = row.getCell('settlement2').value
            city2 = row.getCell('city2').value
            if (settlement2 != null && !settlement2.toString().isEmpty() && city2 != null && !city2.toString().isEmpty()) {
                rowNum = row.getCell('rowNum').value
                msg1 = row.getCell('settlement2').column.name
                msg2 = row.getCell('city2').column.name
                logger.error("Если указан «$msg1», не должен быть указан «$msg2» в строке $rowNum")
            }
        }
    }

    // Проверка доходов и расходов
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            incomeSum = row.getCell('incomeSum').value
            consumptionSum = row.getCell('consumptionSum').value

            if (incomeSum != null && consumptionSum != null) {
                msg1 = row.getCell('incomeSum').column.name
                msg2 = row.getCell('consumptionSum').column.name
                rowNum = row.getCell('rowNum').value
                logger.error("«$msg1» и «$msg2» в строке $rowNum не могут быть одновременно заполнены!")
            }

            if (incomeSum == null && consumptionSum == null) {
                msg1 = row.getCell('incomeSum').column.name
                msg2 = row.getCell('consumptionSum').column.name
                rowNum = row.getCell('rowNum').value
                logger.error("Одна из граф «$msg1» и «$msg2» в строке $rowNum должна быть заполнена!")
            }
        }
    }

    // Проверка количества
    for (row in formData.dataRows) {
        count = row.getCell('count').value
        if (count != null && count != 1) {
            msg = row.getCell('count').column.name
            rowNum = row.getCell('rowNum').value
            logger.error('В графе «$msg» может быть указано только значение «1» в строке $rowNum!')
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

/**
 * Сортировка строк по гр. 2 / гр. 3, гр. 5 / гр. 6, гр. 9, гр. 10, гр. 11, гр. 12, гр. 13, гр. 14, гр. 15.
 */
void sort() {
    formData.dataRows.sort({ DataRow a, DataRow b ->
        // name - innKio - contractNum - contractDate - transactionType
        if (a.name == b.name) {
            if (a.innKio == b.innKio) {
                if (a.countryCode1 == b.countryCode1) {
                    if (a.contractNum == b.contractNum) {
                        if (a.transactionDeliveryDate == b.transactionDeliveryDate) {
                            if (a.innerCode == b.innerCode) {
                                if (a.okpCode == b.okpCode) {
                                    if (a.unitCountryCode == b.unitCountryCode) {
                                        if (a.signPhis == b.signPhis) {
                                            if (a.signTransaction == b.signTransaction) {
                                                return a.countryCode2 <=> b.countryCode2
                                            }
                                            return a.signTransaction <=> b.signTransaction
                                        }
                                        return a.signPhis <=> b.signPhis
                                    }
                                    return a.unitCountryCode <=> b.unitCountryCode
                                }
                                return a.okpCode <=> b.okpCode
                            }
                            return a.innerCode <=> b.innerCode
                        }
                        return a.transactionDeliveryDate <=> b.transactionDeliveryDate
                    }
                    return a.contractNum <=> b.contractNum
                }
                return a.countryCode1 <=> b.countryCode1
            }
            return a.innKio <=> b.innKio
        }
        return a.name <=> b.name;
    })
    recalcRowNum()
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
                    || row.countryCode1 != nextRow.countryCode1
                    || row.contractNum != nextRow.contractNum
                    || row.transactionDeliveryDate != nextRow.transactionDeliveryDate
                    || row.innerCode != nextRow.innerCode
                    || row.okpCode != nextRow.okpCode
                    || row.unitCountryCode != nextRow.unitCountryCode
                    || row.signPhis != nextRow.signPhis
                    || row.signTransaction != nextRow.signTransaction
                    || row.countryCode2 != nextRow.countryCode2) {
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


        incomeSumItg += incomeSum != null ? incomeSum : 0
        consumptionSumItg += consumptionSum != null ? consumptionSum : 0
    }

    newRow.getCell('incomeSum').value = incomeSumItg
    newRow.getCell('consumptionSum').value = consumptionSumItg

    newRow
}