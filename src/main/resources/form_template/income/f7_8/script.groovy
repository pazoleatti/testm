package form_template.income.f7_8

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

import java.text.SimpleDateFormat

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
 * 3    signContractor                  Признак контрагента: 3 - эмитент ценной бумаги, 4 - организатор торговли,
 *                                      5 - прочие
 * 4    contractorName                  Наименование контрагента
 * 5    securityName                    Наименование ценной бумаги (включая наименование эмитента)
 * 6    series                          Серия (выпуск)
 * 7    securityKind                    Вид ценной бумаги: 1 - купонная облигация, 2 - дисконтная облигация, 3 - акция
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
        checkCreation()
        break
    case FormDataEvent.CHECK :
        allCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        allCheck()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED : // после принятия из подготовлена
        allCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        allCheck()
        break
}

void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

def allCheck() {
    return !hasError() && logicalCheck() && checkNSI()
}

void calc(){
    if (beforeCalcChecks()) {
        sort()
        calcDataRows()
        calcOrCheckTotalDataRows(false)
        calcOrCheckTotalForMonth(false)
        calcOrCheckTotalForTaxPeriod(false)
    }
}

def boolean checkNSI(){
    def rows = rows
    def date = new Date()
    def cache = [:]
    def isValid = true
    for (def row : rows){
        if (isFixed(row)){
            continue
        }
        def errStart = getRowIndexString(row)
        //TODO еще справочники
        if (row.balanceNumber != null && null == getRecordId(29, 'BALANCE_ACCOUNT',row.balanceNumber, date, cache)){
            logger.warn(getRefBookErrorMessage(errStart, 29, 'BALANCE_ACCOUNT',row.balanceNumber))
        }
        if (row.signSecurity != null && null == getRecordId(62, 'CODE', row.signSeciruty, date, cache)){
            logger.warn(getRefBookErrorMessage(errStart, 62, 'CODE',row.signSeciruty))
        }
        if (row.currencyCode != null && null == getRecordId(15, 'CODE_2', row.currencyCode, date, cache)){
            isValid = false
            logger.error(getRefBookErrorMessage(errStart, 15, 'CODE_2',row.currencyCode))
        }
        if (row.currencyName != null && null == getRecordId(15, 'NAME', row.currencyName, date, cache)){
            isValid = false
            logger.error(getRefBookErrorMessage(errStart, 15, 'NAME',row.currencyName))
        }
        if (row.currencyCodeTrade != null && null == getRecordId(15, 'CODE_2', row.currencyCodeTrade, date, cache)){
            isValid = false
            logger.error(getRefBookErrorMessage(errStart, 15, 'CODE_2',row.currencyCodeTrade))
        }
        if (row.currencyNameTrade != null && null == getRecordId(15, 'NAME', row.currencyNameTrade, date, cache)){
            isValid = false
            logger.error(getRefBookErrorMessage(errStart, 15, 'NAME',row.currencyNameTrade))
        }

    }
    return isValid
}

void deleteRow(){
    if (!isFixed(currentDataRow)) {
        data.delete(currentDataRow)
    }
}

void addNewRow(){
    def data = data
    def rows = getRows(data)
    DataRow<Cell> newRow = getNewRow()
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        def row = data.getDataRow(rows,'R1-total')
        data.insert(newRow,rows.indexOf(row)+1)
    } else if (currentDataRow.getAlias() == null) {
        data.insert(newRow, currentDataRow.getIndex()+1)
    } else {
        def alias = currentDataRow.getAlias()
        if (alias in ['R10', 'R11']){
            alias = 'R9'
        }
        def totalAlias = alias.contains('total') ? alias : "$alias-total"
        def row = data.getDataRow(rows, totalAlias)
        data.insert(newRow, rows.indexOf(row)+1)
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = data
    // удалить нефиксированные строки
    def deleteRows = []
    getRows(data).each { row ->
        if (!isFixed(row)) {
            deleteRows += row
        }
    }
    data.delete(deleteRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def FormData source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                // подразделы
                getGroups().each { section ->
                    copyRows(source, formData, "$section", "$section-total")
                }
            }
        }
    }
    data.save(getRows(data))
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceForm форма источник
 * @param destinationForm форма приемник
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно),
 *      если = null, то копировать с 0 строки
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def FormData sourceForm, def FormData destinationForm, def fromAlias, def toAlias) {
    def sourceData = getData(sourceForm)
    def destinationData = getData(destinationForm)
    def from = getIndexByAlias(sourceData, fromAlias) + 1
    def to = getIndexByAlias(sourceData, toAlias)
    if (from > to) {
        return
    }

    def copyRows = getRows(sourceData).subList(from, to)
    getRows(destinationData).addAll(getIndexByAlias(destinationData, toAlias), copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    getRows(destinationData).eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

/**
 * Получить новую строку с заданными стилями.
 */
def DataRow getNewRow() {
    def row = formData.createDataRow()
    getEditableCols().each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

boolean logicalCheck() {
    def boolean isValid = true
    def rows = rows

    // проверить обязательные редактируемые поля
    for (def DataRow row : rows){
        if (!isFixed(row) && !checkRequiredColumns(row, requiredCols.intersect(editableCols))){
            isValid = true
        }
    }
    for (def row : rows) {
        if(isFixed(row)){
            continue
        }
        def errStart = getRowIndexString(row)
        def graph27 = getGraph27(row, row)
        if (graph27 != null && graph27 != row.marketPriceRealizationInPerc) {
            isValid = false
            logger.error("${errStart}неверно указана рыночная цена в процентах при погашении!")
        }
        def graph28 = getGraph28(row, row)
        if (graph28 != null && graph28 != row.marketPriceRealizationInRub) {
            isValid = false
            logger.error("${errStart}неверно указана рыночная цена в рублях при погашении!")
        }
        if (row.excessSellingPrice < 0){
            isValid = false
            logger.error("${errStart}превышение цены реализации для целей налогообложения над ценой реализации отрицательное!")
        }
        if (isValid) {
            def values = getValues(row)
            values.keySet().each{
                if (values[it] != '' && row[it] != values[it]){// если не ручной ввод и различаются значения
                    isValid = false
                    def columnName = getColumnName(row, it)
                    logger.error("${errStart}неверно рассчитана графа \"$columnName\"")
                }
            }
        }

    }
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def formDataPrev
    if (prevReportPeriod !=null){
        formDataPrev = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, prevReportPeriod.id)
        //TODO проверить на первом месяце
        if (formDataPrev == null && prevReportPeriod.taxPeriod.id == reportPeriod.taxPeriod.id) {
            logger.warn('Отсутствует предыдущий экземпляр отчета')
        }
    }
    if (isValid && !(calcOrCheckTotalDataRows(true) &&
            calcOrCheckTotalForMonth(true) &&
            calcOrCheckTotalForTaxPeriod(true))){
        isValid = false
    }
    return isValid
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def DataRow row, def ArrayList<String> columns) {
    def colNames = []
    columns.each {
        if (isBlankOrNull(row[it])) {
            def name = getColumnName(row,it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def errorBegin = getRowIndexString(row)
        def errorMsg = colNames.join(', ')
        logger.error(errorBegin+ "не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Начало предупреждений/ошибок
 * @param row
 * @return
 */
def String getRowIndexString(def DataRow row){
    def index = getIndex(row)+1
    return "В строке $index "
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    return row.getIndex() - 1
}

def sort(){
    def data = data
    def rows = getRows(data)
    def sortRows = []
    def from
    def to

    getGroups().each { section ->
        from = getIndexByAlias(data, section) + 1
        to = getIndexByAlias(data, "$section-total") - 1
        if (from<=to) {
            sortRows.add(rows[from..to])
        }

    }
    sortRows.each {
        it.sort {  DataRow a, DataRow b ->
            if (isFixed(a) || isFixed(b)){
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
    data.save(rows)
}

/**
 * Получить номер строки в таблице по псевдонимиу (0..n).
 */
def getIndexByAlias(def DataRowHelper data, String rowAlias) {
    def row = data.getDataRow(rows, rowAlias)
    return (row != null ? getIndex(row) : -1)
}

/**
 * рассчитываем вычисляемые поля для строк с данными, введенными пользователем
 */
def calcDataRows() {
    for (def row :rows){
        if (isFixed(row)){
            continue
        }
        def values = getValues(row)
        values.keySet().each{
            if (values[it] != ''){// если не ручной ввод
                row[it] = values[it]
            }
        }
    }
}

def getValues(def DataRow row){
    def values = [:]
    allCols.each{
        values[it] = row.getCell(it).getValue()
    }
    values.marketPriceInPerc = getGraph19(values, row)
    values.marketPriceInRub = getGraph20(values, row)
    values.costAcquisition = getGraph21(values)
    values.marketPriceRealizationInPerc = getGraph27(values, row)
    values.marketPriceRealizationInRub = getGraph28(values, row)
    values.costRealization = getGraph29(values)
    values.totalLoss = getGraph31(values)
    values.averageWeightedPrice = getGraph32(values, row)
    values.termIssue = getGraph33(values, row)
    values.termHold = getGraph34(values)
    values.interestIncomeCurrency = getGraph35(values)
    values.interestIncomeInRub = getGraph36(values)
    values.realizationResult = getGraph37(values)
    values.excessSellingPrice = getGraph38(values)
    return values
}

/**
 * рассчитываем вычисляемые поля для строк ИТОГО или проверяем расчет
 */
def calcOrCheckTotalDataRows(def check) {
    def data = data
    def rows = rows
    def isValid = true

    getGroups().each { group ->
        def firstRow = data.getDataRow(rows,group)
        def lastRow = data.getDataRow(rows,"$group-total")
        def groupRows = rows.subList(getIndex(firstRow)+1, getIndex(lastRow)-1)
        if (!writeResultsToRowOrCheck(calcTotalResultsForRows(groupRows), lastRow, check)){
            isValid = false
        }
    }
    return isValid
}

/**
 * расчитываем значения для строки "Всего за текущий месяц" или проверяем расчеты
 */
def calcOrCheckTotalForMonth(def check) {
    def totalRows = []

    getGroups().each { group ->
        def totalRow = data.getDataRow(rows,"$group-total")
        if (totalRow != null) {
            totalRows.add(totalRow)
        }
    }
    def totalForMonthRow = data.getDataRow(rows,'R10')
    return writeResultsToRowOrCheck(calcTotalResultsForRows(totalRows), totalForMonthRow, check)
}

/**
 * рассчитываем значения для строки "Всего за текущий налоговый период" или проверяем значения
 */
def calcOrCheckTotalForTaxPeriod(def check) {
    def data = data
    def rows = rows
    def reportPeriodId = formData.getReportPeriodId()
    def reportPeriod = reportPeriodService.get(reportPeriodId)
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(reportPeriod.getId())

    def formDataPrev
    if (prevReportPeriod !=null || prevReportPeriod.taxPeriod.id == reportPeriod.taxPeriod.id)
        formDataPrev = formDataService.find(formData.getFormType(), formData.getKind(), formData.getDepartmentId(), prevReportPeriod.getId())
    def rowPrev
    if (formDataPrev != null) {
        def dataPrev = getData(formDataPrev)
        rowPrev = dataPrev.getDataRow(getRows(dataPrev),'R10')
    }

    def totalForMonthRows = []
    if (rowPrev!=null){
        totalForMonthRows += rowPrev
    }

    totalForMonthRows += data.getDataRow(rows,'R10')

    def totalForTaxPeriodRow = data.getDataRow(rows,'R11')
    return writeResultsToRowOrCheck(calcTotalResultsForRows(totalForMonthRows), totalForTaxPeriodRow, check)
}

/**
 * принимает на вход List строк, для которых нужно посчитать итоговые значения
 * возвращает мапу вида <имя_колонки : значение колонки>
 */
def calcTotalResultsForRows(def dataRowsList) {
    def totalResults = [:]
    getTotalCols().each { col ->
        totalResults.put(col, new BigDecimal(0))
    }

    for (def row : dataRowsList) {
        totalResults.keySet().each { col ->
            final cellValue = row.get(col)
            if (cellValue != null) {
                totalResults.put(col, totalResults.get(col) + cellValue)
            }
        }
    }

    logger.warn(totalResults.toString())

    return totalResults
}

/**
 * Заносим подсчитанные итоговые значения из мапы в выбранную строку или проверяем корректность расчета
 *
 * @param results - мапа с подсчитанными итоговыми значениями для строк.
 *                  для ее получения есть метод calcTotalResultsForRows
 * @param row - строка, в которую нужно записать итоговые значения из мапы
 * @param check - флаг проверка это или реальный расчет
 */
def writeResultsToRowOrCheck(def results, def row, def check) {
    def isValid = true
    results.keySet().each { col ->
        if (!check) {
            row.put (col, results.get(col))
        } else {
            if (results[col] != row[col]){
                isValid = false
                logger.error("Итоговые значения рассчитаны неверно!")
            }
        }
    }
    return isValid
}

/**
 * Проверки, которые должны выполняться только для экземпляра ручного ввода  (т.е. при нажатии на кнопку «Рассчитать»)
 */
boolean beforeCalcChecks() {
    boolean isValid = true
    for (def row : rows) {
        if (!isFixed(row) && !checkRequiredColumns(row, requiredCols)){
            isValid = false
        }
    }
    return isValid
}

/**
 * получаем значение для графы 19
 */
def getGraph19(def values, def row) {
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def date = format.parse('01.01.2010')
    if(values.acquisitionDate < date) {
        return null // не заполняется
    } else {
        return row.marketPriceInPerc //ручной ввод
    }
}

/**
 * получаем значение для графы 20
 */
def getGraph20(def values, def row) {
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def date = format.parse('01.01.2010')
    if(values.acquisitionDate < date){
        return values.costWithoutNKD
    } else {
        return row.marketPriceInRub //ручной ввод
    }

}

/**
 * получаем значение для графы 21
 */
def getGraph21(def row) {
    return (row.costWithoutNKD > row.marketPriceInRub) ? row.marketPriceInRub : row.costWithoutNKD
}

/**
 * получаем значение для графы 27
 */
def getGraph27(def values, def row) {
    if (values.operationType.equals('Погашение')) {        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
        values.marketPriceRealizationInPerc = 100
    } else {
        return row.marketPriceRealizationInPerc
    }
}

/**
 * получаем значение для графы 28
 */
def getGraph28(def values, def row) {
    if (values.operationType.equals('Погашение')) {        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
        values.marketPriceRealizationInRub = values.repaymentWithoutNKD
    } else {
        return row.marketPriceRealizationInRub
    }
}

/**
 * получаем значение для графы 29   //todo (vsergeev) после ответа на http://jira.aplana.com/browse/SBRFACCTAX-2522 перепроверить алгоритм
 */
def getGraph29(def row) {
    final signContractorIs4 = row.signContractor.equals('4')        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
    final signContractorIs5 = row.signContractor.equals('5')        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
    if (signContractorIs4 && (isBargain() || isNegotiatedDeal())
            && row.realizationPriceInPerc >= row.marketPriceRealizationInPerc
            && row.realizationPriceInRub >=  row.marketPriceRealizationInRub
            || signContractorIs5
            || row.realizationPriceInPerc >= row.marketPriceRealizationInPerc
            && row.costRealization >= row.marketPriceRealizationInRub
    ) {
        return row.realizationPriceInRub
    } else if (row.operationType.equals('Погашение') && row.signContractor.equals('3')) {        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
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

/**
 * получаем значение графы 31
 */
def getGraph31(def row) {
    return row.costAcquisition + row.loss + row.lossRealization
}

/**
 * получаем значение графы 32
 */
def getGraph32(def values, def row) {
    return !isDiscountBond(values)? null: row.averageWeightedPrice
}

/**
 * получаем значение графы 33
 */
def getGraph33(def values, def row) {
    return !isDiscountBond(values)? null: row.termIssue
}

/**
 * получаем значение графы 34
 */
def getGraph34(def row) {
    return  (isDiscountBond(row)) ? row.realizationDate - row.acquisitionDate : null
}

/**
 * получаем значение графы 35
 */
def getGraph35(def row) {
    if (isDiscountBond(row)) {
        return (row.nominal - row.averageWeightedPrice) * row.termHold * row.amount / row.termIssue
    } else {
        return null
    }
}

/**
 * получаем значение графы 36
 */
def getGraph36(def row) {
    if (isRubleCurrency(row.currencyCode)) {
        return row.interestIncomeCurrency
    } else if (! isDiscountBond(row)) {
        return null
    }
    return row.interestIncomeCurrency * getCourse(row.currencyCode,row.realizationDate)
}

/**
 * получаем значение графы 37
 */
def getGraph37(def row) {
    if (isDiscountBond(row)) {
        return row.costRealization - row.totalLoss - row.interestIncomeInRub
    } else if (isCouponBound(row)) {
        return row.costRealization - row.totalLoss
    }
}

/**
 * получаем значение для графы 38
 */
def getGraph38(def row) {
    logger.info("row = $row")
    if (row.realizationPriceInRub > 0) {
        return row.costRealization - row.realizationPriceInRub
    } else if (row.realizationPriceInRub == 0 && row.repaymentWithoutNKD > 0) {
        return row.costRealization - row.repaymentWithoutNKD
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
    row.securityKind.equals('2')        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
}

/**
 * определяем, является ли облигация купонной
 * @return Если «графа 7» == «1» || «графа 7» == «3» тогда {@value true} иначе {@value false}
 */
boolean isCouponBound(def row) {
    row.securityKind.equals('1') || row.securityKind.equals('3')        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
}

boolean isBlankOrNull(value) {
    value == null || value.equals('')
}

/**
 * Возвращает алиасы столбцов, значения которых суммируются в итогах
 */
def getTotalCols() {
    return ['amount', 'costWithoutNKD', 'loss', 'marketPriceInRub', 'costAcquisition', 'repaymentWithoutNKD',
            'realizationPriceInRub', 'marketPriceRealizationInRub', 'costRealization', 'lossRealization', 'totalLoss',
            'interestIncomeCurrency', 'interestIncomeInRub', 'realizationResult', 'excessSellingPrice']
}

/**
 * алиасы столбцов, доступных для редактирования
 */
def getEditableCols() {
    return ['balanceNumber', 'operationType', 'signContractor', 'contractorName', 'securityName', 'series',
            'securityKind', 'signSecurity', 'currencyCode', 'currencyName', 'nominal', 'amount', 'acquisitionDate',
            'tradeDate', 'currencyCodeTrade', 'currencyNameTrade', 'costWithoutNKD', 'loss', 'marketPriceInPerc',
            'marketPriceInRub', 'realizationDate', 'tradeDate2', 'repaymentWithoutNKD', 'realizationPriceInPerc',
            'realizationPriceInRub', 'marketPriceRealizationInPerc', 'marketPriceRealizationInRub', 'lossRealization']
}

def getAllCols(){
    return ['balanceNumber', 'operationType', 'signContractor', 'contractorName', 'securityName', 'series',
            'securityKind', 'signSecurity', 'currencyCode', 'currencyName', 'nominal', 'amount', 'acquisitionDate',
            'tradeDate', 'currencyCodeTrade', 'currencyNameTrade', 'costWithoutNKD', 'loss', 'marketPriceInPerc',
            'marketPriceInRub', 'costAcquisition', 'realizationDate', 'tradeDate2', 'repaymentWithoutNKD',
            'realizationPriceInPerc', 'realizationPriceInRub', 'marketPriceRealizationInPerc',
            'marketPriceRealizationInRub', 'costRealization', 'lossRealization', 'totalLoss', 'averageWeightedPrice',
            'termIssue', 'termHold', 'interestIncomeCurrency', 'interestIncomeInRub', 'realizationResult',
            'excessSellingPrice']
}

def getRequiredCols(){
    return ['balanceNumber', 'operationType', 'signContractor', 'contractorName', 'securityName', 'series',
            'securityKind', 'signSecurity', 'currencyCode', 'currencyName', 'nominal', 'amount', 'acquisitionDate',
            'tradeDate', 'currencyCodeTrade', 'currencyNameTrade', 'costWithoutNKD', 'loss', 'marketPriceInPerc',
            'marketPriceInRub', 'costAcquisition', 'realizationDate', 'tradeDate2', 'repaymentWithoutNKD',
            'realizationPriceInPerc', 'realizationPriceInRub', 'marketPriceRealizationInPerc',
            'marketPriceRealizationInRub', 'costRealization', 'lossRealization', 'totalLoss', 'averageWeightedPrice',
            'termIssue', 'termHold', 'interestIncomeCurrency', 'interestIncomeInRub', 'realizationResult',
            'excessSellingPrice']
}

def List<String> getGroups(){
    return ['R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9']
}

/**
 * Хелпер для округления чисел
 * @param value
 * @param newScale
 * @return
 */
BigDecimal roundTo(BigDecimal value, int newScale) {
    if (value != null) {
        return value.setScale(newScale, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

/**
 * Получить helper формы.
 */
def DataRowHelper getData() {
    return getData(formData)
}

def DataRowHelper getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

/**
 * Получить строки формы.
 */
def List<DataRow<Cell>> getRows() {
    return data.getAllCached();
}

def List<DataRow<Cell>> getRows(def DataRowHelper data) {
    return data.getAllCached();
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Проверка валюты на рубли
 */
def isRubleCurrency(def currencyCode) {
    return  refBookService.getStringValue(15,currencyCode,'CODE')=='810'
}

/**
 * Проверка является ли строка фиксированной.
 */
def isFixed(def row) {
    return row != null && row.getAlias() != null
}

/**
 * Получить курс валюты
 */
def getCourse(def currency, def date) {
    if (currency!=null) {
        def refCourseDataProvider = refBookFactory.getDataProvider(22)
        def res = refCourseDataProvider.getRecords(date, null, 'CODE_NUMBER='+currency, null);
        return res.getRecords().get(0).RATE.getNumberValue()
    } else {
        return null
    }
}

def getColumnName(def row, def column){
    return row.getCell(column).getColumn().getName().replace('%', '%%')
}

def getBalanceNumber(def id) {
    return refBookService.getStringValue(29, id, 'BALANCE_ACCOUNT')
}

def getSignSecurity(def id) {
    return refBookService.getStringValue(62, id, 'CODE')
}

def getCompareList(DataRow row) {//TODO справочники
    return [getBalanceNumber(row.balanceNumber),
        getSignSecurity(row.signSecurity),
        row.securityKind,
        row.signContractor,
        row.operationType]
}

/**
 * Получить id справочника.
 *
 * @param ref_id идентификатор справончика
 * @param code атрибут справочника
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return
 */
def getRecordId(def ref_id, String code, def value, Date date, def cache) {
    String filter = code + " = '" + value + "'"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter] != null) {
            return cache[ref_id][filter]
        }
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    // logger.error("Не удалось найти запись в справочнике (id=$ref_id) с атрибутом $code равным $value!")
    return null
}

/**
 * Получить сообщение об ошибке  при проверке НСИ
 * @param errStart начало сообщения с номером строки
 * @param ref_id ид справочника
 * @param atr_alias алиас атрибута
 * @param id ид записи (значение в поле)
 */
void getRefBookErrorMessage(def errStart, def ref_id, def atr_alias, def id){
    def refBook = refBookFactory.get(ref_id)
    def refBookAtr = refBook.getAttribute(atr_alias)
    logger.warn("${errStart}в справочнике \"${refBook.name}\" не найдено значение с id = ${id} в поле \"${refBookAtr.name}\"!")
}