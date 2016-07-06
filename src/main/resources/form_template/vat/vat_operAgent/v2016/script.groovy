package form_template.vat.vat_operAgent.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Разнарядка на безакцептное списание/зачисление по суммам НДС с территориальных банков, Московского банка и подразделений ЦА (по операциям налогового агента).
 *
 * formTemplateId=621
 *
 * TODO:
 *      - консолидация не полная, потому что не реализовали 724.1.1
 *      - дополнить тесты: консолидация, расчет, логические проверки, загрузка эксельки
 */

// графа 1  - code
// графа 2  - name
// графа 3  - totalVAT
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

// алиас строки "Всего по Сбербанку"
@Field
def totalRowAlias = 'total'

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
    for (def row : dataRows) {
        if (row.getAlias() == totalRowAlias) {
            continue
        }
        def index = row.getIndex()

        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 3. Арифметическая проверка (графа 3..6)
        def needValue = [:]
        needValue.month2 = calc5or6(row)
        needValue.month3 = calc5or6(row)
        needValue.month1 = calc4(row)
        def arithmeticCheckAlias = needValue.keySet().asList()
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
    }
    // 2. Проверка итоговых значений
    def totalRow = getDataRow(dataRows, totalRowAlias)
    def tmpTotalRow = formData.createStoreMessagingDataRow()
    calcTotal(dataRows, tmpTotalRow)
    totalColumns.each { alias ->
        if (tmpTotalRow[alias] != totalRow[alias]) {
            logger.error("Строка %d: " + WRONG_TOTAL, totalRow.getIndex(), getColumnName(totalRow, alias))
        }
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // пропустить итоговую строку
        if (row.getAlias() == totalRowAlias) {
            continue
        }
        // графа 5
        row.month2 = calc5or6(row)
        // графа 6
        row.month3 = calc5or6(row)
        // графа 4
        row.month1 = calc4(row)
    }
    // итог
    def totalRow = getDataRow(dataRows, totalRowAlias)
    calcTotal(dataRows, totalRow)
}

def calc4(def row) {
    if (row.totalVAT == null || row.month2 == null || row.month3 == null) {
        return null
    }
    return roundValue(row.totalVAT - row.month2 - row.month3)
}

def calc5or6(def row) {
    if (row.totalVAT == null) {
        return null
    }
    return roundValue(row.totalVAT / 3)
}

/**
 * Посчитать итоги.
 *
 * @param dataRows строки формы
 * @param totalRow строку в которую запишутся итоги
 */
void calcTotal(def dataRows, def totalRow) {
    def rows = dataRows.findAll { it.getAlias() != totalRowAlias }
    totalColumns.each { alias ->
        totalRow[alias] = 0
    }
    for (def row : rows) {
        totalColumns.each { alias ->
            totalRow[alias] += (row[alias] ?: 0)
        }
    }
}

void consolidation() {
    // мапа для связи строк формы и подразделении (id подразделения - алиас строки)
    def departmentMap = [
            4   : '018',
            8   : '042',
            20  : '070',
            27  : '067',
            32  : '049',
            44  : '054',
            52  : '077',
            64  : '055',
            82  : '044',
            88  : '040',
            97  : '016',
            102 : '013',
            109 : '052',
            37  : '038'
    ]
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // обнулить данные
    dataRows.each { row ->
        row.totalVAT = 0
    }

    // источники
    def sourcesTypeIds = [
            603, // 724.4
            605, // 724.7
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
        // графа 3
        row.totalVAT = calc3(sourceDataRowsMap, departmentId)
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

/**
 * графа 3 = A - B + C
 * Получить значение для графы 3 из источника.
 *
 * @param sourceDataRowsMap мапа со строками источников
 * @param departmentId подразделение
 */
def calc3(def sourceDataRowsMap, def departmentId) {
    // 724.7 - A
    def key = getKey(605, departmentId)
    def sourceDataRows = sourceDataRowsMap[key]
    def findRow = sourceDataRows?.find { it.getAlias() == 'total' }
    def a = (findRow ? findRow.ndsSum : 0)

    // 724.4 - B
    key = getKey(603, departmentId)
    sourceDataRows = sourceDataRowsMap[key]
    findRow = sourceDataRows?.find { it.getAlias() == 'total2' }
    def b = (findRow ? findRow.sum2 : 0)

    // 724.1.1 - C
    // TODO (Ramil Timerbaev) форму 724.1.1 еще не реализовали
    def c = 0

    return roundValue(a - b + c)
}

// Округляет число до требуемой точности
def roundValue(def value, int precision = 2) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
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

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 6
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
    def totalRow = getDataRow(dataRows, totalRowAlias)

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
            break
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
    def totalRowTmp = formData.createStoreMessagingDataRow()
    calcTotal(dataRows, totalRowTmp)
    compareTotalValues(totalRow, totalRowTmp, totalColumns, logger, false)

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
            ([(headerRows[0][2]): tmpRow.getCell('totalVAT').column.name]),
            ([(headerRows[0][3]): tmpRow.getCell('month1').column.name]),
            ([(headerRows[0][4]): tmpRow.getCell('month2').column.name]),
            ([(headerRows[0][5]): tmpRow.getCell('month3').column.name])
    ]
    (1..6).each { index ->
        headerMapping.add(([(headerRows[1][index - 1]): index.toString()]))
    }
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

    // графа 3..6
    ['totalVAT', 'month1', 'month2', 'month3'].each { alias ->
        colIndex++
        dataRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
}

/**
 * Заполняет итоговую строку нф значениями из экселя.
 *
 * @param dataRow итоговая строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
void fillTotalFromXls(def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)

    // графа 3..6
    def colIndex = 1
    ['totalVAT', 'month1', 'month2', 'month3'].each { alias ->
        colIndex++
        dataRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
}