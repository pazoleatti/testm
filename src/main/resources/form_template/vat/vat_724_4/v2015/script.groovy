package form_template.vat.vat_724_4.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * (724.4) Налоговые вычеты за прошедший налоговый период, связанные с изменением условий или расторжением договора,
 * в случае возврата ранее реализованных товаров (отказа от услуг) или возврата соответствующих сумм авансовых платежей
 * (Отчёт по сумме НДС, уплаченного в бюджет, в случае возврата ранее реализованных товаров (отказа от услуг)
 * или возврата соответствующих сумм авансовых платежей) (v.2015).
 *
 * formTemplateId=1603
 */

// графа 1 - rowNum
// графа   - fix
// графа 2 - name       - зависит от графы 3 - атрибут 901 - ACCOUNT_NAME - «Наименование счета», справочник 101 «План счетов бухгалтерского учета»
// графа 3 - number     - атрибут 900 - ACCOUNT - «Номер счета», справочник 101 «План счетов бухгалтерского учета»
// графа 4 - sum
// графа 5 - number2
// графа 6 - sum2
// графа 7 - nds

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
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
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

// все атрибуты
@Field
def allColumns = ['rowNum', 'fix', 'name', 'number', 'sum', 'number2', 'sum2', 'nds']

// Редактируемые атрибуты (графа 3..8)
@Field
def editableColumns = ['number', 'sum', 'number2', 'sum2', 'nds']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1, 3, 4, 6, 8)
@Field
def nonEmptyColumns = ['number', 'sum', 'number2', 'sum2', 'nds']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4, 6)
@Field
def totalColumns = ['sum', 'sum2']

// Группируемые атрибуты (графа 3, 5, 2, 4, 6, 7)
@Field
def sortColumns = ['number', 'number2', 'name', 'sum', 'sum2', 'nds']

// список алиасов подразделов
@Field
def sections = ['1', '2']

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

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

//// Кастомные методы

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index
    if (currentDataRow != null) {
        def alias = currentDataRow.getAlias()
        index = currentDataRow.getIndex()
        if (alias == null || alias.startsWith('head')) {
            index++
        }
    } else {
        def lastRow = getDataRow(dataRows, 'total2')
        index = lastRow.getIndex()
    }
    dataRowHelper.insert(getNewRow(), index)
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    autoFillColumns.each {
        newRow.getCell(it).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def section : sections) {
        def firstRow = getDataRow(dataRows, getFirstRowAlias(section))
        def lastRow = getDataRow(dataRows, getLastRowAlias(section))
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        // посчитать итоги по разделам
        def rows = (from <= to ? dataRows[from..to] : [])
        calcTotalSum(rows, lastRow, totalColumns)
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // начиная с периода год 2015 - новый добавилось два новых счета 60309.07 и 60309.08
    // версия от 2015 года, поэтому на более ранние года не проверяем
    boolean isNewCheck = (reportPeriod?.taxPeriod?.year > 2015 || reportPeriod?.order == 4)

    def isSection1 = false
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            isSection1 = (row.getAlias() == 'head1')
            continue
        }
        def index = row.getIndex()

        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 3. Проверка номера балансового счета (графа 5) по разделу 1
        if (isSection1 && row.number2 != null && row.nds != null) {
            def logicCheck3 = ((row.number2 == '60309.01' && row.nds in ['10', '18', '10/110', '18/118']) ||
                            (row.number2 in ['60309.04', '60309.05'] && row.nds == '18/118'))
            if (isNewCheck) {
                logicCheck3 = (logicCheck3 || (row.number2 == '60309.07' && row.nds == '18') || (row.number2 == '60309.08' && row.nds == '18/118'))
            }
            if (!logicCheck3) {
                def number2Name = getColumnName(row, 'number2')
                def ndsName = getColumnName(row, 'nds')
                def msg = "Строка $index: Графы «$number2Name», «$ndsName» заполнены неверно!"
                rowError(logger, row, msg)
            }
        }

        // 4. Проверка номера балансового счета (графа 5) по разделу 2
        if (!isSection1 && row.number2 != null && row.nds != null &&
                !(row.number2 in ['60309.02', '60309.03'] && row.nds == '18/118')) {
            def number2Name = getColumnName(row, 'number2')
            def ndsName = getColumnName(row, 'nds')
            def msg = "Строка $index: Графы «$number2Name», «$ndsName» заполнены неверно!"
            rowError(logger, row, msg)
        }
    }

    // 2. Проверка итоговых значений по разделам
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, getFirstRowAlias(section))
        def lastRow = getDataRow(dataRows, getLastRowAlias(section))
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])
        def tmpTotal = getTotalRow(sectionsRows)
        def hasError = false
        totalColumns.each { alias ->
            if (lastRow[alias] != tmpTotal[alias]) {
                logger.error(WRONG_TOTAL, getColumnName(lastRow, alias))
                hasError = true
            }
        }
        if (hasError) {
            break
        }
    }
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить нефиксированные строки
    deleteNotFixedRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(sourceDataRows, dataRows, getFirstRowAlias(section), getLastRowAlias(section))
                }
            }
        }
    }
}

// Удалить нефиксированные строки
void deleteNotFixedRows(def dataRows) {
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        updateIndexes(dataRows)
    }
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows строки приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def sourceDataRows, def destinationDataRows, def fromAlias, def toAlias) {
    def from = getDataRow(sourceDataRows, fromAlias).getIndex()
    def to = getDataRow(sourceDataRows, toAlias).getIndex() - 1
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getDataRow(destinationDataRows, toAlias).getIndex() - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationDataRows)
}

def getTotalRow(sectionsRows) {
    def newRow = formData.createDataRow()
    totalColumns.each { alias ->
        newRow.getCell(alias).setValue(BigDecimal.ZERO, null)
    }
    for (def row : sectionsRows) {
        totalColumns.each { alias ->
            def value1 = newRow.getCell(alias).value
            def value2 = (row.getCell(alias).value ?: BigDecimal.ZERO)
            newRow[alias] = value1 + value2
        }
    }
    return newRow
}

def getFirstRowAlias(def section) {
    return 'head' + section
}

def getLastRowAlias(def section) {
    return 'total' + section
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def section : sections) {
        def firstRow = getDataRow(dataRows, getFirstRowAlias(section))
        def lastRow = getDataRow(dataRows, getLastRowAlias(section))
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1
        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

        // Массовое разыменование строк НФ
        def columnList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
        refBookService.dataRowsDereference(logger, sectionsRows, columnList)

        sortRows(sectionsRows, sortColumns)
    }

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}

// TODO (SBRFACCTAX-15074) убрать
void checkTFLocal(BufferedInputStream inputStream, String fileName) {
    checkBeforeGetXml(inputStream, fileName);
    if (fileName != null && !fileName.toLowerCase().endsWith(".rnu")) {
        throw new ServiceException("Выбранный файл не соответствует формату rnu!");
    }
}

void importTransportData() {
    // TODO (SBRFACCTAX-15074) заменить на "ScriptUtils.checkTF(ImportInputStream, UploadFileName)"
    checkTFLocal(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 7
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null        // итоговая строка со значениями из тф для добавления
    def mapRows = [:]

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

            def newRow = getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex)
            if (rowCells.length < 8 || newRow == null) {
                continue
            }
            // определить раздел по техническому полю и добавить строку в нужный раздел
            def sectionIndex = pure(rowCells[8])
            if (mapRows[sectionIndex] == null) {
                mapRows[sectionIndex] = []
            }

            mapRows[sectionIndex].add(newRow)
        }
    } finally {
        reader.close()
    }

    def newRows = (mapRows.values().sum { it } ?: [])
    showMessages(newRows, logger)
    if (logger.containsLevel(LogLevel.ERROR) || newRows == null || newRows.isEmpty()) {
        return
    }

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && totalTF) {
        // итоговая строка для сверки сумм
        def totalTmp = formData.createStoreMessagingDataRow()
        calcTotalSum(newRows, totalTmp, totalColumns)
        checkTFSum(totalTmp, totalTF, totalColumns, fileRowIndex, logger, false)
        // итог в файле не должен совпадать с итогами в НФ
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
    }

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows

    def rows = []
    sections.each { section ->
        def firstRow = getDataRow(templateRows, getFirstRowAlias(section))
        def lastRow = getDataRow(templateRows, getLastRowAlias(section))
        def copyRows = mapRows[section]

        rows.add(firstRow)
        if (copyRows != null && !copyRows.isEmpty()) {
            rows.addAll(copyRows)

            // расчет итогов
            calcTotalSum(copyRows, lastRow, totalColumns)
        }
        rows.add(lastRow)
    }
    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
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
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    def int colOffset = 1
    def int colIndex

    // графа 3 - атрибут 900 - ACCOUNT - «Номер счета», справочник 101 «План счетов бухгалтерского учета»
    colIndex = 3
    def record101 = getRecordImport(101, 'ACCOUNT', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    newRow.number = record101?.record_id?.value

    // графа 2 - зависит от графы 3 - атрибут 901 - ACCOUNT_NAME - «Наименование счета», справочник 101 «План счетов бухгалтерского учета»
    if (record101 != null) {
        colIndex = 2
        def value1 = pure(rowCells[colIndex])
        def value2 = record101?.ACCOUNT_NAME?.value?.toString()
        formDataService.checkReferenceValue(101, value1, value2, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 4
    colIndex = 4
    newRow.sum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex = 5
    newRow.number2 = pure(rowCells[colIndex])

    // графа 6
    colIndex = 6
    newRow.sum2 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7
    colIndex = 7
    newRow.nds = pure(rowCells[colIndex])

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
    int COLUMN_COUNT = 8
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

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

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows
    def valuesTotal = [ getDataRow(templateRows, 'head1')?.fix, getDataRow(templateRows, 'head2')?.fix ]

    def rowIndex = 0
    def allValuesCount = allValues.size()
    def mapRows = [:]
    def sectionIndex = null
    def totalRowFromFileMap = [:]

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

        // Пропуск итоговых строк
        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        def firstValue = rowValues[INDEX_FOR_SKIP]
        if (valuesTotal.contains(firstValue)) {
            sectionIndex = firstValue[0]
            mapRows.put(sectionIndex, [])
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (firstValue == 'Итого') {
            rowIndex++
            def alias = getLastRowAlias(sectionIndex)
            totalRowFromFileMap[alias] = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }

        if (sectionIndex == null) {
            throw new ServiceException("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
        }

        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        mapRows[sectionIndex].add(newRow)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    def newRows = (mapRows.values().sum { it } ?: [])
    showMessages(newRows, logger)
    if (logger.containsLevel(LogLevel.ERROR) || newRows == null || newRows.isEmpty()) {
        return
    }

    def rows = []
    sections.each { section ->
        def firstRow = getDataRow(templateRows, getFirstRowAlias(section))
        def lastRow = getDataRow(templateRows, getLastRowAlias(section))
        def copyRows = mapRows[section]

        rows.add(firstRow)
        if (copyRows != null && !copyRows.isEmpty()) {
            rows.addAll(copyRows)
        }
        rows.add(lastRow)
    }
    updateIndexes(rows)

    // сравнение итогов
    if (!totalRowFromFileMap.isEmpty()) {
        mapRows.each { section, sectionRows ->
            def rowAlias = getLastRowAlias(section)
            def totalRow = getDataRow(templateRows, rowAlias)
            def totalRowFromFile = totalRowFromFileMap[rowAlias]
            compareSimpleTotalValues(totalRow, totalRowFromFile, sectionRows, totalColumns, formData, logger, false)
        }
    }

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
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNum')]),
            ([(headerRows[0][2]): 'Данные бухгалтерского учёта']),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'nds')]),

            ([(headerRows[1][2]): 'Налоговая база']),
            ([(headerRows[1][5]): 'НДС']),

            ([(headerRows[2][2]): 'наименование балансового счёта расхода']),
            ([(headerRows[2][3]): 'номер балансового счёта расхода']),
            ([(headerRows[2][4]): 'сумма']),
            ([(headerRows[2][5]): 'номер балансового счёта']),
            ([(headerRows[2][6]): 'сумма']),

            ([(headerRows[3][0]): '1'])
    ]
    (2..7).each { index ->
        headerMapping.add(([(headerRows[3][index]): index.toString()]))
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

    // Графа 3 - атрибут 900 - ACCOUNT - «Номер балансового счета», справочник 101 «План счетов бухгалтерского учета»
    def colIndex = 3
    def record = getRecordImport(101, 'ACCOUNT', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    newRow.number = record?.record_id?.value

    // Графа 2 - зависит от графы 3 - атрибут 901 - ACCOUNT_NAME - «Наименование балансового счета», справочник 101 «План счетов бухгалтерского учета»
    if (record != null) {
        colIndex = 2
        def value1 = values[colIndex]
        def value2 = record?.ACCOUNT_NAME?.value?.toString()
        formDataService.checkReferenceValue(101, value1, value2, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 4
    colIndex = 4
    newRow.sum = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 5
    colIndex = 5
    newRow.number2 = values[colIndex]

    // графа 6
    colIndex = 6
    newRow.sum2 = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 7
    colIndex = 7
    newRow.nds = values[colIndex]

    return newRow
}