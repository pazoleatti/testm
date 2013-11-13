package form_template.income.f7_8

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field
import java.text.SimpleDateFormat
import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.*

/**
 * Скрипт для Ф-7.8 (f7_8.groovy).
 * (Ф 7.8) Реестр совершенных операций с ценными бумагами по продаже и погашению, а также по открытию-закрытию
 * короткой позиции
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
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED : // после принятия из подготовлена
        logicCheck()
        break
    // обобщить
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
            'tradeDate', 'currencyCodeTrade', 'currencyNameTrade', 'costWithoutNKD', 'loss', 'marketPriceInPerc',
            'marketPriceInRub', 'costAcquisition', 'realizationDate', 'tradeDate2', 'repaymentWithoutNKD',
            'realizationPriceInPerc', 'realizationPriceInRub', 'marketPriceRealizationInPerc',
            'marketPriceRealizationInRub', 'costRealization', 'lossRealization', 'totalLoss', 'averageWeightedPrice',
            'termIssue', 'termHold', 'interestIncomeCurrency', 'interestIncomeInRub', 'realizationResult',
            'excessSellingPrice']

@Field
def arithmeticCheckAlias = ['marketPriceInPerc', 'marketPriceInRub', 'costAcquisition', 'marketPriceRealizationInPerc',
        'marketPriceRealizationInRub', 'costRealization', 'totalLoss', 'averageWeightedPrice', 'termIssue', 'termHold',
        'interestIncomeCurrency', 'interestIncomeInRub', 'realizationResult', 'excessSellingPrice']

@Field
def fixedDate = new SimpleDateFormat('dd.MM.yyyy').parse('01.01.2010')

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias, def required) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName, def date,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            date, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

void calc(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def sortRows = []
    def from
    def to

    groups.each { section ->
        from = getDataRow(dataRows, section).getIndex()
        to = getDataRow(dataRows, "$section-total").getIndex() - 2
        if (from<=to) {
            sortRows.add(dataRows[from..to])
        }

    }
    sortRows.each {
        it.sort {  DataRow a, DataRow b ->
            if ((a != null && a.getAlias() != null) || (b != null && b.getAlias() != null)){
                return 0
            }
            def aList = getCompareList(a)
            def bList = getCompareList(b)
            for (def aD : aList){
                bD = bList.get(aList.indexOf(aD))
                if (aD != bD) {
                    return aD <=> bD
                }
            }
        }
    }
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
    for (def row : dataRows){
        if (row != null && row.getAlias() != null){
            continue
        }
        row.with{
            marketPriceInPerc = roundTo(getGraph19(row, row), 2)
            marketPriceInRub = roundTo(getGraph20(row, row), 2)
            costAcquisition = roundTo(getGraph21(row), 2)
            marketPriceRealizationInPerc = roundTo(getGraph27(row, row), 2)
            marketPriceRealizationInRub = roundTo(getGraph28(row, row), 2)
            costRealization = roundTo(getGraph29(row), 2)
            totalLoss = roundTo(getGraph31(row), 2)
            averageWeightedPrice = roundTo(getGraph32(row, row), 2)
            termIssue = roundTo(getGraph33(row, row), 2)
            termHold = roundTo(getGraph34(row), 2)
            interestIncomeCurrency = roundTo(getGraph35(row), 2)
            interestIncomeInRub = roundTo(getGraph36(row), 2)
            realizationResult = roundTo(getGraph37(row), 2)
            excessSellingPrice = roundTo(getGraph38(row), 2)
        }
    }

    calcOrCheckTotalDataRows(dataRows, false)
    calcOrCheckTotalForMonth(dataRows, false)
    calcOrCheckTotalForTaxPeriod(dataRows, false)

    dataRowHelper.save(dataRows)
}

void addNewRow(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    DataRow<Cell> newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        def row = getDataRow(dataRows,'R1-total')
        dataRowHelper.insert(newRow,dataRows.indexOf(row)+1)
    } else if (currentDataRow.getAlias() == null) {
        dataRowHelper.insert(newRow, currentDataRow.getIndex()+1)
    } else {
        def alias = currentDataRow.getAlias()
        if (alias in ['R10', 'R11']){
            alias = 'R9'
        }
        def totalAlias = alias.contains('total') ? alias : "$alias-total"
        def row = getDataRow(dataRows, totalAlias)
        dataRowHelper.insert(newRow, dataRows.indexOf(row)+1)
    }
}

/**
 * Консолидация.
 */
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

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def FormData source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                // подразделы
                groups.each { section ->
                    copyRows(source, dataRows, "$section", "$section-total")
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
 * @param sourceForm форма источник
 * @param destinationRows форма приемник
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно),
 *      если = null, то копировать с 0 строки
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def FormData sourceForm, def destinationRows, def fromAlias, def toAlias) {
    def sourceData = formDataService.getDataRowHelper(sourceForm)
    def sourceRows = sourceData.allCached
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

    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def formDataPrev
    if (prevReportPeriod !=null){
        formDataPrev = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, prevReportPeriod.id)
        if (formDataPrev == null && !isFirstPeriod()) {
            logger.warn('Отсутствует предыдущий экземпляр отчета')
        }
    }
    for (def row : dataRows) {
        if(row != null && row.getAlias() != null){
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        def graph27 = getGraph27(row, row)
        if (graph27 != null && graph27 != row.marketPriceRealizationInPerc) {
            logger.error(errorMsg + "Неверно указана рыночная цена в процентах при погашении!")
        }
        def graph28 = getGraph28(row, row)
        if (graph28 != null && graph28 != row.marketPriceRealizationInRub) {
            logger.error(errorMsg + "Неверно указана рыночная цена в рублях при погашении!")
        }
        if (row.excessSellingPrice < 0){
            logger.error(errorMsg + "Превышение цены реализации для целей налогообложения над ценой реализации отрицательное!")
        }
        def values = [:]
        allColumns.each{
            values[it] = row.getCell(it).getValue()
        }
        values.with{
            marketPriceInPerc = roundTo(getGraph19(values, row), 2)
            marketPriceInRub = roundTo(getGraph20(values, row), 2)
            costAcquisition = roundTo(getGraph21(values), 2)
            marketPriceRealizationInPerc = roundTo(getGraph27(values, row), 2)
            marketPriceRealizationInRub = roundTo(getGraph28(values, row), 2)
            costRealization = roundTo(getGraph29(values), 2)
            totalLoss = roundTo(getGraph31(values), 2)
            averageWeightedPrice = roundTo(getGraph32(values, row), 2)
            termIssue = roundTo(getGraph33(values, row), 2)
            termHold = roundTo(getGraph34(values), 2)
            interestIncomeCurrency = roundTo(getGraph35(values), 2)
            interestIncomeInRub = roundTo(getGraph36(values), 2)
            realizationResult = roundTo(getGraph37(values), 2)
            excessSellingPrice = roundTo(getGraph38(values), 2)
        }
        checkCalc(row, arithmeticCheckAlias, values, logger, true)

        checkNSI(29, row, 'balanceNumber', true)
        checkNSI(87, row, 'operationType', false)
        checkNSI(88, row, 'signContractor', false)
        checkNSI(89, row, 'securityKind', false)
        checkNSI(62, row, 'signSecurity', false)
        checkNSI(15, row, 'currencyCode', true)
        checkNSI(15, row, 'currencyName', false)
        checkNSI(15, row, 'currencyCodeTrade', true)
        checkNSI(15, row, 'currencyNameTrade', true)
    }
    calcOrCheckTotalDataRows(dataRows, true)
    calcOrCheckTotalForMonth(dataRows, true)
    calcOrCheckTotalForTaxPeriod(dataRows, true)
}

/**
 * рассчитываем вычисляемые поля для строк ИТОГО или проверяем расчет
 */
void calcOrCheckTotalDataRows(def dataRows, def check) {
    groups.each { group ->
        def firstRow = getDataRow(dataRows,group)
        def lastRow = getDataRow(dataRows,"$group-total")
        def firstIndex = firstRow.getIndex()
        def lastIndex = lastRow.getIndex() - 1
        def groupRows = (firstIndex<=lastIndex)?dataRows.subList(firstIndex, lastIndex):[]
        writeResultsToRowOrCheck(groupRows, lastRow, check)
    }
}

/**
 * расчитываем значения для строки "Всего за текущий месяц" или проверяем расчеты
 */
void calcOrCheckTotalForMonth(def dataRows, def check) {
    def totalRows = []

    groups.each { group ->
        def totalRow = getDataRow(dataRows,"$group-total")
        if (totalRow != null) {
            totalRows.add(totalRow)
        }
    }
    def totalForMonthRow = getDataRow(dataRows,'R10')
    writeResultsToRowOrCheck(totalRows, totalForMonthRow, check)
}

/**
 * рассчитываем значения для строки "Всего за текущий налоговый период" или проверяем значения
 */
void calcOrCheckTotalForTaxPeriod(def dataRows, def check) {
    def reportPeriodId = formData.getReportPeriodId()
    def reportPeriod = reportPeriodService.get(reportPeriodId)
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(reportPeriod.getId())

    def formDataPrev
    if (prevReportPeriod !=null || prevReportPeriod.taxPeriod.id == reportPeriod.taxPeriod.id)
        formDataPrev = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, prevReportPeriod.id)
    def rowPrev
    if (formDataPrev != null) {
        def dataPrev = formDataService.getDataRowHelper(formDataPrev)
        rowPrev = getDataRow(dataPrev.allCached,'R10')
    }

    def totalForMonthRows = []
    if (rowPrev!=null){
        totalForMonthRows += rowPrev
    }

    totalForMonthRows += getDataRow(dataRows,'R10')

    def totalForTaxPeriodRow = getDataRow(dataRows,'R11')
    writeResultsToRowOrCheck(totalForMonthRows, totalForTaxPeriodRow, check)
}

/**
 * Заносим подсчитанные итоговые значения из мапы в выбранную строку или проверяем корректность расчета
 *
 * @param dataRowsList - строки, для которых считаются итоги
 * @param totalRow - строка, в которую нужно записать итоговые значения из мапы
 * @param check - флаг проверка это или реальный расчет
 */
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

    for(def col : totalResults.keySet()){
        if (!check) {
            totalRow[col] = totalResults[col]
        } else {
            if (totalResults[col] != totalRow[col]){
                def index = totalRow.getIndex()
                def errorMsg = "Строка $index: "
                logger.error(errorMsg + "Итоговые значения рассчитаны неверно!")
                return
            }
        }
    }
}

BigDecimal getGraph19(def values, def row) {
    if(values.acquisitionDate < fixedDate) {
        return null // не заполняется
    } else {
        return row.marketPriceInPerc //ручной ввод
    }
}

BigDecimal getGraph20(def values, def row) {
    if(values.acquisitionDate < fixedDate){
        return values.costWithoutNKD
    } else {
        return row.marketPriceInRub //ручной ввод
    }
}

BigDecimal getGraph21(def row) {
    return (row.costWithoutNKD > row.marketPriceInRub) ? row.marketPriceInRub : row.costWithoutNKD
}

BigDecimal getGraph27(def values, def row) {
    if (getOperationType(values.operationType).equals('Погашение')) {
        values.marketPriceRealizationInPerc = 100
    } else {
        return row.marketPriceRealizationInPerc
    }
}

BigDecimal getGraph28(def values, def row) {
    if (getOperationType(values.operationType).equals('Погашение')) {
        values.marketPriceRealizationInRub = values.repaymentWithoutNKD
    } else {
        return row.marketPriceRealizationInRub
    }
}

//todo (vsergeev) после ответа на http://jira.aplana.com/browse/SBRFACCTAX-2522 перепроверить алгоритм
BigDecimal getGraph29(def row) {
    final signContractorIs4 = getSignContractor(row.signContractor) == 4
    final signContractorIs5 = getSignContractor(row.signContractor) == 5
    if (signContractorIs4 && (isBargain() || isNegotiatedDeal())
            && row.realizationPriceInPerc >= row.marketPriceRealizationInPerc
            && row.realizationPriceInRub >=  row.marketPriceRealizationInRub
            || signContractorIs5
            || row.realizationPriceInPerc >= row.marketPriceRealizationInPerc
            && row.costRealization >= row.marketPriceRealizationInRub
    ) {
        return row.realizationPriceInRub
    } else if (getOperationType(row.operationType).equals('Погашение') && getSignContractor(row.signContractor) == 3) {
        return row.repaymentWithoutNKD
    } else if (signContractorIs4 && isNegotiatedDeal()
            && row.realizationPriceInPerc < row.marketPriceRealizationInPerc
            && row.realizationPriceInRub <  row.marketPriceRealizationInRub
            || signContractorIs5
            && row.realizationPriceInPerc < row.marketPriceRealizationInPerc
            && row.realizationPriceInRub <  row.marketPriceRealizationInRub) {
        return row.marketPriceRealizationInRub
    }
}

BigDecimal getGraph31(def row) {
    if (row.costAcquisition != null && row.loss != null && row.lossRealization && null) {
        return row.costAcquisition + row.loss + row.lossRealization
    } else {
        return null
    }
}

BigDecimal getGraph32(def values, def row) {
    return !isDiscountBond(values)? null: row.averageWeightedPrice
}

BigDecimal getGraph33(def values, def row) {
    return !isDiscountBond(values)? null: row.termIssue
}

BigDecimal getGraph34(def row) {
    if ((isDiscountBond(row))) {
        if (row.realizationDate != null && row.acquisitionDate != null) {
            return row.realizationDate - row.acquisitionDate
        } else {
        }
    } else {
        return null
    }
}

BigDecimal getGraph35(def row) {
    if (isDiscountBond(row)) {
        if (row.nominal != null && row.averageWeightedPrice != null && row.termHold != null && row.amount != null && row.termIssue != null) {
            return (row.nominal - row.averageWeightedPrice) * row.termHold * row.amount / row.termIssue
        } else {
            return null
        }
    } else {
        return null
    }
}

BigDecimal getGraph36(def row) {
    if (row.currencyCode != null) {
        if (refBookService.getStringValue(15,row.currencyCode,'CODE')=='810') {
            return row.interestIncomeCurrency
        } else if (! isDiscountBond(row)) {
            return null
        }
        if (row.interestIncomeCurrency != null) {
            return row.interestIncomeCurrency *
                    (getRecordId(22, 'CODE_NUMBER', "${row.currencyCode}", row.getIndex(),
                            getColumnName(row, 'currencyCode'),
                            row.realizationDate)?.RATE?.numberValue?:0)
        } else {
            return null
        }
    } else {
        return null
    }
}

BigDecimal getGraph37(def row) {
    if (isDiscountBond(row)) {
        if (row.costRealization != null && row.totalLoss != null && row.interestIncomeInRub != null) {
            return row.costRealization - row.totalLoss - row.interestIncomeInRub
        } else {
            return null
        }
    } else if (isCouponBound(row)) {
        if (row.costRealization != null && row.totalLos != null) {
            return row.costRealization - row.totalLoss
        } else {
            return null
        }
    }
}

BigDecimal getGraph38(def row) {
    if (row.realizationPriceInRub > 0) {
        if (row.costRealization != null && row.realizationPriceInRub != null) {
            return row.costRealization - row.realizationPriceInRub
        } else {
            return null
        }
    } else if (row.realizationPriceInRub == 0 && row.repaymentWithoutNKD > 0) {
        if (row.costRealization != null && row.repaymentWithoutNKD != null) {
            return row.costRealization - row.repaymentWithoutNKD
        } else {
            return null
        }
    }
}

/**
 * является биржевой сделкой, кроме переговорных сделок, проводимых на ОРЦБ
 */
def isBargain() {
    return true         //todo (vsergeev) Примечание k299, k301 ВОПРОС: По какой графе определять?
}

/**
 * являеться переговорныой сделкой, проводимой на ОРЦБ
 */
def isNegotiatedDeal() {
    return true         //todo (vsergeev) Примечание k300 ВОПРОС: По какой графе определять?
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
    return [getBalanceNumber(row.balanceNumber),
        getSignSecurity(row.signSecurity),
        getSecurityKind(row.securityKind),
        getSignContractor(row.signContractor),
        getOperationType(row.operationType)]
}

// TODO Проверка на первый месяц
boolean isFirstPeriod(){
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    return reportPeriod.order == 1
}

BigDecimal roundTo(BigDecimal value, int newScale) {
    return value?.setScale(newScale, BigDecimal.ROUND_HALF_UP)
}
