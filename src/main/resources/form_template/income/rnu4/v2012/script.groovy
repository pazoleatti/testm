package form_template.income.rnu4.v2012

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * (РНУ-4) Простой регистр налогового учёта «доходы»
 * formTemplateId=316
 *
 */

// 1 rowNumber № пп
// 2 code      Код налогового учета
// 3 balance   Номер
// 4 name      Наименование счёта
// 5 sum       Сумма дохода за отчётный квартал

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
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
    case FormDataEvent.COMPOSE: // Консолидация
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
        }
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
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

// Редактируемые атрибуты
@Field
def editableColumns = ['balance', 'sum']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'code', 'name']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['balance', 'sum']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['sum']

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
                def Date date, boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date, rowIndex,
            cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {
        // Удаление подитогов
        deleteAllAliased(dataRows)

        // сортируем по кодам
        dataRows.sort { getKnu(it.balance) }
    }

    // посчитать "итого по коду"
    def totalRows = [:]

    def sum = 0
    def prevBalance = null
    dataRows.eachWithIndex { row, i ->
        def code = getKnu(row.balance)
        if (code != null) { // Строки без кода не образуют группы
            // Если код поменялся, то создать новую строку итого с предыдущей суммой
            if (prevBalance != null && prevBalance != row.balance) {
                totalRows.put(i, getNewRow(getKnu(prevBalance), sum))
                sum = 0
            }
            // Если строка последняя то тоже создать строку итого с предудущей суммой + слагаемое из текущей строки
            if (i == dataRows.size() - 1) {
                sum += row.sum ?: 0
                totalRows.put(i + 1, getNewRow(code, sum))
                sum = 0
            }
            sum += row.sum ?: 0
        }
        prevBalance = row.balance
    }

    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        dataRows.add(index + i++, row)
    }

    // Общий итог
    dataRows.add(dataRows.size(), calcTotalRow(dataRows))
    dataRowHelper.save(dataRows)

    // Сортировка групп и строк
    sortFormDataRows()
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 4

    ['rowNumber', 'fix', 'sum'].each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)

    return totalRow
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    //две карты: одна с реальными значениями итого по кодам, а вторая - с рассчитанными
    def totalRows = [:]
    def sumRowsByCode = [:]

    for (def row : dataRows) {
        if (row.getAlias() ==~ /total\d+/) { // если подитог
            totalRows[row.getAlias().replace('total', '')] = row.sum
            continue
        }
        if (row.getAlias() != null) {
            continue
        }

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 4. Арифметическая проверка итоговых значений по каждому <Коду классификации доходов>
        def code = getKnu(row.balance)

        if (sumRowsByCode[code] != null) {
            sumRowsByCode[code] += row.sum ?: 0
        } else {
            sumRowsByCode[code] = row.sum ?: 0
        }
    }

    //4. Арифметическая проверка итоговых значений по каждому <Коду классификации доходов>
    totalRows.each { key, val ->
        if (totalRows.get(key) != sumRowsByCode.get(key)) {
            def msg = formData.createDataRow().getCell('sum').column.name
            logger.error("Неверное итоговое значение по коду '$key' графы «$msg»!")
        }
    }

    // 5. Арифметическая проверка итогового значения по всем строкам для «Графы 5»
    checkTotalSum(dataRows, totalColumns, logger, true)
}

// Получить новую строку подитога
def getNewRow(def alias, def sum) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    newRow.sum = sum
    newRow.fix = 'Итого по КНУ ' + alias
    newRow.getCell('fix').colSpan = 4
    ['rowNumber', 'fix', 'sum'].each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

def String getKnu(def code) {
    if (code == null) {
        return null
    }
    return getRefBookValue(28, code)?.CODE?.stringValue
}

/**
 * Консолидация.
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).allCached.each { sRow ->
                    if (sRow.getAlias() == null || sRow.getAlias() == '') {
                        def isFind = false
                        // строки приемника - искать совпадения, если совпадения есть, то суммировать графу 5
                        for (def row : dataRows) {
                            if (sRow.balance == row.balance) {
                                isFind = true
                                totalColumns.each { alias ->
                                    def tmp = (row.getCell(alias).value ?: 0) + (sRow.getCell(alias).value ?: 0)
                                    row.getCell(alias).setValue(tmp, null)
                                }
                                break
                            }
                        }
                        // если совпадений нет, то просто добавить строку
                        if (!isFind) {
                            dataRows.add(sRow)
                        }
                    }
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
}

// TODO (Ramil Timerbaev)
// Получение импортируемых данных
void importData2() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Код налогового учета',
            (xml.row[0].cell[3]): 'Балансовый счёт',
            (xml.row[0].cell[5]): 'Сумма дохода за отчётный квартал',
            (xml.row[1].cell[3]): 'Номер',
            (xml.row[1].cell[4]): 'Наименование счёта',
            (xml.row[2].cell[0]): '1',
            (xml.row[2].cell[2]): '2',
            (xml.row[2].cell[3]): '3',
            (xml.row[2].cell[4]): '4',
            (xml.row[2].cell[5]): '5'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def newRow = formData.createStoreMessagingDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 3
        newRow.balance = getRecordIdImport(28, 'CODE', row.cell[2].text(), xlsIndexRow, 2 + colOffset)
        def map = getRefBookValue(28, newRow.balance)

        // графа 2
        if (map != null) {
            formDataService.checkReferenceValue(28, row.cell[3].text(), map.NUMBER?.stringValue, xlsIndexRow, 3 + colOffset, logger, true)
        }

        // графа 4
        if (map != null) {
            def String text = row.cell[4].text().replaceAll("  ", " ")
            def String text2 = map.TYPE_INCOME?.stringValue
            text2 = text2.replaceAll("  ", " ")
            formDataService.checkReferenceValue(28, text, text2, xlsIndexRow, 4 + colOffset, logger, true)
        }

        // графа 5
        newRow.sum = parseNumber(row.cell[5].text(), xlsIndexRow, 5 + colOffset, logger, true)
        rows.add(newRow)
    }
    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        dataRowHelper.save(rows)
    }
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), dataRows.find { it.getAlias() == 'total' }, true)
    dataRowHelper.saveSort()
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias())}
}

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 5
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2, ..)
    int rowIndex = 0        // номер строки в НФ (1, 2, ..)
    def totalTF = null      // итоговая строка со значениями из тф для добавления
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
        if (!isEmptyCells(rowCells)) {
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
            if (newRow) {
                newRows.add(newRow)
            }
        }
    } finally {
        reader.close()
    }

    // итоговая строка
    def totalRow = calcTotalRow(newRows)
    newRows.add(totalRow)

    showMessages(newRows, logger)

    // сравнение итогов
    if (totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = [ 'sum' : 5 ]

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).save(newRows)
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
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def int colOffset = 1
    def int colIndex

    // графа 2
    colIndex = 2
    newRow.balance = getRecordIdImport(28, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // графа 5
    colIndex = 5
    newRow.sum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

void importData() {
    int COLUMN_COUNT = 5
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = '№ пп'
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
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
        if (!rowValues[INDEX_FOR_SKIP]) {
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).save(rows)
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    if (headerRows.isEmpty()) {
        logger.error("Заголовок таблицы не соответствует требуемой структуре.")
        return
    }
    checkHeaderSize(headerRows[0].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            (headerRows[0][0]): '№ пп',
            (headerRows[0][2]): 'Код налогового учета',
            (headerRows[0][3]): 'Балансовый счёт',
            (headerRows[0][5]): 'Сумма дохода за отчётный квартал',
            (headerRows[1][3]): 'Номер',
            (headerRows[1][4]): 'Наименование счёта',
            (headerRows[2][0]): '1',
            (headerRows[2][2]): '2',
            (headerRows[2][3]): '3',
            (headerRows[2][4]): '4',
            (headerRows[2][5]): '5'
    ]
    checkHeaderEquals(headerMapping)
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
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    // графа 3
    def colIndex = 2
    newRow.balance = getRecordIdImport(28, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    def map = getRefBookValue(28, newRow.balance)

    // графа 2
    if (map != null) {
        colIndex = 3
        formDataService.checkReferenceValue(28, values[colIndex], map.NUMBER?.stringValue, fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 4
    if (map != null) {
        colIndex = 4
        def String text = values[colIndex].replaceAll("  ", " ")
        def String text2 = map.TYPE_INCOME?.stringValue
        text2 = text2.replaceAll("  ", " ")
        formDataService.checkReferenceValue(28, text, text2, fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 5
    colIndex = 5
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}