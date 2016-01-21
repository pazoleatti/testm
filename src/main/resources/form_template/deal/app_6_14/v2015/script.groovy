package form_template.deal.app_6_14.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 6.14. Беспоставочные срочные сделки
 *
 * formTemplateId = 835
 *
 * @author - Emamedova
 */
// графа    - fix
// графа 1  - rowNumber         - № п/п
// графа 2  - name              - Полное наименование юридического лица с указанием ОПФ
// графа 3  - iksr              - ИНН/ КИО
// графа 4  - countryName       - Наименование страны регистрации
// графа 5  - countryCode    - Код страны регистрации по классификатору ОКСМ
// графа 6  - docNumber         - Номер договора
// графа 7  - docDate           - Дата договора
// графа 8  - dealNumber        - Номер сделки
// графа 9  - dealDate          - Дата заключения сделки
// графа 10 - dealType          - Вид срочной сделки
// графа 11 - income            - Сумма доходов Банка по данным бухгалтерского учета, руб.
// графа 12 - outcome           - Сумма расходов Банка по данным бухгалтерского учета, руб.
// графа 13 - price             - Цена
// графа 14 - cost              - Стоимость
// графа 15 - dealDoneDate      - Дата совершения сделки

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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
def allColumns = ['fix', 'rowNumber', 'name', 'iksr', 'countryName', 'countryCode', 'docNumber', 'docDate', 'dealNumber',
                  'dealDate', 'dealType', 'income', 'outcome', 'price', 'cost', 'dealDoneDate']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'docNumber', 'docDate', 'dealDate', 'dealNumber', 'dealType', 'income', 'outcome', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'countryCode', 'price', 'cost']

//Непустые атрибуты
@Field
def nonEmptyColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealType', 'price', 'cost', 'dealDoneDate']

// Группируемые атрибуты (графа 2, 6, 7, 10, 11)
@Field
def groupColumns = ['name', 'docNumber', 'docDate', 'dealType']

@Field
def totalColumns = ['income', 'outcome', 'cost']

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

//// Обертки методов

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

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // Проверка на заполнение графы
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка корректности даты договора
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // Проверка корректности даты заключения сделки
        if (row.docDate && row.dealDate && (row.docDate > row.dealDate || row.dealDate > getReportPeriodEndDate())) {
            def msg1 = row.getCell('dealDate').column.name
            def msg2 = row.getCell('docDate').column.name
            def msg3 = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2» и не больше $msg3!")
        }

        // Проверка на заполнение доходов и расходов
        boolean noOne = (row.income == null && row.outcome == null)
        boolean both = (row.income != null && row.outcome != null)
        if (noOne) {
            String msg1 = getColumnName(row, 'outcome')
            String msg2 = getColumnName(row, 'income')
            logger.error("Строка $rowNum: Должна быть заполнена хотя бы одна из граф «$msg1», «$msg2»!")
        }

        // Проверка положительной суммы дохода/расхода
        if (!noOne && !both) {
            sum = (row.income != null) ? row.income : row.outcome
            if (sum < 0) {
                msg = (row.income != null) ? getColumnName(row, 'income') : getColumnName(row, 'outcome')
                logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
            }
        }
        if (both) {
            if (row.income < 0 || row.outcome < 0 || (row.outcome <= 0 && row.outcome <= 0)) {
                String msg1 = getColumnName(row, 'outcome')
                String msg2 = getColumnName(row, 'income')
                logger.error("Строка $rowNum: Значение обеих граф «$msg1», «$msg2» должно быть неотрицательным, значение хотя бы одной из данных граф должно быть строго больше «0»!")
            }
        }

        // Проверка цены и стоимости
        if (!noOne && !both) {
            boolean comparePrice = row.price && row.price != sum
            boolean compareCost = row.cost && row.cost != sum
            msg = (row.income != null) ? getColumnName(row, 'income') : getColumnName(row, 'outcome')
            String msg1 = getColumnName(row, 'price')
            String msg2 = getColumnName(row, 'cost')
            if (comparePrice) {
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg»!")
            }
            if (compareCost) {
                logger.error("Строка $rowNum: Значение графы «$msg2» должно быть равно значению графы «$msg»!")
            }
        }
        if (both) {
            absSum = (row.income - row.outcome).abs()
            boolean comparePrice = row.price && row.price != absSum
            boolean compareCost = row.cost && row.cost != absSum
            String msg1 = getColumnName(row, 'price')
            String msg2 = getColumnName(row, 'cost')
            String msg3 = getColumnName(row, 'income')
            String msg4 = getColumnName(row, 'outcome')
            if (comparePrice) {
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно модулю разности значений граф «$msg3» и «$msg4»!")
            }
            if (compareCost) {
                logger.error("Строка $rowNum: Значение графы «$msg2» должно быть равно модулю разности значений граф «$msg3» и «$msg4»!")
            }
        }

        // Проверка корректности даты совершения сделки
        if (row.dealDate && row.dealDoneDate && (row.dealDate > row.dealDoneDate || row.dealDoneDate > getReportPeriodEndDate())) {
            def msg1 = row.getCell('dealDoneDate').column.name
            def msg2 = row.getCell('dealDate').column.name
            def msg3 = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2» и не больше $msg3!")
        }
    }

    // Проверка наличия всех фиксированных строк «Подитог»
    // Проверка отсутствия лишних фиксированных строк «Подитог»
    // Проверка итоговых значений по фиксированной строке «Подитог»
    checkItog(dataRows)

    // Проверка итоговых значений пофиксированной строке «Итого»
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
    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Сортировка
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { groupColumns.contains(it.getAlias())})
    sortRows(dataRows, groupColumns)

    for (row in dataRows) {
        // Расчет поля "Цена", "Стоимость"
        if(row.income != null && row.outcome == null){
            row.price = row.income
            row.cost = row.income
        }
        else if(row.outcome != null && row.income == null){
            row.price = row.outcome
            row.cost = row.outcome

        } else if(row.outcome != null && row.income != null){
            row.price = (row.income - row.outcome).abs()
            row.cost = (row.income - row.outcome).abs()
        }
    }

    // Добавление подитогов
    addAllAliased(dataRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcGroupTotal(i, dataRows)
        }
    }, groupColumns)

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    updateIndexes(dataRows)
}

// Расчет подитогового значения
DataRow<Cell> calcGroupTotal(def int i, def List<DataRow<Cell>> dataRows) {
    def tmpRow = dataRows.get(i)
    def str = getValuesByGroupColumn(tmpRow)
    def newRow = getSubTotalRow(i, str.toLowerCase().hashCode())

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
 */
DataRow<Cell> getSubTotalRow(int i, def key) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.fix = 'Подитог'
    newRow.setAlias('itg' + key.toString() + '#' + i)
    newRow.getCell('fix').colSpan = 6
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 6
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 16
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Общие сведения о контрагенте - юридическом лице'
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
    def totalRowFromFileMap = [:] // мапа для хранения строк подитогов со значениями из файла (стили простых строк)

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
        } else if (rowValues[INDEX_FOR_SKIP].contains('Подитог')) {
            //для расчета уникального среди групп(groupColumns) ключа берем строку перед Подитоговой
            def tmpRowValue = rows.get(rows.size() - 1)
            def str = getValuesByGroupColumn(tmpRowValue)
            def subTotalRow = getNewSubTotalRowFromXls(str.toLowerCase().hashCode(), rowValues, colOffset, fileRowIndex, rowIndex)
            def key = subTotalRow.getAlias().split('#')[0]
            if (totalRowFromFileMap[key] == null) {
                totalRowFromFileMap[key] = []
            }
            totalRowFromFileMap[key].add(subTotalRow)
            rows.add(subTotalRow)

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

    // сравнение подитогов
    if (!totalRowFromFileMap.isEmpty()) {
        // получить посчитанные подитоги
        def tmpRows = calcGroupRows(rows)
        def tmpSubTotalRows = tmpRows.findAll { it.getAlias() }
        tmpSubTotalRows.each { subTotalRow ->
            def key = subTotalRow.getAlias().split('#')[0]
            def totalRows = totalRowFromFileMap[key]
            if (totalRows) {
                totalRows.each { totalRow ->
                    compareTotalValues(totalRow, subTotalRow, totalColumns, logger, false)
                }
                totalRowFromFileMap.remove(key)
            } else {
                def row = tmpRows[Integer.valueOf(subTotalRow.getAlias().split('#')[1])]
                rowWarning(logger, null, String.format(GROUP_WRONG_ITOG, getValuesByGroupColumn(row)))
            }
        }
        if (!totalRowFromFileMap.isEmpty()) {
            // для этих подитогов из файла нет групп
            totalRowFromFileMap.each { key, totalRows ->
                totalRows.each { totalRow ->
                    rowWarning(logger, totalRow, String.format(GROUP_WRONG_ITOG_ROW, totalRow.getIndex()))
                }
            }
        }
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
            ([(headerRows[0][0]) : 'Общие сведения о контрагенте - юридическом лице']),
            ([(headerRows[0][6]) : 'Сведения о сделке']),
            ([(headerRows[1][1]) : getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][2]) : getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][3]) : getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][4]) : getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[1][5]) : getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][6]) : getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][7]) : getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][8]) : getColumnName(tmpRow, 'dealNumber')]),
            ([(headerRows[1][9]) : getColumnName(tmpRow, 'dealDate')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'dealType')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'income')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'outcome')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][14]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[1][15]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][1]) : 'гр. 1']),
            ([(headerRows[2][2]) : 'гр. 2']),
            ([(headerRows[2][3]) : 'гр. 3']),
            ([(headerRows[2][4]) : 'гр. 4.1']),
            ([(headerRows[2][5]) : 'гр. 4.2'])
    ]
    (6..15).each {
        headerMapping.add(([(headerRows[2][it]): 'гр. '+(it - 1).toString()]))
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
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    def countryMap
    // графа 4.1
    if (map != null) {
        countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            def expectedValues = [countryMap.NAME?.stringValue, countryMap.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'countryName'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 4.2                               `
    if (countryMap != null) {
        formDataService.checkReferenceValue(values[colIndex], [countryMap.CODE?.stringValue], getColumnName(newRow, 'countryCode'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
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
    newRow.dealType = getRecordIdImport(91, 'KIND', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 10
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 14
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

/**
 * Получить итоговую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewTotalFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 11
    def colIndex = 11
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 12
    colIndex = 12
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 14
    colIndex = 14
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = dataRows.find { it.getAlias() == 'total'}
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), totalRow, true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

/**
 * Получить новую подитоговую строку нф по значениям из экселя.
 *
 * @param key ключ для сравнения подитоговых строк при импорте
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewSubTotalRowFromXls(def key, def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getSubTotalRow(rowIndex, key)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 11
    def colIndex = 11
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 12
    colIndex = 12
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 14
    colIndex = 14
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
}

// Получить посчитанные подитоговые строки вместе со строками групп
def calcGroupRows(def dataRows) {
    def tmpRows = dataRows.findAll { !it.getAlias() }
    // Добавление подитогов
    addAllAliased(tmpRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcGroupTotal(i, rows)
        }
    }, groupColumns)

    return tmpRows
}

// Проверки подитоговых сумм
void checkItog(def dataRows) {
    // Рассчитанные строки итогов
    def testItogRows = calcGroupRows(dataRows).findAll { it.getAlias() }
    // Имеющиеся строки итогов
    def itogRows = dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
    // все строки, кроме общего итога
    def groupRows = dataRows.findAll { !'total'.equals(it.getAlias()) }
    checkItogRows(groupRows, testItogRows, itogRows, groupColumns, logger, new GroupString() {
        @Override
        String getString(DataRow<Cell> row) {
            return getValuesByGroupColumn(row)
        }
    }, new CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            for (def column : totalColumns) {
                if (row1[column] != row2[column]) {
                    return getColumnName(row1, column)
                }
            }
            return null
        }
    })
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def values = []
    // графа 2
    if (row?.name) {
        def map = getRefBookValue(520, row.name)
        if (map != null) {
            values.add(map.NAME?.stringValue)
        }
    }
    if (values.isEmpty()) {
        values.add('графа 2 не задана')
    }

    // графа 6
    if (row?.docNumber) {
        values.add(row.docNumber)
    } else {
        values.add('графа 5 не задана')
    }

    // графа 7
    if (row?.docDate) {
        values.add(row.docDate?.format('dd.MM.yyyy'))
    } else {
        values.add('графа 6 не задана')
    }

    // графа 10
    def value = null
    if (row?.dealType) {
        def map = getRefBookValue(91, row.dealType)
        if (map != null) {
            value = map.KIND?.stringValue
        }
    }
    if (!value) {
        value = 'графа 9 не задана'
    }
    values.add(value)

    return values.join('; ')
}