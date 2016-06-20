package form_template.deal.rent_provision.v2013

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 2376 - Предоставление нежилых помещений в аренду (1)
 *
 * formTemplateId=2376
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
def editableColumns = ['jurName', 'incomeBankSum', 'outcomeBankSum', 'contractNum', 'contractDate', 'country',
                       'region', 'city', 'settlement', 'count', 'price', 'transactionDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'innKio', 'countryCode', 'cost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['jurName', 'contractNum', 'contractDate', 'country', 'count', 'price', 'cost', 'transactionDate']

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

        checkNonEmptyColumns(row, rowNum, ['contractNum', 'contractDate'], logger, true)
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns - ['contractNum', 'contractDate'], logger, false)

        def cost = row.cost
        def incomeBankSum = row.incomeBankSum
        def outcomeBankSum = row.outcomeBankSum
        def transactionDate = row.transactionDate
        def contractDate = row.contractDate

        def bankSum = null
        def msgBankSum = getColumnName(row, 'incomeBankSum')
        if (incomeBankSum != null && outcomeBankSum == null) {
            bankSum = incomeBankSum
        }
        if (outcomeBankSum != null && incomeBankSum == null) {
            bankSum = outcomeBankSum
            msgBankSum = getColumnName(row, 'outcomeBankSum')
        }

        // Наименования колонок
        def contractDateName = getColumnName(row, 'contractDate')
        def transactionDateName = getColumnName(row, 'transactionDate')
        def costName = getColumnName(row, 'cost')

        // Проверка одновременного заполнения граф 5 и 6
        if (incomeBankSum != null && outcomeBankSum != null) {
            def msg1 = getColumnName(row, 'incomeBankSum')
            def msg2 = getColumnName(row, 'outcomeBankSum')
            rowError(logger, row, "Строка $rowNum: Графа «$msg1» не может быть заполнена одновременно с графой «$msg2»!")
        }

        // Проверка заполнения графы 5 или 6
        if (incomeBankSum == null && outcomeBankSum == null) {
            def msg1 = getColumnName(row, 'incomeBankSum')
            def msg2 = getColumnName(row, 'outcomeBankSum')
            rowError(logger, row, "Строка $rowNum: Графа «$msg1» должна быть заполнена если не заполнена графа «$msg2»!")
        }

        // Проверка стоимости по графе 5
        // и
        // Проверка стоимости по графе 6
        if ((incomeBankSum != null && outcomeBankSum == null || incomeBankSum == null && outcomeBankSum != null)
                && cost != bankSum) {
            rowError(logger, row, "Строка $rowNum: Значение графы «$costName» должно быть равно значению графы «$msgBankSum»!")
        }

        // Корректность даты совершения сделки
        if (transactionDate < contractDate) {
            rowError(logger, row, "Строка $rowNum: Значение графы «$transactionDateName» должно быть не меньше значения графы «$contractDateName»!")
        }

        // Проверка заполненности одного из атрибутов
        if (row.city != null && !row.city.toString().isEmpty() && row.settlement != null && !row.settlement.toString().isEmpty()) {
            def cityName = getColumnName(row, 'city')
            def settleName = getColumnName(row, 'settlement')
            rowError(logger, row, "Строка $rowNum: Графа «$cityName» не может быть заполнена одновременно с графой «$settleName»!")
        }

        // Проверка заполнения региона
        def country = getRefBookValue(10, row.country)?.CODE?.stringValue
        if (country != null) {
            def regionName = getColumnName(row, 'region')
            def countryName = getColumnName(row, 'country')
            if (country == '643' && row.region == null) {
                rowWarning(logger, row, "Строка $rowNum: Графа «$regionName» должна быть заполнена, т.к. в графе «$countryName» указан код 643!")
            } else if (country != '643' && row.region != null) {
                rowWarning(logger, row, "Строка $rowNum: Графа «$regionName» не должна быть заполнена, т.к. в графе «$countryName» указан код, отличный от 643!")
            }
        }

        // Проверка количества
        if (row.count == 0) {
            def countName = getColumnName(row, 'count')
            rowError(logger, row, "Строка $rowNum: Графа «$countName» не может содержать значение 0!")
        }

        // Проверка диапазона дат
        if (row.contractDate) {
            checkDateValid(logger, row, 'contractDate', row.contractDate, true)
        }
        if (row.transactionDate) {
            checkDateValid(logger, row, 'transactionDate', row.transactionDate, true)
        }
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (row in dataRows) {
        def bankSum = null
        if (row.incomeBankSum != null && row.outcomeBankSum == null)
            bankSum = row.incomeBankSum
        if (row.outcomeBankSum != null && row.incomeBankSum == null)
            bankSum = row.outcomeBankSum

        // Расчет поля "Стоимость"
        row.cost = bankSum
    }

    sortFormDataRows(false)
}

def BigDecimal calc13(def row, def bankSum) {
    if (bankSum != null && row.count != null && row.count != 0) {
        return (bankSum / row.count).setScale(0, RoundingMode.HALF_UP)
    }
    return null
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 16
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = 'Общая информация'
    String TABLE_END_VALUE = null

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
            (headerRows[0][0]) : 'Общая информация',
            (headerRows[0][4]) : 'Сведения о сделке',
            (headerRows[1][0]) : tmpRow.getCell('rowNum').column.name,
            (headerRows[1][1]) : tmpRow.getCell('jurName').column.name,
            (headerRows[1][2]) : tmpRow.getCell('innKio').column.name,
            (headerRows[1][3]) : tmpRow.getCell('countryCode').column.name,
            (headerRows[1][4]) : tmpRow.getCell('incomeBankSum').column.name,
            (headerRows[1][5]) : tmpRow.getCell('outcomeBankSum').column.name,
            (headerRows[1][6]) : tmpRow.getCell('contractNum').column.name,
            (headerRows[1][7]) : tmpRow.getCell('contractDate').column.name,
            (headerRows[1][8]) : 'Адрес местонахождения объекта недвижимости',
            (headerRows[1][12]): tmpRow.getCell('count').column.name,
            (headerRows[1][13]): tmpRow.getCell('price').column.name,
            (headerRows[1][14]): tmpRow.getCell('cost').column.name,
            (headerRows[1][15]): tmpRow.getCell('transactionDate').column.name,
            (headerRows[2][8]) : tmpRow.getCell('country').column.name,
            (headerRows[2][9]) : tmpRow.getCell('region').column.name,
            (headerRows[2][10]): tmpRow.getCell('city').column.name,
            (headerRows[2][11] ): tmpRow.getCell('settlement').column.name,
            (headerRows[3][0]) : 'гр. 1',
            (headerRows[3][1]) : 'гр. 2',
            (headerRows[3][2]) : 'гр. 3',
            (headerRows[3][3]) : 'гр. 4',
            (headerRows[3][4]) : 'гр. 5.1',
            (headerRows[3][5]) : 'гр. 5.2',
            (headerRows[3][6]) : 'гр. 6',
            (headerRows[3][7]) : 'гр. 7',
            (headerRows[3][8]) : 'гр. 8',
            (headerRows[3][9]) : 'гр. 9',
            (headerRows[3][10]): 'гр. 10',
            (headerRows[3][11]): 'гр. 11',
            (headerRows[3][12]): 'гр. 12',
            (headerRows[3][13]): 'гр. 13',
            (headerRows[3][14]): 'гр. 14',
            (headerRows[3][15]): 'гр. 15'
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

    def int colIndex = 1

    // графа 2
    newRow.jurName = getRecordIdImport(9, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    def map = getRefBookValue(9, newRow.jurName)
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(9, values[colIndex], map.INN_KIO?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY?.referenceValue)
        if (map != null) {
            formDataService.checkReferenceValue(10, values[colIndex], map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 5.1
    newRow.incomeBankSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 5.2
    newRow.outcomeBankSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 6
    newRow.contractNum = values[colIndex]
    colIndex++

    // графа 7
    newRow.contractDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 8
    newRow.country = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 9
    newRow.region = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 10
    newRow.city = values[colIndex]
    colIndex++

    // графа 11
    newRow.settlement = values[colIndex]
    colIndex++

    // графа 12
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 14
    colIndex++

    // графа 15
    newRow.transactionDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}