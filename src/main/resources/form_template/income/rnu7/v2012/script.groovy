package form_template.income.rnu7.v2012

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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

@Field
def allColumns = ['kny', 'date', 'code', 'docNumber', 'docDate', 'currencyCode', 'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'taxAccountingRuble', 'accountingCurrency', '']

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

// графа 2
@Field
def groupColumns = ['kny']

@Field
def sortColumns = ['kny', 'code', 'date']

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
        refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { (groupColumns + 'code').contains(it.getAlias())})
        sortRows(dataRows, groupColumns)

        if (!isBalancePeriod() && formDataEvent != FormDataEvent.IMPORT) {
            for (row in dataRows) {
                row.rateOfTheBankOfRussia = calc8(row)
                row.taxAccountingRuble = calc10(row)
                row.ruble = calc12(row)
            }
        }

        // Добавление подитогов
        addAllAliased(dataRows, new ScriptUtils.CalcAliasRow() {
            @Override
            DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
                return calcItog(i, rows)
            }
        }, groupColumns)
    }

    dataRows.add(getTotalRow(dataRows))

    updateIndexes(dataRows)
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

/** Получить общую итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def totalRow = getTotalRow('Итого', 'total')
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

/**
 * Получить подитоговую строку.
 *
 * @param rowNumber номер строки
 * @param code КНУ
 * @param key ключ для сравнения подитоговых строк при импорте
 */
def getSubTotalRow(def rowNumber, def code, def key) {
    def title = getTitle(code)
    def alias = 'total' + key.toString() + '#' + rowNumber
    return getTotalRow(title, alias)
}

def getTotalRow(def title, def alias) {
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

    for (def row : dataRows) {
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
    // Проверка наличия всех фиксированных строк
    // Проверка отсутствия лишних фиксированных строк
    // Проверка итоговых значений по фиксированным строкам
    checkItog(dataRows)

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
    checkTF(ImportInputStream, UploadFileName)

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
        // проверить первые строки тф - заголовок и пустая строка
        checkFirstRowsTF(reader, logger)

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

    // Добавление подитогов
    addAllAliased(newRows, new ScriptUtils.CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)

    def totalRow = getTotalRow(newRows)
    newRows.add(totalRow)

    showMessages(newRows, logger)

    // сравнение итогов
    checkAndSetTFSum(totalRow, totalTF, totalColumns, totalTF?.getImportIndex(), logger, false)

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
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}'", fileRowIndex))
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
            newRow.getCell('kny').setRefBookDereference(pure(rowCells[2]))
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
    def columns = sortColumns + (allColumns - sortColumns)
    // Сортировка (внутри групп)
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { columns.contains(it.getAlias())})
    def newRows = []
    def tempRows = []
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            if (!tempRows.isEmpty()) {
                sortRows(tempRows, columns)
                newRows.addAll(tempRows)
                tempRows = []
            }
            newRows.add(row)
            continue
        }
        tempRows.add(row)
    }
    if (!tempRows.isEmpty()) {
        sortRows(tempRows, columns)
        newRows.addAll(tempRows)
    }
    dataRowHelper.setAllCached(newRows)

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
        } else if (rowValues[INDEX_FOR_SKIP].toLowerCase().contains("итого по кну ")) {
            // для расчета уникального среди групп(groupColumns) ключа берем строку перед Подитоговой
            def key = !rows.isEmpty() ? getKey(rows[-1]) : null
            def subTotalRow = getNewSubTotalRowFromXls(key, rowValues, colOffset, fileRowIndex, rowIndex)

            // наш ключ - row.getAlias() до решетки. так как индекс после решетки не равен у расчитанной и импортированной подитогововых строк
            if (totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]] == null) {
                totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]] = []
            }
            totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]].add(subTotalRow)
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
    updateIndexes(rows)

    // сравнение подитогов
    if (!totalRowFromFileMap.isEmpty()) {
        def tmpSubTotalRowsMap = calcSubTotalRowsMap(rows)
        tmpSubTotalRowsMap.each { subTotalRow, groupValues ->
            def totalRows = totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]]
            if (totalRows) {
                totalRows.each { totalRow ->
                    compareTotalValues(totalRow, subTotalRow, totalColumns, logger, false)
                }
                totalRowFromFileMap.remove(subTotalRow.getAlias().split('#')[0])
            } else {
                rowWarning(logger, null, String.format(GROUP_WRONG_ITOG, groupValues))
            }
        }
        if (!totalRowFromFileMap.isEmpty()) {
            // для этих подитогов из файла нет групп
            totalRowFromFileMap.each { key, totalRows ->
                totalRows.each { totalRow ->
                    rowWarning(logger, totalRow, String.format(GROUP_WRONG_ITOG_ROW, totalRow.getIndex()))
                }
            }
        }
    }

    // сравнение итогов
    def totalRow = getTotalRow(rows)
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
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]): '№ пп']),
            ([(headerRows[0][2]): 'Код налогового учета']),
            ([(headerRows[0][3]): 'Дата совершения операции']),
            ([(headerRows[0][4]): 'Балансовый счёт (номер)']),
            ([(headerRows[0][5]): 'Первичный документ']),
            ([(headerRows[0][7]): 'Код валюты']),
            ([(headerRows[0][8]): 'Курс Банка России']),
            ([(headerRows[0][9]): 'Сумма расхода в налоговом учёте']),
            ([(headerRows[0][11]): 'Сумма расхода в бухгалтерском учёте']),
            ([(headerRows[1][5]): 'Номер']),
            ([(headerRows[1][6]): 'Дата']),
            ([(headerRows[1][9]): 'Валюта']),
            ([(headerRows[1][10]): 'Рубли']),
            ([(headerRows[1][11]): 'Валюта']),
            ([(headerRows[1][12]): 'Рубли']),
            ([(headerRows[2][0]): '1'])
    ]
    (2..12).each { index ->
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
 * @param key ключ для сравнения подитоговых строк при импорте
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewSubTotalRowFromXls(def key, def values, def colOffset, def fileRowIndex, def rowIndex) {
    // графа helper
    def title = values[1]
    def code = title?.substring('Итого по КНУ '.size())?.trim()
    def newRow = getSubTotalRow(rowIndex, code, key)
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

// Получить посчитанные подитоговые строки
def calcSubTotalRowsMap(def dataRows) {
    def tmpRows = dataRows.findAll { !it.getAlias() }
    // Добавление подитогов
    addAllAliased(tmpRows, new ScriptUtils.CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)

    // сформировать мапу (строка подитога -> значения группы)
    def map = [:]
    def prevRow = null
    for (def row : tmpRows) {
        if (!row.getAlias()) {
            prevRow = row
            continue
        }
        if (row.getAlias() && prevRow) {
            map[row] = getValuesByGroupColumn(prevRow)
        }
    }

    return map
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def tmpRow = dataRows.get(i)
    def key = getKey(tmpRow)
    def code = getKnu(tmpRow?.code)
    def newRow = getSubTotalRow(i, code, key)

    // Расчеты подитоговых значений
    def rows = []
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        rows.add(dataRows.get(j))
    }
    calcTotalSum(rows, newRow, totalColumns)

    return newRow
}

// Проверки подитоговых сумм
void checkItog(def dataRows) {
    // Рассчитанные строки итогов
    def testItogRowsMap = calcSubTotalRowsMap(dataRows)
    // Имеющиеся строки итогов
    def itogRows = dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
    // все строки, кроме общего итога
    def groupRows = dataRows.findAll { !'total'.equals(it.getAlias()) }
    def testItogRows = testItogRowsMap.keySet().asList()
    checkItogRows(groupRows, testItogRows, itogRows, groupColumns, logger, true, new ScriptUtils.GroupString() {
        @Override
        String getString(DataRow<Cell> row) {
            return getValuesByGroupColumn(row)
        }
    }, new ScriptUtils.CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            for (def alias : totalColumns) {
                if (row1[alias] != row2[alias]) {
                    return getColumnName(row1, alias)
                }
            }
            return null
        }
    }, new ScriptUtils.CheckDiffGroup() {
        @Override
        Boolean check(DataRow<Cell> row1, DataRow<Cell> row2, List<String> groupColumns) {
            if (row1.code == null) {
                return null // для строк с пустыми графами группировки не надо проверять итоги
            }
            return compareGroup(row1, row2)
        }
    })
}

boolean compareGroup(def rowA, def rowB) {
    def valueA = (rowA.getAlias() != null) ? rowA.helper : getTitle(getKnu(rowA.code))
    def valueB = (rowB.getAlias() != null) ? rowB.helper : getTitle(getKnu(rowB.code))
    return valueA != valueB
}

String getTitle(def code) {
    return 'Итого по КНУ ' + (!code || 'null'.equals(code?.trim()) ? '"КНУ не задано"' : code?.trim())
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    // 4
    def code = getKnu(row.code)
    return (code != null ? code : 'графа 4 не задана')
}

/** Получить уникальный ключ группы. */
def getKey(def row) {
    def key = ''
    groupColumns.each { def alias ->
        key = key + (row[alias] != null ? row[alias] : "").toString()
    }
    return key.toLowerCase().hashCode()
}