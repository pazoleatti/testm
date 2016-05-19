package form_template.income.advanceDistribution.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации.
 * formTemplateId=1500
 *
 * @author akadyrgulov
 * @author <a href="mailto:Ramil.Timerbaev@aplana.com">Тимербаев Рамиль</a>
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
// графа 10  - obligationPayTax
// графа 11 - baseTaxOf
// графа 12 - baseTaxOfRub
// графа 13 - subjectTaxStavka
// графа 14 - taxSum
// графа 15 - taxSumOutside
// графа 16 - taxSumToPay
// графа 17 - taxSumToReduction
// графа 18 - everyMontherPaymentAfterPeriod
// графа 19 - everyMonthForKvartalNextPeriod
// графа 20 - everyMonthForSecondKvartalNextPeriod
// графа 21 - everyMonthForThirdKvartalNextPeriod
// графа 22 - everyMonthForFourthKvartalNextPeriod
// графа 23 - minimizeTaxSum
// графа 24 - amountTax

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
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicalCheck()
        // на стороне сервера будет выполнен compose
        break
// обобщить
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
                  'taxSumOutside', 'taxSumToPay', 'taxSumToReduction',
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
    def taxBase
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

    // расчет графы 2..4, 8..21
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

        // графа 14..21
        calcColumnFrom14To21(prevDataRows, row, row, sumNal, reportPeriod)
    }

    // нужен отдельный расчет
    for (row in dataRows) {
        calc18_19(prevDataRows, dataRows, row, reportPeriod)
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

    // После расчета фикс. строки "ЦА(скоррект)" необходимо пересчитать для граф 12, 14, 15, 17, 18, 19
    // фиксированную строку итогов: сумма всех строк, кроме строки "ЦА" и строки итогов
    reCalcTotalRow(dataRows, totalRow)

    updateIndexes(dataRows)
}

void reCalcTotalRow(def dataRows, def totalRow) {
    def isTaxPeriod = reportPeriodService.get(formData.reportPeriodId).order == 4

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
}

void calcTotalRow(def dataRows, def totalRow) {
    def isTaxPeriod = reportPeriodService.get(formData.reportPeriodId).order == 4
    calcTotalSum(dataRows, totalRow, isTaxPeriod ? (totalColumns - 'everyMontherPaymentAfterPeriod') : totalColumns)
    // графа 11
    totalRow.baseTaxOf = roundValue(dataRows.sum{ row ->
        String value = row.baseTaxOf
        (row.getAlias() == null && value?.isBigDecimal()) ? new BigDecimal(value) : BigDecimal.ZERO
    } ?: BigDecimal.ZERO, 15).toPlainString()
}

void calcCaTotalRow(def dataRows, def prevDataRows, def caTotalRow, def totalRow, def taxBase, def sumNal) {
    def reportPeriod = getReportPeriod()

    def isOldCalc = reportPeriod.taxPeriod.year < 2015 || (reportPeriod.taxPeriod.year == 2015 && reportPeriod.order < 3)

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

        def tempValue
        if (isOldCalc) {
            tempValue = caRow.everyMontherPaymentAfterPeriod
        } else {
            switch (reportPeriod.order) {
                case 1: // Период формы «1 квартал»:
                    // Принимает значение «графы 18» подразделения «Центральный аппарат»
                    tempValue = caRow.everyMontherPaymentAfterPeriod
                    break
                case 4: // Период формы "год"
                    tempValue = null
                    break
                default: // Период формы «полугодие / 9 месяцев»:
                    // «Графа 18» = Значение «графы 18» подразделения «Центральный аппарат» + (Значение итоговой строки по «графе 18» - (Значение итоговой строки по «графе 14» - Значение итоговой строки по «графе 14» формы предыдущего периода))
                    def prevTaxSum = prevDataRows?.find {it.getAlias() == 'total'}?.taxSum ?: 0
                    tempValue = (caRow.everyMontherPaymentAfterPeriod ?: 0) + ((totalRow.everyMontherPaymentAfterPeriod ?: 0) - ((totalRow.taxSum ?: 0) - prevTaxSum))
            }
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

        // графа 19
        if (reportPeriod.order == 3) {
            // «графа 19» = «графа 18» строки "ЦА (скорр.)"
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
    def departmentParam
    def divisionName
    if (row.regionBankDivision != null) {
        departmentParam = getRefBookValue(30, row.regionBankDivision)
    }
    def depParam = getDepParam(departmentParam)
    def incomeParamTable = getIncomeParamTable(depParam)
    for (int i = 0; i < incomeParamTable.size(); i++) {
        if (row.kpp != null && row.kpp != '') {
            if (incomeParamTable?.get(i)?.KPP?.stringValue == row.kpp) {
                divisionName = incomeParamTable?.get(i)?.ADDITIONAL_NAME?.stringValue
                break
            }
        }
    }
    return divisionName
}

def calc9(def row) {
    def departmentParam
    def calcFlag
    if (row.regionBankDivision != null) {
        departmentParam = getRefBookValue(30, row.regionBankDivision)
    }
    def depParam = getDepParam(departmentParam)
    def incomeParamTable = getIncomeParamTable(depParam)
    for (int i = 0; i < incomeParamTable.size(); i++) {
        if (row.kpp != null && row.kpp != '') {
            if (incomeParamTable?.get(i)?.KPP?.stringValue == row.kpp) {
                calcFlag = incomeParamTable?.get(i)?.TYPE?.referenceValue
                break
            }
        }
    }
    return calcFlag
}

def calc10(def row) {
    def departmentParam
    def obligationPayTax
    if (row.regionBankDivision != null) {
        departmentParam = getRefBookValue(30, row.regionBankDivision)
    }
    def depParam = getDepParam(departmentParam)
    def incomeParamTable = getIncomeParamTable(depParam)
    for (int i = 0; i < incomeParamTable.size(); i++) {
        if (row.kpp != null && row.kpp != '') {
            if (incomeParamTable?.get(i)?.KPP?.stringValue == row.kpp) {
                obligationPayTax = incomeParamTable?.get(i)?.OBLIGATION?.referenceValue
                break
            }
        }
    }
    return obligationPayTax
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
    def temp = 0
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
    def temp = 0
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

def calc18_19 (def prevDataRows, def dataRows, def row, def reportPeriod) {
    def tmp
    // графа 18
    switch (reportPeriod.order) {
        case 1: //«графа 18» = «графа 14»
            tmp = row.taxSum
            break
        case 4:
            tmp = null
            break
        default:
            // (Сумма всех нефиксированных строк по «графе 14» - Сумма всех нефиксированных строк по «графе 14» из предыдущего периода) * («графа 14» / Сумма всех нефиксированных строк по «графе 14»)
            def currentSum = dataRows?.sum { (it.getAlias() == null) ? (it.taxSum ?: 0) : 0 } ?: 0
            def previousSum = prevDataRows?.sum { (it.getAlias() == null) ? (it.taxSum ?: 0) : 0 } ?: 0
            // остальные
            if (currentSum) {
                tmp = (currentSum - previousSum) * row.taxSum / currentSum
            }
    }
    row.everyMontherPaymentAfterPeriod = tmp

    // графа 19
    row.everyMonthForKvartalNextPeriod = ((reportPeriod.order == 3) ? row.everyMontherPaymentAfterPeriod : 0)
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

    def tmpRows = dataRows.findAll { !it.getAlias() }
    def propertyPriceSumm = getSumAll(tmpRows, "propertyPrice")
    def workersCountSumm = getSumAll(tmpRows, "workersCount")

    // Распределяемая налоговая база за отчетный период.
    def taxBase
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
        calcColumnFrom14To21(prevDataRows, needValue, row, sumNal, reportPeriod)
        calc18_19(prevDataRows, dataRows, needValue, reportPeriod)
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

    def isTaxPeriod = reportPeriodService.get(formData.reportPeriodId).order == 4
    errorColumns = ((isTaxPeriod ? (totalColumns - 'everyMontherPaymentAfterPeriod') : totalColumns) + 'baseTaxOf').findAll { alias ->
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
    int COLUMN_COUNT = 24
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
        return;
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
        updateIndexes(rows);
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
            def isTaxPeriod = reportPeriodService.get(formData.reportPeriodId).order == 4
            calcTotalSum(rows, totalRow, isTaxPeriod ? (totalColumns - 'everyMontherPaymentAfterPeriod') : totalColumns)
            totalRow.baseTaxOf = roundValue(rows.sum { row ->
                String value = row.baseTaxOf
                (row.getAlias() == null && value?.isBigDecimal()) ? new BigDecimal(value) : BigDecimal.ZERO
            } ?: BigDecimal.ZERO, 15).toPlainString()
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
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[0].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [[:]]
    def index = 0
    for (alias in allColumns) {
        if (alias == 'fix') {
            continue
        }
        headerMapping.add(([(headerRows[0][index ? (index + 1) : 0]): getColumnName(tmpRow, alias)]))
        headerMapping.add(([(headerRows[1][index == 0 ? 0 : (index + 1)]): (index + 1).toString()]))
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
    // графа 12
    colIndex++
    newRow.baseTaxOfRub = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 13
    colIndex++
    newRow.subjectTaxStavka = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 14
    colIndex++
    newRow.taxSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 15
    colIndex++
    newRow.taxSumOutside = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 16
    colIndex++
    newRow.taxSumToPay = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 17
    colIndex++
    newRow.taxSumToReduction = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 18
    colIndex++
    newRow.everyMontherPaymentAfterPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 19
    colIndex++
    newRow.everyMonthForKvartalNextPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 20
    colIndex++
    newRow.everyMonthForSecondKvartalNextPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 21
    colIndex++
    newRow.everyMonthForThirdKvartalNextPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 22
    colIndex++
    newRow.everyMonthForFourthKvartalNextPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 23
    colIndex++
    newRow.minimizeTaxSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 24
    colIndex++
    newRow.amountTax = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

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
    def colIndex = 0

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
        // графа 24
        colIndex = 24
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
    // графа 12
    colIndex = 12
    totalRow.baseTaxOfRub = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 13
    colIndex = 13
    totalRow.subjectTaxStavka = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 14
    colIndex = 14
    totalRow.taxSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 15
    colIndex = 15
    totalRow.taxSumOutside = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 16
    colIndex = 16
    totalRow.taxSumToPay = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 17
    colIndex = 17
    totalRow.taxSumToReduction = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 18
    colIndex = 18
    totalRow.everyMontherPaymentAfterPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 19
    colIndex = 19
    totalRow.everyMonthForKvartalNextPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 20
    colIndex = 20
    totalRow.everyMonthForSecondKvartalNextPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 21
    colIndex = 21
    totalRow.everyMonthForThirdKvartalNextPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 22
    colIndex = 22
    totalRow.everyMonthForFourthKvartalNextPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 23
    colIndex = 23
    totalRow.minimizeTaxSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

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

/**
 * Получить сумму столбца (за исключением значении фиксированных строк).
 *
 * @param data данные нф (helper)
 * @param columnAlias алилас столбца по которому считать сумму
 * @return
 */
def getSum(def dataRows, def columnAlias) {
    def from = 0
    def to = dataRows.size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

/**
 * Получить сумму столбца (сумма значении всех строк).
 */
def getSumAll(def dataRows, def columnAlias) {
    def from = 0
    def to = dataRows.size() - 1
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
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
    if(formData == null) {
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
        // Код вида доходов = 13655, 13660, 13665, 13675, 13680, 13685, 13690,
        // 13695, 13705, 13710, 13780, 13785, 13790
        result += getComplexIncomeSumRows9(dataRowsComplex,
                [13655, 13660, 13665, 13675, 13680, 13685, 13690, 13695, 13705, 13710, 13780, 13785, 13790])
    }
    if (dataRowsSimple != null) {
        // Код вида дохода = 14000
        result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu4Field5Accepted', [14000, 14010, 14015])
    }
    return getLong(result)
}

def getTaxBaseAsDeclaration() {

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

    /** Сведения о суммах налога на прибыль, уплаченного Банком за рубежом */
    //    def dataRowsSum = getData(getFormDataSummary(421))?.allSaved
    //    if (dataRowsSum == null) {// TODO wtf?
    //    }

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


    /** ВыручРеалАИ. Код строки декларации 030. Код вида дохода = 10840. */
    def viruchRealAI = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10840]))
    /** УбытРеалАИ. Код строки декларации 060. Код вида расхода = 21780. */
    def ubitRealAI = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21780]))
    /** ЦенРеалПрЗУ. Код строки декларации 240. Код вида дохода = 10890. */
    def cenRealPrZU = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10890]))
    /** УбытРеалПрЗУ. Код строки декларации 260. Код вида расхода = 21390. */
    def ubitRealPrZU = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21390]))
    /** ВыручРеалПТДоСр. Код строки декларации 100. Код вида дохода = 10860. */
    def viruchRealPTDoSr = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10860]))
    /** ВыручРеалПТПосСр. Код строки декларации 110. Код вида дохода = 10870. */
    def viruchRealPTPosSr = getLong(0)// TODO getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10870]))
    /** Убыт1Прев269. Код строки декларации 150. Код вида расхода = 21500. */
    def ubit1Prev269 = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21500]))
    /** Убыт2РеалПТ. Код строки декларации 160. Код вида расхода = 21510. */
    def ubit2RealPT = empty //TODO getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21510]))

    /** ВырРеалТовСоб. Код строки декларации 011. */
    def virRealTovSob = getVirRealTovSob(dataRowsComplexIncome, dataRowsSimpleIncome)
    /** ВырРеалИмПрав. Строка декларации 013. Код вида дохода = 10855, 10880, 10900. */
    def virRealImPrav = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10855, 10870, 10880, 10900]))//TODO
    /** ВырРеалИмПроч. Строка декларации 014. Код вида дохода = 10850. */
    def virRealImProch = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10850]))
    /** ВырРеалВс. Код строки декларации 010. */
    def virRealVs = virRealTovSob + virRealImPrav + virRealImProch
    /** ВырРеалЦБВс. Код строки декларации 020. Код вида дохода = 11180, 11190, 11200, 11210, 11220, 11230, 11240, 11250, 11260. */
    def virRealCBVs = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [11180, 11190, 11200, 11210, 11220, 11230, 11240, 11250, 11260]))
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
    /** ОстСтРеалАИ. Код строки декларации 040. Код вида расхода = 21760. */
    def ostStRealAI = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21760]))
    /** РеалИмущПрав. Код строки декларации 059. Код вида расхода = 21450, 21740, 21750. */
    def realImushPrav = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21450, 21470, 21740, 21750]))// TODO
    /** ПриобрРеалИмущ. Код строки декларации 060. Код вида расхода = 21770. */
    def priobrRealImush = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21770]))
    /* АктивРеалПред. Код строки декларации 061. */
    def activRealPred = empty
    /** ПриобРеалЦБ. Код строки декларации 070. Код вида расхода = 21662, 21664, 21666, 21668, 21670, 21672, 21674, 21676, 21678, 21680. */
    def priobrRealCB = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21662, 21664, 21666, 21668, 21670, 21672, 21674, 21676, 21678, 21680]))

    /** УбытПрошОбсл. Код строки декларации 090. */
    def ubitProshObsl = empty
    /** СтоимРеалПТДоСр. Код строки декларации 120. Код вида расхода = 21460. */
    def stoimRealPTDoSr = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21460]))
    /** СтоимРеалПТПосСр. Код строки декларации 130. Код вида расхода = 21470. */
    def stoimRealPTPosSr = getLong(0)//TODO
    /** РасхРеалТов. Код строки декларации 190. */
    def rashRealTov = empty
    /** РасхДоговДУИ. Код строки декларации 220. */
    def rashDolgovDUI = empty
    /** РасхДоговДУИ_ВнР. Код строки декларации 221. */
    def rashDolgovDUI_VnR = empty
    /** НеВозЗатрПрЗУ. Код строки декларации 250. Код вида расхода = 21385. */
    def neVozZatrPrZU = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21385]))
    /** РасхОпер32, РасхОп302Ит. Код строки декларации = 080 или 350. */
    def rashOper32 = ostStRealAI + stoimRealPTDoSr + stoimRealPTPosSr + rashRealTov + (rashDolgovDUI - rashDolgovDUI_VnR) + neVozZatrPrZU
    /** УбытРеалАмИм. Код строки декларации 100. Код вида расхода = 21520, 21530. */
    def ubitRealAmIm = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21520, 21530]))
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

    return nalBazaIsch
}


def getDohVnereal(def dataRows, def dataRowsSimple) {
    def result = 0.0

    // Код вида дохода = 11405, 11410, 11415, 13040, 13045, 13050, 13055, 13060, 13065,
    // 13070, 13090, 13100, 13110, 13120, 13250, 13650, 13655, 13660, 13665, 13670,
    // 13675, 13680, 13685, 13690, 13695, 13700, 13705, 13710, 13715, 13720, 13780,
    // 13785, 13790, 13940, 13950, 13960, 13970, 13980, 13990, 14140, 14170, 14180,
    // 14190, 14200, 14210, 14220, 14230, 14240, 14250, 14260, 14270, 14280, 14290,
    // 14300, 14310, 14320
    result += getComplexIncomeSumRows9(dataRows, [11405, 11410, 11415, 13040, 13045, 13050, 13055,
                                                  13060, 13065, 13070, 13090, 13100, 13110, 13120, 13250, 13650, 13655, 13660, 13665,
                                                  13670, 13675, 13680, 13685, 13690, 13695, 13700, 13705, 13710, 13715, 13720, 13780,
                                                  13785, 13790, 13940, 13950, 13960, 13970, 13980, 13990, 14140, 14170, 14180, 14190,
                                                  14200, 14210, 14220, 14230, 14240, 14250, 14260, 14270, 14280, 14290, 14300, 14310, 14320])

    // Код вида дохода = 11380, 11385, 11390, 11395, 11400, 11420, 11430, 11840, 11850, 11855,
    // 11860, 11870, 11880, 11930, 11970, 12000, 12010, 12030, 12050, 12070, 12090, 12110, 12130,
    // 12150, 12170, 12190, 12210, 12230, 12250, 12270, 12290, 12320, 12340, 12360, 12390, 12400,
    // 12410, 12420, 12430, 12830, 12840, 12850, 12860, 12870, 12880, 12890, 12900, 12910, 12920,
    // 12930, 12940, 12950, 12960, 12970, 12980, 12985, 12990, 13000, 13010, 13020, 13030, 13035,
    // 13080, 13130, 13140, 13150, 13160, 13170, 13180, 13190, 13230, 13240, 13290, 13300, 13310, 13320, 13330,
    // 13340, 13400, 13410, 13725, 13730, 13920, 13925, 13930, 14000, 14010, 14015, 14020, 14030, 14040,
    // 14050, 14060, 14070, 14080, 14090, 14100, 14110, 14120, 14130, 14150, 14160
    result += getSimpleIncomeSumRows8(dataRowsSimple, [11380, 11385, 11390, 11395, 11400, 11420,
                                                       11430, 11840, 11850, 11855, 11860, 11870, 11880, 11930, 11970, 12000, 12010, 12030,
                                                       12050, 12070, 12090, 12110, 12130, 12150, 12170, 12190, 12210, 12230, 12250, 12270,
                                                       12290, 12320, 12340, 12360, 12390, 12400, 12410, 12420, 12430, 12830, 12840, 12850,
                                                       12860, 12870, 12880, 12890, 12900, 12910, 12920, 12930, 12940, 12950, 12960, 12970,
                                                       12980, 12985, 12990, 13000, 13010, 13020, 13030, 13035, 13080, 13130, 13140, 13150, 13160, 13170,
                                                       13180, 13190, 13230, 13240, 13290, 13300, 13310, 13320, 13330, 13340, 13400, 13410,
                                                       13725, 13730, 13920, 13925, 13930, 14000, 14010, 14015, 14020, 14030, 14040, 14050, 14060,
                                                       14070, 14080, 14090, 14100, 14110, 14120, 14130, 14150, 14160])

    // Код вида дохода = 11860, 11870, 11880, 11930, 11970, 12000, 13930, 14020, 14030, 14040, 14050,
    // 14060, 14070, 14080, 14090, 14100, 14110, 14130, 14150, 14160
    def codes = [11860, 11870, 11880, 11930, 11970, 12000, 13930, 14020, 14030, 14040, 14050,
                 14060, 14070, 14080, 14090, 14100, 14110, 14130, 14150, 14160]
    // графа 5
    result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field10Sum', codes)
    // графа 6
    result -= getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field12Accepted', codes)

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

    // Код вида дохода = 10633, 10634, 10650, 10670
    result += getComplexIncomeSumRows9(dataRows, [10633, 10634, 10650, 10670])

    // Код вида дохода = 10001, 10006, 10041, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10370,
    // 10380, 10390, 10450, 10460, 10470, 10480, 10490, 10571, 10580, 10590, 10600, 10610, 10630,
    // 10631, 10632, 10640, 10680, 10690, 10740, 10744, 10748, 10752, 10756, 10760, 10770, 10790,
    // 10800, 11140, 11150, 11160, 11170, 11320, 11325, 11330, 11335, 11340, 11350, 11360, 11370, 11375
    result += getSimpleIncomeSumRows8(dataRowsSimple, [10001, 10006, 10041, 10300, 10310, 10320,
                                                       10330, 10340, 10350, 10360, 10370, 10380, 10390, 10450, 10460, 10470, 10480, 10490,
                                                       10571, 10580, 10590, 10600, 10610, 10630, 10631, 10632, 10640, 10680, 10690, 10740,
                                                       10744, 10748, 10752, 10756, 10760, 10770, 10790, 10800, 11140, 11150, 11160, 11170,
                                                       11320, 11325, 11330, 11335, 11340, 11350, 11360, 11370, 11375])

    // Код вида доходов = 10001, 10006, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10470,
    // 10480, 10490, 10571, 10590, 10610, 10640, 10680, 10690, 11340, 11350, 11370, 11375
    def codes = [10001, 10006, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10470, 10480,
                 10490, 10571, 10590, 10610, 10640, 10680, 10690, 11340, 11350, 11370, 11375]

    // графа 5
    result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field10Sum', codes)
    // графа 6
    result -= getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field12Accepted', codes)

    return getLong(result)
}

def getCosvRashVs(def dataRows, def dataRowsSimple) {
    def result = 0

    // Код вида расхода = 20320, 20321, 20470, 20750, 20755, 20760, 20765, 20770,
    // 20775, 20780, 20785, 21210, 21280, 21345, 21355, 21365, 21370, 21375, 21380, 21630, 21640
    result += getComplexConsumptionSumRows9(dataRows, [20320, 20321, 20470, 20750, 20755, 20760, 20765,
                                                       20770, 20775, 20780, 20785, 21210, 21280, 21345, 21355, 21365, 21370, 21375, 21380, 21630, 21640])

    // Код вида расхода = 20291, 20300, 20310, 20330, 20332, 20334, 20336, 20338,
    // 20339, 20340, 20360, 20364, 20368, 20370, 20430, 20434, 20438, 20440, 20442,
    // 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475,
    // 20480, 20485, 20490, 20500, 20510, 20520, 20530, 20540, 20550, 20690, 20694,
    // 20698, 20700, 20710, 20810, 20812, 20814, 20816, 20820, 20825, 20830, 20840,
    // 20850, 20860, 20870, 20880, 20890, 20920, 20940, 20945, 20950, 20960, 20970,
    // 21020, 21025, 21030, 21050, 21055, 21060, 21065, 21080, 21130, 21140, 21150,
    // 21154, 21158, 21170, 21270, 21290, 21295, 21300, 21305, 21310, 21315, 21320,
    // 21325, 21340, 21350, 21360, 21400, 21405, 21410, 21580, 21590, 21600, 21610,
    // 21620, 21660, 21700, 21710, 21720, 21730, 21790, 21800, 21810
    result += getSimpleConsumptionSumRows8(dataRowsSimple, [20291, 20300, 20310, 20330, 20332, 20334,
                                                            20336, 20338, 20339, 20340, 20360, 20364, 20368, 20370, 20430, 20434, 20438, 20440,
                                                            20442, 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475,
                                                            20480, 20485, 20490, 20500, 20510, 20520, 20530, 20540, 20550, 20690, 20694, 20698,
                                                            20700, 20710, 20810, 20812, 20814, 20816, 20820, 20825, 20830, 20840, 20850, 20860,
                                                            20870, 20880, 20890, 20920, 20940, 20945, 20950, 20960, 20970, 21020, 21025, 21030,
                                                            21050, 21055, 21060, 21065, 21080, 21130, 21140, 21150, 21154, 21158, 21170, 21270,
                                                            21290, 21295, 21300, 21305, 21310, 21315, 21320, 21325, 21340, 21350, 21360, 21400,
                                                            21405, 21410, 21580, 21590, 21600, 21610, 21620, 21660, 21700, 21710, 21720, 21730,
                                                            21790, 21800, 21810])

    // графа 5
    // Код вида дохода = 20300, 20360, 20370, 20430, 20434, 20438, 20440, 20442, 20446, 20448, 20450,
    // 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530,
    // 20540, 20550, 20690, 20694, 20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830,
    // 20840, 20850, 20870, 20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055,
    // 21060, 21065, 21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580,
    // 21590, 21620, 21660, 21700, 21710, 21730, 21790, 21800, 21810
    result += getSumRowsByCol(dataRowsSimple, 'consumptionTypeId', 'rnu7Field10Sum', [20300, 20360, 20370, 20430,
                                                                                      20434, 20438, 20440, 20442, 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460,
                                                                                      20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530, 20540, 20550, 20690, 20694,
                                                                                      20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830, 20840, 20850, 20870,
                                                                                      20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055, 21060, 21065,
                                                                                      21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580, 21590,
                                                                                      21620, 21660, 21700, 21710, 21730, 21790, 21800, 21810])

    // графа 6
    // Код вида дохода = 20300, 20360, 20370, 20430, 20434, 20438, 20440, 20442, 20446, 20448, 20450,
    // 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530,
    // 20540, 20550, 20690, 20694, 20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830,
    // 20840, 20850, 20870, 20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055,
    // 21060, 21065, 21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580,
    // 21590, 2162021660, 21700, 21710, 21730, 21790, 21800, 21810
    result -= getSumRowsByCol(dataRowsSimple, 'consumptionTypeId', 'rnu7Field12Accepted', [20300, 20360, 20370, 20430,
                                                                                           20434, 20438, 20440, 20442, 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460,
                                                                                           20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530, 20540, 20550, 20690, 20694,
                                                                                           20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830, 20840, 20850, 20870,
                                                                                           20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055, 21060, 21065,
                                                                                           21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580, 21590,
                                                                                           2162021660, 21700, 21710, 21730, 21790, 21800, 21810])

    return getLong(result)
}
def getSimpleConsumptionSumRows8(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'consumptionTypeId', 'rnu5Field5Accepted', codes)
}

def getRashVnerealVs(def dataRows, def dataRowsSimple) {
    def result = 0.0

    // Код вида расхода = 22500, 22505, 22585, 22590, 22595, 22660, 22664, 22668,
    // 22670, 22690, 22695, 22700, 23120, 23130, 23140, 23240 - графа 9
    result += getComplexConsumptionSumRows9(dataRows, [22500, 22505, 22585, 22590, 22595, 22660, 22664, 22668,
                                                       22670, 22690, 22695, 23120, 23130, 23140, 23240])

    // Код вида расхода = 22000, 22010, 22020, 22030, 22040, 22050, 22060, 22070, 22080, 22090, 22100, 22110,
    // 22120, 22130, 22140, 22150, 22160, 22170, 22180, 22190, 22200, 22210, 22220, 22230, 22240, 22250, 22260,
    // 22270, 22280, 22290, 22300, 22310, 22320, 22330, 22340, 22350, 22360, 22370, 22380, 22385, 22390, 22395,
    // 22400, 22405, 22410, 22415, 22420, 22425, 22430, 22435, 22440, 22445, 22450, 22455, 22460, 22465, 22470,
    // 22475, 22480, 22485, 22490, 22496, 22498, 22530, 22534, 22538, 22540, 22544, 22548, 22550, 22560, 22565,
    // 22570, 22575, 22580, 22600, 22610, 22640, 22680, 22710, 22715, 22720, 22750, 22760, 22800, 22810, 22840,
    // 22850, 22860, 22870, 23040, 23050, 23100, 23110, 23200, 23210, 23220, 23230, 23250, 23260, 23270, 23280
    def knu = [ 22000, 22010, 22020, 22030, 22040, 22050, 22060, 22070,
                22080, 22090, 22100, 22110, 22120, 22130, 22140, 22150, 22160, 22170, 22180,
                22190, 22200, 22210, 22220, 22230, 22240, 22250, 22260, 22270, 22280, 22290,
                22300, 22310, 22320, 22330, 22340, 22350, 22360, 22370, 22380, 22385, 22390,
                22395, 22400, 22405, 22410, 22415, 22420, 22425, 22430, 22435, 22440, 22445,
                22450, 22455, 22460, 22465, 22470, 22475, 22480, 22485, 22490, 22496, 22498,
                22530, 22534, 22538, 22540, 22544, 22548, 22550, 22560, 22565, 22570, 22575,
                22580, 22600, 22610, 22640, 22680, 22710, 22715, 22720, 22750, 22760, 22800,
                22810, 22840, 22850, 22860, 22870, 23040, 23050, 23100, 23110, 23200, 23210,
                23220, 23230, 23250, 23260, 23270, 23280 ]
    result += getCalculatedSimpleConsumption(dataRowsSimple, knu)

    // Код вида расхода = 22492, 23150, 23160, 23170 - графа 9
    result -= getComplexConsumptionSumRows9(dataRows, [22492, 23150, 23160, 23170])

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
 * Расчитываем распределяемая налоговая база за отчётный период.
 *
 * @author ekuvshinov
 */
def getTaxBase() {
    def result = 0
    // расходы сложные
    def dataOutcomeComplex = getData(getFormDataSummaryMap([303])[303])

    // Расходы сложные
    if (dataOutcomeComplex != null) {
        for (row in dataOutcomeComplex.allSaved) {
            String khy = row.getCell('consumptionTypeId').hasValueOwner() ? row.getCell('consumptionTypeId').getValueOwner().value : row.getCell('consumptionTypeId').value

            // 9
            BigDecimal consumptionTaxSumS = (BigDecimal) (row.getCell('consumptionTaxSumS').hasValueOwner() ? row.getCell('consumptionTaxSumS').getValueOwner().value : row.getCell('consumptionTaxSumS').value)
            consumptionTaxSumS = consumptionTaxSumS ?: 0
            if (khy in ['21770']) {
                result += consumptionTaxSumS
            }
            if (khy in ['21662', '21664', '21666', '21668', '21670', '21672', '21674', '21676', '21678', '21680']) {
                result -= consumptionTaxSumS
            }
            if (khy in ['21760']) {
                result -= consumptionTaxSumS
            }
            if (khy in ['21460']) {
                result -= consumptionTaxSumS
            }
            if (khy in ['21385']) {
                result -= consumptionTaxSumS
            }
        }
    }
    return (result < 0 ? 0 : result)
}

/**
 * Посчитать значение графы 14..21.
 *
 * @param row строка
 * @param sumNal значение из настроек подраздления "Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде"
 * @param reportPeriod отчетный период
 */
void calcColumnFrom14To21(def prevDataRows, def row, def sourceRow, def sumNal, def reportPeriod) {
    // графа 15
    if (sumNal == null || sourceRow.baseTaxOf == null || !checkColumn11(sourceRow.baseTaxOf)) {
        row.taxSumOutside = 0
    } else {
        row.taxSumOutside = roundValue(sumNal * 0.9 * new BigDecimal(sourceRow.baseTaxOf) / 100, 0)
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
    def prev19 = null
    def prev20 = null

    if ((reportPeriod.order == 2 || reportPeriod.order == 3) && sourceRow.regionBankDivision != null && prevDataRows != null) {
        for (def prevRow : prevDataRows) {
            if (sourceRow.regionBankDivision.equals(prevRow.regionBankDivision)) {
                // графа 20 пред. периода
                prev19 = prevRow.everyMonthForSecondKvartalNextPeriod
                // графа 21 пред. периода
                prev20 = prevRow.everyMonthForThirdKvartalNextPeriod
                break
            }
        }
    }
    // Если не нашлось, считаем 0
    prev19 = prev19 == null ? 0 : prev19
    prev20 = prev20 == null ? 0 : prev20

    // графа 20
    row.everyMonthForSecondKvartalNextPeriod = (reportPeriod.order == 1 ? sourceRow.taxSum : prev19)

    // графа 21
    if (reportPeriod.order != 2 || sourceRow.taxSum == null || row.everyMonthForSecondKvartalNextPeriod == null || sourceRow.everyMonthForKvartalNextPeriod == null) {
        row.everyMonthForThirdKvartalNextPeriod = prev20
    } else {
        row.everyMonthForThirdKvartalNextPeriod =
                ((reportPeriod.order == 2) ? (sourceRow.taxSum - row.everyMonthForSecondKvartalNextPeriod - sourceRow.everyMonthForKvartalNextPeriod) : prev20)
    }

    // графа 22
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
 * Получить значение атрибута строки справочника.
 *
 * @param record строка справочника
 * @param alias алиас
 */
def getValue(def record, def alias) {
    def value = record.get(alias)
    switch (value.getAttributeType()) {
        case RefBookAttributeType.DATE:
            return value.getDateValue()
        case RefBookAttributeType.NUMBER:
            return value.getNumberValue()
        case RefBookAttributeType.STRING:
            return value.getStringValue()
        case RefBookAttributeType.REFERENCE:
            return value.getReferenceValue()
    }
    return null
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
            updateIndexes(dataRows);
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

/**
 * Получение провайдера с использованием кеширования.
 *
 * @param providerId
 * @return
 */
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
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
