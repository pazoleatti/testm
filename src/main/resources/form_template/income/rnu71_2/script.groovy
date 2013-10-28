package form_template.income.rnu71_2

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * Скрипт для РНУ-71.2
 * Форма "(РНУ-71.2) Регистр налогового учёта уступки права требования после предусмотренного кредитным договором срока погашения основного долга в отношении сделок уступки прав требования в пользу Взаимозависимых лиц и резидентов оффшорных зон"
 *  TODO графа 10, 18
 *  @author bkinzyabulatov
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        allCheck()
        break
    case FormDataEvent.CALCULATE :
        if (preCalcCheck()){
            deleteTotal()
            sort()
            calcValue()
            addTotal()
        }
        allCheck()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED : // после принятия из подготовлена
        allCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        if (preCalcCheck()){
            deleteTotal()
            sort()
            calcValue()
            addTotal()
        }
        allCheck()
        break
}

/**
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
 * Графа 16 taxClaimPrice           Рыночная цена прав требования для целей налогообложения
 * Графа 17 correctThisPrev         Корректировка финансового результата в отношении убытка, относящегося к расходам текущего налогового периода, но полученного в предыдущем налоговом периоде
 * Графа 18 correctThisThis         Корректировка финансового результата в отношении убытка, относящегося к расходам текущего налогового периода и полученного в текущем налоговом периоде (прибыли, полученной в текущем налоговом периоде)
 * Графа 19 correctThisNext         Корректировка финансового результата в отношении убытка, относящегося полученного в текущем налоговом периоде, но относящегося к расходам следующего налогового периода
 **/

def allCheck() {
    return !hasError() && logicalCheck() && checkNSI()
}

boolean preCalcCheck(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = dataRowHelper.allCached

    //TODO раскомментировать
//    if(formDataPrev==null && !isBalancePeriod()){
//        logger.error("Не найдены экземпляры РНУ-71.1 за прошлый отчетный период!")
//        return false
//    }
    // проверить обязательные редактируемые поля
    for (def DataRow row : rows){
        if (!isTotal(row) && !checkRequiredColumns(row, requiredCols.intersect(editableCols))){
            return false
        }
    }
    return true
}

void deleteTotal(){
    def delRows = []
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = dataRowHelper.allCached
    rows.each { row ->
        if (isTotal(row)) {
            delRows += row
        }
    }
    rows.removeAll(delRows)
}

void sort(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = dataRowHelper.allCached
    // сортировка
    rows.sort({ DataRow a, DataRow b ->
        sortRow(sortCols, a, b)
    })
    dataRowHelper.save(rows)
}

void calcValue(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = dataRowHelper.allCached
    def startDate = reportPeriodService.getStartDate(formData.getReportPeriodId())?.getTime()
    def endDate = reportPeriodService.getEndDate(formData.getReportPeriodId())?.getTime()
    for(def row : rows) {
        if(isTotal(row)){
            continue
        }
        getValues(row, row, startDate, endDate)
    }
    dataRowHelper.update(rows)
}

/**
 * получаем мапу со значениями, расчитанными для каждой конкретной строки или сразу записываем в строку (для расчетов)
 */
def getValues(def row, def resultRow, def startDate, def endDate) {
    if(resultRow == null){
        resultRow = [:]
    }
    //TODO раскомментировать
    //def rowPrev = getRowPrev(row)
    def rowPrev
    resultRow.with {
        rowNumber = getGraph1(row)
        result = getGraph11(row)
        part2Date = getGraph12(row)
        lossThisQuarter = getGraph13(row, endDate)
        lossNextQuarter = getGraph14(row, endDate)
        lossThisTaxPeriod = getGraph15(row, rowPrev, startDate, endDate)
        taxClaimPrice = getGraph16(row)
        correctThisPrev = getGraph17(row, rowPrev, startDate, endDate)
        correctThisThis = getGraph18(row, endDate)
        correctThisNext = getGraph19(row, endDate)
    }
    return resultRow
}

def getRowPrev(def row){
    def prevDataRowHelper = formDataService.getDataRowHelper(formData)
    for (def rowPrev in prevDataRowHelper.allCached){
        if (isEqualsRow(row, rowPrev)){
            return rowPrev
        }
    }
}

def getGraph1(def row) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def i = 0
    for (def dataRow : dataRowHelper.allCached){
        if (!isTotal(dataRow)){
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
        return rowPrev.lossNextQuarter
    } else {
        return null //не заполняется
    }
}

def getGraph16(def row) {
    return row.lossThisTaxPeriod?:0 - (row.amount - row.amountForReserve)
}

def getGraph17(def row, def rowPrev, def startDate, def endDate) {
    if (row.dateOfAssignment < startDate && row.part2Date in (startDate..endDate)) {
        if (row.result < 0){
            return rowPrev.correctThisNext
        }
    }
    return null
}

//TODO уточнить
def getGraph18(def row, def endDate) {
    if (row.result < 0){
        if (row.part2Date < endDate) {
            return abs(row.taxClaimPrice) - abs(row.result)
        }
        if (row.part2Date > endDate) {
            return (abs(row.taxClaimPrice) - abs(row.result)) * 0.5
        }
    } else {
        if (row.taxClaimPrice > row.result) {
            return row.taxClaimPrice - row.result
        }
    }
}

def getGraph19(def row, def endDate) {
    if (row.result < 0){
        if (row.part2Date < endDate) {
            return 0
        }
        if (row.part2Date > endDate) {
            return (abs(row.taxClaimPrice) - abs(row.result)) * 0.5
        }
    } else {
        if (row.taxClaimPrice > row.result) {
            return null
        }
    }
}

/**
 * Проверяет две строки на "идентичность" по графам 2-5
 * @param rowOne первая сравниваемая строка
 * @param rowTwo вторая сравниваемая строка
 * @return
 */
boolean isEqualsRow(def rowOne, def rowTwo){
    return (rowOne.contragent == rowTwo.contragent &&
            rowOne.inn == rowTwo.inn &&
            rowOne.assignContractNumber == rowTwo.assignContractNumber &&
            rowOne.assignContractDate == rowTwo.assignContractDate)
}

void addTotal(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def name = null
    def totalRows = [:]
    def sums = [:]
    totalCols.each{
        sums[it] = 0
    }
    dataRowHelper.allCached.eachWithIndex { row, i ->
        if(name == null){
            name = row.contragent
        }
        // контрагент поменялся - создаем новую строку итогов
        if (name != row.contragent) {
            totalRows.put(i, getContragentTotalRow(name, totalCols, sums))
            totalCols.each {
                sums[it] = 0
            }
            name = row.contragent
        }
        if (i == dataRowHelper.allCached.size()-1) {
            totalCols.each {
                def value = row.getCell(it).value
                if (value != null) {
                    sums[it] += value
                }
            }
            totalRows.put(i + 1, getContragentTotalRow(name, totalCols, sums))
            totalCols.each {
                sums[it] = 0
            }
        }
        totalCols.each {
            def value = row.getCell(it).value
            if (value != null) {
                sums[it] += value
            }
        }
    }
    def i = 1
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.contragent = 'Итого'
    setTotalStyle(totalRow)
    totalRows.each { index, DataRow row ->
        dataRowHelper.insert(row, index + i++)
        totalCols.each {
            if (totalRow.getCell(it).value == null){
                totalRow.getCell(it).value = row.getCell(it).value
            } else {
                totalRow.getCell(it).value += row.getCell(it).value
            }
        }
    }
    dataRowHelper.insert(totalRow, dataRowHelper.allCached.size() + 1)
}

boolean logicalCheck(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def numbers = []
    def hasTotal = false
    def groups = [:]
    def totalRows = [:]
    def startDate = reportPeriodService.getStartDate(formData.getReportPeriodId())?.getTime()
    def endDate = reportPeriodService.getEndDate(formData.getReportPeriodId())?.getTime()
    for (def DataRow row : dataRowHelper.allCached){
        //проверка и пропуск итогов
        if (isTotal(row)) {
            if (row.getAlias() != getTotalRowAlias()) {
                hasTotal = true//итоги по контрагентам
                totalRows[row.getAlias()] = row
            }
            continue
        }
        if(!checkRequiredColumns(row,requiredCols)){
            return false
        }
        def rowStart = getRowIndexString(row)
        if (!(row.repaymentDate in (startDate..endDate))){
            logger.error("${rowStart}дата совершения операции вне границ отчетного периода!")
            return false
        }
        if (row.rowNumber in numbers){
            logger.error("${rowStart}нарушена уникальность номера по порядку ${row.number}!")
            return false
        }else {
            numbers += row.rowNumber
        }
        if (row.income == 0 && row.lossThisQuarter == 0 && row.lossNextQuarter == 0){
            logger.error("${rowStart}все суммы по операции нулевые!")
            return false
        }
        if (row.dateOfAssignment < row.repaymentDate){
            logger.error("${rowStart}неверно указана дата погашения основного долга!")
            return false
        }
        if (row.amount > 0 && row.income > 0 && row.repaymentDate == null &&
                row.dateOfAssignment == null && row.lossThisTaxPeriod != null){
            logger.error("${rowStart}в момент уступки права требования «Графа 15» не заполняется!")
            return false
        }
        if (row.lossThisTaxPeriod > 0 &&
                ((row.amount == null && row.result != null) ||
                    row.lossThisQuarter != null ||
                    row.lossNextQuarter != null)){
            logger.error("${rowStart}в момент отнесения второй половины убытка на расходы графы кроме графы 15 и графы 12 не заполняются!")
            return false
        }
        def values = getValues(row, null, startDate, endDate)
        for (def colName : calcCols) {
            if (row[colName] != values[colName]) {
                isValid = false
                def columnName = row.getCell(colName).column.name
                logger.error("${rowStart}неверно рассчитана графа \"$columnName\"!")
                return false
            }
        }
        if (groups[row.contragent] == null){
            groups[row.contragent] = []
        }
        groups[row.contragent].add(row)
    }
    if(hasTotal){
        def totalRow = getRowByAlias(dataRowHelper, 'total')
        def totalRowList = []
        //проходим по группам
        for (def alias : groups.keySet()){
            def contragentTotalRow = totalRows['total'+alias]
            if(contragentTotalRow == null || !checkSumWithRow(groups[alias], contragentTotalRow)){
                logger.error("Итоговые значения по контрагенту ${alias} рассчитаны неверно!")
            }
            if (contragentTotalRow != null) {
                totalRowList += contragentTotalRow
            }
        }
        if(!checkSumWithRow(totalRowList, totalRow)){
            logger.error("Итоговые значения рассчитаны неверно!")
        }
    }

}

def checkSumWithRow(def rowsForSum, def sumRow){
    def totalResults = [:]
    def isValid = true
    getTotalCols().each { col ->
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
    totalResults.keySet().each { col ->
        if (totalResults[col] != sumRow[col]){
            isValid = false
        }
    }
    return isValid
}

boolean checkNSI(){

}

int sortRow(List<String> params, DataRow a, DataRow b) {
    for (String param : params) {
        def aD = a.getCell(param).value
        def bD = b.getCell(param).value

        if (aD != bD) {
            return aD <=> bD
        }
    }
    return 0
}

/**
 * Проверить заполненность обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def DataRow row, def ArrayList<String> columns) {
    def colNames = []
    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = row.getCell(it).column.name
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def errorBegin = getRowIndexString(row)
        def errorMsg = colNames.join(', ')
        logger.error(errorBegin+ "не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Начало предупреждений/ошибок
 * @param row
 * @return
 */
def String getRowIndexString(def DataRow row){
    def index = row.rowNumber
    if (index != null) {
        return "В строке \"№ пп\" равной $index "
    } else {
        index = getIndex(row) + 1
        return "В строке $index "
    }
}

void addNewRow(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = dataRowHelper.allCached
    DataRow<Cell> newRow = getNewRow()
    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(isTotal(row) && index>0){
            row = rows.get(--index)
        }
        if(index!=currentDataRow.getIndex() && !isTotal(rows.get(index))){
            index++
        }
    }else if (rows.size()>0) {
        for(int i = rows.size()-1;i>=0;i--){
            def row = rows.get(i)
            if(!isTotal(row)){
                index = getIndex(row)+1
                break
            }
        }
    }
    dataRowHelper.insert(newRow,index+1)
}

void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && isBalancePeriod()) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

def isBalancePeriod(){
    return reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
}

void deleteRow(){
    if (!isTotal(currentDataRow)) {
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    // удалить все строки и собрать из источников их строки
    def rows = new LinkedList<DataRow<Cell>>()

    for (formDataSource in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind())) {
        if (formDataSource.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(formDataSource.formTypeId, formDataSource.kind, formDataSource.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceForm = formDataService.getDataRowHelper(source)
                for (row in sourceForm.allCached) {
                    if (row.getAlias() == null) {
                        rows.add(row)
                    }
                }
            }
        }
    }
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Получить новую строку с заданными стилями.
 */
def DataRow getNewRow() {
    def row = formData.createDataRow()
    editableCols.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

def List<String> getAllCols(){
    return ["rowNumber", "contragent", "inn", "assignContractNumber", "assignContractDate",
            "amount", "amountForReserve", "repaymentDate", "dateOfAssignment", "income",
            "result", "part2Date", "lossThisQuarter", "lossNextQuarter", "lossThisTaxPeriod",
            "taxClaimPrice", "correctThisPrev", "correctThisThis", "correctThisNext"]
}

def List<String> getEditableCols(){
    return ["contragent", "inn", "assignContractNumber", "assignContractDate", "amount",
            "amountForReserve", "repaymentDate", "dateOfAssignment", "income"]
}

def List<String> getRequiredCols(){
    return ["rowNumber", "contragent", "inn", "assignContractNumber", "assignContractDate",
            "amount", "amountForReserve", "repaymentDate", "dateOfAssignment", "income",
            "result", "taxClaimPrice", "correctThisThis"]
}

def List<String> getSortCols(){
    return ["contragent", "assignContractDate", "assignContractNumber"]
}

def List<String> getTotalCols(){
    return ["income", "result", "lossThisQuarter", "lossNextQuarter", "lossThisTaxPeriod",
            "correctThisPrev", "correctThisThis", "correctThisNext"]
}

def List<String> getCalcCols(){
    return ["rowNumber", "result", "part2Date", "lossThisQuarter", "lossNextQuarter", "lossThisTaxPeriod",
            "taxClaimPrice", "correctThisPrev", "correctThisThis", "correctThisNext"]
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formDataService.getDataRowHelper(formData).allCached.indexOf(row)
}

def FormData getFormDataPrev() {
    def reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def formDataPrev = reportPeriodPrev? formDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodPrev.id):null
    if (formDataPrev != null && formDataPrev.state == WorkflowState.ACCEPTED){
        return formDataPrev
    } else {
        return null
    }
}

def getTotalRowAlias(){
    return 'total'
}

def getContragentTotalRow(def alias, def totalColumns, def sums){
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    newRow.getCell('contragent').colSpan = 6
    newRow.contragent = "Итого по " + alias
    setTotalStyle(newRow)
    return newRow
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    allCols.each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
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

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 * @param alias алиас
 */
def getRowByAlias(def dataRowHelper, def alias) {
    return dataRowHelper.getDataRow(dataRowHelper.getAllCached(), alias)
}
