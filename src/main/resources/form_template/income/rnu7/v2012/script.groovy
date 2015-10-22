package form_template.income.rnu7.v2012

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * (РНУ-7) Справка бухгалтера для отражения расходов, учитываемых в РНУ-5,
 *                                  учёт которых требует применения метода начисления
 * formTypeId=311
 *
 * графа 1  Число/15/                       number
 * helper   Строка/1000                     helper
 * графа 2  А140/CODE/Строка/15/            kny
 * графа 3  Дата                            date
 * графа 4  A350/NUMBER/Строка/12/          code
 * графа 5  Строка/15                       docNumber
 * графа 6  Дата/ДД.ММ.ГГГГ                 docDate
 * графа 7  A64/CODE/Строка/3/              currencyCode
 * графа 8  Число/19.4/                     rateOfTheBankOfRussia
 * графа 9  Число/17.2/                     taxAccountingCurrency
 * графа 10 Число/17.2/                     taxAccountingRuble
 * графа 11 Число/17.2/                     accountingCurrency
 * графа 12 Число/17.2/                     ruble
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
        def cols = (isBalancePeriod() ? balanceEditableColumns : editableColumns)
        formDataService.addRow(formData, currentDataRow, cols, null)
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
def editableColumns = ['date', 'code', 'docNumber', 'docDate', 'currencyCode', 'taxAccountingCurrency', 'accountingCurrency']
@Field
def balanceEditableColumns = ['date', 'code', 'docNumber', 'docDate', 'currencyCode', 'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'taxAccountingRuble', 'accountingCurrency', 'ruble']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['date', 'code', 'docNumber', 'docDate', 'currencyCode', 'rateOfTheBankOfRussia']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['taxAccountingRuble', 'ruble']

// дата начала периода
@Field
def start = null

@Field
def endDate = null

// Признак периода ввода остатков
@Field
def Boolean isBalancePeriod = null

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

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        // сортируем по кодам
        dataRows.sort { getKnu(it.code) }

        dataRows.eachWithIndex { row, index ->
            row.setIndex(index + 1)
        }

        if (!isBalancePeriod() && formDataEvent != FormDataEvent.IMPORT) {
            for (row in dataRows) {
                row.rateOfTheBankOfRussia = calc8(row)
                row.taxAccountingRuble = calc10(row)
                row.ruble = calc12(row)
            }
        }

        // посчитать "итого по коду"
        def totalRows = calcSubTotalRows(dataRows)

        // добавить "итого по коду" в таблицу
        def i = 0
        totalRows.each { index, row ->
            dataRows.add(index + i++, row)
        }
    }

    dataRows.add(calcTotalRow(dataRows))
}

def calcSubTotalRows(def dataRows) {
    // посчитать "итого по коду"
    def totalRows = [:]
    def code = null
    def sum = 0, sum2 = 0
    def rows = dataRows.findAll { it.getAlias() == null }

    rows.eachWithIndex { row, i ->
        if (code == null) {
            code = getKnu(row.code)
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (code != getKnu(row.code)) {
            totalRows.put(i, getNewRow(code, sum, sum2))
            sum = 0
            sum2 = 0
            code = getKnu(row.code)
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == rows.size() - 1) {
            sum += (row.taxAccountingRuble ?: 0)
            sum2 += (row.ruble ?: 0)
            def totalRowCode = getNewRow(code, sum, sum2)
            totalRows.put(i + 1, totalRowCode)
            sum = 0
            sum2 = 0
        }
        sum += (row.taxAccountingRuble ?: 0)
        sum2 += (row.ruble ?: 0)
    }

    return totalRows
}

def BigDecimal calc8(DataRow row) {
    if (row.date == null || row.currencyCode == null) {
        return null
    }
    if (isRubleCurrency(row.currencyCode)) {
        return 1
    }
    return getRate(row.date, row.currencyCode)?.setScale(4, BigDecimal.ROUND_HALF_UP)
}

def BigDecimal calc10(DataRow row) {
    if (row.taxAccountingCurrency == null || row.rateOfTheBankOfRussia == null) {
        return null
    }
    return (row.taxAccountingCurrency * row.rateOfTheBankOfRussia).setScale(2, BigDecimal.ROUND_HALF_UP)
}

def BigDecimal calc12(DataRow row) {
    if (row.accountingCurrency == null || row.rateOfTheBankOfRussia == null) {
        return null
    }
    return (row.accountingCurrency * row.rateOfTheBankOfRussia).setScale(2, BigDecimal.ROUND_HALF_UP)
}

// Получить курс валюты value на дату date
def getRate(def Date date, def value) {
    def record = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache, 'CODE_NUMBER', "$value",
            date?:getReportPeriodEndDate(), -1, null, logger, true)

    return record?.RATE?.numberValue
}

// Проверка валюты currencyCode на рубли
def isRubleCurrency(def currencyCode) {
    return currencyCode != null ? (getRefBookValue(15, currencyCode)?.CODE.stringValue in ['810', '643']) : false
}

def calcTotalRow(def dataRows) {
    def totalRow = getTotalRow('total', 'Итого')
    calcTotalSum(dataRows, totalRow, totalColumns)

    return totalRow
}

// Получить новую строку подитога
def getNewRow(def alias, def sum, def sum2) {
    def newRow = getTotalRow('total' + alias, 'Итого по КНУ ' + alias)
    newRow.taxAccountingRuble = sum
    newRow.ruble = sum2
    return newRow
}

def getTotalRow(def alias, def title) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.setAlias(alias)
    newRow.helper = title
    newRow.getCell('helper').colSpan = 9
    ['number', 'helper', 'taxAccountingRuble', 'accountingCurrency', 'ruble'].each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    List<Map<Integer, Object>> docs = new ArrayList<>()
    Map<Map<Integer, Object>, List<Integer>> uniq456 = [:]
    SimpleDateFormat dateFormat = new SimpleDateFormat('dd.MM.yyyy')

    // алиасы графов для арифметической проверки
    def arithmeticCheckAlias = ['rateOfTheBankOfRussia', 'taxAccountingRuble', 'ruble']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // Дата начала отчетного периода
    def startDate = getStartDate()
    // Дата окончания отчетного периода
    def endDate = getReportPeriodEndDate()

    //две карты: одна с реальными значениями итого по кодам, а вторая - с рассчитанными
    def totalRows = [:]
    def sumRowsByCode = [:]
    //две карты: одна с реальными значениями итого по кодам, а вторая - с рассчитанными
    def totalRows2 = [:]
    def sumRowsByCode2 = [:]

    for (def row : dataRows) {
        if (row.getAlias() ==~ /total\d+/) { // если подитог
            totalRows[row.getAlias().replace('total', '')] = row.taxAccountingRuble
            totalRows2[row.getAlias().replace('total', '')] = row.ruble
            continue
        }
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        // 3. Проверка на нулевые значения
        if (!(row.taxAccountingCurrency) && !(row.taxAccountingRuble) &&
                !(row.accountingCurrency) && !(row.ruble)) {
            loggerError(row, errorMsg + 'Все суммы по операции нулевые!')
        }

        // 4. Проверка, что не  отображаются данные одновременно по бухгалтерскому и по налоговому учету
        if (row.taxAccountingRuble && row.ruble) {
            rowWarning(logger, row, errorMsg + 'Одновременно указаны данные по налоговому (графа 10) и бухгалтерскому (графа 12) учету.')
        }

        // 5. Проверка даты совершения операции и границ отчётного периода
        if (row.date != null && (row.date.after(endDate) || row.date.before(startDate))) {
            loggerError(row, errorMsg + 'Дата совершения операции вне границ отчётного периода!')
        }

        // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода
        // +7.
        def Map<Integer, Object> map2 = new HashMap<>()
        def Map<Integer, Object> map3 = new HashMap<>()
        if (row.docDate != null && row.docNumber != null) {
            map2.put(5, row.docNumber)
            map2.put(6, row.docDate)
            if (!docs.contains(map2)) {
                docs.add(map2)
                def c12 = 0
                def c10 = 0
                for (rowSum in dataRows) {
                    if (rowSum.docNumber == row.docNumber && rowSum.docDate == row.docDate) {
                        c12 += (rowSum.ruble ?: 0)
                        c10 += (rowSum.taxAccountingRuble ?: 0)
                    }
                }
                if (c10 < c12) {
                    rowWarning(logger, row, errorMsg + "Сумма данных бухгалтерского учёта превышает сумму начисленных " +
                            "платежей для документа " + row.docNumber + " от " + dateFormat.format(row.docDate) + "!")
                }
            }
            if (row.taxAccountingRuble > 0 && row.code != null) {
                // 7. Проверка на уникальность записи по налоговому учету
                map3.put(4, row.code);
                map3.put(5, row.docNumber)
                map3.put(6, row.docDate)
                if (uniq456.get(map3) != null) {
                    uniq456.get(map3).add(row.getIndex())
                } else {
                    List<Integer> newList = new ArrayList<Integer>()
                    newList.add(row.getIndex())
                    uniq456.put(map3, newList)
                }
            }
        }

        // 8. Арифметические проверки расчета неитоговых строк
        needValue['rateOfTheBankOfRussia'] = calc8(row)
        needValue['taxAccountingRuble'] = calc10(row)
        needValue['ruble'] = calc12(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, !isBalancePeriod())

        // 9. Арифметические проверки расчета итоговых строк «Итого по КНУ»
        def String code = getKnu(row.code)
        if (sumRowsByCode[code] != null) {
            sumRowsByCode[code] += row.taxAccountingRuble ?: 0
        } else {
            sumRowsByCode[code] = row.taxAccountingRuble ?: 0
        }
        if (sumRowsByCode2[code] != null) {
            sumRowsByCode2[code] += row.ruble ?: 0
        } else {
            sumRowsByCode2[code] = row.ruble ?: 0
        }

        // 11. Проверка наличия суммы дохода в налоговом учете, для первичного документа, указанного для суммы дохода в бухгалтерском учёте
        // 12. Проверка значения суммы дохода в налоговом учете, для первичного документа, указанного для суммы дохода в бухгалтерском учёте
        if (row.ruble && row.docDate != null) {
            def Date date = row.docDate
            def Date from = Date.parse('dd.MM.yyyy','01.01.' + (Integer.valueOf(date?.format('yyyy')) - 3))
            def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, from, date)

            isFind = false
            def sum = 0 // сумма 12-х граф
            def periods = []

            for (reportPeriod in reportPeriods) {
                def findFormData = formDataService.getLast(formData.formType.id, formData.kind, formData.departmentId,
                        reportPeriod.id, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
                if (findFormData != null) {
                    for (findRow in formDataService.getDataRowHelper(findFormData).allCached) {
                        // SBRFACCTAX-3531 исключать строку из той же самой формы не надо
                        if (findRow.getAlias() == null && findRow.code == row.code && findRow.docNumber == row.docNumber
                                && findRow.docDate == row.docDate && findRow.taxAccountingRuble > 0) {
                            isFind = true
                            sum += findRow.taxAccountingRuble
                            periods += (reportPeriod.name + " " + reportPeriod.taxPeriod.year)
                        }
                    }
                }
            }
            if (!isFind) {
                rowWarning(logger, row, "Операция, указанная в строке " + row.getIndex() + ", в налоговом учете за " +
                        "последние 3 года не проходила!")
            }
            if (isFind && row.ruble > sum) {
                rowWarning(logger, row, errorMsg + "Операция в налоговом учете имеет сумму, меньше чем указано " +
                        "в бухгалтерском учете! См. РНУ-7 в отчетных периодах: ${periods.join(", ")}.")
            }
        }
    }

    // 7. Проверка на уникальность записи по налоговому учету
    for (def map : uniq456.keySet()) {
        def rowList = uniq456.get(map)
        def name4 = getColumnName(dataRows[0], 'code')
        def name5 = getColumnName(dataRows[0], 'docNumber')
        def name6 = getColumnName(dataRows[0], 'docDate')
        if (rowList.size() > 1) {
            def rowIndexes = rowList.join(', ')
            def value4 = getRefBookValue(27, map.get(4))?.NUMBER?.value
            def value5 = map.get(5)
            def value6 = dateFormat.format(map.get(6))
            def message = "Строки $rowIndexes не уникальны в рамках текущей налоговой формы! По данным строкам значения следующих граф совпадают: «$name4» ($value4), «$name5» ($value5), «$name6» ($value6)."
            logger.warn("%s", message)

        }
    }

    // 9. Арифметические проверки расчета итоговых строк «Итого по КНУ»
    totalRows.each { key, val ->
        if (val != sumRowsByCode[key]) {
            def msg = formData.createDataRow().getCell('taxAccountingRuble').column.name
            loggerError(null, "Неверное итоговое значение по коду '$key' графы «$msg»!")
        }
    }
    totalRows2.each { key, val ->
        if (val != sumRowsByCode2[key]) {
            def msg = formData.createDataRow().getCell('ruble').column.name
            loggerError(null, "Неверное итоговое значение по коду '$key' графы «$msg»!")
        }
    }

    // 10. Арифметические проверки расчета строки общих итогов
    checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod())
}

def String getKnu(def code) {
    return getRefBookValue(27, code)?.CODE?.stringValue
}

def getStartDate() {
    if (!start) {
        start = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return start
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 12
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ (1, 2, ..)
    def totalTF = null      // итоговая строка со значениями из тф для добавления
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)
    try {
        // пропускаем заголовок
        rowCells = reader.readNext()
        if (isEmptyCells(rowCells)) {
            logger.error('Первой строкой должен идти заголовок, а не пустая строка')
        }
        // пропускаем пустую строку
        rowCells = reader.readNext()
        if (!isEmptyCells(rowCells)) {
            logger.error('Вторая строка должна быть пустой')
        }
        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex, true)
                }
                break
            }
            newRows.add(getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex))
        }
    } finally {
        reader.close()
    }

    // посчитать "итого по коду"
    def totalRows = calcSubTotalRows(newRows)
    def i = 0
    totalRows.each { index, row ->
        newRows.add(index + i++, row)
    }

    def totalRow = calcTotalRow(newRows)
    newRows.add(totalRow)

    showMessages(newRows, logger)

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = ['taxAccountingRuble' : 10, 'ruble' : 12]
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR + " Из файла: $v1, рассчитано: $v2", totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
        // задать итоговой строке нф значения из итоговой строки тф
        totalColumns.each { alias ->
            totalRow[alias] = totalTF[alias]
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
        // очистить итоги
        totalColumns.each { alias ->
            totalRow[alias] = null
        }
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(newRows)
        formDataService.getDataRowHelper(formData).allCached = newRows
    }
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 * @param isTotal признак итоговой строки
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex, def isTotal = false) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}", fileRowIndex))
        return newRow
    }
    def cols = (isBalancePeriod() ? balanceEditableColumns : editableColumns)
    cols.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def int colOffset = 1

    if (!isTotal) {
        // графа 3
        newRow.date = parseDate(pure(rowCells[3]), "dd.MM.yyyy", fileRowIndex, 3 + colOffset, logger, true)
        // графа 4
        String filter = getFilter(pure(rowCells[2]), pure(rowCells[4]).replaceAll(/\./, ""))
        def records = refBookFactory.getDataProvider(27).getRecords(reportPeriodEndDate, null, filter, null)
        if (checkImportRecordsCount(records, refBookFactory.get(27), 'CODE', pure(rowCells[2]), reportPeriodEndDate, fileRowIndex, 2, logger, false)) {
            newRow.code = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        }
        // графа 5
        newRow.docNumber = pure(rowCells[5])
        // графа 6
        newRow.docDate = parseDate(pure(rowCells[6]), "dd.MM.yyyy", fileRowIndex, 6 + colOffset, logger, true)
    }
    // графа 7
    newRow.currencyCode = getRecordIdImport(15, 'CODE', pure(rowCells[7]), fileRowIndex, 7 + colOffset, false)
    // графа 8
    newRow.rateOfTheBankOfRussia =  parseNumber(pure(rowCells[8]), fileRowIndex, 8 + colOffset, logger, true)
    // графа 9
    newRow.taxAccountingCurrency = parseNumber(pure(rowCells[9]), fileRowIndex, 9 + colOffset, logger, true)
    // графа 10
    newRow.taxAccountingRuble  =  parseNumber(pure(rowCells[10]), fileRowIndex, 10 + colOffset, logger, true)
    // графа 11
    newRow.accountingCurrency = parseNumber(pure(rowCells[11]), fileRowIndex, 11 + colOffset, logger, true)
    // графа 12
    newRow.ruble  =  parseNumber(pure(rowCells[12]), fileRowIndex, 12 + colOffset, logger, true)

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), dataRows.find { it.getAlias() == 'total' }, true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias())}
}

static String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

void importData() {
    int COLUMN_COUNT = 12
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = '№ пп'
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
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
    def totalRowFromFileMap = [:]           // мапа для хранения строк подитогов со значениями из файла (стили простых строк)

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
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, true)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (rowValues[INDEX_FOR_SKIP].contains("Итого по КНУ ")) {
            def subTotalRow = getNewSubTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            if (totalRowFromFileMap[subTotalRow.helper] == null) {
                totalRowFromFileMap[subTotalRow.helper] = []
            }
            totalRowFromFileMap[subTotalRow.helper].add(subTotalRow)
            rows.add(subTotalRow)

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

    // сравнение подитогов
    if (!totalRowFromFileMap.isEmpty()) {
        def totalRowsMap = calcSubTotalRows(rows)
        totalRowsMap.values().toArray().each { tmpRow ->
            def totalRows = totalRowFromFileMap[tmpRow.helper]
            if (totalRows) {
                totalRows.each { totalRow ->
                    compareTotalValues(totalRow, tmpRow, totalColumns, logger, false)
                }
                totalRowFromFileMap.remove(tmpRow.helper)
            }
        }
        if (!totalRowFromFileMap.isEmpty()) {
            // для этих подитогов из файла нет групп
            totalRowFromFileMap.each { key, totalRows ->
                totalRows.each { totalRow ->
                    totalColumns.each { alias ->
                        def msg = String.format(COMPARE_TOTAL_VALUES, totalRow.getIndex(), getColumnName(totalRow, alias), totalRow[alias], BigDecimal.ZERO)
                        rowWarning(logger, totalRow, msg)
                    }
                }
            }
        }
    }

    // сравнение итогов
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    if (totalRowFromFile) {
        compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)
    }

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
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[0].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            (headerRows[0][0]) : '№ пп',
            (headerRows[0][2]) : 'Код налогового учета',
            (headerRows[0][3]) : 'Дата совершения операции',
            (headerRows[0][4]) : 'Балансовый счёт (номер)',
            (headerRows[0][5]) : 'Первичный документ',
            (headerRows[0][7]) : 'Код валюты',
            (headerRows[0][8]) : 'Курс Банка России',
            (headerRows[0][9]) : 'Сумма расхода в налоговом учёте',
            (headerRows[0][11]): 'Сумма расхода в бухгалтерском учёте',
            (headerRows[1][5]) : 'Номер',
            (headerRows[1][6]) : 'Дата',
            (headerRows[1][9]) : 'Валюта',
            (headerRows[1][10]): 'Рубли',
            (headerRows[1][11]): 'Валюта',
            (headerRows[1][12]): 'Рубли',
            (headerRows[2][0]) : '1'
    ]
    (2..12).each { index ->
        headerMapping.put((headerRows[2][index]), index.toString())
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
 * @param isTotal признак итоговой строки (для пропуска получения справочных значении)
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def isTotal = false) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    def cols = (isBalancePeriod() ? balanceEditableColumns : editableColumns)
    cols.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    // графа 3
    def colIndex = 3
    newRow.date = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 4
    if (!isTotal) {
        String filter =  getFilter(values[2], values[4].replaceAll(/\./, ""))
        def records = refBookFactory.getDataProvider(27).getRecords(reportPeriodEndDate, null, filter, null)
        colIndex = 2
        if (checkImportRecordsCount(records, refBookFactory.get(27), 'CODE', values[colIndex], reportPeriodEndDate, fileRowIndex, colIndex + colOffset, logger, false)) {
            newRow.code = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        }
    }

    // графа 5
    colIndex = 5
    newRow.docNumber = values[colIndex]

    // графа 6
    colIndex++
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7
    colIndex++
    newRow.currencyCode = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // графа 8..12
    ['rateOfTheBankOfRussia', 'taxAccountingCurrency', 'taxAccountingRuble', 'accountingCurrency', 'ruble'].each { alias ->
        colIndex++
        newRow[alias] =  parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

/**
 * Получить новую подитоговую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewSubTotalRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    // графа helper
    def title = values[1]
    def alias = title.substring("Итого по КНУ ".size())?.trim()

    def newRow = getNewRow(alias, 0, 0)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 10
    colIndex = 10
    newRow.taxAccountingRuble = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 12
    colIndex = 12
    newRow.ruble = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

def String getFilter(def String code, def String number){
    String filter = "LOWER(CODE) = LOWER('" + code + "')"
    if (number != '') {
        filter += " and LOWER(NUMBER) = LOWER('" + number + "')"
    }
    return filter
}