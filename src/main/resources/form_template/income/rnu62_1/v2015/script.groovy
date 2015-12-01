package form_template.income.rnu62_1.v2015

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-62) Регистр налогового учёта расходов по дисконтным векселям ПАО Сбербанк (с 9 месяцев 2015)"
 * formTypeId=423 (354 у старого макета)
 *
 * @author bkinzyabulatov
 */

@Field
def formTypeIdOld = 354

// графа 1  rowNumber           № пп
// графа 2  billNumber          Номер векселя
// графа 3  creationDate        Дата составления
// графа 4  nominal             Номинал
// графа 5  sellingPrice        Цена реализации
// графа 6  currencyCode        Код валюты - Справочник 15 «Общероссийский классификатор валют» - 64 атрибут CODE «Код валюты. Цифровой»
// графа 7  rateBRBillDate      Курс Банка России на дату составления векселя
// графа 8  rateBROperationDate Курс Банка России на дату совершения операции
// графа 9  paymentTermStart    Дата наступления срока платежа
// графа 10 paymentTermEnd      Дата окончания срока платежа
// графа 11 interestRate        Процентная ставка
// графа 12 operationDate       Дата совершения операции
// графа 13 rateWithDiscCoef    Ставка с учётом дисконтирующего коэффициента
// графа 14 sumStartInCurrency  Сумма дисконта начисленного на начало отчётного периода в валюте
// графа 15 sumStartInRub       Сумма дисконта начисленного на начало отчётного периода в рублях
// графа 16 sumEndInCurrency    Сумма дисконта начисленного на конец отчётного периода в валюте
// графа 17 sumEndInRub         Сумма дисконта начисленного на конец отчётного периода в рублях
// графа 18 sum                 Сумма дисконта начисленного за отчётный период (руб.)

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
        def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
        formDataService.addRow(formData, currentDataRow, columns, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (!currentDataRow?.getAlias()?.contains('itg')) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationTotal(formData, logger, userInfo, ['itg'])
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

//Все аттрибуты
@Field
def allColumns = ['rowNumber', 'fix', 'billNumber', 'creationDate', 'nominal', 'sellingPrice',
                  'currencyCode', 'rateBRBillDate', 'rateBROperationDate',
                  'paymentTermStart', 'paymentTermEnd', 'interestRate',
                  'operationDate', 'rateWithDiscCoef', 'sumStartInCurrency',
                  'sumStartInRub', 'sumEndInCurrency', 'sumEndInRub', 'sum']

// Поля, для которых подсчитываются итоговые значения (графа 18)
@Field
def totalColumns = ['sum']

// Редактируемые атрибуты (графа 2..13)
@Field
def editableColumns = ['billNumber', 'creationDate', 'nominal', 'sellingPrice', 'currencyCode',
                       'paymentTermStart', 'paymentTermEnd', 'interestRate', 'operationDate', 'rateWithDiscCoef']

// Автозаполняемые атрибуты
@Field
def arithmeticCheckAlias = allColumns - editableColumns

@Field
def arithmeticCheckAliasWithoutNSI = ['sumStartInCurrency', 'sumStartInRub', 'sumEndInCurrency', 'sumEndInRub', 'sum']

// Сортируемые атрибуты (графа 2, 12)
@Field
def sortColumns = ['billNumber', 'operationDate']

// Автозаполняемые атрибуты (графа 1..12, 15, 18)
@Field
def nonEmptyColumns = ['billNumber', 'creationDate', 'nominal', 'sellingPrice', 'currencyCode', 'rateBRBillDate',
                       'rateBROperationDate', 'paymentTermStart', 'paymentTermEnd', 'interestRate', 'operationDate',
                       'sumStartInRub', 'sum']

/** Признак периода ввода остатков. */
@Field
def isBalancePeriod = null

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

//// Некастомные методы

def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

/** Получить курс валюты */
BigDecimal getCourse(def row, def date) {
    def currency = row.currencyCode
    if (date == null || currency == null) {
        return null
    }
    def isRuble = isRubleCurrency(currency)
    if (!isRuble) {
        def res = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache,
                'CODE_NUMBER', currency.toString(), date, row.getIndex(), getColumnName(row, "currencyCode"), logger, false)
        return res?.RATE?.numberValue
    }
    return 1;
}

/** Проверка валюты на рубли */
def isRubleCurrency(def currencyCode) {
    if (currencyCode == null) {
        return null
    }
    return formDataService.getRefBookValue(15, currencyCode.toLong(), refBookCache)?.CODE?.stringValue in ['810', '643']
}

/** Количество дней в году за который делаем */
int getCountDaysInYear(def Date date) {
    def calendar = Calendar.getInstance()
    calendar.setTime(date)
    return (new GregorianCalendar()).isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365
}

def int getDiffBetweenYears(def Date dateA, def Date dateB) {
    def calendarA = Calendar.getInstance()
    calendarA.setTime(dateA)
    def calendarB = Calendar.getInstance()
    calendarB.setTime(dateB)
    return calendarA.get(Calendar.YEAR) - calendarB.get(Calendar.YEAR)
}

//// Кастомные методы
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()

    def totalRow = null
    def sum = 0
    def dataRowsPrev = null
    def countDaysInYear = null
    if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
        dataRowsPrev = getDataRowsPrev()
        countDaysInYear = getCountDaysInYear(dFrom)
    }

    for (def DataRow row : dataRows) {
        if (row?.getAlias()?.contains('itg')) {
            totalRow = row
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        // 2. Проверка даты совершения операции и границ отчетного периода
        if (row.operationDate != null && (row.operationDate < dFrom || dTo < row.operationDate)) {
            loggerError(row, errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }

        // 4. Проверка на нулевые значения
        if (!row.sumStartInCurrency && !row.sumStartInRub && !row.sumEndInCurrency && !row.sumEndInRub && !row.sum) {
            loggerError(row, errorMsg + "Все суммы по операции нулевые!")
        }

        if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
            // 5. Проверка существования необходимых экземпляров форм
            def rowPrev = getRowPrev(dataRowsPrev, row)
            if (rowPrev == null) {
                rowError(logger, row, errorMsg + "Отсутствуют данные в РНУ-62 за предыдущий отчетный период!")
            }

            // 6. Арифметическая проверка граф 14-18
            def values = [:]
            values.sumStartInCurrency = calc14(row, rowPrev)
            values.sumStartInRub = calc15(rowPrev)
            values.sumEndInCurrency = calc16(row, countDaysInYear)
            values.sumEndInRub = calc17(row)
            values.sum = calc18(row)
            checkCalc(row, arithmeticCheckAliasWithoutNSI, values, logger, !isBalancePeriod())
        }

        // Проверки НСИ (графа 7, 8)
        if (row.rateBRBillDate != calc7(row)) {
            rowWarning(logger, row, errorMsg + "В справочнике «Курсы валют» не найдено значение «${row.rateBRBillDate}» в поле «${getColumnName(row, 'rateBRBillDate')}»!")
        }
        if (row.rateBROperationDate != calc8(row)) {
            rowWarning(logger, row, errorMsg + "В справочнике «Курсы валют» не найдено значение «${row.rateBROperationDate}» в поле «${getColumnName(row, 'rateBROperationDate')}»!")
        }

        // 7. Проверка итоговых значений по всей форме
        sum += (row.sum ?: 0)
    }

    // 7. Проверка итоговых значений по всей форме
    if (totalRow != null) {
        checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod())
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // Удаление итогов
    deleteAllAliased(dataRows)

    if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
        def dataRowsPrev = getDataRowsPrev()
        def dFrom = getReportPeriodStartDate()
        def countDaysInYear = getCountDaysInYear(dFrom)

        // Расчет ячеек
        for (def row : dataRows) {
            if (row.getAlias() != null) {
                continue
            }
            def rowPrev = getRowPrev(dataRowsPrev, row)

            row.rateBRBillDate = calc7(row)
            row.rateBROperationDate = calc8(row)
            row.sumStartInCurrency = calc14(row, rowPrev)
            row.sumStartInRub = calc15(rowPrev)
            row.sumEndInCurrency = calc16(row, countDaysInYear)
            row.sumEndInRub = calc17(row)
            row.sum = calc18(row)
        }
    }
    // Добавление итогов
    dataRows.add(getTotalRow(dataRows))
}

// Расчет итоговой строки
def getTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('itg')
    totalRow.fix = 'Итого'
    totalRow.getCell("fix").setColSpan(2)
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

def BigDecimal calc7(def row) {
    // курс валюты из графы 6 на дату из графы 3
    return round(getCourse(row, row.creationDate))
}

def BigDecimal calc8(def row) {
    // курс валюты из графы 6 на дату из графы 12
    return round(getCourse(row, row.operationDate))
}

def BigDecimal calc14(def row, def rowPrev) {
    if (!isRubleCurrency(row.currencyCode)) {
        return null
    } else {
        return round(rowPrev != null ? rowPrev.sumEndInCurrency : BigDecimal.ZERO)
    }
}

def BigDecimal calc15(def rowPrev) {
    return round((rowPrev != null && rowPrev.sumEndInRub) ? rowPrev.sumEndInRub : BigDecimal.ZERO)
}

def BigDecimal calc16(def row, def countDaysInYear) {
    def tmp = null
    if (row.operationDate != null && row.paymentTermStart != null && row.operationDate < row.paymentTermStart) {
        if (row.nominal != null && row.sellingPrice != null && row.creationDate != null && (row.paymentTermStart - row.creationDate != 0)) {
            tmp = (row.nominal - row.sellingPrice) * (row.operationDate - row.creationDate) / (row.paymentTermStart - row.creationDate)
        }
        return round(tmp)
    }
    if (row.operationDate != null && row.paymentTermStart != null && row.operationDate > row.paymentTermStart) {
        if (row.nominal != null && row.sellingPrice != null) {
            tmp = row.nominal - row.sellingPrice
        }
        return round(tmp)
    }
    if (row.rateWithDiscCoef != null) {
        if (row.creationDate != null && row.paymentTermEnd != null) {
            // количество дней в году указаном в 3 графе
            def countDays3 = getCountDaysInYear(row.creationDate)
            // количество дней в году указаном в 10 графе
            def countDays10 = getCountDaysInYear(row.paymentTermEnd)
            // если количество дней в графе 3 и 10 отличается
            if (countDays3 != 0 && countDays10 != 0 && (countDays3 != countDays10)) {
                def d1 = row.creationDate
                def d2 = row.paymentTermEnd
                // дата начала года между датами в графе 3 и 10
                def c = Calendar.getInstance()
                c.clear()
                c.set(Calendar.YEAR, (d1 > d2 ? d1 : d2).format('yyyy').toInteger())
                c.set(Calendar.MONTH, Calendar.JANUARY)
                c.set(Calendar.DAY_OF_MONTH, 1)

                // количество дней между датами из «Графы 10» и «Графы 3», попавших в первый год и во второй год
                def t1 = (d1 > d2 ? d1 - c.time + 1 : c.time - d1 - 1)
                def t2 = (d2 > d1 ? d2 - c.time + 1 : c.time - d2 - 1)
                tmp = row.nominal + subCalc16(row, t1, countDays3) + subCalc16(row, t2, countDays10)
                return round(tmp)
            }
        }

        def tmpForMultiplication = row.rateWithDiscCoef
        if (row.interestRate != null && row.interestRate < row.rateWithDiscCoef) {
            tmpForMultiplication = row.interestRate
        }
        if (row.operationDate != null && row.creationDate != null) {
            //TODO деление и округление
            tmp = row.sellingPrice * (row.operationDate - row.creationDate) / countDaysInYear * tmpForMultiplication / 100
        }
        return round(tmp)
    }

    if (row.paymentTermEnd != null && row.operationDate != null && getDiffBetweenYears(row.paymentTermEnd, row.operationDate) >= 3) {
        return round(BigDecimal.ZERO)
    }
    // погашеный вылютный вексель - это если в графе 6 не рубли
    if (!isRubleCurrency(row.currencyCode)) {
        tmp = null
    }
    return round(tmp)
}

def subCalc16(def row, def t, def days) {
    //TODO деление и округление
    return (row.nominal * t * row.interestRate * (row.rateWithDiscCoef - row.interestRate)) / days * 100
}

def BigDecimal calc17(def row) {
    def tmp = null
    if (row.rateWithDiscCoef != null && row.sumStartInCurrency != null && row.sumEndInCurrency != null) {
        if (row.operationDate != null && row.paymentTermStart != null) {
            if (row.operationDate >= row.paymentTermStart &&
                    row.nominal != null && row.rateBROperationDate != null &&
                    row.sellingPrice != null && row.rateBRBillDate != null) {
                tmp = (row.nominal * row.rateBROperationDate) - (row.sellingPrice * row.rateBRBillDate)
            } else if (row.sumStartInRub != null && row.rateBROperationDate != null &&
                    row.sellingPrice != null && row.rateBRBillDate != null) {
                // иначе второй строкой
                tmp = round(row.sellingPrice * (row.rateBROperationDate - row.rateBRBillDate)) + row.sumStartInRub
            }
        }
    } else if (row.rateWithDiscCoef == null && row.sumStartInCurrency == null && row.sumEndInCurrency != null &&
            row.rateBROperationDate != null) {
        tmp = row.sumEndInCurrency * row.rateBROperationDate
    }
    return round(tmp)
}

def BigDecimal calc18(def row) {
    return (row.sumEndInRub != null && row.sellingPrice != null) ? round(row.sumEndInRub - row.sellingPrice) : null
}

def DataRow getRowPrev(def dataRowsPrev, def DataRow row) {
    for (def rowPrev : dataRowsPrev) {
        if (rowPrev?.getAlias() == null && row.billNumber != null && row.billNumber == rowPrev.billNumber) {
            return rowPrev
        }
    }
    return null
}

def getDataRowsPrev() {
    def formDataPrev = formDataService.getFormDataPrev(formData)
    formDataPrev = formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null
    if (formDataPrev == null) {
        def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.getReportPeriodId());
        if (prevReportPeriod != null) {
            // Последний экземпляр
            formDataPrev = formDataService.getLast(formTypeIdOld, formData.getKind(), formData.getDepartmentId(), prevReportPeriod.getId(), null, formData.comparativePeriodId, formData.accruing);
            formDataPrev = formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null
        }
    }
    if (formDataPrev == null) {
        logger.error("Не найдены экземпляры РНУ-62 за прошлый отчетный период!")
    } else {
        return formDataService.getDataRowHelper(formDataPrev)?.allSaved
    }
    return null
}

BigDecimal round(BigDecimal value, int newScale = 2) {
    return value?.setScale(newScale, RoundingMode.HALF_UP)
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
void prevPeriodCheck() {
    if (formData.kind == FormDataKind.PRIMARY && !isBalancePeriod()) {
        checkFormExistAndAccepted([formData.formType.id, formTypeIdOld], FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, true, logger, true)
    }
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
def loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 18, 1)
    addTransportData(xml)

    // TODO (Ramil Timerbaev) возможно надо поменять на общее сообщение TRANSPORT_FILE_SUM_ERROR
    if (isBalancePeriod() && !logger.containsLevel(LogLevel.ERROR)) {
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
        def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
        columns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        (allColumns - columns).each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 2
        newRow.billNumber = row.cell[2].text()
        // графа 3
        def xlsIndexCol = 3
        newRow.creationDate = parseDate(row.cell[xlsIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xlsIndexCol + colOffset, logger, true)
        // графа 4
        xlsIndexCol = 4
        newRow.nominal = parseNumber(row.cell[xlsIndexCol].text(), rnuIndexRow, xlsIndexCol + colOffset, logger, true)
        // графа 5
        xlsIndexCol = 5
        newRow.sellingPrice = parseNumber(row.cell[xlsIndexCol].text(), rnuIndexRow, xlsIndexCol + colOffset, logger, true)
        // графа 6
        xlsIndexCol = 6
        newRow.currencyCode = getRecordIdImport(15, 'CODE', row.cell[xlsIndexCol].text(), rnuIndexRow, xlsIndexCol + colOffset, false)
        if (isBalancePeriod()) {
            // графа 7
            xlsIndexCol = 7
            newRow.rateBRBillDate = parseNumber(row.cell[xlsIndexCol].text(), rnuIndexRow, xlsIndexCol + colOffset, logger, true)
            // графа 8
            xlsIndexCol = 8
            newRow.rateBROperationDate = parseNumber(row.cell[xlsIndexCol].text(), rnuIndexRow, xlsIndexCol + colOffset, logger, true)
        }
        // графа 9
        xlsIndexCol = 9
        newRow.paymentTermStart = parseDate(row.cell[xlsIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xlsIndexCol + colOffset, logger, true)
        // графа 10
        xlsIndexCol = 10
        newRow.paymentTermEnd = parseDate(row.cell[xlsIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xlsIndexCol + colOffset, logger, true)
        // графа 11
        xlsIndexCol = 11
        newRow.interestRate = parseNumber(row.cell[xlsIndexCol].text(), rnuIndexRow, xlsIndexCol + colOffset, logger, true)
        // графа 12
        xlsIndexCol = 12
        newRow.operationDate = parseDate(row.cell[xlsIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xlsIndexCol + colOffset, logger, true)
        // графа 13 (было 13..18)
        (isBalancePeriod() ? ['rateWithDiscCoef', 'sumStartInCurrency', 'sumStartInRub', 'sumEndInCurrency', 'sumEndInRub', 'sum'] : ['rateWithDiscCoef']).each { alias ->
            xlsIndexCol++
            newRow[alias] = parseNumber(row.cell[xlsIndexCol].text(), rnuIndexRow, xlsIndexCol + colOffset, logger, true)
        }

        rows.add(newRow)
    }
    def totalRow = formData.createDataRow()
    totalRow.setAlias('itg')
    totalRow.fix = 'Итого'
    totalRow.getCell("fix").colSpan = 2
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    rows.add(totalRow)

    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        // графа 18
        xlsIndexCol = 18
        totalRow.sum = parseNumber(row.cell[xlsIndexCol].text(), rnuIndexRow, xlsIndexCol + colOffset, logger, true)

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
    sortRows(refBookService, logger, dataRows, null, getDataRow(dataRows, 'itg'), null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 19
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

    // сравнение итогов (итоговое поле не загружается, т.к. нередактируемое)
    if (isBalancePeriod()) {
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
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'billNumber')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'creationDate')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'nominal')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'sellingPrice')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'currencyCode')]),
            ([(headerRows[0][7]): 'Курс Банка России']),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'paymentTermStart')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'paymentTermEnd')]),
            ([(headerRows[0][11]): getColumnName(tmpRow, 'interestRate')]),
            ([(headerRows[0][12]): getColumnName(tmpRow, 'operationDate')]),
            ([(headerRows[0][13]): getColumnName(tmpRow, 'rateWithDiscCoef')]),
            ([(headerRows[0][14]): 'Сумма дисконта начисленного на начало отчётного периода']),
            ([(headerRows[0][16]): 'Сумма дисконта начисленного на конец отчётного периода']),
            ([(headerRows[0][18]): getColumnName(tmpRow, 'sum')]),
            ([(headerRows[1][7]): 'на дату составления векселя']),
            ([(headerRows[1][8]): 'на дату совершения операции']),
            ([(headerRows[1][14]): 'в валюте']),
            ([(headerRows[1][15]): 'в рублях']),
            ([(headerRows[1][16]): 'в валюте']),
            ([(headerRows[1][17]): 'в рублях']),
            ([(headerRows[2][0]): '1'])
    ]
    (2..18).each { index ->
        headerMapping.add([(headerRows[2][index]): index.toString()])
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
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, boolean isTotal = false) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
    columns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    (allColumns - columns).each {
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
    newRow.sellingPrice = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6
    colIndex++
    newRow.currencyCode = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 7
    colIndex++
    if (isTotal || isBalancePeriod()) {
        newRow.rateBRBillDate = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 8
    colIndex++
    if (isTotal || isBalancePeriod()) {
        newRow.rateBROperationDate = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 9
    colIndex++
    newRow.paymentTermStart = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 10
    colIndex++
    newRow.paymentTermEnd = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 11
    colIndex++
    newRow.interestRate = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 12
    colIndex++
    newRow.operationDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 13 (для итогов 13..18)
    ((isTotal || isBalancePeriod()) ? ['rateWithDiscCoef', 'sumStartInCurrency', 'sumStartInRub', 'sumEndInCurrency', 'sumEndInRub', 'sum'] :['rateWithDiscCoef']).each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

// аналогичен FormDataServiceImpl.checkFormExistAndAccepted, только проверяет несколько типов форм, вместо одного
def checkFormExistAndAccepted(List<Integer> formTypeIds, FormDataKind kind, int departmentId,
                                      int currentReportPeriodId, Boolean prevPeriod,
                                      def logger, boolean required) {
    // определение периода формы
    def reportPeriod;
    if (prevPeriod) {
        reportPeriod = reportPeriodService.getPrevReportPeriod(currentReportPeriodId);
    } else {
        reportPeriod = reportPeriodService.get(currentReportPeriodId);
    }

    // получение данных формы
    def formDataCheck = null;
    boolean foundData = false;
    if (reportPeriod != null) {
        for (int i = 0; i < formTypeIds.size() && !foundData; i++) {
            def formTypeId = formTypeIds[i]
            formDataCheck = formDataService.getLast(formTypeId, kind, departmentId, reportPeriod.getId(), null, formData.comparativePeriodId, formData.accruing);
            // проверка существования, принятости и наличия данных
            if (formDataCheck != null && formDataCheck.getState() == WorkflowState.ACCEPTED) {
                def dataRowHelper = formDataService.getDataRowHelper(formDataCheck);
                def dataRows = dataRowHelper.getAllCached();
                foundData = dataRows != null && !dataRows.isEmpty();
            }
        }
    }


    // выводить ли сообщение
    if (!foundData) {
        String formName = (formDataCheck == null ? formTypeService.get(formTypeIds[0]).getName() : formDataCheck.getFormType().getName());
        // период может не найтись для предыдущего периода, потому что периода не существует
        String periodName = "предыдущий период";
        if (reportPeriod != null) {
            periodName = reportPeriod.getName() + " " + reportPeriod.getTaxPeriod().getYear();
        }
        String msg = String.format("Не найдены экземпляры «%s» за %s в статусе «Принята». Расчеты не могут быть выполнены.", formName, periodName);
        if (required) {
            logger.error("%s", msg);
        } else {
            logger.warn("%s", msg);
        }
    }
}
