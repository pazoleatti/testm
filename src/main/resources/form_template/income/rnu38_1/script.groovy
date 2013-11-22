package form_template.income.rnu38_1

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Форма "РНУ-38.1" "Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 1".
 *
 * @author ivildanov
 *
 * Графы
 * 1  -  series
 * 2  -  amount
 * 3  -  shortPositionDate
 * 4  -  maturityDate
 * 5  -  incomeCurrentCoupon
 * 6  -  currentPeriod
 * 7  -  incomePrev
 * 8  -  incomeShortPosition
 * 9  -  totalPercIncome
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
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

// Все атрибуты
@Field
def allColumns = ['series', 'amount', 'shortPositionDate', 'maturityDate', 'incomeCurrentCoupon', 'currentPeriod', 'incomePrev', 'incomeShortPosition', 'totalPercIncome']

// Редактируемые атрибуты (графа 1..6)
@Field
def editableColumns = ['series', 'amount', 'shortPositionDate', 'maturityDate', 'incomeCurrentCoupon', 'currentPeriod']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты (графа 1)
@Field
def groupColumns = ['series']

// Проверяемые на пустые значения атрибуты (графа 1..6, 9)
@Field
def nonEmptyColumns = ['series', 'amount', 'shortPositionDate', 'maturityDate', 'incomeCurrentCoupon', 'currentPeriod', 'totalPercIncome']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 2, 7..9)
@Field
def totalColumns = ['amount', 'incomePrev', 'incomeShortPosition', 'totalPercIncome']

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

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            currentDate, rowIndex, cellName, logger, required)
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

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // алиасы графов для арифметической проверки (графа 7..9)
    def arithmeticCheckAlias = ['incomePrev', 'incomeShortPosition', 'totalPercIncome']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // отчетная дата
    def reportDay = reportPeriodService.getMonthReportDate(formData.reportPeriodId, formData.periodOrder)?.time
    // последний день месяца
    def lastDay = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder)?.time
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // . Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        //  1. Проверка даты открытия короткой позиции
        if (row.shortPositionDate > reportDay) {
            logger.error(errorMsg + 'неверно указана дата приобретения (открытия короткой позиции)!')
        }

        // 2. Проверка даты погашения
        if (row.maturityDate > reportDay) {
            logger.error(errorMsg + 'неверно указана дата погашения предыдущего купона!')
        }

        // 3. Арифметические проверки графы 7..9
        needValue['incomePrev'] = calc7(row, lastDay)
        needValue['incomeShortPosition'] = calc8(row, lastDay)
        needValue['totalPercIncome'] = calc9(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // проверка делеления не ноль
        if (row.currentPeriod == 0) {
            def name = row.getCell('currentPeriod').column.name
            logger.error(errorMsg + "деление на ноль: \"$name\" имеет нулевое значение.")
        }
    }

    // 4. Проверка итоговых значений по всей форме
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    // последний день месяца
    def lastDay = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder)?.time
    for (def row : dataRows) {
        // графа 7
        row.incomePrev = calc7(row, lastDay)
        // графа 8
        row.incomeShortPosition = calc8(row, lastDay)
        // графа 9
        row.totalPercIncome = calc9(row)
    }
    // добавить строки "итого"
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

/**
 * Посчитать значение графы 7.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def calc7(def row, def lastDay) {
    if (row.maturityDate == null || row.shortPositionDate == null || row.incomeCurrentCoupon == null ||
            lastDay == null || row.currentPeriod == null || row.currentPeriod == 0 || row.amount == null) {
        return null
    }
    if (row.maturityDate > row.shortPositionDate) {
        def tmp = roundValue((row.incomeCurrentCoupon * (lastDay - row.maturityDate) / row.currentPeriod), 2)
        return roundValue(tmp * row.amount, 2)
    } else {
        return null
    }
}

/**
 * Посчитать значение графы 8.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def calc8(def row, def lastDay) {
    if (row.maturityDate == null || row.shortPositionDate == null || row.incomeCurrentCoupon == null ||
            lastDay == null || row.currentPeriod == null || row.currentPeriod == 0 || row.amount == null) {
        return null
    }
    if (row.maturityDate <= row.shortPositionDate) {
        def tmp = roundValue((row.incomeCurrentCoupon * (lastDay - row.shortPositionDate) / row.currentPeriod), 2)
        return roundValue(tmp * row.amount, 2)
    } else {
        return null
    }
}

def calc9(def row) {
    return roundValue((row.incomePrev ?: 0) + (row.incomeShortPosition ?: 0), 2)
}

/** Получить новую стролу с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each { column ->
        newRow.getCell(column).setEditable(true)
        newRow.getCell(column).styleAlias = 'Редактируемая'
    }
    autoFillColumns.each { column ->
        newRow.getCell(column).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

/** Получить итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.series = 'Итого'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}