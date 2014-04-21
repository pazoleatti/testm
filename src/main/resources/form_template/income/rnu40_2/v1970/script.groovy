package form_template.income.rnu40_2.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-40.2) Регистр налогового учёта начисленного процентного дохода по прочим дисконтным облигациям. Отчёт 2"
 * formTemplateId=339
 *
 * @author rtimerbaev
 */

// графа    - fix
// графа 1  - number        атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
// графа 2  - name          атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
// графа 3  - code          атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочника 84 «Ценные бумаги»
// графа 4  - cost
// графа 5  - bondsCount
// графа 6  - percent

@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        checkSourceAccepted()
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        checkSourceAccepted()
        calc()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        checkSourceAccepted()
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
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
def refBookCache = [:]

// Проверяемые на пустые значения атрибуты (графа 1..6)
@Field
def nonEmptyColumns = ['number', 'name', 'code', 'cost', 'bondsCount', 'percent']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 5, 6)
@Field
def totalColumns = ['bondsCount', 'percent']

// список алиасов подразделов
@Field
def sections = ['1', '2', '3', '4', '5', '6', '7', '8']

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def taxPeriod = null

@Field
def sourceFormData = null

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    if (isConsolidated) {
        sort(dataRows)
        calcTotal(dataRows)
        dataRowHelper.save(dataRows)
        return
    }

    // удалить нефиксированные строки
    deleteRows(dataRows)

    // Консолидация данных из первичной рну-40.1 в первичную рну-40.2.
    def sourceDataRows = getDataRowsFromSource()
    sections.each { section ->
        def rows40_1 = getRowsBySection(sourceDataRows, section)
        def newRows = []
        for (def row : rows40_1) {
            if (hasCalcRow(row.name, row.issuer, newRows)) {
                continue
            }
            def newRow = getCalcRowFromRNU_40_1(row.name, row.issuer, rows40_1)
            newRows.add(newRow)
        }
        if (!newRows.isEmpty()) {
            dataRows.addAll(getDataRow(dataRows, 'total' + section).getIndex() - 1, newRows)
            updateIndexes(dataRows)
        }
    }

    // отсортировать/группировать
    sort(dataRows)

    // посчитать итоги по разделам
    calcTotal(dataRows)
    dataRowHelper.save(dataRows)
}

void calcTotal(def dataRows) {
    sections.each { section ->
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        totalColumns.each { alias ->
            lastRow.getCell(alias).setValue(getSum(dataRows, alias, firstRow, lastRow), null)
        }
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // 1. Обязательность заполнения поля графы 1..6
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }

    if (isConsolidated) {
        return
    }

    def dataRows40_1 = getDataRowsFromSource()

    // алиасы графов для арифметической проверки (графа 1..6)
    def arithmeticCheckAlias = nonEmptyColumns

    // 2. Арифметическая проверка графы 1..6
    // подразделы, собрать список списков строк каждого раздела
    for (def section : sections) {
        def rows40_1 = getRowsBySection(dataRows40_1, section)
        def rows40_2 = getRowsBySection(dataRows, section)
        // если в разделе рну 40.1 есть данные, а в аналогичном разделе рну 40.2 нет данных, то ошибка или наоборот, то тоже ошибка
        if (rows40_1.isEmpty() && !rows40_2.isEmpty() ||
                !rows40_1.isEmpty() && rows40_2.isEmpty()) {
            def number = section
            logger.error("Неверно рассчитаны значения графов для раздела $number")
            continue
        }
        if (rows40_1.isEmpty() && rows40_2.isEmpty()) {
            continue
        }
        // сравнить значения строк
        for (def row : rows40_2) {
            def tmpRow = getCalcRowFromRNU_40_1(row.name, row.code, rows40_1)
            def msg = []
            arithmeticCheckAlias.each { alias ->
                def value1 = row.getCell(alias).value
                def value2 = tmpRow.getCell(alias).value
                if (value1 != value2) {
                    msg.add('«' + getColumnName(row, alias) + '»')
                }
            }
            if (!msg.isEmpty()) {
                def columns = msg.join(', ')
                def index = row.getIndex()
                logger.error("Строка $index: Неверное значение граф: $columns")
            }
        }
    }

    // 3. Арифметическая проверка строк промежуточных итогов (графа 5, 6)
    sections.each { section ->
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        def msg = []
        for (def col : totalColumns) {
            def value = roundValue(lastRow.getCell(col).value ?: 0, 6)
            def sum = roundValue(getSum(dataRows, col, firstRow, lastRow), 6)
            if (value != sum) {
                msg.add('«' + getColumnName(lastRow, col) + '»')
            }
        }
        if (!msg.isEmpty()) {
            def columns = msg.join(', ')
            logger.error("Неверно рассчитаны итоговые значения для раздела $section в графе: $columns!")
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
        def source = formDataService.findMonth(it.formTypeId, it.kind, it.departmentId, getTaxPeriod()?.id, formData.periodOrder)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            def sourceDataRows = formDataService.getDataRowHelper(source).allCached
            if (it.formTypeId == formData.formType.id) {
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(sourceDataRows, dataRows, section, 'total' + section)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows строки приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def sourceDataRows, def destinationDataRows, def fromAlias, def toAlias) {
    def from = getDataRow(sourceDataRows, fromAlias).getIndex()
    def to = getDataRow(sourceDataRows, toAlias).getIndex() - 1
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getDataRow(destinationDataRows, toAlias).getIndex() - 1, copyRows)
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
            def recordA = getRefBookValue(30, a.name)
            def recordB = getRefBookValue(30, b.name)
            def numberA = recordA?.SBRF_CODE?.value
            def numberB = recordB?.SBRF_CODE?.value
            if (numberA == numberB) {
                def nameA = recordA?.NAME?.value
                def nameB = recordB?.NAME?.value
                return nameA <=> nameB
            }
            return numberA <=> numberB
        }
    }
}

/** Получить сумму столбца. */
def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
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
 * Получить строки раздела.
 *
 * @param dataRows строки нф
 * @param section алиас начала раздела (н-р: начало раздела - A, итоги раздела - totalA)
 */
def getRowsBySection(def dataRows, def section) {
    def from = getDataRow(dataRows, section).getIndex()
    def to = getDataRow(dataRows, 'total' + section).getIndex() - 2
    return (from <= to ? dataRows[from..to] : [])
}

/**
 * Получить посчитанную строку для рну 40.2 из рну 40.1.
 * <p>
 * Формируется строка для рну 40.2.
 * Для формирования строки отбираются данные из 40.1 по номеру и названию тб и коду валюты.
 * У строк рну 40.1, подходящих под эти условия, суммируются графы 6, 7, 10 в строку рну 40.2 графы 4, 5, 6.
 * </p>
 *
 * @param name наименование тб
 * @param code код валюты номинала
 * @param rows40_1 строки рну 40.1 среди которых искать подходящие (строки должны принадлежать одному разделу)
 * @return строка рну 40.2
 */
def getCalcRowFromRNU_40_1(def name, def code, def rows40_1) {
    if (rows40_1 == null || rows40_1.isEmpty()) {
        return null
    }
    def calcRow = null
    for (def row : rows40_1) {
        if (row.name == name && row.issuer == code) {
            if (calcRow == null) {
                calcRow = formData.createDataRow()
                calcRow.number = name
                calcRow.name = name
                calcRow.code = code
                calcRow.cost = 0
                calcRow.bondsCount = 0
                calcRow.percent = 0
            }
            // графа 4, 5, 6 = графа 6, 7, 10
            calcRow.cost += (row.cost ?: 0)
            calcRow.bondsCount += (row.bondsCount ?: 0)
            calcRow.percent += (row.percent ?: 0)
        }
    }
    return calcRow
}

/**
 * Проверить посчитала ли уже для рну 40.2 строка с заданными параметрами (по номеру и названию тб и коду валюты).
 *
 * @param name наименование тб
 * @param code код валюты номинала
 * @param rows40_2 строки рну 40.2 среди которых искать строку (строки должны принадлежать одному разделу)
 * @return true - строка с такими параметрами уже есть, false - строки нет
 */
def hasCalcRow(def name, def code, def rows40_2) {
    if (rows40_2 != null && !rows40_2.isEmpty()) {
        for (def row : rows40_2) {
            if (row.name == name && row.code == code) {
                return true
            }
        }
    }
    return false
}

def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

def getTaxPeriod() {
    if (taxPeriod == null) {
        taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod
    }
    return taxPeriod
}

/** Получить данные формы РНУ-40.1 (id = 338) */
def getFormDataSource() {
    if (sourceFormData == null) {
        sourceFormData = formDataService.findMonth(338, formData.kind, formDataDepartment.id, getTaxPeriod()?.id, formData.periodOrder)
    }
    return sourceFormData
}

void checkSourceAccepted() {
    if (isConsolidated) {
        return
    }
    def form = getFormDataSource()
    if (form == null || form.state != WorkflowState.ACCEPTED) {
        throw new ServiceException('Не найдены экземпляры РНУ-40.1 за текущий отчетный период!')
    }
}

/** Получить строки из нф РНУ-40.1. */
def getDataRowsFromSource() {
    def formDataSource = getFormDataSource()
    if (formDataSource != null) {
        return formDataService.getDataRowHelper(formDataSource)?.allCached
    }
    return null
}