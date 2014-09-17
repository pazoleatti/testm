package form_template.deal.precious_metals_trade.v2013

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 394 - Купля-продажа драгоценных металлов (19)
 *
 * formTemplateId=394
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
        formDataService.consolidationSimple(formData, logger)
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
def nonEmptyColumns = ['fullName', 'interdependence', 'docNumber', 'docDate', 'dealFocus', 'deliverySign',
        'metalName', 'foreignDeal', 'count', 'price', 'total', 'dealDoneDate']

// Дата окончания отчетного периода
@Field
def endDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
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
    if (!fileName.endsWith('.xls') && !fileName.endsWith('.xlsx') && !fileName.endsWith('.xlsm')) {
        throw new ServiceException('Выбранный файл не соответствует формату xls/xlsx/xlsm!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
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

    // "Да" и "Нет"
    def recYesId = getRecordId(38, 'CODE', '1', -1, null, true)
    def recNoId = getRecordId(38, 'CODE', '0', -1, null, true)

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, ['docNumber', 'docDate'], logger, true)
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns - ['docNumber', 'docDate'], logger, false)

        def docDateCell = row.getCell('docDate')
        def dealDateCell = row.getCell('dealDate')
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name

        // Корректность даты заключения сделки
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
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
                    builder.append("«").append(getAttributes().get(checkField.get(i))[2])
                            .append("» ").append("(")
                            .append(getAttributes().get(checkField.get(i))[1])
                            .append(")")
                    if (i != (checkField.size() - 1)) {
                        builder.append(", ")
                    }
                }
                rowWarning(logger, row, "Строка $rowNum: Графы ${builder.toString()} не должны быть заполнены, т.к. в графе «$msg1» указано значение «ОМС»!")
            }
        } else if (getRefBookValue(18, row.deliverySign)?.CODE?.numberValue == 2) {
            // a
            if(row.countryCodeNumeric == null || row.countryCodeNumeric2 == null){
                def msg1 = row.getCell('deliverySign').column.name
                def msg2 = row.getCell('countryCodeNumeric').column.name
                def msg3 = row.getCell('countryCodeNumeric2').column.name
                rowWarning(logger, row, "Строка $rowNum: Графы «$msg2», «$msg3» должны быть заполнены, т.к. в графе «$msg1» указано значение «Физическая поставка»!")
            }
            // 2bc
            def country = getRefBookValue(10, row.countryCodeNumeric)?.CODE?.stringValue
            if (country != null) {
                def regionName = row.getCell('regionCode').column.name
                def countryName = row.getCell('countryCodeNumeric').column.name
                if (country == '643' && row.regionCode == null) {
                    rowWarning(logger, row, "Строка $rowNum: Графа «$regionName» должна быть заполнена, т.к. в графе «$countryName» указан код 643!")
                } else if (country != '643' && row.regionCode != null) {
                    rowWarning(logger, row, "Строка $rowNum: Графа «$regionName» не должна быть заполнена, т.к. в графе «$countryName» указан код, отличный от 643!")
                }
            }
            // 2de
            country = getRefBookValue(10, row.countryCodeNumeric2)?.CODE?.stringValue
            if (country != null) {
                def regionName = row.getCell('region2').column.name
                def countryName = row.getCell('countryCodeNumeric2').column.name
                if (country == '643' && row.region2 == null) {
                    rowWarning(logger, row, "Строка $rowNum: Графа «$regionName» должна быть заполнена, т.к. в графе «$countryName» указан код 643!")
                } else if (country != '643' && row.region2 != null) {
                    rowWarning(logger, row, "Строка $rowNum: Графа «$regionName» не должна быть заполнена, т.к. в графе «$countryName» указан код, отличный от 643!")
                }
            }
            // 2fg
            def msg1 = row.getCell('city').column.name
            def msg2 = row.getCell('locality').column.name
            if (row.city == null && row.locality == null) {
                rowWarning(logger, row, "Строка $rowNum: Графа «$msg1» должна быть заполнена, если не заполнена графа «$msg2»!")
            } else if (row.city != null && row.locality != null){
                rowWarning(logger, row, "Строка $rowNum: Графа «$msg1» не может быть заполнена одновременно с графой «$msg2»!")
            }
            // 2hi
            msg1 = row.getCell('city2').column.name
            msg2 = row.getCell('locality2').column.name
            if (row.city2 == null && row.locality2 == null) {
                rowWarning(logger, row, "Строка $rowNum: Графа «$msg1» должна быть заполнена, если не заполнена графа «$msg2»!")
            } else if (row.city2 != null && row.locality2 != null){
                rowWarning(logger, row, "Строка $rowNum: Графа «$msg1» не может быть заполнена одновременно с графой «$msg2»!")
            }
        }

        // Проверка доходов и расходов
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            rowWarning(logger, row, "Строка $rowNum: Графа  «$msgOut» должна быть заполнена, если не заполнена графа «$msgIn»!")
        }

        // Проверка доходов/расходов и стоимости
        def msgPrice = row.getCell('price').column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null && row.price != null) {
            if (row.price.abs() != (incomeSumCell.value - outcomeSumCell.value).abs())
                rowWarning(logger, row, "Строка $rowNum: Значение графы «$msgPrice» должно быть равно разнице значений граф «$msgIn» и «$msgOut» по модулю!")
        } else if (incomeSumCell.value != null) {
            if (row.price != incomeSumCell.value)
                rowWarning(logger, row, "Строка $rowNum: Значение графы «$msgPrice» должно быть равно значению графы «$msgIn»!")
        } else if (outcomeSumCell.value != null) {
            if (row.price != outcomeSumCell.value)
                rowWarning(logger, row, "Строка $rowNum: Значение графы «$msgPrice» должно быть равно значению графы «$msgOut»!")
        }

        // Проверка количества
        if (row.count != 1) {
            def msg = row.getCell('count').column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg» может быть только «1»!")
        }

        // Проверка внешнеторговой сделки
        def Boolean deliveryPhis = null
        if (row.deliverySign != null) {
            deliveryPhis = getRefBookValue(18, row.deliverySign)?.CODE?.numberValue == 1
        }
        def msg14 = row.getCell('foreignDeal').column.name
        if (row.countryCodeNumeric == row.countryCodeNumeric2) {
            if (row.foreignDeal != recNoId) {
                rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg14» должно быть равно «Нет»!")
            }
        } else if (deliveryPhis != null && deliveryPhis) {
            if (row.foreignDeal != recNoId) {
                rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg14» должно быть равно «Нет»!")
            }
        } else if (row.foreignDeal != recYesId) {
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg14» должно быть равно «Да»!")
        }

        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка заполнения стоимости сделки
        def total = row.getCell('total')
        def price = row.getCell('price')
        if (total.value != price.value) {
            def msg1 = total.column.name
            def msg2 = price.column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }
    }

    if (formData.kind == FormDataKind.CONSOLIDATED) {
        checkItog(dataRows)
    }
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

    for (row in dataRows) {
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

        // Признак физической поставки
        def Boolean deliveryPhis = null
        if (row.deliverySign != null) {
            deliveryPhis = (getRefBookValue(18, row.deliverySign)?.CODE?.numberValue == 1)
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
        } else {
            row.locality = row.city ?: row.locality
            row.locality2 = row.city2 ?: row.locality2
        }

        row.foreignDeal = (row.countryCodeNumeric == row.countryCodeNumeric2 || deliveryPhis) ? recNoId : recYesId
    }

    // Добавление подитов
    if (formData.kind == FormDataKind.CONSOLIDATED) {
        addAllAliased(dataRows, new CalcAliasRow() {
            @Override
            DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
                return calcItog(i, dataRows)
            }
        }, groupColumns)
    }

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
    def tmpRow = formData.createDataRow()
    def xml = getXML(getColumnName(tmpRow, 'rowNum'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 28, 3)

    def headerMapping = [
            (xml.row[0].cell[0]): getColumnName(tmpRow, 'rowNum'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'fullName'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'interdependence'),
            (xml.row[0].cell[4]): getColumnName(tmpRow, 'inn'),
            (xml.row[0].cell[5]): getColumnName(tmpRow, 'countryName'),
            (xml.row[0].cell[6]): getColumnName(tmpRow, 'countryCode'),
            (xml.row[0].cell[7]): getColumnName(tmpRow, 'docNumber'),
            (xml.row[0].cell[8]): getColumnName(tmpRow, 'docDate'),
            (xml.row[0].cell[9]): getColumnName(tmpRow, 'dealNumber'),
            (xml.row[0].cell[10]): getColumnName(tmpRow, 'dealDate'),
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'dealFocus'),
            (xml.row[0].cell[12]): getColumnName(tmpRow, 'deliverySign'),
            (xml.row[0].cell[13]): getColumnName(tmpRow, 'metalName'),
            (xml.row[0].cell[14]): getColumnName(tmpRow, 'foreignDeal'),
            (xml.row[0].cell[15]): getColumnName(tmpRow, 'countryCodeNumeric'),
            (xml.row[0].cell[16]): getColumnName(tmpRow, 'regionCode'),
            (xml.row[0].cell[17]): getColumnName(tmpRow, 'city'),
            (xml.row[0].cell[18]): getColumnName(tmpRow, 'locality'),
            (xml.row[0].cell[19]): getColumnName(tmpRow, 'countryCodeNumeric2'),
            (xml.row[0].cell[20]): getColumnName(tmpRow, 'region2'),
            (xml.row[0].cell[21]): getColumnName(tmpRow, 'city2'),
            (xml.row[0].cell[22]): getColumnName(tmpRow, 'locality2'),
            (xml.row[0].cell[23]): getColumnName(tmpRow, 'deliveryCode'),
            (xml.row[0].cell[24]): getColumnName(tmpRow, 'count'),
            (xml.row[0].cell[25]): getColumnName(tmpRow, 'incomeSum'),
            (xml.row[0].cell[26]): getColumnName(tmpRow, 'outcomeSum'),
            (xml.row[0].cell[27]): getColumnName(tmpRow, 'price'),
            (xml.row[0].cell[28]): getColumnName(tmpRow, 'total'),
            (xml.row[0].cell[30]): getColumnName(tmpRow, 'dealDoneDate'),
            (xml.row[1].cell[2]): 'гр. 2.1',
            (xml.row[1].cell[3]): 'гр. 2.2',
            (xml.row[1].cell[4]): 'гр. 3',
            (xml.row[1].cell[5]): 'гр. 4.1',
            (xml.row[1].cell[6]): 'гр. 4.2',
            (xml.row[1].cell[7]): 'гр. 5',
            (xml.row[1].cell[8]): 'гр. 6',
            (xml.row[1].cell[9]): 'гр. 7',
            (xml.row[1].cell[10]): 'гр. 8',
            (xml.row[1].cell[11]): 'гр. 9',
            (xml.row[1].cell[12]): 'гр. 10',
            (xml.row[1].cell[13]): 'гр. 11',
            (xml.row[1].cell[14]): 'гр. 12',
            (xml.row[1].cell[15]): 'гр. 13.1',
            (xml.row[1].cell[16]): 'гр. 13.2',
            (xml.row[1].cell[17]): 'гр. 13.3',
            (xml.row[1].cell[18]): 'гр. 13.4',
            (xml.row[1].cell[19]): 'гр. 14.1',
            (xml.row[1].cell[20]): 'гр. 14.2',
            (xml.row[1].cell[21]): 'гр. 14.3',
            (xml.row[1].cell[22]): 'гр. 14.4',
            (xml.row[1].cell[23]): 'гр. 15',
            (xml.row[1].cell[24]): 'гр. 16',
            (xml.row[1].cell[25]): 'гр. 17',
            (xml.row[1].cell[26]): 'гр. 18',
            (xml.row[1].cell[27]): 'гр. 19',
            (xml.row[1].cell[28]): 'гр. 20',
            (xml.row[1].cell[30]): 'гр. 21'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 1)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def int xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

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

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        xmlIndexCol++

        // графа fix
        xmlIndexCol++

        // графа 2.1
        newRow.fullName = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        def map = getRefBookValue(9, newRow.fullName)
        xmlIndexCol++

        // графа 2.2
        newRow.interdependence = getRecordIdImport(38, 'VALUE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            formDataService.checkReferenceValue(9, row.cell[xmlIndexCol].text(), map.INN_KIO?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }
        xmlIndexCol++

        // графа 4.1
        if (map != null) {
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)
            if (map != null) {
                formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.NAME?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
            }
        }
        xmlIndexCol++

        // графа 4.2
        if (map != null) {
            formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.CODE?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
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
        // графа fix
        xmlIndexCol++

        // графа 21
        newRow.dealDoneDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getAttributes() {
    [
            rowNum:             ['rowNum',              'гр. 1', '№ п/п'],
            fullName:           ['fullName',            'гр. 2.1', 'Полное наименование с указанием ОПФ'],
            interdependence:    ['interdependence',     'гр. 2.2', 'Признак взаимозависимости'],
            inn:                ['inn',                 'гр. 3', 'ИНН/ КИО'],
            countryName:        ['countryName',         'гр. 4.1', 'Наименование страны регистрации'],
            countryCode:        ['countryCode',         'гр. 4.2', 'Код страны регистрации по классификатору ОКСМ'],
            docNumber:          ['docNumber',           'гр. 5', 'Номер договора'],
            docDate:            ['docDate',             'гр. 6', 'Дата договора'],
            dealNumber:         ['dealNumber',          'гр. 7', 'Номер сделки'],
            dealDate:           ['dealDate',            'гр. 8', 'Дата заключения сделки'],
            dealFocus:          ['dealFocus',           'гр. 9', 'Направленность сделки'],
            deliverySign:       ['deliverySign',        'гр. 10', 'Признак физической поставки драгоценного металла'],
            metalName:          ['metalName',           'гр. 11', 'Наименование драгоценного металла'],
            foreignDeal:        ['foreignDeal',         'гр. 12', 'Внешнеторговая сделка'],
            countryCodeNumeric: ['countryCodeNumeric',  'гр. 13.1', 'Код страны по классификатору ОКСМ (цифровой)'],
            regionCode:         ['regionCode',          'гр. 13.2', 'Регион (код)'],
            city:               ['city',                'гр. 13.3', 'Город'],
            locality:           ['locality',            'гр. 13.4', 'Населенный пункт (село, поселок и т.д.)'],
            countryCodeNumeric2:['countryCodeNumeric2', 'гр. 14.1', 'Код страны по классификатору ОКСМ (цифровой)'],
            region2:            ['region2',             'гр. 14.2', 'Регион (код)'],
            city2:              ['city2',               'гр. 14.3', 'Город'],
            locality2:          ['locality2',           'гр. 14.4', 'Населенный пункт (село, поселок и т.д.)'],
            deliveryCode:       ['deliveryCode',        'гр. 15', 'Код условия поставки'],
            count:              ['count',               'гр. 16', 'Количество'],
            incomeSum:          ['incomeSum',           'гр. 17', 'Сумма доходов Банка по данным бухгалтерского учета, руб.'],
            outcomeSum:         ['outcomeSum',          'гр. 18', 'Сумма расходов Банка по данным бухгалтерского учета, руб.'],
            price:              ['price',               'гр. 19', 'Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.'],
            total:              ['total',               'гр. 20', 'Итого стоимость без учета НДС, акцизов и пошлины, руб.'],
            dealDoneDate:       ['dealDoneDate',        'гр. 21', 'Дата совершения сделки']
    ]
}