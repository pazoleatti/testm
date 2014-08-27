package form_template.income.f7_8.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

import java.math.RoundingMode
import java.text.SimpleDateFormat

/**
 * (Ф 7.8) Реестр совершенных операций с ценными бумагами по продаже и погашению, а также по открытию-закрытию
 * короткой позиции
 * formTemplateId=362
 *
 * @author vsergeev
 */

// графа    - fix
// графа 1  - balanceNumber                 - абсолютное значение - атрибут 152 - BALANCE_ACCOUNT - «Номер балансового счёта», справочник 29 «Классификатор соответствия счетов бухгалтерского учёта кодам налогового учёта»
// графа 2  - operationType                 - атрибут 824 - OPERATION_TYPE - «Виды операций», справочник 87 «Виды операций»
// графа 3  - signContractor                - атрибут 825 - CODE - «Код признака контрагента», справочник 88 «Признак контрагента»
// графа 4  - contractorName
// графа 5  - securityName                  - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
// графа 6  - series                        - зависит от графы 5 - атрибут 812 - SHORTNAME - «Краткое название ценной бумаги / Выпуск», справочник 84 «Ценные бумаги»
// графа 7  - securityKind                  - зависит от графы 5 - атрибут 815 - TYPE - «Тип (вид) ценной бумаги», справочник 84 «Ценные бумаги»
// графа 8  - signSecurity                  - зависит от графы 5 - атрибут 869 - SIGN - «Признак ценной бумаги», справочник 84 «Ценные бумаги»
// графа 9  - currencyCode                  - зависит от графы 5 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
// графа 10 - currencyName                  - зависит от графы 5 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
// графа 11 - nominal
// графа 12 - amount
// графа 13 - acquisitionDate
// графа 14 - tradeDate
// графа 15 - currencyCodeTrade             - атрибут 65 CODE_2 - «Код валюты. Буквенный», справочник 15 «Единый справочник валют»
// графа 16 - currencyNameTrade             - зависит от графы 15 - атрибут 66 NAME - «Наименование валюты», справочник 15 «Единый справочник валют»
// графа 17 - costWithoutNKD
// графа 18 - loss
// графа 19 - marketPriceInPerc
// графа 20 - marketPriceInRub
// графа 21 - costAcquisition
// графа 22 - realizationDate
// графа 23 - tradeDate2
// графа 24 - repaymentWithoutNKD
// графа 25 - realizationPriceInPerc
// графа 26 - realizationPriceInRub
// графа 27 - marketPriceRealizationInPerc
// графа 28 - marketPriceRealizationInRub
// графа 29 - costRealization
// графа 30 - lossRealization
// графа 31 - totalLoss
// графа 32 - averageWeightedPrice
// графа 33 - termIssue
// графа 34 - termHold
// графа 35 - interestIncomeCurrency
// графа 36 - interestIncomeInRub
// графа 37 - realizationResult
// графа 38 - excessSellingPrice

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
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
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED: // после принятия из подготовлена
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def groups = ['R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9']

// Все аттрибуты
@Field
def allColumns = ['balanceNumber', 'operationType', 'signContractor', 'contractorName', 'securityName', 'series',
        'securityKind', 'signSecurity', 'currencyCode', 'currencyName', 'nominal', 'amount', 'acquisitionDate',
        'tradeDate', 'currencyCodeTrade', 'currencyNameTrade', 'costWithoutNKD', 'loss', 'marketPriceInPerc',
        'marketPriceInRub', 'costAcquisition', 'realizationDate', 'tradeDate2', 'repaymentWithoutNKD',
        'realizationPriceInPerc', 'realizationPriceInRub', 'marketPriceRealizationInPerc',
        'marketPriceRealizationInRub', 'costRealization', 'lossRealization', 'totalLoss', 'averageWeightedPrice',
        'termIssue', 'termHold', 'interestIncomeCurrency', 'interestIncomeInRub', 'realizationResult',
        'excessSellingPrice']

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ['amount', 'costWithoutNKD', 'loss', 'marketPriceInRub', 'costAcquisition', 'repaymentWithoutNKD',
        'realizationPriceInRub', 'marketPriceRealizationInRub', 'costRealization', 'lossRealization', 'totalLoss',
        'interestIncomeCurrency', 'interestIncomeInRub', 'realizationResult', 'excessSellingPrice']

// Редактируемые атрибуты
@Field
def editableColumns = ['balanceNumber', 'operationType', 'signContractor', 'contractorName', 'securityName',
        'nominal', 'amount', 'acquisitionDate',
        'tradeDate', 'currencyCodeTrade', 'costWithoutNKD', 'loss', 'marketPriceInPerc',
        'marketPriceInRub', 'realizationDate', 'tradeDate2', 'repaymentWithoutNKD', 'realizationPriceInPerc',
        'realizationPriceInRub', 'marketPriceRealizationInPerc', 'marketPriceRealizationInRub', 'costRealization', 'lossRealization']

// Обязательно заполняемые атрибуты
@Field
def nonEmptyColumns = ['balanceNumber', 'operationType', 'signContractor', 'contractorName', 'securityName',
        'nominal', 'amount', 'acquisitionDate',
        'tradeDate', 'currencyCodeTrade', 'costWithoutNKD', 'loss', 'realizationDate',
        'tradeDate2', 'marketPriceRealizationInPerc', 'marketPriceRealizationInRub', 'costRealization', 'realizationResult',
        'excessSellingPrice']

@Field
def arithmeticCheckAlias = ['marketPriceInPerc', 'marketPriceInRub', 'costAcquisition', 'marketPriceRealizationInPerc',
        'marketPriceRealizationInRub', 'totalLoss', 'averageWeightedPrice', 'termIssue', 'termHold',
        'interestIncomeCurrency', 'interestIncomeInRub', 'realizationResult', 'excessSellingPrice']

@Field
def fixedDate = new SimpleDateFormat('dd.MM.yyyy').parse('01.01.2010')

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
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

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

//// Кастомные методы

@Field
def formDataPrev = null // Форма предыдущего месяца

@Field
def dataRowHelperPrev = null // DataRowHelper формы предыдущего месяца

// Получение формы предыдущего месяца
FormData getFormDataPrev() {
    if (formDataPrev == null) {
        formDataPrev = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    }
    return formDataPrev
}

// Получение DataRowHelper формы предыдущего месяца
def getDataRowHelperPrev() {
    if (dataRowHelperPrev == null) {
        def formDataPrev = getFormDataPrev()
        if (formDataPrev != null) {
            dataRowHelperPrev = formDataService.getDataRowHelper(formDataPrev)
        }
    }
    return dataRowHelperPrev
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def sortRows = []

    groups.each { section ->
        def from = getDataRow(dataRows, section).getIndex()
        def to = getDataRow(dataRows, "$section-total").getIndex() - 2
        if (from <= to) {
            sortRows.add(dataRows[from..to])
        }
    }

    sortRows.each {
        it.sort { DataRow a, DataRow b ->
            if (a != null && a.getAlias() != null || b != null && b.getAlias() != null) {
                return 0
            }
            def aList = getCompareList(a)
            def bList = getCompareList(b)
            for (def aD : aList) {
                def bD = bList.get(aList.indexOf(aD))
                if (aD != bD) {
                    return aD <=> bD
                }
            }
            return 0
        }
    }
    updateIndexes(dataRows)

    for (def row : dataRows) {
        if (row != null && row.getAlias() != null) {
            continue
        }
        row.with {
            marketPriceInPerc = getGraph19(row, row)
            marketPriceInRub = getGraph20(row, row)
            costAcquisition = getGraph21(row)
            marketPriceRealizationInPerc = getGraph27(row, row)
            marketPriceRealizationInRub = getGraph28(row, row)
            totalLoss = getGraph31(row)
            averageWeightedPrice = getGraph32(row, row)
            termIssue = getGraph33(row, row)
            termHold = getGraph34(row)
            interestIncomeCurrency = getGraph35(row)
            interestIncomeInRub = getGraph36(row)
            realizationResult = getGraph37(row)
            excessSellingPrice = getGraph38(row)
        }
    }

    calcOrCheckTotalDataRows(dataRows, false)
    calcOrCheckTotalForMonth(dataRows, false)
    calcOrCheckTotalForTaxPeriod(dataRows, false)

    dataRowHelper.save(dataRows)
}

void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    DataRow<Cell> newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        def row = getDataRow(dataRows, 'R1-total')
        dataRowHelper.insert(newRow, dataRows.indexOf(row) + 1)
    } else if (currentDataRow.getAlias() == null) {
        dataRowHelper.insert(newRow, currentDataRow.getIndex() + 1)
    } else {
        def alias = currentDataRow.getAlias()
        if (alias in ['R10', 'R11']) {
            alias = 'R9'
        }
        def totalAlias = alias.contains('total') ? alias : "$alias-total"
        def row = getDataRow(dataRows, totalAlias)
        dataRowHelper.insert(newRow, dataRows.indexOf(row) + 1)
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить нефиксированные строки
    deleteNotFixedRows(dataRows)

    // Налоговый период
    def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.findMonth(it.formTypeId, it.kind, it.departmentId, taxPeriod.id, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceRows = formDataService.getDataRowHelper(source).getAll()
                // подразделы
                groups.each { section ->
                    copyRows(sourceRows, dataRows, section, "$section-total")
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
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
    updateIndexes(destinationRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def dataProvider29 = refBookFactory.getDataProvider(29)

    // 5.
    if (formData.periodOrder != 1 && getFormDataPrev() == null) {
        logger.warn('Отсутствует предыдущий экземпляр отчета!')
    }

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, false)

        // 2.
        if (row.marketPriceRealizationInPerc != getGraph27(row, row)) {
            rowError(logger, row, errorMsg + "Неверно указана рыночная цена в процентах при погашении!")
        }

        // 3.
        if (row.marketPriceRealizationInRub != getGraph28(row, row)) {
            rowError(logger, row, errorMsg + "Неверно указана рыночная цена в рублях при погашении!")
        }

        // 4.
        if (row.excessSellingPrice < 0) {
            rowError(logger, row, errorMsg + "Превышение цены реализации для целей налогообложения над ценой реализации отрицательное!")
        }

        // 6.
        def values = [:]
        allColumns.each {
            values[it] = row.getCell(it).getValue()
        }
        values.with {
            marketPriceInPerc = getGraph19(values, row)
            marketPriceInRub = getGraph20(values, row)
            costAcquisition = getGraph21(values)
            marketPriceRealizationInPerc = getGraph27(values, row)
            marketPriceRealizationInRub = getGraph28(values, row)
            totalLoss = getGraph31(values)
            averageWeightedPrice = getGraph32(values, row)
            termIssue = getGraph33(values, row)
            termHold = getGraph34(values)
            interestIncomeCurrency = getGraph35(values)
            interestIncomeInRub = getGraph36(values)
            realizationResult = getGraph37(values)
            excessSellingPrice = getGraph38(values)
        }
        checkCalc(row, arithmeticCheckAlias, values, logger, true)

        def record = dataProvider29.getRecords(getReportPeriodEndDate(), null, "BALANCE_ACCOUNT = '$row.balanceNumber'", null)
        if (record.size() == 0) {
            rowError(logger, row, errorMsg + "Значение графы «Номер балансового счета» отсутствует в справочнике " +
                    "«Классификатор соответствия счетов бухгалтерского учёта кодам налогового учёта»")
        }
    }
    // 7.
    calcOrCheckTotalDataRows(dataRows, true)
    calcOrCheckTotalForMonth(dataRows, true)
    calcOrCheckTotalForTaxPeriod(dataRows, true)
}

// рассчитываем вычисляемые поля для строк ИТОГО или проверяем расчет
void calcOrCheckTotalDataRows(def dataRows, def check) {
    groups.each { group ->
        def firstRow = getDataRow(dataRows, group)
        def lastRow = getDataRow(dataRows, "$group-total")
        def firstIndex = firstRow.getIndex()
        def lastIndex = lastRow.getIndex() - 1
        def groupRows = (firstIndex <= lastIndex) ? dataRows.subList(firstIndex, lastIndex) : []
        writeResultsToRowOrCheck(groupRows, lastRow, check)
    }
}

// расчитываем значения для строки "Всего за текущий месяц" или проверяем расчеты
void calcOrCheckTotalForMonth(def dataRows, def check) {
    def totalRows = []

    groups.each { group ->
        def totalRow = getDataRow(dataRows, "$group-total")
        if (totalRow != null) {
            totalRows.add(totalRow)
        }
    }
    def totalForMonthRow = getDataRow(dataRows, 'R10')
    writeResultsToRowOrCheck(totalRows, totalForMonthRow, check)
}

// рассчитываем значения для строки "Всего за текущий налоговый период" или проверяем значения
void calcOrCheckTotalForTaxPeriod(def dataRows, def check) {
    def prevDataRowHelper = getDataRowHelperPrev()
    def prevDataRows = prevDataRowHelper?.allCached
    def rowPrev
    if (prevDataRows != null) {
        rowPrev = getDataRow(prevDataRows, 'R10')
    }

    def totalForMonthRows = []
    if (rowPrev != null) {
        totalForMonthRows += rowPrev
    }

    totalForMonthRows += getDataRow(dataRows, 'R10')

    def totalForTaxPeriodRow = getDataRow(dataRows, 'R11')
    writeResultsToRowOrCheck(totalForMonthRows, totalForTaxPeriodRow, check)
}

// Заносим подсчитанные итоговые значения из мапы в выбранную строку или проверяем корректность расчета
def writeResultsToRowOrCheck(def dataRowsList, def totalRow, def check) {
    def totalResults = [:]
    totalColumns.each { col ->
        totalResults.put(col, BigDecimal.ZERO)
    }

    for (def dataRow : dataRowsList) {
        totalResults.keySet().each { col ->
            final cellValue = dataRow.get(col)
            if (cellValue != null) {
                totalResults.put(col, totalResults.get(col) + cellValue)
            }
        }
    }

    for (def col : totalResults.keySet()) {
        if (!check) {
            totalRow[col] = totalResults[col]
        } else {
            if (totalResults[col] != totalRow[col]) {
                def errorMsg = "Строка ${totalRow.getIndex()}: "
                logger.error(errorMsg + WRONG_TOTAL, getColumnName(totalRow, col))
                return
            }
        }
    }
}

BigDecimal getGraph19(def values, def row) {
    if (values.acquisitionDate < fixedDate) {
        return null // не заполняется
    }
    return round(row.marketPriceInPerc) //ручной ввод
}

BigDecimal getGraph20(def values, def row) {
    if (values.acquisitionDate < fixedDate) {
        return round(values.costWithoutNKD)
    }
    return round(row.marketPriceInRub) //ручной ввод
}

BigDecimal getGraph21(def row) {
    return (row.costWithoutNKD > row.marketPriceInRub) ? row.marketPriceInRub : row.costWithoutNKD
}

BigDecimal getGraph27(def values, def row) {
    if (getOperationType(values.operationType).equals('Погашение')) {
        values.marketPriceRealizationInPerc = 100
    } else {
        return round(row.marketPriceRealizationInPerc)
    }
}

BigDecimal getGraph28(def values, def row) {
    if (getOperationType(values.operationType).equals('Погашение')) {
        values.marketPriceRealizationInRub = values.repaymentWithoutNKD
    } else {
        return row.marketPriceRealizationInRub
    }
}

BigDecimal getGraph31(def row) {
    if (row.costAcquisition != null && row.loss != null && row.lossRealization != null) {
        return row.costAcquisition + row.loss + row.lossRealization
    }
    return null
}

BigDecimal getGraph32(def values, def row) {
    return !isDiscountBond(values) ? null : row.averageWeightedPrice
}

BigDecimal getGraph33(def values, def row) {
    return !isDiscountBond(values) ? null : row.termIssue
}

BigDecimal getGraph34(def row) {
    if (isDiscountBond(row) && row.realizationDate != null && row.acquisitionDate != null) {
        return row.realizationDate - row.acquisitionDate
    }
    return null
}

BigDecimal getGraph35(def row) {
    if (isDiscountBond(row) && row.nominal != null && row.averageWeightedPrice != null && row.termHold != null
            && row.amount != null && row.termIssue != null) {
        return round((row.nominal - row.averageWeightedPrice) * row.termHold * row.amount / row.termIssue)
    }
    return null
}

BigDecimal getGraph36(def row) {
    if (row.securityName == null) {
        return null
    }
    def currencyCode = getCurrencyCode(row.securityName)
    if (currencyCode in ['810', '643']) {
        return row.interestIncomeCurrency
    }
    if (!isDiscountBond(row)) {
        return null
    }
    if (row.interestIncomeCurrency != null) {
        def rate = getRate(row, row.realizationDate)
        if (rate == null) {
            return null
        }
        return round(row.interestIncomeCurrency * rate ?: 0)
    }
    return null
}

BigDecimal getGraph37(def row) {
    if (isDiscountBond(row) && row.costRealization != null && row.totalLoss != null && row.interestIncomeInRub != null) {
        return row.costRealization - row.totalLoss - row.interestIncomeInRub
    }
    if (isCouponBound(row) && row.costRealization != null && row.totalLoss != null) {
        return row.costRealization - row.totalLoss
    }
    return null
}

BigDecimal getGraph38(def row) {
    if (row.realizationPriceInRub > 0 && row.costRealization != null && row.realizationPriceInRub != null) {
        return row.costRealization - row.realizationPriceInRub
    }
    if (row.realizationPriceInRub == 0 && row.repaymentWithoutNKD > 0 && row.costRealization != null
            && row.repaymentWithoutNKD != null) {
        return row.costRealization - row.repaymentWithoutNKD
    }
    return null
}

/**
 * определяем, является ли облигация дисконтной
 * @return Если «графа 7» == «2» тогда {@value true} иначе {@value false}
 */
boolean isDiscountBond(def row) {
    getSecurityKind(row.securityName) == 2
}

/**
 * определяем, является ли облигация купонной
 * @return Если «графа 7» == «1» || «графа 7» == «3» тогда {@value true} иначе {@value false}
 */
boolean isCouponBound(def row) {
    def securityKind = getSecurityKind(row.securityName)
    return securityKind == 1 || securityKind == 3
}

def getBalanceNumber(def id) {
    return getRefBookValue(29, id)?.BALANCE_ACCOUNT?.stringValue
}

def getOperationType(def id) {
    return getRefBookValue(87, id)?.OPERATION_TYPE?.stringValue
}

def getSignContractor(def id) {
    return getRefBookValue(88, id)?.CODE?.numberValue
}

def getSecurityKind(def record84Id) {
    def record89Id = getRefBookValue(84, record84Id?.toLong())?.TYPE?.value
    return getRefBookValue(89, record89Id)?.CODE?.value
}

/** Получить признак ценной бумаги. */
def getSignSecurity(def record84Id) {
    def signId = getRefBookValue(84, record84Id)?.SIGN?.value
    return getRefBookValue(62, signId)?.CODE?.value
}

/** Получить буквенный код валюты. */
def getCurrencyCode(def record84Id) {
    def record15Id = getRefBookValue(84, record84Id?.toLong())?.CODE_CUR?.value
    return getRefBookValue(15, record15Id)?.CODE?.value
}

def getCompareList(DataRow row) {
    return [row.balanceNumber,
            getSignSecurity(row.securityName),
            getSecurityKind(row.securityName),
            getSignContractor(row.signContractor),
            getOperationType(row.operationType)]
}

def BigDecimal round(BigDecimal value, def int precision = 2) {
    return value?.setScale(precision, RoundingMode.HALF_UP)
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'Номер балансового счёта', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 39, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): 'Номер балансового счёта',
            (xml.row[0].cell[2]): 'Вид операции (продажа, погашение, открытие \\закрытие короткой позиции)',
            (xml.row[0].cell[3]): 'Признак контрагента: 3 - эмитент ценной бумаги, 4 - организатор торговли, 5 - прочие',
            (xml.row[0].cell[4]): 'Наименование контрагента',
            (xml.row[0].cell[5]): 'Наименование ценной бумаги (включая наименование эмитента)',
            (xml.row[0].cell[6]): 'Серия (выпуск)',
            (xml.row[0].cell[7]): 'Вид ценной бумаги: 1 - купонная облигация, 2 - дисконтная облигация, 3 - акция',
            (xml.row[0].cell[8]): 'Признак ценной бумаги: "+" - обращающаяся на ОРЦБ; "-" - необращающаяся на ОРЦБ',
            (xml.row[0].cell[9]): 'Код валюты бумаги (номинала)',
            (xml.row[0].cell[10]): 'Наименование валюты бумаги (номинала)',
            (xml.row[0].cell[11]): 'Номинал 1 бумаги, ед. валюты',
            (xml.row[0].cell[12]): 'Количество ценных бумаг, шт.',
            (xml.row[0].cell[13]): 'Дата приобретения, закрытия короткой позиции (8)',
            (xml.row[0].cell[14]): 'Дата совершения сделки',
            (xml.row[0].cell[15]): 'Код валюты расчётов по сделке',
            (xml.row[0].cell[16]): 'Наименование валюты расчётов по сделке',
            (xml.row[0].cell[17]): 'Стоимость покупки без НКД, рублей (по курсу на дату приобретения)',
            (xml.row[0].cell[18]): 'Расходы банка, связанные с приобретением, рублей (по курсу на дату приобретения)',
            (xml.row[0].cell[19]): 'Рыночная цена на дату приобретения',
            (xml.row[0].cell[21]): 'Стоимость приобретения без НКД в целях налогообложения, рублей (по курсу на дату приобретения)',
            (xml.row[0].cell[22]): 'Дата реализации (погашения), открытия короткой позиции',
            (xml.row[0].cell[23]): 'Дата совершения сделки',
            (xml.row[0].cell[24]): 'Стоимость погашения без НКД, рублей (по курсу на дату признания дохода)',
            (xml.row[0].cell[25]): 'Цена реализации (без НКД)',
            (xml.row[0].cell[27]): 'Рыночная цена на дату реализации',
            (xml.row[0].cell[29]): 'Стоимость реализации (выбытия) без НКД в целях налогообложения, рублей (по курсу на дату признания дохода)',
            (xml.row[0].cell[30]): 'Расходы банка, связанные с реализацией, рублей (по курсу на дату признания дохода)',
            (xml.row[0].cell[31]): 'Всего расходы по реализации, =гр.21 + гр.18 + гр.30, рублей',
            (xml.row[0].cell[32]): 'Средневзвешенная цена 1 бумаги на дату, когда выпуск ценных бумаг признан размещенным, ед. валюты',
            (xml.row[0].cell[33]): 'Срок обращения согласно условиям выпуска (дни) (для дисконтных облигаций)',
            (xml.row[0].cell[34]): 'Срок владения ценной бумагой (дни) (для дисконтных облигаций)',
            (xml.row[0].cell[35]): 'Процентный доход, полученный за время владения дисконтными облигациями',
            (xml.row[0].cell[37]): 'Прибыль (убыток) от реализации (погашения) для дисконтных облигаций = гр.29-гр.31-гр.36; для купонных облигаций и акций = гр.29-гр.31, рублей',
            (xml.row[0].cell[38]): 'Превышение цены реализации для целей налогообложения над ценой реализации, рублей',
            (xml.row[1].cell[19]): '% к номиналу; руб.коп.',
            (xml.row[1].cell[20]): 'рублей по курсу на дату приобретения',
            (xml.row[1].cell[25]): '% к номиналу - для облигаций; руб.коп.- для акций',
            (xml.row[1].cell[26]): 'рублей (по курсу на дату признания дохода)',
            (xml.row[1].cell[27]): '% к номиналу - для облигаций; руб.коп.- для акций',
            (xml.row[1].cell[28]): 'рублей по курсу на дату признания дохода'
    ]

    (1..38).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = 10 // Смещение для индекса колонок в ошибках импорта
    def int colOffset = 1 // Смещение для индекса колонок в ошибках импорта

    def int rowIndex = 1  // Строки НФ, от 1

    def mapRows = [:]
    def sectionIndex = null

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        def firstValue = row.cell[0].text()
        if (firstValue == 'Итого') {
            continue
        } else if (firstValue == 'Всего за текущий месяц' || firstValue == 'Всего за текущий налоговый период') {
            break
        } else if (firstValue != null && firstValue != '' && firstValue != 'Итого') {
            sectionIndex = 'R' + firstValue[0]
            mapRows.put(sectionIndex, [])
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 1

        newRow.balanceNumber = row.cell[indexCell].text()
        indexCell++

        // графа 2
        newRow.operationType = getRecordIdImport(87, 'OPERATION_TYPE', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
        indexCell++

        // графа 3
        newRow.signContractor = getRecordIdImport(88, 'CODE', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
        indexCell++

        // графа 4
        newRow.contractorName = row.cell[indexCell].text()
        indexCell++

        // графа 5 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
        def record100 = getRecordImport(100, 'NAME', row.cell[5].text(), xlsIndexRow, 5 + colOffset)

        if (record100 != null) {
            // поиск записи по эмитенту и серии (графам 5 и 6)
            String filter = "ISSUER = " + (record100?.record_id?.value?.toString() ?: 0) + " and LOWER(SHORTNAME) = LOWER('" + (row.cell[6].text() ?: '') + "')"
            def records = refBookFactory.getDataProvider(84).getRecords(getReportPeriodEndDate(), null, filter, null)
            def record84 = null
            if (records.size() == 1) {
                record84 = records[0]
                newRow.securityName = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
            } else {
                logger.error("Проверка файла: Строка ${xlsIndexRow} содержит значение, отсутствующее в справочнике " +
                        "«" + refBookFactory.get(84).getName() + "»!")
            }

            if (record84 != null) {
                // графа 6 - зависит от графы 5 - атрибут 812 - SHORTNAME - «Краткое название ценной бумаги / Выпуск», справочник 84 «Ценные бумаги»
                indexCell = 6
                def value1 = record84?.SHORTNAME?.value
                def value2 = row.cell[indexCell].text()
                formDataService.checkReferenceValue(84, value1, value2, xlsIndexRow, indexCell + colOffset, logger, true)

                // графа 7 - зависит от графы 5 - атрибут 815 - TYPE - «Тип (вид) ценной бумаги», справочник 84 «Ценные бумаги»
                indexCell = 7
                def record89 = getRecordImport(89, 'CODE', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
                if (record89 != null) {
                    value1 = record89?.record_id?.value?.toString()
                    value2 = record84?.TYPE?.value?.toString()
                    formDataService.checkReferenceValue(84, value1, value2, xlsIndexRow, indexCell + colOffset, logger, true)
                }

                // графа 8 - зависит от графы 5 - атрибут 869 - SIGN - «Признак ценной бумаги», справочник 84 «Ценные бумаги»
                indexCell = 8
                def record62 = getRecordImport(62, 'CODE', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
                if (record62 != null) {
                    value1 = record62?.record_id?.value?.toString()
                    value2 = record84?.SIGN?.value?.toString()
                    formDataService.checkReferenceValue(84, value1, value2, xlsIndexRow, indexCell + colOffset, logger, true)
                }

                // графа 9 - зависит от графы 5 - атрибут 810 - CODE - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
                indexCell = 9
                def record15 = getRecordImport(15, 'CODE', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
                if (record15 != null) {
                    value1 = record15?.record_id?.value?.toString()
                    value2 = record84?.CODE_CUR?.value?.toString()
                    formDataService.checkReferenceValue(84, value1, value2, xlsIndexRow, indexCell + colOffset, logger, true)
                }

                // графа 10 - зависит от графы 5 - атрибут 810 - NAME - «Наименование валюты», справочник 84 «Ценные бумаги»
                indexCell = 10
                if (record15 != null) {
                    value1 = record15?.NAME?.value
                    value2 = row.cell[indexCell].text()
                    formDataService.checkReferenceValue(84, value1, value2, xlsIndexRow, indexCell + colOffset, logger, true)
                }
            }
        } else {
            logger.error("Проверка файла: Строка ${xlsIndexRow} содержит значение, отсутствующее в справочнике " +
                    "«" + refBookFactory.get(100).getName() + "»!")
        }

        // графа 11
        indexCell = 11
        newRow.nominal = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 12
        newRow.amount = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 13
        newRow.acquisitionDate = parseDate(row.cell[indexCell].text(), "dd.MM.yyyy", xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 14
        newRow.tradeDate = parseDate(row.cell[indexCell].text(), "dd.MM.yyyy", xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 15 - атрибут 65 CODE_2 - «Код валюты. Буквенный», справочник 15 «Единый справочник валют»
        record15 = getRecordImport(15, 'CODE_2', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
        newRow.currencyCodeTrade = record15?.record_id?.value
        indexCell++

        // графа 16 - зависит от графы 15 - атрибут 66 NAME - «Наименование валюты», справочник 15 «Единый справочник валют»
        if (record15 != null) {
            def value1 = record15?.NAME?.value
            def value2 = row.cell[indexCell].text()
            formDataService.checkReferenceValue(84, value1, value2, xlsIndexRow, indexCell + colOffset, logger, true)
        }
        indexCell++

        // графа 17
        newRow.costWithoutNKD = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 18
        newRow.loss = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 19
        newRow.marketPriceInPerc = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 20
        newRow.marketPriceInRub = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 21
        indexCell++

        // графа 22
        newRow.realizationDate = parseDate(row.cell[indexCell].text(), "dd.MM.yyyy", xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 23
        newRow.tradeDate2 = parseDate(row.cell[indexCell].text(), "dd.MM.yyyy", xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 24
        newRow.repaymentWithoutNKD = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 25
        newRow.realizationPriceInPerc = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 26
        newRow.realizationPriceInRub = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 27
        newRow.marketPriceRealizationInPerc = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 28
        newRow.marketPriceRealizationInRub = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 29
        newRow.costRealization = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        // графа 30
        newRow.lossRealization = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, true)
        indexCell++

        mapRows[sectionIndex].add(newRow)
    }

    deleteNotFixedRows(dataRows)

    // копирование данных по разделам
    groups.each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, section + '-total').getIndex() - 1
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

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 38, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached
    def int rnuIndexRow = 2
    def int colOffset = 1

    def mapRows = [:]

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }
        def newRow = formData.createDataRow()
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 1
        indexCell = 1
        newRow.balanceNumber = row.cell[indexCell].text()

        // графа 2
        indexCell = 2
        newRow.operationType = getRecordIdImport(87, 'OPERATION_TYPE', row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset)

        // графа 3
        indexCell = 3
        newRow.signContractor = getRecordIdImport(88, 'CODE', row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset)

        // графа 4
        indexCell = 4
        newRow.contractorName = row.cell[indexCell].text()

        // графа 5 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
        def record100 = getRecordImport(100, 'NAME', row.cell[5].text(), rnuIndexRow, 5 + colOffset)
        if (record100 != null) {
            // поиск записи по эмитенту и серии (графам 5 и 6)
            String filter = "ISSUER = " + record100?.record_id?.value?.toString() + " and LOWER(SHORTNAME) = LOWER('" + (row.cell[6].text() ?: '') + "')"
            def records = refBookFactory.getDataProvider(84).getRecords(getReportPeriodEndDate(), null, filter, null)
            def record84 = null
            if (records.size() == 1) {
                record84 = records[0]
                newRow.securityName = record84.get(RefBook.RECORD_ID_ALIAS).numberValue
            } else {
                logger.error("Проверка файла: Строка ${rnuIndexRow} содержит значение, отсутствующее в справочнике " +
                        "«" + refBookFactory.get(84).getName() + "»!")
            }

            if (record84 != null) {
                // графа 6 - зависит от графы 5 - атрибут 812 - SHORTNAME - «Краткое название ценной бумаги / Выпуск», справочник 84 «Ценные бумаги»
                indexCell = 6
                def value1 = record84?.SHORTNAME?.value
                def value2 = row.cell[indexCell].text()
                formDataService.checkReferenceValue(84, value1, value2, rnuIndexRow, indexCell + colOffset, logger, true)

                // графа 7 - зависит от графы 5 - атрибут 815 - TYPE - «Тип (вид) ценной бумаги», справочник 84 «Ценные бумаги»
                indexCell = 7
                def record89 = getRecordImport(89, 'CODE', row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset)
                if (record89 != null) {
                    value1 = record89?.record_id?.value?.toString()
                    value2 = record84?.TYPE?.value?.toString()
                    formDataService.checkReferenceValue(84, value1, value2, rnuIndexRow, indexCell + colOffset, logger, true)
                }

                // графа 8 - зависит от графы 5 - атрибут 869 - SIGN - «Признак ценной бумаги», справочник 84 «Ценные бумаги»
                indexCell = 8
                def record62 = getRecordImport(62, 'CODE', row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset)
                if (record62 != null) {
                    value1 = record62?.record_id?.value?.toString()
                    value2 = record84?.SIGN?.value?.toString()
                    formDataService.checkReferenceValue(84, value1, value2, rnuIndexRow, indexCell + colOffset, logger, true)
                }

                // графа 9 - зависит от графы 5 - атрибут 810 - CODE - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
                indexCell = 9
                def record15 = getRecordImport(15, 'CODE', row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset)
                if (record15 != null) {
                    value1 = record15?.record_id?.value?.toString()
                    value2 = record84?.CODE_CUR?.value?.toString()
                    formDataService.checkReferenceValue(84, value1, value2, rnuIndexRow, indexCell + colOffset, logger, true)
                }

                // графа 10 - зависит от графы 5 - атрибут 810 - NAME - «Наименование валюты», справочник 84 «Ценные бумаги»
                indexCell = 10
                if (record15 != null) {
                    value1 = record15?.NAME?.value
                    value2 = row.cell[indexCell].text()
                    formDataService.checkReferenceValue(84, value1, value2, rnuIndexRow, indexCell + colOffset, logger, true)
                }
            }
        } else {
            logger.error("Проверка файла: Строка ${xlsIndexRow} содержит значение, отсутствующее в справочнике " +
                    "«" + refBookFactory.get(100).getName() + "»!")
        }

        // графа 11
        indexCell = 11
        newRow.nominal = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 12
        indexCell = 12
        newRow.amount = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 13
        indexCell = 13
        newRow.acquisitionDate = parseDate(row.cell[indexCell].text(), "dd.MM.yyyy", rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 14
        indexCell = 14
        newRow.tradeDate = parseDate(row.cell[indexCell].text(), "dd.MM.yyyy", rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 15 - атрибут 65 CODE_2 - «Код валюты. Буквенный», справочник 15 «Единый справочник валют»
        indexCell = 15
        record15 = getRecordImport(15, 'CODE_2', row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset)
        newRow.currencyCodeTrade = record15?.record_id?.value

        // графа 16 - зависит от графы 15 - атрибут 66 NAME - «Наименование валюты», справочник 15 «Единый справочник валют»
        indexCell = 16
        if (record15 != null) {
            def value1 = record15?.NAME?.value
            def value2 = row.cell[indexCell].text()
            formDataService.checkReferenceValue(84, value1, value2, rnuIndexRow, indexCell + colOffset, logger, true)
        }

        // графа 17
        indexCell = 17
        newRow.costWithoutNKD = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 18
        indexCell = 18
        newRow.loss = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 19
        indexCell = 19
        newRow.marketPriceInPerc = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 20
        indexCell = 20
        newRow.marketPriceInRub = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 21
        indexCell = 21
        newRow.costAcquisition = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 22
        indexCell = 22
        newRow.realizationDate = parseDate(row.cell[indexCell].text(), "dd.MM.yyyy", rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 23
        indexCell = 23
        newRow.tradeDate2 = parseDate(row.cell[indexCell].text(), "dd.MM.yyyy", rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 24
        indexCell = 24
        newRow.repaymentWithoutNKD = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 25
        indexCell = 25
        newRow.realizationPriceInPerc = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 26
        indexCell = 26
        newRow.realizationPriceInRub = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 27
        indexCell = 27
        newRow.marketPriceRealizationInPerc = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 28
        indexCell = 28
        newRow.marketPriceRealizationInRub = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 29
        indexCell = 29
        newRow.costRealization = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 30
        indexCell = 30
        newRow.lossRealization = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 31
        indexCell = 31
        newRow.totalLoss = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 32
        indexCell = 32
        newRow.averageWeightedPrice = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 33
        indexCell = 33
        newRow.termIssue = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 34
        indexCell = 34
        newRow.termHold = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 35
        indexCell = 35
        newRow.interestIncomeCurrency = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 36
        indexCell = 36
        newRow.interestIncomeInRub = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 37
        indexCell = 37
        newRow.realizationResult = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 38
        indexCell = 38
        newRow.excessSellingPrice = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // Техническое поле(группа)
        indexCell = 39
        def sectionName = "R" + row.cell[indexCell].text()

        if (mapRows[sectionName] == null) {
            mapRows[sectionName] = []
        }
        mapRows[sectionName].add(newRow)
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
    groups.each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, "$section").getIndex()
            dataRows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }


    calcOrCheckTotalDataRows(dataRows, false)
    calcOrCheckTotalForMonth(dataRows, false)
    def totalRow = getDataRow(dataRows, 'R10')
    calcOrCheckTotalForTaxPeriod(dataRows, false)

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow += 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // графа 12
        indexCell = 12
        total.amount = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 17
        indexCell = 17
        total.costWithoutNKD = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 18
        indexCell = 18
        total.loss = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 20
        indexCell = 20
        total.marketPriceInRub = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 21
        indexCell = 21
        total.costAcquisition = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 24
        indexCell = 24
        total.repaymentWithoutNKD = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 25
        indexCell = 25
        total.realizationPriceInPerc = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 26
        indexCell = 26
        total.realizationPriceInRub = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 28
        indexCell = 28
        total.marketPriceRealizationInRub = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 29
        indexCell = 29
        total.costRealization = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 30
        indexCell = 30
        total.lossRealization = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 31
        indexCell = 31
        total.totalLoss = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 35
        indexCell = 35
        total.interestIncomeCurrency = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 36
        indexCell = 36
        total.interestIncomeInRub = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 37
        indexCell = 37
        total.realizationResult = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        // графа 38
        indexCell = 38
        total.excessSellingPrice = parseNumber(row.cell[indexCell].text(), rnuIndexRow, indexCell + colOffset, logger, true)

        def colIndexMap = ['amount' : 12, 'costWithoutNKD' : 17, 'loss' : 18, 'marketPriceInRub' : 20,
                           'costAcquisition' : 21, 'repaymentWithoutNKD' : 24, 'realizationPriceInRub' : 26,
                           'marketPriceRealizationInRub' : 28, 'costRealization' : 29, 'lossRealization' : 30,
                           'totalLoss' : 31, 'interestIncomeCurrency' : 35, 'interestIncomeInRub' : 36,
                           'realizationResult' : 37, 'excessSellingPrice' : 38]
        for (def alias : totalColumns) {
            def v1 = total[alias]
            def v2 = totalRow[alias]
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.error(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
                break
            }
        }
    }
    dataRowHelper.save(dataRows)
}

// Получить курс валюты по id записи из справочнкиа ценной бумаги (84)
def getRate(def row, def Date date) {
    if (row.securityName == null) {
        return null
    }
    // получить запись (поле Цифровой код валюты выпуска) из справочника ценные бумаги (84) по id записи
    def code = getRefBookValue(84, row.securityName)?.CODE_CUR?.value

    // получить запись (поле курс валюты) из справочника курс валют (22) по цифровому коду валюты
    def record22 = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache,
            'CODE_NUMBER', code?.toString(), date, row.getIndex(), getColumnName(row, 'currencyCode'), logger, true)
    return record22?.RATE?.value
}
