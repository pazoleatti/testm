package form_template.deal.app_6_6.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * 806 - 6.6. Заключение сделок РЕПО
 *
 * formTemplateId=806
 *
 * @author Stanislav Yasinskiy
 */

// графа    ( )    - fix
// графа 1  (1)    - rowNumber            - № п/п
// графа 2  (2)    - name                 - Полное наименование с указанием ОПФ
// графа 3  (3)    - iksr                 - ИНН/КИО
// графа 4  (4.1)  - countryName          - Наименование страны регистрации
// графа 5  (4.2)  - countryCode          - Код страны регистрации по классификатору ОКСМ
// графа 6  (5)    - docNumber            - Номер договора
// графа 7  (6)    - docDate              - Дата договора
// графа 8  (7)    - dealNumber           - Номер сделки
// графа 9  (8)    - dealDate             - Дата (заключения) сделки
// графа 10 (9)    - dealsMode            - Режим переговорных сделок
// графа 11 (10.1) - date1                - Дата исполнения 1-ой части сделки
// графа 12 (10.2) - date2                - Дата исполнения 2-ой части сделки
// графа 13 (11.1) - incomeSum            - Сумма процентного дохода (руб.)
// графа 14 (11.2) - outcomeSum           - Сумма процентного расхода (руб.)
// графа 15 (12)   - priceFirstCurrency   - Цена 1-ой части сделки, ед. валюты
// графа 16 (13)   - currencyCode         - Код валюты расчетов по сделке
// графа 17 (14)   - courseCB             - Курс ЦБ РФ
// графа 18 (15)   - priceFirstRub        - Цена 1-ой части сделки, руб.
// графа 19 (16)   - dealDoneDate         - Дата совершения сделки

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
        'docDate', 'dealNumber', 'dealDate', 'dealsMode', 'date1', 'date2', 'incomeSum', 'outcomeSum',
        'priceFirstCurrency', 'currencyCode', 'courseCB', 'priceFirstRub', 'dealDoneDate']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'date1', 'date2', 'incomeSum',
                       'outcomeSum', 'priceFirstCurrency', 'currencyCode', 'courseCB', 'priceFirstRub']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'countryCode', 'dealsMode', 'dealDoneDate']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'date1', 'date2', 'priceFirstCurrency',
                       'currencyCode', 'courseCB', 'priceFirstRub']

// Группируемые атрибуты
@Field
def groupColumns = ['name']

@Field
def totalColumns = ['incomeSum', 'outcomeSum']

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

    Date dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    Date dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time

    String dateFormat = 'yyyy'
    String formYear = (String) reportPeriodService.get(formData.reportPeriodId).getTaxPeriod().getYear()
    Date formDate = Date.parse('dd.MM.yyyy', "31.12.$formYear")

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка на заполнение графы
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка возможности заполнения режима переговорных сделок
        def countryCode = null
        if (row.name) {
            def map = getRefBookValue(520, row.name)
            if (map) {
                map = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
                if (map) {
                    countryCode = map.NAME?.stringValue
                }
            }
        }
        if (!countryCode) {
            String msg1 = row.getCell('dealsMode').column.name
            String msg2 = row.getCell('countryCode').column.name
            logger.error("Строка $rowNum: Выполнение расчета графы «$msg1» невозможно, так как не заполнена " +
                    "используемая в расчете графа «$msg2»!")
        }

        // 3. Проверка режима переговорных сделок
        if (countryCode) {
            if (row.dealsMode != calc10(row.countryCode)) {
                String msg1 = row.getCell('dealsMode').column.name
                String msg2 = row.getCell('countryCode').column.name
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению «Да», " +
                        "если графа «$msg2» равна значению «643»!")
            }
        }

        // 4. Проверка возможности заполнения даты совершения сделки
        if (!row.date2) {
            String msg1 = row.getCell('dealDoneDate').column.name
            String msg2 = row.getCell('date2').column.name
            logger.error("Строка $rowNum: Выполнение расчета графы «$msg1» невозможно, так как не заполнена " +
                    "используемая в расчете графа «$msg2»!")
        }

        // 5. Проверка даты совершения сделки
        if (row.date2) {
            if (row.dealDoneDate != calc19(row.date2, formYear, formDate, dateFormat)) {
                String msg = row.getCell('dealDoneDate').column.name
                logger.error("Строка $rowNum: Графа «$msg» заполнена неверно!")
            }
        }

        // 6. Заполнение граф 13 и 14 (сумма дохода, расхода)
        boolean noOne = (row.incomeSum == null && row.outcomeSum == null)
        boolean both = (row.incomeSum != null && row.outcomeSum != null)
        if (noOne) {
            String msg1 = row.getCell('outcomeSum').column.name
            String msg2 = row.getCell('incomeSum').column.name
            logger.error("Строка $rowNum: Графа «$msg1» должна быть заполнена, если не заполнена графа «$msg2»!")
        }
        if (both) {
            String msg1 = row.getCell('outcomeSum').column.name
            String msg2 = row.getCell('incomeSum').column.name
            logger.error("Строка $rowNum: Графа «$msg1» не может быть заполнена одновременно с графой «$msg2»!")
        }

        // 7. Проверка положительной суммы дохода/расхода
        if (!noOne && !both) {
            sum = (row.incomeSum != null) ? row.incomeSum : row.outcomeSum
            if (sum <= 0) {
                String msg = (row.incomeSum != null) ? row.getCell('incomeSum').column.name : row.getCell('outcomeSum').column.name
                logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше значения «0»!")
            }
        }

        // 8. Корректность даты (заключения) сделки
        if (row.dealDate && row.docDate && row.dealDate < row.docDate) {
            String msg1 = row.getCell('dealDate').column.name
            String msg2 = row.getCell('docDate').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // 9. Корректность даты исполнения 1–ой части сделки (проверка даты окончания периода)
        // 10. Корректность даты исполнения 1–ой части сделки (проверка даты начала периода)
        Date date1 = row.date1
        if (date1 != null) {
            if (date1 > dTo) {
                String msg = row.getCell('date1').column.name
                logger.error("Строка $rowNum: Значение графы «$msg» не может быть больше даты окончания отчётного периода!")
            }
            if (date1 < dFrom) {
                String msg = row.getCell('date1').column.name
                logger.error("Строка $rowNum: Значение графы «$msg» не может быть меньше даты начала отчётного периода!")
            }
        }

        // 11. Корректность даты совершения сделки
        if (row.dealDoneDate && row.dealDate && row.dealDoneDate < row.dealDate) {
            String msg1 = row.getCell('dealDoneDate').column.name
            String msg2 = row.getCell('dealDate').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // 12. Проверка диапазона дат
        if (row.docDate) {
            checkDateValid(logger, row, 'docDate', row.docDate, true)
        }
    }

    // 13. Проверка наличия всех фиксированных строк «Итого по ЮЛ»
    // 14. Проверка отсутствия лишних фиксированных строк «Итого по ЮЛ»
    // 15. Проверка итоговых значений по фиксированным строкам «Итого по ЮЛ»
    checkItog(dataRows)

    // 16. Проверка итоговых значений пофиксированной строке «Итого»
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

    String dateFormat = 'yyyy'
    def formYear = (String) reportPeriodService.get(formData.reportPeriodId).getTaxPeriod().getYear()
    def formDate = Date.parse('dd.MM.yyyy', "31.12.$formYear")

    for (row in dataRows) {
        // Расчет поля "Режим переговорных сделок"
        row.dealsMode = calc10(row.countryCode)
        // Расчет поля "Дата совершения сделки"
        row.dealDoneDate = calc19(row.date2, formYear, formDate, dateFormat)
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

def String calc10(def countryCode) {
    if (countryCode != null) {
        def country = refBookService.getStringValue(10, countryCode, 'CODE')
        if (country == '643') {
            return 'Да'
        }
    }
    return null
}

def Date calc19(Date date2, String formYear, Date formDate, String dateFormat) {
    if (date2) {
        String date2Year = date2.format(dateFormat)
        if (date2Year == formYear) {
            return date2
        } else {
            return formDate
        }
    }
    return null
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
    int COLUMN_COUNT = 20
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Общая информация о контрагенте - юридическом лице'
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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def totalRowFromFile = null
    def totalRowFromFileMap = [:]           // мапа для хранения строк подитогов со значениями из файла (стили простых строк)

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
            ([(headerRows[0][0]) : 'Общая информация о контрагенте - юридическом лице']),
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
            ([(headerRows[1][10]): getColumnName(tmpRow, 'dealsMode')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'date1')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'date2')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'incomeSum')]),
            ([(headerRows[1][14]): getColumnName(tmpRow, 'outcomeSum')]),
            ([(headerRows[1][15]): getColumnName(tmpRow, 'priceFirstCurrency')]),
            ([(headerRows[1][16]): getColumnName(tmpRow, 'currencyCode')]),
            ([(headerRows[1][17]): getColumnName(tmpRow, 'courseCB')]),
            ([(headerRows[1][18]): getColumnName(tmpRow, 'priceFirstRub')]),
            ([(headerRows[1][19]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][1]) : 'гр. 1']),
            ([(headerRows[2][2]) : 'гр. 2']),
            ([(headerRows[2][3]) : 'гр. 3']),
            ([(headerRows[2][4]) : 'гр. 4.1']),
            ([(headerRows[2][5]) : 'гр. 4.2']),
            ([(headerRows[2][6]) : 'гр. 5']),
            ([(headerRows[2][7]) : 'гр. 6']),
            ([(headerRows[2][8]) : 'гр. 7']),
            ([(headerRows[2][9]) : 'гр. 8']),
            ([(headerRows[2][10]): 'гр. 9']),
            ([(headerRows[2][11]): 'гр. 10.1']),
            ([(headerRows[2][12]): 'гр. 10.2']),
            ([(headerRows[2][13]): 'гр. 11.1']),
            ([(headerRows[2][14]): 'гр. 11.2']),
            ([(headerRows[2][15]): 'гр. 12']),
            ([(headerRows[2][16]): 'гр. 13']),
            ([(headerRows[2][17]): 'гр. 14']),
            ([(headerRows[2][18]): 'гр. 15']),
            ([(headerRows[2][19]): 'гр. 16'])
    ]
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
    newRow.dealsMode = values[colIndex]
    colIndex++

    // графа 10.1
    newRow.date1 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10.2
    newRow.date2 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11.1
    newRow.incomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11.2
    newRow.outcomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.priceFirstCurrency = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
    newRow.currencyCode = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 14
    newRow.courseCB = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 15
    newRow.priceFirstRub = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 16
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

    // графа 13
    def colIndex = 13
    newRow.incomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 14
    colIndex = 14
    newRow.outcomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

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

    // графа 13
    def colIndex = 13
    newRow.incomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 14
    colIndex = 14
    newRow.outcomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

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
            if (row1.incomeSum != row2.incomeSum) {
                return getColumnName(row1, 'incomeSum')
            }
            if (row1.outcomeSum != row2.outcomeSum) {
                return getColumnName(row1, 'outcomeSum')
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