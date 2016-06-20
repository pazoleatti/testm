package form_template.income.rnu57.v2012

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * (РНУ-57) Регистр налогового учёта финансового результата от реализации (погашения) векселей сторонних эмитентов
 * formTypeId=353
 *
 * @author Stanislav Yasinskiy
 */

// 1    number                  № пп
//      fix
// 2    bill                    Вексель
// 3    purchaseDate            Дата приобретения
// 4    purchasePrice           Цена приобретения, руб.
// 5    purchaseOutcome         Расходы, связанные с приобретением,  руб.
// 6    implementationDate      Дата реализации (погашения)
// 7    implementationPrice     Цена реализации (погашения), руб.
// 8    implementationOutcome   Расходы, связанные с реализацией,  руб.
// 9    price                   Расчётная цена, руб.
// 10   percent                 Процентный доход, учтённый в целях налогообложения  (для дисконтных векселей), руб.
// 11   implementationpPriceTax Цена реализации (погашения) для целей налогообложения
//                                                              (для дисконтных векселей без процентного дохода),  руб.
// 12   allIncome               Всего расходы по реализации (погашению), руб.
// 13   implementationPriceUp   Превышение цены реализации для целей налогообложения над ценой реализации, руб.
// 14   income                  Прибыль (убыток) от реализации (погашения) руб.

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkRNU()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkRNU()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)

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
        checkRNU()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationTotal(formData, logger, userInfo, ['total'])
        checkRNU()
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
def editableColumns = ['bill', 'purchaseDate', 'implementationDate', 'implementationPrice', 'implementationOutcome']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['bill', 'purchaseDate', 'purchasePrice', 'purchaseOutcome', 'implementationDate',
        'implementationPrice', 'implementationOutcome', 'price', 'percent', 'implementationpPriceTax', 'allIncome',
        'implementationPriceUp', 'income']

// Атрибуты для итогов
@Field
def totalColumns = ['purchaseOutcome', 'implementationOutcome', 'percent', 'implementationpPriceTax', 'allIncome',
        'implementationPriceUp']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number', 'purchasePrice', 'purchaseOutcome', 'price', 'percent', 'implementationpPriceTax',
        'allIncome', 'implementationPriceUp', 'income']

//// Обертки методов

// Поиск записи в справочнике по значению (для расчетов)
def getRecord(def Long refBookId, def String alias, def String value, def int rowIndex, def String columnName,
              def Date date, boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, date,
            rowIndex, columnName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

void checkRNU() {
    // проверить рну 55 (id = 348) и 56 (id = 349)
    if (formData.kind == FormDataKind.PRIMARY) {
        formDataService.checkFormExistAndAccepted(348, FormDataKind.PRIMARY, formData.departmentId,
                formData.reportPeriodId, false, logger, true, formData.comparativePeriodId, formData.accruing)
        formDataService.checkFormExistAndAccepted(349, FormDataKind.PRIMARY, formData.departmentId,
                formData.reportPeriodId, false, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    if (!dataRows.isEmpty()) {
        // Удаление итогов
        deleteAllAliased(dataRows)

        // сортируем
        //dataRowHelper.save(dataRows.sort { it.bill })

        def rnu55DataRows = getRNU(348)
        def rnu56DataRows = getRNU(349)
        for (row in dataRows) {
            def rnu55Row = getRnuSourceRow(rnu55DataRows, row)
            def rnu56Row = getRnuSourceRow(rnu56DataRows, row)

            // графа 4
            row.purchasePrice = calc4(rnu55Row != null ? rnu55Row : rnu56Row)
            // графа 5
            row.purchaseOutcome = calc5(rnu55Row != null ? rnu55Row : rnu56Row)
            // графа 9
            row.price = calc9(row, rnu55Row, rnu56Row)
            // графа 10
            row.percent = calc10(rnu56Row, rnu56DataRows)
            // графа 11
            row.implementationpPriceTax = calc11(row, rnu55Row, rnu56Row)
            // графа 12
            row.allIncome = calc12(row)
            // графа 13
            row.implementationPriceUp = calc13(row)
            // графа 14
            row.income = calc14(row)
        }
    }

    dataRows.add(calcTotalRow(dataRows))
}

def getRNU(def id) {
    def sourceFormData = formDataService.getLast(id, formData.kind, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
    if (sourceFormData == null)
        return null
    return formDataService.getDataRowHelper(sourceFormData).allSaved
}

def getRnuSourceRow(def rnuSourceDataRows, DataRow row) {
    for (def sourceRow : rnuSourceDataRows) {
        if (sourceRow.getAlias() == null && sourceRow.bill != null && sourceRow.bill == row.bill) {
            return sourceRow
        }
    }
    return null
}

def BigDecimal calc4(DataRow sourceRow) {
    if (sourceRow == null || sourceRow.nominal == null) {
        return null
    }
    def currencyRate = getCurrencyRate(sourceRow, sourceRow.buyDate)
    if (currencyRate != null) {
        return (sourceRow.nominal * currencyRate)?.setScale(2, RoundingMode.HALF_UP)
    }
    return null
}

def BigDecimal calc5(DataRow sourceRow) {
    if (sourceRow != null) {
        return sourceRow.sumIncomeinRuble
    }
    return null
}

def BigDecimal calc9(DataRow row, DataRow rnu55DataRow, DataRow rnu56DataRow) {
    if (rnu56DataRow != null) {
        if (rnu56DataRow.maturity == null || row.implementationDate == null) {
            return null
        }
        if (rnu56DataRow.maturity == row.implementationDate) {
            return 0
        }
        if (rnu56DataRow.maturity > row.implementationDate) {
            def currencyRate = getCurrencyRate(rnu56DataRow, row.implementationDate)
            if (rnu56DataRow.nominal == null || rnu56DataRow.price == null || rnu56DataRow.buyDate == null
                    || row.implementationDate == null || currencyRate == null) {
                return null
            }
            def n = rnu56DataRow.nominal
            def k = rnu56DataRow.price
            def t = rnu56DataRow.maturity - rnu56DataRow.buyDate
            def d = row.implementationDate - rnu56DataRow.buyDate
            return (((n - k) * d / t + k) * currencyRate)?.setScale(2, RoundingMode.HALF_UP)
        }
    }
    if (rnu55DataRow != null) {
        def currencyRate = getCurrencyRate(rnu55DataRow, row.implementationDate)
        if (rnu55DataRow.nominal != null && currencyRate != null) {
            return (rnu55DataRow.nominal * currencyRate)?.setScale(2, RoundingMode.HALF_UP)
        }
    }
    return null
}

def BigDecimal calc10(DataRow rnu56DataRow, def rnu56DataRows) {
    if (rnu56DataRow != null) {
        if (rnu56DataRow.sum != null) {
            return rnu56DataRow.discountInRub
        }
        if (rnu56DataRow.bill == null) {
            def rnu56TotalDataRow = data56aRowHelper.getDataRow(rnu56DataRows, 'total')
            if (rnu56TotalDataRow != null) {
                return rnu56TotalDataRow.sumIncomeinRuble
            }
        }
    }
    return null
}

def BigDecimal calc11(DataRow row, DataRow rnu55DataRow, DataRow rnu56DataRow) {
    def retVal = null
    if (row.price != null) {
        final def tmpValue = 0.8 * row.price
        if (rnu55DataRow != null || rnu56DataRow != null) {
            if (row.implementationPrice != null && row.implementationPrice >= tmpValue) {
                retVal = row.implementationPrice
            } else {
                retVal = tmpValue
            }
        }
        if (rnu56DataRow != null && row.percent != null) {
            retVal -= row.percent
        }
    }
    return retVal
}

def BigDecimal calc12(DataRow row) {
    if (row.purchasePrice == null || row.purchaseOutcome == null || row.implementationOutcome == null) {
        return null
    }
    return row.purchasePrice + row.purchaseOutcome + row.implementationOutcome
}

def BigDecimal calc13(def row) {
    if (row.implementationpPriceTax != null && row.implementationPrice != null) {
        def tmpOne = row.implementationpPriceTax - row.implementationPrice
        if (row.percent == 0) {
            return tmpOne
        }
        if (row.percent != null && row.percent > 0) {
            def tmpTwo = row.implementationpPriceTax + row.percent - row.implementationPrice
            if (tmpOne < 0 || tmpTwo < 0) {
                return 0
            } else {
                return tmpTwo
            }
        }
    }
    return null
}

def BigDecimal calc14(def row) {
    if (row.implementationpPriceTax == null || row.allIncome == null) {
        return null
    }
    return row.implementationpPriceTax - row.allIncome
}

def getCurrencyRate(def row, def date) {
    if (row.currency == null || date == null) {
        return null
    }
    if (!isRubleCurrency(row.currency)) {
        return getRecord(22, 'CODE_NUMBER', "${row.currency}", row.number?.intValue(), getColumnName(row, 'currency'), date)?.RATE?.numberValue
    } else {
        return 1;
    }
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode) {
    return currencyCode != null ? (getRefBookValue(15, currencyCode)?.CODE?.stringValue in ['810', '643']) : false
}

// Логические проверки
boolean logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    def rnu55DataRows = getRNU(348)
    def rnu56DataRows = getRNU(349)

    // алиасы графов для арифметической проверки
    def arithmeticCheckAlias = ['purchasePrice', 'purchaseOutcome', 'price', 'percent', 'implementationpPriceTax',
            'allIncome', 'implementationPriceUp', 'income']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // Дата начала отчетного периода
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка даты приобретения и границ отчетного периода
        if (row.purchaseDate != null && row.purchaseDate.compareTo(endDate) > 0) {
            rowError(logger, row, errorMsg + 'Дата приобретения вне границ отчетного периода!')
        }

        // 3. Проверка даты реализации (погашения) и границ отчетного периода
        if (row.implementationDate != null && (row.implementationDate.compareTo(startDate) < 0 || row.implementationDate.compareTo(endDate) > 0)) {
            rowError(logger, row, errorMsg + 'Дата реализации (погашения) вне границ отчетного периода!')
        }

        def rnu55Row = getRnuSourceRow(rnu55DataRows, row)
        def rnu56Row = getRnuSourceRow(rnu56DataRows, row)

        // 7. Проверка существования необходимых экземпляров форм
        if (rnu55Row == null && rnu56Row == null) {
            rowError(logger, row, errorMsg + 'Отсутствуют данные в РНУ-55 (РНУ-56)!')
        } else {
            // 5. Арифметическая проверка граф 4, 5, 9-14
            needValue['purchasePrice'] = calc4(rnu55Row != null ? rnu55Row : rnu56Row)
            needValue['purchaseOutcome'] = calc5(rnu55Row != null ? rnu55Row : rnu56Row)
            needValue['price'] = calc9(row, rnu55Row, rnu56Row)
            needValue['percent'] = calc10(rnu56Row, rnu56DataRows)
            needValue['implementationpPriceTax'] = calc11(row, rnu55Row, rnu56Row)
            needValue['allIncome'] = calc12(row)
            needValue['implementationPriceUp'] = calc13(row)
            needValue['income'] = calc14(row)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
        }
    }

    // 6. Проверка итоговых значений по всей форме
    checkTotalSum(dataRows, totalColumns, logger, true)
}

def calcTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    (nonEmptyColumns + ['number', 'fix']).each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)

    return totalRow
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
        newRow.bill = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 3
        newRow.purchaseDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 4
        newRow.purchasePrice = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 5
        newRow.purchaseOutcome = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 6
        newRow.implementationDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 7..14
        ['implementationPrice', 'implementationOutcome', 'price', 'percent', 'implementationpPriceTax',
                'allIncome', 'implementationPriceUp', 'income'].each { alias ->
            newRow[alias] = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++
        }

        rows.add(newRow)
    }


    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    (nonEmptyColumns + ['number', 'fix']).each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    rows.add(totalRow)

    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        // графа 5
        totalRow.purchaseOutcome = parseNumber(row.cell[5].text(), rnuIndexRow, 5 + colOffset, logger, true)
        // графа 8
        totalRow.implementationOutcome = parseNumber(row.cell[8].text(), rnuIndexRow, 8 + colOffset, logger, true)
        // графа 10
        totalRow.percent = parseNumber(row.cell[10].text(), rnuIndexRow, 10 + colOffset, logger, true)
        // графа 11
        totalRow.implementationpPriceTax = parseNumber(row.cell[11].text(), rnuIndexRow, 11 + colOffset, logger, true)
        // графа 12
        totalRow.allIncome = parseNumber(row.cell[12].text(), rnuIndexRow, 12 + colOffset, logger, true)
        // графа 13
        totalRow.implementationPriceUp = parseNumber(row.cell[13].text(), rnuIndexRow, 13 + colOffset, logger, true)

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
    int COLUMN_COUNT = 15
    int HEADER_ROW_COUNT = 2
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

    def totalRow = calcTotalRow(rows)
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
            ([(headerRows[0][3]) : tmpRow.getCell('purchaseDate').column.name]),
            ([(headerRows[0][4]) : tmpRow.getCell('purchasePrice').column.name]),
            ([(headerRows[0][5]) : tmpRow.getCell('purchaseOutcome').column.name]),
            ([(headerRows[0][6]) : tmpRow.getCell('implementationDate').column.name]),
            ([(headerRows[0][7]) : tmpRow.getCell('implementationPrice').column.name]),
            ([(headerRows[0][8]) : tmpRow.getCell('implementationOutcome').column.name]),
            ([(headerRows[0][9]) : tmpRow.getCell('price').column.name]),
            ([(headerRows[0][10]): tmpRow.getCell('percent').column.name]),
            ([(headerRows[0][11]): tmpRow.getCell('implementationpPriceTax').column.name]),
            ([(headerRows[0][12]): tmpRow.getCell('allIncome').column.name]),
            ([(headerRows[0][13]): tmpRow.getCell('implementationPriceUp').column.name]),
            ([(headerRows[0][14]): tmpRow.getCell('income').column.name]),
            ([(headerRows[1][0]): '1'])
    ]
    (2..14).each {
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
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    // графа 2
    def colIndex = 2
    newRow.bill = values[colIndex]

    // графа 3
    colIndex++
    newRow.purchaseDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 4
    colIndex++
    newRow.purchasePrice = parseNumber(values[colIndex], fileRowIndex, 0 + colOffset, logger, true)

    // графа 5
    colIndex++
    newRow.purchaseOutcome = parseNumber(values[colIndex], fileRowIndex, 0 + colOffset, logger, true)

    // графа 6
    colIndex++
    newRow.implementationDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7..14
    ['implementationPrice', 'implementationOutcome', 'price', 'percent', 'implementationpPriceTax',
            'allIncome', 'implementationPriceUp', 'income'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}