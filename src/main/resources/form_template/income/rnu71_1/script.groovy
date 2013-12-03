package form_template.income.rnu71_1

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
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

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW :
        if (!currentDataRow?.getAlias()?.contains('itg')) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        calc()
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
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
def allColumns = ["rowNumber", "contragent", "inn", "assignContractNumber", "assignContractDate",
            "amount", "amountForReserve", "repaymentDate", "dateOfAssignment", "income",
            "result", "part2Date", "lossThisQuarter", "lossNextQuarter", "lossThisTaxPeriod"]

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ["income", "result", "lossThisQuarter", "lossNextQuarter", "lossThisTaxPeriod"]

// Редактируемые атрибуты
@Field
def editableColumns = ["contragent", "inn", "assignContractNumber", "assignContractDate",
        "amount", "amountForReserve", "repaymentDate", "dateOfAssignment", "income"]

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ["result", "lossThisQuarter", "lossNextQuarter", "lossThisTaxPeriod"]

// Группируемые атрибуты
@Field
def groupColumns = ['contragent']

@Field
def sortColumns = ["contragent", "assignContractDate", "assignContractNumber"]

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ["rowNumber", "contragent", "inn", "assignContractNumber", "assignContractDate",
        "amount", "amountForReserve", "repaymentDate", "dateOfAssignment", "income",
        "result"]

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Некастомные методы
/**
 * Сравнивает по графам сумму строк и соответствующую им итоговую строку
 * @param rowsForSum список строк для определения сумм по графам
 * @param sumRow итоговая строка для проверки
 * @return
 */
def checkSumWithRow(def rowsForSum, def sumRow){
    def totalResults = formData.createDataRow()
    def isValid = true
    calcTotalSum(rowsForSum, totalResults, totalColumns)
    //Оставил each если понадобится выдавать более сложные сообщения об ошибках
    for (col in totalColumns) {
        if (totalResults[col] != sumRow[col]) {
            isValid = false
            break
        }
    }
    return isValid
}

//// Кастомные методы
void logicCheck(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def dFrom = reportPeriodService.getStartDate(formData.getReportPeriodId())?.time
    def dTo = reportPeriodService.getEndDate(formData.getReportPeriodId())?.time
    def formDataPrev = formDataService.getFormDataPrev(formData, formData.departmentId)
    formDataPrev = formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null
    // Номер последний строки предыдущей формы
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')
    for (def DataRow row : dataRows){
        //проверка и пропуск итогов
        if (row?.getAlias()?.contains('itg')) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        def values = [:]
        def rowPrev = getRowPrev(formDataPrev?.allCached, row)
        values.with {
            result = getGraph11(row)
            part2Date = getGraph12(row)
            lossThisQuarter = getGraph13(row, dTo)
            lossNextQuarter = getGraph14(row, dTo)
            lossThisTaxPeriod = getGraph15(row, rowPrev, dFrom, dTo)
        }
        checkCalc(row, autoFillColumns, values, logger, true)

        if (row.part2Date != values.part2Date){
            logger.error(errorMsg + "Неверное значение графы ${getColumnName(row, 'part2Date')}!")
        }
        if (!(row.repaymentDate in (dFrom..dTo))){
            logger.error(errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }
        if (++i != row.rowNumber) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }
        if (row.income == 0 && row.lossThisQuarter == 0 && row.lossNextQuarter == 0){
            logger.error(errorMsg + "Все суммы по операции нулевые!")
        }
        if (row.dateOfAssignment < row.repaymentDate){
            logger.error(errorMsg + "Неверно указана дата погашения основного долга!")
        }
        if (row.amount > 0 && row.income > 0 && row.repaymentDate == null &&
                row.dateOfAssignment == null && row.lossThisTaxPeriod != null){
            logger.error(errorMsg + "В момент уступки права требования «Графа 15» не заполняется!")
        }
        if (row.lossThisTaxPeriod > 0 &&
                ((row.amount == null && row.result != null) ||
                        row.lossThisQuarter != null ||
                        row.lossNextQuarter != null)){
            logger.error(errorMsg + "В момент отнесения второй половины убытка на расходы графы кроме графы 15 и графы 12 не заполняются!")
        }
    }
    def testRows = dataRows.findAll{ it -> it.getAlias() == null }

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
    def totalRow = getDataRow(dataRows, 'itg')
    def sumRowList = dataRows.findAll{ it -> it.getAlias() == null}
    if(!checkSumWithRow(sumRowList, totalRow)){
        logger.error("Итоговые значения рассчитаны неверно!")
    }
}

void calc(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def formDataPrev = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    formDataPrev = formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null
    if(formDataPrev==null && !reportPeriodService.isBalancePeriod(reportPeriodPrev.id, formData.departmentId)){
        logger.error("Не найдены экземпляры РНУ-71.1 за прошлый отчетный период!")
        return
    }

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Сортировка
    sortRows(dataRows, sortColumns)

    def dFrom = reportPeriodService.getStartDate(formData.getReportPeriodId())?.time
    def dTo = reportPeriodService.getEndDate(formData.getReportPeriodId())?.time

    // Номер последний строки предыдущей формы
    def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    // Расчет ячеек
    for(def row : dataRows) {
        def rowPrev = getRowPrev(formDataPrev?.allCached, row)
        row.with {
            rowNumber = ++index
            result = getGraph11(row)
            part2Date = getGraph12(row)
            lossThisQuarter = getGraph13(row, dTo)
            lossNextQuarter = getGraph14(row, dTo)
            lossThisTaxPeriod = getGraph15(row, rowPrev, dFrom, dTo)
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
    totalRow.contragent = 'Итого'
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    totalColumns.each {
        totalRow[it] = 0
    }
    for(def row : dataRows) {
        if(row?.getAlias()?.contains('itg')){
            continue
        }
        totalColumns.each {
            totalRow[it] += row[it] != null ? row[it] : 0
        }
    }
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('contragent').colSpan = 6
    newRow.contragent = 'Итого по ' + dataRows.get(i).contragent
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

def getRowPrev(def dataRowsPrev, def row){
    if (dataRowsPrev != null) {
        for (def rowPrev in dataRowsPrev){
            if ((row.contragent == rowPrev.contragent &&
                    row.inn == rowPrev.inn &&
                    row.assignContractNumber == rowPrev.assignContractNumber &&
                    row.assignContractDate == rowPrev.assignContractDate)){
                return rowPrev
            }
        }
    }
}

def getGraph11(def row) {
    if (row.income != null && row.amount != null && row.amountForReserve != null) {
        return (row.income - (row.amount - row.amountForReserve)).setScale(2, RoundingMode.HALF_UP)
    }
}

def getGraph12(def row) {
    return row.dateOfAssignment ? (row.dateOfAssignment + 45) : null //не заполняется
}

def getGraph13(def row, def endDate) {
    def tmp
    if(row.result != null && row.result < 0){
        if (row.part2Date != null && endDate != null) {
            if (row.part2Date <= endDate){
                tmp = row.result
            }else{
                tmp = row.result * 0.5
            }
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

def getGraph14(def row, def endDate) {
    def tmp
    if(row.result != null && row.result < 0){
        if (row.part2Date != null && endDate != null) {
            if (row.part2Date <= endDate){
                tmp = BigDecimal.ZERO
            }else{
                tmp = row.result * 0.5
            }
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

def getGraph15(def row, def rowPrev, def startDate, def endDate) {
    def tmp
    if (startDate != null && endDate != null) {
        def period = (startDate..endDate)
        if (row.dateOfAssignment != null && row.part2Date != null && !(row.dateOfAssignment in period) && (row.part2Date in period)){
            tmp = rowPrev?.lossNextQuarter
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}
