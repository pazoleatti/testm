package form_template.income.rnu_171.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 843 - РНУ-171. Регистр налогового учёта уступки права требования после предусмотренного кредитным договором срока
 * погашения основного долга в отношении сделок уступки прав требования в пользу Взаимозависимых лиц и резидентов оффшорных зон
 *
 * formTemplateId=843
 *
 * @author Bulat Kinzyabulatov
 */

//    rowNumber	        1	№ пп
//    fix	            -	fix
//    name	            2	Наименование контрагента
//    iksr	            3	ИНН (его аналог)
//    dealNum	        4	Номер договора цессии
//    dealDate	        5	Дата договора цессии
//    cost	            6	Стоимость права требования (руб. коп.)
//    costReserve	    7	Стоимость права требования, списанного за счёт резервов (руб. коп.)
//    repaymentDate	    8	Дата погашения основного долга
//    concessionsDate	9	Дата уступки права требования
//    income	        10	Доход (выручка) от уступки права требования (руб. коп.)
//    finResult	        11	Финансовый результат уступки права требования (руб. коп.)
//    code	            12	Код налогового учета
//    marketPrice	    13	Рыночная цена прав требования для целей налогообложения (руб. коп.)
//    finResultTax	    14	Финансовый результат, рассчитанный исходя из рыночной цены для целей налогообложения (руб. коп.)
//    incomeCorrection	15	Корректировка финансового результата (руб. коп.)

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
def allColumns = ['rowNumber', 'fix', 'name', 'iksr', 'dealNum', 'dealDate', 'cost', 'costReserve', 'repaymentDate',
                  'concessionsDate', 'income', 'finResult', 'code', 'marketPrice', 'finResultTax', 'incomeCorrection']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'dealNum', 'dealDate', 'cost', 'costReserve', 'repaymentDate', 'concessionsDate',
                       'income', 'code', 'marketPrice']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'finResult', 'finResultTax', 'incomeCorrection']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'dealNum', 'dealDate', 'cost', 'costReserve', 'repaymentDate', 'concessionsDate', 'income',
                       'finResult', 'code', 'marketPrice', 'finResultTax', 'incomeCorrection']

// Группируемые атрибуты
@Field
def groupColumns = ['code']

@Field
def sortColumns = ['code', 'name', 'dealNum', 'dealDate']

@Field
def totalColumns = ['incomeCorrection']

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

        // 1. Проверка на заполнение граф
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка даты договора цессии
        checkDatePeriod(logger, row, 'dealDate', Date.parse("dd.MM.yyyy", "01.01.1991"), getReportPeriodEndDate(), true)

        // 3, 4. Проверка даты погашения и уступки
        ['repaymentDate', 'concessionsDate'].each { alias ->
            checkDatePeriod(logger, row, alias, getReportPeriodStartDate(), getReportPeriodEndDate(), true)
            if (row[alias] && row.dealDate && row[alias] < row.dealDate) {
                logger.error("Строка $rowNum: Значение графы «${getColumnName(row, alias)}» должно быть больше или равно значения графы «${getColumnName(row, 'dealDate')}»!")
            }
        }

        // 5. Проверка положительности суммы дохода
        if (row.income != null && row.income < 0) {
            logger.error("Строка $rowNum: Значение графы «${getColumnName(row, 'income')}» должно быть больше или равно «0»!")
        }

        // 6. Проверка финансовых результатов
        if (row.finResultTax != null && row.finResult != null &&
                row.finResultTax <= row.finResult && row.finResult >= 0) {
            logger.warn("Строка $rowNum: Графа «${getColumnName(row, 'incomeCorrection')}» заполнена значением «0», т.к. не выполнен порядок заполнения графы!")
        }

        // 7. Проверка корректности финансового результата уступки
        if (row.finResult != null && row.income != null && row.cost != null && row.costReserve != null &&
                row.finResult != (row.income - (row.cost - row.costReserve))) {
            logger.error("Строка $rowNum: Значение графы «${getColumnName(row, 'finResult')}» должно равняться выражению: «${getColumnName(row, 'income')}» - («${getColumnName(row, 'cost')}» - «${getColumnName(row, 'costReserve')}»)!")
        }

        // 8. Проверка кода налогового учета
         if (row.code != null && !['10345', '10355'].contains(row.code)) {
             logger.error("Строка $rowNum: Графа «${getColumnName(row, 'code')}» должна принимать значение из следующего списка: «10345» или «10355»!")
         }

        // 9. Проверка корректности финансового результата из рыночной цены
        if (row.finResultTax != null && row.marketPrice != null && row.cost != null && row.costReserve != null &&
                row.finResultTax != (row.marketPrice - (row.cost - row.costReserve))) {
            logger.error("Строка $rowNum: Значение графы «${getColumnName(row, 'finResultTax')}» должно равняться выражению: «${getColumnName(row, 'marketPrice')}» - («${getColumnName(row, 'cost')}» - «${getColumnName(row, 'costReserve')}»)!")
        }

        // 10. Проверка корректировки финансового результата
        // a. Если «Графа 11» больше или равно «0» И «Графа 14» больше «Графа 11», то «Графа 15» = «Графа 14» - «Графа 11»
        // b.	Если «Графа 11» меньше «0» И «Графа 14» больше «Графа 11», то «Графа 15» = |«Графа 13»| - |«Графа 11»|
        // c.	Если «Графа 11» больше или равно «0» И «Графа 14» меньше или равно «Графа 11», то «Графа 15» = «0»
        if (row.finResult != null && row.finResultTax != null) {
            if (row.finResult >= 0 && row.finResultTax > row.finResult &&
                    (row.incomeCorrection != row.finResultTax - row.finResult)) {
                logger.error("Строка $rowNum: Значение графы «${getColumnName(row, 'incomeCorrection')}» должно равняться разнице между графой «${getColumnName(row, 'finResultTax')}» и «${getColumnName(row, 'finResult')}»!")
            }
            if (row.finResult < 0 && row.finResultTax > row.finResult && row.marketPrice != null &&
                    (row.incomeCorrection != row.marketPrice.abs() - row.finResult.abs())) {
                logger.error("Строка $rowNum: Значение графы «${getColumnName(row, 'incomeCorrection')}» должно равняться разнице между графой «${getColumnName(row, 'marketPrice')}» по модулю и «${getColumnName(row, 'finResult')}» по модулю!")
            }
            if (row.finResult >= 0 && row.finResultTax <= row.finResult &&
                    row.incomeCorrection != BigDecimal.ZERO) {
                logger.error("Строка $rowNum: Значение графы «${getColumnName(row, 'incomeCorrection')}» должно быть равно нулю!")
            }
        }
    }

    //  Проверка наличия всех фиксированных строк
    //  Проверка отсутствия лишних фиксированных строк
    //  Проверка итоговых значений по фиксированным строкам
    checkItog(dataRows)

    // Проверка итоговых значений по фиксированной строке «Итого»
    if (dataRows.find { it.getAlias() == 'total' }) {
        checkTotalSum(dataRows, totalColumns, logger, true)
    }
}

// Проверки подитоговых сумм
void checkItog(def dataRows) {
    // Рассчитанные строки итогов
    def testItogRows = calcSubTotalRows(dataRows)
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

// Получить посчитанные подитоговые строки
def calcSubTotalRows(def dataRows) {
    def tmpRows = dataRows.findAll { !it.getAlias() }
    // Добавление подитогов
    addAllAliased(tmpRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)

    updateIndexes(tmpRows)
    return tmpRows.findAll { it.getAlias() }
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def value
    // графа 12
    if (row?.code) {
        value = row.code
    } else {
        value = 'графа 12 не задана'
    }
    return value
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    // Удаление подитогов
    deleteAllAliased(dataRows)

    for (row in dataRows) {
        if(row.getAlias() != null){
            continue
        }
        // «Графа 11» = «Графа 10» - («Графа 6» - «Графа 7»)
        if (row.income != null && row.cost != null && row.costReserve != null) {
            row.finResult = row.income - (row.cost - row.costReserve)
        }
        // «Графа 14» = «Графа 13» - («Графа 6» - «Графа 7»)
        if (row.marketPrice != null && row.cost != null && row.costReserve != null) {
            row.finResultTax = row.marketPrice - (row.cost - row.costReserve)
        }
        // ЕСЛИ «Графа 11» больше или равно «0» И «Графа 14» больше «Графа 11», ТО
        // «Графа 15» = «Графа 14» - «Графа 11»
        // ЕСЛИ «Графа 11» меньше «0» И «Графа 14» больше «Графа 11», ТО
        // «Графа 15» = |«Графа 13»| - |«Графа 11»|
        // ЕСЛИ «Графа 11» больше или равно «0» И «Графа 14» меньше или равно «Графа 11», ТО
        // «Графа 15» = «0»
        if (row.finResult != null && row.finResultTax != null) {
            if (row.finResult >= 0 && row.finResultTax > row.finResult) {
                row.incomeCorrection = row.finResultTax - row.finResult
            } else if (row.finResult < 0 && row.finResultTax > row.finResult) {
                if (row.marketPrice != null) {
                    row.incomeCorrection = row.marketPrice.abs() - row.finResult.abs()
                }
            } else if (row.finResult >= 0 && row.finResultTax <= row.finResult) {
                row.incomeCorrection = 0
            }
        }

    }

    // Сортировка
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { sortColumns.contains(it.getAlias())})
    sortRows(dataRows, sortColumns)

    // Добавление подитогов
    addAllAliased(dataRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, dataRows)
        }
    }, groupColumns)

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    updateIndexes(dataRows)
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
    newRow.code = dataRows.get(i).code

    return newRow
}

DataRow<Cell> getSubTotalRow(int i) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.fix = 'Итого:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Всего:'
    totalRow.getCell('fix').colSpan = 2
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 15
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = '№ пп'
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

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()
    def totalRowFromFile = null
    def totalRowFromFileMap = [:] // мапа для хранения строк подитогов со значениями из файла (стили простых строк)

    // формирование строк нф
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
        if (rowValues[INDEX_FOR_SKIP].contains("Всего")) {
            totalRowFromFile = getNewTotalFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (rowValues[INDEX_FOR_SKIP].contains('Итого')) {
            def subTotalRow = getNewSubTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            if (totalRowFromFileMap[subTotalRow.getIndex()] == null) {
                totalRowFromFileMap[subTotalRow.getIndex()] = []
            }
            totalRowFromFileMap[subTotalRow.getIndex()].add(subTotalRow)
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
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'dealNum')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'dealDate')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'costReserve')]),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'repaymentDate')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'concessionsDate')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'income')]),
            ([(headerRows[0][11]): getColumnName(tmpRow, 'finResult')]),
            ([(headerRows[0][12]): getColumnName(tmpRow, 'code')]),
            ([(headerRows[0][13]): getColumnName(tmpRow, 'marketPrice')]),
            ([(headerRows[0][14]): getColumnName(tmpRow, 'finResultTax')]),
            ([(headerRows[0][15]): getColumnName(tmpRow, 'incomeCorrection')]),
            ([(headerRows[1][0]): '1'])
    ]
    (2..15).each {
        headerMapping.add([(headerRows[1][it]): it.toString()])
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
    def int colIndex = 2

    def recordId = getTcoRecordId(values[2], values[3], getColumnName(newRow, 'iksr'), fileRowIndex, colIndex, getReportPeriodEndDate(), true, logger, refBookFactory, recordCache)
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
    newRow.dealNum = values[colIndex]
    colIndex++

    // графа 5
    newRow.dealDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 6
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.costReserve = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 8
    newRow.repaymentDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.concessionsDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.finResult = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.code = values[colIndex]
    colIndex++

    // графы 13-15
    ['marketPrice', 'finResultTax', 'incomeCorrection'].each{
        newRow[it]= parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { sortColumns.contains(it.getAlias())})
    sortRows(dataRows)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

// Сортируем
void sortRows(def dataRows) {
    def complexList = []
    def tmpRows = []
    def currentCode = null
    for (def row : dataRows) {
        // если (под)итоги, то закрываем группу
        if (row.getAlias() != null) {
            complexList.add(tmpRows)
            tmpRows = []
            continue
        }
        // в начале запоминаем код
        if (currentCode == null) {
            currentCode = row.code ?: ""
        }
        // при смене кода закрываем группу и создаем новую
        if (!(row.code ?: "").equalsIgnoreCase(currentCode)) {
            complexList.add(tmpRows)
            tmpRows = [row]
        } else {
            tmpRows.add(row)
        }
    }
    // последнюю группу заносим в список
    if (!tmpRows.isEmpty()) {
        complexList.add(tmpRows)
    }
    // проходим по группам и сортируем
    complexList.each { rows ->
        sortRows(rows, sortColumns)
    }
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

    // графа 15
    colIndex = 15
    newRow.incomeCorrection = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

/**
 * Получить новую подитоговую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewSubTotalRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getSubTotalRow(fileRowIndex)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 12
    def colIndex = 12
    newRow.code = values[colIndex]

    // графа 15
    colIndex = 15
    newRow.incomeCorrection = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    return newRow
}
