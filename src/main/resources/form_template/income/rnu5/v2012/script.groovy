package form_template.income.rnu5.v2012

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * (РНУ-5) Простой регистр налогового учёта «расходы»
 * formTypeId=317
 *
 * @author Stanislav Yasinskiy
 */

// графа 1 - rowNumber
// графа   - fix
// графа 2 - code       - зависимая от графы 3
// графа 3 - number     - справоочная
// графа 4 - name       - зависимая от графы 3
// графа 5 - sum

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
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
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
def editableColumns = ['number', 'sum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'sum']

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
        dataRowHelper.save(dataRows.sort { getKnu(it.number) })
    }

    // посчитать "итого по коду"
    def totalRows = [:]
    def code = null
    def sum = 0
    dataRows.eachWithIndex { row, i ->
        if (code == null) {
            code = getKnu(row.number)
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (code != getKnu(row.number)) {
            totalRows.put(i, getNewRow(code, sum))
            sum = 0
            code = getKnu(row.number)
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == dataRows.size() - 1) {
            sum += (row.sum ?: 0)
            def totalRowCode = getNewRow(code, sum)
            totalRows.put(i + 1, totalRowCode)
            sum = 0
        }
        sum += (row.sum ?: 0)
    }

    // добавить "итого по коду" в таблицу
    def i = 1
    totalRows.each { index, row ->
        dataRowHelper.insert(row, index + i++)
    }

    dataRows.add(calcTotalRow(dataRows))
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
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 4. Арифметическая проверка итоговых значений по каждому <Коду классификации расходов>
        def code = getKnu(row.number)
        if (sumRowsByCode[code] != null) {
            sumRowsByCode[code] += row.sum ?: 0
        } else {
            sumRowsByCode[code] = row.sum ?: 0
        }
    }

    //4. Арифметическая проверка итоговых значений по каждому <Коду классификации расходов>
    totalRows.each { key, val ->
        if (totalRows.get(key) != sumRowsByCode.get(key)) {
            def msg =  formData.createDataRow().getCell('sum').column.name
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
    return getRefBookValue(27, code)?.CODE?.stringValue
}

/**
 * Консолидация.
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).allCached.each { sRow ->
                    if (sRow.getAlias() == null || sRow.getAlias() == '') {
                        def isFind = false
                        // строки приемника - искать совпадения, если совпадения есть, то суммировать графу 5
                        for (def row : dataRows) {
                            if (sRow.number == row.number) {
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

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Код налогового учёта',
            (xml.row[0].cell[3]): 'Балансовый счёт',
            (xml.row[0].cell[5]): 'Сумма расхода за отчётный квартал',
            (xml.row[1].cell[3]): 'Номер',
            (xml.row[1].cell[4]): 'Наименование',
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
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

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

        if (row.cell[1].text() != null && row.cell[1].text() != '') {
            continue
        }

        def newRow = formData.createStoreMessagingDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 3
        String filter = "LOWER(CODE) = LOWER('" + row.cell[2].text() + "') and LOWER(NUMBER) = LOWER('" + row.cell[3].text() + "')"
        def records = refBookFactory.getDataProvider(27).getRecords(getReportPeriodEndDate(), null, filter, null)
        if (checkImportRecordsCount(records, refBookFactory.get(27), 'CODE', row.cell[2].text(), getReportPeriodEndDate(), xlsIndexRow, 2, logger, true)) {
            // графа 2
            newRow.number = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
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
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), getDataRow(dataRows, 'total'), true)
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
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex, true)
                }
                break
            }
            newRows.add(getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex, false))
        }
    } finally {
        reader.close()
    }

    // итоговая строка
    def totalRow = calcTotalRow(newRows)
    newRows.add(totalRow)

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

/** Добавляет строку в текущий буфер строк. */
boolean addRow(def rows, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells == null) {
        return true
    }
    def newRow = getNewRow(rowCells, columnCount, fileRowIndex, rowIndex, false)
    if (newRow == null) {
        return false
    }
    rows.add(newRow)
    return true
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 * @param isTotal признак итоговой строки
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex, def isTotal) {
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

    if (!isTotal) {
        String filter = "LOWER(CODE) = LOWER('" + pure(rowCells[2]) + "') and LOWER(NUMBER) = LOWER('" + pure(rowCells[3]) + "')"
        def records = refBookFactory.getDataProvider(27).getRecords(getReportPeriodEndDate(), null, filter, null)
        if (checkImportRecordsCount(records, refBookFactory.get(27), 'CODE', pure(rowCells[2]), getReportPeriodEndDate(), fileRowIndex, 2 + colOffset, logger, false)) {
            // графа 2
            newRow.number = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        }
    }

    // графа 5
    newRow.sum = parseNumber(pure(rowCells[5]), fileRowIndex, 5 + colOffset, logger, true)

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}