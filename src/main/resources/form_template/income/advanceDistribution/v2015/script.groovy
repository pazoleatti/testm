package form_template.income.advanceDistribution.v2015

import com.aplana.sbrf.taxaccounting.model.Department
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
    case FormDataEvent.CHECK:
        logicalCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicalCheckAfterCalc()
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
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
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

@Field
def formDataCache = [:]
@Field
def helperCache = [:]

@Field
def summaryMap = [301 : "Доходы, учитываемые в простых РНУ", 302 : "Сводная форма начисленных доходов",
                  303 : "Сводная форма начисленных расходов", 304 : "Расходы, учитываемые в простых РНУ"]

@Field
def baseTaxOfPattern = "[0-9]{1,3}(\\.[0-9]{0,15})?"

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

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

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
        taxBase = roundValue(getTaxBase(), 0)
    }
    // Отчётный период.
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
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
            def depId = depParam.get(RefBook.RECORD_ID_ALIAS).numberValue as int

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
        calcColumnFrom14To21(prevDataRows, row, sumNal, reportPeriod)
    }

    // нужен отдельный расчет
    for (row in dataRows) {
        calc18_19(prevDataRows, dataRows, row, reportPeriod)
    }

    // Сортировка
    // отсортировать можно только после расчета графы regionBank
    dataRows.sort { a, b ->
        def regionBankA = getRefBookValue(30, a.regionBank)?.NAME?.stringValue
        def regionBankB = getRefBookValue(30, b.regionBank)?.NAME?.stringValue
        if (regionBankA == regionBankB) {
            def regionBankDivisionA = getRefBookValue(30, a.regionBankDivision)?.NAME?.stringValue
            def regionBankDivisionB = getRefBookValue(30, b.regionBankDivision)?.NAME?.stringValue
            return (regionBankDivisionA <=> regionBankDivisionB)
        }
        return (regionBankA <=> regionBankB)
    }

    // добавить строку ЦА (скорректрированный) (графа 1..22)
    def caTotalRow = formData.createDataRow()
    caTotalRow.setAlias('ca')
    caTotalRow.fix = 'Центральный аппарат (скорректированный)'
    caTotalRow.getCell('fix').colSpan = 2
    setTotalStyle(caTotalRow)
    dataRows.add(caTotalRow)

    // добавить итого (графа 5..7, 10, 11, 13..22)
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Сбербанк России'
    totalRow.getCell('fix').colSpan = 5
    setTotalStyle(totalRow)
    calcTotalSum(dataRows, totalRow, totalColumns)
    totalRow.baseTaxOf = dataRows.sum{ row ->
        String value = row.baseTaxOf
        (row.getAlias() == null && value?.isBigDecimal()) ? new BigDecimal(value) : BigDecimal.ZERO
    }.toString()
    dataRows.add(totalRow)

    // найти строку ЦА
    def caRow = findCA(dataRows)
    if (caRow != null) {
        // расчеты для строки ЦА (скорректированный)
        ['number', 'regionBankDivision', 'divisionName', 'kpp', 'propertyPrice', 'workersCount', 'subjectTaxCredit',
         'calcFlag', 'obligationPayTax', 'baseTaxOf', 'subjectTaxStavka', 'taxSum', 'taxSumToPay',
         'taxSumToReduction', 'everyMontherPaymentAfterPeriod', 'everyMonthForKvartalNextPeriod',
         'everyMonthForSecondKvartalNextPeriod', 'everyMonthForThirdKvartalNextPeriod',
         'everyMonthForFourthKvartalNextPeriod'].each { alias ->
            caTotalRow.getCell(alias).setValue(caRow.getCell(alias).getValue(), caTotalRow.getIndex())
        }

        if (formDataEvent != FormDataEvent.COMPOSE) {
            // графа 12
            caTotalRow.baseTaxOfRub = taxBase - totalRow.baseTaxOfRub + caRow.baseTaxOfRub
        }

        // графа 14
        caTotalRow.taxSumOutside = 0.9 * sumNal - totalRow.taxSumOutside + caRow.taxSumOutside
    }

    dataRowHelper.save(dataRows)

    sortFormDataRows()
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

    long centralId = 113 // ID Центрального аппарата.
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
        temp = (row.propertyPrice / propertyPriceSumm * 100 + row.workersCount / workersCountSumm * 100) / 2
    }
    return roundValue(temp, 15).toString()
}

def calc12(def row, def taxBase) {
    if (row.baseTaxOf != null && checkFormat(row.baseTaxOf, baseTaxOfPattern) && taxBase != null) {
        return roundValue((18 / 20) * taxBase * new BigDecimal(row.baseTaxOf) / 100, 0)
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
                tmp = (currentSum - previousSum) * (row.taxSum / currentSum)
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def departmentParam
    def fieldNumber = 0

    dataRows.eachWithIndex { row, i ->
        if (isFixedRow(row)) {
            return
        }
        fieldNumber++
        def index = row.getIndex()
        def errorMsg = "Строка ${index}: "
        if (row.regionBankDivision != null) {
            departmentParam = getRefBookValue(30, row.regionBankDivision)
        }
        if (departmentParam == null || departmentParam.isEmpty()) {
            logger.error(errorMsg + "Не найдено подразделение территориального банка!")
            return
        } else {
            long centralId = 113 // ID Центрального аппарата.
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
        def depId = depParam.get(RefBook.RECORD_ID_ALIAS).numberValue as int ?: -1
        def departmentName = depParam?.NAME?.stringValue ?: "Не задано"
        def incomeParam = getProvider(33).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $depId", null)
        def incomeParamTable = getIncomeParamTable(depParam)

        // 2. Проверка наличия формы настроек подразделения
        if (incomeParam == null || incomeParam.isEmpty()) {
            rowServiceException(row, errorMsg + "Для подразделения «${departmentName}» не создана форма настроек подразделений!")
        }

        // 3. Проверка наличия строки с «КПП» в табличной части формы настроек подразделения
        // 4. Проверка наличия значения «Наименование для Приложения №5» в форме настроек подразделения
        for (int k = 0; k < incomeParamTable.size(); k++) {
            if (row.kpp != null && row.kpp != '') {
                if (incomeParamTable?.get(k)?.KPP?.stringValue == row.kpp) {
                    if (incomeParamTable?.get(k)?.ADDITIONAL_NAME?.stringValue == null) {
                        rowServiceException(row, errorMsg + "Для подразделения «${departmentName}» на форме настроек подразделений по КПП «${row.kpp}» отсутствует значение атрибута «Наименование для «Приложения №5»!")
                    }
                    if (incomeParamTable?.get(k)?.TYPE?.referenceValue == null) {
                        rowServiceException(row, errorMsg + "Для подразделения «${departmentName}» на форме настроек подразделений по КПП «${row.kpp}» отсутствует значение атрибута «Признак расчета»!")
                    }
                    if (incomeParamTable?.get(k)?.OBLIGATION?.referenceValue == null) {
                        rowServiceException(row, errorMsg + "Для подразделения «${departmentName}» на форме настроек подразделений по КПП «${row.kpp}» отсутствует значение атрибута «Обязанность по уплате налога»!")
                    }
                    break
                }
                if (k == incomeParamTable.size() - 1) {
                    rowServiceException(row, errorMsg + "Для подразделения «${departmentName}» на форме настроек подразделений отсутствует строка с КПП «${row.kpp}»!")
                }
            }
        }
    }
    if (formDataEvent != FormDataEvent.COMPOSE) {
        summaryMap.each { key, value ->
            def formDataSummary = getFormDataSummary(key)
            if (formDataSummary == null) {
                logger.error("Сводная налоговая форма «$value» в подразделении «${formDataDepartment.name}» не создана!")
            } else if (getData(formDataSummary) == null) {
                logger.error("Сводная налоговая форма «$value» в подразделении «${formDataDepartment.name}» не находится в статусе «Принята»!")
            }
        }
    }
}

void logicalCheckAfterCalc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def row : dataRows) {
        if (isFixedRow(row)) {
            continue
        }
        def index = row.getIndex()

        // 1. Обязательность заполнения поля графы 1..21
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка значения в графе «Доля налоговой базы (№)»
        if (row.baseTaxOf != null && !checkFormat(row.baseTaxOf, baseTaxOfPattern)) {
            logger.error("Строка $index: Графа «%s» заполнена неверно! Ожидаемый тип поля: Число/18.15/ (3 знака до запятой, 15 после запятой).", getColumnName(row,'baseTaxOf'))
        }
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    // Идентификатор шаблона источников (Приложение 5).
    def id = 372
    def newRow

    departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
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
    dataRowHelper.save(dataRows)
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
def getFormDataSummary(def id) {
    if (!formDataCache[id]) {
        formDataCache[id] = formDataService.getLast(id, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder)
    }
    return formDataCache[id]
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

/**
 * Расчитываем распределяемая налоговая база за отчётный период.
 *
 * @author ekuvshinov
 */
def getTaxBase() {
    def result = 0
    // расходы сложные
    def dataOutcomeComplex = getData(getFormDataSummary(303))

    // Расходы сложные
    if (dataOutcomeComplex != null) {
        for (row in dataOutcomeComplex.allCached) {
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
void calcColumnFrom14To21(def prevDataRows, def row, def sumNal, def reportPeriod) {
    // графа 15
    if (sumNal == null || row.baseTaxOf == null || !checkFormat(row.baseTaxOf, baseTaxOfPattern)) {
        row.taxSumOutside = 0
    } else {
        row.taxSumOutside = roundValue(sumNal * 0.9 * new BigDecimal(row.baseTaxOf) / 100, 0)
    }

    // графа 16
    if (row.taxSum == null || row.subjectTaxCredit == null || row.taxSumOutside == null) {
        row.taxSumToPay = 0
    } else {
        row.taxSumToPay = (row.taxSum > row.subjectTaxCredit + row.taxSumOutside ?
                row.taxSum - (row.subjectTaxCredit + row.taxSumOutside) : 0)
    }

    // графа 17
    if (row.taxSum == null || row.subjectTaxCredit == null || row.taxSumOutside == null) {
        row.taxSumToReduction = 0
    } else {
        row.taxSumToReduction = ((row.taxSum < (row.subjectTaxCredit + row.taxSumOutside)) ?
                ((row.subjectTaxCredit + row.taxSumOutside) - row.taxSum) : 0)
    }

    // Значения граф этого же подразделения в форме пред. периода
    def prev19 = null
    def prev20 = null

    if ((reportPeriod.order == 2 || reportPeriod.order == 3) && row.regionBankDivision != null && prevDataRows != null) {
        for (def prevRow : prevDataRows) {
            if (row.regionBankDivision.equals(prevRow.regionBankDivision)) {
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
    row.everyMonthForSecondKvartalNextPeriod = (reportPeriod.order == 1 ? row.taxSum : prev19)

    // графа 21
    if (reportPeriod.order != 2 || row.taxSum == null || row.everyMonthForSecondKvartalNextPeriod == null || row.everyMonthForKvartalNextPeriod == null) {
        row.everyMonthForThirdKvartalNextPeriod = prev20
    } else {
        row.everyMonthForThirdKvartalNextPeriod =
                ((reportPeriod.order == 2) ? (row.taxSum - row.everyMonthForSecondKvartalNextPeriod - row.everyMonthForKvartalNextPeriod) : prev20)
    }

    // графа 22
    if (reportPeriod.order != 3 || row.taxSum == null || row.everyMonthForThirdKvartalNextPeriod == null) {
        row.everyMonthForFourthKvartalNextPeriod = 0
    } else {
        row.everyMonthForFourthKvartalNextPeriod =
                ((reportPeriod.order == 3) ? (row.taxSum - row.everyMonthForThirdKvartalNextPeriod) : 0)
    }
}

/**
 * Найти строку ЦА
 */
def findCA(def dataRows) {
    def resultRow = null
    long centralId = 113 // ID Центрального аппарата
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
    return (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allCached : null)
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    // есть итоговые строки
    if (dataRows.size() > 2) {
        sortRows(refBookService, logger, dataRows, [dataRows.find { it.getAlias() == 'ca' }], dataRows.find { it.getAlias() == 'total' }, true)
        dataRowHelper.saveSort()
    }
}

// Получить строки формы
def getDataRows(def formId, def kind) {
    //def formId = 421
    //def formDataKind = FormDataKind.SUMMARY
    def departmentId = formData.departmentId
    def reportPeriodId = formData.reportPeriodId
    def periodOrder = formData.periodOrder
    def sourceFormData = formDataService.getLast(formId, kind, departmentId, reportPeriodId, periodOrder)
    if (sourceFormData != null && sourceFormData.id != null)
        return formDataService.getDataRowHelper(sourceFormData)?.allCached
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
    if (departmentType.equals(departmentType.TERR_BANK)) {
        depParam = departmentParam
    } else {
        def tbCode = (Integer) departmentParam.get('PARENT_ID').getReferenceValue()
        def taxPlaningTypeCode = departmentService.get(tbCode).getType().MANAGEMENT.getCode()
        depParam = getProvider(30).getRecords(getReportPeriodEndDate(), null, "PARENT_ID = ${departmentParam.get('PARENT_ID').getReferenceValue()} and TYPE = $taxPlaningTypeCode", null).get(0)
    }
    return depParam
}

// Получение параметров (справочник 330)
def getIncomeParamTable(def depParam) {
    def depId = depParam.get(RefBook.RECORD_ID_ALIAS).numberValue as int
    def incomeParam = getProvider(33).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $depId", null)
    if (incomeParam != null && !incomeParam.isEmpty()) {
        def link = incomeParam.get(0).record_id.value
        def incomeParamTable = getProvider(330).getRecords(getReportPeriodEndDate() - 1, null, "LINK = $link", null)
        return incomeParamTable
    }
    return null
}
