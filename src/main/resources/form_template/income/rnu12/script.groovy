package form_template.income.rnu12

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Скрипт для РНУ-12 (rnu12.groovy).
 * Форма "(РНУ-12) Регистр налогового учёта расходов по хозяйственным операциям и оказанным Банку услугам".
 * formTemplateId=364
 *
 * графа 1  - rowNumber
 * графа 2  - code
 * графа 3  - numberFirstRecord
 * графа 4  - opy
 * графа 5  - operationDate
 * графа 6  - name
 * графа 7  - documentNumber
 * графа 8  - date
 * графа 9  - periodCounts
 * графа 10 - advancePayment
 * графа 11 - outcomeInNalog
 * графа 12 - outcomeInBuh
 *
 * @author rtimerbaev
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
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
    case FormDataEvent.COMPOSE: // Консолидация
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

// Редактируемые атрибуты
@Field
def editableColumns = ['code', 'numberFirstRecord', 'opy', 'operationDate', 'name', 'documentNumber', 'date',
        'periodCounts', 'advancePayment', 'outcomeInBuh']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'code', 'numberFirstRecord', 'opy', 'operationDate', 'name', 'documentNumber',
        'date', 'periodCounts', 'advancePayment', 'outcomeInBuh']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['advancePayment', 'outcomeInNalog', 'outcomeInBuh']

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

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

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        // сортируем по кодам
        dataRowHelper.save(dataRows.sort { getKnu(it.code) })

        // номер последний строки предыдущей формы
        def number = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)

        for (row in dataRows) {
            row.rowNumber = ++number
            // графа 11
            row.outcomeInNalog = calc11(row)
        }

        // посчитать "итого по коду"
        def totalRows = [:]
        def tmp = null
        def sums = [:]
        totalColumns.each {
            sums[it] = 0
        }

        dataRows.eachWithIndex { row, i ->
            if (tmp == null) {
                tmp = row.code
            }
            // если код расходы поменялся то создать новую строку "итого по коду"
            if (tmp != row.code) {
                totalRows.put(i, getNewRow(getKnu(tmp), sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
            if (i == dataRows.size() - 1) {
                totalColumns.each {
                    def val = row.getCell(it).getValue()
                    if (val != null)
                        sums[it] += val
                }
                totalRows.put(i + 1, getNewRow(getKnu(row.code), sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                def val = row.getCell(it).getValue()
                if (val != null)
                    sums[it] += val
            }
            tmp = row.code
        }

        // добавить "итого по коду" в таблицу
        def i = 1
        totalRows.each { index, row ->
            dataRowHelper.insert(row, index + i++)
        }
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size + 1)
    dataRowHelper.save(dataRows)

}

def calcTotalRow(def dataRows) {
    def totalRow = getTotalRow('total', 'Итого')
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получить новую строку.
def getNewRow(def alias, def sums) {
    def newRow = getTotalRow('total' + alias, 'Итого по КНУ ' + alias)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}


def getTotalRow(def alias, def title) {
    def newRow = formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = title
    newRow.getCell('fix').colSpan = 9
    ['rowNumber', 'fix', 'code', 'numberFirstRecord', 'numberFirstRecord', 'opy', 'operationDate',
            'name', 'documentNumber', 'date', 'periodCounts',
            'advancePayment', 'outcomeInNalog', 'outcomeInBuh'].each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

def BigDecimal calc11(def row) {
    if (row.advancePayment != null && row.advancePayment > 0
            && row.advancePayment != null && row.periodCounts != null && row.periodCounts != 0) {
        return (row.advancePayment / row.periodCounts).setScale(2, RoundingMode.HALF_UP)
    }
    return null
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    def i = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)

    // Дата начала отчетного периода
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // алиасы графов для арифметической проверки
    def arithmeticCheckAlias = ['outcomeInNalog']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        index = row.getIndex()
        errorMsg = "Строка $index: "

        // Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 1. Проверка даты совершения операции и границ отчетного периода (графа 5)
        if (row.operationDate != null && (row.operationDate.after(endDate) || row.operationDate.before(startDate))) {
            logger.error(errorMsg + 'Дата совершения операции вне границ отчётного периода!')
        }

        // 2. Проверка количества отчетных периодов при авансовых платежах (графа 9)
        if (row.periodCounts != null && (row.periodCounts < 1 || 999 < row.periodCounts)) {
            logger.error(errorMsg + 'Неверное количество отчетных периодов при авансовых платежах!')
        }

        // 3. Проверка на нулевые значения (графа 11, 12)
        if (row.outcomeInNalog == 0 && row.outcomeInBuh == 0) {
            logger.error(errorMsg + 'Все суммы по операции нулевые!')
        }

        // 4. Проверка формата номера первой записи
        if (row.numberFirstRecord != null && !row.numberFirstRecord.matches('\\d{2}-\\w{6}')) {
            logger.error(errorMsg + 'Неправильно указан номер первой записи (формат: ГГ-НННННН, см. №852-р в актуальной редакции)!')
        }

        // 7. Проверка на уникальность поля «№ пп» (графа 1)
        if (++i != row.rowNumber) {
            logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
        }

        needValue['outcomeInNalog'] = calc11(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // Проверки соответствия НСИ
        checkNSI(27, row, "code")
        checkNSI(27, row, "opy")
    }

    // Арифметическая проверка итоговых значений строк «Итого по КНУ»
    checkSubTotalSum(dataRows, totalColumns, logger, true)

    // Арифметическая проверка итогового значения по всем строкам
    checkTotalSum(dataRows, totalColumns, logger, true)
}

def String getKnu(def code) {
    return getRefBookValue(27, code)?.CODE?.stringValue
}