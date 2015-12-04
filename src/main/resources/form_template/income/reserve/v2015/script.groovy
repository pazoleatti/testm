package form_template.income.reserve.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * 614 - (РСД) Расчет резерва по сомнительным долгам в целях налогообложения.
 *
 * formTemplateId=1614
 */

// графа  1 - rowNum			- № п/п
// графа  2 - bankName			- Наименование банка
// графа  3 - sum45			    - Задолженность от 45 до 90 дней. Сумма долга
// графа  4 - norm45			- Задолженность от 45 до 90 дней. Норматив отчислений 50%
// графа  5 - reserve45			- Задолженность от 45 до 90 дней. Расчетный резерв
// графа  6 - sum90			    - Задолженность свыше 90 дней. Сумма долга
// графа  7 - norm90			- Задолженность свыше 90 дней. Норматив отчислений 100%
// графа  8 - reserve90			- Задолженность свыше 90 дней. Расчетный резерв
// графа  9 - totalReserve		- Итого расчетный резерв гр.9=гр.5+гр.8
// графа 10 - sumIncome			- Сумма доходов за отчетный период
// графа 11 - normIncome		- Норматив отчислений от суммы доходов 10%
// графа 12 - valueReserve		- Величина созданного резерва в отчетном периоде
// графа 13 - reservePrev		- Резерв на предыдущую отчетную дату
// графа 14 - reserveCurrent	- Резерв на отчетную дату
// графа 15 - addChargeReserve	- Изменение фактического резерва. Доначисление резерва с отнесением на расходы код 22670
// графа 16 - restoreReserve	- Изменение фактического резерва. Восстановление резерва на доходах код 13091
// графа 17 - usingReserve		- Изменение фактического резерва. Использование резерва на погашение процентов по безнадежным долгам в отчетном периоде

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        preCalcLogicCheck()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
}

@Field
def allColumns = ['rowNum', 'bankName', 'sum45', 'norm45', 'reserve45', 'sum90', 'norm90', 'reserve90',
                  'totalReserve', 'sumIncome', 'normIncome', 'valueReserve', 'reservePrev', 'reserveCurrent',
                  'addChargeReserve', 'restoreReserve', 'usingReserve']

// графы для очистки или не имеют фиксированных значений (графа 3..17)
@Field
def clearColumns = allColumns - ['rowNum', 'bankName']

// неконсолидируемые графы 4, 7, 10..12
@Field
def notConsolidationColumns = ['norm45', 'norm90', 'sumIncome', 'normIncome', 'valueReserve']

// певичная "Сводный регистр налогового учета по формированию и использованию резерва по сомнительным долгам"
@Field
def sourceFormTypeId = 618

// Мапа для хранения номеров строк и id подразделений (номер строки -> идентификатор подразделения)
@Field
def departmentRowMap = [
        3  : 103,
        4  : 98,
        5  : 5,
        6  : 17,
        7  : 89,
        8  : 9,
        9  : 83,
        10 : 33,
        11 : 110,
        12 : 45,
        13 : 65,
        14 : 73,
        15 : 28,
        16 : 21,
        17 : 53,

        20 : 176,
        21 : 131,
        22 : 115,
        23 : 117
]

// тип источника
@Field
def sourceFormType = null

// форма предыдущего периода
@Field
def prevFormData = null

// строки предыдущего период
@Field
def prevDataRows = null

@Field
def startDate = null

@Field
def endDate = null

@Field
def editableStyle = 'Редактируемая'

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

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def prevDataRows = getPrevDataRows()
    calcRows(dataRows, prevDataRows)
}

/**
 * Расчитать строки.
 * Расчеты идут не по чтз, потому что есть зависимости.
 *
 * @param dataRows строки над которыми будут производится расчеты, изменения будут записаны в него же
 * @param prevDataRows строки из предыдущей фомры
 */
void calcRows(def dataRows, def prevDataRows) {
    // очистить
    clearCalcValues(dataRows)


    // Строки 3-17 (по ТБ), строки 20-23 (подразделения ЦА)
    (dataRows[2..16] + dataRows[19..22]).each { row ->
        // графа 4
        row.norm45 = roundValue(50)
        // графа 7
        row.norm90 = roundValue(100)
        // графа 12
        row.valueReserve = roundValue(row.reserveCurrent)
    }


    // Строки 25-44 (Списание безнадежных долгов)
    dataRows[24..43].each { row ->
        // графа 13
        row.reservePrev = calc13(row, prevDataRows)
        // графа 14
        row.reserveCurrent = null
        if (row.reservePrev != null && row.usingReserve != null) {
            row.reserveCurrent = roundValue(row.reservePrev + row.usingReserve)
        }
    }


    // Строка 18 (Центральный аппарат)
    def rowCA = dataRows[17]
    // графа 4
    rowCA.norm45 = roundValue(50)
    // графа 7
    rowCA.norm90 = roundValue(100)
    // графа 3, 5, 6, 8, 9, 12-17
    def row18Columns = ['sum45', 'reserve45', 'sum90', 'reserve90', 'totalReserve', 'valueReserve', 'reservePrev', 'reserveCurrent', 'addChargeReserve', 'restoreReserve', 'usingReserve']
    row18Columns.each { column ->
        def tmp = dataRows[19..22].sum { row -> row[column] ?: 0 }
        rowCA[column] = roundValue(tmp)
    }

    // Строка 45 (ЦА с учетом списания безнадежных долгов ТБ и подразделений банка)
    def rowCAWriteoff = dataRows[44]
    // графы 3, 5, 6, 8, 9, 12, 15, 16
    def row45Columns = ['sum45', 'reserve45', 'sum90', 'reserve90', 'totalReserve', 'valueReserve', 'addChargeReserve', 'restoreReserve']
    row45Columns.each { column ->
        def tmp =  dataRows[20..23].sum { row -> row[column] ?: 0 }
        rowCAWriteoff[column] = roundValue(tmp)
    }
    // графа 13
    rowCAWriteoff.reservePrev = calc13(rowCAWriteoff, prevDataRows)
    // графа 14
    def tmp1 = dataRows[19..22].sum { row -> row.reserveCurrent ?: 0 }
    def tmp2 = dataRows[24..43].sum { row -> row.reserveCurrent ?: 0 }
    rowCAWriteoff.reserveCurrent = roundValue(tmp1 - tmp2)
    // графа 17
    tmp1 = dataRows[19..22].sum { row -> row.usingReserve ?: 0 }
    tmp2 = dataRows[24..43].sum { row -> row.usingReserve ?: 0 }
    rowCAWriteoff.usingReserve = roundValue(tmp1 + tmp2)


    // Строка 1 (Сбербанк России)
    def rowBank = dataRows[0]
    def tmpRows = dataRows[2..16] + dataRows[44]
    // графа 3, 5, 6, 8, 9, 12, 15, 16
    def row1Columns = ['sum45', 'reserve45', 'sum90', 'reserve90', 'totalReserve', 'valueReserve', 'addChargeReserve', 'restoreReserve']
    row1Columns.each { column ->
        def tmp = dataRows[2..16].sum { row -> row[column] ?: 0 }
        rowBank[column] = roundValue(tmp)
    }
    // графа 4
    rowBank.norm45 = roundValue(50)
    // графа 7
    rowBank.norm90 = roundValue(100)
    // графа 11
    rowBank.normIncome = (rowBank.sumIncome != null ? roundValue(rowBank.sumIncome * 0.1) : null)
    // графа 13
    rowBank.reservePrev = calc13(rowBank, prevDataRows)
    // графа 14
    def tmp = tmpRows.sum { it.reserveCurrent ?: 0 }
    rowBank.reserveCurrent = roundValue(tmp > 0 ? tmp : 0)
    // графа 17
    tmp = tmpRows.sum { it.usingReserve ?: 0 }
    rowBank.usingReserve = roundValue(tmp)
}

/** Очистить расчитываемые значения, пропуская консолидированные и редактируемые ячейки. */
void clearCalcValues(def dataRows) {
    def index = 0
    // номера строк заполняемых при консолидации
    def concolidateRowIndexes = departmentRowMap.keySet().asList()
    for (def row : dataRows) {
        index++
        if (index in concolidateRowIndexes) {
            // очистка значении строк, которые незаполняются при консолидации
            notConsolidationColumns.each { alias ->
                row[alias] = null
            }
        } else {
            // очистка значении остальных строк, кроме редактируемых (графа 3..17)
            def columns = clearColumns
            if (index == 1) {
                // для строки 1 исключить графу 10 и 13
                columns = columns - ['sumIncome', 'reservePrev']
            } else if (25 <= index || index <= 44) {
                // для строки 1 исключить графу 13 и 17
                columns = columns - ['reservePrev', 'usingReserve']
            }
            columns.each { column ->
                row[column] = null
            }
        }
    }
}

/** Очистить нередактируемые значения, кроме надписей. */
void clearNotEditableValues(def dataRows) {
    dataRows.each { row ->
        clearColumns.each { alias ->
            if (row.getCell(alias)?.style?.alias != editableStyle) {
                row[alias] = null
            }
        }
    }
}

/**
 * Получить значения для графы 13. Для строк 1, 25..44 и 45.
 *
 * @param row строка текущей формы
 * @param prevDataRows строки предыдущего периода
 */
def calc13(def row, def prevDataRows) {
    if (prevDataRows) {
        for (def prevRow : prevDataRows) {
            if (row.getAlias() == prevRow.getAlias()) {
                return roundValue(prevRow.reserveCurrent)
            }
        }
        def index = row.getIndex()
        def name = getColumnName(row, 'reservePrev')
        logger.warn("Строка $index: В экземпляре формы текущего типа, вида и подразделения за предыдущий отчетный период отсутствует данная фиксированная строка. Графа «$name» автоматически не заполнена.")
    }
    return roundValue(row.reservePrev)
}

void preCalcLogicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // 1. Проверка назначения источником данных форм: первчиные по сомнительным долгам
    def departmentFormTypes = departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate())
    departmentRowMap.each { index, departmentId ->
        def row = dataRows[index - 1]
        def departmentFormType = departmentFormTypes.find {
            // совпадает подразделение и тип формы
            it.departmentId == departmentId && it.formTypeId == sourceFormTypeId
        }
        if (departmentFormType == null) {
            // не назначена источником данных
            def rowIndex = row.getIndex()
            def sourceName = getSourceFormType()?.name
            def bankName = row.bankName
            def departmentName = departmentService.get(departmentId)?.name
            logger.warn("Строка $rowIndex: Не назначена источником налоговая форма «$sourceName» для $bankName (подразделение «$departmentName»)! " +
                    "Графы текущей строки, заполняемые данными формы источника, будут заполнены значением «0».")
        } else {
            def child = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, null, formData.comparativePeriodId, formData.accruing)
            if (child == null || child.state != WorkflowState.ACCEPTED) {
                // Не создана / Создана, но не принята
                def cause = (child == null ? "не создана" : "не находится в статусе «Принята»")
                def rowIndex = row.getIndex()
                def sourceName = getSourceFormType()?.name
                def departmentName = departmentService.get(departmentFormType.departmentId)?.name
                def kindName = departmentFormType.kind.name
                logger.warn("Строка $rowIndex: $kindName налоговая форма «$sourceName» в текущем периоде, подразделении «$departmentName» $cause! " +
                        "Графы текущей строки, заполняемые данными формы источника, будут заполнены значением «0».")
            }
        }
    }

    // 2. Проверка наличия налоговой формы текущего типа, вида и подразделения за предыдущий отчетный период в статусе «Принята»
    def prevFormData = getPrevFormData()
    if (prevFormData == null || prevFormData.state != WorkflowState.ACCEPTED) {
        def cause = (prevFormData == null ? 'не создан' : 'создан, но не находится в статусе «Принята»')
        def column13Name = getColumnName(dataRows[0], 'reservePrev')
        logger.warn("Не найден экземпляр формы текущего типа, вида и подразделения за предыдущий отчетный период ($cause). " +
                "Графа «$column13Name» (строка «Сбербанк России», строки по списанию безнадежных долго, строка по ЦА) не будет заполнена автоматически.")
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // обязательные для заполнения поля (графа 3..9, 12..17)
    // (тут наиболее часто требуемые графы, для разных строк разный состав обязательных граф)
    def nonEmptyColumns = allColumns - ['rowNum', 'bankName', 'sumIncome', 'normIncome']

    // 1. Проверка на заполнение полей: для строки 1 обязательные графы 3..17
    def rowBank = dataRows[0]
    def tmpNonEmptyColumns = nonEmptyColumns + ['sumIncome', 'normIncome']
    checkNonEmptyColumns(rowBank, rowBank.getIndex(), tmpNonEmptyColumns, logger, true)

    // 1. Проверка на заполнение полей: для строк 3..18, 20..23 обязательные графы 3..9, 12..17
    ((2..17) + (19..22)).each { i ->
        def row = dataRows[i]
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }

    // 1. Проверка на заполнение полей: для строк 25..44 обязательные графы 13, 14, 17
    (24..43).each { i ->
        def row = dataRows[i]
        tmpNonEmptyColumns = ['reservePrev', 'reserveCurrent', 'usingReserve']
        checkNonEmptyColumns(row, row.getIndex(), tmpNonEmptyColumns, logger, true)
    }

    // 1. Проверка на заполнение полей: для строки 45 обязательные графы 3, 5, 6, 8, 9, 12..17
    def rowCAWriteoff = dataRows[44]
    tmpNonEmptyColumns = nonEmptyColumns - ['norm45', 'norm90']
    checkNonEmptyColumns(rowCAWriteoff, rowCAWriteoff.getIndex(), tmpNonEmptyColumns, logger, true)


    // 2. Арифметическая проверка значений автоматически заполняемых граф
    def tmpDataRows = []
    // заполнить временные данные из dataRows
    dataRows.each { row ->
        def newTmpRow = formData.createDataRow()
        clearColumns.each { column ->
            newTmpRow[column] = (BigDecimal) row[column]
            newTmpRow.setAlias(row.getAlias())
        }
        tmpDataRows.add(newTmpRow)
    }
    def prevDataRows = getPrevDataRows()
    calcRows(tmpDataRows, prevDataRows)

    def arithmeticCheckAlias = clearColumns
    // номера строк для проверки
    for (def index : (0..44)) {
        // пропустить надписи
        if (index in [2, 19, 24]) {
            continue
        }
        def row = dataRows[index]
        def tmpRow = tmpDataRows[index]
        checkCalc(row, arithmeticCheckAlias, tmpRow, logger, true)
    }
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // очищаем форму
    clearNotEditableValues(dataRows)

    // задать нули в консолидируемые ячейки
    def concolidateRowIndexes = departmentRowMap.keySet().asList()
    def consolidationColumns = clearColumns - notConsolidationColumns
    concolidateRowIndexes.each { index ->
        def row = dataRows[index - 1]
        consolidationColumns.each { column ->
            row[column] = 0
        }
    }

    // получить данные из источников
    for (def departmentFormType : departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate())) {
        if (departmentFormType.formTypeId != sourceFormTypeId) {
            continue
        }
        def child = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, null, formData.comparativePeriodId, formData.accruing)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            def childRows = formDataService.getDataRowHelper(child).allSaved
            def rowTotal = getDataRow(childRows, 'total')
            def childDepartmentId = child.departmentId
            def entry = departmentRowMap.find { index, departmentId ->
                departmentId == childDepartmentId
            }
            if (entry?.key != null) {
                def rowIndex = entry.key.toInteger() - 1
                fillRow(dataRows[rowIndex], rowTotal)
            }
        }
    }
}

/**
 * Заполнить строку приемника данными из строки источника.
 *
 * @param row строка приемника
 * @param sourceRow строка источника
 */
void fillRow(def row, def sourceRow) {
    // графа 3 = графа 3
    row.sum45 = sourceRow.debt45_90DaysSum
    // графа 5 = графа 5
    row.reserve45 = sourceRow.debt45_90DaysReserve
    // графа 6 = графа 6
    row.sum90 = sourceRow.debtOver90DaysSum
    // графа 8 = графа 8
    row.reserve90 = sourceRow.debtOver90DaysReserve
    // графа 9 = графа 9
    row.totalReserve = sourceRow.totalReserve
    // графа 13 = графа 10
    row.reservePrev = sourceRow.reservePrev
    // графа 14 = графа 11
    row.reserveCurrent = sourceRow.reserveCurrent
    // графа 15 = графа 12
    row.addChargeReserve = sourceRow.calcReserve
    // графа 16 = графа 13
    row.restoreReserve = sourceRow.reserveRecovery
    // графа 17 = графа 14
    row.usingReserve = sourceRow.useReserve
}

def roundValue(def value, int precision = 0) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/** Получить тип формы источника. */
def getSourceFormType() {
    if (sourceFormType == null) {
        sourceFormType = formTypeService.get(sourceFormTypeId)
    }
    return sourceFormType
}

/** Получить форму предыдущего периода. */
def getPrevFormData() {
    if (prevFormData == null) {
        prevFormData = formDataService.getFormDataPrev(formData)
    }
    return prevFormData
}
/** Получить строки за предыдущий отчетный период. */
def getPrevDataRows() {
    if (prevDataRows == null) {
        def prevFormData = getPrevFormData()
        prevDataRows = (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allSaved : [])
    }
    return prevDataRows
}

void importData() {
    def tmpRow = formData.createDataRow()
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues)
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

    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // формирование строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[i]
        fileRowIndex++
        rowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            break
        }
        // прервать по загрузке нужных строк
        if (rowIndex > dataRows.size()) {
            break
        }
        // найти нужную строку нф
        def alias = "R" + rowIndex
        def dataRow = dataRows.find { it.getAlias() == alias }
        if (dataRow == null) {
            continue
        }
        // заполнить строку нф значениями из эксель
        fillRowFromXls(dataRow, rowValues, fileRowIndex, rowIndex, colOffset)
    }
    if (rowIndex < dataRows.size()) {
        logger.error("Структура файла не соответствует макету налоговой формы.")
    }
    showMessages(dataRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(dataRows)
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 */
void checkHeaderXls(def headerRows) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[1].size(), headerRows.size(), 4, 2)
    def headerMapping = [
            ([(headerRows[0][0]) : '№ п/п']),
            ([(headerRows[0][1]) : 'Наименование банка']),
            ([(headerRows[0][2]) : 'Задолженность от 45 до 90 дней']),
            ([(headerRows[0][5]) : 'Задолженность свыше 90 дней']),
            ([(headerRows[0][8]) : 'Итого расчетный резерв гр.9=гр.5+гр.8']),
            ([(headerRows[0][9]) : 'Сумма доходов за отчетный период']),
            ([(headerRows[0][10]): 'Норматив отчислений от суммы доходов 10%']),
            ([(headerRows[0][11]): 'Величина созданного резерва в отчетном периоде']),
            ([(headerRows[0][12]): 'Резерв']),
            ([(headerRows[0][14]): 'Изменение фактического резерва']),
            ([(headerRows[1][0]) : '№ п/п']),
            ([(headerRows[1][1]) : 'Наименование банка']),
            ([(headerRows[1][2]) : 'Сумма долга']),
            ([(headerRows[1][3]) : 'Норматив отчислений 50%']),
            ([(headerRows[1][4]) : 'Расчетный резерв']),
            ([(headerRows[1][5]) : 'Сумма долга']),
            ([(headerRows[1][6]) : 'Норматив отчислений 100%']),
            ([(headerRows[1][7]) : 'Расчетный резерв']),
            ([(headerRows[1][8]) : 'Итого расчетный резерв гр.9=гр.5+гр.8']),
            ([(headerRows[1][9]) : 'Сумма доходов за отчетный период']),
            ([(headerRows[1][10]): 'Норматив отчислений от суммы доходов 10%']),
            ([(headerRows[1][11]): 'Величина созданного резерва в отчетном периоде']),
            ([(headerRows[1][12]): 'на предыдущую отчетную дату']),
            ([(headerRows[1][13]): 'на отчетную дату']),
            ([(headerRows[1][14]): 'доначисление резерва с отнесением на расходы код 22670']),
            ([(headerRows[1][15]): 'восстановление резерва на доходах код 13091']),
            ([(headerRows[1][16]): 'использование резерва на погашение процентов по безнадежным долгам в отчетном периоде'])
    ]
    (0..16).each {
        headerMapping.add(([(headerRows[2][it]): (it + 1).toString()]))
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Заполняет заданную строку нф значениями из экселя.
 *
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
def fillRowFromXls(def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)

    def tmpValues = [:]

    def colIndex = 0
    tmpValues.rowNum = round(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true), 0)

    colIndex++
    tmpValues.bankName = values[colIndex]

    tmpValues.keySet().asList().each { alias ->
        def value = StringUtils.cleanString(tmpValues[alias]?.toString())
        def valueExpected = StringUtils.cleanString(dataRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
    }

    ['sum45', 'norm45', 'reserve45', 'sum90', 'norm90', 'reserve90', 'totalReserve', 'sumIncome', 'normIncome',
     'valueReserve', 'reservePrev', 'reserveCurrent', 'addChargeReserve', 'restoreReserve', 'usingReserve'].each {
        dataRow[it] = parseNumber(values[++colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
}