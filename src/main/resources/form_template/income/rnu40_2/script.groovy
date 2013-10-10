package form_template.income.rnu40_2

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-40.2) Регистр налогового учёта начисленного процентного дохода по прочим дисконтным облигациям. Отчёт 2".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck() && checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc() && logicalCheck() && checkNSI()
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
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        calc() && logicalCheck() && checkNSI()
        break
}

// графа 1  - number        атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
// графа 2  - name          атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
// графа 3  - code          атрибут 64  - CODE - "Код валюты. Цифровой", справочник 15 "Общероссийский классификатор валют"
// графа 4  - cost
// графа 5  - bondsCount
// графа 6  - percent

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    def newRow = getNewRow()
    def index

    if (currentDataRow == null || getIndex(currentDataRow) == -1) {
        index = getIndexByAlias(data, 'total1')
    } else if (currentDataRow.getAlias() == null) {
        index = getIndex(currentDataRow) + 1
    } else {
        def alias = currentDataRow.getAlias()
        if (alias.contains('total')) {
            index = getIndexByAlias(data, alias)
        } else {
            index = getIndexByAlias(data, 'total' + alias)
        }
    }
    data.insert(newRow, index + 1)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (currentDataRow.getAlias() == null) {
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

    // список проверяемых столбцов (графа 2..5)
    def requiredColumns = [/*'number',*/ 'name', 'code', 'cost', 'bondsCount']
    for (def row : getRows(data)) {
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    /*
     * Расчеты.
     */

    sort(data)

    def cache = [:]
    for (def row :getRows(data)) {
        if (isFixedRow(row)) {
            continue
        }
        // TODO (Ramil Timerbaev)
        // графа 1      атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
        row.number = 0

        // графа 2      атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
        row.name = 0

        // графа 3      атрибут 64  - CODE - "Код валюты. Цифровой", справочник 15 "Общероссийский классификатор валют"
        row.code = 0

        // графа 4
        row.cost = 0

        // графа 5
        row.bondsCount = 0

        // графа 6
        row.percent = 0
    }

    // посчитать итоги по разделам
    def firstRow
    def lastRow
    def sumColumns = getSumColumns()
    getAliasesSections().each { section ->
        firstRow = getRowByAlias(data, section)
        lastRow = getRowByAlias(data, 'total' + section)
        sumColumns.each {
            lastRow.getCell(it).setValue(getSum(it, firstRow, lastRow))
        }
    }
    save(data) // TODO (Ramil Timerbaev) уточнить про update
    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = getData(formData)
    // 1. Обязательность заполнения поля графы 1-6
    // список проверяемых столбцов (графа 1..6)
    def requiredColumns = ['number', 'name', 'code', 'cost', 'bondsCount', 'percent']

    for (def row : formData.dataRows) {
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def data = getData(formData)
    // 1. Проверка актуальности поля «Номер территориального банка»	(графа 1)
    if (false) {
        logger.warn('Неверный номер территориального банка!')
    }

    // 2. Проверка актуальности поля «Наименование территориального банка / подразделения Центрального аппарата» (графа 2)
    if (false) {
        logger.warn('Неверное наименование территориального банка/ подразделения Центрального аппарата!')
    }

    // 3. Проверка актуальности поля «Код валюты номинала» (графа 3)
    if (false) {
        logger.error('Неверный код валюты!')
        return false
    }
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)

    // удалить нефиксированные строки
    def deleteRows = []
    getRows(data).each { row ->
        if (!isFixedRow(row)) {
            deleteRows.add(row)
        }
    }
    data.delete(deleteRows)

    // собрать из источников строки и разместить соответствующим разделам
    def aliasesSections = getAliasesSections()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceData = getData(source)
                // копирование данных по разделам
                aliasesSections.each { section ->
                    copyRows(sourceData, data, section, 'total' + section)
                }
            }
        }
    }
    save(data)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
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
 * Проверка является ли строка фиксированной.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
}

/**
 * Получить номер строки в таблице (0..n).
 */
def getIndex(def row) {
    return row.getIndex() - 1
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
 * Получить номер строки в таблице по псевдонимиу (0..n).
 */
def getIndexByAlias(def data, String rowAlias) {
    def row = getRowByAlias(data, rowAlias)
    return (row != null ? getIndex(row) : -1)
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
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void update(def data) {
    data.update(getRows(data))
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
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        logger.error("В $index строке не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceData хелпер источника
 * @param destinationData хелпер приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def sourceData, def destinationData, def fromAlias, def toAlias) {
    def from = getIndexByAlias(sourceData, fromAlias) + 1
    def to = getIndexByAlias(sourceData, toAlias)
    if (from > to) {
        return
    }
    def copyRows = getRows(sourceData).subList(from, to)
    getRows(destinationData).addAll(getIndexByAlias(destinationData, toAlias), copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    getRows(destinationData).eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()

    // графа 2..6
    [/*'number',*/ 'name', 'code', 'cost', 'bondsCount'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }

    // графа 6
    newRow.getCell('percent').styleAlias = 'Вычисляемая'
    return newRow
}

/**
 * Получить список алиасов подразделов
 */
def getAliasesSections() {
    return ['1', '2', '3', '4', '5', '6', '7', '8']
}

/**
 * Отсорировать данные (по графе 1, 2).
 *
 * @param data данные нф (хелпер)
 */
void sort(def data) {
    def rows = getRows(data)
    // список со списками строк каждого раздела для сортировки
    def sortRows = []
    def from
    def to

    // подразделы, собрать список списков строк каждого раздела
    getAliasesSections().each { section ->
        from = getIndexByAlias(data, section) + 1
        to = getIndexByAlias(data, 'total' + section) - 1
        if (from <= to) {
            sortRows.add(rows[from..to])
        }
    }

    def cache = [:]
    // отсортировать строки каждого раздела
    sortRows.each { sectionRows ->
        sectionRows.sort { def a, def b ->
            // графа 1  - number (справочник)
            // графа 2  - name (справочник)

            def recordA = getRecordById(30, a.number, cache)
            def recordB = getRecordById(30, b.number, cache)

            def numberA = (recordA != null ? recordA.SBRF_CODE.value : null)
            def numberB = (recordB != null ? recordB.SBRF_CODE.value : null)

            def nameA = (recordA != null ? recordA.NAME.value : null)
            def nameB = (recordB != null ? recordB.NAME.value : null)

            if (numberA == numberB) {
                return nameA <=> nameB
            }
            return numberA <=> numberB
        }
    }
}

/**
 * Получить запись из справочника по идентифкатору записи.
 *
 * @param refBookId идентификатор справончика
 * @param recordId идентификатор записи
 * @param cache кеш
 * @return
 */
def getRecordById(def refBookId, def recordId, def cache) {
    if (cache[refBookId] != null) {
        if (cache[refBookId][recordId] != null) {
            return cache[refBookId][recordId]
        }
    } else {
        cache[refBookId] = [:]
    }
    def record = refBookService.getRecordData(refBookId, recordId)
    if (record != null) {
        cache[refBookId][recordId] = record
        return cache[refBookId][recordId]
    }
    logger.error("Не удалось найти запись (id = $recordId) в справочнике (id = $refBookId)!")
    return null
}

/**
 * Получить список графов для которых вычисляются итоги (графа 5, 6).
 */
def getSumColumns() {
    return ['bondsCount', 'percent']
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias, def rowStart, def rowEnd) {
    def from = getIndex(rowStart) + 1
    def to = getIndex(rowEnd) - 1
    if (from > to) {
        return 0
    }
    return summ(formData, getRows(getData(formData)), new ColumnRange(columnAlias, from, to))
}