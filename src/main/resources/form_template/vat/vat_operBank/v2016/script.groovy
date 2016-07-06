package form_template.vat.vat_operBank.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Разнарядка на безакцептное списание/зачисление по суммам НДС с территориальных банков, Московского банка и подразделений ЦА (по операциям банка, справочно)
 *
 * formTemplateId=620
 *
 * TODO:
 *      - консолидация не полная, потому что не реализовали 724.1.1
 *      - дополнить тесты
 */

// графа 1  - code
// графа 2  - name
// графа    - fix1
// графа 3  - totalVAT
// графа    - fix2
// графа 4  - month1
// графа 5  - month2
// графа 6  - month3

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
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
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

// Проверяемые на пустые значения атрибуты (графа 2..6)
@Field
def nonEmptyColumns = ['name', 'totalVAT', 'month1', 'month2', 'month3']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 3..6)
@Field
def totalColumns = ['totalVAT', 'month1', 'month2', 'month3']

@Field
def totalNDSColumns = ['totalVAT', 'month1']

// алиас строки "Всего по Сбербанку"
@Field
def totalAlias = 'R16'

@Field
def titleAlias = 'R17'

// алиас строки "Всего по Сбербанку (сумма НДС)"
@Field
def totalNDSAlias = 'R21'

// строки для строки "Всего по Сбербанку" (строки 1..15)
@Field
def totalRows = ['R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10', 'R11', 'R12', 'R13', 'R14', 'R15']

// строки для строки "Всего по Сбербанку (сумма НДС)" (строки 18..20)
@Field
def totalNDSRows = ['R18', 'R19', 'R20']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
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

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row in dataRows) {
        if (row.getAlias() in [totalAlias, titleAlias, totalNDSAlias]) {
            continue
        }
        def index = row.getIndex()

        // 1. Проверка заполнения граф
        if (row.getAlias() in totalColumns) {
            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)
        } else {
            def columns = (isCorrectionPeriod() ? ['totalVAT', 'month1'] : ['totalVAT'])
            checkNonEmptyColumns(row, index, columns, logger, true)
        }

        // 3. Арифметическая проверка (графа 4..6)
        if (row.getAlias() in totalRows) {
            def needValue = [:]
            needValue.month2 = calc5or6(row)
            needValue.month3 = calc5or6(row)
            needValue.month1 = calc4(row)
            def arithmeticCheckAlias = needValue.keySet().asList()
            checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
        }
    }

    // 2. Проверка итоговых значений
    def tmpTotalRow = formData.createStoreMessagingDataRow()
    // итоги
    def totalRow = getDataRow(dataRows, totalAlias)
    checkTotal(dataRows, totalRow, tmpTotalRow, totalRows, totalColumns)

    // итоги НДС
    def totalNDSRow = getDataRow(dataRows, totalNDSAlias)
    def columns = (isCorrectionPeriod() ? ['totalVAT', 'month1'] : ['totalVAT'])
    checkTotal(dataRows, totalNDSRow, tmpTotalRow, totalNDSRows, columns)
    if (!isCorrectionPeriod() && totalNDSRow.month1 != null) {
        logger.error("Строка %d: " + WRONG_TOTAL, totalRow.getIndex(), getColumnName(totalRow, 'month1'))
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row : dataRows) {
        if (row.getAlias() in [totalAlias, titleAlias, totalNDSAlias]) {
            continue
        }
        if (row.getAlias() in totalRows) {
            // графа 5
            row.month2 = calc5or6(row)
            // графа 6
            row.month3 = calc5or6(row)
            // графа 4
            row.month1 = calc4(row)
        }
    }

    // итоги
    def totalRow = getDataRow(dataRows, totalAlias)
    calcTotal(dataRows, totalRow, totalRows, totalColumns)

    // итоги НДС
    def totalNDSRow = getDataRow(dataRows, totalNDSAlias)
    def columns = (isCorrectionPeriod() ? ['totalVAT', 'month1'] : ['totalVAT'])
    calcTotal(dataRows, totalNDSRow, totalNDSRows, columns)
    if (!isCorrectionPeriod()) {
        totalNDSRow.month1 = null
    }
}

/**
 * графа 3 = A - B + C для строк 1..15
 * Получить значение для графы 3 из источника.
 *
 * @param sourceDataRowsMap мапа со строками источников
 * @param departmentId подразделение
 */
def calc3(def sourceDataRowsMap, def departmentId) {
    // 724.1 - A
    def key = getKey(600, departmentId)
    def sourceDataRows = sourceDataRowsMap[key]
    // строка Всего
    def findRow = sourceDataRows?.find { it.getAlias() == 'total' }
    def a = (findRow ? findRow.ndsSum : 0)
    // строка итого 7
    findRow = sourceDataRows?.find { it.getAlias() == 'total_7' }
    a += (findRow ? findRow.ndsBookSum - findRow.ndsDealSum : 0)

    // 724.4 - B
    key = getKey(603, departmentId)
    sourceDataRows = sourceDataRowsMap[key]
    findRow = sourceDataRows?.find { it.getAlias() == 'total1' }
    def b = (findRow ? findRow.sum2 : 0)

    // 724.1.1 - C
    // TODO (Ramil Timerbaev) форму 724.1.1 еще не реализовали
    def c = 0

    return roundValue(a - b + c)
}

/**
 * графа 3 = A - B + C - D для строк 18..21
 * Получить значение для графы 3 из источника.
 *
 * @param sourceDataRowsMap мапа со строками источников
 * @param departmentId подразделение
 */
def calc3NDS(def sourceDataRowsMap, def departmentId) {
    // 724.1 - A
    def key = getKey(600, departmentId)
    def sourceDataRows = sourceDataRowsMap[key]
    // строка Всего
    def findRow = sourceDataRows?.find { it.getAlias() == 'total' }
    def a = (findRow ? findRow.ndsSum : 0)
    // строка итого 7
    findRow = sourceDataRows?.find { it.getAlias() == 'total_7' }
    a += (findRow ? findRow.ndsSum - findRow.ndsDealSum : 0)

    // 724.4 - B, D
    key = getKey(603, departmentId)
    sourceDataRows = sourceDataRowsMap[key]
    findRow = sourceDataRows?.find { it.getAlias() == 'total1' }
    def b = (findRow ? findRow.sum2 : 0)
    findRow = sourceDataRows?.find { it.getAlias() == 'total2' }
    def d = (findRow ? b + findRow.sum2 : 0)

    // 724.1.1 - C
    // TODO (Ramil Timerbaev) форму 724.1.1 еще не реализовали
    def c = 0

    return roundValue(a - b + c - d)
}

def calc4(def row) {
    return roundValue((row.totalVAT ?: 0) - (row.month2 ?: 0) - (row.month3 ?: 0))
}

/**
 * Получить из источника значение для графы 4 строк 18..21.
 *
 * @param sourceDataRows строки источника
 * @param formTypeId тип источника
 * @param alias алиас строки текущей нф
 */
def calc4NDS(def sourceDataRowsMap, def departmentId) {
    if (!isCorrectionPeriod()) {
        return null
    }
    // 724.1.1
    // TODO (Ramil Timerbaev) форму 724.1.1 еще не реализовали
    def result = 0
    return roundValue(result)
}

def calc5or6(def row) {
    return roundValue((row.totalVAT ?: 0) / 3)
}

/**
 * Посчитать итоги.
 *
 * @param dataRows строки формы
 * @param totalRow строку в которую запишутся итоги
 * @param rowAliases список алисов строк участвующих в подсчете итогов
 * @param columns список графов участвующих в подсчете итогов
 */
void calcTotal(def dataRows, def totalRow, def rowAliases, def columns) {
    columns.each { alias ->
        totalRow[alias] = 0
    }
    def rows = dataRows.findAll { it.getAlias() in rowAliases }
    for (def row : rows) {
        columns.each { alias ->
            totalRow[alias] += (row[alias] ?: 0)
        }
    }
}

/**
 * Проверить итоги.
 *
 * @param dataRows строки нф
 * @param totalRow строка итогов
 * @param tmpRow временная строка строка для сверки итогов
 * @param rowAliases список алисов строк участвующих в подсчете итогов
 * @param columns список графов участвующих в подсчете итогов
 */
void checkTotal(def dataRows, def totalRow, def tmpRow, def rowAliases, def columns) {
    calcTotal(dataRows, tmpRow, rowAliases, columns)
    columns.each { alias ->
        if (tmpRow[alias] != totalRow[alias]) {
            logger.error("Строка %d: " + WRONG_TOTAL, totalRow.getIndex(), getColumnName(totalRow, alias))
        }
    }
}

void consolidation() {
    // мапа для связи строк формы и подразделении (id подразделения - алиас строки)
    def departmentMap = [
            4     : 'R1',
            8     : 'R2',
            20    : 'R3',
            27    : 'R4',
            32    : 'R5',
            44    : 'R6',
            52    : 'R7',
            64    : 'R8',
            82    : 'R9',
            88    : 'R10',
            97    : 'R11',
            102   : 'R12',
            109   : 'R13',
            37    : 'R14',
            219   : 'R15',
            213   : 'R18',
            15523 : 'R19',
            15518 : 'R20'
    ]
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // обнулить данные
    dataRows.each { row ->
        row.totalVAT = 0
        row.month1 = null
    }

    // источники
    def sourcesTypeIds = [
            600, // 724.1
            603, // 724.4
    ]
    // в корректирующем периоде использовать еще форму 724.1.1
    if (isCorrectionPeriod()) {
        // TODO (Ramil Timerbaev) форму 724.1.1 еще не реализовали, потом добавить
        // sourcesTypeIds.add(0)
    }
    def departmentFormTypeMap = [:] // departmentFormTypeMap
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId in sourcesTypeIds && departmentMap[it.departmentId]) {
            def key = getKey(it.formTypeId, it.departmentId)
            if (departmentFormTypeMap[key] == null) {
                departmentFormTypeMap[key] = []
            }
            departmentFormTypeMap[key].add(it)
        }
    }

    def sourceDataRowsMap = [:]
    // проверить источники и отобрать необходимые для консолидации
    departmentMap.keySet().each { departmentId ->
        def check2 = true
        sourcesTypeIds.each { formTypeId ->
            // 1. Проверка назначения только одной формы-источника по каждому виду формы и подразделению
            def key = getKey(formTypeId, departmentId)
            def departmentFormTypes = departmentFormTypeMap[key]
            if (departmentFormTypes?.size() > 1) {
                def alias = departmentMap[departmentId]
                def row = getDataRow(dataRows, alias)
                def kinds = departmentFormTypes.collect { it.kind.title }.join(', ')
                def departmentName = getDepartment(departmentId as Integer)?.shortName
                def formName = getFormTypeById(formTypeId)?.name
                logger.warn("Строка %d, подразделение «%s»: Текущей форме назначено несколько форм-источников (тип %s) подразделения «%s», вида «%s». " +
                        "При консолидации используется форма-источник типа «Консолидированная»", row.getIndex(), row.name, kinds, departmentName, formName)
            }

            // определить источник
            def departmentFormType = null
            if (departmentFormTypes?.size() == 1) {
                // если источник один то использовать его
                departmentFormType = departmentFormTypes.get(0)
            } else if (departmentFormTypes?.size() > 1) {
                // если для одного типа форм несколько источников в одном подразделении, тогда использовать консолидированную, иначе ничего не использовать
                departmentFormType = departmentFormTypes.find { it.kind == FormDataKind.CONSOLIDATED }
            }

            // получить данные источника и обработать
            def source = null
            if (departmentFormType) {
                source = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId,
                        formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            }
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceDataRowsMap[key] = formDataService.getDataRowHelper(source).allCached
                check2 = false
            }
        }
        if (check2) {
            // 2. Проверка наличия всех необходимых форм-источников
            def alias = departmentMap[departmentId]
            def row = getDataRow(dataRows, alias)
            def columnName = getColumnName(row, 'totalVAT')
            logger.warn("Строка %d, подразделение «%s»: Графа «%s» заполнена значением «0», " +
                    "так как отсутствуют все требуемые принятые формы-источники", row.getIndex(), row.name, columnName)
        }
    }

    // проверить источники и отобрать необходимые для консолидации
    departmentMap.keySet().each { departmentId ->
        def alias = departmentMap[departmentId]
        def row = getDataRow(dataRows, alias)
        // для строк 1..15 и 18..21 разные расчеты
        if (alias in totalRows) {
            // графа 3
            row.totalVAT = calc3(sourceDataRowsMap, departmentId)
        } else {
            // графа 3
            row.totalVAT = calc3NDS(sourceDataRowsMap, departmentId)
            // графа 4
            row.month1 = calc4NDS(sourceDataRowsMap, departmentId)
        }
    }
}

def getKey(def formTypeId, def departmentId) {
    return formTypeId + '#' + departmentId
}

@Field
def departmentMap = [:]

def getDepartment(Integer id) {
    if (id != null && departmentMap[id] == null) {
        departmentMap[id] = departmentService.get(id)
    }
    return departmentMap[id]
}

// Мапа для хранения типов форм (id типа формы -> тип формы)
@Field
def formTypeMap = [:]

/** Получить тип фомры по id. */
def getFormTypeById(def id) {
    if (formTypeMap[id] == null) {
        formTypeMap[id] = formTypeService.get(id)
    }
    return formTypeMap[id]
}

@Field
def isCorrectionPeriod = null

def isCorrectionPeriod() {
    if (isCorrectionPeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isCorrectionPeriod = departmentReportPeriod.getCorrectionDate() != null
    }
    return isCorrectionPeriod
}

// Округляет число до требуемой точности
def roundValue(def value, int precision = 2) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 8
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = tmpRow.getCell('code').column.name
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

    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def totalRow = getDataRow(dataRows, totalAlias)
    def totalNDSRow = getDataRow(dataRows, totalNDSAlias)
    def titleRow = getDataRow(dataRows, titleAlias)

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    def templateRows = formTemplate.rows

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        rowIndex++
        // итоговая строка
        if (rowValues[INDEX_FOR_SKIP] == totalRow.name) {
            fillTotalFromXls(totalRow, rowValues, fileRowIndex, rowIndex, colOffset)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // итоговая строка НДС
        if (rowValues[INDEX_FOR_SKIP] == totalNDSRow.name) {
            fillTotalFromXls(totalNDSRow, rowValues, fileRowIndex, rowIndex, colOffset, true)

            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // строка с надписями
        if (rowValues[2] == titleRow.fix1 && rowValues[4] == titleRow.fix2) {
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        if (rowIndex > dataRows.size()) {
            break
        }
        // найти нужную строку нф
        def dataRow = dataRows.get(rowIndex - 1)
        def templateRow = getDataRow(templateRows, dataRow.getAlias())
        // заполнить строку нф значениями из эксель
        fillRowFromXls(templateRow, dataRow, rowValues, fileRowIndex, rowIndex, colOffset)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // сравнение итогов
    // итоги
    def totalRowTmp = formData.createStoreMessagingDataRow()
    calcTotal(dataRows, totalRowTmp, totalRows, totalColumns)
    compareTotalValues(totalRow, totalRowTmp, totalColumns, logger, false)
    // итоги НДС - графа 3, 4
    def columns = (isCorrectionPeriod() ? ['totalVAT', 'month1'] : ['totalVAT'])
    calcTotal(dataRows, totalRowTmp, totalNDSRows, columns)
    compareTotalValues(totalNDSRow, totalRowTmp, columns, logger, false)

    showMessages(dataRows, logger)
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

    // для проверки шапки
    def headerMapping = [
            ([(headerRows[0][0]): tmpRow.getCell('code').column.name]),
            ([(headerRows[0][1]): tmpRow.getCell('name').column.name]),
            ([(headerRows[0][3]): tmpRow.getCell('totalVAT').column.name]),
            ([(headerRows[0][5]): tmpRow.getCell('month1').column.name]),
            ([(headerRows[0][6]): tmpRow.getCell('month2').column.name]),
            ([(headerRows[0][7]): tmpRow.getCell('month3').column.name]),
            ([(headerRows[1][0]): '1']),
            ([(headerRows[1][1]): '2']),
            ([(headerRows[1][3]): '3']),
            ([(headerRows[1][5]): '4']),
            ([(headerRows[1][6]): '5']),
            ([(headerRows[1][7]): '6']),
    ]
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Заполняет заданную строку нф значениями из экселя.
 *
 * @param templateRow строка макета
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
void fillRowFromXls(def templateRow, def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)
    def tmpValues = formData.createStoreMessagingDataRow()

    // графа 1, 2
    def colIndex = -1
    ['code', 'name'].each { alias ->
        colIndex++
        tmpValues[alias] = values[colIndex]
    }

    // Проверить фиксированные значения
    tmpValues.keySet().toArray().each { alias ->
        def value = tmpValues[alias]?.toString()
        def valueExpected = StringUtils.cleanString(templateRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
    }

    // графа 3
    colIndex = 3
    dataRow.totalVAT = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 4
    colIndex = 5
    dataRow.month1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex = 6
    dataRow.month2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6
    colIndex = 7
    dataRow.month3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
}

/**
 * Заполняет итоговую строку нф значениями из экселя.
 *
 * @param dataRow итоговая строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 * @param isNDS последняя итоговая строка (НДС)
 */
void fillTotalFromXls(def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset, def isNDS = false) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)

    if (isNDS) {
        // графа 3
        colIndex = 3
        dataRow.totalVAT = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

        if (isCorrectionPeriod()) {
            // графа 4
            colIndex = 5
            dataRow.month1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        }
    } else {
        // графа 3
        colIndex = 3
        dataRow.totalVAT = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

        // графа 4
        colIndex = 5
        dataRow.month1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

        // графа 5
        colIndex = 6
        dataRow.month2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

        // графа 6
        colIndex = 7
        dataRow.month3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
}