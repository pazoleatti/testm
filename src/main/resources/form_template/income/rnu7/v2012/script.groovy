package form_template.income.rnu7.v2012

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
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

// Признак периода ввода остатков
@Field
def Boolean isBalancePeriod = null

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
        def cols = (getBalancePeriod() ? balanceEditableColumns : editableColumns)
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
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
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
def nonEmptyColumns = ['number', 'date', 'code', 'docNumber', 'docDate', 'currencyCode', 'rateOfTheBankOfRussia']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['taxAccountingRuble', 'ruble']

// дата начала периода
@Field
def start = null

@Field
def endDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
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
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        // сортируем по кодам
        dataRowHelper.save(dataRows.sort { getKnu(it.code) })

        dataRows.eachWithIndex { row, index ->
            row.setIndex(index + 1)
        }

        dataRows = dataRowHelper.getAllCached() // не убирать, группировка падает
        // номер последний строки предыдущей формы
        def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

        for (row in dataRows) {
            row.number = ++number
            row.rateOfTheBankOfRussia = calc8(row)
            row.taxAccountingRuble = calc10(row)
            row.ruble = calc12(row)
        }

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
        def i = 1
        totalRows.each { index, row ->
            dataRowHelper.insert(row, index + i++)
        }
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size() + 1)
    dataRowHelper.save(dataRows)
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
    return currencyCode != null ? (getRefBookValue(15, currencyCode)?.CODE.stringValue == '810') : false
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

    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

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
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !getBalancePeriod())

        // 2. Проверка на уникальность поля «№ пп»
        if (++i != row.number) {
            loggerError(errorMsg + "Нарушена уникальность номера по порядку!")
        }

        // 3. Проверка на нулевые значения
        if (!(row.taxAccountingCurrency) && !(row.taxAccountingRuble) &&
                !(row.accountingCurrency) && !(row.ruble)) {
            loggerError(errorMsg + 'Все суммы по операции нулевые!')
        }

        // 4. Проверка, что не  отображаются данные одновременно по бухгалтерскому и по налоговому учету
        if (row.taxAccountingRuble && row.ruble) {
            logger.warn(errorMsg + 'Одновременно указаны данные по налоговому (графа 10) и бухгалтерскому (графа 12) учету.')
        }

        // 5. Проверка даты совершения операции и границ отчётного периода
        if (row.date != null && (row.date.after(endDate) || row.date.before(startDate))) {
            loggerError(errorMsg + 'Дата совершения операции вне границ отчётного периода!')
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
                    logger.warn(errorMsg + 'Сумма данных бухгалтерского учёта превышает сумму начисленных платежей ' +
                            'для документа %s от %s!', row.docNumber as String, dateFormat.format(row.docDate))
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
        checkCalc(row, arithmeticCheckAlias, needValue, logger, !getBalancePeriod())

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
            date = row.docDate as Date
            from = new GregorianCalendar()
            from.setTime(date)
            from.set(Calendar.YEAR, from.get(Calendar.YEAR) - 3)
            def sum = 0 // сумма 12-х граф
            def periods = []
            def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, from.getTime(), date)
            isFind = false
            for (reportPeriod in reportPeriods) {
                def findFormData = formDataService.find(formData.formType.id, formData.kind, formData.departmentId,
                        reportPeriod.id)
                if (findFormData != null) {
                    for (findRow in formDataService.getDataRowHelper(findFormData).getAllCached()) {
                        // SBRFACCTAX-3531 исключать строку из той же самой формы не надо
                        if (findRow.code == row.code && findRow.docNumber == row.docNumber
                                && findRow.docDate == row.docDate && findRow.taxAccountingRuble > 0) {
                            isFind = true
                            sum += findRow.taxAccountingRuble
                            periods += (reportPeriod.name + " " + reportPeriod.taxPeriod.year)
                        }
                    }
                }
            }
            if (!(sum > row.ruble)) {
                logger.warn(errorMsg + "Операция в налоговом учете имеет сумму, меньше чем указано " +
                        "в бухгалтерском учете!" + (isFind ? " См. РНУ-7 в отчетных периодах: ${periods.join(", ")}." : ""))
            }
            if (!isFind) {
                logger.warn('Операция, указанная в строке %s, в налоговом учете за последние 3 года не проходила!',
                        row.getIndex())
            }
        }
    }

    // 8 . Проверка на уникальность записи по налоговому учету
    for (def map : uniq456.keySet()) {
        def rowList = uniq456.get(map)
        if (rowList.size() > 1) {
            loggerError("Несколько строк " + rowList.join(", ") + " содержат записи в налоговом учете для балансового " +
                    "счета=%s, документа № %s от %s.", refBookService.getStringValue(27, map.get(4), 'NUMBER').toString(),
                    map.get(5).toString(), dateFormat.format(map.get(6)))
        }
    }

    // 10. Арифметические проверки расчета итоговых строк «Итого по КНУ»
    totalRows.each { key, val ->
        if (val != sumRowsByCode[key]) {
            def msg = formData.createDataRow().getCell('taxAccountingRuble').column.name
            loggerError("Неверное итоговое значение по коду '$key' графы «$msg»!")
        }
    }
    totalRows2.each { key, val ->
        if (val != sumRowsByCode2[key]) {
            def msg = formData.createDataRow().getCell('ruble').column.name
            loggerError("Неверное итоговое значение по коду '$key' графы «$msg»!")
        }
    }

    // 11. Арифметические проверки расчета строки общих итогов
    checkTotalSum(dataRows, totalColumns, logger, !getBalancePeriod())
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

def getBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def msg, Object... args) {
    if (getBalancePeriod()) {
        logger.warn(msg, args)
    } else {
        logger.error(msg, args)
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

/*
 * Получение импортируемых данных
 */

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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
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
        def cols = (getBalancePeriod() ? balanceEditableColumns : editableColumns)
        cols.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        xmlIndexCol++

        // графа fix
        xmlIndexCol++

        xmlIndexCol++

        // графа 3
        newRow.date = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 2
        def id = getRecordIdImport(27, 'NUMBER', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        def map = formDataService.getRefBookValue(27, id, refBookCache)
        if (map != null) {
            def text = row.cell[2].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE?.stringValue)) || ((text == null || text.isEmpty()) && map.CODE?.stringValue != null)) {
                logger.error("Проверка файла: Строка ${xlsIndexRow}, столбец ${2 + colOffset} " +
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(27).getName() + "»!")
            }
        }
        xmlIndexCol++

        // графа 4
        newRow.code = id

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