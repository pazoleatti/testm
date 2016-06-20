package form_template.income.rnu55.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-55) Регистр налогового учёта процентного дохода по процентным векселям сторонних эмитентов".
 * formTypeId=348
 *
 * @author rtimerbaev
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
        def cols = isBalancePeriod() ? (allColumns - 'number') : editableColumns
        def autoColumns = isBalancePeriod() ? ['number'] : autoFillColumns
        formDataService.addRow(formData, currentDataRow, cols, autoColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow?.getAlias() == null) {
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
def editableColumns = ['bill', 'buyDate', 'currency', 'nominal', 'percent', 'implementationDate', 'percentInCurrency']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['bill', 'buyDate', 'currency', 'nominal', 'percent', 'sumIncomeinCurrency', 'sumIncomeinRuble']

// Атрибуты для итогов
@Field
def totalColumns = ['percentInRuble', 'sumIncomeinRuble']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number', 'percentInRuble', 'sumIncomeinCurrency', 'sumIncomeinRuble']

// Все атрибуты
@Field
def allColumns = ['number', 'bill', 'buyDate', 'currency', 'nominal', 'percent', 'implementationDate',
        'percentInCurrency', 'percentInRuble', 'sumIncomeinCurrency', 'sumIncomeinRuble']

@Field
def startDate = null
@Field
def endDate = null

/** Признак периода ввода остатков. */
@Field
def isBalancePeriod = null

//// Обертки методов

// Поиск записи в справочнике по значению (для расчетов)
def getRecord(def Long refBookId, def String alias, def String value, def int rowIndex, def String columnName,
              def Date date, boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, date,
            rowIndex, columnName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getEndDate(), rowIndex, colIndex, logger, required)
}

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    // Проверка только для первичных
    if (formData.kind == FormDataKind.PRIMARY && !isBalancePeriod()) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, formData.kind, formData.departmentId,
                formData.reportPeriodId, true, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def totalRow = getDataRow(dataRows, 'total')
    totalColumns.each {
        totalRow[it] = null
    }

    // Удаление итогов
    deleteAllAliased(dataRows)

    if (!dataRows.isEmpty()) {
        /** Количество дней в году. */
        def daysInYear = getCountDaysInYear(new Date())
        // Отчетная дата
        def reportDate = getReportDate()
        // Дата начала отчетного периода
        def startDate = getStartDate()

        for (def row in dataRows) {
            if (formData.kind != FormDataKind.PRIMARY) {
                continue
            }
            // графа 9
            row.percentInRuble = calc9(row)
            // графа 10
            row.sumIncomeinCurrency = calc10(row, startDate, reportDate, daysInYear)
            // графа 11
            row.sumIncomeinRuble = calc11(row, reportDate, startDate)
        }
    }

    // добавить строку "итого"
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
}

// Ресчет графы 9
def BigDecimal calc9(def row) {
    if (row.percentInCurrency != null && row.currency != null && row.implementationDate != null) {
        rate = 1
        if (!isRubleCurrency(row.currency)) {
            rate = getRate(row.implementationDate, row.currency)
        }
        return (row.percentInCurrency * rate).setScale(2, RoundingMode.HALF_UP)
    } else {
        return null
    }
}

// Ресчет графы 10
def BigDecimal calc10(def row, def startDate, def endDate, def daysInYear) {
    if (row.buyDate == null || startDate == null || endDate == null || row.nominal == null
            || row.percent == null || daysInYear == null || daysInYear == 0 || row.bill == null) {
        return null
    }

    def tmp = 0
    if (row.percentInCurrency == null) {
        countsDays = (row.buyDate >= startDate ?
            endDate - row.buyDate - 1 : endDate - startDate)
        if (countsDays != 0) {
            tmp = row.nominal * (row.percent * countsDays) / (100 * daysInYear)
        }
    } else {
        tmp = row.percentInCurrency - getCalcPrevColumn10(row, 'sumIncomeinCurrency', startDate)
    }
    return tmp.setScale(2, RoundingMode.HALF_UP)
}

// Ресчет графы 11
def BigDecimal calc11(def row, def endDate, def startDate) {
    if (row.currency == null || endDate == null || row.implementationDate == null
            || row.sumIncomeinCurrency == null || row.bill == null) {
        return null
    }
    def tmp = 0
    if (row.percentInCurrency == null) {
        if (row.implementationDate != null) {
            rate = 1
            if (!isRubleCurrency(row.currency)) {
                rate = getRate(row.implementationDate, row.currency)
            }
            tmp = row.sumIncomeinCurrency * rate
        } else {
            tmp = row.sumIncomeinCurrency * getRate(endDate, row.currency)
        }
    } else {
        tmp = row.percentInRuble - getCalcPrevColumn10(row, 'sumIncomeinRuble', startDate)
    }
    return tmp.setScale(2, RoundingMode.HALF_UP)
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    if (dataRows.isEmpty()) {
        return
    }

    // Алиасы граф для арифметической проверки
    def arithmeticCheckAlias = ['percentInRuble', 'sumIncomeinCurrency', 'sumIncomeinRuble']
    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // Количество дней в году
    def daysInYear = getCountDaysInYear(new Date())
    // Дата начала отчетного периода
    def startDate = getStartDate()
    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    def reportDate = getReportDate()

    // Векселя
    def List<String> billsList = new ArrayList<String>()

    for (def row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля 1..11
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        // 2. Проверка даты приобретения и границ отчетного периода (графа 3)
        if (row.buyDate > endDate) {
            loggerError(row, errorMsg + 'Дата приобретения вне границ отчетного периода!')
        }

        // 3. Проверка даты реализации (погашения)  и границ отчетного периода (графа 7)
        if (row.implementationDate < startDate || endDate < row.implementationDate) {
            loggerError(row, errorMsg + 'Дата реализации (погашения) вне границ отчетного периода!')
        }

        // 5. Проверка на уникальность векселя
        if (billsList.contains(row.bill)) {
            loggerError(row, errorMsg + "Повторяющееся значения в графе «Вексель»")
        } else {
            billsList.add(row.bill)
        }

        // 6. Проверка корректности значения в «Графе 3»
        // во всех принятых формах начиная с периода к которому принадлежит дата из графы3 проверяемой строки,
        // заканчивая текущей формой, во всех этих формах ищем графу2 равную графе2 проверяемой строки и смотрим что
        // у них совпадает графа3, если не совпала - ошибка
        // 7. Проверка на наличие данных предыдущих отчетных периодов для заполнения графы 10 и графы 11
        // берем все формы начиная с периода к которому отпосится дата графы3 проверяемой строки и заканчивая
        // текущей фомрой. Если а этом промежутке есть периоды за которые нет формы - ошибка. Если среди найденых
        // форм есть форма в которой нет строки где графа2 = графе2 проверяемоф формы - ошибка.
        if (row.buyDate != null) {
            def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, row.buyDate, startDate - 1)
            for (reportPeriod in reportPeriods) {
                findFormData = formDataService.getLast(formData.formType.id, formData.kind, formData.departmentId,
                        reportPeriod.id, null, formData.comparativePeriodId, formData.accruing)
                if (findFormData != null) {
                    isFind = false
                    for (findRow in formDataService.getDataRowHelper(findFormData).getAllSaved()) {
                        if (findRow.bill == row.bill) {
                            isFind = true
                            // лп 8
                            if (findRow.buyDate != row.buyDate) {
                                loggerError(row, errorMsg + "Неверное указана Дата приобретения в РНУ-55 за "
                                        + reportPeriod.name)
                            }
                            break
                        }
                    }
                    // лп 7
                    if (!isFind) {
                        rowWarning(logger, row, errorMsg + "Экземпляр за период " + reportPeriod.name +
                                " не существует (отсутствуют первичные данные для расчёта)!")
                    }
                }
            }
        }

        // 8. Проверка на неотрицательные значения
        ['percentInCurrency', 'percentInRuble'].each {
            def cell = row.getCell(it)
            if (cell.getValue() != null && cell.getValue() < 0) {
                def name = cell.getColumn().getName()
                loggerError(row, errorMsg + "Значение графы \"$name\" отрицательное!")
            }
        }

        if (formData.kind == FormDataKind.PRIMARY) {
            // 9. Арифметическая проверка графы 9-11
            needValue['percentInRuble'] = calc9(row)
            needValue['sumIncomeinCurrency'] = calc10(row, startDate, reportDate, daysInYear)
            needValue['sumIncomeinRuble'] = calc11(row, reportDate, startDate)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, !isBalancePeriod())
        }
    }

    //10. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
    checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod())
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode) {
    return currencyCode != null ? (getRefBookValue(15, currencyCode)?.CODE.stringValue in ['810', '643']) : false
}

/**
 * Сумма по графе sumColumnName всех предыдущих форм начиная с row.buyDate в строках где bill = row.bill
 * @param row
 * @param sumColumnName алиас графы для суммирования
 */
def BigDecimal getCalcPrevColumn10(def row, def sumColumnName, def startDate) {
    def sum = 0
    if (row.buyDate != null && row.bill != null) {
        def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, row.buyDate, startDate - 1)
        for (reportPeriod in reportPeriods) {
            findFormData = formDataService.getLast(formData.formType.id, formData.kind, formData.departmentId,
                    reportPeriod.id, null, formData.comparativePeriodId, formData.accruing)
            if (findFormData != null) {
                isFind = false
                for (findRow in formDataService.getDataRowHelper(findFormData).allSaved) {
                    if (findRow.bill == row.bill && findRow.buyDate == row.buyDate) {
                        sum += findRow.getCell(sumColumnName).getValue() != null ? findRow.getCell(sumColumnName).getValue() : 0
                    }
                }
            }
        }
    }
    return sum
}

// Получить курс банка России на указанную дату.
def getRate(def Date date, def value) {
    def record = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache, 'CODE_NUMBER', "$value",
            date?:getEndDate(), -1, null, logger, true)

    return record?.RATE?.numberValue
}

// Получить количество дней в году по указанной дате.
def getCountDaysInYear(def date) {
    if (date == null) {
        return 0
    }
    def year = date.format('yyyy')
    def end = Date.parse('dd.MM.yyyy', "31.12.$year")
    def begin = Date.parse('dd.MM.yyyy', "31.12.$year")
    return end - begin + 1
}

def getStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

//Получить отчетную дату
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}

// Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными
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
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 11, 1)
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
        def cols = isBalancePeriod() ? (allColumns - 'number') : editableColumns
        def autoColumns = isBalancePeriod() ? ['number'] : autoFillColumns
        cols.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        xmlIndexCol++
        // fix
        xmlIndexCol++
        // графа 2
        newRow.bill = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 3
        newRow.buyDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 4
        newRow.currency = getRecordIdImport(15, 'CODE', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, false)
        xmlIndexCol++
        // графа 5
        newRow.nominal = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 6
        newRow.percent = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 7
        newRow.implementationDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 8..11
        ['percentInCurrency', 'percentInRuble', 'sumIncomeinCurrency', 'sumIncomeinRuble'].each { alias ->
            xmlIndexCol++
            newRow[alias] = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        }

        rows.add(newRow)
    }

    def totalRow = getDataRow(formDataService.getDataRowHelper(formData).allCached, 'total')
    rows.add(totalRow)

    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        // графа 9
        totalRow.percentInRuble = parseNumber(row.cell[9].text(), rnuIndexRow, 9 + colOffset, logger, true)
        // графа 11
        totalRow.sumIncomeinRuble = parseNumber(row.cell[11].text(), rnuIndexRow, 11 + colOffset, logger, true)
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
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('number').column.name
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
            ([(headerRows[0][0]) : tmpRow.getCell('number').column.name]),
            ([(headerRows[0][2]) : tmpRow.getCell('bill').column.name]),
            ([(headerRows[0][3]) : tmpRow.getCell('buyDate').column.name]),
            ([(headerRows[0][4]) : tmpRow.getCell('currency').column.name]),
            ([(headerRows[0][5]) : tmpRow.getCell('nominal').column.name]),
            ([(headerRows[0][6]) : tmpRow.getCell('percent').column.name]),
            ([(headerRows[0][7]) : tmpRow.getCell('implementationDate').column.name]),
            ([(headerRows[0][8]) : 'Фактически поступившая сумма процентов']),
            ([(headerRows[0][10]): 'Сумма начисленного процентного дохода за отчётный период']),
            ([(headerRows[1][8]) : 'в валюте']),
            ([(headerRows[1][9]) : 'в рублях по курсу Банка России']),
            ([(headerRows[1][10]): 'в валюте']),
            ([(headerRows[1][11]): 'в рублях по курсу Банка России']),
            ([(headerRows[2][0]) : '1'])
    ]
    (2..11).each {
        headerMapping.add(([(headerRows[2][it]): it.toString()]))
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
    def cols = isBalancePeriod() ? (allColumns - 'number') : editableColumns
    def autoColumns = isBalancePeriod() ? ['number'] : autoFillColumns
    cols.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    // графа 2
    def colIndex = 2
    newRow.bill = values[colIndex]

    // графа 3
    colIndex++
    newRow.buyDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 4
    colIndex++
    newRow.currency = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 5
    colIndex++
    newRow.nominal = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6
    colIndex++
    newRow.percent = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7
    colIndex++
    newRow.implementationDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 8..11
    ['percentInCurrency', 'percentInRuble', 'sumIncomeinCurrency', 'sumIncomeinRuble'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}