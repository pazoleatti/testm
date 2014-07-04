package form_template.income.rnu71_1.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import groovy.transform.Field

import java.math.RoundingMode

/**
 * (РНУ-71.1) Регистр налогового учёта уступки права требования после предусмотренного кредитным договором срока погашения основного долга
 * formTypeId=356
 *
 * @author bkinzyabulatov
 */

// графа 1  - rowNumber               № пп
// графа    - fix
// графа 2  - contragent              Наименование контрагента
// графа 3  - inn                     ИНН (его аналог)
// графа 4  - assignContractNumber    Договор цессии. Номер
// графа 5  - assignContractDate      Договор цессии. Дата
// графа 6  - amount                  Стоимость права требования
// графа 7  - amountForReserve        Стоимость права требования, списанного за счёт резервов
// графа 8  - repaymentDate           Дата погашения основного долга
// графа 9  - dateOfAssignment        Дата уступки права требования
// графа 10 - income                  Доход (выручка) от уступки права требования
// графа 11 - result                  Финансовый результат уступки права требования
// графа 12 - part2Date               Дата отнесения на расходы второй половины убытка
// графа 13 - lossThisQuarter         Убыток, относящийся к расходам текущего квартала
// графа 14 - lossNextQuarter         Убыток, относящийся к расходам следующего квартала
// графа 15 - lossThisTaxPeriod       Убыток, относящийся к расходам текущего отчётного (налогового) периода, но полученный в предыдущем квартале

@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
        formDataService.addRow(formData, currentDataRow, columns, (allColumns - columns))
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        prevPeriodCheck()
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
}

// Все поля
@Field
def allColumns = ['rowNumber', 'fix', 'contragent', 'inn', 'assignContractNumber', 'assignContractDate',
        'amount', 'amountForReserve', 'repaymentDate', 'dateOfAssignment', 'income',
        'result', 'part2Date', 'lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod']

// Поля, для которых подсчитываются подитоговые значения (графа 10, 11, 13..15)
@Field
def subTotalColumns = ['income', 'result', 'lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod']

// Поля, для которых подсчитываются итоговые значения (графа 6, 7, 10, 11, 13..15)
@Field
def totalColumns = ['amount', 'amountForReserve', 'income', 'result', 'lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod']

// Редактируемые атрибуты (графа 2..10)
@Field
def editableColumns = ['contragent', 'inn', 'assignContractNumber', 'assignContractDate',
        'amount', 'amountForReserve', 'repaymentDate', 'dateOfAssignment', 'income']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты
@Field
def groupColumns = ['contragent']

// Сортируемые атрибуты (графа 2, 5, 4)
@Field
def sortColumns = ['contragent', 'assignContractDate', 'assignContractNumber']

// Проверяемые на пустые значения атрибуты (графа 1..8, 10, 11)
@Field
def nonEmptyColumns = ['contragent', 'inn', 'assignContractNumber', 'assignContractDate', 'amount', 'amountForReserve',
                       'repaymentDate', 'income', 'result']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

// Признак периода ввода остатков
@Field
def isBalancePeriod = null

// Строки формы предыдущего периода
@Field
def dataRowsOld = null

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

/** Получить дату по строковому представлению (формата дд.ММ.гггг) */
def getDate(def value, def indexRow, def indexCol) {
    return parseDate(value, 'dd.MM.yyyy', indexRow, indexCol + 1, logger, true)
}

//// Кастомные методы
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()

    def dataRowsPrev = null
    if (!isConsolidated && !isBalancePeriod()) {
        dataRowsPrev = getDataRowsPrev()
    }

    for (def DataRow row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        // 2. Проверка даты совершения операции и границ отчетного периода
        if (!(row.repaymentDate in (dFrom..dTo))) {
            loggerError(row, errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }

        // 4. Проверка на нулевые значения
        if (row.income == 0 && row.lossThisQuarter == 0 && row.lossNextQuarter == 0) {
            loggerError(row, errorMsg + "Все суммы по операции нулевые!")
        }

        // 5. Проверка даты погашения основного долга
        if (row.dateOfAssignment != null && row.repaymentDate != null && row.dateOfAssignment < row.repaymentDate) {
            loggerError(row, errorMsg + "Неверно указана дата погашения основного долга!")
        }

        // 6. Проверка корректности заполнения графы 15
        if (row.amount > 0 && row.income > 0 && row.repaymentDate == null &&
                row.dateOfAssignment == null && row.lossThisTaxPeriod != null) {
            loggerError(row, errorMsg + "В момент уступки права требования «Графа 15» не заполняется!")
        }

        // 7. Проверка корректности заполнения граф РНУ при отнесении второй части убытка на расходы
        if (row.lossThisTaxPeriod > 0 &&
                ((row.amount == null && row.result == null) ||
                        row.lossThisQuarter != null ||
                        row.lossNextQuarter != null)) {
            loggerError(row, errorMsg + "В момент отнесения второй половины убытка на расходы графы кроме графы 15 и графы 12 не заполняются!")
        }

        // 8. Арифметические проверки граф
        if (!isConsolidated) {
            def rowPrev = getRowPrev(dataRowsPrev, row)
            // графа 11..15
            def values = getCalc11_15(row, rowPrev, dFrom, dTo)

            // для арифметических проверок (графа 11, 13..15)
            def arithmeticCheckAlias = ['result', 'lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod']
            checkCalc(row, arithmeticCheckAlias, values, logger, !isBalancePeriod())

            // арифметическая проверка графы 12 - сравнение дат отдельно, потому что checkCalc не подходит для нечисловых значений
            if (row.part2Date != values.part2Date){
                loggerError(row, errorMsg + "Неверное значение графы «${getColumnName(row, 'part2Date')}»!")
            }
        }
    }
    def testRows = dataRows.findAll { it -> it.getAlias() == null }

    // 9. Арифметическая проверка строк «Итого по контрагенту»
    addAllAliased(testRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int ind, List<DataRow<Cell>> rows) {
            return calcItog(ind, rows)
        }
    }, groupColumns)
    // Рассчитанные строки итогов
    def testItogRows = testRows.findAll { it -> it.getAlias() != null }
    // Имеющиеся строки итогов
    def itogRows = dataRows.findAll { it -> it.getAlias() != null }

    checkItogRows(testRows, testItogRows, itogRows, groupColumns, logger, new GroupString() {
        @Override
        String getString(DataRow<Cell> dataRow) {
            return dataRow.contragent
        }
    }, new CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            if (row1.contragent != row2.contragent) {
                return getColumnName(row1, 'contragent')
            }
            return null
        }
    })

    // 10. Арифметическая проверка итоговых значений по всей форм
    def sumRowList = dataRows.findAll { it -> it.getAlias() == null || it.getAlias() == 'itg' }
    checkTotalSum(sumRowList, totalColumns, logger, !isBalancePeriod())
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Сортировка
    sortRows(dataRows, sortColumns)

    if (!isBalancePeriod() && !isConsolidated) {
        def dataRowsPrev = getDataRowsPrev()
        def dFrom = getReportPeriodStartDate()
        def dTo = getReportPeriodEndDate()

        // Расчет ячеек
        for (def row : dataRows) {
            def rowPrev = getRowPrev(dataRowsPrev, row)
            def values = getCalc11_15(row, rowPrev, dFrom, dTo)

            // графа 11
            row.result = values.result
            // графа 12
            row.part2Date = values.part2Date
            // графа 13
            row.lossThisQuarter = values.lossThisQuarter
            // графа 14
            row.lossNextQuarter = values.lossNextQuarter
            // графа 15
            row.lossThisTaxPeriod = values.lossThisTaxPeriod
        }
    }

    // Добавить строки подитогов
    addAllAliased(dataRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)

    // Добавить строки итогов
    def totalRow = formData.createDataRow()
    totalRow.setAlias('itg')
    totalRow.fix = 'Всего'
    totalRow.getCell('fix').colSpan = 2
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)

    dataRowHelper.save(dataRows)
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('fix').colSpan = 6
    newRow.fix = 'Итого по ' + (dataRows.get(i).contragent ?: '')
    newRow.setAlias('itg#'.concat(i.toString()))
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }

    // Расчеты подитоговых значений
    subTotalColumns.each {
        newRow[it] = 0
    }
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)

        subTotalColumns.each {
            newRow[it] += (row[it] ?: 0)
        }
    }

    return newRow
}

def getRowPrev(def dataRowsPrev, def row) {
    for (def rowPrev in dataRowsPrev) {
        if ((row.contragent == rowPrev.contragent &&
                row.inn == rowPrev.inn &&
                row.assignContractNumber == rowPrev.assignContractNumber &&
                row.assignContractDate == rowPrev.assignContractDate)) {
            return rowPrev
        }
    }
}

def getDataRowsPrev() {
    if (dataRowsOld == null) {
        def formDataPrev = formDataService.getFormDataPrev(formData, formData.departmentId)
        dataRowsOld = (formDataPrev ? formDataService.getDataRowHelper(formDataPrev)?.allCached : null)
    }
    return dataRowsOld
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков.
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

def loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNumber'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 15, 2)

    def headerMapping = [
            (xml.row[0].cell[0]) : getColumnName(tmpRow, 'rowNumber'),
            (xml.row[0].cell[2]) : getColumnName(tmpRow, 'contragent'),
            (xml.row[0].cell[3]) : getColumnName(tmpRow, 'inn'),
            (xml.row[0].cell[4]) : 'Договор цессии',
            (xml.row[0].cell[6]) : getColumnName(tmpRow, 'amount'),
            (xml.row[0].cell[7]) : getColumnName(tmpRow, 'amountForReserve'),
            (xml.row[0].cell[8]) : getColumnName(tmpRow, 'repaymentDate'),
            (xml.row[0].cell[9]) : getColumnName(tmpRow, 'dateOfAssignment'),
            (xml.row[0].cell[10]): getColumnName(tmpRow, 'income'),
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'result'),
            (xml.row[0].cell[12]): getColumnName(tmpRow, 'part2Date'),
            (xml.row[0].cell[13]): 'Убыток, относящийся к расходам',

            (xml.row[1].cell[4]) : 'Номер',
            (xml.row[1].cell[5]) : 'Дата',
            (xml.row[1].cell[13]): 'текущего квартала',
            (xml.row[1].cell[14]): 'следующего квартала',
            (xml.row[1].cell[15]): 'текущего отчётного (налогового) периода, но полученный в предыдущем квартале',
            (xml.row[2].cell[0]) : '1'
    ]
    (2..15).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 3)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []

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

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setImportIndex(xlsIndexRow)
        def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
        columns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        (allColumns - columns).each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        xmlIndexCol++

        // графа fix
        xmlIndexCol++

        // графа 2
        newRow.contragent = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 3
        newRow.inn = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 4
        newRow.assignContractNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 5
        newRow.assignContractDate = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 6
        newRow.amount = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 7
        newRow.amountForReserve = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 8
        newRow.repaymentDate = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 9
        newRow.dateOfAssignment = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 10
        newRow.income = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графы рассчитываемые, однако не при импорте в консолидированную - должны загружаться
        // графа 11
        newRow.result = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 12
        newRow.part2Date = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 13
        newRow.lossThisQuarter = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 14
        newRow.lossNextQuarter = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 15
        newRow.lossThisTaxPeriod = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

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

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
void prevPeriodCheck() {
    if (!isConsolidated && !isBalancePeriod()) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId, true, logger, true)
    }
}

/**
 * Расчет значений графы 11..15.
 * Одинаковый для формы РНУ-71.1 и РНУ-71.2.
 * // TODO (Ramil Timerbaev) если вносятся изменения в этот метод, то надо поправить метод в РНУ-71.2
 * Возвращет значения в виде мапы, доступ к значению графы по алиасу графы.
 *
 * @param row строка текущей формы
 * @param rowPrev строка формы за предыдуший период
 * @param startDate дата начала периода
 * @param endDate дата оконачания периода
 * @return мапа со значениями графов
 */
def getCalc11_15(def row, def rowPrev, def startDate, def endDate) {
    def values = [:]

    row.each { key, value ->
        values[key] = value
    }
    // графа 11
    def tmp = null
    if (values.income != null && values.amount != null && values.amountForReserve != null) {
         tmp = (values.income - (values.amount - values.amountForReserve))
    }
    values.result = tmp?.setScale(2, RoundingMode.HALF_UP)

    // графа 12
    values.part2Date = (values.dateOfAssignment ? values.dateOfAssignment + 45 : null) //не заполняется

    // графа 13
    tmp = null
    if (values.result != null && values.result < 0) {
        if (values.part2Date != null && endDate != null) {
            if (values.part2Date <= endDate) {
                tmp = values.result
            } else {
                tmp = values.result * 0.5
            }
        }
    }
    values.lossThisQuarter = tmp?.setScale(2, RoundingMode.HALF_UP)

    // графа 14
    tmp = null
    if (values.result != null && values.result < 0 && values.part2Date != null && endDate != null) {
        if (values.part2Date <= endDate) {
            tmp = BigDecimal.ZERO
        } else {
            tmp = values.result * 0.5
        }
    }
    values.lossNextQuarter = tmp?.setScale(2, RoundingMode.HALF_UP)

    // графа 15
    tmp = null
    if (startDate != null && endDate != null && values.dateOfAssignment != null && values.part2Date != null) {
        def period = (startDate..endDate)
        if (!(values.dateOfAssignment in period) && (values.part2Date in period)) {
            tmp = rowPrev?.lossNextQuarter
        }
    }
    values.lossThisTaxPeriod = tmp?.setScale(2, RoundingMode.HALF_UP)

    return values
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName)
    addTransportData(xml)

    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
        columns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        (allColumns - columns).each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def int xmlIndexCol = 2

        // графа 2
        newRow.contragent = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 3
        newRow.inn = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 4
        newRow.assignContractNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 5
        newRow.assignContractDate = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 6
        newRow.amount = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 7
        newRow.amountForReserve = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 8
        newRow.repaymentDate = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 9
        newRow.dateOfAssignment = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 10
        newRow.income = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 11
        newRow.result = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 12
        newRow.part2Date = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 13
        newRow.lossThisQuarter = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 14
        newRow.lossNextQuarter = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++
        // графа 15
        newRow.lossThisTaxPeriod = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        rows.add(newRow)
    }

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()
        total.setAlias('itg')
        total.fix = 'Всего'
        total.getCell('fix').colSpan = 2
        allColumns.each {
            total.getCell(it).setStyleAlias('Контрольные суммы')
        }
        // графа 6
        def xmlIndexCol = 6
        total.amount = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 7
        xmlIndexCol = 7
        total.amountForReserve = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 10
        xmlIndexCol = 10
        total.income = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 11
        xmlIndexCol = 11
        total.result = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 13
        xmlIndexCol = 13
        total.lossThisQuarter = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 14
        xmlIndexCol = 14
        total.lossNextQuarter = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 15
        xmlIndexCol = 15
        total.lossThisTaxPeriod = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        rows.add(total)
    }

    dataRowHelper.save(rows)
}