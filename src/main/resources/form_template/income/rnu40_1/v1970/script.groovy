package form_template.income.rnu40_1.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * (РНУ-40.1) Регистр налогового учёта начисленного процентного дохода по прочим дисконтным облигациям. Отчёт 1
 * formTemplateId=338
 *
 * @author auldanov
 */

// графа    - fix
// графа 1  - number                - зависит от графы 2 - атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
// графа 2  - name                  - атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
// графа 3  - issuer                - атрибут 809 - ISSUER - «Эмитент», справочника 84 «Ценные бумаги»
// графа 4  - registrationNumber    - зависит от графы 3 - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочника 84 «Ценные бумаги»
// графа 5  - buyDate
// графа 6  - cost
// графа 7  - bondsCount
// графа 8  - upCost
// графа 9  - circulationTerm
// графа 10 - percent
// графа 11 - currencyCode          - зависит от графы 3 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочника 84 «Ценные бумаги»

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        addRow()
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
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
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// все атрибуты
@Field
def allColumns = ['fix', 'number', 'name', 'issuer', 'registrationNumber', 'buyDate', 'cost',
        'bondsCount', 'upCost', 'circulationTerm', 'percent', 'currencyCode']

// Редактируемые атрибуты (графа 2, 3, 5..9)
@Field
def editableColumns = ['name', 'issuer', 'buyDate', 'cost', 'bondsCount', 'upCost', 'circulationTerm']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// TODO (Ramil Timerbaev) сделать обязательным графу 3, когда появятся данные в справочнике "ценные бумаги"
// Проверяемые на пустые значения атрибуты (графа 2, 3, 5..10)
@Field
def nonEmptyColumns = ['name', /*'issuer',*/ 'buyDate', 'cost', 'bondsCount', 'upCost', 'circulationTerm', 'percent']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 7, 10)
@Field
def totalSumColumns = ['bondsCount', 'percent']

// список алиасов подразделов
@Field
def sections = ['1', '2', '3', '4', '5', '6', '7', '8']

// Дата окончания отчетного периода
@Field
def endDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

/** Добавить новую строку. */
def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index

    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'total1').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex() + 1
    } else {
        def alias = currentDataRow.getAlias()
        if (alias.contains('total')) {
            index = getDataRow(dataRows, alias).getIndex()
        } else {
            index = getDataRow(dataRows, 'total' + alias).getIndex()
        }
    }
    dataRowHelper.insert(getNewRow(), index)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // отсортировать/группировать
    sort(dataRows)

    def lastDay = getReportPeriodEndDate()
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // графа 10
        row.percent = calc10(row, lastDay)
    }

    // посчитать итоги по разделам
    sections.each { section ->
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        totalSumColumns.each { alias ->
            lastRow.getCell(alias).setValue(getSum(dataRows, alias, firstRow, lastRow), null)
        }
    }
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def lastDay = getReportPeriodEndDate()
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Арифметическая проверка графы 10
        def needValue = [:]
        needValue['percent'] = calc10(row, lastDay)
        checkCalc(row, ['percent'], needValue, logger, true)
    }

    // 3. Арифметическая проверка строк промежуточных итогов (графа 7, 10)
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        for (def col : totalSumColumns) {
            def value = roundValue(lastRow.getCell(col).value ?: 0, 6)
            def sum = roundValue(getSum(dataRows, col, firstRow, lastRow), 6)
            if (sum != value) {
                def name = lastRow.getCell(col).column.name
                logger.error("Неверно рассчитаны итоговые значения для раздела $section в графе \"$name\"!")
            }
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить нефиксированные строки
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    dataRows.removeAll(deleteRows)
    // поправить индексы, потому что они после изменения не пересчитываются
    updateIndexes(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
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
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationDataRows)
}

/** Получить новую стролу с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    autoFillColumns.each {
        newRow.getCell(it).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

/** Отсорировать данные (по графе 1, 2). */
void sort(def dataRows) {
    // список со списками строк каждого раздела для сортировки
    def sortRows = []

    // подразделы, собрать список списков строк каждого раздела
    sections.each { section ->
        def from = getDataRow(dataRows, section).getIndex()
        def to = getDataRow(dataRows, 'total' + section).getIndex() - 2
        if (from <= to) {
            sortRows.add(dataRows[from..to])
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

/**
 * Получить значение для графы 10.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 */
def calc10(def row, def lastDay) {
    if (row.buyDate == null || row.cost == null || row.bondsCount == null || row.upCost == null ||
            row.circulationTerm == null || row.upCost == null || row.bondsCount == 0 || row.circulationTerm == 0) {
        return null
    }
    def tmp
    tmp = ((row.cost / row.bondsCount) - row.upCost) * ((lastDay - row.buyDate) / row.circulationTerm) * row.bondsCount
    tmp = roundValue(tmp, 2)

    // справочник 22 "Курс валют", атрибут 81 RATE - "Курс валют", атрибут 80 CODE_NUMBER - Цифровой код валюты
    def currencyCode = getRefBookValue(84, row.issuer)?.CODE_CUR?.stringValue
    def String cellName = getColumnName(row, 'currencyCode')
    // TODO (Ramil Timerbaev) проверить (как и в рну 39.1)
    def record = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache,
            'CODE_NUMBER', currencyCode, lastDay, row.getIndex(), cellName, logger, true)
    def rate = record?.RATE?.numberValue
    if (rate == null) {
        return null
    }

    tmp = tmp * rate
    return roundValue(tmp, 2)
}

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}