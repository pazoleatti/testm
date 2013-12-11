package form_template.income.rnu46

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode
import java.text.SimpleDateFormat

/**
 * Скрипт для РНУ-46 (rnu46.groovy).
 * Форма "(РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»".
 * formTemplateId=342
 *
 * @author rtimerbaev
 * @author Stanislav Yasinskiy
 * @author Dmitriy Levykin
 */
// графа 1  - rowNumber
// графа 2  - invNumber
// графа 3  - name
// графа 4  - cost
// графа 5  - amortGroup
// графа 6  - usefulLife
// графа 7  - monthsUsed
// графа 8  - usefulLifeWithUsed
// графа 9  - specCoef
// графа 10 - cost10perMonth
// графа 11 - cost10perTaxPeriod
// графа 12 - cost10perExploitation
// графа 13 - amortNorm
// графа 14 - amortMonth
// графа 15 - amortTaxPeriod
// графа 16 - amortExploitation
// графа 17 - exploitationStart
// графа 18 - usefullLifeEnd
// графа 19 - rentEnd
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        // TODO убрать когда появится механизм назначения periodOrder при создании формы
        if (formData.periodOrder == null)
            return

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
        formDataService.addRow(formData, currentDataRow, isMonthBalace() ? balanceColumns : editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def editableColumns = ['invNumber', 'name', 'cost', 'amortGroup', 'usefulLife', 'monthsUsed', 'usefulLifeWithUsed',
        'specCoef', 'exploitationStart', 'rentEnd']

@Field
def balanceColumns = ['invNumber', 'name', 'cost', 'amortGroup', 'usefulLife', 'monthsUsed', 'usefulLifeWithUsed',
        'specCoef', 'cost10perMonth', 'cost10perTaxPeriod', 'cost10perExploitation', 'amortNorm', 'amortMonth',
        'amortTaxPeriod', 'amortExploitation', 'exploitationStart', 'usefullLifeEnd', 'rentEnd']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'invNumber', 'name', 'cost', 'amortGroup', 'usefulLife', 'monthsUsed',
        'usefulLifeWithUsed', 'specCoef', 'cost10perMonth', 'cost10perTaxPeriod', 'cost10perExploitation',
        'amortNorm', 'amortMonth', 'amortTaxPeriod', 'amortExploitation', 'exploitationStart', 'usefullLifeEnd']

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

//// Кастомные методы

@Field
def formDataPrev = null // Форма предыдущего месяца
@Field
def dataRowHelperPrev = null // DataRowHelper формы предыдущего месяца
@Field
def isBalace = null
@Field
def format = new SimpleDateFormat('dd.MM.yyyy')
@Field
def check17 = format.parse('01.01.2006')
@Field
def lastDay2001 = format.parse('31.12.2001')

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

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков и месяц первый в периоде.
def isMonthBalace() {
    if (isBalace == null) {
        // Отчётный период
        if (!reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId) || formData.periodOrder == null) {
            isBalace = false
        } else {
            isBalace = (formData.periodOrder - 1) % 3 == 0
        }
    }
    return isBalace
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (dataRows.isEmpty()) {
        return
    }
    // Дата начала отчетного периода
    def startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time

    // Отчетная дата
    def reportDate = getReportDate()
    def Calendar reportDateC = Calendar.getInstance()
    reportDateC.setTime(reportDate)
    def reportMounth = reportDateC.get(Calendar.MONTH)

    // Принятый отчет за предыдущий месяц
    def dataPrev = null
    if (!isMonthBalace()) {
        if (getFormDataPrev() == null || getFormDataPrev().state != WorkflowState.ACCEPTED) {
            logger.error("Не найдены экземпляры ${formTemplateService.get(formData.formTemplateId).fullName} за прошлый отчетный период!")
        } else {
            dataPrev = getDataRowHelperPrev()
        }
    }

    // Сквозная нумерация с начала года
    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    for (def row in dataRows) {
        // Графа 1
        row.rowNumber = ++rowNumber

        if (isMonthBalace()) {
            // Для периода ввода остатков расчитывается только порядковый номер
            continue;
        }

        def map = row.amortGroup == null ? null : getRefBookValue(71, row.amortGroup)

        // Строка из предыдущей формы с тем же инвентарным номером
        prevRow = getPrevRow(dataPrev, row)

        // Графа 6
        row.usefulLife = row.amortGroup//calc6(map)

        // Графа 8
        row.usefulLifeWithUsed = calc8(row)

        // Графа 10
        row.cost10perMonth = calc10(row, map)

        // графа 12
        row.cost10perExploitation = calc12(row, prevRow, startDate, endDate)

        // графа 18
        row.usefullLifeEnd = calc18(row)

        // графа 14
        row.amortMonth = calc14(row, prevRow, endDate)

        // Графа 11, 15, 16
        def calc11and15and16 = calc11and15and16(reportMounth, row, prevRow)
        row.cost10perTaxPeriod = calc11and15and16[0]
        row.amortTaxPeriod = calc11and15and16[1]
        row.amortExploitation = calc11and15and16[2]

        // графа 13
        row.amortNorm = calc13(row)
    }
    dataRowHelper.update(dataRows);
}

// Ресчет графы 8
BigDecimal calc8(def row) {
    if (row.monthsUsed == null || row.usefulLife == null || row.specCoef == null) {
        return null
    }
    def map = getRefBookValue(71, row.usefulLife)
    def term = map.TERM.numberValue
    if (row.monthsUsed < term) {
        if (row.specCoef > 0) {
            return round((term - row.monthsUsed) / row.specCoef, 0)
        } else {
            return round(term - row.monthsUsed, 0)
        }
    }
    return row.usefulLifeWithUsed
}

// Ресчет графы 10
BigDecimal calc10(def row, def map) {
    def Integer group = map?.GROUP?.numberValue
    if ([1, 2, 8..10].contains(group) && row.cost != null) {
        return round(row.cost * 10)
    } else if ([3..7].contains(row.amortGroup) && row.cost != null) {
        return round(row.cost * 30)
    } else if (row.exploitationStart != null && row.exploitationStart < check17) {
        return 0
    }
    return null
}

// Ресчет граф 11, 15, 16
BigDecimal[] calc11and15and16(def reportMonth, def row, def prevRow) {
    def BigDecimal[] values = new BigDecimal[3]
    if (reportMonth == Calendar.JANUARY) {
        values[0] = row.cost10perMonth
        values[1] = row.amortMonth
        values[2] = row.amortMonth
    } else if (prevRow != null) {
        if (row.cost10perMonth != null && prevRow.cost10perTaxPeriod != null) {
            values[0] = row.cost10perMonth + prevRow.cost10perTaxPeriod
        }
        if (row.amortMonth != null && prevRow.amortTaxPeriod != null) {
            values[1] = row.amortMonth + prevRow.amortTaxPeriod
        }
        if (row.amortMonth != null && prevRow.amortExploitation != null) {
            values[2] = row.amortMonth + prevRow.amortExploitation
        }
    }
    return values
}

// Ресчет графы 12
BigDecimal calc12(def row, def prevRow, def startDate, def endDate) {
    if (prevRow == null || row.exploitationStart == null) {
        return null
    }
    if (startDate < row.exploitationStart && row.exploitationStart < endDate) {
        return row.cost10perMonth
    }
    if (prevRow != null && row.cost10perMonth != null && prevRow.cost10perExploitation != null) {
        return row.cost10perMonth + prevRow.cost10perExploitation
    }
    return null
}

// Ресчет графы 13
BigDecimal calc13(def row) {
    if (row == null || row.usefulLifeWithUsed == null || row.usefulLifeWithUsed == 0) {
        return null
    }
    return round(100 / row.usefulLifeWithUsed, 0)
}

// Ресчет графы 14
BigDecimal calc14(def row, def prevRow, def endDate) {
    if (prevRow == null || row.usefullLifeEnd == null || row.cost10perExploitation == null || row.cost == null
            || prevRow.cost == null || prevRow.amortExploitation == null || (row.usefullLifeEnd - endDate) == 0) {
        return null
    }
    if (row.usefullLifeEnd > lastDay2001) {
        return round((prevRow.cost - row.cost10perExploitation - prevRow.amortExploitation) / (row.usefullLifeEnd - endDate))
    }
    return round(row.cost / 84)
}

// Ресчет графы 18
Date calc18(def row) {
    if (row.exploitationStart == null || row.usefulLifeWithUsed == null) {
        return null
    }
    def Calendar tmpCal = Calendar.getInstance()
    tmpCal.setTime(row.exploitationStart)
    tmpCal.add(Calendar.MONTH, row.usefulLifeWithUsed.intValue())
    tmpCal.set(Calendar.DAY_OF_MONTH, tmpCal.getMaximum(Calendar.DAY_OF_MONTH))
    return tmpCal.getTime()
}

// Логические проверки
void logicCheck() {
    if (formData.periodOrder == null) {
        throw new ServiceException("Месячная форма создана как квартальная!")
    }

    if (isMonthBalace()) {
        // В периоде ввода остатков нет лог. проверок
        return
    }

    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    // Алиасы граф для арифметической проверки
    def arithmeticCheckAlias = ['usefulLifeWithUsed', 'cost10perMonth', 'cost10perTaxPeriod', 'amortTaxPeriod',
            'amortExploitation', 'cost10perExploitation', 'amortNorm', 'amortMonth']

    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // Дата начала отчетного периода
    def startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time

    // Инвентарные номера
    def Set<String> invSet = new HashSet<String>()

    // Отчет за предыдущий месяц
    if (getDataRowHelperPrev() == null || getFormDataPrev().state != WorkflowState.ACCEPTED) {
        logger.error('Отсутствуют данные за прошлые отчетные периоды!')
    }

    // Отчетная дата
    def reportDate = getReportDate()
    def Calendar reportDateC = Calendar.getInstance()
    reportDateC.setTime(reportDate)
    def reportMounth = reportDateC.get(Calendar.MONTH)

    for (def row : dataRows) {
        def map = null
        if (row.amortGroup != null) {
            map = getRefBookValue(71, row.amortGroup)
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        prevRow = getPrevRow(getDataRowHelperPrev(), row)

        // 1. Проверка на заполнение (графа 1..18)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «инвентарный номер»
        if (invSet.contains(row.invNumber)) {
            logger.error(errorMsg + "Инвентарный номер не уникальный!")
        } else {
            invSet.add(row.invNumber)
        }

        // 5. Проверка на нулевые значения (графа 9, 10, 11, 13, 14, 15)
        if (row.specCoef == 0 &&
                row.cost10perMonth == 0 &&
                row.cost10perTaxPeriod == 0 &&
                row.amortNorm &&
                row.amortMonth == 0 &&
                row.amortTaxPeriod) {
            logger.error(errorMsg + 'Все суммы по операции нулевые!')
        }

        def prevSum = getYearSum(['cost10perMonth', 'amortMonth'])

        // 6. Проверка суммы расходов в виде капитальных вложений с начала года
        if (prevRow == null ||
                row.cost10perTaxPeriod == null ||
                row.cost10perMonth == null ||
                prevRow.cost10perTaxPeriod == null ||
                row.cost10perTaxPeriod < row.cost10perMonth ||
                row.cost10perTaxPeriod != row.cost10perMonth + prevRow.cost10perTaxPeriod ||
                row.cost10perTaxPeriod != prevSum.cost10perMonth) {
            logger.error(errorMsg + 'Неверная сумма расходов в виде капитальных вложений с начала года!')
        }

        // 7. Проверка суммы начисленной амортизации с начала года
        if (prevRow == null ||
                row.amortTaxPeriod == null ||
                row.amortMonth == null ||
                row.amortTaxPeriod < row.amortMonth ||
                row.amortTaxPeriod != row.amortMonth + prevRow.amortTaxPeriod ||
                row.amortTaxPeriod != prevSum.amortMonth) {
            logger.error(errorMsg + 'Неверная сумма начисленной амортизации с начала года!')
        }

        // 8. Арифметические проверки расчета граф 8, 10-16, 18
        needValue['usefulLifeWithUsed'] = calc8(row)
        needValue['cost10perMonth'] = calc10(row, map)
        needValue['cost10perExploitation'] = calc12(row, prevRow, startDate, endDate)
        needValue['amortNorm'] = calc13(row)
        needValue['amortMonth'] = calc14(row, prevRow, endDate)
        def calc11and15and16 = calc11and15and16(reportMounth, row, prevRow)
        needValue['cost10perTaxPeriod'] = calc11and15and16[0]
        needValue['amortTaxPeriod'] = calc11and15and16[1]
        needValue['amortExploitation'] = calc11and15and16[2]

        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        if (row.usefullLifeEnd != calc18(row)) {
            logger.error(errorMsg + "Неверное значение графы: ${getColumnName(row, 'usefullLifeEnd')}!")
        }

        // Проверки НСИ
        checkNSI(71, row, 'amortGroup')
    }
}
// Округление
def BigDecimal round(BigDecimal value, def int precision = 2) {
    return value?.setScale(precision, RoundingMode.HALF_UP)
}

// Получить отчетную дату
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return tmp ? tmp.getTime() + 1 : null
}

// Поиск строки из предыдущей формы с тем же инвентарным номером
def getPrevRow(def dataPrev, def row) {
    if (dataPrev != null) {
        for (def rowPrev : dataPrev.getAll()) {
            if (rowPrev.invNumber == row.invNumber) {
                return rowPrev
            }
        }
    }
    return null
}

// Получение суммы по графе всех предыдущих принятых форм и по графе текущей формы
def getYearSum(def aliases) {
    def retVal = [:]

    for (def alias : aliases) {
        retVal[alias] = 0
    }

    // Налоговый период
    def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod

    // Сумма в текущей форме
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    for (def row : dataRows) {
        for (def alias : aliases) {
            def val = row.get(alias)
            retVal[alias] += val == null ? 0 : val
        }
    }
    // Сумма в предыдущих формах
    for (def month = formData.periodOrder - 1; month >= 1; month--) {
        def prevFormData = formDataService.findMonth(formData.formType.id, formData.kind, formData.departmentId,
                taxPeriod.id, month)
        if (prevFormData != null && prevFormData.state == WorkflowState.ACCEPTED) {
            def prevDataRows = formDataService.getDataRowHelper(prevFormData).getAll()
            for (def row : prevDataRows) {
                for (def alias : aliases) {
                    def val = row.get(alias)
                    retVal[alias] += val == null ? 0 : val
                }
            }
        }
    }
    return retVal
}