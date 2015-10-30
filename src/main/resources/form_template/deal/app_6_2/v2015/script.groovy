package form_template.deal.app_6_2.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 804 - 6.2. Размещение средств на межбанковском рынке
 *
 * formTemplateId=804
 *
 * @author Stanislav Yasinskiy
 */

// rowNumber    - № п/п
// name         - Полное наименование юридического лица с указанием ОПФ
// iksr         - ИНН/КИО
// countryName  - Наименование страны регистрации
// countryCode  - Код страны регистрации по классификатору ОКСМ
// docNumber    - Номер договора
// docDate      - Дата договора
// dealNumber   - Номер сделки
// dealDate     - Дата заключения сделки
// count        - Количество
// sum          - Сумма доходов Банка по данным бухгалтерского учета, руб.
// price        - Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
// cost        - Итого стоимость без учета НДС, акцизов и пошлин, руб.
// dealDoneDate - Дата совершения сделки

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
def editableColumns = ['name', 'dealNumber', 'dealDate', 'sum', 'docNumber', 'docDate', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'countryCode', 'count', 'price', 'cost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'count', 'sum', 'dealDoneDate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

//// Обертки методов

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
        def rowNum = row.getIndex()

        // 1. Проверка на заполнение граф
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка возможности заполнения цены и стоимости
        if (!row.sum) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('cost').column.name
            def msg3 = row.getCell('sum').column.name
            logger.error("Строка $rowNum: Графы «$msg1», «$msg2»: выполнение расчета невозможно, так как не заполнена " +
                    "используемая в расчете графа «$msg3»!")
        }

        // 3. Проверка цены
        if (row.sum && row.price != row.sum) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('sum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }

        // 4. Проверка стоимости
        if (row.sum && row.cost != row.sum) {
            def msg1 = row.getCell('cost').column.name
            def msg2 = row.getCell('sum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }

        // 5. Проверка количества
        if (row.count && row.count != 1) {
            def msg = row.getCell('count').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть равно значению «1»!")
        }

        // 6. Корректность даты заключения сделки относительно даты договора
        if (row.docDate && row.dealDate && row.docDate > row.dealDate) {
            def msg1 = row.getCell('dealDate').column.name
            def msg2 = row.getCell('docDate').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // 7. Корректность даты совершения сделки относительно даты заключения сделки
        if (row.dealDate && row.dealDoneDate && row.dealDate > row.dealDoneDate) {
            def msg1 = row.getCell('dealDoneDate').column.name
            def msg2 = row.getCell('dealDate').column.name
            // TODO фатальность уточняется у заказчика
            logger.warn("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // 8. Проверка года совершения сделки
        if (row.dealDoneDate) {
            def dealDoneYear = row.dealDoneDate.format(dateFormat)
            if (dealDoneYear != formYear) {
                def msg = row.getCell('dealDoneDate').column.name
                logger.error("Строка $rowNum: Год, указанный по графе «$msg» ($dealDoneYear), должен относиться " +
                        "к календарному году текущей формы ($formYear)!")
            }
        }

        // 9. Проверка диапазона дат
        if (row.docDate) {
            checkDateValid(logger, row, 'docDate', row.docDate, true)
        }

        // TODO необходимость ЛП 10 уточняется у заказчика
        //if (row.sum && row.sum < 0) {
        //    def msg = row.getCell('sum').column.name
        //    logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше значения «0»!")
        //}
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        // Расчет поля " Количество"
        row.count = 1
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Итого"
        row.cost = row.sum
    }
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 14
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Общие сведения о контрагенте - юридическом лице'
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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

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
            ([(headerRows[0][0]): 'Общие сведения о контрагенте - юридическом лице']),
            ([(headerRows[0][5]): 'Сведения о сделке']),
            ([(headerRows[1][0]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][1]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][2]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][3]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[1][4]): getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][5]): getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][6]): getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][7]): getColumnName(tmpRow, 'dealNumber')]),
            ([(headerRows[1][8]): getColumnName(tmpRow, 'dealDate')]),
            ([(headerRows[1][9]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'sum')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][0]): 'гр. 1']),
            ([(headerRows[2][1]): 'гр. 2']),
            ([(headerRows[2][2]): 'гр. 3']),
            ([(headerRows[2][3]): 'гр. 4.1']),
            ([(headerRows[2][4]): 'гр. 4.2'])
    ]
    (5..12).each {
        headerMapping.add(([(headerRows[2][it]): 'гр. ' + it]))
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

    def int colIndex = 1

    // TODO уточнить id и алиас в справочнике SBRFACCTAX-12861
    // графа 2
    newRow.name = getRecordIdImport(520, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    def map = getRefBookValue(520, newRow.name)
    colIndex++

    // TODO уточнить id и алиас в справочнике SBRFACCTAX-12861
    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4.1
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY?.referenceValue)
        if (map != null) {
            formDataService.checkReferenceValue(10, values[colIndex], map.NAME?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 4.2
    if (map != null) {
        formDataService.checkReferenceValue(10, values[colIndex], map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 5
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 6
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.dealNumber = values[colIndex]
    colIndex++

    // графа 8
    newRow.dealDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
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