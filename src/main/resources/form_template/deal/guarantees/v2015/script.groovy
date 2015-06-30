package form_template.deal.guarantees.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 3388 - Предоставление гарантий (13)
 * (похож на letter_of_credit "Предоставление инструментов торгового финансирования и непокрытых аккредитивов")
 *
 * formTemplateId=3388
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
def editableColumns = ['fullName', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'sum', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'inn', 'countryName', 'price', 'total']

// Группируемые атрибуты
@Field
def groupColumns = ['fullName', 'docNumber', 'docDate']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['fullName', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'sum', 'price', 'total', 'dealDoneDate']

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

        def docDateCell = row.getCell('docDate')
        def dealDateCell = row.getCell('dealDate')
        def sumCell = row.getCell('sum')
        def priceCell = row.getCell('price')
        def totalCell = row.getCell('total')
        def msgSum = sumCell.column.name

        // Корректность даты заключения сделки
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка цены
        if (priceCell.value != sumCell.value) {
            def msg = priceCell.column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg» должно быть равно значению графы «$msgSum»!")
        }

        // Проверка стоимости
        if (totalCell.value != sumCell.value) {
            def msg = totalCell.column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg» должно быть равно значению графы «$msgSum»!")
        }

        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка положительной суммы
        if (sumCell.value <= 0) {
            def msg1 = sumCell.column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть больше «0»!")
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
            if (row1.sum != row2.sum) {
                return getColumnName(row1, 'sum')
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

    // Сортировка
    sortRows(dataRows, groupColumns)

    for (row in dataRows) {
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Итого"
        row.total = row.sum
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

    newRow.getCell('itog').colSpan = 8
    newRow.getCell('fix').colSpan = 2
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))

    // Расчеты подитоговых значений
    def BigDecimal sumItg = 0, priceitg = 0, totalItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        def row = dataRows.get(j)
        def sum = row.sum
        def price = row.price
        def total = row.total

        sumItg += sum != null ? sum : 0
        priceitg += price != null ? price : 0
        totalItg += total != null ? total : 0
    }
    newRow.sum = sumItg
    newRow.price = priceitg
    newRow.total = totalItg

    return newRow
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def sep = ", "
    def StringBuilder builder = new StringBuilder()
    def map = getRefBookValue(9, row.fullName)
    if (map != null)
        builder.append(map.NAME.stringValue).append(sep)
    if (row.docNumber != null)
        builder.append(row.docNumber).append(sep)
    if (row.docDate != null)
        builder.append(formatDate(row.docDate, 'dd.MM.yyyy')).append(sep)

    def String retVal = builder.toString()
    if (retVal.length() < 2)
        return null
    return retVal.substring(0, retVal.length() - 2)
}

// TODO (Ramil Timerbaev)
// Получение импортируемых данных
void importData2() {
    def tmpRow = formData.createDataRow()
    def xml = getXML('Общая информация о контрагенте - юридическом лице', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 11, 3)

    def headerMapping = [
            (xml.row[1].cell[0]): getColumnName(tmpRow, 'rowNumber'),
            (xml.row[1].cell[2]): getColumnName(tmpRow, 'fullName'),
            (xml.row[1].cell[3]): getColumnName(tmpRow, 'inn'),
            (xml.row[1].cell[4]): getColumnName(tmpRow, 'countryName'),
            (xml.row[1].cell[5]): getColumnName(tmpRow, 'docNumber'),
            (xml.row[1].cell[6]): getColumnName(tmpRow, 'docDate'),
            (xml.row[1].cell[7]): getColumnName(tmpRow, 'dealNumber'),
            (xml.row[1].cell[8]): getColumnName(tmpRow, 'dealDate'),
            (xml.row[1].cell[9]): getColumnName(tmpRow, 'sum'),
            (xml.row[1].cell[10]): getColumnName(tmpRow, 'price'),
            (xml.row[1].cell[11]): getColumnName(tmpRow, 'total'),
            (xml.row[1].cell[13]): getColumnName(tmpRow, 'dealDoneDate'),
            (xml.row[2].cell[0]): 'гр. 1',
            (xml.row[2].cell[13]): 'гр. 12'
    ]
    (2..11).each{
        headerMapping.put(xml.row[2].cell[it], 'гр. ' + it)
    }
    checkHeaderEquals(headerMapping, logger)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
    }

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    def xmlIndexRow = -1
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
        if (row.cell[1].text() == "Подитог:") {
            continue
        }

        def newRow = formData.createStoreMessagingDataRow()
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

        // графа 2
        newRow.fullName = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, true)
        def map = getRefBookValue(9, newRow.fullName)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            formDataService.checkReferenceValue(9, row.cell[xmlIndexCol].text(), map.INN_KIO?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 4
        if (map != null) {
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)
            if (map != null) {
                def text = row.cell[xmlIndexCol].text()
                if (text != null) {
                    if (text.length() == 1) {    //для кодов 4, 8 и тд
                        text = "00".concat(text)
                    } else if (text.length() == 2) {    //для кодов 10, 12, 16, 20 и тд
                        text = "0".concat(text)
                    }
                }
                formDataService.checkReferenceValue(10, text, map.CODE?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            }
        }
        xmlIndexCol++

        // графа 5
        newRow.docNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 6
        newRow.docDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 7
        newRow.dealNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 8
        newRow.dealDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 9
        newRow.sum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 10
        newRow.price = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 11
        newRow.total = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа fix
        xmlIndexCol++

        // графа 12
        newRow.dealDoneDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 14
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
            (headerRows[1][0]) : getColumnName(tmpRow, 'rowNumber'),
            (headerRows[1][2]) : getColumnName(tmpRow, 'fullName'),
            (headerRows[1][3]) : getColumnName(tmpRow, 'inn'),
            (headerRows[1][4]) : getColumnName(tmpRow, 'countryName'),
            (headerRows[1][5]) : getColumnName(tmpRow, 'docNumber'),
            (headerRows[1][6]) : getColumnName(tmpRow, 'docDate'),
            (headerRows[1][7]) : getColumnName(tmpRow, 'dealNumber'),
            (headerRows[1][8]) : getColumnName(tmpRow, 'dealDate'),
            (headerRows[1][9]) : getColumnName(tmpRow, 'sum'),
            (headerRows[1][10]): getColumnName(tmpRow, 'price'),
            (headerRows[1][11]): getColumnName(tmpRow, 'total'),
            (headerRows[1][13]): getColumnName(tmpRow, 'dealDoneDate'),
            (headerRows[2][0]) : 'гр. 1',
            (headerRows[2][13]): 'гр. 12'
    ]
    (2..11).each{
        headerMapping.put(headerRows[2][it], 'гр. ' + it)
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
    newRow.fullName = getRecordIdImport(9, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, true)
    def map = getRefBookValue(9, newRow.fullName)
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(9, values[colIndex], map.INN_KIO?.stringValue, fileRowIndex, colIndex + colOffset, logger, true)
    }
    colIndex++

    // графа 4
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY?.referenceValue)
        if (map != null) {
            def text = values[colIndex]
            if (text != null) {
                if (text.length() == 1) {    //для кодов 4, 8 и тд
                    text = "00".concat(text)
                } else if (text.length() == 2) {    //для кодов 10, 12, 16, 20 и тд
                    text = "0".concat(text)
                }
            }
            formDataService.checkReferenceValue(10, text, map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, true)
        }
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
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.total = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа fix
    colIndex++

    // графа 12
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

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