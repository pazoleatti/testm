package form_template.income.rnu49.v2012

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-49) Регистр налогового учёта «ведомость определения результатов от реализации (выбытия) имущества»".
 * formTypeId = 312
 *
 * @author rtimerbaev
 */

// графа 1  - rowNumber
// графа    - tmp
// графа 2  - firstRecordNumber
// графа 3  - operationDate
// графа 4  - reasonNumber
// графа 5  - reasonDate
// графа 6  - invNumber
// графа 7  - name
// графа 8  - price
// графа 9  - amort
// графа 10 - expensesOnSale
// графа 11 - sum
// графа 12 - sumInFact
// графа 13 - costProperty
// графа 14 - marketPrice
// графа 15 - sumIncProfit
// графа 16 - profit
// графа 17 - loss
// графа 18 - usefullLifeEnd
// графа 19 - monthsLoss
// графа 20 - expensesSum
// графа 21 - saledPropertyCode     - атрибут 804 - CODE - «Шифр вида реализованного (выбывшего) имущества», справочник 82 «Шифры видов реализованного (выбывшего) имущества»
// графа 22 - saleCode              - атрибут 806 - CODE - «Шифр вида реализации (выбытия)», справочник 83 «Шифры видов реализации (выбытия)»

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkRNU()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        checkRNU()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        checkRNU()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Поля, для которых подсчитываются итоговые значения (графа 9..13, 15..17, 20)
@Field
def totalColumns = ['amort', 'expensesOnSale', 'sum', 'sumInFact', 'costProperty', 'sumIncProfit',
                    'profit', 'loss', 'expensesSum']

// Редактируемые атрибуты (графа 2..14, 18, 19, 21..22)
@Field
def editableColumns = ['firstRecordNumber', 'operationDate', 'reasonNumber', 'reasonDate',
                       'invNumber', 'name', 'price', 'amort', 'expensesOnSale', 'sum',
                       'sumInFact', 'costProperty', 'marketPrice', 'saledPropertyCode', 'saleCode']

// Обязательно заполняемые атрибуты
@Field
def nonEmptyColumns = ['firstRecordNumber', 'operationDate', 'reasonNumber',
                       'reasonDate', 'invNumber', 'name', 'price', 'amort', 'expensesOnSale',
                       'sum', 'sumInFact', 'costProperty', 'marketPrice', 'sumIncProfit',
                       'profit', 'loss', 'saledPropertyCode', 'saleCode']

@Field
def autoFillColumns = ["price", "amort", "sumIncProfit", "profit", "loss",
                       "expensesSum"]

// подразделы
@Field
def groups = ['A', 'B', 'V', 'G', 'D', 'E']

@Field
def groupsRus = ['А', 'Б', 'В', 'Г', 'Д', 'Е']

// Отчетный период
@Field
def currentReportPeriod = null

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

/** Получить дату по строковому представлению (формата дд.ММ.гггг) */
def getDate(def value, def indexRow, def indexCol) {
    return parseDate(value, 'dd.MM.yyyy', indexRow, indexCol + 1, logger, true)
}

def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    autoFillColumns.each {
        newRow.getCell(it).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

def addNewRow() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def index
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'totalA').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex() + 1
    } else {
        def alias = currentDataRow.getAlias()
        index = getDataRow(dataRows, alias.contains('total') ? alias : 'total' + alias).getIndex()
    }
    dataRows.add(index + 1, getNewRow())
    formDataService.saveCachedDataRows(formData, logger)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (formData.kind != FormDataKind.CONSOLIDATED) {
        sort(dataRows)
        calc(dataRows)
        calcTotal(dataRows)
    } else {
        sort(dataRows)
        calcTotal(dataRows)
    }

    sortFormDataRows(false)
}

void calc(def dataRows) {
    def start = getReportPeriodStartDate()
    def end = getReportPeriodEndDate()
    def reportPeriod = getReportPeriod()
    def dataRows46 = getDataRowsByFormTemplateId(342, reportPeriod, start, end)
    def dataRows45 = getDataRowsByFormTemplateId(341, reportPeriod, start, end)

    // графа 1, 15..17, 20
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def row45 = getRow45(row, dataRows45)
        def row46 = getRow46(row, dataRows46)

        row.with {
            price = getGraph8(row, row46, row45)
            amort = getGraph9(row, row46, row45)
            sumIncProfit = getGraph15(row)
            profit = getGraph16(row)
            loss = getGraph17(row)
            usefullLifeEnd = getGraph18(row, row46)
            monthsLoss = getGraph19(row)
            expensesSum = getGraph20(row)
        }
    }
}

void calcTotal(def dataRows) {
    groups.each { section ->
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        totalColumns.each {
            lastRow[it] = getSum(dataRows, it, firstRow, lastRow)
        }
    }
}

void sort(def dataRows) {
    def sortRows = []
    groups.each { section ->
        def from = getDataRow(dataRows, section).getIndex()
        def to = getDataRow(dataRows, 'total' + section).getIndex() - 2
        if (from <= to) {
            sortRows.add(dataRows[from..to])
        }
    }
    sortRows.each {
        it.sort { it.operationDate }
    }
    updateIndexes(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def start = getReportPeriodStartDate()
    def end = getReportPeriodEndDate()
    def reportPeriod = getReportPeriod()
    def dataRows46 = getDataRowsByFormTemplateId(342, reportPeriod, start, end)
    def dataRows45 = getDataRowsByFormTemplateId(341, reportPeriod, start, end)
    def List<ReportPeriod> reportPeriodList = reportPeriodService.listByTaxPeriod(reportPeriod.taxPeriod.id)
    def numbers = []
    for (ReportPeriod period in reportPeriodList) {
        if (period.order < reportPeriod.order) {
            def findFormData = formDataService.getLast(formData.formType.id, formData.kind, formData.departmentId, period.id, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (!findFormData) {
                continue
            }
            def findRows = formDataService.getDataRowHelper(findFormData)?.allSaved
            for (row in findRows) {
                numbers += row.invNumber
            }
        }
    }
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения поля графы (графа 1..22)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // для консолидированной формы пропускаем остальные проверки
        if (formData.kind == FormDataKind.CONSOLIDATED) {
            return
        }

        // 2. Проверка на уникальность поля «инвентарный номер» (графа 6)
        if (row.invNumber in numbers) {
            rowError(logger, row, errorMsg + "Инвентарный номер ${row.invNumber} не уникальный!")
        } else {
            numbers += row.invNumber
        }

        // 3. Проверка на нулевые значения (графа 8, 13, 15, 17, 20)
        if (row.price == 0 && row.costProperty != 0 && row.sumIncProfit == 0 &&
                row.loss != 0 && row.expensesSum == 0) {
            rowError(logger, row, errorMsg + 'Все суммы по операции нулевые!')
        }
        // 4. Проверка формата номера первой записи	Формат графы 2: ГГ-НННН
        if (row.firstRecordNumber == null || !row.firstRecordNumber.matches('\\w{2}-\\w{6}')) {
            rowError(logger, row, errorMsg + 'Неправильно указан номер предыдущей записи!')
        }

        def row45 = getRow45(row, dataRows45)
        def row46 = getRow46(row, dataRows46)

        // 5,6. Проверка существования необходимых данных (РНУ-45, РНУ-46)
        if (getSaledPropertyCode(row.saledPropertyCode) == 2 && row45 == null) {
            rowError(logger, row, errorMsg + 'Отсутствуют данные РНУ-45!')
        }
        if (getSaledPropertyCode(row.saledPropertyCode) == 1 && row46 == null) {
            rowError(logger, row, errorMsg + 'Отсутствуют данные РНУ-46!')
        }

        def values = [:]
        values.with {
            price = getGraph8(row, row46, row45)
            amort = getGraph9(row, row46, row45)
            sumIncProfit = getGraph15(row)
            profit = getGraph16(row)
            loss = getGraph17(row)
            usefullLifeEnd = getGraph18(row, row46)
            monthsLoss = getGraph19(row)
            expensesSum = getGraph20(row)
        }
        checkCalc(row, autoFillColumns, values, logger, false)

        if (row.usefullLifeEnd != values.usefullLifeEnd) {
            rowError(logger, row, errorMsg + "Неверное значение графы ${getColumnName(row, 'usefullLifeEnd')}!")
        }
        if (row.monthsLoss != values.monthsLoss) {
            rowError(logger, row, errorMsg + "Неверное значение графы ${getColumnName(row, 'monthsLoss')}!")
        }

        // 1. Проверка шифра при реализации амортизируемого имущества
        // Графа 21 (группа «А») = 1 или 2, и графа 22 = 1
        def saledPropertyCode = getSaledPropertyCode(row.saledPropertyCode)
        def saleCode = getSaleCode(row.saleCode)
        if (isSection(dataRows, 'A', row) && ((saledPropertyCode != 1 && saledPropertyCode != 2) || saleCode != 1)) {
            rowError(logger, row, errorMsg + 'Для реализованного амортизируемого имущества (группа «А») указан неверный шифр!')
        }

        // 2. Проверка шифра при реализации прочего имущества
        // Графа 21 (группа «Б») = 3 или 4, и графа 22 = 1
        if (isSection(dataRows, 'B', row) && ((saledPropertyCode != 3 && saledPropertyCode != 4) || saleCode != 1)) {
            rowError(logger, row, errorMsg + 'Для реализованного прочего имущества (группа «Б») указан неверный шифр!')
        }

        // 3. Проверка шифра при списании (ликвидации) амортизируемого имущества
        // Графа 21 (группа «В») = 1 или 2, и графа 22 = 2
        if (isSection(dataRows, 'V', row) && ((saledPropertyCode != 1 && saledPropertyCode != 2) || saleCode != 2)) {
            rowError(logger, row, errorMsg + 'Для списанного (ликвидированного) амортизируемого имущества (группа «В») указан неверный шифр!')
        }

        // 4. Проверка шифра при реализации имущественных прав (кроме прав требования, долей паёв)
        // Графа 21 (группа «Г») = 5, и графа 22 = 1
        if (isSection(dataRows, 'G', row) && (saledPropertyCode != 5 || saleCode != 1)) {
            rowError(logger, row, errorMsg + 'Для реализованных имущественных прав (кроме прав требования, долей паёв) (группа «Г») указан неверный шифр!')
        }

        // 5. Проверка шифра при реализации прав на земельные участки
        // Графа 21 (группа «Д») = 6, и графа 22 = 1
        if (isSection(dataRows, 'D', row) && (saledPropertyCode != 6 || saleCode != 1)) {
            rowError(logger, row, errorMsg + 'Для реализованных прав на земельные участки (группа «Д») указан неверный шифр!')
        }

        // 6. Проверка шифра при реализации долей, паёв
        if (isSection(dataRows, 'E', row) && (saledPropertyCode != 7 || saleCode != 1)) {
            rowError(logger, row, errorMsg + 'Для реализованных имущественных прав (кроме прав требования, долей паёв) (группа «Е») указан неверный шифр!')
        }

        // Проверка даты истечения срока полезного использования
        if (row.usefullLifeEnd > getReportPeriodEndDate()) {
            logger.error(errorMsg + 'Дата истечения срока полезного использования не может быть больше даты окончания отчетного периода!')
        }
    }

    // 9. Проверка итоговых значений формы
    for (def section : groups) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        for (def alias : totalColumns) {
            def value = roundValue(lastRow.getCell(alias).value, 2)
            def sum = roundValue(getSum(dataRows, alias, firstRow, lastRow), 2)
            if (sum != value) {
                def name = getColumnName(lastRow, alias)
                def sectionName = groupsRus.get(groups.indexOf(section))
                logger.error("Неверно рассчитаны итоговые значения для раздела $sectionName в графе «$name»!")
            }
        }
    }
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // удалить нефиксированные строки
    deleteNotFixedRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allSaved
                groups.each { section ->
                    copyRows(sourceDataRows, dataRows, section, 'total' + section)
                }
            }
        }
    }
}

def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

def isSection(def dataRows, def section, def row) {
    def sectionRow = getDataRow(dataRows, section)
    def totalRow = getDataRow(dataRows, 'total' + section)
    return row.getIndex() > sectionRow.getIndex() && row.getIndex() < totalRow.getIndex()
}

void copyRows(def sourceRows, def destinationRows, def fromAlias, def toAlias) {
    def from = getDataRow(sourceRows, fromAlias).getIndex()
    def to = getDataRow(sourceRows, toAlias).getIndex() - 1
    if (from > to) {
        return
    }
    def copyRows = sourceRows.subList(from, to)
    destinationRows.addAll(getDataRow(destinationRows, toAlias).getIndex() - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationRows)
}

BigDecimal getGraph8(def DataRow row49, def DataRow row46, def DataRow row45) {
    // графа 8
    // Если «Графа 21» = 1, то
    // «Графа 8» =Значение «Графы 4» РНУ-46, где «Графа 2» = «Графа 6» РНУ-49
    // Если «Графа 21» = 2, то
    // «Графа 8» =Значение «Графы 7» РНУ-45, где «Графа 2» = «Графа 6» РНУ-49
    // иначе ручной ввод
    BigDecimal tmp = null
    if (row49.saledPropertyCode != null) {
        def saledPropertyCode = getSaledPropertyCode(row49.saledPropertyCode)
        if (row46 != null && saledPropertyCode == 1) {
            tmp = row46.cost
        } else if (row45 != null && saledPropertyCode == 2) {
            tmp = row45.startCost
        } else {
            tmp = row49.price
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

BigDecimal getGraph9(def DataRow row49, def DataRow row46, def DataRow row45) {
    BigDecimal tmp = null
    if (row49.saledPropertyCode != null) {
        def saledPropertyCode = getSaledPropertyCode(row49.saledPropertyCode)
        if (row46 != null && saledPropertyCode == 1) {
            tmp = (row46.cost10perExploitation ?: 0) + (row46.amortExploitation ?: 0)
        } else if (row45 != null && saledPropertyCode == 2) {
            tmp = row45.amortizationSinceUsed
        } else if (saledPropertyCode in [3, 5, 6, 7]) {
            tmp = BigDecimal.ZERO
        } else if (saledPropertyCode == 4) {
            tmp = row49.amort
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

BigDecimal getGraph15(def row) {
    BigDecimal tmp = null
    if (row.sum != null && row.marketPrice != null) {
        if (row.sum - row.marketPrice * 0.8 > 0) {
            tmp = BigDecimal.ZERO
        } else {
            tmp = row.marketPrice * 0.8 - row.sum
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

BigDecimal getGraph16(def row) {
    BigDecimal tmp = null
    if (row.sum != null && row.price != null && row.amort != null && row.expensesOnSale != null && row.sumIncProfit != null) {
        tmp = row.sum - (row.price - row.amort) - row.expensesOnSale + row.sumIncProfit
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

BigDecimal getGraph17(def row) {
    BigDecimal tmp = null
    if (row.sum != null && row.price != null && row.amort != null && row.expensesOnSale != null && row.sumIncProfit != null) {
        tmp = row.sum - (row.price - row.amort) - row.expensesOnSale + row.sumIncProfit
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

def getGraph18(def DataRow row49, def DataRow row46) {
    def tmp = null
    if (row46 != null && row49.loss > 0 && getSaledPropertyCode(row49.saledPropertyCode) == 1 && getSaleCode(row49.saleCode) == 1) {
        tmp = row46.usefullLifeEnd
    }
    return tmp
}

def getGraph19(def DataRow row49) {
    def tmp = null
    if (row49.loss > 0 && row49.usefullLifeEnd != null && row49.operationDate != null) {
        tmp = row49.usefullLifeEnd[Calendar.MONTH] - row49.operationDate[Calendar.MONTH]
    }
    return (tmp == 0) ? 1 : tmp
}

BigDecimal getGraph20(def DataRow row49) {
    BigDecimal tmp = null
    if (row49.monthsLoss != 0 && row49.monthsLoss != null) {
        if (row49.sum > 0 && row49.loss != null) {
            tmp = (row49.loss / row49.monthsLoss)
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

/**
 * Получить атрибут 804 - "Шифр вида реализованного (выбывшего) имущества" справочник 82 - "Шифры видов реализованного (выбывшего) имущества".
 * @param id идентификатор записи справочника
 */
def getSaledPropertyCode(def id) {
    return getRefBookValue(82, id)?.CODE?.numberValue?.intValue()
}

/**
 * Получить атрибут 806 - "Шифр вида реализации (выбытия)" справочник 83 - "Шифры видов реализации (выбытия)".
 * @param id идентификатор записи справочника
 */
def getSaleCode(def id) {
    return getRefBookValue(83, id)?.CODE?.numberValue?.intValue()
}

def DataRow getRow46(DataRow row49, def dataRows46) {
    if (dataRows46 != null && getSaledPropertyCode(row49.saledPropertyCode) == 1) {
        for (def row46 : dataRows46.reverse()) {
            if (row46.invNumber == row49.invNumber) {
                return row46
            }
        }
    }
    return null
}

def DataRow getRow45(DataRow row49, def dataRows45) {
    if (dataRows45 != null && getSaledPropertyCode(row49.saledPropertyCode) == 2) {
        for (def row45 : dataRows45.reverse()) {
            if (row45.inventoryNumber == row49.invNumber) {
                return row45
            }
        }
    }
    return null
}

def getReportPeriod() {
    if (currentReportPeriod == null) {
        currentReportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return currentReportPeriod
}

// Получить строки форм за предыдущие месяцы
def getDataRowsByFormTemplateId(def formTemplateId, def reportPeriod, def start, def end) {
    def formDataList = getFormDataList(formTemplateId, reportPeriod, start, end)
    def dataRows = []
    for (def form in formDataList) {
        if (form != null && form.id != null) {
            def cached = formDataService.getDataRowHelper(form)?.allSaved
            if (cached != null) {
                dataRows += cached
            }
        }
    }
    return dataRows
}

def List<FormData> getFormDataList(def formTemplateId, def reportPeriod, def start, def end) {
    def formList = []
    for (def periodOrder = start[Calendar.MONTH] + 1; periodOrder <= end[Calendar.MONTH] + 1; periodOrder++) {
        formList += formDataService.getLast(formTemplateId, formData.kind, formDataDepartment.id, reportPeriod.taxPeriod.id, periodOrder, formData.comparativePeriodId, formData.accruing)
    }
    return formList
}

// Удалить нефиксированные строки
void deleteNotFixedRows(def dataRows) {
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        updateIndexes(dataRows)
    }
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 23, 0)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def int rnuIndexRow = 2
    def int colOffset = 1

    def mapRows = [:]

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }
        def newRow = getNewRow()
        // графа 2
        def xmlIndexCol = 2
        newRow.firstRecordNumber = row.cell[xmlIndexCol].text()
        // графа 3
        xmlIndexCol = 3
        newRow.operationDate = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 4
        xmlIndexCol = 4
        newRow.reasonNumber = row.cell[xmlIndexCol].text()
        // графа 5
        xmlIndexCol = 5
        newRow.reasonDate = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 6
        xmlIndexCol = 6
        newRow.invNumber = row.cell[xmlIndexCol].text()
        // графа 7
        xmlIndexCol = 7
        newRow.name = row.cell[xmlIndexCol].text()
        // графа 8..17
        ['price', 'amort', 'expensesOnSale', 'sum', 'sumInFact', 'costProperty', 'marketPrice', 'sumIncProfit', 'profit', 'loss'].each { alias ->
            xmlIndexCol++
            newRow[alias] = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        }
        // графа 18
        xmlIndexCol = 18
        newRow.usefullLifeEnd = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 19
        xmlIndexCol = 19
        newRow.monthsLoss = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 20
        xmlIndexCol = 20
        newRow.expensesSum = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 21
        xmlIndexCol = 21
        newRow.saledPropertyCode = getRecordIdImport(82, 'CODE', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 22
        xmlIndexCol = 22
        newRow.saleCode = getRecordIdImport(83, 'CODE', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // Техническое поле(группа)
        xmlIndexCol = 23
        sectionIndex = row.cell[xmlIndexCol].text()

        if (mapRows[sectionIndex] == null) {
            mapRows[sectionIndex] = []
        }
        mapRows[sectionIndex].add(newRow)
    }

    deleteNotFixedRows(dataRows)
    dataRows.each { row ->
        if (row.getAlias()?.contains('total')) {
            totalColumns.each {
                row[it] = null
            }
        }
    }

    // копирование данных по разделам
    mapRows.keySet().each { sectionRus ->//буквы русские
        def copyRows = mapRows.get(sectionRus)
        if (copyRows != null && !copyRows.isEmpty()) {
            def sectionAlias = groups[groupsRus.indexOf(sectionRus)]//алиас заголовка подраздела
            def insertIndex = getDataRow(dataRows, sectionAlias).getIndex()
            dataRows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }

    calcTotal(dataRows)
}

def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

void checkRNU() {
    // проверить рну 45 (id = 341) и 46 (id = 342)
    if (formData.kind == FormDataKind.PRIMARY) {
        def end = getReportPeriodEndDate()
        def month = end[Calendar.MONTH] + 1
        formDataService.checkMonthlyFormExistAndAccepted(341, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, month, false, logger, true, formData.comparativePeriodId, formData.accruing)
        formDataService.checkMonthlyFormExistAndAccepted(342, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, month, false, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def section : groups) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, "total$section")
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1
        def sectionRows = (from < to ? dataRows[from..(to - 1)] : [])

        // Массовое разыменовывание граф НФ
        def columnNameList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
        refBookService.dataRowsDereference(logger, sectionRows, columnNameList)

        sortRowsSimple(sectionRows)
    }
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 23
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows

    def rowIndex = 0
    def allValuesCount = allValues.size()

    def section = null // название секции на английском
    def sectionRowsMap = [:]
    def totalRowFromFileMap = [:]

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++

        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // Пропуск итоговых строк
        def hiddenValue = rowValues[INDEX_FOR_SKIP]
        if (hiddenValue != null && hiddenValue != '' && hiddenValue != 'Итого') {
            if (hiddenValue[0] in groupsRus) {
                section = groups.get(groupsRus.indexOf(hiddenValue[0]))
            }
            sectionRowsMap.put(section, [])

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (hiddenValue == 'Итого') {
            rowIndex++
            totalRowFromFileMap[section] = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        if (section == null) {
            throw new ServiceException('Формат файла некорректен')
        } else {
            def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            sectionRowsMap.get(section).add(newRow)
        }

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // копирование данных по разделам
    updateIndexes(templateRows)
    def rows = []
    sectionRowsMap.keySet().each { sectionKey ->
        def headRow = getDataRow(templateRows, sectionKey)
        def totalRow = getDataRow(templateRows, 'total' + sectionKey)
        rows.add(headRow)
        def copyRows = sectionRowsMap[sectionKey]
        if (copyRows != null && !copyRows.isEmpty()) {
            rows.addAll(copyRows)
        }
        rows.add(totalRow)

        // сравнение итогов
        updateIndexes(rows)
        def totalRowFromFile = totalRowFromFileMap[sectionKey]
        compareSimpleTotalValues(totalRow, totalRowFromFile, copyRows, totalColumns, formData, logger, false)
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            (headerRows[0][0]) : getColumnName(tmpRow, 'rowNumber'),
            (headerRows[0][2]) : getColumnName(tmpRow, 'firstRecordNumber'),
            (headerRows[0][3]) : getColumnName(tmpRow, 'operationDate'),
            (headerRows[0][4]) : 'Основание для совершения операции (первичный документ)',
            (headerRows[0][6]) : getColumnName(tmpRow, 'invNumber'),
            (headerRows[0][7]) : getColumnName(tmpRow, 'name'),
            (headerRows[0][8]) : getColumnName(tmpRow, 'price'),
            (headerRows[0][9]) : getColumnName(tmpRow, 'amort'),
            (headerRows[0][10]): getColumnName(tmpRow, 'expensesOnSale'),
            (headerRows[0][11]): getColumnName(tmpRow, 'sum'),
            (headerRows[0][12]): getColumnName(tmpRow, 'sumInFact'),
            (headerRows[0][13]): getColumnName(tmpRow, 'costProperty'),
            (headerRows[0][14]): getColumnName(tmpRow, 'marketPrice'),
            (headerRows[0][15]): getColumnName(tmpRow, 'sumIncProfit'),
            (headerRows[0][16]): getColumnName(tmpRow, 'profit'),
            (headerRows[0][17]): getColumnName(tmpRow, 'loss'),
            (headerRows[0][18]): getColumnName(tmpRow, 'usefullLifeEnd'),
            (headerRows[0][19]): getColumnName(tmpRow, 'monthsLoss'),
            (headerRows[0][20]): getColumnName(tmpRow, 'expensesSum'),
            (headerRows[0][21]): getColumnName(tmpRow, 'saledPropertyCode'),
            (headerRows[0][22]): getColumnName(tmpRow, 'saleCode'),
            (headerRows[1][4]) : 'номер',
            (headerRows[1][5]) : 'дата',
            (headerRows[2][0]) : '1'
    ]
    (2..22).each { index ->
        headerMapping.put(headerRows[2][index], index.toString())
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 2
    def colIndex = 2
    newRow.firstRecordNumber = values[colIndex]

    // графа 3
    colIndex++
    newRow.operationDate = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 4
    colIndex++
    newRow.reasonNumber = values[colIndex]

    // графа 5
    colIndex++
    newRow.reasonDate = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 6
    colIndex++
    newRow.invNumber = values[colIndex]

    // графа 7
    colIndex++
    newRow.name = values[colIndex]

    // графа 8..17
    ['price', 'amort', 'expensesOnSale', 'sum', 'sumInFact', 'costProperty', 'marketPrice', 'sumIncProfit', 'profit', 'loss'].each { alias ->
        colIndex++
        newRow[alias] = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    }

    // графа 18
    colIndex++
    newRow.usefullLifeEnd = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 19
    colIndex++
    newRow.monthsLoss = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 20
    colIndex++
    newRow.expensesSum = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 21
    colIndex++
    newRow.saledPropertyCode = getRecordIdImport(82, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 22
    colIndex++
    newRow.saleCode = getRecordIdImport(83, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    return newRow
}