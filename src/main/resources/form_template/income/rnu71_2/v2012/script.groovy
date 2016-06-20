package form_template.income.rnu71_2.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-71.2) Регистр налогового учёта уступки права требования после предусмотренного кредитным договором срока погашения основного долга в отношении сделок уступки прав требования в пользу Взаимозависимых лиц и резидентов оффшорных зон"
 * formTypeId=503
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
// графа 16 - taxClaimPrice           Рыночная цена прав требования для целей налогообложения
// графа 17 - finResult               Финансовый результат, рассчитанный исходя из рыночной цены для целей налогообложения
// графа 18 - correctThisPrev         Корректировка финансового результата в отношении убытка, относящегося к расходам текущего налогового периода, но полученного в предыдущем налоговом периоде
// графа 19 - correctThisThis         Корректировка финансового результата в отношении убытка, относящегося к расходам текущего налогового периода и полученного в текущем налоговом периоде (прибыли, полученной в текущем налоговом периоде)
// графа 20 - correctThisNext         Корректировка финансового результата в отношении убытка, относящегося полученного в текущем налоговом периоде, но относящегося к расходам следующего налогового периода

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
        formDataService.saveCachedDataRows(formData, logger)
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
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        prevPeriodCheck()
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

// Все поля
@Field
def allColumns = ['rowNumber', 'fix', 'contragent', 'inn', 'assignContractNumber', 'assignContractDate',
        'amount', 'amountForReserve', 'repaymentDate', 'dateOfAssignment', 'income',
        'result', 'part2Date', 'lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod',
        'taxClaimPrice', 'finResult', 'correctThisPrev', 'correctThisThis', 'correctThisNext']

// Поля, для которых подсчитываются подитоговые значения (графа 10, 11, 13..15, 18..20)
@Field
def subTotalColumns = ['income', 'result', 'lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod',
        'correctThisPrev', 'correctThisThis', 'correctThisNext']

// Поля, для которых подсчитываются итоговые значения (графа 6, 7, 10, 11, 13..15, 18..20)
@Field
def totalColumns = ['amount', 'amountForReserve', 'income', 'result', 'lossThisQuarter', 'lossNextQuarter',
        'lossThisTaxPeriod', 'correctThisPrev', 'correctThisThis', 'correctThisNext']

// Редактируемые атрибуты (графа 2..10)
@Field
def editableColumns = ['contragent', 'inn', 'assignContractNumber', 'assignContractDate',
        'amount', 'amountForReserve', 'repaymentDate', 'dateOfAssignment', 'income', 'taxClaimPrice']

// Группируемые атрибуты
@Field
def groupColumns = ['contragent']

// Сортируемые атрибуты (графа 2, 5, 4)
@Field
def sortColumns = ['contragent', 'assignContractDate', 'assignContractNumber']

// Проверяемые на пустые значения атрибуты (графа 1..8, 10, 11, 16, 17, 19)
@Field
def nonEmptyColumns = ['contragent', 'inn', 'assignContractNumber', 'assignContractDate', 'amount', 'amountForReserve',
                       'repaymentDate', 'income', 'result', 'taxClaimPrice', 'finResult']

/** Признак периода ввода остатков. */
@Field
def isBalancePeriod = null

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

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
    if (!isBalancePeriod() && !isConsolidated) {
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
        if (row.dateOfAssignment < row.repaymentDate) {
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
            // графа 17
            values.finResult = calc17(values)
            // графа 18
            values.correctThisPrev = calc18(values, rowPrev, dFrom, dTo)
            // графа 19
            values.correctThisThis = calc19(values, dTo)
            // графа 20
            values.correctThisNext = calc20(values, dTo)

            // для арифметических проверок (графа 11, 13..15, 17..20)
            def arithmeticCheckAlias = ['result', 'lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod',
                    'finResult', 'correctThisPrev', 'correctThisThis', 'correctThisNext']
            checkCalc(row, arithmeticCheckAlias, values, logger, !isBalancePeriod())

            // арифметическая проверка графы 12 - сравнение дат отдельно, потому что checkCalc не подходит для нечисловых значений
            if (row.part2Date != values.part2Date) {
                loggerError(row, errorMsg + "Неверное значение графы ${getColumnName(row, 'part2Date')}!")
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
    def dataRows = formDataService.getDataRowHelper(formData).allCached

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

            row.finResult = calc17(row)
            row.correctThisPrev = calc18(row, rowPrev, dFrom, dTo)
            row.correctThisThis = calc19(row, dTo)
            row.correctThisNext = calc20(row, dTo)
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
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)

    // Сортировка групп и строк
    sortFormDataRows(false)
}

/** Сформировать итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('itg')
    newRow.fix = 'Всего'
    newRow.getCell('fix').colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = getSubTotal(i, 'Итого по ' + (dataRows.get(i).contragent ?: ''))

    // Расчеты подитоговых значений
    subTotalColumns.each {
        newRow[it] = 0
    }
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)

        subTotalColumns.each {
            newRow[it] += row[it] != null ? row[it] : 0
        }
    }

    return newRow
}

DataRow<Cell> getSubTotal(def int i, def title) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.getCell('fix').colSpan = 6
    newRow.fix = title
    newRow.setAlias('itg#'.concat(i.toString()))
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
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
        def formDataPrev = formDataService.getFormDataPrev(formData)
        dataRowsOld = (formDataPrev ? formDataService.getDataRowHelper(formDataPrev)?.allSaved : null)
    }
    return dataRowsOld
}

def BigDecimal calc17(def row) {
    if (row.taxClaimPrice != null && row.amount != null && row.amountForReserve != null) {
        return (row.taxClaimPrice - (row.amount - row.amountForReserve))
    }
    return null
}

def BigDecimal calc18(def row, def rowPrev, def startDate, def endDate) {
    if (row.dateOfAssignment != null && row.part2Date != null && row.result != null &&
            endDate != null && startDate != null &&
            row.dateOfAssignment < startDate && row.part2Date in (startDate..endDate) && row.result < 0) {
        return rowPrev.correctThisNext
    }
    return null
}

def BigDecimal calc19(def row, def endDate) {
    return calc19or20(row, endDate, true)
}

def BigDecimal calc20(def row, def endDate) {
    return calc19or20(row, endDate, false)
}

def BigDecimal calc19or20(def row, def endDate, boolean is19) {
    if (row.result == null || row.finResult == null) {
        return null
    }
    def tmp = null
    if (row.result < 0 && row.part2Date != null) {
        if (row.part2Date < endDate) {
            tmp = (is19 ? row.finResult.abs() - row.result.abs() : 0)
        } else if (row.part2Date > endDate) {
            tmp = row.finResult.abs() - row.result.abs() * 0.5
        }
    } else if (row.result >= 0 && row.finResult > row.result) {
        tmp = row.finResult - row.result
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
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
        formDataService.checkFormExistAndAccepted(formData.formType.id, formData.kind, formData.departmentId,
                formData.reportPeriodId, true, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

def loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

/**
 * Расчет значений графы 11..15.
 * Одинаковый для формы РНУ-71.1 и РНУ-71.2.
 * // TODO (Ramil Timerbaev) если вносятся изменения в этот метод, то надо поправить метод в РНУ-71.1
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
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 20, 1)
    addTransportData(xml)

    // TODO (Ramil Timerbaev) возможно надо поменять на общее сообщение TRANSPORT_FILE_SUM_ERROR
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRows = formDataService.getDataRowHelper(formData).allCached
        checkTotalSum(dataRows, totalColumns, logger, false)
    }
}

void addTransportData(def xml) {
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
        // графа 13..20
        ['lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod', 'taxClaimPrice', 'finResult', 'correctThisPrev',
                'correctThisThis', 'correctThisNext'].each { alias ->
            newRow[alias] = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
            xmlIndexCol++
        }

        rows.add(newRow)
    }

    def totalRow = formData.createDataRow()
    totalRow.setAlias('itg')
    totalRow.fix = 'Всего'
    totalRow.getCell('fix').colSpan = 2
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    rows.add(totalRow)

    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]
        // графа 6
        def xmlIndexCol = 6
        totalRow.amount = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 7
        xmlIndexCol = 7
        totalRow.amountForReserve = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 10
        xmlIndexCol = 10
        totalRow.income = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 11
        xmlIndexCol = 11
        totalRow.result = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 13
        xmlIndexCol = 13
        totalRow.lossThisQuarter = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 14
        xmlIndexCol = 14
        totalRow.lossNextQuarter = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 15
        xmlIndexCol = 15
        totalRow.lossThisTaxPeriod = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 18
        xmlIndexCol = 18
        totalRow.correctThisPrev = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 19
        xmlIndexCol = 19
        totalRow.correctThisThis = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
        // графа 20
        xmlIndexCol = 20
        totalRow.correctThisNext = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
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

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), getDataRow(dataRows, 'itg'), true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'itg'.equals(it.getAlias())}
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 21
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('rowNumber').column.name
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
    def subTotalRows = [:]

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
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Всего") {
            rowIndex++
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (rowValues[INDEX_FOR_SKIP].contains("Итого по ")) {
            rowIndex++
            def subTotal = getNewSubTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            rows.add(subTotal)
            subTotalRows[subTotal.fix] = subTotal

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // сравнение подитогов
    def onlySimpleRows = rows.findAll { it.getAlias() == null }
    // Добавить строки подитогов
    addAllAliased(onlySimpleRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> tmpRows) {
            return calcItog(i, tmpRows)
        }
    }, groupColumns)
    def onlySubTotalTmpRows = onlySimpleRows.findAll { it.getAlias() != null }
    onlySubTotalTmpRows.each { subTotalTmpRow ->
        def key = subTotalTmpRow.fix
        def subTotalRow = subTotalRows[key]
        compareTotalValues(subTotalRow, subTotalTmpRow, subTotalColumns, logger, false)
    }

    // сравнение итога
    def totalRow = getTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
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
            ([(headerRows[0][0]) : tmpRow.getCell('rowNumber').column.name]),
            ([(headerRows[0][2]) : tmpRow.getCell('contragent').column.name]),
            ([(headerRows[0][3]) : tmpRow.getCell('inn').column.name]),
            ([(headerRows[0][4]) : 'Договор цессии']),
            ([(headerRows[0][6]) : tmpRow.getCell('amount').column.name]),
            ([(headerRows[0][7]) : tmpRow.getCell('amountForReserve').column.name]),
            ([(headerRows[0][8]) : tmpRow.getCell('repaymentDate').column.name]),
            ([(headerRows[0][9]) : tmpRow.getCell('dateOfAssignment').column.name]),
            ([(headerRows[0][10]): tmpRow.getCell('income').column.name]),
            ([(headerRows[0][11]): tmpRow.getCell('result').column.name]),
            ([(headerRows[0][12]): tmpRow.getCell('part2Date').column.name]),
            ([(headerRows[0][13]): 'Убыток, относящийся к расходам']),
            ([(headerRows[0][16]): tmpRow.getCell('taxClaimPrice').column.name]),
            ([(headerRows[0][17]): tmpRow.getCell('finResult').column.name]),
            ([(headerRows[0][18]): 'Корректировка финансового результата в отношении убытка, относящегося']),
            ([(headerRows[1][4]) : 'Номер']),
            ([(headerRows[1][5]) : 'Дата']),
            ([(headerRows[1][13]): 'текущего квартала']),
            ([(headerRows[1][14]): 'следующего квартала']),
            ([(headerRows[1][15]): 'текущего отчётного (налогового) периода, но полученный в предыдущем квартале']),
            ([(headerRows[1][18]): 'к расходам текущего налогового периода, но полученного в предыдущем налоговом периоде']),
            ([(headerRows[1][19]): 'к расходам текущего налогового периода и полученного в текущем налоговом периоде (прибыли, полученной в текущем налоговом периоде)']),
            ([(headerRows[1][20]): 'полученного в текущем налоговом периоде, но относящегося к расходам следующего налогового периода']),
            ([(headerRows[2][0]): '1'])
    ]
    (2..20).each {
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
    def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
    columns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    (allColumns - columns).each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    // графа 2..4
    def int colIndex = 1
    ['contragent', 'inn', 'assignContractNumber'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 5
    colIndex++
    newRow.assignContractDate = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 6
    colIndex++
    newRow.amount = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 7
    colIndex++
    newRow.amountForReserve = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 8
    colIndex++
    newRow.repaymentDate = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 9
    colIndex++
    newRow.dateOfAssignment = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 10
    colIndex++
    newRow.income = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 11
    colIndex++
    newRow.result = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 12
    colIndex++
    newRow.part2Date = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 13..20
    ['lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod', 'taxClaimPrice', 'finResult', 'correctThisPrev',
            'correctThisThis', 'correctThisNext'].each { alias ->
        colIndex++
        newRow[alias] = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    }

    return newRow
}

/**
 * Получить новую подитоговую строку из файла.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewSubTotalRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    // графа fix
    def title = values[1]

    def newRow = getSubTotal(rowIndex - 1, title)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 10
    def colIndex = 10
    newRow.income = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 11
    colIndex = 11
    newRow.result = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 13..15
    colIndex = 12
    ['lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod'].each { alias ->
        colIndex++
        newRow[alias] = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    }

    // графа 18..20
    colIndex = 17
    ['correctThisPrev', 'correctThisThis', 'correctThisNext'].each { alias ->
        colIndex++
        newRow[alias] = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    }

    return newRow
}