package form_template.deal.nondeliverable.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 3392 - Беспоставочные срочные сделки (17)
 *
 * formTemplateId=3392
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
        formDataService.consolidationSimple(formData, logger)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
            formDataService.saveCachedDataRows(formData, logger)
        }
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
def editableColumns = ['name', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate',
                       'transactionType', 'incomeSum', 'consumptionSum', 'transactionDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'innKio', 'country', 'countryCode', 'price', 'cost']

// Группируемые атрибуты
@Field
def groupColumns = ['name', 'innKio', 'contractNum', 'contractDate', 'transactionType']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'contractNum', 'contractDate', 'transactionType', 'price', 'cost', 'transactionDate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
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

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('consumptionSum')
        def price = row.price
        def transactionDeliveryDate = row.transactionDeliveryDate
        def contractDate = row.contractDate
        def cost = row.cost
        def transactionDate = row.transactionDate
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name

        // Проверка доходов и расходов
        if (outcomeSumCell.value == null && incomeSumCell.value  == null) {
            rowError(logger, row, "Строка $rowNum: Графа «$msgIn» должна быть заполнена, если не заполнена графа «$msgOut»!")
        }

        // Корректность даты заключения сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = row.getCell('transactionDeliveryDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка доходов/расходов и стоимости
        def msgPrice = row.getCell('price').column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            if ((row.price ?: 0).abs() != (incomeSumCell.value - outcomeSumCell.value).abs())
                rowError(logger, row, "Строка $rowNum: Значение графы «$msgPrice» должно быть равно разнице значений граф «$msgIn» и «$msgOut» по модулю!")
        } else if (incomeSumCell.value != null) {
            if (row.price != incomeSumCell.value)
                rowError(logger, row, "Строка $rowNum: Значение графы «$msgPrice» должно быть равно значению графы «$msgIn»!")
        } else if (outcomeSumCell.value != null) {
            if (row.price != outcomeSumCell.value)
                rowError(logger, row, "Строка $rowNum: Значение графы «$msgPrice» должно быть равно значению графы «$msgOut»!")
        }

        // Проверка стоимости сделки
        if (cost != price) {
            def msg1 = row.getCell('cost').column.name
            def msg2 = row.getCell('price').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }

        // Корректность дат сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('transactionDeliveryDate').column.name
            rowError(logger, row, "Строка $rowNum: «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка положительной суммы
        if (incomeSumCell.value != null && incomeSumCell.value <= 0) {
            rowError(logger, row, "Строка $rowNum: Значение графы «$msgIn» должно быть больше «0»!")
        }
        if (outcomeSumCell.value != null && outcomeSumCell.value <= 0) {
            rowError(logger, row, "Строка $rowNum: Значение графы «$msgOut» должно быть больше «0»!")
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
            if (row1.price != row2.price) {
                return getColumnName(row1, 'price')
            }
            if (row1.cost != row2.cost) {
                return getColumnName(row1, 'cost')
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

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Сортировка для группировки
    sortRows(dataRows, groupColumns)

    for (row in dataRows) {
        // Графы 13 и 14 из 11 и 12
        incomeSum = row.incomeSum
        consumptionSum = row.consumptionSum

        if (incomeSum != null && consumptionSum == null) {
            row.price = incomeSum
            row.cost = row.price
        } else if (incomeSum == null && consumptionSum != null) {
            row.price = consumptionSum
            row.cost = row.price
        } else if (incomeSum != null && consumptionSum != null) {
            row.price = (consumptionSum - incomeSum).abs()
            row.cost = row.price
        }
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

    newRow.getCell('itog').colSpan = 12
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2

    // Расчеты подитоговых значений
    def BigDecimal priceItg = 0, costItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        def row = dataRows.get(j)
        def price = row.price
        def cost = row.cost

        priceItg += price != null ? price : 0
        costItg += cost != null ? cost : 0
    }
    newRow.price = priceItg
    newRow.cost = costItg

    return newRow
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def sep = ", "
    StringBuilder builder = new StringBuilder()
    def map = getRefBookValue(9, row.name)
    if (map != null)
        builder.append(map.NAME?.stringValue).append(sep)
    if (row.contractNum != null)
        builder.append(row.contractNum).append(sep)
    if (row.contractDate != null)
        builder.append(row.contractDate).append(sep)
    if (row.transactionType != null)
        builder.append(row.transactionType).append(sep)

    def String retVal = builder.toString()
    if (retVal.length() < 2)
        return null
    return retVal.substring(0, retVal.length() - 2)
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 17
    int HEADER_ROW_COUNT = 3
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
        return;
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
            (headerRows[1][2]) : getColumnName(tmpRow, 'name'),
            (headerRows[1][3]) : getColumnName(tmpRow, 'innKio'),
            (headerRows[1][4]) : getColumnName(tmpRow, 'country'),
            (headerRows[1][5]) : getColumnName(tmpRow, 'countryCode'),
            (headerRows[1][6]) : getColumnName(tmpRow, 'contractNum'),
            (headerRows[1][7]) : getColumnName(tmpRow, 'contractDate'),
            (headerRows[1][8]) : getColumnName(tmpRow, 'transactionNum'),
            (headerRows[1][9]) : getColumnName(tmpRow, 'transactionDeliveryDate'),
            (headerRows[1][10]): getColumnName(tmpRow, 'transactionType'),
            (headerRows[1][11]): getColumnName(tmpRow, 'incomeSum'),
            (headerRows[1][12]): getColumnName(tmpRow, 'consumptionSum'),
            (headerRows[1][13]): getColumnName(tmpRow, 'price'),
            (headerRows[1][14]): getColumnName(tmpRow, 'cost'),
            (headerRows[1][16]): getColumnName(tmpRow, 'transactionDate'),
            (headerRows[2][2]) : 'гр. 2',
            (headerRows[2][3]) : 'гр. 3',
            (headerRows[2][4]) : 'гр. 4.1',
            (headerRows[2][5]) : 'гр. 4.2',
            (headerRows[2][16]): 'гр. 14'
    ]
    (6..14).each {
        headerMapping.put(headerRows[2][it], 'гр. ' + (it - 1))
    }
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

    // графа 2
    newRow.name = getRecordIdImport(9, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, true)
    def map = getRefBookValue(9, newRow.name)
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(9, values[colIndex], map.INN_KIO?.stringValue, fileRowIndex, colIndex + colOffset, logger, true)
    }
    colIndex++

    // графа 4.1
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY?.referenceValue)
        if (map != null) {
            formDataService.checkReferenceValue(10, values[colIndex], map.NAME?.stringValue, fileRowIndex, colIndex + colOffset, logger, true)
        }
    }
    colIndex++

    // графа 4.2
    if (map != null) {
        formDataService.checkReferenceValue(10, values[colIndex], map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, true)
    }
    colIndex++

    // графа 5
    newRow.contractNum = values[colIndex]
    colIndex++

    // графа 6
    newRow.contractDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.transactionNum = values[colIndex]
    colIndex++

    // графа 8
    newRow.transactionDeliveryDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.transactionType = values[colIndex]
    colIndex++

    // графа 10..13
    ['incomeSum', 'consumptionSum', 'price', 'cost'].each { alias ->
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    // графа fix
    colIndex++

    // графа 14
    newRow.transactionDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
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