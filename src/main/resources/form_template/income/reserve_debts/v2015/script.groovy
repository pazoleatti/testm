package form_template.income.reserve_debts.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Форма "Сводный регистр налогового учета по формированию и использованию резерва по сомнительным долгам".
 * Эта форма первичная, не сводная.
 * Очень похожа на РНУ-30.
 *
 * formTemplateId=10618
 *
 * TODO:
 *      - пока не сделана предрасчетная проверка наличия предыдущих форм (не согласовано с заказчиком)
 *      - расчет графы 10 пока не делать (не согласовано с заказчиком)
 *      - потом возможно надо будет добавить условие для включения/исключения графы 10 в редактируемые
 *      - логические проверки 4 и 5 пока не сделаны (не согласовано с заказчиком)
 *      - остались неиспользуемые методы:
 *          getRecordIdImport, getRecordIdImport, getRefBookValue, getReportPeriodEndDate, getReportPeriodStartDate
 *          Убирать пока не стал, потому что возможно некоторые графы станут справочными
 *
 * @author rtimerbaev
 */

// графа 1  - number
// графа    - forLabel - для вывода надписей
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
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
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
    case FormDataEvent.COMPOSE:
        // у этой формы нет консолидации, потому что у нее нет источников
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

// Атрибуты итоговых строк для которых вычисляются суммы (раздел А и Б: графа 10, 11, 14)
@Field
def totalColumnsAB = ['reservePrev', 'reserveCurrent', 'useReserve']

// Атрибуты итоговых строк для которых вычисляются суммы (первые строки: графа 3, 5, 6, 8..14)
@Field
def totalColumns1 = ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum', 'debtOver90DaysReserve', 'totalReserve', 'reservePrev', 'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
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

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex,
                     def String cellName, boolean required) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def newRow = formData.createDataRow()
    def index = 1

    if (currentDataRow == null) {

        // в первые строки
        setEdit(newRow, null)
    } else if (currentDataRow.getIndex() == -1 ||
            'total'.equals(currentDataRow.getAlias()) ||
            isFirstSection(dataRows, currentDataRow)) {

        // в первые строки
        if ('total'.equals(currentDataRow.getAlias())) {
            index = getDataRow(dataRows, 'total').getIndex()
        } else {
            index = currentDataRow.getIndex() + 1
        }
        setEdit(newRow, null)
    } else if (isSection(dataRows, currentDataRow, 'A') ||
            'totalA'.equals(currentDataRow.getAlias()) ||
            'A'.equals(currentDataRow.getAlias())) {

        // в раздел А
        if ('totalA'.equals(currentDataRow.getAlias()) || 'A'.equals(currentDataRow.getAlias())) {
            index = getDataRow(dataRows, 'totalA').getIndex()
        } else {
            index = currentDataRow.getIndex() + 1
        }
        setEdit(newRow, 'A')
    } else if (isSection(dataRows, currentDataRow, 'B') ||
            'totalAll'.equals(currentDataRow.getAlias()) ||
            'totalB'.equals(currentDataRow.getAlias()) ||
            'B'.equals(currentDataRow.getAlias())) {

        // в раздел Б
        if ('totalAll'.equals(currentDataRow.getAlias()) || 'totalB'.equals(currentDataRow.getAlias()) ||
                'B'.equals(currentDataRow.getAlias())) {
            index = getDataRow(dataRows, 'totalB').getIndex()
        } else {
            index = currentDataRow.getIndex() + 1
        }
        setEdit(newRow, 'B')
    }
    dataRowHelper.insert(newRow, index)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalRow = getDataRow(dataRows, 'total')
    if(formDataEvent != FormDataEvent.IMPORT) {
        dataRows.each { row ->
            if (row.getAlias() == null) {
                def isFirstSection = (row.getIndex() < totalRow.getIndex())
                if (isFirstSection) {
                    // графа 4
                    row.debt45_90DaysNormAllocation50per = calc4()
                    // графа 5
                    row.debt45_90DaysReserve = calc5(row)
                    // графа 7
                    row.debtOver90DaysNormAllocation100per = calc7()
                    // графа 8
                    row.debtOver90DaysReserve = calc8(row)
                    // графа 11
                    row.totalReserve = calc9(row)

                    // TODO (Ramil Timerbaev) расчет графы 10 пока не делать
                    // графа 10
                    // row.reservePrev = null

                    // графа 11
                    row.reserveCurrent = calc11(row)
                } else {
                    // TODO (Ramil Timerbaev) расчет графы 10 пока не делать
                    // графа 10
                    // row.reservePrev = null

                    // графа 11
                    row.reserveCurrent = calc11AB(row)
                }
            }
        }
    }

    calcTotal(dataRows)
}

/** Рассчитать итоги основного (первого) раздела, итоги разделов А и Б и строки "всего". */
void calcTotal(def dataRows) {
    def totalRow = getDataRow(dataRows, 'total')
    def totalARow = getDataRow(dataRows, 'totalA')
    def totalBRow = getDataRow(dataRows, 'totalB')

    // очистить старые значения
    totalColumnsAll.each { alias ->
        [totalRow, totalARow, totalBRow].each { row ->
            row.getCell(alias).setValue(null, null)
        }
    }

    // итоги первого раздела
    totalColumns1.each { alias ->
        totalRow.getCell(alias).setValue(getSum(dataRows, alias, totalRow), null)
    }

    // итоги раздела А и Б
    def aRow = getDataRow(dataRows, 'A')
    def bRow = getDataRow(dataRows, 'B')
    totalColumnsAB.each { alias ->
        totalARow.getCell(alias).setValue(getSum(dataRows, alias, aRow, totalARow), null)
        totalBRow.getCell(alias).setValue(getSum(dataRows, alias, bRow, totalBRow), null)
    }

    // расчет строки ВСЕГО
    def totalAllRow = getDataRow(dataRows, 'totalAll')

    totalColumnsAll.each { alias ->
        def tmp = (totalRow.getCell(alias).value ?: 0) +
                (totalARow.getCell(alias).value ?: 0) +
                (totalBRow.getCell(alias).value ?: 0)
        totalAllRow.getCell(alias).setValue(tmp, null)
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // для первых строк - графы 1..14
    def requiredColumns1 = ['numberAccount', 'debt45_90DaysSum',
                        'debt45_90DaysNormAllocation50per', 'debt45_90DaysReserve', 'debtOver90DaysSum',
                        'debtOver90DaysNormAllocation100per', 'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
                        'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']
    // для раздера А и Б - графы 1, 2, 10, 11, 14
    def requiredColumnsAB = ['numberAccount', 'reservePrev', 'reserveCurrent', 'useReserve']

    def totalRow = getDataRow(dataRows, 'total')

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def isFirstSection = (row.getIndex() < totalRow.getIndex())

        // 1. Обязательность заполнения полей
        def nonEmptyColumns = (isFirstSection ? requiredColumns1 : requiredColumnsAB)
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Арифметическая проверка
        def needValue = [:]
        if (isFirstSection) {
            // 2. Арифметическая проверка первых строк (графа 4, 5, 7..9, 11)
            needValue['debt45_90DaysNormAllocation50per'] = calc4()
            needValue['debt45_90DaysReserve'] = calc5(row)
            needValue['debtOver90DaysNormAllocation100per'] = calc7()
            needValue['debtOver90DaysReserve'] = calc8(row)
            needValue['totalReserve'] = calc9(row)
            // TODO (Ramil Timerbaev) расчет графы 10 пока не делать
            // needValue['reservePrev'] = null
            needValue['reserveCurrent'] = calc11(row)

            def arithmeticCheckAlias = needValue.keySet().asList()
            checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
        } else {
            // 2. Арифметическая проверка раздела А и Б (графа 11)
            // TODO (Ramil Timerbaev) расчет графы 10 пока не делать
            // needValue['reservePrev'] = null
            needValue['reserveCurrent'] = calc11AB(row)

            checkCalc(row, ['reserveCurrent'], needValue, logger, true)
        }

        // 4. Проверка наличия строки экземпляра налоговой формы за предыдущий отчетный период
        // TODO (Ramil Timerbaev) логическая проверка 4 пока не делана (не согласовано с заказчиком)

        // 5. Проверка уникальности «Графы 2» в рамках Основного раздела, Раздела А и Раздела Б
        // TODO (Ramil Timerbaev) логическая проверка 5 пока не делана (не согласовано с заказчиком)
    }

    // 3. Проверка итоговых значений (основой раздел, т.е. первая итоговая строка)
    for (def alias : totalColumns1) {
        def value = roundValue(totalRow.getCell(alias).value, 2)
        def tmpValue = roundValue(getSum(dataRows, alias, totalRow), 2)
        if (value != tmpValue) {
            def index = totalRow.getIndex()
            def name = getColumnName(totalRow, alias)
            logger.error("Строка $index: Итоговые значения рассчитаны неверно в графе «$name»!")
        }
    }

    // 3. Проверка итоговых значений (по строкам из раздела А и B)
    def aRow = getDataRow(dataRows, 'A')
    def totalARow = getDataRow(dataRows, 'totalA')

    def bRow = getDataRow(dataRows, 'B')
    def totalBRow = getDataRow(dataRows, 'totalB')

    for (def alias : totalColumnsAB) {
        // раздел А
        def value = roundValue(totalARow.getCell(alias).value, 2)
        def tmpValue = roundValue(getSum(dataRows, alias, aRow, totalARow), 2)
        if (value != tmpValue) {
            def index = totalARow.getIndex()
            def name = getColumnName(totalARow, alias)
            logger.error("Строка $index: Итоговые значения рассчитаны неверно в графе «$name»!")
        }

        // раздел Б
        value = roundValue(totalBRow.getCell(alias).value, 2)
        tmpValue = roundValue(getSum(dataRows, alias, bRow, totalBRow), 2)
        if (value != tmpValue) {
            def index = totalBRow.getIndex()
            def name = getColumnName(totalBRow, alias)
            logger.error("Строка $index: Итоговые значения рассчитаны неверно в графе «$name»!")
        }
    }

    // 3. Проверка итоговых значений (строка ВСЕГО)
    def totalAllRow = getDataRow(dataRows, 'totalAll')
    for (def alias : totalColumnsAll) {
        def value = roundValue(totalAllRow.getCell(alias).value, 2)
        // сумма строк ИТОГО
        def tmpValue = dataRows.sum { row ->
            return (row.getAlias() == null && row.getCell(alias).value ? row.getCell(alias).value : 0)
        }
        tmpValue = roundValue(tmpValue, 2)
        if (value != tmpValue) {
            def index = totalAllRow.getIndex()
            def name = getColumnName(totalAllRow, alias)
            logger.error("Строка $index: Итоговые значения рассчитаны неверно в графе «$name»!")
        }
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

/** Получить сумму графы c первой строки и до указанной. */
def getSum(def dataRows, def columnAlias, def rowEnd) {
    def from = 0
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

/** Проверить принадлежит ли указанная строка к первому разделу (до строки "итого"). */
def isFirstSection(def dataRows, def row) {
    return row != null && row.getIndex() < getDataRow(dataRows, 'total').getIndex()
}

/** Проверить принадлежит ли указанная строка к разделу (A или B). */
def isSection(def dataRows, def row, def section) {
    return row != null &&
            row.getIndex() > getDataRow(dataRows, section).getIndex() &&
            row.getIndex() < getDataRow(dataRows, 'total' + section).getIndex()
}

/**
 * Задать редактируемые графы в зависимости от раздела.
 *
 * @param row строка
 * @param section раздел: А, Б или пустая строка (первые строки)
 */
def setEdit(def row, def section) {
    if (row == null) {
        return
    }
    def editColumns
    // TODO (Ramil Timerbaev) потом возможно надо будет добавить условие для включения/исключения графы 10 в редактируемые
    if (isFirstSection(section)) {
        // первые строки (графа 2, 3, 6, 10(!), 12..14)
        editColumns = ['numberAccount', 'debt45_90DaysSum', 'debtOver90DaysSum', 'reservePrev', 'calcReserve','reserveRecovery', 'useReserve',]
    } else {
        // раздел А или Б (графа 2, 10(!), 14)
        editColumns = ['numberAccount', 'reservePrev', 'useReserve']
    }

    editColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
}

def isFirstSection(def alias) {
    return alias == null || alias == '0' || alias == ''
}

def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

def calc4() {
    return roundValue(50, 0)
}

def calc5(def row) {
    if (row.debt45_90DaysSum == null || row.debt45_90DaysNormAllocation50per == null) {
        return null
    }
    def tmp = row.debt45_90DaysSum * row.debt45_90DaysNormAllocation50per / 100
    return roundValue(tmp, 2)
}

def calc7() {
    return roundValue(100, 0)
}

def calc8(def row) {
    if (row.debtOver90DaysSum == null || row.debtOver90DaysNormAllocation100per == null) {
        return null
    }
    def tmp = row.debtOver90DaysSum * row.debtOver90DaysNormAllocation100per / 100
    return roundValue(tmp, 2)
}

def calc9(def row) {
    if (row.debt45_90DaysReserve == null || row.debtOver90DaysReserve == null) {
        return null
    }
    return roundValue(row.debt45_90DaysReserve + row.debtOver90DaysReserve, 2)
}

def calc14(def row) {
    if (row.totalReserve == null || row.useReserve == null || row.reservePrev == null) {
        return null
    }
    return roundValue((row.totalReserve + row.useReserve > row.reservePrev ?
        row.totalReserve + row.useReserve - row.reservePrev : 0), 2)
}

def calc11(def row) {
    if (row.reservePrev == null || row.calcReserve == null || row.reserveRecovery == null || row.useReserve == null) {
        return null
    }
    def result = row.reservePrev + row.calcReserve - row.reserveRecovery - row.useReserve
    result = (result > 0 ? result : 0)
    return roundValue(result, 2)
}

// для раздела А и Б отдельный метод потому что графа 12 и 13 для этих разделов необязательны и не должны влиять на расчеты.
def calc11AB(def row) {
    if (row.reservePrev == null || row.useReserve == null) {
        return null
    }
    return roundValue(row.reservePrev - row.useReserve, 2)
}

// Удалить нефиксированные строки
void deleteNotFixedRows(def dataRows) {
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        updateIndexes(dataRows)
    }
}

void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def section : ['', 'A', 'B']) {
        def firstRow = section.isEmpty() ? dataRows[0] : getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, "total$section")
        def from = firstRow.getIndex() - (section.isEmpty() ? 1 : 0)
        def to = lastRow.getIndex() - 1
        def sectionRows = (from < to ? dataRows[from..(to - 1)] : [])

        // Массовое разыменовывание граф НФ
        def columnNameList = firstRow.keySet().collect{firstRow.getCell(it).getColumn()}
        refBookService.dataRowsDereference(logger, sectionRows, columnNameList)

        sortRowsSimple(sectionRows)
    }
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
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
    def totalTF = null		// итоговая строка со значениями из тф для добавления
    def mapRows = [:]

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    try {
        // пропускаем заголовок
        rowCells = reader.readNext()
        if (isEmptyCells(rowCells)) {
            logger.error('Первой строкой должен идти заголовок, а не пустая строка')
        }
        // пропускаем пустую строку
        rowCells = reader.readNext()
        if (rowCells == null || !isEmptyCells(rowCells)) {
            logger.error('Вторая строка должна быть пустой')
        }
        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }

            def newRow = getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex)
            if (rowCells.length < 16 || newRow == null) {
                continue
            }
            // определить раздел по техническому полю и добавить строку в нужный раздел
            def sectionIndex = pure(rowCells[15])
            if (mapRows[sectionIndex] == null) {
                mapRows[sectionIndex] = []
            }

            mapRows[sectionIndex].add(newRow)
        }
    } finally {
        reader.close()
    }

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows

    def map = [ '' : '0', 'A' : 'А', 'B' : 'Б']
    def rows = []
    map.keySet().asList().each { section ->
        def firstRow = section.isEmpty() ? null : getDataRow(templateRows, section)
        def lastRow = getDataRow(templateRows, "total$section")
        def copyRows = mapRows[map[section]]

        if (firstRow != null) {
            rows.add(firstRow)
        }
        if (copyRows != null && !copyRows.isEmpty()) {
            rows.addAll(copyRows)

            def totalColumns
            // расчет итогов
            switch (section) {
                case "" : totalColumns = totalColumns1
                    break
                case "A" :
                case "B" : totalColumns = totalColumnsAB
                    break
            }
            calcTotalSum(copyRows, lastRow, totalColumns)
        }
        rows.add(lastRow)
    }
    def totalAll = getDataRow(templateRows, "totalAll")
    rows.add(totalAll)

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
        calcTotalSum(rows, totalAll, totalColumnsAll)

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

    updateIndexes(rows)
    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}'", fileRowIndex))
        return newRow
    }

    def int colOffset = 1
    def int colIndex

    // Техническое поле(группа)
    colIndex = 15
    def section = (pure(rowCells[colIndex]) ?: '')

    // графа 2
    colIndex = 2
    newRow.numberAccount = pure(rowCells[colIndex])

    if (isFirstSection(section)) {
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
    setEdit(newRow, section)

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 15
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'number')
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
    def allValuesCount = allValues.size()

    def isFirstRow = true
    def section = null // название секции
    def mapRows = [:]
    mapRows[section] = []
    def totalRowFromFileMap = [:]

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows
    def aRow = getDataRow(templateRows, 'A')
    def bRow = getDataRow(templateRows, 'B')
    def groupsMap = [
            (aRow.forLabel) : 'A',
            (bRow.forLabel) : 'B'
    ]

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

        def titleValue = rowValues[INDEX_FOR_SKIP]
        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        if (!isFirstRow && groupsMap.get(titleValue) != null) {
            section = groupsMap.get(titleValue)
            mapRows[section] = []
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (titleValue == 'ИТОГО') {
            isFirstRow = false
            totalRowFromFileMap[section] = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, section)
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (titleValue == 'ВСЕГО') {
            totalRowFromFileMap['all'] = getTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }

        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, section)
        mapRows[section].add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // копирование данных по разделам
    def rows = []
    mapRows.keySet().each { sectionKey ->
        def headRow = (sectionKey ? getDataRow(templateRows, sectionKey) : null)
        def totalRow = getDataRow(templateRows, 'total' + (sectionKey ?: ''))
        if (headRow) {
            rows.add(headRow)
        }
        def copyRows = mapRows[sectionKey]
        if (copyRows != null && !copyRows.isEmpty()) {
            rows.addAll(copyRows)
        }
        rows.add(totalRow)

        // сравнение итогов ИТОГО
        updateIndexes(rows)
        def totalRowFromFile = totalRowFromFileMap[sectionKey]
        def columns = (sectionKey ? totalColumns1 : totalColumnsAB)
        compareSimpleTotalValues(totalRow, totalRowFromFile, copyRows, columns, formData, logger, false)
    }
    def totalAll = getDataRow(templateRows, 'totalAll')
    rows.add(totalAll)
    updateIndexes(rows)

    // сравнение итогов ВСЕГО
    if (totalRowFromFileMap['all']) {
        def tmpRows = mapRows.values().sum { it } ?: []
        compareSimpleTotalValues(totalAll, totalRowFromFileMap['all'], tmpRows, totalColumnsAll, formData, logger, false)
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
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
            ([(headerRows[0][0]): getColumnName(tmpRow, 'number')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'numberAccount')]),
            ([(headerRows[0][3]): 'Задолженность от 45 до 90 дней']),
            ([(headerRows[1][3]): 'сумма долга']),
            ([(headerRows[1][4]): 'норматив отчислений 50%']),
            ([(headerRows[1][5]): 'расчётный резерв']),
            ([(headerRows[0][6]): 'Задолженность более 90 дней']),
            ([(headerRows[1][6]): 'сумма долга']),
            ([(headerRows[1][7]): 'норматив отчислений 100%']),
            ([(headerRows[1][8]): 'расчётный резерв']),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'totalReserve')]),
            ([(headerRows[0][10]): 'Резерв']),
            ([(headerRows[1][10]): 'на предыдущую отчётную дату']),
            ([(headerRows[1][11]): 'на отчетную дату']),
            ([(headerRows[0][12]): 'Изменение фактического резерва']),
            ([(headerRows[1][12]): 'Доначисление резерва с отнесением на расходы код 22670']),
            ([(headerRows[1][13]): 'Восстановление резерва на доходах код 13091']),
            ([(headerRows[1][14]): 'Использование резерва на погашение процентов по безнадежным долгам в отчетном периоде']),
            ([(headerRows[2][0]): '1'])
    ]
    (2..14).each { index ->
        headerMapping.add(([(headerRows[2][index]): index.toString()]))
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
 * @param section раздел (A, B или null/пустая строка)
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def section) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    // редактируемые(импортируемые) графы:
    // первые строки  (графа 2, 3, 6, 10(!), 12..14)
    // раздел А или Б (графа 2,       10(!),     14)
    setEdit(newRow, section)

    // графа 2
    def colIndex = 2
    newRow.numberAccount = values[colIndex]

    if (isFirstSection(section)) {
        // графа 3..14
        ['debt45_90DaysSum', 'debt45_90DaysNormAllocation50per', 'debt45_90DaysReserve', 'debtOver90DaysSum',
                'debtOver90DaysNormAllocation100per', 'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
                'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve'].each { alias ->
            colIndex++
            newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        }
    } else {
        // графа 10
        colIndex = 10
        newRow.reservePrev = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

        // графа 11
        colIndex = 11
        newRow.reserveCurrent = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

        // графа 14
        colIndex = 14
        newRow.useReserve = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

/**
 * Получить итоговую строку ВСЕГО по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getTotalRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 3
    def colIndex = 3
    newRow.debt45_90DaysSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex = 5
    newRow.debt45_90DaysReserve = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6
    colIndex = 6
    newRow.debtOver90DaysSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 8..14
    colIndex = 7
    ['debtOver90DaysReserve', 'totalReserve', 'reservePrev', 'reserveCurrent',
            'calcReserve', 'reserveRecovery', 'useReserve'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}