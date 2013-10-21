package form_template.income.rnu40_2

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-40.2) Регистр налогового учёта начисленного процентного дохода по прочим дисконтным облигациям. Отчёт 2".
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
        logicalCheck() && checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc() && logicalCheck() && checkNSI()
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
 * Расчеты. Алгоритмы заполнения полей формы.
 */
def calc() {
    def data = getData(formData)
    def rows = getRows(data)

    // удалить нефиксированные строки
    deleteRows(data)

    def data40_1 = getFromRNU40_1()
    if (data40_1 == null) {
        save(data)
        return true
    }

    // подразделы, собрать список списков строк каждого раздела
    getAliasesSections().each { section ->
        def rows40_1 = getRowsBySection(data40_1, section)
        def rows40_2 = getRowsBySection(data, section)
        def newRows = []
        for (def row : rows40_1) {
            if (hasCalcRow(row.number, row.name, row.currencyCode, rows40_2)) {
                continue
            }
            def newRow = getCalcRowFromRNU_40_1(row.number, row.name, row.currencyCode, rows40_1)
            newRows.add(newRow)
            rows40_2.add(newRow)
        }
        if (!newRows.isEmpty()) {
            rows.addAll(getIndexByAlias(data, 'total' + section), newRows)
            updateIndexes(data)
        }
    }

    sort(data)

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
    save(data)
    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = getData(formData)
    def rows = getRows(data)

    def data40_1 = getFromRNU40_1()
    if (data40_1 == null) {
        return true
    }

    // список проверяемых столбцов (графа 1..6)
    def requiredColumns = ['number', 'name', 'code', 'cost', 'bondsCount', 'percent']
    for (def row : rows) {
        // 1. Обязательность заполнения поля графы 1..6
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    // алиасы графов для арифметической проверки (графа 1..6)
    def arithmeticCheckAlias = requiredColumns
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def colNames = []
    def index
    def errorMsg

    // 2. Арифметическая проверка графы 1..6
    // подразделы, собрать список списков строк каждого раздела
    for (def section : getAliasesSections()) {
        def rows40_1 = getRowsBySection(data40_1, section)
        def rows40_2 = getRowsBySection(data, section)
        // если в разделе рну 40.1 есть данные, а в аналогичном разделе рну 40.2 нет данных, то ошибка
        // или наоборот, то тоже ошибка
        if (rows40_1.isEmpty() && !rows40_2.isEmpty() ||
                !rows40_1.isEmpty() && rows40_2.isEmpty()) {
            def number = section
            logger.error("Неверно рассчитаны значения графов для раздела $number")
            return false
        }
        if (rows40_1.isEmpty() && rows40_2.isEmpty()) {
            continue
        }
        for (def row : rows40_2) {
            index = getIndex(row) + 1
            errorMsg = "В строке $index "

            def tmpRow = getCalcRowFromRNU_40_1(row.number, row.name, row.code, rows40_1)
            arithmeticCheckAlias.each { alias ->
                if (row.getCell(alias).getValue() != tmpRow.getCell(alias).getValue()) {
                    def name = getColumnName(row, alias)
                    colNames.add('"' + name + '"')
                }
            }
            if (!colNames.isEmpty()) {
                def msg = colNames.join(', ')
                logger.error(errorMsg + "неверно рассчитано значение графы: $msg.")
                return false
            }
        }
    }

    // 3. Арифметическая проверка строк промежуточных итогов (графа 5, 6)
    def firstRow
    def lastRow
    def sumColumns = getSumColumns()
    getAliasesSections().each { section ->
        firstRow = getRowByAlias(data, section)
        lastRow = getRowByAlias(data, 'total' + section)
        for (def col : sumColumns) {
            def value = roundValue(lastRow.getCell(col).getValue() ?: 0, 6)
            def sum = roundValue(getSum(col, firstRow, lastRow), 6)
            if (value != sum) {
                def name = getColumnName(lastRow, col)
                def number = section
                logger.error("Неверно рассчитаны итоговые значения для раздела $number в графе \"$name\"!")
                return false
            }
        }
    }
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def data = getData(formData)
    def index
    def errorMsg
    def cache = [:]

    for (def row : getRows(data)) {
        if (isFixedRow(row)) {
            continue
        }

        index = getIndex(row) + 1
        errorMsg = "В строке $index "

        def recordDivision = getRecordById(30, row.number, cache)

        // 1. Проверка актуальности поля «Номер территориального банка»	(графа 1)
        // 2. Проверка актуальности поля «Наименование территориального банка / подразделения Центрального аппарата» (графа 2)
        if (recordDivision == null) {
            logger.warn(errorMsg + 'неверный номер территориального банка!')
            logger.warn(errorMsg + 'неверное наименование территориального банка/ подразделения Центрального аппарата!')
        }

        // 3. Проверка актуальности поля «Код валюты номинала» (графа 3)
        if (getRecordById(15, row.code, cache) == null) {
            logger.error(errorMsg + 'неверный код валюты!')
            return false
        }
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
    if (from >= to) {
        return
    }
    def copyRows = getRows(sourceData).subList(from, to)
    getRows(destinationData).addAll(getIndexByAlias(destinationData, toAlias), copyRows)
    updateIndexes(destinationData)
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

/**
 * Получить строки из нф РНУ-40.1.
 */
def getFromRNU40_1() {
    def formDataRNU = formDataService.find(338, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    return getData(formDataRNU)
}

/**
 * Удалить нефиксированные строки.
 */
void deleteRows(def data) {
    def deleteRows = []
    getRows(data).each { row ->
        if (!isFixedRow(row)) {
            deleteRows.add(row)
        }
    }
    getRows(data).removeAll(deleteRows) // data.delete(deleteRows)
    updateIndexes(data)
}

/**
 * Поправить индексы, потому что они после вставки не пересчитываются.
 */
void updateIndexes(def data) {
    getRows(data).eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

/**
 * Получить строки раздела.
 *
 * @param data хелпер
 * @param section алиас начала раздела (н-р: начало раздела - A, итоги раздела - totalA)
 */
def getRowsBySection(def data, def section) {
    from = getIndexByAlias(data, section) + 1
    to = getIndexByAlias(data, 'total' + section) - 1
    def rows = getRows(data)
    return (from <= to ? rows[from..to] : [])
}

/**
 * Получить посчитанную строку для рну 40.2 из рну 40.1.
 * <p>
 * Формируется строка для рну 40.2.
 * Для формирования строки отбираются данные из 40.1 по номеру и названию тб и коду валюты.
 * У строк рну 40.1, подходящих под эти условия, суммируются графы 6, 7, 10 в строку рну 40.2 графы 4, 5, 6.
 * </p>
 *
 * @param number номер тб
 * @param name наименование тб
 * @param code код валюты номинала
 * @param rows40_1 строки рну 40.1 среди которых искать подходящие (строки должны принадлежать одному разделу)
 * @return строка рну 40.2
 */
def getCalcRowFromRNU_40_1(def number, def name, def code, def rows40_1) {
    if (rows40_1 == null || rows40_1.isEmpty()) {
        return null
    }
    def calcRow = null
    for (def row : rows40_1) {
        if (row.number == number && row.name == name && row.currencyCode == code) {
            if (calcRow == null) {
                calcRow = formData.createDataRow()
                calcRow.number = number
                calcRow.name = name
                calcRow.code = code
                calcRow.cost = 0
                calcRow.bondsCount = 0
                calcRow.percent = 0
            }
            // графа 4, 5, 6 = графа 6, 7, 10
            calcRow.cost += row.cost
            calcRow.bondsCount += row.bondsCount
            calcRow.percent += row.percent
        }
    }
    return calcRow
}

/**
 * Проверить посчитала ли уже для рну 40.2 строка с заданными параметрами (по номеру и названию тб и коду валюты).
 *
 * @param number номер тб
 * @param name наименование тб
 * @param code код валюты номинала
 * @param rows40_2 строки рну 40.2 среди которых искать строку (строки должны принадлежать одному разделу)
 * @return true - строка с такими параметрами уже есть, false - строки нет
 */
def hasCalcRow(def number, def name, def code, def rows40_2) {
    if (rows40_2 != null && !rows40_2.isEmpty()) {
        for (def row : rows40_2) {
            if (row.number == number && row.name == name && row.code == code) {
                return true
            }
        }
    }
    return false
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