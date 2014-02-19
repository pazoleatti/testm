package form_template.income.rnu49

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import java.math.RoundingMode
import groovy.transform.Field

/**
 * Форма "(РНУ-49) Регистр налогового учёта «ведомость определения результатов от реализации (выбытия) имущества»".
 * formTemplateId = 312
 *
 * @author rtimerbaev
 *
 * графа 1  - rowNumber
 * графа 2  - firstRecordNumber
 * графа 3  - operationDate
 * графа 4  - reasonNumber
 * графа 5  - reasonDate
 * графа 6  - invNumber
 * графа 7  - name
 * графа 8  - price
 * графа 9  - amort
 * графа 10 - expensesOnSale
 * графа 11 - sum
 * графа 12 - sumInFact
 * графа 13 - costProperty
 * графа 14 - marketPrice
 * графа 15 - sumIncProfit
 * графа 16 - profit
 * графа 17 - loss
 * графа 18 - usefullLifeEnd
 * графа 19 - monthsLoss
 * графа 20 - expensesSum
 * графа 21 - saledPropertyCode
 * графа 22 - saleCode
 */

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
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
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

// Дата начала отчетного периода
@Field
def reportPeriodStartDate = null

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Отчетный период
@Field
def currentReportPeriod = null

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
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
            loss = getGraph16(row)
            profit = getGraph17(row)
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
    reportPeriodList.each { ReportPeriod period ->
        if (period.order < reportPeriod.order) {
            def findFormData = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, period.id)
            def findRows = formDataService.getDataRowHelper(findFormData)?.allCached
            for (row in findRows){
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
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // 2. Проверка на уникальность поля «инвентарный номер» (графа 6)
        if (row.invNumber in numbers) {
            logger.error(errorMsg + "Инвентарный номер ${row.invNumber} не уникальный!")
        } else {
            numbers += row.invNumber
        }

        // 3. Проверка на нулевые значения (графа 8, 13, 15, 17, 20)
        if (row.price == 0 && row.costProperty != 0 && row.sumIncProfit == 0 &&
                row.loss != 0 && row.expensesSum == 0) {
            logger.error(errorMsg + 'Все суммы по операции нулевые!')
        }
        // 4. Проверка формата номера первой записи	Формат графы 2: ГГ-НННН
        if (row.firstRecordNumber == null || !row.firstRecordNumber.matches('\\w{2}-\\w{6}')) {
            logger.error(errorMsg + 'Неправильно указан номер предыдущей записи!')
        }

        // 6. Проверка существования необходимых экземпляров форм (РНУ-46)
        if (dataRows46 == null || dataRows46.size()==0) {
            logger.error('Отсутствуют данные РНУ-46!!')
        }

        def row46 = getRow46(row, dataRows46)
        def row45 = getRow45(row, dataRows45)

        def values = [:]
        values.with {
            price = getGraph8(row, row46, row45)
            amort = getGraph9(row, row46, row45)
            sumIncProfit = getGraph15(row)
            loss = getGraph16(row)
            profit = getGraph17(row)
            usefullLifeEnd = getGraph18(row, row46)
            monthsLoss = getGraph19(row)
            expensesSum = getGraph20(row)
        }
        checkCalc(row, autoFillColumns, values, logger, false)

        if (row.usefullLifeEnd != values.usefullLifeEnd) {
            logger.error(errorMsg + "Неверное значение графы ${getColumnName(row, 'usefullLifeEnd')}!")
        }
        if (row.monthsLoss != values.monthsLoss) {
            logger.error(errorMsg + "Неверное значение графы ${getColumnName(row, 'monthsLoss')}!")
        }

        // 1. Проверка шифра при реализации амортизируемого имущества
        // Графа 21 (группа «А») = 1 или 2, и графа 22 = 1
        def saledPropertyCode = getSaledPropertyCode(row.saledPropertyCode)
        def saleCode = getSaleCode(row.saleCode)
        if (isSection(dataRows, 'A', row) && ((saledPropertyCode != 1 && saledPropertyCode != 2) || saleCode != 1)) {
            logger.error(errorMsg + 'Для реализованного амортизируемого имущества (группа «А») указан неверный шифр!')
        }

        // 2. Проверка шифра при реализации прочего имущества
        // Графа 21 (группа «Б») = 3 или 4, и графа 22 = 1
        if (isSection(dataRows, 'B', row) && ((saledPropertyCode != 3 && saledPropertyCode != 4) || saleCode != 1)) {
            logger.error(errorMsg + 'Для реализованного прочего имущества (группа «Б») указан неверный шифр!')
        }

        // 3. Проверка шифра при списании (ликвидации) амортизируемого имущества
        // Графа 21 (группа «В») = 1 или 2, и графа 22 = 2
        if (isSection(dataRows, 'V', row) && ((saledPropertyCode != 1 && saledPropertyCode != 2) || saleCode != 2)) {
            logger.error(errorMsg + 'Для списанного (ликвидированного) амортизируемого имущества (группа «В») указан неверный шифр!')
        }

        // 4. Проверка шифра при реализации имущественных прав (кроме прав требования, долей паёв)
        // Графа 21 (группа «Г») = 5, и графа 22 = 1
        if (isSection(dataRows, 'G', row) && (saledPropertyCode != 5 || saleCode != 1)) {
            logger.error(errorMsg + 'Для реализованных имущественных прав (кроме прав требования, долей паёв) (группа «Г») указан неверный шифр!')
        }

        // 5. Проверка шифра при реализации прав на земельные участки
        // Графа 21 (группа «Д») = 6, и графа 22 = 1
        if (isSection(dataRows, 'D', row) && (saledPropertyCode != 6 || saleCode != 1)) {
            logger.error(errorMsg + 'Для реализованных прав на земельные участки (группа «Д») указан неверный шифр!')
        }

        // 6. Проверка шифра при реализации долей, паёв
        if (isSection(dataRows, 'E', row) && (saledPropertyCode != 7 || saleCode != 1)) {
            logger.error(errorMsg + 'Для реализованных имущественных прав (кроме прав требования, долей паёв) (группа «Е») указан неверный шифр!')
        }

        // Проверки соответствия НСИ
        checkNSI(82, row, 'saledPropertyCode')
        checkNSI(83, row, 'saleCode')
    }
    // 9. Проверка итоговых значений формы
    for (def section : groups) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        for (def column : totalColumns) {
            if (lastRow[column] != getSum(dataRows, column, firstRow, lastRow)) {
                def index = lastRow.getIndex()
                logger.error("Строка $index: Итоговые значения рассчитаны неверно!")
                break
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
            deleteRows += row
        }
    }
    dataRows.removeAll(deleteRows)
    updateIndexes(dataRows)

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
    logger.info('Формирование консолидированной формы прошло успешно.')
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
    def tmp = null
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
    def tmp = null
    if (row49.saledPropertyCode != null) {
        def saledPropertyCode = getSaledPropertyCode(row49.saledPropertyCode)
        if (row46 != null && saledPropertyCode == 1) {
            tmp = row46.cost10perExploitation + row46.amortExploitation
        } else if (row45 != null && saledPropertyCode == 2) {
            tmp = row45.cost10perTaxPeriod
        } else if (saledPropertyCode in [3,5,6,7]) {
            tmp = BigDecimal.ZERO
        } else if (saledPropertyCode == 4) {
            tmp = row49.amort
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

BigDecimal getGraph15(def row) {
    def tmp = null
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
    def tmp = null
    if (row.sum != null && row.price != null && row.amort != null && row.expensesOnSale != null && row.sumIncProfit != null) {
        tmp = row.sum - (row.price - row.amort) - row.expensesOnSale + row.sumIncProfit
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

BigDecimal getGraph17(def row) {
    def tmp = null
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
    def tmp = null
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
        for (def row46 : dataRows46) {
            if (row46.invNumber == row49.invNumber) {
                return row46
            }
        }
    }
    return null
}

def DataRow getRow45(DataRow row49, def dataRows45) {
    if (dataRows45 != null && getSaledPropertyCode(row49.saledPropertyCode) == 2) {
        for (def row45 : dataRows45) {
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
    for (def periodOrder = start[Calendar.MONTH] + 1; periodOrder <= end[Calendar.MONTH]; periodOrder++) {
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