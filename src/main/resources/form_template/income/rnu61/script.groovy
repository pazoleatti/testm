package form_template.income.rnu61
/**
 * Скрипт для РНУ-61
 * Форма "(РНУ-61) Регистр налогового учёта расходов по процентным векселям ОАО «Сбербанк России», учёт которых требует применения метода начисления"
 * formTemplateId=352
 *
 * @author akadyrgulov
 * @author Stanislav Yasinskiy
 */

import java.math.RoundingMode

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        logicalCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicalCheck()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicalCheck()
        break
    case FormDataEvent.COMPOSE: // обобщить
        consolidation()
        calc()
        logicalCheck()
        break
}

// графа 1  - rowNumber
// графа 2  - billNumber
// графа 3  - creationDate
// графа 4  - nominal
// графа 5  - currencyCode
// графа 6  - rateBRBill
// графа 7  - rateBROperation
// графа 8  - paymentStart
// графа 9  - paymentEnd
// графа 10 - interestRate
// графа 11 - operationDate
// графа 12 - sum70606
// графа 13 - sumLimit
// графа 14 - percAdjustment

//Добавить новую строку
void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    // графа 2..5, 8..12
    ['billNumber', 'creationDate', 'nominal', 'currencyCode', 'paymentStart',
            'paymentEnd', 'interestRate', 'operationDate', 'sum70606'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
}

// Удалить строку
void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

// Ресчет графы 6 и 7
def calc6and7(def currencyCode, def date) {
    if (currencyCode != null && date != null) {
        rate = 1
        if (!isRubleCurrency(currencyCode)) {
            rate = getRate(date, currencyCode)
        }
        return rate
    } else {
        return null
    }
}

// Ресчет графы 12
def calc12(def currencyCode) {
    // TODO вопрос к заказчику
    val =row.sum70606
    if (row.currencyCode != null && isRubleCurrency(row.currencyCode)) {
    } else {
    }
    return val
}

def calc13(def row, def daysOfYear) {
    // TODO вопрос к заказчику
    val =0
    if (row.sum70606 == null) {
        if (row.operationDate < row.paymentEnd) {
            val = round((row.nominal * row.interestRate / 100 * (row.operationDate - row.creationDate)) / daysOfYear) * row.rateBROperation
        }
        if (row.operationDate > row.paymentEnd) {
            val = round((row.nominal * row.interestRate / 100 * (row.paymentEnd - row.creationDate) / daysOfYear)) * row.rateBROperation
        }
    }
    return val
}

def calc14(def row) {
    // TODO вопрос к заказчику
    val =0
    if (row.sum70606 != null && row.sum70606 != '') {
        if (row.sum70606 > row.sumLimit) {
            val = row.sum70606 - row.sumLimit
        }
    } else {
        val = row.nominal * (row.rateBRBill - row.rateBROperation)
    }
    return val
}

// Расчеты. Алгоритмы заполнения полей формы.
def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить строку "итого"
    for (Iterator<DataRow> iter = dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        row = (DataRow) iter.next()
        if (isTotal(row)) {
            iter.remove()
            dataRowHelper.delete(row)
        }
    }

    if (dataRows.isEmpty()) {
        return
    }

    Calendar periodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    def daysOfYear = (new GregorianCalendar()).isLeapYear(periodStartDate.get(Calendar.YEAR)) ? 366 : 365
    // графа 14 для последней строки "итого"
    def total14 = 0
    // индекс
    def index = 0

    for (def row in dataRows) {

        // графа 1
        row.rowNumber = ++index

        // графа 6
        row.rateBRBill = calc6and7(row.currencyCode, row.creationDate)

        // графа 7
        row.rateBROperation = calc6and7(row.currencyCode, row.operationDate)

        // графа 12
        row.sum70606 = calc12(row)

        // графа 13
        row.sumLimit = calc13(row, daysOfYear)

        // графа 14
        row.percAdjustment = calc14(row)

        // графа 14 для последней строки "итого"
        if (row.percAdjustment != null)
            total14 += row.percAdjustment
    }
    dataRowHelper.update(dataRows);

    // добавить строки "итого"
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.billNumber = 'Итого'
    totalRow.getCell('billNumber').colSpan = 12
    setTotalStyle(totalRow)
    totalRow.percAdjustment = total14

    dataRowHelper.insert(totalRow, index + 1)
}

def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {

        // 1. Проверка на заполнение полей 1..14
        def requiredColumns = ['rowNumber', 'billNumber', 'creationDate', 'nominal',
                'currencyCode', 'rateBRBill', 'rateBROperation', 'paymentStart', 'paymentEnd',
                'interestRate', 'operationDate', 'sum70606', 'sumLimit', 'percAdjustment']
        for (def row in dataRows) {
            if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
                return false
            }
        }

        // графа 14 для последней строки "итого"
        def total14 = 0
        // итоговая строка
        def totalRow = null
        // Инвентарные номера
        def List<String> invList = new ArrayList<String>()
        // Отчетная дата
        def reportDate = getReportDate()
        //Начальная дата отчетного периода
        def tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
        def reportDateStart = (tmp ? tmp.getTime() : null)
        Calendar periodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
        def daysOfYear = (new GregorianCalendar()).isLeapYear(periodStartDate.get(Calendar.YEAR)) ? 366 : 365

        for (def row in dataRows) {
            if (isTotal(row)) {
                totalRow = row
                continue
            }

            // Проверка на уникальность поля «инвентарный номер»
            if (invList.contains(row.billNumber)) {
                logger.error("Инвентарный номер не уникальный!")
                return false
            } else {
                invList.add(row.billNumber)
            }

            // 2. Проверка даты совершения операции и границ отчетного периода
            if (row.operationDate < reportDateStart || row.operationDate > reportDate) {
                logger.error("Дата совершения операции вне границ отчетного периода!")
                return false
            }

            // 4. Проверка на нулевые значения
            if (row.sum70606 == 0 && row.sumLimit ==0 && row.percAdjustment == 0) {
                logger.error("Все суммы по операции нулевые!")
                return false
            }

            // 5. Арифметические проверки
            if (check(row.getCell('rateBRBill'), calc6and7(row.currencyCode, row.creationDate)) ||
                    check(row.getCell('rateBROperation'), calc6and7(row.currencyCode, row.operationDate)) ||
                    check(row.getCell('sum70606'), calc12(row)) ||
                    check(row.getCell('sumLimit'), calc13(row, daysOfYear)) ||
                    check(row.getCell('percAdjustment'), calc14(row))) {
                return false
            }

            // 5. Арифметические проверки расчета итоговой строки
            if (row.percAdjustment != null) {
                total14 += row.percAdjustment
            }

            // Проверки соответствия НСИ.
            if (!checkNSI(row, "currencyCode", "Единый справочник валют", 15)) {
                return false
            }
        }

        // 5. Арифметические проверки расчета итоговой строки
        if (totalRow != null && total14 != totalRow.percAdjustment) {
            logger.error('Итоговые значения рассчитаны неверно!')
            return false
        }
    }
    return true
}

boolean check(def cell, def value) {
    if (cell.value != value) {
        logger.error("Неверно рассчитана графа «" + cell.column.name + "»!")
        return false
    }
    return true
}

// Проверка соответствия НСИ
boolean checkNSI(DataRow<Cell> row, String alias, String msg, Long id) {
    def cell = row.getCell(alias)
    if (cell.value != null && refBookService.getRecordData(id, cell.value) == null) {
        def msg2 = cell.column.name
        def rowNum = row.getIndex()
        logger.warn("Строка $rowNum: В справочнике «$msg» не найден элемент «$msg2»!")
        return false
    }
    return true
}

// Проверка при создании формы
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

// Проверка является ли строка итоговой
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def data = getData(formData)
    def from = 0
    def to = getRows(data).size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
}

// Установить стиль для итоговых строк
void setTotalStyle(def row) {
    ['rowNumber', 'billNumber', 'creationDate', 'nominal', 'currencyCode', 'rateBRBill', 'rateBROperation',
            'paymentStart', 'paymentEnd', 'interestRate', 'operationDate',
            'sum70606', 'sumLimit', 'percAdjustment'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Посчитать сумму указанного графа для строк с общим кодом классификации
 *
 * @param code код классификации дохода
 * @param alias название графа
 */
def calcSumByCode(def code, def alias) {
    def data = getData(formData)
    def sum = 0
    getRows(data).each { row ->
        if (!isTotal(row) && row.code == code) {
            sum += row.getCell(alias).getValue()
        }
    }
    return sum
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    def data = getData(formData)
    getRows(data).indexOf(row)
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @param useLog нужно ли записывать сообщения в лог
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    def cell
    columns.each {
        cell = row.getCell(it)
        if (cell.getValue() == null || row.getCell(it).getValue() == '') {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.rowNumber
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 */
def getRows(def data) {
    return data.getAllCached();
}

/**
 * Получить название графы по псевдониму.
 *
 * @param row строка
 * @param alias псевдоним графы
 */
def getColumnName(def row, def alias) {
    if (row != null && alias != null) {
        return row.getCell(alias).getColumn().getName().replace('%', '%%')
    }
    return ''
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    return data.getDataRow(getRows(data), alias)
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

def round(def value, def int precision = 2) {
    if (value == null) {
        return null
    }
    return value.setScale(precision, RoundingMode.HALF_UP)
}

//Получить отчетную дату
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}
