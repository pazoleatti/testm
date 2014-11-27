package form_template.income.rnu7.v2012

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
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
        formDataService.consolidationSimple(formData, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
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
                      def boolean required = true) {
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        // сортируем по кодам
        dataRows.sort { getKnu(it.code) }

        dataRows.eachWithIndex { row, index ->
            row.setIndex(index + 1)
        }

        if (!isBalancePeriod()) {
            for (row in dataRows) {
                row.rateOfTheBankOfRussia = calc8(row)
                row.taxAccountingRuble = calc10(row)
                row.ruble = calc12(row)
            }
        }

        calcSubTotal(dataRows)
    }

    dataRows.add(calcTotalRow(dataRows))
    dataRowHelper.save(dataRows)

    // Сортировка групп и строк
    sortFormDataRows()
}

void calcSubTotal(def dataRows) {
    // посчитать "итого по коду"
    def totalRows = [:]
    def code = null
    def sum = 0, sum2 = 0
    dataRows.eachWithIndex { row, i ->
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
        if (i == dataRows.size() - 1) {
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

    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        dataRows.add(index + i++, row)
    }
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
    def newRow = formData.createDataRow()
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
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
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

        // 9. Арифметические проверки расчета неитоговых строк
        needValue['rateOfTheBankOfRussia'] = calc8(row)
        needValue['taxAccountingRuble'] = calc10(row)
        needValue['ruble'] = calc12(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, !isBalancePeriod())

        // 10. Арифметические проверки расчета итоговых строк «Итого по КНУ»
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

        // 12. Проверка наличия суммы дохода в налоговом учете, для первичного документа, указанного для суммы дохода в бухгалтерском учёте
        // 13. Проверка значения суммы дохода в налоговом учете, для первичного документа, указанного для суммы дохода в бухгалтерском учёте
        if (row.ruble && row.docDate != null) {
            def Date date = row.docDate
            def Date from = new SimpleDateFormat('dd.MM.yyyy').parse('01.01.' + (Integer.valueOf(new SimpleDateFormat('yyyy').format(date)) - 3))
            def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, from, date)

            isFind = false
            def sum = 0 // сумма 12-х граф
            def periods = []

            for (reportPeriod in reportPeriods) {
                def findFormData = formDataService.getLast(formData.formType.id, formData.kind, formData.departmentId,
                        reportPeriod.id, formData.periodOrder)
                if (findFormData != null) {
                    for (findRow in formDataService.getDataRowHelper(findFormData).getAllCached()) {
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
            if (isFind && !(sum > row.ruble)) {
                rowWarning(logger, row, errorMsg + "Операция в налоговом учете имеет сумму, меньше чем указано " +
                        "в бухгалтерском учете! См. РНУ-7 в отчетных периодах: ${periods.join(", ")}.")
            }
        }
    }

    // 8 . Проверка на уникальность записи по налоговому учету
    for (def map : uniq456.keySet()) {
        def rowList = uniq456.get(map)
        if (rowList.size() > 1) {
            loggerError(null, "Несколько строк " + rowList.join(", ") + " содержат записи в налоговом учете для балансового " +
                    "счета=" + refBookService.getStringValue(27, map.get(4), 'NUMBER').toString() + ", документа № " +
                    map.get(5).toString() +" от " + dateFormat.format(map.get(6)) + ".")
        }
    }

    // 10. Арифметические проверки расчета итоговых строк «Итого по КНУ»
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

    // 11. Арифметические проверки расчета строки общих итогов
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

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 12, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Код налогового учета',
            (xml.row[0].cell[3]): 'Дата совершения операции',
            (xml.row[0].cell[4]): 'Балансовый счёт (номер)',
            (xml.row[0].cell[5]): 'Первичный документ',
            (xml.row[0].cell[7]): 'Код валюты',
            (xml.row[0].cell[8]): 'Курс Банка России',
            (xml.row[0].cell[9]): 'Сумма расхода в налоговом учёте',
            (xml.row[0].cell[11]): 'Сумма расхода в бухгалтерском учёте',
            (xml.row[1].cell[5]): 'Номер',
            (xml.row[1].cell[6]): 'Дата',
            (xml.row[1].cell[9]): 'Валюта',
            (xml.row[1].cell[10]): 'Рубли',
            (xml.row[1].cell[11]): 'Валюта',
            (xml.row[1].cell[12]): 'Рубли',
            (xml.row[2].cell[0]): '1'
    ]
    (2..12).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        /* Пропуск строк шапок */
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        def cols = (isBalancePeriod() ? balanceEditableColumns : editableColumns)
        cols.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        xmlIndexCol++

        // графа fix
        xmlIndexCol++

        // графа 2
        xmlIndexCol++

        // графа 3
        newRow.date = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 4
        String filter = "LOWER(CODE) = LOWER('" + row.cell[2].text() + "') and LOWER(NUMBER) = LOWER('" + row.cell[xmlIndexCol].text() + "')"
        def records = refBookFactory.getDataProvider(27).getRecords(reportPeriodEndDate, null, filter, null)
        if (checkImportRecordsCount(records, refBookFactory.get(27), 'CODE', row.cell[2].text(), reportPeriodEndDate, xlsIndexRow, 2, logger, true)) {
            newRow.code = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        }
        xmlIndexCol++

        // графа 5
        newRow.docNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 6
        newRow.docDate = parseDate(row.cell[6].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 7
        newRow.currencyCode = getRecordIdImport(15, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 8
        xmlIndexCol++

        // графа 9
        newRow.taxAccountingCurrency = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 10
        xmlIndexCol++

        // графа 11
        newRow.accountingCurrency = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }

    dataRowHelper.save(rows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 12, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
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
        def cols = (isBalancePeriod() ? balanceEditableColumns : editableColumns)
        cols.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 3
        newRow.date = parseDate(row.cell[3].text(), "dd.MM.yyyy", rnuIndexRow, 3 + colOffset, logger, true)
        // графа 4
        String filter = "LOWER(CODE) = LOWER('" + row.cell[2].text() + "') and LOWER(NUMBER) = LOWER('" + row.cell[4].text() + "')"
        def records = refBookFactory.getDataProvider(27).getRecords(reportPeriodEndDate, null, filter, null)
        if (checkImportRecordsCount(records, refBookFactory.get(27), 'CODE', row.cell[2].text(), reportPeriodEndDate, rnuIndexRow, 2, logger, true)) {
            newRow.code = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        }

        // графа 5
        newRow.docNumber = row.cell[5].text()
        // графа 6
        newRow.docDate = parseDate(row.cell[6].text(), "dd.MM.yyyy", rnuIndexRow, 6 + colOffset, logger, true)
        // графа 7
        newRow.currencyCode = getRecordIdImport(15, 'CODE', row.cell[7].text(), rnuIndexRow, 7 + colOffset)
        // графа 8
        newRow.rateOfTheBankOfRussia =  parseNumber(row.cell[8].text(), rnuIndexRow, 8 + colOffset, logger, true)
        // графа 9
        newRow.taxAccountingCurrency = parseNumber(row.cell[9].text(), rnuIndexRow, 9 + colOffset, logger, true)
        // графа 10
        newRow.taxAccountingRuble  =  parseNumber(row.cell[10].text(), rnuIndexRow, 10+ colOffset, logger, true)
        // графа 11
        newRow.accountingCurrency = parseNumber(row.cell[11].text(), rnuIndexRow, 11 + colOffset, logger, true)
        // графа 12
        newRow.ruble  =  parseNumber(row.cell[12].text(), rnuIndexRow, 12 + colOffset, logger, true)

        rows.add(newRow)
    }

    calcSubTotal(rows)
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // графа 10
        total.taxAccountingRuble = parseNumber(row.cell[10].text(), rnuIndexRow, 10 + colOffset, logger, true)

        // графа 12
        total.ruble = parseNumber(row.cell[12].text(), rnuIndexRow, 12 + colOffset, logger, true)

        def colIndexMap = ['taxAccountingRuble' : 10, 'ruble' : 12]
        for (def alias : totalColumns) {
            def v1 = total[alias]
            def v2 = totalRow[alias]
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.error(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
                break
            }
        }
    }
    dataRowHelper.save(rows)
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), dataRows.find { it.getAlias() == 'total' }, true)
    dataRowHelper.saveSort()
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias())}
}