package form_template.income.rnu36_1

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1".
 *
 * TODO:
 *		- уточнить как получать дату последнего и отчетного дня для месяца
 *
 * @author rtimerbaev
 */

// графа 1  - series
// графа 2  - amount
// графа 3  - nominal
// графа 4  - shortPositionDate
// графа 5  - balance2              атрибут 350 - NUMBER - "Номер", справочник 28 "Классификатор доходов Сбербанка России для целей налогового учёта"
// графа 6  - averageWeightedPrice
// графа 7  - termBondsIssued
// графа 8  - percIncome

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
def allColumns = ['series', 'amount', 'nominal', 'shortPositionDate', 'balance2', 'averageWeightedPrice', 'termBondsIssued', 'percIncome']

// Редактируемые атрибуты (графа 2..8)
@Field
def editableColumns = ['series', 'amount', 'nominal', 'shortPositionDate', 'balance2', 'averageWeightedPrice', 'termBondsIssued']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 2..8)
@Field
def nonEmptyColumns = ['series', 'amount', 'nominal', 'shortPositionDate', 'balance2', 'averageWeightedPrice', 'termBondsIssued', 'percIncome']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 2, 8)
@Field
def totalColumns = ['amount', 'percIncome']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

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

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index

    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'totalA').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex()
    } else {
        index = getDataRow(dataRows, 'totalA').getIndex()
        switch (currentDataRow.getAlias()) {
            case 'A' :
            case 'totalA' :
                index = getDataRow(dataRows, 'totalA').getIndex()
                break
            case 'B' :
            case 'totalB' :
            case 'total' :
                index = getDataRow(dataRows, 'totalB').getIndex()
                break
        }
    }
    dataRowHelper.insert(getNewRow(), index)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // последний день отчетного месяца
    def lastDay = reportPeriodService.getEndDate(formData.reportPeriodId)?.time

    dataRows.each { row ->
        if (row.getAlias() == null) {
            // графа 8
            row.percIncome = calcPercIncome(row, lastDay)
        }
    }

    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    def rowA = getDataRow(dataRows, 'A')
    def rowB = getDataRow(dataRows, 'B')
    def totalRowA = getDataRow(dataRows, 'totalA')
    def totalRowB = getDataRow(dataRows, 'totalB')
    totalColumns.each { alias ->
        totalRowA.getCell(alias).value = getSum(dataRows, rowA, totalRowA, alias)
        totalRowB.getCell(alias).value = getSum(dataRows, rowB, totalRowB, alias)
    }
    // посчитать Итого
    getDataRow(dataRows, 'total').percIncome = (totalRowA.percIncome ?: 0) - (totalRowB.percIncome ?: 0)
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // последний день отчетного месяца
    def lastDay = reportPeriodService.getEndDate(formData.reportPeriodId)?.time

    // отчетная дата
    def reportDay = reportPeriodService.getReportDate(formData.reportPeriodId)?.time

    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // . Проверка обязательных полей (графа 2..8)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 1. Проверка даты приобретения (открытия короткой позиции) (графа 4)
        if (row.shortPositionDate > reportDay) {
            logger.error(errorMsg + 'неверно указана дата приобретения (открытия короткой позиции)!')
        }

        // 2. Арифметическая проверка графы 8
        needValue['percIncome'] = calcPercIncome(row, lastDay)
        checkCalc(row, ['percIncome'], needValue, logger, false)

        // Проверки соответствия НСИ (графа 5)
        checkNSI(28, row, 'balance2')

        // проверки деления на ноль
        if (row.termBondsIssued == 0) {
            def name = getColumnName(row, 'termBondsIssued')
            logger.error(errorMsg + "деление на ноль: \"$name\" имеет нулевое значение.")
        }
    }

    // 3. Проверка итоговых значений по разделу А
    def rowA = getDataRow(dataRows, 'A')
    def totalRowA = getDataRow(dataRows, 'totalA')
    for (def alias : totalColumns) {
        def tmpA = getSum(dataRows, rowA, totalRowA, alias)
        if (totalRowA.getCell(alias).value != tmpA) {
            logger.error("Итоговые значений для раздела A рассчитаны неверно:!")
            break
        }
    }

    // 4. Проверка итоговых значений по разделу Б
    def totalRowB = getDataRow(dataRows, 'totalB')
    def rowB = getDataRow(dataRows, 'B')
    for (def alias : totalColumns) {
        def tmpB = getSum(dataRows, rowB, totalRowB, alias)
        if (totalRowB.getCell(alias).value != tmpB) {
            logger.error("Итоговые значений для раздела Б рассчитаны неверно:!")
            break
        }
    }

    // 5. Проверка итоговых значений по всей форме
    getDataRow(dataRows, 'total').percIncome = (totalRowA.percIncome ?: 0) - (totalRowB.percIncome ?: 0)
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
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        // поправить индексы, потому что они после изменения не пересчитываются
        updateIndexes(dataRows)
    }

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRowHelper = formDataService.getDataRowHelper(source)
                def sourceDataRows = sourceDataRowHelper.allCached
                copyRows(sourceDataRows, dataRows, 'A', 'totalA')
                copyRows(sourceDataRows, dataRows, 'B', 'totalB')
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/*
 * Вспомогательные методы.
 */

/**
 * Посчитать значение графы 8.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def calcPercIncome(def row, def lastDay) {
    if (row.termBondsIssued == null || row.termBondsIssued == 0 ||
            lastDay == null || row.nominal == null || row.averageWeightedPrice == null ||
            row.shortPositionDate == null || row.amount == null) {
        return null
    }
    def tmp = ((row.nominal - row.averageWeightedPrice) *
            (lastDay - row.shortPositionDate) / row.termBondsIssued) * row.amount
    return roundValue(tmp, 2)
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
    if (from > to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getDataRow(destinationDataRows, toAlias).getIndex() - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationDataRows)
}

/** Получить новую строку с заданными стилями. */
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
 * Получить сумму для указанной графы.
 *
 * @param dataRows строки
 * @param labelRow строка начала раздела
 * @param totalRow строка итогов раздела
 * @param alias алиас графы для которой суммируются данные
 */
def getSum(def dataRows, def labelRow, def totalRow, def alias) {
    def from = labelRow.getIndex()
    def to = totalRow.getIndex() - 2
    // если нет строк в разделе то в итоги 0
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(alias, from, to))
}

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}