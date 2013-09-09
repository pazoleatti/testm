package form_template.deal.precious_metals_deliver

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * 393 - Поставочные срочные сделки с драгоценными металлами
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
// После принятия из Утверждено
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
        logicCheck()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicCheck()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId,
            formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? currentDataRow.getIndex() : (size == 0 ? 1 : size)
    ['name', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate', 'innerCode',
            'unitCountryCode', 'signPhis', 'countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2',
            'city2', 'settlement2', 'conditionCode', 'count', 'incomeSum', 'consumptionSum', 'transactionDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
}

void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    // Налоговый период
    def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod

    def dFrom = taxPeriod.getStartDate()
    def dTo = taxPeriod.getEndDate()

    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }

        def rowNum = row.getIndex()

        [
                'rowNum', // № п/п
                'name', // Полное наименование с указанием ОПФ
                'innKio', // ИНН/КИО
                'country', // Наименование страны регистрации
                'countryCode1', // Код страны по классификатору ОКСМ
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'transactionNum', // Номер сделки
                'transactionDeliveryDate', // Дата заключения сделки
                'innerCode', // Внутренний код
                'okpCode', // Код ОКП
                'unitCountryCode', // Код страны происхождения предмета сделки по классификатору ОКСМ
                'signPhis', // Признак физической поставки драгоценного металла
                'signTransaction', // Признак внешнеторговой сделки
                'count', // Количество
                'priceOne', // Цена (тариф) за единицу измерения без учета НДС, руб.
                'totalNds', // Итого стоимость без учета НДС, руб.
                'transactionDate' // Дата совершения сделки
        ].each {
            if (row.getCell(it).value == null || row.getCell(it).value.toString().isEmpty()) {
                msg = row.getCell(it).column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }

        def transactionDeliveryDate = row.transactionDeliveryDate
        def contractDate = row.contractDate
        // def countryCode2 = row.getCell('countryCode2').value
        // def countryCode3 = row.getCell('countryCode3').value
        def settlement1 = row.settlement1
        def city1 = row.city1
        def settlement2 = row.settlement2
        def city2 = row.city2
        def incomeSum = row.incomeSum
        def consumptionSum = row.consumptionSum
        def priceOne = row.priceOne
        def totalNds = row.totalNds
        def count = row.count
        def transactionDate = row.transactionDate

        // Проверка зависимости от признака физической поставки
        def signPhis = row.signPhis
        if (signPhis != null && refBookService.getNumberValue(18, signPhis, 'CODE') == 1) {
            ['countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2', 'city2',
                    'settlement2', 'conditionCode'].each {
                if (row.getCell(it).value != null && !row.getCell(it).value.toString().isEmpty()) {
                    def msg1 = row.getCell('signPhis').column.name
                    def msg2 = row.getCell(it).column.name
                    logger.warn("«$msg1» указан «ОМС», графа «$msg2» строки $rowNum заполняться не должна!")
                }
            }
        }

        // Корректность даты договора
        def dt = contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = row.getCell('contractDate').column.name

            if (dt > dTo) {
                logger.warn("«$msg» не может быть больше даты окончания отчётного периода в строке $rowNum!")
            }

            if (dt < dFrom) {
                logger.warn("«$msg» не может быть меньше даты начала отчётного периода в строке $rowNum!")
            }
        }

        // Корректность даты заключения сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = row.getCell('transactionDeliveryDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        // Корректность заполнения признака внешнеторговой сделки
        def msg14 = row.getCell('signTransaction').column.name
        def sign = refBookService.getNumberValue(38, row.signTransaction, 'CODE')
        if (row.countryCode2 == row.countryCode3 && sign != 0) {
            logger.warn("«$msg14» в строке $rowNum должен быть «Нет»!")
        } else if (row.countryCode2 != row.countryCode3 && sign != 1) {
            logger.warn("«$msg14» в строке $rowNum должен быть «Да»!")
        }

        // Проверка населенного пункта 1
        if (settlement1 != null && !settlement1.toString().isEmpty() && city1 != null && !city1.toString().isEmpty()) {
            def msg1 = row.getCell('settlement1').column.name
            def msg2 = row.getCell('city1').column.name
            logger.warn("Если указан «$msg1», не должен быть указан «$msg2» в строке $rowNum")
        }

        // Проверка населенного пункта 2
        if (settlement2 != null && !settlement2.toString().isEmpty() && city2 != null && !city2.toString().isEmpty()) {
            def msg1 = row.getCell('settlement2').column.name
            def msg2 = row.getCell('city2').column.name
            logger.warn("Если указан «$msg1», не должен быть указан «$msg2» в строке $rowNum")
        }

        // Проверка доходов и расходов
        if (incomeSum != null && consumptionSum != null) {
            def msg1 = row.getCell('incomeSum').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            logger.warn("«$msg1» и «$msg2» в строке $rowNum не могут быть одновременно заполнены!")
        }

        if (incomeSum == null && consumptionSum == null) {
            def msg1 = row.getCell('incomeSum').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            logger.warn("Одна из граф «$msg1» и «$msg2» в строке $rowNum должна быть заполнена!")
        }

        // Проверка доходов/расходов и стоимости
        if (incomeSum != null && consumptionSum == null && priceOne != incomeSum) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('incomeSum').column.name
            logger.warn("Графа «$msg1» должна быть равна «$msg2» в строке $rowNum!")
        }
        if (incomeSum == null && consumptionSum != null && priceOne != consumptionSum) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            logger.warn("Графа «$msg1» должна быть равна «$msg2» в строке $rowNum!")
        }
        if (incomeSum != null && consumptionSum != null &&
                (priceOne == null
                        || consumptionSum == null
                        || incomeSum == null
                        || priceOne.abs() != (consumptionSum - incomeSum).abs())) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            def msg3 = row.getCell('incomeSum').column.name
            logger.warn("Графа «$msg1» должна быть равна разнице графы «$msg2» и графы «$msg3» по модулю в строке $rowNum!")
        }

        // Проверка количества
        if (count != null && count != 1) {
            def msg = row.getCell('count').column.name
            logger.warn("В графе «$msg» может быть указано только значение «1» в строке $rowNum!")
        }

        // Корректность дат сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('transactionDeliveryDate').column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        // Проверка заполнения стоимости сделки
        if (priceOne != totalNds) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('totalNds').column.name
            logger.warn("«$msg1» не может отличаться от «$msg2» сделки в строке $rowNum!")
        }

        // Проверки соответствия НСИ
        checkNSI(row, "name", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "country", "ОКСМ", 10)
        checkNSI(row, "unitCountryCode", "ОКСМ", 10)
        checkNSI(row, "countryCode1", "ОКСМ", 10)
        checkNSI(row, "settlement1", "ОКСМ", 10)
        checkNSI(row, "region1", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "countryCode3", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "okpCode", "Коды драгоценных металлов", 17)
        checkNSI(row, "signPhis", "Признаки физической поставки", 18)
        checkNSI(row, "conditionCode", "Коды условий поставки", 63)
    }
}

/**
 * Проверка соответствия НСИ
 */
void checkNSI(DataRow<Cell> row, String alias, String msg, Long id) {
    def cell = row.getCell(alias)
    if (cell.value != null && refBookService.getRecordData(id, cell.value) == null) {
        def msg2 = cell.column.name
        def rowNum = row.getIndex()
        logger.warn("В справочнике «$msg» не найден элемент графы «$msg2», указанный в строке $rowNum!")
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def index = 1;
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // Порядковый номер строки
        row.rowNum = index++
        // Графы 27 и 28 из 25 и 26
        incomeSum = row.incomeSum
        consumptionSum = row.incomeSum

        if (incomeSum != null) {
            row.priceOne = incomeSum
            row.totalNds = incomeSum
        }

        if (consumptionSum != null) {
            row.priceOne = consumptionSum
            row.totalNds = consumptionSum
        }

        // Расчет полей зависимых от справочников
        if (row.name != null) {
            def map = refBookService.getRecordData(9, row.name)
            row.innKio = map.INN_KIO.numberValue
            row.country = map.COUNTRY.referenceValue
            row.countryCode1 = map.COUNTRY.referenceValue
        } else {
            row.innKio = null
            row.country = null
            row.countryCode1 = null
        }
        if (row.signPhis == 1) {
            row.countryCode2 = null
            row.region1 = null
            row.city1 = null
            row.settlement1 = null
            row.countryCode3 = null
            row.region2 = null
            row.city2 = null
            row.settlement2 = null
        }
        if (row.countryCode2 == row.countryCode3) {
            row.signTransaction = Long.valueOf(182632)
        } else {
            row.signTransaction = Long.valueOf(182633)
        }
    }
    dataRowHelper.update(dataRows);
}

/**
 * Удаление всех статическиех строк "Подитог" из списка строк
 */
void deleteAllStatic() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (Iterator<DataRow> iter = dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            iter.remove()
            dataRowHelper.delete(row)
        }
    }
}

/**
 * Сортировка строк по гр.
 */
void sort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    dataRows.sort({ DataRow a, DataRow b ->
        // гр. 2, гр. 3, гр. 6, гр. 7, гр. 10, гр. 11, гр. 12, гр. 13, гр. 14, гр. 15, гр. 16, гр. 17, гр. 18, гр. 19,
        // гр. 20, гр. 21, гр. 22, гр. 23, гр. 24.
        sortRow(['name', 'innKio', 'contractNum', 'contractDate', 'innerCode', 'okpCode', 'unitCountryCode', 'signPhis',
                'signTransaction', 'countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2',
                'city2', 'settlement2', 'conditionCode', 'count'], a, b)
    })
    dataRowHelper.save(dataRows);
}

int sortRow(List<String> params, DataRow a, DataRow b) {
    for (String param : params) {
        aD = a.getCell(param).value
        bD = b.getCell(param).value

        if (aD != bD) {
            return aD <=> bD
        }
    }
    return 0
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.getAllCached()

        for (int i = 0; i < dataRows.size(); i++) {
            def row = dataRows.get(i)
            def nextRow = null

            if (i < dataRows.size() - 1) {
                nextRow = dataRows.get(i + 1)
            }
            if (row.getAlias() == null)
                if (nextRow == null
                        || row.name != nextRow.name
                        || row.innKio != nextRow.innKio
                        || row.contractNum != nextRow.contractNum
                        || row.contractDate != nextRow.contractDate
                        || row.innerCode != nextRow.innerCode
                        || row.okpCode != nextRow.okpCode
                        || row.unitCountryCode != nextRow.unitCountryCode
                        || row.signPhis != nextRow.signPhis
                        || row.signTransaction != nextRow.signTransaction
                        || row.countryCode2 != nextRow.countryCode2
                        || row.region1 != nextRow.region1
                        || row.city1 != nextRow.city1
                        || row.settlement1 != nextRow.settlement1
                        || row.countryCode3 != nextRow.countryCode3
                        || row.region2 != nextRow.region2
                        || row.city2 != nextRow.city2
                        || row.settlement2 != nextRow.settlement2
                        || row.conditionCode != nextRow.conditionCode
                        || row.count != nextRow.count) {

                    def itogRow = calcItog(i)
                    dataRows.add(i + 1, itogRow)
                    dataRowHelper.insert(itogRow, ++i + 1)
                }
        }
    }
}

/**
 * Расчет подитогового значения
 * @param i
 * @return
 */
def calcItog(int i) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def newRow = formData.createDataRow()

    newRow.getCell('itog').colSpan = 26
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2

    // Расчеты подитоговых значений
    BigDecimal totalNdsItg = 0, priceOneItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)

        priceOne = row.priceOne
        totalNds = row.totalNds

        priceOneItg += priceOne != null ? priceOne : 0
        totalNdsItg += totalNds != null ? totalNds : 0
    }

    newRow.priceOne = priceOneItg
    newRow.totalNds = totalNdsItg

    newRow
}

/**
 * Консолидация
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    dataRows.clear()

    int index = 1;
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null
                && source.state == WorkflowState.ACCEPTED
                && source.getFormType().getId() == formData.getFormType().getId()) {
            formDataService.getDataRowHelper(source).getAllCached().each { row ->
                if (row.getAlias() == null) {
                    dataRowHelper.insert(row, index++)
                }
            }
        }
    }
}