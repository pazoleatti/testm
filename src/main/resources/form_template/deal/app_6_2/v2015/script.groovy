package form_template.deal.app_6_2.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * 804 - 6.2. Размещение средств на межбанковском рынке
 *
 * formTemplateId=804
 *
 * @author Stanislav Yasinskiy
 */

// графа    ( )   - fix
// графа 1  (1)   - rowNumber    - № п/п
// графа 2  (2)   - name         - Полное наименование юридического лица с указанием ОПФ
// графа 3  (3)   - iksr         - ИНН/ КИО
// графа 4  (4.1) - countryName  - Наименование страны регистрации
// графа 5  (4.2) - countryCode  - Код страны регистрации по классификатору ОКСМ
// графа 6  (5)   - docNumber    - Номер договора
// графа 7  (6)   - docDate      - Дата договора
// графа 8  (7)   - dealNumber   - Номер сделки
// графа 9  (8)   - dealDate     - Дата заключения сделки
// графа 10 (9)   - count        - Количество
// графа 11 (10)  - sum          - Сумма доходов Банка по данным бухгалтерского учета, руб.
// графа 12 (11)  - price        - Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
// графа 13 (12)  - cost         - Итого стоимость без учета НДС, акцизов и пошлин, руб.
// графа 14 (13)  - dealDoneDate - Дата совершения сделки

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

@Field
def allColumns = ['fix', 'rowNumber', 'name', 'iksr', 'countryName', 'countryCode', 'docNumber',
        'docDate', 'dealNumber', 'dealDate', 'count', 'sum', 'price', 'cost', 'dealDoneDate']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'dealNumber', 'dealDate', 'sum', 'docNumber', 'docDate', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'countryCode', 'count', 'price', 'cost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'count', 'sum', 'dealDoneDate']

// Группируемые атрибуты
@Field
def groupColumns = ['name']

@Field
def totalColumns = ['sum']

// Дата окончания отчетного периода
@Field
def endDate = null

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
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
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

        // 10.Проверка положительной суммы доходов
        if (row.sum && row.sum < 0) {
            def msg = row.getCell('sum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше значения «0»!")
        }
    }

    // 11. Проверка наличия всех фиксированных строк «Итого по ЮЛ»
    // 12. Проверка отсутствия лишних фиксированных строк «Итого по ЮЛ»
    // 13. Проверка итоговых значений по фиксированным строкам «Итого по ЮЛ»
    checkItog(dataRows)

    // 14. Проверка итоговых значений пофиксированной строке «Итого»
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
    sortRows(dataRows, groupColumns)

    for (row in dataRows) {
        // Расчет поля " Количество"
        row.count = 1
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Итого"
        row.cost = row.sum
    }

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

    sortFormDataRows(false)
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def value2 = getRefBookValue(520L, dataRows.get(i).name)?.NAME?.value
    def newRow = getSubTotalRow(null, value2, i)

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
 *
 * @param title надпись подитога (если задана, то используется это значение)
 * @param value2 значение графы 2 (если value2 не задан, то используется 'Итого ЮЛ не задано')
 * @param i номер строки
 */
DataRow<Cell> getSubTotalRow(def title, def value2, int i) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    if (title) {
        newRow.fix = title
    } else if (value2) {
        newRow.fix = 'Итого по «' + value2 + '»'
    } else {
        newRow.fix = 'Итого по «ЮЛ не задано»'
    }
    newRow.setAlias('itg#'.concat(i.toString()))
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
    int COLUMN_COUNT = 15
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
    def totalRowFromFileMap = [:]           // мапа для хранения строк подитогов со значениями из файла (стили простых строк)

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
            totalRowFromFile = getNewTotalFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (rowValues[INDEX_FOR_SKIP].contains("Итого по ")) {
            def subTotalRow = getNewSubTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            if (totalRowFromFileMap[subTotalRow.fix] == null) {
                totalRowFromFileMap[subTotalRow.fix] = []
            }
            totalRowFromFileMap[subTotalRow.fix].add(subTotalRow)
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
        def tmpRows = rows.findAll { !it.getAlias() }
        // получить посчитанные подитоги
        def tmpSubTotalRows = calcSubTotalRows(rows)
        tmpSubTotalRows.each { subTotalRow ->
            def totalRows = totalRowFromFileMap[subTotalRow.fix]
            if (totalRows) {
                totalRows.each { totalRow ->
                    compareTotalValues(totalRow, subTotalRow, totalColumns, logger, false)
                }
                totalRowFromFileMap.remove(subTotalRow.fix)
            }
        }
        if (!totalRowFromFileMap.isEmpty()) {
            // для этих подитогов из файла нет групп
            totalRowFromFileMap.each { key, totalRows ->
                totalRows.each { totalRow ->
                    totalColumns.each { alias ->
                        def msg = String.format(COMPARE_TOTAL_VALUES, totalRow.getIndex(), getColumnName(totalRow, alias), totalRow[alias], BigDecimal.ZERO)
                        rowWarning(logger, totalRow, msg)
                    }
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
            ([(headerRows[1][10]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'sum')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[1][14]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][1]) : 'гр. 1']),
            ([(headerRows[2][2]) : 'гр. 2']),
            ([(headerRows[2][3]) : 'гр. 3']),
            ([(headerRows[2][4]) : 'гр. 4.1']),
            ([(headerRows[2][5]) : 'гр. 4.2'])
    ]
    (5..13).each {
        headerMapping.add([(headerRows[2][it + 1]): 'гр. ' + it])
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

    def recordId = getRecordId(nameFromFile, values[3], fileRowIndex, colIndex, iksrName)
    def map = getRefBookValue(520, recordId)
    if (map && nameFromFile != map.NAME?.stringValue) {
        if (map && nameFromFile != map.NAME?.stringValue) {
            // сообщение 4
            String msg = "Наименование юридического лица в файле не заполнено!"
            if (nameFromFile) {
                msg = "В файле указано другое наименование юридического лица - «$nameFromFile»!"
            }
            logger.warn("Строка $fileRowIndex , столбец " + ScriptUtils.getXLSColumnName(colIndex) + ": " +
                    "На форме графы с общей информацией о юридическом лице заполнены данными записи справочника «Участники ТЦО», " +
                    "в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «" + map.NAME?.stringValue + "», " +
                    "атрибут «ИНН (заполняется для резидентов, некредитных организаций)» = «" + map.INN?.stringValue + "». $msg")
        }
    }

    // графа 2
    newRow.name = recordId
    colIndex++

    // графа 3
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

// Получение Id записи из справочника 520 с использованием кэширования
def getRecordId(String name, String iksr, int fileRowIndex, int colIndex, String iksrName) {
    if (!iksr) {
        logger.warn("Строка $fileRowIndex , столбец " + ScriptUtils.getXLSColumnName(colIndex) + ": " +
                "На форме не заполнены графы с общей информацией о юридическом лице, так как в файле отсутствует значение по графе «$iksrName»!")
        return
    }
    def ref_id = 520
    def RefBook refBook = refBookFactory.get(ref_id)

    String filter = "(LOWER(INN) = LOWER('$iksr') or " +
            "LOWER(REG_NUM) = LOWER('$iksr') or " +
            "LOWER(TAX_CODE_INCORPORATION) = LOWER('$iksr') or " +
            "LOWER(SWIFT) = LOWER('$iksr') or " +
            "LOWER(KIO) = LOWER('$iksr'))"
    if (recordCache[ref_id] != null) {
        if (recordCache[ref_id][filter] != null) {
            return recordCache[ref_id][filter]
        }
    } else {
        recordCache[ref_id] = [:]
    }

    def provider = refBookFactory.getDataProvider(ref_id)
    def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
    if (records.size() == 1) {
        // 5
        recordCache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        return recordCache[ref_id][filter]
    } else if (records.empty) {
        // 6
        if(!name){
            name = "наименование юридического лица в файле не заполнено"
        }
        // сообщение 1
        logger.warn("Строка $fileRowIndex , столбец " + ScriptUtils.getXLSColumnName(colIndex) + ": " +
                "Для заполнения графы «$iksrName» формы в справочнике «Участники ТЦО» " +
                "не найдено значение «$iksr» ($name), актуальное на дату «" + getReportPeriodEndDate().format("dd.MM.yyyy") + "»!")
        endMessage(iksrName)
    } else {
        // 7
        def recordsByName
        if (name) {
            recordsByName = provider.getRecords(getReportPeriodEndDate(), null, "LOWER(NAME) = LOWER('$name') and " + filter, null)
        }
        if (recordsByName && recordsByName.size() == 1) {
            recordCache[ref_id][filter] = recordsByName.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
            return recordCache[ref_id][filter]
        } else {
            if (!name) {
                name = "наименование юридического лица в файле не заполнено"
            }
            // сообщение 2
            logger.warn("Строка $fileRowIndex , столбец " + ScriptUtils.getXLSColumnName(colIndex) + ": " +
                    "Для заполнения графы «$iksrName» формы в справочнике «Участники ТЦО» " +
                    "найдено несколько записей со значением «$iksr» ($name), актуальным на дату «" + getReportPeriodEndDate().format("dd.MM.yyyy") + "»! " +
                    "Графа «$iksrName» формы заполнена первой найденной записью справочника:")
            def record
            records.each {
                def refBookAttributeName
                for(alias in ['INN', 'REG_NUM', 'TAX_CODE_INCORPORATION', 'SWIFT', 'KIO']){
                   if(iksr.equals(it.get(alias)?.stringValue)){
                       refBookAttributeName = refBook.attributes.find{ it.alias == alias}.name
                       record = it
                       break
                   }
                }
                // сообщение 3
                logger.warn("Атрибут «Полное наименование юридического лица с указанием ОПФ» = «" + it.get('NAME')?.stringValue + "», " +
                        "атрибут «$refBookAttributeName» = «" + iksr + "»")
            }
            endMessage(iksrName)
            return record.get(RefBook.RECORD_ID_ALIAS).numberValue
        }
    }
    return null
}

def endMessage(String iksrName) {
    // сообщение 5
    logger.warn("Для заполнения на форме граф с общей информацией о юридическом лице выполнен поиск значения файла " +
            "по графе «$iksrName» в следующих атрибутах справочника «Участники ТЦО»: " +
            "«ИНН (заполняется для резидентов, некредитных организаций)», " +
            "«Регистрационный номер в стране инкорпорации (заполняется для нерезидентов)», " +
            "«Код налогоплательщика в стране инкорпорации», " +
            "«Код SWIFT (заполняется для кредитных организаций, резидентов и нерезидентов)», " +
            "«КИО (заполняется для нерезидентов)»")
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), dataRows.find { it.getAlias() == 'total' }, true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
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

    // графа 11
    def colIndex = 11
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

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
    def newRow = getSubTotalRow(values[0], null, fileRowIndex)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 11
    def colIndex = 11
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias())}
}

// Получить посчитанные подитоговые строки
def calcSubTotalRows(def dataRows) {
    def tmpRows = dataRows.findAll { !it.getAlias() }
    // Добавление подитогов
    addAllAliased(tmpRows, new ScriptUtils.CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)

    return tmpRows.findAll { it.getAlias() }
}

// Проверки подитоговых сумм
void checkItog(def dataRows) {
    // Рассчитанные строки итогов
    def testItogRows = calcSubTotalRows(dataRows)
    // Имеющиеся строки итогов
    def itogRows = dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
    checkItogRows(dataRows, testItogRows, itogRows, groupColumns, logger, new ScriptUtils.GroupString() {
        @Override
        String getString(DataRow<Cell> row) {
            return getValuesByGroupColumn(row)
        }
    }, new ScriptUtils.CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            if (row1.sum != row2.sum) {
                return getColumnName(row1, 'sum')
            }
            return null
        }
    })
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    if (!row.name) {
        return 'ЮЛ не задано'
    }
    def map = getRefBookValue(520, row.name)
    if (map != null) {
        return map.NAME?.stringValue
    }
    return null
}