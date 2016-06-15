package form_template.income.advanceDistribution_2.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

/**
 * Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации.
 * formTemplateId=507
 */

// графа 1  - number
// графа    - fix
// графа 2  - regionBank
// графа 3  - regionBankDivision
// графа 4  - divisionName
// графа 5  - kpp
// графа 6  - propertyPrice
// графа 7  - workersCount
// графа 8  - subjectTaxCredit
// графа 9  - calcFlag
// графа 10 - obligationPayTax
// графа 11 - baseTaxOf
// графа 12 - baseTaxOfRub
// графа 13 - subjectTaxStavka
// графа 14 - taxSum
// графа 15 - taxSumOutside
// графа 16 - taxSumToPay
// графа 17 - taxSumToReduction
// графа 18 - everyMontherPayment1
// графа 19 - everyMontherPayment2
// графа 20 - everyMontherPayment3
// графа 21 - everyMontherPaymentAfterPeriod
// графа 22 - everyMonthForKvartalNextPeriod
// графа 23 - everyMonthForSecondKvartalNextPeriod
// графа 24 - everyMonthForThirdKvartalNextPeriod
// графа 25 - everyMonthForFourthKvartalNextPeriod
// графа 26 - minimizeTaxSum
// графа 27 - amountTax

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_LOAD:
        afterLoad()
        break
    case FormDataEvent.CHECK:
        logicalCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicalCheckAfterCalc()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow.getAlias() == null) formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicalCheck()
        checkDeclaration()
        break
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicalCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            formDataService.saveCachedDataRows(formData, logger)
        }
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

// Все атрибуты
@Field
def allColumns = ['number', 'regionBank', 'fix', 'regionBankDivision', 'divisionName', 'kpp', 'propertyPrice',
                  'workersCount', 'subjectTaxCredit', 'calcFlag', 'obligationPayTax',
                  'baseTaxOf', 'baseTaxOfRub', 'subjectTaxStavka', 'taxSum',
                  'taxSumOutside', 'taxSumToPay', 'taxSumToReduction', 'everyMontherPayment1', 'everyMontherPayment2', 'everyMontherPayment3',
                  'everyMontherPaymentAfterPeriod', 'everyMonthForKvartalNextPeriod',
                  'everyMonthForSecondKvartalNextPeriod', 'everyMonthForThirdKvartalNextPeriod',
                  'everyMonthForFourthKvartalNextPeriod', 'minimizeTaxSum', 'amountTax']

// Редактируемые атрибуты
@Field
def editableColumns = ['regionBankDivision', 'kpp', 'propertyPrice', 'workersCount', 'subjectTaxCredit', 'minimizeTaxSum', 'amountTax']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['regionBank', 'regionBankDivision',
                       'divisionName', 'kpp', 'propertyPrice', 'workersCount',
                       'subjectTaxCredit', 'calcFlag', 'obligationPayTax',
                       'baseTaxOf', 'baseTaxOfRub', 'subjectTaxStavka',
                       'taxSum', 'taxSumOutside', 'taxSumToPay',
                       'taxSumToReduction', 'everyMonthForKvartalNextPeriod',
                       'everyMonthForSecondKvartalNextPeriod', 'everyMonthForThirdKvartalNextPeriod',
                       'everyMonthForFourthKvartalNextPeriod']

// Группируемые атрибуты
@Field
def groupColumns = ['regionBankDivision', 'regionBank']

// Атрибуты для итогов
@Field
def totalColumns = ['propertyPrice', 'workersCount', 'subjectTaxCredit',
                    'baseTaxOfRub', 'taxSum', 'taxSumOutside', 'taxSumToPay',
                    'taxSumToReduction', 'everyMontherPaymentAfterPeriod',
                    'everyMonthForKvartalNextPeriod', 'everyMonthForSecondKvartalNextPeriod',
                    'everyMonthForThirdKvartalNextPeriod',
                    'everyMonthForFourthKvartalNextPeriod', 'minimizeTaxSum']

@Field
def totalColumnsPeriod4 = totalColumns - ['everyMontherPayment1', 'everyMontherPayment2', 'everyMontherPayment3', 'everyMontherPaymentAfterPeriod']

// расчитываемые поля (графа 12..22)
@Field
def calcColumns = [ 'baseTaxOfRub', 'subjectTaxStavka', 'taxSum', 'taxSumOutside', 'taxSumToPay', 'taxSumToReduction',
        'everyMontherPaymentAfterPeriod', 'everyMonthForKvartalNextPeriod', 'everyMonthForSecondKvartalNextPeriod',
        'everyMonthForThirdKvartalNextPeriod', 'everyMonthForFourthKvartalNextPeriod']

// расчитываемые поля (графа 2, 4, 9, 10, 11)
@Field
def notNumberCalcColumns = ['regionBank', 'divisionName', 'calcFlag', 'obligationPayTax', 'baseTaxOf']

@Field
def formDataCache = [:]
@Field
def helperCache = [:]

@Field
def summaryMap = [[301, 305] : "Доходы, учитываемые в простых РНУ", [302] : "Сводная форма начисленных доходов", //максимум два вида источников с одним именем
                  [303] : "Сводная форма начисленных расходов", [304, 310] : "Расходы, учитываемые в простых РНУ"]

@Field
long centralId = 113 // ID Центрального аппарата

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

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
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

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить фиксированные строки
    deleteAllAliased(dataRows)

    logicalCheckBeforeCalc()
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    def propertyPriceSumm = getSumAll(dataRows, "propertyPrice")
    def workersCountSumm = getSumAll(dataRows, "workersCount")

    // Распределяемая налоговая база за отчетный период.
    def taxBase = null
    if (formDataEvent != FormDataEvent.COMPOSE) {
        taxBase = roundValue(getTaxBaseAsDeclaration(), 0)

        def tmpRow = (dataRows ? dataRows[0] : formData.createDataRow())
        def msg = "Для заполнения графы «%s» рассчитано значение «%s» переменной «Распределяемая налоговая база за отчётный/налоговый период»"
        logger.info(msg, getColumnName(tmpRow, 'baseTaxOfRub'), taxBase)
    }
    // Отчётный период.
    def reportPeriod = getReportPeriod()
    def departmentParamsDate = getReportPeriodEndDate() - 1

    // Получение строк формы "Сведения о суммах налога на прибыль, уплаченного Банком за рубежом"
    def dataRowsSum = getDataRows(421, FormDataKind.ADDITIONAL)

    // Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде.
    def sumNal = 0
    def sumTaxRecords = getRefBookRecord(33, "DEPARTMENT_ID", "1", departmentParamsDate, -1, null, false)
    if (sumTaxRecords != null && !sumTaxRecords.isEmpty()) {
        sumNal = getAliasFromForm(dataRowsSum, 'taxSum', 'SUM_TAX')
    }

    def prevDataRows = getPrevDataRows()

    // расчет графы 2..4, 8..17, 23, 24
    for (row in dataRows) {
        // графа 2 - название подразделения
        row.regionBank = calc2(row)

        def incomeParam
        if (row.regionBankDivision != null) {
            def departmentParam = getRefBookValue(30, row.regionBankDivision)

            def depParam = getDepParam(departmentParam)
            def depId = depParam.get(RefBook.RECORD_ID_ALIAS).numberValue as long

            incomeParam = getRefBookRecord(33, "DEPARTMENT_ID", "$depId", departmentParamsDate, -1, null, false)
        }
        if (incomeParam == null || incomeParam.isEmpty()) {
            continue
        }
        // графа 4 - наименование подразделения в декларации
        row.divisionName = calc4(row)

        // графа 9 - Признак расчёта
        row.calcFlag = calc9(row)

        // графа 10 - Обязанность по уплате налога
        row.obligationPayTax = calc10(row)

        // графа 11
        row.baseTaxOf = calc11(row, propertyPriceSumm, workersCountSumm)

        if (formDataEvent != FormDataEvent.COMPOSE) {
            // графа 12
            row.baseTaxOfRub = calc12(row, taxBase)
        }
        // графа 13
        row.subjectTaxStavka = calc13(row)

        // графа 14
        row.taxSum = calc14(row)

        // графа 14..17, 23, 24
        calcColumnFrom14To24(prevDataRows, row, row, sumNal, reportPeriod)
    }

    // нужен отдельный расчет
    for (row in dataRows) {
        calc18_22(prevDataRows, dataRows, row, reportPeriod)
    }

    def templateRows = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId).getRows()

    // добавить строку ЦА (скорректрированный) (графа 1..22)
    def caTotalRow = getDataRow(templateRows, 'ca')
    dataRows.add(caTotalRow)

    // добавить итого (графа 5..7, 10, 11, 13..22)
    def totalRow = getDataRow(templateRows, 'total')
    calcTotalRow(dataRows, totalRow)
    dataRows.add(totalRow)

    // в строках должна быть итоговая
    calcCaTotalRow(dataRows, prevDataRows, caTotalRow, totalRow, taxBase, sumNal)

    // После расчета фикс. строки "ЦА(скоррект)" необходимо пересчитать для граф 12, 14, 15, 17, 18, 19, 20, 21, 22
    // фиксированную строку итогов: сумма всех строк, кроме строки "ЦА" и строки итогов
    reCalcTotalRow(dataRows, totalRow)

    updateIndexes(dataRows)
}

void reCalcTotalRow(def dataRows, def totalRow) {
    def isTaxPeriod = getReportPeriod().order == 4

    totalRow.baseTaxOfRub = 0
    totalRow.taxSum = 0
    totalRow.taxSumOutside = 0
    totalRow.taxSumToReduction = 0
    totalRow.everyMontherPaymentAfterPeriod = (isTaxPeriod ? null : 0)
    totalRow.everyMonthForKvartalNextPeriod = 0
    for (row in dataRows) {
        if (row.getAlias() == 'total' || row.regionBank == centralId) {
            continue
        }
        totalRow.baseTaxOfRub += row.baseTaxOfRub ?: 0
        totalRow.taxSum += row.taxSum ?: 0
        totalRow.taxSumOutside += row.taxSumOutside ?: 0
        totalRow.taxSumToReduction += row.taxSumToReduction ?: 0
        if (!isTaxPeriod) {
            totalRow.everyMontherPaymentAfterPeriod += row.everyMontherPaymentAfterPeriod ?: 0
        }
        totalRow.everyMonthForKvartalNextPeriod += row.everyMonthForKvartalNextPeriod ?: 0
    }
    // графа 18, 19, 20 расчитываются после графы 21
    calc18_19_20(totalRow, getReportPeriod())
}

void calcTotalRow(def dataRows, def totalRow) {
    def isTaxPeriod = getReportPeriod().order == 4
    def columns = (isTaxPeriod ? totalColumnsPeriod4 : totalColumns)
    calcTotalSum(dataRows, totalRow, columns)

    // графа 11
    totalRow.baseTaxOf = roundValue(dataRows.sum { row ->
        String value = row.baseTaxOf
        (row.getAlias() == null && value?.isBigDecimal()) ? new BigDecimal(value) : BigDecimal.ZERO
    } ?: BigDecimal.ZERO, 15).toPlainString()
}

void calcCaTotalRow(def dataRows, def prevDataRows, def caTotalRow, def totalRow, def taxBase, def sumNal) {
    def reportPeriod = getReportPeriod()

    // найти строку ЦА
    def caRow = findCA(dataRows)
    if (caRow != null) {
        // расчеты для строки ЦА (скорректированный)
        ['number', 'regionBankDivision', 'divisionName', 'kpp', 'propertyPrice', 'workersCount', 'subjectTaxCredit',
         'calcFlag', 'obligationPayTax', 'baseTaxOf', 'subjectTaxStavka', 'taxSumToPay',
         'everyMonthForSecondKvartalNextPeriod', 'everyMonthForThirdKvartalNextPeriod',
         'everyMonthForFourthKvartalNextPeriod'].each { alias ->
            caTotalRow[alias] = caRow[alias]
        }

        // графа 21
        def tempValue
        switch (reportPeriod.order) {
            case 1: // Период формы «1 квартал»:
                // Принимает значение «графы 14» подразделения «Центральный аппарат»
                tempValue = caRow.taxSum
                break
            case 4: // Период формы "год"
                tempValue = null
                break
            default: // Период формы «полугодие / 9 месяцев»:
                // «Графа 18» = Значение «графы 18» подразделения «Центральный аппарат» + (Значение итоговой строки по «графе 18» - (Значение итоговой строки по «графе 14» - Значение итоговой строки по «графе 14» формы предыдущего периода))
                def prevTaxSum = prevDataRows?.find {it.getAlias() == 'total'}?.taxSum ?: 0
                tempValue = (caRow.everyMontherPaymentAfterPeriod ?: 0) + ((totalRow.everyMontherPaymentAfterPeriod ?: 0) - ((totalRow.taxSum ?: 0) - prevTaxSum))
        }
        caTotalRow.everyMontherPaymentAfterPeriod = tempValue

        if (formDataEvent != FormDataEvent.COMPOSE) {
            // графа 12
            caTotalRow.baseTaxOfRub = (taxBase ?: 0) - (totalRow.baseTaxOfRub ?: 0) + (caRow.baseTaxOfRub ?: 0)
        }

        // графа 14
        caTotalRow.taxSum = roundValue((caTotalRow.baseTaxOfRub ?: 0) * (caTotalRow.subjectTaxStavka ?: 0) / 100, 0)

        // графа 15
        caTotalRow.taxSumOutside = 0.9 * sumNal - totalRow.taxSumOutside + caRow.taxSumOutside

        // графа 17
        if (caTotalRow.taxSum < ((caTotalRow.subjectTaxCredit ?: 0) + caTotalRow.taxSumOutside)) { // «графа 14» < («графа 8» + «графа 15» )
            // («графа 8» + «графа 15») - «графа 14»
            caTotalRow.taxSumToReduction = ((caTotalRow.subjectTaxCredit ?: 0) + caTotalRow.taxSumOutside) - caTotalRow.taxSum
        } else {
            caTotalRow.taxSumToReduction = 0
        }

        // графа 18, 19, 20 расчитываются после графы 21
        calc18_19_20(caTotalRow, reportPeriod)

        // графа 22
        if (reportPeriod.order == 3) {
            // «графа 22» = «графа 21» строки "ЦА (скорр.)"
            caTotalRow.everyMonthForKvartalNextPeriod = caTotalRow.everyMontherPaymentAfterPeriod
        } else {
            caTotalRow.everyMonthForKvartalNextPeriod = 0
        }
    }
}

// название подразделения
def calc2(def row) {
    def departmentParam
    if (row.regionBankDivision != null) {
        departmentParam = getRefBookValue(30L, row.regionBankDivision)
    }
    if (departmentParam == null || departmentParam.isEmpty()) {
        return null
    }

    // У Центрального аппарата родительским подразделением должен быть он сам
    if (centralId == row.regionBankDivision) {
        return centralId
    } else {
        return departmentParam.get('PARENT_ID').getReferenceValue()
    }
}

def calc4(def row) {
    return calc4_9_10(row, 'ADDITIONAL_NAME', true)
}

def calc9(def row) {
    return calc4_9_10(row, 'TYPE')
}

def calc10(def row) {
    return calc4_9_10(row, 'OBLIGATION')
}

/**
 * Получить значение из настроек подразделения.
 *
 * @param row строка формы
 * @param attributeName имя возвращаемого атрибута справочника "Параметры подразделения по налогу на прибыль (таблица)" 330
 * @param isString тип возвращаемого значение строка, иначе справочное значение
 */
def calc4_9_10(def row, def attributeName, def isString = false) {
    def departmentParam = null
    def value = null
    if (row.regionBankDivision != null) {
        departmentParam = getRefBookValue(30, row.regionBankDivision)
    }
    def depParam = getDepParam(departmentParam)
    def incomeParamTable = getIncomeParamTable(depParam)
    for (int i = 0; i < incomeParamTable.size(); i++) {
        if (row.kpp != null && row.kpp != '') {
            if (incomeParamTable?.get(i)?.KPP?.stringValue == row.kpp) {
                value = (isString ?
                    incomeParamTable?.get(i)?.get(attributeName)?.stringValue :
                    incomeParamTable?.get(i)?.get(attributeName)?.referenceValue)
                break
            }
        }
    }
    return value
}

def calc11(def row, def propertyPriceSumm, def workersCountSumm) {
    BigDecimal temp = 0
    if (row.propertyPrice != null && row.workersCount != null && propertyPriceSumm > 0 && workersCountSumm > 0) {
        temp = (row.propertyPrice * 100 / propertyPriceSumm + row.workersCount * 100 / workersCountSumm) / 2
    }
    return roundValue(temp, 15).toPlainString()
}

def calc12(def row, def taxBase) {
    if (row.baseTaxOf != null && checkColumn11(row.baseTaxOf) && taxBase != null) {
        return roundValue(taxBase * new BigDecimal(row.baseTaxOf) / 100, 0) // умножение на 18/20 и деление на 100
    }
    return 0
}

def calc13(def row) {
    def temp
    def minimizeTaxSum = row.minimizeTaxSum ?: 0
    if (row.amountTax == null) {
        if (row.baseTaxOfRub > 0) {
            if (minimizeTaxSum > 0) {
                temp = roundValue((row.baseTaxOfRub * 0.18 - minimizeTaxSum) * 100 / row.baseTaxOfRub, 2)
                if (temp < 13.5) {
                    temp = 13.5
                }
            } else {
                temp = 18
            }
        } else {
            temp = 0
        }
    } else {
        temp = row.amountTax
    }
    return temp
}

def calc14(def row) {
    def temp
    def minimizeTaxSum = row.minimizeTaxSum ?: 0
    if (row.baseTaxOfRub > 0) {
        if (minimizeTaxSum == 0) {
            temp = roundValue(row.baseTaxOfRub * row.subjectTaxStavka / 100, 0)
        } else {
            if (row.baseTaxOfRub * 0.135 - minimizeTaxSum < 0) {
                temp = roundValue(row.baseTaxOfRub * 0.135, 0)
            } else {
                temp = roundValue(row.baseTaxOfRub * 0.18, 0) - minimizeTaxSum
            }
        }
    } else {
        temp = 0
    }
    return temp
}

def calc18_22(def prevDataRows, def dataRows, def row, def reportPeriod) {
    def tmp = null
    // графа 21
    switch (reportPeriod.order) {
        case 1:
            // «графа 21» = «графа 14»
            tmp = row.taxSum
            break
        case 4:
            tmp = null
            break
        default:
            // (Сумма всех нефиксированных строк по «графе 14» - Сумма всех нефиксированных строк по «графе 14» из предыдущего периода) * («графа 14» / Сумма всех нефиксированных строк по «графе 14»)
            def currentSum  =     dataRows?.sum { (it.getAlias() == null) ? (it.taxSum ?: 0) : 0 } ?: 0
            def previousSum = prevDataRows?.sum { (it.getAlias() == null) ? (it.taxSum ?: 0) : 0 } ?: 0
            // остальные
            if (currentSum) {
                tmp = (currentSum - previousSum) * row.taxSum / currentSum
            }
    }
    row.everyMontherPaymentAfterPeriod = tmp

    // графа 22
    row.everyMonthForKvartalNextPeriod = ((reportPeriod.order == 3) ? row.everyMontherPaymentAfterPeriod : 0)

    // графа 18, 19, 20 расчитываются после графы 21
    calc18_19_20(row, reportPeriod)
}

// графа 18, 19, 20 расчитываются после графы 21
void calc18_19_20(def row, def reportPeriod) {
    if (reportPeriod.order == 4 || row.everyMontherPaymentAfterPeriod == null) {
        row.everyMontherPayment1 = null
        row.everyMontherPayment2 = null
        row.everyMontherPayment3 = null
    } else {
        def a = roundValue(row.everyMontherPaymentAfterPeriod, 0)
        def b = a * 3
        def c = row.everyMontherPaymentAfterPeriod - b
        row.everyMontherPayment1 = (c == -1 ? a - 1 : a)
        row.everyMontherPayment2 = a
        row.everyMontherPayment3 = (c == 1 ? a + 1 : a)
    }
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck() {
    logicalCheckBeforeCalc()
    logicalCheckAfterCalc()
}

void logicalCheckBeforeCalc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def fieldNumber = 0

    dataRows.eachWithIndex { row, i ->
        if (isFixedRow(row)) {
            return
        }
        fieldNumber++
        def index = row.getIndex()
        def errorMsg = "Строка ${index}: "
        def departmentParam = null
        if (row.regionBankDivision != null) {
            departmentParam = getRefBookValue(30, row.regionBankDivision)
        }
        if (departmentParam == null || departmentParam.isEmpty()) {
            return
        } else {
            // У Центрального аппарата родительским подразделением должен быть он сам
            if (centralId != row.regionBankDivision) {
                // графа 2 - название подразделения
                if (departmentParam.get('PARENT_ID')?.getReferenceValue() == null) {
                    logger.error(errorMsg + "Для подразделения территориального банка «${departmentParam.NAME.stringValue}» в справочнике «Подразделения» отсутствует значение наименования родительского подразделения!")
                }
            }
        }

        // Определение условий для проверок 2, 3, 4
        def depParam = getDepParam(departmentParam)
        def depId = (depParam?.get(RefBook.RECORD_ID_ALIAS)?.numberValue ?: -1) as long
        def departmentName = depParam?.NAME?.stringValue ?: "Не задано"
        def incomeParam
        if (depId != -1) {
            incomeParam = getProvider(33).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $depId", null)
        }
        def incomeParamTable = getIncomeParamTable(depParam)

        // 2. Проверка наличия формы настроек подразделения
        if (incomeParam == null || incomeParam.isEmpty()) {
            rowError(logger, row, errorMsg + "Для подразделения «${departmentName}» не создана форма настроек подразделений!")
        }

        // 3. Проверка наличия строки с «КПП» в табличной части формы настроек подразделения
        // 4. Проверка наличия значения «Наименование для Приложения №5» в форме настроек подразделения
        for (int k = 0; k < incomeParamTable?.size(); k++) {
            if (row.kpp != null && row.kpp != '') {
                if (incomeParamTable?.get(k)?.KPP?.stringValue == row.kpp) {
                    if (incomeParamTable?.get(k)?.ADDITIONAL_NAME?.stringValue == null) {
                        rowError(logger, row, errorMsg + "Для подразделения «${departmentName}» на форме настроек подразделений по КПП «${row.kpp}» отсутствует значение атрибута «Наименование для «Приложения №5»!")
                    }
                    if (incomeParamTable?.get(k)?.TYPE?.referenceValue == null) {
                        rowError(logger, row, errorMsg + "Для подразделения «${departmentName}» на форме настроек подразделений по КПП «${row.kpp}» отсутствует значение атрибута «Признак расчета»!")
                    }
                    if (incomeParamTable?.get(k)?.OBLIGATION?.referenceValue == null) {
                        rowError(logger, row, errorMsg + "Для подразделения «${departmentName}» на форме настроек подразделений по КПП «${row.kpp}» отсутствует значение атрибута «Обязанность по уплате налога»!")
                    }
                    break
                }
                if (k == incomeParamTable.size() - 1) {
                    rowError(logger, row, errorMsg + "Для подразделения «${departmentName}» на форме настроек подразделений отсутствует строка с КПП «${row.kpp}»!")
                }
            }
        }
    }
    if (formDataEvent != FormDataEvent.COMPOSE) {
        summaryMap.each { keys, value ->
            def foundFormsMap = getFormDataSummaryMap(keys)
            // Ищем принятые формы
            def acceptedFormsMap = [:]
            keys.each { key ->
                if (foundFormsMap[key] != null && getData(foundFormsMap[key]) != null) {
                    acceptedFormsMap[key] = foundFormsMap[key]
                }
            }

            if (acceptedFormsMap.size() > 1) {
                logger.warn("Неверно настроены источники сводной! Одновременно созданы в качестве источников налоговые формы: «%s», «%s». Расчет произведен из «%s».",
                        formTypeService.get(keys[1]).name, formTypeService.get(keys[0])?.name, formTypeService.get(keys[1])?.name)
            }else if (acceptedFormsMap.size() == 0) {
                logger.error("Сводная налоговая форма «$value» в подразделении «${formDataDepartment.name}» не находится в статусе «Принята»!")
            } else if (foundFormsMap.size() == 0) {
                logger.error("Сводная налоговая форма «$value» в подразделении «${formDataDepartment.name}» не создана!")
            }
        }
    }
}

void logicalCheckAfterCalc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    boolean wasError = false

    def propertyPriceSumm = getSumAll(dataRows, "propertyPrice")
    def workersCountSumm = getSumAll(dataRows, "workersCount")

    // Распределяемая налоговая база за отчетный период.
    def taxBase = null
    if (formDataEvent != FormDataEvent.COMPOSE) {
        taxBase = roundValue(getTaxBaseAsDeclaration(), 0)
    }
    // Отчётный период.
    def reportPeriod = getReportPeriod()
    def departmentParamsDate = getReportPeriodEndDate() - 1

    // Получение строк формы "Сведения о суммах налога на прибыль, уплаченного Банком за рубежом"
    def dataRowsSum = getDataRows(421, FormDataKind.ADDITIONAL)

    // Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде.
    def sumNal = 0
    def sumTaxRecords = getRefBookRecord(33, "DEPARTMENT_ID", "1", departmentParamsDate, -1, null, false)
    if (sumTaxRecords != null && !sumTaxRecords.isEmpty()) {
        sumNal = getAliasFromForm(dataRowsSum, 'taxSum', 'SUM_TAX')
    }

    def prevDataRows = getPrevDataRows()

    def caTotalRow
    def totalRow
    for (def row : dataRows) {
        if (isFixedRow(row)) {
            if ("ca".equals(row.getAlias())){
                caTotalRow = row
            }
            if ("total".equals(row.getAlias())){
                totalRow = row
            }
            continue
        }
        def index = row.getIndex()

        // 1. Обязательность заполнения поля графы 1..21
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка значения в графе «Доля налоговой базы (№)»
        if (row.baseTaxOf != null && !checkColumn11(row.baseTaxOf)) {
            logger.error("Строка $index: Графа «%s» заполнена неверно! Ожидаемый тип поля: Число/18.15/ (максимальное общее количество цифр 18 - до и после точки; после точки максимальное количество цифр 15).", getColumnName(row,'baseTaxOf'))
        }
        // 3. Проверка на соответствие паттерну
        if (row.kpp && !checkPattern(logger, row, 'kpp', row.kpp, KPP_PATTERN, wasError ? null : KPP_MEANING, true)) {
            wasError = true
        }

        // 5. Проверка значений автоматически заполняемых граф (арифметические проверки)
        def needValue = formData.createDataRow()
        def incomeParam
        if (row.regionBankDivision != null) {
            def departmentParam = getRefBookValue(30, row.regionBankDivision)
            def depParam = getDepParam(departmentParam)
            def depId = depParam.get(RefBook.RECORD_ID_ALIAS).numberValue as long
            incomeParam = getRefBookRecord(33, "DEPARTMENT_ID", "$depId", departmentParamsDate, -1, null, false)
        }
        if (incomeParam == null || incomeParam.isEmpty()) {
            continue
        }
        needValue.regionBank = calc2(row)
        needValue.divisionName = calc4(row)
        needValue.calcFlag = calc9(row)
        needValue.obligationPayTax = calc10(row)
        needValue.baseTaxOf = calc11(row, propertyPriceSumm, workersCountSumm)
        if (formDataEvent != FormDataEvent.COMPOSE) {
            needValue.baseTaxOfRub = calc12(row, taxBase)
        }
        needValue.subjectTaxStavka = calc13(row)
        needValue.taxSum = calc14(row)
        needValue.subjectTaxCredit = row.subjectTaxCredit
        calcColumnFrom14To24(prevDataRows, needValue, row, sumNal, reportPeriod)
        calc18_22(prevDataRows, dataRows, needValue, reportPeriod)
        checkCalc(row, calcColumns, needValue, logger, false)
        // нечисловые значения
        def names = []
        notNumberCalcColumns.each { alias ->
            if (needValue[alias] != row[alias]) {
                names.add(getColumnName(row, alias))
            }
        }
        if (names) {
            def tmpNames = '«' + names.join('», «') + '»'
            logger.warn(WRONG_CALC, row.getIndex(), tmpNames)
        }
    }
    if (caTotalRow == null || totalRow == null) { // строк нет, в расчеты не зашел, проверять нечего
        return
    }

    def templateRows = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId).getRows()
    def caTotalRowTemp = getDataRow(templateRows, 'ca')
    def totalRowTemp = getDataRow(templateRows, 'total')

    calcTotalRow(dataRows, totalRowTemp)
    calcCaTotalRow(dataRows, prevDataRows, caTotalRowTemp, totalRowTemp, taxBase, sumNal)
    reCalcTotalRow(dataRows, totalRowTemp)

    def errorColumns = (allColumns - 'number').findAll { alias ->
        caTotalRowTemp[alias] != caTotalRow[alias]
    }
    errorColumns.each { alias ->
        rowWarning(logger, caTotalRow, String.format("Строка %s: Итоговые значения рассчитаны неверно в графе «%s»!", caTotalRow.getIndex(), getColumnName(caTotalRow, alias)))
    }

    def isTaxPeriod = getReportPeriod().order == 4
    errorColumns = ((isTaxPeriod ? totalColumnsPeriod4 : totalColumns) + 'baseTaxOf').findAll { alias ->
        totalRowTemp[alias] != totalRow[alias]
    }
    errorColumns.each { alias ->
        rowWarning(logger, totalRow, String.format("Строка %s: Итоговые значения рассчитаны неверно в графе «%s»!", totalRow.getIndex(), getColumnName(totalRow, alias)))
    }
}

// проверка графы 11: максимальное общее количество цифр 18 - до и после точки; после точки максимальное количество цифр 15
def checkColumn11(def value) {
    if (value == null) {
        return false
    }
    def i = value.indexOf('.')
    if (i != -1) {
        def head = value.substring(0, i)
        def tail = value.substring(i + 1)
        def headSize = 18 - tail.size()
        // после точки максимум 15 знаков, всего не больше 18 знаков
        return head.matches("[0-9]{1,$headSize}") && tail.matches("[0-9]{1,15}")
    } else {
        // если нет точки, то размер не больше 18
        return value.matches("[0-9]{1,18}")
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def dataRows = []

    // Идентификатор шаблона источников (Приложение 5).
    def id = 372
    def newRow

    departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allSaved
                sourceDataRows.each { row ->
                    if ((row.getAlias() == null || row.getAlias() == '') && row.regionBankDivision != null) {
                        newRow = dataRows.find {
                            it.regionBankDivision == row.regionBankDivision
                        }
                        def isNew = (newRow == null)
                        newRow = newRow ?: formData.createDataRow()
                        if (isNew) {
                            editableColumns.each {
                                newRow.getCell(it).editable = true
                                newRow.getCell(it).setStyleAlias('Редактируемая')
                            }
                            autoFillColumns.each {
                                newRow.getCell(it).setStyleAlias('Автозаполняемая')
                            }
                        }

                        newRow.regionBank = row.regionBank
                        newRow.regionBankDivision = row.regionBankDivision
                        newRow.kpp = row.kpp
                        newRow.propertyPrice = newRow.propertyPrice ? (newRow.propertyPrice + row.avepropertyPricerageCost) : row.avepropertyPricerageCost
                        newRow.workersCount = newRow.workersCount ? (newRow.workersCount + row.workersCount) : row.workersCount
                        newRow.subjectTaxCredit = newRow.subjectTaxCredit ? (newRow.subjectTaxCredit + row.subjectTaxCredit) : row.subjectTaxCredit
                        // если поле в Приложении 5 не заполнено, то и в приемнике оно должно быть незаполнено(похоже подразумевается что источник один)
                        // условие срабатывает только для источника со значением в поле или если оно уже заполнено
                        if (newRow.minimizeTaxSum != null || row.decreaseTaxSum != null) {
                            newRow.minimizeTaxSum = (newRow.minimizeTaxSum ?: 0) + (row.decreaseTaxSum ?: 0)
                        }
                        if (newRow.amountTax != null || row.taxRate != null) {
                            newRow.amountTax = (newRow.amountTax ?: 0) + (row.taxRate ?: 0)
                        }
                        if (isNew) {
                            dataRows.add(newRow)
                        }
                    }
                }
            }
        }
    }

    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 27
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'number')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

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

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()

    def caTotalRow
    def totalRow

    // формирование строк нф
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
        if (rowValues[INDEX_FOR_SKIP] && (rowValues[INDEX_FOR_SKIP] == "Центральный аппарат (скорректированный)" || rowValues[INDEX_FOR_SKIP] =="Сбербанк России")) {
            switch (rowValues[INDEX_FOR_SKIP]) {
                case 'Центральный аппарат (скорректированный)' :
                    caTotalRow = getTotalRowFromXls('ca', rowValues[INDEX_FOR_SKIP], 2, rowValues, colOffset, fileRowIndex, rowIndex, true)
                    break
                case 'Сбербанк России' :
                    totalRow = getTotalRowFromXls('total', rowValues[INDEX_FOR_SKIP], 5, rowValues, colOffset, fileRowIndex, rowIndex, false)
            }
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        // добавить строку ЦА (скорректрированный)
        if (caTotalRow == null) { // если итоговых строк нет, то создать
            caTotalRow = formData.createDataRow()
            caTotalRow.setAlias('ca')
            caTotalRow.fix = 'Центральный аппарат (скорректированный)'
            caTotalRow.getCell('fix').colSpan = 2
            setTotalStyle(caTotalRow)
        }
        rows.add(caTotalRow)

        // добавить итого
        if (totalRow == null) { // если итоговых строк нет, то создать
            totalRow = formData.createDataRow()
            totalRow.setAlias('total')
            totalRow.fix = 'Сбербанк России'
            totalRow.getCell('fix').colSpan = 5
            setTotalStyle(totalRow)

            calcTotalRow(rows, totalRow)
        }
        rows.add(totalRow)
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
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [[:]]
    def index = 0
    for (alias in allColumns) {
        if (alias == 'fix') {
            continue
        }
        headerMapping.add(([(headerRows[0][index ? (index + 1) : 0]): getColumnName(tmpRow, alias)]))
        headerMapping.add(([(headerRows[1][index ? (index + 1) : 0]): (index + 1).toString()]))
        index++
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

    def required = true

    // графа 1
    def colIndex = 0
    // fix
    colIndex++
    // графа 2
    colIndex++
    newRow.regionBank = getRecordIdImport(30L, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 3
    colIndex++
    newRow.regionBankDivision = getRecordIdImport(30L, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 4
    colIndex++
    newRow.divisionName = values[colIndex]
    // графа 5
    colIndex++
    newRow.kpp = values[colIndex]
    // графа 6
    colIndex++
    newRow.propertyPrice = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 7
    colIndex++
    newRow.workersCount = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 8
    colIndex++
    newRow.subjectTaxCredit = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 9
    colIndex++
    newRow.calcFlag = getRecordIdImport(26L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 10
    colIndex++
    newRow.obligationPayTax = getRecordIdImport(25L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 11
    colIndex++
    newRow.baseTaxOf = values[colIndex]

    // графа 12..27
    ['baseTaxOfRub', 'subjectTaxStavka', 'taxSum', 'taxSumOutside', 'taxSumToPay',
            'taxSumToReduction', 'everyMontherPayment1', 'everyMontherPayment2',
            'everyMontherPayment3', 'everyMontherPaymentAfterPeriod', 'everyMonthForKvartalNextPeriod',
            'everyMonthForSecondKvartalNextPeriod', 'everyMonthForThirdKvartalNextPeriod',
            'everyMonthForFourthKvartalNextPeriod', 'minimizeTaxSum', 'amountTax'].each { column ->
        colIndex++
        newRow[column] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    }

    return newRow
}

def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

def getTotalRowFromXls(def alias, def title, def colSpan, def values, def colOffset, def fileRowIndex, def rowIndex, boolean fillCA) {
    def totalRow = formData.createStoreMessagingDataRow()
    totalRow.setIndex(rowIndex)
    totalRow.setImportIndex(fileRowIndex)
    totalRow.setAlias(alias)
    totalRow.fix = title
    totalRow.getCell('fix').colSpan = colSpan
    setTotalStyle(totalRow)

    def required = true

    // графа 1
    def colIndex

    if (fillCA) {
        // графа 2
        colIndex = 2
        totalRow.regionBank = getRecordIdImport(30L, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
        // графа 3
        colIndex = 3
        totalRow.regionBankDivision = getRecordIdImport(30L, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
        // графа 4
        colIndex = 4
        totalRow.divisionName = values[colIndex]
        // графа 5
        colIndex = 5
        totalRow.kpp = values[colIndex]
        // графа 9
        colIndex = 9
        totalRow.calcFlag = getRecordIdImport(26L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
        // графа 10
        colIndex = 10
        totalRow.obligationPayTax = getRecordIdImport(25L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
        // графа 11
        colIndex = 11
        totalRow.baseTaxOf = values[colIndex]
        // графа 27
        colIndex = 27
        totalRow.amountTax = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    }

    // графа 6
    colIndex = 6
    totalRow.propertyPrice = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 7
    colIndex = 7
    totalRow.workersCount = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 8
    colIndex = 8
    totalRow.subjectTaxCredit = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 12..26
    colIndex = 11
    ['baseTaxOfRub', 'subjectTaxStavka', 'taxSum', 'taxSumOutside', 'taxSumToPay',
            'taxSumToReduction', 'everyMontherPayment1', 'everyMontherPayment2',
            'everyMontherPayment3', 'everyMontherPaymentAfterPeriod', 'everyMonthForKvartalNextPeriod',
            'everyMonthForSecondKvartalNextPeriod', 'everyMonthForThirdKvartalNextPeriod',
            'everyMonthForFourthKvartalNextPeriod', 'minimizeTaxSum'].each { column ->
        colIndex++
        totalRow[column] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    }

    return totalRow
}

/**
 * Проверка наличия декларации для текущего department
 */
void checkDeclaration() {
    declarationType = 2    // Тип декларации которую проверяем (Налог на прибыль)
    declaration = declarationService.getLast(declarationType, formData.getDepartmentId(), formData.getReportPeriodId())
    if (declaration != null && declaration.isAccepted()) {
        logger.error("Декларация банка находится в статусе принята")
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка фиксированной.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    allColumns.each {
        row.getCell(it).setStyleAlias('Итоговая')
    }
}

/** Получить сумму столбца (сумма значении всех строк). */
def getSumAll(def dataRows, def columnAlias) {
    return dataRows.sum { it.getAlias() ? BigDecimal.ZERO : it[columnAlias] }
}

// Получить данные сводной
def getFormDataSummaryMap(def ids) {
    def map = [:]
    ids.each{ id ->
        if (!formDataCache[id]) {
            formDataCache[id] = formDataService.getLast(id, com.aplana.sbrf.taxaccounting.model.FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        }
        if (formDataCache[id]) {
            map[id] = formDataCache[id]
        }
    }
    return map
}

def getData(def formData) {
    if (formData == null) {
        return null
    }
    if (formData.id != null && formData.state == WorkflowState.ACCEPTED && !helperCache[formData.id]) {
        helperCache[formData.id] = formDataService.getDataRowHelper(formData)
    }
    return helperCache[formData.id]
}

def getDohIsklPrib(def dataRowsComplex, def dataRowsSimple) {
    def result = 0.0

    if (dataRowsComplex != null) {
        // Код вида доходов = 16440, 16450, 16460, 16480, 16490, 16500, 16510, 16520, 16540, 16550, 16570, 16580, 16660, 16670
        result += getComplexIncomeSumRows9(dataRowsComplex,
                [16440, 16450, 16460, 16480, 16490, 16500, 16510, 16520, 16540, 16550, 16570, 16580, 16660, 16670])
    }
    if (dataRowsSimple != null) {
        // Код вида дохода = 17130, 17140, 17150
        result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field10Sum', [17130, 17140, 17150])
    }
    return getLong(result)
}

@Field
def taxBaseAsDeclaration = null

def getTaxBaseAsDeclaration() {
    if (taxBaseAsDeclaration != null) {
        return taxBaseAsDeclaration
    }
    def empty = 0

    // Данные налоговых форм.

    /** Доходы сложные уровня Банка "Сводная форма начисленных доходов". */
    def dataRowsComplexIncome = getData(getFormDataSummaryMap([302])[302])?.allSaved

    /** Доходы простые уровня Банка "Расшифровка видов доходов, учитываемых в простых РНУ". */
    def dataRowsSimpleIncome = getData(getFormDataSummaryMap([305])[305])?.allSaved
    if (dataRowsSimpleIncome == null) {
        dataRowsSimpleIncome = getData(getFormDataSummaryMap([301])[301])?.allSaved
    }

    /** Расходы сложные уровня Банка "Сводная форма начисленных расходов". */
    def dataRowsComplexConsumption = getData(getFormDataSummaryMap([303])[303])?.allSaved

    /** Расходы простые уровня Банка "Расшифровка видов расходов, учитываемых в простых РНУ". */
    def dataRowsSimpleConsumption = getData(getFormDataSummaryMap(304)[304])?.allCached
    if (dataRowsSimpleConsumption == null) {
        dataRowsSimpleConsumption = getData(getFormDataSummaryMap([310])[310])?.allCached
    }

    /** ВыручРеалТов. Код строки декларации 180. */
    def viruchRealTov = empty
    /** ДохДоговДУИ. Код строки декларации 210. */
    def dohDolgovDUI = empty
    /** ДохДоговДУИ_ВнР. Код строки декларации 211. */
    def dohDolgovDUI_VnR = empty
    /** УбытОбОбслНеобл. Код строки декларации 201. */
    def ubitObObslNeobl = empty
    /** УбытДоговДУИ. Код строки декларации 230. */
    def ubitDogovDUI = empty
    /** УбытПрошПер. Код строки декларации 301. */
    def ubitProshPer = empty
    /** СумБезнадДолг. Код строки декларации 302. */
    def sumBeznalDolg = empty
    /** УбытПриравнВс. Код строки декларации 300. */
    def ubitPriravnVs = ubitProshPer + sumBeznalDolg

    /** ВыручРеалАИ. Код строки декларации 030. Код вида дохода = 10910. */
    def viruchRealAI = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10910]))
    /** УбытРеалАИ. Код строки декларации 060. Код вида расхода = 21420. */
    def ubitRealAI = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21420]))
    /** ЦенРеалПрЗУ. Код строки декларации 240. Код вида дохода = 10950. */
    def cenRealPrZU = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10950]))
    /** УбытРеалПрЗУ. Код строки декларации 260. С 1 квартала 2016 года не заполняется. */
    def ubitRealPrZU = empty
    /** ВыручРеалПТДоСр. Код строки декларации 100. Код вида дохода = 10970. */
    def viruchRealPTDoSr = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10970]))
    /** ВыручРеалПТПосСр. Код строки декларации 110. Код вида дохода = 10870. Не заполняется с 2015 года. */
    def viruchRealPTPosSr = getLong(0)
    /** Убыт1Прев269. Код строки декларации 150. Код вида расхода = 21070. */
    def ubit1Prev269 = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21070]))
    /** Убыт2РеалПТ. Код строки декларации 160. Код вида расхода = 21510. */
    def ubit2RealPT = empty

    /** ВырРеалТовСоб. Код строки декларации 011. */
    def virRealTovSob = getVirRealTovSob(dataRowsComplexIncome, dataRowsSimpleIncome)
    /** ВырРеалИмПрав. Строка декларации 013. Код вида дохода = 10940, 10960, 10980. */
    def virRealImPrav = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10940, 10960, 10980]))
    /** ВырРеалИмПроч. Строка декларации 014. Код вида дохода = 10930. */
    def virRealImProch = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10930]))
    /** ВырРеалВс. Код строки декларации 010. */
    def virRealVs = virRealTovSob + virRealImPrav + virRealImProch
    /** ВырРеалЦБВс. Код строки декларации 020. Код вида дохода = 11040, 11050, 11060, 11070, 11080, 11090, 11100, 11110. */
    def virRealCBVs = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [11040, 11050, 11060, 11070, 11080, 11090, 11100, 11110]))
    /** ВырРеалПред. Код строки декларации 023. */
    def virRealPred = empty
    /** ВыручОп302Ит. Код строки декларации 340. Строка 030 + строка 100 + строка 110 + строка 180 + (строка 210 – строка 211) + строка 240. */
    def viruchOp302It = viruchRealAI + viruchRealPTDoSr + viruchRealPTPosSr + viruchRealTov + dohDolgovDUI - dohDolgovDUI_VnR + cenRealPrZU

    /** ДохРеал, ВырРеалИтог. */
    def dohReal = virRealVs + virRealCBVs + virRealPred + viruchOp302It
    /** ДохВнереал. Код строки декларации 100. */
    def dohVnereal = getDohVnereal(dataRowsComplexIncome, dataRowsSimpleIncome)
    /** ПрямРасхРеал. Код строки декларации 010. */
    def pramRashReal = empty
    /** ПрямРасхТоргВс. Код строки декларации 020. */
    def pramRashTorgVs = empty
    /** КосвРасхВс. Код строки декларации 040. */
    def cosvRashVs = getCosvRashVs(dataRowsComplexConsumption, dataRowsSimpleConsumption)
    /** РасхВнереалВС. Строка 200. */
    def rashVnerealVs = getRashVnerealVs(dataRowsComplexConsumption, dataRowsSimpleConsumption)
    /** РасхВнереал. Строка 200 + строка 300. */
    def rashVnereal = rashVnerealVs + ubitPriravnVs
    /** ОстСтРеалАИ. Код строки декларации 040. Код вида расхода = 21400. */
    def ostStRealAI = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21400]))
    /** РеалИмущПрав. Код строки декларации 059. Код вида расхода = 21080, 21100, 21390. */
    def realImushPrav = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21080, 21100, 21390]))
    /** ПриобрРеалИмущ. Код строки декларации 060. Код вида расхода = 21410. */
    def priobrRealImush = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21410]))
    /* АктивРеалПред. Код строки декларации 061. */
    def activRealPred = empty
    /** ПриобРеалЦБ. Код строки декларации 070. Код вида расхода = 21230, 21240, 21250, 21260, 21270, 21280, 21290, 21300, 21310. */
    def priobrRealCB = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21230, 21240, 21250, 21260, 21270, 21280, 21290, 21300, 21310]))

    /** УбытПрошОбсл. Код строки декларации 090. */
    def ubitProshObsl = empty
    /** СтоимРеалПТДоСр. Код строки декларации 120. Код вида расхода = 21050. */
    def stoimRealPTDoSr = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21050]))
    /** СтоимРеалПТПосСр. Код строки декларации 130. Код вида расхода = 21470. Не заполняется с 2015 года. */
    def stoimRealPTPosSr = getLong(0)
    /** РасхРеалТов. Код строки декларации 190. */
    def rashRealTov = empty
    /** РасхДоговДУИ. Код строки декларации 220. */
    def rashDolgovDUI = empty
    /** РасхДоговДУИ_ВнР. Код строки декларации 221. */
    def rashDolgovDUI_VnR = empty
    /** НеВозЗатрПрЗУ. Код строки декларации 250. С 1 квартала 2016 года не заполняется. */
    def neVozZatrPrZU = empty
    /** РасхОпер32, РасхОп302Ит. Код строки декларации = 080 или 350. */
    def rashOper32 = ostStRealAI + stoimRealPTDoSr + stoimRealPTPosSr + rashRealTov + (rashDolgovDUI - rashDolgovDUI_VnR) + neVozZatrPrZU
    /** УбытРеалАмИм. Код строки декларации 100. Код вида расхода = 21110. */
    def ubitRealAmIm = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21110]))
    /** УбытРеалЗемУч. Код строки декларации 110. */
    def ubitRealZemUch = empty
    /** НадбПокПред. Код строки декларации 120. */
    def nadbPokPred = empty
    /** РасхУмРеал, РасхПризнИтого. Код строки декларации 130. */
    def rashUmReal = pramRashReal + pramRashTorgVs + cosvRashVs + realImushPrav +
            priobrRealImush + activRealPred + priobrRealCB + rashOper32 + ubitProshObsl +
            ubitRealAmIm + ubitRealZemUch + nadbPokPred
    /** Убытки, УбытОп302. Код строки декларации 360. Cтрока 060 + строка 150 + строка 160 + строка 201+ строка 230 + строка 260. */
    def ubitki = ubitRealAI + ubit1Prev269 + ubit2RealPT + ubitObObslNeobl + ubitDogovDUI + ubitRealPrZU
    /** ПрибУб. */
    def pribUb = dohReal + dohVnereal - rashUmReal - rashVnereal + ubitki
    /** ДохИсклПриб. */
    def dohIsklPrib = getDohIsklPrib(dataRowsComplexIncome, dataRowsSimpleIncome)
    // НалБаза строка 60 - строка 70 - строка 80 - строка 90 - строка 400 (Приложение №2 к Листу 02)
    def nalBaza = pribUb - dohIsklPrib - 0 - 0 - 0
    /** НалБазаИсч, НалБазаОрг. */
    def nalBazaIsch = getNalBazaIsch(nalBaza, 0)

    taxBaseAsDeclaration = nalBazaIsch
    return taxBaseAsDeclaration
}


def getDohVnereal(def dataRows, def dataRowsSimple) {
    def result = 0.0

    // Код вида дохода = 14120, 15170, 15180, 15190, 15200, 15210, 15220, 15230,
    // 16110, 16120, 16130, 16140, 16280, 16290, 16300, 16310, 16420, 16430, 16440, 16450, 16460, 16470, 16480,
    // 16490, 16500, 16510, 16520, 16530, 16540, 16550, 16560, 16570, 16580, 16590, 16600, 16660, 16670, 16970,
    // 16980, 16990, 17000, 17010, 17020, 17030, 17040, 17050, 17060, 17070, 17080, 17090, 17100, 17110, 17120
    result += getComplexIncomeSumRows9(dataRows, [14120, 15170, 15180, 15190, 15200, 15210, 15220, 15230,
            16110, 16120, 16130, 16140, 16280, 16290, 16300, 16310, 16420, 16430, 16440, 16450, 16460, 16470, 16480,
            16490, 16500, 16510, 16520, 16530, 16540, 16550, 16560, 16570, 16580, 16590, 16600, 16660, 16670, 16970,
            16980, 16990, 17000, 17010, 17020, 17030, 17040, 17050, 17060, 17070, 17080, 17090, 17100, 17110, 17120])

    // Код вида дохода = 14000, 14010, 14020, 14030, 14040, 14050, 14060, 14070, 14080,
    // 14090, 14100, 14110, 14130, 14140, 14150, 14160, 14170, 14180, 14190, 14200, 14210, 14220, 14230, 14240,
    // 14250, 14260, 14270, 14280, 14290, 14300, 14310, 14320, 14330, 14340, 14350, 14360, 14370, 14380, 14390,
    // 14400, 14410, 14420, 14430, 14440, 14450, 14460, 14470, 14480, 14490, 14500, 14510, 14520, 14530, 14540,
    // 14550, 14560, 14570, 14580, 14590, 14600, 14610, 14620, 14630, 14640, 14650, 14660, 14670, 14680, 14690,
    // 14700, 14710, 14720, 14730, 14740, 14750, 14760, 14770, 14780, 14790, 14800, 14810, 14820, 14830, 14840,
    // 14850, 14860, 14870, 14880, 14890, 14900, 14910, 14920, 14930, 14940, 14950, 14960, 14970, 14980, 14990,
    // 15000, 15010, 15020, 15030, 15040, 15050, 15060, 15070, 15080, 15090, 15100, 15110, 15120, 15130, 15140,
    // 15150, 15160, 15260, 15270, 15280, 15290, 15300, 15310, 15320, 15330, 15340, 15350, 15360, 15370, 15380,
    // 15390, 15400, 15410, 15420, 15430, 15440, 15450, 15460, 15470, 15480, 15490, 15500, 15510, 15520, 15530,
    // 15540, 15550, 15560, 15570, 15580, 15590, 15600, 15610, 15620, 15630, 15640, 15650, 15660, 15670, 15680,
    // 15690, 15700, 15710, 15720, 15730, 15740, 15750, 15760, 15770, 15780, 15790, 15800, 15810, 15820, 15830,
    // 15840, 15850, 15860, 15870, 15880, 15890, 15900, 15910, 15920, 15930, 15940, 15950, 15960, 15970, 15980,
    // 15990, 16000, 16010, 16020, 16030, 16040, 16050, 16060, 16070, 16080, 16090, 16100, 16150, 16160, 16170,
    // 16180, 16190, 16200, 16210, 16220, 16230, 16240, 16250, 16260, 16270, 16320, 16330, 16340, 16350, 16360,
    // 16370, 16380, 16390, 16400, 16410, 16610, 16620, 16630, 16640, 16650, 16680, 16690, 16700, 16710, 16720,
    // 16730, 16740, 16750, 16760, 16770, 16780, 16790, 16800, 16810, 16820, 16830, 16840, 16850, 16860, 16870,
    // 16880, 16890, 16900, 16910, 16920, 16930, 16940, 16950, 16960, 17160, 17170, 17180, 17190, 17200, 17210,
    // 17220, 17230, 17240, 17250, 17260, 17270, 17280, 17290, 17300, 17310
    result += getSimpleIncomeSumRows8(dataRowsSimple, [14000, 14010, 14020, 14030, 14040, 14050, 14060, 14070, 14080,
            14090, 14100, 14110, 14130, 14140, 14150, 14160, 14170, 14180, 14190, 14200, 14210, 14220, 14230, 14240,
            14250, 14260, 14270, 14280, 14290, 14300, 14310, 14320, 14330, 14340, 14350, 14360, 14370, 14380, 14390,
            14400, 14410, 14420, 14430, 14440, 14450, 14460, 14470, 14480, 14490, 14500, 14510, 14520, 14530, 14540,
            14550, 14560, 14570, 14580, 14590, 14600, 14610, 14620, 14630, 14640, 14650, 14660, 14670, 14680, 14690,
            14700, 14710, 14720, 14730, 14740, 14750, 14760, 14770, 14780, 14790, 14800, 14810, 14820, 14830, 14840,
            14850, 14860, 14870, 14880, 14890, 14900, 14910, 14920, 14930, 14940, 14950, 14960, 14970, 14980, 14990,
            15000, 15010, 15020, 15030, 15040, 15050, 15060, 15070, 15080, 15090, 15100, 15110, 15120, 15130, 15140,
            15150, 15160, 15260, 15270, 15280, 15290, 15300, 15310, 15320, 15330, 15340, 15350, 15360, 15370, 15380,
            15390, 15400, 15410, 15420, 15430, 15440, 15450, 15460, 15470, 15480, 15490, 15500, 15510, 15520, 15530,
            15540, 15550, 15560, 15570, 15580, 15590, 15600, 15610, 15620, 15630, 15640, 15650, 15660, 15670, 15680,
            15690, 15700, 15710, 15720, 15730, 15740, 15750, 15760, 15770, 15780, 15790, 15800, 15810, 15820, 15830,
            15840, 15850, 15860, 15870, 15880, 15890, 15900, 15910, 15920, 15930, 15940, 15950, 15960, 15970, 15980,
            15990, 16000, 16010, 16020, 16030, 16040, 16050, 16060, 16070, 16080, 16090, 16100, 16150, 16160, 16170,
            16180, 16190, 16200, 16210, 16220, 16230, 16240, 16250, 16260, 16270, 16320, 16330, 16340, 16350, 16360,
            16370, 16380, 16390, 16400, 16410, 16610, 16620, 16630, 16640, 16650, 16680, 16690, 16700, 16710, 16720,
            16730, 16740, 16750, 16760, 16770, 16780, 16790, 16800, 16810, 16820, 16830, 16840, 16850, 16860, 16870,
            16880, 16890, 16900, 16910, 16920, 16930, 16940, 16950, 16960, 17160, 17170, 17180, 17190, 17200, 17210,
            17220, 17230, 17240, 17250, 17260, 17270, 17280, 17290, 17300, 17310])

    // Код вида дохода = 14190, 14200, 14210, 14220, 14240, 14250, 15240, 15250, 17130, 17140,
    // 17150, 17250, 17260, 17270, 17280, 17300, 17310, 17320
    // графа 5
    result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field10Sum', [14190, 14200, 14210, 14220, 14240,
            14250, 15240, 15250, 17130, 17140, 17150, 17250, 17260, 17270, 17280, 17300, 17310, 17320])
    // графа 6
    // Код вида дохода = 14190, 14200, 14210, 14220, 14240, 14250, 17130, 17140, 17150, 17250, 17260, 17270, 17280, 17300, 17310, 17320
    result -= getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field12Accepted', [14190, 14200, 14210, 14220, 14240,
            14250, 17130, 17140, 17150, 17250, 17260, 17270, 17280, 17300, 17310, 17320])

    return getLong(result)
}

def getComplexIncomeSumRows9(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'incomeTypeId', 'incomeTaxSumS', codes)
}

def getComplexConsumptionSumRows9(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'consumptionTypeId', 'consumptionTaxSumS', codes)
}

def getSimpleIncomeSumRows8(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'incomeTypeId', 'rnu4Field5Accepted', codes)
}

def getSumRowsByCol(def dataRows, def columnCode, def columnSum, def codes) {
    def result = 0
    if (!dataRows) {
        return result
    }
    dataRows.each { row ->
        def cell = row.getCell(columnSum)
        if (row.getCell(columnCode).value in (String [])codes && !cell.hasValueOwner()) {
            result += (cell.value ?: 0)
        }
    }
    return result
}

def getVirRealTovSob(def dataRows, def dataRowsSimple) {
    def result = 0.0

    // Код вида дохода = 10830
    result += getComplexIncomeSumRows9(dataRows, [10830])

    // Код вида дохода = 10000, 10010, 10020, 10030, 10040, 10050, 10060, 10070, 10080,
    // 10090, 10100, 10110, 10120, 10130, 10140, 10150, 10160, 10170, 10180, 10190, 10200, 10210, 10220, 10230,
    // 10240, 10250, 10260, 10270, 10280, 10290, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10370, 10380,
    // 10390, 10400, 10410, 10420, 10430, 10440, 10450, 10460, 10470, 10480, 10490, 10500, 10510, 10520, 10530,
    // 10540, 10550, 10560, 10570, 10580, 10590, 10600, 10610, 10620, 10630, 10640, 10650, 10660, 10670, 10680,
    // 10690, 10700, 10710, 10720, 10730, 10740, 10750, 10780, 10790, 10800, 10810, 10820, 10840, 10850, 10860,
    // 10870, 10890, 10900, 10990, 11000, 11010, 11020, 11030, 11160, 11170, 11180, 11190, 11200, 11210, 11220,
    // 11230, 11240, 11250, 11260, 11270, 11280, 11290, 11300, 11310
    result += getSimpleIncomeSumRows8(dataRowsSimple, [10000, 10010, 10020, 10030, 10040, 10050, 10060, 10070, 10080,
            10090, 10100, 10110, 10120, 10130, 10140, 10150, 10160, 10170, 10180, 10190, 10200, 10210, 10220, 10230,
            10240, 10250, 10260, 10270, 10280, 10290, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10370, 10380,
            10390, 10400, 10410, 10420, 10430, 10440, 10450, 10460, 10470, 10480, 10490, 10500, 10510, 10520, 10530,
            10540, 10550, 10560, 10570, 10580, 10590, 10600, 10610, 10620, 10630, 10640, 10650, 10660, 10670, 10680,
            10690, 10700, 10710, 10720, 10730, 10740, 10750, 10780, 10790, 10800, 10810, 10820, 10840, 10850, 10860,
            10870, 10890, 10900, 10990, 11000, 11010, 11020, 11030, 11160, 11170, 11180, 11190, 11200, 11210, 11220,
            11230, 11240, 11250, 11260, 11270, 11280, 11290, 11300, 11310])

    // Код вида доходов = 10160, 10230, 10240, 10250, 10260, 10270, 10280, 10330, 10760, 10770, 10880, 11170, 11300, 11310
    def codes = [10160, 10230, 10240, 10250, 10260, 10270, 10280, 10330, 10760, 10770, 10880, 11170, 11300, 11310]

    // графа 5
    result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field10Sum', codes)
    // графа 6
    result -= getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field12Accepted', codes)

    return getLong(result)
}

def getCosvRashVs(def dataRows, def dataRowsSimple) {
    def result = 0

    // Код вида расхода = 20020, 20030, 20050, 20130, 20200, 20210, 20280, 20470, 20490,
    // 20500, 20510, 20630, 20820, 20850, 20930, 20940, 20950, 20960, 20970, 20980, 21170
    result += getComplexConsumptionSumRows9(dataRows, [20020, 20030, 20050, 20130, 20200, 20210, 20280, 20470, 20490,
            20500, 20510, 20630, 20820, 20850, 20930, 20940, 20950, 20960, 20970, 20980, 21170])

    // Код вида расхода = 20000, 20010, 20040, 20060, 20070, 20080, 20090, 20100, 20110,
    // 20120, 20140, 20150, 20160, 20170, 20180, 20190, 20220, 20230, 20240, 20250, 20260, 20270, 20290, 20300,
    // 20310, 20320, 20330, 20340, 20350, 20360, 20370, 20380, 20400, 20410, 20430, 20450, 20530, 20540, 20550,
    // 20560, 20570, 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660, 20670, 20680, 20690, 20700, 20710,
    // 20720, 20730, 20740, 20750, 20760, 20770, 20780, 20790, 20800, 20810, 20830, 20860, 20870, 20880, 20890,
    // 20900, 20910, 20920, 20990, 21000, 21010, 21020, 21040, 21120, 21130, 21140, 21150, 21160, 21180, 21190,
    // 21200, 21210, 21220, 21340, 21350, 21360, 21370, 21380, 21430, 21440, 21450, 21460, 21470, 21480, 21490
    result += getSimpleConsumptionSumRows8(dataRowsSimple, [20000, 20010, 20040, 20060, 20070, 20080, 20090, 20100, 20110,
            20120, 20140, 20150, 20160, 20170, 20180, 20190, 20220, 20230, 20240, 20250, 20260, 20270, 20290, 20300,
            20310, 20320, 20330, 20340, 20350, 20360, 20370, 20380, 20400, 20410, 20430, 20450, 20530, 20540, 20550,
            20560, 20570, 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660, 20670, 20680, 20690, 20700, 20710,
            20720, 20730, 20740, 20750, 20760, 20770, 20780, 20790, 20800, 20810, 20830, 20860, 20870, 20880, 20890,
            20900, 20910, 20920, 20990, 21000, 21010, 21020, 21040, 21120, 21130, 21140, 21150, 21160, 21180, 21190,
            21200, 21210, 21220, 21340, 21350, 21360, 21370, 21380, 21430, 21440, 21450, 21460, 21470, 21480, 21490])

    // графа 5
    // Код вида дохода = 20010, 20110, 20120, 20160,
    // 20170, 20180, 20190, 20220, 20230, 20240, 20250, 20260, 20270, 20290, 20300, 20310, 20320, 20330, 20340,
    // 20350, 20360, 20370, 20380, 20400, 20410, 20450, 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660,
    // 20670, 20680, 20690, 20700, 20730, 20740, 20750, 20760, 20780, 20800, 20810, 20830, 21040, 21380, 21440,
    // 21450, 21460, 21470
    result += getSumRowsByCol(dataRowsSimple, 'consumptionTypeId', 'rnu7Field10Sum', [20010, 20110, 20120, 20160,
            20170, 20180, 20190, 20220, 20230, 20240, 20250, 20260, 20270, 20290, 20300, 20310, 20320, 20330, 20340,
            20350, 20360, 20370, 20380, 20400, 20410, 20450, 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660,
            20670, 20680, 20690, 20700, 20730, 20740, 20750, 20760, 20780, 20800, 20810, 20830, 21040, 21380, 21440,
            21450, 21460, 21470])

    // графа 6
    // Код вида дохода = 20010, 20110, 20120, 20160,
    // 20170, 20180, 20190, 20220, 20230, 20240, 20250, 20260, 20270, 20290, 20300, 20310, 20320, 20330, 20340,
    // 20350, 20360, 20370, 20380, 20400, 20410, 20450, 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660,
    // 20670, 20680, 20690, 20700, 20730, 20740, 20750, 20760, 20780, 20800, 20810, 20830, 21040, 21380, 21440,
    // 21450, 21460, 21470
    result -= getSumRowsByCol(dataRowsSimple, 'consumptionTypeId', 'rnu7Field12Accepted', [20010, 20110, 20120, 20160,
            20170, 20180, 20190, 20220, 20230, 20240, 20250, 20260, 20270, 20290, 20300, 20310, 20320, 20330, 20340,
            20350, 20360, 20370, 20380, 20400, 20410, 20450, 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660,
            20670, 20680, 20690, 20700, 20730, 20740, 20750, 20760, 20780, 20800, 20810, 20830, 21040, 21380, 21440,
            21450, 21460, 21470])

    return getLong(result)
}

def getSimpleConsumptionSumRows8(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'consumptionTypeId', 'rnu5Field5Accepted', codes)
}

def getRashVnerealVs(def dataRows, def dataRowsSimple) {
    def result = 0.0

    // графа 9
    // Код вида расхода = 24660, 24670, 25680, 25690, 25830, 26310, 26320, 26330, 26340,
    // 26360, 26370, 26820, 26830, 26840, 26850, 26860, 26870, 26880, 26890, 26900, 27060
    result += getComplexConsumptionSumRows9(dataRows, [24660, 24670, 25680, 25690, 25830, 26310, 26320, 26330, 26340,
            26360, 26370, 26820, 26830, 26840, 26850, 26860, 26870, 26880, 26890, 26900, 27060])

    // графа 8 + 5 - 6
    // Код вида расхода = 24000, 24010, 24020, 24030, 24040, 24050, 24060, 24070, 24080, 24090, 24100, 24110, 24120, 24130, 24140,
    // 24150, 24160, 24170, 24180, 24190, 24200, 24210, 24220, 24230, 24240, 24250, 24260, 24270, 24280, 24290,
    // 24300, 24310, 24320, 24330, 24340, 24350, 24360, 24370, 24380, 24390, 24400, 24410, 24420, 24430, 24440,
    // 24450, 24460, 24470, 24480, 24490, 24500, 24510, 24520, 24530, 24540, 24550, 24560, 24570, 24580, 24590,
    // 24600, 24610, 24620, 24630, 24640, 24680, 24690, 24700, 24710, 24720, 24730, 24740, 24750, 24760, 24770,
    // 24780, 24790, 24800, 24810, 24820, 24830, 24840, 24850, 24860, 24870, 24880, 24890, 24900, 24910, 24920,
    // 24930, 24940, 24950, 24960, 24970, 24980, 24990, 25000, 25010, 25020, 25030, 25040, 25050, 25060, 25070,
    // 25080, 25090, 25100, 25110, 25120, 25130, 25140, 25150, 25160, 25170, 25180, 25190, 25200, 25210, 25220,
    // 25230, 25240, 25250, 25260, 25270, 25280, 25290, 25300, 25310, 25320, 25330, 25340, 25350, 25360, 25370,
    // 25380, 25390, 25400, 25410, 25420, 25430, 25440, 25450, 25460, 25470, 25480, 25490, 25500, 25510, 25520,
    // 25530, 25540, 25550, 25560, 25570, 25580, 25590, 25600, 25610, 25620, 25630, 25640, 25650, 25660, 25670,
    // 25700, 25710, 25720, 25730, 25740, 25750, 25760, 25770, 25780, 25790, 25800, 25810, 25820, 25840, 25850,
    // 25860, 25870, 25880, 25890, 25900, 25910, 25920, 25930, 25940, 25950, 25960, 25970, 25980, 25990, 26000,
    // 26010, 26020, 26030, 26040, 26050, 26060, 26070, 26080, 26090, 26100, 26110, 26120, 26130, 26140, 26150,
    // 26160, 26170, 26180, 26190, 26200, 26210, 26220, 26230, 26240, 26250, 26260, 26270, 26280, 26290, 26300,
    // 26350, 26380, 26390, 26400, 26410, 26420, 26430, 26440, 26450, 26460, 26470, 26480, 26490, 26500, 26510,
    // 26520, 26530, 26540, 26550, 26560, 26570, 26580, 26590, 26600, 26610, 26620, 26630, 26640, 26650, 26660,
    // 26670, 26680, 26690, 26700, 26710, 26720, 26730, 26740, 26750, 26760, 26770, 26780, 26790, 26800, 26810,
    // 27010, 27020, 27030, 27040, 27050, 27070, 27080
    def knu = [24000, 24010, 24020, 24030, 24040, 24050, 24060, 24070, 24080, 24090, 24100, 24110, 24120, 24130, 24140,
            24150, 24160, 24170, 24180, 24190, 24200, 24210, 24220, 24230, 24240, 24250, 24260, 24270, 24280, 24290,
            24300, 24310, 24320, 24330, 24340, 24350, 24360, 24370, 24380, 24390, 24400, 24410, 24420, 24430, 24440,
            24450, 24460, 24470, 24480, 24490, 24500, 24510, 24520, 24530, 24540, 24550, 24560, 24570, 24580, 24590,
            24600, 24610, 24620, 24630, 24640, 24680, 24690, 24700, 24710, 24720, 24730, 24740, 24750, 24760, 24770,
            24780, 24790, 24800, 24810, 24820, 24830, 24840, 24850, 24860, 24870, 24880, 24890, 24900, 24910, 24920,
            24930, 24940, 24950, 24960, 24970, 24980, 24990, 25000, 25010, 25020, 25030, 25040, 25050, 25060, 25070,
            25080, 25090, 25100, 25110, 25120, 25130, 25140, 25150, 25160, 25170, 25180, 25190, 25200, 25210, 25220,
            25230, 25240, 25250, 25260, 25270, 25280, 25290, 25300, 25310, 25320, 25330, 25340, 25350, 25360, 25370,
            25380, 25390, 25400, 25410, 25420, 25430, 25440, 25450, 25460, 25470, 25480, 25490, 25500, 25510, 25520,
            25530, 25540, 25550, 25560, 25570, 25580, 25590, 25600, 25610, 25620, 25630, 25640, 25650, 25660, 25670,
            25700, 25710, 25720, 25730, 25740, 25750, 25760, 25770, 25780, 25790, 25800, 25810, 25820, 25840, 25850,
            25860, 25870, 25880, 25890, 25900, 25910, 25920, 25930, 25940, 25950, 25960, 25970, 25980, 25990, 26000,
            26010, 26020, 26030, 26040, 26050, 26060, 26070, 26080, 26090, 26100, 26110, 26120, 26130, 26140, 26150,
            26160, 26170, 26180, 26190, 26200, 26210, 26220, 26230, 26240, 26250, 26260, 26270, 26280, 26290, 26300,
            26350, 26380, 26390, 26400, 26410, 26420, 26430, 26440, 26450, 26460, 26470, 26480, 26490, 26500, 26510,
            26520, 26530, 26540, 26550, 26560, 26570, 26580, 26590, 26600, 26610, 26620, 26630, 26640, 26650, 26660,
            26670, 26680, 26690, 26700, 26710, 26720, 26730, 26740, 26750, 26760, 26770, 26780, 26790, 26800, 26810,
            27010, 27020, 27030, 27040, 27050, 27070, 27080]
    result += getCalculatedSimpleConsumption(dataRowsSimple, knu)

    // графа 9
    // Код вида расхода = 24650, 26910, 26920, 26930, 26940, 26950, 26960, 26970, 26980, 26990, 27000
    result -= getComplexConsumptionSumRows9(dataRows, [24650, 26910, 26920, 26930, 26940, 26950, 26960, 26970, 26980, 26990, 27000])

    return getLong(result)
}

/** Подсчет простых расходов: сумма(графа 8 + графа 5 - графа 6). */
def getCalculatedSimpleConsumption(def dataRowsSimple, def codes) {
    def result = 0
    if (dataRowsSimple == null) {
        return result
    }
    dataRowsSimple.each { row ->
        if (row.getCell('consumptionTypeId').value in (String [])codes) {
            result +=
                    (row.rnu5Field5Accepted ?: 0) +
                            (row.rnu7Field10Sum ?: 0) -
                            (row.rnu7Field12Accepted ?: 0)
        }
    }
    return result
}


def getNalBazaIsch(def row100, def row110) {
    def result
    if (row100 != null && row110 != null && (row100 < 0 || row100 == row110)) {
        result = 0.0
    } else {
        result = row100 - row110
    }
    return getLong(result)
}

// Получить округленное, целочисленное значение.
def getLong(def value) {
    if (value == null) {
        return 0
    }
    return roundValue(value, 0)
}

/**
 * Посчитать значение графы 14..17, 23, 24.
 *
 * @param row строка
 * @param sumNal значение из настроек подраздления "Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде"
 * @param reportPeriod отчетный период
 */
void calcColumnFrom14To24(def prevDataRows, def row, def sourceRow, def sumNal, def reportPeriod) {
    // графа 15
    if (sumNal == null || sourceRow.baseTaxOf == null || !checkColumn11(sourceRow.baseTaxOf)) {
        row.taxSumOutside = 0
    } else {
        row.taxSumOutside = roundValue(roundValue(sumNal * 0.9, 0) * new BigDecimal(sourceRow.baseTaxOf) / 100, 0)
    }

    // графа 16
    if (sourceRow.taxSum == null || sourceRow.subjectTaxCredit == null || row.taxSumOutside == null) {
        row.taxSumToPay = 0
    } else {
        row.taxSumToPay = (sourceRow.taxSum > sourceRow.subjectTaxCredit + row.taxSumOutside ?
            sourceRow.taxSum - (sourceRow.subjectTaxCredit + row.taxSumOutside) : 0)
    }

    // графа 17
    if (sourceRow.taxSum == null || sourceRow.subjectTaxCredit == null || row.taxSumOutside == null) {
        row.taxSumToReduction = 0
    } else {
        row.taxSumToReduction = ((sourceRow.taxSum < (sourceRow.subjectTaxCredit + row.taxSumOutside)) ?
                ((sourceRow.subjectTaxCredit + row.taxSumOutside) - sourceRow.taxSum) : 0)
    }

    // Значения граф этого же подразделения в форме пред. периода
    def prev23 = null
    def prev24 = null

    if ((reportPeriod.order == 2 || reportPeriod.order == 3) && sourceRow.regionBankDivision != null && prevDataRows != null) {
        for (def prevRow : prevDataRows) {
            if (sourceRow.regionBankDivision.equals(prevRow.regionBankDivision)) {
                // графа 23 пред. периода
                prev23 = prevRow.everyMonthForSecondKvartalNextPeriod
                // графа 24 пред. периода
                prev24 = prevRow.everyMonthForThirdKvartalNextPeriod
                break
            }
        }
    }
    // Если не нашлось, считаем 0
    prev23 = prev23 == null ? 0 : prev23
    prev24 = prev24 == null ? 0 : prev24

    // графа 23
    row.everyMonthForSecondKvartalNextPeriod = (reportPeriod.order == 1 ? sourceRow.taxSum : prev23)

    // графа 24
    if (reportPeriod.order != 2 || sourceRow.taxSum == null || row.everyMonthForSecondKvartalNextPeriod == null || sourceRow.everyMonthForKvartalNextPeriod == null) {
        row.everyMonthForThirdKvartalNextPeriod = prev24
    } else {
        row.everyMonthForThirdKvartalNextPeriod =
                ((reportPeriod.order == 2) ? (sourceRow.taxSum - row.everyMonthForSecondKvartalNextPeriod - sourceRow.everyMonthForKvartalNextPeriod) : prev24)
    }

    // графа 25
    if (reportPeriod.order != 3 || sourceRow.taxSum == null || row.everyMonthForThirdKvartalNextPeriod == null) {
        row.everyMonthForFourthKvartalNextPeriod = 0
    } else {
        row.everyMonthForFourthKvartalNextPeriod =
                ((reportPeriod.order == 3) ? (sourceRow.taxSum - row.everyMonthForThirdKvartalNextPeriod) : 0)
    }
}

/**
 * Найти строку ЦА
 */
def findCA(def dataRows) {
    def resultRow = null
    if (dataRows != null) {
        for (def row : dataRows) {
            if (row.regionBank == centralId) {
                resultRow = row
            }
        }
    }
    return resultRow
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(BigDecimal value, def precision) {
    value.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

/** Получить строки за предыдущий отчетный период. */
def getPrevDataRows() {
    def prevFormData = formDataService.getFormDataPrev(formData)
    return (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allSaved : null)
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    // есть итоговые строки
    if (dataRows.size() > 2) {
        sortRows(refBookService, logger, dataRows, [dataRows.find { it.getAlias() == 'ca' }], dataRows.find { it.getAlias() == 'total' }, true)
        if (saveInDB) {
            dataRowHelper.saveSort()
        } else {
            updateIndexes(dataRows)
        }
    }
}

// Получить строки формы
def getDataRows(def formId, def kind) {
    //def formId = 421
    //def formDataKind = FormDataKind.SUMMARY
    def departmentId = formData.departmentId
    def reportPeriodId = formData.reportPeriodId
    def periodOrder = formData.periodOrder
    def sourceFormData = formDataService.getLast(formId, kind, departmentId, reportPeriodId, periodOrder, formData.comparativePeriodId, formData.accruing)
    if (sourceFormData != null && sourceFormData.id != null)
        return formDataService.getDataRowHelper(sourceFormData)?.allSaved
    return null
}

/**
 * Получить значение ячейки фиксированной строки из налоговой формы.
 *
 * @param dataRows строки нф
 * @param columnName название столбца
 * @param alias алиас строки
 * @return значение столбца
 *
 */
def getAliasFromForm(def dataRows, def columnName, def alias) {
    if (dataRows != null && !dataRows.isEmpty()) {
        def aliasRow = getDataRow(dataRows, alias)
        return aliasRow.getCell(columnName).value
    }
    return 0
}

/** Получение провайдера с использованием кеширования. */
def getProvider(def long providerId) {
    return formDataService.getRefBookProvider(refBookFactory, providerId, providerCache)
}

// Получение параметров подразделения, форма настроек которого будет использоваться
// для получения данных (согласно алгоритму 6.2.5.8.7.1)
def getDepParam(def departmentParam) {
    def departmentId = departmentParam.get(RefBook.RECORD_ID_ALIAS).numberValue as int
    def departmentType = departmentService.get(departmentId).getType()
    def depParam
    if (departmentType.equals(departmentType.TERR_BANK)) {
        depParam = departmentParam
    } else {
        def tbCode = (Integer) departmentParam.get('PARENT_ID').getReferenceValue()
        if (tbCode == null) {
            return null
        }
        def taxPlaningTypeCode = departmentService.get(tbCode).getType().MANAGEMENT.getCode()
        depParam = getProvider(30).getRecords(getReportPeriodEndDate(), null, "PARENT_ID = ${departmentParam.get('PARENT_ID').getReferenceValue()} and TYPE = $taxPlaningTypeCode", null).get(0)
    }
    return depParam
}

// Получение параметров (справочник 330)
def getIncomeParamTable(def depParam) {
    def depId = (depParam?.get(RefBook.RECORD_ID_ALIAS)?.numberValue ?: -1) as long
    def incomeParam
    if (depId != -1) {
        incomeParam = getProvider(33).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $depId", null)
    }
    if (incomeParam != null && !incomeParam.isEmpty()) {
        def link = incomeParam.get(0).record_id.value
        def incomeParamTable = getProvider(330).getRecords(getReportPeriodEndDate() - 1, null, "LINK = $link", null)
        return incomeParamTable
    }
    return null
}

@Field
def reportPeriod = null

def getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return reportPeriod
}

void afterLoad() {
    // прибыль сводная
    if (binding.variables.containsKey("specialPeriod") && formData.kind == FormDataKind.SUMMARY) {
        // для справочников начало от 01.01.year (для прибыли start_date)
        specialPeriod.calendarStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
}