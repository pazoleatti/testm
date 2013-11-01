package form_template.income.rnu71_1

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * Скрипт для РНУ-71.1
 * Форма "(РНУ-71.1) Регистр налогового учёта уступки права требования после предусмотренного кредитным договором срока погашения основного долга
 *  TODO графа 10
 *  @author bkinzyabulatov
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
def autoFillColumns = ["rowNumber", "result", "part2Date", "lossThisQuarter", "lossNextQuarter", "lossThisTaxPeriod"]

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

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            currentDate, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение xml с общими проверками
def getXML(def String startStr, def String endStr) {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        throw new ServiceException('Имя файла не должно быть пустым')
    }
    def is = ImportInputStream
    if (is == null) {
        throw new ServiceException('Поток данных пуст')
    }
    if (!fileName.endsWith('.xls')) {
        throw new ServiceException('Выбранный файл не соответствует формату xls!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }
    return xml
}

//// Некастомные методы
/**
 * Сравнивает по графам сумму строк и соответствующую им итоговую строку
 * @param rowsForSum список строк для определения сумм по графам
 * @param sumRow итоговая строка для проверки
 * @return
 */
def checkSumWithRow(def rowsForSum, def sumRow){
    def totalResults = [:]
    def isValid = true
    totalColumns.each { col ->
        totalResults.put(col, new BigDecimal(0))
    }
    for (def row : rowsForSum) {
        totalResults.keySet().each { col ->
            final cellValue = row.get(col)
            if (cellValue != null) {
                totalResults.put(col, totalResults.get(col) + cellValue)
            }
        }
    }
    //Оставил each если понадобится выдавать более сложные сообщения об ошибках
    totalResults.keySet().each { col ->
        if (totalResults[col] != sumRow[col]){
            isValid = false
        }
    }
    return isValid
}

/**
 * Хелпер для округления чисел
 * @param value
 * @param newScale
 * @return
 */
BigDecimal roundTo(BigDecimal value, int newScale) {
    if (value != null) {
        return value.setScale(newScale, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

//// Кастомные методы

// Логические проверки
void logicCheck(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def numbers = []

    def dFrom = reportPeriodService.getStartDate(formData.getReportPeriodId())?.time
    def dTo = reportPeriodService.getEndDate(formData.getReportPeriodId())?.time

    def rowNum = 0
    for (def DataRow row : dataRows){
        //проверка и пропуск итогов
        if (row?.getAlias()?.contains('itg')) {
            continue
        }
        rowNum++

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, false)

        def index = row.rowNumber
        def rowStart
        if (index != null) {
            rowStart = "В строке \"№ пп\" равной $index "
        } else {
            index = dataRows.indexOf(row) + 1
            rowStart = "В строке $index "
        }
        if (!(row.repaymentDate in (dFrom..dTo))){
            logger.error("${rowStart}дата совершения операции вне границ отчетного периода!")
        }
        if (row.rowNumber in numbers){
            logger.error("${rowStart}нарушена уникальность номера по порядку ${row.rowNumber}!")
        }else {
            numbers += row.rowNumber
        }
        if (row.income == 0 && row.lossThisQuarter == 0 && row.lossNextQuarter == 0){
            logger.error("${rowStart}все суммы по операции нулевые!")
        }
        if (row.dateOfAssignment < row.repaymentDate){
            logger.error("${rowStart}неверно указана дата погашения основного долга!")
        }
        if (row.amount > 0 && row.income > 0 && row.repaymentDate == null &&
                row.dateOfAssignment == null && row.lossThisTaxPeriod != null){
            logger.error("${rowStart}в момент уступки права требования «Графа 15» не заполняется!")
        }
        if (row.lossThisTaxPeriod > 0 &&
                ((row.amount == null && row.result != null) ||
                        row.lossThisQuarter != null ||
                        row.lossNextQuarter != null)){
            logger.error("${rowStart}в момент отнесения второй половины убытка на расходы графы кроме графы 15 и графы 12 не заполняются!")
        }

        def testRows = dataRows.findAll{ it -> it.getAlias() == null }

        def values = getValues(testRows, row, null, dFrom, dTo)
        for (def colName : autoFillColumns) {
            if (row[colName] != values[colName]) {
                isValid = false
                def columnName = row.getCell(colName).column.name
                logger.error("${rowStart}неверно рассчитана графа \"$columnName\"!")
            }
        }

        addAllAliased(testRows, new ScriptUtils.CalcAliasRow() {
            @Override
            DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
                return calcItog(i, rows)
            }
        }, groupColumns)
        // Рассчитанные строки итогов
        def testItogRows = testRows.findAll { it -> it.getAlias() != null }
        // Имеющиеся строки итогов
        def itogRows = dataRows.findAll { it -> it.getAlias() != null }

        checkItogRows(testRows, testItogRows, itogRows, groupColumns, logger, new ScriptUtils.GroupString() {
            @Override
            String getString(DataRow<Cell> dataRow) {
                return dataRow.contragent
            }
        }, new ScriptUtils.CheckGroupSum() {
            @Override
            String check(DataRow<Cell> row1, DataRow<Cell> row2) {
                if (row1.contragent != row2.contragent) {
                    return getColumnName(row1, 'contragent')
                }
                return null
            }
        })
    }
    def totalRow = dataRowHelper.getDataRow(dataRows, 'itg')
    def totalRowList = dataRows.findAll{ it -> it.getAlias() != null && it.getAlias() != 'itg' }
    if(!checkSumWithRow(totalRowList, totalRow)){
        logger.error("Итоговые значения рассчитаны неверно!")
    }
}

void calc(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def formDataPrev = reportPeriodPrev? formDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodPrev.id):null
    if (formDataPrev != null && formDataPrev.state != WorkflowState.ACCEPTED){
        formDataPrev = null
    }
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
    for(def row : dataRows) {
        if(row?.getAlias()?.contains('itg')){
            continue
        }
        getValues(dataRows, row, row, dFrom, dTo)
    }

    // Добавить строки итогов/подитогов
    addAllAliased(dataRows, new ScriptUtils.CalcAliasRow() {
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
    dataRows.add(dataRows.size(), totalRow)
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

/**
 * Получаем мапу со значениями, расчитанными для каждой конкретной строки или сразу записываем в строку (для расчетов)
 * @param dataRows
 * @param row исходная строка
 * @param resultRow результируемая строка в которую записывают значения (в режиме расчета)
 * @param startDate
 * @param endDate
 * @return карту со значениями (в режиме проверки)
 */
def getValues(def dataRows, def row, def resultRow, def startDate, def endDate) {
    if(resultRow == null){
        resultRow = [:]
    }
    def rowPrev = getRowPrev(row)
    resultRow.with {
        rowNumber = getGraph1(dataRows, row)
        result = getGraph11(row)
        part2Date = getGraph12(row)
        lossThisQuarter = getGraph13(row, endDate)
        lossNextQuarter = getGraph14(row, endDate)
        lossThisTaxPeriod = getGraph15(row, rowPrev, startDate, endDate)
    }
    return resultRow
}

def getRowPrev(def row){
    def prevDataRowHelper = formDataService.getDataRowHelper(formData)
    for (def rowPrev in prevDataRowHelper.allCached){
        if ((row.contragent == rowPrev.contragent &&
                row.inn == rowPrev.inn &&
                row.assignContractNumber == rowPrev.assignContractNumber &&
                row.assignContractDate == rowPrev.assignContractDate)){
            return rowPrev
        }
    }
}

def getGraph1(def dataRows, def row) {
    def i = 0
    for (def dataRow : dataRows){
        if (dataRow.getAlias()==null){
            i++
            if (row == dataRow){
                return i
            }
        }
    }
}

def getGraph11(def row) {
    return row.income - (row.amount - row.amountForReserve)
}

def getGraph12(def row) {
    return row.dateOfAssignment ?
        (row.dateOfAssignment + 45) :
        null //не заполняется
}

def getGraph13(def row, def endDate) {
    if(row.result < 0){
        if (row.part2Date <= endDate){
            return row.result
        }else{
            return row.result?roundTo(row.result * 0.5, 2):null
        }
    } else {
        return null //не заполняется
    }
}

def getGraph14(def row, def endDate) {
    if(row.result < 0){
        if (row.part2Date <= endDate){
            return 0
        }else{
            return row.result?roundTo(row.result * 0.5, 2):null
        }
    } else {
        return null //не заполняется
    }
}

def getGraph15(def row, def rowPrev, def startDate, def endDate) {
    def period = (startDate..endDate)
    if (!(row.dateOfAssignment in period) && (row.part2Date in period)){
        return rowPrev?.lossNextQuarter
    } else {
        return null //не заполняется
    }
}
