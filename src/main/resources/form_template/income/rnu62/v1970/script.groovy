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
 * TODO:
 *      - неясность с расчетом графы 8, 16, 17
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
def allColumns = ["rowNumber", "billNumber", "creationDate", "nominal", "sellingPrice",
        "currencyCode", "rateBRBillDate", "rateBROperationDate",
        "paymentTermStart", "paymentTermEnd", "interestRate",
        "operationDate", "rateWithDiscCoef", "sumStartInCurrency",
        "sumStartInRub", "sumEndInCurrency", "sumEndInRub", "sum"]

// Поля, для которых подсчитываются итоговые значения (графа 18)
@Field
def totalColumns = ["sum"]

// TODO (Ramil Timerbaev) уточнить у аналитика: графа 7 и 8 в перечне полей могут редактироваться, но в тоже время расчитываемые
 // Редактируемые атрибуты (графа 2..13)
@Field
def editableColumns = ["billNumber", "creationDate", "nominal", "sellingPrice", "currencyCode", "rateBRBillDate",
        "rateBROperationDate", "paymentTermStart", "paymentTermEnd", "interestRate", "operationDate", "rateWithDiscCoef"]

// Автозаполняемые атрибуты
@Field
def arithmeticCheckAlias = allColumns - editableColumns

@Field
def arithmeticCheckAliasWithoutNSI = ["sumStartInCurrency", "sumStartInRub", "sumEndInCurrency", "sumEndInRub", "sum"]

// Сортируемые атрибуты (графа 2, 12)
@Field
def sortColumns = ["billNumber", "operationDate"]

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
            values.rateBRBillDate = calc7(row)
            values.rateBROperationDate = calc8(row)
            values.sumStartInCurrency = calc14(row, rowPrev)
            values.sumStartInRub = calc15(rowPrev)
            values.sumEndInCurrency = calc16(row, countDaysInYear)
            values.sumEndInRub = calc17(row)
            values.sum = calc18(row)
            checkCalc(row, arithmeticCheckAliasWithoutNSI, values, logger, true)
        }

        if (row.rateBRBillDate != calc7(row)) {
            logger.warn(errorMsg + "В справочнике \"Курсы валют\" не найдено значение ${row.rateBRBillDate} в поле \"${getColumnName(row, 'rateBRBillDate')}\"!")
        }

        if (row.rateBROperationDate != calc8(row)) {
            logger.warn(errorMsg + "В справочнике \"Курсы валют\" не найдено значение ${row.rateBROperationDate} в поле \"${getColumnName(row, 'rateBROperationDate')}\"!")
        }

        // 7. Проверка итоговых значений по всей форме
        sum += (row.sum ?: 0)
    }

    // 7. Проверка итоговых значений по всей форме
    if (totalRow == null || totalRow.sum != sum) {
        loggerError("Итоговые значения рассчитаны неверно!")
    }
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    if (formDataEvent != FormDataEvent.IMPORT && formDataEvent != FormDataEvent.MIGRATION) {
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
            row.with {
                rowNumber = ++index
                rateBRBillDate = calc7(row)
                rateBROperationDate = calc8(row)
                sumStartInCurrency = calc14(row, rowPrev)
                sumStartInRub = calc15(rowPrev)
                sumEndInCurrency = calc16(row, countDaysInYear)
                sumEndInRub = calc17(row)
                sum = calc18(row)
            }
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
    totalRow.billNumber = 'Итого'
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
    // TODO (Ramil Timerbaev) уточнить у аналитика: про то что надо брать курс валют для графы 5
    // курс валюты из графы 5 на дату из графы 12
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
        } else {
            tmp = null
        }
    }
    if (row.operationDate != null && row.paymentTermStart != null && row.operationDate > row.paymentTermStart) {
        if (row.nominal != null && row.sellingPrice != null) {
            tmp = row.nominal - row.sellingPrice
        } else {
            tmp = null
        }
    }
    if (row.rateWithDiscCoef != null) {
        def tmpForMultiplication = row.rateWithDiscCoef
        if (row.interestRate != null && row.interestRate < row.rateWithDiscCoef) {
            tmpForMultiplication = row.interestRate
        }
        tmp = row.sellingPrice * (row.operationDate - row.creationDate) / countDaysInYear * tmpForMultiplication / 100
    }

    // TODO (Ramil Timerbaev) уточнить у аналитика про эту часть расчета
    // количество дней в году указаном в 3 графе
    def countDays3 = (row.creationDate != null ? getCountDaysInYear(row.creationDate) : 0)
    // количество дней в году указаном в 10 графе
    def countDays10 = (row.paymentTermEnd != null  ? getCountDaysInYear(row.paymentTermEnd) : 0)
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
    }

    if (row.paymentTermEnd != null && row.operationDate != null && getDiffBetweenYears(row.paymentTermEnd, row.operationDate) >= 3) {
        tmp = BigDecimal.ZERO
    }
    // TODO (Ramil Timerbaev) что за погашеный вылютный вексель
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
    // TODO (Ramil Timerbaev) уточнить у аналитика про это уловие: если заполнена только графа 16 среди всех строк или среди граф 13, 14, 16
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


// Получение xml с общими проверками
def getXML(def String startStr, def String endStr) {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        throw new ServiceException('Имя файла не должно быть пустым')
    }
    def is = ImportInputStream
    if (is == null) {
        throw new ServiceException('Поток данных пуст')
    }
    if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xlsm')) {
        throw new ServiceException('Выбранный файл не соответствует формату xlsx/xlsm!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    return xml
}

// Получение импортируемых данных
void importData() {
    def xml = getXML('№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 18, 2)

    def headerMapping = [
            (xml.row[0].cell[0]) : '№ пп',
            (xml.row[0].cell[1]) : 'Номер векселя',
            (xml.row[0].cell[2]) : 'Дата составления',
            (xml.row[0].cell[3]) : 'Номинал',
            (xml.row[0].cell[4]) : 'Цена реализации',
            (xml.row[0].cell[5]) : 'Код валюты',
            (xml.row[0].cell[6]) : 'Курс Банка России',
            (xml.row[0].cell[8]) : 'Дата наступления срока платежа',
            (xml.row[0].cell[9]) : 'Дата окончания срока платежа',
            (xml.row[0].cell[10]): 'Процентная ставка',
            (xml.row[0].cell[11]): 'Дата совершения операции',
            (xml.row[0].cell[12]): 'Ставка с учётом дисконтирующего коэффициента',
            (xml.row[0].cell[13]): 'Сумма дисконта начисленного на начало отчётного периода',
            (xml.row[0].cell[15]): 'Сумма дисконта начисленного на конец отчётного периода',
            (xml.row[0].cell[17]): 'Сумма дисконта начисленного за отчётный период (руб.)',
            (xml.row[1].cell[6]) : 'на дату составления векселя',
            (xml.row[1].cell[7]) : 'на дату совершения операции',
            (xml.row[1].cell[13]): 'в валюте',
            (xml.row[1].cell[14]): 'в рублях',
            (xml.row[1].cell[15]): 'в валюте',
            (xml.row[1].cell[16]): 'в рублях'
    ]

    (1..18).each { index ->
        headerMapping.put((xml.row[2].cell[index - 1]), index.toString())
    }


checkHeaderEquals(headerMapping)

    addData(xml, 3)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = 10 // Смещение для индекса колонок в ошибках импорта
    def int colOffset = 1 // Смещение для индекса колонок в ошибках импорта

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
        if (row.cell[0].text() == null || row.cell[0].text() == '') {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        arithmeticCheckAlias.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 1
        def xlsIndexCol = 0
        newRow.rowNumber = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 2
        newRow.billNumber = row.cell[1].text()

        // графа 3
        xlsIndexCol = 2
        newRow.creationDate = parseDate(row.cell[xlsIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 4
        xlsIndexCol = 3
        newRow.nominal = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 5
        xlsIndexCol = 4
        newRow.sellingPrice = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 6
        xlsIndexCol = 5
        newRow.currencyCode = getRecordIdImport(15, 'CODE', row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset)

        // графа 7
        xlsIndexCol = 6
        newRow.rateBRBillDate = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 8
        xlsIndexCol = 7
        newRow.rateBROperationDate = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 9
        xlsIndexCol = 8
        newRow.paymentTermStart = parseDate(row.cell[xlsIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 10
        xlsIndexCol = 9
        newRow.paymentTermEnd = parseDate(row.cell[xlsIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 11
        xlsIndexCol = 10
        newRow.interestRate = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 12
        xlsIndexCol = 11
        newRow.operationDate = parseDate(row.cell[xlsIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        // графа 12
        xlsIndexCol = 12
        newRow.rateWithDiscCoef = parseNumber(row.cell[xlsIndexCol].text(), xlsIndexRow, xlsIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}