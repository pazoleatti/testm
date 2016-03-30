package form_template.income.rnu_123.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 РНУ-123. Регистр налогового учёта доходов по гарантиям и аккредитивам и иным гарантийным продуктам,
 * включая инструменты торгового финансирования, предоставляемым Взаимозависимым лицам и резидентам оффшорных зон по ценам, не соответствующим рыночному уровню
 *
 * formTemplateId=841
 */

// fix
// rowNumber    		(1) -  № пп
// name         		(2) -  Наименование Взаимозависимого лица (резидента оффшорной зоны)
// iksr					(3) -  Идентификационный номер
// countryName			(4) -  Страна регистрации
// code        			(5) -  Код классификации дохода
// docNumber 			(6) -  номер
// docDate 				(7) -  дата
// sum1 				(8) -  Сумма кредита для расчёта (остаток задолженности, невыбранный лимит кредита), ед. вал.
// course   			(9) -  Валюта
// transDoneDate		(10) - Фактическое отражение в бухгалтерском учете
// taxDoneDate			(11) - Для целей доначисления дохода в налоговом учете
// course2				(12) - Курс На дату фактического отражения в бухгалтерском учете
// course3				(13) - Курс Для целей доначисления дохода в налоговом учете
// startDate1			(14) - Дата начала
// endDate1				(15) - Дата окончания
// startDate2			(16) - Дата начала
// endDate2				(17) - Дата окончания
// base					(18) - База для расчета, кол. дней
// dealPay				(19) - Плата по условиям сделки, % год./ед. вал.
// sum2					(20) - По данным бухгалтерского учета
// sum3					(21) - Доначисление для целей налогового учета
// sum4					(22) - Всего по данным налогового учета
// tradePay				(23) - Рыночная Плата, % годовых/ед. вал.
// sum5					(24) - Доходу начисленному по данным налогового учета согласно условиям сделк
// sum6					(25) - Доначисленному доходу для целей налогового учета
// sum7					(26) - Всей сумме дохода, начисленного по данным налогового учета
// sum8					(27) - Фактическому доходу, начисленному по данным налогового учета согласно условиям сделки
// sum9					(28) - Доначисленному доходу для целей налогового учета
// sum10				(29) - Всей сумме дохода, начисленного по данным налогового учета


switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_LOAD:
        // afterLoad() TODO убрал в 0.9.3, вернуть в 1.0
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
def allColumns = ['fix', 'rowNumber', 'name', 'iksr', 'countryName', 'code', 'docNumber', 'docDate', 'sum1', 'course',
                  'transDoneDate', 'taxDoneDate', 'course2', 'course3', 'startDate1', 'endDate1', 'startDate2', 'endDate2',
                  'base', 'dealPay', 'sum2', 'sum3', 'sum4', 'tradePay', 'sum5', 'sum6', 'sum7', 'sum8', 'sum9', 'sum10']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'code', 'docNumber', 'docDate', 'sum1', 'course', 'transDoneDate', 'taxDoneDate', 'course2',
                       'course3', 'startDate1', 'endDate1', 'startDate2', 'endDate2', 'base', 'dealPay', 'sum2', 'sum3', 'tradePay', 'sum6', 'sum7', 'sum9', 'sum10']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'sum4', 'sum5', 'sum8']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'code', 'docNumber', 'docDate', 'sum1', 'course', 'transDoneDate', 'course2', 'startDate1',
                       'endDate1', 'base', 'dealPay', 'sum2', 'sum4', 'tradePay', 'sum7']

@Field
def totalColumns = ['sum2', 'sum3', 'sum4', 'sum5', 'sum6', 'sum7', 'sum8', 'sum9', 'sum10']

@Field
def sortColumns = ['name', 'docNumber', 'docDate', 'transDoneDate']

@Field
def calcColumns = ['sum4', 'sum5', 'sum8']

@Field
def course810 = getRecordId(15, 'CODE', '810')

@Field
def pattern = /[0-9]{1,10}[\.]?[0-9]{0,2}\%?/

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

// Порядок отчетного периода
@Field
def periodOrder = null

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

def getPeriodOrder() {
    if (periodOrder == null) {
        periodOrder = reportPeriodService.get(formData.reportPeriodId).getOrder()
    }
    return periodOrder
}

// Поиск записи в справочнике по значению (для расчетов)
def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), -1, null, logger, true)
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

        // 2. Проверка корректности даты первичного документа
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // 3. Проверка суммы гарантии/ аккредитива
        if (row.sum1 != null && row.sum1 < 0) {
            def msg = row.getCell('sum1').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // 4. Проверка даты операции, фактически отраженной в бухгалтерском учете
        // 5. Проверка корректности даты операции для целей доначисления дохода в налоговом учете
        ['transDoneDate', 'taxDoneDate'].each { alias ->
            if (row.docDate && row[alias] && (row[alias].before(getReportPeriodStartDate()) ||
                    row[alias].after(getReportPeriodEndDate()) || row[alias] < row.docDate)) {
                def msg7 = row.getCell('docDate').column.name
                def msg = row.getCell(alias).column.name
                def dateFrom = getReportPeriodStartDate()?.format('dd.MM.yyyy')
                def dateTo = getReportPeriodEndDate()?.format('dd.MM.yyyy')
                logger.error("Строка $rowNum: Дата по графе «$msg» должна принимать значение из диапазона $dateFrom - $dateTo и быть больше либо равна дате по графе «$msg7»!")
            }
        }

        // 6. Проверка курса валюты
        ['course2', 'course3'].each { alias ->
            if (row[alias] != null && row[alias] <= 0) {
                def msg = row.getCell(alias).column.name
                logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше «0»!")
            }
        }

        // 7. Проверка даты начала расчетного периода для целейбухгалтерского учета(согласно условиям сделки)
        // 8. Проверка даты окончания расчетного периода для целей бухгалтерского учета (согласно условиям сделки)
        // 9. Проверка даты начала расчетного периода для целей доначисления в налоговом учете
        // 10. Проверка даты окончания расчетного периода для целей доначисления в налоговом учете
        def aliasesMap = [
                'startDate1' : 'endDate1',      // графа 14 - графа 15
                'endDate1'   : 'transDoneDate', // графа 15 - графа 10
                'startDate2' : 'endDate2',      // графа 16 - графа 17
                'endDate2'   : 'taxDoneDate'    // графа 17 - графа 11
        ]
        aliasesMap.each { alias1, alias2 ->
            if (row[alias1] && row[alias2] && row[alias1] > row[alias2]) {
                def msg1 = row.getCell(alias1).column.name
                def msg2 = row.getCell(alias2).column.name
                logger.error("Строка $rowNum: Дата по графе «$msg1» должна быть не больше даты по графе «$msg2»!")
            }
        }

        // 11. Проверка количества дней
        if (row.base != null && row.base < 1) {
            def msg = row.getCell('base').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «1»!")
        }

        // 12. Проверка допустимых значений
        ['dealPay', 'tradePay'].each { alias ->
            if (row[alias] != null && !(row[alias].replaceAll(" ", "") ==~ pattern)) {
                def msg = row.getCell(alias).column.name
                logger.error("Строка $rowNum: Значение графы «%s» должно соответствовать следующему формату: первые символы: (0-9)," +
                        " следующий символ «.», следующие символы (0-9), последний символ %s или пусто!", msg, "(%)")
            }
        }

        def flag23 = calcFlag23(row)
        BigDecimal calcCol23 = calc23(flag23, row)

        // 13. Проверка допустимых целочисленных значений
        def maxValueColumn23 = 100_000_000_000
        if (!flag23 && calcCol23 != null && calcCol23 >= maxValueColumn23) {
            // a.
            def msg = row.getCell('tradePay').column.name
            logger.error("Строка $rowNum: Значение графы «%s» должно быть меньше значения «100 000 000 000»!", msg)

        } else if (flag23 && calcCol23 != null && calcCol23 * 100 >= maxValueColumn23) {
            // b.
            def msg = row.getCell('tradePay').column.name
            logger.error("Строка $rowNum: Значение графы «%s» должно быть меньше значения «100 000 000 000%%»!", msg)
        }

        // 14. Проверка положительной суммы дохода
        ['sum2', 'sum3', 'sum5', 'sum6'].each {
            if (row.getCell(it).value != null && row.getCell(it).value < 0) {
                def msg = row.getCell(it).column.name
                logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
            }
        }

        // 15. Проверка расчётных граф (арифметические проверки)
        def needValue = formData.createDataRow()
        needValue.sum4 = calc22(row)
        needValue.sum5 = calc24(row, flag23, calcCol23)
        needValue.sum8 = calc27(row)
        checkCalc(row, calcColumns, needValue, logger, true)

        // 16. Проверка корректности суммы доначисления  дохода до рыночного уровня по данным налогового учета
        if (row.sum4 && row.sum7 && row.sum8 && row.sum9 && row.sum10) {
            def msg22 = row.getCell('sum4').column.name
            def msg26 = row.getCell('sum7').column.name
            def msg27 = row.getCell('sum8').column.name
            def msg28 = row.getCell('sum9').column.name
            def msg29 = row.getCell('sum10').column.name
            if (getPeriodOrder() == 1 && row.sum10 != row.sum7 - row.sum4 && row.sum10 != row.sum8 - row.sum9) {
                // a
                logger.error("Строка $rowNum: Значение графы «$msg29» должно быть равно разности значений граф «$msg26» и «$msg22» или разности значений граф «$msg27» и «$msg28»!")
            } else if (getPeriodOrder() == 4 && row.sum10 != row.sum7 - row.sum4 && row.sum10 != row.sum8 + row.sum9) {
                // b
                logger.error("Строка $rowNum: Значение графы «$msg29» должно быть равно разности значений граф «$msg26» и «$msg22» или сумме значений граф «$msg27» и «$msg28»!")
            }
        }
    }

    // 17. Проверка итоговых значений пофиксированной строке «Итого»
    if (dataRows.find { it.getAlias() == 'total' }) {
        checkTotalSum(dataRows, totalColumns, logger, true)
    } else {
        logger.error("Отсутствует итоговая строка!")
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // Удаление итогов
    deleteAllAliased(dataRows)

    for (def row in dataRows) {
        def flag23 = calcFlag23(row)
        BigDecimal calcCol23 = calc23(flag23, row)
        // графа 22
        row.sum4 = calc22(row)
        // графа 24
        row.sum5 = calc24(row, flag23, calcCol23)
        // графа 27
        row.sum8 = calc27(row)
        // графы 19, 23
        ['dealPay', 'tradePay'].each { alias ->
            if (row[alias] != null) {
                row[alias] = row[alias].replaceAll(",", ".")
            }
        }
    }

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    updateIndexes(dataRows)
}

def calc22(def row) {
    if (row.sum3 == null) {
        return row.sum2
    } else if (getPeriodOrder() == 1) {
        return row.sum2 - row.sum3
    } else if (getPeriodOrder() == 4) {
        return row.sum2 + row.sum3
    } else {
        return 0
    }
}

def calcFlag23(def row) {
    if (row.tradePay != null) {
        String col23 = row.tradePay.trim()
        return (col23[-1] != "%") ? false : true
    }
}

BigDecimal calc23(def flag23, def row) {
    if (row.tradePay != null && row.tradePay.replaceAll(" ", "") ==~ pattern) {
        String col23 = row.tradePay.replaceAll(/\s/, "").replaceAll(",", ".")
        return flag23 ? round(new BigDecimal(col23[0..-2]) / 100, 8) : // взяли с запасом
                round(new BigDecimal(col23), 6)
    }
}

def calc24(def row, def flag23, def calcCol23) {
    if (row.tradePay != null && row.tradePay.replaceAll(" ", "") ==~ pattern) {
        if (row.sum3 == null) {
            return null
        } else if (!flag23) {
            if (row.course == course810) {
                return calcCol23
            } else {
                return round(calcCol23 * row.course2, 2)
            }
        } else if (flag23 && !(row.base < 1)) {
            if (row.course == course810) {
                return ((BigDecimal) (calcCol23 * row.sum1 * (row.endDate1 - row.startDate1 + 1))).divide(row.base, 2, BigDecimal.ROUND_HALF_UP)
            } else {
                return ((BigDecimal) (calcCol23 * row.sum1 * (row.endDate1 - row.startDate1 + 1) * row.course2)).divide(row.base, 2, BigDecimal.ROUND_HALF_UP)
            }
        } else {
            return 0
        }
    }
}

def calc27(def row) {
    if (row.sum5 == null) {
        return null
    } else {
        return row.sum5 - row.sum2
    }
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
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
    int COLUMN_COUNT = 29
    int HEADER_ROW_COUNT = 4
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
        if (rowValues[INDEX_FOR_SKIP]?.trim()?.equalsIgnoreCase("Итого")) {
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
    if (totalRowFromFile) {
        compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)
        rows.add(totalRow)
    }
    updateIndexes(rows)

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
            ([(headerRows[0][2]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'code')]),
            ([(headerRows[0][6]): 'Первичный документ']),
            ([(headerRows[0][7]): '']),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'sum1')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'course')]),
            ([(headerRows[0][10]): 'Дата операции']),
            ([(headerRows[0][11]): '']),
            ([(headerRows[0][12]): 'Курс Банка России (руб.)']),
            ([(headerRows[0][13]): '']),
            ([(headerRows[0][14]): 'Расчетный период']),
            ([(headerRows[0][15]): '']),
            ([(headerRows[0][16]): '']),
            ([(headerRows[0][17]): '']),
            ([(headerRows[0][18]): getColumnName(tmpRow, 'base')]),
            ([(headerRows[0][19]): getColumnName(tmpRow, 'dealPay')]),
            ([(headerRows[0][20]): 'Сумма фактического дохода, руб.']),
            ([(headerRows[0][21]): '']),
            ([(headerRows[0][22]): '']),
            ([(headerRows[0][23]): getColumnName(tmpRow, 'tradePay')]),
            ([(headerRows[0][24]): 'Рыночная сумма дохода (руб.), соответствующая: ']),
            ([(headerRows[0][25]): '']),
            ([(headerRows[0][26]): '']),
            ([(headerRows[0][27]): 'Сумма доначисления дохода до рыночного уровня (руб.), соответствующая: ']),
            ([(headerRows[0][28]): '']),
            ([(headerRows[0][29]): ''])
    ]
    (2..19).each {
        headerMapping.add([(headerRows[3][it]): it.toString()])
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
        def expectedValues = [ map.INN?.value, map.REG_NUM?.value, map.TAX_CODE_INCORPORATION?.value, map.SWIFT?.value, map.KIO?.value ]
        expectedValues = expectedValues.unique().findAll{ it != null && it != '' }
        formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'iksr'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4
    if (map != null) {
        def countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            def expectedValues = [countryMap.NAME?.stringValue, countryMap.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'countryName'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 5
    newRow.code = values[colIndex]
    colIndex++

    // графа 6
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 7
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 8
    newRow.sum1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.course = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 10
    newRow.transDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.taxDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.course2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
    newRow.course3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 14
    newRow.startDate1 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 15
    newRow.endDate1 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 16
    newRow.startDate2 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 17
    newRow.endDate2 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 18
    newRow.base = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 19
    newRow.dealPay = values[colIndex]
    colIndex++

    // графа 20
    newRow.sum2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 21
    newRow.sum3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 22
    newRow.sum4 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 23
    newRow.tradePay = values[colIndex]
    colIndex++

    // графы 24-29
    ['sum5', 'sum6', 'sum7', 'sum8', 'sum9', 'sum10'].each {
        newRow[it] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

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

    // графа 20
    colIndex = 20
    newRow.sum2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 21
    colIndex = 21
    newRow.sum3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 22
    colIndex = 22
    newRow.sum4 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    colIndex = 24
    ['sum5', 'sum6', 'sum7', 'sum8', 'sum9', 'sum10'].each {
        newRow[it] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    return newRow
}
// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def columns = sortColumns + (allColumns - sortColumns)
    // Сортировка (без подитогов)
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { columns.contains(it.getAlias())})
    sortRows(dataRows, columns)

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void afterLoad() {
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def year = reportPeriod.taxPeriod.year
    def periodName = ""
    switch (reportPeriod.order) {
        case 1 : periodName = "первый квартал"
            break
        case 2 : periodName = "полугодие"
            break
        case 3 : periodName = "9 месяцев"
            break
        case 4 : periodName = "год"
            break
    }
    specialPeriod.name = periodName
    specialPeriod.calendarStartDate = Date.parse("dd.MM.yyyy", "01.01.$year")
    specialPeriod.endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
}
