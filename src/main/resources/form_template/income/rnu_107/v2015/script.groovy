package form_template.income.rnu_107.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * РНУ 107. Регистр налогового учёта доходов, возникающих в связи с применением в сделках c Взаимозависимыми лицами и резидентами оффшорных зон тарифов, не соответствующих рыночному уровню
 *
 * formTemplateId=821
 *
 * @author Emamedova
 */

// fix
// rowNumber    		(1) -  № пп
// name         		(2) -  Наименование Взаимозависимого лица (резидента оффшорной зоны)
// iksr					(3) -  Идентификационный номер
// transDoneDate  		(4) -  Дата совершения операции
// code        			(5) -  Код налогового учёта
// reasonNumber 		(6) -  Номер
// reasonDate 			(7) -  Дата
// sum1 				(8) -  Сумма операции (сумма оборота по операции) / количество операций
// dealTariff   		(9) -  Тариф за оказание услуги
// taxTariff 			(10) - Тариф, признаваемый рыночным для целей налогообложения
// sum2					(11) - Сумма фактически начисленного дохода
// sum3					(12) - Сумма дохода, соответствующая рыночному уровню тарифа
// sum4					(13) - Сумма доначисления дохода до рыночного уровня тарифа

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
def allColumns = ['fix', 'rowNumber', 'name', 'iksr', 'transDoneDate', 'code', 'reasonNumber', 'reasonDate',
                  'sum1', 'dealTariff', 'taxTariff', 'sum2', 'sum3', 'sum4']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'transDoneDate', 'code', 'reasonNumber', 'reasonDate', 'sum1', 'dealTariff', 'taxTariff', 'sum2', 'sum3']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'sum4']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'transDoneDate', 'code', 'reasonNumber', 'reasonDate', 'sum1', 'dealTariff', 'taxTariff', 'sum2', 'sum3', 'sum4']

@Field
def totalColumns = ['sum4']

@Field
def calcColumns = ['sum4']

// Группируемые атрибуты
@Field
def groupColumns = ['code']

@Field
def sortColumns = ['code', 'name', 'reasonNumber', 'reasonDate', 'transDoneDate']

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

        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка даты совершения сделки
        if (row.reasonDate && row.transDoneDate && (row.transDoneDate.before(getReportPeriodStartDate()) ||
                row.transDoneDate.after(getReportPeriodEndDate()) || row.transDoneDate < row.reasonDate)) {
            def msg7 = row.getCell('reasonDate').column.name
            def msg4 = row.getCell('transDoneDate').column.name
            def dateFrom = getReportPeriodStartDate()?.format('dd.MM.yyyy')
            def dateTo = getReportPeriodEndDate()?.format('dd.MM.yyyy')
            logger.error("Строка $rowNum: Дата по графе «$msg4» должна принимать значение из диапазона $dateFrom - $dateTo и быть больше либо равна дате по графе «$msg7»!")
        }

        // 3. Проверка даты основания совершения операции
        checkDatePeriod(logger, row, 'reasonDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // 4. Проверка положительной суммы дохода
        if (row.sum2 != null && row.sum2 < 0) {
            def msg = row.getCell('sum2').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // 5. Проверка положительной суммы доначисления доходов
        if (row.sum2 != null && row.sum3 != null && row.sum3 < row.sum2) {
            def msg = row.getCell('sum2').column.name
            def msg1 = row.getCell('sum3').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg»!")
        }

        // 6. Проверка расчётных граф (арифметические проверки)
        def needValue = formData.createDataRow()
        needValue.sum4 = calc13(row)
        checkCalc(row, calcColumns, needValue, logger, true)
    }

    // 7. Проверка наличия всех фиксированных строк
    // 8. Проверка отсутствия лишних фиксированных строк
    // 9. Проверка итоговых значений по фиксированным строкам
    checkItog(dataRows)

    // 10. Проверка итоговых значений пофиксированной строке «Итого»
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

    for (row in dataRows) {
        row.sum4 = calc13(row)
    }

    // Сортировка по группируемой графе (разыменовывание не нужно, так как строка)
    sortRows(dataRows, groupColumns)

    // Добавление подитогов
    addAllAliased(dataRows, new ScriptUtils.CalcAliasRow() {
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

def calc13(def row) {
    if (row.sum2 != null && row.sum3 != null) {
        return row.sum3 - row.sum2
    }
    return null
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Всего'
    totalRow.getCell('fix').colSpan = 3
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 13
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = '№ пп'
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

    // формирование строк нф
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
        if (rowValues[INDEX_FOR_SKIP] == "Всего") {
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

    // сравнение подитогов
    if (!totalRowFromFileMap.isEmpty()) {
        // получить посчитанные подитоги
        def tmpSubTotalRows = calcSubTotalRows(rows)
        tmpSubTotalRows.each { subTotalRow ->
            def totalRows = totalRowFromFileMap[subTotalRow.getIndex()]
            if (totalRows) {
                totalRows.each { totalRow ->
                    compareTotalValues(totalRow, subTotalRow, totalColumns, logger, false)
                }
                totalRowFromFileMap.remove(subTotalRow.getIndex())
            } else {
                row = rows[subTotalRow.getIndex() - 1]
                def groupValue = getValuesByGroupColumn(row)
                rowWarning(logger, null, String.format(GROUP_WRONG_ITOG, groupValue))
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
            ([(headerRows[0][6]): 'Основание для совершения операции']),
            ([(headerRows[1][2]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][3]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][4]): getColumnName(tmpRow, 'transDoneDate')]),
            ([(headerRows[1][5]): getColumnName(tmpRow, 'code')]),
            ([(headerRows[1][6]): 'Номер']),
            ([(headerRows[1][7]): 'Дата']),
            ([(headerRows[1][8]): getColumnName(tmpRow, 'sum1')]),
            ([(headerRows[1][9]): getColumnName(tmpRow, 'dealTariff')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'taxTariff')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'sum2')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'sum3')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'sum4')]),
    ]
    (2..13).each {
        headerMapping.add([(headerRows[2][it]): it.toString()])
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

    def recordId = getTcoRecordId(nameFromFile, values[3], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), true, logger, refBookFactory, recordCache)
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
    newRow.transDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 5
    newRow.code = values[colIndex]
    colIndex++

    // графа 6
    newRow.reasonNumber = values[colIndex]
    colIndex++

    // графа 7
    newRow.reasonDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графы 8
    newRow.sum1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.dealTariff = values[colIndex]
    colIndex++

    // графа 10
    newRow.taxTariff = values[colIndex]
    colIndex++

    // графы 11-13
    ['sum2', 'sum3', 'sum4'].each {
        newRow[it] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

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

    // графа 5
    def colIndex = 5
    newRow.code = values[colIndex]

    // графа 13
    colIndex = 13
    newRow.sum4 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    return newRow
}
/**
 * Получить подитоговую строку с заданными стилями.
 *
 * @param i номер строки
 */
DataRow<Cell> getSubTotalRow(int i) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.fix = 'Итого'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 3
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}
// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
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

    // графа 13
    colIndex = 13
    newRow.sum4 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}
// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def columns = sortColumns + (allColumns - sortColumns)
    // Сортировка (внутри групп)
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { columns.contains(it.getAlias())})
    def newRows = []
    def tempRows = []
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            if (!tempRows.isEmpty()) {
                sortRows(tempRows, columns)
                newRows.addAll(tempRows)
                tempRows = []
            }
            newRows.add(row)
            continue
        }
        tempRows.add(row)
    }
    if (!tempRows.isEmpty()) {
        sortRows(tempRows, columns)
        newRows.addAll(tempRows)
    }
    dataRowHelper.setAllCached(newRows)

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
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
    checkItogRows(groupRows, testItogRows, itogRows, new GroupString() {
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

// вынес метод в скрипт для правки проверок
void checkItogRows(def dataRows, def testItogRows, def itogRows, ScriptUtils.GroupString groupString, ScriptUtils.CheckGroupSum checkGroupSum) {
    // считает количество реальных групп данных
    def groupCount = 0
    // Итоговые строки были удалены
    // Неитоговые строки были удалены
    for (int i = 0; i < dataRows.size(); i++) {
        DataRow<Cell> row = dataRows.get(i);
        // строка или итог другой группы после строки без подитога между ними
        if (i > 0) {
            def prevRow = dataRows.get(i - 1)
            if (i < (dataRows.size() - 1) && prevRow.getAlias() == null && isDiffRow(prevRow, row, groupColumns)) { // TODO сравнение
                itogRows.add(groupCount, null)
                groupCount++
                String groupCols = groupString.getString(prevRow);
                if (groupCols != null) {
                    logger.error("Группа «%s» не имеет строки итога!", groupCols); // итога (не  подитога)
                }
            }
        }
        if (row.getAlias() != null) {
            // итог после итога (или после строки из другой группы)
            if (i < 1 || dataRows.get(i - 1).getAlias() != null || isDiffRow(dataRows.get(i - 1), row, groupColumns)) { // TODO сравнение
                logger.error("Строка %d: Строка итога не относится к какой-либо группе!", row.getIndex()); // итога (не  подитога)
                // удаляем из проверяемых итогов строку без подчиненных строк
                itogRows.remove(row)
            } else {
                groupCount++
            }
        }
    }
    if (testItogRows.size() == itogRows.size()) {
        for (int i = 0; i < testItogRows.size(); i++) {
            DataRow<Cell> testItogRow = testItogRows.get(i);
            DataRow<Cell> realItogRow = itogRows.get(i);
            if (realItogRow == null) {
                continue
            }
            int rowIndex = dataRows.indexOf(realItogRow) - 1
            def row = dataRows.get(rowIndex)
            String groupCols = groupString.getString(row);
            if (groupCols != null) {
                String checkStr = checkGroupSum.check(testItogRow, realItogRow);
                if (checkStr != null) {
                    logger.error(String.format(GROUP_WRONG_ITOG_SUM, realItogRow.getIndex(), groupCols, checkStr));
                }
            }
        }
    }
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def value
    // графа 5
    if (row?.code) {
        value = row.code
    } else {
        value = 'графа 5 не задана'
    }
    return value
}