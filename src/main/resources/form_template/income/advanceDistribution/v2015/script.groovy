package form_template.income.advanceDistribution.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
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
def summaryMap = [[301, 305] : "Доходы, учитываемые в простых РНУ", [302] : "Сводная форма начисленных доходов", //максимум два вида источников с одним именем
                  [303] : "Сводная форма начисленных расходов", [304] : "Расходы, учитываемые в простых РНУ"]

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
        taxBase = roundValue(getTaxBaseAsDeclaration(), 0)
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
        return roundValue(taxBase * new BigDecimal(row.baseTaxOf) / 100, 0)
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
def getFormDataSummaryMap(def ids) {
    def map = [:]
    ids.each{ id ->
        if (!formDataCache[id]) {
            formDataCache[id] = formDataService.getLast(id, com.aplana.sbrf.taxaccounting.model.FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder)
        }
        if (!formDataCache[id]) {
            map[id] = formDataCache[id]
        }
    }
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
    def dataRowsComplexIncome = getData(getFormDataSummaryMap([302])[302])?.allCached

    /** Доходы простые уровня Банка "Расшифровка видов доходов, учитываемых в простых РНУ". */
    def dataRowsSimpleIncome = getData(getFormDataSummaryMap([305])[305])?.allCached
    if (dataRowsSimpleIncome == null) {
        dataRowsSimpleIncome = getData(getFormDataSummaryMap([301])[301])?.allCached
    }

    /** Расходы сложные уровня Банка "Сводная форма начисленных расходов". */
    def dataRowsComplexConsumption = getData(getFormDataSummaryMap([303])[303])?.allCached

    /** Расходы простые уровня Банка "Расшифровка видов расходов, учитываемых в простых РНУ". */
    def dataRowsSimpleConsumption = getData(getFormDataSummaryMap(304)[304])?.allCached

    /** Сведения о суммах налога на прибыль, уплаченного Банком за рубежом */
    //    def dataRowsSum = getData(getFormDataSummary(421))?.allCached
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

    // Код вида расхода = 22492, 22500, 22505, 22585, 22590, 22595, 22660, 22664, 22668,
    // 22670, 22690, 22695, 22700, 23120, 23130, 23140, 23240 - графа 9
    result += getComplexConsumptionSumRows9(dataRows, [22492, 22500, 22505, 22585, 22590, 22595, 22660, 22664, 22668,
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

    // Код вида расхода = 23150, 23160, 23170 - графа 9
    result -= getComplexConsumptionSumRows9(dataRows, [23150, 23160, 23170])

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
