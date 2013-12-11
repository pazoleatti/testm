package form_template.deal.precious_metals_deliver

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 393 - Поставочные срочные сделки с драгоценными металлами (18)
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        calc()
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'dependence', 'dealType', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate', 'innerCode',
        'unitCountryCode', 'signPhis', 'countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2',
        'city2', 'settlement2', 'conditionCode', 'incomeSum', 'consumptionSum', 'transactionDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'innKio', 'country', 'countryCode1', 'dealType', 'signTransaction', 'count', 'priceOne',
        'totalNds']

// Группируемые атрибуты
@Field
def groupColumns = ['name', 'dependence', 'country', 'countryCode1', 'signPhis', 'signTransaction']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNum', 'name', 'dependence', 'dealType', 'innKio', 'country', 'countryCode1', 'contractNum',
        'contractDate', 'transactionNum', 'transactionDeliveryDate', 'innerCode', 'unitCountryCode', 'signPhis',
        'signTransaction', 'count', 'priceOne', 'totalNds', 'transactionDate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            currentDate, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение xml с общими проверками
def getXML(def String startStr, def String endStr) {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        throw new ServiceException('Имя файла не должно быть пустым')
    }
    def is = ImportInputStream
    if (is == null) {
        throw new ServiceException('Поток данных пуст')
    }
    if (!fileName.endsWith('.xls')) {
        throw new ServiceException('Выбранный файл не соответствует формату xls!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }
    return xml
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // "Да" и "Нет"
    def recYesId = getRecordId(38, 'CODE', '1', -1, null, true)
    def recNoId = getRecordId(38, 'CODE', '0', -1, null, true)

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, false)

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
        if (getRefBookValue(18, signPhis)?.CODE?.numberValue == 1) {
            def isHaveNotEmptyField = false
            def checkField = ['countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2', 'city2', 'settlement2', 'conditionCode']
            for (it in checkField) {
                isHaveNotEmptyField = row.getCell(it).value != null && !row.getCell(it).value.toString().isEmpty()
                if (isHaveNotEmptyField)
                    break
            }
            if (isHaveNotEmptyField) {
                StringBuilder builder = new StringBuilder()
                for (int i = 0; i < checkField.size(); i++) {
                    builder.append("«").append(getAtributes().get(checkField.get(i))[2])
                            .append("» ").append("(")
                            .append(getAtributes().get(checkField.get(i))[1])
                            .append(")")
                    if (i != (checkField.size() - 1)) {
                        builder.append(", ")
                    }
                }
                def msg1 = row.getCell('signPhis').column.name
                logger.warn("Строка $rowNum: В графе «$msg1» указан «ОМС», графы ${builder.toString()} заполняться не должны!")
            }
        }

        // Корректность даты договора
        def dt = contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = row.getCell('contractDate').column.name
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }

        // Корректность даты заключения сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = row.getCell('transactionDeliveryDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Корректность заполнения признака внешнеторговой сделки
        def msg14 = row.getCell('signTransaction').column.name
        if (row.countryCode2 == row.countryCode3 && row.signTransaction != recNoId ||
                row.countryCode2 != row.countryCode3 && row.signTransaction != recYesId) {
            logger.warn("Строка $rowNum: «$msg14» не соответствует сведениям о стране отправке и о стране доставки драгоценных металлов!")
        }

        // Проверка населенного пункта 1
        if (settlement1 != null && !settlement1.toString().isEmpty() && city1 != null && !city1.toString().isEmpty()) {
            def msg1 = row.getCell('settlement1').column.name
            def msg2 = getGrafNum('settlement1')
            def msg3 = row.getCell('city1').column.name
            def msg4 = getGrafNum('city1')
            logger.warn("Строка $rowNum: Если указан «$msg1»($msg2), не должен быть указан «$msg3»($msg4)")
        }

        // Проверка населенного пункта 2
        if (settlement2 != null && !settlement2.toString().isEmpty() && city2 != null && !city2.toString().isEmpty()) {
            def msg1 = row.getCell('settlement2').column.name
            def msg2 = getGrafNum('settlement2')
            def msg3 = row.getCell('city2').column.name
            def msg4 = getGrafNum('city2')
            logger.warn("Строка $rowNum: Если указан «$msg1»($msg2), не должен быть указан «$msg3»($msg4)")
        }

        // Проверка доходов и расходов
        if (incomeSum == null && consumptionSum == null) {
            def msg1 = row.getCell('incomeSum').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            logger.warn("Строка $rowNum: Одна из граф «$msg1» и «$msg2» должна быть заполнена!")
        }

        // Проверка доходов/расходов и стоимости
        if (incomeSum != null && consumptionSum == null && priceOne != incomeSum) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('incomeSum').column.name
            logger.warn("Строка $rowNum: Графа «$msg1» должна быть равна «$msg2»!")
        }
        if (incomeSum == null && consumptionSum != null && priceOne != consumptionSum) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            logger.warn("Строка $rowNum: Графа «$msg1» должна быть равна «$msg2»!")
        }
        if (incomeSum != null && consumptionSum != null &&
                (priceOne == null
                        || consumptionSum == null
                        || incomeSum == null
                        || priceOne.abs() != (consumptionSum - incomeSum).abs())) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            def msg3 = row.getCell('incomeSum').column.name
            logger.warn("Строка $rowNum: Графа «$msg1» должна быть равна разнице графы «$msg2» и графы «$msg3» по модулю!")
        }

        // Проверка количества
        if (count != null && count != 1) {
            def msg = row.getCell('count').column.name
            logger.warn("Строка $rowNum: В графе «$msg» может быть указано только значение «1» в!")
        }

        // Корректность дат сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('transactionDeliveryDate').column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверка заполнения стоимости сделки
        if (priceOne != totalNds) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('totalNds').column.name
            logger.warn("Строка $rowNum: «$msg1» не может отличаться от «$msg2» сделки!")
        }

        // Проверка заполнения региона отправки
        def country = getRefBookValue(10, row.countryCode2)?.CODE?.stringValue
        if (country != null) {
            def regionName = row.getCell('region1').column.name
            def countryName = row.getCell('countryCode2').column.name
            if (country == '643' && row.region1 == null) {
                logger.warn("Строка $rowNum: «$regionName» должен быть заполнен, т.к. в «$countryName» указан код 643!")
            } else if (country != '643' && row.region1 != null) {
                logger.warn("Строка $rowNum: «$regionName» не должен быть заполнен, т.к. в «$countryName» указан код, отличный от 643!")
            }
        }

        // Проверка заполнения региона доставки
        country = getRefBookValue(10, row.countryCode3)?.CODE?.stringValue
        if (country != null) {
            def regionName = row.getCell('region2').column.name
            def countryName = row.getCell('countryCode3').column.name
            if (country == '643' && row.region2 == null) {
                logger.warn("Строка $rowNum: «$regionName» должен быть заполнен, т.к. в «$countryName» указан код 643!")
            } else if (country != '643' && row.region2 != null) {
                logger.warn("Строка $rowNum: «$regionName» не должен быть заполнен, т.к. в «$countryName» указан код, отличный от 643!")
            }
        }

        // Проверки соответствия НСИ
        checkNSI(9, row, "name")
        checkNSI(10, row, "countryCode1")
        checkNSI(10, row, "unitCountryCode")
        checkNSI(10, row, "country")
        checkNSI(10, row, "countryCode3")
        checkNSI(4, row, "region1")
        checkNSI(4, row, "region2")
        checkNSI(18, row, "signPhis")
        checkNSI(63, row, "conditionCode")
        checkNSI(38, row, "dependence")
        checkNSI(85, row, "dealType")
    }

    checkItog(dataRows)
}

// Проверки подитоговых сумм
void checkItog(def dataRows) {
    def testRows = dataRows.findAll { it -> it.getAlias() == null }
    addAllAliased(testRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, testRows)
        }
    }, groupColumns)
    // Рассчитанные строки итогов
    def testItogRows = testRows.findAll { it -> it.getAlias() != null }
    // Имеющиеся строки итогов
    def itogRows = dataRows.findAll { it -> it.getAlias() != null }

    checkItogRows(dataRows, testItogRows, itogRows, groupColumns, logger, new GroupString() {
        @Override
        String getString(DataRow<Cell> row) {
            return getValuesByGroupColumn(row)
        }
    }, new CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            if (row1.incomeSum != row2.incomeSum) {
                return getColumnName(row1, 'incomeSum')
            }
            if (row1.consumptionSum != row2.consumptionSum) {
                return getColumnName(row1, 'total')
            }
            if (row1.totalNds != row2.totalNds) {
                return getColumnName(row1, 'total')
            }
            return null
        }
    })
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    // "Да" / "Нет"
    def recYesId = getRecordId(38, 'CODE', '1', -1, null, true)
    def recNoId = getRecordId(38, 'CODE', '0', -1, null, true)

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Сортировка
    sortRows(dataRows, groupColumns)

    def index = 1
    for (row in dataRows) {
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
        def map = getRefBookValue(9, row.name)
        row.innKio = map?.INN_KIO?.stringValue
        row.country = map?.COUNTRY?.referenceValue
        row.countryCode1 = map?.COUNTRY?.referenceValue

        // Признак физической поставки
        def Boolean deliveryPhis = null
        if (row.deliverySign != null) {
            deliveryPhis = getRefBookValue(18, row.signPhis)?.CODE?.numberValue == 1
        }
        if (deliveryPhis != null && deliveryPhis) {
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
        def country = getRefBookValue(10, row.countryCode2)?.CODE?.stringValue
        if (country != null && country != '643') {
            row.region1 = null
        }
        country = getRefBookValue(10, row.countryCode3)?.CODE?.stringValue
        if (country != null && country != '643') {
            row.region2 = null
        }

        row.signTransaction = (row.countryCode2 == row.countryCode3) ? recNoId : recYesId
    }

    // Добавление подитов
    addAllAliased(dataRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, dataRows)
        }
    }, groupColumns)

    // Если нет сортировки и подитогов, то dataRowHelper.update(dataRows)
    dataRowHelper.save(dataRows)
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('itog').colSpan = 25
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2

    // Расчеты подитоговых значений
    BigDecimal incomeSumItg = 0, consumptionSumItg = 0, totalNdsItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        def row = dataRows.get(j)
        def incomeSum = row.incomeSum
        def consumptionSum = row.consumptionSum
        def totalNds = row.totalNds

        incomeSumItg += incomeSum != null ? incomeSum : 0
        consumptionSumItg += consumptionSum != null ? consumptionSum : 0
        totalNdsItg += totalNds != null ? totalNds : 0
    }
    newRow.incomeSum = incomeSumItg
    newRow.consumptionSum = consumptionSumItg
    newRow.totalNds = totalNdsItg

    return newRow
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def sep = ", "
    StringBuilder builder = new StringBuilder()

    def map = getRefBookValue(9, row.name)
    if (map != null)
        builder.append(map.NAME.stringValue).append(sep)

    dependence = getRefBookValue(38, row.dependence)?.VALUE?.stringValue
    if (dependence != null)
        builder.append(dependence).append(sep)

    country = getRefBookValue(38, row.country)?.NAME?.stringValue
    if (country != null)
        builder.append(country).append(sep)

    countryCode1 = getRefBookValue(10, row.countryCode1)?.CODE?.stringValue
    if (countryCode1 != null)
        builder.append(countryCode1).append(sep)

    signPhis = getRefBookValue(18, row.signPhis)?.SIGN?.stringValue
    if (signPhis != null)
        builder.append(signPhis).append(sep)

    signTransaction = getRefBookValue(38, row.signTransaction)?.VALUE?.stringValue
    if (signTransaction != null)
        builder.append(signTransaction).append(sep)

    def String retVal = builder.toString()
    if (retVal.length() < 2)
        return null
    return retVal.substring(0, retVal.length() - 2)
}

// Получение импортируемых данных.
void importData() {
    def xml = getXML('Полное наименование с указанием ОПФ', 'Подитог:')

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 29, 2)

    def headerMapping = [
            (xml.row[0].cell[1]): 'Признак взаимозависимости',
            (xml.row[0].cell[2]): 'ИНН/ КИО',
            (xml.row[0].cell[3]): 'Наименование страны регистрации',
            (xml.row[0].cell[4]): 'Код страны регистрации по классификатору ОКСМ',
            (xml.row[0].cell[5]): 'Номер договора',
            (xml.row[0].cell[6]): 'Дата договора',
            (xml.row[0].cell[7]): 'Номер сделки',
            (xml.row[0].cell[8]): 'Вид срочной сделки',
            (xml.row[0].cell[9]): 'Дата заключения сделки',
            (xml.row[0].cell[10]): 'Характеристика базисного актива',
            (xml.row[0].cell[12]): 'Признак физической поставки драгоценного металла',
            (xml.row[0].cell[13]): 'Признак внешнеторговой сделки',
            (xml.row[0].cell[14]): 'Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами',
            (xml.row[0].cell[18]): 'Место совершения сделки (адрес места доставки (разгрузки) драгоценного металла)',
            (xml.row[0].cell[22]): 'Код условия поставки',
            (xml.row[0].cell[23]): 'Количество',
            (xml.row[0].cell[24]): 'Сумма доходов Банка по данным бухгалтерского учета, руб.',
            (xml.row[0].cell[25]): 'Сумма расходов Банка по данным бухгалтерского учета, руб.',
            (xml.row[0].cell[26]): 'Цена (тариф) за единицу измерения без учета НДС, руб.',
            (xml.row[0].cell[27]): 'Итого стоимость без учета НДС, руб.',
            (xml.row[0].cell[28]): 'Дата совершения сделки',
            (xml.row[2].cell[0]): 'гр. 2.1',
            (xml.row[2].cell[1]): 'гр. 2.2',
            (xml.row[2].cell[2]): 'гр. 3',
            (xml.row[2].cell[3]): 'гр. 4.1',
            (xml.row[2].cell[4]): 'гр. 4.2',
            (xml.row[2].cell[5]): 'гр. 5',
            (xml.row[2].cell[6]): 'гр. 6',
            (xml.row[2].cell[7]): 'гр. 7.1',
            (xml.row[2].cell[8]): 'гр. 7.2',
            (xml.row[2].cell[9]): 'гр. 8',
            (xml.row[2].cell[10]): 'гр. 9.1',
            (xml.row[2].cell[11]): 'гр. 9.2',
            (xml.row[2].cell[12]): 'гр. 10',
            (xml.row[2].cell[13]): 'гр. 11',
            (xml.row[2].cell[14]): 'гр. 12.1',
            (xml.row[2].cell[15]): 'гр. 12.2',
            (xml.row[2].cell[16]): 'гр. 12.3',
            (xml.row[2].cell[17]): 'гр. 12.4',
            (xml.row[2].cell[18]): 'гр. 13.1',
            (xml.row[2].cell[19]): 'гр. 13.2',
            (xml.row[2].cell[20]): 'гр. 13.3',
            (xml.row[2].cell[21]): 'гр. 13.4',
            (xml.row[2].cell[22]): 'гр. 14',
            (xml.row[2].cell[23]): 'гр. 15',
            (xml.row[2].cell[24]): 'гр. 16',
            (xml.row[2].cell[25]): 'гр. 17',
            (xml.row[2].cell[26]): 'гр. 18',
            (xml.row[2].cell[27]): 'гр. 19',
            (xml.row[2].cell[28]): 'гр. 20'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def int xmlIndexRow = -1
    def int rowOffset = 3
    def int colOffset = 2

    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // пропустить шапку таблицы
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def xmlIndexCol = 0

        // столбец 1
        newRow.rowNum = xmlIndexRow - headRowCount

        // столбец 2.1
        newRow.name = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        def map = getRefBookValue(9, newRow.name)
        xmlIndexCol++

        // графа 2.2 Признак взаимозависимости
        newRow.dependence = getRecordIdImport(38, 'VALUE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO?.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO?.stringValue != null)) {
                logger.warn("Строка ${xlsIndexRow} столбец ${xmlIndexCol + colOffset} содержит значение, отсутствующее в справочнике!")
            }
        }
        xmlIndexCol++

        // графа 4.1
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)
            if ((text != null && !text.isEmpty() && !text.equals(map?.NAME?.stringValue)) || ((text == null || text.isEmpty()) && map?.NAME?.stringValue != null)) {
                logger.warn("Строка ${xlsIndexRow} столбец ${xmlIndexCol + colOffset} содержит значение, отсутствующее в справочнике!")
            }
        }
        xmlIndexCol++

        // графа 4.2
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if ((text != null && !text.isEmpty() && !text.equals(map?.CODE?.stringValue)) || ((text == null || text.isEmpty()) && map?.CODE?.stringValue != null)) {
                logger.warn("Строка ${xlsIndexRow} столбец ${xmlIndexCol + colOffset} содержит значение, отсутствующее в справочнике!")
            }
        }
        xmlIndexCol++

        // графа 5
        newRow.contractNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 6
        newRow.contractDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 7.1
        newRow.transactionNum = row.cell[indexCell].text()
        indexCell++

        // графа 7.2 Вид срочной сделки
        newRow.dealType = getRecordIdImport(85, 'CONTRACT_TYPE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 8
        newRow.transactionDeliveryDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 9.1
        newRow.innerCode = getRecordIdImport(17, 'INNER_CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 9.2
        newRow.unitCountryCode = getRecordIdImport(10, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 10
        newRow.signPhis = getRecordIdImport(18, 'SIGN', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 11
        newRow.signTransaction = getRecordIdImport(38, 'VALUE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 12.1
        newRow.countryCode2 = getRecordIdImport(10, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 12.2
        String code = row.cell[xmlIndexCol].text()
        if (code.length() == 1) {    //для кодов 1, 2, 3...9
            code = "0".concat(code)
        }
        newRow.region1 = getRecordIdImport(4, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 12.3
        newRow.city1 = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 12.4
        newRow.settlement1 = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 13.1
        newRow.countryCode3 = getRecordIdImport(10, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 13.2
        code = row.cell[xmlIndexCol].text()
        if (code.length() == 1) {    //для кодов 1, 2, 3...9
            code = "0".concat(code)
        }
        newRow.region2 = getRecordIdImport(4, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 13.3
        newRow.city2 = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 13.4
        newRow.settlement2 = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 14
        newRow.conditionCode = getRecordIdImport(63, 'STRCODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 15
        newRow.count = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 16
        newRow.incomeSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 17
        newRow.consumptionSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 18
        newRow.priceOne = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 19
        newRow.totalNds = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 20
        newRow.transactionDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}