package form_template.deal.app_6_3.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Предоставление нежилых помещений в аренду
 *
 * formTemplateId=812
 *
 * @author Emamedova
 */
// графа    ( )   - fix
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
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
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
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
def nonEmptyColumns = ['name', 'sum', 'docNumber', 'docDate', 'country', 'count', 'price', 'cost', 'dealDoneDate']

@Field
def totalColumns = ['sum', 'count', 'price', 'cost']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Дата окончания отчетного периода
@Field
def endDate = null



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
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение xml с общими проверками
def getXML(def String startStr, def String endStr) {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        throw new ServiceException('Имя файла не должно быть пустым')
    }
    def is = ImportInputStream
    if (is == null) {
        throw new ServiceException('Поток данных пуст')
    }
    if (!fileName.endsWith('.xls') && !fileName.endsWith('.xlsx') && !fileName.endsWith('.xlsm')) {
        throw new ServiceException('Выбранный файл не соответствует формату xls/xlsx/xlsm!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    return xml
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    String dateFormat = 'yyyy'
    def formYear = (String) reportPeriodService.get(formData.reportPeriodId).getTaxPeriod().getYear()

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNumber = row.getIndex()

        checkNonEmptyColumns(row, rowNumber, nonEmptyColumns, logger, false)

        // Проверка суммы доходов
        if (row.sum!=null && !row.sum.toString().isEmpty() && row.sum <= 0) {
            def income = row.getCell('sum').column.name
            logger.error("Строка $rowNumber: Значение графы «$income» должно быть больше «0»!")
        }

        // Проверка заполнения населенного пункта
        if ((row.city == null || row.city.toString().isEmpty()) && (row.settlement == null || row.settlement.toString().isEmpty())) {
            def settleName = row.getCell('settlement').column.name
            rowError(logger, row, "Строка $rowNumber: Графа «$settleName» не заполнена! Выполнение расчета невозможно!")
        }

        // Проверка количества
        if (row.count!=null && !row.count.toString().isEmpty() && row.count <= 0) {
            def countName = row.getCell('count').column.name
            rowError(logger, row, "Строка $rowNumber: Значение графы «$countName» должно быть больше «0»!")
        }

        // Проверка цены
        if (row.sum != null && !row.sum.toString().isEmpty() && row.count > 0) {
            if(row.price != (row.sum/row.count)){
                def income = row.getCell('sum').column.name
                def countName = row.getCell('count').column.name
                def priceName = row.getCell('price').column.name
                rowError(logger, row, "Строка $rowNumber: Значение графы  «$priceName», должно быть равно отношению графы «$income» к графе «$countName»! Выполнение расчета невозможно!")
            }
        }

        // Проверка стоимости по графе 5
        if (row.sum != null && !row.sum.toString().isEmpty() && row.cost != row.sum) {
            def income = row.getCell('sum').column.name
            def costName = row.getCell('cost').column.name
            rowError(logger, row, "Строка $rowNumber: Значение графы «$costName» должно быть равно значению графы «$income»!")
        }

        // Корректность даты совершения сделки относительно даты договора
        if (row.dealDoneDate < row.docDate) {
            def dealDoneDateName = row.getCell('dealDoneDate').column.name
            def docDateName = row.getCell('docDate').column.name
            rowError(logger, row, "Строка $rowNumber: Значение графы «$dealDoneDateName» должно быть не меньше значения графы «$docDateName»!")
        }

        //Проверка года совершения сделки
        if (row.dealDoneDate) {
            def dealDoneDate = row.dealDoneDate.format(dateFormat)
            if (dealDoneDate != formYear) {
                def dealDoneDateName = row.getCell('dealDoneDate').column.name
                logger.error("Строка $rowNumber: Год, указанный по графе «$dealDoneDateName» ($dealDoneDate), должен относиться " +
                        "к календарному году текущей формы ($formYear)!")
            }
        }

        // Проверка диапазона дат
        if (row.docDate) {
            checkDateValid(logger, row, 'docDate', row.docDate, true)
        }
        if (row.dealDoneDate) {
            checkDateValid(logger, row, 'dealDoneDate', row.dealDoneDate, true)
        }
    }
    //Проверка итоговых значений по фиксированной строке «Итого»
    if (dataRows.find { it.getAlias() == 'total' }) {
        checkTotalSum(dataRows, totalColumns, logger, true)
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    // Удаление итогов
    deleteAllAliased(dataRows)

    for (row in dataRows) {
        // Расчет поля "Цена"
        row.price = calc1314(row)
        // Расчет поля "Стоимость"
        row.cost = row.sum
    }


    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)
}

def BigDecimal calc1314(def row) {
    if (row.sum != null && row.count != null && row.count != 0) {
        return (row.sum / row.count).setScale(0, RoundingMode.HALF_UP)
    }
    return null
}
// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = getSubTotalRow(i)
    // Расчеты подитоговых значений
    def rows = []
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        rows.add(dataRows.get(j))
    }
    calcTotalSum(rows, newRow, totalColumns)

    return newRow
}
/**
 * Получить подитоговую строку с заданными стилями.
 * @param i номер строки
 */
DataRow<Cell> getSubTotalRow(int i) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.fix = 'Итого'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 5
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 5
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}
// Получение импортируемых данных
void importData() {
    int INDEX_FOR_SKIP = 0
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 15
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = 'Общая информация'
    String TABLE_END_VALUE = null

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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def totalRowFromFile = null

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
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Итого") {
            totalRowFromFile = getNewTotalFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

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

    // сравнение итогов
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    if (totalRowFromFile) {
        compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)
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
            ([(headerRows[0][0]) : 'Общая информация']),
            ([(headerRows[0][5]) : 'Сведения о сделке']),
            ([(headerRows[1][1]) : getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][2]) : getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][3]) : getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][4]) : getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][5]) : getColumnName(tmpRow, 'sum')]),
            ([(headerRows[1][6]) : getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][7]) : getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][8]) : 'Адрес местонахождения объекта недвижимости']),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][14]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[1][15]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][8]) : getColumnName(tmpRow, 'country')]),
            ([(headerRows[2][9]) : getColumnName(tmpRow, 'region')]),
            ([(headerRows[2][10]): getColumnName(tmpRow, 'city')]),
            ([(headerRows[2][11] ): getColumnName(tmpRow, 'settlement')])
    ]
    (1..15).each {
        headerMapping.add(([(headerRows[3][it]): 'гр. ' + it.toString()]))
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
    def nameFromFile = values[2]

    def int colIndex = 2

    def recordId = getTcoRecordId(nameFromFile, values[3], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 2
    newRow.name = recordId
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(9, values[colIndex], map.INN_KIO?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY?.referenceValue)
        if (map != null) {
            formDataService.checkReferenceValue(10, values[colIndex], map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
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