package form_template.income.outcome_complex.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "Сводная форма начисленных расходов (расходы сложные)"
 * formTypeId=303
 *
 * http://conf.aplana.com/pages/viewpage.action?pageId=8784122
 *
 * @author Stanislav Yasinskiy
 *
 * графа  1 - consumptionTypeId
 * графа  2 - consumptionGroup
 * графа  3 - consumptionTypeByOperation
 * графа  4 - consumptionBuhSumAccountNumber
 * графа  5 - consumptionBuhSumRnuSource
 * графа  6 - consumptionBuhSumAccepted
 * графа  7 - consumptionBuhSumPrevTaxPeriod
 * графа  8 - consumptionTaxSumRnuSource
 * графа  9 - consumptionTaxSumS
 * графа 10 - rnuNo
 * графа 11 - logicalCheck
 * графа 12 - accountingRecords
 * графа 13 - opuSumByEnclosure3
 * графа 14 - opuSumByTableP
 * графа 15 - opuSumTotal
 * графа 16 - difference
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        if (formData.kind != FormDataKind.SUMMARY) {
            logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
        }
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calcTotal(null)
        break
    case FormDataEvent.IMPORT:
        importData()
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS']

//Аттрибуты, очищаемые перед импортом формы
@Field
def resetColumns = ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS', 'logicalCheck',
        'opuSumByEnclosure3', 'opuSumByTableP', 'opuSumTotal', 'difference']

@Field
def totalColumn = 'consumptionTaxSumS'

@Field
def rowsCalc = ['R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10', 'R11', 'R12', 'R13', 'R14', 'R15', 'R16', 'R17', 'R1',
        'R26', 'R27', 'R28', 'R29', 'R30', 'R31', 'R32', 'R70', 'R71']

@Field
def notImportSum = ['R1', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R12', 'R13', 'R15', 'R16', 'R17', 'R27', 'R29',
        'R67', 'R68', 'R71']

// справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
@Field
def rbIncome102 = null

@Field
def editableStyle = 'Редактирование (светло-голубой)'

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

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def message = 'ТРЕБУЕТСЯ ОБЪЯСНЕНИЕ'
    def tmp
    def value
    def formDataSimple = getFormDataSimple()
    def income102NotFound = []
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (def row : dataRows) {
        // исключить итоговые строки
        if (row.getAlias() in ['R67', 'R90']) {
            continue
        }
        if (!isEmpty(row.consumptionTaxSumS) && !isEmpty(row.consumptionBuhSumAccepted) &&
                !isEmpty(row.consumptionBuhSumPrevTaxPeriod)) {
            // ОКРУГЛ( «графа9»-(Сумма 6-Сумма 7);2),
            sum6 = 0
            sum7 = 0
            for (rowSum in dataRows) {
                if (rowSum.getCell('consumptionBuhSumAccepted')?.style?.alias != editableStyle
                        || !rowSum.getCell('consumptionBuhSumPrevTaxPeriod')) {
                    continue
                }
                String knySum
                String kny
                if (rowSum.getCell('consumptionTypeId').hasValueOwner()) {
                    knySum = rowSum.getCell('consumptionTypeId').valueOwner.value
                } else {
                    knySum = rowSum.getCell('consumptionTypeId').value
                }
                if (row.getCell('consumptionTypeId').hasValueOwner()) {
                    kny = row.getCell('consumptionTypeId').valueOwner.value
                } else {
                    kny = row.getCell('consumptionTypeId').value
                }
                if (kny == knySum) {
                    sum6 += (rowSum.consumptionBuhSumAccepted ?: 0)
                    sum7 += (rowSum.consumptionBuhSumPrevTaxPeriod ?: 0)
                }
            }
            tmp = round(row.consumptionTaxSumS - (sum6 - sum7), 2)
            value = ((BigDecimal) tmp).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = ((tmp < 0) ? message : value.toString())
        }

        if (!isEmpty(row.consumptionBuhSumAccepted) && !isEmpty(row.consumptionBuhSumPrevTaxPeriod)) {
            // графа 13
            if (row.getAlias() in ['R3', 'R11']) {
                tmp = calcColumn6(dataRows, ['R3', 'R11'])
            } else {
                tmp = row.consumptionBuhSumAccepted
            }
            row.opuSumByEnclosure3 = tmp

            // графа 14
            row.opuSumByTableP = getSumFromSimple(formDataSimple, 'consumptionAccountNumber',
                    'rnu5Field5Accepted', row.consumptionBuhSumAccountNumber)

            // графа 15
            // получить отчет о прибылях и убытках
            def income102Records = getIncome102Data(row)
            row.opuSumTotal = 0
            if (income102Records == null || income102Records.isEmpty()) {
                income102NotFound += getIndex(row)
            }
            for (income102 in income102Records) {
                row.opuSumTotal += income102.TOTAL_SUM.numberValue
            }

            // графа 16
            row.difference = (getValue(row.opuSumByEnclosure3) + getValue(row.opuSumByTableP)) - getValue(row.opuSumTotal)
        }
    }

    if (!income102NotFound.isEmpty()) {
        def rows = income102NotFound.join(', ')
        logger.warn("Не найдены соответствующие данные в отчете о прибылях и убытках для строк: $rows")
    }

    calcTotal(dataRows)
    dataRowHelper.save(dataRows)
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def rowIndexes102 = []
    for (def row in dataRows) {
        if (rowsCalc.contains(row.getAlias())) {
            final income102Data = getIncome102Data(row)
            if (!income102Data || income102Data.isEmpty()) {
                rowIndexes102 += row.getIndex()
            }
        }
    }
    if (!rowIndexes102.isEmpty()) {
        logger.warn("Cтроки ${rowIndexes102.join(', ')}: Отсутствуют данные бухгалтерской отчетности в форме \"Отчет о прибылях и убытках\"")
    }
    for (def row in dataRows) {
        if (rowsCalc.contains(row.getAlias())) {
            // Проверка обязательных полей
            checkRequiredColumns(row, nonEmptyColumns)
        }
    }
    checkTotalSum(getDataRow(dataRows, 'R67'), getSum(dataRows, totalColumn, 'R2', 'R66'))
    checkTotalSum(getDataRow(dataRows, 'R90'), getSum(dataRows, totalColumn, 'R69', 'R89'))
}

/**
 * Расчет итоговых строк.
 */
void calcTotal(def rows) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = rows ?: dataRowHelper.getAllCached()
    def totalRow1 = getDataRow(dataRows, 'R67')
    def totalRow2 = getDataRow(dataRows, 'R90')

    // суммы для графы 9
    totalRow1.getCell(totalColumn).setValue(getSum(dataRows, totalColumn, 'R2', 'R66'), totalRow1.getIndex())
    totalRow2.getCell(totalColumn).setValue(getSum(dataRows, totalColumn, 'R69', 'R89'), totalRow2.getIndex())
}

// Консолидация формы
def consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    isBank() ? consolidationBank(dataRows) : consolidationSummary(dataRows)
    dataRowHelper.update(dataRows)
}

void consolidationBank(def dataRows) {
    if (dataRows == null || dataRows.isEmpty()) {
        return
    }
    // очистить форму
    dataRows.each { row ->
        ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle) {
                row[alias] = 0
            }
        }
        // графа 11, 13..16
        ['logicalCheck', 'opuSumByEnclosure3', 'opuSumByTableP', 'opuSumTotal', 'difference'].each { alias ->
            row[alias] = null
        }
        if (row.getAlias() in ['R67', 'R90']) {
            row.consumptionTaxSumS = 0
        }
    }

    // получить консолидированные формы из источников
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        def child = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == formData.formType.id) {
            for (def row : formDataService.getDataRowHelper(child).getAllCached()) {
                if (row.getAlias() == null) {
                    continue
                }
                def rowResult = getDataRow(dataRows, row.getAlias())
                ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each {
                    if (row.getCell(it).getValue() != null && !row.getCell(it).hasValueOwner()) {
                        rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)), rowResult.getIndex())
                    }
                }
            }
        }
    }
}

// Консолидация из первичек
void consolidationSummary(def dataRows) {
    if (dataRows == null || dataRows.isEmpty()) {
        return
    }
    // очистить форму
    dataRows.each { row ->
        ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each { alias ->
            if (row.getCell(alias)?.style?.alias != editableStyle) {
                row[alias] = 0
            }
        }
        // графа 11, 13..16
        ['logicalCheck', 'opuSumByEnclosure3', 'opuSumByTableP', 'opuSumTotal', 'difference'].each { alias ->
            row[alias] = null
        }
        if (row.getAlias() in ['R67', 'R90']) {
            row.consumptionTaxSumS = 0
        }
    }
    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    // Предыдущий отчётный период
    def formDataOld = formDataService.getFormDataPrev(formData, formData.departmentId)
    if (formDataOld != null && reportPeriod.order != 1) {
        def dataRowsOld = formDataService.getDataRowHelper(formDataOld)?.getAll()
        //графа 6
        prevList = ((14..17) + (26..32) + [70, 71])
        addPrevValue(prevList, dataRows, 'consumptionBuhSumAccepted', dataRowsOld, 'consumptionBuhSumAccepted')
        //графа 7
        prevList = ((2..17) + (26..32) + [70, 71])
        addPrevValue(prevList, dataRows, 'consumptionBuhSumPrevTaxPeriod', dataRowsOld, 'consumptionBuhSumAccepted')
        //графа 9
        prevList = ([14] + (18..26) + [28] + (30..46) + (49..66) + [69, 70, 72] + (76..88) + [90, 91])
        addPrevValue(prevList, dataRows, 'consumptionBuhSumPrevTaxPeriod', dataRowsOld, 'consumptionBuhSumAccepted')
    }
    // получить формы-источники в текущем налоговом периоде
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        def isMonth = it.formTypeId in [332] //ежемесячная
        def children = []
        if (isMonth) {
            for (def periodOrder = 3 * reportPeriod.order - 2; periodOrder < 3 * reportPeriod.order + 1; periodOrder++) {
                def child = formDataService.findMonth(it.formTypeId, it.kind, it.departmentId, reportPeriod.taxPeriod.id, periodOrder)
                children.add(child)
            }
        } else {
            children.add(formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId))
        }
        for (def child in children) {
            if (child != null) {
                def dataRowsChild = formDataService.getDataRowHelper(child)?.allCached
                switch (child.formType.id) {
                    case 343: //(РНУ-48.1) Регистр налогового учёта «ведомость ввода в эксплуатацию инвентаря и принадлежностей до 40 000 руб.»
                        break
                    case 313: //(РНУ-48.2) Регистр налогового учёта «Сводная ведомость ввода в эксплуатацию инвентаря и принадлежностей до 40 000 руб.»
                        // графа 6 = сумма граф 3 фикс строк 1 и 2 форм
                        addChildTotalData(dataRows, 2, 'consumptionBuhSumAccepted', dataRowsChild, "R1", 'summ')
                        addChildTotalData(dataRows, 2, 'consumptionBuhSumAccepted', dataRowsChild, "R2", 'summ')
                        //TODO строка 2 графа 9
                        // графа 6 = сумма граф 3 фикс строк 3 форм
                        addChildTotalData(dataRows, 3, 'consumptionBuhSumAccepted', dataRowsChild, "R3", 'summ')
                        break
                    case 364: //(РНУ-12) Регистр налогового учёта расходов по хозяйственным операциям и оказанным Банку услугам

                        // графа 6 = сумма граф 12 строк с условием форм
                        def graph6Sum12Map = [
                                14: '26302.17',
                                15: '26302.19',
                                17: '26412.25',
                                26: '26408',
                                27: '26409',
                                28: '26401.03',
                                29: '27203.39',
                                30: '26304',
                                31: '26406.12',
                                32: '26203'
                        ]
                        graph6Sum12Map.each { key, value ->
                            addRowValue(dataRows, key, 'consumptionBuhSumAccepted',
                                    getRnu12AddValue(dataRowsChild, value, 'outcomeInBuh'))
                        }

                        // графа 9 = сумма граф 11 строк с условием форм
                        def graph9Sum11Map = [
                                14: '26302.17',
                                26: '26408',
                                28: '26401.03',
                                30: '26304',
                                31: '26406.12',
                                32: '26203',
                                33: '21370',
                                34: '21375',
                                35: '21380'
                        ]
                        graph9Sum11Map.each { key, value ->
                            addRowValue(dataRows, key, 'consumptionTaxSumS',
                                    getRnu12AddValue(dataRowsChild, value, 'outcomeInNalog'))
                        }

                        // графа 7 = сумма граф 12 строк с условием форм
                        addRowValue(dataRows, 14, 'consumptionBuhSumPrevTaxPeriod',
                                getRnu12AddValue(dataRowsChild, '26302.17', 'outcomeInBuh'))
                        // графа 6 = сумма граф 11 строк с условием форм
                        addRowValue(dataRows, 16, 'consumptionBuhSumAccepted',
                                getRnu12AddValue(dataRowsChild, '26412.13', 'outcomeInNalog'))

                        break
                    case 344: //(РНУ-47) Регистр налогового учёта «ведомость начисленной амортизации по основным средствам, а также расходов в виде капитальных вложений»
                        // графа 9 = сумма граф 5 всех строк форм
                        (0..16).each {
                            addChildTotalData(dataRows, 18, 'consumptionTaxSumS', dataRowsChild, "R$it", 'amortTaxPeriod')
                        }
                        // графа 9 = сумма граф 3 строки 12 форм
                        addChildTotalData(dataRows, 20, 'consumptionTaxSumS', dataRowsChild, "R11", 'sumTaxPeriodTotal')
                        // графа 9 = сумма граф 3 строки 13 форм
                        addChildTotalData(dataRows, 21, 'consumptionTaxSumS', dataRowsChild, "R12", 'sumTaxPeriodTotal')
                        //TODO строка 22
                        // графа 9 = сумма граф 5 строки 14 форм
                        addChildTotalData(dataRows, 23, 'consumptionTaxSumS', dataRowsChild, "R13", 'amortTaxPeriod')
                        // графа 9 = сумма граф 5 строки 15 форм
                        addChildTotalData(dataRows, 24, 'consumptionTaxSumS', dataRowsChild, "R14", 'amortTaxPeriod')
                        // графа 9 = сумма граф 5 строки 16 форм
                        addChildTotalData(dataRows, 25, 'consumptionTaxSumS', dataRowsChild, "R15", 'amortTaxPeriod')
                        break
                    case 341: //(РНУ-45) Регистр налогового учёта «ведомость начисленной амортизации по нематериальным активам»
                        // графа 9 = сумма граф 10 итогов форм
                        addChildTotalData(dataRows, 19, 'consumptionTaxSumS', dataRowsChild, "total", 'amortizationSinceYear')
                        break
                    case 312: //(РНУ-49) Регистр налогового учёта «ведомость определения результатов от реализации (выбытия) имущества»
                        // графа 9 = сумма граф 8 итогов раздела Д форм
                        addChildTotalData(dataRows, 36, 'consumptionTaxSumS', dataRowsChild, "totalD", 'price')
                        // графа 9 = сумма (графа 11 - 8 - 10 + 15) итогов раздела Д форм
                        addRowValue(dataRows, 37, 'consumptionTaxSumS',
                                getChildTotalValue(dataRowsChild, "totalD", 'sum') -
                                        getChildTotalValue(dataRowsChild, "totalD", 'price') -
                                        getChildTotalValue(dataRowsChild, "totalD", 'expensesOnSale') +
                                        getChildTotalValue(dataRowsChild, "totalD", 'sumIncProfit'))
                        // графа 9 = сумма граф 8 итогов раздела Е форм
                        addChildTotalData(dataRows, 62, 'consumptionTaxSumS', dataRowsChild, "totalE", 'price')
                        // графа 9 = сумма (графа 8 + 10) итогов раздела Г форм
                        addRowValue(dataRows, 63, 'consumptionTaxSumS',
                                getChildTotalValue(dataRowsChild, "totalG", 'price') +
                                        getChildTotalValue(dataRowsChild, "totalG", 'expensesOnSale'))
                        // графа 9 = сумма (графа 8 - 9 + 10) итогов раздела А форм
                        addRowValue(dataRows, 64, 'consumptionTaxSumS',
                                getChildTotalValue(dataRowsChild, "totalA", 'price') -
                                        getChildTotalValue(dataRowsChild, "totalA", 'amort') +
                                        getChildTotalValue(dataRowsChild, "totalA", 'expensesOnSale'))
                        // графа 9 = сумма (графа 8 - 9 + 10) итогов раздела Б форм
                        addRowValue(dataRows, 65, 'consumptionTaxSumS',
                                getChildTotalValue(dataRowsChild, "totalB", 'price') -
                                        getChildTotalValue(dataRowsChild, "totalB", 'amort') +
                                        getChildTotalValue(dataRowsChild, "totalB", 'expensesOnSale'))
                        // графа 9 = сумма граф 17 итогов раздела А форм
                        addChildTotalData(dataRows, 66, 'consumptionTaxSumS', dataRowsChild, "totalA", 'loss')
                        // графа 9 = сумма граф 17 итогов раздела В форм
                        addChildTotalData(dataRows, 80, 'consumptionTaxSumS', dataRowsChild, "totalV", 'loss')
                        break
                    case 358: //(РНУ-72) Регистр налогового учёта уступки права требования как реализации финансовых услуг и операций с закладными
                        // графа 9 = сумма (графа 6 - 7) итогов форм
                        addRowValue(dataRows, 38, 'consumptionTaxSumS',
                                getChildTotalValue(dataRowsChild, "total", 'cost279') -
                                        getChildTotalValue(dataRowsChild, "total", 'costReserve'))
                        // графа 9 = сумма граф 8 итогов форм
                        addChildTotalData(dataRows, 41, 'consumptionTaxSumS', dataRowsChild, "total", 'loss')
                        break
                    case 504: //(РНУ-70.1) Регистр налогового учёта уступки права требования до наступления, предусмотренного кредитным договором срока погашения основного долга
                        // графа 9 = сумма граф 10 итогов форм
                        addChildTotalData(dataRows, 42, 'consumptionTaxSumS', dataRowsChild, "total", 'financialResult1')
                        // графа 9 = сумма граф 11 итогов форм
                        addChildTotalData(dataRows, 43, 'consumptionTaxSumS', dataRowsChild, "total", 'currencyDebtObligation')
                        break
                    case 357: //(РНУ-70.2) Регистр налогового учёта уступки права требования до наступления, предусмотренного кредитным договором срока погашения основного долга в отношении сделок уступки прав требования в пользу Взаимозависимых лиц и резидентов оффшорных зон
                        break
                    case 356: //(РНУ-71.1) Регистр налогового учёта уступки права требования после предусмотренного кредитным договором срока погашения основного долга
                        // графа 9 = сумма граф 9 и 10 итогов форм
                        addRowValue(dataRows, 44, 'consumptionTaxSumS',
                                getChildTotalValue(dataRowsChild, "itg", 'result') +
                                        getChildTotalValue(dataRowsChild, "itg", 'income'))
                        // графа 9 = сумма граф 11 итогов форм
                        addChildTotalData(dataRows, 81, 'consumptionTaxSumS', dataRowsChild, "itg", 'result')
                        // графа 9 = сумма граф 9 и 11 итогов форм
                        addRowValue(dataRows, 82, 'consumptionTaxSumS',
                                getChildTotalValue(dataRowsChild, "itg", 'dateOfAssignment') +
                                        getChildTotalValue(dataRowsChild, "itg", 'result'))
                        break
                    case 503: //(РНУ-71.2) Регистр налогового учёта уступки права требования после предусмотренного кредитным договором срока погашения основного долга в отношении сделок уступки прав требования в пользу Взаимозависимых лиц и резидентов оффшорных зон
                        break
                    case 365: //(РНУ-50) Регистр налогового учёта «ведомость понесённых убытков от реализации амортизируемого имущества»
                        // графа 9 = сумма граф 5 итогов форм
                        addChildTotalData(dataRows, 45, 'consumptionTaxSumS', dataRowsChild, "total", 'lossTaxPeriod')
                        // графа 9 = сумма граф 4 итогов форм
                        addChildTotalData(dataRows, 46, 'consumptionTaxSumS', dataRowsChild, "total", 'lossReportPeriod')
                        break
                    case 353: //(РНУ-57) Регистр налогового учёта финансового результата от реализации (погашения) векселей сторонних эмитентов
                        // графа 9 = сумма граф 12 итогов форм
                        addChildTotalData(dataRows, 49, 'consumptionTaxSumS', dataRowsChild, "total", 'allIncome')
                        break
                    case 332: //(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО
                        // графа 9 = сумма граф 12 итогов форм
                        addChildTotalData(dataRows, 50, 'consumptionTaxSumS', dataRowsChild, "total", 'taxPrice')
                        // графа 9 = сумма граф 17 итогов форм
                        addChildTotalData(dataRows, 59, 'consumptionTaxSumS', dataRowsChild, "total", 'marketPriceRuble')
                        break
                    case 345: //(РНУ-51) Регистр налогового учёта финансового результата от реализации (выбытия) ОФЗ
                        // графа 9 = сумма граф 20 итогов форм
                        addChildTotalData(dataRows, 51, 'consumptionTaxSumS', dataRowsChild, "itogo", 'expensesTotal')
                        // графа 9 = сумма граф (8 - 12) итогов форм
                        addRowValue(dataRows, 60, 'consumptionTaxSumS',
                                getChildTotalValue(dataRowsChild, "total", 'acquisitionPrice') -
                                        getChildTotalValue(dataRowsChild, "total", 'acquisitionPriceTax'))
                        break
                    case 362: //(Ф 7.8) Реестр совершенных операций с ценными бумагами по продаже и погашению, а также по открытию-закрытию короткой позиции
                        // графа 9 = сумма граф 31 фикс строки 14 форм
                        addChildTotalData(dataRows, 52, 'consumptionTaxSumS', dataRowsChild, "R7-total", 'totalLoss')
                        // графа 9 = сумма граф 31 фикс строки 2 форм
                        addChildTotalData(dataRows, 53, 'consumptionTaxSumS', dataRowsChild, "R1-total", 'totalLoss')
                        // графа 9 = сумма граф 31 фикс строки 4 форм
                        addChildTotalData(dataRows, 54, 'consumptionTaxSumS', dataRowsChild, "R2-total", 'totalLoss')
                        //TODO 21674 21676
                        // графа 9 = сумма граф 31 фикс строки 18 форм
                        addChildTotalData(dataRows, 57, 'consumptionTaxSumS', dataRowsChild, "R9-total", 'totalLoss')
                        // графа 9 = сумма граф (17 - 21) итогов "Всего за текущий налоговый период" форм
                        addRowValue(dataRows, 61, 'consumptionTaxSumS',
                                getChildTotalValue(dataRowsChild, "R11", 'costWithoutNKD') -
                                        getChildTotalValue(dataRowsChild, "R11", 'costAcquisition'))
                        break
                    case 355: //(РНУ-64) Регистр налогового учёта затрат, связанных с проведением сделок РЕПО
                        // графа 9 = сумма граф 6 итогов форм
                        addChildTotalData(dataRows, 58, 'consumptionTaxSumS', dataRowsChild, "total", 'costs')
                        break
                    case 352: //(РНУ-61) Регистр налогового учёта расходов по процентным векселям ОАО «Сбербанк России», учёт которых требует применения метода начисления
                        // графа 9 = сумма граф 14 итогов форм
                        addChildTotalData(dataRows, 69, 'consumptionTaxSumS', dataRowsChild, "total", 'percAdjustment')
                        break
                    case 354: //(РНУ-62) Регистр налогового учёта расходов по дисконтным векселям ОАО «Сбербанк России»
                        //TODO 22500 (2 строки)
                        break
                    case 374: //(РНУ-112) Регистр налогового учета сделок РЕПО и сделок займа ценными бумагами
                        //TODO пропускаю пока не определились с РНУ-100+
                        break
                    case 324: //(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения
                        // графа 9 = сумма граф 12 итогов форм
                        addChildTotalData(dataRows, 76, 'consumptionTaxSumS', dataRowsChild, "total", 'reserveCreation')
                        break
                    case 325: //(РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения
                        // графа 9 = сумма граф 16 итогов форм
                        addChildTotalData(dataRows, 77, 'consumptionTaxSumS', dataRowsChild, "total", 'reserveCreation')
                        break
                    case 326: //(РНУ-27) Регистр налогового учёта расчёта резерва под возможное обеспечение субфедеральных и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения
                        // графа 9 = сумма граф 16 итогов форм
                        addChildTotalData(dataRows, 78, 'consumptionTaxSumS', dataRowsChild, "total", 'reserveCreation')
                        break
                    case 329: //(РНУ-30) Расчёт резерва по сомнительным долгам на основании результатов инвентаризации сомнительной задолженности и безнадежных долгов.
                        // графа 9 = сумма граф 14 итогов форм
                        addChildTotalData(dataRows, 79, 'consumptionTaxSumS', dataRowsChild, "total", 'calcReserve')
                        break
                    case 370: //(РНУ-117) Регистр налогового учёта доходов и расходов, по операциям со сделками форвард, квалифицированным в качестве операций с ФИСС для целей налогообложения
                        //TODO пропускаю пока не определились с РНУ-100+
                        break
                    case 373: //(РНУ-118) Регистр налогового учёта доходов и расходов, по операциям со сделками опцион, квалифицированным в качестве операций с ФИСС для целей налогообложения.
                        //TODO пропускаю пока не определились с РНУ-100+
                        break
                    case 371: //(РНУ-119) Регистр налогового учёта доходов и расходов, по сделкам своп, квалифицированным в качестве операций с ФИСС для целей налогообложения
                        //TODO пропускаю пока не определились с РНУ-100+
                        break
                    case 369: //(РНУ-115) Регистр налогового учёта доходов, возникающих в связи с применением в сделках по предоставлению Межбанковских кредитов Взаимозависимым лицам и резидентам оффшорных зон Процентных ставок, не соответствующих рыночному уровню
                        //TODO пропускаю пока не определились с РНУ-100+
                        break
                    case 368: //(РНУ-116) Регистр налогового учёта доходов и расходов, возникающих в связи с применением в конверсионных сделках со Взаимозависимыми  лицами и резидентами оффшорных зон курса, не соответствующих рыночному уровню
                        //TODO пропускаю пока не определились с РНУ-100+
                        break

                }
            }
        }
    }
}

// Проверка на банк
def isBank() {
    boolean isBank = true
    // получаем список приемников
    def list = departmentFormTypeService.getFormDestinations(formData.departmentId, formData.formType.id, FormDataKind.SUMMARY, getReportPeriodStartDate(), getReportPeriodEndDate())
    // если есть приемники в других подразделениях, то это не банк, а ОП
    list.each {
        if (it.departmentId != formData.departmentId) {
            isBank = false
        }
    }
    return isBank
}

/**
 * Получить сумму диапазона строк определенного столбца.
 */
def getSum(def dataRows, String columnAlias, String rowFromAlias, String rowToAlias) {
    def from = getDataRow(dataRows, rowFromAlias).getIndex() - 1
    def to = getDataRow(dataRows, rowToAlias).getIndex() - 1
    if (from > to) {
        return 0
    }
    return ((BigDecimal)summ(formData, dataRows, new ColumnRange(columnAlias, from, to))).setScale(2, BigDecimal.ROUND_HALF_UP)
}

/**
 * Получить значение или ноль.
 *
 * @param value значение которое надо проверить
 */
def getValue(def value) {
    return value ?: 0
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formDataService.getDataRowHelper(formData).getAllCached().indexOf(row)
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == ''
}

// Получить значение для графы 13. Сумма значении графы 6 указанных строк
def calcColumn6(def dataRows, def aliasRows) {
    def sum = 0
    aliasRows.each { alias ->
        sum += getDataRow(dataRows, alias).consumptionBuhSumAccepted
    }
    return sum
}

/**
 * Получить данные формы "расходы простые" (id = 304)
 */
def getFormDataSimple() {
    return formDataService.find(304, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

/**
 * Получить сумму значений из расходов простых.
 *
 * @param data данные формы
 * @param columnAliasCheck алиас графы, по которой отбираются строки для суммирования
 * @param columnAliasSum алиас графы, значения которой суммируются
 * @param value значение, по которому отбираются строки для суммирования
 */
def getSumFromSimple(data, columnAliasCheck, columnAliasSum, value) {
    def sum = 0
    if (data != null && (columnAliasCheck != null || columnAliasCheck != '') && value != null) {
        for (def row : formDataService.getDataRowHelper(data).getAllCached()) {
            if (row.getCell(columnAliasCheck).getValue() == value) {
                sum += (row.getCell(columnAliasSum).getValue() ?: 0)
            }
        }
    }
    return sum
}

// Проверить заполненость обязательных полей
// Нередактируемые не проверяются
def checkRequiredColumns(def row, def columns) {
    def colNames = []
    columns.each {
        def cell = row.getCell(it)
        if (cell?.style?.alias == editableStyle && (cell.getValue() == null || row.getCell(it).getValue() == '')) {
            def name = getColumnName(row, it)
            colNames.add('«' + name + '»')
        }
    }
    if (!colNames.isEmpty()) {
        def errorMsg = colNames.join(', ')
        logger.error("Строка ${row.getIndex()}: не заполнены графы : $errorMsg.")
    }
}

def getOpuValue(def value) {
    formDataService.getRefBookValue(27, value, refBookCache)?.OPU?.stringValue
}

/**
 * Добавить значение из формы предыдущего периода
 * @param rowNumbers - номера строк
 * @param dataRows - строки текущей сводной
 * @param column - столбец текущей сводной
 * @param dataRowsOld - строки прошлой сводной
 * @param aliasOld - столбец прошлой сводной
 */
void addPrevValue(Collection<Integer> rowNumbers, def dataRows, String column, def dataRowsOld, String aliasOld) {
    if (!(dataRows && dataRowsOld && column && aliasOld)) {
        return
    }
    rowNumbers.each { number ->
        def rowOld = getDataRow(dataRowsOld, "R$number")
        addRowValue(dataRows, number, column, rowOld[aliasOld])
    }
}

/**
 * Метод для консолидации - расчет ячейки из итоговой строки источника
 * @param dataRows - строки сводной
 * @param number - номер рассчитываемой строки сводной
 * @param column - рассчитываемый столбец сводной
 * @param dataRowsChild - строки источника
 * @param totalAlias - псевдоним итоговой строки
 * @param columnChild - графа для сложения из источников
 */
void addChildTotalData(def dataRows, def number, def column, def dataRowsChild, String totalAlias, def columnChild) {
    def addValue = getChildTotalValue(dataRowsChild, totalAlias, columnChild)
    addRowValue(dataRows, number, column, addValue)
}

/**
 * Метод для консолидации - получение значения из итоговой строки источника
 * @param dataRowsChild - строки источника
 * @param totalAlias - псевдоним итоговой строки
 * @param columnChild - графа для сложения из источников
 */
def getChildTotalValue(def dataRowsChild, String totalAlias, def columnChild){
    if (!(dataRowsChild && totalAlias && columnChild)){
        logger.info("Ошибка при консолидации. Псевдоним \"${totalAlias}\", столбец \"${columnChild}\"")//TODO заменить на что-то более адекватное
        return
    }
    def addValue = 0
    for (def rowChild : dataRowsChild) {
        //ищем итоговую строку
        if (rowChild.getAlias() == totalAlias) {
            addValue += (rowChild[columnChild] ?: 0)
        }
    }
    return addValue
}

/**
 * Добавить к ячейке сводной значение
 * @param dataRows - строки сводной
 * @param number - номер строки ячейки сводной
 * @param column - столбец ячейки сводной
 * @param addValue - прибавляемое значение
 */
void addRowValue(def dataRows, def number, def column, def addValue){
    if (!(dataRows && number && column)){
        logger.info("Ошибка при консолидации")//TODO заменить на что-то более адекватное
        return
    }
    if (addValue) {
        def row = getDataRow(dataRows, "R$number")
        if (row[column] == null) {
            logger.info("Пустая ячейка в строке ${number} псевдоним ${alias}")
        }
        row[column] ? (row[column] += addValue) : (row[column] = addValue)
    }
}

def getRnu12AddValue(def dataRowsChild, String knu, String columnChild) {
    if (!(dataRowsChild && knu && columnChild)) {
        return null
    }
    def addValue = 0
    dataRowsChild.each { rowChild ->
        if (!rowChild.getAlias() && knu == getOpuValue(rowChild.opy)){
            addValue += (rowChild[columnChild] ?: 0)
        }
    }
    return addValue
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'КНУ', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 10, 3)

    def headerMapping = [
            (xml.row[0].cell[0]): 'КНУ',
            (xml.row[0].cell[1]): 'Группа расхода',
            (xml.row[0].cell[2]): 'Вид расхода по операции',
            (xml.row[0].cell[3]): 'Расход по данным бухгалтерского учёта',
            (xml.row[0].cell[7]): 'Расход по данным налогового учёта',
            (xml.row[1].cell[3]): 'номер счёта учёта',
            (xml.row[1].cell[4]): 'источник информации в РНУ',
            (xml.row[1].cell[5]): 'сумма',
            (xml.row[1].cell[6]): 'в т.ч. учтено в предыдущих налоговых периодах',
            (xml.row[1].cell[7]): 'источник информации в РНУ',
            (xml.row[1].cell[8]): 'сумма',
            (xml.row[1].cell[9]): 'форма РНУ'
    ]
    (0..9).each { index ->
        headerMapping.put((xml.row[2].cell[index]), (index + 1).toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()
    def int maxRow = 90

    def rows = dataRowHelper.allCached
    def int rowIndex = 1
    def knu
    def group
    //def type
    def num
    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // пропустить шапку таблицы
        if (xmlIndexRow <= headRowCount) {
            continue
        }
        // прервать по загрузке нужных строк
        if (rowIndex > maxRow) {
            break
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def alias = "R" + rowIndex
        def curRow = getDataRow(rows, alias)
        curRow.setImportIndex(xlsIndexRow)

        //очищаем столбцы
        resetColumns.each {
            curRow[it] = null
        }

        knu = normalize(curRow.consumptionTypeId)
        group = normalize(curRow.consumptionGroup)
        //type = normalize(curRow.consumptionTypeByOperation)
        num = normalize(curRow.consumptionBuhSumAccountNumber)

        def xmlIndexCol = 0

        def knuImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        def groupImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        //def typeImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        def numImport = normalize(row.cell[xmlIndexCol].text())

        //если совпадают или хотя бы один из атрибутов не пустой и значения строк в файлах входят в значения строк в шаблоне,
        //то продолжаем обработку строки иначе пропускаем строку
        if (!((knu == knuImport && group == groupImport && num == numImport) ||
                ((!knuImport.isEmpty() || !groupImport.isEmpty() || !numImport.isEmpty()) &&
                        knu.contains(knuImport) && group.contains(groupImport) && num.contains(numImport)))) {
            continue
        }
        rowIndex++

        xmlIndexCol = 5

        // графа 6
        curRow.consumptionBuhSumAccepted = parseNumber(row.cell[xmlIndexCol].text().trim(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 7
        curRow.consumptionBuhSumPrevTaxPeriod = parseNumber(row.cell[xmlIndexCol].text().trim(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 8
        xmlIndexCol++

        // графа 9
        if (!notImportSum.contains(alias)) {
            curRow.consumptionTaxSumS = parseNumber(row.cell[xmlIndexCol].text().trim(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }

    }
    if (rowIndex < maxRow) {
        logger.error("Структура файла не соответствует макету налоговой формы в строке с КНУ = $knu. ")
    }
    dataRowHelper.update(rows)
}

// Возвращает данные из Отчета о прибылях и убытках за период, для которого сформирована текущая форма
def getIncome102Data(def row) {
    // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
    if (rbIncome102 == null) {
        rbIncome102 = refBookFactory.getDataProvider(52L)
    }
    return rbIncome102?.getRecords(getReportPeriodEndDate(), null, "OPU_CODE = '${row.accountingRecords}' AND DEPARTMENT_ID = ${formData.departmentId}", null)
}

void checkTotalSum(totalRow, sum){
    if (totalRow[totalColumn] != sum) {
        logger.error("Итоговое значение в строке ${totalRow.getIndex()} рассчитано неверно в графе \"" + getColumnName(totalRow, totalColumn) + "\"")
    }
}