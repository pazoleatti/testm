package form_template.income.rnu49.v2012

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import java.math.RoundingMode
import groovy.transform.Field

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
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        checkRNU()
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        checkRNU()
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        checkRNU()
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
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

// Дата начала отчетного периода
@Field
def reportPeriodStartDate = null

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Отчетный период
@Field
def currentReportPeriod = null

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getEndDate(), rowIndex, colIndex, logger, required)
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

def addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def newRow = formData.createDataRow()
    newRow.keySet().each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }

    def index
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'totalA').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex() + 1
    } else {
        def alias = currentDataRow.getAlias()
        index = getDataRow(dataRows, alias.contains('total') ? alias : 'total' + alias).getIndex()
    }
    dataRowHelper.insert(newRow, index)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (formData.kind != FormDataKind.CONSOLIDATED) {
        sort(dataRows)
        calc(dataRows)
        calcTotal(dataRows)
    } else {
        sort(dataRows)
        calcTotal(dataRows)
    }
    dataRowHelper.save(dataRows)
}

void calc(def dataRows) {
    def start = getStartDate()
    def end = getEndDate()
    def reportPeriod = getReportPeriod()
    def dataRows46 = getDataRowsByFormTemplateId(342, reportPeriod, start, end)
    def dataRows45 = getDataRowsByFormTemplateId(341, reportPeriod, start, end)

    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')
    // графа 1, 15..17, 20
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def row45 = getRow45(row, dataRows45)
        def row46 = getRow46(row, dataRows46)

        row.with {
            rowNumber = ++i
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
        def to = getDataRow(dataRows, 'total'+section).getIndex() - 2
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
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached

    def start = getStartDate()
    def end = getEndDate()
    def reportPeriod = getReportPeriod()
    def dataRows46 = getDataRowsByFormTemplateId(342, reportPeriod, start, end)
    def dataRows45 = getDataRowsByFormTemplateId(341, reportPeriod, start, end)

    // Номер последний строки предыдущей формы
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')
    def List<ReportPeriod> reportPeriodList = reportPeriodService.listByTaxPeriod(reportPeriod.taxPeriod.id)
    def numbers = []
    for (ReportPeriod period in reportPeriodList) {
        if (period.order < reportPeriod.order) {
            def findFormData = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, period.id)
            if (!findFormData) {
                continue
            }
            def findRows = formDataService.getDataRowHelper(findFormData)?.allCached
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

        if (++i != row.rowNumber) {
            rowError(logger, row, errorMsg + 'Нарушена уникальность номера по порядку!')
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    // удалить нефиксированные строки
    deleteNotFixedRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                groups.each { section ->
                    copyRows(sourceDataRows, dataRows, section, 'total' + section)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
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
        } else if (saledPropertyCode in [3,5,6,7]) {
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

def getStartDate() {
    if (reportPeriodStartDate == null) {
        reportPeriodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)?.time
    }
    return reportPeriodStartDate
}

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
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
            def cached = formDataService.getDataRowHelper(form)?.allCached
            if (cached != null) {
                dataRows += cached
            }
        }
    }
    return dataRows
}

def List<FormData> getFormDataList(def formTemplateId, def reportPeriod, def start, def end){
    def formList = []
    for (def periodOrder = start[Calendar.MONTH] + 1; periodOrder <= end[Calendar.MONTH] + 1; periodOrder++) {
        formList += formDataService.findMonth(formTemplateId, formData.kind, formDataDepartment.id, reportPeriod.taxPeriod.id, periodOrder)
    }
    return formList
}

// поправить индексы
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNumber'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 22, 3)

    def headerMapping = [
            (xml.row[0].cell[0]) : getColumnName(tmpRow, 'rowNumber'),
            (xml.row[0].cell[2]) : getColumnName(tmpRow, 'firstRecordNumber'),
            (xml.row[0].cell[3]) : getColumnName(tmpRow, 'operationDate'),
            (xml.row[0].cell[4]) : 'Основание для совершения операции (первичный документ)',
            (xml.row[0].cell[6]) : getColumnName(tmpRow, 'invNumber'),
            (xml.row[0].cell[7]) : getColumnName(tmpRow, 'name'),
            (xml.row[0].cell[8]) : getColumnName(tmpRow, 'price'),
            (xml.row[0].cell[9]) : getColumnName(tmpRow, 'amort'),
            (xml.row[0].cell[10]): getColumnName(tmpRow, 'expensesOnSale'),
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'sum'),
            (xml.row[0].cell[12]): getColumnName(tmpRow, 'sumInFact'),
            (xml.row[0].cell[13]): getColumnName(tmpRow, 'costProperty'),
            (xml.row[0].cell[14]): getColumnName(tmpRow, 'marketPrice'),
            (xml.row[0].cell[15]): getColumnName(tmpRow, 'sumIncProfit'),
            (xml.row[0].cell[16]): getColumnName(tmpRow, 'profit'),
            (xml.row[0].cell[17]): getColumnName(tmpRow, 'loss'),
            (xml.row[0].cell[18]): getColumnName(tmpRow, 'usefullLifeEnd'),
            (xml.row[0].cell[19]): getColumnName(tmpRow, 'monthsLoss'),
            (xml.row[0].cell[20]): getColumnName(tmpRow, 'expensesSum'),
            (xml.row[0].cell[21]): getColumnName(tmpRow, 'saledPropertyCode'),
            (xml.row[0].cell[22]): getColumnName(tmpRow, 'saleCode'),
            (xml.row[1].cell[4]) : 'номер',
            (xml.row[1].cell[5]) : 'дата',
            (xml.row[2].cell[0]) : '1'
    ]
    (2..22).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 3)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def section = null //название секции на английском
    def sectionRowsMap = [:]
    for (def row : xml.row) {
        xmlIndexRow++

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        def hiddenValue = row.cell[1].text()
        if (hiddenValue != null && hiddenValue != '' && hiddenValue != 'Итого') {
            if (hiddenValue[0] in groupsRus) {
                section = groups.get(groupsRus.indexOf(hiddenValue[0]))
            }
            sectionRowsMap.put(section, [])
            continue
        } else if (hiddenValue == 'Итого') {
            continue
        }

        def xmlIndexCol = 0
        def int xlsIndexRow = xmlIndexRow + rowOffset

        def newRow = formData.createDataRow()
        newRow.setImportIndex(xlsIndexRow)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        xmlIndexCol++
        // графа 1
        xmlIndexCol++
        // графа 2
        newRow.firstRecordNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 3
        newRow.operationDate = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 4
        newRow.reasonNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 5
        newRow.reasonDate = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 6
        newRow.invNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 7
        newRow.name = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 8
        newRow.price = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 9
        newRow.amort = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 10
        newRow.expensesOnSale = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 11
        newRow.sum = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 12
        newRow.sumInFact = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 13
        newRow.costProperty = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 14
        newRow.marketPrice = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 15
        newRow.sumIncProfit = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 16
        newRow.profit = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 17
        newRow.loss = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 18
        newRow.usefullLifeEnd = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 19
        newRow.monthsLoss = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 20
        newRow.expensesSum = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 21
        newRow.saledPropertyCode = getRecordIdImport(82, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 22
        newRow.saleCode = getRecordIdImport(83, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        if(section == null) {
            throw new ServiceException('Формат файла некорректен')
        } else {
            sectionRowsMap.get(section).add(newRow)
        }
    }

    // удаляем все нефиксированные строки
    deleteNotFixedRows(dataRows)

    // копирование данных по разделам
    sectionRowsMap.keySet().each { sectionKey ->
        def copyRows = sectionRowsMap.get(sectionKey)
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, sectionKey).getIndex()
            dataRows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }

    dataRowHelper.save(dataRows)
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
        def end = getEndDate()
        def month = end[Calendar.MONTH] + 1
        formDataService.checkMonthlyFormExistAndAccepted(341, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, month, false, logger, true)
        formDataService.checkMonthlyFormExistAndAccepted(342, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, month, false, logger, true)
    }
}