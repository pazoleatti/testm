package form_template.income.rnu72

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-72) Регистр налогового учёта уступки права требования как реализации финансовых услуг и операций с закладными".
 *
 * TODO:
 *      - добавить перед логическими проверками или расчетами проверку рну 71.1
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        calc() && logicalCheck()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicalCheck()
        break
    case FormDataEvent.COMPOSE : // обобщить
        consolidation()
        calc() && logicalCheck()
        break
}

// графа 1  - number
// графа 2  - date
// графа 3  - nominal
// графа 4  - price
// графа 5  - income
// графа 6  - cost279
// графа 7  - costReserve
// графа 8  - loss
// графа 9  - profit

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)

    def index = 0
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while (row.getAlias() != null && index > 0) {
            row = getRows(data).get(--index)
        }
        if (index != currentDataRow.getIndex() && getRows(data).get(index).getAlias() == null) {
            index++
        }
    } else if (getRows(data).size() > 0) {
        for (int i = getRows(data).size()-1; i >= 0; i--) {
            def row = getRows(data).get(i)
            if (!isTotal(row)) {
                index = getIndex(row) + 1
                break
            }
        }
    }
    data.insert(getNewRow(),index + 1)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isTotal(currentDataRow)) {
        def data = getData(formData)
        data.delete(currentDataRow)
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
def calc() {
    def data = getData(formData)

    /*
     * Проверка обязательных полей.
     */

    // список проверяемых столбцов (графа 1..9)
    def requiredColumns = ['date', 'nominal', 'price', 'income', 'cost279', 'costReserve']

    for (def row : getRows(data)) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    /*
     * Расчеты
     */

    // удалить строку "итого"
    def deleteRows = []
    for (def row : getRows(data)) {
        if (isTotal(row)) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        getRows(data).removeAll(deleteRows)
    }
    if (getRows(data).isEmpty()) {
        return true
    }

    // отсортировать/группировать
    sort(data)

    getRows(data).eachWithIndex { row, i ->
        // графа 1
        row.number = i + 1

        // графа 8
        row.loss = calc8(row)

        // графа 9
        row.profit = calc9(row)
    }
    save(data)

    // добавить итого
    def total = getCalcTotalRow()
    insert(data, total)

    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = getData(formData)
    def i = 1

    // список проверяемых столбцов (графа 1..9)
    def requiredColumns = ['number', 'date', 'nominal', 'price',
            'income', 'cost279', 'costReserve', 'loss', 'profit']

    // суммы строки общих итогов
    def totalSums = [:]

    // графы для которых надо вычислять итого (графа 5..9)
    def totalColumns = getTotalColumns()

    // признак наличия итоговых строк
    def hasTotal = false

    def index
    def errorMsg

    // алиасы графов для арифметической проверки (графа 8, 9)
    def arithmeticCheckAlias = ['loss', 'profit']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    def colNames = []

    for (def row : getRows(data)) {
        if (isTotal(row)) {
            hasTotal = true
            continue
        }

        // 1. Обязательность заполнения поля графы 1..9
        if (!checkRequiredColumns(row, requiredColumns)) {
            return false
        }

        index = getIndex(row) + 1
        errorMsg = "В строке $index "

        // 2. Проверка на уникальность поля «№ пп» (графа 1)
        if (row.number != i++) {
            logger.error(errorMsg + 'нарушена уникальность номера по порядку!')
            return false
        }

        // 3. Проверка на нулевые значения
        // TODO (Ramil Timerbaev) в аналитике: Не (графа 5 → графа 9 = 0). Неясности со стрелкой (это перечисление графов или импликация), пока оставить, но сказали что уточнят
        // Не (графа 5 > графа 9 = 0)
        hasError = true
        ['income', 'cost279', 'costReserve', 'loss', 'profit'].each {
            if (!isEmpty(row.getCell(it).getValue())) {
                hasError = false
            }
        }
        if (hasError) {
            logger.error(errorMsg + 'все суммы по операции нулевые!')
            return false
        }

        // 4. Арифметическая проверка графы 8, 9
        needValue['loss'] = calc8(row)
        needValue['profit'] = calc9(row)
        arithmeticCheckAlias.each { alias ->
            if (needValue[alias] != row.getCell(alias).getValue()) {
                def name = getColumnName(row, alias)
                colNames.add('"' + name + '"')
            }
        }
        if (!colNames.isEmpty()) {
            def msg = colNames.join(', ')
            logger.error(errorMsg + "неверно рассчитано значение графы: $msg.")
            return false
        }

        // 5. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
        totalColumns.each { alias ->
            if (totalSums[alias] == null) {
                totalSums[alias] = 0
            }
            totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
        }
    }

    if (hasTotal) {
        def totalRow = getRowByAlias(data, 'total')

        // 5. Проверка итогового значений по всей форме (графа 5..9)
        for (def alias : totalColumns) {
            if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                logger.error('Итоговые значения рассчитаны неверно!')
                return false
            }
        }
    }
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)
    // удалить все строки и собрать из источников их строки
    data.clear()
    def newRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        newRows.add(row)
                    }
                }
            }
        }
    }
    if (!newRows.isEmpty()) {
        data.insert(newRows, 1)
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    /** Признак периода ввода остатков. */
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

    //проверка периода ввода остатков
    if (isBalancePeriod) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'forLabel', 'date', 'nominal', 'price',
            'income', 'cost279', 'costReserve', 'loss', 'profit'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получить номер строки в таблице (1..n).
 *
 * @param row строка
 */
def getIndex(def row) {
    row.getIndex() - 1
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def data = getData(formData)
    def from = 0
    def to = getRows(data).size() - 1
    if (from > to) {
        return 0
    }
    return summ(formData, getRows(data), new ColumnRange(columnAlias, from, to))
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.number
        def errorMsg = colNames.join(', ')
        if (!isEmpty(index)) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

def getNewRow() {
    def newRow = formData.createDataRow()

    // графа 2..7
    ['date', 'nominal', 'price', 'income', 'cost279', 'costReserve'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

/**
 * Проверить значение на пустоту и вернуть его.
 */
def getValue(def value) {
    return value ?: 0
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
 * Получить список строк формы.
 *
 * @param data данные нф (helper)
 */
def getRows(def data) {
    return data.getAllCached();
}

/**
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void save(def data) {
    data.save(getRows(data))
}

/**
 * Вставить новую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
}

/**
 * Удалить строку из нф
 *
 * @param data данные нф (helper)
 * @param row строка для удаления
 */
void deleteRow(def data, def row) {
    data.delete(row)
}

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

/**
 * Получить список алиасов графов для которых нужно посчитать итоги (графа 5..9).
 */
def getTotalColumns() {
    // графы для которых надо вычислять итого (графа 5..9)
    return ['income', 'cost279', 'costReserve', 'loss', 'profit']
}

/**
 * Получить модуль числа. Вместо Math.abs() потому что возможна потеря точности.
 */
def abs(def value) {
    return value < 0 ? -value : value
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    if (alias == null || alias == '' || data == null) {
        return null
    }
    for (def row : getRows(data)) {
        if (alias.equals(row.getAlias())) {
            return row
        }
    }
    return null
}

/**
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    // добавить итого (графа 5..9)
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.forLabel = 'Итого'
    totalRow.getCell('forLabel').setColSpan(2)
    setTotalStyle(totalRow)
    getTotalColumns().each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }
    return totalRow
}

/**
 * Отсорировать данные (по графе 2, 3).
 *
 * @param data данные нф (хелпер)
 */
void sort(def data) {
    getRows(data).sort { def a, def b ->
        // графа 2  - date
        // графа 3  - nominal
        if (a.date == b.date) {
            return a.nominal <=> b.nominal
        }
        return a.date <=> b.date
    }
}

/**
 * Получить одинаковую часть расчетов для графы 8 или 9.
 */
def calcFor8or9(def row) {
    return row.income - (row.cost279 - row.costReserve)
}

def calc8(def row) {
    def tmp = calcFor8or9(row)
    return (tmp < 0 ? abs(tmp) : 0)
}

def calc9(def row) {
    def tmp = calcFor8or9(row)
    return (tmp >= 0 ? tmp : 0)
}