package form_template.income.reserve_debts_1.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Форма "Сводный регистр налогового учета по формированию и использованию резерва по сомнительным долгам (с периода год 2015)".
 * Эта форма первичная, не сводная.
 * TODO проверка 7 не проходит, похоже что некорректна.
 * TODO тесты
 *
 * formTemplateId=847
 *
 * @author bkinzyabulatov
 */

// графа    - fix - для вывода надписей
// графа 1  - number
// графа 2  - numberAccount
// графа 3  - debt45_90DaysSum
// графа 4  - debt45_90DaysNormAllocation50per
// графа 5  - debt45_90DaysReserve
// графа 6  - debtOver90DaysSum
// графа 7  - debtOver90DaysNormAllocation100per
// графа 8  - debtOver90DaysReserve
// графа 9  - totalReserve
// графа 10 - reservePrev
// графа 11 - reserveCurrent
// графа 12 - calcReserve
// графа 13 - reserveRecovery
// графа 14 - useReserve

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED: // проверка при "вернуть из принята в подготовлена"
        break
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED: // после принятия из подготовлена
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Атрибуты итоговых строк для которых вычисляются суммы (всего: графа 3, 5, 6, 8, 9, 12, 13 + графы 10, 11, 14)
@Field
def totalColumnsAll = ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum', 'debtOver90DaysReserve',
        'totalReserve', 'reservePrev', 'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']

// Атрибуты итоговых строк для которых вычисляются суммы (раздел Б: графа 10, 11, 14)
@Field
def totalColumnsB = ['reservePrev', 'reserveCurrent', 'useReserve']

// Атрибуты итоговых строк для которых вычисляются суммы (первые строки: графа 3, 5, 6, 8..14)
@Field
def totalColumnsA = ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum', 'debtOver90DaysReserve', 'totalReserve', 'reservePrev', 'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']

@Field
def endDate = null

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalA = getDataRow(dataRows, 'totalA')
    dataRows.each { row ->
        if (row.getAlias() == null) {
            if (row.getIndex() < totalA.getIndex()) {
                // графа 4
                row.debt45_90DaysNormAllocation50per = calc4(row)
                // графа 7
                row.debtOver90DaysNormAllocation100per = calc7(row)
                // графа 8
                row.debtOver90DaysReserve = calc8(row)
                // графа 9
                row.totalReserve = calc9(row)
                // графа 11
                row.reserveCurrent = calc11(row, true, LogLevel.WARNING)
            } else {
                // графа 11
                row.reserveCurrent = calc11B(row)
            }
        }
    }

    calcTotal(dataRows)
}

/** Рассчитать итоги разделов А и Б и строки "всего". */
void calcTotal(def dataRows) {
    def totalARow = getDataRow(dataRows, 'totalA')
    def totalBRow = getDataRow(dataRows, 'totalB')

    // очистить старые значения
    totalColumnsAll.each { alias ->
        [totalARow, totalBRow].each { row ->
            row.getCell(alias).setValue(null, null)
        }
    }

    // итоги раздела А и Б
    def aRow = getDataRow(dataRows, 'A')
    def bRow = getDataRow(dataRows, 'B')
    totalColumnsA.each { alias ->
        totalARow.getCell(alias).setValue(getSum(dataRows, alias, aRow, totalARow), null)
    }
    totalColumnsB.each { alias ->
        totalBRow.getCell(alias).setValue(getSum(dataRows, alias, bRow, totalBRow), null)
    }

    // расчет строки ВСЕГО
    def totalAllRow = getDataRow(dataRows, 'totalAll')

    totalColumnsAll.each { alias ->
        def tmp = (totalARow.getCell(alias).value ?: 0) +
                (totalBRow.getCell(alias).value ?: 0)
        totalAllRow.getCell(alias).setValue(tmp, null)
    }
}

void logicCheck() {
    boolean nonCalcEvent = formDataEvent != FormDataEvent.CALCULATE
    def logLevel = nonCalcEvent ? LogLevel.ERROR : LogLevel.WARNING
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // для раздела А - графы 1..14
    def requiredColumnsA = ['numberAccount', 'debt45_90DaysSum',
                        'debt45_90DaysNormAllocation50per', 'debt45_90DaysReserve', 'debtOver90DaysSum',
                        'debtOver90DaysNormAllocation100per', 'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
                        'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']
    // для раздера Б - графы 1, 2, 10, 11, 14
    def requiredColumnsB = ['numberAccount', 'reservePrev', 'reserveCurrent', 'useReserve']

    def totalARow = getDataRow(dataRows, 'totalA')
    def totalBRow = getDataRow(dataRows, 'totalB')
    def aRow = getDataRow(dataRows, 'A')
    def bRow = getDataRow(dataRows, 'B')

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def isASection = (row.getIndex() < totalARow.getIndex())
        def isBSection = (row.getIndex() > bRow.getIndex() && row.getIndex() < totalBRow.getIndex())

        // 1. Обязательность заполнения полей
        def nonEmptyColumns = []
        if (isASection) {
            nonEmptyColumns = requiredColumnsA
        }
        if (isBSection) {
            nonEmptyColumns = requiredColumnsB
        }
        if (isASection || isBSection) {
            checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
        }

        // 2. Арифметическая проверка значений автоматически заполняемых граф
        def needValue = [:]
        if (isASection) {
            // 2. Арифметическая проверка первых строк (графа 4, 7..9, 11)
            needValue['debt45_90DaysNormAllocation50per'] = calc4(row)
            needValue['debtOver90DaysNormAllocation100per'] = calc7(row)
            needValue['debtOver90DaysReserve'] = calc8(row)
            needValue['totalReserve'] = calc9(row)
            // 7. Проверка значения, рассчитанного по графе 11
            needValue['reserveCurrent'] = calc11(row, nonCalcEvent, logLevel)

            def arithmeticCheckAlias = needValue.keySet().asList()
            checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
        } else {
            // 2. Арифметическая проверка раздела Б (графа 11)
            needValue['reserveCurrent'] = calc11B(row)
            checkCalc(row, ['reserveCurrent'], needValue, logger, true)
        }
    }

    // 3. Проверка итоговых значений (по строкам из раздела А и B)
    boolean totalAError = false
    for (def alias : totalColumnsA) {
        // раздел А
        def value = roundValue(totalARow.getCell(alias).value, 2)
        def tmpValue = roundValue(getSum(dataRows, alias, aRow, totalARow), 2)
        if (value != tmpValue) {
            totalAError = true
            logger.error("Строка ${totalARow.getIndex()}: Итоговые значения рассчитаны неверно в графе «${getColumnName(totalARow, alias)}»!")
        }
    }

    boolean totalBError = false
    for (def alias : totalColumnsB) {
        // раздел Б
        value = roundValue(totalBRow.getCell(alias).value, 2)
        def tmpValue = roundValue(getSum(dataRows, alias, bRow, totalBRow), 2)
        if (value != tmpValue) {
            totalBError = true
            logger.error("Строка ${totalBRow.getIndex()}: Итоговые значения рассчитаны неверно в графе «${getColumnName(totalBRow, alias)}»!")
        }
    }

    // 3. Проверка итоговых значений (строка ВСЕГО)
    boolean totalAllError = false
    def totalAllRow = getDataRow(dataRows, 'totalAll')
    for (def alias : totalColumnsAll) {
        def value = roundValue(totalAllRow.getCell(alias).value, 2)
        // сумма строк ИТОГО
        def tmpValue = (totalARow[alias] ?: 0) + (totalBRow[alias] ?: 0)
        tmpValue = roundValue(tmpValue, 2)
        if (value != tmpValue) {
            totalAllError = true
            logger.error("Строка ${totalAllRow.getIndex()}: Итоговые значения рассчитаны неверно в графе «${getColumnName(totalAllRow, alias)}»!")
        }
    }

    // 4. Контрольная проверка по графе 11 итоговой строки раздела А
    if (!totalAError && (totalARow.reserveCurrent != calc11(totalARow, false, null))) {
        logger.log(logLevel, "Строка ${totalARow.getIndex()}: Графа «${getColumnName(totalARow, 'reserveCurrent')}» (раздел А) не равна значению, рассчитанному по формуле: «Графа 10» + «Графа 12» – «Графа 13» – «Графа 14»!")
    }
    // 5. Контрольная проверка по графе 11 итоговой строки раздела Б
    if (!totalBError && (totalBRow.reserveCurrent != calc11B(totalBRow))) {
        logger.log(logLevel, "Строка ${totalBRow.getIndex()}: Графа «${getColumnName(totalBRow, 'reserveCurrent')}» (раздел Б) не равна значению, рассчитанному по формуле: «Графа 10» + «Графа 14»!")
    }
    // 6. Контрольная проверка по графе 11 итоговой строки общего итога
    if (!totalAllError && (totalAllRow.reserveCurrent != calc11(totalAllRow, false, null))) {
        logger.log(logLevel, "Строка ${totalAllRow.getIndex()}: Графа «${getColumnName(totalAllRow, 'reserveCurrent')}» (всего) не равна значению, рассчитанному по формуле: «Графа 10» + «Графа 12» – «Графа 13» – «Графа 14»!")
    }
}

/** Получить сумму графы в указанном диапозоне строк. */
def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

def calc4(def row) {
    return roundValue(50, row.getCell('debt45_90DaysNormAllocation50per').getColumn().precision)
}

def calc7(def row) {
    return roundValue(100, row.getCell('debtOver90DaysNormAllocation100per').getColumn().precision)
}

def calc8(def row) {
    if (row.debtOver90DaysSum == null || row.debtOver90DaysNormAllocation100per == null) {
        return null
    }
    def tmp = row.debtOver90DaysSum * row.debtOver90DaysNormAllocation100per / 100
    return roundValue(tmp, row.getCell('debtOver90DaysReserve').getColumn().precision)
}

def calc9(def row) {
    if (row.debt45_90DaysReserve == null || row.debtOver90DaysReserve == null) {
        return null
    }
    return roundValue(row.debt45_90DaysReserve + row.debtOver90DaysReserve, row.getCell('totalReserve').getColumn().precision)
}

def calc11(def row, boolean performCheck, def logLevel) {
    if (row.reservePrev == null || row.calcReserve == null || row.reserveRecovery == null || row.useReserve == null) {
        return null
    }
    def result = row.reservePrev + row.calcReserve - row.reserveRecovery - row.useReserve
    if (result < 0) {
        result = 0
        if (performCheck) {
            logger.log(logLevel, "Строка ${row.number}: Значение, рассчитанное по графе «${getColumnName(row, 'reserveCurrent')}» («Графа 10» + «Графа 12» – «Графа 13» – «Графа 14»), меньше 0!")
        }
    }
    return roundValue(result, row.getCell('reserveCurrent').getColumn().precision)
}

// для раздела Б отдельный метод потому что графа 12 и 13 необязательны и не должны влиять на расчеты.
def calc11B(def row) {
    if (row.reservePrev == null || row.useReserve == null) {
        return null
    }
    return roundValue(row.reservePrev + row.useReserve, row.getCell('reserveCurrent').getColumn().precision)
}

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 14
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def mapRows = [:]

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows
    def totalTF = null		// итоговая строка со значениями из тф для добавления

    try {
        // проверить первые строки тф - заголовок и пустая строка
        checkFirstRowsTF(reader, logger)

        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    totalTF = formData.createStoreMessagingDataRow()
                    totalTF = getNewRow(totalTF, rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }

            def templateRow = templateRows[rowIndex]
            templateRow = getNewRow(templateRow, rowCells, COLUMN_COUNT, fileRowIndex, rowIndex)
            if (rowCells.length < 16 || templateRow == null) {
                continue
            }
            // определить раздел по техническому полю и добавить строку в нужный раздел
            def sectionIndex = pure(rowCells[15])
            if (mapRows[sectionIndex] == null) {
                mapRows[sectionIndex] = []
            }

            mapRows[sectionIndex].add(templateRow)
        }
    } finally {
        reader.close()
    }

    def map = [ 'A' : 'А', 'B' : 'Б']
    def rows = []
    map.keySet().asList().each { section ->
        def lastRow = getDataRow(templateRows, "total$section")
        def copyRows = mapRows[map[section]]

        if (copyRows != null && !copyRows.isEmpty()) {
            def totalColumns = null
            // расчет итогов
            switch (section) {
                case "A" : totalColumns = totalColumnsA
                    break
                case "B" : totalColumns = totalColumnsB
                    break
            }
            calcTotalSum(copyRows, lastRow, totalColumns)
        }
    }
    def totalAll = getDataRow(templateRows, "totalAll")

    // сравнение итогов
    if (totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = [
                'debt45_90DaysSum'      : 3,
                'debt45_90DaysReserve'  : 5,
                'debtOver90DaysSum'     : 6,
                'debtOver90DaysReserve' : 8,
                'totalReserve'          : 9,
                'reservePrev'           : 10,
                'reserveCurrent'        : 11,
                'calcReserve'           : 12,
                'reserveRecovery'       : 13,
                'useReserve'            : 14
        ]

        // подсчет итогов
        calcTotalSum(templateRows, totalAll, totalColumnsAll)

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
            def v2 = totalAll.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR + " Из файла: $v1, рассчитано: $v2", totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
        // задать кварталаьной итоговой строке нф значения из итоговой строки тф
        totalColumnsAll.each { alias ->
            totalAll[alias] = totalTF[alias]
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
        // очистить итоги
        totalColumnsAll.each { alias ->
            totalAll[alias] = null
        }
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param newRow
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(def newRow, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    newRow.keySet().each { newRow.getCell(it).setCheckMode(true) }
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}'", fileRowIndex))
    }

    def int colOffset = 1
    def int colIndex

    // Техническое поле(группа)
    colIndex = 15
    def section = (pure(rowCells[colIndex]) ?: '')

    // графа 2
    colIndex = 2
    newRow.numberAccount = pure(rowCells[colIndex])

    if (section == 'A') {
        // графа 3..14
        ['debt45_90DaysSum', 'debt45_90DaysNormAllocation50per', 'debt45_90DaysReserve', 'debtOver90DaysSum',
                'debtOver90DaysNormAllocation100per', 'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
                'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve'].each { alias ->
            colIndex++
            newRow[alias] = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
        }
    } else {
        // графа 10
        colIndex = 10
        newRow.reservePrev = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

        // графа 11
        colIndex = 11
        newRow.reserveCurrent = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

        // графа 14
        colIndex = 14
        newRow.useReserve = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    }
    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 15
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('number').column.name
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
    def allValuesCount = allValues.size()

    def section = null // название секции
    def mapRows = [:]
    mapRows[section] = []
    def totalRowFromFileMap = [:]

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows
    def aRow = getDataRow(templateRows, 'A')
    def bRow = getDataRow(templateRows, 'B')
    def vRow = getDataRow(templateRows, 'V')
    def groupsMap = [
            (aRow.fix) : 'A',
            (bRow.fix) : 'B',
            (vRow.fix) : 'V'
    ]

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

        def titleValue = rowValues[INDEX_FOR_SKIP]
        def templateRow = templateRows[i]
        if (templateRow != null) {
            if (templateRow.fix != null && templateRow.fix != titleValue) {
                throw new ServiceException("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
            }
        }
        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        if (groupsMap.get(titleValue) != null) {
            section = groupsMap.get(titleValue)
            mapRows[section] = []
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (titleValue != null && titleValue.contains('ИТОГО')) {
            isFirstRow = false
            fillSimpleRowFromXls(templateRow, rowValues, colOffset, fileRowIndex, rowIndex, section)
            totalRowFromFileMap[section] = templateRow
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (titleValue == 'ВСЕГО') {
            fillTotalRowFromXls(templateRow, rowValues, colOffset, fileRowIndex, rowIndex)
            totalRowFromFileMap['all'] = templateRow
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }

        // простая строка
        rowIndex++
        fillSimpleRowFromXls(templateRow, rowValues, colOffset, fileRowIndex, rowIndex, section)
        mapRows[section].add(templateRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // копирование данных по разделам
    mapRows.keySet().each { sectionKey ->
        def copyRows = mapRows[sectionKey]
        // сравнение итогов ИТОГО
        def totalRowTemp = formData.createDataRow()
        def totalRowFromFile = totalRowFromFileMap[sectionKey]
        def columns = (sectionKey == 'A' ? totalColumnsA : totalColumnsB)
        compareSimpleTotalValues(totalRowTemp, totalRowFromFile, copyRows, columns, formData, logger, false)
    }

    def totalAllTemp = formData.createDataRow()
    // сравнение итогов ВСЕГО
    if (totalRowFromFileMap['all']) {
        def tmpRows = mapRows.values().sum { it } ?: []
        compareSimpleTotalValues(totalAllTemp, totalRowFromFileMap['all'], tmpRows, totalColumnsAll, formData, logger, false)
    }

    showMessages(templateRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = templateRows
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
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]): tmpRow.getCell('number').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('numberAccount').column.name]),
            ([(headerRows[0][3]): 'Задолженность от 45 до 90 дней']),
            ([(headerRows[1][3]): 'Сумма долга']),
            ([(headerRows[1][4]): 'Норматив отчислений 50%']),
            ([(headerRows[1][5]): 'Расчетный резерв']),
            ([(headerRows[0][6]): 'Задолженность более 90 дней']),
            ([(headerRows[1][6]): 'Сумма долга']),
            ([(headerRows[1][7]): 'Норматив отчислений 100%']),
            ([(headerRows[1][8]): 'Расчетный резерв']),
            ([(headerRows[0][9]): tmpRow.getCell('totalReserve').column.name]),
            ([(headerRows[0][10]): 'Резерв']),
            ([(headerRows[1][10]): 'на предыдущую отчетную дату']),
            ([(headerRows[1][11]): 'на отчетную дату']),
            ([(headerRows[0][12]): 'Изменение фактического резерва']),
            ([(headerRows[1][12]): 'Доначисление резерва с отнесением на расходы Банка']),
            ([(headerRows[1][13]): 'Восстановление резерва на доходах Банка']),
            ([(headerRows[1][14]): 'Использование резерва на покрытие убытков Банка от безнадежных долгов в отчетном периоде'])
    ]
    (1..14).each { index ->
        headerMapping.add(([(headerRows[2][index]): index.toString()]))
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param newRow строка
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 * @param section раздел (A, B или null/пустая строка)
 */
void fillSimpleRowFromXls(def newRow, def values, def colOffset, def fileRowIndex, def rowIndex, def section) {
    newRow.keySet().each { newRow.getCell(it).setCheckMode(true) }
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    // редактируемые(импортируемые) графы:
    // раздел А (графа 3, 5, 6, 10, 12-14)
    // раздел Б (графа          10,    14)

    def tmpValues = [:]
    // графа 2
    def colIndex = 2
    tmpValues['numberAccount'] = values[colIndex]

    // графа 3..14
    def columnsA = ['debt45_90DaysSum', 'debt45_90DaysNormAllocation50per', 'debt45_90DaysReserve', 'debtOver90DaysSum',
                    'debtOver90DaysNormAllocation100per', 'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
                    'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']
    def columnsB = ['reservePrev', 'reserveCurrent', 'useReserve']
    def fillColumns = []
    if (section == 'A') {
        fillColumns = columnsA
    }
    if (section == 'B') {
        fillColumns = columnsB
    }
    columnsA.each { alias ->
        colIndex++
        if (fillColumns.contains(alias)) {
            newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        } else {
            tmpValues[alias] = values[colIndex]
        }
    }

    tmpValues.keySet().asList().each { alias ->
        def value = StringUtils.cleanString(tmpValues[alias]?.toString())
        def valueExpected = StringUtils.cleanString(newRow.getCell(alias).value?.toString())
        checkFixedValue(newRow, value, valueExpected, newRow.getIndex(), alias, logger, true)
    }
}

/**
 * Получить итоговую строку ВСЕГО по значениям из экселя.
 *
 * @param totalRow строка шаблона
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
void fillTotalRowFromXls(def totalRow, def values, def colOffset, def fileRowIndex, def rowIndex) {
    totalRow.keySet().each { totalRow.getCell(it).setCheckMode(true) }
    totalRow.setIndex(rowIndex)
    totalRow.setImportIndex(fileRowIndex)

    // графа 3
    def colIndex = 3
    totalRow.debt45_90DaysSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex = 5
    totalRow.debt45_90DaysReserve = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6
    colIndex = 6
    totalRow.debtOver90DaysSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 8..14
    colIndex = 7
    ['debtOver90DaysReserve', 'totalReserve', 'reservePrev', 'reserveCurrent',
            'calcReserve', 'reserveRecovery', 'useReserve'].each { alias ->
        colIndex++
        totalRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
}