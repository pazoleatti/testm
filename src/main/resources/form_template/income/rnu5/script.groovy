package form_template.income.rnu5

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
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
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
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
def editableColumns = ['number', 'sum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'number', 'sum']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['sum']

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

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
        dataRowHelper.save(dataRows.sort { getKnu(it.number) })

        // номер последний строки предыдущей формы
        def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

        for (row in dataRows) {
            // графа 1
            row.rowNumber = ++index
        }
    }

    // посчитать "итого по коду"
    def totalRows = [:]
    def tmp = null
    def sum = 0
    dataRows.eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.number
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (tmp != row.number) {
            def code = getKnu(tmp)
            totalRows.put(i, getNewRow(code, sum, tmp))
            sum = 0
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == dataRows.size() - 1) {
            sum += (row.sum ?: 0)
            def code = getKnu(row.number)
            def totalRowCode = getNewRow(code, sum, row.number)
            totalRows.put(i + 1, totalRowCode)
            sum = 0
        }
        sum += (row.sum ?: 0)
        tmp = row.number
    }

    // добавить "итого по коду" в таблицу
    def i = 1
    totalRows.each { index, row ->
        dataRowHelper.insert(row, index + i++)
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size() + 1)
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

    for (def row : dataRows) {
        if (row.getAlias() ==~ /total\d+/) { // если подитог
            totalRows[row.getAlias().replace('total', '')] = row.sum
            continue
        }
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «№ пп»
        if (++i != row.rowNumber) {
            logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
        }

        // 4. Арифметическая проверка итоговых значений по каждому <Коду классификации расходов>
        if (sumRowsByCode[""+row.number] != null) {
            sumRowsByCode[""+row.number] += row.sum ?: 0
        } else {
            sumRowsByCode[""+row.number] = row.sum ?: 0
        }
    }

    //4. Арифметическая проверка итоговых значений по каждому <Коду классификации расходов>
    totalRows.each { key, val ->
        if (totalRows.get(key) != sumRowsByCode.get(key)) {
            def msg =  formData.createDataRow().getCell('sum').column.name
            def code = getKnu(Long.valueOf(key))
            logger.error("Неверное итоговое значение по коду '$code' графы «$msg»!")
        }
    }

    // 5. Арифметическая проверка итогового значения по всем строкам для «Графы 5»
    checkTotalSum(dataRows, totalColumns, logger, true)
}

// Получить новую строку подитога
def getNewRow(def alias, def sum, def alias_id) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias_id)
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

/**
 * Консолидация.
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).allCached.each { sRow ->
                    if (sRow.getAlias() == null || sRow.getAlias() == '') {
                        def isFind = false
                        // строки приемника - искать совпадения, если совпадения есть, то суммировать графу 5
                        for (def row : dataRows) {
                            if (sRow.number == row.number) {
                                isFind = true
                                totalColumns.each { alias ->
                                    def tmp = (row.getCell(alias).value ?: 0) + (sRow.getCell(alias).value ?: 0)
                                    row.getCell(alias).setValue(tmp, null)
                                }
                                break
                            }
                        }
                        // если совпадений нет, то просто добавить строку
                        if (!isFind) {
                            dataRows.add(sRow)
                        }
                    }
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}