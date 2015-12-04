package form_template.deal.precious_metals_deliver.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 3393 - Поставочные срочные сделки с драгоценными металлами (18)
 *
 * formTemplateId=3393
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
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
def editableColumns = ['name', 'dependence', 'dealType', 'contractNum', 'contractDate', 'transactionNum',
        'transactionDeliveryDate', 'innerCode','unitCountryCode', 'signPhis', 'countryCode2', 'region1', 'city1',
        'settlement1', 'countryCode3', 'region2', 'city2', 'settlement2', 'conditionCode', 'incomeSum',
        'consumptionSum', 'transactionDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'innKio', 'country', 'countryCode1', 'signTransaction', 'count', 'priceOne', 'totalNds']

// Группируемые атрибуты
@Field
def groupColumns = ['name', 'dependence', 'country', 'countryCode1', 'signPhis', 'signTransaction']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'dependence', 'dealType', 'contractNum', 'contractDate', 'innerCode', 'unitCountryCode',
                       'signPhis', 'signTransaction', 'count', 'priceOne', 'totalNds', 'transactionDate']

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

        // графа 14 = элемент с кодом «1» ("ОМС")
        def isOMS = (getRefBookValue(18, row.signPhis)?.CODE?.numberValue == 1)
        // графа 14 = элемент с кодом «2» ("Физическая поставка")
        def isPhysics = (getRefBookValue(18, row.signPhis)?.CODE?.numberValue == 2)

        checkNonEmptyColumns(row, rowNum, isOMS ? nonEmptyColumns : nonEmptyColumns + ['countryCode2'], logger, true)

        def transactionDeliveryDate = row.transactionDeliveryDate
        def contractDate = row.contractDate

        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('consumptionSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name
        def priceOne = row.priceOne
        def totalNds = row.totalNds
        def count = row.count
        def transactionDate = row.transactionDate

        // Проверка зависимости от признака физической поставки
        if (isOMS) {
            // 1
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
                    builder.append("«").append(getAttributes().get(checkField.get(i))[2])
                            .append("» ").append("(")
                            .append(getAttributes().get(checkField.get(i))[1])
                            .append(")")
                    if (i != (checkField.size() - 1)) {
                        builder.append(", ")
                    }
                }
                def msg1 = row.getCell('signPhis').column.name
                rowError(logger, row, "Строка $rowNum: Графы ${builder.toString()} не должны быть заполнены, т.к. в графе «$msg1» указано значение «ОМС»!")
            }
        } else if(isPhysics){
            // 2a
            if(row.countryCode2 == null || row.countryCode3 == null){
                def msg1 = row.getCell('signPhis').column.name
                def msg2 = row.getCell('countryCode2').column.name
                def msg3 = row.getCell('countryCode3').column.name
                rowError(logger, row, "Строка $rowNum: Графы «$msg2», «$msg3» должны быть заполнены, т.к. в графе «$msg1» указано значение «Физическая поставка»!")
            }
            // 2bc
            def country = getRefBookValue(10, row.countryCode2)?.CODE?.stringValue
            if (country != null) {
                def regionName = row.getCell('region1').column.name
                def countryName = row.getCell('countryCode2').column.name
                if (country == '643' && row.region1 == null) {
                    rowError(logger, row, "Строка $rowNum: Графа «$regionName» должна быть заполнена, т.к. в графе «$countryName» указан код 643!")
                } else if (country != '643' && row.region1 != null) {
                    rowError(logger, row, "Строка $rowNum: Графа «$regionName» не должна быть заполнена, т.к. в графе «$countryName» указан код, отличный от 643!")
                }
            }
            // 2de
            country = getRefBookValue(10, row.countryCode3)?.CODE?.stringValue
            if (country != null) {
                def regionName = row.getCell('region2').column.name
                def countryName = row.getCell('countryCode3').column.name
                if (country == '643' && row.region2 == null) {
                    rowError(logger, row, "Строка $rowNum: Графа «$regionName» должна быть заполнена, т.к. в графе «$countryName» указан код 643!")
                } else if (country != '643' && row.region2 != null) {
                    rowError(logger, row, "Строка $rowNum: Графа «$regionName» не должна быть заполнена, т.к. в графе «$countryName» указан код, отличный от 643!")
                }
            }
            // 2fg
            if (row.city1 == null && row.settlement1 == null) {
                def msg1 = row.getCell('city1').column.name
                def msg2 = row.getCell('settlement1').column.name
                rowError(logger, row, "Строка $rowNum: Графа «$msg1» должна быть заполнена, если не заполнена графа «$msg2»!")
            }
            // 2hi
            if (row.city2 == null && row.settlement2 == null) {
                msg1 = row.getCell('city2').column.name
                msg2 = row.getCell('settlement2').column.name
                rowError(logger, row, "Строка $rowNum: Графа «$msg1» должна быть заполнена, если не заполнена графа «$msg2»!")
            }
        }

        // Корректность даты заключения сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = row.getCell('transactionDeliveryDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Корректность заполнения признака внешнеторговой сделки
        def msg14 = row.getCell('signTransaction').column.name
        if (row.countryCode2 == row.countryCode3 && row.signTransaction != recNoId ||
                row.countryCode2 != row.countryCode3 && row.signTransaction != recYesId) {
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg14» не соответствует сведениям о стране отправке и о стране доставки драгоценных металлов!")
        }

        // Проверка доходов и расходов
        if (incomeSumCell.value  == null && outcomeSumCell.value == null) {
            rowError(logger, row, "Строка $rowNum: Графа «$msgIn» должна быть заполнена, если не заполнена графа «$msgOut»!")
        }

        // Проверка доходов/расходов и стоимости
        def msgPrice = row.getCell('priceOne').column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            if ((row.priceOne ?: 0).abs() != (incomeSumCell.value - outcomeSumCell.value).abs())
                rowError(logger, row, "Строка $rowNum: Значение графы «$msgPrice» должно быть равно разнице значений граф «$msgIn» и «$msgOut» по модулю!")
        } else if (incomeSumCell.value != null) {
            if (row.priceOne != incomeSumCell.value)
                rowError(logger, row, "Строка $rowNum: Значение графы «$msgPrice» должно быть равно значению графы «$msgIn»!")
        } else if (outcomeSumCell.value != null) {
            if (row.priceOne != outcomeSumCell.value)
                rowError(logger, row, "Строка $rowNum: Значение графы «$msgPrice» должно быть равно значению графы «$msgOut»!")
        }

        // Проверка количества
        if (count != null && count != 1) {
            def msg = row.getCell('count').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg» может быть только «1»!")
        }

        // Корректность дат сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('transactionDeliveryDate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка заполнения стоимости сделки
        if (priceOne != totalNds) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('totalNds').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }

        // Проверка диапазона дат
        if (row.contractDate) {
            checkDateValid(logger, row, 'contractDate', row.contractDate, true)
        }
        if (row.transactionDate) {
            checkDateValid(logger, row, 'transactionDate', row.transactionDate, true)
        }
        if (row.transactionDeliveryDate) {
            checkDateValid(logger, row, 'transactionDeliveryDate', row.transactionDeliveryDate, true)
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

        // Признак физической поставки
        def Boolean deliveryPhis = null
        if (row.signPhis != null) {
            deliveryPhis = (getRefBookValue(18, row.signPhis)?.CODE?.numberValue == 1)
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
        } else {
            row.settlement1 = row.city1 ?: row.settlement1
            row.settlement2 = row.city2 ?: row.settlement2
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

    newRow.getCell('itog').colSpan = 25
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2
    ['rowNum', 'itog', 'name', 'dependence', 'innKio', 'country', 'countryCode1', 'contractNum', 'contractDate',
            'transactionNum', 'dealType', 'transactionDeliveryDate', 'innerCode', 'unitCountryCode', 'signPhis',
            'signTransaction', 'countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2',
            'city2', 'settlement2', 'conditionCode', 'count', 'incomeSum', 'consumptionSum', 'priceOne', 'totalNds',
            'fix', 'transactionDate', 'okpCode'].each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }

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

    map2 = getRefBookValue(10, map?.COUNTRY?.referenceValue)

    country = map2?.NAME?.stringValue
    if (country != null)
        builder.append(country).append(sep)

    countryCode1 = map2?.CODE?.stringValue
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

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 33
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = 'Общие сведения о контрагенте - юридическом лице'
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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

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
            ([(headerRows[0][0]) : 'Общие сведения о контрагенте - юридическом лице']),
            ([(headerRows[0][7]) : 'Сведения о сделке']),
            ([(headerRows[1][0]) : getColumnName(tmpRow, 'rowNum')]),
            ([(headerRows[1][2]) : getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][3]) : getColumnName(tmpRow, 'dependence')]),
            ([(headerRows[1][4]) : getColumnName(tmpRow, 'innKio')]),
            ([(headerRows[1][5]) : getColumnName(tmpRow, 'country')]),
            ([(headerRows[1][6]) : getColumnName(tmpRow, 'countryCode1')]),
            ([(headerRows[1][7]) : getColumnName(tmpRow, 'contractNum')]),
            ([(headerRows[1][8]) : getColumnName(tmpRow, 'contractDate')]),
            ([(headerRows[1][9]) : getColumnName(tmpRow, 'transactionNum')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'dealType')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'transactionDeliveryDate')]),
            ([(headerRows[2][12]): getColumnName(tmpRow, 'innerCode')]),
            ([(headerRows[2][13]): getColumnName(tmpRow, 'unitCountryCode')]),
            ([(headerRows[2][14]): getColumnName(tmpRow, 'signPhis')]),
            ([(headerRows[2][15]): getColumnName(tmpRow, 'signTransaction')]),
            ([(headerRows[2][16]): getColumnName(tmpRow, 'countryCode2')]),
            ([(headerRows[2][17]): getColumnName(tmpRow, 'region1')]),
            ([(headerRows[2][18]): getColumnName(tmpRow, 'city1')]),
            ([(headerRows[2][19]): getColumnName(tmpRow, 'settlement1')]),
            ([(headerRows[2][20]): getColumnName(tmpRow, 'countryCode3')]),
            ([(headerRows[2][21]): getColumnName(tmpRow, 'region2')]),
            ([(headerRows[2][22]): getColumnName(tmpRow, 'city2')]),
            ([(headerRows[2][23]): getColumnName(tmpRow, 'settlement2')]),
            ([(headerRows[1][24]): getColumnName(tmpRow, 'conditionCode')]),
            ([(headerRows[1][25]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[1][26]): getColumnName(tmpRow, 'incomeSum')]),
            ([(headerRows[1][27]): getColumnName(tmpRow, 'consumptionSum')]),
            ([(headerRows[1][28]): getColumnName(tmpRow, 'priceOne')]),
            ([(headerRows[1][29]): getColumnName(tmpRow, 'totalNds')]),
            ([(headerRows[1][31]): getColumnName(tmpRow, 'transactionDate')]),
            ([(headerRows[3][0]) : 'гр. 1']),
            ([(headerRows[3][2]) : 'гр. 2.1']),
            ([(headerRows[3][3]) : 'гр. 2.2']),
            ([(headerRows[3][4]) : 'гр. 3']),
            ([(headerRows[3][5]) : 'гр. 4.1']),
            ([(headerRows[3][6]) : 'гр. 4.2']),
            ([(headerRows[3][7]) : 'гр. 5']),
            ([(headerRows[3][8]) : 'гр. 6']),
            ([(headerRows[3][9]) : 'гр. 7.1']),
            ([(headerRows[3][10]): 'гр. 7.2']),
            ([(headerRows[3][11]): 'гр. 8']),
            ([(headerRows[3][12]): 'гр. 9.1']),
            ([(headerRows[3][13]): 'гр. 9.2']),
            ([(headerRows[3][14]): 'гр. 10']),
            ([(headerRows[3][15]): 'гр. 11']),
            ([(headerRows[3][16]): 'гр. 12.1']),
            ([(headerRows[3][17]): 'гр. 12.2']),
            ([(headerRows[3][18]): 'гр. 12.3']),
            ([(headerRows[3][19]): 'гр. 12.4']),
            ([(headerRows[3][20]): 'гр. 13.1']),
            ([(headerRows[3][21]): 'гр. 13.2']),
            ([(headerRows[3][22]): 'гр. 13.3']),
            ([(headerRows[3][23]): 'гр. 13.4']),
            ([(headerRows[3][24]): 'гр. 14']),
            ([(headerRows[3][25]): 'гр. 15']),
            ([(headerRows[3][26]): 'гр. 16']),
            ([(headerRows[3][27]): 'гр. 17']),
            ([(headerRows[3][28]): 'гр. 18']),
            ([(headerRows[3][29]): 'гр. 19']),
            ([(headerRows[3][31]): 'гр. 20'])
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

    // столбец 2.1
    newRow.name = getRecordIdImport(9, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    def map = getRefBookValue(9, newRow.name)
    colIndex++

    // графа 2.2 Признак взаимозависимости
    newRow.dependence = getRecordIdImport(38, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
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
    newRow.contractNum = values[colIndex]
    colIndex++

    // графа 6
    newRow.contractDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7.1
    newRow.transactionNum = values[colIndex]
    colIndex++

    // графа 7.2 Вид срочной сделки
    newRow.dealType = getRecordIdImport(85, 'CONTRACT_TYPE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 8
    newRow.transactionDeliveryDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9.1
    newRow.innerCode = getRecordIdImport(17, 'INNER_CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 9.2
    newRow.unitCountryCode = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 10
    newRow.signPhis = getRecordIdImport(18, 'SIGN', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 11
    newRow.signTransaction = getRecordIdImport(38, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 12.1
    newRow.countryCode2 = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 12.2
    String code = values[colIndex]
    if (code.length() == 1) {    //для кодов 1, 2, 3...9
        code = "0".concat(code)
    }
    newRow.region1 = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 12.3
    newRow.city1 = values[colIndex]
    colIndex++

    // графа 12.4
    newRow.settlement1 = values[colIndex]
    colIndex++

    // графа 13.1
    newRow.countryCode3 = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 13.2
    code = values[colIndex]
    if (code.length() == 1) {    //для кодов 1, 2, 3...9
        code = "0".concat(code)
    }
    newRow.region2 = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 13.3
    newRow.city2 = values[colIndex]
    colIndex++

    // графа 13.4
    newRow.settlement2 = values[colIndex]
    colIndex++

    // графа 14
    newRow.conditionCode = getRecordIdImport(63, 'STRCODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 15..19
    ['count', 'incomeSum', 'consumptionSum', 'priceOne', 'totalNds'].each { alias ->
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    // графа fix
    colIndex++

    // графа 20
    newRow.transactionDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

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