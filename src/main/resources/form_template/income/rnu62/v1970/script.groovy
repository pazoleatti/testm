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
 * TODO походу расчеты еще изменят
 * @author bkinzyabulatov
 *
 * Графа 1  rowNumber           № пп
 * Графа 2  billNumber          Номер векселя
 * Графа 3  creationDate        Дата составления
 * Графа 4  nominal             Номинал
 * Графа 5  sellingPrice        Цена реализации
 * Графа 6  currencyCode        Код валюты - Справочник 15 - 64 атрибут CODE
 * Графа 7  rateBRBillDate      Курс Банка России на дату составления векселя
 * Графа 8  rateBROperationDate Курс Банка России на дату совершения операции
 * Графа 9  paymentTermStart    Дата наступления срока платежа
 * Графа 10 paymentTermEnd      Дата окончания срока платежа
 * Графа 11 interestRate        Процентная ставка
 * Графа 12 operationDate       Дата совершения операции
 * Графа 13 rateWithDiscCoef    Ставка с учётом дисконтирующего коэффициента
 * Графа 14 sumStartInCurrency  Сумма дисконта начисленного на начало отчётного периода в валюте
 * Графа 15 sumStartInRub       Сумма дисконта начисленного на начало отчётного периода в рублях
 * Графа 16 sumEndInCurrency    Сумма дисконта начисленного на конец отчётного периода в валюте
 * Графа 17 sumEndInRub         Сумма дисконта начисленного на конец отчётного периода в рублях
 * Графа 18 sum                 Сумма дисконта начисленного за отчётный период (руб.)
 */

/** Признак периода ввода остатков. */
@Field
def isBalancePeriod
isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

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
        def columns = (isBalancePeriod ? allColumns - 'rowNumber' : editableColumns)
        formDataService.addRow(formData, currentDataRow, columns, arithmeticCheckAlias)
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
// обобщить
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

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ["sum"]

// Редактируемые атрибуты
@Field
def editableColumns = ["billNumber", "creationDate", "nominal",
        "sellingPrice", "currencyCode", "rateBRBillDate",
        "rateBROperationDate", "paymentTermStart", "paymentTermEnd",
        "interestRate", "operationDate", "rateWithDiscCoef"]

// Автозаполняемые атрибуты
@Field
def arithmeticCheckAlias = ["rowNumber", "rateBRBillDate", "rateBROperationDate",
        "sumStartInCurrency", "sumStartInRub", "sumEndInCurrency", "sumEndInRub", "sum"]

@Field
def arithmeticCheckAliasWithoutNSI = ["sumStartInCurrency", "sumStartInRub",
        "sumEndInCurrency", "sumEndInRub", "sum"]

@Field
def sortColumns = ["billNumber", "operationDate"]

// Автозаполняемые атрибуты
// TODO уточнить
@Field
def nonEmptyColumns = ["rowNumber", "billNumber", "creationDate", "nominal",
        "sellingPrice", "currencyCode", "rateBRBillDate",
        "rateBROperationDate", "paymentTermStart", "paymentTermEnd",
        "interestRate", "operationDate", "sumEndInCurrency", "sumEndInRub", "sum"]

//// Некастомные методы

/** Получить курс валюты */
BigDecimal getCourse(def row, def date) {
    def currency = row.currencyCode
    def isRuble = isRubleCurrency(currency)
    if (date != null && currency != null && !isRuble) {
        def res = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache,
                'CODE_NUMBER', currency.toString(), date, row.getIndex(), getColumnName(row, "currencyCode"), logger, false)
        return res?.RATE?.numberValue
    } else if (isRuble) {
        return 1;
    } else {
        return null
    }
}

/** Проверка валюты на рубли */
def isRubleCurrency(def currencyCode) {
    return formDataService.getRefBookValue(15, currencyCode, refBookCache)?.CODE?.stringValue == '810'
}

/** Количество дней в году за который делаем */
int getCountDaysInYear(def Date date) {
    def calendar = Calendar.getInstance()
    calendar.setTime(date)
    return (new GregorianCalendar()).isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365
}

def boolean isZeroEmpty(def value) {
    return value == null || value == 0
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = null
    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowsPrev
    def countDaysInYear
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

        // Проверка заполнения граф
        if (!isRubleCurrency(row.currencyCode)) {
            nonEmptyColumns = nonEmptyColumns - 'sumEndInCurrency'
        }
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        // Проверка даты совершения операции и границ отчетного периода
        if (row.operationDate != null && (row.operationDate.compareTo(dFrom) < 0 || row.operationDate.compareTo(dTo) > 0)) {
            loggerError(errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }

        // Проверка на уникальность поля «№ пп»
        if (++i != row.rowNumber) {
            loggerError(errorMsg + 'Нарушена уникальность номера по порядку!')
        }
        // Проверка на нулевые значения
        if (isZeroEmpty(row.sumStartInCurrency) &&
                isZeroEmpty(row.sumStartInRub) &&
                isZeroEmpty(row.sumEndInCurrency) &&
                isZeroEmpty(row.sumEndInRub) &&
                isZeroEmpty(row.sum)) {
            loggerError(errorMsg + "Все суммы по операции нулевые!")
        }
        // Проверка существования необходимых экземпляров форм
        if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
            def rowPrev = getRowPrev(dataRowsPrev, row)
            if (rowPrev == null) {
                logger.error(errorMsg + "Отсутствуют данные в РНУ-62 за предыдущий отчетный период!")
            }
            def values = [:]

            //TODO проверить, в аналитике неадекватно описано
            values.with {
                rateBRBillDate = calc7(row)
                rateBROperationDate = calc8(row)
                sumStartInCurrency = calc14(row, rowPrev)
                sumStartInRub = calc15(rowPrev)
                sumEndInCurrency = calc16(row, countDaysInYear)
                sumEndInRub = calc17(row)
                sum = calc18(row)
            }
            // Проверяем расчеты для параметров(14-18), не использующих справочники, остальные (7,8) проверяются ниже
            checkCalc(row, arithmeticCheckAliasWithoutNSI, values, logger, true)
        }
        // Проверки соответствия НСИ
        formDataService.checkNSI(15, refBookCache, row, "currencyCode", logger, false)
        if (row.rateBRBillDate != calc7(row)) {
            logger.warn(errorMsg + "В справочнике \"Курсы валют\" не найдено значение ${row.rateBRBillDate} в поле \"${getColumnName(row, 'rateBRBillDate')}\"!")
        }
        if (row.rateBROperationDate != calc8(row)) {
            logger.warn(errorMsg + "В справочнике \"Курсы валют\" не найдено значение ${row.rateBROperationDate} в поле \"${getColumnName(row, 'rateBROperationDate')}\"!")
        }
    }
    // Не стал усложнять проверку итогов для одной графы
    if (totalRow == null || (totalRow.sum != dataRows.sum { it -> (it.getAlias() == null) ? it.sum ?: 0 : 0 })) {
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
        def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
        def countDaysInYear = getCountDaysInYear(dFrom)

        // Расчет ячеек
        dataRows.each { row ->
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
        dataRows.each { row ->
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
    return round(getCourse(row, row.creationDate))
}

def BigDecimal calc8(def row) {
    return round(getCourse(row, row.operationDate))
}

def BigDecimal calc14(def row, def rowPrev) {
    if (row.rateWithDiscCoef != null) {
        return null
    } else {
        if (rowPrev != null) {
            return round(rowPrev.sumEndInCurrency)
        } else {
            return BigDecimal.ZERO
        }
    }
}

def BigDecimal calc15(def rowPrev) {
    if (rowPrev != null) {
        return round(rowPrev.sumEndInRub)
    } else {
        return BigDecimal.ZERO
    }
}

def BigDecimal calc16(def row, def countDaysInYear) {
    def tmp
    if (row.operationDate != null && row.paymentTermStart != null && row.operationDate < row.paymentTermStart) {
        if (row.nominal != null && row.sellingPrice != null && row.creationDate != null) {
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
        if (row.interestRate < row.rateWithDiscCoef) {
            tmp = row.sellingPrice * (row.operationDate - row.creationDate) / countDaysInYear * row.interestRate / 100
        } else {
            tmp = row.sellingPrice * (row.operationDate - row.creationDate) / countDaysInYear * row.rateWithDiscCoef / 100
        }
    }

    if (row.creationDate != null && row.paymentTermStart != null && (getCountDaysInYear(row.creationDate) - getCountDaysInYear(row.paymentTermStart) != 0)) {
        //TODO заполняется вручную, но возможна формула
        tmp = row.sumEndInCurrency
    }

    if (row.paymentTermEnd != null && row.operationDate != null && getDiffBetweenYears(row.paymentTermEnd, row.operationDate) >= 3) {
        tmp = BigDecimal.ZERO
    }
    if (!isRubleCurrency(row.currencyCode)) {
        tmp = null
    }
    return round(tmp)
}

def BigDecimal calc17(def row) {
    def tmp
    if (row.rateWithDiscCoef != null &&
            row.sumStartInCurrency != null &&
            row.sumEndInCurrency != null) {
        if (row.operationDate != null && row.paymentTermStart != null) {
            if (row.operationDate >= row.paymentTermStart) {
                if (row.nominal != null && row.rateBROperationDate != null && row.sellingPrice != null && row.rateBRBillDate != null) {
                    tmp = (row.nominal * row.rateBROperationDate) - (row.sellingPrice * row.rateBRBillDate)
                } else {
                    tmp = null
                }
            } else {
                //TODO "второй строкой"? Округление?
                if (row.sumStartInRub != null && row.rateBROperationDate != null && row.sellingPrice != null && row.rateBRBillDate != null) {
                    tmp = (row.sellingPrice * (row.rateBROperationDate - row.rateBRBillDate)) + row.sumStartInRub
                } else {
                    tmp = null
                }
            }
        } else {
            return null
        }
    } else {
        tmp = (row.sumEndInCurrency != null && row.rateBROperationDate != null) ? (row.sumEndInCurrency * row.rateBROperationDate) : null//TODO check
    }
    return round(tmp)
}

def BigDecimal calc18(def row) {
    return (row.sumEndInRub != null && row.sellingPrice != null) ? round(row.sumEndInRub - row.sellingPrice) : null //TODO check
}

def DataRow getRowPrev(def dataRowsPrev, def DataRow row) {
    for (def rowPrev : dataRowsPrev) {
        if (rowPrev?.getAlias() == null && row.billNumber != null && row.billNumber == rowPrev.billNumber) {
            return rowPrev
        }
    }
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
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[1]): 'Номер векселя',
            (xml.row[0].cell[2]): 'Дата составления',
            (xml.row[0].cell[3]): 'Номинал',
            (xml.row[0].cell[4]): 'Цена реализации',
            (xml.row[0].cell[5]): 'Код валюты',
            (xml.row[0].cell[6]): 'Курс Банка России',
            (xml.row[0].cell[8]): 'Дата наступления срока платежа',
            (xml.row[0].cell[9]): 'Дата окончания срока платежа',
            (xml.row[0].cell[10]): 'Процентная ставка',
            (xml.row[0].cell[11]): 'Дата совершения операции',
            (xml.row[0].cell[12]): 'Ставка с учётом дисконтирующего коэффициента',
            (xml.row[0].cell[13]): 'Сумма дисконта начисленного на начало отчётного периода',
            (xml.row[0].cell[15]): 'Сумма дисконта начисленного на конец отчётного периода',
            (xml.row[0].cell[17]): 'Сумма дисконта начисленного за отчётный период (руб.)',
            (xml.row[1].cell[6]): 'на дату составления векселя',
            (xml.row[1].cell[7]): 'на дату совершения операции',
            (xml.row[1].cell[13]): 'в валюте',
            (xml.row[1].cell[14]): 'в рублях',
            (xml.row[1].cell[15]): 'в валюте',
            (xml.row[1].cell[16]): 'в рублях',
            (xml.row[2].cell[0]): '1',
            (xml.row[2].cell[1]): '2',
            (xml.row[2].cell[2]): '3',
            (xml.row[2].cell[3]): '4',
            (xml.row[2].cell[4]): '5',
            (xml.row[2].cell[5]): '6',
            (xml.row[2].cell[6]): '7',
            (xml.row[2].cell[7]): '8',
            (xml.row[2].cell[8]): '9',
            (xml.row[2].cell[9]): '10',
            (xml.row[2].cell[10]): '11',
            (xml.row[2].cell[11]): '12',
            (xml.row[2].cell[12]): '13',
            (xml.row[2].cell[13]): '14',
            (xml.row[2].cell[14]): '15',
            (xml.row[2].cell[15]): '16',
            (xml.row[2].cell[16]): '17',
            (xml.row[2].cell[17]): '18'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
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
        if (xmlIndexRow <= headRowCount) {
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
        newRow.rowNumber = parseNumber(row.cell[0].text(), xlsIndexRow, 0 + colOffset, logger, false)

        // графа 2
        newRow.billNumber = row.cell[1].text()

        // графа 3
        newRow.creationDate = parseDate(row.cell[2].text(), "dd.MM.yyyy", xlsIndexRow, 2 + colOffset, logger, false)

        // графа 4
        newRow.nominal = parseNumber(row.cell[3].text(), xlsIndexRow, 3 + colOffset, logger, false)

        // графа 5
        newRow.sellingPrice = parseNumber(row.cell[4].text(), xlsIndexRow, 4 + colOffset, logger, false)

        // графа 6
        newRow.currencyCode = getRecordIdImport(15, 'CODE', row.cell[5].text(), xlsIndexRow, 5 + colOffset)

        // графа 7

        // графа 8

        // графа 9
        newRow.paymentTermStart = parseDate(row.cell[8].text(), "dd.MM.yyyy", xlsIndexRow, 8 + colOffset, logger, false)

        // графа 10
        newRow.paymentTermEnd = parseDate(row.cell[9].text(), "dd.MM.yyyy", xlsIndexRow, 9 + colOffset, logger, false)

        // графа 11
        newRow.interestRate = parseNumber(row.cell[10].text(), xlsIndexRow, 10 + colOffset, logger, false)

        // графа 12
        newRow.operationDate = parseDate(row.cell[11].text(), "dd.MM.yyyy", xlsIndexRow, 11 + colOffset, logger, false)

        // графа 12
        newRow.rateWithDiscCoef = parseNumber(row.cell[12].text(), xlsIndexRow, 12 + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}