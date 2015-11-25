package form_template.income.rnu45.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Скрипт для РНУ-45
 * Форма "(РНУ-45) Регистр налогового учёта «ведомость начисленной амортизации по нематериальным активам»"  (341)
 * formTemplateId=341
 *
 * графа 1	- rowNumber
 * графа 2	- inventoryNumber
 * графа 3	- name
 * графа 4	- buyDate
 * графа 5	- usefulLife
 * графа 6	- expirationDate
 * графа 7	- startCost
 * графа 8	- depreciationRate
 * графа 9	- amortizationMonth
 * графа 10	- amortizationSinceYear
 * графа 11	- amortizationSinceUsed
 *
 * @author akadyrgulov
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        def columns = isMonthBalance() ? balanceEditableColumns : editableColumns
        def autoColumns = isMonthBalance() ? ['rowNumber'] : autoFillColumns
        formDataService.addRow(formData, currentDataRow, columns, autoColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationTotal(formData, logger, userInfo, ['total'])
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

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def editableColumns = ['inventoryNumber', 'name', 'buyDate', 'usefulLife', 'expirationDate', 'startCost']
@Field
def balanceEditableColumns = ['inventoryNumber', 'name', 'buyDate', 'usefulLife', 'expirationDate', 'startCost',
                              'depreciationRate', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['inventoryNumber', 'name', 'buyDate', 'usefulLife', 'expirationDate', 'startCost',
                       'depreciationRate', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['startCost', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'depreciationRate', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']

@Field
def isBalance = null
@Field
def startDate = null
@Field
def endDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                def Date date, boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date, rowIndex,
            cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    if (formData.kind == FormDataKind.PRIMARY && !isMonthBalance()) {
        formDataService.checkMonthlyFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder, true, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков и месяц первый в периоде.
def isMonthBalance() {
    if (isBalance == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        if (!departmentReportPeriod.isBalance() || formData.periodOrder == null) {
            isBalance = false
        } else {
            isBalance = (formData.periodOrder - 1) % 3 == 0
        }
    }
    return isBalance
}

//// Кастомные методы

@Field
def formDataPrev = null // Форма предыдущего месяца
@Field
def dataRowHelperPrev = null // DataRowHelper формы предыдущего месяца

// Получение формы предыдущего месяца
def getFormDataPrev() {
    if (formDataPrev == null) {
        formDataPrev = formDataService.getFormDataPrev(formData)
    }
    return formDataPrev
}

// Получение DataRowHelper формы предыдущего месяца
def getDataRowHelperPrev() {
    if (dataRowHelperPrev == null) {
        def formDataPrev = getFormDataPrev()
        if (formDataPrev != null) {
            dataRowHelperPrev = formDataService.getDataRowHelper(formDataPrev)
        }
    }
    return dataRowHelperPrev
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // получить строку "итого"
    def totalRow = getDataRow(dataRows, 'total')
    // очистить итоги
    totalColumns.each {
        totalRow[it] = null
    }

    // Удаление подитогов
    deleteAllAliased(dataRows)

    def endDate = getMonthEndDate()
    def dateStart = getMonthStartDate()

    def dataOld = null
    if (formData.kind == FormDataKind.PRIMARY) {
        dataOld = getFormDataPrev() != null ? getDataRowHelperPrev() : null
    }

    for (def row in dataRows) {
        if (formData.kind != FormDataKind.PRIMARY) {
            continue;
        }

        // графа 8
        row.depreciationRate = calc8(row)
        // графа 9
        row.amortizationMonth = calc9(row)
        // для граф 10 и 11
        prevValues = getPrev10and11(dataOld, row)
        // графа 10
        row.amortizationSinceYear = calc10(row, dateStart, endDate, prevValues[0])
        // графа 11
        row.amortizationSinceUsed = calc11(row, dateStart, endDate, prevValues[1])
    }
    // добавить строку "итого"
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
}

// Ресчет графы 8
def BigDecimal calc8(def row) {
    if (row.usefulLife == null || row.usefulLife == 0) {
        return null
    }
    return (100 / row.usefulLife).setScale(4, RoundingMode.HALF_UP)
}

// Ресчет графы 9
def BigDecimal calc9(def row) {
    if (row.startCost == null || row.depreciationRate == null) {
        return null
    }
    return (row.startCost * row.depreciationRate / 100).setScale(2, RoundingMode.HALF_UP)
}

// Ресчет графы 10
def BigDecimal calc10(def row, def dateStart, def dateEnd, def oldRow10) {
    Calendar buyDate = calc10and11(row)
    if (buyDate != null && dateStart != null && dateEnd != null && row.amortizationMonth != null)
        return row.amortizationMonth + ((buyDate.get(Calendar.MONTH) == Calendar.JANUARY || (buyDate.after(dateStart) && buyDate.before(dateEnd))) ? 0 : ((oldRow10 == null) ? 0 : oldRow10))
    return null
}

// Ресчет графы 11
def BigDecimal calc11(def row, def dateStart, def dateEnd, def oldRow11) {
    Calendar buyDate = calc10and11(row)
    if (buyDate != null && dateStart != null && dateEnd != null && row.amortizationMonth != null)
        return row.amortizationMonth + ((buyDate.after(dateStart) && buyDate.before(dateEnd)) ? 0 : ((oldRow11 == null) ? 0 : oldRow11))
    return null
}

// Общая часть ресчета граф 10 и 11
Calendar calc10and11(def row) {
    if (row.buyDate == null) {
        return null
    }
    Calendar buyDate = Calendar.getInstance()
    buyDate.setTime(row.buyDate)
    return buyDate
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    if (!dataRows.isEmpty()) {
        // Инвентарные номера
        def Set<String> invSet = new HashSet<String>()
        def inventoryNumbersOld = []
        def dataRowHelperOld = null
        if (formData.kind == FormDataKind.PRIMARY && !isMonthBalance()) {
            dataRowHelperOld = getFormDataPrev() != null ? getDataRowHelperPrev() : null
            if (dataRowHelperOld) {
                dataRowHelperOld.allSaved.each { row ->
                    inventoryNumbersOld.add(row.inventoryNumber)
                }
            }
        }

        def dateEnd = getMonthEndDate()
        def dateStart = getMonthStartDate()

        // алиасы графов для арифметической проверки
        def arithmeticCheckAlias = ['depreciationRate', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']
        // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
        def needValue = [:]

        for (def row in dataRows) {
            if (row.getAlias() != null) {
                continue
            }

            def index = row.getIndex()
            def errorMsg = "Строка $index: "

            // 1. Проверка на заполнение поля
            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isMonthBalance())

            // 2. Проверка на уникальность поля «инвентарный номер»
            if (invSet.contains(row.inventoryNumber)) {
                loggerError(row, errorMsg + "Инвентарный номер не уникальный!")
            } else {
                invSet.add(row.inventoryNumber)
            }

            // 3. Проверка на нулевые значения
            if (row.startCost == 0 && row.amortizationMonth == 0 && row.amortizationSinceYear == 0 && row.amortizationSinceUsed == 0) {
                loggerError(row, errorMsg + "Все суммы по операции нулевые!")
            }

            // 5. Арифметические проверки расчета неитоговых граф
            if (formData.kind == FormDataKind.PRIMARY) {
                needValue['depreciationRate'] = calc8(row)
                needValue['amortizationMonth'] = calc9(row)
                prevValues = getPrev10and11(dataRowHelperOld, row)
                needValue['amortizationSinceYear'] = calc10(row, dateStart, dateEnd, prevValues[0])
                needValue['amortizationSinceUsed'] = calc11(row, dateStart, dateEnd, prevValues[1])
                checkCalc(row, arithmeticCheckAlias, needValue, logger, !isMonthBalance())
            }
        }

        // 6. Арифметические проверки расчета итоговой строки
        checkTotalSum(dataRows, totalColumns, logger, !isMonthBalance())

        // 4. Проверки существования необходимых экземпляров форм
        if (formData.kind == FormDataKind.PRIMARY && !isMonthBalance()) {
            for (def inv in invSet) {
                if (!inventoryNumbersOld.contains(inv)) {
                    logger.warn('Отсутствуют данные за прошлые отчетные периоды!')
                    break
                }
            }
        }
    }
}

// Получить значение за предыдущий отчетный период для графы 10 и 11
def getPrev10and11(def dataOld, def row) {
    if (dataOld != null)
        for (def rowOld : dataOld.allSaved) {
            if (rowOld.inventoryNumber == row.inventoryNumber) {
                return [rowOld.amortizationSinceYear, rowOld.amortizationSinceUsed]
            }
        }
    return [null, null]
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 11, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }
        def columns = isMonthBalance() ? balanceEditableColumns : editableColumns
        columns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 2
        def xmlIndexCol = 2
        newRow.inventoryNumber = row.cell[xmlIndexCol].text()
        // графа 3
        xmlIndexCol = 3
        newRow.name = row.cell[xmlIndexCol].text()
        // графа 4
        xmlIndexCol = 4
        newRow.buyDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 5
        xmlIndexCol = 5
        newRow.usefulLife = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 6
        xmlIndexCol = 6
        newRow.expirationDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 7
        xmlIndexCol = 7
        newRow.startCost = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 8
        xmlIndexCol = 8
        newRow.depreciationRate = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 9
        xmlIndexCol = 9
        newRow.amortizationMonth = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 10
        xmlIndexCol = 10
        newRow.amortizationSinceYear = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 11
        xmlIndexCol = 11
        newRow.amortizationSinceUsed = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    def totalRow = getDataRow(dataRows, 'total')
    calcTotalSum(rows, totalRow, totalColumns)
    rows.add(totalRow)

    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow += 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // графа 7
        xmlIndexCol = 7
        total.startCost = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 9
        xmlIndexCol = 9
        total.amortizationMonth = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 10
        xmlIndexCol = 10
        total.amortizationSinceYear = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 11
        xmlIndexCol = 11
        total.amortizationSinceUsed = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        def colIndexMap = ['startCost': 7, 'amortizationMonth': 9, 'amortizationSinceYear': 10, 'amortizationSinceUsed': 11]
        for (def alias : totalColumns) {
            def v1 = total[alias]
            def v2 = totalRow[alias]
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

def getMonthStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return startDate
}

def getMonthEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

def loggerError(def row, def msg) {
    if (isMonthBalance()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

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
    int COLUMN_COUNT = 12
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

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

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def totalRow = getDataRow(formTemplate.rows, 'total')
    rows.add(totalRow)
    updateIndexes(rows)
    // сравнение итогов
    compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)

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
            ([(headerRows[0][0]) : getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[0][2]) : getColumnName(tmpRow, 'inventoryNumber')]),
            ([(headerRows[0][3]) : getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][4]) : getColumnName(tmpRow, 'buyDate')]),
            ([(headerRows[0][5]) : getColumnName(tmpRow, 'usefulLife')]),
            ([(headerRows[0][6]) : getColumnName(tmpRow, 'expirationDate')]),
            ([(headerRows[0][7]) : getColumnName(tmpRow, 'startCost')]),
            ([(headerRows[0][8]) : getColumnName(tmpRow, 'depreciationRate')]),
            ([(headerRows[0][9]) : getColumnName(tmpRow, 'amortizationMonth')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'amortizationSinceYear')]),
            ([(headerRows[0][11]): getColumnName(tmpRow, 'amortizationSinceUsed')]),
            ([(headerRows[1][0]) : '1'])
    ]
    (2..11).each {
        headerMapping.add(([(headerRows[1][it]): it.toString()]))
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
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    def columns = isMonthBalance() ? balanceEditableColumns : editableColumns
    columns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    // графа 2
    def colIndex = 2
    newRow.inventoryNumber = values[colIndex]

    // графа 3
    colIndex++
    newRow.name = values[colIndex]

    // графа 4
    colIndex++
    newRow.buyDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex++
    newRow.usefulLife = parseNumber(values[colIndex], fileRowIndex, 0 + colOffset, logger, true)

    // графа 6
    colIndex++
    newRow.expirationDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7..11
    ['startCost', 'depreciationRate', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}