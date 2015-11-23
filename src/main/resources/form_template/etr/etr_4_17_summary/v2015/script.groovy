package form_template.etr.etr_4_17_summary.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.DepartmentType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Аналитический отчет «Сведения о начисленных и уплачиваемых налогах, сборах и взносах, отнесенных на расходы»(Банк)
 * formTemplateId = 7170
 *
 * @author LHaziev
 *
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.CALCULATE:
        preCalcCheck()
        if (logger.containsLevel(LogLevel.ERROR))
            return
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        preCalcCheck()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        preCalcCheck()
        if (logger.containsLevel(LogLevel.ERROR))
            return
        logicCheck()
        break
}

// Редактируемые атрибуты
@Field
def editableColumns = ['department']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['tax26411_01', 'tax26411_02', 'sum34', 'rate5', 'tax26411_03', 'rate7', 'tax26411_13', 'rate9', 'tax26411_12', 'rate11', 'tax26412', 'rate13', 'tax26410_09', 'rate15', 'sum', 'rate']

// Атрибуты по которым рассчитываются итоги
@Field
def totalColumns = ['tax26411_01', 'tax26411_02', 'sum34', 'tax26411_03', 'tax26411_13', 'tax26411_12', 'tax26412', 'tax26410_09', 'sum']

@Field
def nonEmptyColumns = ['department', 'tax26411_01', 'tax26411_02', 'sum34', 'rate5', 'tax26411_03', 'rate7', 'tax26411_13', 'rate9', 'tax26411_12', 'rate11', 'tax26412', 'rate13', 'tax26410_09', 'rate15', 'sum', 'rate']

// alias -> opu code
@Field
def opuMap = [tax26411_01: '26411.01',
              tax26411_02: '26411.02',
              tax26411_03: '26411.03',
              tax26411_13: '26411.13',
              tax26411_12: '26411.12',
              tax26412: '26102',
              tax26410_09: '26410.09']

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
def periodMap = [:]

def getReportPeriod(int reportPeriodId) {
    if (periodMap[reportPeriodId] == null) {
        periodMap[reportPeriodId] = reportPeriodService.get(reportPeriodId)
    }
    return periodMap[reportPeriodId]
}
@Field
def startDateMap = [:]

@Field
def endDateMap = [:]

def getStartDate(int reportPeriodId) {
    if (startDateMap[reportPeriodId] == null) {
        startDateMap[reportPeriodId] = reportPeriodService.getStartDate(reportPeriodId)?.time
    }
    return startDateMap[reportPeriodId]
}

def getEndDate(int reportPeriodId) {
    if (endDateMap[reportPeriodId] == null) {
        endDateMap[reportPeriodId] = reportPeriodService.getEndDate(reportPeriodId)?.time
    }
    return endDateMap[reportPeriodId]
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

@Field
def departmentManagementMap = [:]

def getDepartmentManagement(def depId) {
    if (depId != null && departmentManagementMap[depId] == null) {
        def children = departmentService.getAllChildren(depId)
        for(def child: children) {
            if (child.type == DepartmentType.MANAGEMENT) {
                departmentManagementMap[depId] = child
                return child
            }
        }
    }
    return departmentManagementMap[depId]
}

@Field
def departmentMap = [:]

def getDepartment(Integer depId) {
    if (depId != null && departmentMap[depId] == null) {
        departmentMap[depId] = departmentService.get(depId)
    }
    return departmentMap[depId]
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
                    'Отсутствуют значения по следующим символам: %s! При заполнении формы, значения по данным символам будут приняты за нулевые.',
                    getPeriodNameBO(date), date.format('yyyy'), department.name, missedCodes.join(', '))
        }
    }
}

void preCalcCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // находим записи для текущего периода и подразделений по строкам
    def reportPeriod = getReportPeriod(formData.reportPeriodId)
    def date = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order)
    def prevDate
    if (!formData.accruing && reportPeriod.order != 1) {
        prevDate = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order - 1)
    }
    for(def row : dataRows) {
        if (row.getAlias())
            continue

        if (row.department != null) {
            def department = getDepartment(row.department.intValue())
            if (department.type == DepartmentType.TERR_BANK) {
                def management = getDepartmentManagement(row.department.intValue())
                if (management == null) {
                    logger.error("Строка %s: Графа «%s»: для подразделения «%s» не найдено дочернее подразделение с типом «Управление».",
                            row.getIndex(), getColumnName(row, 'department'), department.getName())
                } else {
                    if (!formData.accruing && reportPeriod.order != 1) {
                        checkOpuCodes(management, prevDate)
                    }
                    checkOpuCodes(management, date)
                }
            } else {
                logger.error("Строка %s: Графа «%s»: выполнение расчета невозможно, так как выбранный тип подразделения не соответствует значению «Территориальный банк». Необходимо выбрать подразделение с типом «Территориальный банк».",
                        row.getIndex(), getColumnName(row, 'department'))
            }
        } else {
            checkNonEmptyColumns(row, row.getIndex(), ['department'], logger, true)
        }
    }
}

// Возвращает данные из Формы 102 БО за период + флаг корректности
def get102(def departmentId, def date) {
    def filter = "OPU_CODE = '${opuMap.values().join("' OR OPU_CODE = '")}'"
    // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
    def records = bookerStatementService.getRecords(52L, departmentId, date, filter)
    if (records == null || records.isEmpty()) {
        return [[:], false]
    }
    return [records.collect{
        def record = [:]
        record[it.OPU_CODE.stringValue] = it.TOTAL_SUM.numberValue / 1000
        return record
    }.sum(), true]
}

def calcBO(def departmentId) {
    def result = [:]
    def reportPeriod = getReportPeriod(formData.reportPeriodId)
    def date = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order)
    def pair = get102(departmentId, date)
    opuMap.each { k, v ->
        result[k] = pair[1] ? (pair[0][v]?pair[0][v]:0) : 0
    }
    if (!formData.accruing && reportPeriod.order != 1 && pair[1]) {
        def prevDate = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order - 1)
        pair = get102(departmentId, prevDate)
        opuMap.each { k, v ->
            result[k] = result[k] - (pair[1] ? (pair[0][v]?:0) : 0)
        }
    }
    return result
}

void calcCARow(def caRow) {
    def sourceFormTypeId = 717
    totalColumns.each { k ->
        caRow[k] = 0
    }
    departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind,
            getStartDate(formData.reportPeriodId), getEndDate(formData.reportPeriodId)).each {
        if (it.formTypeId == sourceFormTypeId) {
            def accruing = formData.accruing
            // в первом квартале нельзя создать форму с признаком нарастающим итогом
            if (getReportPeriod(formData.reportPeriodId).order == 1)
                accruing = false

            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def dataRowsETR_17 = formDataService.getDataRowHelper(source).allCached
                def totalRow_17 = getDataRow(dataRowsETR_17, 'total')
                totalColumns.each { k ->
                    caRow[k] = totalRow_17[k] / 1000
                }
            }
        }
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // получение данных из БО
    for(def row : dataRows) {
        if (row.getAlias())
            continue

        def management = getDepartmentManagement(row.department?.intValue())
        def records = calcBO(management?.getId())
        opuMap.each { k, v ->
            row[k] = records[k] ? records[k] : 0
        }
        row.sum34 = row.tax26411_01 + row.tax26411_02
        row.sum = row.sum34 + row.tax26411_01 + row.tax26411_02 + row.tax26411_03 + row.tax26411_13 + row.tax26411_12 +
                row.tax26412 + row.tax26410_09
    }

    // расчет по ЦА
    def caRow = getDataRow(dataRows, 'ca{wan}')
    calcCARow(caRow)

    // расчет итоговой строки
    def totalRow = getDataRow(dataRows, 'total')
    totalColumns.each { alias ->
        totalRow[alias] = summ(formData, dataRows, new ColumnRange(alias, 0, dataRows.size() - 2))
        def rateAlias = rateMap.find{ it.value==alias }?.key
        if (rateAlias)
            totalRow[rateAlias] = totalRow[alias] > 0 ? 100 : 0
    }

    // расчет процентов
    for(def row : dataRows) {
        if (row.getAlias() && row.getAlias() != 'ca{wan}')
            continue

        rateMap.each { k, v ->
            row[k] = calcRate(row, totalRow, v, k)
        }
    }

    dataRows.remove(caRow)
    dataRows.remove(totalRow)
    dataRows.sort { getDepartment(it.department?.intValue())?.name }
    dataRows.add(caRow)
    dataRows.add(totalRow)

    updateIndexes(dataRows)
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
        logger.warn("Строка %s: Графа «%s»: выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно)». Ячейка будет заполнена значением «0».",
                row.getIndex(), getColumnName(row, resultAlias))
    }
    return result
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def calcTotalRow = formData.createDataRow()
    totalColumns.each{
        calcTotalRow[it] = 0
    }

    // проверка нефиксированных строк
    for(def row : dataRows) {
        if (row.getAlias())
            continue
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        totalColumns.each{
            calcTotalRow[it] += row[it]?:0
        }

        def needValue = formData.createDataRow()
        def management = getDepartmentManagement(row.department?.intValue())
        def records = calcBO(management?.getId())
        opuMap.each { k, v ->
            needValue[k] = records[k]
        }
        needValue.sum34 = needValue.tax26411_01 + needValue.tax26411_02
        needValue.sum = needValue.sum34 + needValue.tax26411_01 + needValue.tax26411_02 + needValue.tax26411_03 + needValue.tax26411_13 + needValue.tax26411_12 +
                needValue.tax26412 + needValue.tax26410_09
        checkCalc(row, totalColumns, needValue, logger, true)
    }

    // проверка ЦА
    def calcСАRow = formData.createDataRow()
    calcCARow(calcСАRow)
    totalColumns.each{
        calcTotalRow[it] += calcСАRow[it]?:0
    }
    checkCalc(getDataRow(dataRows, 'ca{wan}'), totalColumns, calcСАRow, logger, true)

    // проверка итоговой строки
    checkCalc(getDataRow(dataRows, 'total'), totalColumns, calcTotalRow, logger, true)
}