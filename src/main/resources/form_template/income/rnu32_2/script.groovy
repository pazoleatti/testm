package form_template.income.rnu32_2

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-32.2) Регистр налогового учёта начисленного процентного дохода по облигациям, по которым открыта короткая позиция. Отчёт 2".
 *
 * TODO:
 *      - логическая проверка 1 - проблемы с форматом TTBBBB - http://jira.aplana.com/browse/SBRFACCTAX-4780 - РНУ-32.1 Формат графы 1 "Номер территориального банка"
 *
 * @author rtimerbaev
 */

// графа 1  - number        атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
// графа 2  - name          атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
// графа 3  - code          атрибут 64  - CODE - "Код валюты. Цифровой", справочник 15 "Общероссийский классификатор валют"
// графа 4  - cost
// графа 5  - bondsCount
// графа 6  - percent

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkUniq()
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

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить нефиксированные строки
    deleteRows(dataRows)

    def dataRows32_1 = getFromRNU32_1()
    if (dataRows32_1 == null) {
        dataRowHelper.save(dataRows)
        return true
    }

    // подразделы, собрать список списков строк каждого раздела
    getAliasesSections().each { section ->
        def rows32_1 = getRowsBySection32_1(dataRows32_1, section)
        def rows32_2 = getRowsBySection(dataRows, section)
        def newRows = []
        for (def row : rows32_1) {
            if (hasCalcRow(row.number, row.name, row.code, rows32_2)) {
                continue
            }
            def newRow = getCalcRowFromRNU_32_1(row.number, row.name, row.code, rows32_1)
            newRows.add(newRow)
            rows32_2.add(newRow)
        }
        if (!newRows.isEmpty()) {
            dataRows.addAll(getIndexByAlias(dataRows, section) + 1, newRows)
            updateIndexes(dataRows)
        }
    }

    sort(dataRows)

    dataRowHelper.save(dataRows)
    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def dataRows32_1 = getFromRNU32_1()
    if (dataRows32_1 == null) {
        return true
    }

    // список проверяемых столбцов (графа 1..6)
    def requiredColumns = ['number', 'name', 'code', 'cost', 'bondsCount', 'percent']
    for (def row : dataRows) {
        // 2. Обязательность заполнения поля графы 1..6
        if (row.getAlias() == null && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    // 1. Проверка формата номера подразделения	Формат графы 1: ТТВВВВ	1	Неправильно указан номер подразделения (формат: ТТВВВВ)!
    // TODO (Ramil Timerbaev) http://jira.aplana.com/browse/SBRFACCTAX-4780	- РНУ-32.1 Формат графы 1 "Номер территориального банка"

    // алиасы графов для арифметической проверки (графа 1..6)
    def arithmeticCheckAlias = requiredColumns
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def colNames = []
    def index
    def errorMsg

    // 3. Арифметическая проверка графы 1..6
    // подразделы, собрать список списков строк каждого раздела
    for (def section : getAliasesSections()) {
        def rows32_1 = getRowsBySection32_1(dataRows32_1, section)
        def rows32_2 = getRowsBySection(dataRows, section)
        // если в разделе рну 32.1 есть данные, а в аналогичном разделе рну 32.2 нет данных, то ошибка
        // или наоборот, то тоже ошибка
        if (rows32_1.isEmpty() && !rows32_2.isEmpty() ||
                !rows32_1.isEmpty() && rows32_2.isEmpty()) {
            def number = section
            logger.error("Неверно рассчитаны значения графов для раздела $number")
            return false
        }
        if (rows32_1.isEmpty() && rows32_2.isEmpty()) {
            continue
        }
        for (def row : rows32_2) {
            index = row.getIndex()
            errorMsg = "В строке $index "

            def tmpRow = getCalcRowFromRNU_32_1(row.number, row.name, row.code, rows32_1)
            arithmeticCheckAlias.each { alias ->
                if (row.getCell(alias).getValue() != tmpRow.getCell(alias).getValue()) {
                    def name = row.getCell(alias).column.name
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
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def index
    def errorMsg
    def cache = [:]

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        index = row.getIndex()
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить нефиксированные строки
    // deleteRows()
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    dataRowHelper.delete(deleteRows)

    // собрать из источников строки и разместить соответствующим разделам
    def aliasesSections = getAliasesSections()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourcedataRowHelper = formDataService.getDataRowHelper(source)
                def sourceDataRows = sourcedataRowHelper.getAllCached()
                // копирование данных по разделам
                aliasesSections.each { section ->
                    copyRows(sourceDataRows, dataRows, section, (Integer.valueOf(section) + 1).toString())
                }
//                (1..6).each { section ->
//                    copyRows(sourceDataRows, dataRows, section.toString(), (Integer.valueOf(section) + 1).toString())
//                }
//                copyRows(sourceDataRows, dataRows, '7', null)
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверяет уникальность в отчётном периоде и вид
 * (не был ли ранее сформирован отчет, параметры которого совпадают с параметрами, указанными пользователем )
 */
void checkUniq() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

/**
 * Получить строку по алиасу.
 *
 * @param dataRows строки нф
 * @param alias алиас
 */
def getRowByAlias(def dataRows, def alias) {
    if (alias == null || alias == '' || dataRows == null) {
        return null
    }
    for (def row : dataRows) {
        if (alias.equals(row.getAlias())) {
            return row
        }
    }
    return null
}

/**
 * Получить номер строки в таблице по псевдонимиу (1..n).
 */
def getIndexByAlias(def dataRows, String rowAlias) {
    def row = getRowByAlias(dataRows, rowAlias)
    return (row != null ? row.getIndex() : -1)
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
            def name = row.getCell(it).column.name
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.getIndex()
        def errorMsg = colNames.join(', ')
        logger.error("В $index строке не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows хелпер приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно)
 */
void copyRows(def sourceDataRows, def destinationDataRows, def fromAlias, def toAlias) {
    def from = getIndexByAlias(sourceDataRows, fromAlias)
    def to = (toAlias != '8' ? getIndexByAlias(sourceDataRows, toAlias) - 1 : sourceDataRows.size())
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getIndexByAlias(destinationDataRows, fromAlias) + 1, copyRows)
    updateIndexes(destinationDataRows)
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
 * @param dataRows строки нф
 */
void sort(def dataRows) {
    // список со списками строк каждого раздела для сортировки
    def sortRows = []
    def from
    def to

    // подразделы, собрать список списков строк каждого раздела
    getAliasesSections().each { section ->
        from = getIndexByAlias(dataRows, section)
        to = getIndexByAlias(dataRows, 'total' + section) - 2
        if (from <= to) {
            sortRows.add(dataRows[from..to])
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
    return null
}

/**
 * Получить сумму столбца.
 *
 * @param dataRows строки нф
 * @param columnAlias алиас графы который суммировать
 * @param rowStart строка начала суммиравония
 * @param rowEnd строка окончания суммирования
 * @return
 */
def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

/**
 * Получить строки из нф РНУ-32.1.
 */
def getFromRNU32_1() {
    def formDataRNU = formDataService.find(330, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    def dataRowHelper = formDataService.getDataRowHelper(formDataRNU)
    return dataRowHelper.getAllCached()
}

/**
 * Удалить нефиксированные строки.
 */
void deleteRows(def dataRows) {
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    dataRows.removeAll(deleteRows) // data.delete(deleteRows)
    updateIndexes(dataRows)
}

/**
 * Поправить индексы, потому что они после вставки не пересчитываются.
 */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

/**
 * Получить строки раздела для рну 32.1 (там где есть заголовок раздела и итоги раздела).
 *
 * @param dataRows строки нф
 * @param section алиас начала раздела (н-р: начало раздела - заголовок раздела A, конец раздела - итоги раздела totalA)
 */
def getRowsBySection32_1(def dataRows, def section) {
    from = getIndexByAlias(dataRows, section)
    to = getIndexByAlias(dataRows, 'total' + section) - 2
    return (from <= to ? dataRows[from..to] : [])
}

/**
 * Получить строки раздела рну 32.2 (там где есть заголовок раздела, но НЕТ итогов раздела).
 *
 * @param dataRows строки нф
 * @param section алиас начала раздела (н-р: начало раздела - заголовок раздела A, конец раздела - следующий заголовок раздела B)
 */
def getRowsBySection(def dataRows, def section) {
    from = getIndexByAlias(dataRows, section)
    to = (section != '8' ? getIndexByAlias(dataRows, (Integer.valueOf(section) + 1).toString()) - 2 : dataRows.size() - 1)
    return (from <= to ? dataRows[from..to] : [])
}


/**
 * Получить посчитанную строку для рну 32.2 из рну 32.1.
 * <p>
 * Формируется строка для рну 32.2.
 * Для формирования строки отбираются данные из 32.1 по номеру и названию тб и коду валюты.
 * У строк рну 32.1, подходящих под эти условия, суммируются графы 7, 8, 18 в строку рну 32.2 графы 4, 5, 6.
 * </p>
 *
 * @param number номер тб
 * @param name наименование тб
 * @param code код валюты номинала
 * @param rows32_1 строки рну 32.1 среди которых искать подходящие (строки должны принадлежать одному разделу)
 * @return строка рну 32.2
 */
def getCalcRowFromRNU_32_1(def number, def name, def code, def rows32_1) {
    if (rows32_1 == null || rows32_1.isEmpty()) {
        return null
    }
    def calcRow = null
    for (def row : rows32_1) {
        if (row.number == number && row.name == name && row.code == code) {
            if (calcRow == null) {
                calcRow = formData.createDataRow()
                calcRow.number = number
                calcRow.name = name
                calcRow.code = code
                calcRow.cost = 0
                calcRow.bondsCount = 0
                calcRow.percent = 0
            }
            // графа 4, 5, 6 = графа 7, 8, 18
            calcRow.cost += (row.faceValue ?: 0)
            calcRow.bondsCount += (row.countsBonds ?: 0)
            calcRow.percent += (row.totalPercIncome ?: 0)
        }
    }
    return calcRow
}

/**
 * Проверить посчитала ли уже для рну 32.2 строка с заданными параметрами (по номеру и названию тб и коду валюты).
 *
 * @param number номер тб
 * @param name наименование тб
 * @param code код валюты номинала
 * @param rows32_2 строки рну 32.2 среди которых искать строку (строки должны принадлежать одному разделу)
 * @return true - строка с такими параметрами уже есть, false - строки нет
 */
def hasCalcRow(def number, def name, def code, def rows32_2) {
    if (rows32_2 != null && !rows32_2.isEmpty()) {
        for (def row : rows32_2) {
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