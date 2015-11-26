package form_template.deal.precious_metals_trade.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 3394 - Купля-продажа драгоценных металлов (19)
 *
 * formTemplateId=3394
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
        formDataService.saveCachedDataRows(formData, logger)
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
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
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
    def dataRows = formDataService.getDataRowHelper(formData).allCached
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

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

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
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
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
                rowError(logger, row, "Строка $rowNum: Графы ${builder.toString()} не должны быть заполнены, т.к. в графе «$msg1» указано значение «ОМС»!")
            }
        } else if (getRefBookValue(18, row.deliverySign)?.CODE?.numberValue == 2) {
            // a
            if(row.countryCodeNumeric == null || row.countryCodeNumeric2 == null){
                def msg1 = row.getCell('deliverySign').column.name
                def msg2 = row.getCell('countryCodeNumeric').column.name
                def msg3 = row.getCell('countryCodeNumeric2').column.name
                rowError(logger, row, "Строка $rowNum: Графы «$msg2», «$msg3» должны быть заполнены, т.к. в графе «$msg1» указано значение «Физическая поставка»!")
            }
            // 2bc
            def country = getRefBookValue(10, row.countryCodeNumeric)?.CODE?.stringValue
            if (country != null) {
                def regionName = row.getCell('regionCode').column.name
                def countryName = row.getCell('countryCodeNumeric').column.name
                if (country == '643' && row.regionCode == null) {
                    rowError(logger, row, "Строка $rowNum: Графа «$regionName» должна быть заполнена, т.к. в графе «$countryName» указан код 643!")
                } else if (country != '643' && row.regionCode != null) {
                    rowError(logger, row, "Строка $rowNum: Графа «$regionName» не должна быть заполнена, т.к. в графе «$countryName» указан код, отличный от 643!")
                }
            }
            // 2de
            country = getRefBookValue(10, row.countryCodeNumeric2)?.CODE?.stringValue
            if (country != null) {
                def regionName = row.getCell('region2').column.name
                def countryName = row.getCell('countryCodeNumeric2').column.name
                if (country == '643' && row.region2 == null) {
                    rowError(logger, row, "Строка $rowNum: Графа «$regionName» должна быть заполнена, т.к. в графе «$countryName» указан код 643!")
                } else if (country != '643' && row.region2 != null) {
                    rowError(logger, row, "Строка $rowNum: Графа «$regionName» не должна быть заполнена, т.к. в графе «$countryName» указан код, отличный от 643!")
                }
            }
            // 2fg
            if (row.city == null && row.locality == null) {
                def msg1 = row.getCell('city').column.name
                def msg2 = row.getCell('locality').column.name
                rowWarning(logger, row, "Строка $rowNum: Графа «$msg1» должна быть заполнена, если не заполнена графа «$msg2»!")
            }
            // 2hi
            if (row.city2 == null && row.locality2 == null) {
                msg1 = row.getCell('city2').column.name
                msg2 = row.getCell('locality2').column.name
                rowWarning(logger, row, "Строка $rowNum: Графа «$msg1» должна быть заполнена, если не заполнена графа «$msg2»!")
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

        // Проверка диапазона дат
        if (row.docDate) {
            checkDateValid(logger, row, 'docDate', row.docDate, true)
        }
        if (row.dealDate) {
            checkDateValid(logger, row, 'dealDate', row.dealDate, true)
        }
        if (row.dealDoneDate) {
            checkDateValid(logger, row, 'dealDoneDate', row.dealDoneDate, true)
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
    def dataRows = formDataService.getDataRowHelper(formData).allCached
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

    sortFormDataRows(false)
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('itog').colSpan = 26
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2
    ['rowNum', 'itog', 'fullName', 'interdependence', 'inn', 'countryName', 'countryCode',
            'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealFocus', 'deliverySign',
            'metalName', 'foreignDeal', 'countryCodeNumeric', 'regionCode', 'city', 'locality',
            'countryCodeNumeric2', 'region2', 'city2', 'locality2', 'deliveryCode', 'count',
            'incomeSum', 'outcomeSum', 'price', 'total', 'fix', 'dealDoneDate'].each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }

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

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 31
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Общая информация о контрагенте - юридическом лице'
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues || rowValues.isEmpty() || !rowValues.find { it }) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Подитог:") {
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]) : 'Общая информация о контрагенте - юридическом лице']),
            ([(headerRows[0][7]) : 'Сведения о сделке']),
            ([(headerRows[1][0]) : getColumnName(tmpRow, 'rowNum')]),
            ([(headerRows[1][2]) : getColumnName(tmpRow, 'fullName')]),
            ([(headerRows[1][3]) : getColumnName(tmpRow, 'interdependence')]),
            ([(headerRows[1][4]) : getColumnName(tmpRow, 'inn')]),
            ([(headerRows[1][5]) : getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[1][6]) : getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][7]) : getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][8]) : getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][9]) : getColumnName(tmpRow, 'dealNumber')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'dealDate')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'dealFocus')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'deliverySign')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'metalName')]),
            ([(headerRows[1][14]): getColumnName(tmpRow, 'foreignDeal')]),
            ([(headerRows[1][15]): getColumnName(tmpRow, 'countryCodeNumeric')]),
            ([(headerRows[1][16]): getColumnName(tmpRow, 'regionCode')]),
            ([(headerRows[1][17]): getColumnName(tmpRow, 'city')]),
            ([(headerRows[1][18]): getColumnName(tmpRow, 'locality')]),
            ([(headerRows[1][19]): getColumnName(tmpRow, 'countryCodeNumeric2')]),
            ([(headerRows[1][20]): getColumnName(tmpRow, 'region2')]),
            ([(headerRows[1][21]): getColumnName(tmpRow, 'city2')]),
            ([(headerRows[1][22]): getColumnName(tmpRow, 'locality2')]),
            ([(headerRows[1][23]): getColumnName(tmpRow, 'deliveryCode')]),
            ([(headerRows[1][24]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[1][25]): getColumnName(tmpRow, 'incomeSum')]),
            ([(headerRows[1][26]): getColumnName(tmpRow, 'outcomeSum')]),
            ([(headerRows[1][27]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][28]): getColumnName(tmpRow, 'total')]),
            ([(headerRows[1][30]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][0]) : 'гр. 1']),
            ([(headerRows[2][2]) : 'гр. 2.1']),
            ([(headerRows[2][3]) : 'гр. 2.2']),
            ([(headerRows[2][4]) : 'гр. 3']),
            ([(headerRows[2][5]) : 'гр. 4.1']),
            ([(headerRows[2][6]) : 'гр. 4.2']),
            ([(headerRows[2][7]) : 'гр. 5']),
            ([(headerRows[2][8]) : 'гр. 6']),
            ([(headerRows[2][9]) : 'гр. 7']),
            ([(headerRows[2][10]): 'гр. 8']),
            ([(headerRows[2][11]): 'гр. 9']),
            ([(headerRows[2][12]): 'гр. 10']),
            ([(headerRows[2][13]): 'гр. 11']),
            ([(headerRows[2][14]): 'гр. 12']),
            ([(headerRows[2][15]): 'гр. 13.1']),
            ([(headerRows[2][16]): 'гр. 13.2']),
            ([(headerRows[2][17]): 'гр. 13.3']),
            ([(headerRows[2][18]): 'гр. 13.4']),
            ([(headerRows[2][19]): 'гр. 14.1']),
            ([(headerRows[2][20]): 'гр. 14.2']),
            ([(headerRows[2][21]): 'гр. 14.3']),
            ([(headerRows[2][22]): 'гр. 14.4']),
            ([(headerRows[2][23]): 'гр. 15']),
            ([(headerRows[2][24]): 'гр. 16']),
            ([(headerRows[2][25]): 'гр. 17']),
            ([(headerRows[2][26]): 'гр. 18']),
            ([(headerRows[2][27]): 'гр. 19']),
            ([(headerRows[2][28]): 'гр. 20']),
            ([(headerRows[2][30]): 'гр. 21'])
    ]
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    def int colIndex = 0

    // графа 1
    colIndex++

    // графа fix
    colIndex++

    // графа 2.1
    newRow.fullName = getRecordIdImport(9, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    def map = getRefBookValue(9, newRow.fullName)
    colIndex++

    // графа 2.2
    newRow.interdependence = getRecordIdImport(38, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(9, values[colIndex], map.INN_KIO?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4.1
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY?.referenceValue)
        if (map != null) {
            formDataService.checkReferenceValue(10, values[colIndex], map.NAME?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 4.2
    if (map != null) {
        formDataService.checkReferenceValue(10, values[colIndex], map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 5
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 6
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.dealNumber = values[colIndex]
    colIndex++

    // графа 8
    newRow.dealDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.dealFocus = getRecordIdImport(20, 'DIRECTION', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 10
    newRow.deliverySign = getRecordIdImport(18, 'SIGN', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 11
    newRow.metalName = getRecordIdImport(17, 'INNER_CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 12
    newRow.foreignDeal = getRecordIdImport(38, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 13.1
    newRow.countryCodeNumeric = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 13.2
    String code = values[colIndex]
    if (code.length() == 1) {    //для кодов 1, 2, 3...9
        code = "0".concat(code)
    }
    newRow.regionCode = getRecordIdImport(4, 'CODE', code, fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 13.3
    newRow.city = values[colIndex]
    colIndex++

    // графа 13.4
    newRow.locality = values[colIndex]
    colIndex++

    // графа 14.1
    newRow.countryCodeNumeric2 = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 14.2
    code = values[colIndex]
    if (code.length() == 1) {    //для кодов 1, 2, 3...9
        code = "0".concat(code)
    }
    newRow.region2 = getRecordIdImport(4, 'CODE', code, fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 14.3
    newRow.city2 = values[colIndex]
    colIndex++

    // графа 14.4
    newRow.locality2 = values[colIndex]
    colIndex++

    // графа 15
    newRow.deliveryCode = getRecordIdImport(63, 'STRCODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 16
    colIndex++

    // графа 17
    newRow.incomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 18
    newRow.outcomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 19
    colIndex++
    // графа 20
    colIndex++
    // графа fix
    colIndex++

    // графа 21
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
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

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), null, true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null}
}