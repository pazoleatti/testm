package form_template.income.rnu56.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.TaxType
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-56) Регистр налогового учёта процентного дохода по дисконтным векселям сторонних эмитентов"
 * formTypeId=349
 *
 * @author rtimerbaev
 * @author Stanislav Yasinskiy
 */

// графа 1  - number
// графа 2  - bill
// графа 3  - buyDate
// графа 4  - currency
// графа 5  - nominal
// графа 6  - price
// графа 7  - maturity
// графа 8  - termDealBill
// графа 9  - percIncome
// графа 10 - implementationDate
// графа 11 - sum
// графа 12 - discountInCurrency
// графа 13 - discountInRub
// графа 14 - sumIncomeinCurrency
// графа 15 - sumIncomeinRuble

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
        def cols = isBalancePeriod() ? (allColumns - 'number') : editableColumns
        def autoColumns = isBalancePeriod() ? ['number'] : autoFillColumns
        formDataService.addRow(formData, currentDataRow, cols, autoColumns)
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
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationTotal(formData, formDataDepartment.id, logger, ['total'])
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

// Редактируемые атрибуты
@Field
def editableColumns = ['bill', 'buyDate', 'currency', 'nominal', 'price', 'maturity', 'implementationDate', 'sum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['bill', 'buyDate', 'currency', 'nominal', 'price', 'maturity', 'termDealBill',
        'implementationDate', 'sum', 'sumIncomeinCurrency', 'sumIncomeinRuble']

// Атрибуты для итогов
@Field
def totalColumns = ['discountInRub', 'sumIncomeinRuble']

// Все атрибуты
@Field
def allColumns = ['number', 'bill', 'buyDate', 'currency', 'nominal', 'price', 'maturity', 'termDealBill', 'percIncome',
        'implementationDate', 'sum', 'discountInCurrency', 'discountInRub', 'sumIncomeinCurrency', 'sumIncomeinRuble']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number', 'termDealBill', 'percIncome', 'discountInCurrency', 'discountInRub',
                       'sumIncomeinCurrency', 'sumIncomeinRuble']

@Field
def startDate = null

@Field
def endDate = null

@Field
def isBalancePeriod

//// Обертки методов

// Поиск записи в справочнике по значению (для расчетов)
def getRecord(def Long refBookId, def String alias, def String value, def int rowIndex, def String columnName,
              def Date date, boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, date,
            rowIndex, columnName, logger, required)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getEndDate(), rowIndex, colIndex, logger, required)
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

// Получить дату по строковому представлению (формата дд.ММ.гггг)
def getDate(def value, def indexRow, def indexCol) {
    return parseDate(value, 'dd.MM.yyyy', indexRow, indexCol + 1, logger, true)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    // Проверка только для первичных
    if (formData.kind == FormDataKind.PRIMARY && !isBalancePeriod()) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, true, logger, true)
    }
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // получить строку "итого"
    def totalRow = getDataRow(dataRows, 'total')
    // очистить итоги
    totalColumns.each {
        totalRow[it] = null
    }

    // Удаление итогов
    deleteAllAliased(dataRows)

    // Дата начала отчетного периода
    def startDate = getStartDate()
    // Дата окончания отчетного периода
    def endDate = getEndDate()

    for (row in dataRows) {
        if (formData.kind != FormDataKind.PRIMARY) {
            continue
        }
        // графа 8
        row.termDealBill = calcTermDealBill(row)
        // графа 9
        row.percIncome = calcPercIncome(row)
        // графа 12
        row.discountInCurrency = calcDiscountInCurrency(row)
        // графа 13
        row.discountInRub = calcDiscountInRub(row)
        // графа 14
        row.sumIncomeinCurrency = calcSumIncomeinCurrency(row, startDate, endDate)
        // графа 15
        row.sumIncomeinRuble = calcSumIncomeinRuble(row, endDate, startDate)
    }

    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

// Расчет графы 8
def BigDecimal calcTermDealBill(def row) {
    if (row.buyDate == null || row.maturity == null) {
        return null
    }
    return row.maturity - row.buyDate + 1
}

// Расчет графы 9
def BigDecimal calcPercIncome(def row) {
    if (row.nominal == null || row.price == null) {
        return null
    }
    return row.nominal - row.price
}

// Расчет графы 12
def BigDecimal calcDiscountInCurrency(def row) {
    if (row.sum == null || row.price == null) {
        return null
    }
    return row.sum - row.price
}

// Расчет графы 13
def BigDecimal calcDiscountInRub(def row) {
    if (row.discountInCurrency != null) {
        if (row.currency != null && !isRubleCurrency(row.currency)) {
            def map = null
            if (row.implementationDate != null) {
                // значение поля «Курс валюты» справочника «Курсы валют» на дату из «Графы 10»
                map = getRecord(22, 'CODE_NUMBER', "${row.currency}", row.number?.intValue(),
                        getColumnName(row, 'currency'), row.implementationDate)
            }
            if (map != null) {
                return (row.discountInCurrency * map?.RATE?.numberValue)?.setScale(2,
                        RoundingMode.HALF_UP)
            }
        } else {
            return row.discountInCurrency
        }
    }
    return null
}

// Расчет графы 14
def BigDecimal calcSumIncomeinCurrency(def row, def startDate, def endDate) {
    if (startDate == null || endDate == null || row.implementationDate == null) {
        return null
    }
    def tmp
    if ((row.implementationDate < startDate || row.implementationDate > endDate) && row.sum == null) {
        if (row.percIncome == null || row.termDealBill == null) {
            return null
        }
        // Количество дней владения векселем в отчетном периоде
        def countsDays = !row.buyDate.before(startDate) ? endDate - row.buyDate : endDate - startDate
        if (countsDays == 0) {
            return null
        }
        tmp = (row.percIncome / row.termDealBill * countsDays).setScale(2, RoundingMode.HALF_UP)
    } else {
        def sum = getCalcPrevColumn(row, 'sumIncomeinCurrency', startDate)
        if (row.sum != null) {
            if (row.discountInCurrency == null) {
                return null
            }
            tmp = row.discountInCurrency - sum
        } else {
            if (row.percIncome == null) {
                return null
            }
            tmp = row.percIncome - sum
        }
    }
    return tmp
}

// Расчет графы 15
def BigDecimal calcSumIncomeinRuble(def row, def endDate, def startDate) {
    def tmp
    if (row.sum == null) {
        if (!isRubleCurrency(row.currency)) {
            if (row.sumIncomeinCurrency == null) {
                return null
            }
            tmp = (row.sumIncomeinCurrency * getRate(endDate, row.currency)).setScale(2, RoundingMode.HALF_UP)
        } else {
            tmp = row.sumIncomeinCurrency
        }
    } else {
        if (row.discountInRub == null) {
            return null
        }
        tmp = row.discountInRub - getCalcPrevColumn(row, 'sumIncomeinRuble', startDate)
    }

    return tmp
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    if (dataRows.isEmpty()) {
        return
    }

    // алиасы графов для арифметической проверки (графа 8, 9, 12-15)
    def arithmeticCheckAlias = ['termDealBill', 'percIncome', 'discountInCurrency', 'discountInRub',
            'sumIncomeinCurrency', 'sumIncomeinRuble']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // Дата начала отчетного периода
    def startDate = getStartDate()
    // Дата окончания отчетного периода
    def endDate = getEndDate()
    // Векселя
    def List<String> billsList = new ArrayList<String>()

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        // 2. Проверка даты приобретения и границ отчетного периода
        if (endDate != null && row.buyDate != null && row.buyDate.after(endDate)) {
            loggerError(row, errorMsg + 'Дата приобретения вне границ отчетного периода!')
        }

        // 4. Проверка на уникальность векселя
        if (billsList.contains(row.bill)) {
            loggerError(row, errorMsg + 'Повторяющееся значения в графе «Вексель»')
        } else {
            billsList.add(row.bill)
        }

        // 5. Проверка на нулевые значения (графа 12..15)
        if (row.discountInCurrency == 0 &&
                row.discountInRub == 0 &&
                row.sumIncomeinCurrency == 0 &&
                row.sumIncomeinRuble == 0) {
            loggerError(row, errorMsg + 'Все суммы по операции нулевые!')
        }

        // 6. Проверка на наличие данных предыдущих отчетных периодов для заполнения графы 14 и графы 15
        // 7. Проверка корректности значения в «Графе 3»
        if (row.buyDate != null) {
            def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, row.buyDate, startDate - 1)
            for (reportPeriod in reportPeriods) {
                findFormData = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriod.id)
                if (findFormData != null) {
                    isFind = false
                    for (findRow in formDataService.getDataRowHelper(findFormData).getAllCached()) {
                        if (findRow.bill == row.bill) {
                            isFind = true
                            // лп 7
                            if (findRow.buyDate != row.buyDate) {
                                loggerError(row, errorMsg + "Неверное указана Дата приобретения в РНУ-56 за "
                                        + reportPeriod.name)
                            }
                            break
                        }
                    }
                    // лп 6
                    if (!isFind) {
                        rowWarning(logger, row, errorMsg + "Экземпляр за период " + reportPeriod.name +
                                " не существует (отсутствуют первичные данные для расчёта)!")
                    }
                }
            }
        }

        // 8. Проверка корректности расчёта дисконта
        if (row.sum != null && row.price != null && row.sum - row.price <= 0 && (row.discountInCurrency != 0
                || row.discountInRub != 0)) {
            loggerError(row, errorMsg + 'Расчёт дисконта некорректен!')
        }

        // 9. Проверка на неотрицательные значения
        if (row.discountInCurrency != null && row.discountInCurrency < 0) {
            loggerError(row, errorMsg + "Значение графы «${row.getCell('discountInCurrency').column.name}» отрицательное!")
        }
        if (row.discountInRub != null && row.discountInRub < 0) {
            loggerError(row, errorMsg + "Значение графы «${row.getCell('discountInRub').column.name}» отрицательное!")
        }

        if (formData.kind == FormDataKind.PRIMARY) {
            // 10. Арифметические проверки граф 8, 9, 12-15
            needValue['termDealBill'] = calcTermDealBill(row)
            needValue['percIncome'] = calcPercIncome(row)
            needValue['discountInCurrency'] = calcDiscountInCurrency(row)
            needValue['discountInRub'] = calcDiscountInRub(row)
            needValue['sumIncomeinCurrency'] = calcSumIncomeinCurrency(row, startDate, endDate)
            needValue['sumIncomeinRuble'] = calcSumIncomeinRuble(row, endDate, startDate)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, !isBalancePeriod())
        }

    }

    // 11. Арифметические проверки итогов
    checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod())
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode) {
    def record = getRefBookValue(15, currencyCode)
    return record != null && record.CODE?.stringValue in ['810', '643']
}

// Получить курс банка России на указанную дату
def getRate(def Date date, def value) {
    return getRecord(22, 'CODE_NUMBER', "$value", -1, null, date ?: new Date(), true)?.RATE?.numberValue
}

/**
 * Сумма по графе sumColumnName всех предыдущих форм начиная с row.buyDate в строках где bill = row.bill
 * @param row
 * @param sumColumnName алиас графы для суммирования
 */
def BigDecimal getCalcPrevColumn(def row, def sumColumnName, def startDate) {
    def sum = 0
    if (row.buyDate != null && row.bill != null) {
        def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, row.buyDate, startDate - 1)
        for (reportPeriod in reportPeriods) {
            findFormData = formDataService.find(formData.formType.id, formData.kind, formData.departmentId,
                    reportPeriod.id)
            if (findFormData != null) {
                isFind = false
                for (findRow in formDataService.getDataRowHelper(findFormData).getAllCached()) {
                    if (findRow.bill == row.bill && findRow.buyDate == row.buyDate) {
                        sum += findRow.getCell(sumColumnName).getValue() != null ? findRow.getCell(sumColumnName).getValue() : 0
                    }
                }
            }
        }
    }
    return sum
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'number'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 15, 3)

    def headerMapping = [
            (xml.row[0].cell[0]) : getColumnName(tmpRow, 'number'),
            (xml.row[0].cell[2]) : getColumnName(tmpRow, 'bill'),
            (xml.row[0].cell[3]) : getColumnName(tmpRow, 'buyDate'),
            (xml.row[0].cell[4]) : getColumnName(tmpRow, 'currency'),
            (xml.row[0].cell[5]) : getColumnName(tmpRow, 'nominal'),
            (xml.row[0].cell[6]) : getColumnName(tmpRow, 'price'),
            (xml.row[0].cell[7]) : getColumnName(tmpRow, 'maturity'),
            (xml.row[0].cell[8]) : getColumnName(tmpRow, 'termDealBill'),
            (xml.row[0].cell[9]) : getColumnName(tmpRow, 'percIncome'),
            (xml.row[0].cell[10]): getColumnName(tmpRow, 'implementationDate'),
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'sum'),
            (xml.row[0].cell[12]): 'Фактически поступившая сумма дисконта',
            (xml.row[0].cell[14]): 'Сумма начисленного процентного дохода за отчётный период',
            (xml.row[1].cell[12]): 'в валюте',
            (xml.row[1].cell[13]): 'в рублях по курсу Банка России',
            (xml.row[1].cell[14]): 'в валюте',
            (xml.row[1].cell[15]): 'в рублях по курсу Банка России',
            (xml.row[2].cell[0]) : '1'
    ]
    (2..15).each { index ->
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

    def dataRows = dataRowHelper.allCached

    // Итоговая строка
    def totalRow = getDataRow(dataRows, 'total')
    // Очистка итогов
    totalColumns.each { alias ->
        totalRow[alias] = null
    }

    for (def row : xml.row) {
        xmlIndexRow++

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != '') {
            continue
        }

        def xmlIndexCol = 0
        def int xlsIndexRow = xmlIndexRow + rowOffset

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        def cols = isBalancePeriod() ? (allColumns - 'number') : editableColumns
        def autoColumns = isBalancePeriod() ? ['number'] : autoFillColumns
        cols.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 1
        xmlIndexCol++
        // fix
        xmlIndexCol++
        // графа 2
        newRow.bill = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 3
        newRow.buyDate = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 4
        newRow.currency = getRecordIdImport(15, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 5
        newRow.nominal = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 6
        newRow.price = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 7
        newRow.maturity = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 8
        newRow.termDealBill = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 9
        newRow.percIncome = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 10
        newRow.implementationDate = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 11
        newRow.sum = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 12
        newRow.discountInCurrency = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 13
        newRow.discountInRub = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 14
        newRow.sumIncomeinCurrency = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 15
        newRow.sumIncomeinRuble = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        rows.add(newRow)
    }
    rows.add(totalRow)
    dataRowHelper.save(rows)
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

def getStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName)
    addTransportData(xml)

    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    checkTotalSum(dataRows, totalColumns, logger, true)
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
        def cols = isBalancePeriod() ? (allColumns - 'number') : editableColumns
        def autoColumns = isBalancePeriod() ? ['number'] : autoFillColumns
        cols.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        xmlIndexCol++
        // fix
        xmlIndexCol++
        // графа 2
        newRow.bill = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 3
        newRow.buyDate = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 4
        newRow.currency = getRecordIdImport(15, 'CODE', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 5
        newRow.nominal = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 6
        newRow.price = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 7
        newRow.maturity = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 8
        newRow.termDealBill = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 9
        newRow.percIncome = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 10
        newRow.implementationDate = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 11
        newRow.sum = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 12
        newRow.discountInCurrency = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 13
        newRow.discountInRub = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 14
        newRow.sumIncomeinCurrency = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 15
        newRow.sumIncomeinRuble = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        rows.add(newRow)
    }

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        def total = getDataRow(dataRowHelper.getAllCached(), 'total')

        // графа 13
        total.discountInRub = parseNumber(row.cell[13].text(), rnuIndexRow, 13 + colOffset, logger, true)
        // графа 15
        total.sumIncomeinRuble = parseNumber(row.cell[15].text(), rnuIndexRow, 15 + colOffset, logger, true)

        rows.add(total)
    }
    dataRowHelper.save(rows)
}