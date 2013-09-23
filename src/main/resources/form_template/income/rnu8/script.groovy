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
 *
 * @version 59
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        allCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        allCheck()
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
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED : // после принятия из подготовлена
        allCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        if (allCheck()) {
            // для сохранения изменений приемников
            data.commit()
        }
        break
}

// графа 1  - number
// графа 2  - code
// графа 3  - balance
// графа 4  - name
// графа 5  - income
// графа 6  - outcome

def allCheck() {
    return !hasError() && logicalCheck() && checkNSI()
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Вставка строки в случае если форма генерирует динамически строки итого (на основе данных введённых пользователем)
 */
void addNewRow() {
    def data = data
    DataRow<Cell> newRow = getNewRow()
    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(row.getAlias()!=null && index>0){
            row = getRows(data).get(--index)
        }
        if(index!=currentDataRow.getIndex() && getRows(data).get(index).getAlias()==null){
            index++
        }
    }else if (getRows(data).size()>0) {
        for(int i = getRows(data).size()-1;i>=0;i--){
            def row = getRows(data).get(i)
            if(row.getAlias()==null){
                index = getRows(data).indexOf(row)+1
                break
            }
        }
    }
    data.insert(newRow,index+1)
}

/**
 * Добавить новую строку.
 */
def DataRow getNewRow() {
    def DataRow row = formData.createDataRow()

    // графа 3,5,6
    ['balance', 'income', 'outcome'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isTotal(currentDataRow)) {
        data.delete(currentDataRow)
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def data = data
    if (getRows(data).isEmpty()) {
        return
    }
    /*
	 * Проверка обязательных полей.
	 */

    // список проверяемых столбцов (графа 3,5,6)
    def requiredColumns = ['balance', 'income', 'outcome']
    for (def row : getRows(data)) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
            return
        }
    }

    // удалить строки "итого" и "итого по коду"
    def delRow = []
    getRows(data).each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        data.delete(row)
    }
    if (getRows(data).isEmpty()) {
        return
    }

    // отсортировать/группировать
    getRows(data).sort { getKnu(it.balance) }

    // графа 1
    getRows(data).eachWithIndex { row, index ->
        row.number = index + 1
        row.code = row.balance
        row.name = row.balance
    }
    data.save(getRows(data))

    /** Столбцы для которых надо вычислять итого и итого по коду. Графа 5, 6. */
    def totalColumns = ['income', 'outcome']

    // добавить строку "итого"
    def DataRow totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.getCell("fix").setColSpan(2)
    totalRow.fix = 'Итого'
    setTotalStyle(totalRow)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }
    data.insert(totalRow,getRows(data).size()+1)

    // посчитать "итого по коду"
    def totalRows = [:]
    def tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }
    getRows(data).eachWithIndex { row, i ->
        if (!isTotal(row)) {
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
            if (i == getRows(data).size() - 2) {
                totalColumns.each {
                    sums[it] += row.getCell(it).getValue()
                }
                totalRows.put(i + 1, getNewRow(row.code, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += row.getCell(it).getValue()
            }
            tmp = row.code
        }
    }

    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        data.insert(row, index + i + 1)
        i++
    }
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    if (!getRows(data).isEmpty()) {
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

        for (def row : getRows(data)) {
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
            }else{
                numberList.add(row.number)
            }

            // 3. Проверка итоговых значений по кодам классификации дохода - нахождение кодов классификации
            if (!totalGroupsName.contains(row.code)) {
                totalGroupsName.add(row.code)
            }

            // 4. Проверка итогового значених по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += row.getCell(alias).getValue()
            }
        }


        if (hasTotal) {
            def totalRow = data.getDataRow(getRows(data),'total')

            // 3. Проверка итоговых значений по кодам классификации дохода
            for (def codeName : totalGroupsName) {
                def row = data.getDataRow(getRows(data),'total' + codeName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        def colName = getColumnName(row,alias)
                        logger.error("Неверное итоговое значение по коду ${getKnu(codeName)} столбца \"$colName\"!")
                        return false
                    }
                }
            }

            // 4. Проверка итогового значений по всей форме
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    def colName = getColumnName(totalRow,alias)
                    logger.error("Неверное итоговое значение столбца \"$colName\"!")
                    return false
                }
            }
        }
    }
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
            if (row.code!=null && getKnu(row.code)==null) {
                logger.warn(rowStart+'код налогового учета в справочнике отсутствует!!')
            }

            // 2. Проверка графы «Балансовый счет. Номер»
            if (row.balance!=null && getIncomeType(row.balance)==null) {
                logger.warn(rowStart+'операция в РНУ не учитывается!')
            }

            // 3. Проверка графы «Балансовый счет. Наименование»
            if (row.name!=null && getNumber(row.name)==null) {
                logger.warn(rowStart+'балансовый счёт в справочнике отсутствует!')
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
def getRowIndexString(def DataRow row){
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
                getRows(getData(source)).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        data.insert(row,getRows(data).size()+1)
                    }
                }
            }
        }
    }
    def ignoredRows = []
    for(def row : getRows(data)){
        if (!ignoredRows.contains(row)) {
            for(def rowB : getRows(data)){
                if(row!=rowB && row.balance==rowB.balance && !ignoredRows.contains(rowB)){
                    row.income+=rowB.income
                    row.outcome+=rowB.outcome
                    ignoredRows.add(rowB)
                }
            }
        }
    }
    data.save(getRows(data))
    ignoredRows.each{
        data.delete(it)
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверка при создании формы.
 */
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
    def to = getRows(data).size()-1
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
    newRow.fix = 'Итого по КНУ '+getKnu(alias)
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
        logger.error(errorBegin+ "не заполнены колонки : $errorMsg.")
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

def DataRowHelper getData(){
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

