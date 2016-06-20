package form_template.income.rnu38_1.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Форма "РНУ-38.1" "Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 1".
 * formTemplateId=334
 *
 * @author ivildanov
 *
 * 1  -  series
 * 2  -  amount
 * 3  -  shortPositionDate
 * 4  -  maturityDate
 * 5  -  incomeCurrentCoupon
 * 6  -  currentPeriod
 * 7  -  incomePrev
 * 8  -  incomeShortPosition
 * 9  -  totalPercIncome
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

// Все атрибуты
@Field
def allColumns = ['series', 'amount', 'shortPositionDate', 'maturityDate', 'incomeCurrentCoupon', 'currentPeriod', 'incomePrev', 'incomeShortPosition', 'totalPercIncome']

// Редактируемые атрибуты (графа 1..6)
@Field
def editableColumns = ['series', 'amount', 'shortPositionDate', 'maturityDate', 'incomeCurrentCoupon', 'currentPeriod']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты (графа 1)
@Field
def groupColumns = ['series']

// Проверяемые на пустые значения атрибуты (графа 1..6, 9)
@Field
def nonEmptyColumns = ['series', 'amount', 'shortPositionDate', 'maturityDate', 'incomeCurrentCoupon', 'currentPeriod', 'totalPercIncome']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 2, 7..9)
@Field
def totalColumns = ['amount', 'incomePrev', 'incomeShortPosition', 'totalPercIncome']

// Дата окончания отчетного периода
@Field
def endDate = null

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

/** Получить дату по строковому представлению (формата дд.ММ.гггг) */
def getDate(def value, def indexRow, def indexCol) {
    return parseDate(value, 'dd.MM.yyyy', indexRow, indexCol + 1, logger, true)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // алиасы графов для арифметической проверки (графа 7..9)
    def arithmeticCheckAlias = ['incomePrev', 'incomeShortPosition', 'totalPercIncome']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    // отчетная дата
    def reportDay = reportPeriodService.getMonthReportDate(formData.reportPeriodId, formData.periodOrder)?.time
    // последний день месяца
    def lastDay = getMonthEndDate()

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        //  2. Проверка даты открытия короткой позиции
        if (row.shortPositionDate > reportDay) {
            rowError(logger, row, errorMsg + 'Неверно указана дата приобретения (открытия короткой позиции)!')
        }

        // 3. Проверка даты погашения
        if (row.maturityDate > reportDay) {
            rowError(logger, row, errorMsg + 'Неверно указана дата погашения предыдущего купона!')
        }

        // 4. Арифметические проверки графы 7..9
        needValue['incomePrev'] = calc7(row, lastDay)
        needValue['incomeShortPosition'] = calc8(row, lastDay)
        needValue['totalPercIncome'] = calc9(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
    }

    // 4. Проверка итоговых значений по всей форме
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    // последний день месяца
    def lastDay = getMonthEndDate()
    for (def row : dataRows) {
        // графа 7
        row.incomePrev = calc7(row, lastDay)
        // графа 8
        row.incomeShortPosition = calc8(row, lastDay)
        // графа 9
        row.totalPercIncome = calc9(row)
    }
    // добавить строки "итого"
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
}

/**
 * Посчитать значение графы 7.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def calc7(def row, def lastDay) {
    if (row.maturityDate == null || row.shortPositionDate == null || row.incomeCurrentCoupon == null ||
            lastDay == null || row.currentPeriod == null || row.currentPeriod == 0 || row.amount == null) {
        return null
    }
    if (row.maturityDate > row.shortPositionDate) {
        def tmp = roundValue((row.incomeCurrentCoupon * (lastDay - row.maturityDate) / row.currentPeriod), 2)
        return roundValue(tmp * row.amount, 2)
    }
    return null
}

/**
 * Посчитать значение графы 8.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def calc8(def row, def lastDay) {
    if (row.maturityDate == null || row.shortPositionDate == null || row.incomeCurrentCoupon == null ||
            lastDay == null || row.currentPeriod == null || row.currentPeriod == 0 || row.amount == null) {
        return null
    }
    if (row.maturityDate <= row.shortPositionDate) {
        def tmp = roundValue((row.incomeCurrentCoupon * (lastDay - row.shortPositionDate) / row.currentPeriod), 2)
        return roundValue(tmp * row.amount, 2)
    }
    return null
}

def calc9(def row) {
    return roundValue((row.incomePrev ?: 0) + (row.incomeShortPosition ?: 0), 2)
}

/** Получить итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.series = 'Итого'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

def getMonthEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 9, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def rows = []

    def int rnuIndexRow = 2
    def int colOffset = 1

    for (def row : xml.row) {
        rnuIndexRow++

        def rnuIndexCol
        def newRow = formData.createDataRow()
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).styleAlias = 'Редактируемая'
        }

        // графа 1
        newRow.series = row.cell[1].text()
        // графа 2
        rnuIndexCol = 2
        newRow.amount = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 3
        rnuIndexCol = 3
        newRow.shortPositionDate = getDate(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 4
        rnuIndexCol = 4
        newRow.maturityDate = getDate(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 5..9
        ['incomeCurrentCoupon', 'currentPeriod', 'incomePrev', 'incomeShortPosition', 'totalPercIncome'].each { alias ->
            rnuIndexCol++
            newRow[alias] = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        }

        rows.add(newRow)
    }

    def totalRow = getTotalRow(rows)
    rows.add(totalRow)

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 2
        def rnuIndexCol = 2
        total.amount = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 7
        rnuIndexCol = 7
        total.incomePrev = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 8
        rnuIndexCol = 8
        total.incomeShortPosition = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 9
        rnuIndexCol = 9
        total.totalPercIncome = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        def colIndexMap = ['amount': 2, 'incomePrev': 7, 'incomeShortPosition': 8, 'totalPercIncome': 9]
        for (def alias : totalColumns) {
            def v1 = total.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
            }
        }
        // задать итоговой строке нф значения из итоговой строки тф
        totalColumns.each { alias ->
            totalRow[alias] = total[alias]
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
        // очистить итоги
        totalColumns.each { alias ->
            totalRow[alias] = null
        }
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, getDataRow(dataRows, 'total'), null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 9
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = tmpRow.getCell('series').column.name
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

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
    def totalRowFromFile = null

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        rowIndex++
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Итого") {
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // итоговая строка
    def totalRow = getTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    // сравнение итогов
    compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
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
            ([(headerRows[0][0]): tmpRow.getCell('series').column.name]),
            ([(headerRows[0][1]): tmpRow.getCell('amount').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('shortPositionDate').column.name]),
            ([(headerRows[0][3]): tmpRow.getCell('maturityDate').column.name]),
            ([(headerRows[0][4]): tmpRow.getCell('incomeCurrentCoupon').column.name]),
            ([(headerRows[0][5]): tmpRow.getCell('currentPeriod').column.name]),
            ([(headerRows[0][6]): tmpRow.getCell('incomePrev').column.name]),
            ([(headerRows[0][7]): tmpRow.getCell('incomeShortPosition').column.name]),
            ([(headerRows[0][8]): tmpRow.getCell('totalPercIncome').column.name])
    ]
    (1..9).each {
        headerMapping.add(([(headerRows[1][it - 1]): it.toString()]))
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

    // графа 1
    def colIndex = 0
    newRow.series = values[colIndex]

    // графа 2
    colIndex++
    newRow.amount = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 3
    colIndex++
    newRow.shortPositionDate = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 4
    colIndex++
    newRow.maturityDate = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 5..9
    ['incomeCurrentCoupon', 'currentPeriod', 'incomePrev', 'incomeShortPosition', 'totalPercIncome'].each { alias ->
        colIndex++
        newRow[alias] = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    }

    return newRow
}