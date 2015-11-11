package form_template.etr.etr_4_17.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Аналитический отчет «Сведения о начисленных и уплачиваемых налогах, сборах и взносах, отнесенных на расходы»
 * formTemplateId = 717
 *
 * @author LHaziev
 *
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        preCalcCheck()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
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
}

@Field
def totalColumns = ['tax26411_01', 'tax26411_02', 'sum34', 'tax26411_03', 'tax26411_13', 'tax26411_12', 'tax26412', 'tax26410_09', 'sum']

@Field
def rateMap = ['rate5': 'sum34',
               'rate7': 'tax26411_03',
               'rate9': 'tax26411_13',
               'rate11': 'tax26411_12',
               'rate13': 'tax26412',
               'rate15': 'tax26410_09',
               'rate': 'sum']

@Field
def providerCache = [:]

@Field
def departmentMap = [R1: 4,
                     R2: 8,
                     R3: 16,
                     R4: 20,
                     R5: 27,
                     R6: 32,
                     R7: 44,
                     R8: 52,
                     R9: 64,
                     R10: 72,
                     R11: 82,
                     R12: 88,
                     R13: 97,
                     R14: 102,
                     R15: 109,
                     R16: 37,
                     R17: 113
                    ]

@Field
def opuMap = [tax26411_01: '26411.01', tax26411_02: '26411.02', tax26411_03: '26411.03', tax26411_13: '26411.13', tax26411_12: '26411.12', tax26412: '26102', tax26410_09: '26410.09']

@Field
def periodMap = [:]

def getReportPeriod(int reportPeriodId) {
    if (periodMap[reportPeriodId] == null) {
        periodMap[reportPeriodId] = reportPeriodService.get(reportPeriodId)
    }
    return periodMap[reportPeriodId]
}

@Field
def endDateBOMap = [:]

/**
 * Получить последний день периода БО.
 *
 * @param year год
 * @param order номер периода БО (1, 2, 3, 4)
 */
def getEndDateBO(def year, def order) {
    def dateStr
    switch (order) {
        case 1:
            dateStr = "31.03.$year"
            break
        case 2:
            dateStr = "30.06.$year"
            break
        case 3:
            dateStr = "30.09.$year"
            break
        default:
            dateStr = "31.12.$year"
            break
    }
    return Date.parse('dd.MM.yyyy', dateStr)
}

def getEndDate(def year, def order) {
    def key = year + "#" + order
    if (endDateBOMap[key] == null) {
        endDateBOMap[key] = getEndDateBO(year, order)
    }
    return endDateBOMap[key]
}

@Field
def periodNameBOMap = [:]

/* Получить название периода БО по дате. */
def getPeriodNameBO(def date) {
    if (periodNameBOMap[date] == null) {
        periodNameBOMap[date] = bookerStatementService.getPeriodValue(date)?.NAME?.value
    }
    return periodNameBOMap[date]
}

void checkOpuCodes(def department, def date) {
    def opuCodes = opuMap.values()
    def accountPeriodId = bookerStatementService.getAccountPeriodId(department.id, date)
    if (accountPeriodId == null) {
        logger.warn('Форма 102 бухгалтерской отчетности: Подразделение: %s. Отсутствует отчетный период, соответствующий значениям НФ! При заполнении граф формы значения будут приняты за нулевые.',
                department.name)
        return
    }

    // 3. Проверка наличия ф.102 «Отчет о финансовых результатах» (предрасчетные проверки)
    boolean foundBO = true
    if (accountPeriodId == null) {
        foundBO = false
    } else {
        def provider = formDataService.getRefBookProvider(refBookFactory, 52L, providerCache)
        def ids = provider.getUniqueRecordIds(new Date(), "ACCOUNT_PERIOD_ID = ${accountPeriodId}")
        if (ids == null || ids.isEmpty()) {
            foundBO = false
        }
    }
    if (!foundBO) {
        logger.warn('Не найдена форма 102 бухгалтерской отчетности: Период: %s %s, Подразделение: %s. ' +
                'Ячейки по графам, заполняемые из данной формы, будут заполнены нулевым значением',
                getPeriodNameBO(date), date.format('yyyy'), department.name)
    } else {
        // 4. Проверка наличия значений в ф.102 «Отчет о финансовых результатах» по символам (предрасчетные проверки)
        // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
        def filter = "OPU_CODE = '${opuCodes.join("' OR OPU_CODE = '")}'"
        def records = bookerStatementService.getRecords(52L, department.id, date, filter)
        def recordOpuCodes = records?.collect { it.OPU_CODE.stringValue }?.unique() ?: []
        def missedCodes = opuCodes.findAll { !recordOpuCodes.contains(it) }
        if (!missedCodes.isEmpty()) {
            logger.warn('Форма 102 бухгалтерской отчетности: Период: %s %s, Подразделение: %s. ' +
                    'Отсутствуют значения по следующим символам: %s! При заполнении граф формы значения по данным символам будут приняты за нулевые.',
                    getPeriodNameBO(date), date.format('yyyy'), department.name, missedCodes.join(', '))
        }
    }
}

void preCalcCheck() {
    // находим записи для текущего периода и подразделений по строкам
    def reportPeriod = getReportPeriod(formData.reportPeriodId)
    def date = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order)
    departmentMap.values().each{departmentId ->
        checkOpuCodes(departmentService.get(departmentId), date)
    }
}

// Возвращает данные из Формы 102 БО за период + флаг корректности
def get102(def departmentId, def date) {
    def filter = "OPU_CODE = '${opuMap.values().join("' OR OPU_CODE = '")}'"
    // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
    def records = bookerStatementService.getRecords(52L, departmentId, date, filter)
    if (records == null || records.isEmpty()) {
        return [:]
    }
    return records.collect{
        def record = [:]
        record[it.OPU_CODE.stringValue] = it.TOTAL_SUM.numberValue / 1000
        return record
    }.sum()
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def reportPeriod = getReportPeriod(formData.reportPeriodId)
    def date = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order)
    for (def alias in departmentMap.keySet()) {
        def row = getDataRow(dataRows, alias)
        def records = get102(departmentMap.get(alias), date)
        opuMap.each{k,v ->
            def value = records?records.get(v):0
            row[k] = value
        }
        row.sum34 = row.tax26411_01 + row.tax26411_02
        row.sum = row.sum34 + row.tax26411_01 + row.tax26411_02 + row.tax26411_03 + row.tax26411_13 + row.tax26411_12 +
                row.tax26412 + row.tax26410_09
    }
    //расчет итоговой строки
    def totalRow = getDataRow(dataRows, 'total')
    totalColumns.each { alias ->
        totalRow[alias] = summ(formData, dataRows, new ColumnRange(alias, 0, 16))
        def rateAlias = rateMap.find{ it.value==alias}?.key
        if (rateAlias)
            totalRow[rateAlias] = totalRow[alias] > 0 ? 100 : 0
    }

    //расчет процентов
    for (def alias in departmentMap.keySet()) {
        def row = getDataRow(dataRows, alias)
        rateMap.each { k, v ->
            row[k] = calcRate(row, totalRow, v, k)
        }
    }
}

def calcRate(def row, def totalRow, def alias, def resultAlias) {
    def result = 0
    def dividend = (row[alias] ?: 0)
    def divider = totalRow[alias]
    // проверка делителя на 0 или null
    if (divider) {
        // расчет
        result = dividend * 100 / divider
    } else {
        def msg = String.format("Строка %s: Графа «%s»: выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно)». Ячейка будет заполнена значением «0».",
                row.getIndex(), getColumnName(row, resultAlias))
        logger.warn(msg)
    }
    return result
}

void logicCheck() {
}