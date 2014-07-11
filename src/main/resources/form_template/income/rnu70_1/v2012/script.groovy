package form_template.income.rnu70_1.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * (РНУ-70.1) Регистр налогового учёта уступки права требования до наступления предусмотренного кредитным договором
 * срока погашения основного долга
 * formTypeId=504
 *
 * @author Stanislav Yasinskiy
 * @author Lenar Haziev
 *
 * Графы:
 * 1    rowNumber                -      № пп
 * 2    name                     -      Наименование контрагента
 * 3    inn                      -      ИНН (его аналог)
 * 4    number                   -      Номер
 * 5    date                     -      Дата
 * 6    cost                     -      Стоимость права требования
 * 7    repaymentDate            -      Дата погашения основного долга
 * 8    concessionsDate          -      Дата уступки права требования
 * 9    income                   -      Доход (выручка) от уступки права требования
 * 10   financialResult1         -      Финансовый результат уступки права требования
 * 11   currencyDebtObligation   -      Валюта долгового обязательства
 * 12   rateBR                   -      Ставка Банка России
 * 13   interestRate             -      Ставка процента, установленная соглашением сторон
 * 14   perc                     -      Проценты по долговому обязательству, рассчитанные с учётом ст. 269 НК РФ за
 *                                      период от даты уступки права требования до даты платежа по договору
 * 15   loss                     -      Убыток, превышающий проценты по долговому обязательству, рассчитанные с учётом
 *                                      ст. 269 НК РФ за период от даты уступки  права требования до даты платежа по
 *                                      договору
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
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
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
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
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Все атрибуты
@Field
def allColumns = ['rowNumber', 'fix', 'name', 'inn', 'number', 'date', 'cost', 'repaymentDate', 'concessionsDate', 'income',
                  'financialResult1', 'currencyDebtObligation', 'rateBR', 'interestRate', 'perc', 'loss']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'inn', 'number', 'date', 'cost', 'repaymentDate', 'concessionsDate', 'income',
                       'currencyDebtObligation', 'interestRate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'inn', 'number', 'date', 'cost', 'repaymentDate', 'concessionsDate', 'income',
                       'financialResult1', 'currencyDebtObligation', 'perc', 'loss']

// алиасы графов для арифметической проверки
@Field
def arithmeticCheckAlias = ['financialResult1', 'perc', 'loss']

// Группируемые атрибуты
@Field
def groupColums = ['name', 'number', 'date']

// Атрибуты для итогов
@Field
def totalColumns = ['income', 'financialResult1', 'perc', 'loss']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Дата начала отчетного периода
@Field
def startDate

// Дата окончания отчетного периода
@Field
def reportDate

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

/**
 * Алгоритмы заполнения полей формы
 */
void calc() {
    //расчет
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    reportDate = getReportDate()
    startDate = reportPeriodService.getStartDate(formData.reportPeriodId).getTime()

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // сортировка
    sortRows(dataRows, groupColums)

    for (def row : dataRows) {
        row.financialResult1 = getFinancialResult1(row)
        row.rateBR = getRateBR(row, reportDate)
        row.perc = getPerc(row)
        row.loss = getLoss(row)
    }

    // посчитать "Итого по <Наименование контрагента>"
    def totalRows = getCalcTotalName(dataRows)
    // добавить "Итого по <Наименование контрагента>" в таблицу
    def i = 0
    totalRows.each { index, row ->
        dataRows.add(index + i++, row)
    }

    // добавить строку "итого"
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
    // используется save() т.к. есть сортировка
    dataRowHelper.save(dataRows)
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    reportDate = getReportDate()
    startDate = reportPeriodService.getStartDate(formData.reportPeriodId).getTime()

    for (def row : dataRows) {
        if (row?.getAlias() != null){
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка ${index}: "

        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        //  Проверка даты погашения основного долга «Графа 7» >= «Графа 8»
        if (row.concessionsDate != null && row.repaymentDate?.before(row.concessionsDate)) {
            rowError(logger, row, errorMsg + "Неверно указана дата погашения основного долга")
        }

        //  Проверка даты совершения операции «Графа 8» <= дата окончания отчётного периода
        if (row.concessionsDate != null && reportDate.before(row.concessionsDate)) {
            rowError(logger, row, errorMsg + "Неверно указана дата погашения основного долга")
        }

        def calcValues = [
                financialResult1: getFinancialResult1(row),
                perc: getPerc(row),
                loss: getLoss(row)
        ]
        //  Арифметическая проверка граф 10, 14, 15
        //  Графы 10, 14, 15 должны содержать значение, полученное согласно алгоритмам, описанным в разделе «Алгоритмы заполнения полей формы».
        checkCalc(row, arithmeticCheckAlias, calcValues, logger, true)

        //Проверка принадлежности даты графы 8 отчетному периоду
        if (row.concessionsDate != null && !isInPeriod(row.concessionsDate)) {
            rowError(logger, row, errorMsg + "«Графа 8» не принадлежит отчетному периоду")
        }
    }

    checkSubTotalSum(dataRows, totalColumns, logger, true)

    checkTotalSum(dataRows, totalColumns, logger, true)
}

/**
 * вычисляем значение графы 10
 */
def getFinancialResult1(def dataRow) {
    if (dataRow.income == null || dataRow.cost == null)
        return null
    return dataRow.income - dataRow.cost
}

/**
 * Получить курс валюты
 */
def getRateBR(def dataRow, def date) {
    def currency = dataRow.currencyDebtObligation
    if (currency != null && date != null)
        if (isRoublel(dataRow)) {
            return 1
        } else {
            return getRefBookRecord(22, 'CODE_NUMBER', "${currency}", date, -1, null, true)?.RATE?.getNumberValue()
        }
    return null
}

/**
 * вычисляем значение для графы 14
 */
BigDecimal getPerc(def dataRow) {
    if (dataRow.financialResult1 == null)
        return null
    if (dataRow.financialResult1 < 0) {
        if (dataRow.concessionsDate == null)
            return null
        final DateFormat dateFormat = new SimpleDateFormat('dd.MM.yyyy')
        final firstJan2010 = dateFormat.parse('01.01.2010')
        final thirtyFirstDec2013 = dateFormat.parse('31.12.2013')
        final firstJan2011 = dateFormat.parse('01.01.2011')

        BigDecimal x

        final repaymentDateDuration = getRepaymentDateDuration(dataRow)
        if (repaymentDateDuration == null)
            return null
        if (isRoublel(dataRow)) {
            if (dataRow.concessionsDate.after(firstJan2010) && dataRow.concessionsDate.before(thirtyFirstDec2013)) {
                if (dataRow.rateBR == null || dataRow.interestRate == null)
                    return null
                if (dataRow.rateBR * 1.8 <= dataRow.interestRate) {
                    x = getXByRateBR(dataRow, repaymentDateDuration, 1.8)
                } else {
                    x = getXByInterestRate(dataRow, repaymentDateDuration)
                }
                if (dataRow.interestRate == 0) x = getXByRateBR(dataRow, repaymentDateDuration, 1.8)
            } else {
                x = getXByRateBR(dataRow, repaymentDateDuration, 1.1)
            }
        } else {
            if (dataRow.concessionsDate.after(firstJan2011) && dataRow.concessionsDate.before(thirtyFirstDec2013)) {
                x = getXByRateBR(dataRow, repaymentDateDuration, 0.8)
            } else {
                x = getXByIncomeOnly(dataRow, repaymentDateDuration, 0.15)
            }
        }
        if (x == null)
            return null
        if (x.abs() > dataRow.financialResult1.abs()) {
            return dataRow.financialResult1
        } else {
            return x.setScale(2, BigDecimal.ROUND_HALF_UP)
        }
    } else {
        return new BigDecimal(0)
    }
}

/**
 * вычисляем значение для графы 15
 */
def getLoss(def dataRow) {
    if (dataRow.cost < 0 && dataRow.financialResult1 != null && dataRow.perc != null) {
        return dataRow.financialResult1.abs() - dataRow.perc
    } else {
        return new BigDecimal(0)
    }
}

/**
 * Проверка валюты на рубли
 */
def isRoublel(def dataRow) {
    return getRefBookValue(15, dataRow.currencyDebtObligation)?.CODE?.stringValue in ['810', '643']
}

def getRepaymentDateDuration(dataRow) {
    if (dataRow.repaymentDate == null || dataRow.concessionsDate == null)
        return null
    return (dataRow.repaymentDate - dataRow.concessionsDate) / getCountDaysInYear(new Date())    //(«графа 7» - «графа 8»)/365(366)
}

/**
 * Получить количество дней в году по указанной дате.
 */
def getCountDaysInYear(def date) {
    if (date == null) {
        return 0
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def year = date.format('yyyy')
    def end = format.parse("31.12.$year")
    def begin = format.parse("01.01.$year")
    return end - begin + 1
}

// вспомогательная функция для расчета графы 14
// «Графа 14» = «Графа 9» * «Графа 13» * («Графа 7» - «Графа 8») / 365(366);
BigDecimal getXByInterestRate(def dataRow, def repaymentDateDuration) {
    if (dataRow.income == null || dataRow.interestRate == null || repaymentDateDuration == null)
        return null
    x2 = dataRow.income * dataRow.interestRate * repaymentDateDuration
    return x2.setScale(2, BigDecimal.ROUND_HALF_UP)
}

// вспомогательная функция для расчета графы 14
// «Графа 14» = «Графа 9» * «Графа 12» * index * («Графа 7» – «Графа 8») / 365(366)
BigDecimal getXByRateBR(def dataRow, def repaymentDateDuration, BigDecimal index) {
    if (dataRow.income == null || dataRow.rateBR == null || repaymentDateDuration == null || index == null)
        return null
    x = dataRow.income * dataRow.rateBR * index * repaymentDateDuration
    return x.setScale(2, BigDecimal.ROUND_HALF_UP)
}

// вспомогательная функция для расчета графы 14
// «Графа 14» = «Графа 9» * 15% * («Графа 7» - «Графа 8») / 365(366);
BigDecimal getXByIncomeOnly(def dataRow, def repaymentDateDuration, BigDecimal index) {
    if (dataRow.income == null || repaymentDateDuration == null || index == null)
        return null
    x = dataRow.income * index * repaymentDateDuration
    return x.setScale(2, BigDecimal.ROUND_HALF_UP)
}

boolean isInPeriod(Date date) {
    if (reportDate == null || date == null)
        return null
    return reportDate.after(date) && startDate.before(date)
}

/**
 * Получить отчетную дату.
 */
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}

/** Сформировать итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.fix = "Всего"
    newRow.getCell('fix').colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

/**
 * Посчитать все итоговые строки "Итого по <Наименование контрагента>"
 */
def getCalcTotalName(def dataRows) {
    def totalRows = [:]
    dataRows.eachWithIndex { row, i ->
        DataRow<Cell> nextRow = getRow(i + 1, dataRows)
        // если код расходы поменялся то создать новую строку "Итого по <Наименование контрагента>"
        if (nextRow?.name != row.name || i == (dataRows.size() - 1)) {
            totalRows.put(i + 1, getCalcTotalNameRow(row, dataRows))
        }
    }
    return totalRows
}

/**
 * Посчитать строку "Итого по <Наименование контрагента>"
 */
def getCalcTotalNameRow(def row, def dataRows) {
    def tRow = getPrevRowWithoutAlias(row, dataRows)
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + tRow.name)
    newRow.fix = 'Итого по ' + tRow.name
    newRow.getCell('fix').colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
        newRow.getCell(it).editable = false
    }
    for (column in totalColumns) {
        newRow."$column" = new BigDecimal(0)
    }

    // идем от текущей позиции вверх и ищем нужные строки
    for (int j = dataRows.indexOf(tRow); j >= 0; j--) {
        srow = getRow(j, dataRows)

        if (srow?.getAlias() == null) {
            if (getRow(j, dataRows)?.name != tRow.name) {
                break
            }

            for (column in totalColumns) {
                if (srow?.get(column) != null) {
                    newRow."$column" = newRow.getCell(column).value + (BigDecimal) srow.get(column)
                }
            }
        }
    }
    return newRow
}

/**
 * Ищем вверх по форме первую строку без альяса
 */
DataRow getPrevRowWithoutAlias(DataRow row, def dataRows) {
    int pos = dataRows.indexOf(row)
    def prevRow
    for (int i = pos; i >= 0; i--) {
        prevRow = getRow(i, dataRows)
        if (prevRow?.getAlias() == null) {
            return prevRow
        }
    }
    throw new IllegalArgumentException()
}

/**
 * Получение строки по номеру
 * @author ivildanov
 */
DataRow<Cell> getRow(int i, def dataRows) {
    if ((i < dataRows.size()) && (i >= 0)) {
        return dataRows.get(i)
    } else {
        return null
    }
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 15, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Наименование контрагента',
            (xml.row[0].cell[3]): 'ИНН (его аналог)',
            (xml.row[0].cell[4]): 'Договор цессии',
            (xml.row[0].cell[6]): 'Стоимость права требования',
            (xml.row[0].cell[7]): 'Дата погашения основного долга',
            (xml.row[0].cell[8]): 'Дата уступки права требования',
            (xml.row[0].cell[9]): 'Доход (выручка) от уступки права требования',
            (xml.row[0].cell[10]): 'Финансовый результат уступки права требования',
            (xml.row[0].cell[11]): 'Валюта долгового обязательства',
            (xml.row[0].cell[12]): 'Ставка Банка России',
            (xml.row[0].cell[13]): 'Ставка процента, установленная соглашением сторон',
            (xml.row[0].cell[14]): 'Проценты по долговому обязательству, рассчитанные с учётом ст. 269 НК РФ за период от даты уступки права требования до даты платежа по договору',
            (xml.row[0].cell[15]): 'Убыток, превышающий проценты по долговому обязательству, рассчитанные с учётом ст. 269 НК РФ за период от даты уступки права требования до даты платежа по договору',
            (xml.row[1].cell[4]): 'Номер',
            (xml.row[1].cell[5]): 'Дата',
            (xml.row[2].cell[0]): '1'
    ]
    (2..15).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
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
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 2
        newRow.name = row.cell[2].text()

        // графа 3
        newRow.inn = row.cell[3].text()

        // графа 4
        newRow.number = row.cell[4].text()

        // графа 5
        newRow.date = parseDate(row.cell[5].text(), "dd.MM.yyyy", xlsIndexRow, 5 + colOffset, logger, true)

        // графа 6
        newRow.cost = parseNumber(row.cell[6].text(), xlsIndexRow, 6 + colOffset, logger, true)

        // графа 7
        newRow.repaymentDate = parseDate(row.cell[7].text(), "dd.MM.yyyy", xlsIndexRow, 7 + colOffset, logger, true)

        // графа 8
        newRow.concessionsDate = parseDate(row.cell[8].text(), "dd.MM.yyyy", xlsIndexRow, 8 + colOffset, logger, true)

        // графа 9
        newRow.income = parseNumber(row.cell[9].text(), xlsIndexRow, 9 + colOffset, logger, true)

        // графа 10

        // графа 11
        if (row.cell[11].text() == "Российский рубль") { // TODO http://jira.aplana.com/browse/SBRFACCTAX-6288
            newRow.currencyDebtObligation = getRecordIdImport(15, 'CODE', "810", xlsIndexRow, 11 + colOffset)
        } else {
            newRow.currencyDebtObligation = getRecordIdImport(15, 'NAME', row.cell[11].text(), xlsIndexRow, 11 + colOffset)
        }

        // графа 12

        // графа 13
        newRow.interestRate = parseNumber(row.cell[13].text(), xlsIndexRow, 13 + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName)
    addTransportData(xml)

    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void addTransportData(def xml) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
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
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 2
        newRow.name = row.cell[2].text()
        // графа 3
        newRow.inn = row.cell[3].text()
        // графа 4
        newRow.number = row.cell[4].text()
        // графа 5
        newRow.date = parseDate(row.cell[5].text(), "dd.MM.yyyy", rnuIndexRow, 5 + colOffset, logger, true)
        // графа 6
        newRow.cost = parseNumber(row.cell[6].text(), rnuIndexRow, 6 + colOffset, logger, true)
        // графа 7
        newRow.repaymentDate = parseDate(row.cell[7].text(), "dd.MM.yyyy", rnuIndexRow, 7 + colOffset, logger, true)
        // графа 8
        newRow.concessionsDate = parseDate(row.cell[8].text(), "dd.MM.yyyy", rnuIndexRow, 8 + colOffset, logger, true)
        // графа 9
        newRow.income = parseNumber(row.cell[9].text(), rnuIndexRow, 9 + colOffset, logger, true)
        // графа 10
        newRow.financialResult1 = parseNumber(row.cell[10].text(), rnuIndexRow, 10 + colOffset, logger, true)
        // графа 11
        if (row.cell[11].text() == "Российский рубль") { // TODO http://jira.aplana.com/browse/SBRFACCTAX-6288
            newRow.currencyDebtObligation = getRecordIdImport(15, 'CODE', "810", rnuIndexRow, 11 + colOffset)
        } else {
            newRow.currencyDebtObligation = getRecordIdImport(15, 'NAME', row.cell[11].text(), rnuIndexRow, 11 + colOffset)
        }
        // графа 12
        newRow.rateBR = parseNumber(row.cell[12].text(), rnuIndexRow, 12 + colOffset, logger, true)
        // графа 13
        newRow.interestRate = parseNumber(row.cell[13].text(), rnuIndexRow, 13 + colOffset, logger, true)
        // графа 14
        newRow.perc = parseNumber(row.cell[14].text(), rnuIndexRow, 14 + colOffset, logger, true)
        // графа 15
        newRow.loss = parseNumber(row.cell[15].text(), rnuIndexRow, 15 + colOffset, logger, true)

        rows.add(newRow)
    }

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()
        total.setAlias('total')
        total.fix = "Всего"
        total.getCell('fix').colSpan = 2
        allColumns.each {
            total.getCell(it).setStyleAlias('Контрольные суммы')
        }

        // графа 9
        total.income = parseNumber(row.cell[9].text(), rnuIndexRow, 9 + colOffset, logger, true)
        // графа 10
        total.financialResult1 = parseNumber(row.cell[10].text(), rnuIndexRow, 10 + colOffset, logger, true)
        // графа 14
        total.perc = parseNumber(row.cell[14].text(), rnuIndexRow, 14 + colOffset, logger, true)
        // графа 15
        total.loss = parseNumber(row.cell[15].text(), rnuIndexRow, 15 + colOffset, logger, true)

        rows.add(total)
    }

    dataRowHelper.save(rows)
}