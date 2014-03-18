package form_template.income.f7_8.v1970

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

import java.math.RoundingMode
import java.text.SimpleDateFormat

/**
 * Скрипт для Ф-7.8 (f7_8.groovy).
 * (Ф 7.8) Реестр совершенных операций с ценными бумагами по продаже и погашению, а также по открытию-закрытию
 * короткой позиции
 * formTemplateId=362
 *
 * Версия ЧТЗ: 57
 *
 * @author vsergeev
 *
 * Графы:
 * 1    balanceNumber                   Номер балансового счёта Справочник 29 Атрибут 152 BALANCE_ACCOUNT
 * 2    operationType                   Вид операции (продажа, погашение, открытие \закрытие короткой позиции)
 *                                      Справочник 87 Атрибут 824 OPERATION_TYPE
 * 3    signContractor                  Признак контрагента: 3 - эмитент ценной бумаги, 4 - организатор торговли,
 *                                      5 - прочие Справочник 88 Атрибут 825 CODE
 * 4    contractorName                  Наименование контрагента
 * 5    securityName                    Наименование ценной бумаги (включая наименование эмитента)
 * 6    series                          Серия (выпуск)
 * 7    securityKind                    Вид ценной бумаги: 1 - купонная облигация, 2 - дисконтная облигация, 3 - акция
 *                                      Справочник 89 Атрибут 827 CODE
 * 8    signSecurity                    Признак ценной бумаги: «+» - обращающаяся на ОРЦБ; «-» - необращающаяся на ОРЦБ
 *                                      Справочник 62 Атрибут 621 CODE
 * 9    currencyCode                    Код валюты бумаги (номинала) Справочник 15 Атрибут 65 CODE_2
 * 10   currencyName                    Наименование валюты бумаги (номинала) Справочник 15 Атрибут 66 NAME
 * 11   nominal                         Номинал одной бумаги (ед. вал.)
 * 12   amount                          Количество ценных бумаг (шт.)
 * 13   acquisitionDate                 Дата приобретения, закрытия короткой позиции
 * 14   tradeDate                       Дата совершения сделки
 * 15   currencyCodeTrade               Код валюты расчётов по сделке Справочник 15 Атрибут 65 CODE_2
 * 16   currencyNameTrade               Наименование валюты расчётов по сделке Справочник 15 Атрибут 66 NAME
 * 17   costWithoutNKD                  Стоимость покупки без НКД, рублей (по курсу на дату приобретения)
 * 18   loss                            Расходы банка, связанные с приобретением, рублей (по курсу на дату приобретения)
 * 19   marketPriceInPerc               % к номиналу (руб.)
 * 20   marketPriceInRub                в рублях (по курсу на дату приобретения)
 * 21   costAcquisition                 Стоимость приобретения без НКД в целях налогообложения (руб. по курсу на
 *                                      дату приобретения)
 * 22   realizationDate                 Дата реализации (погашения), открытия короткой позиции
 * 23   tradeDate2                      Дата совершения сделки
 * 24   repaymentWithoutNKD             Стоимость погашения без НКД, рублей (по курсу на дату признания дохода)
 * 25   realizationPriceInPerc          % к номиналу (руб.)
 * 26   realizationPriceInRub           в рублях (по курсу на дату признания дохода)
 * 27   marketPriceRealizationInPerc    % к номиналу (руб.)
 * 28   marketPriceRealizationInRub     в рублях (по курсу на дату признания дохода)
 * 29   costRealization                 Стоимость реализации (выбытия) без НКД в целях налогообложения (руб. по курсу на
 *                                      дату признания дохода)
 * 30   lossRealization                 Расходы банка, связанные с реализацией (руб. по курсу на дату признания дохода)
 * 31   totalLoss                       Всего расходы по реализации
 * 32   averageWeightedPrice            Средневзвешенная цена одной бумаги на дату, когда выпуск ценных бумаг признан
 *                                      размещённым (ед. вал.)
 * 33   termIssue                       Срок обращения согласно условиям выпуска (дни) (для дисконтных облигаций)
 * 34   termHold                        Срок владения ценной бумагой (дни) (для дисконтных облигаций)
 * 35   interestIncomeCurrency          ед. валюты
 * 36   interestIncomeInRub             в рублях (по курсу на дату признания дохода)
 * 37   realizationResult               Прибыль (убыток) от реализации (погашения) для дисконтных и купонных
 *                                      облигаций и акций
 * 38   excessSellingPrice              Превышение цены реализации для целей налогообложения над ценой реализации (руб.)
 */
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
// обобщить
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
def editableColumns = ['balanceNumber', 'operationType', 'signContractor', 'contractorName', 'securityName', 'series',
        'securityKind', 'signSecurity', 'currencyCode', 'currencyName', 'nominal', 'amount', 'acquisitionDate',
        'tradeDate', 'currencyCodeTrade', 'currencyNameTrade', 'costWithoutNKD', 'loss', 'marketPriceInPerc',
        'marketPriceInRub', 'realizationDate', 'tradeDate2', 'repaymentWithoutNKD', 'realizationPriceInPerc',
        'realizationPriceInRub', 'marketPriceRealizationInPerc', 'marketPriceRealizationInRub', 'lossRealization']

// Обязательно заполняемые атрибуты
@Field
def nonEmptyColumns = ['balanceNumber', 'operationType', 'signContractor', 'contractorName', 'securityName', 'series',
        'securityKind', 'signSecurity', 'currencyCode', 'currencyName', 'nominal', 'amount', 'acquisitionDate',
        'tradeDate', 'currencyCodeTrade', 'currencyNameTrade', 'costWithoutNKD', 'loss', 'realizationDate',
        'tradeDate2', 'marketPriceRealizationInPerc', 'marketPriceRealizationInRub', 'costRealization', 'realizationResult',
        'excessSellingPrice']

@Field
def arithmeticCheckAlias = ['marketPriceInPerc', 'marketPriceInRub', 'costAcquisition', 'marketPriceRealizationInPerc',
        'marketPriceRealizationInRub', 'costRealization', 'totalLoss', 'averageWeightedPrice', 'termIssue', 'termHold',
        'interestIncomeCurrency', 'interestIncomeInRub', 'realizationResult', 'excessSellingPrice']

@Field
def fixedDate = new SimpleDateFormat('dd.MM.yyyy').parse('01.01.2010')

@Field
def reportPeriodEndDate

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias, def required) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
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
                bD = bList.get(aList.indexOf(aD))
                if (aD != bD) {
                    return aD<=>bD
                }
            }
        }
    }
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
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
            costRealization = getGraph29(row)
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
    def deleteRows = []
    dataRows.each { row ->
        if (!(row != null && row.getAlias() != null)) {
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
                    copyRows(sourceRows, dataRows, section, "$section-total")
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

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def dataProvider29 = refBookFactory.getDataProvider(29)
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

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
            logger.error(errorMsg + "Неверно указана рыночная цена в процентах при погашении!")
        }

        // 3.
        if (row.marketPriceRealizationInRub != getGraph28(row, row)) {
            logger.error(errorMsg + "Неверно указана рыночная цена в рублях при погашении!")
        }

        // 4.
        if (row.excessSellingPrice < 0) {
            logger.error(errorMsg + "Превышение цены реализации для целей налогообложения над ценой реализации отрицательное!")
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
            costRealization = getGraph29(values)
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

        def record = dataProvider29.getRecords(reportPeriodEndDate, null, "BALANCE_ACCOUNT = '$row.balanceNumber'", null)
        if (record.size()==0) {
            logger.error(errorMsg + "Значение графы «Номер балансового счета» отсутствует в справочнике «Классификатор соответствия счетов бухгалтерского учёта кодам налогового учёта»")
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
                def index = totalRow.getIndex()
                def errorMsg = "Строка $index: "
                logger.error(errorMsg + "Итоговые значения рассчитаны неверно!")
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

//TODO Левыкин: http://jira.aplana.com/browse/SBRFACCTAX-2522
// Подразумевается два значения с "4", у нас только одно
BigDecimal getGraph29(def row) {
    def signContractor = getSignContractor(row.signContractor)
    if (signContractor == 4 // ???
            || signContractor == 4
            && row.realizationPriceInPerc >= row.marketPriceRealizationInPerc
            && row.realizationPriceInRub >= row.marketPriceRealizationInRub
            || signContractor == 5
            && row.realizationPriceInPerc >= row.marketPriceRealizationInPerc
            && row.realizationPriceInRub >= row.marketPriceRealizationInRub
    ) {
        return row.realizationPriceInRub
    }

    if (getOperationType(row.operationType).equals('Погашение') && signContractor == 3) {
        return row.repaymentWithoutNKD
    }

    if (signContractor == 4
            && row.realizationPriceInPerc < row.marketPriceRealizationInPerc
            && row.realizationPriceInRub < row.marketPriceRealizationInRub
            || signContractor == 5
            && row.realizationPriceInPerc < row.marketPriceRealizationInPerc
            && row.realizationPriceInRub < row.marketPriceRealizationInRub) {
        return row.marketPriceRealizationInRub
    }
    return null
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
    if (row.currencyCode == null) {
        return null
    }
    if (getRefBookValue(15, row.currencyCode)?.CODE?.stringValue == '810') {
        return row.interestIncomeCurrency
    }
    if (!isDiscountBond(row)) {
        return null
    }
    if (row.interestIncomeCurrency != null) {
        def String cellName = getColumnName(row, 'currencyCode')
        def record = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache, 'CODE_NUMBER', "${row.currencyCode}",
                row.maturityDateCurrent, row.getIndex(), cellName, logger, true)

        def rate = record?.RATE?.numberValue
        return row.interestIncomeCurrency * rate ?: 0
    }
    return null
}

BigDecimal getGraph37(def row) {
    if (isDiscountBond(row)&& row.costRealization != null && row.totalLoss != null && row.interestIncomeInRub != null) {
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
    getSecurityKind(row.securityKind) == 2
}

/**
 * определяем, является ли облигация купонной
 * @return Если «графа 7» == «1» || «графа 7» == «3» тогда {@value true} иначе {@value false}
 */
boolean isCouponBound(def row) {
    getSecurityKind(row.securityKind) == 1 || getSecurityKind(row.securityKind) == 3
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

def getSecurityKind(def id) {
    return getRefBookValue(89, id)?.CODE?.numberValue
}

def getSignSecurity(def id) {
    return getRefBookValue(62, id)?.CODE?.stringValue
}

def getCompareList(DataRow row) {
    return [row.balanceNumber,
            getSignSecurity(row.signSecurity) ,
            getSecurityKind(row.securityKind),
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
            (xml.row[1].cell[28]): 'рублей по курсу на дату признания дохода',
            (xml.row[2].cell[1]): '1',
            (xml.row[2].cell[2]): '2',
            (xml.row[2].cell[3]): '3',
            (xml.row[2].cell[4]): '4',
            (xml.row[2].cell[5]): '5',
            (xml.row[2].cell[6]): '6',
            (xml.row[2].cell[7]): '7',
            (xml.row[2].cell[8]): '8',
            (xml.row[2].cell[9]): '9',
            (xml.row[2].cell[10]): '10',
            (xml.row[2].cell[11]): '11',
            (xml.row[2].cell[12]): '12',
            (xml.row[2].cell[13]): '13',
            (xml.row[2].cell[14]): '14',
            (xml.row[2].cell[15]): '15',
            (xml.row[2].cell[16]): '16',
            (xml.row[2].cell[17]): '17',
            (xml.row[2].cell[18]): '18',
            (xml.row[2].cell[19]): '19',
            (xml.row[2].cell[20]): '20',
            (xml.row[2].cell[21]): '21',
            (xml.row[2].cell[22]): '22',
            (xml.row[2].cell[23]): '23',
            (xml.row[2].cell[24]): '24',
            (xml.row[2].cell[25]): '25',
            (xml.row[2].cell[26]): '26',
            (xml.row[2].cell[27]): '27',
            (xml.row[2].cell[28]): '28',
            (xml.row[2].cell[29]): '29',
            (xml.row[2].cell[30]): '30',
            (xml.row[2].cell[31]): '31',
            (xml.row[2].cell[32]): '32',
            (xml.row[2].cell[33]): '33',
            (xml.row[2].cell[34]): '34',
            (xml.row[2].cell[35]): '35',
            (xml.row[2].cell[36]): '36',
            (xml.row[2].cell[37]): '37',
            (xml.row[2].cell[38]): '38'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = 10 // Смещение для индекса колонок в ошибках импорта
    def int colOffset = 1 // Смещение для индекса колонок в ошибках импорта

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    def aliasR = ['1. Еврооблигации 2018 года погашения, полученные в результате реструктуризации ГКО': [getDataRow(dataRows, 'R1')],
                'R1': [getDataRow(dataRows, 'R1-total')],
                '2. Прочие еврооблигации и ОВГВЗ': [getDataRow(dataRows, 'R2')],
                'R2': [getDataRow(dataRows, 'R2-total')],
                '3. Акции: ОАО «АК Сбербанк России»': [getDataRow(dataRows, 'R3')],
                'R3': [getDataRow(dataRows, 'R3-total')],
                '4. Акции: ОАО «ГМК Норильский никель», РАО «ЕЭС России», ОАО «Газпром», ОАО «Мосэнерго», ОАО «НК Роснефть», ОАО «Сургутнефтегаз», ОАО «НК ЛУКойл», ОАО «Ростелеком», ОАО «Татнефть», ОАО «Газпром нефть»': [getDataRow(dataRows, 'R4')],
                'R4': [getDataRow(dataRows, 'R4-total')],
                '5. Другие акции и облигации акционерных обществ, включённые в классификатор АС «Статотчётность»': [getDataRow(dataRows, 'R5')],
                'R5': [getDataRow(dataRows, 'R5-total')],
                '6. Прочие акции и облигации акционерных обществ, не включённые в классификатор АС «Статотчётность»': [getDataRow(dataRows, 'R6')],
                'R6': [getDataRow(dataRows, 'R6-total')],
                '7. Субфедеральные и муниципальные ценные бумаги, кроме муниципальных ценных бумаг, эмитированных до 1 января 2007 года на срок не менее 3 лет': [getDataRow(dataRows, 'R7')],
                'R7': [getDataRow(dataRows, 'R7-total')],
                '8. Муниципальные ценные бумаги, эмитированные до 1 января 2007 года на срок не менее 3 лет': [getDataRow(dataRows, 'R8')],
                'R8': [getDataRow(dataRows, 'R8-total')],
                '9. Прочие ценные бумаги': [getDataRow(dataRows, 'R9')],
                'R9': [getDataRow(dataRows, 'R9-total')],
                'R10-11': [getDataRow(dataRows, 'R10'), getDataRow(dataRows, 'R11')]
    ]

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

        // Пропуск итоговых строк
        if (row.cell[0].text() != null && row.cell[0].text() != '') {
            title = row.cell[0].text()
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 1

        newRow.balanceNumber = row.cell[indexCell].text()//getRecordIdImport(29, 'BALANCE_ACCOUNT', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
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

        // графа 5
        newRow.securityName = row.cell[indexCell].text()
        indexCell++

        // графа 6
        newRow.series = row.cell[indexCell].text()
        indexCell++

        // графа 7
        newRow.securityKind = getRecordIdImport(89, 'CODE', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
        indexCell++

        // графа 8
        newRow.signSecurity = getRecordIdImport(62, 'CODE', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
        indexCell++

        // графа 9
        newRow.currencyCode = getRecordIdImport(15, 'CODE_2', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
        indexCell++

        // графа 10
        newRow.currencyName = getRecordIdImport(15, 'NAME', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
        indexCell++

        // графа 11
        newRow.nominal = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 12
        newRow.amount = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 13
        newRow.acquisitionDate = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 14
        newRow.tradeDate = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 15
        newRow.currencyCodeTrade = getRecordIdImport(15, 'CODE_2', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
        indexCell++

        // графа 16
        newRow.currencyNameTrade = getRecordIdImport(15, 'NAME', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
        indexCell++

        // графа 17
        newRow.costWithoutNKD = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 18
        newRow.loss = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 19
        newRow.marketPriceInPerc = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 20
        newRow.marketPriceInRub = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 21
        indexCell++

        // графа 22
        newRow.realizationDate = parseDate(row.cell[indexCell].text(), "dd.MM.yyyy", xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 23
        newRow.tradeDate2 = parseDate(row.cell[indexCell].text(), "dd.MM.yyyy", xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 24
        newRow.repaymentWithoutNKD = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 25
        newRow.realizationPriceInPerc = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 26
        newRow.realizationPriceInRub = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 27
        newRow.marketPriceRealizationInPerc = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 28
        newRow.marketPriceRealizationInRub = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 29
        indexCell++

        // графа 30
        newRow.lossRealization = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        aliasR[title].add(newRow)
    }
    aliasR.each{k, v ->
        rows.addAll(v)
    }
    dataRowHelper.save(rows)
}