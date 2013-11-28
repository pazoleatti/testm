package form_template.income.rnu72

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Форма "(РНУ-72) Регистр налогового учёта уступки права требования как реализации финансовых услуг и операций с закладными".
 *
 * TODO:
 *      - перед логическими проверками или расчетами проверка рну-71.1 - опечатка в чтз! надо будет проверять рну-4 для заполнения графы 6, а пока графа 6 заполняется в ручную
 *
 * @author rtimerbaev
 */

// графа 1  - number
// графа 0  - forLabel - fix
// графа 2  - date
// графа 3  - nominal
// графа 4  - price
// графа 5  - income
// графа 6  - cost279
// графа 7  - costReserve
// графа 8  - loss
// графа 9  - profit

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
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

// все атрибуты
@Field
def allColumns = ['number', 'forLabel', 'date', 'nominal', 'price', 'income', 'cost279', 'costReserve', 'loss', 'profit']

// Редактируемые атрибуты (графа 2..7)
@Field
def editableColumns = ['date', 'nominal', 'price', 'income', 'cost279', 'costReserve']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты (графа 2, 3)
@Field
def groupColumns = ['date', 'nominal']

// Проверяемые на пустые значения атрибуты (графа 1..9)
@Field
def nonEmptyColumns = ['number', 'date', 'nominal', 'price', 'income', 'cost279', 'costReserve', 'loss', 'profit']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 5..9)
@Field
def totalSumColumns = ['income', 'cost279', 'costReserve', 'loss', 'profit']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

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

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    def index = getPrevRowNumber()
    dataRows.eachWithIndex { row, i ->
        // графа 1
        row.number = ++index
        // графа 8
        row.loss = calc8(row)
        // графа 9
        row.profit = calc9(row)
    }

    // добавить итого
    def total = getTotalRow(dataRows)
    dataRows.add(total)
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // алиасы графов для арифметической проверки (графа 8, 9)
    def arithmeticCheckAlias = ['loss', 'profit']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    // номер последний строки предыдущей формы
    def rowNumber = getPrevRowNumber()

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «№ пп» (графа 1)
        if (++rowNumber != row.number) {
            logger.error('Нарушена уникальность номера по порядку!')
        }

        // 3. Проверка на нулевые значения
        def hasError = true
        for (def alias : ['income', 'cost279', 'costReserve', 'loss', 'profit']) {
            if (row.getCell(alias).value != 0) {
                hasError = false
                break
            }
        }
        if (hasError) {
            logger.error(errorMsg + 'все суммы по операции нулевые!')
        }

        // 4. Арифметическая проверка графы 8, 9
        needValue['loss'] = calc8(row)
        needValue['profit'] = calc9(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
    }

    // 5. Проверка итогового значений по всей форме (графа 5..9)
    def totalRow = dataRowHelper.getDataRow(dataRows, 'total')
    def tmpRow = getTotalRow(dataRows)
    if (isDiffRow(totalRow, tmpRow, totalSumColumns)) {
        logger.error('Итоговые значения рассчитаны неверно!')
    }
}

/*
 * Вспомогательные методы.
 */

/** Получить сумму столбца. */
def getSum(def dataRows, def columnAlias) {
    def sum = 0
    dataRows.each { row ->
        if (row.getAlias() == null) {
            sum += (row.getCell(columnAlias).value ?: 0)
        }
    }
    return sum
}

def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

/** Получить модуль числа. Вместо Math.abs() потому что возможна потеря точности. */
def abs(def value) {
    return value < 0 ? -value : value
}

/** Получить итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.forLabel = 'Итого'
    newRow.getCell('forLabel').setColSpan(2)
    allColumns.each { alias ->
        newRow.getCell(alias).setStyleAlias('Контрольные суммы')
    }
    totalSumColumns.each { alias ->
        newRow.getCell(alias).setValue(getSum(dataRows, alias))
    }
    return newRow
}

/** Получить одинаковую часть расчетов для графы 8 или 9. */
def calcFor8or9(def row) {
    if (row.income == null || row.cost279 == null || row.costReserve == null) {
        return null
    }
    return row.income - (row.cost279 - row.costReserve)
}

def calc8(def row) {
    def tmp = calcFor8or9(row)
    if (tmp == null) {
        return null
    }
    return roundValue((tmp < 0 ? abs(tmp) : 0), 2)
}

def calc9(def row) {
    def tmp = calcFor8or9(row)
    if (tmp == null) {
        return null
    }
    return roundValue((tmp >= 0 ? tmp : 0), 2)
}

def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/** Получить значение "Номер по порядку" из формы предыдущего периода. */
def getPrevRowNumber() {
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // получить номер последний строки предыдущей формы если текущая форма не первая в этом году
    if (reportPeriod != null && reportPeriod.order == 1) {
        return 0
    }
    def prevFormData = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    def prevDataRows = (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allCached : null)
    if (prevDataRows != null && !prevDataRows.isEmpty()) {
        // пропустить последнюю итоговую строку
        return prevDataRows[prevDataRows.size - 2].number
    }
    return 0
}