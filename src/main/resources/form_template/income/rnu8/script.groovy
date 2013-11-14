package form_template.income.rnu8

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * (РНУ-8) Простой регистр налогового учёта «Требования»
 * formTemplateId=320
 *
 * графа 1  - number
 * графа 2  - code
 * графа 3  - balance
 * графа 4  - name
 * графа 5  - income
 * графа 6  - outcome
 *
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
def editableColumns = ['code', 'income', 'outcome']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'code', 'balance', 'name', 'income', 'outcome']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['income', 'outcome']

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
            row.number = ++number
            row.balance = row.code
            row.name = row.code
        }

        // посчитать "итого по коду"
        def totalRows = [:]
        def tmp = null
        def sum = 0, sum2 = 0
        dataRows.eachWithIndex { row, i ->
            if (tmp == null) {
                tmp = row.code
            }
            // если код расходы поменялся то создать новую строку "итого по коду"
            if (tmp != row.code) {
                def code = getKnu(tmp)
                totalRows.put(i, getNewRow(code, sum, sum2))
                sum = 0
                sum2 = 0
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
            if (i == dataRows.size() - 1) {
                sum += (row.income ?: 0)
                sum2 += (row.outcome ?: 0)
                def code = getKnu(row.code)
                def totalRowCode = getNewRow(code, sum, sum2)
                totalRows.put(i + 1, totalRowCode)
                sum = 0
                sum2 = 0
            }
            sum += (row.income ?: 0)
            sum2 += (row.outcome ?: 0)
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

// Получить новую строку подитога
def getNewRow(def alias, def sum, def sum2) {
    def newRow = getTotalRow('total' + alias, 'Итого по КНУ ' + alias)
    newRow.income = sum
    newRow.outcome = sum2
    return newRow
}

def getTotalRow(def alias, def title) {
    def newRow = formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = title
    newRow.getCell('fix').colSpan = 4
    ['number', 'fix', 'income', 'outcome'].each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }
    def i = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)
    //две карты: одна с реальными значениями итого по кодам, а вторая - с рассчитанными
    def totalRows = [:]
    def sum1 = [:]
    //две карты: одна с реальными значениями итого по кодам, а вторая - с рассчитанными
    def totalRows2 = [:]
    def sum2 = [:]

    for (def row : dataRows) {
        if (row.getAlias() ==~ /total\d+/) { // если подитог
            totalRows[row.getAlias().replace('total', '')] = row.income
            totalRows2[row.getAlias().replace('total', '')] = row.outcome
            continue
        }
        if (row.getAlias() != null) {
            continue
        }
        index = row.getIndex()
        errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «№ пп»
        if (++i != row.number) {
            logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
        }

        // 3. Проверка соответствия балансового счета коду налогового учета
        if (row.code != row.name) {
            logger.error(errorMsg + "Балансовый счет не соответствует коду налогового учета!")
        }

        // Проверки соответствия НСИ
        checkNSI(28, row, 'code')
        checkNSI(28, row, 'balance')
        checkNSI(28, row, 'name')

        // 4. Арифметическая проверка итоговых значений строк «Итого по КНУ»
        def code = getKnu(row.code)
        if (sum1[code] != null) {
            sum1[code] += row.income ?: 0
        } else {
            sum1[code] = row.income ?: 0
        }
        if (sum2 [code] != null) {
            sum2 [code] += row.outcome ?: 0
        } else {
            sum2 [code] = row.outcome ?: 0
        }
    }

    // 4. Арифметическая проверка итоговых значений строк «Итого по КНУ»
    totalRows.each { key, val ->
        if (totalRows.get(key) != sum1.get(key)) {
            def msg = formData.createDataRow().getCell('income').column.name
            logger.error("Неверное итоговое значение по коду '$key' графы «$msg»!")
        }
    }
    totalRows2.each { key, val ->
        if (totalRows2.get(key) != sum2 .get(key)) {
            def msg = formData.createDataRow().getCell('outcome').column.name
            logger.error("Неверное итоговое значение по коду '$key' графы «$msg»!")
        }
    }

    // 5. Арифметическая проверка итогового значения по всем строкам
    checkTotalSum(dataRows, totalColumns, logger, true)
}

def String getKnu(def code) {
    return getRefBookValue(28, code)?.CODE?.stringValue
}