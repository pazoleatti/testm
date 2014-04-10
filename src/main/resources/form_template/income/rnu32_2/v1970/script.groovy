package form_template.income.rnu32_2.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-32.2) Регистр налогового учёта начисленного процентного дохода по облигациям, по которым открыта короткая позиция. Отчёт 2".
 * formTemplateId=331
 *
 * @author rtimerbaev
 */

// графа 1  - number        атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
// графа 2  - name          атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
// графа 3  - code          атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
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
        if (!isConsolidated) {
            calc()
        }
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
def allColumns = ['number', 'name', 'code', 'cost', 'bondsCount', 'percent']

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

    // удалить нефиксированные строки
    deleteRows(dataRows)

    // получение данных из первичной рну-32.1
    def dataRowsFromSource = getDataRowsFromSource()
    if (!dataRowsFromSource) {
        dataRowHelper.save(dataRows)
        return
    }

    // подразделы, собрать список списков строк каждого раздела
    sections.each { section ->
        def rows32_1 = getRowsBySection32_1(dataRowsFromSource, section)
        def rows32_2 = getRowsBySection(dataRows, section)
        def newRows = []
        for (def row : rows32_1) {
            def code = getCode(row, getReportPeriodEndDate())
            if (hasCalcRow(row.name, code, rows32_2)) {
                continue
            }
            def newRow = getCalcRowFromRNU_32_1(row.name, code, rows32_1)
            newRows.add(newRow)
            rows32_2.add(newRow)
        }
        if (!newRows.isEmpty()) {
            dataRows.addAll(getDataRow(dataRows, section).getIndex(), newRows)
            updateIndexes(dataRows)
        }
    }

    sort(dataRows)

    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowsFromSource = getDataRowsFromSource()
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // . Обязательность заполнения поля графы 1..6
        checkNonEmptyColumns(row, row.getIndex(), allColumns, logger, true)
    }

    if (isConsolidated) {
        return
    }

    // . Арифметическая проверка графы 1..6
    for (def section : sections) {
        def rows32_1 = getRowsBySection32_1(dataRowsFromSource, section)
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
            def tmpRow = getCalcRowFromRNU_32_1(row.name, row.code, rows32_1)
            "Строка %d: Неверное значение граф: %s!"
            def msg = []
            allColumns.each { alias ->
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
                // Консолидация данных из первичной рну-32.2 в консолидированную рну-32.2.
                // копирование данных по разделам
                sections.each { section ->
                    def toAlias = (section.toInteger() + 1).toString()
                    copyRows(sourceDataRows, dataRows, section, toAlias)
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
 * @param destinationDataRows хелпер приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно)
 */
void copyRows(def sourceDataRows, def destinationDataRows, def fromAlias, def toAlias) {
    def from = getDataRow(sourceDataRows, fromAlias).getIndex()
    def to = (toAlias != '9' ? getDataRow(sourceDataRows, toAlias).getIndex() - 1 : sourceDataRows.size())
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
    def toAlias = (section.toInteger() + 1).toString()
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
 * @param name наименование тб
 * @param code код валюты номинала
 * @param rows32_1 строки рну 32.1 среди которых искать подходящие (строки должны принадлежать одному разделу)
 * @return строка рну 32.2
 */
def getCalcRowFromRNU_32_1(def name, def code, def rows32_1) {
    if (rows32_1 == null || rows32_1.isEmpty()) {
        return null
    }
    def calcRow = null
    for (def row : rows32_1) {
        def code32_1 = getCode(row, getReportPeriodEndDate())
        if (row.name == name && code32_1 == code) {
            if (calcRow == null) {
                calcRow = formData.createDataRow()
                calcRow.number = name as BigDecimal
                calcRow.name = name as BigDecimal
                calcRow.code = code as BigDecimal
                calcRow.cost = BigDecimal.ZERO
                calcRow.bondsCount = BigDecimal.ZERO
                calcRow.percent = BigDecimal.ZERO
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
 * Проверить посчитана ли уже для рну 32.2 строка с заданными параметрами (по номеру и названию тб и коду валюты).
 *
 * @param name наименование тб
 * @param code код валюты номинала
 * @param rows32_2 строки рну 32.2 среди которых искать строку (строки должны принадлежать одному разделу)
 * @return true - строка с такими параметрами уже есть, false - строки нет
 */
def hasCalcRow(def name, def code, def rows32_2) {
    if (rows32_2) {
        for (def row : rows32_2) {
            if (row.name == name && row.code == code) {
                return true
            }
        }
    }
    return false
}

// Округляет число до требуемой точности.
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/**
 * Получить код валюты номинала по id записи из справочнкиа ценной бумаги (84).
 *
 * @param row строку из рну-32.1
 * @param lastDay последний день отчетного месяца
 * @return id записи справочника 15
 */
def getCode(def row, def lastDay) {
    if (row.issuer == null) {
        return null
    }
    // получить запись (поле Цифровой код валюты выпуска) из справочника ценные бумаги (84) по id записи
    def code = getRefBookValue(84, row.issuer)?.CODE_CUR?.value
    // получить id записи из справочника валют (15) по цифровому коду валюты
    def recordId = getRecordId(15, 'CODE', code?.toString(), row.getIndex(), getColumnName(row, 'issuer'), lastDay)
    return recordId
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder)?.time
    }
    return endDate
}

def getTaxPeriod() {
    if (taxPeriod == null) {
        taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod
    }
    return taxPeriod
}

/** Получить данные формы РНУ-32.1 (id = 330) */
def getFormDataSource() {
    if (sourceFormData == null) {
        sourceFormData = formDataService.findMonth(330, formData.kind, formDataDepartment.id, getTaxPeriod()?.id, formData.periodOrder)
    }
    return sourceFormData
}

void checkSourceAccepted() {
    if (isConsolidated) {
        return
    }
    def form = getFormDataSource()
    if (form == null || form.state != WorkflowState.ACCEPTED) {
        throw new ServiceException('Не найдены экземпляры РНУ-32.1 за текущий отчетный период!')
    }
}

/** Получить строки из нф РНУ-32.1. */
def getDataRowsFromSource() {
    def formDataSource = getFormDataSource()
    if (formDataSource != null) {
        return formDataService.getDataRowHelper(formDataSource)?.allCached
    }
    return null
}