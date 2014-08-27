package form_template.income.rnu38_1.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Форма "РНУ-38.1" "Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 1".
 * formTemplateId=334
 *
 * @author ivildanov
 *
 * 1  -  series
 * 2  -  amount
 * 3  -  shortPositionDate
 * 4  -  maturityDate
 * 5  -  incomeCurrentCoupon
 * 6  -  currentPeriod
 * 7  -  incomePrev
 * 8  -  incomeShortPosition
 * 9  -  totalPercIncome
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
}

// Все атрибуты
@Field
def allColumns = ['series', 'amount', 'shortPositionDate', 'maturityDate', 'incomeCurrentCoupon', 'currentPeriod', 'incomePrev', 'incomeShortPosition', 'totalPercIncome']

// Редактируемые атрибуты (графа 1..6)
@Field
def editableColumns = ['series', 'amount', 'shortPositionDate', 'maturityDate', 'incomeCurrentCoupon', 'currentPeriod']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты (графа 1)
@Field
def groupColumns = ['series']

// Проверяемые на пустые значения атрибуты (графа 1..6, 9)
@Field
def nonEmptyColumns = ['series', 'amount', 'shortPositionDate', 'maturityDate', 'incomeCurrentCoupon', 'currentPeriod', 'totalPercIncome']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 2, 7..9)
@Field
def totalColumns = ['amount', 'incomePrev', 'incomeShortPosition', 'totalPercIncome']

// Дата окончания отчетного периода
@Field
def endDate = null

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

/** Получить дату по строковому представлению (формата дд.ММ.гггг) */
def getDate(def value, def indexRow, def indexCol) {
    return parseDate(value, 'dd.MM.yyyy', indexRow, indexCol + 1, logger, true)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached

    // алиасы графов для арифметической проверки (графа 7..9)
    def arithmeticCheckAlias = ['incomePrev', 'incomeShortPosition', 'totalPercIncome']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    // отчетная дата
    def reportDay = reportPeriodService.getMonthReportDate(formData.reportPeriodId, formData.periodOrder)?.time
    // последний день месяца
    def lastDay = getMonthEndDate()

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        //  2. Проверка даты открытия короткой позиции
        if (row.shortPositionDate > reportDay) {
            rowError(logger, row, errorMsg + 'Неверно указана дата приобретения (открытия короткой позиции)!')
        }

        // 3. Проверка даты погашения
        if (row.maturityDate > reportDay) {
            rowError(logger, row, errorMsg + 'Неверно указана дата погашения предыдущего купона!')
        }

        // 4. Арифметические проверки графы 7..9
        needValue['incomePrev'] = calc7(row, lastDay)
        needValue['incomeShortPosition'] = calc8(row, lastDay)
        needValue['totalPercIncome'] = calc9(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
    }

    // 4. Проверка итоговых значений по всей форме
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    // последний день месяца
    def lastDay = getMonthEndDate()
    for (def row : dataRows) {
        // графа 7
        row.incomePrev = calc7(row, lastDay)
        // графа 8
        row.incomeShortPosition = calc8(row, lastDay)
        // графа 9
        row.totalPercIncome = calc9(row)
    }
    // добавить строки "итого"
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

/**
 * Посчитать значение графы 7.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def calc7(def row, def lastDay) {
    if (row.maturityDate == null || row.shortPositionDate == null || row.incomeCurrentCoupon == null ||
            lastDay == null || row.currentPeriod == null || row.currentPeriod == 0 || row.amount == null) {
        return null
    }
    if (row.maturityDate > row.shortPositionDate) {
        def tmp = roundValue((row.incomeCurrentCoupon * (lastDay - row.maturityDate) / row.currentPeriod), 2)
        return roundValue(tmp * row.amount, 2)
    }
    return null
}

/**
 * Посчитать значение графы 8.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def calc8(def row, def lastDay) {
    if (row.maturityDate == null || row.shortPositionDate == null || row.incomeCurrentCoupon == null ||
            lastDay == null || row.currentPeriod == null || row.currentPeriod == 0 || row.amount == null) {
        return null
    }
    if (row.maturityDate <= row.shortPositionDate) {
        def tmp = roundValue((row.incomeCurrentCoupon * (lastDay - row.shortPositionDate) / row.currentPeriod), 2)
        return roundValue(tmp * row.amount, 2)
    }
    return null
}

def calc9(def row) {
    return roundValue((row.incomePrev ?: 0) + (row.incomeShortPosition ?: 0), 2)
}

/** Получить итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.series = 'Итого'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

def getMonthEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

// Получение импортируемых данных
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    def xml = getXML(ImportInputStream, importService, fileName, 'Серия', 'Итого')

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 9, 2)

    def headerMapping = [
            (xml.row[0].cell[0]) : 'Серия',
            (xml.row[0].cell[1]) : 'Количество, шт.',
            (xml.row[0].cell[2]) : 'Дата открытия короткой позиции',
            (xml.row[0].cell[3]) : 'Дата погашения предыдущего купона',
            (xml.row[0].cell[4]) : 'Объявленный доход по текущему купону, руб.коп.',
            (xml.row[0].cell[5]) : 'Текущий купонный период, дней',
            (xml.row[0].cell[6]) : 'Доход с даты погашения предыдущего купона, руб.коп.',
            (xml.row[0].cell[7]) : 'Доход с даты открытия короткой позиции, руб.коп.',
            (xml.row[0].cell[8]) : 'Всего процентный доход, руб.коп.'
    ]

    (1..9).each { index ->
        headerMapping.put((xml.row[1].cell[index - 1]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = 10 // Смещение для индекса колонок в ошибках импорта
    def int colOffset = 0 // Смещение для индекса колонок в ошибках импорта

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formDataService.addRow(formData, null, editableColumns, null)
        newRow.setImportIndex(xlsIndexRow)

        // графа 1
        newRow.series = row.cell[0].text()
        // графа 2
        def xmlIndexCol = 1
        newRow.amount = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 3
        xmlIndexCol = 2
        newRow.shortPositionDate = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 4
        xmlIndexCol = 3
        newRow.maturityDate = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 5
        xmlIndexCol = 4
        newRow.incomeCurrentCoupon = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 6
        xmlIndexCol = 5
        newRow.currentPeriod = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        dataRows.add(newRow)
    }
    dataRowHelper.save(dataRows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 9, 1)

    // загрузить данные
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    def int rnuIndexRow = 2
    def int colOffset = 1

    for (def row : xml.row) {
        rnuIndexRow++

        def rnuIndexCol
        def newRow = formData.createDataRow()
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).styleAlias = 'Редактируемая'
        }

        // графа 1
        newRow.series = row.cell[1].text()
        // графа 2
        rnuIndexCol = 2
        newRow.amount = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 3
        rnuIndexCol = 3
        newRow.shortPositionDate = getDate(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 4
        rnuIndexCol = 4
        newRow.maturityDate = getDate(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 5
        rnuIndexCol = 5
        newRow.incomeCurrentCoupon = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 6
        rnuIndexCol = 6
        newRow.currentPeriod = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 7
        rnuIndexCol = 7
        newRow.incomePrev = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 8
        rnuIndexCol = 8
        newRow.incomeShortPosition = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 9
        rnuIndexCol = 9
        newRow.totalPercIncome = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        dataRows.add(newRow)
    }

    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)

    // сравнение итогов
    if (xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()


        // графа 2
        def rnuIndexCol = 2
        total.amount = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 7
        rnuIndexCol = 7
        total.incomePrev = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 8
        rnuIndexCol = 8
        total.incomeShortPosition = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)
        // графа 9
        rnuIndexCol = 9
        total.totalPercIncome = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        def colIndexMap = ['amount' : 2, 'incomePrev' : 7, 'incomeShortPosition' : 8, 'totalPercIncome' : 9]
        for (def alias : totalColumns) {
            def v1 = total.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.error(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
                break
            }
        }

    }
    dataRowHelper.save(dataRows)
}