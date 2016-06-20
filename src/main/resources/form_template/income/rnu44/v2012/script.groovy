package form_template.income.rnu44.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * (РНУ-44) Регистр налогового учёта доходов, в виде восстановленной амортизационной премии при реализации ранее,
 * чем по истечении 5 лет с даты ввода в эксплуатацию Взаимозависимым лицам и резидентам оффшорных зон основных средств
 * введённых в эксплуатацию после 01.01.2013
 * formTemplateId=340
 *
 * Версия ЧТЗ: 64
 *
 * @author lhaziev
 * @author vsergeev
 *
 * Графы:
 *
 *                  *****   РНУ 44  *****
 * 1    number          № пп
 * 2    operationDate   Дата операции
 * 3    name            Основное средство
 * 4    inventoryNumber Инвентарный номер
 * 5    baseNumber      Номер
 * 6    baseDate        Дата
 * 7    summ            Сумма восстановленной амортизационной премии
 *
 *                  *****   РНУ 49  *****
 * 1    rowNumber   № пп
 * 2    firstRecordNumber       Номер первой записи
 * 3    operationDate           Дата операции
 * 4    reasonNumber            номер
 * 5    reasonDate              дата
 * 6    invNumber               Инвентарный номер
 * 7    name                    Наименование
 * 8    price                   Цена приобретения
 * 9    amort                   Фактически начислено амортизации (отнесено на расходы)
 * 10   expensesOnSale          Расходы при реализации
 * 11   sum                     Сумма начисленной выручки от реализации
 * 12   sumInFact               Сумма фактически поступивших денежных средств
 * 13   costProperty            Стоимость материалов и имущества, полученных при ликвидации основных средств
 * 14   marketPrice             Рыночная цена
 * 15   sumIncProfit            Сумма к увеличению прибыли (уменьшению убытка)
 * 16   profit                  Прибыль от реализации
 * 17   loss                    Убыток от реализации
 * 18   usefullLifeEnd          Дата истечения срока полезного использования
 * 19   monthsLoss              Количество месяцев отнесения убытков на расходы
 * 20   expensesSum             Сумма расходов, приходящаяся на каждый месяц
 * 21   saledPropertyCode       Шифр вида реализованного (выбывшего) имущества
 * 22   saleCode                Шифр вида реализации (выбытия)
 * 23   propertyType            Тип имущества
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkRNU()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        checkRNU()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
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
        checkRNU()
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

// все атрибуты
@Field
def allColumns = ['number', 'operationDate', 'name', 'inventoryNumber', 'baseNumber', 'baseDate', 'summ']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'inventoryNumber']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns//['operationDate', 'baseNumber', 'baseDate', 'summ']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['operationDate', 'name', 'inventoryNumber', 'baseNumber', 'baseDate', 'summ']

// Атрибуты итоговых строк для которых вычисляются суммы
@Field
def totalColumns = ['summ']

// алиасы графов для арифметической проверки
@Field
def arithmeticCheckAlias = ['summ']

// алиасы графов для не арифметической проверки
@Field
def otherCheckAlias = ['operationDate', 'baseNumber', 'baseDate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

//границы раздела "А" в РНУ-49
@Field
String rnu49AIndex = 'A'

//границы раздела "А" в РНУ-49
@Field
String rnu49TotalAIndex = 'totalA'

//// Обертки методов

// заполняем ячейки, вычисляемые автоматически
def calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def rnu49FormData = getRnu49FormData()
    def rnu49Rows = rnu49FormData?.getAllCached()
    if (rnu49Rows == null || rnu49Rows.isEmpty()) return

    // получить строку "итого"
    def totalRow = getDataRow(dataRows, 'total')
    // очистить итоги
    totalColumns.each {
        totalRow[it] = null
    }

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    for (def dataRow : dataRows) {
        def rnu49Row = getRnu49Row(rnu49Rows, dataRow)

        if (rnu49Row) {
            def values = getValues(rnu49Row)
            values.keySet().each { colName ->
                dataRow[colName] = values[colName]
            }
        }
    }
    // добавить строку "итого"
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
}

/**
 * Получить данные за формы "(РНУ-49) Регистр налогового учёта «ведомость определения результатов от реализации (выбытия) имущества»"
 */
def getRnu49FormData() {
    def formData49 = formDataService.getLast(312, formData.kind, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
    if (formData49 != null && formData49.id != null) {
        return formDataService.getDataRowHelper(formData49)
    }
    return null
}

/**
 * В разделе А РНУ-49 находим строку, для которой «графа 6» = «графа 4» текущей строки формы РНУ-44.
 */
def getRnu49Row(def rnu49Rows, def dataRow) {
    //находим границы раздела "А" в РНУ-49
    def startToSearchIndex = rnu49Rows.indexOf(getDataRow(rnu49Rows, rnu49AIndex))
    def endToSearchIndex = rnu49Rows.indexOf(getDataRow(rnu49Rows, rnu49TotalAIndex))

    def indexRow = (startToSearchIndex..endToSearchIndex).find { index ->
        rnu49Rows[index].invNumber == dataRow.inventoryNumber
    }
    return indexRow ? rnu49Rows[indexRow] : [:]
}

/**
 * получаем мапу со значениями, расчитанными для каждой конкретной строки
 */
def getValues(def rnu49Row) {
    def values = [:]

    values.with {
        /*2*/ operationDate = getOperationDate(rnu49Row)
        /*5*/ baseNumber = getBaseNumber(rnu49Row)
        /*6*/ baseDate = getBaseDate(rnu49Row)
        /*7*/ summ = getSumm(rnu49Row)
    }

    return values
}

def getOperationDate(def rnu49Row) {
    return rnu49Row.operationDate
}

def getBaseNumber(rnu49Row) {
    return rnu49Row.reasonNumber
}

def getBaseDate(rnu49Row) {
    return rnu49Row.reasonDate
}

def getSumm(rnu49Row) {
    return rnu49Row.sum
}

/**
 * проверяем все данные формы на обязательное и корректное заполнение
 */
boolean logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def rnu49Rows = getRnu49FormData()?.allSaved
    if (rnu49Rows == null) return

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка ${index}: "

        // 0.
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 1. Проверка уникальности значий в графе «Номер ссудного счета»
        def find = dataRows.find { it ->
            (row != it && row.inventoryNumber == it.inventoryNumber)
        }
        if (find != null) {
            rowError(logger, row, errorMsg + "Инвентарный номер не уникальный!")
        }

        // 3. Арифметические проверки расчета неитоговых граф
        def rnu49Row = getRnu49Row(rnu49Rows, row)
        if (rnu49Row == [:]) {
            rowError(logger, row, errorMsg + "Отсутствуют данные в РНУ-49!")
        }
        def calcValues = getValues(rnu49Row)
        checkCalc(row, arithmeticCheckAlias, calcValues, logger, true)
        for (def colName : otherCheckAlias) {
            if (row[colName] != calcValues[colName]) {
                String msg = getColumnName(row, colName)
                rowError(logger, row, errorMsg + "Неверно рассчитана графа «$msg»!")
            }
        }
    }
    // 4. Арифметические проверки расчета итоговой графы
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 7, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 2
        def xmlIndexCol = 2
        newRow.operationDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 3
        xmlIndexCol = 3
        newRow.name = row.cell[xmlIndexCol].text()
        // графа 4
        xmlIndexCol = 4
        newRow.inventoryNumber = row.cell[xmlIndexCol].text()
        // графа 5
        xmlIndexCol = 5
        newRow.baseNumber = row.cell[xmlIndexCol].text()
        // графа 6
        xmlIndexCol = 6
        newRow.baseDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 7
        xmlIndexCol = 7
        newRow.summ = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    def totalRow = getDataRow(dataRows, 'total')
    calcTotalSum(rows, totalRow, totalColumns)
    rows.add(totalRow)

    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow += 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // графа 7
        xmlIndexCol = 7
        total.summ = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        def colIndexMap = ['summ': 7]
        for (def alias : totalColumns) {
            def v1 = total[alias]
            def v2 = totalRow[alias]
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
            }
        }
        // задать итоговой строке нф значения из итоговой строки тф
        totalColumns.each { alias ->
            totalRow[alias] = total[alias]
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
        // очистить итоги
        totalColumns.each { alias ->
            totalRow[alias] = null
        }
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

// проверить наличие рну 49
void checkRNU() {
    if (formData.kind == FormDataKind.PRIMARY) {
        formDataService.checkFormExistAndAccepted(312, FormDataKind.PRIMARY, formData.departmentId,
                formData.reportPeriodId, false, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, getDataRow(dataRows, 'total'), null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 8
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('number').column.name
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

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def totalRow = getDataRow(formTemplate.rows, 'total')
    rows.add(totalRow)
    updateIndexes(rows)
    // сравнение итогов
    compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)

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
            ([(headerRows[0][0]): tmpRow.getCell('number').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('operationDate').column.name]),
            ([(headerRows[0][3]): 'Основное средство']),
            ([(headerRows[1][3]): 'Наименование']),
            ([(headerRows[1][4]): 'Инвентарный номер']),
            ([(headerRows[0][5]): 'Основание для восстановления амортизационной премии']),
            ([(headerRows[1][5]): 'Номер']),
            ([(headerRows[1][6]): 'Дата']),
            ([(headerRows[0][7]): tmpRow.getCell('summ').column.name]),
            ([(headerRows[2][0]): '1'])
    ]
    (2..7).each {
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

    // графа 1
    def colIndex = 0
    newRow.number = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // fix
    colIndex++

    // графа 2
    colIndex++
    newRow.operationDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 3
    colIndex++
    newRow.name = values[colIndex]

    // графа 4
    colIndex++
    newRow.inventoryNumber = values[colIndex]

    // графа 5
    colIndex++
    newRow.baseNumber = values[colIndex]

    // графа 6
    colIndex++
    newRow.baseDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7
    colIndex++
    newRow.summ = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}