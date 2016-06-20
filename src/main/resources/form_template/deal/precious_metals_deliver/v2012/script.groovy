package form_template.deal.precious_metals_deliver.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

import java.text.SimpleDateFormat

/**
 * 393 - Поставочные срочные сделки с драгоценными металлами (18)
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
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        logicCheck()
        break
// Импорт
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            deleteAllStatic()
            sort()
            calc()
            addAllStatic()
            logicCheck()
        }
        break
}

def getAtributes() {
    [
            rowNum:                     ['rowNum', 'гр. 1', '№ п/п'],
            name:                       ['name', 'гр. 2', 'Полное наименование с указанием ОПФ'],
            dependence:                 ['dependence', 'гр. 2.2', 'Признак взаимозависимости'],
            innKio:                     ['innKio', 'гр. 3', 'ИНН/ КИО'],
            country:                    ['country', 'гр. 4.1', 'Наименование страны регистрации'],
            countryCode1:               ['countryCode1', 'гр. 4.2', 'Код страны регистрации по классификатору ОКСМ'],
            contractNum:                ['contractNum', 'гр. 5', 'Номер договора'],
            contractDate:               ['contractDate', 'гр. 6', 'Дата договора'],
            transactionNum:             ['transactionNum', 'гр. 7.1', 'Номер сделки'],
            dealType:                   ['dealType', 'гр. 7.2', 'Вид срочной сделки'],
            transactionDeliveryDate:    ['transactionDeliveryDate', 'гр. 8', 'Дата заключения сделки'],
            innerCode:                  ['innerCode', 'гр. 9.1', 'Внутренний код'],
            unitCountryCode:            ['unitCountryCode', 'гр. 9.3', 'Код страны происхождения предмета сделки'],
            signPhis:                   ['signPhis', 'гр. 10', 'Признак физической поставки драгоценного металла'],
            signTransaction:            ['signTransaction', 'гр. 11', 'Признак внешнеторговой сделки'],
            countryCode2:               ['countryCode2', 'гр. 12.1', 'Код страны по классификатору ОКСМ'],
            region1:                    ['region1', 'гр. 12.2', 'Регион (код)'],
            city1:                      ['city1', 'гр. 12.3', 'Город'],
            settlement1:                ['settlement1', 'гр. 12.4', 'Населенный пункт'],
            countryCode3:               ['countryCode3', 'гр. 13.1', 'Код страны по классификатору ОКСМ'],
            region2:                    ['region2', 'гр. 13.2', 'Регион (код)'],
            city2:                      ['city2', 'гр. 13.3', 'Город'],
            settlement2:                ['settlement2', 'гр. 13.4', 'Населенный пункт'],
            conditionCode:              ['conditionCode', 'гр. 14', 'Код условия поставки'],
            count:                      ['count', 'гр. 15', 'Количество'],
            incomeSum:                  ['incomeSum', 'гр. 16', 'Сумма доходов Банка по данным бухгалтерского учета, руб.'],
            consumptionSum:             ['consumptionSum', 'гр. 17', 'Сумма расходов Банка по данным бухгалтерского учета, руб.'],
            priceOne:                   ['priceOne', 'гр. 18', 'Цена (тариф) за единицу измерения без учета НДС, руб.'],
            totalNds:                   ['totalNds', 'гр. 19', 'Итого стоимость без учета НДС, руб.'],
            transactionDate:            ['transactionDate', 'гр. 20', 'Дата совершения сделки']
    ]
}

// Возвращает графу вида "гр. хх"
def getGrafNum(def alias) {
    def atr = getAtributes().find { it -> it.getValue()[0] == alias }
    atr.getValue()[1]
}

// 2, 3, 5, 6, 14, 15
def getGroupColumns() {
    ['name', 'dependence', 'country', 'countryCode1',  'signPhis', 'signTransaction']
}

def getEditColumns() {
    ['name', 'dependence', 'dealType', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate', 'innerCode',
            'unitCountryCode', 'signPhis', 'countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2',
            'city2', 'settlement2', 'conditionCode', 'incomeSum', 'consumptionSum', 'transactionDate']
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentReportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)

    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = 0
    row.keySet().each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    getEditColumns().each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
        def pointRow = currentDataRow
        while (pointRow.getAlias() != null && index > 0) {
            pointRow = dataRows.get(--index)
        }
        if (index != currentDataRow.getIndex() && dataRows.get(index).getAlias() == null) {
            index++
        }
    } else if (size > 0) {
        for (int i = size - 1; i >= 0; i--) {
            def pointRow = dataRows.get(i)
            if (pointRow.getAlias() == null) {
                index = dataRows.indexOf(pointRow) + 1
                break
            }
        }
    }
    dataRowHelper.insert(row, index + 1)
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
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    def dFrom = startDate.time
    startDate.add(Calendar.YEAR, 1)
    startDate.add(Calendar.DAY_OF_YEAR, -1)
    def dTo = startDate.time

    def dataRows = dataRowHelper.getAllCached()

    def rowNum = 0
    for (row in dataRows) {
        rowNum++
        if (row.getAlias() != null) {
            continue
        }

        [
                'rowNum', // № п/п
                'name', // Полное наименование с указанием ОПФ
                'dependence', // Признак взаимозависимости
                'dealType', // Вид срочной сделки
                'innKio', // ИНН/КИО
                'country', // Наименование страны регистрации
                'countryCode1', // Код страны по классификатору ОКСМ
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'transactionNum', // Номер сделки
                'transactionDeliveryDate', // Дата заключения сделки
                'innerCode', // Внутренний код
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
                logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
            }
        }

        def transactionDeliveryDate = row.transactionDeliveryDate
        def contractDate = row.contractDate
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
            def isHaveNotEmptyField = false
            def checkField = ['countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2', 'city2', 'settlement2', 'conditionCode']
            for (it in checkField) {
                isHaveNotEmptyField = row.getCell(it).value != null && !row.getCell(it).value.toString().isEmpty()
                if (isHaveNotEmptyField)
                    break
            }
            if (isHaveNotEmptyField) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < checkField.size(); i++) {
                    builder.append("«").append(getAtributes().get(checkField.get(i))[2])
                            .append("» ").append("(")
                            .append(getAtributes().get(checkField.get(i))[1])
                            .append(")")
                    if (i != (checkField.size() - 1)) {
                        builder.append(", ")
                    }
                }
                def msg1 = getColumnName(row, 'signPhis')
                logger.warn("Строка $rowNum: В графе «$msg1» указан «ОМС», графы ${builder.toString()} заполняться не должны!")
            }
        }

        // Корректность даты договора
        def dt = contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = getColumnName(row, 'contractDate')
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }

        // Корректность даты заключения сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = getColumnName(row, 'transactionDeliveryDate')
            def msg2 = getColumnName(row, 'contractDate')
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Корректность заполнения признака внешнеторговой сделки
        def msg14 = getColumnName(row, 'signTransaction')
        def sign = refBookService.getNumberValue(38, row.signTransaction, 'CODE')
        if (row.countryCode2 == row.countryCode3 && sign != 0 ||
                row.countryCode2 != row.countryCode3 && sign != 1) {
            logger.warn("Строка $rowNum: «$msg14» не соответствует сведениям о стране отправке и о стране доставки драгоценных металлов!")
        }

        // Проверка населенного пункта 1
        if (settlement1 != null && !settlement1.toString().isEmpty() && city1 != null && !city1.toString().isEmpty()) {
            def msg1 = getColumnName(row, 'settlement1')
            def msg2 = getGrafNum('settlement1')
            def msg3 = getColumnName(row, 'city1')
            def msg4 = getGrafNum('city1')
            logger.warn("Строка $rowNum: Если указан «$msg1»($msg2), не должен быть указан «$msg3»($msg4)")
        }

        // Проверка населенного пункта 2
        if (settlement2 != null && !settlement2.toString().isEmpty() && city2 != null && !city2.toString().isEmpty()) {
            def msg1 = getColumnName(row, 'settlement2')
            def msg2 = getGrafNum('settlement2')
            def msg3 = getColumnName(row, 'city2')
            def msg4 = getGrafNum('city2')
            logger.warn("Строка $rowNum: Если указан «$msg1»($msg2), не должен быть указан «$msg3»($msg4)")
        }

        // Проверка доходов и расходов
        if (incomeSum == null && consumptionSum == null) {
            def msg1 = getColumnName(row, 'incomeSum')
            def msg2 = getColumnName(row, 'consumptionSum')
            logger.warn("Строка $rowNum: Одна из граф «$msg1» и «$msg2» должна быть заполнена!")
        }

        // Проверка доходов/расходов и стоимости
        if (incomeSum != null && consumptionSum == null && priceOne != incomeSum) {
            def msg1 = getColumnName(row, 'priceOne')
            def msg2 = getColumnName(row, 'incomeSum')
            logger.warn("Строка $rowNum: Графа «$msg1» должна быть равна «$msg2»!")
        }
        if (incomeSum == null && consumptionSum != null && priceOne != consumptionSum) {
            def msg1 = getColumnName(row, 'priceOne')
            def msg2 = getColumnName(row, 'consumptionSum')
            logger.warn("Строка $rowNum: Графа «$msg1» должна быть равна «$msg2»!")
        }
        if (incomeSum != null && consumptionSum != null &&
                (priceOne == null
                        || consumptionSum == null
                        || incomeSum == null
                        || priceOne.abs() != (consumptionSum - incomeSum).abs())) {
            def msg1 = getColumnName(row, 'priceOne')
            def msg2 = getColumnName(row, 'consumptionSum')
            def msg3 = getColumnName(row, 'incomeSum')
            logger.warn("Строка $rowNum: Графа «$msg1» должна быть равна разнице графы «$msg2» и графы «$msg3» по модулю!")
        }

        // Проверка количества
        if (count != null && count != 1) {
            def msg = getColumnName(row, 'count')
            logger.warn("Строка $rowNum: В графе «$msg» может быть указано только значение «1» в!")
        }

        // Корректность дат сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = getColumnName(row, 'transactionDate')
            def msg2 = getColumnName(row, 'transactionDeliveryDate')
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверка заполнения стоимости сделки
        if (priceOne != totalNds) {
            def msg1 = getColumnName(row, 'priceOne')
            def msg2 = getColumnName(row, 'totalNds')
            logger.warn("Строка $rowNum: «$msg1» не может отличаться от «$msg2» сделки!")
        }

        // Проверка заполнения региона отправки
        if (row.countryCode2 != null) {
            def country = refBookService.getStringValue(10, row.countryCode2, 'CODE')
            if (country != null) {
                def regionName = getColumnName(row, 'region1')
                def countryName = getColumnName(row, 'countryCode2')
                if (country == '643' && row.region1 == null) {
                    logger.warn("Строка $rowNum: «$regionName» должен быть заполнен, т.к. в «$countryName» указан код 643!")
                } else if (country != '643' && row.region1 != null) {
                    logger.warn("Строка $rowNum: «$regionName» не должен быть заполнен, т.к. в «$countryName» указан код, отличный от 643!")
                }
            }
        }

        // Проверка заполнения региона доставки
        if (row.countryCode3 != null) {
            def country = refBookService.getStringValue(10, row.countryCode3, 'CODE')
            if (country != null) {
                def regionName = getColumnName(row, 'region2')
                def countryName = getColumnName(row, 'countryCode3')
                if (country == '643' && row.region2 == null) {
                    logger.warn("Строка $rowNum: «$regionName» должен быть заполнен, т.к. в «$countryName» указан код 643!")
                } else if (country != '643' && row.region2 != null) {
                    logger.warn("Строка $rowNum: «$regionName» не должен быть заполнен, т.к. в «$countryName» указан код, отличный от 643!")
                }
            }
        }

        // Проверки соответствия НСИ
        checkNSI(row, "name", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryCode1", "ОКСМ", 10)
        checkNSI(row, "unitCountryCode", "ОКСМ", 10)
        checkNSI(row, "country", "ОКСМ", 10)
        checkNSI(row, "countryCode3", "ОКСМ", 10)
        checkNSI(row, "region1", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "region2", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "signPhis", "Признаки физической поставки", 18)
        checkNSI(row, "conditionCode", "Коды условий поставки", 63)
        checkNSI(row, "dependence", "Да/Нет", 38)
        checkNSI(row, "dealType", "Виды срочных сделок", 85)
    }

    //Проверки подитоговых сумм
    def testRows = dataRows.findAll { it -> it.getAlias() == null }
    //добавляем итоговые строки для проверки
    for (int i = 0; i < testRows.size(); i++) {
        def testRow = testRows.get(i)
        def nextRow = null

        if (i < testRows.size() - 1) {
            nextRow = testRows.get(i + 1)
        }

        if (testRow.getAlias() == null && nextRow == null || isDiffRow(testRow, nextRow, getGroupColumns())) {
            def itogRow = calcItog(i, testRows)
            testRows.add(++i, itogRow)
        }
    }

    def testItogRows = testRows.findAll { it -> it.getAlias() != null }
    def itogRows = dataRows.findAll { it -> it.getAlias() != null }

    if (testItogRows.size() > itogRows.size()) {            //если удалили итоговые строки

        for (int i = 0; i < dataRows.size(); i++) {
            def row = dataRows[i]
            def nextRow = dataRows[i + 1]
            if (row.getAlias() == null) {
                if (nextRow == null ||
                        nextRow.getAlias() == null && isDiffRow(row, nextRow, getGroupColumns())) {
                    def String groupCols = getValuesByGroupColumn(row)
                    if (groupCols != null) {
                        logger.error("Группа «$groupCols» не имеет строки подитога!")
                    }
                }
            }
        }

    } else if (testItogRows.size() < itogRows.size()) {     //если удалили все обычные строки, значит где то 2 подряд подитог.строки

        for (int i = 0; i < dataRows.size(); i++) {
            if (dataRows[i].getAlias() != null) {
                if (i - 1 < -1 || dataRows[i - 1].getAlias() != null) {
                    logger.error("Строка ${dataRows[i].getIndex()}: Строка подитога не относится к какой-либо группе!")
                }
            }
        }
    } else {
        for (int i = 0; i < testItogRows.size(); i++) {
            def testItogRow = testItogRows[i]
            def realItogRow = itogRows[i]
            int itg = Integer.valueOf(testItogRow.getAlias().replaceAll("itg#", ""))
            if (dataRows[itg].getAlias() != null) {
                logger.error("Строка ${dataRows[i].getIndex()}: Строка подитога не относится к какой-либо группе!")
            } else {
                def String groupCols = getValuesByGroupColumn(dataRows[itg])
                def mes = "Строка ${realItogRow.getIndex()}: Неверное итоговое значение по группе «$groupCols» в графе"
                if (groupCols != null) {
                    if (testItogRow.incomeSum != realItogRow.incomeSum) {
                        logger.error(mes + " «${getAtributes().incomeSum[2]}»")
                    }
                    if (testItogRow.consumptionSum != realItogRow.consumptionSum) {
                        logger.error(mes + " «${getAtributes().consumptionSum[2]}»")
                    }
                    if (testItogRow.totalNds != realItogRow.totalNds) {
                        logger.error(mes + " «${getAtributes().totalNds[2]}»")
                    }
                }
            }
        }
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
        logger.warn("Строка $rowNum: В справочнике «$msg» не найден элемент «$msg2»!")
    }
}

/**
 * Возвращает строку со значениями полей строки по которым идет группировка
 *  ['name', 'dependence', 'country', 'countryCode1',  'signPhis', 'signTransaction']
 */
String getValuesByGroupColumn(DataRow row) {
    def sep = ", "
    StringBuilder builder = new StringBuilder()

    def map = row.name != null ? refBookService.getRecordData(9, row.name) : null
    if (map != null)
        builder.append(map.NAME.stringValue).append(sep)

    dependence = getRefBookValue(38, row.dependence, 'VALUE')
    if (dependence != null)
        builder.append(dependence).append(sep)

    country = getRefBookValue(10, row.country, 'NAME')
    if (country != null)
        builder.append(country).append(sep)

    countryCode1 = getRefBookValue(10, row.countryCode1, 'CODE')
    if (countryCode1 != null)
        builder.append(countryCode1).append(sep)

    signPhis = getRefBookValue(18, row.signPhis, 'SIGN')
    if (signPhis != null)
        builder.append(signPhis).append(sep)

    signTransaction = getRefBookValue(38, row.signTransaction, 'VALUE')
    if (signTransaction != null)
        builder.append(signTransaction).append(sep)

    def String retVal = builder.toString()
    if (retVal.length() < 2)
        return null
    return retVal.substring(0, retVal.length() - 2)
}

def getRefBookValue(int id, def cell, def alias) {
    def map = cell != null ? refBookService.getRecordData(id, cell) : null
    return map == null ? null : map.get(alias).stringValue
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

        // Количество
        row.count = 1

        // Графы 27 и 28 из 25 и 26
        incomeSum = row.incomeSum
        consumptionSum = row.consumptionSum
        if (incomeSum != null && consumptionSum == null) {
            row.priceOne = incomeSum
        } else if (incomeSum == null && consumptionSum != null) {
            row.priceOne = consumptionSum
        } else if (incomeSum != null && consumptionSum != null) {
            row.priceOne = Math.abs(incomeSum - consumptionSum)
        } else {
            row.priceOne = null
        }

        row.totalNds = row.priceOne

        // Расчет полей зависимых от справочников
        if (row.name != null) {
            def map = refBookService.getRecordData(9, row.name)
            row.innKio = map.INN_KIO.stringValue
            row.country = map.COUNTRY.referenceValue
            row.countryCode1 = map.COUNTRY.referenceValue
        } else {
            row.innKio = null
            row.country = null
            row.countryCode1 = null
        }

        // Признак физической поставки
        def boolean deliveryPhis = refBookService.getNumberValue(18, row.signPhis, 'CODE') == 1

        if (deliveryPhis) {
            row.countryCode2 = null
            row.region1 = null
            row.city1 = null
            row.settlement1 = null
            row.countryCode3 = null
            row.region2 = null
            row.city2 = null
            row.settlement2 = null
        }

        // Сбарсываем "Регион РФ" если страна не Россия
        if (row.countryCode2 != null) {
            def country = refBookService.getStringValue(10, row.countryCode2, 'CODE')
            if (country != null && country != '643') {
                    row.region1 = null
            }
        }
        if (row.countryCode3 != null) {
            def country = refBookService.getStringValue(10, row.countryCode3, 'CODE')
            if (country != null && country != '643') {
                row.region2 = null
            }
        }

        if (row.countryCode2 == row.countryCode3) {
            def valNo = refBookFactory.getDataProvider(38L).getRecords(new Date(), null, "CODE = 0", null)
            if (valNo != null && valNo.size() == 1) {
                row.signTransaction = valNo.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
            }
        } else {
            def valYes = refBookFactory.getDataProvider(38L).getRecords(new Date(), null, "CODE = 1", null)
            if (valYes != null && valYes.size() == 1) {
                row.signTransaction = valYes.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
            }
        }
    }
    dataRowHelper.update(dataRows);
}

/**
 * проверяет разные ли строки по значениям полей группировки
 * @param a первая  строка
 * @param b вторая строка
 * @return true - разные, false = одинаковые
 */
boolean isDiffRow(DataRow row, DataRow nextRow, def groupColumns) {
    def rez = false
    groupColumns.each { def n ->
        rez = rez || (row.get(n) != nextRow.get(n))
    }
    return rez
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
        sortRow(getGroupColumns(), a, b)
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
                if (nextRow == null || isDiffRow(row, nextRow, getGroupColumns())) {
                    def itogRow = calcItog(i, dataRows)
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
def calcItog(int i, def dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('itog').colSpan = 25
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2

    // Расчеты подитоговых значений
    BigDecimal incomeSumItg = 0, consumptionSumItg = 0, totalNdsItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)

        incomeSum = row.incomeSum
        consumptionSum = row.consumptionSum
        totalNds = row.totalNds

        incomeSumItg += incomeSum != null ? incomeSum : 0
        consumptionSumItg += consumptionSum != null ? consumptionSum : 0
        totalNdsItg += totalNds != null ? totalNds : 0
    }

    newRow.incomeSum = incomeSumItg
    newRow.consumptionSum = consumptionSumItg
    newRow.totalNds = totalNdsItg

    newRow
}

/**
 * Консолидация
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = new LinkedList<DataRow<Cell>>()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentReportPeriodId, it.periodOrder, it.comparativePeriodId, it.accruing)
        if (source != null
                && source.state == WorkflowState.ACCEPTED
                && source.getFormType().getId() == formData.getFormType().getId()) {
            formDataService.getDataRowHelper(source).getAll().each { row ->
                if (row.getAlias() == null) {
                    rows.add(row)
                }
            }
        }
    }
    dataRowHelper.save(rows)
}

def getHeaderRowCount() {
    return 2
}

/**
 * Получение импортируемых данных.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    if (!fileName.endsWith('.xls')) {
        logger.error('Выбранный файл не соответствует формату xls!')
        return
    }

    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Полное наименование с указанием ОПФ', 'Подитог:')
    if (xmlString == null) {
        logger.error('Отсутствие значения после обработки потока данных')
        return
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Ошибка в обработке данных!')
        return
    }

    // добавить данные в форму
    try {
        if (!checkTableHead(xml)) {
            logger.error('Заголовок таблицы не соответствует требуемой структуре!')
            return
        }
        addData(xml)
    } catch (Exception e) {
        logger.error("" + e.message)
    }
}

/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml) {
    def colCount = 29
    def rc = getHeaderRowCount()
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < rc || xml.row[0].cell.size() < colCount) {
        return false
    }
    def result = (
    checkHeaderRow(xml, rc, 0, ['Полное наименование с указанием ОПФ', '', 'гр. 2.1']) &&
            checkHeaderRow(xml, rc, 1, ['Признак взаимозависимости', '', 'гр. 2.2']) &&
            checkHeaderRow(xml, rc, 2, ['ИНН/ КИО', '', 'гр. 3']) &&
            checkHeaderRow(xml, rc, 3, ['Наименование страны регистрации', '', 'гр. 4.1']) &&
            checkHeaderRow(xml, rc, 4, ['Код страны регистрации по классификатору ОКСМ', '', 'гр. 4.2']) &&
            checkHeaderRow(xml, rc, 5, ['Номер договора', '', 'гр. 5']) &&
            checkHeaderRow(xml, rc, 6, ['Дата договора', '', 'гр. 6']) &&
            checkHeaderRow(xml, rc, 7, ['Номер сделки', '', 'гр. 7.1']) &&
            checkHeaderRow(xml, rc, 8, ['Вид срочной сделки', '', 'гр. 7.2']) &&
            checkHeaderRow(xml, rc, 9, ['Дата заключения сделки', '', 'гр. 8']) &&
            checkHeaderRow(xml, rc, 10, ['Характеристика базисного актива', 'Внутренний код', 'гр. 9.1']) &&
            checkHeaderRow(xml, rc, 11, ['', 'Код страны происхождения предмета сделки', 'гр. 9.2']) &&
            checkHeaderRow(xml, rc, 12, ['Признак физической поставки драгоценного металла', '', 'гр. 10']) &&
            checkHeaderRow(xml, rc, 13, ['Признак внешнеторговой сделки', '', 'гр. 11']) &&
            checkHeaderRow(xml, rc, 14, ['Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами', 'Код страны по классификатору ОКСМ', 'гр. 12.1']) &&
            checkHeaderRow(xml, rc, 15, ['', 'Регион (код)', 'гр. 12.2']) &&
            checkHeaderRow(xml, rc, 16, ['', 'Город', 'гр. 12.3']) &&
            checkHeaderRow(xml, rc, 17, ['', 'Населенный пункт', 'гр. 12.4']) &&
            checkHeaderRow(xml, rc, 18, ['Место совершения сделки (адрес места доставки (разгрузки) драгоценного металла)', 'Код страны по классификатору ОКСМ', 'гр. 13.1']) &&
            checkHeaderRow(xml, rc, 19, ['', 'Регион (код)', 'гр. 13.2']) &&
            checkHeaderRow(xml, rc, 20, ['', 'Город', 'гр. 13.3']) &&
            checkHeaderRow(xml, rc, 21, ['', 'Населенный пункт', 'гр. 13.4']) &&
            checkHeaderRow(xml, rc, 22, ['Код условия поставки', '', 'гр. 14']) &&
            checkHeaderRow(xml, rc, 23, ['Количество', '', 'гр. 15']) &&
            checkHeaderRow(xml, rc, 24, ['Сумма доходов Банка по данным бухгалтерского учета, руб.', '', 'гр. 16']) &&
            checkHeaderRow(xml, rc, 25, ['Сумма расходов Банка по данным бухгалтерского учета, руб.', '', 'гр. 17']) &&
            checkHeaderRow(xml, rc, 26, ['Цена (тариф) за единицу измерения без учета НДС, руб.', '', 'гр. 18']) &&
            checkHeaderRow(xml, rc, 27, ['Итого стоимость без учета НДС, руб.', '', 'гр. 19']) &&
            checkHeaderRow(xml, rc, 28, ['Дата совершения сделки', '', 'гр. 20'])
    )
    return result
}

def checkHeaderRow(def xml, def headRowCount, def cellNumber, def arrayHeaders) {
    for (int i = 0; i < headRowCount; i++) {
        String value = xml.row[i].cell[cellNumber]
        //убираем перевод строки, множественное использование пробелов
        value = value.replaceAll('\\n', '').replaceAll('\\r', '').replaceAll(' {2,}', ' ').trim()
        if (value != arrayHeaders[i]) {
            return false
        }
    }
    return true
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    Date date = reportPeriodService.get(formData.reportPeriodId).taxPeriod.getEndDate()

    def headShift = getHeaderRowCount()
    def cache = [:]
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = new LinkedList()

    def indexRow = -1     // пропустить шапку таблицы
    for (def row : xml.row) {
        indexRow++
        // пропустить шапку таблицы
        if (indexRow <= headShift) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()

        getEditColumns().each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // столбец 1
        newRow.rowNum = newRow.index = indexRow - headShift

        // столбец 2.1
        newRow.name = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        def map = newRow.name == null ? null : refBookService.getRecordData(9, newRow.name)
        indexCell++

        // графа 2.2 Признак взаимозависимости
        newRow.dependence = getRecordId(38, 'VALUE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 3
        if (map != null) {
            def text = row.cell[indexCell].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(9).getName()+"»!")
            }
        }
        indexCell++

        // графа 4.1
        if (map != null) {
            def text = row.cell[indexCell].text()
            map = refBookService.getRecordData(10, map.COUNTRY.referenceValue)
            if ((text != null && !text.isEmpty() && !text.equals(map.NAME.stringValue)) || ((text == null || text.isEmpty()) && map.NAME.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        indexCell++

        // графа 4.2
        if (map != null) {
            def text = row.cell[indexCell].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE.stringValue)) || ((text == null || text.isEmpty()) && map.CODE.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        indexCell++

        // графа 5
        newRow.contractNum = row.cell[indexCell].text()
        indexCell++

        // графа 6
        newRow.contractDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 7.1
        newRow.transactionNum = row.cell[indexCell].text()
        indexCell++

        // графа 7.2 Вид срочной сделки
        newRow.dealType = getRecordId(85, 'CONTRACT_TYPE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 8
        newRow.transactionDeliveryDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 9.1
        newRow.innerCode = getRecordId(17, 'INNER_CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        //  графа 9.2
        newRow.unitCountryCode = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 10
        newRow.signPhis = getRecordId(18, 'SIGN', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 11
        newRow.signTransaction = getRecordId(38, 'VALUE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 12.1
        newRow.countryCode2 = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 12.2
        String code = row.cell[indexCell].text()
        if (code.length() == 1) {    //для кодов 1, 2, 3...9
            code = "0".concat(code)
        }
        newRow.region1 = getRecordId(4, 'CODE', code, date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 12.3
        newRow.city1 = row.cell[indexCell].text()
        indexCell++

        // графа 12.4
        newRow.settlement1 = row.cell[indexCell].text()
        indexCell++

        // графа 13.1
        newRow.countryCode3 = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 13.2
        code = row.cell[indexCell].text()
        if (code.length() == 1) {    //для кодов 1, 2, 3...9
            code = "0".concat(code)
        }
        newRow.region2 = getRecordId(4, 'CODE', code, date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 13.3
        newRow.city2 = row.cell[indexCell].text()
        indexCell++

        // графа 13.4
        newRow.settlement2 = row.cell[indexCell].text()
        indexCell++

        // графа 14
        newRow.conditionCode = getRecordId(63, 'STRCODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 15
        newRow.count = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 16
        newRow.incomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 17
        newRow.consumptionSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 18
        newRow.priceOne = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 19
        newRow.totalNds = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 20
        newRow.transactionDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value, int indexRow, int indexCell) {
    if (value == null) {
        return null
    }
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    // поменять запятую на точку и убрать пробелы
    tmp = tmp.replaceAll(',', '.').replaceAll('[^\\d.,-]+', '')
    try {
        return new BigDecimal(tmp)
    } catch (Exception e) {
        throw new Exception("Проверка файла: Строка ${indexRow - (getHeaderRowCount() - 1)} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String alias, String value, Date date, def cache, int indexRow, int indexCell, boolean mandatory = true) {
    String filter = "LOWER($alias) = LOWER('$value')"
    if (value == '') filter = "$alias is null"
    if (cache[ref_id] != null) {
        if (cache[ref_id][filter] != null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null)
    if (records.size() == 1) {
        cache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        return cache[ref_id][filter]
    }
    def msg = "Проверка файла: Строка ${indexRow - (getHeaderRowCount() - 1)} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(ref_id).getName()+"»!"
    if (mandatory) {
        throw new Exception(msg)
    } else {
        logger.warn(msg)
    }
    return null
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value, int indexRow, int indexCell) {
    if (value == null || value == '') {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    try {
        return format.parse(value)
    } catch (Exception e) {
        throw new Exception("Проверка файла: Строка ${indexRow - (getHeaderRowCount() - 1)} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}