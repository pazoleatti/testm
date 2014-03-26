package form_template.income.rnu61.v1970

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
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
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
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
def allColumns = ['rowNumber', 'billNumber', 'creationDate', 'nominal', 'currencyCode', 'rateBRBill',
        'rateBROperation', 'paymentStart', 'paymentEnd', 'interestRate', 'operationDate', 'sum70606', 'sumLimit',
        'percAdjustment']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['percAdjustment']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
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
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
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

    // Удаление итогов
    deleteAllAliased(dataRows)

    if (!dataRows.isEmpty()) {

        def daysOfYear = getCountDays(reportPeriodService.getStartDate(formData.reportPeriodId).time)

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
    totalRow.billNumber = 'Итого'
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
    if (row.paymentEnd == null || row.creationDate == null) {
        return null
    }
    if (getCountDays(row.creationDate) != getCountDays(row.paymentEnd)) {
        return row.sumLimit
    }
    if (row.sum70606 == null && isRubleCurrency(row.currencyCode)) {
        if (row.operationDate != null && row.nominal != null
                && row.interestRate != null && row.rateBROperation != null) {
            def date = (row.operationDate < row.paymentEnd) ? row.operationDate : row.paymentEnd
            return ((row.nominal * row.interestRate / 100 * (date - row.creationDate)) / daysOfYear)
                    .setScale(2, RoundingMode.HALF_UP) * row.rateBROperation
        }
    }
    return null
}

// Ресчет графы 14
def BigDecimal calc14(def row) {
    if (row.sum70606 != null) {
        if (row.sumLimit != null && row.sum70606 > row.sumLimit) {
            return row.sum70606 - row.sumLimit
        } else {
            return null
        }
    } else if (row.nominal != null && row.rateBRBill != null && row.rateBROperation != null) {
        return row.nominal * (row.rateBRBill - row.rateBROperation)
    }
    return null
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
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, false)

        // Проверка на уникальность поля «№ пп»
        if (++i != row.rowNumber) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // Проверка на уникальность поля «инвентарный номер»
        if (invList.contains(row.billNumber)) {
            logger.error(errorMsg + "Инвентарный номер не уникальный!")
        } else {
            invList.add(row.billNumber)
        }

        // 2. Проверка даты совершения операции и границ отчетного периода
        if (row.operationDate < reportDateStart || row.operationDate > reportDate) {
            logger.error(errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }

        // 4. Проверка на нулевые значения
        if (row.sum70606 == 0 && row.sumLimit == 0 && row.percAdjustment == 0) {
            logger.error(errorMsg + "Все суммы по операции нулевые!")
        }

        // 5. Арифметические проверки
        needValue['rateBRBill'] = calc6and7(row.currencyCode, row.creationDate)
        needValue['rateBROperation'] = calc6and7(row.currencyCode, row.operationDate)
        needValue['sumLimit'] = calc13(row, daysOfYear)
        needValue['percAdjustment'] = calc14(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // Проверки соответствия НСИ
        checkNSI(15, row, 'currencyCode')
    }

    // 5. Арифметические проверки расчета итоговой строки
    checkTotalSum(dataRows, totalColumns, logger, true)
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
            (xml.row[0].cell[1]): 'Номер векселя',
            (xml.row[0].cell[2]): 'Дата составления',
            (xml.row[0].cell[3]): 'Номинал',
            (xml.row[0].cell[4]): 'Код валюты',
            (xml.row[0].cell[5]): 'Курс Банка России',
            (xml.row[0].cell[7]): 'Дата наступления срока платежа',
            (xml.row[0].cell[8]): 'Дата окончания срока платежа',
            (xml.row[0].cell[9]): 'Процентная ставка',
            (xml.row[0].cell[10]): 'Дата совершения операции',
            (xml.row[0].cell[11]): 'Сумма процентов, отнесённая на счёт 70606, учитываемая в РНУ-5',
            (xml.row[0].cell[12]): 'Предельная сумма процентов, учитываемых в налоговом учёте',
            (xml.row[0].cell[13]): 'Корректировка процентов, учтённых в РНУ-5',
            (xml.row[1].cell[5]): 'на дату составления векселя',
            (xml.row[1].cell[6]): 'на дату совершения операции',
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
            (xml.row[2].cell[13]): '14'
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
        autoFillColumns.each {
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
        newRow.currencyCode = getRecordIdImport(15, 'CODE', row.cell[4].text(), xlsIndexRow, 4 + colOffset)

        // графа 8
        newRow.paymentStart = parseDate(row.cell[7].text(), "dd.MM.yyyy", xlsIndexRow, 7 + colOffset, logger, false)

        // графа 9
        newRow.paymentEnd = parseDate(row.cell[8].text(), "dd.MM.yyyy", xlsIndexRow, 8 + colOffset, logger, false)

        // графа 10
        newRow.interestRate = parseNumber(row.cell[9].text(), xlsIndexRow, 9 + colOffset, logger, false)

        // графа 11
        newRow.operationDate = parseDate(row.cell[10].text(), "dd.MM.yyyy", xlsIndexRow, 10 + colOffset, logger, false)

        // графа 12
        newRow.sum70606 = parseNumber(row.cell[11].text(), xlsIndexRow, 11 + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}
