package form_template.etr.etr_4_17.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Аналитический отчет «Сведения о начисленных и уплачиваемых налогах, сборах и взносах, отнесенных на расходы» (ЦА)
 * formTemplateId = 717
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
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

// Редактируемые атрибуты
@Field
def editableColumns = ['department']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['parentTB', 'tax26411_01', 'tax26411_02', 'sum34', 'rate5', 'tax26411_03', 'rate7', 'tax26411_13', 'rate9', 'tax26411_12', 'rate11', 'tax26412', 'rate13', 'tax26410_09', 'rate15', 'sum', 'rate']

// Атрибуты по которым рассчитываются итоги(не включая проценты)
@Field
def totalColumns = ['tax26411_01', 'tax26411_02', 'sum34', 'tax26411_03', 'tax26411_13', 'tax26411_12', 'tax26412', 'tax26410_09', 'sum']

// Атрибуты по которым рассчитываются итоги(включая проценты)
@Field
def allTotalColumns = ['tax26411_01', 'tax26411_02', 'sum34', 'rate5', 'tax26411_03', 'rate7', 'tax26411_13', 'rate9', 'tax26411_12', 'rate11', 'tax26412', 'rate13', 'tax26410_09', 'rate15', 'sum', 'rate']

@Field
def nonEmptyColumns = ['parentTB', 'department']

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
def recordCache = [:]

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
def reportPeriodEndDate

def getEndDate(int reportPeriodId) {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(reportPeriodId)?.time
    }
    return reportPeriodEndDate
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
def parentTBMap = [:]

@Field
def departmentMap = [:]

def getParentTBId(def id) {
    if (id != null && parentTBMap[id] == null) {
        parentTBMap[id] = departmentService.getParentTBId(id)
    }
    return parentTBMap[id]
}

def getDepartment(Integer id) {
    if (id != null && departmentMap[id] == null) {
        departmentMap[id] = departmentService.get(id)
    }
    return departmentMap[id]
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
            def parentTB = getParentTBId(row.department.intValue())
            if (parentTB == 113) {
                if (!formData.accruing && reportPeriod.order != 1) {
                    checkOpuCodes(getDepartment(row.department.intValue()), prevDate)
                }
                checkOpuCodes(getDepartment(row.department.intValue()), date)
            } else {
                logger.error("Строка %s: Графа «%s»: Выбранное подразделение не является дочерним подразделением для ЦА!",
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

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // получение данных из БО
    for(def row : dataRows) {
        if (row.getAlias())
            continue

        row.parentTB = row.department ? getParentTBId(row.department.intValue()) : null
        def records = calcBO(row.department?.intValue())
        opuMap.each { k, v ->
            row[k] = records[k]
        }
        row.sum34 = row.tax26411_01 + row.tax26411_02
        row.sum = row.sum34 + row.tax26411_03 + row.tax26411_13 + row.tax26411_12 +
                row.tax26412 + row.tax26410_09
    }

    // расчет итоговой строки
    def totalRow = getDataRow(dataRows, 'total')
    totalColumns.each { alias ->
        totalRow[alias] = dataRows.size() > 1 ? summ(formData, dataRows, new ColumnRange(alias, 0, dataRows.size() - 2)) : 0
        def rateAlias = rateMap.find{ it.value==alias }?.key
        if (rateAlias)
            totalRow[rateAlias] = totalRow[alias] > 0 ? 100 : 0
    }

    // расчет процентов
    for(def row : dataRows) {
        if (row.getAlias())
            continue

        rateMap.each { k, v ->
            row[k] = calcRate(row, totalRow, v, k)
        }
    }

    dataRows.remove(totalRow)
    dataRows.sort { getDepartment(it.department?.intValue())?.name }
    dataRows.add(totalRow)
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
    for(def row : dataRows) {
        if (row.getAlias())
            continue
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        if (row.department != null) {
            def parentTB = getParentTBId(row.department.intValue())
            if (parentTB != 113) {
                logger.error("Строка %s: Графа «%s»: Выбранное подразделение не является дочерним подразделением для ЦА!",
                        row.getIndex(), getColumnName(row, 'department'))
            }
        }
        def needValue = formData.createDataRow()
        needValue.parentTB = row.department ? getParentTBId(row.department.intValue()) : null

        def records = calcBO(row.department?.intValue())
        opuMap.each { k, v ->
            needValue[k] = records[k]
        }
        needValue.sum34 = needValue.tax26411_01 + needValue.tax26411_02
        needValue.sum = needValue.sum34 + needValue.tax26411_03 + needValue.tax26411_13 + needValue.tax26411_12 +
                needValue.tax26412 + needValue.tax26410_09
        checkCalc(row, totalColumns, needValue, logger, true)
    }
    checkTotalSum(dataRows, totalColumns, logger, true)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getEndDate(formData.reportPeriodId), rowIndex, colIndex, logger, required)
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 20
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

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
    def totalRowFromFile = null

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        def rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "ИТОГО") {
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, true)
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows

    // итоговая строка
    def totalRow = getDataRow(templateRows, 'total')
    rows.add(totalRow)
    updateIndexes(rows)
    // сравнение итогов
    compareSimpleTotalValues(totalRow, totalRowFromFile, rows, allTotalColumns, formData, logger, false)

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
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
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def headers = formTemplate.getHeaders()
    def headerMapping = []
    [
            [i: 0, j:0, alias: 'fix'],
            [i: 0, j:2, alias: 'parentTB'],
            [i: 0, j:3, alias: 'department'],
            [i: 0, j:4, alias: 'tax26411_01'],
            [i: 0, j:8, alias: 'tax26411_03'],
            [i: 0, j:10, alias: 'tax26411_13'],
            [i: 0, j:12, alias: 'tax26411_12'],
            [i: 0, j:14, alias: 'tax26412'],
            [i: 0, j:16, alias: 'tax26410_09'],
            [i: 0, j:18, alias: 'sum'],

            [i: 1, j:4, alias: 'tax26411_01'],
            [i: 1, j:5, alias: 'tax26411_02'],
            [i: 1, j:6, alias: 'sum34'],
            [i: 1, j:7, alias: 'rate5'],
            [i: 1, j:8, alias: 'tax26411_03'],
            [i: 1, j:9, alias: 'rate7'],
            [i: 1, j:10, alias: 'tax26411_13'],
            [i: 1, j:11, alias: 'rate9'],
            [i: 1, j:12, alias: 'tax26411_12'],
            [i: 1, j:13, alias: 'rate11'],
            [i: 1, j:14, alias: 'tax26412'],
            [i: 1, j:15, alias: 'rate13'],
            [i: 1, j:16, alias: 'tax26410_09'],
            [i: 1, j:17, alias: 'rate15'],
            [i: 1, j:18, alias: 'sum'],
            [i: 1, j:19, alias: 'rate'],

            [i: 2, j:4, alias: 'tax26411_01'],
            [i: 2, j:5, alias: 'tax26411_02'],
            [i: 2, j:6, alias: 'sum34'],
            [i: 2, j:7, alias: 'rate5'],
            [i: 2, j:8, alias: 'tax26411_03'],
            [i: 2, j:9, alias: 'rate7'],
            [i: 2, j:10, alias: 'tax26411_13'],
            [i: 2, j:11, alias: 'rate9'],
            [i: 2, j:12, alias: 'tax26411_12'],
            [i: 2, j:13, alias: 'rate11'],
            [i: 2, j:14, alias: 'tax26412'],
            [i: 2, j:15, alias: 'rate13'],
            [i: 2, j:16, alias: 'tax26410_09'],
            [i: 2, j:17, alias: 'rate15'],
            [i: 2, j:18, alias: 'sum'],
            [i: 2, j:19, alias: 'rate'],

            [i: 3, j:0, alias: 'fix'],
            [i: 3, j:2, alias: 'parentTB'],
            [i: 3, j:3, alias: 'department'],
            [i: 3, j:4, alias: 'tax26411_01'],
            [i: 3, j:5, alias: 'tax26411_02'],
            [i: 3, j:6, alias: 'sum34'],
            [i: 3, j:7, alias: 'rate5'],
            [i: 3, j:8, alias: 'tax26411_03'],
            [i: 3, j:9, alias: 'rate7'],
            [i: 3, j:10, alias: 'tax26411_13'],
            [i: 3, j:11, alias: 'rate9'],
            [i: 3, j:12, alias: 'tax26411_12'],
            [i: 3, j:13, alias: 'rate11'],
            [i: 3, j:14, alias: 'tax26412'],
            [i: 3, j:15, alias: 'rate13'],
            [i: 3, j:16, alias: 'tax26410_09'],
            [i: 3, j:17, alias: 'rate15'],
            [i: 3, j:18, alias: 'sum'],
            [i: 3, j:19, alias: 'rate']
    ].each {
        headerMapping.add([(headerRows[it.i][it.j]): it.alias!=null?headers[it.i][it.alias]:it.value ])
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
 * @param isTotal признак того что строка итоговая
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def isTotal = false) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 2, 3
    colIndex = 2
    if (!isTotal) {
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }
        newRow.parentTB = getRecordIdImport(30, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
        colIndex++
        newRow.department = getRecordIdImport(30, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    } else {
        colIndex++
    }

    // графа 4..19
    allTotalColumns.each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
    return newRow
}