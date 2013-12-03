package form_template.income.rnu32_2

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field
import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.*

/**
 * Форма "(РНУ-32.2) Регистр налогового учёта начисленного процентного дохода по облигациям, по которым открыта короткая позиция. Отчёт 2".
 * formTemplateId=331
 * TODO:
 *      - логическая проверка 1 - проблемы с форматом TTBBBB - http://jira.aplana.com/browse/SBRFACCTAX-4780 - РНУ-32.1 Формат графы 1 "Номер территориального банка"
 *      - невозможно проверить форму пока не будет готова рну 32.1.
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
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck()
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
        calc()
        logicalCheck()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Проверяемые на пустые значения атрибуты (графа 1..6)
@Field
def nonEmptyColumns = ['number', 'name', 'code', 'cost', 'bondsCount', 'percent']

// список алиасов подразделов
@Field
def sections = ['1', '2', '3', '4', '5', '6', '7', '8']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            currentDate, rowIndex, cellName, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sort(dataRows)
    dataRowHelper.save(dataRows)
}

void logicalCheck() {
    def dataRows32_1 = getFromRNU32_1()
    if (dataRows32_1 == null) {
        return
    }
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            return
        }
        // 1. Проверка формата номера подразделения	Формат графы 1: ТТВВВВ	1	Неправильно указан номер подразделения (формат: ТТВВВВ)!
        // TODO (Ramil Timerbaev) http://jira.aplana.com/browse/SBRFACCTAX-4780	- РНУ-32.1 Формат графы 1 "Номер территориального банка"

        // 2. Обязательность заполнения поля графы 1..6
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }

    // 3. Арифметическая проверка графы 1..6
    def arithmeticCheckAlias = nonEmptyColumns
    for (def section : sections) {
        def rows32_1 = getRowsBySection32_1(dataRows32_1, section)
        def rows32_2 = getRowsBySection(dataRows, section)
        // если в разделе рну 32.1 есть данные, а в аналогичном разделе рну 32.2 нет данных, то ошибка или наоборот, то тоже ошибка
        if (rows32_1.isEmpty() && !rows32_2.isEmpty() ||
                !rows32_1.isEmpty() && rows32_2.isEmpty()) {
            def number = section
            logger.error("Неверно рассчитаны значения графов для раздела $number")
            continue
        }
        if (rows32_1.isEmpty() && rows32_2.isEmpty()) {
            continue
        }
        for (def row : rows32_2) {
            def tmpRow = getCalcRowFromRNU_32_1(row.number, row.name, row.code, rows32_1)
            if (tmpRow) {
                checkCalc(row, arithmeticCheckAlias, tmpRow, logger, true)
            }
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить нефиксированные строки
    deleteRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            def sourceDataRows = formDataService.getDataRowHelper(source).allCached
            if (it.formTypeId == formData.formType.id) {
                // Консолидация данных из первичной рну-32.2 в консолидированную рну-32.2.
                // копирование данных по разделам
                sections.each { section ->
                    def toAlias = (Integer.valueOf(section) + 1).toString()
                    copyRows(sourceDataRows, dataRows, section, toAlias)
                }
            } else {
                // Консолидация данных из первичной рну-32.1 в первичную рну-32.2.
                // подразделы, собрать список списков строк каждого раздела
                sections.each { section ->
                    def rows32_1 = getRowsBySection32_1(sourceDataRows, section)
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
                        dataRows.addAll(getDataRow(dataRows, section).getIndex(), newRows)
                        updateIndexes(dataRows)
                    }
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    if (formData.kind == FormDataKind.CONSOLIDATED) {
        logger.info('Формирование консолидированной формы прошло успешно.')
    } else {
        logger.info('Формирование первичной формы РНУ-32.2 прошло успешно.')
    }
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
    def from = getDataRow(sourceDataRows, fromAlias).getIndex()
    def to = (toAlias != '8' ? getDataRow(sourceDataRows, toAlias).getIndex() - 1 : sourceDataRows.size())
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getDataRow(destinationDataRows, fromAlias).getIndex() - 1, copyRows)
    updateIndexes(destinationDataRows)
}

/** Отсорировать данные (по графе 1, 2). */
void sort(def dataRows) {
    // список со списками строк каждого раздела для сортировки
    def sortRows = []
    // подразделы, собрать список списков строк каждого раздела
    sections.each { section ->
        def rows = getRowsBySection(dataRows, section)
        if (!rows.isEmpty()) {
            sortRows.add(rows)
        }
    }
    // отсортировать строки каждого раздела
    sortRows.each { sectionRows ->
        sectionRows.sort { def a, def b ->
            // графа 1  - number (справочник)
            // графа 2  - name (справочник)
            def recordA = getRefBookValue(30, a.number)
            def recordB = getRefBookValue(30, b.number)
            def numberA = recordA?.SBRF_CODE?.value
            def numberB = recordB?.SBRF_CODE?.value
            def nameA = recordA?.NAME?.value
            def nameB = recordB?.NAME?.value

            if (numberA == numberB) {
                return nameA <=> nameB
            }
            return numberA <=> numberB
        }
    }
}

/**
 * Получить сумму столбца.
 *
 * @param dataRows строки нф
 * @param columnAlias алиас графы который суммировать
 * @param rowStart строка начала суммиравония
 * @param rowEnd строка окончания суммирования
 */
def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

/** Получить строки из нф РНУ-32.1. */
def getFromRNU32_1() {
    def formDataRNU = formDataService.find(330, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formDataRNU != null) {
        return formDataService.getDataRowHelper(formDataRNU)?.allCached
    }
    return null
}

/** Удалить нефиксированные строки. */
void deleteRows(def dataRows) {
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    dataRows.removeAll(deleteRows)
    updateIndexes(dataRows)
}

/** Поправить индексы, потому что они после вставки не пересчитываются. */
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
    def from = getDataRow(dataRows, section).getIndex()
    def to = getDataRow(dataRows, 'total' + section).getIndex() - 2
    return (from <= to ? dataRows[from..to] : [])
}

/**
 * Получить строки раздела рну 32.2 (там где есть заголовок раздела, но НЕТ итогов раздела).
 *
 * @param dataRows строки нф
 * @param section алиас начала раздела (н-р: начало раздела - заголовок раздела A, конец раздела - следующий заголовок раздела B)
 */
def getRowsBySection(def dataRows, def section) {
    def from = getDataRow(dataRows, section).getIndex()
    def toAlias = (Integer.valueOf(section) + 1).toString()
    def to = (section != '8' ? getDataRow(dataRows, toAlias).getIndex() - 2 : dataRows.size() - 1)
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