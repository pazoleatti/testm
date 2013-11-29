package form_template.income.rnu4

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * (РНУ-5) Простой регистр налогового учёта «расходы»
 * formTemplateId=317
 *
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
def editableColumns = ['code', 'sum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'code', 'number', 'name', 'sum']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['sum']

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

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    if (!isBalancePeriod && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        def formName = formData.getFormType().getName()
        throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
    }
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
        def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

        for (row in dataRows) {
            // графа 1
            row.rowNumber = ++index

            row.number = row.code
            row.name = row.code
        }
    }

    // посчитать "итого по коду"
    def totalRows = [:]
    def tmp = null
    def sum = 0
    dataRows.eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.code
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (tmp != row.code) {
            def code = getKnu(tmp)
            totalRows.put(i, getNewRow(code, sum))
            sum = 0
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == dataRows.size() - 1) {
            sum += (row.sum ?: 0)
            def code = getKnu(row.code)
            def totalRowCode = getNewRow(code, sum)
            totalRows.put(i + 1, totalRowCode)
            sum = 0
        }
        sum += (row.sum ?: 0)
        tmp = row.code
    }

    // добавить "итого по коду" в таблицу
    def i = 1
    totalRows.each { index, row ->
        dataRowHelper.insert(row, index + i++)
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size + 1)
    dataRowHelper.save(dataRows)
}

def calcTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 4

    ['rowNumber', 'fix', 'sum'].each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)

    return totalRow
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    //две карты: одна с реальными значениями итого по кодам, а вторая - с рассчитанными
    def totalRows = [:]
    def sumRowsByCode = [:]

    def index
    def errorMsg

    for (def row : dataRows) {
        if (row.getAlias() ==~ /total\d+/) { // если подитог
            totalRows[row.getAlias().replace('total', '')] = row.sum
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
        if (++i != row.rowNumber) {
            logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
        }

        // 3. Проверка соответствия «Графы 3» «Графе 2»
        if(row.code != row.number){
            logger.error(errorMsg + "Балансовый счет не соответствует коду налогового учета!")
        }

        // 4. Арифметическая проверка итоговых значений по каждому <Коду классификации расходов>
        def code = getKnu(row.code)

        if (sumRowsByCode[code] != null) {
            sumRowsByCode[code] += row.sum ?: 0
        } else {
            sumRowsByCode[code] = row.sum ?: 0
        }

        // Проверки соответствия НСИ
        checkNSI(27, row, 'code')
        checkNSI(27, row, 'number')
        checkNSI(27, row, 'name')

    }

    //4. Арифметическая проверка итоговых значений по каждому <Коду классификации расходов>
    totalRows.each { key, val ->
        if (totalRows.get(key) != sumRowsByCode.get(key)) {
            def msg =  formData.createDataRow().getCell('sum').column.name
            logger.error("Неверное итоговое значение по коду '$key' графы «$msg»!")
        }
    }

    // 5. Арифметическая проверка итогового значения по всем строкам для «Графы 5»
    checkTotalSum(dataRows, totalColumns, logger, true)
}

// Получить новую строку подитога
def getNewRow(def alias, def sum) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    newRow.sum = sum
    newRow.fix = 'Итого по КНУ ' + alias
    newRow.getCell('fix').colSpan = 4
    ['rowNumber', 'fix', 'sum'].each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

def String getKnu(def code) {
    return getRefBookValue(27, code)?.CODE?.stringValue
}