package form_template.income.rnu46

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Скрипт для РНУ-46 (rnu46.groovy).
 * Форма "(РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»".
 * formTemplateId=342
 *
 * @author rtimerbaev
 * @author Stanislav Yasinskiy
 * @author Dmitriy Levykin
 */
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode
import java.text.SimpleDateFormat

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        // TODO Уникальность для ежемесячных форм
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
        'specCoef', 'exploitationStart', 'usefullLifeEnd', 'rentEnd']

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
        def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
        if (!reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId) || formData.periodOrder == null) {
            isBalace = false
        } else {
            isBalace = formData.periodOrder - 1 % 3 == 0
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

    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def lastDay2001 = format.parse('31.12.2001')
    def check17 = format.parse('01.01.2006')
    // Дата начала отчетного периода
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // Отчетная дата
    def reportDate = getReportDate()
    def Calendar reportDateC = Calendar.getInstance()
    reportDateC.setTime(reportDate)
    def reportMounth = reportDateC.get(Calendar.MONTH)

    // Отчет за предыдущий месяц
    def dataPrev = null
    if (!isMonthBalace()) {
        if (getFormDataPrev() == null || getFormDataPrev().state != WorkflowState.ACCEPTED) {
            logger.error("Не найдены экземпляры ${formTemplateService.get(formData.formTemplateId).fullName} за прошлый отчетный период!")
        } else {
            dataPrev = getDataRowHelperPrev()
        }
    }

    // Индекс
    def index = 0

    for (def row in dataRows) {
        def map = null
        if (row.amortGroup != null) {
            map = getRefBookValue(71, row.amortGroup)
        }

        // графа 1
        row.rowNumber = ++index

        if (isMonthBalace()) {
            continue;
        }

        prevRow = getPrevRow(dataPrev, row)

        // графа 6
        row.usefulLife = calc6(map)

        // графа 8
        row.usefulLifeWithUsed = calc8(row)

        // графа 10
        row.cost10perMonth = calc10(row, map, check17)
        calc11and15and16 = calc11and15and16(reportMounth, row, prevRow)

        // графа 11
        row.cost10perTaxPeriod = calc11and15and16[0]

        // графа 15
        row.amortTaxPeriod = calc11and15and16[1]

        // графа 16
        row.amortExploitation = calc11and15and16[2]

        // графа 12
        row.cost10perExploitation = calc12(row, prevRow, startDate, endDate)

        // графа 13
        row.amortNorm = calc13(row)

        // графа 14
        row.amortMonth = calc14(row, prevRow, lastDay2001, endDate)

        // графа 18
        row.usefullLifeEnd = calc18(row)
    }

    dataRowHelper.update(dataRows);
}

// Ресчет графы 6
BigDecimal calc6(def map) {
    if (map != null) {
        return map.TERM.numberValue
    }
    return null
}

// Ресчет графы 8
BigDecimal calc8(def row) {
    if (row.monthsUsed == null || row.usefulLife == null || row.specCoef == null)
        return null
    if (row.monthsUsed < row.usefulLife) {
        if (row.specCoef > 0) {
            return round((row.usefulLife - row.monthsUsed) / row.specCoef, 0)
        } else {
            return round(row.usefulLife - row.monthsUsed, 0)
        }
    }
    return row.usefulLifeWithUsed
}

// Ресчет графы 10
BigDecimal calc10(def row, def map, def check17) {
    def BigDecimal tmp = null
    if (map != null) {
        if (map.GROUP.numberValue in [1, 2, 8..10] && row.cost != null) {
            tmp = row.cost * 10
        } else if (row.amortGroup in (3..7) && row.cost != null) {
            tmp = row.cost * 30
        } else if (row.exploitationStart != null && row.exploitationStart < check17) {
            tmp = 0
        }
    }
    return round(tmp)
}

// Ресчет граф 11, 15, 16
BigDecimal[] calc11and15and16(def reportMonth, def row, def prevRow) {
    def BigDecimal[] values = new BigDecimal[3]
    if (reportMonth == Calendar.JANUARY) {
        values[0] = row.cost10perMonth
        values[1] = row.amortMonth
        values[2] = row.amortMonth
    } else if (prevRow != null) {
        if (prevRow.cost10perTaxPeriod != null)
            values[0] = row.cost10perMonth + prevRow.cost10perTaxPeriod
        if (prevRow.amortTaxPeriod != null)
            values[1] = row.amortMonth + prevRow.amortTaxPeriod
        if (prevRow.amortExploitation != null)
            values[2] = row.amortMonth + prevRow.amortExploitation
    }
    return values
}

// Ресчет графы 12
BigDecimal calc12(def row, def prevRow, def rpStartDate, def rpEndDate) {
    def BigDecimal val = null
    if (row.exploitationStart == null) {
        val = null
    } else if (rpStartDate < row.exploitationStart && row.exploitationStart < rpEndDate) {
        val = row.cost10perMonth
    } else if (prevRow != null && prevRow.cost10perExploitation != null) {
        val = row.cost10perMonth + prevRow.cost10perExploitation
    }
    return val
}

// Ресчет графы 13
BigDecimal calc13(def row) {
    if (row == null || row.usefulLifeWithUsed == null || row.usefulLifeWithUsed == 0) {
        return null
    }
    return round(100 / row.usefulLifeWithUsed, 0)
}

// Ресчет графы 14
BigDecimal calc14(def row, def prevRow, def lastDay2001, def endDate) {
    def BigDecimal val
    if (row.usefullLifeEnd == null || row.cost10perExploitation == null || row.cost == null
            || prevRow.cost == null || prevRow.amortExploitation == null || (row.usefullLifeEnd - endDate) == 0)
        val = null
    else if (row.usefullLifeEnd > lastDay2001) {
        val = (prevRow.cost - row.cost10perExploitation - prevRow.amortExploitation) / (row.usefullLifeEnd - endDate)
    } else {
        val = row.cost / 84
    }
    return round(val)
}

// Ресчет графы 18
Date calc18(def row) {
    if (row.exploitationStart == null || row.usefulLifeWithUsed == null)
        return null
    def Calendar tmpCal = Calendar.getInstance()
    tmpCal.setTime(row.exploitationStart)
    tmpCal.add(Calendar.MONTH, Integer.valueOf(row.usefulLifeWithUsed.toString()))
    tmpCal.set(Calendar.DAY_OF_MONTH, tmpCal.getMaximum(Calendar.DAY_OF_MONTH))
    return tmpCal.getTime()
}

// Логические проверки
void logicCheck() {
    if (formData.periodOrder == null) {
        throw new ServiceException("Месячная форма создана как квартальная!")
    }

    if (isMonthBalace()) {
        return
    }

    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    // Алиасы граф для арифметической проверки
    def arithmeticCheckAlias = ['usefulLifeWithUsed', 'cost10perMonth', 'cost10perTaxPeriod', 'amortTaxPeriod',
            'amortExploitation', 'cost10perExploitation', 'amortNorm', 'amortMonth']

    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // Дата начала отчетного периода
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // Инвентарные номера
    def Set<String> invSet = new HashSet<String>()

    // TODO Левыкин: Уникальность номера с начала года
    for (def row in dataRows) {
        // 1. Проверка на заполнение (графа 1..18)
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
        // Проверка на уникальность поля «инвентарный номер»
        if (invSet.contains(row.invNumber)) {
            logger.error("Инвентарный номер не уникальный!")
        } else {
            invSet.add(row.invNumber)
        }
    }

    // Отчет за предыдущий месяц
    if (getDataRowHelperPrev() == null || getFormDataPrev().state != WorkflowState.ACCEPTED) {
        logger.error('Отсутствуют данные за прошлые отчетные периоды!')
    }

    def SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def check17 = format.parse('01.01.2006')
    def lastDay2001 = format.parse('31.12.2001')

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

        prevRow = getPrevRow(getDataRowHelperPrev(), row)

        // 5. Проверка на нулевые значения (графа 9, 10, 11, 13, 14, 15)
        if (row.specCoef == 0 &&
                row.cost10perMonth == 0 &&
                row.cost10perTaxPeriod == 0 &&
                row.amortNorm &&
                row.amortMonth == 0 &&
                row.amortTaxPeriod) {
            logger.error('Все суммы по операции нулевые!')
        }

        // 6. Проверка суммы расходов в виде капитальных вложений с начала года
        if (prevRow != null &&
                row.cost10perTaxPeriod >= row.cost10perMonth &&
                row.cost10perTaxPeriod == row.cost10perTaxPeriod + prevRow.cost10perTaxPeriod &&
                row.cost10perTaxPeriod == prevRow.cost10perTaxPeriod) {
            logger.error('Неверная сумма расходов в виде капитальных вложений с начала года!')
        }

        // 7. Проверка суммы начисленной амортизации с начала года
        if (prevRow != null &&
                row.amortTaxPeriod < row.amortMonth &&
                row.amortTaxPeriod == row.cost10perTaxPeriod + prevRow.amortTaxPeriod &&
                row.amortTaxPeriod == prevRow.amortTaxPeriod) {
            logger.error('Неверная сумма начисленной амортизации с начала года!')
        }

        // 8. Арифметические проверки расчета граф 8, 10-16, 18
        def calc11and15and16 = calc11and15and16(reportMounth, row, prevRow)

        needValue['usefulLifeWithUsed'] = calc8(row)
        needValue['cost10perMonth'] = calc10(row, map, check17)
        needValue['cost10perTaxPeriod'] = calc11and15and16[0]
        needValue['amortTaxPeriod'] = calc11and15and16[1]
        needValue['amortExploitation'] = calc11and15and16[2]
        needValue['cost10perExploitation'] = calc12(row, prevRow, startDate, endDate)
        needValue['amortNorm'] = calc13(row)
        needValue['amortMonth'] = calc14(row, prevRow, lastDay2001, endDate)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        if (row.usefullLifeEnd !=  calc18(row)) {
            logger.error("Cтрока ${row.getIndex()}: Неверное значение графы: ${getColumnName(row, 'usefullLifeEnd')}!")
        }

        // Проверки НСИ
        checkNSI(71, row, 'amortGroup')
    }
}
// Округление
def BigDecimal round(def value, def int precision = 2) {
    if (value == null) {
        return null
    }
    return value.setScale(precision, RoundingMode.HALF_UP)
}

// Получить отчетную дату
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return tmp ? tmp.getTime() + 1 : null
}

// Получить значение за предыдущий отчетный период для графы 11, 12, 14, 15, 16
def getPrevRow(def dataPrev, def row) {
    if (dataPrev != null)
        for (def rowPrev : dataPrev.getAll()) {
            if (rowPrev.invNumber == row.invNumber) {
                return rowPrev
            }
        }
    return null
}