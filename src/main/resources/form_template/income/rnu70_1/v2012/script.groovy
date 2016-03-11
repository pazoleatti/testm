package form_template.income.rnu70_1.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
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
        formDataService.saveCachedDataRows(formData, logger)
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
def groupColumns = ['name', 'number', 'date']

// Атрибуты для итогов
@Field
def totalColumns = ['income', 'financialResult1', 'perc', 'loss']

// Дата окончания отчетного периода
@Field
def endDate = null

// Дата начала отчетного периода
@Field
def startDate

// Дата окончания отчетного периода
@Field
def reportDate

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
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
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    reportDate = getReportDate()
    startDate = reportPeriodService.getStartDate(formData.reportPeriodId).getTime()

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // сортировка
    sortRows(dataRows, groupColumns)

    for (def row : dataRows) {
        row.financialResult1 = getFinancialResult1(row)
        row.rateBR = getRateBR(row, reportDate)
        row.perc = getPerc(row)
        row.loss = getLoss(row)
    }

    // Добавление подитогов
    addAllAliased(dataRows, new ScriptUtils.CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)

    // добавить строку "итого"
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)

    // Сортировка групп и строк
    sortFormDataRows(false)
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

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

    // Проверка наличия всех фиксированных строк
    // Проверка отсутствия лишних фиксированных строк
    // Проверка итоговых значений по фиксированным строкам
    checkItog(dataRows)

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

def isInPeriod(Date date) {
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
 * Получить подитоговую строку с заданными стилями.
 *
 * @param rowNumber номер строки
 * @param name надпись для "Итого по ..."
 * @param key ключ для сравнения подитоговых строк при импорте
 */
def getSubTotalRow(def rowNumber, def name, def key) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.setAlias('total' + key.toString() + '#' + rowNumber)
    newRow.fix = 'Итого по ' + name
    newRow.getCell('fix').colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
        newRow.getCell(it).editable = false
    }
    return newRow
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 15, 1)
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
            newRow.currencyDebtObligation = getRecordIdImport(15, 'CODE', "810", rnuIndexRow, 11 + colOffset, false)
        } else {
            newRow.currencyDebtObligation = getRecordIdImport(15, 'NAME', row.cell[11].text(), rnuIndexRow, 11 + colOffset, false)
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

    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = "Всего"
    totalRow.getCell('fix').colSpan = 2
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    rows.add(totalRow)

    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        // графа 9
        totalRow.income = parseNumber(row.cell[9].text(), rnuIndexRow, 9 + colOffset, logger, true)
        // графа 10
        totalRow.financialResult1 = parseNumber(row.cell[10].text(), rnuIndexRow, 10 + colOffset, logger, true)
        // графа 14
        totalRow.perc = parseNumber(row.cell[14].text(), rnuIndexRow, 14 + colOffset, logger, true)
        // графа 15
        totalRow.loss = parseNumber(row.cell[15].text(), rnuIndexRow, 15 + colOffset, logger, true)
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
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), getDataRow(dataRows, 'total'), true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias())}
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 16
    int HEADER_ROW_COUNT = 3
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
        if (rowValues[INDEX_FOR_SKIP] == "Всего") {
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (rowValues[INDEX_FOR_SKIP].contains("Итого по ")) {
            // для расчета уникального среди групп(groupColumns) ключа берем строку перед Подитоговой
            def tmpRowValue = rows[-1]
            def key = getKey(tmpRowValue)
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
            ([(headerRows[0][2]) : getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][3]) : getColumnName(tmpRow, 'inn')]),
            ([(headerRows[0][4]) : 'Договор цессии']),
            ([(headerRows[0][6]) : getColumnName(tmpRow, 'cost')]),
            ([(headerRows[0][7]) : getColumnName(tmpRow, 'repaymentDate')]),
            ([(headerRows[0][8]) : getColumnName(tmpRow, 'concessionsDate')]),
            ([(headerRows[0][9]) : getColumnName(tmpRow, 'income')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'financialResult1')]),
            ([(headerRows[0][11]): getColumnName(tmpRow, 'currencyDebtObligation')]),
            ([(headerRows[0][12]): getColumnName(tmpRow, 'rateBR')]),
            ([(headerRows[0][13]): getColumnName(tmpRow, 'interestRate')]),
            ([(headerRows[0][14]): getColumnName(tmpRow, 'perc')]),
            ([(headerRows[0][15]): getColumnName(tmpRow, 'loss')]),
            ([(headerRows[1][4]) : 'Номер']),
            ([(headerRows[1][5]) : 'Дата']),
            ([(headerRows[2][0]) : '1'])
    ]
    (2..15).each {
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
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    // графа 2..4
    def colIndex = 1
    ['name', 'inn', 'number'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 5
    colIndex++
    newRow.date = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6
    colIndex++
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7
    colIndex++
    newRow.repaymentDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 8
    colIndex++
    newRow.concessionsDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9
    colIndex++
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 10
    colIndex++
    newRow.financialResult1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 11
    colIndex++
    if (values[colIndex] == "Российский рубль") { // TODO http://jira.aplana.com/browse/SBRFACCTAX-6288
        newRow.currencyDebtObligation = getRecordIdImport(15, 'CODE', "810", fileRowIndex, colIndex + colOffset)
    } else {
        newRow.currencyDebtObligation = getRecordIdImport(15, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset)
    }

    // графа 12..15
    ['rateBR', 'interestRate', 'perc', 'loss'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
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
    // графа fix
    def title = values[1]
    def name = title.substring("Итого по ".size())?.trim()

    def newRow = getSubTotalRow(rowIndex, name, key)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 9
    def colIndex = 9
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 10
    colIndex = 10
    newRow.financialResult1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 14
    colIndex = 14
    newRow.perc = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 15
    colIndex = 15
    newRow.loss = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
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
    def newRow = getSubTotalRow(i, tmpRow?.name, key)

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
    checkItogRows(groupRows, testItogRows, itogRows, groupColumns, logger, new ScriptUtils.GroupString() {
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
    })
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def values = []
    def tmp

    // 2
    tmp = (row.name != null ? row.name : 'графа 2 не задана')
    values.add(tmp)

    // 4
    tmp = (row.number != null ? row.number : 'графа 4 не задана')
    values.add(tmp)

    // 5
    tmp = (row.date != null ? row.date?.format('dd.MM.yyyy') : 'графа 5 не задана')
    values.add(tmp)

    return values.join("; ")
}

/** Получить уникальный ключ группы. */
def getKey(def row) {
    def key = ''
    groupColumns.each { def alias ->
        key = key + (row[alias] != null ? row[alias] : "").toString()
    }
    return key.toLowerCase().hashCode()
}