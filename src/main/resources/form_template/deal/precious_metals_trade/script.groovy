package form_template.deal.precious_metals_trade

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 394 - Купля-продажа драгоценных металлов (19)
 *
 * @author Stanislav Yasinskiy
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
def editableColumns = ['fullName', 'interdependence', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealFocus',
        'deliverySign', 'metalName', 'countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2',
        'region2', 'city2', 'locality2', 'deliveryCode', 'incomeSum', 'outcomeSum', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'inn', 'countryName', 'countryCode', 'foreignDeal', 'count', 'price', 'total']

// Группируемые атрибуты
@Field
def groupColumns = ['fullName', 'docNumber', 'docDate', 'dealFocus', 'deliverySign', 'metalName', 'foreignDeal',
        'deliveryCode']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNum', 'fullName', 'interdependence', 'inn', 'countryName', 'countryCode', 'docNumber',
        'docDate', 'dealNumber', 'dealDate', 'dealFocus', 'deliverySign', 'metalName', 'foreignDeal', 'count',
        'price', 'total', 'dealDoneDate']

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

    def rowNum = 0
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        rowNum++

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, false)

        def docDateCell = row.getCell('docDate')
        def dealDateCell = row.getCell('dealDate')

        // Корректность даты договора
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }

        // Корректность даты заключения сделки
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Зависимости от признака физической поставки
        if (getRefBookValue(18, row.deliverySign)?.CODE?.numberValue == 1) {
            def isHaveNotEmptyField = false
            def msg1 = row.getCell('deliverySign').column.name
            def checkField = ['countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2', 'locality2', 'deliveryCode']
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
                logger.warn("Строка $rowNum: В графе «$msg1» указан «ОМС», графы ${builder.toString()} заполняться не должны!")
            }
        }

        // Проверка заполнения населенного пункта
        localityCell = row.getCell('locality')
        cityCell = row.getCell('city')
        if (localityCell.value != null && !localityCell.value.toString().isEmpty() && cityCell.value != null && !cityCell.value.toString().isEmpty()) {
            logger.warn("Строка $rowNum: Если указан «${localityCell.column.name}», не должен быть указан «${cityCell.column.name}»!")
        }
        localityCell = row.getCell('locality2')
        cityCell = row.getCell('city2')
        if (localityCell.value != null && !localityCell.value.toString().isEmpty() && cityCell.value != null && !cityCell.value.toString().isEmpty()) {
            logger.warn("Строка $rowNum: Если указан «${localityCell.column.name}», не должен быть указан «${cityCell.column.name}»!")
        }

        // Проверка доходов и расходов
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            logger.warn("Строка $rowNum: Одна из граф «$msgIn» и «$msgOut» должна быть заполнена!")
        }

        // Проверка доходов/расходов и стоимости
        def msgPrice = row.getCell('price').column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null && row.price != null) {
            if (row.price.abs() != (incomeSumCell.value - outcomeSumCell.value).abs())
                logger.warn("Строка $rowNum: Графа «$msgPrice» должна быть равна разнице графы «$msgIn» и «$msgOut» по модулю!")
        } else if (incomeSumCell.value != null) {
            if (row.price != incomeSumCell.value)
                logger.warn("Строка $rowNum: Графа «$msgPrice» должна быть равна «$msgIn»!")
        } else if (outcomeSumCell.value != null) {
            if (row.price != outcomeSumCell.value)
                logger.warn("Строка $rowNum: Графа «$msgPrice» должна быть равна «$msgOut»!")
        }

        // Проверка количества
        if (row.count != 1) {
            def msg = row.getCell('count').column.name
            logger.warn("Строка $rowNum: В графе «$msg» может быть указано только значение «1»!")
        }

        // Проверка внешнеторговой сделки
        def Boolean deliveryPhis = null
        if (row.deliverySign != null) {
            deliveryPhis = getRefBookValue(18, row.deliverySign)?.CODE?.numberValue == 1
        }
        def msg14 = row.getCell('foreignDeal').column.name
        if (row.countryCodeNumeric == row.countryCodeNumeric2) {
            if (row.foreignDeal != recNoId) {
                logger.warn("Строка $rowNum: «$msg14» должно иметь значение «Нет»!")
            }
        } else if (deliveryPhis != null && deliveryPhis) {
            if (row.foreignDeal != recNoId) {
                logger.warn("Строка $rowNum: «$msg14» должно иметь значение «Нет»!")
            }
        } else if (row.foreignDeal != recYesId) {
            logger.warn("Строка $rowNum: «$msg14» должно иметь значение «Да»!")
        }

        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверка заполнения стоимости сделки
        def total = row.getCell('total')
        def price = row.getCell('price')
        if (total.value != price.value) {
            def msg1 = total.column.name
            def msg2 = price.column.name
            logger.warn("Строка $rowNum: «$msg1» не может отличаться от «$msg2»!")
        }

        // Проверка заполнения региона отправки
        def country = getRefBookValue(10, row.countryCodeNumeric)?.CODE?.stringValue
        if (country != null) {
            def regionName = row.getCell('regionCode').column.name
            def countryName = row.getCell('countryCodeNumeric').column.name
            if (country == '643' && row.regionCode == null) {
                logger.warn("Строка $rowNum: «$regionName» должен быть заполнен, т.к. в «$countryName» " +
                        "указан код 643!")
            } else if (country != '643' && row.regionCode != null) {
                logger.warn("Строка $rowNum: «$regionName» не должен быть заполнен, т.к. в «$countryName» " +
                        "указан код, отличный от 643!")
            }
        }

        // Проверка заполнения региона доставки
        country = getRefBookValue(10, row.countryCodeNumeric2)?.CODE?.stringValue
        if (country != null) {
            def regionName = row.getCell('region2').column.name
            def countryName = row.getCell('countryCodeNumeric2').column.name
            if (country == '643' && row.region2 == null) {
                logger.warn("Строка $rowNum: «$regionName» должен быть заполнен, т.к. в «$countryName» " +
                        "указан код 643!")
            } else if (country != '643' && row.region2 != null) {
                logger.warn("Строка $rowNum: «$regionName» не должен быть заполнен, т.к. в «$countryName» " +
                        "указан код, отличный от 643!")
            }
        }

        // Проверки соответствия НСИ
        checkNSI(9, row, "fullName")
        checkNSI(10, row, "countryCode")
        checkNSI(10, row, "countryCodeNumeric")
        checkNSI(10, row, "countryCodeNumeric2")
        checkNSI(4, row, "regionCode")
        checkNSI(4, row, "region2")
        checkNSI(17, row, "metalName")
        checkNSI(18, row, "deliverySign")
        checkNSI(63, row, "deliveryCode")
        checkNSI(20, row, "dealFocus")
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
            if (row1.price != row2.price) {
                return getColumnName(row1, 'price')
            }
            if (row1.total != row2.total) {
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
        // Расчет поля "Цена"
        if (row.incomeSum != null && row.outcomeSum != null) {
            row.price = (row.incomeSum - row.outcomeSum).abs()
        } else {
            row.price = row.incomeSum != null ? row.incomeSum : row.outcomeSum
        }
        // Расчет поля "Итого"
        row.total = row.price
        // Расчет поля "Количество"
        row.count = 1

        // Расчет полей зависимых от справочников
        def map = getRefBookValue(9, row.fullName)
        row.inn = map?.INN_KIO?.stringValue
        row.countryCode = map?.COUNTRY?.referenceValue
        row.countryName = map?.COUNTRY?.referenceValue

        // Признак физической поставки
        def Boolean deliveryPhis = null
        if (row.deliverySign != null) {
            deliveryPhis = getRefBookValue(18, row.deliverySign)?.CODE?.numberValue == 1
        }
        if (deliveryPhis != null && deliveryPhis) {
            row.countryCodeNumeric = null
            row.regionCode = null
            row.city = null
            row.locality = null
            row.countryCodeNumeric2 = null
            row.region2 = null
            row.city2 = null
            row.locality2 = null
        }

        row.foreignDeal = (row.countryCodeNumeric == row.countryCodeNumeric2 || deliveryPhis) ? recNoId : recYesId
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

    newRow.getCell('itog').colSpan = 26
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2

    // Расчеты подитоговых значений
    def BigDecimal priceItg = 0, totalItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        def row = dataRows.get(j)
        def price = row.price
        def total = row.total

        priceItg += price != null ? price : 0
        totalItg += total != null ? total : 0
    }
    newRow.price = priceItg
    newRow.total = totalItg

    return newRow
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def sep = ", "
    def StringBuilder builder = new StringBuilder()
    def map = getRefBookValue(9, row.fullName)
    if (map != null)
        builder.append(map.NAME?.stringValue).append(sep)
    if (row.docNumber != null)
        builder.append(row.docNumber).append(sep)
    if (row.docDate != null)
        builder.append(row.docDate).append(sep)
    dealFocus = getRefBookValue(20, row.dealFocus)?.DIRECTION?.stringValue
    if (dealFocus != null)
        builder.append(dealFocus).append(sep)
    deliverySign = getRefBookValue(18, row.deliverySign)?.SIGN?.stringValue
    if (deliverySign != null)
        builder.append(deliverySign).append(sep)
    metalName = getRefBookValue(17, row.metalName)?.INNER_CODE?.stringValue
    if (metalName != null)
        builder.append(metalName).append(sep)
    foreignDeal = getRefBookValue(38, row.foreignDeal)?.VALUE?.stringValue
    if (foreignDeal != null)
        builder.append(foreignDeal).append(sep)
    deliveryCode = getRefBookValue(63, row.deliveryCode)?.STRCODE?.stringValue
    if (deliveryCode != null)
        builder.append(deliveryCode).append(sep)

    def String retVal = builder.toString()
    if (retVal.length() < 2)
        return null
    return retVal.substring(0, retVal.length() - 2)
}

// Получение импортируемых данных.
void importData() {
    def xml = getXML('Полное наименование с указанием ОПФ', 'Подитог:')

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 28, 3)

    def headerMapping = [
            (xml.row[0].cell[1]): 'Признак взаимозависимости',
            (xml.row[0].cell[2]): 'ИНН/ КИО',
            (xml.row[0].cell[3]): 'Наименование страны регистрации',
            (xml.row[0].cell[4]): 'Код страны регистрации по классификатору ОКСМ',
            (xml.row[0].cell[5]): 'Номер договора',
            (xml.row[0].cell[6]): 'Дата договора',
            (xml.row[0].cell[7]): 'Номер сделки',
            (xml.row[0].cell[8]): 'Дата заключения сделки',
            (xml.row[0].cell[9]): 'Направленность сделки',
            (xml.row[0].cell[10]): 'Признак физической поставки драгоценного металла',
            (xml.row[0].cell[11]): 'Наименование драгоценного металла',
            (xml.row[0].cell[12]): 'Внешнеторговая сделка',
            (xml.row[0].cell[17]): 'Место совершения сделки (адрес места доставки (разгрузки драгоценного металла)',
            (xml.row[0].cell[21]): 'Код условия поставки',
            (xml.row[0].cell[22]): 'Количество',
            (xml.row[0].cell[23]): 'Сумма доходов Банка по данным бухгалтерского учета, руб.',
            (xml.row[0].cell[24]): 'Сумма расходов Банка по данным бухгалтерского учета, руб.',
            (xml.row[0].cell[25]): 'Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.',
            (xml.row[0].cell[26]): 'Итого стоимость без учета НДС, акцизов и пошлины, руб.',
            (xml.row[0].cell[27]): 'Дата совершения сделки',
            (xml.row[1].cell[13]): '\"Код страны по классификатору ОКСМ (цифровой)\"',
            (xml.row[1].cell[14]): '\"Регион (код)\"',
            (xml.row[1].cell[15]): 'Город',
            (xml.row[1].cell[16]): 'Населенный пункт (село, поселок и т.д.)',
            (xml.row[1].cell[17]): 'Код страны по классификатору ОКСМ (цифровой)',
            (xml.row[1].cell[18]): '\"Регион (код)\"',
            (xml.row[1].cell[19]): 'Город',
            (xml.row[1].cell[20]): 'Населенный пункт (село, поселок и т.д.)',
            (xml.row[2].cell[0]): 'гр. 2.1',
            (xml.row[2].cell[1]): 'гр. 2.2',
            (xml.row[2].cell[2]): 'гр. 3',
            (xml.row[2].cell[3]): 'гр. 4.1',
            (xml.row[2].cell[4]): 'гр. 4.2',
            (xml.row[2].cell[5]): 'гр. 5',
            (xml.row[2].cell[6]): 'гр. 6',
            (xml.row[2].cell[7]): 'гр. 7',
            (xml.row[2].cell[8]): 'гр. 8',
            (xml.row[2].cell[9]): 'гр. 9',
            (xml.row[2].cell[10]): 'гр. 10',
            (xml.row[2].cell[11]): 'гр. 11',
            (xml.row[2].cell[12]): 'гр. 12',
            (xml.row[2].cell[13]): 'гр. 13.1',
            (xml.row[2].cell[14]): 'гр. 13.2',
            (xml.row[2].cell[15]): 'гр. 13.3',
            (xml.row[2].cell[16]): 'гр. 13.4',
            (xml.row[2].cell[17]): 'гр. 14.1',
            (xml.row[2].cell[18]): 'гр. 14.2',
            (xml.row[2].cell[19]): 'гр. 14.3',
            (xml.row[2].cell[20]): 'гр. 14.4',
            (xml.row[2].cell[21]): 'гр. 15',
            (xml.row[2].cell[22]): 'гр. 16',
            (xml.row[2].cell[23]): 'гр. 17',
            (xml.row[2].cell[24]): 'гр. 18',
            (xml.row[2].cell[25]): 'гр. 19',
            (xml.row[2].cell[26]): 'гр. 20',
            (xml.row[2].cell[27]): 'гр. 21'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def int xmlIndexRow = -1
    def int xlsIndexRow = 0
    def int rowOffset = 3
    def int colOffset = 2

    def rows = new LinkedList<DataRow<Cell>>()

    for (def row : xml.row) {
        xmlIndexRow++
        xlsIndexRow = xmlIndexRow + rowOffset

        // пропустить шапку таблицы
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        newRow.rowNum = xmlIndexRow - headRowCount

        // графа 2.1
        newRow.fullName = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        def map = getRefBookValue(9, newRow.fullName)
        xmlIndexCol++

        // графа 2.2
        newRow.interdependence = getRecordIdImport(38, 'VALUE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
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
            if (map != null) {
                if ((text != null && !text.isEmpty() && !text.equals(map.NAME?.stringValue)) || ((text == null || text.isEmpty()) && map.NAME?.stringValue != null)) {
                    logger.warn("Строка ${xlsIndexRow} столбец ${xmlIndexCol + colOffset} содержит значение, отсутствующее в справочнике!")
                }
            }
        }
        xmlIndexCol++

        // графа 4.2
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE?.stringValue)) || ((text == null || text.isEmpty()) && map.CODE?.stringValue != null)) {
                logger.warn("Строка ${xlsIndexRow} столбец ${xmlIndexCol + colOffset} содержит значение, отсутствующее в справочнике!")
            }
        }
        xmlIndexCol++

        // графа 5
        newRow.docNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 6
        newRow.docDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 7
        newRow.dealNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 8
        newRow.dealDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 9
        newRow.dealFocus = getRecordIdImport(20, 'DIRECTION', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 10
        newRow.deliverySign = getRecordIdImport(18, 'SIGN', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 11
        newRow.metalName = getRecordIdImport(17, 'INNER_CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 12
        newRow.foreignDeal = getRecordIdImport(38, 'VALUE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 13.1
        newRow.countryCodeNumeric = getRecordIdImport(10, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 13.2
        String code = row.cell[xmlIndexCol].text()
        if (code.length() == 1) {    //для кодов 1, 2, 3...9
            code = "0".concat(code)
        }
        newRow.regionCode = getRecordIdImport(4, 'CODE', code, xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 13.3
        newRow.city = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 13.4
        newRow.locality = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 14.1
        newRow.countryCodeNumeric2 = getRecordIdImport(10, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 14.2
        code = row.cell[xmlIndexCol].text()
        if (code.length() == 1) {    //для кодов 1, 2, 3...9
            code = "0".concat(code)
        }
        newRow.region2 = getRecordIdImport(4, 'CODE', code, xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 14.3
        newRow.city2 = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 14.4
        newRow.locality2 = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 15
        newRow.deliveryCode = getRecordIdImport(63, 'STRCODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 16
        xmlIndexCol++

        // графа 17
        newRow.incomeSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 18
        newRow.outcomeSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 19
        xmlIndexCol++
        // графа 20
        xmlIndexCol++

        // графа 21
        newRow.dealDoneDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}