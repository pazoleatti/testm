package form_template.income.rnu71_1.v1970

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

import java.math.RoundingMode

/**
 * (РНУ-71.1) Регистр налогового учёта уступки права требования после предусмотренного кредитным договором срока погашения основного долга
 * formTemplateId=356
 * TODO графа 10
 * @author bkinzyabulatov
 *
 * Графа 1  rowNumber               № пп
 * Графа 2  contragent              Наименование контрагента
 * Графа 3  inn                     ИНН (его аналог)
 * Графа 4  assignContractNumber    Договор цессии. Номер
 * Графа 5  assignContractDate      Договор цессии. Дата
 * Графа 6  amount                  Стоимость права требования
 * Графа 7  amountForReserve        Стоимость права требования, списанного за счёт резервов
 * Графа 8  repaymentDate           Дата погашения основного долга
 * Графа 9  dateOfAssignment        Дата уступки права требования
 * Графа 10 income                  Доход (выручка) от уступки права требования
 * Графа 11 result                  Финансовый результат уступки права требования
 * Графа 12 part2Date               Дата отнесения на расходы второй половины убытка
 * Графа 13 lossThisQuarter         Убыток, относящийся к расходам текущего квартала
 * Графа 14 lossNextQuarter         Убыток, относящийся к расходам следующего квартала
 * Графа 15 lossThisTaxPeriod       Убыток, относящийся к расходам текущего отчётного (налогового) периода, но полученный в предыдущем квартале
 */

/** Признак периода ввода остатков. */
@Field
def isBalancePeriod
isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
        formDataService.addRow(formData, currentDataRow, columns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (!currentDataRow?.getAlias()?.contains('itg')) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
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
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Все поля
@Field
def allColumns = ['rowNumber', 'fix', 'contragent', 'inn', 'assignContractNumber', 'assignContractDate',
        'amount', 'amountForReserve', 'repaymentDate', 'dateOfAssignment', 'income',
        'result', 'part2Date', 'lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod']

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ['income', 'result', 'lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod']

// Редактируемые атрибуты
@Field
def editableColumns = ['contragent', 'inn', 'assignContractNumber', 'assignContractDate',
        'amount', 'amountForReserve', 'repaymentDate', 'dateOfAssignment', 'income']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['result', 'lossThisQuarter', 'lossNextQuarter', 'lossThisTaxPeriod']

// Группируемые атрибуты
@Field
def groupColumns = ['contragent']

@Field
def sortColumns = ['contragent', 'assignContractDate', 'assignContractNumber']

// TODO Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'contragent', 'inn', 'assignContractNumber', 'assignContractDate',
        'amount', 'amountForReserve', 'repaymentDate', 'income', 'result']

//// Кастомные методы
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def dFrom = reportPeriodService.getStartDate(formData.getReportPeriodId())?.time
    def dTo = reportPeriodService.getEndDate(formData.getReportPeriodId())?.time

    def dataRowsPrev
    if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
        dataRowsPrev = getDataRowsPrev()
    }

    // Номер последний строки предыдущей формы
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    for (def DataRow row : dataRows) {
        //проверка и пропуск итогов
        if (row?.getAlias()?.contains('itg')) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        if (formData.kind == FormDataKind.PRIMARY) {
            def values = [:]
            def rowPrev = getRowPrev(dataRowsPrev, row)
            values.with {
                result = calc11(row)
                part2Date = calc12(row)
                lossThisQuarter = calc13(row, dTo)
                lossNextQuarter = calc14(row, dTo)
                lossThisTaxPeriod = calc15(row, rowPrev, dFrom, dTo)
            }
            checkCalc(row, autoFillColumns, values, logger, !isBalancePeriod())

            if (row.part2Date != values.part2Date){
                loggerError(errorMsg + "Неверное значение графы ${getColumnName(row, 'part2Date')}!")
            }
        }
        if (!(row.repaymentDate in (dFrom..dTo))) {
            loggerError(errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }
        if (++i != row.rowNumber) {
            loggerError(errorMsg + 'Нарушена уникальность номера по порядку!')
        }
        if (row.income == 0 && row.lossThisQuarter == 0 && row.lossNextQuarter == 0) {
            loggerError(errorMsg + "Все суммы по операции нулевые!")
        }
        if (row.dateOfAssignment != null && row.dateOfAssignment < row.repaymentDate) {
            loggerError(errorMsg + "Неверно указана дата погашения основного долга!")
        }
        if (row.amount > 0 && row.income > 0 && row.repaymentDate == null &&
                row.dateOfAssignment == null && row.lossThisTaxPeriod != null) {
            loggerError(errorMsg + "В момент уступки права требования «Графа 15» не заполняется!")
        }
        if (row.lossThisTaxPeriod > 0 &&
                ((row.amount == null && row.result != null) ||
                        row.lossThisQuarter != null ||
                        row.lossNextQuarter != null)) {
            loggerError(errorMsg + "В момент отнесения второй половины убытка на расходы графы кроме графы 15 и графы 12 не заполняются!")
        }
    }
    def testRows = dataRows.findAll { it -> it.getAlias() == null }

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

    // Номер последний строки предыдущей формы
    def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
        def dataRowsPrev = getDataRowsPrev()
        def dFrom = reportPeriodService.getStartDate(formData.getReportPeriodId())?.time
        def dTo = reportPeriodService.getEndDate(formData.getReportPeriodId())?.time

        // Расчет ячеек
        for (def row : dataRows) {
            def rowPrev = getRowPrev(dataRowsPrev, row)
            row.with {
                rowNumber = ++index
                result = calc11(row)
                part2Date = calc12(row)
                lossThisQuarter = calc13(row, dTo)
                lossNextQuarter = calc14(row, dTo)
                lossThisTaxPeriod = calc15(row, rowPrev, dFrom, dTo)
            }
        }
    } else {
        for (def row : dataRows) {
            row.rowNumber = ++index
        }
    }

    // Добавить строки итогов/подитогов
    addAllAliased(dataRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)
    def totalRow = formData.createDataRow()
    totalRow.setAlias('itg')
    totalRow.fix = 'Итого'
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
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)

        totalColumns.each {
            sums[it] += row[it] != null ? row[it] : 0
        }
    }

    totalColumns.each {
        newRow[it] = sums[it]
    }
    return newRow
}

def getRowPrev(def dataRowsPrev, def row) {
    if (dataRowsPrev != null) {
        for (def rowPrev in dataRowsPrev) {
            if ((row.contragent == rowPrev.contragent &&
                    row.inn == rowPrev.inn &&
                    row.assignContractNumber == rowPrev.assignContractNumber &&
                    row.assignContractDate == rowPrev.assignContractDate)) {
                return rowPrev
            }
        }
    }
}

def getDataRowsPrev() {
    def formDataPrev = formDataService.getFormDataPrev(formData, formData.departmentId)
    formDataPrev = formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null
    if (formDataPrev == null) {
        logger.error("Не найдены экземпляры РНУ-71.1 за прошлый отчетный период!")
    } else {
        return formDataService.getDataRowHelper(formDataPrev)?.allCached
    }
    return null
}

def BigDecimal calc11(def row) {
    if (row.income != null && row.amount != null && row.amountForReserve != null) {
        return (row.income - (row.amount - row.amountForReserve)).setScale(2, RoundingMode.HALF_UP)
    }
}

def Date calc12(def row) {
    return row.dateOfAssignment ? (row.dateOfAssignment + 45) : null //не заполняется
}

def BigDecimal calc13(def row, def endDate) {
    def tmp
    if (row.result != null && row.result < 0) {
        if (row.part2Date != null && endDate != null) {
            if (row.part2Date <= endDate) {
                tmp = row.result
            } else {
                tmp = row.result * 0.5
            }
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

def BigDecimal calc14(def row, def endDate) {
    def tmp
    if (row.result != null && row.result < 0) {
        if (row.part2Date != null && endDate != null) {
            if (row.part2Date <= endDate) {
                tmp = BigDecimal.ZERO
            } else {
                tmp = row.result * 0.5
            }
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

def BigDecimal calc15(def row, def rowPrev, def startDate, def endDate) {
    def tmp
    if (startDate != null && endDate != null) {
        def period = (startDate..endDate)
        if (row.dateOfAssignment != null && row.part2Date != null && !(row.dateOfAssignment in period) && (row.part2Date in period)) {
            tmp = rowPrev?.lossNextQuarter
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков.
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

def loggerError(def msg) {
    if (isBalancePeriod()) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 15, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Наименование контрагента',
            (xml.row[0].cell[3]): 'ИНН (его аналог)',
            (xml.row[0].cell[4]): 'Договор цессии',
            (xml.row[0].cell[6]): 'Стоимость права требования',
            (xml.row[0].cell[7]): 'Стоимость права требования, списанного за счёт резервов',
            (xml.row[0].cell[8]): 'Дата погашения основного долга',
            (xml.row[0].cell[9]): 'Дата уступки права требования',
            (xml.row[0].cell[10]): 'Доход (выручка) от уступки права требования',
            (xml.row[0].cell[11]): 'Финансовый результат уступки права требования',
            (xml.row[0].cell[12]): 'Дата отнесения на расходы второй половины убытка',
            (xml.row[0].cell[13]): 'Убыток, относящийся к расходам',
            (xml.row[1].cell[4]): 'Номер',
            (xml.row[1].cell[5]): 'Дата',
            (xml.row[1].cell[13]): 'текущего квартала',
            (xml.row[1].cell[14]): 'следующего квартала',
            (xml.row[1].cell[15]): 'текущего отчётного (налогового) периода, но полученный в предыдущем квартале',
            (xml.row[2].cell[0]): '1'
    ]
    (2..15).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount) {
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
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
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
        newRow.assignContractDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 6
        newRow.amount = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 7
        newRow.amountForReserve = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 8
        newRow.repaymentDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 9
        newRow.dateOfAssignment = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 10
        newRow.income = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}