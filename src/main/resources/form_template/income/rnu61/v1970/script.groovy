package form_template.income.rnu61.v1970

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.time.TimeCategory
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-61) Регистр налогового учёта расходов по процентным векселям ОАО «Сбербанк России»,
 * учёт которых требует применения метода начисления"
 * formTemplateId=352
 *
 * графа 1  - rowNumber
 * графа 2  - billNumber
 * графа 3  - creationDate
 * графа 4  - nominal
 * графа 5  - currencyCode
 * графа 6  - rateBRBill
 * графа 7  - rateBROperation
 * графа 8  - paymentStart
 * графа 9  - paymentEnd
 * графа 10 - interestRate
 * графа 11 - operationDate
 * графа 12 - sum70606
 * графа 13 - sumLimit
 * графа 14 - percAdjustment
 *
 * @author akadyrgulov
 * @author Stanislav Yasinskiy
 */

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
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        def cols = isBalancePeriod ? (allColumns - 'rowNumber') : editableColumns
        def autoColumns = isBalancePeriod ? ['rowNumber'] : autoFillColumns
        formDataService.addRow(formData, currentDataRow, cols, autoColumns)
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
    case FormDataEvent.COMPOSE:
        formDataService.consolidationTotal(formData, formDataDepartment.id, logger, ['total'])
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
def editableColumns = ['billNumber', 'creationDate', 'nominal', 'currencyCode', 'paymentStart', 'paymentEnd',
        'interestRate', 'operationDate', 'sum70606']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'rateBRBill', 'rateBROperation', 'sumLimit', 'percAdjustment']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'billNumber', 'creationDate', 'nominal', 'currencyCode', 'rateBRBill',
        'rateBROperation', 'paymentStart', 'paymentEnd', 'interestRate', 'operationDate',
        'percAdjustment']

@Field
def allColumns = ['rowNumber', 'fix', 'billNumber', 'creationDate', 'nominal', 'currencyCode', 'rateBRBill',
        'rateBROperation', 'paymentStart', 'paymentEnd', 'interestRate', 'operationDate', 'sum70606', 'sumLimit',
        'percAdjustment']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['percAdjustment']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
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

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    // Проверка только для первичных
    if (formData.kind != FormDataKind.PRIMARY) {
        return
    }
    if (!isBalancePeriod && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        def formName = formData.getFormType().getName()
        throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
    }
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // Отчетная дата
    def reportDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    // Начальная дата отчетного периода
    def reportDateStart = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def daysOfYear = getCountDays(reportDateStart)

    // Удаление итогов
    deleteAllAliased(dataRows)

    if (!dataRows.isEmpty()) {
        // номер последний строки предыдущей формы
        def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

        for (def row in dataRows) {
            // графа 1
            row.rowNumber = ++index
            // графа 6
            row.rateBRBill = calc6and7(row.currencyCode, row.creationDate)
            // графа 7
            row.rateBROperation = calc6and7(row.currencyCode, row.operationDate)
            // графа 13
            row.sumLimit = calc13(row, daysOfYear)
            // графа 14
            row.percAdjustment = calc14(row)
        }
    }
    // Добавление итогов
    dataRows.add(getTotalRow(dataRows))

    dataRowHelper.save(dataRows)
}

// Расчет итоговой строки
def getTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Ресчет графы 6 и 7
def BigDecimal calc6and7(def currencyCode, def date) {
    if (currencyCode != null && date != null) {
        def rate = 1
        if (!isRubleCurrency(currencyCode)) {
            rate = getRate(date, currencyCode)
        }
        return rate
    } else {
        return null
    }
}

// Ресчет графы 13
def BigDecimal calc13(def DataRow<Cell> row, def daysOfYear) {
    def daysOfYear3 = null as Integer
    def daysOfYear9 = null as Integer
    def diffYear = false // Признак разницы в годах между графами 3 и 9
    def t1 = 0 as Integer
    def t2 = 0 as Integer
    if (row.creationDate != null && row.paymentEnd != null) {
        daysOfYear3 = getCountDays(row.creationDate)
        daysOfYear9 = getCountDays(row.paymentEnd)
        def c3 = Calendar.getInstance()
        c3.setTime(row.creationDate)
        def c9 = Calendar.getInstance()
        c9.setTime(row.paymentEnd)
        diffYear = c3.get(Calendar.YEAR) != c9.get(Calendar.YEAR)
        if (diffYear) {
            if (row.paymentEnd > row.creationDate) {
                c3.set(Calendar.DAY_OF_MONTH, 31)
                c3.set(Calendar.MONTH, 12)
                t1 = TimeCategory.minus(c3.getTime(), row.creationDate).getDays()
                c9.set(Calendar.DAY_OF_MONTH, 1)
                c9.set(Calendar.MONTH, 1)
                t2 = TimeCategory.minus(row.paymentEnd, c3.getTime()).getDays()
            } else {
                c9.set(Calendar.DAY_OF_MONTH, 31)
                c9.set(Calendar.MONTH, 12)
                t1 = TimeCategory.minus(row.creationDate, c9.getTime()).getDays()
                c3.set(Calendar.DAY_OF_MONTH, 1)
                c3.set(Calendar.MONTH, 1)
                t2 = TimeCategory.minus(c9.getTime(), row.paymentEnd).getDays()
            }
        }
    }
    // Если «Графа 3» и «Графа 9» принадлежат разным годам
    // и продолжительность каждого года разная (в одном 365 дней, в другом 366)
    if (daysOfYear3 != null && daysOfYear9 != null && diffYear && daysOfYear3 != daysOfYear9) {
        if (row.nominal == null || row.interestRate == null) {
            return null
        }
        if (daysOfYear3 == 365) {
            // Если первый год содержит 365 дней
            // «Графа 13» = («Графа 4» * «Графа 10» * ( T1 - 1)) / 365*100 +(«Графа 4» * «Графа 10» * T2) / 366*100
            return ((row.nominal * row.interestRate * (t1 - 1)) / 36500 + (row.nominal * row.interestRate * t2 / 36600)).setScale(2, RoundingMode.HALF_UP)
        } else {
            // Если первый год содержит 366 дней
            // «Графа 13» = («Графа 4» * «Графы 10» * ( T2 - 1)) / 365*100 +(«Графа 4» * «Графы 10» * T1) / 365*100
            return ((row.nominal * row.interestRate * (t2 - 1)) / 36500 + (row.nominal * row.interestRate * t1 / 36500)).setScale(2, RoundingMode.HALF_UP)
        }
    } else {
        if (row.sum70606 == null && isRubleCurrency(row.currencyCode)) {
            // Если «Графа 12» не заполнена и «Графа 5»= 810
            if (row.paymentEnd == null || row.operationDate == null) {
                return null
            }
            if (row.paymentEnd > row.operationDate) {
                // Если «Графа 11» < «Графа 9»
                if (row.nominal == null || row.interestRate == null
                        || row.operationDate == null || row.creationDate == null
                        || row.rateBROperation == null) {
                    return null
                }
                // «Графа 13» = («Графа 4» * «Графа 10» / 100 * («Графа 11» - «Графа 3») / 365 (366)),
                // с округлением до двух знаков после запятой по правилам округления * «Графа 7»
                return (row.nominal * row.interestRate / 100 * TimeCategory.minus(row.operationDate, row.creationDate).getDays() / daysOfYear * row.rateBROperation)
                        .setScale(2, RoundingMode.HALF_UP)

            } else if (row.paymentEnd < row.operationDate) {
                // Если «Графа 11» > «графы 9»
                if (row.nominal == null || row.interestRate == null
                        || row.paymentEnd == null || row.creationDate == null
                        || row.rateBROperation == null) {
                    return null
                }
                // «Графа 13» = («Графа 4» * «Графа 10» / 100 * («Графа 9» - «Графа 3») / 365 (366)),
                // с округлением до двух знаков после запятой по правилам округления * «Графа 7»
                return (row.nominal * row.interestRate / 100 * TimeCategory.minus(row.paymentEnd, row.creationDate).getDays() / daysOfYear * row.rateBROperation)
                        .setScale(2, RoundingMode.HALF_UP)
            }
        }
    }
    return null
}

// Ресчет графы 14
def BigDecimal calc14(def row) {
    BigDecimal temp = null
    if (row.sum70606 != null) {
        if (row.sumLimit != null && row.sum70606 > row.sumLimit) {
            temp = row.sum70606 - row.sumLimit
        }
    } else if (row.nominal != null && row.rateBRBill != null && row.rateBROperation != null) {
        temp = row.nominal * (row.rateBRBill - row.rateBROperation)
    }
    return temp?.setScale(2, RoundingMode.HALF_UP)
}
// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    // алиасы графов для арифметической проверки (графа 8, 9, 12-15)
    def arithmeticCheckAlias = ['rateBRBill', 'rateBROperation', 'sumLimit', 'percAdjustment']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    // Инвентарные номера
    def List<String> invList = new ArrayList<String>()
    // Отчетная дата
    def reportDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    //Начальная дата отчетного периода
    def reportDateStart = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def daysOfYear = getCountDays(reportDateStart)

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod)

        // Проверка на уникальность поля «№ пп»
        if (++i != row.rowNumber) {
            loggerError(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // Проверка на уникальность поля «инвентарный номер»
        if (invList.contains(row.billNumber)) {
            loggerError(errorMsg + "Инвентарный номер не уникальный!")
        } else {
            invList.add(row.billNumber)
        }

        // 2. Проверка даты совершения операции и границ отчетного периода
        if (row.operationDate < reportDateStart || row.operationDate > reportDate) {
            loggerError(errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }

        // 4. Проверка на нулевые значения
        if (row.sum70606 == 0 && row.sumLimit == 0 && row.percAdjustment == 0) {
            loggerError(errorMsg + "Все суммы по операции нулевые!")
        }

        // 5. Арифметические проверки
        needValue['rateBRBill'] = calc6and7(row.currencyCode, row.creationDate)
        needValue['rateBROperation'] = calc6and7(row.currencyCode, row.operationDate)
        needValue['sumLimit'] = calc13(row, daysOfYear)
        needValue['percAdjustment'] = calc14(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, !isBalancePeriod)
    }

    // 5. Арифметические проверки расчета итоговой строки
    checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod)
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode) {
    return refBookService.getStringValue(15, currencyCode, 'CODE') == '810'
}

// Получить курс банка России на указанную дату.
def getRate(def Date date, def value) {
    def res = refBookFactory.getDataProvider(22).getRecords(date != null ? date : new Date(), null, "CODE_NUMBER = $value", null);
    return res.getRecords().get(0).RATE.numberValue
}

def getCountDays(def Date date) {
    def Calendar calendar = Calendar.getInstance()
    calendar.setTime(date)
    return (new GregorianCalendar()).isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 14, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Номер векселя',
            (xml.row[0].cell[3]): 'Дата составления',
            (xml.row[0].cell[4]): 'Номинал',
            (xml.row[0].cell[5]): 'Код валюты',
            (xml.row[0].cell[6]): 'Курс Банка России',
            (xml.row[0].cell[8]): 'Дата наступления срока платежа',
            (xml.row[0].cell[9]): 'Дата окончания срока платежа',
            (xml.row[0].cell[10]): 'Процентная ставка',
            (xml.row[0].cell[11]): 'Дата совершения операции',
            (xml.row[0].cell[12]): 'Сумма процентов, отнесённая на счёт 70606, учитываемая в РНУ-5',
            (xml.row[0].cell[13]): 'Предельная сумма процентов, учитываемых в налоговом учёте',
            (xml.row[0].cell[14]): 'Корректировка процентов, учтённых в РНУ-5',
            (xml.row[1].cell[6]): 'на дату составления векселя',
            (xml.row[1].cell[7]): 'на дату совершения операции',
            (xml.row[2].cell[0]): '1'
    ]
    (2..14).each { index ->
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
        def cols = isBalancePeriod ? (allColumns - 'rowNumber') : editableColumns
        def autoColumns = isBalancePeriod ? ['rowNumber'] : autoFillColumns
        cols.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 2
        newRow.billNumber = row.cell[2].text()

        // графа 3
        newRow.creationDate = parseDate(row.cell[3].text(), "dd.MM.yyyy", xlsIndexRow, 2 + colOffset, logger, false)

        // графа 4
        newRow.nominal = parseNumber(row.cell[4].text(), xlsIndexRow, 3 + colOffset, logger, false)

        // графа 5
        newRow.currencyCode = getRecordIdImport(15, 'CODE', row.cell[5].text(), xlsIndexRow, 4 + colOffset)

        // графа 8
        newRow.paymentStart = parseDate(row.cell[8].text(), "dd.MM.yyyy", xlsIndexRow, 7 + colOffset, logger, false)

        // графа 9
        newRow.paymentEnd = parseDate(row.cell[9].text(), "dd.MM.yyyy", xlsIndexRow, 8 + colOffset, logger, false)

        // графа 10
        newRow.interestRate = parseNumber(row.cell[10].text(), xlsIndexRow, 9 + colOffset, logger, false)

        // графа 11
        newRow.operationDate = parseDate(row.cell[11].text(), "dd.MM.yyyy", xlsIndexRow, 10 + colOffset, logger, false)

        // графа 12
        newRow.sum70606 = parseNumber(row.cell[12].text(), xlsIndexRow, 11 + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def msg, Object...args) {
    if (isBalancePeriod) {
        logger.warn(msg, args)
    } else {
        logger.error(msg, args)
    }
}
