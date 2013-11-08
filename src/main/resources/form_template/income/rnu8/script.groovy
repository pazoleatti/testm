package form_template.income.rnu8

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 * Скрипт для РНУ-8.
 * Форма "(РНУ-8) Простой регистр налогового учёта «Требования»".
 * formTemplateId=320
 *
 * @author rtimerbaev
 * @author Stanislav Yasinskiy
 */

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
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED: // после принятия из подготовлена
        allCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicalCheck()
        break
}

// графа 1  - number
// графа 2  - code
// графа 3  - balance
// графа 4  - name
// графа 5  - income
// графа 6  - outcome

//Добавить новую строку
void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    ['balance', 'income', 'outcome'].each {
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

void calc() {
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

    // отсортировать/группировать
    dataRows.sort({ getKnu(it.balance) })

    // посчитать "итого"
    def totalIncome = 0
    def totalOutcom = 0

    // посчитать "итого по коду"
    def totalColumns = ['income', 'outcome']
    def totalRows = [:]
    def tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }

    dataRows.eachWithIndex { row, i ->
        row.number = i

        if (row.income !=null)
            totalIncome += row.income
        if (row.outcome !=null)
            totalOutcom += row.outcome

        if (tmp == null) {
            tmp = row.code
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (tmp != row.code) {
            totalRows.put(i, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == dataRows.size() - 1) {
            totalColumns.each {
                def val = row.getCell(it).getValue()
                if(val!=null)
                    sums[it] += val
            }
            totalRows.put(i + 1, getNewRow(row.code, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        totalColumns.each {
            def val = row.getCell(it).getValue()
            if(val!=null)
                sums[it] += val
        }
        tmp = row.code
    }
    dataRowHelper.save(dataRows)

    // добавить строку "итого"
    def DataRow totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.getCell("fix").setColSpan(4)
    totalRow.fix = 'Итого'
    setTotalStyle(totalRow)
    totalRow.income = totalIncome
    totalRow.outcome = totalOutcom
    dataRowHelper.insert(totalRow, dataRows.size() + 1)

    // добавить "итого по коду" в таблицу
    i = 0
    totalRows.each { index, row ->
        dataRowHelper.insert(row, index + i + 1)
        i++
    }
}

def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    if (!dataRows.isEmpty()) {
        def numberList = []

        // список проверяемых столбцов (графа 1..6)
        def requiredColumns = ['number', 'code', 'balance', 'name', 'income', 'outcome']

        // суммы строки общих итогов
        def totalSums = [:]

        // столбцы для которых надо вычислять итого и итого по коду классификации дохода. Графа 5, 6
        def totalColumns = ['income', 'outcome']

        // признак наличия итоговых строк
        def hasTotal = false

        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        for (def row : dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 1. Обязательность заполнения поля (графа 1..6)
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }

            // 2. Проверка на уникальность поля «№ пп» (графа 1)
            if (numberList.contains(row.number)) {
                logger.error("Нарушена уникальность номера по порядку ${row.number}!")
                return false
            } else {
                numberList.add(row.number)
            }

            // 3. Проверка соответствия балансового счета коду налогового учета

            // 4. Проверка итоговых значений по кодам классификации дохода - нахождение кодов классификации
            if (!totalGroupsName.contains(row.code)) {
                totalGroupsName.add(row.code)
            }

            // 5. Проверка итогового значених по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += row.getCell(alias).getValue()
            }
        }


        if (hasTotal) {
            def totalRow = data.getDataRow(getRows(data), 'total')

            // 4. Проверка итоговых значений по кодам классификации дохода
            for (def codeName : totalGroupsName) {
                def row = data.getDataRow(getRows(data), 'total' + codeName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        def colName = getColumnName(row, alias)
                        logger.error("Неверное итоговое значение по коду ${getKnu(codeName)} столбца \"$colName\"!")
                        return false
                    }
                }
            }

            // 5. Проверка итогового значений по всей форме
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    def colName = getColumnName(totalRow, alias)
                    logger.error("Неверное итоговое значение столбца \"$colName\"!")
                    return false
                }
            }
        }
    }
    checkNSI()

    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    for (def row : getRows(data)) {
        if (!isTotal(row)) {
            def rowStart = getRowIndexString(row)
            // 1. Проверка графы «Код налогового учета»
            if (row.code != null && getKnu(row.code) == null) {
                logger.warn(rowStart + 'код налогового учета в справочнике отсутствует!!')
            }

            // 2. Проверка графы «Балансовый счет. Номер»
            if (row.balance != null && getIncomeType(row.balance) == null) {
                logger.warn(rowStart + 'операция в РНУ не учитывается!')
            }

            // 3. Проверка графы «Балансовый счет. Наименование»
            if (row.name != null && getNumber(row.name) == null) {
                logger.warn(rowStart + 'балансовый счёт в справочнике отсутствует!')
            }
        }
    }
    return true
}

/**
 * Начало предупреждений/ошибок
 * @param row
 * @return
 */
def getRowIndexString(def DataRow row) {
    def index = row.number
    if (index != null) {
        return "В строке \"№ пп\" равной $index "
    } else {
        index = getIndex(row) + 1
        return "В строке $index "
    }

}

/**
 * Консолидация.
 */
void consolidation() {
    def data = data
    // удалить все строки и собрать из источников их строки
    data.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        data.insert(row, getRows(data).size() + 1)
                    }
                }
            }
        }
    }
    def ignoredRows = []
    for (def row : getRows(data)) {
        if (!ignoredRows.contains(row)) {
            for (def rowB : getRows(data)) {
                if (row != rowB && row.balance == rowB.balance && !ignoredRows.contains(rowB)) {
                    row.income += rowB.income
                    row.outcome += rowB.outcome
                    ignoredRows.add(rowB)
                }
            }
        }
    }
    data.save(getRows(data))
    ignoredRows.each {
        data.delete(it)
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

// Проверка при создании формы.
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
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def from = 0
    def to = getRows(data).size() - 1
    if (from > to) {
        return 0
    }
    return summ(formData, getRows(data), new ColumnRange(columnAlias, from, to))
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    newRow.getCell("fix").setColSpan(2)
    newRow.fix = 'Итого по КНУ ' + getKnu(alias)
    setTotalStyle(newRow)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'fix', 'code', 'balance', 'name', 'income', 'outcome'].each {
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
    getRows(data).indexOf(row)
}

/**
 * Проверить заполненность обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def errorBegin = getRowIndexString(row)
        def errorMsg = colNames.join(', ')
        logger.error(errorBegin + "не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def DataRowHelper getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

def DataRowHelper getData() {
    return getData(formData)
}

/**
 * Получить строки формы.
 *
 * @param formData форма
 */
def List<DataRow<Cell>> getRows(def DataRowHelper data) {
    return data.getAllCached()
}

/**
 * Получить атрибут 140 - "Код налогового учёта" справочник 27 - "Классификатор доходов Сбербанка России для целей налогового учёта".
 *
 * @param id идентификатор записи справочника
 */
def getKnu(def id) {
    return refBookService.getStringValue(28, id, 'CODE')
}

/**
 * Получить атрибут 142 - "Вид дохода по операциям" справочник 28 - "Классификатор доходов Сбербанка России для целей налогового учёта".
 *
 * @param id идентификатор записи справочника
 */
def getIncomeType(def id) {
    return refBookService.getStringValue(28, id, 'TYPE_INCOME')
}

/**
 * Получить атрибут 350 - "Номер" справочник 28 - "Классификатор доходов Сбербанка России для целей налогового учёта".
 *
 * @param id идентификатор записи справочника
 */
def getNumber(def id) {
    return refBookService.getStringValue(28, id, 'NUMBER')
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

