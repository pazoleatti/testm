package form_template.deal.app_6_3.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * Предоставление нежилых помещений в аренду
 *
 * formTemplateId=812
 *
 * @author Emamedova
 */
// графа 1  (1)   - rowNumber       - № п/п
// графа 2  (2)   - name         - Полное наименование юридического лица с указанием ОПФ
// графа 3  (3)   - iksr          - ИНН/ КИО
// графа 4  (4)   - countryCode     - Код страны регистрации по классификатору ОКСМ
// графа 5  (5)   - sum   - Сумма доходов Банка по данным бухгалтерского учета, руб.
// графа 6  (6)   - docNumber     - Номер договора
// графа 7  (7)   - docDate    - Дата договора
// графа 8  (8)   - country   		- Страна (код)
// графа 9  (9)   - region     		- Регион (код)
// графа 10 (10)  - city        	- Город
// графа 11 (11)  - settlement      - Населенный пункт
// графа 12 (12)  - count         	- Количество
// графа 13 (13)  - price 			- Цена
// графа 14 (14)  - cost 			- Стоимость
// графа 15 (15)  - dealDoneDate - Дата совершения сделки
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
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, logger, userInfo)
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

@Field
def allColumns = ['name', 'iksr', 'countryCode', 'sum', 'docNumber', 'docDate', 'country',
                  'region', 'city', 'settlement', 'count', 'price', 'cost', 'dealDoneDate']
// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'sum', 'docNumber', 'docDate', 'country',
                       'region', 'city', 'settlement', 'count', 'dealDoneDate']
// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryCode', 'price', 'cost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'sum', 'docDate', 'country', 'count', 'price', 'cost', 'dealDoneDate']

@Field
def totalColumns = ['sum', 'price', 'cost']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
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
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), -1, null, logger, true)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    def country643 = getRecordId(10, 'CODE', '643')

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка суммы доходов
        if (row.sum != null && row.sum < 0) {
            def income = row.getCell('sum').column.name
            logger.error("Строка $rowNum: Значение графы «$income» должно быть больше или равно «0»!")
        }

        // Проверка корректности даты договора
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // Проверка заполнения кода региона
        if (row.country == country643 && !row.region) {
            def msg = row.getCell('region').column.name
            logger.error("Строка $rowNum: Графа «$msg» должна быть заполнена, т.к. указанная страна местонахождения объекта недвижимости Россия!")
        }
        if (row.country && row.country != country643 && row.region) {
            def msg = row.getCell('region').column.name
            logger.error("Строка $rowNum: Графа «$msg» не должна быть заполнена, т.к. указанная страна местонахождения объекта недвижимости не Россия!")
        }

        // Проверка заполнения населенного пункта
        if (!row.city && !row.settlement) {
            def msg1 = row.getCell('city').column.name
            def msg2 = row.getCell('settlement').column.name
            logger.error("Строка $rowNum: Должна быть заполнена графа «$msg1» или «$msg2»!")
        }

        // Проверка количества
        if (row.count != null && row.count <= 0) {
            def countName = row.getCell('count').column.name
            logger.error("Строка $rowNum: Значение графы «$countName» должно быть больше «0»!")
        }

        // Проверка цены
        if (row.sum != null && row.count > 0) {
            if (row.price != round((BigDecimal) (row.sum / row.count), 2)) {
                def income = row.getCell('sum').column.name
                def countName = row.getCell('count').column.name
                def priceName = row.getCell('price').column.name
                logger.error("Строка $rowNum: Значение графы  «$priceName», должно быть равно отношению графы «$income» к графе «$countName»! Выполнение расчета невозможно!")
            }
        }

        // Проверка стоимости по графе 5
        if (row.sum != null && row.cost != row.sum) {
            def income = row.getCell('sum').column.name
            def costName = row.getCell('cost').column.name
            logger.error("Строка $rowNum: Значение графы «$costName» должно быть равно значению графы «$income»!")
        }

        // Проверка корректности даты совершения сделки
        // TODO (SBRFACCTAX-15094) заменить на checkDatePeriodExt
        checkDatePeriodExtLocal(logger, row, 'dealDoneDate', 'docDate', Date.parse('dd.MM.yyyy', '01.01.' + getReportPeriodEndDate().format('yyyy')), getReportPeriodEndDate(), true)
    }
}

// TODO (SBRFACCTAX-15094) удалить
void checkDatePeriodExtLocal(logger, row, String alias, String startAlias, Date yearStartDate, Date endDate, boolean fatal) {
    // дата проверяемой графы
    Date docDate = row.getCell(alias).getDateValue();
    // дата другой графы
    Date startDate = row.getCell(startAlias).getDateValue();

    if (docDate != null && startDate != null && (docDate.before(yearStartDate) || docDate.after(endDate) || docDate.before(startDate))) {
        logger.error(String.format("Строка %d: Дата по графе «%s» должна принимать значение из диапазона %s - %s и быть больше либо равна дате по графе «%s»!",
                row.getIndex(),
                getColumnName(row, alias),
                formatDate(yearStartDate, "dd.MM.yyyy"),
                formatDate(endDate, "dd.MM.yyyy"),
                getColumnName(row, startAlias)
        ));
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        // Расчет поля "Цена"
        row.price = calcNum(row)
        // Расчет поля "Стоимость"
        row.cost = row.sum
    }
}

def BigDecimal calcNum(def row) {
    if (row.sum != null && row.count != null && row.count != 0) {
        return round(row.sum / row.count, 2)
    }
    return null
}

// Получение импортируемых данных
void importData() {
    int INDEX_FOR_SKIP = 0
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 14
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = 'Общая информация'
    String TABLE_END_VALUE = null

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

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues || rowValues.isEmpty() || !rowValues.find { it }) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        rowIndex++
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
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

    def headerMapping = [
            ([(headerRows[0][0]): 'Общая информация']),
            ([(headerRows[0][4]): 'Сведения о сделке']),
            ([(headerRows[1][0]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][1]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][2]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][3]): getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][4]): getColumnName(tmpRow, 'sum')]),
            ([(headerRows[1][5]): getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][6]): getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][7]): 'Адрес местонахождения объекта недвижимости']),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[1][14]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][7]): getColumnName(tmpRow, 'country')]),
            ([(headerRows[2][8]): getColumnName(tmpRow, 'region')]),
            ([(headerRows[2][9]): getColumnName(tmpRow, 'city')]),
            ([(headerRows[2][10]): getColumnName(tmpRow, 'settlement')])
    ]
    (0..14).each {
        headerMapping.add(([(headerRows[3][it]): 'гр. ' + (it+1)]))
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
    def String iksrName = getColumnName(newRow, 'iksr')
    def nameFromFile = values[1]

    def int colIndex = 1

    def recordId = getTcoRecordId(nameFromFile, values[2], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 2
    newRow.name = recordId
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4
    if (map != null) {
        def countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            formDataService.checkReferenceValue(values[colIndex], [countryMap.CODE?.stringValue], getColumnName(newRow, 'countryCode'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 5
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 6
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 7
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 8
    newRow.country = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 9
    newRow.region = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 10
    newRow.city = values[colIndex]
    colIndex++

    // графа 11
    newRow.settlement = values[colIndex]
    colIndex++

    // графа 12
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 14
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 15
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
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

    int COLUMN_COUNT = 15
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2, ..)
    int rowIndex = 0        // номер строки в НФ (1, 2, ..)
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
                break
            }
            def newRow = getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex, false)
            if (newRow) {
                newRows.add(newRow)
            }
        }
    } finally {
        reader.close()
    }

    // отображать ошибки переполнения разряда
    showMessages(newRows, logger)

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
        def String iksrName = getColumnName(newRow, 'iksr')
        def nameFromFile = pure(rowCells[2])
        def recordId = getTcoRecordId(nameFromFile, pure(rowCells[3]), iksrName, fileRowIndex, 2, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)

        // графа 2
        newRow.name = recordId

        colIndex = 6
        // графа 6
        newRow.docNumber = pure(rowCells[colIndex])
        colIndex++
        // графа 7
        newRow.docDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
        // графа 8
        newRow.country = getRecordIdImport(10L, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
        colIndex++
        // графа 9
        newRow.region = getRecordIdImport(4L, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
        colIndex++
        // графа 10
        newRow.city = pure(rowCells[colIndex])
        colIndex++
        // графа 11
        newRow.settlement = pure(rowCells[colIndex])
        colIndex++
        // графа 15
        newRow.dealDoneDate = parseDate(pure(rowCells[15]), "dd.MM.yyyy", fileRowIndex, 15 + colOffset, logger, true)
    }
    // графа 5
    newRow.sum = parseNumber(pure(rowCells[5]), fileRowIndex, 5 + colOffset, logger, true)
    // графа 12
    newRow.count = parseNumber(pure(rowCells[12]), fileRowIndex, 12 + colOffset, logger, true)
    // графа 13
    newRow.price = parseNumber(pure(rowCells[13]), fileRowIndex, 13 + colOffset, logger, true)
    // графа 14
    newRow.cost = parseNumber(pure(rowCells[14]), fileRowIndex, 14 + colOffset, logger, true)

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}