/**
 * Скрипт для Ф-7.8 (f7_8.groovy).
 * (Ф 7.8) Реестр совершенных операций с ценными бумагами по продаже и погашению, а также по открытию-закрытию
 * короткой позиции
 *
 * Версия ЧТЗ: 57
 *
 * TODO:
 *
 * @author vsergeev
 *
 * Графы:
 * 1    balanceNumber                   Номер баланс. счёта
 * 2    operationType                   Вид операции (продажа, погашение, открытие \закрытие короткой позиции)
 * 3    signContractor                  Признак контрагента: 3 - эмитент ценной бумаги, 4 - организатор торговли,
 *                                      5 - прочие
 * 4    contractorName                  Наименование контрагента
 * 5    securityName                    Наименование ценной бумаги (включая наименование эмитента)
 * 6    series                          Серия (выпуск)
 * 7    securityKind                    Вид ценной бумаги: 1 - купонная облигация, 2 - дисконтная облигация, 3 - акция
 * 8    signSecurity                    Признак ценной бумаги: «+» - обращающаяся на ОРЦБ; «-» - необращающаяся на ОРЦБ
 * 9    currencyCode                    Код валюты бумаги (номинала)
 * 10   currencyName                    Наименование валюты бумаги (номинала)
 * 11   nominal                         Номинал одной бумаги (ед. вал.)
 * 12   amount                          Количество ценных бумаг (шт.)
 * 13   acquisitionDate                 Дата приобретения, закрытия короткой позиции
 * 14   tradeDate                       Дата совершения сделки
 * 15   currencyCodeTrade               Код валюты расчётов по сделке
 * 16   currencyNameTrade               Наименование валюты расчётов по сделке
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
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        if (beforeCalcChecks()) {
//            calcDataRows()        //todo (vsergeev) раскомментировать и переделать логику в соответствии с ответом на http://jira.aplana.com/browse/SBRFACCTAX-2531
            calcTotalDataRows()
            calcTotalForMonth()
//            calcTotalForTaxPeriod()       //todo (vsergeev) раскомментировать и доделать
        }
        break
    case FormDataEvent.ADD_ROW :
        addNewRowAction()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

void deleteRow() {
    if (isDeletable(currentDataRow)) {
        formData.deleteDataRow(currentDataRow)
    } else {
        logger.error('Выделенную строку  нельзя удалить!')
    }
}

void addNewRowAction() {
    if (currentDataRow == null) {
        logger.error ('Сперва выделите любую ячейку из строк группы, в которую следует добавить строку')
        return
    }
    def group = getDataRowGroup(currentDataRow)
    def totalRow = tryGetTotalRowForGroup(group)
    if (totalRow != null) {
        def totalRowIndex = formData.getDataRowIndex(totalRow.getAlias())
        def newRowNumber = Random.newInstance().nextInt(Integer.MAX_VALUE)      //задаем границы, исключая отрицательные значения - знак "минус" ломает поиск по регулярке
        def newRow = formData.appendDataRow(totalRowIndex, "R$group-$newRowNumber")
        getAliasesForEditableCols().each {
            newRow.getCell(it).editable = true
        }
    } else {
        logger.error('В выделенную группу нельзя добавлять новые строки!')
    }
}

boolean logicalChecks() {
    boolean isValid = true

    //проверка на обязательность заполнения полей (предупреждение)
    isRequiredColsFilled()
    for (def dataRow : formData.getDataRows()) {

        def tmpMarketPriceRealizationInPerc = getGetMarketPriceRealizationInPerc(dataRow)
        if (dataRow.marketPriceRealizationInPerc == null ||
                tmpMarketPriceRealizationInPerc == null &&
                ( !tmpMarketPriceRealizationInPerc.equals(dataRow.marketPriceRealizationInPerc))) {
            isValid = false
            logger.error('Неверно указана рыночная цена в процентах при погашении!')
        }

   }

    return isValid
}

/**
 * Проверяет
 * 1.  Обязательность заполнения поля графы (с 1 по 4)
 * @return {@value true} если все обязательные поля заполнены, иначе {@value false}
 */
boolean isRequiredColsFilled() {
    for (def dataRow : formData.getDataRows) {
        def fieldName = ""                                      //todo (vsergeev) откуда брать наименование поля? в вопросах Аванесову п.4
        for (def colName : dataRow.keySet()) {
            if (isBlankOrNull(dataRow.get(colName))) {
                logger.warn("Поле $fieldName не заполнено!")
                break
            }
        }
    }
}

/**
 * рассчитываем вычисляемые поля для строк с данными, введенными пользователем
 */
def calcDataRows() {
    tryGetAllDataRowsWithData().each { dataRow ->
        dataRow.costAcquisition = getCostAcquisition(dataRow)
        dataRow.marketPriceRealizationInPerc = getGetMarketPriceRealizationInPerc(dataRow)
        dataRow.marketPriceRealizationInRub = getMarketPriceRealizationInRub(dataRow)
        dataRow.costRealization = getCostRealization(dataRow)
        dataRow.totalLoss = getTotalLoss(dataRow)
        dataRow.averageWeightedPrice = getAverageWeightedPrice(dataRow)
        dataRow.termIssue = getTermIssue(dataRow)
        dataRow.termHold = getTermHold(dataRow)
        dataRow.interestIncomeCurrency = getInterestIncomeCurrency(dataRow)
        dataRow.interestIncomeInRub = getInterestIncomeInRub(dataRow)
        dataRow.realizationResult = getRealizationResult(dataRow)
        dataRow.excessSellingPrice = getExcessSellingPrice(dataRow)
    }
}

/**
 * рассчитываем вычисляемые поля для строк ИТОГО
 */
void calcTotalDataRows() {
    def result = [:]

    getExistingGroups().each { group ->
        def totalRow = tryGetTotalRowForGroup(group)
        if (totalRow != null) {
            def dataRows = tryGetDataRowsForGroup(group)
            result.put(totalRow, dataRows)
        }
    }

   result.keySet().each { totalRow ->
        writeResultsToRow(calcTotalResultsForRows(result.get(totalRow)), totalRow)
    }

}

/**
 * расчитываем значения для строки "Всего за текущий месяц"
 */
void calcTotalForMonth() {      //todo (vsergeev) возможно, нужно будет переписать логику в соответствии с ответом на http://jira.aplana.com/browse/SBRFACCTAX-2534
    def totalRows = []

    getExistingGroups().each { group ->
        def totalRow = tryGetTotalRowForGroup(group)
        if (totalRow != null) {
            totalRows.add(totalRow)
        }
    }

    def totalForMonthRow = formData.getDataRow('R10')
    writeResultsToRow(calcTotalResultsForRows(totalRows), totalForMonthRow)
}

/**
 * рассчитываем значения для строки "Всего за текущий налоговый период"
 */
void calcTotalForTaxPeriod() {               //todo (vsergeev) отладить
    def reportPeriodId = formData.getReportPeriodId()
    def reportPeriod = reportPeriodService.get(reportPeriodId)
    def reportPeriodsInTax = reportPeriodService.listByTaxPeriod(reportPeriod.getTaxPeriodId())

    def formDatas = []
    for (def period : reportPeriodsInTax) {
        if (! period.getId().equals(reportPeriod))
        def formDataFromAnotherPeriod =
            formDataService.find(formData.getFormType(), formData.getKind(), formData.getDepartmentId(), period.getId())
        if (formDataFromAnotherPeriod != null) {
            formDatas.add(formDataFromAnotherPeriod)
        }
    }

    def totalForMonthRows = []
    formDatas.each { fData ->
        fData.getDataRow('R10')
    }

    totalForMonthRows.add(formData.getDataRow('R10'))

    def totalForTaxPeriodRow = formData.getDataRow('R11')
    writeResultsToRow(calcTotalResultsForRows(totalForMonthRows), totalForTaxPeriodRow)
}

/**
 * принимает на вход List строк, для которых нужно посчитать итоговые значения
 * возвращает мапу вида <имя_колонки : значение колонки>
 */
def calcTotalResultsForRows(def dataRowsList) {
    def totalResults = [:]
    getAliasesForTotalCols().each { col ->
        totalResults.put(col, new BigDecimal(0))
    }

    for (def dataRow : dataRowsList) {
        totalResults.keySet().each { col ->
            final cellValue = dataRow.get(col)
            if (cellValue != null) {
                totalResults.put(col, totalResults.get(col) + cellValue)
            }
        }
    }

    logger.warn(totalResults.toString())

    return totalResults
}

/**
 * Заносим подсчитанные итоговые значения из мапы в выбранную строку
 *
 * @param results - мапа с подсчитанными итоговыми значениями для строк.
 *                  для ее получения есть метод calcTotalResultsForRows
 * @param dataRow - строка, в которую нужно записать итоговые значения из мапы
 */
void writeResultsToRow(def results, def dataRow) {
    results.keySet().each { col ->
        dataRow.put (col, results.get(col))
    }
}

/**
 * Проверки, которые должны выполняться только для экземпляра ручного ввода  (т.е. при нажатии на кнопку «Рассчитать»)
 */
boolean beforeCalcChecks() {
    boolean isValid = true
    for (def dataRow : formData.getDataRows()) {

        if (! dataRow.signContractor.equals('5'))       //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
            logger.warn('графа 3 не равна 5')           //todo (vsergeev) нужен ли идентификатор строки? вопросы Аванесову № 5
    }

    return isValid
}

/**
 * получаем значение для графы 21
 */
def getCostAcquisition(def dataRow) {
    return (dataRow.costWithoutNKD > dataRow.marketPriceInRub) ? dataRow.marketPriceInRub : dataRow.costWithoutNKD
}

/**
 * получаем значение для графы 27
 */
def getGetMarketPriceRealizationInPerc(def dataRow) {
    if (dataRow.operationType.equals('Погашение')) {        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
        dataRow.marketPriceRealizationInPerc = 100
    } else {
        return null                                         //todo (vsergeev) http://jira.aplana.com/browse/SBRFACCTAX-2521
    }
}

/**
 * получаем значение для графы 28
 */
def getMarketPriceRealizationInRub(def dataRow) {
    if (dataRow.operationType.equals('Погашение')) {        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
        dataRow.marketPriceRealizationInRub = dataRow.repaymentWithoutNKD
    } else {
        return null                                         //todo (vsergeev) http://jira.aplana.com/browse/SBRFACCTAX-2521
    }
}

/**
 * получаем значение для графы 29   //todo (vsergeev) после ответа на http://jira.aplana.com/browse/SBRFACCTAX-2522 перепроверить алгоритм
 */
def getCostRealization(def dataRow) {
    final signContractorIs4 = dataRow.signContractor.equals('4')        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
    final signContractorIs5 = dataRow.signContractor.equals('5')        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
    if (signContractorIs4 && (isBargain() || isNegotiatedDeal())
            && dataRow.realizationPriceInPerc >= dataRow.marketPriceRealizationInPerc
            && dataRow.realizationPriceInRub >=  dataRow.marketPriceRealizationInRub
            || signContractorIs5
            || dataRow.realizationPriceInPerc >= dataRow.marketPriceRealizationInPerc
            && dataRow.costRealization >= dataRow.marketPriceRealizationInRub
    ) {
        return dataRow.realizationPriceInRub
    } else if (dataRow.operationType.equals('Погашение') && dataRow.signContractor.equals('3')) {        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
        return dataRow.repaymentWithoutNKD
    } else if (signContractorIs4 && isNegotiatedDeal()
            || signContractorIs5) {
        return dataRow.marketPriceRealizationInRub
    }
}

/**
 * получаем значение графы 31
 */
def getTotalLoss(def dataRow) {
    return dataRow.costAcquisition + dataRow.loss + dataRow.lossRealization
}

/**
 * получаем значение графы 32
 */
def getAverageWeightedPrice(def dataRow) {
    if (! isDiscountBond(dataRow)) {
        return null
    }
}

/**
 * получаем значение графы 33
 */
def getTermIssue(def dataRow) {
    if (! isDiscountBond(dataRow)) {
        return null
    }
}

/**
 * получаем значение графы 34
 */
def getTermHold(def dataRow) {
    return  (isDiscountBond(dataRow)) ? dataRow.realizationDate - dataRow.acquisitionDate : null
}

/**
 * получаем значение графы 35
 */
def getInterestIncomeCurrency(def dataRow) {
    if (isDiscountBond(dataRow)) {
        return (dataRow.nominal - dataRow.averageWeightedPrice) * dataRow.termHold * dataRow.amount / dataRow.termIssue
    } else {
        return null
    }
}

/**
 * получаем значение графы 36
 */
def getInterestIncomeInRub(def dataRow) {
    if (dataRow.currencyCode.equals('RUR')) {       //todo (vsergeev) Справочник валют!
        return dataRow.interestIncomeCurrency
    } else if (! isDiscountBond(dataRow)) {
        return null
    }
    return dataRow.interestIncomeCurrency * 0      //todo (vsergeev) http://jira.aplana.com/browse/SBRFACCTAX-2510
}

/**
 * получаем значение графы 37
 */
def getRealizationResult(def dataRow) {
    if (isDiscountBond(dataRow)) {
        return dataRow.costRealization - dataRow.totalLoss - dataRow.interestIncomeInRub
    } else if (isCouponBound(dataRow)) {
        return dataRow.costRealization - dataRow.totalLoss
    }
}

/**
 * получаем значение для графы 38
 */
def getExcessSellingPrice(def dataRow) {
    if (dataRow.realizationPriceInRub > 0) {
        return dataRow.costRealization - dataRow.realizationPriceInRub
    } else if (dataRow.realizationPriceInRub == 0) {
        return dataRow.costRealization - dataRow.repaymentWithoutNKD
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
boolean isDiscountBond(def dataRow) {
    dataRow.securityKind.equals('2')        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
}

/**
 * определяем, является ли облигация купонной
 * @return Если «графа 7» == «1» || «графа 7» == «3» тогда {@value true} иначе {@value false}
 */
boolean isCouponBound(def dataRow) {
    dataRow.securityKind.equals('1') || dataRow.securityKind.equals('3')        //todo (vsergeev) нужен справочник!!! http://jira.aplana.com/browse/SBRFACCTAX-2504
}

boolean isBlankOrNull(value) {
    value == null || value.equals('')
}

/**
 * получаем номер группы для строки. по номерам группируются строки, для каждой группы итоги подсчитываются отдельно
 */
def getDataRowGroup(def dataRow) {
    def currentRowAlias = dataRow.getAlias()
    def matcher = (currentRowAlias =~ '^R(\\d+)')

    return (matcher.find(0) && matcher[0].size > 1) ? matcher[0][1] : null
}

/**
 * получаем список всех строк в группе, которые содержат данные, по номеру группы
 */
def tryGetDataRowsForGroup(def aliasGroup) {
    def pattern = '^R' + aliasGroup + '-\\d+'
    def result = []
    formData.getDataRows().each { dataRow ->
        if ((dataRow.getAlias() =~ pattern).matches()) {
            result.add(dataRow)
        }
    }

    return (result.size() > 0) ? result : null
}

/**
 * получаем список всех строк с данными
 */
def tryGetAllDataRowsWithData() {
    return tryGetDataRowsForGroup('\\d+')
}

def getLastRowAlias(def aliasGroup, def rowsInGroup) {
    def pattern = '^R' + aliasGroup + '-(\\d+)'
    def maxIndex = -1
    def rowWithMaxIndexAlias
    rowsInGroup.each { rowAlias ->
        def matcher = (rowAlias =~ pattern)
        if (matcher.find(0) && matcher[0].size > 1) {
            try {
                rowIndex = Integer.parseInt(matcher[0][1])
                if (maxIndex < rowIndex) {
                    rowWithMaxIndexAlias = rowAlias
                    maxIndex = rowIndex
                }
            } catch (NumberFormatException ex) {
                // do nothing
            }
        }
    }

    return rowWithMaxIndexAlias
}

/**
 * получаем номер строку итогов для группы
 */
def tryGetTotalRowForGroup(def groupIndex) {
    if (groupIndex.equals('10') || groupIndex.equals('11')) {       //в 10 и 11 группы нельзя добавлять ячейки
        return null
    } else {
        return formData.getDataRow("R$groupIndex-total")
    }
}

/**
 * проверяем, можно ли удалить строку
 * для удаления доступны только строки группы, у которых есть номер (не итоговые и не строки с названием групп)
 */
boolean isDeletable(def dataRow) {
    def pattern = '^R\\d+-\\d+'
    if ((dataRow.getAlias() =~ pattern).matches()) {
        return true
    } else {
        return false
    }
}

/**
 * Возвращает алиасы столбцов, значения которых суммируются в итогах
 */
def getAliasesForTotalCols() {
    return ['amount', 'costWithoutNKD', 'loss', 'marketPriceInRub', 'costAcquisition', 'repaymentWithoutNKD',
            'realizationPriceInRub', 'marketPriceRealizationInRub', 'costRealization', 'lossRealization', 'totalLoss',
            'interestIncomeCurrency', 'interestIncomeInRub', 'realizationResult', 'excessSellingPrice']
}

/**
 * алиасы стольбцов, доступных для редактирования
 */
def getAliasesForEditableCols() {
    return ['balanceNumber', 'operationType', 'signContractor', 'contractorName', 'securityName', 'series',
            'securityKind', 'signSecurity', 'currencyCode', 'currencyName', 'nominal', 'amount', 'acquisitionDate',
            'tradeDate', 'currencyCodeTrade', 'currencyNameTrade', 'costWithoutNKD', 'loss', 'marketPriceInPerc',
            'marketPriceInRub', 'realizationDate', 'tradeDate2', 'repaymentWithoutNKD', 'realizationPriceInPerc',
            'realizationPriceInRub', 'marketPriceRealizationInPerc', 'marketPriceRealizationInRub', 'lossRealization']
}

/**
 * @return List всех существующих в таблице групп
 */
def getExistingGroups() {

    def groups = []
    def group

    for (def dataRow : formData.getDataRows()) {
        final currentGroup = getDataRowGroup(dataRow)
        if (! currentGroup.equals(group)) {
            group = currentGroup
            groups.add(group)
        }
    }

    return groups
}