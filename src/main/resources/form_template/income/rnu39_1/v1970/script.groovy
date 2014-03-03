package form_template.income.rnu39_1.v1970

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Форма "(РНУ-39.1) Регистр налогового учёта процентного дохода по коротким позициям. Отчёт 1(месячный)
 * formTemplateId=336
 *
 * @author bkinzyabulatov
 * @author Dmitriy Levykin
 *
 * Графа 1  currencyCode        Код валюты номинала Справочник 15 Атрибут 64 CODE
 * Графа 2  issuer              Эмитент
 * Графа 3  regNumber           Номер государственной регистрации Справочник 84 Атрибут 813 REG_NUM
 * Графа 4  amount              Количество облигаций (шт.)
 * Графа 5  cost                Номинальная стоимость лота (руб.коп.)
 * Графа 6  shortPositionOpen   Дата открытия короткой позиции
 * Графа 7  shortPositionClose  Дата закрытия короткой позиции
 * Графа 8  pkdSumOpen          Сумма ПКД полученного при открытии короткой позиции
 * Графа 9  pkdSumClose         Сумма ПКД уплаченного при закрытии короткой позиции
 * Графа 10 maturityDatePrev    Дата погашения предыдущего купона
 * Графа 11 maturityDateCurrent Дата погашения текущего купона
 * Графа 12 currentCouponRate   Ставка текущего купона (% годовых)
 * Графа 13 incomeCurrentCoupon Объявленный доход по текущему купону (руб.коп.)
 * Графа 14 couponIncome        Выплачиваемый купонный доход (руб.коп.)
 * Графа 15 totalPercIncome     Всего процентный доход (руб.коп.)
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
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

//Все аттрибуты
@Field
def allColumns = ["currencyCode", "issuer", "regNumber", "amount", "cost", "shortPositionOpen", "shortPositionClose",
        "pkdSumOpen", "pkdSumClose", "maturityDatePrev", "maturityDateCurrent", "currentCouponRate",
        "incomeCurrentCoupon", "couponIncome", "totalPercIncome"]

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ["amount", "cost", "pkdSumOpen", "pkdSumClose", "couponIncome", "totalPercIncome"]

// Редактируемые атрибуты
@Field
def editableColumns = ["currencyCode", "issuer", "regNumber", "amount", "cost", "shortPositionOpen",
        "shortPositionClose", "pkdSumOpen", "pkdSumClose", "maturityDatePrev", "maturityDateCurrent",
        "currentCouponRate", "incomeCurrentCoupon"]

// Обязательно заполняемые атрибуты
@Field
def nonEmptyColumns = ["currencyCode", "issuer", /*"regNumber",*/ "amount", "cost", "shortPositionOpen",  // TODO Левыкин: Раскомментировать после появления значений в справочнике
        "shortPositionClose", "pkdSumOpen", "pkdSumClose", "maturityDatePrev", "maturityDateCurrent",
        "currentCouponRate", "incomeCurrentCoupon", "couponIncome", "totalPercIncome"]

@Field
def sortColumns = ["currencyCode", "issuer"]

@Field
def autoFillColumns = ["couponIncome", "totalPercIncome"]

@Field
def groups = ['A1', 'A2', 'A3', 'A4', 'B1', 'B2', 'B3', 'B4']

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def dFrom = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    def dTo = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time

    def reportDateNextDay = dTo + 1

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        if (row.shortPositionOpen > reportDateNextDay) {
            logger.error(errorMsg + "Неверно указана дата первой части сделки!")
        }
        if ((isInASector(dataRows, row) && row.shortPositionClose == null) || (!isInASector(dataRows, row) && row.shortPositionClose > reportDateNextDay)) {
            logger.error(errorMsg + "Неверно указана дата второй части сделки!")
        }

        def values = [:]
        values.couponIncome = round(calc14(dataRows, row, (dFrom..dTo)))
        values.totalPercIncome = round(calc15(dataRows, row))

        checkCalc(row, autoFillColumns, values, logger, true)
        // Проверки соответствия НСИ
        checkNSI(15, row, 'currencyCode')
        // checkNSI(84, row, 'regNumber') // TODO Левыкин: Раскомментировать после появления значений в справочнике
    }

    // Проверка итоговых значений формы
    for (def section : groups) {
        firstRow = getDataRow(dataRows, section)
        lastRow = getDataRow(dataRows, 'total' + section)
        def columnNames = ""
        for (def column : totalColumns) {
            if (lastRow[column] != getSum(dataRows, column, firstRow, lastRow)) {
                if (columnNames != "") {
                    columnNames += ", "
                }
                columnNames += "\"${getColumnName(lastRow, column)}\""
            }
        }
        if (columnNames != "") {
            def index = lastRow.getIndex()
            def errorMsg = "Строка $index: "
            logger.error(errorMsg + "Неверно рассчитано итоговое значение граф: $columnNames!")
        }
    }
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def sortRows = []

    groups.each { section ->
        def from = getDataRow(dataRows, section).getIndex()
        def to = getDataRow(dataRows, 'total' + section).getIndex() - 2
        if (from <= to) {
            sortRows.add(dataRows[from..to])
        }
    }

    sortRows.each {
        it.sort { DataRow a, DataRow b ->

            def aD = getRefBookValue(15, a.currencyCode)?.CODE?.stringValue
            def bD = getRefBookValue(15, b.currencyCode)?.CODE?.stringValue
            if (aD != bD) {
                return aD<=>bD
            } else {
                aD = a.issuer
                bD = b.issuer
                return aD<=>bD
            }
        }
    }

    def dFrom = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    def dTo = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time

    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
        if (row.getAlias() == null) {
            row.couponIncome = round(calc14(dataRows, row, (dFrom..dTo)))
            row.totalPercIncome = round(calc15(dataRows, row))
        }
    }

    // расчет итогов
    groups.each { section ->
        firstRow = getDataRow(dataRows, section)
        lastRow = getDataRow(dataRows, 'total' + section)
        totalColumns.each {
            lastRow[it] = getSum(dataRows, it, firstRow, lastRow)
        }
    }

    dataRowHelper.save(dataRows)
}

void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    DataRow<Cell> newRow = formData.createDataRow()
    newRow.keySet().each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def index
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        def row = getDataRow(dataRows, 'totalA1')
        index = dataRows.indexOf(row)
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex()
    } else {
        def alias = currentDataRow.getAlias()
        def totalAlias = alias.contains('total') ? alias : 'total' + ((alias in ['A', 'B']) ? (alias + '1') : alias)
        def row = getDataRow(dataRows, totalAlias)
        index = dataRows.indexOf(row)
    }

    dataRowHelper.insert(newRow, index + 1)
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить нефиксированные строки
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows += row
        }
    }
    dataRows.removeAll(deleteRows)
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }

    // Налоговый период
    def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.findMonth(it.formTypeId, it.kind, it.departmentId, taxPeriod.id, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceRows = formDataService.getDataRowHelper(source).getAll()
                // подразделы
                groups.each { section ->
                    copyRows(sourceRows, dataRows, section, 'total' + section)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

// Копировать заданный диапозон строк из источника в приемник
void copyRows(def sourceRows, def destinationRows, def fromAlias, def toAlias) {
    def from = getDataRow(sourceRows, fromAlias).getIndex()
    def to = getDataRow(sourceRows, toAlias).getIndex() - 1
    if (from > to) {
        return
    }

    def copyRows = sourceRows.subList(from, to)
    destinationRows.addAll(getDataRow(destinationRows, toAlias).getIndex() - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    destinationRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

BigDecimal calc14(def dataRows, def row, def period) {
    def boolean condition = false
    if (isInASector(dataRows, row) && row.maturityDateCurrent != null && row.maturityDateCurrent in period) {
        condition = true
    } else if (row.maturityDateCurrent != null && row.shortPositionOpen != null && row.shortPositionClose != null &&
            row.maturityDateCurrent in ((row.shortPositionOpen)..(row.shortPositionClose))) {
        condition = true
    }

    if (!condition) {
        return 0
    }

    if (getRefBookValue(15, row.currencyCode)?.CODE?.stringValue == '810') {
        if (row.amount == null || row.incomeCurrentCoupon == null) {
            return null
        }
        return row.amount * row.incomeCurrentCoupon
    } else {
        if (row.currentCouponRate == null || row.cost == null || row.maturityDateCurrent == null ||
            row.maturityDatePrev == null || row.currencyCode == null) {
            return null
        }
        def String cellName = getColumnName(row, 'currencyCode')
        def record = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache, 'CODE_NUMBER', "${row.currencyCode}",
                row.maturityDateCurrent, row.getIndex(), cellName, logger, true)
        def rate = record?.RATE?.numberValue
        if (rate == null) {
            return null
        }
        return round(row.currentCouponRate * row.cost * (row.maturityDateCurrent - row.maturityDatePrev) / 360 * rate)
    }
}

BigDecimal calc15(def dataRows, def row) {
    if (isInASector(dataRows, row)) {
        return row.couponIncome
    } else if (row.pkdSumClose != null && row.couponIncome != null && row.pkdSumOpen != null) {
        return row.pkdSumClose + row.couponIncome - row.pkdSumOpen
    } else {
        return null
    }
}

boolean isInASector(def dataRows, def row) {
    def rowB = getDataRow(dataRows, 'B')
    return row.getIndex() < rowB.getIndex()
}

// Округление
BigDecimal round(BigDecimal value, int precision = 2) {
    return value?.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

// Получить сумму столбца
def getSum(def rows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, rows, new ColumnRange(columnAlias, from, to))
}