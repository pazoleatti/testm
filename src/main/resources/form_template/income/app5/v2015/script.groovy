package form_template.income.app5.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * 6.9	(Приложение 5) Сведения для расчета налога на прибыль
 * formTemplateId=1372
 *
 * @author Lenar Haziev
 */

// графа 1  - number
// графа -  - fix
// графа 2  - regionBank                атрибут 161 NAME "Наименование подразделение" - справочник 30 "Подразделения"
// графа 3  - regionBankDivision        атрибут 161 NAME "Наименование подразделение" - справочник 30 "Подразделения"
// графа 4  - divisionName
// графа 5  - kpp
// графа 6  - avepropertyPricerageCost
// графа 7  - workersCount
// графа 8  - subjectTaxCredit
// графа 9  - decreaseTaxSum
// графа 10 - taxRate

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        logicCheckBeforeCalc()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
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
        formDataService.consolidationTotal(formData, logger, userInfo, ['total'])
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
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
def allColumns = ['number', 'fix', 'regionBank', 'regionBankDivision', 'divisionName', 'kpp', 'avepropertyPricerageCost',
                  'workersCount', 'subjectTaxCredit', 'decreaseTaxSum', 'taxRate']

// Редактируемые атрибуты
@Field
def editableColumns = ['regionBankDivision', 'kpp', 'avepropertyPricerageCost', 'workersCount', 'subjectTaxCredit', 'decreaseTaxSum', 'taxRate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['regionBank', 'divisionName']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['regionBank', 'regionBankDivision', 'divisionName', 'kpp', 'avepropertyPricerageCost',
                       'workersCount', 'subjectTaxCredit']

// Группируемые атрибуты
@Field
def groupColumns = ['regionBankDivision', 'regionBank']

// Атрибуты для итогов
@Field
def totalColumns = ['avepropertyPricerageCost', 'workersCount', 'subjectTaxCredit', 'decreaseTaxSum']

@Field
def endDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
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

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

//// Кастомные методы

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def newRow = getNewRow()

    def index = 0
    if (currentDataRow != null && currentDataRow.getAlias() != null) {
        // выбрана итоговая - вставить перед итоговой
        index = currentDataRow.getIndex()
    } else if (currentDataRow != null && currentDataRow.getAlias() == null) {
        // выбрана фиксированная строка - после выбранной нефиксированной
        index = currentDataRow.getIndex() + 1
    } else {
        // не выбрана строка - вставить перед итоговой
        def dataRows = dataRowHelper.allCached
        index = getDataRow(dataRows, 'total').getIndex()
    }

    dataRowHelper.insert(newRow, index)
}

// Логические проверки
void logicCheckBeforeCalc() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    for (row in dataRows) {
        if (row != null && row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка наличия значения «Наименование подразделения» в справочнике «Подразделения»
        def departmentParam
        if (row.regionBankDivision != null) {
            departmentParam = getRefBookValue(30, row.regionBankDivision)
        }
        if (departmentParam == null || departmentParam.isEmpty()) {
            rowServiceException(row, errorMsg + "Не найдено подразделение территориального банка!")
        } else {
            long centralId = 113 // CODE Центрального аппарата.
            // У Центрального аппарата родительским подразделением должен быть он сам
            if (centralId != row.regionBankDivision) {
                // графа 2 - название подразделения
                if (departmentParam.get('PARENT_ID')?.getReferenceValue() == null) {
                    rowServiceException(row, errorMsg + "Для подразделения территориального банка " +
                            "«${departmentParam.NAME.stringValue}» в справочнике «Подразделения» отсутствует значение " +
                            "наименования родительского подразделения!")
                }
            }
        }

        // Определение условий для проверок 2, 3, 4
        def depParam = getDepParam(departmentParam, index)
        def depId = depParam.get(RefBook.RECORD_ID_ALIAS).numberValue as long ?: -1
        def departmentName = depParam?.NAME?.stringValue ?: "Не задано"
        def incomeParam = getProvider(33).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $depId", null)
        def incomeParamTable = getIncomeParamTable(depParam)

        // 2. Проверка наличия формы настроек подразделения
        if (incomeParam == null || incomeParam.isEmpty()) {
            rowServiceException(row, errorMsg + "Для подразделения «${departmentName}» не создана форма настроек подразделений!")
        }

        // 3. Проверка наличия строки с «КПП» в табличной части формы настроек подразделения
        // 4. Проверка наличия значения «Наименование для Приложения №5» в форме настроек подразделения
        for (int i = 0; i < incomeParamTable.size(); i++) {
            if (row.kpp != null && row.kpp != '') {
                if (incomeParamTable?.get(i)?.KPP?.stringValue == row.kpp) {
                    if (incomeParamTable?.get(i)?.ADDITIONAL_NAME?.stringValue == null) {
                        rowServiceException(row, errorMsg + "Для подразделения «${departmentName}» на форме настроек подразделений по КПП «${row.kpp}» отсутствует значение атрибута «Наименование для «Приложения №5»!")
                    }
                    break
                }
                if (i == incomeParamTable.size() - 1) {
                    rowServiceException(row, errorMsg + "Для подразделения «${departmentName}» на форме настроек подразделений отсутствует строка с КПП «${row.kpp}»!")
                }
            }
        }
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    boolean wasError = false

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        def departmentParam
        if (row.regionBankDivision != null) {
            departmentParam = getRefBookValue(30, row.regionBankDivision)
        }
        // 1. Проверка на заполнение поля «<Наименование поля>»
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // Проверки НСИ
        def depParam = getDepParam(departmentParam, index)
        def departmentName = depParam?.NAME?.stringValue ?: "Не задано"
        def incomeParamTable = getIncomeParamTable(depParam)

        // 2. Проверка значения графы «Наименование подразделения для декларации»
        for (int i = 0; i < incomeParamTable?.size(); i++) {
            if (row.kpp != null && row.kpp != '') {
                if (incomeParamTable?.get(i)?.KPP?.stringValue == row.kpp) {
                    if (incomeParamTable?.get(i)?.ADDITIONAL_NAME?.stringValue != row.divisionName) {
                        def name = getColumnName(row, 'divisionName')
                        rowServiceException(row, errorMsg + "Значение графы «$name» не соответствует значению на форме настроек подразделений для подразделения «${departmentName}» по КПП «${row.kpp}»!")
                    }
                }
            }
        }

        if (row.kpp && checkPattern(logger, row, 'kpp', row.kpp, KPP_PATTERN, wasError ? null : KPP_MEANING, true)) {
            wasError = true
        }
    }

    // 3. Арифметические проверки расчета итоговой строки
    checkTotalSum(dataRows, totalColumns, logger, true)
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    // Удаление подитогов
    deleteAllAliased(dataRows)

    if(formDataEvent != FormDataEvent.IMPORT) {
        for (row in dataRows) {
            // название подразделения
            row.regionBank = calc2(row)

            // наименование подразделения в декларации
            row.divisionName = calc4(row)
        }
    }

    dataRows.add(getTotalRow(dataRows))
}

void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, dataRows.find { it.getAlias() == 'total' }, null)
    dataRowHelper.saveSort()
}

// название подразделения
def calc2(def row) {
    def departmentParam
    if (row.regionBankDivision != null) {
        departmentParam = getRefBookValue(30, row.regionBankDivision)
    }
    if (departmentParam == null || departmentParam.isEmpty()) {
        return null
    }

    long centralId = 113 // CODE Центрального аппарата.
    // У Центрального аппарата родительским подразделением должен быть он сам
    if (centralId == row.regionBankDivision) {
        return centralId
    } else {
        return departmentParam.get('PARENT_ID').getReferenceValue()
    }
}

// наименование подразделения в декларации
def calc4(def row) {
    def divisionName
    def departmentParam
    if (row.regionBankDivision != null) {
        departmentParam = getRefBookValue(30, row.regionBankDivision)
    }
    def depParam = getDepParam(departmentParam, row.getIndex())
    def incomeParamTable = getIncomeParamTable(depParam)
    for (int i = 0; i < incomeParamTable?.size(); i++) {
        if (row.kpp != null && row.kpp != '') {
            if (incomeParamTable?.get(i)?.KPP?.stringValue == row.kpp) {
                return incomeParamTable?.get(i)?.ADDITIONAL_NAME?.stringValue
            }
        }
    }
    return null
}

// Расчет итоговой строки
def getTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 4
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getNewRow() {
    def newRow = formData.createStoreMessagingDataRow()

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
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
// для получения данных (согласно алгоритму 1.8.4.5.1)
def getDepParam(def departmentParam, def rowNum) {
    if (departmentParam == null) {
        return null
    }
    def depParam
    def departmentId = departmentParam.get(RefBook.RECORD_ID_ALIAS).numberValue as int
    def departmentType = departmentService.get(departmentId).getType()
    if (departmentType.equals(departmentType.TERR_BANK)) {
        depParam = departmentParam
    } else {
        def tbCode = (Integer) departmentParam.get('PARENT_ID').getReferenceValue()
        def taxPlaningTypeCode = departmentService.get(tbCode).getType().MANAGEMENT.getCode()
        depParamList = getProvider(30).getRecords(getReportPeriodEndDate(), null, "PARENT_ID = $tbCode and TYPE = $taxPlaningTypeCode", null)
        if(depParamList != null && depParamList.size()>0){
            depParam = depParamList.get(0)
        }
        if(depParam == null){
            throw new ServiceException("Строка $rowNum: Не найдены параметры подразделения")
        }
    }

    return depParam
}

// Получение параметров (справочник 330)
def getIncomeParamTable(def depParam) {
    if (depParam == null) {
        return null
    }
    def depId = depParam.get(RefBook.RECORD_ID_ALIAS).numberValue as long
    def incomeParam = getProvider(33).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $depId", null)
    if (incomeParam != null && !incomeParam.isEmpty()) {
        def link = incomeParam.get(0).record_id.value
        def incomeParamTable = getProvider(330).getRecords(getReportPeriodEndDate() - 1, null, "LINK = $link", null)
        return incomeParamTable
    }
    return null
}

void importTransportData() {
    ScriptUtils.checkTF(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 10
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null		// итоговая строка со значениями из тф для добавления
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    try {
        // пропускаем заголовок
        rowCells = reader.readNext()
        if (isEmptyCells(rowCells)) {
            logger.error('Первой строкой должен идти заголовок, а не пустая строка')
        }
        // пропускаем пустую строку
        rowCells = reader.readNext()
        if (rowCells == null || !isEmptyCells(rowCells)) {
            logger.error('Вторая строка должна быть пустой')
        }
        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }
            newRows.add(getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex))
        }
    } finally {
        reader.close()
    }

    def totalRow = getTotalRow(newRows)
    // добавить итоговую строку
    newRows.add(totalRow)

    showMessages(newRows, logger)

    // сравнение итогов
    checkAndSetTFSum(totalRow, totalTF, totalColumns, totalTF?.getImportIndex(), logger, false)

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(newRows)
        formDataService.getDataRowHelper(formData).allCached = newRows
    }
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}", fileRowIndex))
        return newRow
    }

    def int colOffset = 1
    def int colIndex = 1

    // графа 2, 3
    ['regionBank', 'regionBankDivision'].each { alias ->
        colIndex++
        newRow[alias] = getRecordIdImport(30, 'NAME', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    }

    // графа 4, 5
    ['divisionName', 'kpp'].each { alias ->
        colIndex++
        newRow[alias] = pure(rowCells[colIndex])
    }

    // графа 6..10
    ['avepropertyPricerageCost', 'workersCount', 'subjectTaxCredit', 'decreaseTaxSum', 'taxRate'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    }
    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 10
    int HEADER_ROW_COUNT = 3
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
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        rowIndex++
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Итого") {
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

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

    def totalRow = getTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    // сравнение итогов
    compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)

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
    checkHeaderSize(headerRows[0].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]) : getColumnName(tmpRow, 'number')]),
            ([(headerRows[0][2]) : getColumnName(tmpRow, 'regionBank')]),
            ([(headerRows[0][3]) : getColumnName(tmpRow, 'regionBankDivision')]),
            ([(headerRows[0][4]) : getColumnName(tmpRow, 'divisionName')]),
            ([(headerRows[0][5]) : getColumnName(tmpRow, 'kpp')]),
            ([(headerRows[0][6]) : getColumnName(tmpRow, 'avepropertyPricerageCost')]),
            ([(headerRows[0][7]) : getColumnName(tmpRow, 'workersCount')]),
            ([(headerRows[0][8]) : getColumnName(tmpRow, 'subjectTaxCredit')]),
            ([(headerRows[0][9]) : 'Льготы по налогу в бюджет субъекта (руб.)']),
            ([(headerRows[1][9]) : 'Уменьшение суммы налога (руб.)']),
            ([(headerRows[1][10]): 'Ставка налога (%)']),
            ([(headerRows[2][0]): '1'])
    ]
    (2..10).each {
        headerMapping.add(([(headerRows[2][it]): it.toString()]))
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

    // графа 3
    def colIndex = 3
    newRow.regionBankDivision = getRecordIdImport(30, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // графа 5
    colIndex = 5
    newRow.kpp = values[colIndex]

    // графа 6..10
    ['avepropertyPricerageCost', 'workersCount', 'subjectTaxCredit', 'decreaseTaxSum', 'taxRate'].each { alias ->
        colIndex++
        newRow[alias] = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    }

    return newRow
}