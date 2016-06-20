package form_template.income.rnu61_1.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.time.TimeCategory
import groovy.transform.Field

import java.math.RoundingMode
import java.text.SimpleDateFormat

/**
 * Форма "(РНУ-61) Регистр налогового учёта расходов по процентным векселям ПАО Сбербанк,
 * учёт которых требует применения метода начисления (с 9 месяцев 2015)"
 * formTypeId=422 (352 у старого макета)
 *
 * графа 1  - rowNumber
 * графа 2  - billNumber
 * графа 3  - creationDate
 * графа 4  - nominal
 * графа 5  - currencyCode
 * графа 6  - rateBRBill
 * графа 7  - rateBROperation
 * графа 8  - paymentStart
 * графа 9  - paymentEnd
 * графа 10 - interestRate
 * графа 11 - operationDate
 * графа 12 - sum70606
 * графа 13 - sumLimit
 * графа 14 - percAdjustment
 *
 * @author akadyrgulov
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
        def cols = isBalancePeriod() ? (allColumns - 'rowNumber') : editableColumns
        def autoColumns = isBalancePeriod() ? ['rowNumber'] : autoFillColumns
        formDataService.addRow(formData, currentDataRow, cols, autoColumns)
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
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
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
def editableColumns = ['billNumber', 'creationDate', 'nominal', 'currencyCode', 'paymentStart', 'paymentEnd',
                       'interestRate', 'operationDate', 'sum70606']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'rateBRBill', 'rateBROperation', 'sumLimit', 'percAdjustment']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['billNumber', 'creationDate', 'nominal', 'currencyCode', 'rateBRBill',
                       'rateBROperation', 'paymentStart', 'paymentEnd', 'interestRate', 'operationDate',
                       'percAdjustment']

@Field
def allColumns = ['rowNumber', 'fix', 'billNumber', 'creationDate', 'nominal', 'currencyCode', 'rateBRBill',
                  'rateBROperation', 'paymentStart', 'paymentEnd', 'interestRate', 'operationDate', 'sum70606', 'sumLimit',
                  'percAdjustment']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['percAdjustment']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

@Field
def isBalancePeriod = null

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
                def Date date, boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date, rowIndex,
            cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
def calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // Начальная дата отчетного периода
    def reportDateStart = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def daysOfYear = getCountDays(reportDateStart)

    // Удаление итогов
    deleteAllAliased(dataRows)

    if (!dataRows.isEmpty()) {
        for (def row in dataRows) {
            // графа 6
            row.rateBRBill = calc6and7(row.currencyCode, row.creationDate)
            // графа 7
            row.rateBROperation = calc6and7(row.currencyCode, row.operationDate)
            // графа 13
            row.sumLimit = calc13(row, daysOfYear)
            // графа 14
            row.percAdjustment = calc14(row)
        }
    }
    // Добавление итогов
    dataRows.add(getTotalRow(dataRows))
}

// Расчет итоговой строки
def getTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Ресчет графы 6 и 7
def BigDecimal calc6and7(def currencyCode, def date) {
    if (currencyCode != null && date != null) {
        def rate = 1
        if (!isRubleCurrency(currencyCode)) {
            rate = getRate(date, currencyCode)
        }
        return rate
    } else {
        return null
    }
}

// Ресчет графы 13
def BigDecimal calc13(def DataRow<Cell> row, def daysOfYear) {
    def daysOfYear3 = null as Integer
    def daysOfYear9 = null as Integer
    def diffYear = false // Признак разницы в годах между графами 3 и 9
    def t1 = 0 as Integer
    def t2 = 0 as Integer
    if (row.creationDate != null && row.paymentEnd != null) {
        daysOfYear3 = getCountDays(row.creationDate)
        daysOfYear9 = getCountDays(row.paymentEnd)
        def c3 = Calendar.getInstance()
        c3.setTime(row.creationDate)
        def c9 = Calendar.getInstance()
        c9.setTime(row.paymentEnd)
        diffYear = c3.get(Calendar.YEAR) != c9.get(Calendar.YEAR)
        if (diffYear) {
            if (row.paymentEnd > row.creationDate) {
                c3.set(Calendar.DAY_OF_MONTH, 31)
                c3.set(Calendar.MONTH, 12)
                t1 = TimeCategory.minus(c3.getTime(), row.creationDate).getDays()
                c9.set(Calendar.DAY_OF_MONTH, 1)
                c9.set(Calendar.MONTH, 1)
                t2 = TimeCategory.minus(row.paymentEnd, c3.getTime()).getDays()
            } else {
                c9.set(Calendar.DAY_OF_MONTH, 31)
                c9.set(Calendar.MONTH, 12)
                t1 = TimeCategory.minus(row.creationDate, c9.getTime()).getDays()
                c3.set(Calendar.DAY_OF_MONTH, 1)
                c3.set(Calendar.MONTH, 1)
                t2 = TimeCategory.minus(c9.getTime(), row.paymentEnd).getDays()
            }
        }
    }
    // Если «Графа 3» и «Графа 9» принадлежат разным годам
    // и продолжительность каждого года разная (в одном 365 дней, в другом 366)
    if (daysOfYear3 != null && daysOfYear9 != null && diffYear && daysOfYear3 != daysOfYear9) {
        if (row.nominal == null || row.interestRate == null) {
            return null
        }
        if (daysOfYear3 == 365) {
            // Если первый год содержит 365 дней
            // «Графа 13» = («Графа 4» * «Графа 10» * ( T1 - 1)) / 365*100 +(«Графа 4» * «Графа 10» * T2) / 366*100
            return ((row.nominal * row.interestRate * (t1 - 1)) / 36500 + (row.nominal * row.interestRate * t2 / 36600)).setScale(2, RoundingMode.HALF_UP)
        } else {
            // Если первый год содержит 366 дней
            // «Графа 13» = («Графа 4» * «Графы 10» * ( T2 - 1)) / 365*100 +(«Графа 4» * «Графы 10» * T1) / 365*100
            return ((row.nominal * row.interestRate * (t2 - 1)) / 36500 + (row.nominal * row.interestRate * t1 / 36500)).setScale(2, RoundingMode.HALF_UP)
        }
    } else {
        if (row.sum70606 == null && isRubleCurrency(row.currencyCode)) {
            // Если «Графа 12» не заполнена и «Графа 5»= 810 или 643
            if (row.paymentEnd == null || row.operationDate == null) {
                return null
            }
            if (row.paymentEnd > row.operationDate) {
                // Если «Графа 11» < «Графа 9»
                if (row.nominal == null || row.interestRate == null
                        || row.operationDate == null || row.creationDate == null
                        || row.rateBROperation == null) {
                    return null
                }
                // «Графа 13» = («Графа 4» * «Графа 10» / 100 * («Графа 11» - «Графа 3») / 365 (366)),
                // с округлением до двух знаков после запятой по правилам округления * «Графа 7»
                //TODO деление и округление
                return (row.nominal * row.interestRate / 100 * TimeCategory.minus(row.operationDate, row.creationDate).getDays() / daysOfYear * row.rateBROperation)
                        .setScale(2, RoundingMode.HALF_UP)

            } else if (row.paymentEnd < row.operationDate) {
                // Если «Графа 11» > «графы 9»
                if (row.nominal == null || row.interestRate == null
                        || row.paymentEnd == null || row.creationDate == null
                        || row.rateBROperation == null) {
                    return null
                }
                // «Графа 13» = («Графа 4» * «Графа 10» / 100 * («Графа 9» - «Графа 3») / 365 (366)),
                // с округлением до двух знаков после запятой по правилам округления * «Графа 7»
                //TODO деление и округление
                return (row.nominal * row.interestRate / 100 * TimeCategory.minus(row.paymentEnd, row.creationDate).getDays() / daysOfYear * row.rateBROperation)
                        .setScale(2, RoundingMode.HALF_UP)
            }
        }
    }
    return null
}

// Ресчет графы 14
def BigDecimal calc14(def row) {
    BigDecimal temp = null
    if (row.sum70606 != null) {
        if (row.sumLimit != null && row.sum70606 > row.sumLimit) {
            temp = row.sum70606 - row.sumLimit
        }
    } else if (row.nominal != null && row.rateBRBill != null && row.rateBROperation != null) {
        temp = row.nominal * (row.rateBRBill - row.rateBROperation)
    }
    return temp?.setScale(2, RoundingMode.HALF_UP)
}
// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    // алиасы графов для арифметической проверки (графа 8, 9, 12-15)
    def arithmeticCheckAlias = ['rateBRBill', 'rateBROperation', 'sumLimit', 'percAdjustment']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    // Инвентарные номера
    def List<String> invList = new ArrayList<String>()
    // Отчетная дата
    def reportDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    //Начальная дата отчетного периода
    def reportDateStart = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def daysOfYear = getCountDays(reportDateStart)

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        // Проверка на уникальность поля «инвентарный номер»
        if (invList.contains(row.billNumber)) {
            loggerError(row, errorMsg + "Инвентарный номер не уникальный!")
        } else {
            invList.add(row.billNumber)
        }

        // 2. Проверка даты совершения операции и границ отчетного периода
        if (row.operationDate < reportDateStart || row.operationDate > reportDate) {
            loggerError(row, errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }

        // 4. Проверка на нулевые значения
        if (row.sum70606 == 0 && row.sumLimit == 0 && row.percAdjustment == 0) {
            loggerError(row, errorMsg + "Все суммы по операции нулевые!")
        }

        // 5. Арифметические проверки
        needValue['rateBRBill'] = calc6and7(row.currencyCode, row.creationDate)
        needValue['rateBROperation'] = calc6and7(row.currencyCode, row.operationDate)
        needValue['sumLimit'] = calc13(row, daysOfYear)
        needValue['percAdjustment'] = calc14(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, !isBalancePeriod())
    }

    // 5. Арифметические проверки расчета итоговой строки
    checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod())
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode) {
    return currencyCode != null ? (getRefBookValue(15, currencyCode)?.CODE?.stringValue in ['810', '643']) : false
}

// Получить курс банка России на указанную дату.
def getRate(def Date date, def value) {
    def res = refBookFactory.getDataProvider(22).getRecords((date ?: getReportPeriodEndDate()), null, "CODE_NUMBER = $value", null);
    if (res.getRecords().isEmpty()) {
        SimpleDateFormat dateFormat = new SimpleDateFormat('dd.MM.yyyy')
        throw new ServiceException("В справочнике \"Курсы Валют\" не обнаружена строка для валюты \"${getRefBookValue(15, value)?.NAME?.stringValue}\" на дату \"${dateFormat.format(date)}\"")
    } else {
        return res.getRecords().get(0)?.RATE?.numberValue
    }
}

def getCountDays(def Date date) {
    def Calendar calendar = Calendar.getInstance()
    calendar.setTime(date)
    return (new GregorianCalendar()).isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365
}

def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return reportPeriodEndDate
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 14, 1)
    addTransportData(xml)

    // TODO (Ramil Timerbaev) возможно надо поменять на общее сообщение TRANSPORT_FILE_SUM_ERROR
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRows = formDataService.getDataRowHelper(formData).allCached
        checkTotalSum(dataRows, totalColumns, logger, false)
    }
}

void addTransportData(def xml) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        def cols = isBalancePeriod() ? (allColumns - 'rowNumber') : editableColumns
        def autoColumns = isBalancePeriod() ? ['rowNumber'] : autoFillColumns
        cols.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 2
        newRow.billNumber = row.cell[2].text()
        // графа 3
        newRow.creationDate = parseDate(row.cell[3].text(), "dd.MM.yyyy", rnuIndexRow, 3 + colOffset, logger, true)
        // графа 4
        newRow.nominal = parseNumber(row.cell[4].text(), rnuIndexRow, 4 + colOffset, logger, true)
        // графа 5
        newRow.currencyCode = getRecordIdImport(15, 'CODE', row.cell[5].text(), rnuIndexRow, 5 + colOffset, false)
        // графа 6
        newRow.rateBRBill = parseNumber(row.cell[6].text(), rnuIndexRow, 6 + colOffset, logger, true)
        // графа 7
        newRow.rateBROperation = parseNumber(row.cell[7].text(), rnuIndexRow, 7 + colOffset, logger, true)
        // графа 8
        newRow.paymentStart = parseDate(row.cell[8].text(), "dd.MM.yyyy", rnuIndexRow, 8 + colOffset, logger, true)
        // графа 9
        newRow.paymentEnd = parseDate(row.cell[9].text(), "dd.MM.yyyy", rnuIndexRow, 9 + colOffset, logger, true)
        // графа 10
        newRow.interestRate = parseNumber(row.cell[10].text(), rnuIndexRow, 10 + colOffset, logger, true)
        // графа 11
        newRow.operationDate = parseDate(row.cell[11].text(), "dd.MM.yyyy", rnuIndexRow, 11 + colOffset, logger, true)
        // графа 12
        newRow.sum70606 = parseNumber(row.cell[12].text(), rnuIndexRow, 12 + colOffset, logger, true)
        // графа 13
        newRow.sumLimit = parseNumber(row.cell[13].text(), rnuIndexRow, 13 + colOffset, logger, true)
        // графа 14
        newRow.percAdjustment = parseNumber(row.cell[14].text(), rnuIndexRow, 14 + colOffset, logger, true)

        rows.add(newRow)
    }

    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()
        total.setAlias('total')
        total.fix = 'Итого'
        total.getCell('fix').colSpan = 2
        allColumns.each {
            total.getCell(it).setStyleAlias('Контрольные суммы')
        }

        // графа 14
        total.percAdjustment = parseNumber(row.cell[14].text(), rnuIndexRow, 14 + colOffset, logger, true)

        rows.add(total)
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
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
    int COLUMN_COUNT = 15
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('rowNumber').column.name
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

    def totalRow = getTotalRow(rows)
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
            ([(headerRows[0][0]): tmpRow.getCell('rowNumber').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('billNumber').column.name]),
            ([(headerRows[0][3]): tmpRow.getCell('creationDate').column.name]),
            ([(headerRows[0][4]): tmpRow.getCell('nominal').column.name]),
            ([(headerRows[0][5]): tmpRow.getCell('currencyCode').column.name]),
            ([(headerRows[0][6]): 'Курс Банка России']),
            ([(headerRows[1][6]): 'на дату составления векселя']),
            ([(headerRows[1][7]): 'на дату совершения операции']),
            ([(headerRows[0][8]): tmpRow.getCell('paymentStart').column.name]),
            ([(headerRows[0][9]): tmpRow.getCell('paymentEnd').column.name]),
            ([(headerRows[0][10]): tmpRow.getCell('interestRate').column.name]),
            ([(headerRows[0][11]): tmpRow.getCell('operationDate').column.name]),
            ([(headerRows[0][12]): tmpRow.getCell('sum70606').column.name]),
            ([(headerRows[0][13]): tmpRow.getCell('sumLimit').column.name]),
            ([(headerRows[0][14]): tmpRow.getCell('percAdjustment').column.name]),
            ([(headerRows[2][0]): '1'])
    ]
    (2..14).each { index ->
        headerMapping.add(([(headerRows[2][index]): index.toString()]))
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
    def cols = isBalancePeriod() ? (allColumns - 'rowNumber') : editableColumns
    def autoColumns = isBalancePeriod() ? ['rowNumber'] : autoFillColumns
    cols.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    // графа 2
    def colIndex = 2
    newRow.billNumber = values[colIndex]

    // графа 3
    colIndex++
    newRow.creationDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 4
    colIndex++
    newRow.nominal = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex++
    newRow.currencyCode = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 6
    colIndex++
    newRow.rateBRBill = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7
    colIndex++
    newRow.rateBROperation = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 8
    colIndex++
    newRow.paymentStart = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9
    colIndex++
    newRow.paymentEnd = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 10
    colIndex++
    newRow.interestRate = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 11
    colIndex++
    newRow.operationDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 12..14
    ['sum70606', 'sumLimit', 'percAdjustment'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}