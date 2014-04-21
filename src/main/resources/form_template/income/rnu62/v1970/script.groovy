package form_template.income.rnu62.v1970

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-62) Регистр налогового учёта расходов по дисконтным векселям ОАО «Сбербанк России»"
 * formTemplateId=354
 *
 * @author bkinzyabulatov
 */

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
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationTotal(formData, formDataDepartment.id, logger, ['itg'])
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
def nonEmptyColumns = ['rowNumber', 'billNumber', 'creationDate', 'nominal',
        'sellingPrice', 'currencyCode', 'rateBRBillDate', 'rateBROperationDate',
        'paymentTermStart', 'paymentTermEnd', 'interestRate', 'operationDate', 'sumStartInRub', 'sum']

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
    return formDataService.getRefBookValue(15, currencyCode.toLong(), refBookCache)?.CODE?.stringValue == '810'
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

    // Номер последний строки предыдущей формы
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

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
            loggerError(errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }

        // 3. Проверка на уникальность поля «№ пп»
        if (++i != row.rowNumber) {
            loggerError(errorMsg + 'Нарушена уникальность номера по порядку!')
        }
        // 4. Проверка на нулевые значения
        if (!row.sumStartInCurrency && !row.sumStartInRub && !row.sumEndInCurrency && !row.sumEndInRub && !row.sum) {
            loggerError(errorMsg + "Все суммы по операции нулевые!")
        }

        if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
            // 5. Проверка существования необходимых экземпляров форм
            def rowPrev = getRowPrev(dataRowsPrev, row)
            if (rowPrev == null) {
                logger.error(errorMsg + "Отсутствуют данные в РНУ-62 за предыдущий отчетный период!")
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
            logger.warn(errorMsg + "В справочнике «Курсы валют» не найдено значение «${row.rateBRBillDate}» в поле «${getColumnName(row, 'rateBRBillDate')}»!")
        }
        if (row.rateBROperationDate != calc8(row)) {
            logger.warn(errorMsg + "В справочнике «Курсы валют» не найдено значение «${row.rateBROperationDate}» в поле «${getColumnName(row, 'rateBROperationDate')}»!")
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    if (formDataEvent != FormDataEvent.IMPORT) {
        sortRows(dataRows, sortColumns)
    }

    // Удаление итогов
    deleteAllAliased(dataRows)

    // Номер последней строки предыдущей формы
    def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

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

            row.rowNumber = ++index
            row.rateBRBillDate = calc7(row)
            row.rateBROperationDate = calc8(row)
            row.sumStartInCurrency = calc14(row, rowPrev)
            row.sumStartInRub = calc15(rowPrev)
            row.sumEndInCurrency = calc16(row, countDaysInYear)
            row.sumEndInRub = calc17(row)
            row.sum = calc18(row)
        }
    } else {
        for (def row : dataRows) {
            if (row.getAlias() != null) {
                continue
            }

            row.rowNumber = ++index
        }
    }
    // Добавление итогов
    dataRows.add(getTotalRow(dataRows))

    dataRowHelper.save(dataRows)
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
    return round(rowPrev != null ? rowPrev.sumEndInRub : BigDecimal.ZERO)
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
        tmp = row.sellingPrice * (row.operationDate - row.creationDate) / countDaysInYear * tmpForMultiplication / 100
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
    def formDataPrev = formDataService.getFormDataPrev(formData, formData.departmentId)
    formDataPrev = formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null
    if (formDataPrev == null) {
        logger.error("Не найдены экземпляры РНУ-62 за прошлый отчетный период!")
    } else {
        return formDataService.getDataRowHelper(formDataPrev)?.allCached
    }
    return null
}

BigDecimal round(BigDecimal value, int newScale = 2) {
    return value?.setScale(newScale, RoundingMode.HALF_UP)
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков.
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
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
    if (!isBalancePeriod() && !formDataService.existAcceptedFormDataPrev(formData, formData.departmentId)) {
        throw new ServiceException('Не найдены экземпляры РНУ-62 за прошлый отчетный период!')
    }
}

def loggerError(def msg) {
    if (isBalancePeriod()) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 18, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Номер векселя',
            (xml.row[0].cell[3]): 'Дата составления',
            (xml.row[0].cell[4]): 'Номинал',
            (xml.row[0].cell[5]): 'Цена реализации',
            (xml.row[0].cell[6]): 'Код валюты',
            (xml.row[0].cell[7]): 'Курс Банка России',
            (xml.row[0].cell[9]): 'Дата наступления срока платежа',
            (xml.row[0].cell[10]): 'Дата окончания срока платежа',
            (xml.row[0].cell[11]): 'Процентная ставка',
            (xml.row[0].cell[12]): 'Дата совершения операции',
            (xml.row[0].cell[13]): 'Ставка с учётом дисконтирующего коэффициента',
            (xml.row[0].cell[14]): 'Сумма дисконта начисленного на начало отчётного периода',
            (xml.row[0].cell[16]): 'Сумма дисконта начисленного на конец отчётного периода',
            (xml.row[0].cell[18]): 'Сумма дисконта начисленного за отчётный период (руб.)',
            (xml.row[1].cell[7]): 'на дату составления векселя',
            (xml.row[1].cell[8]): 'на дату совершения операции',
            (xml.row[1].cell[14]): 'в валюте',
            (xml.row[1].cell[15]): 'в рублях',
            (xml.row[1].cell[16]): 'в валюте',
            (xml.row[1].cell[17]): 'в рублях',
            (xml.row[2].cell[0]): '1'
    ]
    (2..18).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 3)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
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
        if (xmlIndexRow <= headRowCount - 1) {
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
        newRow.creationDate = parseDate(row.cell[xlsIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 4
        xlsIndexCol = 4
        newRow.nominal = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 5
        xlsIndexCol = 5
        newRow.sellingPrice = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 6
        xlsIndexCol = 6
        newRow.currencyCode = getRecordIdImport(15, 'CODE', row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset)

        // графа 7
        xlsIndexCol = 7
        newRow.rateBRBillDate = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 8
        xlsIndexCol = 8
        newRow.rateBROperationDate = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 9
        xlsIndexCol = 9
        newRow.paymentTermStart = parseDate(row.cell[xlsIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 10
        xlsIndexCol = 10
        newRow.paymentTermEnd = parseDate(row.cell[xlsIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 11
        xlsIndexCol = 11
        newRow.interestRate = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 12
        xlsIndexCol = 12
        newRow.operationDate = parseDate(row.cell[xlsIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 13
        xlsIndexCol = 13
        newRow.rateWithDiscCoef = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 14
        xlsIndexCol = 14
        newRow.sumStartInCurrency = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 15
        xlsIndexCol = 15
        newRow.sumStartInRub = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 16
        xlsIndexCol = 16
        newRow.sumEndInCurrency = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 17
        xlsIndexCol = 17
        newRow.sumEndInRub = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 18
        xlsIndexCol = 18
        newRow.sum = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}